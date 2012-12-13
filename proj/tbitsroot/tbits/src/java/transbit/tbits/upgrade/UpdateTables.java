package transbit.tbits.upgrade;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.HashUtilities;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;

public class UpdateTables implements IUpgrade, ITBitsJob{
	
	//====================================================================================

//	private static String ATTACHMENT_DIR = Configuration.findAbsolutePath(PropertiesHandler.getProperty(TBitsPropEnum.KEY_ATTACHMENTDIR));
	private static int securityCodeBase = 10000;
	
	//====================================================================================

	public boolean upgrade(Connection conn, String folder, String sysType)
			throws SQLException, DatabaseException, TBitsException {
		
		return computeRepoFilesSha1(conn);
	}

	private boolean computeRepoFilesSha1(Connection conn) {
		// Fetch a list of all the entries in the file_repo_index table
		try {
			String query = "select id, location from file_repo_index";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				int id = rs.getInt(1);
				String location = rs.getString(2);
				
				// Compute the new parameters to be added
				//-----------------------------------------------------------------------------
				File currFile = new File(APIUtil.getAttachmentLocation() + File.separatorChar + location);
				if(!currFile.exists()){
					System.out.println("File not found. Quitting the operation.\nFile : " + location);
					return false;
				}
				// Find the sha1 hash of the file to be inserted.
				String hash = null;
				try {
					hash = HashUtilities.computeHash(currFile, "SHA1");
				} 
				catch (NoSuchAlgorithmException e1) {
					e1.printStackTrace();
					System.out.println("SHA1 algorithm not found. Quitting the operation.");
					return false;
				}
				// 5 digit random number for security code.
				float securityCodeGenerator = new Random(new Date().getTime()).nextInt(securityCodeBase);
				securityCodeGenerator *= securityCodeGenerator;
				securityCodeGenerator /= new Random(new Date().getTime()).nextInt(securityCodeBase/2);
				int securityCode = (int)(securityCodeBase + (securityCodeGenerator%(securityCodeBase*10)));
				//-----------------------------------------------------------------------------
				updateRecordInFileRepoIndex(conn, id, hash, securityCode);
			}
			
		} 
		catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Some error occured while fetching from or updating to the file_repo_index table.");
			return false;
		}
		
		return true;
	}

	//====================================================================================

	private void updateRecordInFileRepoIndex(Connection conn, int id, String hash, int securityCode) throws SQLException {
		
		try {
			String updateStatement = "update file_repo_index set hash=?, security_code=? where id=?";
			PreparedStatement statement = conn.prepareStatement(updateStatement);
			statement.setString(1, hash);
			statement.setInt(2, securityCode);
			statement.setInt(3, id);
			
			statement.executeUpdate();
		} 
		catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("Some error occured while updating in file_repo_index table.\n" +
								"Repo ID : " + id);
		}
		return;
	}

	public String getDisplayName() {
		return "Compute SHA1 of Repo Files";
	}

	public Hashtable<String, JobParameter> getParameters() throws SQLException {
		return new Hashtable<String, JobParameter>();
	}

	public boolean validateParams(Hashtable<String, String> params)
			throws IllegalArgumentException {
		return true;
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		computeRepoFilesSha1();
	}
	public static void main(String[] args) {
		UpdateTables ut = new UpdateTables();
		ut.computeRepoFilesSha1();
	}

	private void computeRepoFilesSha1() {
		Connection conn = null;
		try
		{
			conn = DataSourcePool.getConnection();
			computeRepoFilesSha1(conn);
		}
		catch(SQLException exp)
		{
			exp.printStackTrace();
		}
		finally 
		{
			if(conn != null)
			{
				try
				{
					conn.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	//====================================================================================

}
