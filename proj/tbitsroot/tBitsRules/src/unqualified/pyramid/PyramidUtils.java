/**
 * 
 */
package pyramid;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;
import static transbit.tbits.webapps.WebUtil.ADD_ACTION;
import static transbit.tbits.webapps.WebUtil.ADD_REQUEST;
import static transbit.tbits.webapps.WebUtil.ADD_SUBREQUEST;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;

/**
 * @author Lokesh
 *
 */
public class PyramidUtils {
	private static final String PYRAMID_DTN_SYS_PREFIXES = "pyramid.dtn_sys_prefixes";
	private static final String PYRAMID_DUE_DATE_OFFSET = "pyramid.comment_due_date_offset";
	private static final String PYRAMID_SYS_PREFIXES_COMMON = "pyramid.sys_prefixes_common";
	private static final String PYRAMID_SYS_PREFIXES_345_RULES = "pyramid.sys_prefixes_345_rules";
	private static final String APP_PROPERTIES = "app.properties";
	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	
	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*try {
			System.out.println("ParentId: " + getSubRequestCountBySysIdReqId(2, 1));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		System.out.println("Exists: " + isExistsInCommons("DCR343"));
	}

	public static int getSubRequestCountBySysIdReqId (int aSystemId, int aParentId) throws SQLException{
		int count = 0;
		Connection connection = null;
	
		try {
			connection = DataSourcePool.getConnection();
	
			CallableStatement cs = connection.prepareCall("stp_request_lookupBySystemIdAndParentId ?, ?");
	
			cs.setInt(1, aSystemId);
			cs.setInt(2, aParentId);
			
			ResultSet rs = cs.executeQuery();
	
			if (rs != null) {
				if (rs.next() != false) {
					count = rs.getInt(1);
				}
	
				rs.close();
			}
	
			cs.close();
			rs = null;
			cs = null;
		} catch (SQLException sqle) {
			throw sqle;
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
	
		return count;
	}
		
	public static String getProperty(String propertyName)
    {
    	URL url = PyramidUtils.class.getResource(APP_PROPERTIES);
		String file = url.getFile();
		File f = new File(file);
		if (f.exists()) {
			Properties props = new Properties();
			try {
				props.load(new FileInputStream(f));
				String imprestBaPrefix = props.getProperty(propertyName);
				return imprestBaPrefix;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			LOG.error("PyramidUtils: The " + f.getAbsolutePath()
					+ " file is missing. Please check is it exist.");
		}
		return null;
    }
	
	public static File getResourceFile(String relativePath){
		URL url = PyramidUtils.class.getResource(relativePath);
		String file = url.getFile();
		File f = new File(file);
		return f;
	}
	
	/*
	 * Takes comma separated string and a compare string. Checks if the compare string 
	 * exists in the comma separated string.
	 */
	public static boolean isExistsInString(String parentString, String childString){
		String[] strArray = parentString.split(",");
		for (String str : strArray){
			if (str.trim().equals(childString.trim()))
				return true;
			else continue;
		}
		return false;
	}
	
	public static boolean isExistsInCommons(String sysPrefix){
		return isExistsInString (getProperty(PYRAMID_SYS_PREFIXES_COMMON), sysPrefix);
	}
	
	public static boolean inProperty345SysPrefixes(String sysPrefix){
		return isExistsInString (getProperty(PYRAMID_SYS_PREFIXES_345_RULES), sysPrefix);
	}
	
	public static int getPropertyDueDateOffset(){
		return Integer.parseInt(getProperty(PYRAMID_DUE_DATE_OFFSET));
	}
	
	public static boolean inPropertyDTNSysPrefixes(String sysPrefix){
		return isExistsInString (getProperty(PYRAMID_DTN_SYS_PREFIXES), sysPrefix);
	}
	
	/**
	 * @param aSystemId
	 * @param aRequestId
	 * @param ccList
	 * @return
	 * @throws DatabaseException
	 */
	public static ArrayList<RequestUser> getRequestUsersList(int aSystemId,
			int aRequestId, String ccList) throws DatabaseException {
		int usrId = 0;
		String[] userLoginArray = ccList.split(",");
		ArrayList<RequestUser> ruList = new ArrayList<RequestUser>();
		int ccGroup = ruList.size();
		for (String userLogin : userLoginArray){
			User tempUsr = getUserByLoginOrEmail(userLogin);
			if (tempUsr != null){
				usrId = tempUsr.getUserId();
				RequestUser ru = new RequestUser(aSystemId, aRequestId, UserType.CC, usrId, ++ccGroup, false);
				ruList.add(ru);									
			}
			else{
				continue;
			}								
		}
		return ruList;
	}
	
	/**
	 * @param aSystemId
	 * @param aRequestId
	 * @param ccList
	 * @return
	 * @throws DatabaseException
	 */	
	public static Set<RequestUser> getRequestUsersSet(int aSystemId,
			int aRequestId, String ccList) throws DatabaseException {
		int usrId = 0;
		String[] userLoginArray = ccList.split(",");
		Set<RequestUser> ruSet = new HashSet<RequestUser>();
		int ccGroup = ruSet.size();
		for (String userLogin : userLoginArray){
			User tempUsr = getUserByLoginOrEmail(userLogin);
			if (tempUsr != null){
				usrId = tempUsr.getUserId();
				RequestUser ru = new RequestUser(aSystemId, aRequestId, UserType.CC, usrId, ++ccGroup, false);
				ruSet.add(ru);									
			}
			else{
				continue;
			}								
		}
		return ruSet;
	}

	/**
	 * @param userLogin
	 * @return
	 * @throws DatabaseException
	 */
	private static User getUserByLoginOrEmail(String userLogin)
			throws DatabaseException {
		User tempUsr = User.lookupAllByUserLogin(userLogin);
		if (tempUsr == null){
			tempUsr = User.lookupAllByEmail(userLogin);
		}
		return tempUsr;
	}	
	
   /*
    * This method returns the caller of this servlet.
    *
    * @param aPathInfo  URL used to call the servlet.
    *
    * @return Enum that denotes the caller based on the url.
    *         <UL>
    *         <LI>ADD_REQUEST
    *         <LI>ADD_ACTION
    *         <LI>ADD_SUBREQUEST
    *         </UL>
    */
   @SuppressWarnings("unused")
   private int getCaller(String aPathInfo) {
       int index = ADD_REQUEST;

       if (aPathInfo.indexOf("add-request") > 0) {
           index = ADD_REQUEST;
       }

       if (aPathInfo.indexOf("add-action") > 0) {
           index = ADD_ACTION;
       }

       if (aPathInfo.indexOf("add-subrequest") > 0) {
           index = ADD_SUBREQUEST;
       }

       return index;
   }

}
