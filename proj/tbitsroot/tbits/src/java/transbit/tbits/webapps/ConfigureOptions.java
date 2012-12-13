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
 * ConfigureOptions.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------
import transbit.tbits.authentication.AuthConstants;
import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.BAConfig;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DateTimeFormat;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

//Static imports.
//Imports from the current package.
import transbit.tbits.upgrade.UpgradeDB;
import transbit.tbits.upgrade.VersionInfo;
import transbit.tbits.webapps.WebUtil;

//~--- JDK imports ------------------------------------------------------------

//Java imports
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//~--- classes ----------------------------------------------------------------

/**
 * This servlet is used to configure the general options across TBits.
 *
 * @author  Vaibhav, Vinod Gupta 
 * @version $Id: $
 */
public class ConfigureOptions extends HttpServlet implements TBitsConstants, TBitsPropEnum {

    // Html Interface used for displaying the TBits Options Page.
    private static final String HTML_FILE       = "web/tbits-options.htm";
    private static final String BA_OPTIONS_FILE = "web/tbits-ba-options.htm";

    // Loggers used to log information/error messages to the Application Log.
    private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

    // Current Domain.
    public static String ourCurrentSite;

    //~--- static initializers ------------------------------------------------

    static {
        try {
            ourCurrentSite = PropertiesHandler.getProperty(KEY_DOMAIN);
        } catch (Exception e) {
            LOG.info("Value for " + KEY_DOMAIN + " is not found");
        }

        if ((ourCurrentSite == null) || ourCurrentSite.trim().equals("")) {
            ourCurrentSite = "HYD";
        } else {
            ourCurrentSite = ourCurrentSite.toUpperCase();
        }
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method adds all the elements of the given comma separated string
     * to a Hashtable
     *
     * @param css Comma separated String containing the values.
     *
     * @return Hashtable containing all the values.
     */
    private Hashtable<String, Object> cssToHash(String css) {
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        if (css == null) {
            return result;
        }

        StringTokenizer st = new StringTokenizer(css, ",;");

        while (st.hasMoreTokens()) {
            String string = ((String) st.nextToken()).trim();

            if (!string.equals("")) {
                result.put(string, new Object());
            }
        }

        return result;
    }

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

        Utilities.registerMDCParams(aRequest);

        try {
            doGetHandler(aRequest, aResponse);
        } catch (DatabaseException de) {
            session.setAttribute("ExceptionObject", de);
            LOG.severe("DatabaseException", de);
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
     * Handler for Get Requests.
     *
     *
     * @param aRequest
     * @param aResponse
     * @throws ServletException
     * @throws IOException
     * @throws TBitsException
     * @throws DatabaseException
     * @throws FileNotFoundException
     */
    private void doGetHandler(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException, FileNotFoundException {
        aResponse.setContentType("text/html");

        PrintWriter out = aResponse.getWriter();

        /*
         * Validate the user, get the user Object, Read the web configuration
         * settings.
         */
        User      user       = WebUtil.validateUser(aRequest);
        int       userId     = user.getUserId();
        String    userLogin  = user.getUserLogin();
        WebConfig userConfig = WebConfig.getWebConfig(user.getWebConfig());

        LOG.info("Configuring TBits Options: " + "User: " + userLogin);

        /*
         * Call the WebUtil's getParamInfo, which reads the pathInfo and
         * get the corresponding BA object.
         */
        Hashtable paramInfo = WebUtil.getRequestParams(aRequest, userConfig, WebUtil.OPTIONS);

        // Get the BA Object corresponding to this System Id.
        BusinessArea ba = (BusinessArea) paramInfo.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }

        int       systemId  = ba.getSystemId();
        String    sysPrefix = ba.getSystemPrefix();
        SysConfig sc        = ba.getSysConfigObject();

        // Get the User's Configuration for this Business Area.
        Hashtable<String, BAConfig> baConfigs = userConfig.getBAConfigs();
        BAConfig                    config    = userConfig.getBAConfig(sysPrefix);

        /*
         * Get the table that contains the field name and the permissions
         * the user obtained by virtue of his association with the business
         * area.
         */
        Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(systemId, user.getUserId());
        boolean                    isAdmin   = false;

        if (permTable.get("__ADMIN__") != null) {
            isAdmin = true;
        }

        String dateFormat = userConfig.getWebDateFormat();

        /*
         * Now, its time to read the Html Template, replace all the tags
         * with appropriate values and then output it.
         */
        DTagReplacer hp      = new DTagReplacer(HTML_FILE);
        StringBuffer sysList = new StringBuffer();

        getBAList(userConfig.getSystemPrefix(), user.getUserId(), sysList);
        getMyRequestFilterSettings(hp, userConfig.getFilter());
        hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
        hp.replace("systemId", Integer.toString(systemId));
        hp.replace("formAction", sysPrefix);
        hp.replace("sysPrefix", sysPrefix);
        hp.replace("currentSite", ourCurrentSite);
        hp.replace("systemId", Integer.toString(systemId));
        hp.replace("businessAreaList", sysList.toString());
        hp.replace("dateFormatList", getDateFormatHtml(dateFormat));
        hp.replace("instance", ourCurrentSite);
		hp.replace("userLogin", userLogin);

		String display_logout = "none";
		if(aRequest.getAuthType() == AuthConstants.AUTH_TYPE)
			display_logout = "";
		hp.replace("display_logout", display_logout);

        String styleSheet = sc.getWebStylesheet();

        hp.replace("cssFile", WebUtil.getCSSFile(styleSheet, sysPrefix, false));

        int prefZone = userConfig.getPreferredZone();

        hp.replace("preferredZoneList", getZoneList(prefZone));

        int rowsPerPage = userConfig.getRowsPerPage();

        hp.replace("resultsPerPage", getResultsPerPageHtml(rowsPerPage));

        int refreshRate = userConfig.getRefreshRate();

        hp.replace("refreshRate", getRateHtml(refreshRate, "refresh"));

        int actionOrder = userConfig.getActionOrder();

        hp.replace("actionOrder", getActionOrderHtml(actionOrder));

        int autoSaveRate = userConfig.getAutoSaveRate();

        hp.replace("autoSaveRate", getRateHtml(autoSaveRate, "save"));

        StringBuffer analystBAList    = new StringBuffer();
        StringBuffer csvAnalystBAList = new StringBuffer();

        getVacationSysList(userConfig, analystBAList, csvAnalystBAList, userId);
        hp.replace("myAnalystVacationBAList", analystBAList.toString());
        hp.replace("analystBAList", csvAnalystBAList.toString());

        if (analystBAList.toString().trim().equals("")) {
            hp.replace("catProfileDisplay", "none");
        } else {
            hp.replace("catProfileDisplay", "inline-block");
        }

        String checked = (userConfig.getIEAutoClose() == true)
                         ? "checked"
                         : "";

        hp.replace("autoClose", checked);

        String sWin = (userConfig.getSingleIEWindow() == true)
                      ? "checked"
                      : "";

        hp.replace("singleWindow", sWin);

        String actHdr = (userConfig.getActionHeader() == true)
                        ? "checked"
                        : "";

        hp.replace("actionHeader", actHdr);

        String appName = PropertiesHandler.getProperty(KEY_APP_NAME);

        hp.replace("appName", appName);

        String appVersion = PropertiesHandler.getProperty(KEY_APP_VERSION);
        VersionInfo dbVersion = UpgradeDB.getOverAllVersion("request");
        
        hp.replace("appVersion", "App: " + appVersion + ", DB: " + dbVersion.getMajor() + ", Systype: " + dbVersion.getSysType());
        out.println(hp.parse(systemId));

        return;
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
        HttpSession session = aRequest.getSession();

        Utilities.registerMDCParams(aRequest);

        try {
            doPostHandler(aRequest, aResponse);
        } catch (DatabaseException de) {
            session.setAttribute("ExceptionObject", de);
            LOG.severe("DatabaseException", de);
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
     * Method to handle the POST requests.
     *
     *
     * @param aRequest
     * @param aResponse
     * @throws ServletException
     * @throws IOException
     * @throws TBitsException
     * @throws DatabaseException
     * @throws FileNotFoundException
     */
    private void doPostHandler(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException, FileNotFoundException {
        aResponse.setContentType("text/html");

        PrintWriter  out        = aResponse.getWriter();
        HttpSession  session    = aRequest.getSession();
        User         user       = WebUtil.validateUser(aRequest);
        WebConfig    userConfig = WebConfig.getWebConfig(user.getWebConfig());
        int          userId     = user.getUserId();
        String       userLogin  = user.getUserLogin();
        Hashtable    paramInfo  = WebUtil.getRequestParams(aRequest, userConfig, WebUtil.OPTIONS);
        BusinessArea ba         = (BusinessArea) paramInfo.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }

        int    systemId  = ba.getSystemId();
        String sysPrefix = ba.getSystemPrefix();
        String strAction = aRequest.getParameter("action");

        if ((strAction != null) && strAction.equalsIgnoreCase("default")) {
            WebConfig                   defaultConfig = new WebConfig();
            Hashtable<String, BAConfig> baConfigs     = userConfig.getBAConfigs();

            // Do not reset the BA Specific configurations and ListDateFormat.
            defaultConfig.setBAConfigs(baConfigs);

            String xml = defaultConfig.xmlSerialize();

            user.setWebConfig(xml);

            // Update User. This internally updates the user in the Mapper also.
            User.update(user);

            String forwardUrl = "/options/" + sysPrefix;

            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, forwardUrl));

            return;
        }

        // Get the Home selected by the user.
        String newHomePrefix = aRequest.getParameter("businessArea");
        String oldHomePrefix = userConfig.getSystemPrefix();

        if ((newHomePrefix == null) || newHomePrefix.trim().equals("")) {
            newHomePrefix = oldHomePrefix;
        }

        /*
         * When there is a change in the user's default BA, overwrite the
         * running BA id with his new Home BA Id.
         */
        if ((newHomePrefix.equalsIgnoreCase(oldHomePrefix) == false)) {
            sysPrefix = newHomePrefix;
        }

        // Get the my-requests filter.
        int filter = readMyRequestsFilter(aRequest);

        /*
         *  If the filter is different from the older value, then overwrite
         * the value in the session.
         */
        if (filter != userConfig.getFilter()) {
            session.setAttribute("MY_REQUESTS_FILTER", filter);
        }

        // Get the user-selected dateformat.
        String strDateFormat = aRequest.getParameter("dateFormat");

        if ((strDateFormat == null) || strDateFormat.trim().equals("")) {
            strDateFormat = userConfig.getWebDateFormat();
        }

        // Get the user-preferred zone.
        int zone = userConfig.getPreferredZone();

        zone = readIntegerValue(aRequest, "preferredZone", zone);

        // Get the user-preferred actionOrder
        int actionOrder = userConfig.getActionOrder();

        actionOrder = readIntegerValue(aRequest, "actionOrder", actionOrder);

        // Get the ResultsPerPage value;
        int resPerPage = userConfig.getRowsPerPage();

        resPerPage = readIntegerValue(aRequest, "resultsPerPage", resPerPage);

        // Get the RefreshRate value;
        int refreshRate = userConfig.getRefreshRate();

        refreshRate = readIntegerValue(aRequest, "refreshRate", refreshRate);

        // Get the AutoSaveRate value;
        int autoSaveRate = userConfig.getAutoSaveRate();

        autoSaveRate = readIntegerValue(aRequest, "autoSaveRate", autoSaveRate);

        // Get the List of Business Areas the user preferred to be on vacation.
        String strVacation = aRequest.getParameter("vacation");

        /*
         * Get the list of Business Areas in which the user does not wish to get
         * mail for his own appends
         */
        String strNotify = aRequest.getParameter("notify");

        // Get the user's option on auto close.
        String  strAutoClose = aRequest.getParameter("autoClose");
        boolean autoClose    = userConfig.getIEAutoClose();

        if ((strAutoClose != null) &&!strAutoClose.trim().equals("")) {
            autoClose = true;
        } else {
            autoClose = false;
        }

        // Get the user's option on single window.
        String  strSingleWindow = aRequest.getParameter("singleWindow");
        boolean singleWindow    = userConfig.getSingleIEWindow();

        if ((strSingleWindow != null) &&!strSingleWindow.trim().equals("")) {
            singleWindow = true;
        } else {
            singleWindow = false;
        }

        // Get the user's option on single window.
        String  strActionHeader = aRequest.getParameter("actionHeader");
        boolean actionHeader    = userConfig.getActionHeader();

        if ((strActionHeader != null) &&!strActionHeader.trim().equals("")) {
            actionHeader = true;
        } else {
            actionHeader = false;
        }

        userConfig.setSystemPrefix(newHomePrefix.toUpperCase());
        userConfig.setFilter(filter);
        userConfig.setWebDateFormat(strDateFormat);
        userConfig.setListDateFormat(strDateFormat);
        userConfig.setPreferredZone(zone);
        userConfig.setIEAutoClose(autoClose);
        userConfig.setActionHeader(actionHeader);
        userConfig.setSingleIEWindow(singleWindow);
        userConfig.setActionOrder(actionOrder);
        userConfig.setRowsPerPage(resPerPage);
        userConfig.setRefreshRate(refreshRate);
        userConfig.setAutoSaveRate(autoSaveRate);

        Hashtable<String, BAConfig> baConfigTable = userConfig.getBAConfigs();

        baConfigTable = modifyOnVacation(baConfigTable, cssToHash(strVacation), cssToHash(strNotify));
        userConfig.setBAConfigs(baConfigTable);

        String xml = userConfig.xmlSerialize();

        user.setWebConfig(xml);

        //
        // Update User. This internally updates the user in the
        // Mapper also.
        //
        User.update(user);
        aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "options/" + sysPrefix));

        return;
    }

