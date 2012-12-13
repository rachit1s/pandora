//Package webapps

package transbit.tbits.webapps;


/*
 *** What is being done in this class *****
 *Check whether the attachment is present or not 
 *This Class finds the list of all attachments
 *Put these name of attachments into the file frmFiles.html 
 *Two functions in setFile and setCompareFile allows viewer to open or compare the file
 *
 */

/*
 * 
 * 
 * 
 * 
 * 
 */

//import java servlet classes
import javax.servlet.http.HttpServlet; 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


//import java utilities
import java.io.*;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.StringTokenizer;

//imports from tbits

import transbit.tbits.webapps.WebUtil;
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsFileInfo;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;

import transbit.tbits.exception.TBitsException;
import transbit.tbits.Helper.Messages;
import transbit.tbits.config.Attachment;
import transbit.tbits.config.WebConfig;

import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User; 
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;

//static constants and keys

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;
import static transbit.tbits.Helper.TBitsConstants.LEFT_FRAME_NUM;
import static transbit.tbits.Helper.TBitsConstants.APPLET_FRAME_NUM;
import static transbit.tbits.Helper.TBitsPropEnum.JVUESERVER;
import static transbit.tbits.Helper.TBitsPropEnum.JVUECODEBASE;
import static transbit.tbits.Helper.TBitsPropEnum.IS_AUTOVUE_ENABLED;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_ATTACHMENTDIR;
/**
 * 
 * @author Abhishek Agarwal, nitiraj
 *
 */
public class OpenAttachment extends HttpServlet {

	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
//	public static String jvueServer;
//	public static String jvueCodebase;
	public static String isAutovueEnabled;
//	public static String ourAttachmentLocation;
//	public String aAttachments = "";

	public String currentAttachment;
	public static final String SIDE_FRAME_HTML = "jVue/frmFiles.html";
	public static final String MAIN_FRAME_HTML = "jVue/frmApplet.html";
	public static final String MAIN_PAGE_HTML = "jVue/MainPage.html";
	public static final String DETAILS_PAGE_HTML = "jVue/FileDetails.html";


//	static {
//		try {
//			ourAttachmentLocation = Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_ATTACHMENTDIR));
//		}
//		catch(IllegalArgumentException e) {
//			LOG.severe(e.toString(), e);
//		}
//	}




