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
 * ConfigureMyRequests.java
 *
 * $Header:
 *
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.Messages;
import transbit.tbits.common.DTagReplacer;

//TBits Imports.
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.BAConfig;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

import static transbit.tbits.Helper.TBitsConstants.FILTER_ASSIGNEE;

//My Requests filter.
import static transbit.tbits.Helper.TBitsConstants.FILTER_LOGGER;
import static transbit.tbits.Helper.TBitsConstants.FILTER_PRIMARY_ASSIGNEE;
import static transbit.tbits.Helper.TBitsConstants.FILTER_SUBSCRIBER;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

//~--- JDK imports ------------------------------------------------------------

//Java imports.
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//~--- classes ----------------------------------------------------------------

//Third-party imports.

/**
 * This servlet mostly does rendering of different views in search page.
 *
 * @author  Vaibhav.
 * @version $Id: $
 */
public class ConfigureMyRequests extends HttpServlet {

    // Application logger.
    public static TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

    // Html interface to save shortcut.
    public static final String MY_REQUESTS_CONFIG = "web/tbits-configure-myrequests.htm";

    // Table that maps the currentFilter with its corresponding filter options.
    public static Hashtable<Integer, String> ourFilterOptions;

    //~--- static initializers ------------------------------------------------

    static {

        // Options based on my-requests filter.
        StringBuilder buffer        = new StringBuilder();
        int           currentFilter = FILTER_LOGGER;

        ourFilterOptions = new Hashtable<Integer, String>();
        currentFilter    = FILTER_LOGGER;
        buffer.append("<OPTION value='").append(FILTER_LOGGER).append("' SELECTED>Logger</OPTION>\n").append("<OPTION value='").append(FILTER_ASSIGNEE).append("'>Assignee</OPTION>\n").append(
            "<OPTION value='").append(FILTER_SUBSCRIBER).append("'>Subscriber</OPTION>\n").append("<OPTION value='").append(FILTER_PRIMARY_ASSIGNEE).append("'>PrimaryAssignee</OPTION>\n");
        ourFilterOptions.put(currentFilter, buffer.toString());
        buffer        = new StringBuilder();
        currentFilter = FILTER_ASSIGNEE;
        buffer.append("<OPTION value='").append(FILTER_LOGGER).append("'>Logger</OPTION>\n").append("<OPTION value='").append(FILTER_ASSIGNEE).append("' SELECTED >Assignee</OPTION>\n").append(
            "<OPTION value='").append(FILTER_SUBSCRIBER).append("'>Subscriber</OPTION>\n").append("<OPTION value='").append(FILTER_PRIMARY_ASSIGNEE).append("'>PrimaryAssignee</OPTION>\n");
        ourFilterOptions.put(currentFilter, buffer.toString());
        buffer        = new StringBuilder();
        currentFilter = FILTER_SUBSCRIBER;
        buffer.append("<OPTION value='").append(FILTER_LOGGER).append("'>Logger</OPTION>\n").append("<OPTION value='").append(FILTER_ASSIGNEE).append("'>Assignee</OPTION>\n").append(
            "<OPTION value='").append(FILTER_SUBSCRIBER).append("' SELECTED >Subscriber</OPTION>\n").append("<OPTION value='").append(FILTER_PRIMARY_ASSIGNEE).append("'>PrimaryAssignee</OPTION>\n");
        ourFilterOptions.put(currentFilter, buffer.toString());
        buffer        = new StringBuilder();
        currentFilter = FILTER_PRIMARY_ASSIGNEE;
        buffer.append("<OPTION value='").append(FILTER_LOGGER).append("'>Logger</OPTION>\n").append("<OPTION value='").append(FILTER_ASSIGNEE).append("'>Assignee</OPTION>\n").append(
            "<OPTION value='").append(FILTER_SUBSCRIBER).append("'>Subscriber</OPTION>\n").append("<OPTION value='").append(FILTER_PRIMARY_ASSIGNEE).append("' SELECTED >PrimaryAssignee</OPTION>\n");
        ourFilterOptions.put(currentFilter, buffer.toString());
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method to service http-get requests.
     *
     * @param  aRequest  HttpServletRequest object.
     * @param  aResponse HttpServletResponse object.
     *
     * @exception ServletException
     * @exception IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        HttpSession aSession = aRequest.getSession(true);

        try {
            long start = System.currentTimeMillis();

            handleGetRequest(aRequest, aResponse);

            long end = System.currentTimeMillis();
        } catch (FileNotFoundException fnfe) {
            LOG.warn("",(fnfe));

            TBitsException de = new TBitsException(Messages.getMessage("HTML_INTERFACE_MISSING"));

            aSession.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        } catch (Exception de) {
            LOG.warn("",(de));
            aSession.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        }
    }

