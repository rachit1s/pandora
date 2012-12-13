/**
 * 
 */
package transbit.tbits.domain;

import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;

/**
 * Domain Object Class for database Table report_specific_users
 * @author dheeru
 *
 */
public class ReportSpecificUsers   implements Comparable<Role>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

	private static final int REPORTID = 1;
	private static final int USERID = 2;
	private static final int ISINCLUDED = 3;

	// ...........................................

	private int myReportId;
	private int myUserId;
	private boolean isIncluded;

	//...............................................

	//--------- myReportId-----------
	public int getReportId(){
		return this.myReportId;
	}
	public void setReportId(int aReportId){
		this.myReportId = aReportId;
	}

	//---------myUserId---------------
	public int getUserId(){
		return this.myUserId;
	}
	public void setUserId(int aUserId){
		this.myUserId = aUserId;
	}

	//-------------isIncluded ------------
	public boolean getIsIncluded(){
		return this.isIncluded;
	}
	public void setIsIncluded(boolean isIncluded){
		this.isIncluded = isIncluded;
	}

	////// --------------- DataBase methods ----------------------------

	public static boolean updateReportSpecificUser(List<String> userLogins, int reportId, boolean include) throws DatabaseException{		
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);

			if (userLogins.isEmpty()){
				PreparedStatement psDelete = connection.prepareStatement("DELETE FROM report_specific_users WHERE report_id=" + reportId +
						" and is_included='" + include + "'");
				psDelete.execute();
				connection.commit();
				psDelete.close();
				psDelete = null;
			}
			else{				
				PreparedStatement ps = connection.prepareStatement("DELETE FROM report_specific_users WHERE " +
						"report_id=" + reportId + "and is_included='" + include + "'");
				ps.execute();
				ps.close();
				for (String userLogin : userLogins){
					User user = User.lookupByUserLogin(userLogin);
					int userId = user.getUserId();
					if (user != null){
						CallableStatement cs = connection.prepareCall("stp_report_specific_user_insert ?, ?, ?");
						cs.setInt (REPORTID, reportId);
						cs.setInt (USERID, userId);
						cs.setBoolean (ISINCLUDED, include);
						cs.execute();
						cs.close();
						cs = null; 
					}
					else
						LOG.info("No user found with login: " + userLogin);
				}
				connection.commit();														
			}
			connection.close();
			connection = null;
			return true;
			//	        connection.close();
		}catch (SQLException sqle) {
			try {
				if(connection != null)
					connection.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			StringBuilder message = new StringBuilder();

			message.append("An exception occurred while inserting ").append("Report Object.").append("\nReport Id: ").append(reportId).append("\n");

			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				connection = null;
			}            
		}   	
	}

	public static HashMap<Integer, Boolean> getReportSpecificUsers(int reportId) throws DatabaseException{
		Connection con = null;
		HashMap<Integer, Boolean> reportSpecificUsers = new HashMap<Integer, Boolean>();
		try {
			con = DataSourcePool.getConnection();
			con.setAutoCommit(false);
			PreparedStatement ps = con.prepareStatement("SELECT * FROM report_specific_users WHERE report_id=" + reportId);
			ResultSet rs = ps.executeQuery();
			con.commit();
			while (rs.next()){
				reportSpecificUsers.put(rs.getInt(ReportSpecificUsers.USERID), rs.getBoolean(ReportSpecificUsers.ISINCLUDED));
			}
			ps.close();
			ps = null;
			rs.close();
			rs = null;
			con.commit();
			con.close();
			con = null;
			return reportSpecificUsers;
		} catch (SQLException e) {
			try {
				if(con != null)
					con.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			StringBuilder message = new StringBuilder();

			message.append("An exception occurred while retrieving ").append("Report roles").append("\nReport Id: ").append(reportId).append("\n");

			throw new DatabaseException(message.toString(), e);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				con = null;
			}            
		}
	}
	public int compareTo(Role arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
