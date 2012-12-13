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
 * ShortcutHandler.java
 *
 *$Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;
import static transbit.tbits.search.SearchConstants.NORMAL_VIEW;

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

import transbit.tbits.Helper.Messages;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.BAConfig;
import transbit.tbits.config.Shortcut;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BAUser;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

//~--- classes ----------------------------------------------------------------

/**
 * @author Vaibhav
 *
 */
public class ShortcutHandler extends HttpServlet {

    // Application logger.
    public static TBitsLogger  LOG               = TBitsLogger.getLogger(PKG_WEBAPPS);
    public static final String SH_DISP_FILE      = "web/tbits-shortcut.htm";
    public static final String SH_SAVE_FILE      = "web/tbits-save-shortcut.htm";
    public static final String SH_SELECT_DEFAULT = "web/tbits-shortcut-select-default.htm";
    public static final String SH_RECORD         = "web/tbits-shortcut-record.htm";
    public static final String SH_LIST_HEADER    = "web/tbits-shortcut-listhdr.htm";
    public static final String SH_ADMIN_RECORD   = "web/tbits-shortcut-record-admin.htm";
    public static final String SH_ADMIN          = "web/tbits-shortcut-ba-admin.htm";
    public static final String SH_USER           = "web/tbits-shortcut-ba-user.htm";
    public static final String SH_USER_RECORD    = "web/tbits-shortcut-record-user.htm";
    public static final String SH_CONSOLIDATED   = "web/tbits-shortcuts-consolidated.htm";

    //~--- constant enums -----------------------------------------------------

    private enum SHRequestType {
        DISPLAY, LIST, SAVE, MARK_PRIVATE, MARK_SHARED, SET_DEFAULT, UNSET_DEFAULT, DELETE
    }

    ;

    //~--- methods ------------------------------------------------------------

    /**
     * This method deletes a shortcut from the BA's list of shortcuts.
     *
     * @param aBA               Business Area
     * @param aShortcut         Shortcut.
     * @throws DatabaseException
     */
    private static void deleteShortcut(User aUser, BusinessArea aBA, Shortcut aShortcut) throws DatabaseException {
        SysConfig sysConfig = aBA.getSysConfigObject();

        sysConfig.deleteShortcut(aShortcut.getName());

        String xml = sysConfig.xmlSerialize();

        aBA.setSysConfig(xml);
        BusinessArea.update(aBA);
        LOG.info("Deleted shortcut: [ " + aShortcut.getName() + ", " + aBA.getSystemPrefix() + ", BA Shortcut ]");

        /*
         * Check if this is the default shortcut for the user.
         */
        String    sysPrefix       = aBA.getSystemPrefix();
        WebConfig wc              = aUser.getWebConfigObject();
        BAConfig  bc              = wc.getBAConfig(sysPrefix);
        String    defShortcutName = bc.getDefaultShortcutName();

        if (defShortcutName.startsWith("BA:")) {
            defShortcutName = defShortcutName.split(":")[1];

            if (defShortcutName.equals(aShortcut.getName())) {

                /*
                 * Set the default to empty and update the config in the
                 * database as well.
                 */
                bc.setDefaultShortcutName("");
                wc.setBAConfig(sysPrefix, bc);
                xml = wc.xmlSerialize();
                aUser.setWebConfig(xml);
                User.update(aUser);
            }
        }

        return;
    }

    /**
     * This method deletes the shortcut from the user's configuration and
     * updates the database.
     *
     * @param aUser                     User object.
     * @param aPrefix                   Sys Prefix.
     * @param aShortcut                 Shortcut.
     * @throws DatabaseException
     */
    private static void deleteShortcut(User aUser, String aPrefix, Shortcut aShortcut) throws DatabaseException {
        WebConfig webConfig = aUser.getWebConfigObject();
        BAConfig  baConfig  = webConfig.getBAConfig(aPrefix);

        baConfig.deleteShortcut(aShortcut.getName());
        webConfig.setBAConfig(aPrefix, baConfig);

        String xml = webConfig.xmlSerialize();

        aUser.setWebConfig(xml);
        User.update(aUser);
        LOG.info("Deleted shortcut: [ " + aShortcut.getName() + ", " + aPrefix + ", " + aUser.getUserLogin() + ", User Shortcut ]");

        return;
    }