    /**
     * Method to service http-post requests.
     *
     * @param  aRequest  HttpServletRequest object.
     * @param  aResponse HttpServletResponse object.
     *
     * @exception ServletException
     * @exception IOException
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        HttpSession aSession = aRequest.getSession(true);

        try {
            long start = System.currentTimeMillis();

            handlePostRequest(aRequest, aResponse);

            long end = System.currentTimeMillis();
        } catch (FileNotFoundException fnfe) {
            LOG.warn("",(fnfe));

            TBitsException de = new TBitsException(Messages.getMessage("HTML_INTERFACE_MISSING"));

            aSession.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        } catch (Exception de) {
            LOG.warn("",(de));
            aSession.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        }
    }

    /**
     * This method actually services the http requests.
     *
     *
     * @param aRequest  HttpServletRequest object.
     * @param aResponse HttpServletResponse object.
     * @exception ServletException
     * @exception IOException
     * @exception DESTBitsExceptionexception DatabaseException
     */
    public void handleGetRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException, FileNotFoundException {

        //
        // Get all the mandatory objects required from the Http Request object.
        // - PrintWriter to output the HTML content.
        // - Session object to store the results and other configurations.
        // - User object of the user accessing this page.
        //
        aResponse.setContentType("text/html");

        PrintWriter out        = aResponse.getWriter();
        HttpSession aSession   = aRequest.getSession();
        User        user       = WebUtil.validateUser(aRequest);
        WebConfig   userConfig = user.getWebConfigObject();
        int         userId     = user.getUserId();

        LOG.info("Configuring My-Requests Page: " + "\nUser: " + user.getUserLogin());

        // Get the User's default business area.
        String homePrefix = userConfig.getSystemPrefix();
        String action     = aRequest.getParameter("action");

        if ((action != null) && action.trim().equalsIgnoreCase("statusInfo")) {

            //
            // This is an XML Http Request to get the status information of
            // the specified business area.
            //
            String sysPrefix = aRequest.getParameter("sysPrefix");

            if ((sysPrefix == null) || sysPrefix.trim().equals("")) {
                LOG.info("Invalid BA Prefix: " + sysPrefix);
                out.println("false");

                return;
            }

            // Get the correpsonding Business Area object.
            BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

            if (ba == null) {
                LOG.info("Invalid BA: " + sysPrefix);
                out.println("false");

                return;
            }

            // Read the required properties of the BA.
            int       systemId = ba.getSystemId();
            String    dName    = ba.getDisplayName();
            SysConfig sc       = ba.getSysConfigObject();

            // Get the user's configuration in this BA.
            BAConfig baConfig = userConfig.getBAConfig(sysPrefix);

            // Get the permission table for this user.
            Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(systemId, userId);

            // Check if the user has permission to view private types.
            boolean isPrivate = WebUtil.getIsPrivate(permTable);

            // Get the status filters in my-requests page in this BA.
            ArrayList<String> statFilter = baConfig.getStatusFilter();

            // Html serialize the types
            String sf = getStatusFilterJSON(systemId, statFilter, isPrivate, baConfig.getShowBA());

            out.println(sf);

            return;
        }

        // Check if the filter is present in the request object.
        int filter = getMyRequestsFilter(aRequest, userConfig.getFilter());

        // Default is LOGGER filter.
        String  strFilter = "";
        boolean primary   = false;
        boolean first     = true;

        if ((filter & FILTER_LOGGER) != 0) {
            strFilter = "2";
            first     = false;
        }

        if ((filter & FILTER_ASSIGNEE) != 0 || (filter & FILTER_PRIMARY_ASSIGNEE) != 0) {
            if (first == false) {
                strFilter = strFilter + ",";
            }

            strFilter += "3";
            first     = false;
        }

        if ((filter & FILTER_SUBSCRIBER) != 0) {
            if (first == false) {
                strFilter = strFilter + ",";
            }

            strFilter += "4";
            first     = false;
        }

        if ((filter & FILTER_PRIMARY_ASSIGNEE) != 0) {
            primary = true;
        }

        LOG.info("Filter: " + strFilter);

        // Get the list of BA Prefixes where this user is a request user.
        ArrayList<String> prefixList = BusinessArea.getUserBAList(userId, strFilter, primary);

        LOG.info("Prefix List: " + prefixList);

        if ((prefixList == null) || (prefixList.size() == 0)) {
            throw new TBitsException("No business areas matched the criteria.");
        }

        // Get the corresponding Business Area objects
        ArrayList<BusinessArea> baList = new ArrayList<BusinessArea>();

        /*
         * During the process of fetching the BA objects, check if the
         * homePrefix is a part of this list. This is just to fallback on the
         * homePrefix as default selection if no sysPrefix is found in the
         * request object.
         */
        boolean validHomePrefix = false;

        for (String sysPrefix : prefixList) {
            BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

            if (ba == null) {
                continue;
            }

            baList.add(ba);

            if (sysPrefix.equalsIgnoreCase(homePrefix)) {
                validHomePrefix = true;
            }
        }

        BusinessArea.setSortField(BusinessArea.DISPLAYNAME);
        baList = BusinessArea.sort(baList);

        // Check if the homePrefix is a part of the BA List obtained.
        if (validHomePrefix == false) {

            //
            // If the BA List is not empty, then consider the first one as
            // the user's homeprefix.
            //
            if (baList.size() > 0) {
                validHomePrefix = true;
                homePrefix      = baList.get(0).getSystemPrefix();
            }
        }

        //
        // Check if the system prefix is already specified in the request
        // object.
        //
        String sysPrefix = aRequest.getParameter("sysPrefix");

        if ((sysPrefix == null) || sysPrefix.trim().equals("")) {

            // If it is not specified then check if the home prefix is valid.
            if (validHomePrefix == true) {
                sysPrefix = homePrefix;
            } else {
                throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
            }
        }

        // Get the correpsonding Business Area object.
        BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }

