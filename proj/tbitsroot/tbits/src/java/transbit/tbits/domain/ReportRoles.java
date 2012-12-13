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
import java.util.ArrayList;
import java.util.HashMap;

import org.jfree.util.Log;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.exception.TBitsException;

/**
 * Domain Object Class corresponding to the report_roles table in the database
 * @author dheeru
 *
 */
public class ReportRoles  implements Comparable<ReportRoles>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

	// Enum sort of fields for Attributes.
	public static final int SYSTEMID = 1;
	public static final int REPORTID = 2;
	public static final int ROLEID = 3;


	//Attributes for this domain Objects
	private int mySystemId;
	private int    myRoleId;
	private int myReportId;

	public ReportRoles(){
		super();
	}

	public ReportRoles(int aSystemId, int aRoleId, int aReportId) {
		this.mySystemId = aSystemId;
		this.myRoleId = aRoleId;
		this.myReportId = aReportId;
	}

	// ........... mySystemId
	public int getSystemId(){
		return this.mySystemId;
	}

	public void setSystemId(int aSystemId){
		this.mySystemId = aSystemId;
	}

	// ...............myRoleId
	public int getRoleId(){
		return this.myRoleId;
	}

	public void setRoleId(int aRoleId){
		this.myRoleId = aRoleId;
	}

	//.....................myReportId
	public int getReportId(){
		return this.myReportId;
	}

	public void setReportId(int aReportId){
		this.myReportId = aReportId;
	}


	// ------------------------- database methods --------------------------------------


	public static ReportRoles insert(int aSystemId, int aRoleId, int aReportId, Connection con ) throws SQLException, TBitsException, DatabaseException
	{
		ReportRoles reportRoles = new ReportRoles(aSystemId, aRoleId, aReportId);
		return insert(reportRoles,con);
	}



	public static ReportRoles insert(ReportRoles reportRole, Connection con) throws TBitsException, SQLException, DatabaseException {

		if( null == con || con.isClosed() == true ){
			LOG.info("The connection object supplied was null or closed.");
			throw new DatabaseException("The connection object supplied was null or closed.", new SQLException());    		
		}

		if (null == reportRole){
			throw new TBitsException("The role supplied was null.");
		}

		CallableStatement aCS = con.prepareCall("stp_report_role_insert ?,?,?");	
		aCS.setInt(SYSTEMID, reportRole.getSystemId());
		aCS.setInt(ROLEID, reportRole.getRoleId());
		aCS.setInt(REPORTID, reportRole.getReportId());
		aCS.execute();
		aCS.close();
		aCS = null;
		return reportRole;
	}

	public static boolean updateReportRoles(HashMap<String, ArrayList<Role>> baRoleMap, int reportId) throws DatabaseException{
		Connection connection = null;
		boolean success = false;
		try{
			connection = DataSourcePool.getConnection();	
			connection.setAutoCommit(false);
			if (baRoleMap.isEmpty() || (baRoleMap == null)){
				PreparedStatement psDeleteAll = connection.prepareStatement("DELETE FROM report_roles WHERE report_id=" + reportId);
				psDeleteAll.execute();
				psDeleteAll.close();	
				psDeleteAll = null;
				success = true;
			}
			else{
				BusinessArea tempBA;				
				for (String sysPrefix : baRoleMap.keySet()){					

					tempBA = BusinessArea.lookupByName(sysPrefix);
					if (tempBA == null){
						Log.info("Wrong system prefix provided for report role");
						continue;
					}
					int aSystemId = tempBA.getSystemId();

					PreparedStatement psDelete = connection.prepareStatement ("DELETE FROM report_roles WHERE report_id=" + reportId + " and sys_id=" + aSystemId);
					psDelete.execute();
					psDelete.close();

					for (Role role : baRoleMap.get(sysPrefix)){						
						int roleId = role.getRoleId();							
						try{
							insert(new ReportRoles(aSystemId, roleId, reportId), connection);
						} catch (TBitsException e){
							LOG.severe(e.getMessage());
						}
					}
					psDelete = null;
				}
				connection.commit();
				success = true;
			}
			connection.close();
			connection = null;
			return success;
		}catch (SQLException sqle) {
			try {
				if(connection != null)
					connection.rollback();
			} catch (SQLException e) {
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

	public static HashMap<Integer, ArrayList<Integer>> getReportRoles(int reportId) throws DatabaseException{
		Connection con = null;
		HashMap<Integer, ArrayList<Integer>> reportRoles = new HashMap<Integer, ArrayList<Integer>>();
		try {
			con = DataSourcePool.getConnection();
			con.setAutoCommit(false);
			PreparedStatement ps = con.prepareStatement("SELECT * FROM report_roles WHERE report_id=" + reportId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()){
				ArrayList<Integer> tempList = reportRoles.get(rs.getInt(SYSTEMID));
				if (tempList == null){
					tempList = new ArrayList<Integer>();
					tempList.add(rs.getInt(ROLEID));
					reportRoles.put(rs.getInt(SYSTEMID), tempList);
				} else {
					tempList.add(rs.getInt(ROLEID));
				}
			}
			rs.close();
			ps.close();
			con.commit();
			con.close();
			con = null;
			return reportRoles;
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

	public int compareTo(ReportRoles arg0) {
		return 0;
	}

}