    /**
     * This method modifies the onVacation flag for business area specified in
     * vSysList and returns the updated BAConfigTable.
     *
     * @param configTable BA Config Table.
     * @param vSysList    List of BAs user specified to be on vacation.
     *
     * @return Modified BAConfiguration Table.
     */
    private Hashtable<String, BAConfig> modifyOnVacation(Hashtable<String, BAConfig> configTable, Hashtable<String, Object> vSysList, Hashtable<String, Object> nSysList) {
        Hashtable<String, BAConfig> newTable = new Hashtable<String, BAConfig>();

        // ArrayList<BAConfig> list =
        // new ArrayList(configTable.values());
        Collection<BAConfig> list     = configTable.values();
        Iterator             iterator = list.iterator();

        if ((list == null) || (vSysList == null)) {
            return configTable;
        }

        // int size = list.size();
        // for (int i = 0; i < size; i++)
        while (iterator.hasNext()) {
            BAConfig config       = (BAConfig) iterator.next();
            String   systemPrefix = config.getPrefix().toUpperCase();

            if (vSysList.get(systemPrefix) != null) {
                vSysList.remove(systemPrefix);
                config.setVacation(true);
            } else {
                config.setVacation(false);
            }

            if (nSysList.get(systemPrefix) != null) {
                nSysList.remove(systemPrefix);
                config.setNotify(false);
            } else {
                config.setNotify(true);
            }

            newTable.put(systemPrefix, config);
        }

        // For those systemPrefixes remained in the vSysList, there are no
        // corresponding BAConfig objects. So get the BAConfig from the default
        Enumeration<String> keys = vSysList.keys();

        while (keys.hasMoreElements()) {
            try {
                BAConfig config       = new BAConfig();
                String   systemPrefix = keys.nextElement().toUpperCase();

                // LOG.info("SystemId: " + systemId);
                config.setPrefix(systemPrefix);
                config.setVacation(true);

                if (nSysList.get(systemPrefix) != null) {
                    nSysList.remove(systemPrefix);
                    config.setNotify(false);
                }

                newTable.put(systemPrefix, config);
            } catch (Exception e) {
                LOG.info("Exception ", e);
            }
        }

        keys = nSysList.keys();

        while (keys.hasMoreElements()) {
            try {
                BAConfig config       = new BAConfig();
                String   systemPrefix = keys.nextElement().toUpperCase();

                // LOG.info("SystemId: " + systemId);
                config.setPrefix(systemPrefix);
                config.setNotify(false);
                newTable.put(systemPrefix, config);
            } catch (Exception e) {
                LOG.info("Exception ", e);
            }
        }

        return newTable;
    }