        // Read the required properties of the BA.
        int       systemId = ba.getSystemId();
        String    dName    = ba.getDisplayName();
        SysConfig sc       = ba.getSysConfigObject();

        // Get the user's configuration in this BA.
        BAConfig baConfig = userConfig.getBAConfig(sysPrefix);

        // Get the permission table for this user.
        Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(systemId, userId);

        // Check if the user has permission to view private types.
        boolean isPrivate = WebUtil.getIsPrivate(permTable);

        // Get the status filters in my-requests page in this BA.
        ArrayList<String> statFilter = baConfig.getStatusFilter();

        /*
         * Check the showBA option and decide on
         *   - status of Show checkbox
         *   - disabled property of status drop down.
         */
        String disabled = "";
        String checked  = "";

        if (baConfig.getShowBA() == true) {
            checked = "CHECKED";
        } else {
            disabled = "DISABLED";
        }

        // Html serialize the business areas.
        String baHtml = renderBAList(baList, sysPrefix);

        // Html serialize the types
        String       sfHtml = getStatusFilterHtml(systemId, statFilter, isPrivate);
        DTagReplacer hp     = new DTagReplacer(MY_REQUESTS_CONFIG);

        hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
        hp.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
        hp.replace("baList", baHtml);
        hp.replace("disabled", disabled);
        hp.replace("checked", checked);
        hp.replace("statuses", sfHtml);
        out.println(hp.parse(systemId));