    /**
     * This method returns the HTML that is displayed in the dialog to save a
     * shortcut.
     *
     * @param aRequest           Http Request object.
     * @param aResponse          Http Response object.
     * @param aBA                Business Area.
     * @param aUser              User
     * @throws ServletException
     * @throws IOException
     */
    private void displaySaveDialog(HttpServletRequest aRequest, HttpServletResponse aResponse, BusinessArea aBA, User aUser) throws ServletException, IOException, DatabaseException {
        aResponse.setContentType("text/html");

        PrintWriter out = aResponse.getWriter();

        /*
         * Read the BA details.
         */
        int       systemId  = aBA.getSystemId();
        String    sysPrefix = aBA.getSystemPrefix();
        SysConfig sysConfig = aBA.getSysConfigObject();

        /*
         * Read the user configuration details.
         */
        int       userId      = aUser.getUserId();
        WebConfig webConfig   = aUser.getWebConfigObject();
        BAConfig  baconfig    = webConfig.getBAConfig(sysPrefix);
        boolean   isUserAdmin = isAdmin(systemId, userId);

        /*
         * Parameters to be read from the request object are
         * 1. view: The view of search page in which the shortcut is saved.
         * 2. desc: The text in the description box in search page.
         * 3. filter: The text filter applied in the query.
         * 4. query: DQL representation of the search criteria.
         * 5. listAll: Boolean to check if list All is selected.
         */
        int          view    = readIntegerParam(aRequest, "view", NORMAL_VIEW);
        String       desc    = readStringParam(aRequest, "description", "");
        String       filter  = readStringParam(aRequest, "filter", "subject");
        String       query   = readStringParam(aRequest, "query", "");
        String       listAll = readStringParam(aRequest, "listAll", "");
        String       scList  = getShortcutJSON(baconfig.getShortcuts(), sysConfig.getShortcuts());
        DTagReplacer hp      = new DTagReplacer(SH_SAVE_FILE);

        hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
        hp.replace("cssFile", WebUtil.getCSSFile(sysConfig.getWebStylesheet(), sysPrefix, false));
        hp.replace("sysPrefix", sysPrefix);
        hp.replace("view", Integer.toString(view));
        hp.replace("description", Utilities.htmlEncode(desc));
        hp.replace("filter", filter);
        hp.replace("query", Utilities.htmlEncode(query));
        hp.replace("scList", scList);
        hp.replace("listAll", listAll);
        hp.replace("baWideShortcut", (isUserAdmin == true)
                                     ? ""
                                     : "none");
        out.println(hp.parse(aBA.getSystemId()));

        return;
    }

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
            handleRequest(aRequest, aResponse);
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
        doGet(aRequest, aResponse);
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
    public void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException, FileNotFoundException {
        aResponse.setContentType("text/html");

        PrintWriter out      = aResponse.getWriter();
        HttpSession aSession = aRequest.getSession();
        User        user     = WebUtil.validateUser(aRequest);

        /*
         * Check the kind of action to be performed w.r.t shortcuts. The
         * following actions are defined:
         *      - DISPLAY SAVE DIALOG
         *      - LIST SHORTCUTS
         *      - SAVE A SHORTCUT
         */
        String        strRequest = readStringParam(aRequest, "request", "");
        SHRequestType reqType    = SHRequestType.LIST;

        try {
            reqType = SHRequestType.valueOf(strRequest.trim().toUpperCase());
        } catch (Exception e) {
            LOG.info("An exception has occurred while parsing the action." + "",(e));
            out.println("");

            return;
        }

        /*
         * Get the business are on which the action should be performed.
         */
        String       sysPrefix = readStringParam(aRequest, "sysPrefix", "");
        BusinessArea ba        = BusinessArea.lookupBySystemPrefix(sysPrefix);

        if (ba == null) {
            LOG.info("Invalid business area prefix: " + sysPrefix);
            out.println("");

            return;
        }

        int       systemId  = ba.getSystemId();
        SysConfig sysConfig = ba.getSysConfigObject();

        sysPrefix = ba.getSystemPrefix();

        int       userId     = user.getUserId();
        WebConfig userConfig = user.getWebConfigObject();
        BAConfig  baconfig   = userConfig.getBAConfig(sysPrefix);

        switch (reqType) {
        case LIST :
            out.println(renderShortcuts(aRequest, user, ba));

            return;

        case DISPLAY :
            displaySaveDialog(aRequest, aResponse, ba, user);

            break;

        case SAVE :
            saveShortcut(aRequest, aResponse, ba, user);

            break;

        case SET_DEFAULT :
        case UNSET_DEFAULT :
        case MARK_PRIVATE :
        case MARK_SHARED :
        case DELETE :
            operate(aRequest, aResponse, ba, user, reqType);

            break;
        }

        return;
    }

