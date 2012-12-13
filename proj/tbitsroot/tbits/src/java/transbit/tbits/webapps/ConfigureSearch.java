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
 * ConfigureSearch.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.TBitsConstants;

//TBits Imports
import transbit.tbits.api.Mapper;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.BAConfig;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DateTimeFormat;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.search.Searcher;

//Imports from the current package.
import transbit.tbits.webapps.WebUtil;

//~--- JDK imports ------------------------------------------------------------

//Java imports
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//~--- classes ----------------------------------------------------------------

/**
 * This servlet is used to configure the search related options.
 *
 * @author  Vaibhav, Vinod Gupta.
 * @version $Id: $
 */
public class ConfigureSearch extends HttpServlet implements TBitsConstants {

    // Html Interface used to render the Configure Search page.
    private static final String HTML_FILE = "web/tbits-configure-search.htm";

    // Logger to log Information/Error messages to the Application Log.
    private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

    //~--- methods ------------------------------------------------------------

    /**
     * The doGet method of the servlet.
     *
     * @param  aRequest  the HttpServlet Request Object
     * @param  aResponse the HttpServlet Response Object
     *
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        HttpSession session = aRequest.getSession();

        aResponse.setContentType("text/html");

        PrintWriter out = aResponse.getWriter();

        Utilities.registerMDCParams(aRequest);

        try {
            User      user       = null;
            WebConfig userConfig = null;

            //
            // Validate the user, get the user Object, Read the web
            // configuration settings. Get the Search page related settings.
            //
            user       = WebUtil.validateUser(aRequest);
            userConfig = WebConfig.getWebConfig(user.getWebConfig());

            // Get the request parameters from the path info.
            Hashtable<String, Object> reqParams = WebUtil.getRequestParams(aRequest, userConfig, WebUtil.SEARCH);
            BusinessArea              ba        = (BusinessArea) reqParams.get(Field.BUSINESS_AREA);

            if (ba == null) {
                throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
            }

            int       systemId  = ba.getSystemId();
            String    sysPrefix = ba.getSystemPrefix();
            SysConfig sc        = ba.getSysConfigObject();

            LOG.info("Configuring Search Results: " + "\nUser: " + user.getUserLogin() + "\nBA: " + sysPrefix);

            // Get the User's Configuration for this Business Area.
            BAConfig config = userConfig.getBAConfig(sysPrefix);

            // Get all the fields which the user can view in this business_area
            ArrayList<Field> fieldList = Field.getFieldsBySystemIdAndUserId(systemId, user.getUserId());

            // Get the Display And Result Headers from user's config.
            ArrayList<String> displayHeader = config.getDisplayHeader();

            if ((displayHeader == null) || (displayHeader.size() == 0)) {
                displayHeader = Searcher.ourDefaultHeader;
            }

            ArrayList<String> resultHeader = new ArrayList<String>();

            // Form a table of (fieldname, field object) pairs.
            Hashtable<String, Field> fieldTable = new Hashtable<String, Field>();
            int                      listSize   = fieldList.size();
            String                   fieldName  = null;

            for (int i = 0; i < listSize; i++) {
                Field field = (Field) fieldList.get(i);
                fieldName = field.getName();
                fieldTable.put(fieldName, field);
                if (displayHeader.contains(fieldName) == false) {
                    resultHeader.add(fieldName);
                }
            }

            // Get the Sort Params and Date Format as selected by user.
            String sortField  = config.getSortField();
            int    sortOrder  = config.getSortOrder();
            String dateFormat = userConfig.getListDateFormat();
            //
            // Now, its time to read the Html Template, replace all the tags
            // with appropriate values and then output it.
            //
            DTagReplacer hp         = new DTagReplacer(HTML_FILE);
            StringBuffer sortBuffer = new StringBuffer();

            hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
            hp.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
            hp.replace("systemId", Integer.toString(systemId));
            hp.replace("systemName", ba.getName());
            hp.replace("sysPrefix", ba.getSystemPrefix());
            hp.replace("displayHeader", getDisplayHeaderHtml(getFilteredFields(displayHeader, systemId), fieldTable, sortField, sortBuffer));
            hp.replace("resultHeader", getResultHeaderHtml(getFilteredFields(resultHeader, systemId), fieldTable));
            hp.replace("sortColumn", sortBuffer.toString());
            hp.replace("enableVE", ((config.getEnableVE() == true)
                                    ? "checked"
                                    : ""));

            if (sortOrder == 1) {
                hp.replace("descSelected", " SELECTED ");
                hp.replace("ascSelected", " ");
            } else {
                hp.replace("ascSelected", " SELECTED ");
                hp.replace("descSelected", " ");
            }

            hp.replace("dateFormatList", getDateFormatHtml(dateFormat));
            out.println(hp.parse(systemId));
        } catch (FileNotFoundException fnfe) {
            LOG.severe("The HTML file hasnt been found ", fnfe);
        } catch (DatabaseException de) {
            session.setAttribute("ExceptionObject", de);
            LOG.severe("Database Exception", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));

            return;
        } catch (TBitsException de) {
            session.setAttribute("ExceptionObject", de);
            LOG.severe("TBits Exception", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));

            return;
        } finally {
            Utilities.clearMDCParams();
        }
    }

    /**
     * The doPost method of the servlet.
     *
     * @param  aRequest  the HttpServlet Request Object
     * @param  aResponse the HttpServlet Response Object
     *
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        aResponse.setContentType("text/html");

        PrintWriter out     = aResponse.getWriter();
        HttpSession session = aRequest.getSession();

        Utilities.registerMDCParams(aRequest);

        try {
            User      user       = null;
            WebConfig userConfig = null;

            //
            // Validate the user, get the user Object, Read the web
            // configuration settings. Get the Search page related settings.
            //
            user       = WebUtil.validateUser(aRequest);
            userConfig = WebConfig.getWebConfig(user.getWebConfig());

            // Get the request parameters from the path info.
            Hashtable<String, Object> reqParams = WebUtil.getRequestParams(aRequest, userConfig, WebUtil.SEARCH);
            BusinessArea              ba        = (BusinessArea) reqParams.get(Field.BUSINESS_AREA);

            if (ba == null) {
                throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
            }

            int    systemId  = ba.getSystemId();
            String sysPrefix = ba.getSystemPrefix();

            LOG.info("System Prefix: " + sysPrefix);

            Hashtable<String, BAConfig> baConfigTable = userConfig.getBAConfigs();
            BAConfig                    config        = userConfig.getBAConfig(sysPrefix);
            String                      strAction     = aRequest.getParameter("action");

            // If the action is to revert to default settings, then do the same
            if ((strAction != null) && strAction.trim().equalsIgnoreCase("default")) {

                // BAConfig defaultConfig = userConfig.getBAConfig("");
                // Set the prefix of this default one to the current BAPrefix.
                // defaultConfig.setPrefix(sysPrefix);
                // defaultConfig.setDisplayHeader(ourDefaultHeader);
                config.setDisplayHeader(Searcher.ourDefaultHeader);

                try {

                    // baConfigTable.put(sysPrefix, defaultConfig);
                    baConfigTable.put(sysPrefix, config);
                    userConfig.setBAConfigs(baConfigTable);

                    String xml = userConfig.xmlSerialize();

                    LOG.info("\n" + xml);
                    user.setWebConfig(xml);

                    //
                    // Update User. This internally updates the user in the
                    // Mapper also.
                    //
                    User.update(user);
                    Mapper.refreshBOMapper();
                    out.println("true");
                    LOG.info("Set to default");

                    return;
                } catch (Exception e) {
                    LOG.info("Exception " + e);
                    e.printStackTrace();
                    out.println("false");

                    return;
                }
            }

            // ResultHeader cannot be null, but this can be empty.
            String strResultHeader = aRequest.getParameter("resultHeader");

            if (strResultHeader == null) {
                LOG.info("Result Header is null. Returning false.");
                out.println("false");

                return;
            }

            // Display Header cannot be null or empty.
            String strDisplayHeader = aRequest.getParameter("displayHeader");

            if (strDisplayHeader == null) {
                LOG.info("Display Header is null. Returning false.");
                out.println("false");

                return;
            }

            // Get the sort column selected by user.
            String strSortField = aRequest.getParameter("sortColumn");

            if ((strSortField == null) || strSortField.equals("")) {
                strSortField = Field.REQUEST;
            }

            // Get the sort order selected by user.
            String strSortOrder = aRequest.getParameter("sortOrder");

            if ((strSortOrder == null) || strSortOrder.trim().equals("")) {
                strSortOrder = "1";
            }

            int sortOrder = 1;

            try {
                sortOrder = Integer.parseInt(strSortOrder);
            } catch (Exception e) {
                sortOrder = 1;
            }

            // Get the DateFormat for date values in search results.
            String strDateFormat = aRequest.getParameter("dateFormat");

            if ((strDateFormat == null) || strDateFormat.trim().equals("")) {
                strDateFormat = userConfig.getListDateFormat();
            }

            // Get the user's option on single window.
            String  strEnableVE = aRequest.getParameter("enableVE");
            boolean enableVE    = config.getEnableVE();

            if ((strEnableVE != null) &&!strEnableVE.trim().equals("")) {
                enableVE = true;
            } else {
                enableVE = false;
            }

//          //Get the User-Selected Defautl My-Requests Filter.
//          String strFilter = aRequest.getParameter("myRequestsFilter");
//          int filter = config.getFilter();
//          if((strFilter != null) && (!strFilter.trim().equals(""))) 
//          {
//          try
//          {
//             filter = Integer.parseInt(strFilter.trim());
//          }
//          catch(NumberFormatException nfe)
//          {
//             LOG.severe
//             ("Exception while parsing My-Request filter",nfe);
//             nfe.printStackTrace();
//          }
//          }
            //
            // Incase there is any change in the sort column or sort
            // order then update the user's session appropriately so that,
            // this will be in effect immediately.
            //
            if (!config.getSortField().equals(strSortField)) {
                session.setAttribute("SearchSortField", strSortField);
            }

            if (config.getSortOrder() != sortOrder) {
                session.setAttribute("SearchSortOrder", Integer.toString(sortOrder));
            }

            // Make sure that always request_id is a part of the display header
            if ((strDisplayHeader.matches("^request_id.*") == false) && (strDisplayHeader.matches(".*request_id$") == false) && (strDisplayHeader.matches(".*,request_id,.*") == false)) {
                strDisplayHeader = "request_id," + strDisplayHeader;
            }

            //
            // Also, Make sure that request_id is not a part of the
            // result header.
            //
            if (strResultHeader.indexOf("request_id,") >= 0) {
                strResultHeader.replaceAll("request_id", "");
                strResultHeader.replaceAll(",,", ",");
            }

            config.setPrefix(sysPrefix);

            // config.setResultHeader
            // (ConversionUtil.toArrayList(strResultHeader));
            config.setDisplayHeader(Utilities.toArrayList(strDisplayHeader));
            config.setSortField(strSortField);
            config.setSortOrder(sortOrder);

//          config.setFilter(filter);
            config.setEnableVE(enableVE);
            userConfig.setListDateFormat(strDateFormat);

            //
            // Replace the Old BAConfig object for this BA with the new one
            // in the BAconfig Table for this user.
            //
            baConfigTable.put(sysPrefix, config);
            userConfig.setBAConfigs(baConfigTable);

            //
            // Update the WebConfig field of the user in the database and also
            // in the mapper.
            //
            String xml = userConfig.xmlSerialize();

            user.setWebConfig(xml);

            //
            // Update User. This internally updates the user in the
            // Mapper also.
            //
            User.update(user);
            out.println("true");

            return;
        } catch (Exception e) {
            LOG.info("Exception", e);
            out.println("false");

            return;
        } finally {
            Utilities.clearMDCParams();
        }
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns a list of available date formats.
     *
     * @param aDateFormat  Date Format that should be preselected.
     *
     * @return HTML Select Options List.
     */
    private String getDateFormatHtml(String aDateFormat) throws DatabaseException, TBitsException {
        StringBuffer buffer = new StringBuffer();

        try {
            ArrayList<DateTimeFormat> list = DateTimeFormat.getAllDateTimeFormats();
            int                       size = list.size();

            for (int i = 0; i < size; i++) {
                DateTimeFormat   dtf    = list.get(i);
                String           format = dtf.getFormat();
                SimpleDateFormat sdf    = new SimpleDateFormat(format);

                buffer.append("<OPTION ");

                if (format.equalsIgnoreCase(aDateFormat)) {
                    buffer.append(" SELECTED ");
                }

                buffer.append(" value='").append(format).append("'>").append(sdf.format(new Date())).append("</OPTION>\n");
            }
        } catch (Exception e) {
            LOG.info("Exception while processing Datetime Formats in" + "configure Search", e);
        }

        return buffer.toString();
    }