        return;
    }

    /**
     * This method actually services the http requests.
     *
     *
     * @param aRequest  HttpServletRequest object.
     * @param aResponse HttpServletResponse object.
     * @exception ServletException
     * @exception IOException
     * @exception DESTBitsExceptionexception DatabaseException
     */
    public void handlePostRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException, FileNotFoundException {

        //
        // Get all the mandatory objects required from the Http Request object.
        // - PrintWriter to output the HTML content.
        // - Session object to store the results and other configurations.
        // - User object of the user accessing this page.
        //
        aResponse.setContentType("text/html");

        PrintWriter out        = aResponse.getWriter();
        HttpSession aSession   = aRequest.getSession();
        User        user       = WebUtil.validateUser(aRequest);
        WebConfig   userConfig = user.getWebConfigObject();
        int         userId     = user.getUserId();

        // Get the system prefix in the query string.
        String sysPrefix = aRequest.getParameter("sysPrefix");

        if ((sysPrefix == null) || sysPrefix.trim().equalsIgnoreCase("")) {
            LOG.info("Invalid BA Prefix: " + sysPrefix);
            out.println("false");

            return;
        }

        BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

        if (ba == null) {
            LOG.info("Invalid BA: " + sysPrefix);
            out.println("false");

            return;
        }

        int    systemId = ba.getSystemId();
        String dName    = ba.getDisplayName();

        sysPrefix = ba.getSystemPrefix();

        /*
         * Get the list of statuses selected from the dropdown.
         */
        String statusList = aRequest.getParameter("statusList");

        if ((statusList == null) || statusList.trim().equals("")) {
            statusList = "__NO_FILTER__";
        } else {
            statusList = statusList.trim();
        }

        ArrayList<String> sList = Utilities.toArrayList(statusList);

        /*
         * Check if this BA should be shown by default in My Requests.
         */
        String  strShow = aRequest.getParameter("show");
        boolean show    = true;

        if ((strShow != null) && strShow.trim().equals("false")) {
            show = false;
        }

        /*
         * Get the configuration for this BA and call the mutator methods
         * with the new values.
         */
        BAConfig baConfig = userConfig.getBAConfig(sysPrefix);

        baConfig.setStatusFilter(sList);
        baConfig.setShowBA(show);

        /*
         * Put back the config for this BA in web config, xml-serialize it and
         * update the user's web_config column.
         */
        userConfig.setBAConfig(sysPrefix, baConfig);
        user.setWebConfig(userConfig.xmlSerialize());

        try {
            User.update(user);

            /*
             * We are still in this block as update operation is successful.
             * So convey this to the invoker and return.
             */
            out.println("true");

            return;
        } catch (Exception e) {
            LOG.info("",(e));
        }

        // We come here if the update operation fails. convey this to the caller
        out.println("false");

        return;
    }

    /**
     * This method returns the list of Business Areas the user can view with
     * the specified BA preselected.
     *
     * @param aBAList  List of business areas.
     * @param aPrefix  Business Area prefix.
     *
     * @return List of Business Area objects rendered as HTML Select options.
     */
    private String renderBAList(ArrayList<BusinessArea> aBAList, String aPrefix) throws DatabaseException {
        StringBuffer list = new StringBuffer();

        if (aBAList != null) {
            for (BusinessArea ba : aBAList) {
                String sysPrefix = ba.getSystemPrefix();

                list.append("\n<OPTION value='").append(sysPrefix).append("'");

                if (aPrefix.equalsIgnoreCase(sysPrefix)) {
                    list.append(" SELECTED ");
                }

                list.append(">").append(ba.getDisplayName()).append(" [").append(sysPrefix).append("]").append((ba.getIsPrivate() == true)
                        ? "&nbsp;&dagger;"
                        : "").append("</OPTION>");
            }
        }

        return list.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method checks if the filter for my-requests page is present in
     * Http Request or Http Session. If not found, then the user's configured
     * filter in the options page is returned.
     *
     * @param aRequest Http Request Object.
     * @param aFilter  Default filter configured by the user in the
     *                 options page.
     *
     * @return Filter for my-requests.
     */
    private int getMyRequestsFilter(HttpServletRequest aRequest, int aFilter) {
        HttpSession session = aRequest.getSession();
        int         filter  = aFilter;

        //
        // Check if the filter is present in the Http Request. If so that
        // will take precedence
        //
        String strFilter = aRequest.getParameter("filter");

        if ((strFilter != null) && (strFilter.trim().equals("") == false)) {
            try {
                filter = Integer.parseInt(strFilter);
            } catch (Exception e) {
                filter = aFilter;
            }
        } else {

            // Check if it can be found in the session.
            Integer temp = (Integer) session.getAttribute("MY_REQUESTS_FILTER");

            if (temp != null) {
                filter = temp.intValue();
            }
        }

        // If the filter is still 0, then consider the default.
        if (filter == 0) {
            filter = FILTER_LOGGER + FILTER_ASSIGNEE + FILTER_SUBSCRIBER + FILTER_PRIMARY_ASSIGNEE;
        }

        // Store the value in session for later reference.
        session.setAttribute("MY_REQUESTS_FILTER", filter);

        return filter;
    }

    /**
     * This method returns the list of statuses in the given BA with those
     * in the statFilter preselected.
     *
     * @param aSystemId   System Id.
     * @param sFilter     Status Filter
     * @param isPrivate   Set if the flag is set to private.
     *
     * @return HTML to render status filter for My Requests.
     */
    public static String getStatusFilterHtml(int aSystemId, ArrayList<String> sFilter, boolean isPrivate) throws DatabaseException {
        StringBuffer    buffer     = new StringBuffer();
        ArrayList<Type> statusList = Type.lookupBySystemIdAndFieldName(aSystemId, Field.STATUS);

        if (statusList == null) {
            return buffer.toString();
        }

        Hashtable<String, Boolean> table = new Hashtable<String, Boolean>();

        if (sFilter == null) {
            sFilter = new ArrayList<String>();
        } else {
            for (String str : sFilter) {
                table.put(str.toUpperCase(), true);
            }
        }

        for (Type type : statusList) {
            String str = "";

            // Ignore inactive statuses.
            if (type.getIsActive() == false) {
                continue;
            }

            // Ignore the private status if the user does not have permission.
            if ((type.getIsPrivate() == true) && (isPrivate == false)) {
                continue;
            }

            if (sFilter.size() == 0) {
                if (type.getIsChecked() == true) {
                    str = " SELECTED ";
                }
            } else {
                if (table.get(type.getName().toUpperCase()) != null) {
                    str = " SELECTED ";
                }
            }

            buffer.append("\n<OPTION value='").append(Utilities.htmlEncode(type.getName())).append("' ").append(str).append(">").append(type.getDisplayName()).append("</OPTION>");
        }

        return buffer.toString();
    }

    /**
     * This method returns the list of statuses in the given BA with those
     * in the statFilter preselected.
     *
     * @param aSystemId   System Id.
     * @param sFilter     Status Filter
     * @param isPrivate   Set if the flag is set to private.
     *
     * @return HTML to render status filter for My Requests.
     */
    public static String getStatusFilterJSON(int aSystemId, ArrayList<String> sFilter, boolean isPrivate, boolean showBA) throws DatabaseException {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\n{").append("\n\t\"showBA\": \"").append(showBA).append("\",").append("\n\t\"statusList\":").append("\n\t[");

        ArrayList<Type> statusList = Type.lookupBySystemIdAndFieldName(aSystemId, Field.STATUS);

        if (statusList == null) {
            buffer.append("\n\t]").append("\n}");

            return buffer.toString();
        }

        Hashtable<String, Boolean> table = new Hashtable<String, Boolean>();

        if (sFilter == null) {
            sFilter = new ArrayList<String>();
        } else {
            for (String str : sFilter) {
                table.put(str.toUpperCase(), true);
            }
        }

        boolean first = true;

        for (Type type : statusList) {
            boolean isSelected = false;

            // Ignore inactive statuses.
            if (type.getIsActive() == false) {
                continue;
            }

            // Ignore the private status if the user does not have permission.
            if ((type.getIsPrivate() == true) && (isPrivate == false)) {
                continue;
            }

            if (sFilter.size() == 0) {
                if (type.getIsChecked() == true) {
                    isSelected = true;
                }
            } else {
                if (table.get(type.getName().toUpperCase()) != null) {
                    isSelected = true;
                }
            }

            if (first == false) {
                buffer.append(",");
            } else {
                first = false;
            }

            buffer.append("\n\t\t{").append("\n\t\t\t\"name\":\" ").append(Utilities.htmlEncode(type.getName())).append("\",").append("\n\t\t\t\"displayName\":\" ").append(
                Utilities.htmlEncode(type.getDisplayName())).append("\",").append("\n\t\t\t\"isSelected\":\" ").append(isSelected).append("\"").append("\n\t\t}");
        }

        buffer.append("\n\t]").append("\n}");

        return buffer.toString();
    }
}
