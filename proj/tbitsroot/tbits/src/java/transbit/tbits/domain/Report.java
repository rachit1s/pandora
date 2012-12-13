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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;

/**
 * @author Lokesh
 *
 */
public class Report implements Serializable {	

	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);
	
	//private int mySystemId;
	private int myReportId;
	private String myReportName;
	private String myDescription;
	private String myFileName;
	private boolean isPrivate;
	private boolean isEnabled;
	private String myGroup;
	
	//SQL query parameters
	private static final String IS_INCLUDED = "is_included";
	private static final String REPORT_SPECIFIC_USERS = "report_specific_users";
	private static final String REPORT_ID = "report_id";
	private static final String USER_ID = "user_id";
	private static final String DELIMETER_COMMA = ",";
	private static final String WHITESPACE_STRING = " ";
	
	//Delimeters
	private static final String DELIMETER_EQUALS = "=";
	
	//Numeric constants of array indexes or db table column indexes
	private static final int ROLE_ID_INDEX = 2;
	
	public Report() {
		
	}
	
	public Report(int aReportId, String aReportName, String aDescription, String fileName, boolean isPrivate, boolean isEnabled,String aGroup){
		//mySystemId    = aSystemId;
        myReportId    = aReportId;
        myReportName  = aReportName;
        myDescription = aDescription;
        myFileName = fileName;
        myGroup   = aGroup;
        this.isEnabled = isEnabled;
        this.isPrivate = isPrivate;
	}
	
	public String toString(){
		return (myReportId + WHITESPACE_STRING + myReportName + WHITESPACE_STRING + myDescription + WHITESPACE_STRING + myFileName + isEnabled + WHITESPACE_STRING + isPrivate);
	}
	
	public void setReportId (int aReportId){
		myReportId = aReportId;
	}
	
	public void setReportName(String aReportName){
		myReportName = aReportName;
	}
	
	public void setDescription (String aDescription){
		myDescription = aDescription;
	}
	
	public void setFileName (String aFileName){
		myFileName = aFileName;
	}
	
	public void setIsEnabled (boolean isEnabled){
		this.isEnabled = isEnabled;
	}
	
	public void setIsPrivate (boolean isPrivate){
		this.isPrivate = isPrivate;
	}
	public void setGroup(String aGroup){
		myGroup =  aGroup;
	}
	
	public int getReportId(){
		return myReportId;
	}
	
	public String getReportName(){
		return myReportName;
	}
	
	public String getDescription(){
		return myDescription;
	}
	
	public String getFileName(){
		return myFileName;
	}
	
	public boolean getIsPrivate(){
		return isPrivate;
	}
	
	public boolean getIsEnabled(){
		return isEnabled;
	}
	public String getGroup(){
		return myGroup;
	}
	
	public static Report createFromResultSet(ResultSet aRS) throws SQLException {
        Report report = new Report(aRS.getInt(REPORT_ID), aRS.getString("report_name"), aRS.getString("description"), 
        		aRS.getString("file_name"), aRS.getBoolean("is_private"), aRS.getBoolean("is_enabled"),aRS.getString("group_name"));
        return report;
    }
	
	public static int insert (Report report) throws DatabaseException {
		int reportId = -1;
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();	
			connection.setAutoCommit(false);
			CallableStatement cs = connection.prepareCall("stp_report_insert ?, ?, ?, ?, ?, ?,?");
            cs.setString(1, report.getReportName());
            cs.setString(2, report.getDescription());
            cs.setString(3, report.getFileName());
            cs.setBoolean(4, report.getIsPrivate());
            cs.setBoolean(5, report.getIsEnabled());
            cs.setString(6, report.getGroup());
            cs.registerOutParameter(7, java.sql.Types.INTEGER);
            cs.execute();  
            reportId = cs.getInt(7);
            cs.close();
            
            connection.commit();
            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //           
            cs = null;
            /**
			 * the connection object was getting closed 2 times first in the
			 * try block and the again in the finally block 
			 * fixed the bug by commeting the connection.close in line below
			 */
//            connection.close();
		}catch (SQLException sqle) {
			try {
				if(connection != null)
					connection.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			StringBuilder message = new StringBuilder();

            message.append("An exception occurred while inserting ").append("Report Object.").append("\nReport Name: ").append(report.getReportName()).append("\n");

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
		return reportId;
	}
	
	public static Report update(Report aObject) throws DatabaseException {

        // Update logic here.
        if (aObject == null) {
            return aObject;
        }

        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_report_update ?, ?, ?, ?, ?, ?");
            aObject.setCallableParameters(cs);
            cs.execute();
            cs.close();
            aCon.commit();
        } catch (SQLException sqle) {
        	try {
        		if(aCon != null)
					aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while updating the field.").append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {
                	LOG.warn("Exception while closing the connection:", sqle);
                }
            }
        }

        return aObject;
    } 
	
	private void setCallableParameters(CallableStatement cs) throws SQLException {
		System.out.println(myReportId + ", " + myReportName + ", " + myDescription + ", " + myFileName + ", " + isPrivate + ", " + isEnabled);
		cs.setInt(1, myReportId);
		cs.setString(2, myReportName);
        cs.setString(3, myDescription);
        cs.setString(4, myFileName);
        cs.setBoolean(5, isPrivate);
        cs.setBoolean(6, isEnabled);
	}

	public static Report lookupByReportId (int aReportId) throws DatabaseException{
		Report report = null;
		Connection connection = null;
        try {
            connection = DataSourcePool.getConnection();
            CallableStatement cs = connection.prepareCall("stp_report_lookupByReportId ?");

            cs.setInt(1, aReportId);
            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    report = createFromResultSet(rs);
                }
                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the Role.").append("\nReport Id: ").append(aReportId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warning("Exception occurred while closing the connection.");
                }
            }

            connection = null;
        }
		return report;
	}
	
	public static Report lookupByReportName (String aReportName) throws DatabaseException{
		Report report = null;
		Connection connection = null;
        try {
            connection = DataSourcePool.getConnection();
            CallableStatement cs = connection.prepareCall("stp_admin_report_lookupByReportName ?");

            cs.setString(1, aReportName);
            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    report = createFromResultSet(rs);
                }
                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the Role.").append("\nReport Name: ").append(aReportName).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warning("Exception occurred while closing the connection.");
                }
            }

            connection = null;
        }
		return report;
	}
	
	public static ArrayList<Report> lookupPublicReports () throws DatabaseException{
		ArrayList<Report> reportsList = new ArrayList<Report>();
		Connection connection = null;
        try {
            connection = DataSourcePool.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM reports WHERE is_private = 0");
           
            ResultSet rs = ps.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                	Report report = createFromResultSet(rs); 
                	if ((report != null) && (report.getIsEnabled()))
                		reportsList.add(report);                	             
                }
                rs.close();
            }

            ps.close();
            rs = null;
            ps = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the reports \n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warning("Exception occurred while closing the connection.");
                }
            }

            connection = null;
        }
		return reportsList;
	}
	
	public static ArrayList<Report> lookupByUserlogin (String aUserLogin) throws DatabaseException{
		Connection connection = null;
		ArrayList<Report> reportsList = new ArrayList<Report>();
		User user = User.lookupAllByUserLogin(aUserLogin);
		
		if(user == null)
			return null;
		
		ArrayList<BusinessArea> baList = BusinessArea.getActiveBusinessAreas();
		
		ArrayList<User> allUsersToLookIn = new ArrayList<User>();
		ArrayList<User> directMailingLists = MailListUser.getMailListsByRecursiveMembership(user.getUserId());
		
		allUsersToLookIn.add(user);
		if(directMailingLists != null)
			allUsersToLookIn.addAll(directMailingLists);
		
		for(User aUser : allUsersToLookIn){
			for (BusinessArea ba : baList){
				int aSystemId = ba.getSystemId(); //System.out.println("sysId: " + aSystemId + " " + user.getUserLogin());
				ArrayList<String> rolesList = Role.getUserRolesBySysIdAndUserId(aSystemId, aUser.getUserId());
				for (String role : rolesList){
					String[] roleStr = role.split(DELIMETER_COMMA);				
					//Role role = Role.lookupBySystemIdAndRoleName(aSystemId, roleStr[0]);
					//System.out.println("RoleName: " + roleStr[0] + " " + roleStr[2]);
					if (Boolean.parseBoolean(roleStr[1])){
						ArrayList<Report> tempList	= lookupBySystemIdAndRoleId(aSystemId, Integer.parseInt(roleStr[ROLE_ID_INDEX]));
						if (tempList != null){
							for(Report report : tempList)
								if(report.isEnabled && !reportsList.contains(report))
									reportsList.add(report);
						}
					}
				}
			}
		
			//Continue with user specific reports		
			try{
				connection = DataSourcePool.getConnection();
				PreparedStatement ps = connection.prepareStatement("SELECT " + REPORT_ID + DELIMETER_COMMA + IS_INCLUDED
						+ " FROM " + REPORT_SPECIFIC_USERS + " WHERE " + USER_ID + DELIMETER_EQUALS + aUser.getUserId());
				ResultSet rs = ps.executeQuery();
				
				ArrayList <Report> usrReportRemoveList = new ArrayList<Report>();
				ArrayList <Report> usrReportAddList = new ArrayList<Report>();
				if (rs != null){				
					while (rs.next()){
						int reportId = rs.getInt(REPORT_ID);
						Report report = Report.lookupByReportId(reportId);		
						if ((report != null) && (report.getIsEnabled())){
							if (rs.getBoolean(IS_INCLUDED) == false)
								usrReportRemoveList.add(report);
							else
								usrReportAddList.add(report);
						}
						else //if null continue
							continue;
					}
					
					if (!usrReportRemoveList.isEmpty()){
						if (!reportsList.isEmpty()){
							Set<Report> roleReportsSet = new HashSet<Report>(reportsList);
							Set<Report> usrReportsSet = new HashSet<Report>(usrReportRemoveList);
							roleReportsSet.removeAll(usrReportsSet);
							reportsList.clear();
							reportsList.addAll(roleReportsSet);
						}
					}	
									
					if (!usrReportAddList.isEmpty()){
						if (reportsList == null)
							reportsList = new ArrayList<Report>();	
						Set<Report> roleReportsSet = new HashSet<Report>(reportsList);
						roleReportsSet.addAll(usrReportAddList);
						reportsList.clear();
						reportsList.addAll(roleReportsSet);					
					}
				}
				rs.close();
				ps.close();
			}catch (SQLException sqle) {
	            StringBuilder message = new StringBuilder();
	            message.append("An exception occured while retrieving the existing reports").append("\n");
	            try {
					throw new DatabaseException(message.toString(), sqle);
				} catch (DatabaseException e) {
					LOG.error("Exception while closing the connection:", sqle);
				}
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
		return reportsList;		
	}
	
	public static Hashtable<String, ArrayList<User>> getUserListByReportId (int reportId) throws DatabaseException{	
		Connection connection = null;		
		Hashtable<String, ArrayList<User>> userTable = new Hashtable<String, ArrayList<User>>();
		ArrayList<User> includeList = new ArrayList<User>();
		ArrayList<User> excludeList = new ArrayList<User>();
		try {
			connection = DataSourcePool.getConnection();			
			PreparedStatement ps = connection.prepareStatement("SELECT " + USER_ID + DELIMETER_COMMA + IS_INCLUDED + " FROM " + REPORT_SPECIFIC_USERS 
					+ " WHERE " + REPORT_ID  + DELIMETER_EQUALS + reportId);
			ResultSet rs = ps.executeQuery();
			if (rs!=null){
				while (rs.next()){
					int userId = rs.getInt(1);
					User user = User.lookupAllByUserId(userId);
					if (rs.getBoolean(2))
						includeList.add(user);
					else
						excludeList.add(user);
				}	
				userTable.put("includeUsers", includeList);
				userTable.put("excludeUsers", excludeList);
			}
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occured while retrieving the existing reports").append("\n");
			try {
				throw new DatabaseException(message.toString(), sqle);
			} catch (DatabaseException e) {
				LOG.error("Exception while closing the connection:", sqle);
			}
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
		return userTable;
	}
	
	private static int getReportIndex (int reportId, ArrayList<Report> reportsList) {
		for (Report report : reportsList){
			if (report.getReportId() == reportId)
				return reportsList.indexOf(report);
			else 
				continue;
		}		
		return -1;
	}

	public static ArrayList<Report> lookupBySystemIdAndRoleId (int aSystemId, int aRoleId) throws DatabaseException{
		ArrayList<Report> reportsList = new ArrayList<Report>();
		Connection connection = null;
        try {
            connection = DataSourcePool.getConnection();
            CallableStatement cs = connection.prepareCall("stp_report_lookupBySysIdAndRoleId ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRoleId);
            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                	Report report = createFromResultSet(rs);                	
                	reportsList.add(report);
                }
                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the report for roleId: ").append(aRoleId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warning("Exception occurred while closing the connection.");
                }
            }

            connection = null;
        }
		return reportsList;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isEnabled ? 1231 : 1237);
		result = prime * result + (isPrivate ? 1231 : 1237);
		result = prime * result
				+ ((myDescription == null) ? 0 : myDescription.hashCode());
		result = prime * result
				+ ((myFileName == null) ? 0 : myFileName.hashCode());
		result = prime * result + myReportId;
		result = prime * result
				+ ((myReportName == null) ? 0 : myReportName.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Report))
			return false;
		final Report other = (Report) obj;
		if (isEnabled != other.isEnabled)
			return false;
		if (isPrivate != other.isPrivate)
			return false;
		if (myDescription == null) {
			if (other.myDescription != null)
				return false;
		} else if (!myDescription.equals(other.myDescription))
			return false;
		if (myFileName == null) {
			if (other.myFileName != null)
				return false;
		} else if (!myFileName.equals(other.myFileName))
			return false;
		if (myReportId != other.myReportId)
			return false;
		if (myReportName == null) {
			if (other.myReportName != null)
				return false;
		} else if (!myReportName.equals(other.myReportName))
			return false;
		return true;
	}

	public static void main(String[] args){
		try {
			ArrayList<Report> list = lookupByUserlogin("abha.lad");
			if (list == null){
				System.out.println("No reports found");
			}else{
				for (Report rp : list){
			    	System.out.println("Report Id : " + rp.getReportId() + "\t Report Name: " + rp.getReportName());
			    }
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
}
