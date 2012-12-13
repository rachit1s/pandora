/**
 * 
 */
package transbit.tbits.webapps;

import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.User;

/**
 * @author Lokesh
 *
 */
public class ReportUtil {
	public static String getBARoles(){
		JSONArray baRoleMap = new JSONArray();		
		try {
			StringBuffer tempSB = new StringBuffer();
			ArrayList<BusinessArea> baList = BusinessArea.getActiveBusinessAreas();
			for (BusinessArea ba : baList){
				ArrayList<Role> rolesList = Role.getRolesBySysId(ba.getSystemId());				
				for (Role role : rolesList){					
					prepareString(tempSB, role.getRoleName());
				}
				JSONObject obj = new JSONObject();
				obj.put("baName", ba.getName());
				obj.put("roles", tempSB.toString());				
				baRoleMap.add(obj);
				tempSB.delete(0, tempSB.length()) ;
			}	
			return baRoleMap.toString();
		} catch (DatabaseException e) {
			e.printStackTrace();
			return baRoleMap.toString();
		}		
	}
	
	public static String getUsers(){
		StringBuffer tempSB = new StringBuffer();
		for(User user : User.getActiveUsers()){
			prepareString (tempSB, user.getUserLogin());
		}
		return tempSB.toString();	
	}	
	
	public static JSONArray getJSONArrayOfUsers(){
		JSONArray userArray = new JSONArray();
		for(User user : User.getActiveUsers()){
			userArray.add(user.getUserLogin());
		}
		return userArray;
	}
	
	public static JSONArray getJSONArrayOfUsersWithUserType(){
		JSONArray userArray = new JSONArray();
		for(User user : User.getActiveUsers()){
			JSONObject usrObj = new JSONObject();
			usrObj.put("userLogin", user.getUserLogin());
			usrObj.put("userType", user.getUserTypeId());
			userArray.add(usrObj);
		}
		return userArray;
	}
	
	public static void prepareString(StringBuffer sb, String appendString){
		if (sb.toString().equals(""))
			sb.append(appendString);
		else
			sb.append(",").append(appendString);
	}	
}
