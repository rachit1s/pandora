package transbit.tbits.TVN;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

import com.tbitsglobal.tvncore.Error;
import com.tbitsglobal.tvncore.TvnConstants;
import com.tbitsglobal.tvncore.TvnException;
import com.tbitsglobal.tvncore.TvnFile;
import com.tbitsglobal.tvncore.TvnLogEntry;
import com.tbitsglobal.tvncore.TvnType;
import com.tbitsglobal.tvncore.UserInfo;
import com.tbitsglobal.tvncore.delta.DeltaUtil;
import com.tbitsglobal.tvncore.delta.TVNDeltaReader;
import com.tbitsglobal.tvncore.delta.TVNDiffWindow;

public class Services {
	
	//====================================================================================

	private final static String STRUCTURE_TABLE = "tvn_folder_structure";
	public static final String TVN_NAME = "tvn_name";
	public static final String VERSION_NUM = "version-number";

	//====================================================================================
	
	/**
	 * Reads the folder structure from the folder structures table.
	 * 
	 * @return the folder structure to be implemented my the files in Tvn
	 * @throws TvnException 
	 */
	public static ArrayList<TvnType> readFolderStructure(String path) throws TvnException {
		
		Connection conn = null;
		ArrayList<TvnType> structure = null;
		try{
			// Get the sys_prefix from the path
			String sys_prefix = path;
			if(sys_prefix.startsWith("/"))
				sys_prefix = sys_prefix.substring(1);
			if(sys_prefix.indexOf("/") != -1)
				sys_prefix = sys_prefix.substring(0, sys_prefix.indexOf("/"));
			
			BusinessArea BA = BusinessArea.lookupBySystemPrefix(sys_prefix);
			int sys_id = BA.getSystemId();
			
			structure = new ArrayList<TvnType>();
			
			// Add sys_id to the structure
			TvnType toBeAdded = new TvnType();
			toBeAdded.identifier = Field.BUSINESS_AREA;
			structure.add(toBeAdded);
			
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
				
			String query = "select identifier, _order from " + STRUCTURE_TABLE + " where sys_id=? order by _order asc";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, sys_id);
			ResultSet result = statement.executeQuery();
			
			while(result.next()){
				toBeAdded = new TvnType();
				toBeAdded.identifier = result.getString("identifier");
				structure.add(toBeAdded);
			}
			
			conn.commit();
		}
		catch (DatabaseException e) {
			e.printStackTrace();
			System.out.println("Unable to get BA from path.");
			try{
				if(conn!=null)
					conn.rollback();
			}
			catch(SQLException sqlex1){
				sqlex1.printStackTrace();
				System.out.println("Unable to roll back.");
			}
			throw new TvnException("Unable to read folder structure from the database.");
		}
		catch(SQLException sqlex){
			sqlex.printStackTrace();
			System.out.println("Unable to read folder structure from the database.");
			try{
				if(conn!=null)
					conn.rollback();
			}
			catch(SQLException sqlex1){
				sqlex1.printStackTrace();
				System.out.println("Unable to roll back.");
			}
		} 
		finally{
			try {
				if(conn != null)
					conn.close();
			} 
			catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Unable to close connection.");
			}
			
			// Add request, attachment type and attachment
			TvnType toBeAdded = new TvnType();
			toBeAdded.identifier = Field.REQUEST;
			structure.add(toBeAdded);
			
			toBeAdded = new TvnType();
			toBeAdded.identifier = "attachment_type_id";
			structure.add(toBeAdded);
			