    /**
     * This method reads the value of given parameter as an integer.
     *
     * @param aReq
     * @param aParamName
     * @param aDefValue
     * @return
     * @throws ServletException
     */
    private int readIntegerValue(HttpServletRequest aReq, String aParamName, int aDefValue) throws ServletException {
        int    value      = aDefValue;
        String paramValue = aReq.getParameter(aParamName);

        try {
            value = Integer.parseInt(paramValue);
        } catch (Exception e) {
            LOG.info("An exception has occurred while converting the value " + aParamName + " to an integer: " + paramValue + "\n" + "",(e));
            value = aDefValue;
        }

        return value;
    }

    /**
     * This method reads the states of the checkboxes that represent the filter
     * options for my-requests page and calculates the value of the filter.
     *
     * @param aRequest HttpServletRequest.
     * @return  Filter value.
     */
    private int readMyRequestsFilter(HttpServletRequest aRequest) {
        int    filter            = 0;
        String loggerChecked     = aRequest.getParameter("chboxLogger");
        String assigneeChecked   = aRequest.getParameter("chboxAssignee");
        String subscriberChecked = aRequest.getParameter("chboxSubscriber");
        String pAssigneeChecked  = aRequest.getParameter("chboxPrimaryAssignee");

        if ((loggerChecked != null) &&!loggerChecked.trim().equals("")) {
            filter = filter + FILTER_LOGGER;
        }

        if ((assigneeChecked != null) &&!assigneeChecked.trim().equals("")) {
            filter = filter + FILTER_ASSIGNEE;
        }

        if ((subscriberChecked != null) &&!subscriberChecked.trim().equals("")) {
            filter = filter + FILTER_SUBSCRIBER;
        }

        if ((pAssigneeChecked != null) &&!pAssigneeChecked.trim().equals("")) {
            filter = filter + FILTER_PRIMARY_ASSIGNEE;
        }

        return filter;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns a list of allowed orders of actions in Action
     * Details page.
     *
     * @param actionOrder Order that should be pre-selected.
     * @return HTML Select Options List of allowed Action Order values.
     */
    private String getActionOrderHtml(int actionOrder) {
        StringBuffer buffer = new StringBuffer();

        if (actionOrder == ASC_ORDER) {
            buffer.append("<OPTION SELECTED value='" + ASC_ORDER + "'>Ascending</OPTION>\n").append("<OPTION value='" + DESC_ORDER + "'>Descending</OPTION>\n");
        } else {
            buffer.append("<OPTION value='" + ASC_ORDER + "'>Ascending</OPTION>\n").append("<OPTION SELECTED value='" + DESC_ORDER + "'>Descending</OPTION>\n");
        }

        return buffer.toString();
    }

    /**
     * This method returns a list of Business Areas, the user can see.
     *
     * @param aSystemPrefix  Business Area that should be pre-selected.
     * @param aUserId        User accessing the page.
     * @param aBuffer        HTML Select Options of the BA List.
     *
     * @param ArrayList that contains BA Objects the user can see.
     */
    private ArrayList getBAList(String aSystemPrefix, int aUserId, StringBuffer aBuffer) throws DatabaseException, TBitsException {
        ArrayList baList = BusinessArea.getBusinessAreasByUserId(aUserId);

        if ((baList == null) || (baList.size() == 0)) {
            return new ArrayList();
        }

        int     listSize = baList.size();
        boolean selected = false;

        for (int i = 1; i < listSize; i++) {
            BusinessArea ba        = (BusinessArea) baList.get(i);
            String       sysPrefix = ba.getSystemPrefix();

            aBuffer.append("\n<OPTION VALUE='").append(sysPrefix).append("' ");

            if (sysPrefix.equalsIgnoreCase(aSystemPrefix)) {
                aBuffer.append(" SELECTED ");
                selected = true;
            }

            aBuffer.append(">").append(ba.getDisplayName()).append(" [").append(sysPrefix).append("] ").append("</OPTION>");
        }

        StringBuffer firstOption = new StringBuffer();
        BusinessArea ba          = (BusinessArea) baList.get(0);
        String       sysPrefix   = ba.getSystemPrefix();

        firstOption.append("\n<OPTION VALUE='").append(sysPrefix).append("' ");

        if (sysPrefix.equalsIgnoreCase(aSystemPrefix) || (selected == false)) {
            firstOption.append(" SELECTED ");
        }

        firstOption.append(">").append(ba.getDisplayName()).append(" [").append(sysPrefix).append("] ").append("</OPTION>");
        aBuffer.insert(0, firstOption.toString());

        return baList;
    }

    /**
     * This method returns HTML Select Options List of available Date Formats.
     *
     * @param aDateFormat  DateFormat that should be pre-selected.
     *
     * @return HTML Select Options.
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
            LOG.info("Exception while forming DateFormat HTML", e);
        }

        return buffer.toString();
    }

    /**
     * This method checks the user's filter in my-requests page and selects
     * the checkboxes accordingly.
     *
     * @param hp       DTagReplacer object.
     * @param aFilter  Filter.
     *
     */
    private void getMyRequestFilterSettings(DTagReplacer hp, int filter) {
        hp.replace("loggerChecked", ((filter & FILTER_LOGGER) != 0)
                                    ? " CHECKED "
                                    : "");
        hp.replace("assigneeChecked", ((filter & FILTER_ASSIGNEE) != 0)
                                      ? " CHECKED "
                                      : "");
        hp.replace("subscriberChecked", ((filter & FILTER_SUBSCRIBER) != 0)
                                        ? " CHECKED "
                                        : "");
        hp.replace("primaryAssigneeChecked", ((filter & FILTER_PRIMARY_ASSIGNEE) != 0)
                ? " CHECKED "
                : "");

        return;
    }

    /**
     * This method is to get a list of allowed refresh rates.
     *
     * @param refreshRate The value that should be pre-selected.
     *
     * @return HTML Select Options List of allowed refresh rates
     */
    private String getRateHtml(int refreshRate, String aAction) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\n<OPTION value='2'").append((refreshRate == 2)
                ? " SELECTED "
                : "").append(">Every 2 minutes</OPTION>").append("\n<OPTION value='5'").append((refreshRate == 5)
                ? " SELECTED "
                : "").append(">Every 5 minutes</OPTION>").append("\n<OPTION value='10'").append((refreshRate == 10)
                ? " SELECTED "
                : "").append(">Every 10 minutes</OPTION>").append("\n<OPTION value='15'").append((refreshRate == 15)
                ? " SELECTED "
                : "").append(">Every 15 minutes</OPTION>").append("\n<OPTION value='30'").append((refreshRate == 30)
                ? " SELECTED "
                : "").append(">Every 30 minutes</OPTION>").append("\n<OPTION value='60'").append((refreshRate == 60)
                ? " SELECTED "
                : "").append(">Every 1 hour</OPTION>").append("\n<OPTION value='-1'").append((refreshRate == -1)
                ? " SELECTED "
                : "").append(">Do not auto").append(aAction).append("</OPTION>");

