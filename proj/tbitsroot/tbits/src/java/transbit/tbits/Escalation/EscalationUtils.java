package transbit.tbits.Escalation;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import com.google.gson.Gson;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.admin.AdminEscalations;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.CalenderUtils;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

public class EscalationUtils {
	public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_ADMIN);
	private static final int COLUMN_SYSTEM_ID = 1;
	private static final int COLUMN_USER_ID = 2;
	private static final int COLUMN_PARENT_USER_ID = 3;
	
	public static void Escalate(Request request)
	{
		request.setAssignees(getNewAssignees(request.getAssignees()));
		
	}
	public static boolean shouldEscalate(Request request)
	{
		return true;
	}
	@Deprecated // Nitiraj : Always returns null .
	public static ArrayList<RequestUser> getNewAssignees(Collection<RequestUser> currentAssignees)
	{
		return null;
	}
	
	public static Date getNextDueDate(int span)
	{

		if (span != -1) {
			double daysDouble = span*1.0/60/24;
			int days = (int) Math.floor(daysDouble);
			int offsetMinutes = span % (60*24);
			
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MINUTE, offsetMinutes);
			Date dueDate = CalenderUtils.slideDate(c.getTime(), days);
			return dueDate;
		}
		return null;
	}
	
	public static ArrayList<String> getParentUsers(int aSystemId, int aUserId) throws TBitsException{
		ArrayList<String> userList = new ArrayList<String>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT parent_user_id FROM escalation_heirarchy " +
					"WHERE sys_id=" + aSystemId + " and user_id=" + aUserId);
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				while(rs.next()){
					String userIdStr = rs.getString("parent_user_id");
					User user = User.lookupByUserId(Integer.parseInt(userIdStr));
					userList.add(user.getUserLogin());
				}
				rs.close();
			}
			ps.close();
//			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TBitsException("Unable to retrieve parent_user_ids for the selected user", e);
		} catch (NumberFormatException e) {
			AdminEscalations.LOG.error("Invalid user id provided for retrieving escalation hierarchy");
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TBitsException("Unable to retrieve parent_user_ids for the selected user", e);			
		}finally{
			try {
				if(connection != null)
				connection.close();
			} catch (SQLException e) {
				AdminEscalations.LOG.warn("SeverityEscalation: Unable to close connection after getting parent_id in escalation hierarchy.", e);
			}
		}
		return userList;    	
	}
	
	public static ArrayList<String> getChildUsers(int aSystemId, int aUserId) throws TBitsException{
		ArrayList<String> userList = new ArrayList<String>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT user_id FROM escalation_heirarchy " +
					"WHERE sys_id=" + aSystemId + " and parent_user_id=" + aUserId);
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				while(rs.next()){
					String userIdStr = rs.getString("user_id");
					User user = User.lookupByUserId(Integer.parseInt(userIdStr));
					userList.add(user.getUserLogin());
				}
				rs.close();
				rs = null;
			}
			ps.close();
			ps = null;