	public void doPost(HttpServletRequest aRequest,HttpServletResponse aResponse) throws ServletException,IOException {

		HttpSession session = aRequest.getSession(true);
		try {
			handleGetRequest(aRequest, aResponse);
		}
		catch(TBitsException e) {
			session.setAttribute("ExceptionObject",e);
			LOG.info("",(e));
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));
		}
		catch(DatabaseException e) {
			session.setAttribute("ExceptionObject",e);
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));
		}
		catch(IOException e) {
			session.setAttribute("ExceptionObject",e);
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));
		}

	}

	public void doGet(HttpServletRequest aRequest,HttpServletResponse aResponse) throws ServletException, IOException {
		doPost(aRequest,aResponse);
	}

	public void handleGetRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws TBitsException, DatabaseException,IOException,ServletException {

		//verify whether autoVue is installed and enabled	
		try {
			isAutovueEnabled = PropertiesHandler.getProperty(IS_AUTOVUE_ENABLED);
//			jvueServer = PropertiesHandler.getProperty(JVUESERVER);
//			jvueCodebase = PropertiesHandler.getProperty(JVUECODEBASE);
		}
		catch(IllegalArgumentException e) {
			throw new TBitsException(Messages.getMessage("HTML_INTERFACE_MISSING"));
		}

		if(isAutovueEnabled.trim().equalsIgnoreCase("true") == false) 
			throw new TBitsException(Messages.getMessage("HTML_INTERFACE_MISSING"));

//		if(jvueServer == null || jvueCodebase == null || jvueServer.equals("") || jvueCodebase.equals(""))
//			throw new TBitsException(Messages.getMessage("HTML_INTERFACE_MISSING"));		

		//validate the user

		User user = WebUtil.validateUser(aRequest);
		String userLogin = user.getUserLogin();

		/* Define the BusinessArea and Request Parameters  */
		String aSysPrefix = "";
		BusinessArea myBusinessArea = null;
		Request request;
		int requestId = 0;
		int    actionId  = -1;
		String filePath = null;
		int fieldId = -1;
		int requestFileId = -1;
		int userId = user.getUserId();


		String pathInfo = aRequest.getPathInfo();
		StringTokenizer st = new StringTokenizer(pathInfo, "/\\");
		if(st.hasMoreTokens() == true) {
			aSysPrefix = st.nextToken();
			myBusinessArea = BusinessArea.lookupBySystemPrefix(aSysPrefix);
		}

		if(myBusinessArea == null) {
			throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
		}
		
		String requestIdStr = null;
		String actionIdStr = null;
		String requestFileIdStr = null;		
		String fieldIdStr = null;
		
		if(st.hasMoreTokens() == true) {
			requestIdStr = st.nextToken();
		}
		
		if(st.hasMoreTokens() == true) {
			actionIdStr = st.nextToken();
		}
		
		if(st.hasMoreTokens() == true) {
			fieldIdStr = st.nextToken();
		}
		
		if(st.hasMoreTokens() == true) {
			requestFileIdStr = st.nextToken();
		}

		int systemId = myBusinessArea.getSystemId();
		
		if ((requestIdStr == null) || (requestFileIdStr == null)) {
			throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
		}
		try {
			requestId = Integer.parseInt(requestIdStr);
		} catch (NumberFormatException nfe) {
			throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
		}

		try {
			requestFileId = Integer.parseInt(requestFileIdStr);
		} catch (NumberFormatException nfe) {
			throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
		}

		try {
			actionId = Integer.parseInt(actionIdStr);
		} catch (Exception e) {
		}
		
		try {
			fieldId = Integer.parseInt(fieldIdStr);
		} catch (Exception e) {
		}	
		// Get the permissions.
		Hashtable<String, Integer> permTable = null;

		try {
			if(actionId == -1) {					
				permTable = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(systemId, requestId, userId);
			} else
				permTable = RolePermission.getPermissionsBySystemIdAndRequestIdAndActionIdAndUserId(systemId, requestId, actionId, userId);

			if (permTable == null) {
				LOG.warn("Permission table is null.");

				throw new Exception("");
			}
		} catch (Exception e) {
			throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
		}


		request  =  Request.lookupBySystemIdAndRequestId(systemId, requestId);
		if(request == null) {
			throw new TBitsException(Messages.getMessage("INVALID_REQUEST_ID"));
		}
		String pageTitle = aSysPrefix.concat("#").concat(String.valueOf(requestId));

		try {
			TBitsFileInfo fi = Uploader.getFileInfo(systemId, requestId, actionId, requestFileId, fieldId);
			filePath =  APIUtil.getAttachmentLocation() + "/" + fi.getFileLocation();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		

		if(!(new File(filePath)).exists())
			throw new TBitsException(Messages.getMessage("NO_ATTACHMENT"));

		//get the output stream associated with Response
		PrintWriter out = aResponse.getWriter();
		aResponse.setContentType("text/html;charset=UTF-8");

		//get the parameter frame from request
		int frameId = 0;
		if(aRequest.getParameter("frame") != null)
			frameId = Integer.parseInt(aRequest.getParameter("frame"));

		String BaseUrl = WebUtil.getServletPath(""); //we need absolute path.

		//---------------------------------	
		StringBuilder sbf = new StringBuilder();
		String displayName = "";
		DTagReplacer hp = null;
		//_____________________________________


		/* Gets the List of all attachments per action 
		 * gets the information related to attachment and show it to the user
		 */	

		switch(frameId) {
		case LEFT_FRAME_NUM :
			int fileCount = 1;
			//int userId;
			User actionUser = null;
			String actionUserName = null;
			Timestamp lastUpdated = null;

			Hashtable<Integer, Collection<ActionFileInfo>> allActionFiles = Action.getAllActionFiles(systemId, requestId);
			for (Integer actId : allActionFiles.keySet()){
				Action attAction = Action.lookupBySystemIdAndRequestIdAndActionId(systemId, requestId, actId);
				userId = attAction.getUserId();
				actionUser = User.lookupByUserId(userId);
				actionUserName = actionUser.getUserLogin();
				lastUpdated = attAction.getLastUpdatedDate();
				Collection<ActionFileInfo> actionAttachments = allActionFiles.get(actId);
				if ((actionAttachments != null) && (!actionAttachments.isEmpty()))
					for (ActionFileInfo afi : actionAttachments){
						displayName = afi.getName();
						try {
							hp = new DTagReplacer(DETAILS_PAGE_HTML);
							hp.replace("fileCount",String.valueOf(fileCount));
							hp.replace("displayName", displayName);
							hp.replace("baseUrl", BaseUrl);
							hp.replace("aSysPrefix", aSysPrefix);
							hp.replace("actionId", String.valueOf(attAction.getActionId()));
							hp.replace("author",actionUserName);
							hp.replace("lastUpdated", lastUpdated.toCustomFormat("dd/MM/yyyy"));
							hp.replace("request_id", requestIdStr);
							hp.replace("request_file_id", afi.getRequestFileId()+"");
							hp.replace("field_id", afi.getFieldId()+"");
							hp.replace("action_id", actId+"");
							sbf.append(hp.parse(systemId));
						}
						catch(Exception e) {
							throw new TBitsException(e.toString());
						}
						fileCount++;
					}
			}

			try {
				hp = new DTagReplacer(SIDE_FRAME_HTML);
				hp.replace("filesList",sbf.toString());
				out.println(hp.parse(systemId));
				out.close();
			}
			catch(Exception e) {
				throw new TBitsException(e.toString());
			}
			break;

		case APPLET_FRAME_NUM :
			try {        	
				String autoVueAttachment = (new StringBuilder()).append(aSysPrefix).append("/").append(requestId).append("/").append(actionId)
																	.append("/").append(fieldId).append("/" ).append(requestFileId).toString();

				hp = new DTagReplacer(MAIN_FRAME_HTML);
				hp.replace("filename",autoVueAttachment);
//				hp.replace("jvueserver",jvueServer);
//				hp.replace("codebase",jvueCodebase);
				hp.replace("username",userLogin);
				hp.replace("nearestPath", BaseUrl);

				out.println(hp.parse(systemId));
				out.close();
			}
			catch(Exception e) {
				throw new TBitsException(e.toString());
			}
			break;
		default:
			try {
				String leftFrameUrl = (new StringBuilder()).append(BaseUrl).append("open-attachment/").append(aSysPrefix).append("/").append(requestId).append("/").append(actionId).append("/").append(fieldId).append("/" ).append(requestFileId).append("?frame=").append(LEFT_FRAME_NUM).toString();
				String appletFrameUrl = (new StringBuilder()).append(BaseUrl).append("open-attachment/").append(aSysPrefix).append("/").append(requestId).append("/").append(actionId).append("/").append(fieldId).append("/" ).append(requestFileId).append("?frame=").append(APPLET_FRAME_NUM).toString();
				hp = new DTagReplacer(MAIN_PAGE_HTML);
				hp.replace("title",pageTitle);
				hp.replace("leftFrameUrl",leftFrameUrl);
				hp.replace("appletFrameUrl",appletFrameUrl);
				out.println(hp.parse(systemId));
				out.close();
			}catch(Exception e) {
				throw new TBitsException(e.toString());					
			}
		} 
		out.flush();   
		return;
	}
}

