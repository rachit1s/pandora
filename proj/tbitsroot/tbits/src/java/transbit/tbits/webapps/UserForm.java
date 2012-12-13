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
 * UserForm.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;

//import section for TBits
import transbit.tbits.config.WebConfig;
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
 * The UserForm class. This class displays form from which a
 * list of users can be selected.
 *
 * @author : Vaibhav
 * @author : Vinod Gupta
 * @version $Id: $
 */
public class UserForm extends HttpServlet {

    // Logger used to log information messages/errors to the application log.
    private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

    //~--- methods ------------------------------------------------------------

    /**
     * This method services the Http-GET Requests for this Servlet.
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
        User        user    = null;

        // Check If user is Valid
        try {

            // Validate the user before proceeding further.
            user = WebUtil.validateUser(aRequest);
        } catch (DatabaseException e) {
            LOG.severe("DatabaseException", e);
            session.setAttribute("ExceptionObject", e);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));

            return;
        } catch (Exception e) {
            LOG.severe("Exception", e);
            session.setAttribute("ExceptionObject", e);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));

            return;
        }

        WebConfig webConfig = null;

        try {

            // Read user's web-configuration.
            webConfig = WebConfig.getWebConfig(user.getWebConfig());
        } catch (TBitsException e) {
            LOG.severe("TBits Exception", e);
            session.setAttribute("ExceptionObject", e);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));

            return;
        }

        //
        // Check what should be the Filter on the users.
        // It can be
        // - Users -> 7
        // - Mailing Lists -> 8
        // - Contacts -> 10
        //
        String strUserTypeId = aRequest.getParameter("userTypeId");

        strUserTypeId = ((strUserTypeId == null) || strUserTypeId.trim().equals(""))
                        ? "7"
                        : strUserTypeId;

        int userTypeId = 7;

        try {
            userTypeId = Integer.parseInt(strUserTypeId.trim());
        } catch (Exception e) {
            userTypeId = 7;
        }

        //
        // Check if any user/users is/are to be selected by default before
        // loading the page.
        //
        String subscribers = aRequest.getParameter("subscribers");

        if (subscribers == null) {
            subscribers = "";
        }

        String cc = aRequest.getParameter("cc");

        if (cc == null) {
            cc = "";
        }

        //
        // Get the Current Business Area Id. If it is not found in the
        // Request Object, take the user's Home ID. This is just to find
        // out which StyleSheet should be used for this page.
        //
        String strSystemId = aRequest.getParameter("systemId");

        strSystemId = (strSystemId == null)
                      ? ""
                      : strSystemId.trim();

        int systemId = 0;

        try {
            systemId = Integer.parseInt(strSystemId);
        } catch (NumberFormatException e) {
            String systemPrefix = webConfig.getSystemPrefix();
        }

        // BusinessArea ba = BusinessArea.lookupBySystemId(systemId);
        // String sysPrefix = ba.getSystemPrefix();
        // Reads the request html template file.
        DTagReplacer hp = new DTagReplacer("web/tbits-users.htm");

        // String displayThemeSelectedFile =
        // WebUtil.getThemeFile(sysPrefix,
        // webConfig.getStyle().getThemeFile());
        try {

            // Replace the tags present in the html-inteface.
            // hp.replace("displayTheme", displayThemeSelectedFile);
            hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
            hp.replace("userList", getUserList(userTypeId));
            hp.replace("userFilter", getUserFilter(strUserTypeId));
            hp.replace("subscribers", getExistingUserList(subscribers));
            hp.replace("cc", getExistingUserList(cc));
        } catch (DatabaseException dbe) {
            LOG.debug("DatabaseException while parsing", dbe);
        }

        String htmlContent = hp.parse(systemId);

        out.println(htmlContent);
    }

    /**
     * This method services the Http-POST Requests for this Servlet.
     *
     * @param  req the HttpServlet Request Object
     * @param  res the HttpServlet Response Object
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns a list of users in  html format so that this
     * can be set as a list of options to the select tag.
     *
     * @param aExistingUser comma-separated list of users who should be
     *                      put on the RHS when the page is loaded.
     *
     * @return String that has user-list in HTML Format.
     */
    private String getExistingUserList(String aExistingUser) throws DatabaseException {
        StringBuffer buffer           = new StringBuffer();
        ArrayList    existingUserList = WebUtil.convertNamesToUserArrayList(aExistingUser);
        User         user             = null;

        for (int i = 0; i < existingUserList.size(); i++) {
            user = (User) existingUserList.get(i);
            buffer.append("<option value=\"").append(user.getUserLogin()).append("\">").append(user.getDisplayName()).append("</option>\n");
        }

        return buffer.toString();
    }

