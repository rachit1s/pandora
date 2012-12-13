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
 * WebUtil.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

//Log4j imports
import org.apache.log4j.MDC;
import org.htmlcleaner.ContentToken;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jfree.util.Log;

import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsPropEnum;

//Static imports.
//TBits Imports.
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.config.DraftConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserDraft;
import transbit.tbits.exception.TBitsException;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//~--- classes ----------------------------------------------------------------

/**
 * This is an utility class that contains methods used by almost all the
 * servlets in the webapps package.
 *
 * @author  : Vaibhav.
 * @author  : Vinod Gupta .
 * @version : $Id: $
 */
public class WebUtil implements TBitsConstants, TBitsPropEnum {

    // Enums to represent the pages.
    public static final int ADD_REQUEST     = 1;
    public static final int ADD_ACTION      = 2;
    public static final int ADD_SUBREQUEST  = 3;
    public static final int MY_REQUESTS     = 4;
    public static final int SEARCH          = 5;
    public static final int REPORTS         = 6;
    public static final int OPTIONS         = 7;
    public static final int HELP            = 8;
    public static final int VIEW_REQUEST    = 9;
    public static final int ADMIN 			= 10;
    public static final int READ_ATTACHMENT = 11;

    // File that is returned as ouput by getPreferredZone method when
    // clientOffset could not be obtained from any of the sources.
    private static final String OFFSET_FILE = "web/tbits-offset.htm";

    // The Logger that is used to log messages to the application log.
    private static final TBitsLogger LOG   = TBitsLogger.getLogger(PKG_WEBAPPS);

    // Flag to see if NTLM-Authentication is enabled.
    public static boolean isNTLMEnabled = true;

    // Name of the client offset cookie.
    private static final String CO_COOKIE_NAME = "ClientOffset";

    // Maximum age of a cookie - set to an year.
    private static final int COOKIE_AGE = 60 * 60 * 24 * 365;

    // Redirection URL Value
    public static String ourRedirectionUrl = "";

    // Nearest URL Value
    public static String ourNearestPath = "";

    // Domain
    public static String ourDomain = "";

    //~--- static initializers ------------------------------------------------

