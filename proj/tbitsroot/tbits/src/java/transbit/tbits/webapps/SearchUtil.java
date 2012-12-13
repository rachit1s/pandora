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
 * SearchUtil.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.ActionHelper;
import transbit.tbits.Helper.DateTimeParser;
import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.SysPrefixes;
import transbit.tbits.Helper.TBitsPropEnum;

//TBits Imports.
import transbit.tbits.api.Mapper;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.SysConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FieldDescriptor;
import transbit.tbits.domain.HolidaysList;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.TypeDescriptor;
import transbit.tbits.domain.TypeUser;
import transbit.tbits.domain.User;

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

//Static Imports.
import static transbit.tbits.api.Mapper.ourFieldDescListMap;
import static transbit.tbits.api.Mapper.ourTypeDescListMap;

//~--- JDK imports ------------------------------------------------------------

//Java Imports
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//~--- classes ----------------------------------------------------------------

/**
 * This is an utility servlet that works for Search Page in TBits.
 * Some of the functions it provides are
 * <ul>
 *   <li>Validating a list of given userlogins.
 *   <li>Validating a prefixedId.
 *   <li>Returning a list of users whose userlogin starts with a given prefix.
 *   <li>Returning a list of category users whose userlogin starts
 *       with a given prefix.
 *   <li>Delete a requested Saved-Search Criteria.
 * </ul>
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public class SearchUtil extends HttpServlet {

    // The Logger.
    public static final TBitsLogger LOG                   = TBitsLogger.getLogger(PKG_WEBAPPS);
    private static final String     LOADING_FILE          = "web/tbits-loading.htm";
    private static final String     EMAIL_HELP_FILE       = "web/tbits-notification-help.htm";
    private static final String     DELIM                 = "\n";
    private static final String     ACTION_DIFF_FILE      = "web/tbits-action-diff.htm";
    private static final String     SUMMARY_DIFF_FILE     = "web/tbits-summary-diff.htm";
    private static final String     DESCRIPTORS_LIST_FILE = "web/tbits-descriptors-list.htm";

    //~--- methods ------------------------------------------------------------

    /**
     * This method services the Http-GET Requests for this Servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {

    	HttpSession session = aRequest.getSession() ;
    	PrintWriter out = aResponse.getWriter();

        aResponse.setContentType("text/plain");

        String userInfo = aRequest.getParameter("userInfo");

        if (userInfo != null) {
            out.println(Mapper.ourUserInfo.toString());

            return;
        }

        User user = null;

        try {
            user = WebUtil.validateUser(aRequest);
        } catch (Exception e) {
            LOG.warn("Exception while validating the user: " + "",(e));
            session.setAttribute(TBitsError.EXCEPTION_OBJECT, e) ;
            aResponse.sendRedirect(WebUtil.getNearestPath(aRequest,"/error")) ;
            return;
        }

        /*
         * Check if this is a call to get the loading file during searching.
         */
        String aLoading = aRequest.getParameter("loading");

        if (aLoading != null) {
            try {
                aResponse.setContentType("text/html");

                String strWord = aRequest.getParameter("word");

                if ((strWord == null) || strWord.trim().equals("")) {
                    strWord = "Searching";
                }

                DTagReplacer hp = new DTagReplacer(LOADING_FILE);

                hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
                hp.replace("word", strWord);
                out.println(hp.parse(0));
            } catch (Exception e) {
                LOG.severe("",(e));
            }

            return;
        }

        /*
         * Check if the request is to show the action diff dialog.
         */
        String actionDiff = aRequest.getParameter("actionDiff");

        if (actionDiff != null) {
            try {
                String sysPrefix = aRequest.getParameter("sysPrefix");

                sysPrefix = ((sysPrefix == null) || sysPrefix.trim().equals(""))
                            ? user.getWebConfigObject().getSystemPrefix()
                            : sysPrefix.trim();

                BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

                sysPrefix = ba.getSystemPrefix();

                SysConfig sc = ba.getSysConfigObject();

                aResponse.setContentType("text/html");

                DTagReplacer hp = new DTagReplacer(ACTION_DIFF_FILE);

                hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
                hp.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
                out.println(hp.parse(ba.getSystemId()));

                return;
            } catch (Exception e) {
                LOG.severe("",(e));
            }

            return;
        }

        /*
         * Check if this is a call to generate summary diff.
         */
        String summaryDiff = aRequest.getParameter("summaryDiff");

        if (summaryDiff != null) {
            try {
                String sysPrefix = aRequest.getParameter("sysPrefix");

                sysPrefix = ((sysPrefix == null) || sysPrefix.trim().equals(""))
                            ? user.getWebConfigObject().getSystemPrefix()
                            : sysPrefix.trim();

                BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

                sysPrefix = ba.getSystemPrefix();

                SysConfig sc = ba.getSysConfigObject();

                aResponse.setContentType("text/html");

                DTagReplacer hp = new DTagReplacer(SUMMARY_DIFF_FILE);

                hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
                hp.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
                out.println(hp.parse(ba.getSystemId()));

                return;
            } catch (Exception e) {
                LOG.severe("",(e));
            }

            return;
        }

        /*
         * Check if this is a call to get the subject of a request.
         */
        String aSubject = aRequest.getParameter("subject");

        if ((aSubject != null) && aSubject.equalsIgnoreCase("true")) {
            try {
                aResponse.setContentType("text/html");

                String sysPrefix = aRequest.getParameter("sysPrefix");

                sysPrefix = (sysPrefix == null)
                            ? ""
                            : sysPrefix.trim();

                BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

                if (ba == null) {
                    return;
                }

                int    requestId = Integer.parseInt(aRequest.getParameter("requestId"));
                String subject   = Request.lookupSubject(sysPrefix, requestId, user.getUserId(), false);

                if (subject != null) {
                    out.println(subject);
                }
            } catch (Exception e) {
                LOG.severe("",(e));
            }

            return;
        }

        /*
         * check if this is a call to get formatted list of holidays.
         */
        String holidayList = aRequest.getParameter("holidays");

        if (holidayList != null) {
            aResponse.setContentType("text/html");
            out.println(getFormattedHolidayList());

            return;
        }

        /*
         * Check if this is a request to validate a given set of users.
         */
        // USE json instead of separators, separators may confuse this algo.. 
        String validateUsers = aRequest.getParameter("validateUsersList");

        if (validateUsers != null) {
            StringBuilder sb             = new StringBuilder();
            StringBuilder usrList        = new StringBuilder();
            String        usersList      = aRequest.getParameter("usersList");
            String[]      usersListSplit = usersList.split("(;|,)");
            int           len            = usersListSplit.length;
            int           count          = 0;

            user = null;

            for (int i = 0; i < len; i++) {
                String[] currentUserSplit = usersListSplit[i].split(":");
                String   currentUser      = currentUserSplit[0].trim();
                String   userType         = currentUserSplit[1].trim();

                if (userType.equalsIgnoreCase("dbUser")) {
                    if (!currentUser.equals("") &&!currentUser.trim().equals("auto")) {
                        try {

                            // String userLogin = usersListSplit[i].trim();
                            if (currentUser.startsWith("*")) {
                                currentUser = currentUser.substring(1);
                            }

                            user = User.lookupAllByUserLogin(currentUser);
                        } catch (DatabaseException dbe) {
                        	LOG.error(dbe);
                        }

                        if (user == null) {
                            count++;
                            usrList.append(currentUser + ";");
                        }
                    }
                } else if (userType.equalsIgnoreCase("extUser")) {
//                    String regex = "^[a-zA-Z][.-]*[a-zA-Z0-9]*@[a-zA-Z0-9]" 
//                    	+ "[.-]*[a-zA-Z0-9]*\\.[a-zA-Z]+$";
                	String regex = "^.+@.+$";
                	
                    if (!currentUser.equals("") &&!currentUser.matches(regex)) {
                        try {

                            // String userLogin = usersListSplit[i].trim();
                            if (currentUser.startsWith("*")) {
                                currentUser = currentUser.substring(1);
                            }

                            user = User.lookupAllByUserLogin(currentUser);
                        } catch (DatabaseException dbe) {
                        	LOG.error(dbe);
                        }
                        if (user == null) {
                            count++;
                            usrList.append(currentUser + ";");
                        }
                    }
                }
            }

            if (count > 1) {
                sb.append(" " + Messages.getMessage("INVALID_TRANSBIT_USERS", usrList.toString()));
            } else if (count == 1) {
                sb.append(" " + Messages.getMessage("INVALID_TRANSBIT_USER", usrList.toString()));
            }

            String dueDate = aRequest.getParameter("dueDate");

            if (dueDate != null) {
                Hashtable<String, Date> dueDateHash = DateTimeParser.parse(dueDate);

                if (dueDateHash == null) {
                    sb.append("$_D_E_L_I_M_$").append(Messages.getMessage("INVALID_DUE_DATE_FORMAT"));
                }
            }

            out.println(sb.toString());

            return;
        }

        /*
         * Check if this is a call to get the assignee information for
         * the given business area and category id.
         */
        String aInfo = aRequest.getParameter("assigneeInfo");

        if (aInfo != null) {
            StringBuilder assigneeInfo = new StringBuilder();

            try {
                int          sysId       = Integer.parseInt(aRequest.getParameter("systemId"));
                BusinessArea ba          = BusinessArea.lookupBySystemId(sysId);
                SysConfig    sc          = ba.getSysConfigObject();
                boolean      assignToAll = sc.getAssignToAll();

                if (assignToAll == true) {
                    out.println("[" + Mapper.ourUserInfo.toString() + "]");

                    return;
                }

                String              category = aRequest.getParameter("categoryId");
                Type                type     = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, Field.CATEGORY, category);
                ArrayList<TypeUser> list     = TypeUser.lookupBySystemIdAndFieldNameAndTypeId(sysId, Field.CATEGORY, type.getTypeId());

                if (list != null) {
                    assigneeInfo.append("[");

                    boolean first = true;

                    for (TypeUser tu : list) {
                        if (tu.getUserTypeId() != 3) {
                            continue;
                        }

                        User value = tu.getUser();

                        if (first == false) {
                            assigneeInfo.append(",\n");
                        } else {
                            first = false;
                        }

                        assigneeInfo.append(getUserInfoRecord(value));
                    }

                    assigneeInfo.append("]");
                } else {
                    assigneeInfo.append("[]");
                }
            } catch (Exception e) {
                LOG.warn("",(e));
            }

            out.println(assigneeInfo.toString());

            return;
        }

        /*
         * This call is about validating a prefixed id and return the
         * corresponding <SysId><RequestId><SysPrefix>
         */
        String strPrefixId = aRequest.getParameter("prefixId");

        if ((strPrefixId != null) && (strPrefixId.trim().equals("") == false)) {
            try {
                LOG.info(strPrefixId);

                StringTokenizer st       = new StringTokenizer(strPrefixId, "#");
                String          prefix   = st.nextToken().trim();
                String          strReqId = st.nextToken().trim();
                StringTokenizer st1      = new StringTokenizer(strReqId, ",;-_");
                int             id       = 0;

                try {
                    id = Integer.parseInt(st1.nextToken().trim());
                } catch (Exception e) {
                    out.println("no_requestId");
                    LOG.debug("no_requestId");

                    return;
                }

                //
                // look up in Mapper for the Business Area corresponding to
                // the prefix we received.
                //
                BusinessArea ba = BusinessArea.lookupBySystemPrefix(prefix);

                if (ba != null) {

                    //
                    // We found a BA, now check if the request id is in the
                    // valid range for this business area.
                    //
                    if (id <= ba.getMaxRequestId()) {
                        String output = ba.getSystemId() + DELIM + id + DELIM + ba.getSystemPrefix();

                        out.println(output);
                        LOG.debug(output);

                        return;
                    } else {
                        Mapper.refreshBAMap();
                        ba = BusinessArea.lookupBySystemPrefix(prefix);

                        if (ba != null) {
                            if (id <= ba.getMaxRequestId()) {
                                String output = ba.getSystemId() + DELIM + id + DELIM + ba.getSystemPrefix();

                                out.println(output);
                                LOG.debug(output);

                                return;
                            }
                        } else {
                            out.println("no_ba");
                            LOG.debug("no_ba");

                            return;
                        }
                    }

                    out.println("no_requestId");
                    LOG.debug("no_requestId");

                    return;
                } else {
                    if (SysPrefixes.isValid(prefix) == false) {
                        out.println("no_ba");
                        LOG.debug("no_ba");

                        return;
                    } else {
                        String output = "0" + DELIM + id + DELIM + prefix;

                        out.println(output);
                        LOG.debug(output);

                        return;
                    }
                }
            } catch (Exception e) {
                LOG.info("",(e));

                return;
            }
        }

        /*
         * Check if this is a call to get the notification options page.
         */
        String notification = aRequest.getParameter("notification");

        if (notification != null) {
            try {
                String sysPrefix = aRequest.getParameter("sysPrefix");

                sysPrefix = ((sysPrefix == null) || sysPrefix.trim().equals(""))
                            ? user.getWebConfigObject().getSystemPrefix()
                            : sysPrefix.trim();

                BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

                sysPrefix = ba.getSystemPrefix();

                SysConfig sc = ba.getSysConfigObject();

                aResponse.setContentType("text/html");

                DTagReplacer hp = new DTagReplacer(EMAIL_HELP_FILE);

                hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
                hp.replace("holidaysList", getFormattedHolidayList());
                hp.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
                out.println(hp.parse(ba.getSystemId()));
            } catch (Exception e) {
                LOG.severe("",(e));
            }

            return;
        }

        /*
         * Check if this call is to get cross site BAs list accesible to user
         */
        String baList     = aRequest.getParameter("baList");
        String actionPage = aRequest.getParameter("actionPage");

        if (baList != null) {

            /*
             * If actionPage is null, default it to search.
             */
            if ((actionPage == null) || (actionPage.trim().equals("") == true)) {
                actionPage = "search";
            }

            aResponse.setContentType("text/html");

            try {
                out.println(WebUtil.getBAsListHtml(user.getUserId(), actionPage));

                return;
            } catch (Exception e) {
                LOG.severe("",(e));
            }

            return;
        }

        /*
         * Check if this is a call to get the field descriptors legend.
         */
        String descriptorsHelp = aRequest.getParameter("descriptorsHelp");

        if (descriptorsHelp != null) {
            String sysPrefix = aRequest.getParameter("sysPrefix");

            if (sysPrefix == null) {
                sysPrefix = "";
            }

            try {
                aResponse.setContentType("text/html");

                Hashtable<String, String> tagTable = new Hashtable<String, String>();
                DTagReplacer              hp       = new DTagReplacer(DESCRIPTORS_LIST_FILE);
                String                    path     = WebUtil.getServletPath(aRequest, "");

                WebUtil.setInstanceBold(tagTable, sysPrefix);

                String hydPath = tagTable.get("instancePathHyd");
                String nycPath = tagTable.get("instancePathNyc");

                if (hydPath.startsWith("/search")) {

                    // PATH ends with "/". So, remove the "/" in hydPath.
                    tagTable.put("instancePathHyd", path + hydPath.substring(1));
                } else if (nycPath.startsWith("/search")) {

                    // PATH ends with "/". So, remove the "/" in nycPath.
                    tagTable.put("instancePathNyc", path + nycPath.substring(1));
                }

                tagTable.put("nearestPath", WebUtil.getNearestPath(aRequest, ""));
                tagTable.put("cssFile", WebUtil.getCSSFile(null, "", false));
                tagTable.put("sysPrefix", sysPrefix);
                tagTable.put("path", path);
                tagTable.put("standarddescriptors", getFieldDescriptorsListHtml(sysPrefix, false));
                tagTable.put("extendeddescriptors", getFieldDescriptorsListHtml(sysPrefix, true));
                tagTable.put("typeValueDivs", getTypeValueDivs(sysPrefix));
                ActionHelper.replaceTags(hp, tagTable, null);
                out.println(hp.parse(BusinessArea.lookupBySystemPrefix(sysPrefix).getSystemId()));
            } catch (Exception e) {
                LOG.severe("",(e));
            }

            return;
        }

        /*
         * Check if this is a request to get the BA details.
         */
        String baHelp = aRequest.getParameter("baHelp");

        if (baHelp != null) {
            StringBuffer data = new StringBuffer();

            /*
             * This includes
             *    1. Display Name.
             *    2. Prefix
             *    3. Email Address list
             *    4. Administrators.
             * as of now.
             */
            String sysPrefix = aRequest.getParameter("sysPrefix");

            if ((sysPrefix == null) || (sysPrefix.trim().equals("") == true)) {
                out.println(data.toString());

                return;
            }

            // Get the corresponding business area record.
            BusinessArea ba = null;

            try {
                ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
            } catch (DatabaseException de) {
                LOG.info(de.toString());
                out.println(data.toString());

                return;
            }

            if (ba == null) {
                LOG.info("The business area you are trying to access " + "does not exist: " + sysPrefix);
                out.println(data.toString());

                return;
            }

            int systemId = ba.getSystemId();

            sysPrefix = ba.getSystemPrefix();

            ArrayList<String> emailList = ba.getEmailList();

            // Get the list of Administrators for this BA.
            ArrayList<RoleUser> adminList = null;

            try {
                adminList = RoleUser.lookupBySystemIdAndRoleId(systemId, RoleUser.ADMIN);
            } catch (DatabaseException de) {
                LOG.info(de.toString());
            }

            /*
             * Starting building the HTML.
             */
            data.append("\n<table cellspacing='0' cellpadding='0' width='100%'").append("class='shortcuts' name='baDet' ").append("onmouseover='notInToolTip=false;' ").append("onmouseout=").append(
                "'closeTarget(event, \"TABLE\", \"baDet\");'").append(">").append("\n\t<tbody>").append("\n\t<tr>").append("\n\t\t<td class='ncol b' valign='top' nowrap ").append(
                "onmouseout=").append("'closeTarget(event, \"TABLE\", \"baDet\");'").append(">").append("Prefix</td>").append("\n\t\t<td class='ncol' valign='top' nowrap ").append(
                "onmouseout=").append("'closeTarget(event, \"TABLE\", \"baDet\");'").append(" >").append(ba.getSystemPrefix()).append("</td>").append("\n\t</tr>").append("\n\t<tr>").append(
                "\n\t\t<td class='ncol b' valign='top' nowrap ").append("onmouseout=").append("'closeTarget(event, \"TABLE\", \"baDet\");'").append(">").append("Email</td>").append(
                "\n\t\t<td class='ncol' valign='top' nowrap ").append("onmouseout=").append("'closeTarget(event, \"TABLE\", \"baDet\");'").append(">").append(htmlFormatEmails(emailList)).append(
                "</td>").append("\n\t</tr>").append("\n\t\t<td class='ncol b' valign='top' nowrap ").append("onmouseout=").append("'closeTarget(event, \"TABLE\", \"baDet\");'").append(">").append(
                "Administrator(s)</td>").append("\n\t\t<td class='ncol' valign='top' nowrap>").append(htmlFormatAdmins(adminList)).append("</td>").append("\n\t</tr>").append("\n\t<tbody>").append(
                "\n</table>").append("\n");
            aResponse.setContentType("text/html");
            out.println(data.toString());
            return;
        }
    }

    private static String htmlFormatAdmins(ArrayList<RoleUser> list) {
        StringBuffer buffer = new StringBuffer();

        if (list == null) {
            User user = null;

            try {
                user = User.lookupAllByEmail("tbits-dev@hyd");
                buffer.append(user.getDisplayName()).append(" (<a href='mailto:").append(user.getEmail()).append("'>").append(user.getEmail()).append("</a>").append(")<br>");
            } catch (Exception e) {}

            return buffer.toString();
        }

        for (RoleUser ru : list) {
            User user = null;

            try {
                user = User.lookupAllByUserId(ru.getUserId());
            } catch (Exception e) {
                continue;
            }

            buffer.append(user.getDisplayName()).append(" (<a href='mailto:").append(user.getEmail()).append("'>").append(user.getUserLogin()).append("</a>)<br>");
        }

        return buffer.toString();
    }

    private static String htmlFormatEmails(ArrayList<String> list) {
        StringBuffer buffer = new StringBuffer();

        if (list == null) {
            return buffer.toString();
        }

        for (String email : list) {
            buffer.append("<a href=\"mailto:").append(email).append("\">").append(email).append("</a><br>");
        }

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /*
     */
    public static String getDataTypeLabel(Field field) {
        return "-";
    }

    /**
     * This method returns the fields descriptors list for a ba
     *
     * @param aSysPrefix Ba prefix
     *
     * @return String descriptors list
     */
    private String getFieldDescriptorsListHtml(String aSysPrefix, boolean isExtended) throws DatabaseException {
        BusinessArea ba = BusinessArea.lookupBySystemPrefix(aSysPrefix);

        if (ba == null) {
            return "";
        }

        int                      systemId = ba.getSystemId();
        StringBuffer             buffer   = new StringBuffer();
        Hashtable<Field, String> table    = new Hashtable<Field, String>();

        // Try to retrieve from the mapper.
        if (ourFieldDescListMap != null) {
            String                     key  = Integer.toString(systemId);
            ArrayList<FieldDescriptor> list = ourFieldDescListMap.get(key);

            if (list != null) {
                for (FieldDescriptor fd : list) {
                    Field field = Field.lookupBySystemIdAndFieldId(systemId, fd.getFieldId());

                    if (field == null) {
                        continue;
                    }

                    if ((isExtended == true) && (field.getIsExtended() == false)) {
                        continue;
                    } else if ((isExtended == false) && (field.getIsExtended() == true)) {
                        continue;
                    }

                    if (((field.getPermission() & Permission.D_ACTION) == 0) && ((field.getPermission() & Permission.SEARCH) == 0)) {
                        continue;
                    }

                    String temp = fd.getDescriptor().toLowerCase();

                    if (fd.getIsPrimary() == true) {
                        temp = "<b>" + temp + "</b>";
                    }

                    String desc = table.get(field);

                    if (desc == null) {
                        table.put(field, temp);
                    } else {
                        if (fd.getIsPrimary() == true) {
                            desc = temp + ", " + desc;
                        } else {
                            desc = desc + ", " + temp;
                        }

                        table.put(field, desc);
                    }
                }
            }
        }

        if ((table.size() > 0) && (isExtended == true)) {
            buffer.append("<TR><TD class=\"sx b dp\" colSpan=2>").append("Custom Fields</TD></TR>");
        }

        Enumeration<Field> e    = table.keys();
        ArrayList<Field>   keys = new ArrayList<Field>();

        while (e.hasMoreElements()) {
            keys.add(e.nextElement());
        }

        Field.setSortParams(Field.DISPLAYNAME, ASC_ORDER);
        keys = Field.sort(keys);

        for (Field field : keys) {
            String desc = table.get(field);

            if (field.getDataTypeId() == DataType.TYPE) {
                buffer.append("\n<tr>\n<td class=\"dp\">").append("<span class=\"sx b ch\">").append(field.getDisplayName()).append("</span><span onclick=\"show('").append(field.getName()).append(
                    "')\" class=\"s cb l\">").append("&nbsp;&nbsp;&nbsp;(Values)</span>");
            } else {
                buffer.append("\n<tr>\n<td class=\"sx b ch dp\">").append(field.getDisplayName());
            }

            buffer.append("</td>\n<td class=\"sx\">").append(desc).append("</td>").

            // append("<td class=\"sx dp\">").
            // append(getDataTypeLabel(field)).
            // append("</td>").
            append("\n</tr>");
        }

        return buffer.toString();
    }

    /**
     * This method returns the holidays in different offices in a formatted
     * html.
     *
     * @return formatted list of holidays.
     */
    public static String getFormattedHolidayList() {
        ArrayList<String>                          officeList = new ArrayList<String>();
        Hashtable<String, ArrayList<HolidaysList>> hTable     = new Hashtable<String, ArrayList<HolidaysList>>();

        HolidaysList.getHolidays(officeList, hTable);

        if ((officeList == null) || (officeList.size() == 0)) {
            return "No Holidays";
        }

        StringBuffer buffer = new StringBuffer();

        for (String office : officeList) {
            ArrayList<HolidaysList> list = hTable.get(office);

            if ((list == null) || (list.size() == 0)) {
                continue;
            }

            buffer.append("\n<TD valign='top'>").append("\n<span class='sx b'>").append(office.toUpperCase()).append(":</span>").append("\n<table class=\"results\" width='90%'>")

//          .append("\n<TR class=\"header\">")
//          .append("\n<TD class='b'>&nbsp;Date</TD>")
//          .append("\n<TD class='b'>&nbsp;Holiday</TD>")
//          .append("\n</TR>")
            ;

            String currentDomain = "";

            currentDomain = PropertiesHandler.getProperty(TBitsPropEnum.KEY_DOMAIN);

            for (HolidaysList hl : list) {
                buffer.append("<TR>").append("<TD nowrap class='ff'>").append(hl.getHolidayDate().toCustomFormat("yyyy MMM dd")).append("</TD>").append("<TD nowrap class='ff'>").append(
                    hl.getDescription()).append("</TD>").append("</TR>");
            }

            buffer.append("\n</table>").append("\n</TD>")
            ;
        }

        return buffer.toString();
    }

    /*
     */
    private static String getTypeDescriptorHtml(Field aField) throws DatabaseException {
        if (aField == null) {
            return "";
        }

        int                     systemId = aField.getSystemId();
        int                     fieldId  = aField.getFieldId();
        StringBuffer            buffer   = new StringBuffer();
        Hashtable<Type, String> table    = new Hashtable<Type, String>();

        // Try to retrieve from the mapper.
        if (ourTypeDescListMap != null) {
            String                    key  = Integer.toString(systemId) + "-" + fieldId;
            ArrayList<TypeDescriptor> list = ourTypeDescListMap.get(key);

            if (list != null) {
                for (TypeDescriptor td : list) {
                    Type type = Type.lookupBySystemIdAndFieldIdAndTypeId(systemId, fieldId, td.getTypeId());

                    if (type == null) {
                        continue;
                    }

                    String temp = td.getDescriptor().toLowerCase();

                    if (td.getIsPrimary() == true) {
                        temp = "<b>" + temp + "</b>";
                    }

                    String desc = table.get(type);

                    if (desc == null) {
                        table.put(type, temp);
                    } else {
                        if (td.getIsPrimary() == true) {
                            desc = temp + ", " + desc;
                        } else {
                            desc = desc + ", " + temp;
                        }

                        table.put(type, desc);
                    }
                }
            }
        }

        if (table.size() == 0) {
            return "";
        } else {
            buffer.append("<TABLE border=1><TBODY><TR>").append("<TD class=\"bn sxs b cw dp\">").append(aField.getDisplayName()).append(" Type Name</TD>").append(
                "<TD class=\"bn sxs b cw dp\">&nbsp;Descriptors</TD>").append("</TR>");
        }

        Enumeration<Type> e    = table.keys();
        ArrayList<Type>   keys = new ArrayList<Type>();

        while (e.hasMoreElements()) {
            keys.add(e.nextElement());
        }

        Type.setSortParams(Type.DISPLAYNAME, ASC_ORDER);
        keys = Type.sort(keys);

        for (Type type : keys) {
            String desc = table.get(type);

            buffer.append("\n<tr>\n<td class=\"sx b ch dp\">").append(type.getDisplayName()).append("</td>\n<td class=\"sx\">").append(desc).append("</td>\n</tr>");
        }

        buffer.append("</tbody></table>");

        return buffer.toString();
    }

    /*
     */
    public static String getTypeValueDivs(String aSysPrefix) throws DatabaseException {
        BusinessArea ba = BusinessArea.lookupBySystemPrefix(aSysPrefix);

        if (ba == null) {
            return "";
        }

        int                     systemId = ba.getSystemId();
        StringBuffer            buffer   = new StringBuffer();
        ArrayList<Field>        fields   = Field.lookupBySystemId(ba.getSystemId());
        Hashtable<Type, String> table    = new Hashtable<Type, String>();

        for (Field field : fields) {
            if (field.getDataTypeId() != DataType.TYPE) {
                continue;
            }

            buffer.append("\n<div id='").append(field.getName()).append("' name='").append(field.getName()).append("'>\n").append(getTypeDescriptorHtml(field)).append("</div>");
        }

        return buffer.toString();
    }

    /**
     * This method returns the userinfo needed for autocomplete feature.
     *
     * @param value User object.
     *
     * @return UserInfo record.
     */
    private String getUserInfoRecord(User value) {
        StringBuffer buffer      = new StringBuffer();
        String       userLogin   = value.getUserLogin();
        String       displayName = value.getDisplayName();

        displayName = displayName.replaceAll(".transbittech.com", "");
        userLogin   = userLogin.replaceAll(".transbittech.com", "");
        buffer.append("\"");

        if (displayName.equals(userLogin)) {
            buffer.append(userLogin);
        } else {
            buffer.append(displayName).append(" <").append(userLogin).append(">");
        }

        buffer.append("\"");

        return buffer.toString();
    }
}
