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
 * EditAction.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

//log4j imports.
import org.apache.log4j.MDC;

//Imports from current package.
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.DTagReplacer;

//TBits Imports.
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.Attachment;
import transbit.tbits.config.SysConfig;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//~--- classes ----------------------------------------------------------------

/**
 * The EditAction class. This class displays form to edit description and
 * attachments for an action
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public class EditAction extends HttpServlet implements TBitsPropEnum {
    public static final TBitsLogger LOG       = TBitsLogger.getLogger(PKG_WEBAPPS);
    private static final String     EDIT_FILE = "web/tbits-edit-action.htm";

    // Location of attachments.
//    public static String ourAttachmentLocation;

    //~--- static initializers ------------------------------------------------

//    static {
//        try {
//            ourAttachmentLocation = Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_ATTACHMENTDIR));
//        } catch (IllegalArgumentException e) {
//            LOG.severe(e.toString(), e);
//        }
//    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method removes the attachments present in the list which belong to
     * the business area denoted by the prefix.
     *
     * @param prefix   BA prefix.
     * @param list     List of attachments to be delete.
     *
     */
    private void deleteAttachments(String prefix, ArrayList<Attachment> list) {
        if (list == null) {
            return;
        }

        if ((prefix == null) || prefix.trim().equals("")) {
            return;
        }

        // Directory name is always in lowercase.
        prefix = prefix.toLowerCase();

        for (Attachment item : list) {
            String name     = item.getName();
            String location = APIUtil.getAttachmentLocation() + "/" + prefix + "/" + name;

            try {
                File file = new File(location);

                LOG.info("Deleting attachment: " + location);

                boolean flag = file.delete();

                file = null;
            } catch (Exception e) {
                LOG.info("",(e));
            }
        }

        return;
    }

    /**
     * This method services the Http-GET Requests to this servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        aResponse.setContentType("text/html");

        PrintWriter out     = aResponse.getWriter();
        HttpSession session = aRequest.getSession(true);

        Utilities.registerMDCParams(aRequest);

        try {
            handleGetRequest(aRequest, aResponse);
        } catch (Exception de) {
            session.setAttribute("ExceptionObject", de);
            LOG.info("",(de));
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));

            return;
        } finally {
            Utilities.clearMDCParams();
        }
    }

    /**
     * This method services the Http-POST Requests to this servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        aResponse.setContentType("text/html");

        PrintWriter out     = aResponse.getWriter();
        HttpSession session = aRequest.getSession(true);

        Utilities.registerMDCParams(aRequest);

        try {
            handlePostRequest(aRequest, aResponse);
        } catch (Exception de) {
            out.println("false");
            LOG.info("",(de));

            return;
        } finally {
            Utilities.clearMDCParams();
        }
    }

    /**
     * This method actually services the Http-GET Requests to this servlet.
     *
     *
     * @param aRequest          the HttpServlet Request Object
     * @param aResponse         the HttpServlet Response Object
     * @throws ServletException
     * @throws IOException
     * @throws TBitsExceptionrows DatabaseException
     */
    private void handleGetRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException {
        aResponse.setContentType("text/html");

        PrintWriter out = aResponse.getWriter();

        // Validate the user.
        User user   = WebUtil.validateUser(aRequest);
        int  userId = user.getUserId();

        // Check if the sysPrefix is a part of the Query String.
        // This a required param.
        String sysPrefix = aRequest.getParameter("sysPrefix");

        if ((sysPrefix == null) || (sysPrefix.trim().equals("") == true)) {
            out.println("Invalid Business Area.");

            return;
        }

        // Check if the sysprefix corresponds to a valid BA.
        BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

        if (ba == null) {
            out.println("Invalid Business Area.");

            return;
        }

        // Get the SystemId and the Prefix value as in BA record.
        int systemId = ba.getSystemId();

        sysPrefix = ba.getSystemPrefix();

        SysConfig sc = ba.getSysConfigObject();

        MDC.put("SYS_PREFIX", sysPrefix);

        // Get the request id whose action should be edited and validate it.
        String strReqId = aRequest.getParameter("requestId");

        if ((strReqId == null) || strReqId.trim().equals("")) {
            out.println("Invalid Request.");

            return;
        }

        int requestId = 0;

        try {
            requestId = Integer.parseInt(strReqId);
        } catch (NumberFormatException nfe) {
            out.println("Invalid Request.");

            return;
        }

        MDC.put("REQUEST_ID", Integer.toString(requestId));

        // Get the action id to be edited and validate it.
        String strActionId = aRequest.getParameter("actionId");

        if ((strActionId == null) || strActionId.trim().equals("")) {
            out.println("Invalid Action.");

            return;
        }

        int actionId = 0;

        try {
            actionId = Integer.parseInt(strActionId);
        } catch (NumberFormatException nfe) {
            out.println("Invalid Action.");

            return;
        }

        // Get the permission table for this user
        Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndRequestIdAndActionIdAndUserId(systemId, requestId, actionId, userId);

        LOG.info("Editing action: " + "\nUser: " + user.getUserLogin() + "\nBA: " + ba.getSystemPrefix() + "\nRequest: " + requestId + "\nAction: " + actionId);

        // If the permission table is null, lets no go further as this is
        // one of those operations given only to selected list of users.
        if (permTable == null) {
            out.println("Insufficient permissions.");

            return;
        }

        // Check if the user can change descritpion.
        boolean cDesc = WebUtil.canChange(permTable, Field.DESCRIPTION);

        // Check if the user can change attachments.
        boolean cAtt = WebUtil.canChange(permTable, Field.ATTACHMENTS);

        // If he cannot change both, there is not point in bringing up the form
        if ((cDesc == false) && (cAtt == false)) {
            out.println("Insufficient permissions.");

            return;
        }

        // Get the action text.
        Action                action  = getActionText(systemId, requestId, actionId);
        ArrayList<Attachment> attList = null;

        try {

            // Parse the attachment information in XML and get the list of
            // attachments.
            attList = Attachment.getAttachments(action.getAttachments());
        } catch (Exception e) {
            LOG.warn("",(e));
        }

        // Open the html interface and start replacing the tags.
        DTagReplacer hp = new DTagReplacer(EDIT_FILE);

        hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
        hp.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
        hp.replace("sysPrefix", sysPrefix);
        hp.replace("requestId", Integer.toString(requestId));
        hp.replace("actionId", Integer.toString(actionId));
        hp.replace("descDisabled", ((cDesc == true)
                                    ? ""
                                    : " DISABLED "));
        hp.replace("description", Utilities.htmlEncode(action.getDescription()));

        if ((cAtt == true) && ((attList != null) && (attList.size() != 0))) {

            // Since the user can change attachments, set the display mode
            // of the attachments Row to BLOCK.
            hp.replace("showAttachments", "BLOCK");

            // Get the HTML list for attachments.
            hp.replace("attachmentList", toHtmlList(attList));

            // We would atmost want to display 9 attachments without scrolling
            int size = attList.size();

            if (size > 9) {

                // Fix the height at 250px so that user should scroll to see
                // next set of attachments
                hp.replace("height", "250");
            } else {

                // Each attachment row occupies 25px. Set the size accordingly.
                size = (size + 1) * 25;
                hp.replace("height", Integer.toString(size));
            }
        } else {

            // Since the user cannot change the attachments, set the display
            // mode to none and nullify other tags by assigning empty string.
            hp.replace("showAttachments", "NONE");
            hp.replace("attachmentList", "");
            hp.replace("height", "0");
        }

        // Print out the html.
        out.println(hp.parse(systemId));

        return;
    }

    /**
     * This method actually services the Http-POST Requests to this servlet.
     *
     *
     * @param aRequest          the HttpServlet Request Object
     * @param aResponse         the HttpServlet Response Object
     * @throws ServletException
     * @throws IOException
     * @throws TBitsExceptionrows DatabaseException
     */
    private void handlePostRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException {
        aResponse.setContentType("text/html");

        PrintWriter out = aResponse.getWriter();

        // Validate the user and get the required information.
        User   user      = WebUtil.validateUser(aRequest);
        int    userId    = user.getUserId();
        String userLogin = user.getUserLogin();
        String userName  = user.getDisplayName();

        // SysPrefix should be part of the query string.
        String sysPrefix = aRequest.getParameter("sysPrefix");

        if ((sysPrefix == null) || (sysPrefix.trim().equals("") == true)) {
            out.println("Invalid Business Area.");

            return;
        }

        // Sysprefix specified should correspond to a valid business area.
        BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

        if (ba == null) {
            out.println("Invalid Business Area.");

            return;
        }

        // Get the System Id and the prefix as in BA Records.
        int systemId = ba.getSystemId();

        sysPrefix = ba.getSystemPrefix();
        MDC.put("SYS_PREFIX", sysPrefix);

        // Get the request Id whose action should be edited.
        String strReqId = aRequest.getParameter("requestId");

        if ((strReqId == null) || strReqId.trim().equals("")) {
            out.println("Invalid Request.");

            return;
        }

        int requestId = 0;

        try {
            requestId = Integer.parseInt(strReqId);
        } catch (NumberFormatException nfe) {
            out.println("Invalid Request.");

            return;
        }

        MDC.put("REQUEST_ID", Integer.toString(requestId));

        // Get the action Id to be edited.
        String strActionId = aRequest.getParameter("actionId");

        if ((strActionId == null) || strActionId.trim().equals("")) {
            out.println("Invalid Action.");

            return;
        }

        int actionId = 0;

        try {
            actionId = Integer.parseInt(strActionId);
        } catch (NumberFormatException nfe) {
            out.println("Invalid Action.");

            return;
        }

        // Get the permission table for the user for this action.
        Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndRequestIdAndActionIdAndUserId(systemId, requestId, actionId, userId);

        // If the permission table could not be obtained, lets not go further.
        if (permTable == null) {
            out.println("Insufficient permissions.");

            return;
        }

        // Check if the user has permission to change description.
        boolean cDesc = WebUtil.canChange(permTable, Field.DESCRIPTION);

        // Check if the user has permission to change attachments.
        boolean cAtt = WebUtil.canChange(permTable, Field.ATTACHMENTS);

        // If the user did not have both the permission, return from here
        // itself.
        if ((cDesc == false) && (cAtt == false)) {
            out.println("Insufficient permissions.");

            return;
        }

        // Get the action text.
        Action                action  = getActionText(systemId, requestId, actionId);
        ArrayList<Attachment> attList = null;

        try {

            // Parse the XML to get the list of attachment objects.
            attList = Attachment.getAttachments(action.getAttachments());
        } catch (Exception e) {
            LOG.warn("",(e));
        }

        // Get the description from the user.
        String description = aRequest.getParameter("description");

        description = (description == null)
                      ? ""
                      : description.trim();

        // Get the list of ids of attachments to be removed.
        String attachments = aRequest.getParameter("attachments");

        attachments = (attachments == null)
                      ? ""
                      : attachments.trim();

        // Get the header description from the action text.
        String headerDesc = action.getHeaderDescription();

        if (cDesc == false) {

            // User does not have change permission on Description, so
            // retain the old description.
            description = action.getDescription();
        } else {

            // User had change permission on description. Check if he tried
            // to change it.
            if (description.equals(action.getDescription()) == false) {

                //
                // Since the user has changed the description, add a record
                // to header description about the same.
                //
                Field field = Field.lookupBySystemIdAndFieldName(systemId, Field.DESCRIPTION);

                if (field != null) {
                    int    fieldId   = field.getFieldId();
                    String fieldName = field.getName();

                    headerDesc = headerDesc + "\n" + fieldName + "##" + fieldId + "##[ Content edited by " + userName + " (" + userLogin + "): " + getCurrentTime() + " ]";
                }
            }
        }

        if (cAtt == false) {

            // User does not have change permission on attachments, so
            // retain the old attachments XML.
            attachments = action.getAttachments();
        } else {

            //
            // User had change permission on attachments. Check if he tried to
            // delete some of them.
            //
            // Convert the comma separated list to a table.
            Hashtable<String, Boolean> table = toHash(attachments);

            // This will hold the list of attachments to be retained.
            ArrayList<Attachment> finalList = new ArrayList<Attachment>();

            // This will hold the list of attachments to be deletes.
            ArrayList<Attachment> deleteList = new ArrayList<Attachment>();

            for (Attachment item : attList) {
                String id = item.getId();

                if (table.get(id) != null) {
                    deleteList.add(item);
                } else {
                    finalList.add(item);
                }
            }

            // Serialize the list to XML.
            attachments = Attachment.xmlSerialize(finalList);

            // Check if the user tried to delete any of the attachments.
            if (deleteList.size() != 0) {

                // Add a record to the header_description stating the same.
                deleteAttachments(sysPrefix, deleteList);

                Field field = Field.lookupBySystemIdAndFieldName(systemId, Field.ATTACHMENTS);

                if (field != null) {
                    int    fieldId   = field.getFieldId();
                    String fieldName = field.getName();

                    headerDesc = headerDesc + "\n" + fieldName + "##" + fieldId + "##[ Attachment(s) removed by " + userName + " (" + userLogin + "): " + getCurrentTime() + " ]";
                }
            }
        }

        //
        // Update the action with the latest description, attachments and
        // header_description.
        //
        updateAction(systemId, requestId, actionId, description, headerDesc, attachments);

        // Return true as a token of successful completion.
        out.println("true");

        return;
    }

    /**
     * This method returns a hashtable for the given css list.
     *
     * @param cssList Comma separated list of items.
     *
     * @return table [item, true]
     */
    private Hashtable<String, Boolean> toHash(String cssList) {
        Hashtable<String, Boolean> table = new Hashtable<String, Boolean>();
        StringTokenizer            st    = new StringTokenizer(cssList, ",");

        while (st.hasMoreTokens() == true) {
            String token = st.nextToken();

            table.put(token, true);
        }

        return table;
    }

    /**
     * This method returns the HTML table display of given list of attachments.
     *
     * @param attList  List of attachments.
     *
     * @return HTML Table.
     */
    private String toHtmlList(ArrayList<Attachment> attList) {
        StringBuffer buffer = new StringBuffer();

        if (attList == null) {
            return buffer.toString();
        }

        for (Attachment att : attList) {
            String id   = att.getId();
            String name = att.getDisplayName();

            buffer.append("\n<TR>").append("\n\t<TD><INPUT type='checkbox' value='").append(id).append("' onclick='count(this.value);'></TD>\n\t<TD>").append(Utilities.htmlEncode(name)).append(
                "</TD>\n</TR>");
        }

        return buffer.toString();
    }

    /**
     * This method updates the following information of the specified action.
     *   <UL>
     *     <LI>Description</LI>
     *     <LI>Header Description</LI>
     *     <LI>Attachments</LI>
     *   </UL>
     *
     * @param systemId     Business Area Id.
     * @param requestId    Request Id.
     * @param actionId     Action Id.
     * @param description  Description of the action.
     * @param headerDesc   Header Description of the action.
     * @param attachments  Attachment information of the action.
     *
     * @throws DatabaseException incase of any database related errors.
     */
    public static void updateAction(int systemId, int requestId, int actionId, String description, String headerDesc, String attachments) throws DatabaseException {
        Connection con = null;

        try {
            con = DataSourcePool.getConnection();
            con.setAutoCommit(false);

            CallableStatement cs = con.prepareCall("stp_action_updateActionText ?, ?, ?, ?, ?, ?");

            cs.setInt(1, systemId);
            cs.setInt(2, requestId);
            cs.setInt(3, actionId);
            cs.setString(4, description);
            cs.setString(5, headerDesc);
            cs.setString(6, attachments);

            boolean flag = cs.execute();

            cs.close();
            cs = null;
            con.commit();
        } catch (SQLException sqle) {
        	try {
        		if(con != null)
					con.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            StringBuffer message = new StringBuffer();

            message.append("An error has occurred while retrieving the action.");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {}
            }
        }

        return;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method retrieves the following information of the specified action.
     *   <UL>
     *     <LI>Description</LI>
     *     <LI>Header Description</LI>
     *     <LI>Attachments</LI>
     *   </UL>
     *
     * @param systemId  Business Area Id.
     * @param requestId Request Id.
     * @param actionId  Action Id.
     *
     * @return Action object with the above information.
     *
     * @throws DatabaseException incase of any database related errors.
     */
    private Action getActionText(int systemId, int requestId, int actionId) throws DatabaseException {
        Connection con    = null;
        Action     action = new Action();

        try {
            con = DataSourcePool.getConnection();

            CallableStatement cs = con.prepareCall("stp_action_getActionText ?, ?, ?");

            cs.setInt(1, systemId);
            cs.setInt(2, requestId);
            cs.setInt(3, actionId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    action.setSystemId(rs.getInt("sys_id"));
                    action.setRequestId(rs.getInt("request_id"));
                    action.setActionId(rs.getInt("action_id"));
                    action.setDescription(((rs.getString("description") != null)
                                           ? rs.getString("description")
                                           : ""));
                    action.setHeaderDescription(((rs.getString("header_description") != null)
                                                 ? rs.getString("header_description")
                                                 : ""));
                    action.setAttachments(rs.getString("attachments"));
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;
        } catch (SQLException sqle) {
            StringBuffer message = new StringBuffer();

            message.append("An error has occurred while retrieving the action.");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {}
            }
        }

        return action;
    }

    /**
     * This method returns the current time in MM/dd/yyyy HH:mm:ss z format.
     *
     * @return current time.
     */
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");

        return sdf.format(new Date());
    }
}