    /**
     * This method changes the default/private property of a shortcut based on
     * the request type.
     *
     * @param aRequest                  Request object.
     * @param aResponse                 Response object.
     * @param aBA                       Business Area.
     * @param aUser                     User.
     * @param aRequestType              Request Type
     * @throws ServletException
     * @throws IOException
     * @throws DatabaseException
     */
    private void operate(HttpServletRequest aRequest, HttpServletResponse aResponse, BusinessArea aBA, User aUser, SHRequestType aRequestType) throws ServletException, IOException, DatabaseException {
        aResponse.setContentType("text/html");

        PrintWriter out = aResponse.getWriter();

        /*
         * Read the BA and User properties.
         */
        int       systemId  = aBA.getSystemId();
        String    sysPrefix = aBA.getSystemPrefix();
        SysConfig sysConfig = aBA.getSysConfigObject();
        int       userId    = aUser.getUserId();
        String    userLogin = aUser.getUserLogin();
        WebConfig webConfig = aUser.getWebConfigObject();
        BAConfig  baConfig  = webConfig.getBAConfig(sysPrefix);

        /*
         * Get the name of the shortcut from the Request object. If it is not
         * found, return false and log this event.
         */
        String shortcutName = readStringParam(aRequest, "shortcut", "");

        if (shortcutName.equals("")) {
            LOG.info("The name of the shortcut is not specified.");
            out.println("false");

            return;
        }

        /*
         * Obtain the correpsonding shortcut object either from the user config
         * or from the BA config in that order.
         */
        boolean  isBAShortcut = false;
        Shortcut shortcut     = getShortcutByName(userLogin, sysPrefix, shortcutName);

        /*
         * Check if we could get a shortcut object corresponding to the given
         * name either from user_config or from sys_config. If not, log this
         * event and return.
         */
        if (shortcut == null) {
            LOG.info("Could not find shortcut: " + "[ User: " + aUser.getUserLogin() + ", " + " BA: " + sysPrefix + ", " + " Shortcut: " + shortcutName + " ]");
            out.println("false");

            return;
        }

        isBAShortcut = shortcut.getIsBAShortcut();

        /*
         * We could map the given name to a shortcut object. If this shortcut
         * is a BA shortcut then the user must be an admin to modify it.
         */
        if (isBAShortcut == true) {
            boolean isUserAdmin = isAdmin(systemId, userId);

            if (isUserAdmin == false) {
                out.println("false");

                return;
            }
        }

        /*
         * Modify the property of the shortcut based on the request type.
         */
        switch (aRequestType) {
        case MARK_PRIVATE :
            shortcut.setIsPublic(false);

            break;

        case MARK_SHARED :
            shortcut.setIsPublic(true);

            break;

        case SET_DEFAULT : {

            /*
             * Set the default property of current default shortcut to false
             * first.
             */
            Shortcut defShortcut = getShortcutByName(userLogin, sysPrefix, baConfig.getDefaultShortcutName());

            if (defShortcut != null) {
                defShortcut.setIsDefault(false);

                /*
                 * Update the corresponding config if the shortcut that is
                 * going to be set as default is not of the same type.
                 */
                if (defShortcut.getIsBAShortcut() == true) {
                    sysConfig.addShortcut(defShortcut);

                    if (shortcut.getIsBAShortcut() == false) {
                        aBA.setSysConfig(sysConfig.xmlSerialize());
                        BusinessArea.update(aBA);
                    } else {

                        /*
                         * The sysconfig will anyway be updated below.
                         */
                    }
                } else {
                    baConfig.addShortcut(defShortcut);

                    if (shortcut.getIsBAShortcut() == true) {
                        webConfig.setBAConfig(sysPrefix, baConfig);
                        aUser.setWebConfig(webConfig.xmlSerialize());
                        User.update(aUser);
                    } else {

                        /*
                         * The user config will anyway be updated below.
                         */
                    }
                }
            }

            shortcut.setIsDefault(true);
            baConfig.setDefaultShortcutName(shortcutName);
        }

        break;

        case UNSET_DEFAULT :
            shortcut.setIsDefault(false);
            baConfig.setDefaultShortcutName("");

            break;

        case DELETE :
            if (isBAShortcut == true) {
                deleteShortcut(aUser, aBA, shortcut);
            } else {
                deleteShortcut(aUser, sysPrefix, shortcut);
            }

            out.println(Messages.getMessage("SHORTCUT_DELETED"));

            return;
        }

        LOG.info("[ " + shortcutName + ", " + aRequestType + " ]");

        /*
         * Persist the changes to this shortcut permanently by updating the
         * record in the database.
         */
        if (isBAShortcut == true) {
            sysConfig.addShortcut(shortcut);

            String xml = sysConfig.xmlSerialize();

            aBA.setSysConfig(xml);
            BusinessArea.update(aBA);
            out.println("true");

            return;
        } else {
            baConfig.addShortcut(shortcut);
            webConfig.setBAConfig(sysPrefix, baConfig);

            String xml = webConfig.xmlSerialize();

            aUser.setWebConfig(xml);
            User.update(aUser);
            out.println("true");

            return;
        }
    }

