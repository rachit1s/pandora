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
 * CustomReport.java
 *
 * $Header:
 */
package transbit.tbits.report;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.SysPrefixes;

//TBits Imports
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.TBitsInstance;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

import static transbit.tbits.Helper.TBitsConstants.PKG_REPORT;
import static transbit.tbits.common.PropertiesEnum.KEY_DB_LOGIN;
import static transbit.tbits.common.PropertiesEnum.KEY_DB_PASSWORD;

//Static imports
import static transbit.tbits.common.PropertiesEnum.KEY_DRIVER_NAME;
import static transbit.tbits.common.PropertiesEnum.KEY_DRIVER_TAG;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//~--- classes ----------------------------------------------------------------

/**
 * This servlet is used to generate custom reports
 *
 * @author  Vinod Gupta
 * @version $Id: $
 */
public class CustomReport extends HttpServlet {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_REPORT);

    // XML File that holds the attributes of instances.
    public static final String                     INSTANCES_XML  = "etc/instances.xml";
    private static final String                    HTML_FILE      = "web/tbits-custom-report.htm";
    private static final String                    CSS_FILE       = "web/css/tbits.css";
    public static String                           ourDriverClass = "";
    public static String                           ourDriverTag   = "";
    public static String                           ourDbUser      = "";
    public static String                           ourDbPass      = "";
    public static Hashtable<String, TBitsInstance> myInstances;

    //~--- static initializers ------------------------------------------------

    static {

        // Get the driver details from the properties handler.
        try {
            ourDriverClass = PropertiesHandler.getProperty(KEY_DRIVER_NAME);
            ourDriverTag   = PropertiesHandler.getProperty(KEY_DRIVER_TAG);
            ourDbUser      = PropertiesHandler.getProperty(KEY_DB_LOGIN);
            ourDbPass      = PropertiesHandler.getProperty(KEY_DB_PASSWORD);
        } catch (Exception e) {
            LOG.severe("Exception while retrieving the Driver details." + "",(e));
        }

        myInstances = loadInstances();
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method to check if user is a super-user or permission admin in
     * this instance.
     *
     * @param  aUserId   the UserId for which the RoleUser Objects needed
     *
     * @return boolean true if user has access.
     * @exception DatabaseException In case of any database related error
     */
    public static boolean checkUserPermissions(int aUserId) throws DatabaseException {
        boolean    authorized = false;
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            Statement stmt  = connection.createStatement();
            String    query = "select user_id from roles_users where user_id = " + aUserId + " and role_id = 10\n" + " union" + " \n select user_id from super_users where user_id = " + aUserId
                              + " and is_active = 1";
            ResultSet rs = stmt.executeQuery(query);

            if (rs != null) {
                while (rs.next()) {
                    authorized = true;

                    break;
                }
            }

            stmt.close();
            rs   = null;
            stmt = null;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.warning("Exception occured while closing the connection");
            }
        }

        return authorized;
    }

    /**
     * The doGet method of the servlet.
     *
     * @param  aHttpRequest          the HttpServlet Request Object
     * @param  aHttpResponse         the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse) throws ServletException, IOException {
        HttpSession session = aHttpRequest.getSession();

        Utilities.registerMDCParams(aHttpRequest);

        try {
            String format = aHttpRequest.getParameter("format");

            if ((format != null) && (format.trim().equals("excel") == true)) {
                handlePostRequest(aHttpRequest, aHttpResponse);
            } else {
                handleGetRequest(aHttpRequest, aHttpResponse);
            }
        } catch (TBitsException e) {
            LOG.info("User: " + aHttpRequest.getRemoteUser() + "\npathInfo: " + aHttpRequest.getPathInfo() + "\n" + "",(e));
            session.setAttribute("ExceptionObject", e);
            aHttpResponse.sendRedirect(WebUtil.getServletPath(aHttpRequest, "/error"));

            return;
        } catch (RuntimeException e) {
            LOG.severe("User: " + aHttpRequest.getRemoteUser() + "\npathInfo: " + aHttpRequest.getPathInfo() + "\n" + "",(e));
            session.setAttribute("ExceptionObject", e);
            aHttpResponse.sendRedirect(WebUtil.getServletPath(aHttpRequest, "/error"));

            return;
        } catch (Exception e) {
            LOG.severe("User: " + aHttpRequest.getRemoteUser() + "\npathInfo: " + aHttpRequest.getPathInfo() + "\n" + "",(e));
            session.setAttribute("ExceptionObject", e);
            aHttpResponse.sendRedirect(WebUtil.getServletPath(aHttpRequest, "/error"));

            return;
        } finally {
            Utilities.clearMDCParams();
        }
    }

    /**
     * The doPost method of the servlet.
     *
     * @param  aHttpRequest          the HttpServlet Request Object
     * @param  aHttpResponse         the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse) throws ServletException, IOException {
        HttpSession session = aHttpRequest.getSession();

        Utilities.registerMDCParams(aHttpRequest);

        try {
            Long start = System.currentTimeMillis();

            handlePostRequest(aHttpRequest, aHttpResponse);

            Long end = System.currentTimeMillis();

            LOG.debug("Time taken to process report: " + (end - start) + " mecs");
        } catch (TBitsException e) {
            LOG.info("User: " + aHttpRequest.getRemoteUser() + "\npathInfo: " + aHttpRequest.getPathInfo() + "\n" + "",(e));
            session.setAttribute("ExceptionObject", e);
            aHttpResponse.sendRedirect(WebUtil.getServletPath(aHttpRequest, "/error"));

            return;
        } catch (RuntimeException e) {
            LOG.severe("User: " + aHttpRequest.getRemoteUser() + "\npathInfo: " + aHttpRequest.getPathInfo() + "\n" + "",(e));
            session.setAttribute("ExceptionObject", e);
            aHttpResponse.sendRedirect(WebUtil.getServletPath(aHttpRequest, "/error"));

            return;
        } catch (Exception e) {
            LOG.severe("User: " + aHttpRequest.getRemoteUser() + "\npathInfo: " + aHttpRequest.getPathInfo() + "\n" + "",(e));
            session.setAttribute("ExceptionObject", e);
            aHttpResponse.sendRedirect(WebUtil.getServletPath(aHttpRequest, "/error"));

            return;
        } finally {
            Utilities.clearMDCParams();
        }
    }

    /**
     * Method that actually handles the Get Request.
     *
     *
     * @param aHttpRequest          the HttpServlet Request Object
     * @param aHttpResponse         the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     * @throws DESTBitsExceptionthrows Exception
     */
    public void handleGetRequest(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse) throws ServletException, IOException, TBitsException, Exception {
        aHttpResponse.setContentType("text/html");

        PrintWriter out = aHttpResponse.getWriter();

        //
        // Validate the user, get the user Object, Read the configuration.
        //
        User user = WebUtil.validateUser(aHttpRequest);

        if (user == null) {
            LOG.info("user null");

            return;
        }

        int userId = user.getUserId();

        // Accessible only to permission admins and super-users
        boolean authorized = checkUserPermissions(userId);

        if (authorized == false) {
            throw new TBitsException("You are not authorized to access this page.");
        }

        DTagReplacer hp = new DTagReplacer(HTML_FILE);

        hp.replace("nearestPath", WebUtil.getNearestPath(aHttpRequest, ""));
        hp.replace("cssFile", WebUtil.getCSSFile(null, "", false));
        hp.replace("baList", getBaList());
        out.println(hp.parse(0));

        return;
    }

    /**
     * Method that actually handles the Post Request.
     *
     *
     * @param aHttpRequest          the HttpServlet Request Object
     * @param aHttpResponse         the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     * @throws DESTBitsExceptionthrows Exception
     */
    public void handlePostRequest(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse) throws ServletException, IOException, TBitsException, Exception {
        aHttpResponse.setContentType("text/html");

        PrintWriter out = aHttpResponse.getWriter();

        //
        // Validate the user, get the user Object, Read the configuration.
        //
        User user = WebUtil.validateUser(aHttpRequest);

        if (user == null) {
            LOG.info("user null");

            return;
        }

        int userId = user.getUserId();

        // Accessible only to permission admins and super-users
        boolean authorized = checkUserPermissions(userId);

        if (authorized == false) {
            throw new TBitsException("You are not authorized to access this page.");
        }

        String reportTypeStr = aHttpRequest.getParameter("reportType");

        if ((reportTypeStr == null) || (reportTypeStr.trim().equals("") == true)) {
            LOG.info("Select a valid report type");
            out.println("Select a valid report type");

            return;
        }

        int reportType = 0;

        try {
            reportType = Integer.parseInt(reportTypeStr);
        } catch (Exception e) {
            LOG.info("Select a valid report type");
            out.println("Select a valid report type");

            return;
        }

        LOG.info("reportType : " + reportType);

        String output = "";

        switch (reportType) {
        case 1 :
            output = handleReport1();

            break;

        case 2 :
            output = handleReport2();

            break;

        case 3 :
            output = handleReport3();

            break;

        case 4 :
            output = handleReport4();

            break;

        case 5 :
            output = handleReport5();

            break;

        case 6 :
            output = handleReport6();

            break;

        case 7 :
            String sysPrefix = aHttpRequest.getParameter("sysPrefix");

            if ((sysPrefix == null) || (sysPrefix.trim().equals("") == true)) {
                LOG.info("Select a valid BA for this report");
                out.println("Select a valid report type");

                return;
            }

            output = handleReport7(sysPrefix);

            break;

        default :
            output = "";
        }

        String format = aHttpRequest.getParameter("format");

        if ((format != null) && (format.trim().equals("excel") == true)) {
            aHttpResponse.setContentType("application/vnd.ms-excel");

            DTagReplacer hp = new DTagReplacer(CSS_FILE);

            output = "<html>\n<head>\n<style>\n" + hp.parse(0) + "\n</style>\n</head>\n" + "\n<body>" + output + "\n</body>\n</html>";
        }

        out.println(output);

        return;
    }

    /*
     *
     */
    private String handleReport1() throws Exception {
        StringBuffer header = new StringBuffer();

        header.append("<Table class='results'>").append("<TR class='header'>").append("<TD class=\"b\">Business Area</TD>").append("<TD class=\"b\">").append("No.Of.Requests Un-Closed</TD>").append(
            "</TR>");

        StringBuffer aHtml = new StringBuffer();
        String       query = "select ba.sys_prefix, count(*) 'count' from" + " business_areas ba join types t on" + " t.sys_id = ba.sys_id and t.field_id = 4"
                             + " and (t.name not like 'close%' and t.name not like 'done%')" + " and t.is_active=1 and ba.is_active=1" + " join requests r on" + " ba.sys_id = r.sys_id"
                             + " and t.type_id = r.status_id " + " group by sys_prefix order by sys_prefix";

        LOG.info("ReportType-1 query: " + query);

        Connection connection = null;

        try {
            connection = getConnection("hyd");

            Statement stmt = connection.createStatement();
            ResultSet rs   = stmt.executeQuery(query);

            if ((rs != null) && (rs.next() != false)) {
                do {
                    String ba     = rs.getString("sys_prefix");
                    int    aCount = rs.getInt("count");

                    aHtml.append("<TR>\n").append("\t<TD>").append(ba).append("</TD>\n").append("\t<TD>").append(aCount).append("</TD>\n").append("</TR>\n");
                } while (rs.next() != false);
            }

            stmt.close();
            rs   = null;
            stmt = null;
            connection.close();
            connection = getConnection("nyc");
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery(query);

            if ((rs != null) && (rs.next() != false)) {
                do {
                    String ba     = rs.getString("sys_prefix");
                    int    aCount = rs.getInt("count");

                    aHtml.append("<TR>\n").append("\t<TD>").append(ba).append("</TD>\n").append("\t<TD>").append(aCount).append("</TD>\n").append("</TR>\n");
                } while (rs.next() != false);
            }

            stmt.close();
            rs   = null;
            stmt = null;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }

            connection = null;
        }

        if (aHtml.length() > 0) {
            header.append(aHtml.toString()).append("</table>");

            return header.toString();
        } else {
            return "";
        }
    }

    /*
     *
     */
    private String handleReport2() throws Exception {
        StringBuffer header = new StringBuffer();
        header.append("<Table class='results'>").append("<TR class='header'>").append("<TD class=\"b\">Business Area</TD>").append("<TD class=\"b\">").append("Assignee</TD>").append(
            "<TD class=\"b\">").append("No.Of.Requests Un-Closed</TD>").append("</TR>");
        StringBuffer aHtml = new StringBuffer();
        String       query = "select ba.sys_prefix," + " isNull(user_login, '-') 'user_login', count(*) 'count'" + " from business_areas ba join types t on"
                             + " t.sys_id = ba.sys_id and t.field_id = 4" + " and (t.name not like 'close%' and t.name not like 'done%')" + " and t.is_active=1 and ba.is_active=1"
                             + " join requests r on" + " ba.sys_id = r.sys_id" + " and t.type_id = r.status_id " + " left join request_users ru on"
                             + " r.sys_id = ru.sys_id and r.request_id = ru.request_id" + " and ru.user_type_id=3" + " left join users u on" + " ru.user_id = u.user_id"
                             + " group by sys_prefix, user_login" + " order by sys_prefix, user_login";

        LOG.info("ReportType-2 query: " + query);

        Connection connection = null;

        try {
            connection = getConnection("hyd");

            Statement stmt = connection.createStatement();
            ResultSet rs   = stmt.executeQuery(query);

            if ((rs != null) && (rs.next() != false)) {
                do {
                    String ba     = rs.getString("sys_prefix");
                    String login  = rs.getString("user_login");
                    int    aCount = rs.getInt("count");

                    aHtml.append("<TR>\n").append("\t<TD>").append(ba).append("</TD>\n").append("\t<TD>").append(login).append("</TD>\n").append("\t<TD>").append(aCount).append("</TD>\n").append(
                        "</TR>\n");
                } while (rs.next() != false);
            }

            stmt.close();
            rs   = null;
            stmt = null;
            connection.close();
            /*connection = getConnection("nyc");
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery(query);

            if ((rs != null) && (rs.next() != false)) {
                do {
                    String ba     = rs.getString("sys_prefix");
                    String login  = rs.getString("user_login");
                    int    aCount = rs.getInt("count");

                    aHtml.append("<TR>\n").append("\t<TD>").append(ba).append("</TD>\n").append("\t<TD>").append(login).append("</TD>\n").append("\t<TD>").append(aCount).append("</TD>\n").append(
                        "</TR>\n");
                } while (rs.next() != false);
            }

            stmt.close();
            rs   = null;
            stmt = null;*/
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }

            connection = null;
        }

        if (aHtml.length() > 0) {
            header.append(aHtml.toString()).append("</table>");

            return header.toString();
        } else {
            return "";
        }
    }

    /*
     *
     */
    private String handleReport3() throws Exception {
        Hashtable<String, Hashtable<Integer, Integer>> data  = new Hashtable<String, Hashtable<Integer, Integer>>();
        StringBuffer                                   aHtml = new StringBuffer();
        String                                         query = "select isNull(u.user_login, '-') 'user_login'," + " year(a.lastupdated_datetime) 'year', count(*) 'count'"
                                                               + " from actions a join users u on" + " a.user_id = u.user_id " + " group by u.user_login, year(a.lastupdated_datetime)"
                                                               + " order by u.user_login asc," + " year(a.lastupdated_datetime) desc";

        LOG.info("ReportType-3 query: " + query);

        Connection connection = null;
        int        maxYear    = 0;
        int        minYear    = 100000;

        try {
            connection = getConnection("hyd");

            Statement stmt = connection.createStatement();
            ResultSet rs   = stmt.executeQuery(query);

            if ((rs != null) && (rs.next() != false)) {
                do {
                    String login = rs.getString("user_login");
                    int    year  = rs.getInt("year");
                    int    count = rs.getInt("count");

                    maxYear = ((maxYear < year)
                               ? year
                               : maxYear);
                    minYear = ((minYear > year)
                               ? year
                               : minYear);

                    if (data.get(login) != null) {
                        Hashtable<Integer, Integer> temp = data.get(login);

                        temp.put(year, count);
                        data.put(login, temp);
                    } else {
                        Hashtable<Integer, Integer> temp = new Hashtable<Integer, Integer>();

                        temp.put(year, count);
                        data.put(login, temp);
                    }
                } while (rs.next() != false);
            }

            stmt.close();
            rs   = null;
            stmt = null;
            connection.close();
            connection = getConnection("nyc");
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery(query);

            if ((rs != null) && (rs.next() != false)) {
                do {
                    String login = rs.getString("user_login");
                    int    year  = rs.getInt("year");
                    int    count = rs.getInt("count");

                    maxYear = ((maxYear < year)
                               ? year
                               : maxYear);
                    minYear = ((minYear > year)
                               ? year
                               : minYear);

                    if (data.get(login) != null) {
                        Hashtable<Integer, Integer> temp = data.get(login);

                        if (temp.get(year) != null) {
                            int c1 = temp.get(year);

                            temp.put(year, count + c1);
                        } else {
                            temp.put(year, count);
                        }

                        data.put(login, temp);
                    } else {
                        Hashtable<Integer, Integer> temp = new Hashtable<Integer, Integer>();

                        temp.put(year, count);
                        data.put(login, temp);
                    }
                } while (rs.next() != false);
            }

            stmt.close();
            rs   = null;
            stmt = null;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }

            connection = null;
        }

        if (data.size() > 0) {
            StringBuffer header = new StringBuffer();

            header.append("<Table class='results'>").append("<TR class='header'>").append("<TD class=\"b\">USER</TD>");

            for (int i = maxYear; i >= minYear; i--) {
                header.append("<TD class=\"b\">").append(i).append("</TD>");
            }

            header.append("</TR>");

            Enumeration<String> e    = data.keys();
            int                 size = data.size();
            String[]            keys = new String[size];
            int                 j    = 0;

            while (e.hasMoreElements()) {
                String str = e.nextElement();

                keys[j] = str;
                j++;
            }

            Arrays.sort(keys, String.CASE_INSENSITIVE_ORDER);

            for (String login : keys) {
                Hashtable<Integer, Integer> temp = data.get(login);

                aHtml.append("<TR>\n").append("\t<TD>").append(login).append("</TD>\n");

                for (int i = maxYear; i >= minYear; i--) {
                    String count = (temp.get(i) == null)
                                   ? "-"
                                   : "" + temp.get(i);

                    aHtml.append("<TD>").append(count).append("</TD>");
                }

                aHtml.append("</TR>\n");
            }

            header.append(aHtml.toString()).append("</table>");

            return header.toString();
        } else {
            return "";
        }
    }

    /*
     *
     */
    private String handleReport4() throws Exception {
        Hashtable<String, Hashtable<String, Integer>> data  = new Hashtable<String, Hashtable<String, Integer>>();
        StringBuffer                                  aHtml = new StringBuffer();
        String                                        query = "select isNull(u.user_login, '-') 'user_login'," + " ba.sys_prefix, count(*) 'count'" + " from business_areas ba join actions a"
                                                              + " on ba.sys_id = a.sys_id" + " join users u on" + " a.user_id = u.user_id " + " where year(a.lastupdated_datetime) = 2007"
                                                              + " group by u.user_login,ba.sys_prefix " + " order by u.user_login, ba.sys_prefix ";

        LOG.info("ReportType-4 query: " + query);

        Connection        connection = null;
        ArrayList<String> baPrefixes = new ArrayList<String>();

        try {
            connection = getConnection("hyd");

            Statement stmt = connection.createStatement();
            ResultSet rs   = stmt.executeQuery(query);

            if ((rs != null) && (rs.next() != false)) {
                do {
                    String login  = rs.getString("user_login");
                    String prefix = rs.getString("sys_prefix");
                    int    count  = rs.getInt("count");

                    if (baPrefixes.contains(prefix) == false) {
                        baPrefixes.add(prefix);
                    }

                    if (data.get(login) != null) {
                        Hashtable<String, Integer> temp = data.get(login);

                        temp.put(prefix, count);
                        data.put(login, temp);
                    } else {
                        Hashtable<String, Integer> temp = new Hashtable<String, Integer>();

                        temp.put(prefix, count);
                        data.put(login, temp);
                    }
                } while (rs.next() != false);
            }

            stmt.close();
            rs   = null;
            stmt = null;
            connection.close();
            connection = getConnection("nyc");
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery(query);

            if ((rs != null) && (rs.next() != false)) {
                do {
                    String login  = rs.getString("user_login");
                    String prefix = rs.getString("sys_prefix");
                    int    count  = rs.getInt("count");

                    if (baPrefixes.contains(prefix) == false) {
                        baPrefixes.add(prefix);
                    }

                    if (data.get(login) != null) {
                        Hashtable<String, Integer> temp = data.get(login);

                        temp.put(prefix, count);
                        data.put(login, temp);
                    } else {
                        Hashtable<String, Integer> temp = new Hashtable<String, Integer>();

                        temp.put(prefix, count);
                        data.put(login, temp);
                    }
                } while (rs.next() != false);
            }

            stmt.close();
            rs   = null;
            stmt = null;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }

            connection = null;
        }

        if (data.size() > 0) {
            StringBuffer header = new StringBuffer();

            header.append("<Table class='results'>").append("<TR class='header'>").append("<TD class=\"b\">USER</TD>").append("<TD class=\"b\">2007</TD>");

            for (String str : baPrefixes) {
                header.append("<TD class=\"b\">").append(str).append("</TD>");
            }

            header.append("</TR>");

            Enumeration<String> e    = data.keys();
            int                 size = data.size();
            String[]            keys = new String[size];
            int                 j    = 0;

            while (e.hasMoreElements()) {
                String str = e.nextElement();

                keys[j] = str;
                j++;
            }

            Arrays.sort(keys, String.CASE_INSENSITIVE_ORDER);

            for (String login : keys) {
                Hashtable<String, Integer> temp   = data.get(login);
                StringBuffer               tempSb = new StringBuffer();
                int                        total  = 0;

                for (String str : baPrefixes) {
                    String count = (temp.get(str) == null)
                                   ? "-"
                                   : "" + temp.get(str);

                    if (count.equals("-") == false) {
                        total += Integer.parseInt(count);
                    }

                    tempSb.append("<TD>").append(count).append("</TD>");
                }

                aHtml.append("<TR>\n").append("\t<TD>").append(login).append("</TD>\n").append("\t<TD>").append(total).append("</TD>\n");
                aHtml.append(tempSb);
                aHtml.append("</TR>\n");
            }

            header.append(aHtml.toString()).append("</table>");

            return header.toString();
        } else {
            return "";
        }
    }

    /*
     *
     */
    private String handleReport5() throws Exception {
        Hashtable<String, Hashtable<Integer, String>> data  = new Hashtable<String, Hashtable<Integer, String>>();
        StringBuffer                                  aHtml = new StringBuffer();
        String                                        query = "select r.sys_id, r.request_id, r.logged_datetime," + "1/CAST(count(*) as float) 'weight' into tmp_requests"
                                                              + " from requests r join request_users ru on" + " r.sys_id = ru.sys_id and r.request_id = ru.request_id" + " where ru.user_type_id = 2 "
                                                              + " group by r.sys_id, r.request_id, r.logged_datetime" + " \nselect isNull(u.user_login, '-') 'user_login',"
                                                              + " year(r.logged_datetime) 'year', " + " str(sum(weight),20,2) 'count'" + " from tmp_requests r join request_users ru on"
                                                              + " r.sys_id = ru.sys_id and r.request_id = ru.request_id" + " join users u on" + " ru.user_id = u.user_id "
                                                              + " where ru.user_type_id = 2 " + " group by u.user_login, year(r.logged_datetime)" + " order by u.user_login asc,"
                                                              + " year(r.logged_datetime) desc" + " \ndrop table tmp_requests";

        LOG.info("ReportType-5 query: " + query);

        Connection connection = null;
        int        maxYear    = 0;
        int        minYear    = 100000;

        try {
            connection = getConnection("hyd");

            Statement stmt = connection.createStatement();
            ResultSet rs   = stmt.executeQuery(query);

            if ((rs != null) && (rs.next() != false)) {
                do {
                    String login = rs.getString("user_login");
                    int    year  = rs.getInt("year");
                    String count = rs.getString("count").trim();

                    if (count.endsWith(".00") == true) {
                        count = count.substring(0, count.indexOf(".00"));
                    }

                    maxYear = ((maxYear < year)
                               ? year
                               : maxYear);
                    minYear = ((minYear > year)
                               ? year
                               : minYear);

                    if (data.get(login) != null) {
                        Hashtable<Integer, String> temp = data.get(login);

                        temp.put(year, count);
                        data.put(login, temp);
                    } else {
                        Hashtable<Integer, String> temp = new Hashtable<Integer, String>();

                        temp.put(year, count);
                        data.put(login, temp);
                    }
                } while (rs.next() != false);
            }

            stmt.close();
            rs   = null;
            stmt = null;
            connection.close();
            connection = getConnection("nyc");
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery(query);

            if ((rs != null) && (rs.next() != false)) {
                do {
                    String login = rs.getString("user_login");
                    int    year  = rs.getInt("year");
                    String count = rs.getString("count").trim();

                    if (count.endsWith(".00") == true) {
                        count = count.substring(0, count.indexOf(".00"));
                    }

                    maxYear = ((maxYear < year)
                               ? year
                               : maxYear);
                    minYear = ((minYear > year)
                               ? year
                               : minYear);

                    if (data.get(login) != null) {
                        Hashtable<Integer, String> temp = data.get(login);

                        if (temp.get(year) != null) {
                            String c1    = temp.get(year);
                            Double total = Double.parseDouble(c1) + Double.parseDouble(count);

                            temp.put(year, "" + total);
                        } else {
                            temp.put(year, count);
                        }

                        data.put(login, temp);
                    } else {
                        Hashtable<Integer, String> temp = new Hashtable<Integer, String>();

                        temp.put(year, count);
                        data.put(login, temp);
                    }
                } while (rs.next() != false);
            }

            stmt.close();
            rs   = null;
            stmt = null;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }

            connection = null;
        }

        if (data.size() > 0) {
            StringBuffer header = new StringBuffer();

            header.append("<Table class='results'>").append("<TR class='header'>").append("<TD class=\"b\">USER</TD>");

            for (int i = maxYear; i >= minYear; i--) {
                header.append("<TD class=\"b\">").append(i).append("</TD>");
            }

            header.append("</TR>");

            Enumeration<String> e    = data.keys();
            int                 size = data.size();
            String[]            keys = new String[size];
            int                 j    = 0;

            while (e.hasMoreElements()) {
                String str = e.nextElement();

                keys[j] = str;
                j++;
            }

            Arrays.sort(keys, String.CASE_INSENSITIVE_ORDER);

            for (String login : keys) {
                Hashtable<Integer, String> temp = data.get(login);

                aHtml.append("<TR>\n").append("\t<TD>").append(login).append("</TD>\n");

                for (int i = maxYear; i >= minYear; i--) {
                    String count = (temp.get(i) == null)
                                   ? "-"
                                   : "" + temp.get(i);

                    aHtml.append("<TD>").append(count).append("</TD>");
                }

                aHtml.append("</TR>\n");
            }

            header.append(aHtml.toString()).append("</table>");

            return header.toString();
        } else {
            return "";
        }
    }

    /*
     *
     */
    private String handleReport6() throws Exception {
        Hashtable<String, Hashtable<String, String>> data  = new Hashtable<String, Hashtable<String, String>>();
        StringBuffer                                 aHtml = new StringBuffer();
        String                                       query = "select r.sys_id, r.request_id, r.logged_datetime," + "1/CAST(count(*) as float) 'weight' into tmp_requests"
                                                             + " from requests r join request_users ru on" + " r.sys_id = ru.sys_id and r.request_id = ru.request_id" + " where ru.user_type_id = 2 "
                                                             + " group by r.sys_id, r.request_id, r.logged_datetime" + " \nselect isNull(u.user_login, '-') 'user_login',"
                                                             + " ba.sys_prefix ,  str(sum(weight),20,2) 'count'" + " from business_areas ba join tmp_requests r " + " on ba.sys_id = r.sys_id"
                                                             + " join request_users ru on" + " r.sys_id = ru.sys_id and r.request_id = ru.request_id" + " join users u on" + " ru.user_id = u.user_id "
                                                             + " where ru.user_type_id = 2 and year(r.logged_datetime) = 2007" + " group by u.user_login, ba.sys_prefix"
                                                             + " order by u.user_login, ba.sys_prefix asc" + " \ndrop table tmp_requests";

        LOG.info("ReportType-6 query: " + query);

        Connection        connection = null;
        ArrayList<String> baPrefixes = new ArrayList<String>();

        try {
            connection = getConnection("hyd");

            Statement stmt = connection.createStatement();
            ResultSet rs   = stmt.executeQuery(query);

            if ((rs != null) && (rs.next() != false)) {
                do {
                    String login  = rs.getString("user_login");
                    String prefix = rs.getString("sys_prefix");
                    String count  = rs.getString("count").trim();

                    if (count.endsWith(".00") == true) {
                        count = count.substring(0, count.indexOf(".00"));
                    }

                    if (baPrefixes.contains(prefix) == false) {
                        baPrefixes.add(prefix);
                    }

                    if (data.get(login) != null) {
                        Hashtable<String, String> temp = data.get(login);

                        temp.put(prefix, count);
                        data.put(login, temp);
                    } else {
                        Hashtable<String, String> temp = new Hashtable<String, String>();

                        temp.put(prefix, count);
                        data.put(login, temp);
                    }
                } while (rs.next() != false);
            }

            stmt.close();
            rs   = null;
            stmt = null;
            connection.close();
            connection = getConnection("nyc");
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery(query);

            if ((rs != null) && (rs.next() != false)) {
                do {
                    String login  = rs.getString("user_login");
                    String prefix = rs.getString("sys_prefix");
                    String count  = rs.getString("count").trim();

                    if (count.endsWith(".00") == true) {
                        count = count.substring(0, count.indexOf(".00"));
                    }

                    if (baPrefixes.contains(prefix) == false) {
                        baPrefixes.add(prefix);
                    }

                    if (data.get(login) != null) {
                        Hashtable<String, String> temp = data.get(login);

                        temp.put(prefix, count);
                        data.put(login, temp);
                    } else {
                        Hashtable<String, String> temp = new Hashtable<String, String>();

                        temp.put(prefix, count);
                        data.put(login, temp);
                    }
                } while (rs.next() != false);
            }

            stmt.close();
            rs   = null;
            stmt = null;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }

            connection = null;
        }

        if (data.size() > 0) {
            StringBuffer header = new StringBuffer();

            header.append("<Table class='results'>").append("<TR class='header'>").append("<TD class=\"b\">USER</TD>").append("<TD class=\"b\">2007</TD>");

            for (String str : baPrefixes) {
                header.append("<TD class=\"b\">").append(str).append("</TD>");
            }

            header.append("</TR>");

            Enumeration<String> e    = data.keys();
            int                 size = data.size();
            String[]            keys = new String[size];
            int                 j    = 0;

            while (e.hasMoreElements()) {
                String str = e.nextElement();

                keys[j] = str;
                j++;
            }

            Arrays.sort(keys, String.CASE_INSENSITIVE_ORDER);

            for (String login : keys) {
                Hashtable<String, String> temp   = data.get(login);
                StringBuffer              tempSb = new StringBuffer();
                int                       total  = 0;

                for (String str : baPrefixes) {
                    String count = (temp.get(str) == null)
                                   ? "-"
                                   : "" + temp.get(str);

                    if (count.equals("-") == false) {
                        total += Double.parseDouble(count);
                    }

                    tempSb.append("<TD>").append(count).append("</TD>");
                }

                aHtml.append("<TR>\n").append("\t<TD>").append(login).append("</TD>\n").append("\t<TD>").append(total).append("</TD>\n");
                aHtml.append(tempSb);
                aHtml.append("</TR>\n");
            }

            header.append(aHtml.toString()).append("</table>");

            return header.toString();
        } else {
            return "";
        }
    }

    /*
     *
     */
    private String handleReport7(String aSysPrefix) throws Exception {
        StringBuffer header = new StringBuffer();

        header.append("<Table class='results'>").append("<TR class='header'>").append("<TD class=\"b\">Assignee</TD>").append("<TD class=\"b\">").append("No.Of.Requests Closed</TD>").append("</TR>");

        StringBuffer aHtml      = new StringBuffer();
        Connection   connection = null;

        try {
            String location = SysPrefixes.getLocation(aSysPrefix);

            connection = getConnection(location);

            CallableStatement stmt = connection.prepareCall("stp_customreport_request_closed_yearly ?, ?");

            stmt.setString(1, aSysPrefix);
            stmt.setInt(2, 2007);

            ResultSet rs = stmt.executeQuery();

            if ((rs != null) && (rs.next() != false)) {
                do {
                    String ba    = rs.getString("user_login");
                    String count = rs.getString("count");

                    if (count.endsWith(".00") == true) {
                        count = count.substring(0, count.indexOf(".00"));
                    }

                    aHtml.append("<TR>\n").append("\t<TD>").append(ba).append("</TD>\n").append("\t<TD>").append(count).append("</TD>\n").append("</TR>\n");
                } while (rs.next() != false);
            }

            stmt.close();
            rs   = null;
            stmt = null;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }

            connection = null;
        }

        if (aHtml.length() > 0) {
            header.append(aHtml.toString()).append("</table>");

            return header.toString();
        } else {
            return "";
        }
    }

    private static Hashtable<String, TBitsInstance> loadInstances() {
        String xml = "";

        try {
            xml = getInstancesXML();
        } catch (Exception e) {
            LOG.error("Exception while reading the instances.xml file: " + "",(e));
            return null;
        }

        LOG.debug("Read instances.xml....");

        /*
         *      Parse the XML that contains the details of the instances.
         */
        ArrayList<TBitsInstance> list = null;

        try {
            list = TBitsInstance.xmlDeSerialize(xml);
        } catch (Exception e) {
            LOG.error("Exception while de-serializing the XML:" + "",(e));
            return null;
        }

        LOG.debug("Parsed the xml data....");

        Hashtable<String, TBitsInstance> map = new Hashtable<String, TBitsInstance>();

        for (TBitsInstance dI : list) {
            map.put(dI.getLocation().toLowerCase(), dI);
        }

        return map;
    }

    //~--- get methods --------------------------------------------------------

    private String getBaList() {
        ArrayList<String> baList = SysPrefixes.getBAList();

        if ((baList == null) || (baList.size() == 0)) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();

        for (String str : baList) {
            String[] arr    = str.split("\n");
            String   name   = arr[0].trim();
            String   prefix = arr[1].trim();

            buffer.append("\n<OPTION value='").append(prefix).append("'>").append(name).append(" [").append(prefix).append("]").append("</OPTION>");
        }

        return buffer.toString();
    }

    /*
     *
     */
    private Connection getConnection(String aSite) throws Exception {
//        TBitsInstance dI = myInstances.get(aSite.toLowerCase());
//
//        if (dI == null) {
//            return null;
//        }
        return DataSourcePool.getConnection();
       //return DataSourcePool.getConnection(dI.getDBServer(), dI.getDBName(), ourDbUser, ourDbPass, ourDriverClass, ourDriverTag);
    }

    /**
     * This method read the instances.xml file and returns the content xml.
     *
     * @return XML content from instances.xml file.
     */
    private static String getInstancesXML() throws Exception {
        StringBuffer   buffer = new StringBuffer();
        File           file   = Configuration.findPath(INSTANCES_XML);
        BufferedReader br     = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String         str    = "";

        while ((str = br.readLine()) != null) {
            buffer.append(str).append("\n");
        }

        br.close();

        return buffer.toString();
    }
}