    /**
     * This method returns a list of filters in  html format so that this
     * can be set as a list of options to the select tag.
     *
     * @param aUserTypeId  Filter on the users.
     * It can be
     * <ul>
     *   <li>Users -> 7
     *   <li>Mailing Lists -> 8
     *   <li>Contacts -> 10
     *   <li>All -> 0
     * </ul>
     *
     * @return list of filters in html format.
     */
    private String getUserFilter(String aUserTypeId) {
        if (aUserTypeId.trim().equals("7")) {
            return "<OPTION VALUE='7' SELECTED>Users</OPTION>\n" + "<OPTION VALUE='8'>Mailing Lists</OPTION>\n" + "<OPTION VALUE='10'>Contacts</OPTION>\n" + "<OPTION VALUE='0'>All</OPTION>\n";
        } else if (aUserTypeId.trim().equals("8")) {
            return "<OPTION VALUE='7'>Users</OPTION>\n" + "<OPTION VALUE='8' SELECTED>Mailing Lists</OPTION>\n" + "<OPTION VALUE='10'>Contacts</OPTION>\n" + "<OPTION VALUE='0'>All</OPTION>\n";
        } else if (aUserTypeId.trim().equals("10")) {
            return "<OPTION VALUE='7'>Users</OPTION>\n" + "<OPTION VALUE='8'>Mailing Lists</OPTION>\n" + "<OPTION VALUE='10' SELECTED>Contacts</OPTION>\n" + "<OPTION VALUE='0'>All</OPTION>\n";
        }

        if (aUserTypeId.trim().equals("0")) {
            return "<OPTION VALUE='7'>Users</OPTION>\n" + "<OPTION VALUE='8'>Mailing Lists</OPTION>\n" + "<OPTION VALUE='10'>Contacts</OPTION>\n" + "<OPTION VALUE='0' SELECTED>All</OPTION>\n";
        } else {
            return "<OPTION VALUE='7' SELECTED>Users</OPTION>\n" + "<OPTION VALUE='8'>Mailing Lists</OPTION>\n" + "<OPTION VALUE='10'>Contacts</OPTION>\n" + "<OPTION VALUE='0'>All</OPTION>\n";
        }
    }

    /**
     * This method returns a list of  users in  html format so that this
     * can be set as a list of options to the select tag.
     *
     * @param aUserTypeId  Filter on the users.
     * It can be
     * <ul>
     *   <li>Users -> 7
     *   <li>Mailing Lists -> 8
     *   <li>Contacts -> 10
     *   <li>All -> 0
     * </ul>
     *
     * @return list of users in html format.
     */
    private String getUserList(int aUserTypeId) {
        ArrayList activeUsers = User.getActiveUsers();

        if (activeUsers == null) {
            return "";
        }

        StringBuffer buffer   = new StringBuffer();
        Iterator     iterator = activeUsers.iterator();
        User         user     = null;

        while (iterator.hasNext()) {
            user = (User) iterator.next();

            if ((aUserTypeId != 0) && (user.getUserTypeId() != aUserTypeId)) {
                continue;
            }

            buffer.append("<option value=\"").append(user.getUserLogin()).append("\">").append(user.getDisplayName()).append("</option>\n");
        }

        return buffer.toString();
    }
}