    /**
     * This method reads a request parameter that holds a boolean value.
     *
     * @param aRequest          Http Request object.
     * @param aParamName        Name of the request parameter.
     * @param aDefaultValue     Default value.
     * @return Boolean value.
     */
    private static boolean readBooleanParam(HttpServletRequest aRequest, String aParamName, boolean aDefaultValue) {
        String strValue = aRequest.getParameter(aParamName);

        strValue = (strValue == null)
                   ? ""
                   : strValue.trim().toLowerCase();

        if (strValue.trim().equals("") == true) {
            return aDefaultValue;
        }

        if (strValue.equals("false") || strValue.equals("0")) {
            return false;
        }

        if (strValue.equals("true") || strValue.equals("1")) {
            return true;
        }

        return aDefaultValue;
    }

    /**
     * This method reads a request parameter that holds an integer value.
     *
     * @param aRequest          Http Request object.
     * @param aParamName        Name of the request parameter.
     * @param aDefaultValue     Default value.
     * @return integer value.
     */
    private static int readIntegerParam(HttpServletRequest aRequest, String aParamName, int aDefaultValue) {
        String strValue = aRequest.getParameter(aParamName);

        if (strValue == null) {
            return aDefaultValue;
        }

        try {
            return Integer.parseInt(strValue);
        } catch (NumberFormatException nfe) {
            LOG.info("Exception while parsing an integer in string: " + strValue + "\n" + "",(nfe));
        }

        return aDefaultValue;
    }

    /**
     * This method reads a request parameter that holds a string value.
     *
     * @param aRequest          Http Request object.
     * @param aParamName        Name of the request parameter.
     * @param aDefaultValue     Default value.
     * @return String value.
     */
    private static String readStringParam(HttpServletRequest aRequest, String aParamName, String aDefaultValue) {
        String strValue = aRequest.getParameter(aParamName);

        if (strValue == null) {
            return aDefaultValue;
        }

        return strValue.trim();
    }