        return buffer.toString();
    }

    /**
     * This method is to get a list of allowed ResultsPerPage values.
     *
     * @param resultsPerPage The value that should be pre-selected.
     *
     * @return HTML Select Options List of allowed ResultsPerPage values.
     */
    private String getResultsPerPageHtml(int resultsPerPage) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\n<OPTION value='20'").append((resultsPerPage == 20)
                ? " SELECTED "
                : "").append(">20</OPTION>").append("\n<OPTION value='50'").append((resultsPerPage == 50)
                ? " SELECTED "
                : "").append(">50</OPTION>").append("\n<OPTION value='100'").append((resultsPerPage == 100)
                ? " SELECTED "
                : "").append(">100</OPTION>").append("\n<OPTION value='500'").append((resultsPerPage == 500)
                ? " SELECTED "
                : "").append(">500</OPTION>").append("\n<OPTION value='1000'").append((resultsPerPage == 1000)
                ? " SELECTED "
                : "").append(">1000</OPTION>");

        return buffer.toString();
    }

    /**
     * This method builds list of Business areas where the users is an
     * analyst and is on vacation..
     *
     * @param aUserConfig  User Configuration.
     * @param aBAList      List Of BAs where user is an analyst and is not
     *                     on Vacation - Out Parameter.
     * @param aCSVBAList   List Of BAs where user is an analyst and is not
     *                     on Vacation - Out Parameter.
     * @param aUserId      Id of the user.
     *
     */
    private void getVacationSysList(WebConfig aUserConfig, StringBuffer aBAList, StringBuffer aCSVBAList, int aUserId) throws DatabaseException, FileNotFoundException, IOException {
        ArrayList                   baList        = BusinessArea.getAnalystBusinessAreas(aUserId);
        int                         size          = baList.size();
        Hashtable<String, BAConfig> baConfigTable = aUserConfig.getBAConfigs();

        for (int i = 0; i < size; i++) {
            BusinessArea ba        = (BusinessArea) baList.get(i);
            String       sysPrefix = ba.getSystemPrefix().toUpperCase();
            String       sysName   = ba.getDisplayName();
            BAConfig     baConfig  = aUserConfig.getBAConfig(sysPrefix);

            aCSVBAList.append(ba.getSystemPrefix().toUpperCase()).append(",");

            DTagReplacer hp               = new DTagReplacer(BA_OPTIONS_FILE);
            String       strOnVacation    = "";
            String       strEmailDisabled = "";
            String       bOnVacation      = "";
            String       bEmailDisabled   = "";

            if ((baConfig != null) && (baConfig.getNotify() == false)) {
                strEmailDisabled = " Disabled";
                bEmailDisabled   = "CHECKED";
            }

            if ((baConfig != null) && (baConfig.getVacation() == true)) {
                strOnVacation = " On vacation";
                bOnVacation   = "CHECKED";
            }

            hp.replace("sysName", sysName);
            hp.replace("sysPrefix", sysPrefix);
            hp.replace("bEmailDisabled", bEmailDisabled);
            hp.replace("strEmailDisabled", strEmailDisabled);
            hp.replace("bOnVacation", bOnVacation);
            hp.replace("strOnVacation", strOnVacation);
            aBAList.append(hp.parse(ba.getSystemId()));
        }

        return;
    }

    /**
     * This method is to get a list of zones the user can choose from for his
     * Date values in Action-Details to appear in.
     *
     * @param aZone Zone value that should be pre-selected.
     *
     * @return HTML Select Options List of allowed Zones values.
     */
    private String getZoneList(int aZone) {

        // Default is Site.
        StringBuffer buffer = new StringBuffer();

        if (aZone == LOCAL_ZONE) {
            buffer.append("<OPTION VALUE='" + LOCAL_ZONE + "' SELECTED>Browser time</OPTION>\n").append("<OPTION VALUE='" + SITE_ZONE + "'>Server time</OPTION>\n").append("<OPTION VALUE='" + GMT_ZONE
                          + "'>GMT time</OPTION>\n");
        } else if (aZone == SITE_ZONE) {
            buffer.append("<OPTION VALUE='" + LOCAL_ZONE + "' >Browser time</OPTION>\n").append("<OPTION VALUE='" + SITE_ZONE + "' SELECTED>Server time</OPTION>\n").append("<OPTION VALUE='"
                          + GMT_ZONE + "'>GMT time</OPTION>\n");
        } else if (aZone == GMT_ZONE) {
            buffer.append("<OPTION VALUE='" + LOCAL_ZONE + "' >Browser time</OPTION>\n").append("<OPTION VALUE='" + SITE_ZONE + "'>Server time</OPTION>\n").append("<OPTION VALUE='" + GMT_ZONE
                          + "' SELECTED>GMT time</OPTION>\n");
        } else {
            buffer.append("<OPTION VALUE='" + LOCAL_ZONE + "' SELECTED>Browser time</OPTION>\n").append("<OPTION VALUE='" + SITE_ZONE + "'>Server time</OPTION>\n").append("<OPTION VALUE='" + GMT_ZONE
                          + "' >GMT time</OPTION>\n");
        }

        return buffer.toString();
    }
}