			toBeAdded = new TvnType();
			toBeAdded.identifier = "attachment_id";
			structure.add(toBeAdded);
		}
		
		return structure;
	}

	//====================================================================================

	/**
	 * Verify the structure of a TvnFile from the database.
	 * 
	 * @param structure : the structure of the Tvn File
	 * @param version : the version for which the file needs to be verified
	 * @return the structure of the TvnFile with information retrieved from database lookup.
	 */
	public static ArrayList<TvnType> verifyFromDB(ArrayList<TvnType> structure, int version) {
		
		Connection conn = null;
		try{
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
				
			int sysID = 0;
			BusinessArea myBusinessArea = null;
			
			for(int i=0; i<structure.size(); i++){
				String lookUpVal = structure.get(i).identifier;
				if(lookUpVal.equals(Field.BUSINESS_AREA)){
					/// Set the BUSINESS_AREA type
					myBusinessArea = BusinessArea.lookupBySystemPrefix(structure.get(i).identifierValue);
					if (myBusinessArea == null) {
						structure.get(i).foundInDB = false;
						return structure;
					}
					structure.get(i).foundInDB = true;
					structure.get(i).identifierHandler = myBusinessArea.getSystemId();
					structure.get(i).identifierObject = (Object) myBusinessArea;
					sysID = myBusinessArea.getSystemId();
				}
				else if(lookUpVal.equals(Field.REQUEST)){
					// Set up the tvnName and set the attachment type values and attachment values underneath
					Action action = lookupByParams(conn,sysID,structure.get(i).identifierValue,version);
					if (action == null) {
						structure.get(i).foundInDB = false;
						return structure;
					}
					// Found the tvnName
					structure.get(i).foundInDB = true;
					structure.get(i).identifierHandler = action.getRequestId();
					structure.get(i).identifierObject = (Object) action;
					
					// Now look for attachment type
					Field attType = null;
					i++;
					if(i<structure.size()){
						ArrayList <Field> FieldList = Field.lookupBySystemId(sysID, DataType.ATTACHMENTS);
						if(FieldList != null){
							for(int count = 0; count < FieldList.size(); count++){
								if(FieldList.get(count).getName().equals(structure.get(i).identifierValue)){
									attType = FieldList.get(count); 
									break;
								}
							}
						}
						if(attType == null){
							structure.get(i).foundInDB = false;
							return structure;
						}
						// Found attachment type
						structure.get(i).foundInDB = true;
						structure.get(i).identifierHandler = attType.getFieldId();
						
						// Now look for attachment
						AttachmentInfo attachment = null;
						i++;
						if(i<structure.size()){
							int requestId = action.getRequestId();
							Hashtable<String, Object> params = getActionAndFileActionsOfType
													(conn,sysID, requestId, version, structure.get(i).identifierValue, attType.getFieldId());
							Action myaction = (Action)params.get(Field.ACTION);
							String fileAction = (String)params.get(TvnConstants.FILE_ACTION);
							if(myaction == null || fileAction.equals(TvnConstants.FILE_DELETED)){
								structure.get(i).foundInDB = false;
								return structure;
							}
							else{
								attachment = getAttachment(myaction, structure.get(i).identifierValue);
								if(attachment == null){
									attachment = getAttachmentOfType(myaction, structure.get(i).identifierValue, attType.getFieldId());
								}
								if(attachment == null) {
									structure.get(i).foundInDB = false;
									return structure;
								}
							}
							// Found attachment
							structure.get(i).foundInDB = true;
							structure.get(i).identifierHandler = attachment.repoFileId;
							structure.get(i).utilityHandlers.put(LocksServices.REQUEST_FILE_ID, attachment.requestFileId+"");
						}
					}
				}
				else {
					// Set the fixed and extended fields
					Type tBitstype = Type.lookupAllBySystemIdAndFieldNameAndTypeName
															(sysID,lookUpVal,structure.get(i).identifierValue);
					if(tBitstype == null) {
						structure.get(i).foundInDB = false;
						return structure;
					}
					structure.get(i).foundInDB = true;
					structure.get(i).identifierHandler = tBitstype.getTypeId();
				}
			}
			
			conn.commit();
		}
		catch(Exception ex){
			structure.get(0).foundInDB = false;
			ex.printStackTrace();
			System.out.println("Unable to verify the file from the database.");
			try{
				if(conn!=null)
					conn.rollback();
			}
			catch(SQLException sqlex1){
				sqlex1.printStackTrace();
				System.out.println("Unable to roll back.");
			}
			return structure;
		} 
		finally{
			try {
				if(conn != null)
					conn.close();
			} 
			catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Unable to close connection.");
			}
		}
		
		return structure;
	}

	//====================================================================================

	/**
	 * Populate the subfolders for the given structure, version and user.
	 * 
	 * @param structure : the present structure of the Tvn File
	 * @param subType : the Tvn Type immediately following the present structure
	 * @param user : the user for whom the subfolders need to be shown. The user access permissions are checked and subfolders shown accordingly.
	 * @throws TvnException 
	 */
	public static ArrayList<String> getSubFolders(ArrayList<TvnType> structure, TvnType subType, int version, UserInfo user) throws TvnException{
		
		Connection conn = null;
		ArrayList<String> subFolders = new ArrayList<String>();
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			// Find the sysID and see if the user is allowed to view the BA
			int sysID = 0;
			int attTypeID = 0;
			Action action = null;
			
			for(int i=0; i<structure.size(); i++){
				if(structure.get(i).identifier.equals(Field.BUSINESS_AREA)){
					sysID = structure.get(i).identifierHandler;
					// Check permission to view BA
					Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(sysID, user.getUserID());
					int perm = permTable.get(Field.BUSINESS_AREA);
					if((perm & Permission.VIEW) == 0){
						return subFolders;
					}
				}
				else if(structure.get(i).identifier.equals(Field.REQUEST)){
					action = (Action) structure.get(i).identifierObject;
				}
				else if(structure.get(i).identifier.equals("attachment_type_id")){
					attTypeID = structure.get(i).identifierHandler;
				}
			}
			
			// Populate the subfolder
			if(subType.identifier.equals(Field.REQUEST)){
				// Populate for requests
				// Get a list of all actions and their related Tvn Names
				Hashtable<Action, String> allActionsAndTvnNames = getActionsAndTVNName(conn,sysID,version);
				if(allActionsAndTvnNames == null)
					return null;
				
				Enumeration<Action> allActions = allActionsAndTvnNames.keys();
				if(allActions == null)
					return null;
				while(allActions.hasMoreElements()) {
					
					Action myAction = allActions.nextElement();
					if(myAction == null)
						continue;
					// Find the appropriate action
					boolean correctAction = true;
					for(int j=0; j<structure.size(); j++){
						if(structure.get(j).identifier.equals(Field.BUSINESS_AREA))
							continue;
						if(structure.get(j).identifier.equals(Field.CATEGORY)){
							if(myAction.getCategoryId() != structure.get(j).identifierHandler)
								correctAction = false;
						}
						else if(structure.get(j).identifier.equals(Field.REQUEST_TYPE)){
							if(myAction.getRequestTypeId() != structure.get(j).identifierHandler)
								correctAction = false;
						}
						else if(structure.get(j).identifier.equals(Field.STATUS)){
							if(myAction.getStatusId() != structure.get(j).identifierHandler)
								correctAction = false;
						}
						else {
							Request req = Request.lookupBySystemIdAndRequestId(sysID, myAction.getRequestId());
							String fieldName = req.get(structure.get(j).identifier);
							if (!fieldName.equals(structure.get(j).identifierValue))
								correctAction = false;
						}
					}
					
					// If the current action is the appropriate action, put the tvnName into the subfolder list
					if(correctAction){
						Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndRequestIdAndActionIdAndUserId
																(sysID, myAction.getRequestId(), myAction.getActionId(), user.getUserID());
						if((permTable.get(Field.REQUEST) & Permission.VIEW) != 0){
							String tvnName = allActionsAndTvnNames.get(myAction);
							if(tvnName == null) {
								System.out.println("Null TVN Name obtained while loading sub folders");
								continue;
							}
							subFolders.add(tvnName);
						}
					}
				}
			}
			
			else if(subType.identifier.equals("attachment_type_id")){
				// Populate for all attachment types
				Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndRequestIdAndActionIdAndUserId
														(sysID, action.getRequestId(), action.getActionId(), user.getUserID());
				ArrayList <Field> FieldList = Field.lookupBySystemId(sysID, DataType.ATTACHMENTS);
				for(int count = 0; count < FieldList.size(); count++){
					String name = FieldList.get(count).getName();
					int perm = permTable.get(name);
					if((perm & Permission.VIEW) != 0){
						subFolders.add(name);
					}
				}
			}
			else if(subType.identifier.equals("attachment_id")){
				// Populate for all attachments
				Hashtable<AttachmentInfo,Action> actionAndAttachments = getActionAndAttachmentsAtVer(conn, sysID, 
																			action.getRequestId(), attTypeID, version);
				Enumeration<AttachmentInfo> myAttachments = actionAndAttachments.keys();
				while(myAttachments.hasMoreElements()) {
					AttachmentInfo itemAtt = myAttachments.nextElement();
					subFolders.add(itemAtt.name);
				}
			}
			else {
				// Populate for fixed types
				ArrayList<Type> list = Type.lookupAllBySystemIdAndFieldName(sysID, subType.identifier);
				Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(sysID, user.getUserID());
				int perm = permTable.get(subType.identifier);
				if((perm & Permission.VIEW) != 0){
					for(int count=0; count<list.size(); count++) {
						subFolders.add(list.get(count).getName());
					}
				}
			}
			
			conn.commit();
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to populate sub-folders.");
			try {
				if(conn != null)
					conn.rollback();
			} 
			catch (SQLException e1) {
				e1.printStackTrace();
				System.out.println("Unable to rollback.");
			}
			throw new TvnException("Unable to populate sub-folders.");
		}
		finally
		{
			if(conn != null)
			{
				try
				{
					conn.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
					System.out.println("Unable to close connection.");
				}
			}
		}
		
		return subFolders;
	}

	//====================================================================================

	/**
	 * @param repoFileID : the handler of the file in the repository.
	 * @return path of the file of given ID in the repository.
	 */
	public static String getFilePathInRepo(String repoFileID) throws TvnException {
		
		String filePath = "";
		try {
			filePath = UserDefinedData.getAttachmentFolderPath() + File.separatorChar + Uploader.getFileLocation(Integer.parseInt(repoFileID));
		} 
		catch (DatabaseException e) {
			e.printStackTrace();
			throw new TvnException("File path not found in repository for File ID : " + repoFileID);
		}
		return filePath;
	}

	//====================================================================================

	public static String uploadIntoRepository(InputStream diff, String oldFileLocation, String pathHint) 
						throws Exception {
		
		String basePath = pathHint;
		if(basePath.startsWith("/"))
			basePath = basePath.substring(1, basePath.length());
		if(basePath.contains("/"))
			basePath = basePath.substring(0, basePath.indexOf("/"));
		
		File mainFile = prepareFile(diff, oldFileLocation, pathHint);
		
		// TODO: The file location in file_repo_index does not append 
		// 		 the request_id and the action_id. The constructor needs
		// 		 to be passed the ids incase it is required.
		Uploader myUploader = new Uploader();
		myUploader.setFolderHint(basePath);
		AttachmentInfo myAttachmentInfo = myUploader.moveIntoRepository(mainFile);
		mainFile.delete();
		return String.valueOf(myAttachmentInfo.repoFileId);
	}

	//====================================================================================

	public static String getFileRepoID(ArrayList<TvnType> structure) throws TvnException {
		
		if(structure.get(structure.size()-1).identifierHandler == -1)
			throw new TvnException("Unable to get file repo ID");
		return String.valueOf(structure.get(structure.size()-1).identifierHandler);
	}
	
	//====================================================================================

	public static int updateInDB(String activityID, Hashtable<String, Hashtable<String,String>> allProps) throws TvnException {
		
		UpdateRequest update = new UpdateRequest();
		Request result = null;
		int version = 0;
		Enumeration<String> paths = allProps.keys();
        while(paths.hasMoreElements()){
        	String path = paths.nextElement();
        	Hashtable<String, String> requestProps = allProps.get(path);
        	try {			
				String attName = path.substring(path.lastIndexOf("/")+1);
				String parentPath = path.substring(0, path.lastIndexOf("/"));
				String attType = parentPath.substring(parentPath.lastIndexOf("/")+1);
				String requestIdentifier = parentPath.substring(0, parentPath.lastIndexOf("/"));
				String reqSubject = requestIdentifier.substring(requestIdentifier.lastIndexOf("/")+1);
				
				// Properties required for update
				Hashtable<String, String> updateProps = new Hashtable<String, String>();
				// Get the attachment info for the request
				TvnFile requestFile = new TvnFile(requestIdentifier, VersioningServices.getHeadVersion(requestIdentifier));
				ArrayList<TvnType> structure = requestFile.getStructure();
				int sysID = 0;
				Action action = null;
				for(int i=0; i<structure.size(); i++){
					updateProps.put(structure.get(i).identifier, structure.get(i).identifierValue);
					if(structure.get(i).identifier.equals(Field.REQUEST)){
						action = (Action) structure.get(i).identifierObject;
						updateProps.put(structure.get(i).identifier,String.valueOf(action.getRequestId()));
					}
					else {
						if(structure.get(i).identifier.equals(Field.BUSINESS_AREA)){
							sysID = structure.get(i).identifierHandler;
						}
					}
				}
				Hashtable<String, Hashtable<String, AttachmentInfo>> attachmentsInfo = new Hashtable<String, Hashtable<String, AttachmentInfo>>();
				Request request = Request.lookupBySystemIdAndRequestId(action.getSystemId(), action.getRequestId());
				ArrayList <Field> FieldList = Field.lookupBySystemId(sysID, DataType.ATTACHMENTS);
				if(FieldList != null){
					for(int count = 0; count < FieldList.size(); count++){
						Collection<AttachmentInfo> attCollection = request.getAttachmentsOfType(FieldList.get(count).getName());
						if(attCollection != null) {
							Hashtable <String, AttachmentInfo> currentTypeAtts = attachmentsInfo.get(FieldList.get(count).getName());
							if(currentTypeAtts == null)
								currentTypeAtts = new Hashtable<String, AttachmentInfo>();
							for(AttachmentInfo att: attCollection){ 
								currentTypeAtts.put(att.name, att);
							}
							attachmentsInfo.put(FieldList.get(count).getName(), currentTypeAtts);
						}
					}
				}
				
				// Find if the file is to be added or deleted or modified
				String fileAction = requestProps.remove(TvnConstants.FILE_ACTION);
				if(fileAction.equals(TvnConstants.FILE_ADDED)){
					
					// Add attachment information
					AttachmentInfo toBeAdded = new AttachmentInfo();
					toBeAdded.name = attName;
					toBeAdded.requestFileId = 0;
					toBeAdded.repoFileId = Integer.parseInt(requestProps.remove(TvnConstants.RUUID));
					toBeAdded.size = Integer.parseInt(requestProps.remove(TvnConstants.SIZE));
					
					if(attachmentsInfo.get(attType) == null){
						attachmentsInfo.put(attType, new Hashtable<String, AttachmentInfo>());
					}
					attachmentsInfo.get(attType).put(toBeAdded.name, toBeAdded);
				}
				else if(fileAction.equals(TvnConstants.FILE_DELETED)){
					
					attachmentsInfo.get(attType).remove(attName);
				}
				else if(fileAction.equals(TvnConstants.FILE_MODIFIED)){
					
					// Make entry for modified content
					Hashtable<String, AttachmentInfo> tempTable = attachmentsInfo.get(attType);
					AttachmentInfo toBeModified = tempTable.get(attName);
					toBeModified.repoFileId = Integer.parseInt(requestProps.remove(TvnConstants.RUUID));
					toBeModified.size = Integer.parseInt(requestProps.remove(TvnConstants.SIZE));
				}
				
				// Prepare params for updateRequest
				// Make collections of all types of attachments present in this request
				Enumeration<String> allKeys = attachmentsInfo.keys();
				while(allKeys.hasMoreElements()){
					String currentKey = allKeys.nextElement();
					Hashtable<String, AttachmentInfo> tempTable = attachmentsInfo.get(currentKey);
					Collection<AttachmentInfo> tempColl = tempTable.values();
					if(tempColl != null)
						updateProps.put(currentKey, AttachmentInfo.toJson(tempColl));
				}
				// Add remaining properties in requestProps to updateProps
				// Translate all the TVN type property names to tBits type property names
				Enumeration<String> remainingProps = requestProps.keys();
				while(remainingProps.hasMoreElements()){
					String currProp = remainingProps.nextElement();
					String currVal = requestProps.remove(currProp);
					if(currProp.startsWith(TvnConstants.SECRET))
						currProp = currProp.substring(TvnConstants.SECRET.length(), currProp.length());
					else if(currProp.startsWith(UserDefinedData.getUserPrefix()))
						currProp = currProp.substring(UserDefinedData.getUserPrefix().length(), currProp.length());
					else if(currProp.equals(TvnConstants.USER))
						currProp = Field.USER;
					else if(currProp.equals(TvnConstants.COMMENT))
						currProp = Field.DESCRIPTION;
					updateProps.put(currProp, currVal);
				}
				updateProps.put(TVN_NAME, reqSubject);
				updateProps.put("from","client");
				updateProps.put(VERSION_NUM, String.valueOf(version));
	        	
				result = update.updateRequest(updateProps);
				version = result.getVersion();
	        }
        	catch (Exception e) {
    			e.printStackTrace();
    			throw new TvnException(Error.UNABLE_TO_SET_UPDATE_PARAMS + " Path : " + path);
    		} 
    		catch (APIException apie) {
    			apie.printStackTrace();
    			throw new TvnException(Error.UNABLE_TO_UPDATE_REQUEST + " Path : " + path);
    		}
		} 
		
		return version;
	}

	//====================================================================================

	/**
	 * Fetches all the log information related to the given path between the specified versions. 
	 * The limit specifies the maximum number of revisions returned.
	 */
	public static ArrayList<TvnLogEntry> getAllLogItems(String pathInfo, int maxVersion, int minVersion, int limit) 
						throws TvnException {
		
		ArrayList<TvnLogEntry> logItems = new ArrayList<TvnLogEntry>();
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			String query = 	"select v.attachment as attachment, v.file_action as file_action, "+
							"v.request_id as request_id, v.action_id as action_id, "+
							"v.tvn_name as subject, f.name as att_type, "+
							"a.lastupdated_datetime as lastupdated_datetime, "+
							"a.description as description, a.user_id as user_id from "+
							"versions v join fields f "+
							"on v.sys_id = f.sys_id and v.field_id = f.field_id "+
							"join actions a "+
							"on a.sys_id = v.sys_id and a.request_id = v.request_id "+
							"and a.action_id = v.action_id and v.version_no=?";
			int sys_id = 0;
			int request_id = 0;
			String BAname = null;
			TvnFile tempFile = new TvnFile(pathInfo, maxVersion);
			ArrayList<TvnType> structure = tempFile.getStructure();
			for(int i=0; i<structure.size(); i++){
				if(structure.get(i).identifier.equals(Field.BUSINESS_AREA)){
					sys_id = ((BusinessArea) structure.get(i).identifierObject).getSystemId();
					BAname = structure.get(i).identifierValue;
					query += " and v.sys_id=?";
				}
				else if(structure.get(i).identifier.equals(Field.REQUEST)){
					request_id = ((Action) structure.get(i).identifierObject).getRequestId();
					query += " and v.request_id=?";
				}
			}
			PreparedStatement ps = conn.prepareStatement(query);
			if(sys_id > 0)
				ps.setInt(2, sys_id);
			if(request_id > 0)
				ps.setInt(3, request_id);
			
			int currVersion = maxVersion;
			while(limit != 0 || currVersion >= minVersion){
				// Get all actions for this version
				ps.setInt(1, currVersion);
				ResultSet rs = ps.executeQuery();
				if(rs != null){
					// Form the log entry object
					rs.next();
					User creator = User.lookupAllByUserId(rs.getInt("user_id"));
					String date = TvnConstants.FORMAT.format(rs.getDate("lastupdated_datetime"));
					String comment = rs.getString("description");
					TvnLogEntry log_entry = new TvnLogEntry(String.valueOf(currVersion), creator.getUserLogin(), date, comment);
					// Find the added, deleted and modified paths
					do{
						String attName = rs.getString("attachment");
						String fileAction = rs.getString("file_action");
						int reqID = rs.getInt("request_id");
						// Form the relative path from the provided path
						Request request = Request.lookupBySystemIdAndRequestId(conn, sys_id, reqID);
						String path = 	BAname + formPath(pathInfo, request) + rs.getString("subject") + 
										"/" + rs.getString("att_type") + "/" + attName;
						String header = pathInfo;
						if(header.startsWith("/"))
							header = header.substring(1);
						int lSlash = header.lastIndexOf("/");
						if(lSlash != -1)
							header = header.substring(0, lSlash);
						if(path.contains(header)){
							int startIndex = path.indexOf(header);
							if(startIndex != -1)
							path = path.substring(startIndex + header.length());
						}
						// Check the file action and add to the corresponding list
						if(fileAction.equals(TvnConstants.FILE_ADDED))
							log_entry.putAddedPath(path);
						else if(fileAction.equals(TvnConstants.FILE_DELETED))
							log_entry.putDeletedPath(path);
						else if(fileAction.equals(TvnConstants.FILE_MODIFIED))
							log_entry.putModifiedPath(path);
					}while(rs.next());
					// Add log entry to the list of log items
					logItems.add(log_entry);
				}
				limit--;
				currVersion--;
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		
		return logItems;
	}
	
	/**
	 * @param request
	 * @return Path for the given request based on expected folder structure
	 */
	public static String createPathFor(int sysId, int requestId, int attFieldId){
		
		String path = null;
		
		Connection conn = null;
		try{
			
			if(sysId == 0)
				return path;
			
			BusinessArea ba = BusinessArea.lookupBySystemId(sysId);
			
			if(requestId == 0)
				return ba.getSystemPrefix();
			
			Request request = Request.lookupBySystemIdAndRequestId(sysId, requestId);
			if(request == null)
				throw new Exception("Request not found for sysId : "+sysId+" and requestId : "+requestId+".");
			
			ArrayList<TvnType> structure = readFolderStructure(ba.getSystemPrefix());
			StringBuilder pathBuilder = new StringBuilder();
			for(int i=0; i<structure.size(); i++){
				String lookUpVal = structure.get(i).identifier;
				if(lookUpVal.equals(Field.BUSINESS_AREA)){
					// Set the BUSINESS_AREA type
					pathBuilder.append(ba.getSystemPrefix());
				}
				else if(lookUpVal.equals(Field.REQUEST)){
					// Set the tvnName
					conn = DataSourcePool.getConnection();
					conn.setAutoCommit(false);
					
					String sql = "select distinct tvn_name from versions where sys_id = ? and request_id = ?";
					
					PreparedStatement ps = conn.prepareStatement(sql);
					ps.setInt(1, request.getSystemId());
					ps.setInt(2, request.getRequestId());
					ResultSet rs = ps.executeQuery();
					if(!rs.next()){
						return null;
					}
					
					pathBuilder.append("/");
					pathBuilder.append(rs.getString(1));
					
					rs.close();
					
					if(attFieldId == 0)
						break;
					
					// Now look for attachment type
					i++;
					if(i<structure.size()){
						Field f = Field.lookupBySystemIdAndFieldId(sysId, attFieldId);
						if(f.getDataTypeId() != DataType.ATTACHMENTS)
							break;
						
						pathBuilder.append("/");
						pathBuilder.append(f.getName());
					}
				}
				else {
					// Set the fixed and extended fields
					pathBuilder.append("/");
					pathBuilder.append(request.get(lookUpVal));
				}
			}
			
			conn.commit();
			path = pathBuilder.toString();
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println("Unable to fetch information from the database.");
			try{
				if(conn!=null)
					conn.rollback();
			}
			catch(SQLException sqlex1){
				sqlex1.printStackTrace();
				System.out.println("Unable to roll back.");
			}
			return null;
		} 
		finally{
			try {
				if(conn != null)
					conn.close();
			} 
			catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Unable to close connection.");
			}
		}
		
		return path.trim();
	}
	