    /**
     * This method returns the HTML used to render BA Shortcuts.
     * @param aRequest TODO
     * @param aList             Table of BA Shortcuts.
     * @param aAdmin            Flag to specify if the user is an Admin.
     *
     * @return  HTML to render the BA shortcuts.
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static String renderBAShortcuts(HttpServletRequest aRequest, ArrayList<Shortcut> aList, boolean aAdmin, String aPrefix, String aDefaultValue) throws FileNotFoundException, IOException {
        StringBuilder buffer = new StringBuilder();

        if (aList == null) {
            return buffer.toString();
        }

        int listSize = aList.size();

        if (listSize == 0) {
            return buffer.toString();
        }

        String nearestPath = WebUtil.getNearestPath(aRequest, "");
        int    counter     = 0;

        for (Shortcut sc : aList) {
            counter = counter + 1;

            String       id        = "ba_shortcut_" + counter;
            String       name      = sc.getName();
            String       value     = "BA:" + name;
            String       encName   = Utilities.htmlEncode(name);
            String       encValue  = Utilities.htmlEncode(value);
            String       className = (value.equals(aDefaultValue))
                                     ? "b"
                                     : "";
            DTagReplacer hpRecord  = null;

            if (aAdmin == true) {
                hpRecord = new DTagReplacer(SH_ADMIN_RECORD);
            } else {
                hpRecord = new DTagReplacer(SH_USER_RECORD);
            }

            hpRecord.replace("id", id);
            hpRecord.replace("className", className);
            hpRecord.replace("encValue", encValue);
            hpRecord.replace("name", name);
            hpRecord.replace("nearestPath", nearestPath);
            hpRecord.replace("encName", encName);
            buffer.append(hpRecord.parse(0));
        }

        DTagReplacer hp = null;

        if (aAdmin == true) {
            hp = new DTagReplacer(SH_ADMIN);
        } else {
            hp = new DTagReplacer(SH_USER);
        }

        hp.replace("shortcutList", buffer.toString());
        hp.replace("sysPrefix", aPrefix);
        hp.replace("baShortcutCount", Integer.toString(listSize));

        return hp.parse(0);
    }

    /**
     * This method returns the HTML related to displaying the list of shortcuts
     * the user has.
     * @param aRequest TODO
     * @param scTable  Table of shortcut names and shortcut objects.
     *
     * @return HTML Table for displaying the shortcuts.
     */
    public static String renderShortcuts(HttpServletRequest aRequest, User user, BusinessArea ba) throws FileNotFoundException, IOException, DatabaseException {

        /*
         * Get the business area details.
         */
        int       systemId  = ba.getSystemId();
        String    sysPrefix = ba.getSystemPrefix();
        SysConfig sysConfig = ba.getSysConfigObject();

        /*
         * Get the user details.
         */
        int       userId    = user.getUserId();
        String    userLogin = user.getUserLogin();
        WebConfig webConfig = user.getWebConfigObject();
        BAConfig  baConfig  = webConfig.getBAConfig(sysPrefix);

        /*
         * Get the details of the default shortcut if any.
         */
        String   defaultShortcutName     = baConfig.getDefaultShortcutName();
        Shortcut defaultShortcut         = getShortcutByName(userLogin, sysPrefix, defaultShortcutName);
        boolean  isDefault_A_BA_Shortcut = false;

        if (null != defaultShortcut) {
            isDefault_A_BA_Shortcut = defaultShortcut.getIsBAShortcut();
        }

        /*
         * 1. Get the user shortcuts
         * 2. Sort them by name.
         * 3. Prepare the HTML to render them in a table.
         * 4. Prepare the HTML to render them as option in a select box under
         *    a different option group.
         */
        Hashtable<String, Shortcut> usTable = baConfig.getShortcuts();
        ArrayList<Shortcut>         usList  = null;

        if ((usTable != null) && (usTable.size() > 0)) {
            usList = new ArrayList<Shortcut>(usTable.values());
            usList = Shortcut.sort(usList);
        }

        String userShortcutsHTML   = renderUserShortcuts(aRequest, usList);
        String userShortcutOptions = shortcutOptions(usList, defaultShortcutName, "My Searches:", "");
        String baShortcutsHTML     = "";
        String baShortcutOptions   = "";

        /*
         * 1. Get the BA shortcuts.
         * 2. Sort them by name.
         * 3. Check if the list is not empty and the user is a BAUser.
         * 4. Check if the user is an admin.
         * 5. Prepare the HTML to render the shortcuts in a table.
         * 6. Prepare the HTML to render the shortcuts as options in select box
         *    under a different option group.
         */
        Hashtable<String, Shortcut> bsTable = sysConfig.getShortcuts();
        ArrayList<Shortcut>         bsList  = null;

        if ((bsTable != null) && (bsTable.size() > 0)) {
            bsList = new ArrayList<Shortcut>(bsTable.values());
            bsList = Shortcut.sort(bsList);
        }

        if ((bsList != null) && (bsList.size() != 0) && (BAUser.isBAUser(systemId, userId) == true)) {

            /*
             * Since this user is a BAUser, check if he is an admin in this BA.
             * If so, he is privileged to delete these shortcuts. Otherwise not.
             */
            boolean isUserAdmin = isAdmin(systemId, userId);

            baShortcutsHTML = renderBAShortcuts(aRequest, bsList, isUserAdmin, sysPrefix, defaultShortcutName);

            String label = sysPrefix + "# BA Searches:";

            baShortcutOptions = shortcutOptions(bsList, defaultShortcutName, label, "BA:");
        }

        String encDefShortcut = Utilities.htmlEncode(defaultShortcutName);

        /*
         * The option NONE that needs to be displayed in the select box.
         */
        String noneOption = "";

        if (defaultShortcut != null) {
            noneOption = "\n\t<OPTION class='list-item-default' " + "value=\"" + encDefShortcut + "\">None</OPTION>";
        } else {
            noneOption = "\n\t<OPTION class='list-item-default' SELECTED " + "value=\"" + encDefShortcut + "\">None</OPTION>";
        }

        DTagReplacer hpSelect = new DTagReplacer(SH_SELECT_DEFAULT);

        hpSelect.replace("noneOption", noneOption);
        hpSelect.replace("userOptionList", userShortcutOptions);
        hpSelect.replace("baOptionList", baShortcutOptions);

        DTagReplacer hp = new DTagReplacer(SH_CONSOLIDATED);

        hp.replace("userShortcuts", userShortcutsHTML);
        hp.replace("baShortcuts", baShortcutsHTML);
        hp.replace("sysPrefix", sysPrefix);
        hp.replace("scList", hpSelect.parse(ba.getSystemId()));

        return hp.parse(ba.getSystemId());
    }

