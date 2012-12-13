package transbit.tbits.api;

import static transbit.tbits.Helper.TBitsPropEnum.KEY_ATTACHMENTDIR;

import java.io.*;
import java.sql.SQLException;
import java.util.Hashtable;

import javax.servlet.*;
import javax.servlet.http.*;

import transbit.tbits.Helper.ActionHelper;
import transbit.tbits.Helper.Messages;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsFileInfo;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Uploader;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

/**
*
* @author Sourabh
* @version
*/

public class CopyPaste extends HttpServlet {
	 private static final String SYS_PREFIX = "sysPrefix";
	private static final String ACTION_ID = "action_id";
	private static final String FIELD_ID = "fieldId";
	private static final String SIZES = "sizes";
	private static final String REQUEST_FILE_IDS = "requestFileIds";
	private static final String REQUEST_ID = "requestId";
	private static final String IS_COPIED = "is_copies";
	private static final long serialVersionUID = 1L;
	 private static final TBitsLogger LOG = TBitsLogger.getLogger("transbit.tbits.api") ;
//	 public static String ourAttachmentLocation;
	
	    //~--- static initializers ------------------------------------------------
	
	    // Static block to initialize the attachment location.
//	    static {
//	        try {
//	            ourAttachmentLocation = Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_ATTACHMENTDIR));
//	          } catch (IllegalArgumentException e) {
//	            LOG.severe(e.toString(), e);
//	        }
//	    }
	    
	    
	 protected void processGetRequest(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException, TBitsException, DatabaseException {
		 	HttpSession session = request.getSession();
		 
		// Validate the user.
	        User user   = WebUtil.validateUser(request);
	        int  userId = user.getUserId();

	        // Get the User WebConfig.
	        WebConfig wc = user.getWebConfigObject();

	        // Get the request params.
	        Hashtable<String, Object> params = WebUtil.getRequestParams(request, wc, WebUtil.READ_ATTACHMENT);

	        if (params == null) {
	            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
	        }

	        // Get the Business area object.
	        BusinessArea ba = (BusinessArea) params.get(Field.BUSINESS_AREA);

	        if (ba == null) {
	            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
	        }
	        
	        

	        String sysPrefix = ba.getSystemPrefix();
	        int    systemId  = ba.getSystemId();
	     
	        int requestId = -1;
	        int fieldId = -1;
	        int[] requestFileIds;
	       
	        String requestIdStr = request.getParameter("request_id");
			String requestFileIdStr = request.getParameter("request_file_id_str");
			if ((requestIdStr == null || requestFileIdStr == null)) {
				throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
			}
			try {
				requestId = Integer.parseInt(requestIdStr);
			} catch (NumberFormatException nfe) {
				throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
			}
			
			try {
				String[] requestFileIdArr = requestFileIdStr.substring(1).split("-");
				requestFileIds = new int[requestFileIdArr.length];
				for(int i=0; i < requestFileIdArr.length; i++ ){
					requestFileIds[i] = Integer.parseInt(requestFileIdArr[i]);
				}
			} catch (NumberFormatException nfe) {
				throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
			}

			String sizeStr = request.getParameter("size_str");
			String[] sizeArr;
			try {
				sizeArr = sizeStr.substring(1).split("-");
			} catch (NumberFormatException nfe) {
				throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
			}
			
			String fieldIdStr = request.getParameter("field_id");
			try {
				fieldId = Integer.parseInt(fieldIdStr);
			} catch (Exception e) {
				throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
			}
			
			 // Get the permissions.
	        Hashtable<String, Integer> permTable = null;

	        try {
	        	permTable = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(systemId, requestId, userId);
	     
	            if (permTable == null) {
	                LOG.warn("Permission table is null.");

	                throw new Exception("");
	            }
	        } catch (Exception e) {
	            throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
	        }

	        // Get the permissions pertaining to attachments field.
	        Integer tmp = permTable.get(Field.ATTACHMENTS);

	        if (tmp == null) {
	            throw new TBitsException(Messages.getMessage("NO_PERMISSION_ATTACHMENT"));
	        }

	        int permission = tmp.intValue();

	        if ((permission & Permission.VIEW) == 0) {
	            throw new TBitsException(Messages.getMessage("NO_PERMISSION_ATTACHMENT"));
	        }
	        
	        session.setAttribute(IS_COPIED, 1);
	        session.setAttribute(SYS_PREFIX, sysPrefix);
	        session.setAttribute(REQUEST_ID, requestId);
	        session.setAttribute(REQUEST_FILE_IDS, requestFileIds);
	        session.setAttribute(SIZES, sizeArr);
	        session.setAttribute(FIELD_ID, fieldId);
	 }
	 
	 protected void processPostRequest(HttpServletRequest request, HttpServletResponse response)
	 	throws TBitsException, SQLException, DatabaseException, ServletException{
		 HttpSession session = request.getSession();
		 
		 if(session.getAttribute(IS_COPIED) == null || Integer.parseInt(session.getAttribute(IS_COPIED).toString()) != 1){
			 try{
				 response.getWriter().println("No Copied Files on the Clipboard");
			 }catch(IOException ex){
				 ex.printStackTrace();
			 }
			 return;
		 }
		 
		 User user   = WebUtil.validateUser(request);
		 WebConfig wc = user.getWebConfigObject();
	
	     // Get the request params.
	     Hashtable<String, Object> params = WebUtil.getRequestParams(request, wc, WebUtil.READ_ATTACHMENT);
		 
	     if (params == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
	     }

        // Get the Business area object.
	     //BusinessArea ba = (BusinessArea) params.get(Field.BUSINESS_AREA);
	     Object sysPrefixObj = session.getAttribute(SYS_PREFIX);;
	     if(sysPrefixObj == null)
	    	 throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
	     
	     String sysPrefix = (String) sysPrefixObj;
	     
	     BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix); 
	     if (ba == null) {
	    	 throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
	     }
	     
	     
	     int systemId  = ba.getSystemId();
		 
		 int requestId = Integer.parseInt(session.getAttribute(REQUEST_ID).toString());
		 int[] requestFileIds = (int[])session.getAttribute(REQUEST_FILE_IDS);
		 String[] sizes = (String[])session.getAttribute(SIZES);
		 int fieldId = Integer.parseInt(session.getAttribute(FIELD_ID).toString());
		 
		 int actionId = -1;
		 String actionIdStr = request.getParameter(ACTION_ID);
		 try {
			actionId = Integer.parseInt(actionIdStr);
		 } catch (Exception e) {
		 }
		 
		 String filePath[] = new String[requestFileIds.length];
		 String fileName[] = new String[requestFileIds.length];
		 
		 for(int i = 0; i < requestFileIds.length; i++){
			 TBitsFileInfo fi = Uploader.getFileInfo(systemId, requestId, -1, requestFileIds[i], fieldId);
			 filePath[i] = APIUtil.getAttachmentLocation() + "/" + fi.getFileLocation();
     		 fileName[i] = fi.getFileName();
     		 try{
     			 int id = Uploader.getRepoFileId(systemId, requestId, actionId, requestFileIds[i], fieldId);
     			 response.getWriter().print("Record,Name:" + fileName[i] + ",Size:" + sizes[i] + ",RepoId:" + id);
     		 }catch(IOException ex){
     			 ex.printStackTrace();
     		 }
		 }
		 
	 }
	
	/** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try{
        	processGetRequest(request, response);
        }catch(DatabaseException de){
        	HttpSession session = request.getSession();
        	session.setAttribute("ExceptionObject", de);
            response.sendRedirect(WebUtil.getServletPath(request, "error"));
        }catch (TBitsException de) {
        	HttpSession session = request.getSession();
        	session.setAttribute("ExceptionObject", de);
            LOG.info("",(de));
            response.sendRedirect(WebUtil.getServletPath(request, "error"));
        }
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    	try{
    		processPostRequest(request, response);
    	}catch(DatabaseException de){
        	HttpSession session = request.getSession();
        	session.setAttribute("ExceptionObject", de);
            response.sendRedirect(WebUtil.getServletPath(request, "error"));
        }catch(SQLException se){
        	HttpSession session = request.getSession();
        	session.setAttribute("ExceptionObject", se);
            response.sendRedirect(WebUtil.getServletPath(request, "error"));
        }catch (TBitsException de) {
        	HttpSession session = request.getSession();
        	session.setAttribute("ExceptionObject", de);
            LOG.info("",(de));
            response.sendRedirect(WebUtil.getServletPath(request, "error"));
        }
    }
}