    static {
        ourRedirectionUrl = PropertiesHandler.getProperty(KEY_REDIRECTION_URL);

        if ((ourRedirectionUrl != null) && (ourRedirectionUrl.trim().equals("") == false)) {
            ourRedirectionUrl = ourRedirectionUrl.trim();

            if (ourRedirectionUrl.endsWith("/") == false) {
                ourRedirectionUrl = ourRedirectionUrl + "/";
            }
        }

        ourNearestPath = PropertiesHandler.getProperty(KEY_NEAREST_INSTANCE);

        if ((ourNearestPath != null) && (ourNearestPath.trim().equals("") == false)) {
            ourNearestPath = ourNearestPath.trim();

            if (ourNearestPath.endsWith("/") == false) {
                ourNearestPath = ourNearestPath + "/";
            }
        }

        ourDomain = PropertiesHandler.getProperty(KEY_DOMAIN);
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method return true if the given field can be added according to
     * the given perm table.
     *
     * @param aPermTable Permission Table.
     * @param aFieldName Field Name.
     *
     * @return True  if the field can be changed.
     *         False otherwise.
     */
    public static boolean canAdd(Hashtable<String, Integer> aPermTable, String aFieldName) {
        Integer temp       = null;
        int     permission = 0;

        temp = aPermTable.get(aFieldName);

        if (temp == null) {
            return false;
        }

        permission = temp.intValue();

        if ((permission & Permission.ADD) == 0) {
            return false;
        }

        return true;
    }

    /**
     * This method return true if the given field can be changed according to
     * the given perm table.
     *
     * @param aPermTable Permission Table.
     * @param aFieldName Field Name.
     *
     * @return True  if the field can be changed.
     *         False otherwise.
     */
    public static boolean canChange(Hashtable<String, Integer> aPermTable, String aFieldName) {
        Integer temp       = null;
        int     permission = 0;

        temp = aPermTable.get(aFieldName);

        if (temp == null) {
            return false;
        }

        permission = temp.intValue();

        if ((permission & Permission.CHANGE) == 0) {
            return false;
        }

        return true;
    }

    /**
     * Method to convert comma seperated username (Login Names) string to
     * Arraylist of  coresponding user objects
     *
     * @param  aUserLogins  the comma seperated string of user login Names
     * @return   Arraylist ofcorresponding to user objects.
     */
    public static ArrayList<User> convertNamesToUserArrayList(String aUserLogins) throws DatabaseException {
        ArrayList<User> usersArrayList = new ArrayList<User>();

        if ((aUserLogins == null) || aUserLogins.trim().equals("")) {
            return usersArrayList;
        }

        StringTokenizer st   = new StringTokenizer(aUserLogins, ",;");
        User            user = null;

        while (st.hasMoreTokens()) {
            user = User.lookupByUserLogin(((String) st.nextToken()).trim());

            // Add If user active and not a duplicate
            if ((user != null) && (!usersArrayList.contains(user))) {
                usersArrayList.add(user);
            }
        }

        return usersArrayList;
    }

    /*
     */
    public static String listUserDrafts(HttpServletRequest aRequest, int aUserId, boolean separator, boolean needEmptyInfo) {
        ArrayList<UserDraft> drafts      = null;
        String               onMouseOver = "this.T_STICKY = true; " + "this.T_TEMP = 0; " + "this.T_DELAY = 300; " + "this.T_WIDTH  = '250px';" + "this.T_BORDERWIDTH = 0;";

        try {
            drafts = UserDraft.lookupByUserId(aUserId);
        } catch (Exception e) {
            LOG.info("",(e));
        }

        if (drafts == null) {
            drafts = new ArrayList<UserDraft>();
        }

        String bClass = "";

        if ((needEmptyInfo == false) && (drafts.size() == 0)) {
            return "";
        }

        if (drafts.size() > 0) {
            bClass = "class=\"b\"";
        }

        BusinessArea  ba        = null;
        int           systemId  = 0;
        int           requestId = 0;
        StringBuilder sb        = new StringBuilder();

        sb.append("<span ").append(bClass).append(" onclick=\"").append(onMouseOver).append(" return escape(").append("showDrafts(event)").append(");\"").append(
            " onmouseout=\"setMouseOutTimer()\" ;").append(" >").append("Drafts").append(" (").append(drafts.size()).append(")\n</span>&nbsp;").append((separator == true)
                ? "|"
                : "").append("&nbsp;<div id=\"").append("drafts").append("\" style=\"display:none;\">");

        if (drafts.size() == 0) {
            sb.append("No Drafts.");
        } else {
            sb.append("\n<table cellpadding=\"0\"").append("cellspacing=\"0\" width=\"100%\" name=\"draftsTable\"").append("onmouseout='closeTarget(").append(
                "event,\"TABLE\",\"draftsTable\");' ").append(" id=\"draftsTable\" class=\"shortcuts\">\n").append("\n\t<colgroup>").append("\n\t\t<col width=\"80%\"/>").append(
                "\n\t\t<col width=\"20%\" align=\"center\" />").append("\n\t</colgroup>").append("\n\t<thead>").append("\n\t<tr><td>Subject</td>").append("\n\t<td class='title'>Delete</td></tr>").append(
                "\n\t</thead>").append("\n\t<tbody>");

            int i = 0;

            for (UserDraft ud : drafts) {
                systemId  = ud.getSystemId();
                requestId = ud.getRequestId();

                Hashtable<String, String> draftHash = new Hashtable<String, String>();

                try {
                    draftHash = DraftConfig.xmlDeSerialize(ud.getDraft());
                } catch (Exception e) {
                    draftHash = new Hashtable<String, String>();
                }

                try {
                    ba = BusinessArea.lookupBySystemId(systemId);
                } catch (Exception e) {
                    LOG.debug("",(e));
                }

                String sysPrefix = ((ba == null)
                                    ? ""
                                    : ba.getSystemPrefix());
                String subject   = draftHash.get(Field.SUBJECT);

                if (subject == null) {
                    subject = "&nbsp;";
                }

                sb.append("\n\t\t<tr valign=\"top\" align=\"left\">").append("\n\t\t\t<td class='title' ").append("onmouseover=").append("'this.style.backgroundColor=\"darkgray\";'").append(
                    " onmouseout='this.style.backgroundColor=").append("\"transparent\";").append("closeTarget(event,\"TABLE\",\"draftsTable\"); ' ").append("onclick=\"javascript:loadDraft(").append(
                    "'").append(sysPrefix).append("'").append(",").append(systemId).append(",").append(requestId).append(",").append(ud.getTimestamp().toSiteTimestamp().getTime()).append(",").append(ud.getDraftId()).append(
                    ");\">").append(sysPrefix).append("#").append((requestId == 0)
                        ? ""
                        : Integer.toString(requestId)).append(":&nbsp;").append(subject).append("&nbsp;&nbsp;(").append(ud.getTimestamp().toDateMin()).append(")").append("</td>").append(
                            "\n\t\t\t<td class=\"title\" ").append("onmouseover=").append("'this.style.backgroundColor=\"darkgray\";'").append(" onmouseout='this.style.backgroundColor=").append(
                            "\"transparent\";").append("closeTarget(event,\"TABLE\",\"draftsTable\"); ' ").append("onclick=\"javascript:deleteDraft(").append(systemId).append(",").append(
                            requestId).append(",").append(ud.getTimestamp().toSiteTimestamp().getTime()).append(",").append(ud.getDraftId()).append(",").append(i).append(");\">").append("<img width=\"16\" height=\"16\" src=\"").append(
                            WebUtil.getNearestPath(aRequest, "web/images/ckdeny.gif")).append("\" />").append("</td>\n\t\t</tr>");
                i++;
            }

            sb.append("\n</tbody>\n</table>\n");
        }

        sb.append("\n</div>");

        return sb.toString();
    }

    /**
     * This method checks if the user should be allowed to access the system.
     * If yes, then it returns the corresponding User Object.
     * If no,  then it throws TBitsException.
     *
     *
     * @param aRequest HttpRequest object.
     * @exception DatabaseException In case of any database related errors.
     * @exception TBitsExcepiton  In case of any other exceptions.
     */
    public static User validateUser(HttpServletRequest aRequest) throws DatabaseException, TBitsException {
        User   user               = null;
        String aRequestRemoteUser = "";

        // Login of the user.
        String userLogin = "";

        // Email address of the user.
        String userEmail = "";
        
        /*
         * Cases to be tested when this method is changed. This can be done by
         * turning off the automatic NTLM authentication from firefox and then
         * accessing tbits test server.
         *    1. UserLogin: Vaibhav
         *    2. UserLogin: transbit\Vaibhav
         *    3. UserLogin: Vaibhav@transbittech.com
         *    4. UserLogin: xyz.hyd.transbittech.com\Vaibhav
         *    5.
         */

        // Check the flag if NTLM Authentication is enabled.
        if (isNTLMEnabled == true) {

            //
            // If it is enabled, we get the user-details from the
            // getRemoteUser() method of Http Request Object.
            //
            aRequestRemoteUser = aRequest.getRemoteUser();

            if (aRequestRemoteUser != null) {

                // e.g. transbit\Vaibhav
                String domainRegex = "([^\\\\]+)\\\\([A-Za-z0-9\\-_\\.]+)";


                // e.g. tbits.hyd.transbittech.com\Vaibhav
                String serverRegex = "(.+)\\\\([A-Za-z0-9\\-_\\.]+)";

                // e.g. Vaibhav@transbittech.com
                String emailRegex = "([A-Za-z0-9\\-_]+)@([^\\.]+)\\.(.+)";

                if (aRequestRemoteUser.matches(domainRegex) == true) {
                    LOG.info("Login user pattern mathed " + domainRegex);

                    Pattern p = Pattern.compile(domainRegex);
                    Matcher m = p.matcher(aRequestRemoteUser);

                    if (m.matches() == true) {

                        // Second part is the user login.
                        userLogin = m.group(2);
                        userEmail = userLogin + "@" + m.group(1) + ".com";
                    }
                } else if (aRequestRemoteUser.matches(emailRegex) == true) {
                    LOG.info("Login user pattern mathed " + emailRegex);

                    Pattern p = Pattern.compile(emailRegex);
                    Matcher m = p.matcher(aRequestRemoteUser);

                    if (m.matches() == true) {

                        // First part is the user login.
                        userLogin = m.group(1);

                        // Remote user itself is in email format.
                        userEmail = aRequestRemoteUser;
                    }
                } else if (aRequestRemoteUser.matches(serverRegex) == true) {
                    
                    Pattern p = Pattern.compile(serverRegex);
                    Matcher m = p.matcher(aRequestRemoteUser);

                    if (m.matches() == true) {
                        LOG.info("Login user pattern mathed " + serverRegex);
                        // Second part is user login.
                        userLogin = m.group(2);

                        // Append @transbittech.com to the userlogin
                        userEmail = userLogin + "@transbittech.com";//TODO: Remove this.
                    }
                } else {
                    LOG.debug("None of the patterns matched on user login. So going ahead with the user name '" + aRequestRemoteUser + "' as such");
                    userLogin = aRequestRemoteUser;
                    userEmail = userLogin + "@transbittech.com";//TODO: Remove this.
                }
            }
        } else {

            //
            // If NTLM is not enabled, we read the user-details from the
            // HttpRequest methods as a request-parameter.
            //
            aRequestRemoteUser = aRequest.getParameter("remoteUser");
            userLogin          = aRequestRemoteUser;
            userEmail          = userLogin + "@transbittech.com"; //TODO: Remove this.
        }

        /*
         * Incase we could not get the RemoteUser from any of the above paths,
         * There is some serious error, so reject the request as we cannot
         * proceed further without the user-details.
         */
        if ((aRequestRemoteUser == null) || aRequestRemoteUser.trim().equals("")) {
            throw new TBitsException(Messages.getMessage("INVALID_USER", "unknown"));
        }

        try {

            // Look up in the active user list by login first.
            LOG.debug("USERLOGIN: " + userLogin);
            user = User.lookupByUserLogin(userLogin);
            LOG.debug(" AfterLooking up USERLOGIN: " + user);

            if (user == null) {
                LOG.debug("Lookup by user login failed!!! " + "Looking up by email now ...");

                // Try to resolve the user with email address.
                user = User.lookupByUserLogin(userEmail);
            }
        } catch (DatabaseException de) {
            throw new TBitsException(Messages.getMessage("INVALID_USER", aRequestRemoteUser), de);
        } catch (Exception e) {
            throw new TBitsException(Messages.getMessage("INVALID_USER", aRequestRemoteUser), e);
        }

        if (user == null) {
            throw new TBitsException(Messages.getMessage("INVALID_USER", aRequestRemoteUser));
        }

        return user;
    }

    //~--- get methods --------------------------------------------------------

    /*
     */
    public static String getBAsListHtml(int aUserId, String aAction) {
        String target = " target=\"search_hyd\"";
        String domain = "NYC";

        if (PropertiesHandler.getProperty("transbit.tbits.myDomain").toLowerCase().equals("hyd")) {
            target = " target=\"search_nyc\"";
            domain = "HYD";
        }

        if (aAction.equals("add-request")) {
            target = " target=\"_blank\"";
        }

        ArrayList<BusinessArea> userBAs = null;

        try {
            userBAs = BusinessArea.getBusinessAreasByUserId(aUserId);
        } catch (Exception e) {
            LOG.severe("",(e));

            return "";
        }

        if ((userBAs == null) || (userBAs.size() == 0)) {
            return "";
        }

        StringBuilder baListHtml = new StringBuilder();

        baListHtml.

        // append("\n<script>document.domain=\"transbittech.com\";</script>").
        append("<TABLE name='baList' cellpadding='0' cellspacing='0' ").append("class='shortcuts' width='100%'").append("onmouseover='notInToolTip = false;' ").append(
            "onmouseout='closeTarget(event,\"TABLE\",\"baList\");'>").append("<colgroup><col width='250px' /></colgroup>").

        // append("<THEAD><TR><TD align='center' ").
        // append("onmouseout='closeTarget(event,\"TABLE\",\"baList\");'>").
        // append(domain).
        // append(" Business Areas</TD></TR></THEAD><TBODY>");
        append("<TBODY");

        for (BusinessArea ba : userBAs) {
            baListHtml.append("\n<TR><TD ").append("onmouseover=").append("'this.style.backgroundColor=\"darkgray\";'").append(" onmouseout='this.style.backgroundColor=").append(
                "\"transparent\";").append("closeTarget(event,\"TABLE\",\"baList\");'>").append("<a onmouseout=").append("'closeTarget(event,\"TABLE\",\"baList\");' ").append(
                "class=\"l cbk\" href=\"/").append(aAction).append("/").append(ba.getSystemPrefix()).append("\"").append(target).append(">").append(ba.getDisplayName()).append(" [").append(
                ba.getSystemPrefix()).append("]</a></TD></TR>");
        }

        baListHtml.append("\n</TBODY></TABLE>");

        return baListHtml.toString();
    }

    /**
     * This method returns the name of the css file to be used.
     *
     *
     * @param cssFile   Filename as in SysConfig object of the BA.
     * @param sysPrefix BA Prefix.
     * @param email     True if for email.
     * @return Name of the css file.
     */
    public static String getCSSFile(String cssFile, String sysPrefix, boolean email) {
        String defaultFile = (email == true)
                             ? "tbits_email.css"
                             : "tbits.css";

        // Convert sysPrefix to lower case.
        sysPrefix = sysPrefix.toLowerCase();

        // searchFile holds the name of the file to be searched.
        String searchFile = cssFile;

        if ((cssFile == null) || cssFile.trim().equals("")) {
            searchFile = sysPrefix + "_" + defaultFile;
        } else {
            searchFile = cssFile;
        }

        try {
            File file = Configuration.findPath("web/css/" + searchFile);

            if (file == null) {
                searchFile = sysPrefix + "_" + defaultFile;
                file       = Configuration.findPath("web/css/" + searchFile);

                if (file == null) {
                    cssFile = defaultFile;
                } else {
                    cssFile = searchFile;
                }
            } else {
                cssFile = searchFile;
            }
        } catch (Exception e) {
            cssFile = defaultFile;
        }

        return cssFile;
    }

    /**
     * Method to get the clientOffset given the Http Request Object
     *
     * @param aRequest  HttpRequest Object which may contain information
     *                  related to the clientOffset.
     *
     * @return Returns the clientOffset.
     */
    private static int getClientOffset(HttpServletRequest aRequest) {
        String strClientOffset = aRequest.getParameter("clientOffset");

        if ((strClientOffset != null) &&!strClientOffset.trim().equals("")) {
            try {
                int clientOffset = Integer.parseInt(strClientOffset);

                return clientOffset;
            } catch (Exception e) {
                return -1;
            }
        }

        return -1;
    }

    /**
     * Method to get the clientOffset given the Http Request Object and set
     * a cookie in the Response object.
     *
     * @param aRequest  HttpRequest Object which may contain information
     *                  related to the clientOffset.
     * @param aResponse HttpResponse Object which has a cookie set for storing
     *                  the clientOffset on client's machine.
     *
     * @return Returns the clientOffset.
     */
    public static int getClientOffset(HttpServletRequest aRequest, HttpServletResponse aResponse) {
        Cookie[] cookies = aRequest.getCookies();

        // If there are no cookies, then check if we got client offset
        // as a request object parameter.
        if (cookies == null) {
            int clientOffset = getClientOffset(aRequest);

            if (clientOffset != -1) {
                Cookie cookie = new Cookie(CO_COOKIE_NAME, Integer.toString(clientOffset));

                cookie.setPath("/");
                cookie.setMaxAge(COOKIE_AGE);
                aResponse.addCookie(cookie);
            }

            return clientOffset;
        }

        for (int i = 0; i < cookies.length; i++) {
            if (cookies[i].getName().equalsIgnoreCase(CO_COOKIE_NAME)) {
                try {
                    int clientOffset = Integer.parseInt(cookies[i].getValue());

                    cookies[i].setPath("/");
                    cookies[i].setMaxAge(COOKIE_AGE);
                    aResponse.addCookie(cookies[i]);

                    return clientOffset;
                } catch (Exception e) {
                    return -1;
                }
            }
        }

        //
        // there is no clientOffset cookie in the list of cookies we received
        // so check if we got clientOffset as a request object parameter.
        //
        int clientOffset = getClientOffset(aRequest);

        if (clientOffset != -1) {
            Cookie cookie = new Cookie(CO_COOKIE_NAME, Integer.toString(clientOffset));

            cookie.setMaxAge(COOKIE_AGE);
            aResponse.addCookie(cookie);
        }

        return clientOffset;
    }

    /**
     * This method returns the URL of this site.
     *
     * @return URL of the current site.
     */
    private static String getCurrentSiteURL() {
        String url = getServletPath("");

        if (ourDomain.equalsIgnoreCase("hyd")) {
            url = PropertiesHandler.getProperty(KEY_HYDURL);
        } else if (ourDomain.equalsIgnoreCase("nyc")) {
            url = PropertiesHandler.getProperty(KEY_NYCURL);
        }

        if (url.endsWith("/") == false) {
            url = url + "/";
        }

        return url;
    }

    /**
     * This method takes the GMT Timestamp in Site's Zone and then returns the
     * date in preferred format in preferred zone.
     *
     * @param aGmtTime  Gmt Time in Site's Zone.
     * @param aTimeZone Preferred Time Zone.
     * @param aFormat   Preferred format.
     *
     * @return Date time in preferred format.
     */
    public static String getDateInFormat(Timestamp aGmtTime, TimeZone aTimeZone, String aFormat) {
        try {

            // Site's Zone.
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

            format.setTimeZone(TimeZone.getDefault());

            // GMT Zone.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

            // GMT Time in Site Zone.
            Date   t  = new Date(aGmtTime.getTime());
            String dt = sdf.format(new Date(t.getTime()));
            Date   tt = t;
            Date   t2 = format.parse(dt);

            tt = new Date(t.getTime() + (t.getTime() - t2.getTime()));

            Calendar cal = Calendar.getInstance();

            cal.setTimeInMillis(tt.getTime());

            SimpleDateFormat finale = new SimpleDateFormat(aFormat);

            finale.setTimeZone(aTimeZone);

            return finale.format(cal.getTime());
        } catch (Exception e) {
            LOG.info("",(e));

            return aGmtTime.toDateMin();
        }
    }
    
    public static String getDateInFormat(Date aGmtTime, TimeZone aTimeZone, String aFormat) {
        try {

            // Site's Zone.
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

            format.setTimeZone(TimeZone.getDefault());

            // GMT Zone.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

            // GMT Time in Site Zone.
            Date   t  = new Date(aGmtTime.getTime());
            String dt = sdf.format(new Date(t.getTime()));
            Date   tt = t;
            Date   t2 = format.parse(dt);

            tt = new Date(t.getTime() + (t.getTime() - t2.getTime()));

            Calendar cal = Calendar.getInstance();

            cal.setTimeInMillis(tt.getTime());

            SimpleDateFormat finale = new SimpleDateFormat(aFormat);

            finale.setTimeZone(aTimeZone);

            return finale.format(cal.getTime());
        } catch (Exception e) {
            LOG.info("",(e));

            return Timestamp.toDateMin(aGmtTime);
        }
    }

    /**
     * This method returns true if the user has view permission on confidential
     * field in this business area..
     *
     * @param aPermTable Permission table of this user.
     * @return True If the user has view on private field. False Otherwise.
     */
    public static boolean getIsPrivate(Hashtable<String, Integer> aPermTable) {
        boolean isPrivate = false;
        Integer tmp       = aPermTable.get(Field.IS_PRIVATE);

        if (tmp != null) {

            // Check if the user has view on is_private.
            int permission = tmp.intValue();

            if ((permission & Permission.VIEW) != 0) {
                isPrivate = true;
            }
        }

        return isPrivate;
    }

    /**
     * This method returns the nearest path for the static resource
     * @param aRequest TODO
     * @param aStaticResource generally an image or a css or a javascript file.
     *
     * @return Nearest path
     */
    public static String getNearestPath(String aStaticResource) {
        String path = "";

        if (aStaticResource.startsWith("/") == true) {
            aStaticResource = aStaticResource.substring(1);
        }

        path = ourNearestPath + aStaticResource;

        return path;
    }
    
    /**
     * This method returns the nearest path for the static resource wrt to the context path of a servlet
     *
     * @param aStaticResource generally an image or a css or a javascript file.
     *
     * @return Nearest path
     */
    public static String getNearestPath(HttpServletRequest aRequest, String aStaticResource) {
    	if(aRequest == null)
    	{
    		System.out.println("Request is null!");
    		return getNearestPath(aStaticResource);
    	}
    	return getNearestPath(aRequest.getContextPath(), aStaticResource);
    }
    
    /**
     * This method returns the nearest path for the static resource wrt to the context path of a servlet
     *
     * @param aStaticResource generally an image or a css or a javascript file.
     *
     * @return Nearest path
     */
    public static String getNearestPath(String servletContext, String aStaticResource) {
    	
    	if(!servletContext.endsWith("/"))
    		servletContext = servletContext + "/";
        String path = servletContext;

        if (aStaticResource.startsWith("/") == true) {
            aStaticResource = aStaticResource.substring(1);
        }

        path = path + aStaticResource;
        return path;
    }
    /**
     * Method that returns a Timezone object based on the userZone passed
     *
     * @param aUserZone     string that represents the user's preference.
     * @param aClientOffset To be used if user's preference is local/browser.
     *
     * @return TimeZone object according to the user-preferred zone.
     */
    public static TimeZone getPreferredZone(int aUserZone, int aClientOffset) {
        TimeZone preferredZone = null;

        try {

            // if aUserZone is null or empty then default to "site".
            if ((aUserZone != 1) && (aUserZone != 3)) {
                aUserZone = SITE_ZONE;
            }

            if (aUserZone == LOCAL_ZONE) {
                if (aClientOffset == 240) {
                    preferredZone = TimeZone.getTimeZone("EST5EDT");
                } else if (aClientOffset == 300) {
                    preferredZone = TimeZone.getTimeZone("EST");
                } else {
                    aClientOffset = -1 * aClientOffset;

                    String[] zones = TimeZone.getAvailableIDs(aClientOffset * 60000);

                    if (zones.length == 0) {
                        preferredZone = (Calendar.getInstance()).getTimeZone();
                    } else {
                        preferredZone = TimeZone.getTimeZone(zones[0]);
                    }
                }
            } else if (aUserZone == SITE_ZONE) {
                preferredZone = (Calendar.getInstance()).getTimeZone();
            } else if (aUserZone == GMT_ZONE) {

                // Global zone is GMT Zone.
                preferredZone = TimeZone.getTimeZone("GMT");
            } else {

                // Site's zone is the default.
                preferredZone = (Calendar.getInstance()).getTimeZone();
            }
        } catch (Exception e) {
            LOG.warn("",(e));

            // Site's zone is the default.
            preferredZone = (Calendar.getInstance()).getTimeZone();
        }

        return preferredZone;
    }

    /**
     * This method returns the HTML that, when sent to the client, will call
     * the servlet specified as action with clientOffset as a request parameter
     *
     * @param aRequest    Http Request Object.
     * @param aAction     The name of the servlet that should be called with
     *                    clientOffset as a request parameter.
     *
     * @return RedirectionHTML as a string.
     */
    public static String getRedirectionHtml(HttpServletRequest aRequest, String aAction) {
        try {
            DTagReplacer hp          = new DTagReplacer(OFFSET_FILE);
            String       queryString = aRequest.getQueryString();

            queryString = (queryString == null)
                          ? ""
                          : queryString;
            hp.replace("nearestPath", WebUtil.getServletPath(aRequest, ""));
            hp.replace("action", WebUtil.getServletPath(aRequest, aAction));
            hp.replace("queryString", queryString);

            return hp.parse(0);
        } catch (FileNotFoundException fnfe) {
            LOG.severe("The offset file for getting the client offset " + "hasnt been found", fnfe);

            return "";
        } catch (IOException ioe) {
            LOG.severe("",(ioe));

            return "";
        }
    }

    /**
     * This method parses the PathInfo of the HttpRequest object and extracts
     * the required parameters based on the caller.
     *
     *
     * @param aRequest    Http Request Object.
     * @param aConfig     UserConfig.
     * @param aPage       Caller of the function.
     * @return Hashtable that contains the BA and Request Object based on
     *            the caller.
     * @exception ServletException  Thrown in case of servlet related errors.
     * @exception TBitsExceptionTBitsException of any application errors.
     * @exception DatabaseException Thrown in case of any database errors.
     */
    public static Hashtable<String, Object> getRequestParams(HttpServletRequest aRequest, WebConfig aConfig, int aPage) throws ServletException, TBitsException, DatabaseException {

        // This hashtable will contain the BusinessArea object in all cases
        // and Request object also in case of View-Request page.
        Hashtable<String, Object> result   = new Hashtable<String, Object>();
        String                    pathInfo = aRequest.getPathInfo();

        // Check if pathInfo is null.
        if (pathInfo == null) {

            //
            // If View-Request is the caller, then it is a must that we get
            // both the system-prefix and the requestId. Without any of these
            // we cannot proceed further, so,  return null.
            //
            if ((aPage == VIEW_REQUEST) || (aPage == ADD_ACTION) || (aPage == ADD_SUBREQUEST)) {
                throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA", ""));
            }

            // In all other cases, consider the user's Default BA.
            String       systemPrefix = aConfig.getSystemPrefix();
            BusinessArea ba           = BusinessArea.lookupBySystemPrefix(systemPrefix);

            if (ba == null) {

                // If user default BA is no longer active,
                // get First available active Ba
                ba = BusinessArea.getFirstActiveBusinessArea();

                if (ba == null) {
                    throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA", systemPrefix));
                }
            }

            result.put(Field.BUSINESS_AREA, ba);
            MDC.put("SYS_PREFIX", ba.getSystemPrefix());
        } else {

            // There is something in the pathInfo we received.
            StringTokenizer st = new StringTokenizer(pathInfo, "/\\");

            //
            // Once the pathInfo is tokenized, the first token is expected to
            // be the SystemPrefix.
            //
            if (st.hasMoreTokens() == true) {
                String       sysPrefix = st.nextToken().trim();
                BusinessArea ba        = BusinessArea.lookupBySystemPrefix(sysPrefix);

                if (ba == null) {
                    throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA", sysPrefix));
                }

                result.put(Field.BUSINESS_AREA, ba);
                MDC.put("SYS_PREFIX", ba.getSystemPrefix());

                int systemId = ba.getSystemId();

                //
                // If View-Request is the caller, then consider the next token
                // as the Request ID.
                //
                if ((aPage == VIEW_REQUEST) || (aPage == ADD_ACTION) || (aPage == ADD_SUBREQUEST)) {
                    if (st.hasMoreTokens() == true) {
                        String strRequestId = st.nextToken().trim();                     
                        int    requestId    = 0;

                        try {
                            requestId = Integer.parseInt(strRequestId);
                        } catch (NumberFormatException nfe) {

                            // If the requestId token is in incorrect format,
                            // Throw an error about the same.
                            throw new TBitsException(Messages.getMessage("INVALID_REQUEST_ID", strRequestId), nfe);
                        }

                        // Get the request object corresponding to this Id.
                        Request request = null;

//                        if (aPage == VIEW_REQUEST) {
                            request = Request.lookupBySystemIdAndRequestId(systemId, requestId);
//                        } else {
//                            request = Request.lookupBySystemIdAndRequestId(systemId, requestId);
//                        }

                        if (request == null) {
                            throw new TBitsException(Messages.getMessage("INVALID_REQUEST_ID", strRequestId));
                        }

                        result.put(Field.REQUEST, request);
                        MDC.put("REQUEST_ID", Integer.toString(request.getRequestId()));
                    } else {

                        // Request Id is not specified.
                        throw new TBitsException(Messages.getMessage("INVALID_REQUEST_ID", ""));
                    }
                }
            } else {

                //
                // If View-Request called this, then there is no use of
                // taking the default BA because we are still without the
                // request id value.
                //
                if ((aPage == VIEW_REQUEST) || (aPage == ADD_ACTION) || (aPage == ADD_SUBREQUEST)) {

                    // Request Id is not specified.
                    throw new TBitsException(Messages.getMessage("INVALID_REQUEST_ID", ""));
                }

                String       systemPrefix = aConfig.getSystemPrefix();
                BusinessArea ba           = BusinessArea.lookupBySystemPrefix(systemPrefix);

                if (ba == null) {

                    // If user default BA is no longer active,
                    // get First available active Ba
                    ba = BusinessArea.getFirstActiveBusinessArea();

                    if (ba == null) {
                        throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA", ""));
                    }
                }

                result.put(Field.BUSINESS_AREA, ba);
            }
        }

        return result;
    }

    /**
     * This method returns the absolute path for the servlet given in aAction.
     * @param aRequest TODO
     * @param aAction     The name of the servlet.
     *
     * @return Absolute path as a string.
     */
    public static String getServletPath(HttpServletRequest aRequest, String aAction) {
//        String path = "";
//
//        if (aAction.startsWith("/") == true) {
//            aAction = aAction.substring(1);
//        }
//
//        path = ourRedirectionUrl + aAction;

        return getNearestPath(aRequest, aAction);
    }
    
    public static String getServletPath(String aAction) {
    	return getNearestPath(aAction);
    }
    public static String getServletPath(String contextPath, String aAction) {
    	return getNearestPath(contextPath, aAction);
    }

    /**
     * This method returns the name of the TBits Theme file based on the
     * system prefix. By default, it returns the name of default theme file.
     *
     * @param aPrefix        System Prefix
     * @param aDefaultTheme  Name of the default theme file.
     *
     * @return Name of the theme's filename for the requested BA.
     */
    public static String getThemeFile(String aPrefix, String aDefaultTheme) {
        String themeFile = aDefaultTheme;

        try {

            //
            // If the business area has a customized stylesheet, then the
            // filename is <SysPrefix>_<defaultThemeFileName>. Check if there
            // is any such file in the directory where we store all the
            // stylesheet-related stuff.
            //
            String baThemeFile = aPrefix.toLowerCase() + "_" + aDefaultTheme;
            File   f           = Configuration.findPath("web/css/" + baThemeFile);

            if (f == null) {

                // Since there is no such file, we shall use the defualt file.
                themeFile = aDefaultTheme;
            } else {

                // Since there is a stylesheet customized for this BA, use that
                themeFile = baThemeFile;
            }
        } catch (Exception e) {

            // Incase of any exceptions, we use the defualt file.
            themeFile = aDefaultTheme;
        }

        return themeFile;
    }

    //~--- set methods --------------------------------------------------------

    /*
     * UI REWORK DEMO FUNCTION
     * ##############################
     */
    public static void setCrossInstance(Hashtable<String, String> aTagTable) {

        // onmouseover handler.
        // String onMouseOver =
        // "this.T_STICKY = true; " +
        // //"this.T_FIX = [gal(this) + 15, gat(this) + 20];" +
        // "this.T_TEMP = 0; " +
        // "this.T_DELAY = 100; " +
        // "this.T_WIDTH  = '250px';" +
        // "this.T_BORDERWIDTH = 0;" +
        // "return escape(";
        String onMouseOver = "";
        String hydUrl      = PropertiesHandler.getProperty(KEY_HYDURL);
        String nycUrl      = PropertiesHandler.getProperty(KEY_NYCURL);

        if (PropertiesHandler.getProperty(KEY_DOMAIN).toLowerCase().equals("hyd")) {
            aTagTable.put("crossInstancePath", nycUrl + "/search");
            aTagTable.put("onMouseOverNyc", "onclick=\"" + onMouseOver + "showBAList" + "('" + nycUrl + "','search', 'nyc', event);\"" + " onmouseout=\"setMouseOutTimer();\" ");
            aTagTable.put("crossInstance", "NYC");
            aTagTable.put("crossInstanceTarget_search", "search_nyc");
        } else {
            aTagTable.put("crossInstancePath", hydUrl + "/search");
            aTagTable.put("onMouseOverNyc", "onclick=\"" + onMouseOver + "showBAList" + "('" + hydUrl + "','search', 'nyc', event);\"" + " onmouseout=\"setMouseOutTimer();\" ");
            aTagTable.put("crossInstance", "HYD");
            aTagTable.put("crossInstanceTarget_search", "search_hyd");
        }
    }

    /*
     */
    public static void setInstanceBold(Hashtable<String, String> aTagTable, String aSysPrefix) {

        // onmouseover handler.
        // String onMouseOver =
        // "this.T_STICKY = true; " +
        // "this.T_FIX = [gal(this) + 15, gat(this) + 20];" +
        // "this.T_TEMP = 0; " +
        // "this.T_DELAY = 100; " +
        // "this.T_WIDTH  = '250px';" +
        // "this.T_BORDERWIDTH = 0;" +
        // "return escape(";
        String onMouseOver = "";
        String hydUrl      = PropertiesHandler.getProperty(KEY_HYDURL);
        String nycUrl      = PropertiesHandler.getProperty(KEY_NYCURL);

        if (PropertiesHandler.getProperty(KEY_DOMAIN).toLowerCase().equals("hyd")) {
            aTagTable.put("instanceBoldHyd", " b ");
            aTagTable.put("instanceBoldNyc", " ");
            aTagTable.put("instancePathHyd", "/search/" + aSysPrefix);
            aTagTable.put("instancePathNyc", nycUrl + "/search");
            aTagTable.put("onMouseOverNyc", "onclick=\"" + onMouseOver + "showBAList" + "('" + nycUrl + "','search', 'nyc', event);\"" + " onmouseout=\"setMouseOutTimer();\" ");
            aTagTable.put("onMouseOverHyd", "onclick=\"" + onMouseOver + "showBAList" + "('" + hydUrl + "','search', 'hyd', event);\"" + " onmouseout=\"setMouseOutTimer();\" ");
            aTagTable.put("target_search", "search_hyd");
        } else {
            aTagTable.put("instanceBoldHyd", " ");
            aTagTable.put("instanceBoldNyc", " b ");
            aTagTable.put("instancePathHyd", hydUrl + "/search");
            aTagTable.put("instancePathNyc", "/search/" + aSysPrefix);
            aTagTable.put("onMouseOverHyd", "onclick=\"" + onMouseOver + "showBAList" + "('" + hydUrl + "','search', 'hyd');\"" + " onmouseout=\"setMouseOutTimer();\" ");
            aTagTable.put("onMouseOverNyc", "onclick=\"" + onMouseOver + "showBAList" + "('" + nycUrl + "','search', 'nyc');\"" + " onmouseout=\"setMouseOutTimer();\" ");
            aTagTable.put("target_search", "search_nyc");
        }
    }
    
    public static String prepareValidHtml(String str) throws IOException{
    	String html = "";
    	
    	HtmlCleaner cleaner = new HtmlCleaner();
    	TagNode node = cleaner.clean(str);
		List list = node.getChildren();
		TagNode body = (TagNode) list.get(1);
		
		TagNode[] anchors = body.getElementsByName("a", true);
		for(TagNode anchor : anchors){
			anchor.removeAttribute("target");
			anchor.addAttribute("target", "_blank");
		}
		
		html = prepareValidHtml(body);
		
		return html;
    }
    //TODO: extra space before the closing angle bracket of a tag: <br >
    //TODO: Optimize
    public static String prepareValidHtml(TagNode currentNode){
    	String html = "";
    	
    	String name = currentNode.getName();
    	
    	if(!name.toLowerCase().equals("body")){
	    	Map attributeMap = currentNode.getAttributes();
	    	String attributes = "";
	    	for(Object o : attributeMap.keySet()){
	    		String key = (String) o;
	    		String value = (String) attributeMap.get(o);
	    		attributes += key + "=\"" + value + "\" ";
	    	}
	    	
	    	html += "<" + name + " " + attributes + ">";
    	}
    	List childList = currentNode.getChildren();
		for(Object o : childList){
			if(o instanceof TagNode){
				currentNode = (TagNode) o;
				html += prepareValidHtml(currentNode);
			}else if(o instanceof ContentToken){
				ContentToken content = (ContentToken) o;
				String str = content.getContent();
				html += str;
			}
		}
		
		if(!name.equalsIgnoreCase("body") && !name.equalsIgnoreCase("br"))
			html += "</" + name + ">";
		return html;
    }

	public static boolean isClassicInterface()
	{
		Boolean isClassicInterface = false;
		try
		{
			String value = PropertiesHandler.getProperty(TBitsPropEnum.KEY_ISCLASSIC_UI);
			if(value != null)
				isClassicInterface = Boolean.parseBoolean(value);
		}
		catch(Exception exp)
		{
			Log.error("Unable to get the key: '" + TBitsPropEnum.KEY_ISCLASSIC_UI + "'. assuming it to be false.", exp);
		}
		return isClassicInterface;
	}
}