    /**
     * This method renders the user shortcuts.
     * @param aRequest TODO
     * @param aList     Table of user shortcuts.
     *
     * @return  HTML that represents the user shortcuts.
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static String renderUserShortcuts(HttpServletRequest aRequest, ArrayList<Shortcut> aList) throws FileNotFoundException, IOException {
        String noShortcuts = "<span>No saved searches.</span>";

        if (aList == null) {
            return noShortcuts;
        }

        int listSize = aList.size();

        if (listSize == 0) {
            return noShortcuts;
        }

        /*
         * Cases to be tested when this method is changed.
         */
        StringBuilder header             = new StringBuilder();
        StringBuilder buffer             = new StringBuilder();
        DTagReplacer  hp                 = new DTagReplacer(SH_DISP_FILE);
        int           counter            = 0;
        boolean       hasDefault         = false;
        String        encDefShortcutName = "";
        String        nearestPath        = WebUtil.getNearestPath(aRequest, "");

        for (Shortcut sc : aList) {

            // Skip the my-requests shortcut.
            if (sc.getName().equalsIgnoreCase("my-requests") == true) {
                continue;
            }

            counter++;

            String  trId            = "shortcut_" + counter;
            String  className       = "";
            boolean isPublic        = sc.getIsPublic();
            boolean isDefault       = sc.getIsDefault();
            String  name            = sc.getName();
            String  encName         = Utilities.htmlEncode(sc.getName());
            String  isPublicChecked = "";

            if (isDefault == true) {
                className = "title b";
            } else {
                className = "title";
            }

            if (isPublic == true) {
                isPublicChecked = " CHECKED ";
            }

            DTagReplacer hpRecord = new DTagReplacer(SH_RECORD);

            hpRecord.replace("id", trId);
            hpRecord.replace("className", className);
            hpRecord.replace("encName", encName);
            hpRecord.replace("name", name);
            hpRecord.replace("chboxId", "chbox_" + counter);
            hpRecord.replace("isPublic", Boolean.toString(isPublic));
            hpRecord.replace("isDefault", Boolean.toString(isDefault));
            hpRecord.replace("isPublicChecked", isPublicChecked);
            hpRecord.replace("nearestPath", nearestPath);
            buffer.append(hpRecord.parse(0));
            hpRecord = null;
        }