//	/**
//	 * @param request
//	 * @param version
//	 * @return TvnFile corresponding to the given Request
//	 * @throws TvnException
//	 */
//	public static TvnFile createTvnFileFor(Request request, int version) throws TvnException{
//		
//		String path = createPathFor(request);
//		
//		if(path == null || path.equals(""))
//			return null;
//		return new TvnFile(path, version);
//	}

	//====================================================================================

	// Utility Methods
	
	private static String formPath(String pathInfo, Request request) throws TvnException {
		
		String retPath = "/";
		ArrayList<TvnType> structure = readFolderStructure(pathInfo);
		for(int i=1; i<structure.size(); i++){
			if(structure.get(i).identifier.equals(Field.REQUEST)){
				break;
			}
			else{
				retPath += request.get(structure.get(i).identifier) + "/";
			}
		}
		return retPath;
	}

	public static Action lookupByParams(Connection conn,int aSystemId,String tvnName,int version) throws DatabaseException, TBitsException {
		
		try {
		
			String sql = "select max(action_id), request_id from versions where sys_id = ? and tvn_name = ? and version_no <= ? group by request_id";
			
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, aSystemId);
			ps.setString(2, tvnName);
			ps.setInt(3, version);
			ResultSet rs = ps.executeQuery();
			if(!rs.next()){
				return null;
			}
			int action_id = rs.getInt(1);
			int aRequestId = rs.getInt(2);
			Action action = Action.lookupBySystemIdAndRequestIdAndActionId(aSystemId, aRequestId, action_id);
			rs.close();
			return action;
		}
		catch (SQLException e) {
			throw new TBitsException("Error in finding Head Revision: " + e.toString(), e);
		} 
		catch (DatabaseException e) {
			throw new TBitsException("Error in finding Head Revision: " + e.toString(), e);
		}
	}
	
	public static AttachmentInfo getAttachment(Action action, String attachmentName) 
												throws TBitsException, DatabaseException {

		if(attachmentName == null || attachmentName == "")
			return null;
		
		String aAttachments = action.getAttachments();
		if(aAttachments == null || aAttachments == "") 
			return null;
		
		for(AttachmentInfo myAtt:AttachmentInfo.fromJson(aAttachments)){
		if(myAtt.name.equals(attachmentName)) 
			return myAtt;
		}
		return null;
	}
	
	public static AttachmentInfo getAttachmentOfType(Action action, String attachmentName, int fieldID) throws DatabaseException{
		if(attachmentName == null || attachmentName == "")
			return null;
		Hashtable<Integer, Collection<ActionFileInfo>> allActionFiles = Action.getAllActionFiles(action.getSystemId(), action.getRequestId());
		Collection<ActionFileInfo> requiredActionFiles = allActionFiles.get(action.getActionId());
		if(requiredActionFiles == null)
			return null;
		else{
			for(ActionFileInfo currActionFile: requiredActionFiles){
				if(currActionFile.getName().equals(attachmentName) && currActionFile.getFieldId() == fieldID){
					AttachmentInfo returnAtt = new AttachmentInfo(currActionFile.getName(), (int)currActionFile.getFileId(),
													currActionFile.getRequestId(), currActionFile.getSize());
					return returnAtt;
				}
			}
			return null;
		}
	}
	
	public static Hashtable<String, Object> getActionAndFileActionsOfType(Connection connection,
			int systemId, int requestId, int verNum, String attachmentName, int fieldID) {

		Action action = null;
		Hashtable<String, Object> params = new Hashtable<String, Object>();

		try {
			String sql = "SELECT action_id,file_action from versions " +
					"where sys_id = ? " +
					"and request_id = ? " +
					"and version_no <= ? " +
					"and attachment = ? "+
					"and field_id = ? order by action_id desc";
			
			PreparedStatement ps = connection.prepareStatement(sql);

			ps.setInt(1, systemId);
			ps.setInt(2, requestId);
			ps.setInt(3, verNum);
			ps.setString(4, attachmentName);
			ps.setInt(5, fieldID);
			
			ResultSet rs = ps.executeQuery();
			
			
			if(null != rs) {
				if(rs.next()) {
					int actionId = rs.getInt(1);
					String fileAction = rs.getString(2);
					action = Action.lookupBySystemIdAndRequestIdAndActionId(systemId,
																			requestId,
																			actionId);
					
					
					if(null != fileAction)
						params.put(TvnConstants.FILE_ACTION, fileAction);
					if(null != action)
						params.put(Field.ACTION, action);
				}
				rs.close();
			}
			ps.close();
			ps = null;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return params;
	}
	
	public static Hashtable<Action,String> getActionsAndTVNName(Connection conn,int systemId,
			int verNum) {
		try {
			Hashtable<Action, String> actionAndTVNNames = new Hashtable<Action, String>();
			Action action = null;
			
			String sql = "select v.request_id, v.action_id, v.tvn_name from versions v where action_id = " +
			"(select max(w.action_id) from versions w where v.request_id = w.request_id and v.sys_id = w.sys_id" +
			" and w.version_no <= ? group by w.request_id) and v.sys_id = ?";
			
			PreparedStatement cs = conn.prepareStatement(sql);
			cs.setInt(1,verNum);
			cs.setInt(2, systemId);

			ResultSet rs = cs.executeQuery();
			while(rs.next()) {
				int requestId = rs.getInt(1);
				int actionId = rs.getInt(2);
				String tvnName = rs.getString(3);
				action = Action.lookupBySystemIdAndRequestIdAndActionId(conn,systemId,requestId,actionId);
				if(null == action) {
					System.out.println("Null action obtained: ");
					System.out.println(systemId + ":" + requestId + ":" + actionId);
					continue;
				}
				if(null == tvnName || tvnName.length() == 0) {
					System.out.println("Null TVN Name obtained");
					System.out.println(systemId + ":" + requestId + ":" + actionId);
					continue;
				}
				actionAndTVNNames.put(action, tvnName);
			}
			rs.close();
			cs.close();
			return actionAndTVNNames;
		}
		catch (Exception e) {
			System.err.println(e.toString());
			e.printStackTrace();
		}
		return null;
	}
	
	public static Hashtable<AttachmentInfo, Action> getActionAndAttachmentsAtVer(Connection conn,
			int systemId, int requestId, int fieldId, int verNum) {
		try {
			Hashtable<AttachmentInfo, Action> actionAndAttachments = new Hashtable<AttachmentInfo, Action>();
			
			String sql = "select X.attachment,X.action_id " +
							"from (SELECT  attachment,max(action_id) as action_id, field_id " +
							"from versions where sys_id = ? and request_id = ? " +
							"and version_no <= ? GROUP BY field_id, attachment)X" +
							",versions V where V.action_id=X.action_id and V.attachment=X.attachment and V.field_id=X.field_id " +
							"and V.sys_id = ? and V.request_id = ? and V.field_id = ? and not (V.file_action = 'D')";
			
			PreparedStatement cs = conn.prepareStatement(sql);
			cs.setInt(1,systemId);
			cs.setInt(2, requestId);
			cs.setInt(3, verNum);
			cs.setInt(4, systemId);
			cs.setInt(5, requestId);
			cs.setInt(6, fieldId);
			
			ResultSet rs = cs.executeQuery();
			while(rs.next()) {
				Action action = Action.lookupBySystemIdAndRequestIdAndActionId(systemId, requestId, rs.getInt("action_id"));
				String attName = rs.getString("attachment");
				AttachmentInfo attachment = new AttachmentInfo();
				attachment.name = attName;
				if(null != action && null != attachment)
					actionAndAttachments.put(attachment,action);
			}
			rs.close();
			cs.close();
			return actionAndAttachments;
			
		} catch (SQLException e) {
			System.err.println("Error in getting action and " +
					"attachments for [sys_id,request_id,version]" +
					" [" + systemId + "," + requestId + "," + verNum + "]");
			e.printStackTrace();
		} catch (DatabaseException e) {
			System.err.println("Error in getting action and " +
					"attachments for [sys_id,request_id,version]" +
					" [" + systemId + "," + requestId + "," + verNum + "]");
			e.printStackTrace();
		}
	
		return null;
	}

	/**
	 * Prepares and returns the file to be uploaded. The file is used for comparing the checksum.
	 */
	private static File prepareFile(InputStream is, String sourceLocation, String path) throws IOException, TvnException{
		
		int size = DeltaUtil.FILE_SIZE_FOR_TRANSFER;
		int read = 0;
		byte[] tempBytes = new byte[size];
		TVNDeltaReader reader = new TVNDeltaReader();
		TVNDiffWindow window = null;
		File mainFile = new File(UserDefinedData.getTempFolderPath() + path);
		// First create parent directories
		File parentDir = mainFile.getParentFile();
		parentDir.mkdirs();
		// Create the main file
		if(mainFile.exists())
			mainFile.delete();
		mainFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(mainFile);
		FileInputStream sourceInput = null;
		if(sourceLocation != null) {
			File file = new File(sourceLocation);
			sourceInput = new FileInputStream(file);
		}
		
		long myLastOffset = 0;
		int myLastSourceLength = 0;
		while((read = is.read(tempBytes, 0, size)) >= 0) {
			window = reader.nextWindow(tempBytes,0, read);
			while(null != window) {
				byte[] sourceBuffer = new byte[0];
				if(null != sourceInput) {
					sourceBuffer = new byte[window.getSourceViewLength()];
					int myoffset = (int)(window.getSourceViewOffset() - myLastOffset - myLastSourceLength);
					if(myoffset < 0) {
						throw new TvnException("SVN-Diff has sliding-backwards view");
					}
					sourceInput.read(sourceBuffer,myoffset, window.getSourceViewLength());
					myLastOffset = window.getSourceViewOffset();
					myLastSourceLength = window.getSourceViewLength();
				}
				byte[] targetBuffer = new byte[window.getTargetViewLength()];
				window.apply(sourceBuffer, targetBuffer);
				fos.write(targetBuffer,0,targetBuffer.length);
				window = reader.nextWindow(new byte[0],0,0);
			}
		}
		
		fos.flush();
		fos.close();
		
		return mainFile;
	}

	public static HashMap<String, Integer> getParamsFromPath(String path) throws TvnException {

		HashMap<String, Integer> params = new HashMap<String, Integer>();
		TvnFile file = new TvnFile(path, VersioningServices.getHeadVersion(path));
		for(TvnType type : file.getStructure()){
			if(type.identifier.equals(Field.BUSINESS_AREA))
				params.put(LocksServices.SYS_ID, type.identifierHandler);
			else if(type.identifier.equals(Field.REQUEST))
				params.put(LocksServices.REQUEST_ID, type.identifierHandler);
			else if(type.identifier.equals("attachment_type_id"))
				params.put(LocksServices.FIELD_ID, type.identifierHandler);
			else if(type.identifier.equals("attachment_id")){
				params.put(LocksServices.REQUEST_FILE_ID, Integer.parseInt(type.utilityHandlers.get(LocksServices.REQUEST_FILE_ID)));
			}
		}
		
		return params;
	}

	//====================================================================================

}
