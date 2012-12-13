/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/**
 * ReadAttachment.java
 *
 * $Header:
 *
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_ATTACHMENTDIR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.pdfbox.exceptions.COSVisitorException;

import transbit.tbits.Helper.Messages;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.PDFAnnotationMerge;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsFileInfo;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Uploader;
import transbit.tbits.common.readerizer.Readerizer;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

//~--- classes ----------------------------------------------------------------

/**
 * This class attends the requests to view the attachments and does the
 * following checks before streaming out the attachment content.
 * <ul>
 *      <li>Check if the user is valid.
 *      <li>Check if the BA is valid.
 *      <li>Check if the user is authorized to view the attachment.
 *      <li>Check if the attachment is present.
 * </ul>
 *
 * @author  Vaibhav.
 * @version $Id: $
 *
 */
public class ReadAttachment extends HttpServlet {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
    
      
    

    // Default Mime Type.
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    // Location of attachments.
//    public static String ourAttachmentLocation;

    //~--- static initializers ------------------------------------------------

    // Static block to initialize the attachment location.
//    static {
//        try {
//            ourAttachmentLocation = Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_ATTACHMENTDIR));
//          } catch (IllegalArgumentException e) {
//            LOG.severe(e.toString(), e);
//        }
//    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method services the Http-Get Requests to this servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        HttpSession session = aRequest.getSession(true);

