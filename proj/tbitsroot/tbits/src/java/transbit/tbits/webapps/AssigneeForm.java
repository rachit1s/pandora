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
 * Assignee.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.Messages;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;

//import section for TBits
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.TypeUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

//Imports from the current package.
import transbit.tbits.webapps.WebUtil;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

//~--- JDK imports ------------------------------------------------------------

//imports section for java
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//~--- classes ----------------------------------------------------------------

/**
 * This class displays the Assignee Picker.
 *
 * @author  : Vaibhav
 * @author  : Vinod Gupta
 * @version : $Id: $
 */
public class AssigneeForm extends HttpServlet {

    // Logger used to log information messages/errors to the application log.
    private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

    //~--- fields -------------------------------------------------------------

    // WebConfig of users.
    private WebConfig webConfig = null;

    //~--- methods ------------------------------------------------------------

    /**
     * This method services the Http-GET Requests for this servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        aResponse.setContentType("text/html");

        PrintWriter out     = aResponse.getWriter();
        HttpSession session = aRequest.getSession(true);
        User        user    = null;

        // Check If user is Valid
        try {
            user = WebUtil.validateUser(aRequest);
        } catch (DatabaseException e) {
            LOG.severe("Database Exception", e);
            session.setAttribute("ExceptionObject", e);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));

            return;
        } catch (Exception e) {
            LOG.severe("Exception", e);
            session.setAttribute("ExceptionObject", e);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));

            return;
        }

        // If invalid user, direct to error page
        if (user == null) {
            aResponse.setContentType("text/html");
            out.println(aRequest.getRemoteUser() + ": " + Messages.getMessage("INVALID_USER"));

            return;
        }

        try {

            // Read user's web-configuration.
            webConfig = WebConfig.getWebConfig(user.getWebConfig());
        } catch (TBitsException e) {
            LOG.severe("Exception", e);
            session.setAttribute("ExceptionObject", e);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));

            return;
        }

        try {
            out.println(getHtmlContent(aRequest, webConfig));
        } catch (DatabaseException e) {
            LOG.severe("DatabaseException", e);
            session.setAttribute("ExceptionObject", e);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));

            return;
        }
    }

    /**
     * This handles the Http-POST requests for this Servlet.
     *
     * @param  req the HttpServlet Request Object
     * @param  res the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns a list of category users for this business area in
     * html format so that this can be set as a list of options to the select
     * tag.
     *
     * @param systemId    business area id.
     * @param aCategoryId Category
     * @param aUserTypeId User Type (User/MailingList/Contact)
     *
     * @return list of category users in html format.
     */
    private String getAssigneeList(int aSystemId, String aCategoryId, String aUserTypeId) throws DatabaseException {
        Type                type        = Type.lookupBySystemIdAndFieldNameAndTypeName(aSystemId, Field.CATEGORY, aCategoryId);
        ArrayList<TypeUser> activeUsers = TypeUser.lookupBySystemIdAndFieldNameAndTypeId(aSystemId, Field.CATEGORY, type.getTypeId());

        if ((activeUsers == null) || (activeUsers.size() == 0)) {
            return null;
        }

        ArrayList<User> validAssignees = new ArrayList<User>();

        for (TypeUser tu : activeUsers) {
            if (tu.getUserTypeId() != 3) {
                continue;
            }

            validAssignees.add(tu.getUser());
        }

        User.setSortParams(5, 0);
        validAssignees = User.sort(validAssignees);

        StringBuilder buffer   = new StringBuilder();
        Iterator      iterator = validAssignees.iterator();
        User          user     = null;

        while (iterator.hasNext()) {
            user = (User) iterator.next();
            buffer.append("<option value=\"").append(user.getUserLogin()).append("\" >").append(user.getDisplayName()).append("</option>\n");
        }

        return buffer.toString();
    }

    /**
     * This method returns a list of users, present in aExistingAssignee
     * as a comma separated list, as HTML Select Options
     *
     * @param aExistingAssignee
     * @return list of users as HTML Select Options.
     */
    private String getExistingAssigneeList(String aExistingAssignee) throws DatabaseException {
        StringBuilder buffer = new StringBuilder();

        aExistingAssignee = aExistingAssignee.replaceAll(";", ",");

        ArrayList existingAssigneeList = WebUtil.convertNamesToUserArrayList(aExistingAssignee);
        User      user                 = null;

        for (int i = 0; i < existingAssigneeList.size(); i++) {
            user = (User) existingAssigneeList.get(i);
            buffer.append("<option value=\"").append(user.getUserLogin()).append("\" >").append(user.getDisplayName()).append("</option>\n");
        }

        return buffer.toString();
    }

