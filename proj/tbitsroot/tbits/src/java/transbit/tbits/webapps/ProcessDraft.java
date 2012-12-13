/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/*
 * ProcessDraft.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

//TBits Imports
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.DraftConfig;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserDraft;
import transbit.tbits.webapps.WebUtil;
import transbit.tbits.webapps.AddHtmlRequest;

//Static imports
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;
import static transbit.tbits.webapps.WebUtil.ADD_ACTION;
import static transbit.tbits.webapps.WebUtil.ADD_REQUEST;
import static transbit.tbits.webapps.WebUtil.ADD_SUBREQUEST;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//~--- classes ----------------------------------------------------------------

/**
 * This servlet is used to save/load user drafts
 *
 * @author  Vinod Gupta
 * @version $Id: $
 */
public class ProcessDraft extends HttpServlet {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

    //~--- methods ------------------------------------------------------------

    /**
     * The doGet method of the servlet.
     *
     * @param  aHttpRequest          the HttpServlet Request Object
     * @param  aHttpResponse         the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse) throws ServletException, IOException {
        PrintWriter out = null;
        //System.out.println("process draft called do get");
        Utilities.registerMDCParams(aHttpRequest);

        try {
            out = aHttpResponse.getWriter();

            long start = System.currentTimeMillis();

            handleGetRequest(aHttpRequest, aHttpResponse);

            long end = System.currentTimeMillis();

            LOG.debug("Time taken to get drafts: " + (end - start) + " mecs");
        } catch (RuntimeException e) {
            LOG.severe("\npathInfo: " + aHttpRequest.getPathInfo() + "\n" + "",(e));

            return;
        } catch (Exception e) {
            LOG.severe("\npathInfo: " + aHttpRequest.getPathInfo() + "\n" + "",(e));

            return;
        } finally {
            Utilities.clearMDCParams();
        }
    }

    /**
     * The doPost method of the servlet.
     *
     * @param  aHttpRequest          the HttpServlet Request Object
     * @param  aHttpResponse         the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse) throws ServletException, IOException {
        PrintWriter out = null;
        Utilities.registerMDCParams(aHttpRequest);

        try {
            out = aHttpResponse.getWriter();

            long start = System.currentTimeMillis();

            handlePostRequest(aHttpRequest, aHttpResponse);

            long end = System.currentTimeMillis();

            LOG.debug("Time taken to process draft: " + (end - start) + " mecs");
        } catch (RuntimeException e) {
            LOG.severe("\npathInfo: " + aHttpRequest.getPathInfo() + "\n" + "",(e));

            return;
        } catch (Exception e) {
            LOG.severe("\npathInfo: " + aHttpRequest.getPathInfo() + "\n" + "",(e));

            return;
        } finally {
            Utilities.clearMDCParams();
        }
    }
    
    

    /**
     * Method that actually handles the Get Request.
     *
     * @param  aHttpRequest          the HttpServlet Request Object
     * @param  aHttpResponse         the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     * @throws Exception
     */
    public void handleGetRequest(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse) throws ServletException, IOException, Exception {
        aHttpResponse.setContentType("text/html;");

        PrintWriter out         = aHttpResponse.getWriter();
        HttpSession httpSession = aHttpRequest.getSession();

        
         //System.out.println("handle Get request called in process Draft.");
        //
        // Validate the user, get the user Object, Read the configuration.
        //
        User user = WebUtil.validateUser(aHttpRequest);

        if (user == null) {
            LOG.info("user null: draft not loaded");

            return;
        }

        int                       userId     = user.getUserId();
        Hashtable<String, String> paramTable = getDraftParamTable(aHttpRequest);
        String                    action     = "list";

        try {
            action = paramTable.get("action");
        } catch (Exception e) {
            action = "list";
        }

        if (action == null) {
            action = "list";
        }

        if (action.trim().equalsIgnoreCase("list") == true) {
            String  strSeparator = aHttpRequest.getParameter("separator");
            String  strEmptyInfo = aHttpRequest.getParameter("emptyInfo");
            boolean separator    = true;
            boolean emptyInfo    = true;

            if ((strSeparator != null) && strSeparator.equals("false")) {
                separator = false;
            }

            if ((strEmptyInfo != null) && strEmptyInfo.equals("false")) {
                emptyInfo = false;
            }

            out.print(WebUtil.listUserDrafts(aHttpRequest, userId, separator, emptyInfo));
            
//            String draftId = paramTable.get("draftid");
//            System.out.println("draft id load in handlegetRequest:"+ draftId);
            return;
        }
    }