        try {
            handleGetRequest(aRequest, aResponse);
        } catch (DatabaseException de) {
            session.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));
        } catch (TBitsException de) {
            session.setAttribute("ExceptionObject", de);
            LOG.info("",(de));
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));
        } catch (SQLException e) {
            session.setAttribute("ExceptionObject", e);
            LOG.info("",(e));
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));
        } catch (APIException e) {
        	session.setAttribute("ExceptionObject", e);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));
		} catch (COSVisitorException e) {
			session.setAttribute("ExceptionObject", e);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));
		}
    }

    /**
     * This method services the Http-Post Requests to this servlet.
     *
     * @param aRequest the HttpServlet Request Object
     * @param aResponse the HttpServlet Response Object
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        doGet(aRequest, aResponse);
    }

    /**
     * Method that actually handles the Get Request.
     * It requires the following
     * 	pathinfo: /sysPrefix
     *  mandatory params: sys_id, request_id, request_file_id
     *  optional params: field_id (default is 22), action_id (default is max_action_id)
     * @param aRequest          the HttpServlet Request Object
     * @param aResponse         the HttpServlet Response Object
     * @throws ServletException
     * @throws IOException
     * @throws APIException 
     * @throws COSVisitorException 
     * @throws DESTBitsExceptionthrows DatabaseException
     * @throws FileNotFoundException
     */
    private void handleGetRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, DatabaseException, TBitsException, SQLException, APIException, COSVisitorException {
        
    	aResponse.setContentType(DEFAULT_MIME_TYPE);
        
        ServletOutputStream out = aResponse.getOutputStream();
        
        // Validate the user.
        User user   = WebUtil.validateUser(aRequest);
        int  userId = user.getUserId();

        // Get the User WebConfig.
        WebConfig wc = user.getWebConfigObject();

        // Get the request params.
        Hashtable<String, Object> params = WebUtil.getRequestParams(aRequest, wc, WebUtil.READ_ATTACHMENT);

        if (params == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }

        // Get the Business area object.
        BusinessArea ba = (BusinessArea) params.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }

        int    systemId  = ba.getSystemId();

        // Get the attachment name passed as a request parameter.
        String attachment = aRequest.getParameter("attachment");
        boolean isOldMechanism = false;
        int    requestId = -1;
        int    actionId  = -1;
        String filePath = null;
        String fileName = null;
        int fieldId = 22;
        int requestFileId = -1;
       
        String requestIdStr = aRequest.getParameter("request_id");
		String requestFileIdStr = aRequest.getParameter("request_file_id");
		if ((requestIdStr == null)) {
			throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
		}
		try {
			requestId = Integer.parseInt(requestIdStr);
		} catch (NumberFormatException nfe) {
			throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
		}

		String actionIdStr = aRequest.getParameter("action_id");

		try {
			actionId = Integer.parseInt(actionIdStr);
		} catch (Exception e) {
		}

		String fieldIdStr = aRequest.getParameter("field_id");
		try {
			fieldId = Integer.parseInt(fieldIdStr);
		} catch (Exception e) {
			LOG.error("invalid field id: " + fieldIdStr);
		}
		
		Field attachmentField = Field.lookupBySystemIdAndFieldId(ba.getSystemId(), fieldId);
		
		int mode = 1;
		if(requestFileIdStr == null){
			requestFileIdStr = aRequest.getParameter("request_file_id_str");
			if(requestFileIdStr == null){
				throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
			}else mode = 2;
		}else{
			try {
				requestFileId = Integer.parseInt(requestFileIdStr);
			} catch (NumberFormatException nfe) {
				throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
			}
		}
		
		String format = aRequest.getParameter("format");
		if(format != null && format.equals("zip")){
			mode = 3;
		}
		
        // Get the permissions.
        Hashtable<String, Integer> permTable = null;

        try {
        	if(actionId == -1)
        		permTable = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(systemId, requestId, userId);
        	else
        		permTable = RolePermission.getPermissionsBySystemIdAndRequestIdAndActionIdAndUserId(systemId, requestId, actionId, userId);

            if (permTable == null) {
                LOG.warn("Permission table is null.");

                throw new Exception("");
            }
        } catch (Exception e) {
            throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
        }

        // Get the permissions pertaining to attachments field.
        Integer tmp = permTable.get(attachmentField.getName());

        if (tmp == null) {
            throw new TBitsException(Messages.getMessage("NO_PERMISSION_ATTACHMENT"));
        }

        int permission = tmp.intValue();

        if ((permission & Permission.VIEW) == 0) {
            throw new TBitsException(Messages.getMessage("NO_PERMISSION_ATTACHMENT"));
        }
		
		if(mode == 1){
	        if(!isOldMechanism)
	        {
	        	try {
	        		TBitsFileInfo fi = Uploader.getFileInfo(systemId, requestId, actionId, requestFileId, fieldId);
	        		filePath = APIUtil.getAttachmentLocation() + "/" + fi.getFileLocation();
	        		fileName = fi.getFileName();
				} catch (SQLException e) {
					e.printStackTrace();
					//System.out.println(e.getMessage());
					// TODO Auto-generated catch block
					throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
				}
	        }
	        // Set the content-disposition field.
	        String contentDisposition = "fileName=\"" + fileName + "\"";
	
	        //
	        // Check if this is a request to save the attachment instead of opening
	        // it directly.
	        //
	        String isSaveRequest = aRequest.getParameter("saveAs");
	
	        if (isSaveRequest != null) {
	            contentDisposition = "attachment; " + contentDisposition;
	        }
	        
	       aResponse.setHeader("Content-Disposition", contentDisposition);
	
	        // The user has permissions to view the attachment.
	        // Open the file.
	        File   f        = new File(filePath);
			String mimeType = Readerizer.getMimeType(f);
	
	        if ((mimeType != null) && (mimeType.trim().equals("") == false)) {
	            aResponse.setContentType(mimeType);
	        } else {
	            mimeType = DEFAULT_MIME_TYPE;
	        }
	
	        LOG.info("Reading Attachment: " + "[ " + user.getUserLogin() + ", " + ba.getSystemPrefix() + "#" + requestId + "#" + actionId + ", " + mimeType + " ]");
	
	        try {
	
	            //
	            // Open an inputstream to read the attachment 1MB at a time and
	            // output the same.
	            //
	            FileInputStream fis  = new FileInputStream(f);
	            int             size = 1024 * 1024;
	            byte[]          b    = new byte[size];
	            int             read = 0;
	
	            while ((read = fis.read(b, 0, size)) > 0) {
	                out.write(b, 0, read);
	            }
	
	            fis.close();
	        } catch (FileNotFoundException fnfe) {
	            throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", attachment));
	        } catch (Exception e) {
	            throw new TBitsException(e.toString());
	        }
	
	        // Flush the output.
	        out.flush();
		}else if(mode == 2){
			String[] requestFileIdArr = requestFileIdStr.substring(1).split("-");
			int[] requestFileIds = new int[requestFileIdArr.length];
			for(int i=0; i < requestFileIdArr.length; i++ ){
				requestFileIds[i] = Integer.parseInt(requestFileIdArr[i]);
			}
			
			try {
				TBitsFileInfo parentFile = Uploader.getFileInfo(systemId, requestId, actionId, requestFileIds[0], fieldId);
				List<TBitsFileInfo> files = new ArrayList<TBitsFileInfo>();
				
				for(int i = 1; i<requestFileIds.length; i++ ){	
					TBitsFileInfo fi = Uploader.getFileInfo(systemId, requestId, actionId, requestFileIds[i], fieldId);
					files.add(fi);
				}
				
				File outputFile = PDFAnnotationMerge.getMergedFile(parentFile, files);
				if(outputFile != null){
					String contentDisposition = "fileName=\"" + outputFile.getName() + "\"";
					String isSaveRequest = aRequest.getParameter("saveAs");
			        if (isSaveRequest != null) {
			            contentDisposition = "attachment; " + contentDisposition;
			        }
			        aResponse.setHeader("Content-Disposition", contentDisposition);
			        
			        try {
			            //
			            // Open an inputstream to read the attachment 1MB at a time and
			            // output the same.
			            //
			            FileInputStream fis  = new FileInputStream(outputFile);
			            int             size = 1024 * 1024;
			            byte[]          b    = new byte[size];
			            int             read = 0;
			
			            while ((read = fis.read(b, 0, size)) > 0) {
			                out.write(b, 0, read);
			            }
			
			            fis.close();
			            
			            // Flush the output.
				        out.flush();
			        } catch (FileNotFoundException fnfe) {
			            throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", attachment));
			        } catch (Exception e) {
			            throw new TBitsException(e.toString());
			        }
				}
			} catch (SQLException e) {
				LOG.error("",(e));
				throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
			}
		}else if(mode == 3){
			String[] requestFileIdArr = requestFileIdStr.substring(1).split("-");
			int[] requestFileIds = new int[requestFileIdArr.length];
			for(int i=0; i < requestFileIdArr.length; i++ ){
				requestFileIds[i] = Integer.parseInt(requestFileIdArr[i]);
			}
			TBitsFileInfo fi;
			String contentDisposition;
			File f;
			byte[] buffer = new byte[18024];
			fileName = "zipped" + requestIdStr + ".zip";
			ZipOutputStream zipOut = new ZipOutputStream(out);
			zipOut.setLevel(Deflater.DEFAULT_COMPRESSION);
			
			contentDisposition = "fileName=\"" + fileName + "\"";
			String isSaveRequest = aRequest.getParameter("saveAs");
	        
			if (isSaveRequest != null) {
	            contentDisposition = "attachment; " + contentDisposition;
	        }
	        
			aResponse.setHeader("Content-Disposition", contentDisposition);
			
			for(int i = 0; i<requestFileIds.length; i++ ){	
				try {
					fi = Uploader.getFileInfo(systemId, requestId, actionId, requestFileIds[i], fieldId);
					filePath = APIUtil.getAttachmentLocation() + "/" + fi.getFileLocation();
					String tempFileName = fi.getFileName();
					f = new File(filePath);
					if(!f.exists())
				    {
						throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
				    }
					try{
						FileInputStream in = new FileInputStream(filePath);
				        zipOut.putNextEntry(new ZipEntry(tempFileName));
					    int len;
					    while ((len = in.read(buffer)) > 0){
					    	zipOut.write(buffer, 0, len);
					    }
					    in.close();
					    zipOut.closeEntry();
					}
					catch (IllegalArgumentException iae){
				      iae.printStackTrace();
				    }
				    catch (FileNotFoundException fnfe){
				      fnfe.printStackTrace();
				    }
				    catch (IOException ioe){
				    	ioe.printStackTrace();
				    	System.out.println(ioe.getMessage());
				    	if(ioe.getMessage().substring(0,15).equals("duplicate entry")){
				    		tempFileName = "1_" + tempFileName;
				    		FileInputStream in = new FileInputStream(filePath);
				    		zipOut.putNextEntry(new ZipEntry(tempFileName));
						    int len;
						    while ((len = in.read(buffer)) > 0){
						    	zipOut.write(buffer, 0, len);
						    }
						    in.close();
						    zipOut.closeEntry();
						    continue;
				    	}
				    }
				} catch (SQLException e) {
					e.printStackTrace();
					// TODO Auto-generated catch block
					throw new TBitsException(Messages.getMessage("NO_ATTACHMENT", ""));
				}
				
			}
			zipOut.finish();
	        zipOut.flush();
	        zipOut.close();
		}
        return;
    }