    /**
     * This method returns a list of fields that should appear on RHS in
     * the field-chooser component of Configure-Search Page.
     *
     * @param aDisplayHeader List of Fields in the Result Header
     * @param aFieldTable    Table of fieldName, Field Object.
     * @param aSortColumn    Default Sort Field.
     * @param aSortBuffer    Buffer to hold the sort field options.
     *
     * @return HTML Select Options List.
     */
    private String getDisplayHeaderHtml(ArrayList<String> aDisplayHeader, Hashtable<String, Field> aFieldTable, String aSortColumn, StringBuffer aSortBuffer) {
        StringBuffer buffer   = new StringBuffer();
        int          listSize = aDisplayHeader.size();

        for (int i = 0; i < listSize; i++) {
            String fieldName = (String) aDisplayHeader.get(i);
            Object obj       = aFieldTable.get(fieldName);

            if (obj == null) {
                continue;
            }

            Field field = (Field) obj;

            buffer.append("<OPTION value='").append(fieldName).append("'>").append(field.getDisplayName()).append("</OPTION>\n");
            aSortBuffer.append("<OPTION ");

            if (fieldName.equalsIgnoreCase(aSortColumn) == true) {
                aSortBuffer.append(" SELECTED ");
            }

            aSortBuffer.append(" value='").append(fieldName).append("'>").append(field.getDisplayName()).append("</OPTION>\n");
            aFieldTable.remove(fieldName);
        }

        return buffer.toString();
    }