//			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TBitsException("Unable to retrieve parent_user_ids for the selected user", e);
		} catch (NumberFormatException e) {
			AdminEscalations.LOG.error("Invalid user id provided for retrieving escalation heirarchy");
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TBitsException("Unable to retrieve parent_user_ids for the selected user", e);			
		}finally{
			try {
				if(connection != null)
				connection.close();
			} catch (SQLException e) {
				AdminEscalations.LOG.warn("SeverityEscalation: Unable to close connection after getting user_id in escalation hierarchy.", e);
			}
		}
		return userList;    	
	}
	
	public static HashMap<Integer, ArrayList<Integer>> getAllParentChildUsers(int aSystemId) throws TBitsException{
		HashMap<Integer,ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();    
      	Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM escalation_heirarchy " +
					"WHERE sys_id=" + aSystemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				while(rs.next()){
					int parent_Id = Integer.parseInt(rs.getString("parent_user_id"));
					int user_Id = Integer.parseInt(rs.getString("user_id"));
					if(map.keySet().contains(parent_Id)){
						ArrayList<Integer> arr = map.get(parent_Id);
					    arr.add(user_Id);
					    map.remove(parent_Id);
					    map.put(parent_Id,arr);
					}
					else{
						ArrayList<Integer> arr = new ArrayList<Integer>();
						arr.add(user_Id);
						map.put(parent_Id, arr);
					}	
				}
			    rs.close();
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TBitsException("Unable to retrieve parent_user_ids & user_id for the sys_id", e);
		} catch (NumberFormatException e) {
			AdminEscalations.LOG.error("Invalid user id provided for retrieving escalation hierarchy");
			e.printStackTrace();
		}finally{
			try {
				if(connection != null)
				connection.close();
			} catch (SQLException e) {
				AdminEscalations.LOG.warn("SeverityEscalation: Unable to close connection after getting parent_id in escalation hierarchy.", e);
			}
		}
		return map;
	}
	
	public static String insertUserHierarchy(int aSystemId, int aChildUserId, int aParentUserId) throws DatabaseException{

		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			//Avoiding cyclicity
			//1. Check if the current parent user is not already the child user of the user
			PreparedStatement ps = connection.prepareStatement("SELECT * from escalation_heirarchy WHERE " +
					"sys_id=" + aSystemId + " and parent_user_id=" + aChildUserId + " and user_id=" + aParentUserId);
			User childUser = User.lookupByUserId(aChildUserId);
			User parentUser = User.lookupByUserId(aParentUserId);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && (rs.next())){
				rs.close();				
				if ((childUser != null) && (parentUser !=null))
					return  "\""+ childUser.getUserLogin() + "\""+ " is parent user of " + "\""+ parentUser.getUserLogin() + "\"" + ". Hence cannot be added as child user of \"" +  parentUser.getUserLogin() + "\"";
			}
			rs.close();
			rs = null;	
						
			//Continue with insertion.
			CallableStatement cs = connection.prepareCall("stp_admin_insert_escalation_heirarchy ?,?,?");
			cs.setInt(COLUMN_SYSTEM_ID, aSystemId);
			cs.setInt(COLUMN_USER_ID, aChildUserId);
			cs.setInt(COLUMN_PARENT_USER_ID, aParentUserId);
			cs.execute();			
			cs.close();
			cs = null;
//			connection.close();
			return "Matters of \"" + childUser.getDisplayName() + "\" will be escalated to \"" + parentUser.getDisplayName() + "\"";
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while inserting Escalation heirarchy\n");
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
	
	public static void deleteUserHierarchy(int aSystemId, int aUserId) throws DatabaseException{
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			deleteUserHierarchy(connection, aSystemId, aUserId);
//			connection.close();
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while deleting Escalation heirarchy.\n");
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
	
	public static void deleteUserHierarchy(Connection connection, int aSystemId, int aUserId) throws SQLException{
			
		CallableStatement cs = connection.prepareCall("stp_admin_delete_escalation_heirarchy ?,?");
		cs.setInt(COLUMN_SYSTEM_ID, aSystemId);
		cs.setInt(COLUMN_USER_ID, aUserId);
		cs.execute();
		cs.close();
		cs = null;		
	}
	
	public static void deleteUserHierarchy(int aSystemId, int aUserId, int aParentId) throws DatabaseException{
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("DELETE FROM escalation_heirarchy " +
					"WHERE sys_id=" + aSystemId + " and user_id=" + aUserId + " and parent_user_id=" +
							aParentId);
			ps.executeUpdate();
			ps.close();
			ps = null;
//			connection.close();
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while deleting Escalation heirarchy.\n");
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
	
	public static String getUserHTML(ArrayList<String> userList){
    	StringBuffer sb = new StringBuffer();
    	for (String userLogin : userList){
    		sb.append("<Option value='").append(userLogin).append("'>").append(userLogin).append("</Option>");
    	}    	
    	return sb.toString();
    }
	
	public static void main(String[] args){
		try {
			System.out.println("Inserting>>> " + insertUserHierarchy(6, 3, 2));
			//System.out.println("Deleting: "); deleteUserHierarchy(6,16,10);
			System.out.println("Done....");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
}

