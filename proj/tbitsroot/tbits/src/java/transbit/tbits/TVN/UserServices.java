package transbit.tbits.TVN;

import java.util.ArrayList;
import java.util.Hashtable;

import transbit.tbits.authentication.AuthUtils;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;

import com.tbitsglobal.tvncore.TvnException;
import com.tbitsglobal.tvncore.TvnServlet;
import com.tbitsglobal.tvncore.TvnType;
import com.tbitsglobal.tvncore.UserInfo;
import com.tbitsglobal.tvncore.delta.TVNBase64;

/**
 * This class provides the basic user services to TVN. 
 * User information returned to TVN is in the form of UserInfo 
 * (defined in the class tvnExternal.UserInfo).
 * 
 * @author karan
 *
 */

public class UserServices {

	//====================================================================================

	/**
	 * Authenticates and sets the user info according to the authorisation string. 
	 * @throws TvnException 
	 */
	public static UserInfo authenticate(String authorizationString) throws TvnException {
		
		if(authorizationString == null)
			return null;
		
		authorizationString = authorizationString.replace(TvnServlet.AUTH_SCHEME,"");
		authorizationString = authorizationString.replaceFirst("[ ]*", "");
		authorizationString.trim();
		
		StringBuffer sbuf = new StringBuffer(authorizationString);
		
		//Decode this string which is encoded in base64 format
		byte[] resultBytes = TVNBase64.base64ToByteArray(sbuf);
		String resultStr = new String(resultBytes);
		if(null == resultStr)
			return null;
		
		int colon = resultStr.indexOf(':');
		
		String userLogin = resultStr.substring(0,colon);
		String password = resultStr.substring(colon+1);
		
		transbit.tbits.domain.User user = null;
		try {
			if(AuthUtils.validateUser(userLogin, password) == true)
				user = User.lookupByUserLogin(userLogin);
		} catch (DatabaseException e) {
			e.printStackTrace();
			user = null;
		}
		
		if(user == null)
			throw new TvnException("Invalid username or password.");
		
		return convert(user);
	}

	//====================================================================================
	
	private static UserInfo convert(transbit.tbits.domain.User user){
		
		if(user == null)
			return null;
		UserInfo converted = new UserInfo(user.getDisplayName(), user.getUserId(), user.getUserLogin());
		return converted;
	}

	//====================================================================================

	public static boolean modificationPermitted(ArrayList<TvnType> structure, UserInfo user) {
		
		int sysID = structure.get(0).identifierHandler;
		int userID = user.getUserID();
		
		try{
			int perm = 1;
			for(int i=1; i<structure.size(); i++){
				if(structure.get(i).identifier.equals(Field.REQUEST)){
					// Permissioning for request and attachment type
					Action action = (Action)structure.get(i).identifierObject;
					perm = requestPermissionFor(sysID, userID, Field.REQUEST, Permission.CHANGE, action);
					if(perm == 0)
						return false;
					i++;
					perm = requestPermissionFor(sysID, userID, structure.get(i).identifierValue, Permission.CHANGE, action);
					if(perm == 0)
						return false;
					break;
				}
				else{
					// Permissioning for fields
					perm = permissionFor(sysID, userID, structure.get(i).identifier, Permission.CHANGE);
					if(perm == 0)
						return false;
				}
			}
		}
		catch (DatabaseException e){
			// Handle exception
		}
		
		return true;
	}

	//====================================================================================

	public static boolean accessPermitted(ArrayList<TvnType> structure, UserInfo user, int version) {
		
		ArrayList<TvnType> file = new ArrayList<TvnType>();
		for(int i=0; i<structure.size()-1; i++){
			TvnType toCheck = structure.get(i+1);
			file.add(structure.get(i));
			ArrayList<String> subFiles;
			try {
				subFiles = Services.getSubFolders(file, toCheck, version, user);
			} 
			catch (TvnException e) {
				e.printStackTrace();
				return false;
			}
			if(!subFiles.contains(toCheck.identifierValue))
				return false;
		}
		return true;
	}

	//====================================================================================

	public static boolean addPermitted(ArrayList<TvnType> structure, UserInfo user) {
		
		int sysID = structure.get(0).identifierHandler;
		int userID = user.getUserID();
		
		try{
			int perm = 1;
			for(int i=1; i<structure.size(); i++){
				if(structure.get(i).identifier.equals(Field.REQUEST)){
					// Permissioning for request and attachment type
					Action action = (Action)structure.get(i).identifierObject;
					perm = requestPermissionFor(sysID, userID, Field.REQUEST, Permission.ADD, action);
					if(perm == 0)
						return false;
					i++;
					perm = requestPermissionFor(sysID, userID, structure.get(i).identifierValue, Permission.ADD, action);
					if(perm == 0)
						return false;
					break;
				}
				else{
					// Permissioning for fields
					perm = permissionFor(sysID, userID, structure.get(i).identifier, Permission.ADD);
					if(perm == 0)
						return false;
				}
			}
		}
		catch (DatabaseException e){
			// Handle exception
		}
		
		return true;
	}

	//====================================================================================
	
	// Utility Methods
	
	public static int permissionFor(int sysID, int userID, String fieldName, int actionToBePerformed) throws DatabaseException{
		
		Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(sysID, userID);
		if (permTable.containsKey(fieldName)){
			int perm = permTable.get(fieldName);
			return (perm & actionToBePerformed);
		}
		else
			return 0;
	}
	
	public static int requestPermissionFor(int sysID, int userID, String fieldName, int actionToBePerformed, Action action) throws DatabaseException{
		
		Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndRequestIdAndActionIdAndUserId
												(sysID, action.getRequestId(), action.getActionId(), userID);
		if (permTable.containsKey(fieldName)){
			int perm = permTable.get(fieldName);
			return (perm & actionToBePerformed);
		}
		else
			return 0;
	}
	
	//====================================================================================

}
