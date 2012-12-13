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
 * AnalystProfile.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;

//TBits Imports
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

//Imports from the current package.
import transbit.tbits.webapps.WebUtil;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

//~--- JDK imports ------------------------------------------------------------

//Java Imports
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//~--- classes ----------------------------------------------------------------

/**
 * This class displays the Information pertaining to an analyst in a given
 * business area.
 *
 * @author   : Vaibhav.
 * @version  : $Id: $
 */
public class AnalystProfile extends HttpServlet {

    // Logger used to log Information/Error Messages to the Application Log.
    private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

    // Html Interface used to display the Analyst Info Page.
    private static final String HTML_FILE = "web/tbits-analyst-profile.htm";

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

        HttpSession session = aRequest.getSession();

        try {
            handleRequest(aRequest, aResponse);
        } catch (TBitsException de) {}
        catch (Exception e) {
            LOG.info("",(e));
            session.setAttribute("ExceptionObject", e);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));

            return;
        }
    }

    /**
     * This method services the Http-POST Requests for this Servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        doGet(aRequest, aResponse);
    }

    /**
     * This method actually services the Http-POST Requests for this Servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @throws ServletException
     * @throws IOException
     */
    public void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws DatabaseException, TBitsException, ServletException, IOException, FileNotFoundException {
        aResponse.setContentType("text/html");

        PrintWriter  out        = aResponse.getWriter();
        HttpSession  session    = aRequest.getSession();
        DTagReplacer hp         = new DTagReplacer(HTML_FILE);
        User         user       = null;
        WebConfig    userConfig = null;

        //
        // Validate the user, get the user Object, Read the web configuration
        // settings.
        //
        user = WebUtil.validateUser(aRequest);

        int userId = user.getUserId();

        userConfig = user.getWebConfigObject();
        LOG.info("Analyst Profile: \nUser: " + user.getUserLogin());

        //
        // Call the WebUtil's getParamInfo, which reads the pathInfo and
        // get the corresponding BA object.
        //
        Hashtable<String, Object> paramInfo = WebUtil.getRequestParams(aRequest, userConfig, WebUtil.OPTIONS);
        BusinessArea              ba        = (BusinessArea) paramInfo.get(Field.BUSINESS_AREA);
        int                       systemId  = ba.getSystemId();
        String                    sysPrefix = ba.getSystemPrefix();
        SysConfig                 sc        = ba.getSysConfigObject();

        //
        // Get the table that contains the field name and the permissions
        // the user obtained by virtue of his association with the business
        // area.
        //
        Hashtable<String, Integer> permTable   = RolePermission.getPermissionsBySystemIdAndUserId(systemId, userId);
        boolean                    viewPrivate = WebUtil.getIsPrivate(permTable);

        // Check if the field is present in the request object.
        String fieldName = aRequest.getParameter("fieldName");
        Field  field     = null;

        if ((fieldName == null) || fieldName.trim().equals("")) {
            fieldName = Field.CATEGORY;
        } else {
            try {
                field = Field.lookupBySystemIdAndFieldName(systemId, fieldName);
            } catch (DatabaseException de) {
                LOG.info(de.toString());
                fieldName = Field.CATEGORY;
            }
        }

        if (field == null) {
            try {
                field = Field.lookupBySystemIdAndFieldName(systemId, fieldName);
            } catch (DatabaseException de) {
                LOG.info(de.toString());

                throw de;
            }
        }

        int                     fieldId          = field.getFieldId();
        String                  fieldDisplayName = field.getDisplayName();
        StringBuffer            baListBuffer     = new StringBuffer();
        ArrayList<BusinessArea> baList           = getBAList(systemId, userId, baListBuffer);

        hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
        hp.replace("fieldName", fieldDisplayName);
        hp.replace("userLogin", user.getUserLogin());
        hp.replace("baName", ba.getDisplayName());
        hp.replace("systemId", Integer.toString(systemId));
        hp.replace("baList", baListBuffer.toString());
        hp.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));

        if ((baList == null) || (baList.size() == 0)) {
            hp.replace("errorDisplay", "BLOCK");
            hp.replace("profileDisplay", "NONE");
            hp.replace("fieldList", "");
            hp.replace("headerData", "");
            hp.replace("typeInfo", "");
        } else {
            hp.replace("errorDisplay", "NONE");
            hp.replace("profileDisplay", "BLOCK");
            hp.replace("fieldList", getFieldList(systemId, fieldName, viewPrivate));

            String typeInfo = getTypeInfo(aRequest, systemId, userId, fieldId, fieldName, viewPrivate);

            if ((typeInfo != null) && (typeInfo.trim().equals("") == false)) {
                hp.replace("typeInfo", typeInfo);
                hp.replace("tableDisplay", "BLOCK");
                hp.replace("infoDisplay", "NONE");
            } else {
                hp.replace("typeInfo", "");
                hp.replace("tableDislay", "NONE");
                hp.replace("infoDisplay", "BLOCK");
            }
        }

        out.println(hp.parse(systemId));

        return;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This Method returns the list of business areas where the user is a
     * valid category assignee.
     *
     * @param aSystemId  Business Area that should be preselected.
     * @param aUserId    Id of user under consideration.
     * @param buffer     Buffer to hold the ba list as HTML SELECT options.
     *
     * @return List of Business Areas .
     */
    public ArrayList<BusinessArea> getBAList(int aSystemId, int aUserId, StringBuffer buffer) throws DatabaseException, TBitsException {
        ArrayList<BusinessArea> balist = BusinessArea.getAnalystBusinessAreas(aUserId);

        for (BusinessArea ba : balist) {
            int    systemId  = ba.getSystemId();
            String sysPrefix = ba.getSystemPrefix();

            buffer.append("\n<OPTION VALUE='").append(sysPrefix).append("' ").append((systemId == aSystemId)
                    ? " SELECTED "
                    : "").append(">").append(ba.getDisplayName()).append(" [").append(ba.getSystemPrefix()).append("] ").append("</OPTION>");
        }

        return balist;
    }

    /**
     * This method formats the category profile of the user present in the
     * resultset.
     * @param aRequest TODO
     * @param rs            ResultSet that holds the cateogry profile of the
     *                      user.
     * @param aViewPrivate  True if user can view private types.
     *
     * @return Category profile of the user as HTML
     * @throws Exception In caseof database related exceptions.
     */
    private String getCategoryProfile(HttpServletRequest aRequest, ResultSet rs, boolean aViewPrivate) throws Exception {
        StringBuffer profile = new StringBuffer();

        if (rs == null) {
            return profile.toString();
        }

        String checked = "<image src=\"" + WebUtil.getNearestPath(aRequest, "/web/images/check.gif") + "\" />";

        while (rs.next() != false) {
            String  userLogin   = rs.getString("user_login");
            String  typeName    = rs.getString("type_name");
            boolean isPrivate   = rs.getBoolean("is_private");
            String  emailOption = rs.getString("email_option");
            boolean isVolunteer = rs.getBoolean("is_volunteer");

            if ((isPrivate == true) && (aViewPrivate == false)) {

                //
                // This is a private type and the user does not have
                // permission to view the private types. So skip
                // this.
                //
                continue;
            }

            profile.append("<TR>").append("<TD>").append(typeName).append("</TD>");

            if (userLogin != null) {
                profile.append("<TD>").append(checked).append("</TD>").append("<TD>").append(emailOption).append("</TD>");

                if (isVolunteer == true) {
                    profile.append("<TD>").append(checked).append("</TD>");
                } else {
                    profile.append("<TD>-</TD>");
                }
            } else {
                profile.append("<TD>-</TD>").append("<TD>-</TD>").append("<TD>-</TD>");
            }

            profile.append("</TR>");
        }

        return profile.toString();
    }

    /**
     * This Method returns the list of type'd fields for given BA.
     *
     * @param aSystemId  Business Area that should be preselected.
     * @param aIsPrivate Flag set if user has permission to view private types.
     *
     * @return List of Business Areas as HTML Select Options.
     */
    public String getFieldList(int aSystemId, String aFieldName, boolean aIsPrivate) throws DatabaseException, TBitsException {
        StringBuffer     buffer    = new StringBuffer();
        ArrayList<Field> fieldList = Field.lookupBySystemId(aSystemId);

        Field.setSortField(Field.DISPLAYNAME);
        fieldList = Field.sort(fieldList);

        for (Field field : fieldList) {
            if ((field.getIsPrivate() == true) && (aIsPrivate == false)) {
                continue;
            }

            if (field.getDataTypeId() != DataType.TYPE) {
                continue;
            }

            String fieldName   = field.getName();
            String displayName = field.getDisplayName();

            buffer.append("\n<OPTION VALUE='").append(fieldName).append("' ").append((aFieldName.equalsIgnoreCase(fieldName))
                    ? " SELECTED "
                    : "").append(">").append(displayName).append("</OPTION>");
        }

        return buffer.toString();
    }

    /**
     * This method returns the column group and header information based on the
     * field name..
     *
     * @param fieldName  Name of the field.
     * @return Colgroup and Header information.
     */
    private String getHeaderData(String fieldName) {
        StringBuffer buffer = new StringBuffer();

        if (fieldName.equals(Field.CATEGORY)) {
            buffer.append("<THEAD>").append("<TD style='color:black; width: 150px;'>Name</TD>").append("<TD ").append("style='color:black; width:  90px; align:center;'").append(
                ">Assignee</TD>").append("<TD ").append("style='color:black; width: 200px; align:center;'").append(">Email</TD>").append("<TD ").append(
                "style='color:black; width:  90px; align:center;'").append(">Volunteer</TD>").append("</THEAD>");
        } else {
            buffer.append("<THEAD>").append("<TD style='color:black; width: 25%'>Name</TD>").append("<TD style='color:black; width: 75%'>Email</TD>").append("</THEAD>");
        }

        return buffer.toString();
    }

    /**
     * This Method returns the category information of a user in a given
     * business area.
     * @param aRequest TODO
     * @param aSystemId     Business Area
     * @param aUserId       Id of user under consideration.
     * @param aViewPrivate  Flag if user has View on Private categories.
     *
     * @return Category information in HTML.
     */
    public String getTypeInfo(HttpServletRequest aRequest, int aSystemId, int aUserId, int aFieldId, String aFieldName, boolean aViewPrivate) {
        StringBuffer buffer     = new StringBuffer();
        String       header     = "";
        String       profile    = "";
        Connection   connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_tu_getAnalystInfo ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aFieldId);
            cs.setInt(3, aUserId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {

                /*
                 * Get the profile based on the field name.
                 */
                if (aFieldName.equals(Field.CATEGORY)) {
                    profile = getCategoryProfile(aRequest, rs, aViewPrivate);
                } else {
                    profile = getTypeProfile(aRequest, rs, aViewPrivate);
                }

                // If profile is neither null nor empty, get the header.
                if ((profile != null) && (profile.trim().equals("") == false)) {
                    header = getHeaderData(aFieldName);
                }

                buffer.append(header).append(profile);
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (Exception e) {
            LOG.warn("",(e));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                    connection = null;
                }
            } catch (Exception e) {
                LOG.warn("Error while closing connection: " + e.toString());
            }
        }

        return buffer.toString();
    }

    /**
     * This method returns the profile of the user for the given type.
     * @param aRequest TODO
     * @param rs            ResultSet that holds the field profile of the user.
     * @param aViewPrivate  True if user can view private types.
     *
     * @return Category profile of the user as HTML
     * @throws Exception In caseof database related exceptions.
     */
    private String getTypeProfile(HttpServletRequest aRequest, ResultSet rs, boolean aViewPrivate) throws Exception {
        StringBuffer profile = new StringBuffer();

        if (rs == null) {
            return profile.toString();
        }

        String checked = "<image src=\"" + WebUtil.getNearestPath(aRequest, "/web/images/check.gif") + "\" />";

        while (rs.next() != false) {
            String  userLogin   = rs.getString("user_login");
            String  typeName    = rs.getString("type_name");
            boolean isPrivate   = rs.getBoolean("is_private");
            String  emailOption = rs.getString("email_option");

            if ((isPrivate == true) && (aViewPrivate == false)) {

                //
                // This is a private type and the user does not have
                // permission to view the private types. So skip
                // this.
                //
                continue;
            }

            profile.append("<TR>").append("<TD>").append(typeName).append("</TD>");

            if (userLogin != null) {
                profile.append("<TD>").append(emailOption).append("</TD>");
            } else {
                profile.append("<TD>-</TD>");
            }

            profile.append("</TR>");
        }

        return profile.toString();
    }
}