    /**
     * This method returns a list of fields on which the user has display
     * permissions.
     *
     * @param fields    List of Fields in the Result Header
     * @param aSystemId Business Area id.
     *
     * @return List of field names.
     */
    private ArrayList<String> getFilteredFields(ArrayList<String> fields, int aSystemId) {
        ArrayList<String> result = new ArrayList<String>();

        for (int i = 0; i < fields.size(); i++) {
            String fieldName = fields.get(i);

            try {
                Field field = Field.lookupBySystemIdAndFieldName(aSystemId, fieldName);

                if (field != null) {
                    int permission = field.getPermission();

                    if ((permission & Permission.DISPLAY) != 0) {
                        result.add(fieldName);
                    }
                } else {
                    LOG.info("No Field: " + fieldName);
                }
            } catch (DatabaseException dbe) {
                LOG.info("DatabaseException while getting display" + "permissions", dbe);
            }
        }

        return result;
    }

    /**
     * This method returns a list of fields that should appear on LHS in
     * the field-chooser component of Configure-Search Page.
     *
     * @param aResultHeader List of Fields in the Result Header
     * @param aFieldTable   Table of fieldName, Field Object.
     *
     * @return HTML Select Options List.
     */
    private String getResultHeaderHtml(ArrayList<String> aResultHeader, Hashtable<String, Field> aFieldTable) {
        StringBuffer buffer   = new StringBuffer();
        int          listSize = aResultHeader.size();

        for (int i = 0; i < listSize; i++) {
            String fieldName = (String) aResultHeader.get(i);
            Object obj       = aFieldTable.get(fieldName);

            if (obj == null) {
                continue;
            }

            Field field = (Field) obj;

            buffer.append("<OPTION value='").append(fieldName).append("'>").append(field.getDisplayName()).append("</OPTION>\n");
            aFieldTable.remove(fieldName);
        }

        return buffer.toString();
    }
}
