package transbit.tbits.upgrade;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.plugin.PluginHelper;
import transbit.tbits.plugin.PluginModule;

/*
 * This class provides functions to upgrade DB
 */
public class UpgradeDB {

	private static final String SYSTYPE = "systype";
	private static final String TAKEDBBACKUP_CMD = "takedbbackup";
	private static final String BACKUP_CMD = "backup";
	private static final String UPGRADE_CMD = "upgrade";
	private static final String LISTSCRIPTS_CMD = "listscripts";
	private static final String DBVERSION_CMD = "dbversion";
	private static final String GUESSVERSION_CMD = "guessversion";
	private static final String VERSION_CMD = "version";
	
//	private static final String DMS = "dms";
	private static final String COMMON = "common";
	private static final String TBITS = "tbits";
	private static final String DB_UPGRADES_FOLDER = "db/upgrades";
	private static final String DB_TYPE_NAME = "mssql";

	/*
	 * Upgrades the DB and returns the final point to which the upgradation has
	 * happened. This should ensure that the things that are in a folder should
	 * be run atomically.
	 */
	public static void upgrade(String moduleName, File dbUpgradesF) throws Throwable {
		if ((moduleName == null) || (moduleName.length() == 0))
			moduleName = TBITS;

		String takeBackUpStr = System.getProperty(TAKEDBBACKUP_CMD);
		boolean takeBackUp = true;
		try
		{
			if(takeBackUpStr != null)
				takeBackUp = Boolean.parseBoolean(takeBackUpStr);
		}
		catch(Exception e)
		{}
		// TAKE A BACKUP
		if(takeBackUp)
			DBUtils.backUpDB();
		else
			System.out.println("Skipping the backup.");
		

		//Maintain the current versions table - create or upgrade
		//Upgrade the current version table
		upgradeCurrentVersionsTable();
		
		AppUpgradRegistry appUpgradeReg = AppUpgradRegistry.get();

		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
		
			
			VersionInfo currentVersionInfo = getOverAllVersion(
					moduleName, conn);

			System.out
					.println("The current version is (modulename, major, minor): "
							+ currentVersionInfo.getSysType() + ", "
							+ currentVersionInfo.getMajor() + ", "
							+ currentVersionInfo.getMinor());
			String currentVersion = currentVersionInfo.getMajor();

			// Get the various upgrade folders
			ArrayList<String> versionFolders = getFoldersAfter(currentVersion, dbUpgradesF);
			if(versionFolders.size() > 0)
				System.out.println("Using the version folder versions: "
					+ versionFolders);
			else
				System.out.println("The database is up-to-date.");
			
			for (String folder : versionFolders) {
				System.out.println("Upgrading using: " + folder);

				// Just before upgrading
				for (IUpgrade upgradeApp : appUpgradeReg.getUpgradeAppBefore(
						folder, currentVersionInfo.getSysType())) {
					upgradeApp.upgrade(conn, folder, currentVersionInfo
							.getSysType());
				}

				ArrayList<SQLScriptFile> sqlScripts = getSQLFileForFolder(
						folder, currentVersionInfo.getSysType(), dbUpgradesF);
				System.out
						.println("Found the following scripts: " + sqlScripts);
				upgradeDBWithSQL(conn, sqlScripts);

				// Mark the version of current database
				System.out.println("Updating to the version: " + folder);
				MarkDbAsVersion(conn, folder, currentVersionInfo.getSysType());

				System.out.println("Calling the App Upgrades.");
				for (IUpgrade upgradeApp : appUpgradeReg.getUpgradeAppAfter(
						folder, currentVersionInfo.getSysType())) {
					System.out.println("Executing: " + upgradeApp.getClass().getName());
					upgradeApp.upgrade(conn, folder, currentVersionInfo
							.getSysType());
				}
				System.out.println("Committing...");
				// commit as soon as one version is upgraded
				conn.commit();
				System.out.println("Upgraded to " + folder);
			}
			System.out.println("Finished!");
		} catch (Exception e) {
			try {
				e.printStackTrace();
				System.out.println("Rolling back.");
				if (conn != null)
					conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
				throw e1;
			}
			throw e;
		}catch (Throwable t){
			try {
				System.out.println("Rolling back.");
				if (conn != null)
					conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
				throw e1;
			}
			throw t;
		} finally {
			if (conn != null) {
				try {
					System.out.println("Closing the connection.");
					conn.close();
					System.out.println("Closed the connection.Now you can terminate the application if it doesn't happen automatically.");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * Upgrades the current version table.
	 * @return true if upgrade was successful
	 */
	private static boolean upgradeCurrentVersionsTable() {
		Connection conn = null;
		try
		{
			conn = DataSourcePool.getConnection();
			upgradeCurrentVersionsTable(conn);
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			if(conn != null)
			{
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private static void upgradeCurrentVersionsTable(Connection conn) throws SQLException {
		if(DBUtils.tableExists("current_version"))
		{
			if(DBUtils.tableContainsColumn("current_version", "systype"))
			{
				// Create a new column modulename and delete the old one. 
				// Also keep only a single entry with the module name as tbits.
				// Also increase the size of major column to 100
				Statement stmt = conn.createStatement();
				stmt.addBatch("ALTER TABLE dbo.current_version ADD	modulename nchar(100) NULL");
				stmt.addBatch("ALTER TABLE dbo.current_version	DROP COLUMN systype");
				stmt.addBatch("select top 1 major, minor  into #tmp from current_version");
				stmt.addBatch("delete from current_version");
				stmt.addBatch("insert into current_version (modulename, major,minor) select 'tbits' modulename, major, minor from #tmp");
				stmt.addBatch("alter table current_version alter column major VARCHAR(100)");
				stmt.addBatch("drop table #tmp");
				stmt.executeBatch();
			}
		}
		else
		{
			Statement stmt = conn.createStatement();
			
			//create current version table
			String sql =  "CREATE TABLE [dbo].[current_version]("
			+ "	[major] [nchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,"
			+ "	[minor] [nchar](10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,"
			+ "	[modulename] [nchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL"
			+ ") ON [PRIMARY]";
			stmt.addBatch(sql);
			
			//sql = "insert into current_version (modulename, major, minor) values ('tbits', '6.0.24', '2031')";
			//stmt.addBatch(sql);
			stmt.executeBatch();
		}
	}
	public static VersionInfo getOverAllVersion(String suggestedSysType)
	{
		Connection conn = null;
		try
		{
			conn = DataSourcePool.getConnection();
			return getOverAllVersion(suggestedSysType, conn);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			if(conn != null)
			{
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	public static VersionInfo getOverAllVersion(String moduleName,
			Connection conn) throws SQLException, Exception {
		// Get the current Version from Database
		VersionInfo currentVersionInfo = getCurrentDBVersion(conn, moduleName);
		if (currentVersionInfo == null) {
			System.out.println("Could not get the current version number.");
			// return;
		}
		
		if(moduleName.equals(TBITS)){
			// This will be the minimal version
			VersionInfo sixtyVersion = new VersionInfo(moduleName,
					"6.0.65", "");
			if ((currentVersionInfo != null)
					&& (currentVersionInfo.compareTo(sixtyVersion) <= 0)) {
				currentVersionInfo = null;
			}
			
			if (currentVersionInfo == null) {
				int lastVersion = guessVersion();
				currentVersionInfo = new VersionInfo(moduleName, "6.0."
						+ lastVersion, "");
			}
		}
		
		if(currentVersionInfo == null)
			currentVersionInfo = new VersionInfo(moduleName, "0.0.0", "");
		
		return currentVersionInfo;
	}

	private static void upgradeDBWithSQL(Connection conn,
			ArrayList<SQLScriptFile> sqlScripts) throws SQLException,
			IOException {
		if(sqlScripts == null)
			return;
		Statement stmt = conn.createStatement();

		for (SQLScriptFile sqlFile : sqlScripts) {
			try {
				String scriptContents = FileUtils.getContents(sqlFile.getFile())
						.replaceAll("^GO$", "");
				System.out.println("------- The script: "
						+ sqlFile.getFile().getAbsolutePath()
						+ "----------");
				Pattern pattern = Pattern.compile("^GO$",
						Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

				for (String piece : pattern.split(scriptContents)) {
					System.out.println(piece);
					System.out.println("GO");

					if (piece.trim().length() == 0) {
						System.out.println("Skipping...");
						continue;
					}
					System.out.println("Executing...");
					boolean isResultSet = stmt.execute(piece);
					if (isResultSet) {
						do {
							ResultSet rs = stmt.getResultSet();
							DBUtils.print(rs);
						} while (stmt.getMoreResults());
					} else {
						System.out.println("Updated "
								+ stmt.getUpdateCount() + " rows.");
					}
				}
				System.out.println("*****************************");
				// stmt.addBatch(scriptContents);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			}
		}
	}

	public static int guessVersion()
			throws SQLException, Exception {
		int lastVersion;
		if(DBUtils.stpExists("stp_role_delete"))
		{
			lastVersion = 63;
		}
		else if (DBUtils.tableExists("gadgets")) {
			lastVersion = 60;
		} else if (DBUtils.tableExists("file_repo_index")) {
			lastVersion = 59;
		} else if (DBUtils.tableExists("ba_menu_table")) {
			lastVersion = 58;
		} else if (DBUtils.stpExists("stp_transmittal_getMaxTransmittalId")){
//				&& suggestedSysType.equals(DMS)) {
			lastVersion = 56;
		} else if (DBUtils.stpExists("stp_ba_incrAndGetRequestId")) {
			lastVersion = 56;
		} else if (DBUtils.stpExists("stp_admin_delete_escalation_condition")) {
			lastVersion = 55;
		} else if (DBUtils.tableExists("reports")) {
			lastVersion = 53;
		} else if (DBUtils.stpExists("stp_ba_insert")) {
			lastVersion = 51;
		} else if (DBUtils.stpExists("stp_ba_caption_insert")) {
			lastVersion = 47;
		} else if (DBUtils.stpExists("stp_request_lookupBySystemIdAndParentId")) {
			lastVersion = 44;
		} else if (DBUtils.tableExists("transmittal_templates")){
//				&& suggestedSysType.equals(DMS)) {
			lastVersion = 43;
		} else if (DBUtils.stpExists("stp_wr_insert")) {
			lastVersion = 40;
		} else if (DBUtils.stpExists("stp_ba_insert")) {
			lastVersion = 38;
		} else if (DBUtils.tableExists("captions_properties")) {
			lastVersion = 36;
		} else {
			throw new Exception(
					"The system can not upgrade automatically below 36.");
		}
		return lastVersion;
	}

	private static void MarkDbAsVersion(Connection conn, String newVersion, String moduleName)
			throws SQLException {
		VersionInfo curVersion = getCurrentDBVersion(conn, moduleName);
		if (curVersion != null) {
			System.out.println("Updating current version of " + moduleName + " to " + newVersion);
			PreparedStatement stmt = conn
					.prepareStatement("update current_version set major = ? where modulename=?");
			stmt.setString(1, newVersion);
			stmt.setString(2, moduleName);
			
			stmt.execute();
		} else {
			System.out.println("inserting current version of " + moduleName + " to " + newVersion);
			PreparedStatement stmt = conn
			.prepareStatement("insert into current_version (major, modulename) Values (?, ?)");
			stmt.setString(1, newVersion);
			stmt.setString(2, moduleName);
			
			stmt.execute();
//			System.out
//					.println("System doesnt have a database version. Please create a row in database with major as "
//							+ newVersion);
		}
	}

	public static ArrayList<String> getFoldersAfter(String version, File dbUpgradesF) {
		ArrayList<String> output = new ArrayList<String>();
		if ((dbUpgradesF != null) && (dbUpgradesF.exists())) {
			String[] dirs = dbUpgradesF.list();
			for (String child : dirs) {
				if (child.endsWith(".ex") || child.endsWith(".svn"))
					continue;
				if (VersionInfo.compareMajors(child, version) > 0) {
					output.add(child);
				}
			}
		}
		
		Collections.sort(output, new Comparator<String>(){

			public int compare(String o1, String o2) {
				return VersionInfo.compareMajors(o1, o2);
			}});
		
		return output;
	}

	public static ArrayList<SQLScriptFile> getSQLFileForFolder(String folderName,
			String sysType, File dbUpgradesF) {
		String sqlFolder = folderName + "/" + DB_TYPE_NAME;
		File sqlFolderF = new File(dbUpgradesF, sqlFolder);
		if (sqlFolderF.exists()) {
			ArrayList<SQLScriptFile> all = new ArrayList<SQLScriptFile>();

			File commonF = new File(sqlFolderF, COMMON);
			if (commonF.exists()) {
				File[] commonFiles = commonF.listFiles(new SQLFileFilter());
				if (commonFiles != null) {
					for (File f : commonFiles) {
						all.add(new SQLScriptFile(f.getName(), f));
					}
				}
			} else
				System.out.println("Can not locate: "
						+ commonF.getAbsolutePath());

			File sysTypeF = new File(sqlFolderF, sysType);

			if (sysTypeF.exists()) {
				File[] sysFiles = sysTypeF.listFiles(new SQLFileFilter());
				if (sysFiles != null) {
					for (File f : sysFiles) {
						all.add(new SQLScriptFile(f.getName(), f));
					}
				}
			} else
				System.out.println("Can not locate: "
						+ sysTypeF.getAbsolutePath());
			Collections.sort(all);
			return all;
		}
		return new ArrayList<SQLScriptFile>();
	}

	public static void listScripts(String moduleName, File dbUpgradesF)
	{
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			VersionInfo currentVersionInfo = getOverAllVersion(
					moduleName, conn);
			ArrayList<String> versionFolders = getFoldersAfter(currentVersionInfo
					.getMajor(), dbUpgradesF);
			for (String folder : versionFolders) {

				ArrayList<SQLScriptFile> sqlScripts = getSQLFileForFolder(
						folder, currentVersionInfo.getSysType(), dbUpgradesF);
				System.out
						.println("Found the following scripts: " + sqlScripts);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	public static VersionInfo getCurrentDBVersion(String moduleName)
	{
		Connection conn = null;
		try
		{
			conn = DataSourcePool.getConnection();
			return getCurrentDBVersion(conn, moduleName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			if(conn != null)
			{
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static VersionInfo getCurrentDBVersion(Connection connection, String moduleName)
			throws SQLException {
		ArrayList<VersionInfo> allVersions = getAllDBVersions(connection, moduleName);
		if (allVersions.size() <= 0)
			return null;
		Collections.sort(allVersions);
		return allVersions.get(allVersions.size() - 1);
	}

	public static ArrayList<VersionInfo> getAllDBVersions(Connection connection, String moduleName)
			throws SQLException {
		if (connection == null)
			throw new NullPointerException("Connection shouldnt be null");

		ArrayList<VersionInfo> versions = new ArrayList<VersionInfo>();

		String colName = "modulename";
		String sql = "select * from current_version where  modulename= '" + moduleName + "'";
		if(!DBUtils.tableContainsColumn("current_version", "modulename"))
		{
			sql = "select top 1 * from current_version ";
			colName = "systype";
		}
		Statement stmt = connection.createStatement();
		
		ResultSet rs = stmt.executeQuery(sql);
		while (rs.next()) {
			String module = rs.getString(colName);
			String major = rs.getString("major");
			String minor = rs.getString("minor");
			
			if(module != null)
				module = module.trim();
			if(major != null)
				major = major.trim();
			if(minor != null)
				minor = minor.trim();

			versions.add(new VersionInfo(module, major, minor));
		}
		return versions;
	}
	static String[] validArgs = new String[]{VERSION_CMD, GUESSVERSION_CMD, DBVERSION_CMD, LISTSCRIPTS_CMD, UPGRADE_CMD, BACKUP_CMD};
	
	public static void main(String[] args) {
//		String systype = System.getProperty(SYSTYPE);
//		if((systype == null) || (systype.trim().length() == 0))
//		{
//			systype = "request";
//		}
		if(args.length == 0)
		{
			System.out.println("Invalid arguments");
			showHelp();
			return;
		}
		for(String s:args)
		{
			File dbUpgradesF = Configuration.findPath(DB_UPGRADES_FOLDER);
			List<PluginModule> plugins = PluginHelper.getPluginModules(Configuration.findPath("jaguarsource/plugins"));
			
			if(s.equals(VERSION_CMD))
			{
				System.out.println("Overall Version: " + getOverAllVersion(TBITS));
			}
			else if(s.equals(GUESSVERSION_CMD))
			{
				try {
					System.out.println("Guessed Version : " + guessVersion());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(s.equals(DBVERSION_CMD))
			{
				System.out.println("DB Version: " + getCurrentDBVersion(TBITS));
			}
			else if(s.equals(BACKUP_CMD))
			{
				DBUtils.backUpDB();
			}
			else if(s.equals(LISTSCRIPTS_CMD))
			{
				listScripts(TBITS, dbUpgradesF);
			}
			else if(s.equals(UPGRADE_CMD))
			{
				try {
					upgrade(TBITS, dbUpgradesF);
					
					for(PluginModule plugin : plugins){
						if(plugin.getName() != null && plugin.getDBUpgradesDir() != null){
							try {
								upgrade(plugin.getName(), new File(plugin.getDBUpgradesDir()));
							} catch (Exception e) {
								e.printStackTrace();
							}catch (Throwable e) {
								e.printStackTrace();
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				showHelp();
				break;
			}
		}
	}
	public static void showHelp()
	{
		System.out.println("Please provide at least one of the following as argument.");
		for(String s:validArgs)
		{
			System.out.println(s);
		}
		System.out.println("The following environment variables are supported. You can pass these as -Dkey=value.");
		System.out.println(DBUtils.DBBACKUPFILE_CMD + ": the location of backup file");
		System.out.println(TAKEDBBACKUP_CMD + ": whether to take backup of not. By default true");
		System.out.println(SYSTYPE + ": The package type for e.g. dms, request, kms. The default is request.");
	}
}

class SQLFileFilter implements FilenameFilter {
	public boolean accept(File dir, String name) {
		if (name.endsWith(".sql") && new File(dir, name).isFile()) {
			return true;
		}
		return false;
	}
}
