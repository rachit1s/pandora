package transbit.tbits.upgrade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.ConnectionProperties;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.PropertiesEnum;

public class DBUtils {

	public static final String DBBACKUPFILE_CMD = "dbbackupfile";

	public static boolean stpExists(String stpName) throws SQLException {
		String query = "SELECT count(*) FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].["
				+ stpName + "]') AND type in (N'P', N'PC')";
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement(query);
			// ps.setString(1, stpName);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				if (rs.getInt(1) > 0) {
					return true;
				}
			}
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return false;
	}

	public static boolean tableExists(String tableName) throws SQLException {
		String query = "SELECT count(*) FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].["
				+ tableName + "]') AND type in (N'U')";
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement(query);
			// ps.setString(1, tableName);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				if (rs.getInt(1) > 0) {
					return true;
				}
			}
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return false;
	}

	public static void print(ResultSet rs) {
		// TODO Auto-generated method stub
		
	}

	public static void backUpDB() {
		String dbBackUpFile = System.getProperty(DBBACKUPFILE_CMD);
		if(dbBackUpFile == null)
		{
			String buildDir = Configuration.findAbsolutePath("");
			File parentDir = new File(buildDir + "/" + "Backup");
			
			if(!parentDir.exists())
				parentDir.mkdirs();
			
			File f = new File(parentDir, "tbits.bak");
			dbBackUpFile = f.getAbsolutePath();
		}
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
		String driverUrl = ConnectionProperties.getDBPoolProperty("driverURL");
		String dbName = driverUrl.substring(driverUrl.lastIndexOf('/') + 1);
		System.out.println("Starting backup of '" + dbName + "' database into the file '" + dbBackUpFile + "'");
		String query = "BACKUP DATABASE [" + dbName + "] TO  DISK = N'"
			+ dbBackUpFile 
			+"' WITH NOFORMAT, NOINIT,  NAME = N'tbits-Full Database Backup', SKIP, NOREWIND, NOUNLOAD,  STATS = 10";
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(query);
		System.out.println("Backup to '" + dbBackUpFile + "' finished.");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
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

	public static boolean tableContainsColumn(String tableName, String colName) throws SQLException {
		String query = "select count(*) from sys.columns where Name = N'" + colName + "' and Object_ID = Object_ID(N'" + tableName + "')";
	Connection connection = null;
	try {
		connection = DataSourcePool.getConnection();
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			if (rs.getInt(1) > 0) {
				return true;
			}
		}
	} catch (SQLException sqle) {
		throw sqle;
	} finally {
		if (connection != null) {
			connection.close();
		}
	}
	return false;
	}

}
