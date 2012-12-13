package transbit.tbits.TVN;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

import com.tbitsglobal.tvncore.InterfaceExternalServices;
import com.tbitsglobal.tvncore.TvnException;
import com.tbitsglobal.tvncore.TvnLockInfo;
import com.tbitsglobal.tvncore.TvnLogEntry;
import com.tbitsglobal.tvncore.TvnType;
import com.tbitsglobal.tvncore.UserInfo;

/**
 * This class provides an interface to all the external services supported by the specific system.
 * This class MUST be implemented with all the mentioned methods. The implementation of the methods
 * may vary as per the database structures.
 * 
 * @author Karan Gupta
 *
 */

public class ExternalServices implements InterfaceExternalServices{
	
	//====================================================================================

	/**
	 * Authenticate the user and return the user information.
	 * 
	 * @param authorizationString
	 * @return user
	 * @throws TvnException 
	 */
	public UserInfo authenticate(String authorizationString) throws TvnException{
		
		return UserServices.authenticate(authorizationString);
	}
	
	//====================================================================================

	/**
	 * Returns the head version of the path. The head version is the maximum version for a given path.
	 * 
	 * @param path
	 * @return headVersion
	 * @throws TvnException 
	 */
	public int getHeadVersion(String path) throws TvnException{
		
		return VersioningServices.getHeadVersion(path);
	}
	
	//====================================================================================

	/**
	 * Returns the user prefix for the user defined data types.
	 * 
	 * @return USER_PREFIX
	 */
	public String getUserPrefix(){
		
		return UserDefinedData.getUserPrefix();
	}
	
	//====================================================================================

	/**
	 * Read the folder structure from the corresponding database.
	 * @throws TvnException 
	 */
	public ArrayList<TvnType> readFolderStructure(String path) throws TvnException {
		
		return Services.readFolderStructure(path);
	}

	//====================================================================================

	/**
	 * Verify if the structure exists at the mentioned version in the database
	 */
	public ArrayList<TvnType> verifyFromDB(ArrayList<TvnType> structure, int version) {
		
		return Services.verifyFromDB(structure, version);
	}

	//====================================================================================

	/**
	 * Returns a list of names of subfolders or the given file structure
	 * @throws TvnException 
	 */
	public ArrayList<String> getSubFolders(ArrayList<TvnType> structure, TvnType subType, int version, UserInfo user) throws TvnException {
		
		return Services.getSubFolders(structure, subType, version, user);
	}

	//====================================================================================

	/**
	 * Returns a hashtable of the properties of the 
	 */
	public Hashtable<String, String> getProps(ArrayList<TvnType> structure) {
		
		return UserDefinedData.getProps(structure);
	}
	
	//====================================================================================

	/**
	 * Returns the path of the temporary folder where the data is to be stored
	 */
	public String getTempFolderPath(){
		
		return UserDefinedData.getTempFolderPath();
	}

	//====================================================================================

	/**
	 * Returns whether the user is permitted to modify resources at leaf of given structure.
	 */
	public boolean modificationPermitted(ArrayList<TvnType> structure, UserInfo user, int version) {
		
		return UserServices.modificationPermitted(structure, user);
	}

	//====================================================================================

	/**
	 * Returns whether the user is permitted to view resources at leaf of given structure.
	 */
	public boolean accessPermitted(ArrayList<TvnType> structure, UserInfo user, int version) {
		
		return UserServices.accessPermitted(structure, user, version);
	}

	//====================================================================================

	/**
	 * Returns whether the user is permitted to add resources at leaf of given structure.
	 */
	public boolean addPermitted(ArrayList<TvnType> structure, UserInfo user, int version) {
		
		return UserServices.addPermitted(structure, user);
	}

	//====================================================================================

	/**
	 * Returns the TvnPath's filepath in the repository.
	 * @throws TvnException 
	 */
	public String getFilePathInRepo(String repoFileID) throws TvnException {
		
		return Services.getFilePathInRepo(repoFileID);
	}

	//====================================================================================

	/**
	 * @param structure : The structure of the Tvn File
	 * @return The ID of the file in the repository
	 * @throws TvnException 
	 */
	public String getFileRepoID(ArrayList<TvnType> structure) throws TvnException {
		
		return Services.getFileRepoID(structure);
	}
	
	//====================================================================================

	/**
	 * Upload the given file into the repository.
	 * @throws Exception 
	 */
	public String uploadIntoRepository(InputStream diff, String oldFileLocation, String pathHint) throws Exception {
		
		return Services.uploadIntoRepository(diff, oldFileLocation, pathHint);
	}

	//====================================================================================

	/**
	 * Update the properties of the given request path in the database.
	 * @throws TvnException 
	 */
	public int updateInDB(String activityID, Hashtable<String, Hashtable<String,String>> allProps) throws TvnException {
		
		return Services.updateInDB(activityID, allProps);
	}
	
	//====================================================================================

	/**
	 * Returns the last committed version for a given type structure.
	 */
	public int lastCommittedVersion(ArrayList<TvnType> structure, int version){
		
		return VersioningServices.lastCommittedVersion(structure, version);
	}

	//====================================================================================

	/**
	 * Returns whether the path can be unlocked by the given user.
	 * The path can be unlocked by the user issuing the lock or 
	 * if the given user has the privilege to override the locker's 
	 * rights to the resource.
	 */
	public boolean hasRightsTo(UserInfo user, String path) {
		
		return LocksServices.hasRightsTo(user, path);
	}

	//====================================================================================

	/**
	 * Gets the persistent lock information of the path.
	 */
	public TvnLockInfo getLockInfo(String path){
		
		return LocksServices.getLockInfo(path);
	}
	
	//====================================================================================

	/**
	 * Remove the lock information pertaining to the given path.
	 */
	public boolean removeLock(String path) {

		return LocksServices.removeLock(path);
	}

	//====================================================================================

	/**
	 * Add lock information or the given path.
	 * @param path
	 * @param user
	 * @param request 
	 */
	public String addLock(String path, UserInfo user, HttpServletRequest request) {
		
		return LocksServices.addLock(path, user, request);
	}

	//====================================================================================

	public ArrayList<TvnLogEntry> getAllLogItems(String pathInfo, int startRevision, int endRevision, int limit) 
									throws TvnException {

		return Services.getAllLogItems(pathInfo, startRevision, endRevision, limit);
	}

	//====================================================================================

}