        DTagReplacer hpHeader = new DTagReplacer(SH_LIST_HEADER);

        header.append(hpHeader.parse(0));
        hp.replace("header", header.toString());
        hp.replace("shortcutList", buffer.toString());
        hp.replace("userShortcutCount", Integer.toString(listSize));

        return hp.parse(0);
    }

    /**
     * This method saves the shortcut permanently in the user/ba config.
     *
     * @param aRequest           Http Request object.
     * @param aResponse          Http Response object.
     * @param aBA                Business Area.
     * @param aUser              User
     * @throws ServletException
     * @throws IOException
     */
    private void saveShortcut(HttpServletRequest aRequest, HttpServletResponse aResponse, BusinessArea aBA, User aUser) throws ServletException, IOException, DatabaseException {
        aResponse.setContentType("text/html");

        PrintWriter out           = aResponse.getWriter();
        int         userId        = aUser.getUserId();
        int         systemId      = aBA.getSystemId();
        String      sysPrefix     = aBA.getSystemPrefix();
        SysConfig   sysConfig     = aBA.getSysConfigObject();
        String      scName        = readStringParam(aRequest, "scName", "");
        String      query         = readStringParam(aRequest, "query", "");
        String      desc          = readStringParam(aRequest, "description", "");
        String      filter        = readStringParam(aRequest, "filter", "subject");
        int         view          = readIntegerParam(aRequest, "view", NORMAL_VIEW);
        boolean     bDefault      = readBooleanParam(aRequest, "isDefault", false);
        boolean     bPublic       = readBooleanParam(aRequest, "isPublic", false);
        boolean     bListAll      = readBooleanParam(aRequest, "listAll", false);
        boolean     bIsBAShortcut = readBooleanParam(aRequest, "isBAWide", false);

        LOG.info("IS BA Shortcut: " + bIsBAShortcut);

        /*
         * Only a business area admin can set the shortcut to be available for
         * entire business area.
         */
        if (bIsBAShortcut == true) {

            /*
             * Check if the user is an admin in this BA. If not, we cannot
             * mark this shortcut as BA wide available one.
             */
            boolean isUserAdmin = isAdmin(systemId, userId);

            if (isUserAdmin == false) {
                bIsBAShortcut = false;
            }
        }

        try {
            boolean flag = Shortcut.saveShortcut(aUser, aBA, sysPrefix, scName, query, desc, filter, view, bDefault, bPublic, bListAll, bIsBAShortcut);

            out.println(Boolean.toString(flag));
        } catch (Exception e) {
            String message = e.toString();

            out.println("false");
        }

        return;
    }

    /**
     * This method renders the shortcuts as select options.
     *
     * @param sList             List of shortcuts.
     * @param sValue            Value to be selected.
     * @param grpName           Option group name.
     * @param prefix            Prefix to be added to the shortcut names.
     * @return
     */
    private static String shortcutOptions(ArrayList<Shortcut> sList, String sValue, String grpName, String prefix) {
        StringBuilder buffer = new StringBuilder();

        if ((sList == null) || (sList.size() == 0)) {
            return "";
        }

        buffer.append("\n\t<OPTGROUP LABEL=\"").append(grpName).append("\">");

        for (Shortcut sc : sList) {
            String text     = sc.getName().trim();
            String value    = prefix + text;
            String encText  = Utilities.htmlEncode(text);
            String encValue = Utilities.htmlEncode(value);

            buffer.append("\n\t\t<OPTION value='").append(encValue).append("'").append((value.equals(sValue) == true)
                    ? " SELECTED "
                    : "").append(">").append(encText).append("</OPTION>");
        }

        buffer.append("\n\t</OPTGROUP>");

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method tries to resolve the shortcut name in the following manner:
     *  - Looks up in the user config
     *  - Interpret the shortcut name as USER:Shortcut and resolve from the
     *    corresponding user's configuration
     *  - Interpret the shortcut name as BA:Shortcut and resolve from the
     *    current BA's configuration.
     *  - Finally check the shortcutName as in the BA's configuration.
     *
     *
     * @param aUserLogin
     * @param aPrefix
     * @param aShortcutName
     * @return
     * @throws DatabaseException
     */
    public static Shortcut getShortcutByName(String aUserLogin, String aPrefix, String aShortcutName) throws DatabaseException {
        Shortcut shortcut = null;

        /*
         * First check if the shortcut is present in the user's configuration
         * for this business area.
         */
        User      user = User.lookupByUserLogin(aUserLogin);
        WebConfig wc   = user.getWebConfigObject();
        BAConfig  bc   = wc.getBAConfig(aPrefix);

        shortcut = bc.getShortcut(aShortcutName);

        if (shortcut != null) {
            return shortcut;
        }

        /*
         * Check if the shortcut name is of type <USER>/<BA>:<ShortcutName>
         */
        if (aShortcutName.indexOf(":") > 0) {
            String[] tmp   = aShortcutName.split(":");
            String   sPref = tmp[0].trim();
            String   sName = tmp[1].trim();

            if (sPref.equalsIgnoreCase("BA")) {
                BusinessArea ba = BusinessArea.lookupBySystemPrefix(aPrefix);
                SysConfig    sc = ba.getSysConfigObject();

                shortcut = sc.getShortcut(sName);
            } else {

                /*
                 * Check if we are dealing with the same user.
                 */
                if (sPref.equals(aUserLogin) == true) {
                    shortcut = bc.getShortcut(sName);
                } else {
                    User      tUser = User.lookupByUserLogin(sPref);
                    WebConfig tWc   = tUser.getWebConfigObject();
                    BAConfig  tBc   = tWc.getBAConfig(aPrefix);

                    shortcut = tBc.getShortcut(sName);
                }
            }
        } else {

            /*
             * Check if this is a BA shortcut.
             */
            BusinessArea ba = BusinessArea.lookupBySystemPrefix(aPrefix);
            SysConfig    sc = ba.getSysConfigObject();

            shortcut = sc.getShortcut(aShortcutName);
        }

        return shortcut;
    }

    /**
     * This method returns the the list of shortcuts user.
     *
     * @param scTable
     *            Table of shortcut names and shortcut objects.
     *
     * @return List of shortcuts.
     */
    public static String getShortcutJSON(Hashtable<String, Shortcut> uList, Hashtable<String, Shortcut> bList) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\n\t\tvar scList = new Object();");

        ArrayList<Shortcut> list = new ArrayList<Shortcut>(uList.values());

        for (Shortcut sc : list) {
            String name = sc.getName().toUpperCase();

            buffer.append("\n\t\tscList[\"" + name + "\"] = \"true\";");
        }

        list = new ArrayList<Shortcut>(bList.values());
        buffer.append("\n\t\tvar baShortcutList = new Object();");

        for (Shortcut sc : list) {
            String name = sc.getName().toUpperCase();

            buffer.append("\n\t\tbaShortcutList[\"" + name + "\"] = \"true\";");
        }

        return buffer.toString();
    }

    /**
     * This method checks if the user is an admin in specified BA.
     *
     * @param systemId          BA ID
     * @param userId            User ID
     * @return True if user is an admin
     * @throws DatabaseException
     */
    private static boolean isAdmin(int systemId, int userId) throws DatabaseException {
        Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(systemId, userId);
        Integer                    temp      = null;

        // Check if the user has change permission on Business Area.
        temp = permTable.get(Field.BUSINESS_AREA);

        if ((temp != null) && (temp & Permission.CHANGE) != 0) {
            return true;
        }

        // Check for the admin tag in the permission table.
        temp = permTable.get("__ADMIN__");

        if ((temp != null) && (temp != 0)) {
            return true;
        }

        // Check for the permission admin tag.
        temp = permTable.get("__PERMISSIONADMIN__");

        if ((temp != null) && (temp != 0)) {
            return true;
        }

        return false;
    }
}
