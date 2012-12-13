package transbit.tbits.TVN;

import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.catalina.util.MD5Encoder;
import org.apache.catalina.util.URLEncoder;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

import com.tbitsglobal.tvncore.Error;

/**
 * 
 * @author Abhishek Agarwal
 * Assumptions made: One action cannot have more than one file
 * with same name
 * TODO: IMP. STORE ALL DATABASE queries to some other place
 * make stored procedures for them
 * TODO: ALL SQL Queries will be changed, so they are hard coded here 
 * Column Names and table Names must be at a single place
 * Must be moved at a common Place as soon as the system is tested
 * TODO: BA list is not going to be shown to user
 * TODO: These functions are totally messed up..
 */
public class WebdavUtil implements WebdavConstants{

	private static Hashtable<String, Integer> HeadVersions = new Hashtable<String, Integer>();
	
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
	
	/**
	 * This method returns the action for given parameters
	 * Currently this method will return null if
	 * No attachment is present
	 * or 
	 * attachment was deleted at this particular action
	 * @param systemId
	 * @param requestId
	 * @param verNum
	 * @param attachmentName
	 * @return
	 */
	public static Hashtable<String, Object> getActionAndFileActions(Connection connection,
			int systemId, int requestId, int verNum, String attachmentName) {

		Action action = null;
		Hashtable<String, Object> params = new Hashtable<String, Object>();

		try {
			String sql = "SELECT action_id,file_action from versions " +
					"where sys_id = ? " +
					"and request_id = ? " +
					"and version_no <= ? " +
					"and attachment = ? order by action_id desc";
			
			PreparedStatement ps = connection.prepareStatement(sql);

			ps.setInt(1, systemId);
			ps.setInt(2, requestId);
			ps.setInt(3, verNum);
			ps.setString(4, attachmentName);
			
			ResultSet rs = ps.executeQuery();
			
			
			if(null != rs) {
				if(rs.next()) {
					int actionId = rs.getInt(1);
					String fileAction = rs.getString(2);
					action = Action.lookupBySystemIdAndRequestIdAndActionId(systemId,
																			requestId,
																			actionId);
					
					
					if(null != fileAction)
						params.put(FILE_ACTION, fileAction);
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
	
	// Same as getActionAndFileActions function above but works for specified attachment type
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
						params.put(FILE_ACTION, fileAction);
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
	
	/*public static ArrayList<String> getSubFolders(User user, String currentPath) 
									throws DatabaseException,  TBitsException {
		int userId = user.getUserId();
		int headVersion = WebdavUtil.getHeadRevision(currentPath);
		return getSubFolders(user, currentPath,headVersion);
		
	}*/
	
	public static Hashtable<AttachmentInfo, Action> getActionAndAttachmentsAtVer(Connection conn,
			int systemId, int requestId, int fieldId, int verNum) {
		try {
			Hashtable<AttachmentInfo, Action> actionAndAttachments = new Hashtable<AttachmentInfo, Action>();
//			System.out.println("Attempting to read Action From database");
//			System.out.println("Inside getActionAndAttachmentsAtVer()");
			
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
				//AttachmentInfo attachment = getAttachment(action, attName);
				if(null != action && null != attachment)
					actionAndAttachments.put(attachment,action);
			}
			rs.close();
			cs.close();
//			System.out.println("Successfully Read from DB");
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
	
	public static Hashtable<AttachmentInfo, Action> getActionAndAttachmentsAtVer(Connection conn,
			int systemId, int requestId, int verNum) {
		try {
			Hashtable<AttachmentInfo, Action> actionAndAttachments = new Hashtable<AttachmentInfo, Action>();
//			System.out.println("Attempting to read Action From database");
//			System.out.println("Inside getActionAndAttachmentsAtVer()");
			
			String sql = WebdavConstants.SELECT_ATTACHMENT_AND_ACTION_AT_VERSION;
			PreparedStatement cs = conn.prepareStatement(sql);
			cs.setInt(1,systemId);
			cs.setInt(2, requestId);
			cs.setInt(3, verNum);
			cs.setInt(4, systemId);
			cs.setInt(5, requestId);
			
			ResultSet rs = cs.executeQuery();
			while(rs.next()) {
				Action action = Action.lookupBySystemIdAndRequestIdAndActionId(systemId, requestId, rs.getInt("action_id"));
				AttachmentInfo attachment = getAttachment(action, rs.getString("attachment"));
				if(null != action && null != attachment)
					actionAndAttachments.put(attachment,action);
			}
			rs.close();
			cs.close();
//			System.out.println("Successfully Read from DB");
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
		} catch (TBitsException e) {
			System.err.println("Error in getting Attachment From Action");
			e.printStackTrace();
		}
//		finally
//		{
//			if(conn != null)
//			{
//				try {
//					conn.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
	
		return null;
	}

	public static Hashtable<Action,String> getActionsAndTVNName(Connection conn,int systemId,
			int verNum) {
		try {
			Hashtable<Action, String> actionAndTVNNames = new Hashtable<Action, String>();
			Action action = null;
			
//			System.out.println("Attempting to Read from " +
//					"DB in getMaxRequestIdAtVer");
			
			String sql = "select v.request_id, v.action_id, v.tvn_name from versions v where action_id = " +
			"(select max(w.action_id) from versions w where v.request_id = w.request_id and v.sys_id = w.sys_id" +
			" and w.version_no <= ? group by w.request_id) and v.sys_id = ?";
			
			/*sql = "select distinct x.request_id,x.action_id,v.tvn_name from " +
					"(select request_id,max(action_id) action_id from versions " +
					"where sys_id = ? and version_no <= ? group by request_id)x " +
					",versions v where v.request_id = x.request_id " +
					" and v.sys_id = ? " +
					"and v.action_id = 1 order by x.request_id DESC";*/
			PreparedStatement cs = conn.prepareStatement(sql);
			cs.setInt(1,verNum);
			cs.setInt(2, systemId);

			ResultSet rs = cs.executeQuery();
//			System.out.println("Successfully Read the properties");
			while(rs.next()) {
				int requestId = rs.getInt(1);
				int actionId = rs.getInt(2);
				String tvnName = rs.getString(3);
				action = Action.lookupBySystemIdAndRequestIdAndActionId(systemId,
																			requestId,actionId);
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
//		finally
//		{
//			if(conn != null)
//			{
//				try {
//					conn.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
		return null;
	}

	public static DocumentBuilder getDocumentBuilder()
    throws TBitsException {
	    DocumentBuilder documentBuilder = null;
	    DocumentBuilderFactory documentBuilderFactory = null;
	    try {
	        documentBuilderFactory = DocumentBuilderFactory.newInstance();
	        documentBuilderFactory.setNamespaceAware(true);
	        documentBuilder = documentBuilderFactory.newDocumentBuilder();
	    } catch(ParserConfigurationException e) {
	        throw new TBitsException
	            ("Error in Parsing Configuration");
	    }
	    return documentBuilder;
	}
	
	public static String getRelativePath(HttpServletRequest aRequest) {
		String path = aRequest.getPathInfo();
		if(path == null) {
			path = "";
		}
		path = URLDecoder.decode(path);
		//String servletPath = aRequest.getServletPath();
		return path;
	}

	/**
	 * reform path will delete the portion of special svn url from
	 * any path..
	 */
	public static String reformPath(String path) {
		if(null != path)
			path = path.replaceAll(WebdavConstants.SVN_SHORT_URL + "/[^/.]*/[^/.]*/?","");
		return path;
	}
	
	public static String getSvnUrlId(String pathInfo) {
		String svnUrl = WebdavConstants.SVN_SHORT_URL;
		if(pathInfo != null) 
			if(pathInfo.contains(svnUrl)) 
				return pathInfo.replaceAll(".*/" + svnUrl + "/[^/.]*/","")
									.replaceAll("/.*", "");
		
		return pathInfo;
	}

	public static Integer getVersionOfAttachment(Connection conn,
			int systemId, int requestId, int actionId) {
		try {
			String sql = "SELECT distinct version_no from versions "
					+ "where sys_id = ? and "
					+ "request_id = ? and action_id = ? ";
			PreparedStatement cs = conn.prepareStatement(sql);
			cs.setInt(1, systemId);
			cs.setInt(2, requestId);
			cs.setInt(3, actionId);

			ResultSet rs = cs.executeQuery();

			// System.out.println("Successfully read From this function");

			if (rs.next()) {
				int verNum = rs.getInt(1);
				rs.close();
				return verNum;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * This function will return the last version, at which request was committed
	 * if at this revision, request was deleted, it will return -1
	 * if the request does not even exists at this revision, it will return 0
	 * 
	 * @param request
	 * @param verNum
	 * @return
	 */
	public static int getLastVersion(Connection conn,int systemId,int requestId,int verNum) {

		try {
//			System.out.println("Attempting to read Action From database");
//			System.out.println("[Inside getLastCommitVersionOfRequest() ");
			/*
			 * TODO: if the request is not present in last version
			 * its version will be considered equal to 1
			 * I can think of two possible solutions
			 * assume that every request of a versionable BA will 
			 * be in versions table and if not,take its version as 1
			 * otherwise you pick all the requests from the 
			 * versions table itself !!!!!!
			 */
			String sql = "SELECT  max(version_no) as version  from versions " +
			"where sys_id = ? and " +
			"request_id = ? and version_no <= ? GROUP BY request_id";
			PreparedStatement cs = conn.prepareStatement(sql);
			cs.setInt(1, systemId);
			cs.setInt(2,requestId);
			cs.setInt(3, verNum);

			ResultSet rs = cs.executeQuery();

			if(rs.next()) {
				int lastVersion = rs.getInt(1);
				rs.close();
				cs.close();
				return lastVersion;
			}
			else {
				rs.close();
				cs.close();
				return 0;
			}
			
		}
		catch (Exception e) {
			System.err.println(e.toString());
			e.printStackTrace();
			return 0;
		}
//		finally
//		{
//			if(conn != null)
//			{
//				try {
//					conn.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
	}

	public static int getHeadRevision(String path) throws TBitsException {
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
		
		conn.setAutoCommit(false);
		int headRevision = getHeadRevision(conn, path);
		conn.commit();
		return headRevision;
		} catch (SQLException e) {
			try {
				if(conn != null)
					conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new TBitsException("Error getting head revision", e);
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
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
	}
	
	public static int getHeadRevision(Connection conn,String path) throws TBitsException {
		if(path == null)
			throw new TBitsException(Error.NO_PATH_SUPPLIED);
		
		/*  First get the sys_prefix from the path */
		path = reformPath(path);		//Removes any special URI if there are any
		StringTokenizer st = new StringTokenizer(path, "/\\");
		if(!st.hasMoreTokens())
			throw new TBitsException("No sys_prefix found.");
		String sys_prefix = st.nextToken().toLowerCase();	//to lower case will solve the problem 
															// regarding case of system prefix
		
		if(null == HeadVersions)
			HeadVersions = new Hashtable<String, Integer>();
		
		if(null != HeadVersions.get(sys_prefix)) {
			return HeadVersions.get(sys_prefix);
		}
		else {
			loadHeadVersion(conn,sys_prefix);
			return HeadVersions.get(sys_prefix);
		}
		
	}

	public static int updateVersion(Request result,ArrayList<FileAction> fileActions,
										boolean newRequest,String tvnName,int newVersion, boolean fromWeb) 
	throws TBitsException {
		Connection connection = null;
		int newVersionNum;
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			newVersionNum = updateVersion(connection, result, fileActions,newRequest, tvnName,newVersion,fromWeb);
			connection.commit();
		} catch (SQLException sqle) {
			try {
				if(connection != null)
					connection.rollback();
			} catch (SQLException sqle1) {
				sqle1.printStackTrace();
			}
			throw new TBitsException(sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
				}
			}
			connection = null;
		}
		return newVersionNum;
	}

	public static int incrAndGetNewVersion(int sysId) throws DatabaseException
	{
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();

			CallableStatement cs = conn
					.prepareCall("stp_ba_incrAndGetVersionNumber ?");
			cs.setInt(1, sysId);
			ResultSet rs = cs.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new DatabaseException("Error loading properties", e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * substitute the following characters /\:*?"<>| with _
	 * also there is some problem with !
	 * @param str
	 * @return
	 */
	public static String tr2WindowsFileName(String str)
	{
		str = str.replaceAll("[/\\\\:*?\"<>|!]+", "_");
		if(str.length() > 92)
			str = str.substring(0, 92); //size == 92 + 1 for underscore + 7 digits for request_id
		return str;
	}

	public static int updateVersion(Connection conn, Request result,ArrayList<FileAction> fileActions,
												boolean newRequest,String tvnName,int newVersion, boolean fromWeb) 
																	throws TBitsException {
		return updateVersion(conn, result.getSystemId(), result.getRequestId(), result.getSubject(), result.getMaxActionId(), fileActions, newRequest, tvnName, newVersion, fromWeb);
	}
	
	public static int updateVersion(Connection conn, int sysId, int requestId, String subject, int maxActionId, ArrayList<FileAction> fileActions,
						boolean newRequest, String tvnName, int newVersion, boolean fromWeb) 
											throws TBitsException {

		if(fromWeb)
		{
			tvnName = tr2WindowsFileName(subject) + "_" + requestId;
		}
		if(null == tvnName){
			throw new TBitsException("TVN Name is null");
		}
		try {
			BusinessArea myBusinessArea = BusinessArea.lookupBySystemId(sysId);
			if(newVersion == 0) {
				newVersion = incrAndGetNewVersion(myBusinessArea.getSystemId());
			}
			
			
			String sql = "SELECT v.request_id from actions a,versions v,versions x where " +
			"a.sys_id = ? " +
			"and a.sys_id = v.sys_id and a.request_id = v.request_id " +
			"and a.action_id = v.action_id " +
			"and x.tvn_name = ? " +
			"and x.request_id = v.request_id " +
			"and x.action_id = (select max(action_id) from versions where sys_id = ? and request_id = x.request_id) " +
			"and v.version_no <= ?" +
			"and v.request_id <> ?";
			
//			PreparedStatement ps = conn.prepareStatement(sql);
//			ps.setInt(1, sysId);
//			ps.setString(2, tvnName);
//			ps.setInt(3, sysId);
//			ps.setInt(4, newVersion);
//			ps.setInt(5, requestId);
//			ResultSet rs = ps.executeQuery();
//			
//			if(rs.next()){
//				return -1;
//			}
			
			sql = "INSERT INTO versions(sys_id, request_id, " +
			"action_id, attachment, version_no, file_action, field_id, request_file_id, file_id, tvn_name) Values(?,?,?,?,?,?,?,?,?,?)"; 
			
			PreparedStatement cs = conn.prepareStatement(sql);
			if(fileActions.size() == 0) 
			{
				if(newRequest) {
					cs.setInt(1, sysId);
					cs.setInt(2, requestId);
					cs.setInt(3, maxActionId);
					cs.setString(4, "");
					cs.setInt(5, newVersion);
					cs.setString(6, FILE_ADDED);
					cs.setInt(7, 0);
					cs.setInt(8, 0);
					cs.setInt(9, 0);
					cs.setString(10, tvnName);
					cs.execute();
				}
				else {
					cs.setInt(1, sysId);
					cs.setInt(2, requestId);
					cs.setInt(3, maxActionId);
					cs.setString(4, "");
					cs.setInt(5, newVersion);
					cs.setString(6, FILE_MODIFIED);
					cs.setInt(7, 0);
					cs.setInt(8, 0);
					cs.setInt(9, 0);
					cs.setString(10, tvnName);
					cs.execute();
				}
			}
			else {
				for(FileAction fileAction:fileActions)
				{
					AttachmentInfo ai = fileAction.getAttachmentInfo();
					cs.setInt(1, sysId);
					cs.setInt(2, requestId);
					cs.setInt(3, maxActionId);
					cs.setString(4, ai.name);
					cs.setInt(5, newVersion);
					cs.setString(6, fileAction.getFileAction());
					cs.setInt(7, fileAction.getFieldId());
					cs.setInt(8, ai.requestFileId);
					cs.setInt(9, ai.repoFileId);
					cs.setString(10, tvnName);
					cs.execute();
				}
			}
			
			loadHeadVersion(conn, myBusinessArea.getSystemPrefix());
			return newVersion;
		}
		 catch (SQLException e) {
			System.err.println("Error in updating the Versions Information");
			e.printStackTrace();
			throw new TBitsException(e);
		} catch (DatabaseException e) {
			System.err.println("Error in obtaining the BusinessArea");
			throw new TBitsException(e);
		}
//		finally
//		{
//			if(conn != null)
//			{
//				try {
//					conn.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
		
	}
	
	private static String generateTvnName(Request result) {
		String modifiedSub = result.getSubject().replaceAll("[^0-9_ a-zA-Z]*", "");
		if(modifiedSub.length() > MAX_TVN_NAME_LENGTH)
			modifiedSub = modifiedSub.substring(0, MAX_TVN_NAME_LENGTH);
		String TVNName = modifiedSub + "_" + result.getRequestId();
		return TVNName;
	}

	/**
	 * 
	 * @param pathInfo
	 * @param startRevision
	 * @param endRevision
	 * @param limit
	 * @return
	 * @throws TBitsException
	 * @throws DatabaseException
	 * @throws SQLException
	 * 
	 * Currently this function is will take much higher time to
	 * execute....totally wrong implementation...
	 * what can be done: 
	 * Get all the committed versions of this path
	 * start from the first one
	 * get the subfolders at first revision-> All the sub directories will be marked 
	 * as added
	 * Go to next version-> Get All the subfolders and if new folder or file is added,
	 * Add it to the added-path otherwise add it to modified-path
	 * see its so simple
	 * but we are talking about efficiency and I am not Donald Erwin knuth
	 * 
	 */
	
	public static int loadHeadVersion(Connection conn,String path) throws TBitsException {
		
		if(null == path)
			throw new TBitsException("No Path Supplied");
		
		/*  First get the sys_prefix from the path */
		path = reformPath(path);		//Removes any special URI if there are any
		StringTokenizer st = new StringTokenizer(path, "/\\");
		if(!st.hasMoreTokens())
			throw new TBitsException("Cannot Obtain the system prefix.");
		String sys_prefix = st.nextToken();
		try {
			BusinessArea myBusinessArea  = BusinessArea.lookupBySystemPrefix(sys_prefix);
			int sys_id = myBusinessArea.getSystemId();
			String sql = "SELECT  max_version_no version from business_areas where sys_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, sys_id);
			ResultSet rs = ps.executeQuery();
			if(!rs.next()){
				HeadVersions.put(sys_prefix, 0);
				return 0;
			}
			int headRev = rs.getInt(1);
			//if this BA does not contain any requests,its version will be 1
			headRev = (headRev == 0)?1:headRev;
			HeadVersions.put(sys_prefix, headRev);
			rs.close();
			return headRev;
		}
		catch (SQLException e) {
			throw new TBitsException("Error in finding Head Revision: " +
					 e.toString(), e);
		} catch (DatabaseException e) {
			throw new TBitsException("Error in finding Head Revision: " +
					 e.toString(), e);
		}
		
	}
	
	public static Action lookupByParams(Connection conn,int aSystemId, 
												int status, int reqType, int category, 
												String tvnName,int version) 
						throws DatabaseException, TBitsException {
		try {
			/*String sql = "SELECT a.action_id,a.request_id from actions a,versions v,versions x where " +
							"a.sys_id = ? " +
							"and a.status_id = ? " +
							"and a.request_type_id = ? " +
							"and a.category_id = ? " +
							"and a.request_id = v.request_id " +
							"and a.action_id = v.action_id " +
							"and x.tvn_name = ? " +
							"and x.request_id = v.request_id " +
							"and x.action_id = (select max(action_id) from versions where sys_id = ? and request_id = x.request_id and tvn_name = ?) " +
							"and v.version_no <= ?";*/
			
			String sql = "select max(action_id), request_id from versions where sys_id = ? and tvn_name = ? and version_no <= ? group by request_id";
			
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, aSystemId);
//			ps.setInt(2, status);
//			ps.setInt(3, reqType);
//			ps.setInt(4, category);
			ps.setString(2, tvnName);
//			ps.setInt(6, aSystemId);
//			ps.setString(7, tvnName);
			ps.setInt(3, version);
			ResultSet rs = ps.executeQuery();
			if(!rs.next()){
				return null;
			}
			int action_id = rs.getInt(1);
			int aRequestId = rs.getInt(2);
			Action action = Action.lookupBySystemIdAndRequestIdAndActionId
											(aSystemId, aRequestId, action_id);
			rs.close();
			return action;
		}
		catch (SQLException e) {
			throw new TBitsException("Error in finding Head Revision: " +
					 e.toString(), e);
		} catch (DatabaseException e) {
			throw new TBitsException("Error in finding Head Revision: " +
					 e.toString(), e);
		}
		/*finally
		{
			if(conn != null)
			{
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}*/
		
	}

	

	public static String encodeURL(String href) {
		URLEncoder encoder = new URLEncoder();
		encoder.addSafeCharacter('/');
		encoder.addSafeCharacter('.');
		encoder.addSafeCharacter('-');
		encoder.addSafeCharacter('_');
		encoder.addSafeCharacter('*');
		encoder.addSafeCharacter('!');
		encoder.addSafeCharacter('%');
		String encodedUrl = encoder.encode(href);
		return encodedUrl;
	}
	
	public static String encodeString(String lockTokenStr) throws TBitsException {
		try {
			MD5Encoder md5Encoder = new MD5Encoder();
			MessageDigest md5Helper = MessageDigest.getInstance("MD5");
			String lockToken = md5Encoder.encode(md5Helper.digest(lockTokenStr.getBytes()));
			return lockToken;
		} catch (NoSuchAlgorithmException e) {
			throw new TBitsException(e);
		}
	}
	
//====================================================================================
	
	// Correct the path for certain cases when tortoiseSVN sends incorrect path.
	
	public static String correctPath(String path, String svnUrl){
		if(path.endsWith("/"))
			path = path.substring(0, path.length()-1);
		String str = path.substring(path.lastIndexOf("/"));
		if(path.endsWith(svnUrl + str)){
			int in;
			in = path.substring(1).indexOf("/");
			String part1 = path.substring(0, in + 2);
			String temp = path.substring(in + 2);
			in = temp.indexOf(svnUrl + str);
			String part2 = temp.substring(in);
			String part3 = temp.substring(0, in);
			if(!part1.endsWith("/"))
				part1 += "/";
			if(part2.startsWith("/"))
				part2 = part2.substring(1);
			if(!part2.endsWith("/"))
				part2 += "/";
			if(part3.startsWith("/"))
				part3 = part3.substring(1);
			path = part1 + part2 + part3;
		}
		return path;
	}
}