    /**
     * This method actually handles the Request and returns the output
     * in HTML Format.
     *
     * @param aRequest     HTTP Request Object.
     * @param aUserConfig  User Configuration Object.
     *
     * @return Output in HTML Format.
     */
    private String getHtmlContent(HttpServletRequest aRequest, WebConfig aUserConfig) throws IOException, DatabaseException {
        int systemId = -1;

        try {
            systemId = Integer.parseInt(aRequest.getParameter("systemId"));
        } catch (Exception npe) {
            return "Please specify a valid business area id";
        }

        // int categoryId = -1;
        String categoryId    = aRequest.getParameter("categoryId");
        String strUserTypeId = aRequest.getParameter("userTypeId");

        strUserTypeId = ((strUserTypeId == null) || strUserTypeId.trim().equals(""))
                        ? "7"
                        : strUserTypeId;

        String existingAssignee = aRequest.getParameter("existingAssignee");

        if (existingAssignee == null) {
            existingAssignee = "";
        }

        BusinessArea ba           = BusinessArea.lookupBySystemId(systemId);
        SysConfig    sc           = ba.getSysConfigObject();
        String       assigneeList = getAssigneeList(systemId, categoryId, strUserTypeId);
        String       sysPrefix    = ba.getSystemPrefix();

        // String displayThemeSelectedFile =
        // WebUtil.getThemeFile(sysPrefix,
        // aUserConfig.getStyle().getThemeFile());
        if (assigneeList != null) {

            // Reads the request html template file.
            DTagReplacer hp = new DTagReplacer("web/tbits-assignees.htm");

            // hp.replace("displayTheme", displayThemeSelectedFile);
            hp.replace("BusinessArea", ba.getDisplayName());
            hp.replace("assigneeList", assigneeList);
            hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
            hp.replace("existingAssignee", getExistingAssigneeList(existingAssignee));

            if (sc.getAssignToAll() == true) {
                hp.replace("userFilter", getUserFilter(strUserTypeId));

                return hp.parse(systemId);
            } else {
                hp.replace("userFilter", "");

                return hp.parse(systemId);
            }
        } else {
            StringBuffer buffer = new StringBuffer();

            buffer.append("<html><head><link rel='stylesheet' type='text/css' ").append("href='" + WebUtil.getNearestPath(aRequest, "")).append("web/css/").append(
                WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false)).append("'><script language=\"javascript\" ").append("src=\"web/scripts/assignees.js\"></script>").append(
                "</head><body onunload=\"onUnloadBody()\"><table><tr>").append("<td colspan='2' class='results-title' align=center>").append("TBits Message</td></tr><tr>").append(
                "<td class='results-title' width='12%'><b>Type</b>").append("</td><td class='chart-gr'>Information</td></tr><tr>").append("<td class='results-title'><b>Message</b></td>").append(
                "<td class='chart-gr'>No Assignee Found for this ").append("category</td></tr></table></body></html>");

            return buffer.toString();
        }
    }

    /**
     * This method returns the List of filters available on user list.
     *
     * @param aUserTypeId UserType that should be pre-selected.
     *
     * @return List of User Types as HTML Select Options.
     */
    private String getUserFilter(String aUserTypeId) {
        StringBuffer userFilter = new StringBuffer("<td width=\"250\" height=\"20\">Filter:<select ");

        userFilter.append("name=\"userFilter\" ").append("onchange=\"onChangeUserFilter()\">");

        if (aUserTypeId.trim().equals("7")) {
            userFilter.append("<OPTION VALUE='7' SELECTED>Users</OPTION>\n").append("<OPTION VALUE='8'>Mailing List</OPTION>\n").append("<OPTION VALUE='10'>Contacts</OPTION>\n").append(
                "<OPTION VALUE='7,8,10'>All</OPTION>\n");
        } else if (aUserTypeId.trim().equals("8")) {
            userFilter.append("<OPTION VALUE='7'>Users</OPTION>\n").append("<OPTION VALUE='8' SELECTED>Mailing Lists</OPTION>\n").append("<OPTION VALUE='10'>Contacts</OPTION>\n").append(
                "<OPTION VALUE='7,8,10'>All</OPTION>\n");
        } else if (aUserTypeId.trim().equals("10")) {
            userFilter.append("<OPTION VALUE='7'>Users</OPTION>\n").append("<OPTION VALUE='8'>Mailing Lists</OPTION>\n").append("<OPTION VALUE='10' SELECTED>Contacts</OPTION>\n").append(
                "<OPTION VALUE='7,8,10'>All</OPTION>\n");
        } else if (aUserTypeId.trim().equals("7,8,10")) {
            userFilter.append("<OPTION VALUE='7'>Users</OPTION>\n").append("<OPTION VALUE='8'>Mailing Lists</OPTION>\n").append("<OPTION VALUE='10'>Contacts</OPTION>\n").append(
                "<OPTION VALUE='7,8,10' SELECTED>All</OPTION>\n");
        } else {
            userFilter.append("<OPTION VALUE='7' SELECTED>Users</OPTION>\n").append("<OPTION VALUE='8'>Mailing Lists</OPTION>\n").append("<OPTION VALUE='10'>Contacts</OPTION>\n").append(
                "<OPTION VALUE='7,8,10'>All</OPTION>\n");
        }

        userFilter.append("</select></td>");

        return userFilter.toString();
    }
}