    /**
     */
    public void handlePostRequest(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse) throws ServletException, IOException, Exception {
        aHttpResponse.setContentType("text/html");

        PrintWriter out         = aHttpResponse.getWriter();
        HttpSession httpSession = aHttpRequest.getSession();

        //
        // Validate the user, get the user Object, Read the configuration.
        //
        
        User user = WebUtil.validateUser(aHttpRequest);
   
        
        
        if (user == null) {
            LOG.info("user null: draft not saved");

            return;
        }
        
        
        int       userId    = user.getUserId();
        
        UserDraft userDraft = new UserDraft();
        
        
        		
        userDraft.setUserId(userId);

        Hashtable<String, String> paramTable = getDraftParamTable(aHttpRequest);
        
        int                       systemId   = 0;
        int                       requestId  = 0;
        Timestamp                 timeStamp  = null;
        String                    action     = "save";
        int draftId = 0;
     
     
        

        try {
            systemId = Integer.parseInt(paramTable.get("systemId"));
            } catch (Exception e) {
            LOG.info("incorrect systemId: draft not saved");

            return;
        }
        
        try
        {
          draftId = Integer.parseInt(paramTable.get("draftId"));
        }catch(Exception e){
         draftId = 0;
        }
        
       //  System.out.println("draft id in handlePostRequestIn processdraft:"+ draftId);
        
         BusinessArea ba = BusinessArea.lookupBySystemId(systemId);

        if (ba == null) {
            LOG.info("incorrect sys_id: draft not saved");

            return;
        }

        userDraft.setSystemId(systemId);
        paramTable.remove(systemId);

        try {
            requestId = Integer.parseInt(paramTable.get("requestId"));
        } catch (Exception e) {
            requestId = 0;
        }
        
        
        userDraft.setRequestId(requestId);
        paramTable.remove(requestId);
        timeStamp = new Timestamp();
       
        userDraft.setTimestamp(timeStamp.toGmtTimestamp());
        paramTable.remove("DTimestamp");

        try {
            action = paramTable.get("action");
        } catch (Exception e) {
            action = "save";
        }

        if (action == null) {
            action = "save";
        }

        if (action.trim().equalsIgnoreCase("save")) {
        	
            userDraft.setDraft("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + DraftConfig.xmlSerialize(systemId, paramTable));
            
        // next line add by paritosh
            
            userDraft.setDraftId(draftId);
            
         // System.out.println("userDraft before insert/update in processdraft: Draft id:"+draftId);
               if(draftId >0)
               {  
            	  //System.out.println("Update Draft called");
        	      UserDraft.update(userDraft);
                }
              else
              { 
            	  //System.out.println("Insert Draft called");
                  UserDraft.insert(userDraft);
              }
            
        } else if (action.trim().equalsIgnoreCase("delete")) {
        	try {
        		
                draftId = Integer.parseInt(paramTable.get("draftId"));
                
                userDraft.setDraftId(draftId);
                
                UserDraft.delete(userDraft);
        	
        	} catch (Exception e) {
                LOG.info("incorrect draftid: draft not deleted");
                out.print("false");
                return;
            }
            
        }
        out.print(userDraft.getDraftId());

        return;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the table of [paremter, value]
     * <UL><LI>
     *
     * @param aReq   HttpSerlvetRequest Object.
     *
     * @return Table of Parameter name,value pairs.
     *
     * @exception ServletException
     * @exception IOException
     *
     */
    private Hashtable<String, String> getDraftParamTable(HttpServletRequest aReq) throws ServletException, IOException {
        Hashtable<String, String> paramTable = new Hashtable<String, String>();
        Enumeration               fieldList  = aReq.getParameterNames();

        while (fieldList.hasMoreElements()) {
            String fieldName  = (String) fieldList.nextElement();
            String fieldValue = aReq.getParameter(fieldName);

            paramTable.put(fieldName, fieldValue);
        }

        return paramTable;
    }
}