//    /**
//     * This method is for backward compatibility with TBits 3.1
//     * @param aRequest
//     * @param aResponse
//     * @return
//     * @throws IOException
//     */
//    private boolean redirected(HttpServletRequest aRequest, HttpServletResponse aResponse) throws IOException {
//        String pathInfo   = aRequest.getPathInfo();
//        String attachment = aRequest.getParameter("attachment");
//
//        if (attachment == null) {
//            return false;
//        }
//
//        String[] parts = attachment.split("-");
//
//        if (parts.length <= 3) {
//            return false;
//        }
//
//        String strSysId = parts[0];
//        String strReqId = parts[1];
//        String strActId = parts[2];
//        String strSize  = parts[3];
//        String strCF    = parts[4];
//        String fileName = pathInfo.substring(1);
//
//        LOG.info("SystemId : " + strSysId);
//        LOG.info("RequestId : " + strReqId);
//        LOG.info("Action Id : " + strActId);
//
//        Hashtable<String, String> prefixMap = new Hashtable<String, String>();
//
//        
//        String sysPrefix = prefixMap.get(strSysId);
//
//        if (sysPrefix == null) {
//            return false;
//        }
//
//        String attName        = strReqId + "-" + strActId + "-" + fileName;
//        String redirectionURL = "/read-attachment/" + sysPrefix + "/" + fileName + "?attachment=" + attName;
//
//        LOG.info("Redirection URL: " + redirectionURL);
//        aResponse.sendRedirect(WebUtil.getServletPath(aRequest, redirectionURL));
//
//        return true;
//    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method detects the mime type of the file passed.
     *
     * @param file  File whose mime type has to be found out.
     * @return Mime type of the file.
     */
//    private String getMimeType1(File file) {
//        String mimeType = "";
//
//        try {
//            Magic      aMagic = new Magic();
//            MagicMatch aMatch = aMagic.getMagicMatch(file);
//
//            mimeType = aMatch.getMimeType();
//        } catch (MagicMatchNotFoundException mmnfe) {
//
//            // Set the mime type to default value;
//            mimeType = DEFAULT_MIME_TYPE;
//        } catch (Exception e) {
//            mimeType = DEFAULT_MIME_TYPE;
//            LOG.info("",(e));
//        }

        /*
         * Incase the jMimeMagic identifies the mime type to be
         * application/msword, it is a document in OLE2 format and jMimeMagic
         * is not particular about the correct mime type. So just set
         * the mimeType to application/octet-stream in such case.
         */
//        if ((mimeType != null) && (mimeType.trim().equals("") == false) && (mimeType.trim().equals("application/msword") == true)) {
//            mimeType = DEFAULT_MIME_TYPE;
//        }
//
//        return mimeType;
//    }
    
}
