/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transrecbit Technologies Pvt. Ltd.
 */



/*
 * GroupedRequestSummary.java
 */
package transbit.tbits.report;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DTagReplacer;

//TBits Imports.
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.Mail;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.UserType;
import transbit.tbits.webapps.WebUtil;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_REPORT;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//~--- classes ----------------------------------------------------------------

/**
 * This class generates a summary of requests attended during a given period.
 *
 * @author  : Vaibhav
 * @version : $Id: $
 */
public class GroupedRequestSummary extends HttpServlet {
    private static final String REPORT_TEMPLATE  = "web/tbits-summary-report.htm";
    private static final String REPORTS_CSS_FILE = "web/css/tbits_report.css";

    // Logger to log the information/error messages to the application log.
    private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_REPORT);

    //~--- fields -------------------------------------------------------------

    private int myStartCount = 0;
    private int myEndCount   = 0;

    //~--- methods ------------------------------------------------------------

    /*
     * This function reinitializes the class level variables.
     *
     */
    private void ReInit() {
        myStartCount = 0;
        myEndCount   = 0;
    }

    /**
     * This method services the Http-Get Requests for this Servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        ReInit();

        StringBuffer log = new StringBuffer();
        PrintWriter  out = aResponse.getWriter();

        aResponse.setContentType("text/html");

        String queryString = aRequest.getQueryString();

        if ((queryString != null) && (queryString.equalsIgnoreCase("help") == true)) {
            out.println(getHelp());

            return;
        }

        String       strSystemId   = aRequest.getParameter("business_area");
        String       strAddress    = aRequest.getParameter("address");
        String       strFromAddr   = aRequest.getParameter("fromAddress");
        String       strReportType = aRequest.getParameter("reporttype");
        String       strStartDate  = aRequest.getParameter("startDate");
        String       strEndDate    = aRequest.getParameter("endDate");
        StringBuffer output        = new StringBuffer();
        int          reportType    = 1;

        // Process the systemId parameter.
        int systemId = 0;

        try {
            systemId = Integer.parseInt(strSystemId);
        } catch (Exception e) {
            out.println("Invalid Business Area ID: " + strSystemId);

            return;
        }

        BusinessArea ba = null;

        try {
            ba = BusinessArea.lookupBySystemId(systemId);
        } catch (Exception e) {}

        if (ba == null) {
            out.println("Incorrect Business Area ID: " + systemId);

            return;
        }

        // Process the Recipient Address.
        if ((strAddress == null) || strAddress.trim().equals("")) {
            out.println("Specify a valid recipient address.");

            return;
        }

        // Process the strFromAddr
        if ((strFromAddr == null) || strFromAddr.trim().equals("")) {
            output.append("Business Area's Email Address is taken as From.");
            strFromAddr = ba.getEmail();

            int commaIndex = strFromAddr.indexOf(',');

            if (commaIndex > 0) {
                strFromAddr = strFromAddr.substring(0, commaIndex);
                LOG.info("From Address: " + strFromAddr);
            }
        }

        if ((strReportType == null) || strReportType.trim().equals("")) {
            reportType = 1;
        } else if (strReportType.trim().equalsIgnoreCase("daily")) {
            reportType = 1;
        } else if (strReportType.trim().equalsIgnoreCase("periodic")) {
            reportType = 2;
        } else {
            reportType = 1;
        }

        if (reportType == 1) {
            strStartDate = getToday("yyyyMMdd") + "000000";
            strEndDate   = getToday("yyyyMMdd") + "235959";
        } else {
            if ((strStartDate == null) || strStartDate.trim().equals("")) {
                strStartDate = getToday("yyyyMMdd") + "000000";
            } else {
                try {
                    strStartDate = strStartDate + "000000";
                } catch (Exception e) {
                    output.append("Start Date should be specified in MM/dd/yyyy").append(" format. Today is taken as default.");
                    strStartDate = getToday("yyyyMMdd") + "000000";
                }
            }

            if ((strEndDate == null) || strEndDate.trim().equals("")) {
                strEndDate = getToday("yyyyMMdd") + "235959";
            } else {
                try {
                    strEndDate = strEndDate + "235959";
                } catch (Exception e) {
                    output.append("End Date should be specified in MM/dd/yyyy").append(" format. Today is taken as default.");
                    strEndDate = getToday("yyyyMMdd") + "235959";
                }
            }
        }

        String report = prepareReport(aRequest, ba, strStartDate, strEndDate, strAddress, strFromAddr, output, reportType);

        if (report != null) {
            out.println(report);

            if (!output.toString().trim().equals("")) {
                out.println(output.toString());
            }
        }

        LOG.info(log.toString());

        return;
    }

    /**
     * This method services the Http-Post Requests for this Servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        ReInit();
        doGet(aRequest, aResponse);
    }

    public String prepareReport(HttpServletRequest aRequest, BusinessArea aBusinessArea, String aStartDate, String aEndDate, String aRecipient, String aSender, StringBuffer aOutput, int aReportType) {
        Connection con = null;

        try {
            int          aSystemId = aBusinessArea.getSystemId();
            DTagReplacer hp        = new DTagReplacer(REPORT_TEMPLATE);

            con = DataSourcePool.getConnection();

            if (con == null) {
                aOutput.append("Connection could not be established");

                return null;
            }

            StringBuffer report = new StringBuffer();
            Timestamp    aStart = new Timestamp(aStartDate);
            Timestamp    aEnd   = new Timestamp(aEndDate);

            hp.replace("systemName", aBusinessArea.getDisplayName());
            hp.replace("sysPrefix", aBusinessArea.getSystemPrefix());
            hp.replace("startDate", aStart.toCustomFormat("yyyyMMdd H:mm"));
            hp.replace("endDate", aEnd.toCustomFormat("yyyyMMdd H:mm"));

            /*
             *  hp.replace
             *   ("assigneeSummary",
             *    getUserReport
             *    (con, aSystemId, aStart, aEnd, UserType.ASSIGNEE));
             */

            /*
             * hp.replace
             *   ("loggerSummary",
             *    getUserReport
             *    (con, aSystemId, aStart, aEnd, UserType.LOGGER));
             */
            getFieldReport(con, aSystemId, aStart, aEnd, hp);

            /*
             *  hp.replace
             *   ("statusSummary",
             *    getFieldReport
             *    (con, aSystemId, aStart, aEnd, 4));
             *
             * hp.replace
             *   ("categorySummary",
             *    getFieldReport
             *    (con, aSystemId, aStart, aEnd, 3));
             *
             * hp.replace
             *   ("typeSummary",
             *    getFieldReport
             *    (con, aSystemId, aStart, aEnd, 6));
             *
             * hp.replace
             *   ("severitySummary",
             *    getFieldReport
             *    (con, aSystemId, aStart, aEnd, 5));
             */
            hp.replace("startCount", Integer.toString(myStartCount));
            hp.replace("endCount", Integer.toString(myEndCount));

            StringBuffer closedHtml = new StringBuffer();
            int          closeCount = getClosedCount(con, aSystemId, aStart, aEnd, closedHtml);
            int          openCount  = getOpenCount(con, aSystemId, aStart, aEnd);
            String       homepage   = WebUtil.getServletPath(aRequest, "search/" + aBusinessArea.getSystemPrefix());

            hp.replace("homeLink", homepage);
            hp.replace("closeSummary", closedHtml.toString());
            hp.replace("openCount", Integer.toString(openCount));
            hp.replace("closeCount", Integer.toString(closeCount));

            DTagReplacer css = new DTagReplacer(REPORTS_CSS_FILE);

            hp.replace("cssContent", css.parse(aBusinessArea.getSystemId()));

            String subject = null;

            if (aReportType == 2) {
                subject = "Request Summary for " + aBusinessArea.getDisplayName() + " for period between " + aStart.toCustomFormat("yyyyMMdd") + " and " + aEnd.toCustomFormat("yyyyMMdd");
            } else {
                subject = "Request Summary for " + aBusinessArea.getDisplayName() + " on " + aStart.toCustomFormat("yyyyMMdd");
            }

            Mail.sendWithHtml(aRecipient, aSender, subject, hp.parse(aBusinessArea.getSystemId()));

            return "Mails Send.";
        } catch (Exception e) {
            aOutput.append(e.toString());
            LOG.info("",(e));

            return null;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException sqle) {
                    LOG.info("",(sqle));
                }
            }
        }
    }

    //~--- get methods --------------------------------------------------------

    public int getClosedCount(Connection aConnection, int aSystemId, Timestamp aStartDate, Timestamp aEndDate, StringBuffer aHtml) {
        int closeCount = 0;

        try {
            CallableStatement cs = aConnection.prepareCall("stp_report_request_closed_between ?, ?, ?");

            LOG.info("stp_report_requests_closed_between " + aSystemId + ", '" + aStartDate.toGmtTimestamp().toCustomFormat("yyyy-MM-dd HH:mm:ss") + "', '"
                     + aEndDate.toGmtTimestamp().toCustomFormat("yyyy-MM-dd HH:mm:ss") + "'");
            cs.setInt(1, aSystemId);
            cs.setTimestamp(2, aStartDate.toGmtTimestamp().toSqlTimestamp());
            cs.setTimestamp(3, aEndDate.toGmtTimestamp().toSqlTimestamp());

            ResultSet rs = cs.executeQuery();

            if ((rs != null) && (rs.next() != false)) {
                aHtml.append("<TR><TD colspan=\"4\"></TD><TR>").append("<TR>").append("<TD class=\"chart-title\">Closed By</TD>").append("<TD class=\"chart-title\" colspan=\"3\">").append(
                    "No.Of.Requests Closed</TD>").append("</TR>");

                do {
                    String aLogin = rs.getString(1);
                    int    aCount = rs.getInt(2);

                    closeCount += aCount;
                    aHtml.append("<TR>\n").append("\t<TD>").append(aLogin).append("</TD>\n").append("\t<TD colspan=\"3\">").append(aCount).append("</TD>\n").append("</TR>\n");
                } while (rs.next() != false);
            } else {
                closeCount = 0;
            }

            cs.close();
        } catch (Exception e) {
            LOG.info("",(e));
        }

        return closeCount;
    }

    public String getFieldReport(Connection aConnection, int aSystemId, Timestamp aStartDate, Timestamp aEndDate, DTagReplacer aHp) {
        StringBuffer report = new StringBuffer();

        try {
            CallableStatement cs = aConnection.prepareCall("stp_report_getUnclosedRequestsByField ?, ?, ?");

            LOG.info("stp_report_getUnclosedRequestsByField " + aSystemId + ", '" + aStartDate.toGmtTimestamp().toCustomFormat("yyyy-MM-dd HH:mm:ss") + "', '"
                     + aEndDate.toGmtTimestamp().toCustomFormat("yyyy-MM-dd HH:mm:ss"));
            cs.setInt(1, aSystemId);
            cs.setTimestamp(2, aStartDate.toGmtTimestamp().toSqlTimestamp());
            cs.setTimestamp(3, aEndDate.toGmtTimestamp().toSqlTimestamp());

            // cs.setInt(4, aFieldId);
            boolean flag = cs.execute();

            // If the output of the stored procedure is not a result set,
            // return the empty output string.
            if ((flag == false) && (cs.getUpdateCount() == -1)) {
                return report.toString();
            }

            // cs.getMoreResults();
            while ((cs.getMoreResults() == false) && (cs.getUpdateCount() != -1));

            ResultSet rs1 = cs.getResultSet();

            aHp.replace("categorySummary", getReportHtml(rs1, "Category"));
            cs.getMoreResults();

            ResultSet rs2 = cs.getResultSet();

            aHp.replace("statusSummary", getReportHtml(rs2, "Status"));
            cs.getMoreResults();

            ResultSet rs3 = cs.getResultSet();

            aHp.replace("severitySummary", getReportHtml(rs3, "Severity"));
            cs.getMoreResults();

            ResultSet rs4 = cs.getResultSet();

            aHp.replace("typeSummary", getReportHtml(rs4, "Request Type"));
            aHp.replace("loggerSummary", getUserReportHtml(cs, 2));
            aHp.replace("assigneeSummary", getUserReportHtml(cs, 3));
            cs.close();
        } catch (Exception e) {
            LOG.info("",(e));
        }

        return report.toString();
    }

    private String getHelp() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("<table align=center border=0 style='font-family: ").append("trebuchet MS;font-size: 13px; background-color: ").append(
            "mintcream'><tr style='color: red;background-color: ").append("peachpuff'><td colspan='3'>Parameter &nbsp;&nbsp;&nbsp;").append(
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Description</td></tr><tr>").append("<td align=right>business_area</td><td>:</td><td>ID of ").append(
            "the business area.</td></tr><tr><td align=right>address").append("</td><td>:</td>     <td>Comma-separated list of ").append("recipients</td></tr><tr><td align=right>fromAddress").append(
            "</td><td>:</td>     <td>Sender's Address. Default value ").append("is BusinessArea's email address.</td></tr><tr><td ").append(
            "align=right>reporttype</td><td>:</td><td>daily/periodic.").append(" Default is daily</td></tr><tr><td align=right>startDate").append(
            "</td><td>:</td><td>starting date in yyyyMMdd format. ").append("Relevant only for periodic reports.</td></tr><tr><td ").append(
            "align=right>endDate</td><td>:</td><td>ending date in ").append("yyyyMMdd format. Relevant only for periodic reports.").append("</td></tr></table>");

        return buffer.toString();
    }

    public int getOpenCount(Connection aConnection, int aSystemId, Timestamp aStartDate, Timestamp aEndDate) {
        int openCount = 0;

        try {
            CallableStatement cs = aConnection.prepareCall("stp_report_getTotalOpened ?, ?, ?");

            LOG.info("stp_report_getTotalOpened " + aSystemId + ", '" + aStartDate.toGmtTimestamp().toCustomFormat("yyyy-MM-dd HH:mm:ss") + "', '"
                     + aEndDate.toGmtTimestamp().toCustomFormat("yyyy-MM-dd HH:mm:ss") + "'");
            cs.setInt(1, aSystemId);
            cs.setTimestamp(2, aStartDate.toGmtTimestamp().toSqlTimestamp());
            cs.setTimestamp(3, aEndDate.toGmtTimestamp().toSqlTimestamp());

            ResultSet rs = cs.executeQuery();

            if ((rs != null) && (rs.next() != false)) {
                openCount = rs.getInt(1);
                rs.close();
            } else {
                LOG.info("Procedure returned an empty/null resultset.");
                openCount = 0;
            }

            cs.close();
        } catch (Exception e) {
            LOG.info("",(e));
        }

        return openCount;
    }

    public String getReportHtml(ResultSet rs, String fieldName) throws SQLException {
        StringBuilder report = new StringBuilder();

        report.append("<TR><TD colspan=\"4\"></TD><TR>").append("<TD class=\"chart-title\">By ").append(fieldName).append("</TD>").append("<TD class=\"chart-title\">Start</TD>").append(
            "<TD class=\"chart-title\">End</TD>").append("<TD class=\"chart-title\">Change</TD>").append("</TR>");

        while (rs.next() != false) {
            report.append("<TR>\n").append("\t<TD>").append(rs.getString("Type")).append("</TD>\n").append("\t<TD>").append(rs.getInt("Start")).append("</TD>\n").append("\t<TD>").append(
                rs.getInt("End")).append("</TD>\n").append("\t<TD>").append(rs.getInt("Difference")).append("</TD>\n").append("</TR>\n");
        }

        // while (rs.next() != false);
        // rs.close();
        return report.toString();
    }

    /**
     * Returns today's date in the specified format.
     *
     * @param aFormat   Format of the date.
     *
     * @return Date in specified zone.
     */
    private String getToday(String aFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(aFormat);

        return sdf.format((Calendar.getInstance()).getTime());
    }

    public String getUserReportHtml(CallableStatement cs, int aUserTypeId) {
        myStartCount = 0;
        myEndCount   = 0;

        StringBuilder report = new StringBuilder();
        long          start  = 0,
                      exec   = 0,
                      trs1   = 0,
                      trs2   = 0,
                      html   = 0;

        try {
            Hashtable<String, Record> table = new Hashtable<String, Record>();

            cs.getMoreResults();

            ResultSet rs1 = cs.getResultSet();

            if ((rs1 != null) && (rs1.next() != false)) {
                StringBuilder buffer       = new StringBuilder();
                long          oldRequestId = -1;

                do {
                    long   requestId = rs1.getLong("request_id");
                    String userLogin = rs1.getString("user_login");

                    if ((oldRequestId != -1) && (oldRequestId != requestId)) {

                        // We started with a new request id.
                        // So put the previous values in a table.
                        String userList = buffer.toString();
                        Object temp     = table.get(userList);

                        if (temp == null) {
                            Record rec = new Record();

                            rec.aLogin = userList;
                            rec.aStart = 1;
                            table.put(userList, rec);
                        } else {
                            Record rec = (Record) temp;

                            rec.aStart = rec.aStart + 1;
                            table.put(userList, rec);
                        }

                        buffer       = new StringBuilder();
                        oldRequestId = requestId;
                    }

                    if (buffer.toString().trim().equals("") == true) {
                        buffer.append(userLogin);
                    } else {
                        buffer.append(", ").append(userLogin);
                    }

                    oldRequestId = requestId;
                } while (rs1.next() != false);

                String userList = buffer.toString();
                Object temp     = table.get(userList);

                if (temp == null) {
                    Record rec = new Record();

                    rec.aLogin = userList;
                    rec.aStart = 1;
                    table.put(userList, rec);
                } else {
                    Record rec = (Record) temp;

                    rec.aStart = rec.aStart + 1;
                    table.put(userList, rec);
                }
            } else {
                LOG.info("First ResultSet is null");
            }

            cs.getMoreResults();

            ResultSet rs2 = cs.getResultSet();

            if ((rs2 != null) && (rs2.next() != false)) {
                StringBuffer buffer       = new StringBuffer();
                long         oldRequestId = -1;

                do {
                    long   requestId = rs2.getLong("request_id");
                    String userLogin = rs2.getString("user_login");

                    if ((oldRequestId != -1) && (oldRequestId != requestId)) {

                        // We started with a new request id.
                        // So put the previous values in a table.
                        String userList = buffer.toString();
                        Object temp     = table.get(userList);

                        if (temp == null) {
                            Record rec = new Record();

                            rec.aLogin = userList;
                            rec.aEnd   = 1;
                            table.put(userList, rec);
                        } else {
                            Record rec = (Record) temp;

                            rec.aEnd = rec.aEnd + 1;
                            table.put(userList, rec);
                        }

                        buffer       = new StringBuffer();
                        oldRequestId = requestId;
                    }

                    if (buffer.toString().trim().equals("") == true) {
                        buffer.append(userLogin);
                    } else {
                        buffer.append(", ").append(userLogin);
                    }

                    oldRequestId = requestId;
                } while (rs2.next() != false);

                String userList = buffer.toString();
                Object temp     = table.get(userList);

                if (temp == null) {
                    Record rec = new Record();

                    rec.aLogin = userList;
                    rec.aEnd   = 1;
                    table.put(userList, rec);
                } else {
                    Record rec = (Record) temp;

                    rec.aEnd = rec.aEnd + 1;
                    table.put(userList, rec);
                }

                rs2.close();
            } else {
                LOG.info("Second ResultSet is null");
            }

            ArrayList<Record> list = new ArrayList<Record>(table.values());

            list = Record.sort(list);

            int size = list.size();

            if (size == 0) {
                return report.toString();
            }

            String userType = "";

            if (aUserTypeId == UserType.LOGGER) {
                report.append("<TR><TD colspan=\"4\"></TD><TR>");
                userType = "Logger";
            }

            if (aUserTypeId == UserType.ASSIGNEE) {
                userType = "Assignee";
            }

            report.append("<TR>").append("<TD class=\"chart-title\">By ").append(userType).append("</TD>").append("<TD class=\"chart-title\">Start</TD>").append(
                "<TD class=\"chart-title\">End</TD>").append("<TD class=\"chart-title\">Change</TD>").append("</TR>");

            for (int i = 0; i < size; i++) {
                Record rec    = (Record) list.get(i);
                String aLogin = rec.aLogin;
                int    aStart = rec.aStart;
                int    aEnd   = rec.aEnd;
                int    aDiff  = rec.aEnd - rec.aStart;

                myStartCount = myStartCount + aStart;
                myEndCount   = myEndCount + aEnd;
                report.append("<TR>\n").append("\t<TD>").append(aLogin).append("</TD>\n").append("\t<TD>").append(aStart).append("</TD>\n").append("\t<TD>").append(aEnd).append("</TD>\n").append(
                    "\t<TD>").append(aDiff).append("</TD>\n").append("</TR>\n");
            }
        } catch (Exception e) {
            LOG.info("",(e));
        }

        return report.toString();
    }
}


class Record implements Comparable {
    public int    aEnd;
    public String aLogin;
    public int    aStart;
    public int    ourSortOrder;

    //~--- methods ------------------------------------------------------------

    /**
     * Method to compare the object with another object
     */
    public int compareTo(Object o) {
        Record obj = (Record) o;

        return ((ourSortOrder == 0)
                ? (this.aLogin).compareToIgnoreCase(obj.aLogin)
                : (obj.aLogin).compareToIgnoreCase(this.aLogin));
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the Type objects in sorted order
     */
    public static ArrayList<Record> sort(ArrayList<Record> source) {
        int      size     = source.size();
        Record[] srcArray = new Record[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new RecordComparator());

        ArrayList<Record> target = new ArrayList<Record>();

        size = srcArray.length;

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    public String toString() {
        return aLogin + ":" + aStart + ":" + aEnd;
    }
}


class RecordComparator implements Comparator<Record>, Serializable {
    public int compare(Record obj1, Record obj2) {
        return ((Record) obj1).compareTo(obj2);
    }
}
