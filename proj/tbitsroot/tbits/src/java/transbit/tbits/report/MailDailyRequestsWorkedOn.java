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
 * MailDailyRequestsWorkedOn.java
 *
 * $Header:
 */
package transbit.tbits.report;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.SysPrefixes;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;

//TBits Imports
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Mail;
import transbit.tbits.common.MailResourceManager;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.SysConfig;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DateTimeFormat;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.TextDataType;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.webapps.WebUtil;

//Static imports
import static transbit.tbits.Helper.TBitsConstants.PKG_REPORT;

//~--- JDK imports ------------------------------------------------------------

//Java Imports
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//~--- classes ----------------------------------------------------------------

/**
 * The MailDailyRequestsWorkedOn class generates reports listing all tasks
 * worked on today or a specified period for the specified  business area
 *
 * @author Vinod Gupta, Vaibhav
 * @version $Id: $
 */
public class MailDailyRequestsWorkedOn extends HttpServlet implements TBitsPropEnum {
    private static final String REPORT_TEMPLATE  = "web/tbits-daily-report.htm";
    private static final String REPORTS_CSS_FILE = "web/css/tbits_report.css";

    // Logger to log the information/error messages to the application log.
    private static final TBitsLogger LOG    = TBitsLogger.getLogger(PKG_REPORT);
    private static final String      header = new StringBuilder().append("<TR>\n").append("<TD class=\"chart-title\">Request ID.</TD>\n").append("<TD class=\"chart-title\">Logger</TD>\n").append(
                                                  "<TD class=\"chart-title\">Logged On</TD>\n").append("<TD class=\"chart-title\">Assignee</TD>\n").append(
                                                  "<TD class=\"chart-title\">Severity</TD>\n").append("<TD class=\"chart-title\">Status</TD>\n").append(
                                                  "<TD class=\"chart-title\">Category</TD>\n").append("<TD class=\"chart-title\">Type</TD>\n").append(
                                                  "<TD class=\"chart-title\">Last Updated</TD>\n").append("<TD class=\"chart-title\">Last Updated By</TD>\n").append(
                                                  "<TD class=\"chart-title\">Subject</TD>\n</TR>").toString();
    private static final String preHtml = new StringBuilder().append("<html>\n<head>\n").append("<STYLE type=\"text/css\" media=\"screen\">\n").append(
                                              ".details-label { font-family: verdana; font-size:").append("8pt; font-weight: bold; color: black; text-indent:").append(
                                              "3px; border: silver 1px solid; padding-top: 3px;").append("margin: 2px 0px 0px 0px; height: 22px; width: 99% }\n").append(
                                              ".details-content { font-family: courier new;").append("font-size: 9pt; padding: 5px 0px 10px 30px; ").append(
                                              "width: 99%; word-wrap: break-word }\n.").append("header { font-family: verdana; font-size: 8pt;").append(
                                              "font-weight: bold; color: white; text-indent: 3px;").append("background-color: #6679b3; border: 1px solid black;").append(
                                              "padding-top: 3px; margin: 2px 0px 0px 0px;").append("height: 22px; width: 99% }\n.details-spacer ").append(
                                              "{padding: 5px 0px 5px 0px; max-height: 10px;}\n.").append("headerDesc { font-family: courier new; font-size:").append(
                                              "9pt; color: #4A5782 }\ntable {font-family: ").append("verdana;font-size: 8pt;padding: 1px 0px 1px 0px;").append(
                                              "border: 1px solid #cee794;width: 99%}\n").append("td{height: 20px;background-color: white;").append(
                                              "border: 1px solid #cee794;border-top: none;border-right:").append("none;padding: 0px 1px 0px 1px;vertical-align: top;}\n").append(
                                              "td.chart-title {font-weight:bold;color: #6679b3;").append("background-color: #E6F3C9;padding-right: 0px;").append(
                                              "white-space: nowrap;height: 20px;vertical-align:").append("middle;}\n</STYLE>\n</head>\n<body>\n").append("<div align=\"left\">\n<table>").toString();
    private static final String postHtml = "\n</div>\n</body>\n</html>";

    //~--- fields -------------------------------------------------------------

    private int    myBusinessAreaId = 0;
    private String myAddress        = null;
    private String myFromAddress    = null;
    private String myStartDate      = null;
    private String myEndDate        = null;

    //~--- methods ------------------------------------------------------------

    /*
     * This function reinitializes the class level variables.
     *
     */
    private void ReInit() {
        myBusinessAreaId = 0;
        myAddress        = null;
        myFromAddress    = null;
        myStartDate      = null;
        myEndDate        = null;
    }

    /**
     * This method is used to create the Request object from the ResultSet
     *
     * @param  aRequestList
     * @return the corresponding Request object created from the ResultSet
     * @throws SQLException 
     */
    private Request createFromResultSet(ResultSet aRequestList) throws DatabaseException, SQLException 
    {
    	return Request.createFromResultSet(aRequestList);
    }

    /**
     * Servlet doGet Method.
     * @param aRequest the HttpServletRequest object
     * @param aResponse the HttpServletResponse object
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        ReInit();
        doPost(aRequest, aResponse);
    }

    /**
     * Servlet doPost Method.
     * @param aRequest the HttpServletRequest object
     * @param aResponse the HttpServletResponse object
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        ReInit();
        aResponse.setContentType("text/html");

        PrintWriter out            = aResponse.getWriter();
        String      businessAreaId = aRequest.getParameter("business_area");

        if ((businessAreaId == null) || businessAreaId.equals("")) {
            out.println("business_area parameter missing ");

            return;
        } else {
            try {
                myBusinessAreaId = Integer.parseInt(businessAreaId);
            } catch (NumberFormatException e) {
                LOG.warn("An exception occured while parsing the Business" + "Area Id", e);
                out.println("The Business Area " + "you are trying to " + "access does not exist.");

                return;
            }
        }

        myAddress = aRequest.getParameter("address");

        if ((myAddress == null) || myAddress.equals("")) {
            out.println("address parameter missing");

            return;
        }

        myFromAddress = aRequest.getParameter("fromaddress");

        if ((myFromAddress != null) && myFromAddress.equals("")) {
            myFromAddress = null;
        }

        myStartDate = aRequest.getParameter("startDate");
        myEndDate   = aRequest.getParameter("endDate");

        if ((myStartDate != null) && myStartDate.equals("")) {
            out.println("Empty string specified for startDate parameter");

            return;
        } else {
            if ((myEndDate != null) && myEndDate.equals("")) {
                out.println("startDate and endDate parameter value needed");

                return;
            }
        }

        try {
            out.println(sendMailForDailyTasks());
        } catch (Exception e) {
            String error = new StringBuilder().append("Exception From RequestsWorkedOn Report:\n").append("SystemId: " + myBusinessAreaId + "\n").append("StartDate: " + myStartDate
                               + "\n").append("EndDate: " + myEndDate + "\n").toString();

            LOG.severe(error + "\n", e);
            e.printStackTrace(out);
        }

        return;
    }

    /**
     * Method to lookup For Business Area by Prefix in the Prefixes File
     * @param aSysPrefix the prefix to look up BA object
     * @return Business area object corresponding to prefix
     */
    public BusinessArea lookupBySystemPrefixInFile(String aSysPrefix) throws DatabaseException {
        String sysIdLocation = SysPrefixes.getLocation(aSysPrefix);

        if (sysIdLocation == null) {
            return null;
        } else {

            /*
             *  int index = sysIdLocation.indexOf(",");
             * String sysId = sysIdLocation.substring(0,index);
             * String location = sysIdLocation.substring(index+1);
             * BusinessArea ba =
             *   new BusinessArea(Integer.parseInt(sysId),
             *                    "",//Name
             *                    "",//DisplayName
             *                    "",//email
             *                    aSysPrefix,
             *                    "",//description
             *                    location,
             *                    1,//maxRequestId
             *                    1,//maxEmailActions
             *                    false,//emailActive
             *                    true,//Isactive
             *                    null,//domainIds
             *                    null,//Datecreated
             *                    false,//Isprivate
             *                    false,
             *                    false,
             *                    false,
             *                    0,
             *                    false,
             *                    false,
             *                    false,
             *                    false,
             *                    1,
             *                    0,
             *                    0,
             *                    0,
             *                    0,
             *                    0,
             *                    0,
             *                    "", //LegacyPrefixes
             *                    "");// BA rules config
             */
            BusinessArea ba = BusinessArea.lookupBySystemPrefix(aSysPrefix);

            return ba;
        }
    }

    /**
     * Method to replace the substring containing BusinessArea#Number
     * with the corresponding hyperlinks.
     *
     * @param  aString the String in which the # sign has to be replaced
     *                 with the hyperlinks
     * @return the String with the # replaced with the hyper links
     * @throws DatabaseException
     */
    public String replaceBaHashSign(String aString) throws DatabaseException {
        StringBuilder sb         = new StringBuilder(aString);
        String        server     = "";
        BusinessArea  ba         = null;
        int           startIndex = 0;

        startIndex = sb.indexOf("#", 0);

        while (startIndex != -1) {

            // Getting the first separator after the # sign
            int endIndex = getFirstIndex(sb.toString(), startIndex + 1);

            if (endIndex == -1) {
                endIndex = sb.length();
            }

            int beginIndex = startIndex - 1;

            if (beginIndex != -1) {

                // Getting the first separator before the # sign
                while ((sb.charAt(beginIndex) != ' ') && (sb.charAt(beginIndex) != ',') && (sb.charAt(beginIndex) != '&') && (sb.charAt(beginIndex) != '\n') && (sb.charAt(beginIndex) != ';')
                        && (sb.charAt(beginIndex) != '.') && (sb.charAt(beginIndex) != ':') && (sb.charAt(beginIndex) != '-') && (sb.charAt(beginIndex) != '_') && (sb.charAt(beginIndex) != '/')
                        && (sb.charAt(beginIndex) != '\\') && (sb.charAt(beginIndex) != ')') && (sb.charAt(beginIndex) != '(') && (sb.charAt(beginIndex) != '{') && (sb.charAt(beginIndex) != '}')
                        && (sb.charAt(beginIndex) != '<') && (sb.charAt(beginIndex) != '>') && (sb.charAt(beginIndex) != ']') && (sb.charAt(beginIndex) != '[')) {
                    beginIndex = beginIndex - 1;

                    if (beginIndex == -1) {
                        break;
                    }
                }
            }

            // Getting systemPrefix
            String systemPrefix = sb.substring(beginIndex + 1, startIndex).trim();

            // Lookup Business Area by prefix in the Mapper
            ba = BusinessArea.lookupBySystemPrefix(systemPrefix);

            // else look up in the prefixes file
            if (ba == null) {
                ba = lookupBySystemPrefixInFile(systemPrefix);
            }

            // If BA not in prefixes file also, look for next #
            if (ba == null) {
                startIndex = sb.indexOf("#", startIndex + 1);

                continue;
            } else {
                int systemId = ba.getSystemId();

                // Getting the request id
                String requestIdString = sb.substring(startIndex + 1, endIndex);
                int    requestId       = 0;

                // Checking if the value after the # is a number of not.
                // If it is not a number, not showing the hyperlink.
                try {
                    requestId = Integer.parseInt(requestIdString);
                } catch (NumberFormatException e) {
                    startIndex = sb.indexOf("#", startIndex + 1);

                    continue;
                }

                String hrefLink = WebUtil.getServletPath("Q/") + ba.getSystemPrefix() + "/" + requestId;

                // Smart Links support for old instances
                if (ba.getLocation().toUpperCase().trim().endsWith("OLD")) {
                    hrefLink = server + "view-request?systemId=" + systemId + "&requestId=" + requestId;
                }

                String hashString    = sb.substring(beginIndex + 1, endIndex);
                String replaceString = "<a href = \"" + hrefLink + "\"" + " target=\"_blank\">" + hashString + "</a>";
                int    len           = replaceString.length();

                sb.replace(beginIndex + 1, endIndex, replaceString);

                // Getting the next #
                startIndex = sb.indexOf("#", beginIndex + len);
            }
        }

        return sb.toString();
    }

    /**
     * Method to replace the substring containing http://,ftp: etc with the
     * corresponding hyperlinks.
     *
     * @param  aString the String in which the links has to be replaced
     *                 with the hyperlinks
     * @return the String with the links replaced.
     */
    private static String replaceLinks(String aString) {
        String[] formats = { "http://", "tftp://", "file:", "https://", "ftp://" };

        // Punctuations thats needs to be removed if trailing .
        String[]     punctuations = {
            ".", ",", "?", "!", "\"", "'", ":", "[", "]", "{", "}", "(", ")", "-", "<", ">", "&lt;", "&gt;", "&quot;", "&nbsp;", ";"
        };
        String       removedChars = "";
        boolean      puncRemoved  = false;
        StringBuffer sb           = new StringBuffer(aString);
        int          startIndex   = 0;
        int          endIndex     = 0;
        String       httpString   = "";

        for (int j = 0; j < formats.length; j++) {
            startIndex = sb.indexOf(formats[j]);
            endIndex   = 0;

            while (startIndex != -1) {
                httpString   = "";
                removedChars = "";
                puncRemoved  = false;
                endIndex     = getFirstIndexForHref(sb.toString(), startIndex + formats[j].length() - 1);

                if (endIndex == -1) {
                    endIndex = sb.length();
                }

                httpString = sb.substring(startIndex, endIndex).trim();

                if (formats[j].equals("ftp://") && sb.substring(startIndex - 1, endIndex).trim().toLowerCase().startsWith("tftp")) {
                    startIndex = sb.indexOf(formats[j], endIndex);

                    continue;
                }

                for (int i = 0; ; i++) {
                    if (formats[j].equals("file:") && httpString.equals("file:")) {
                        break;
                    }

                    // there can be multiple trailing punctuations
                    // so reset to start till all punctuations removed
                    if (i == punctuations.length) {
                        i           = 0;
                        puncRemoved = false;
                    }

                    // If in an entire round if even one punctuation removed
                    // set puncRemoved to true . Restart if punctuation found,
                    // since we need to check punctuations in order to check ;
                    // at last
                    if (httpString.endsWith(punctuations[i])) {
                        httpString   = httpString.substring(0, httpString.length() - (punctuations[i].length()));
                        removedChars = punctuations[i] + removedChars;
                        puncRemoved  = true;
                        i            = 0;

                        continue;
                    }

                    // If for entire round no punctuation removed, break
                    if ((i == (punctuations.length - 1)) &&!puncRemoved) {
                        break;
                    }
                }

                String replaceString = "<a href = '" + httpString + "'" + " target='_blank'>" + httpString + "</a>" + removedChars;

                sb.replace(startIndex, endIndex, replaceString);

                // Getting the next occurence of the format
                int len = replaceString.length();

                startIndex = sb.indexOf(formats[j], startIndex + len);
            }
        }

        return sb.toString();
    }

    /*
     *  This function makes the html mail content and send mails
     *
     * returns the status as string
     */
    public String sendMailForDailyTasks() throws IOException, DatabaseException, SQLException {
        BusinessArea ba = null;

        try {
            ba = BusinessArea.lookupBySystemId(myBusinessAreaId);

            if (ba == null) {
                return "The Business Area" + "you are trying to access does not exist.";
            }
        } catch (DatabaseException e) {
            String error = new StringBuilder().append("Exception From RequestsWorkedOn Report:\n").append("SystemId: " + myBusinessAreaId + "\n").append("StartDate: " + myStartDate
                               + "\n").append("EndDate: " + myEndDate + "\n").toString();

            LOG.severe(error + "\n", e);

            return e.toString();
        }

        if (myFromAddress == null) {
            myFromAddress = ba.getEmail();

            int commaIndex = myFromAddress.indexOf(',');

            if (commaIndex > 0) {
                myFromAddress = myFromAddress.substring(0, commaIndex);
                LOG.info("From Address: " + myFromAddress);
            }
        }

        Timestamp     startTimestamp = null;
        Timestamp     endTimestamp   = null;
        StringBuilder title          = new StringBuilder(ba.getSystemPrefix() + ": ");
        StringBuilder date           = new StringBuilder();

        try {
            if ((myStartDate != null) &&!myStartDate.equals("") && (myEndDate != null) &&!myEndDate.equals("")) {
                StringTokenizer ss        = new StringTokenizer(myStartDate, "/.-:");
                StringBuffer    startTime = new StringBuffer();
                StringBuffer    endTime   = new StringBuffer();

                while (ss.hasMoreTokens()) {
                    startTime.append(ss.nextToken());
                }

                startTime.append("000000");
                ss = new StringTokenizer(myEndDate, "/.-:");

                while (ss.hasMoreTokens()) {
                    endTime.append(ss.nextToken());
                }

                endTime.append("235959");

                Timestamp t = new Timestamp(startTime.toString());

                date.append(t.toCustomFormat("yyyyMMdd"));
                startTimestamp = t.toGmtTimestamp();
                t              = new Timestamp(endTime.toString());
                date.append(" to " + t.toCustomFormat("yyyyMMdd"));
                endTimestamp = t.toGmtTimestamp();
            } else {
                Timestamp tmst = new Timestamp();

                startTimestamp = new Timestamp(tmst.yyyymmdd() + "000000").toGmtTimestamp();
                endTimestamp   = new Timestamp(tmst.yyyymmdd() + "235959").toGmtTimestamp();
                date.append(tmst.toCustomFormat("yyyyMMdd"));
            }
        } catch (ParseException e) {
            String error = new StringBuilder().append("Exception From RequestsWorkedOn Report:\n").append("SystemId: " + myBusinessAreaId + "\n").append("StartDate: " + myStartDate
                               + "\n").append("EndDate: " + myEndDate + "\n").toString();

            LOG.severe(error + "\n", e);

            return e.toString();
        }

        title.append(date.toString()).append("- Business Area Report.");

        ArrayList<Request> RequestsList = null;

        try {
            RequestsList = getRequestsWorkedOn(myBusinessAreaId, startTimestamp.toSqlDate(), endTimestamp.toSqlDate());
        } catch (DatabaseException e) {
            String error = new StringBuilder().append("Exception From RequestsWorkedOn Report:\n").append("SystemId: " + myBusinessAreaId + "\n").append("StartDate: " + myStartDate
                               + "\n").append("EndDate: " + myEndDate + "\n").toString();

            LOG.severe(error + "\n", e);

            return e.toString();
        }

        int           totalTasks = RequestsList.size();
        StringBuilder sb         = new StringBuilder();
        MimeMultipart mp         = new MimeMultipart();
        Request       request    = null;
        DTagReplacer  cssParser  = null;
        DTagReplacer  hp         = null;

        try {
            cssParser = new DTagReplacer(REPORTS_CSS_FILE);
            hp        = new DTagReplacer(REPORT_TEMPLATE);
        } catch (FileNotFoundException e) {
            LOG.severe("The HTML_FILE hasnt been found", e);
        }

        String sysPrefix = ba.getSystemPrefix();
        String homepage  = WebUtil.getServletPath("search/" + sysPrefix);

        hp.replace("styleDtd", cssParser.parse(ba.getSystemId()));
        hp.replace("homeLink", homepage);
        hp.replace("sysPrefix", ba.getSystemPrefix());
        hp.replace("date", date.toString());
        hp.replace("reqCount", "" + totalTasks);

        for (int i = 0; i < totalTasks; i++) {
            StringBuilder sbTemp = new StringBuilder();

            request = RequestsList.get(i);
            sbTemp.append("<tr><td>").append("<a href=\"").append(WebUtil.getServletPath("/Q/")).append(ba.getSystemPrefix()).append("/").append(request.getRequestId()).append(
                "\" target=\"_blank\">").append(request.getRequestId()).append("</td>");
            sbTemp.append("<td>").append(getUserLogins(request.getLoggers())).append("</td>");

            Timestamp t = Timestamp.getTimestamp(request.getLoggedDate());

            sbTemp.append("<td>").append(t.toDateMin()).append("</td>");
            sbTemp.append("<td>").append(getUserLogins(request.getAssignees())).append("</td>");
            sbTemp.append("<td>").append(request.getSeverityId().getDisplayName()).append("</td>");
            sbTemp.append("<td>").append(request.getStatusId().getDisplayName()).append("</td>");
            sbTemp.append("<td>").append(request.getCategoryId().getDisplayName()).append("</td>");
            sbTemp.append("<td>").append(request.getRequestTypeId().getDisplayName()).append("</td>");
            t = Timestamp.getTimestamp(request.getLastUpdatedDate());
            sbTemp.append("<td>").append(t.toDateMin()).append("</td>");

            // ###############################
            // Hack to take care of null appenders due to migration
            if (request.getUserId() != null) {
                sbTemp.append("<td>").append(User.lookupByUserId(request.getUserId()).getUserLogin());
            } else {
                sbTemp.append("- ");
            }

            // ###############################
            sbTemp.append("</td>");
            sbTemp.append("<td>").append(Utilities.htmlEncode(request.getSubject())).append("</td>");
            sbTemp.append("</tr>");
            sb.append(sbTemp.toString());

            try {
                StringBuilder htmlText = new StringBuilder();

                htmlText.append(preHtml).append(header).append(sbTemp.toString()).append("\n</table>\n</div>\n<div class=\"header\">").append("Request History</div>\n<div>");

                MimeBodyPart tempMbp = new MimeBodyPart();

                try {
                    htmlText.append(getHtmlActionDetails(myBusinessAreaId, request.getRequestId())).append(postHtml);
                    tempMbp.setContent(htmlText.toString(), "text/html");
                } catch (DatabaseException e) {
                    String error = new StringBuilder().append("Exception From RequestsWorkedOn Report:\n").append("SystemId: " + myBusinessAreaId + "\n").append("StartDate: " + myStartDate
                                       + "\n").append("EndDate: " + myEndDate + "\n").toString();

                    LOG.severe(error + "\n", e);
                }

                tempMbp.setDisposition(Part.ATTACHMENT);
                tempMbp.setFileName(request.getRequestId() + ".htm");
                mp.addBodyPart(tempMbp, i);
            } catch (MessagingException e) {
                String error = new StringBuilder().append("Exception From RequestsWorkedOn Report:\n").append("SystemId: " + myBusinessAreaId + "\n").append("StartDate: " + myStartDate
                                   + "\n").append("EndDate: " + myEndDate + "\n").toString();

                LOG.severe(error + "\n", e);
            }
        }

        if (totalTasks > 0) {
            MimeMultipart mpFinal = null;

            hp.replace("reportData", sb.toString());

            try {
                mpFinal = new MimeMultipart();

                MimeBodyPart mbp2 = new MimeBodyPart();

                mbp2.setContent(hp.parse(ba.getSystemId()), "text/html");
                mpFinal.addBodyPart(mbp2, 0);

                for (int i = 0; i < totalTasks; i++) {
                    mpFinal.addBodyPart(mp.getBodyPart(i), i + 1);
                }
            } catch (MessagingException e) {
                String error = new StringBuilder().append("Exception From RequestsWorkedOn Report:\n").append("SystemId: " + myBusinessAreaId + "\n").append("StartDate: " + myStartDate
                                   + "\n").append("EndDate: " + myEndDate + "\n").toString();

                LOG.severe(error + "\n", e);
            }

            // Nitiraj : what is the need of creating mailResMgr when it is not 
            // needed and the mails are sent directly ??? 
            MailResourceManager mailResMgr = new MailResourceManager();
            try {
				Mail.sendHtmlAndAttachments(myAddress, myFromAddress, title.toString(), myFromAddress, 1, getTBitsRecipients(myAddress), mpFinal);
			} catch (MessagingException e) {
				LOG.info("Mail Delivery failed for : myAddress = " + myAddress + " : myFromAddress = " + myFromAddress + " : title = " + title.toString() + " : because of following reason : " , e ) ;
				LOG.info("",(e));
			}
			catch (Exception e) {
				LOG.info("Mail Delivery failed for : myAddress = " + myAddress + " : myFromAddress = " + myFromAddress + " : title = " + title.toString() + " : because of following reason : " , e ) ;
				LOG.info("",(e));
			}
            mailResMgr.commit();

            return "mails send with all Attachments";
        }

        return "no tasks worked on today so no mails send";
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method to get Date disaplyed as per Business area DateTimeFormat
     *
     * @param aDate DateTime as Timestamp
     *
     * @return DateString as per Business area DateTimeFormat configuration
     * @throws DatebaseException
     */
    public String getDateString(int aSystemId, Timestamp aDate) throws DatabaseException {
        BusinessArea   ba             = BusinessArea.lookupBySystemId(aSystemId);
        SysConfig      sc             = ba.getSysConfigObject();
        DateTimeFormat dateTimeFormat = DateTimeFormat.lookupByDateTimeFormatId(sc.getEmailDateFormat());

        return aDate.toSiteTimestamp().toCustomFormat(dateTimeFormat.getFormat());
    }

    public String getTBitsRecipients(String aUsers) {
        String tbitsRecipients = aUsers;

        tbitsRecipients = tbitsRecipients.replaceAll(".transbittech.com", "");
        tbitsRecipients = tbitsRecipients.replaceAll("@transbittech.com", "");

        return tbitsRecipients;
    }

    /**
     * Method to get the first index of ,; )] in the given string, starting
     * from a given index
     *
     * @param  aString the string in which the characters are to be found
     * @param  aStartIndex the start index from which the characters are seen
     * @return the least index of all the characters, else -1
     *
     */
    private static int getFirstIndex(String aString, int aStartIndex) {
        int endIndex = 99999;
        int index1   = aString.indexOf(" ", aStartIndex + 1);

        if (index1 != -1) {
            endIndex = index1;
        }

        int index2 = aString.indexOf(";", aStartIndex + 1);

        if (index2 != -1) {
            endIndex = Math.min(endIndex, index2);
        }

        int index3 = aString.indexOf(",", aStartIndex + 1);

        if (index3 != -1) {
            endIndex = Math.min(endIndex, index3);
        }

        int index4 = aString.indexOf(")", aStartIndex + 1);

        if (index4 != -1) {
            endIndex = Math.min(endIndex, index4);
        }

        int index5 = aString.indexOf("]", aStartIndex + 1);

        if (index5 != -1) {
            endIndex = Math.min(endIndex, index5);
        }

        int index6 = aString.indexOf(".", aStartIndex + 1);

        if (index6 != -1) {
            endIndex = Math.min(endIndex, index6);
        }

        int index7 = aString.indexOf(":", aStartIndex + 1);

        if (index7 != -1) {
            endIndex = Math.min(endIndex, index7);
        }

        int index8 = aString.indexOf("-", aStartIndex + 1);

        if (index8 != -1) {
            endIndex = Math.min(endIndex, index8);
        }

        int index9 = aString.indexOf("_", aStartIndex + 1);

        if (index9 != -1) {
            endIndex = Math.min(endIndex, index9);
        }

        int index10 = aString.indexOf("[", aStartIndex + 1);

        if (index10 != -1) {
            endIndex = Math.min(endIndex, index10);
        }

        int index11 = aString.indexOf("(", aStartIndex + 1);

        if (index11 != -1) {
            endIndex = Math.min(endIndex, index11);
        }

        int index12 = aString.indexOf("/", aStartIndex + 1);

        if (index12 != -1) {
            endIndex = Math.min(endIndex, index12);
        }

        int index13 = aString.indexOf("\\", aStartIndex + 1);

        if (index13 != -1) {
            endIndex = Math.min(endIndex, index13);
        }

        int index14 = aString.indexOf("{", aStartIndex + 1);

        if (index14 != -1) {
            endIndex = Math.min(endIndex, index14);
        }

        int index15 = aString.indexOf("}", aStartIndex + 1);

        if (index15 != -1) {
            endIndex = Math.min(endIndex, index15);
        }

        int index16 = aString.indexOf("&", aStartIndex + 1);

        if (index16 != -1) {
            endIndex = Math.min(endIndex, index16);
        }

        int index17 = aString.indexOf("\n", aStartIndex + 1);

        if (index17 != -1) {
            endIndex = Math.min(endIndex, index17);
        }

        int index18 = aString.indexOf("<", aStartIndex + 1);

        if (index18 != -1) {
            endIndex = Math.min(endIndex, index18);
        }

        int index19 = aString.indexOf(">", aStartIndex + 1);

        if (index19 != -1) {
            endIndex = Math.min(endIndex, index19);
        }

        int index20 = aString.indexOf("?", aStartIndex + 1);

        if (index20 != -1) {
            endIndex = Math.min(endIndex, index20);
        }

        int index21 = aString.indexOf("!", aStartIndex + 1);

        if (index21 != -1) {
            endIndex = Math.min(endIndex, index21);
        }

        int index22 = aString.indexOf("'", aStartIndex + 1);

        if (index22 != -1) {
            endIndex = Math.min(endIndex, index22);
        }

        if (endIndex == 99999) {
            endIndex = -1;
        }

        return endIndex;
    }

    /**
     * Method to get the first index of ,; )] in the given string, starting
     * from a given index
     *
     * @param  aString the string in which the characters are to be found
     * @param  aStartIndex the start index from which the characters are seen
     * @return the least index of all the characters, else -1
     *
     */
    private static int getFirstIndexForHref(String aString, int aStartIndex) {
        int endIndex = 99999;
        int index1   = aString.indexOf(" ", aStartIndex + 1);

        if (index1 != -1) {
            endIndex = index1;
        }

        int index2 = aString.indexOf("&nbsp;", aStartIndex + 1);

        if (index2 != -1) {
            endIndex = Math.min(endIndex, index2);
        }

        if (endIndex == 99999) {
            endIndex = -1;
        }

        return endIndex;
    }

    /**
     * Method to get the Header Description
     *
     * @param aSystemId          BA Id.
     * @param aRequestId         Request ID.
     * @param aHeaderDescription Header Description.
     * @return Returns Header description in readable format.
     */
    private String getHeaderDescription(int aSystemId, long aRequestId, String aHeaderDescription) {
        StringBuffer headerDesc = new StringBuffer();

        try {
            StringTokenizer st = new StringTokenizer(aHeaderDescription, "\n");

            while (st.hasMoreTokens()) {
                String aLine = st.nextToken();

                if (aLine.indexOf("##") < 0) {
                    headerDesc.append(aLine).append("<br>");

                    continue;
                }

                // Header is of format <FieldName>##<FieldId>##trackingInfo
                int    index     = aLine.indexOf("##");
                String fieldName = aLine.substring(0, index).trim();

                aLine = aLine.substring(index + 2).trim();
                index = aLine.indexOf("##");

                String fieldId = aLine.substring(0, index).trim();
                String desc    = aLine.substring(index + 2).trim();

                headerDesc.append(desc).append("<br>");
            }
        } catch (Exception e) {
            LOG.info("An exception occured while processing " + "header description", e);

            return "";
        }

        return headerDesc.toString();
    }

    public String getHtmlActionDetails(int aSystemId, int aRequestId) throws DatabaseException {
        StringBuffer      actionDescription = new StringBuffer();
        ArrayList<Action> actionList        = new ArrayList<Action>();

        actionList = Action.getAllActions(aSystemId, aRequestId, "desc");

        Iterator iterator = actionList.iterator();

        while (iterator.hasNext()) {
            Action action = (Action) iterator.next();

            // Getting the description
            String description = action.getDescription();

            if (!description.equals("")) {

                // Replace the \n with <br> and \t with 4 spaces.
                description = description.replaceAll("^[\n]+", "");
                description = Utilities.htmlEncode(description);
                description = description.replaceAll("\\n", " <br> ");

                int           findex = -6;
                StringBuilder sb     = new StringBuilder(description);

                while ((description.indexOf("<br>", findex + 6)) != -1) {
                    findex      = description.indexOf("<br>", findex + 6);
                    sb          = new StringBuilder(description);
                    sb          = sb.replace(findex, findex + 4, "\n<br>");
                    description = sb.toString();

                    if ((findex + 6) >= description.length()) {
                        break;
                    }
                }

                description = description.replaceAll("\\r", " ");
                description = description.replaceAll("  ", "&nbsp;&nbsp;");
                description = description.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp; ");

                // replacing the http:,file: etc
                // with the corresponding hyperlinks
                description = replaceLinks(description);

                // Replaces the BusinessArea#Number with the corresponding link
                description = replaceBaHashSign(description);
            }

            actionDescription.append("<div class='details-label'>").append(action.getActionId()).append(". ");

            // ###############################
            // Hack to take care of null appenders due to migration
            if ((action.getUserId() != 0) && (User.lookupByUserId(action.getUserId()) != null)) {
                actionDescription.append((User.lookupByUserId(action.getUserId())).getDisplayName());
            } else {
                actionDescription.append("- ");
            }

            // ##################################
            actionDescription.append(": ").append(getDateString(aSystemId, action.getLastUpdatedDate())).append("</div>").append("<div class='details-content'>").append(description);

            if ((action.getAttachments() != null) && (!(action.getAttachments()).equals(""))) {
                actionDescription.append("<div class=\"details-spacer\"></div>");

                StringTokenizer st = new StringTokenizer(action.getAttachments(), ",");

                while (st.hasMoreTokens()) {
                    String          aAttachment     = st.nextToken().trim();
                    String          displayFileName = "";
                    String          size            = "";
                    String          conversion      = "";
                    StringTokenizer st1             = new StringTokenizer(aAttachment, "-");

                    try {

                        // New Format <ReqId>-<ActId>-<Size>-<CF>-FName
                        // Old Format <SysId>-<ReqId>-<ActId>-<Size>-<CF>-FName
                        st1.nextToken();    // Request Id
                        st1.nextToken();    // Action Id
                        size            = st1.nextToken();
                        conversion      = st1.nextToken();
                        displayFileName = st1.nextToken();

                        if (conversion.equals("0")) {
                            conversion = "";
                        } else if (conversion.equals("1")) {
                            conversion = " (converted from .bmp)";
                        }

                        size = "[ " + size + " ]";
                    } catch (Exception e) {
                        displayFileName = size;
                        size            = "";
                    }

                    try {
                        actionDescription.append("<a href=\"").append(WebUtil.getServletPath("read-attachment")).append("?attachment=").append(URLEncoder.encode(aAttachment,
                                "UTF-8")).append("\" target=\"_blank\">").append(displayFileName).append("</a> ").append(size).append(conversion).append("<br>");
                    } catch (UnsupportedEncodingException uee) {
                        LOG.severe("UnsupportedEncodingException", uee);
                    }
                }
            }

            String headerDesc = Utilities.htmlEncode(action.getHeaderDescription());

            headerDesc = getHeaderDescription(aSystemId, aRequestId, headerDesc);

            if (!(headerDesc.trim()).equals("")) {

                // headerDesc = headerDesc.replaceAll("\\n", "<br>");
                actionDescription.append("\n<div class=\"details-spacer\"></div>").append("<div class='headerDesc'>").append(headerDesc).append("</div>");
            }

            actionDescription.append("</div>\n");
        }

        return actionDescription.toString();
    }

    /**
     * This method retrieves the Users related to this request.
     *
     * @param aSystemId       System ID.
     * @param aRequestId      Request ID.
     * @param aRequest        Request Object
     * @param aResultSet      ResultSet Object.
     *
     */
    private Request getRequestUsers(int aSystemId, long aRequestId, Request aRequest, ResultSet aResultSet) throws DatabaseException, SQLException {
        ArrayList<RequestUser> loggerList     = new ArrayList<RequestUser>();
        ArrayList<RequestUser> assigneeList   = new ArrayList<RequestUser>();
        ArrayList<RequestUser> subscriberList = new ArrayList<RequestUser>();
        ArrayList<RequestUser> toList         = new ArrayList<RequestUser>();
        ArrayList<RequestUser> ccList         = new ArrayList<RequestUser>();

        while (true) {
            int         sysId = aResultSet.getInt("sys_id");
            long        reqId = aResultSet.getLong("request_id");
            RequestUser obj   = null;

            if ((sysId == aSystemId) && (reqId == aRequestId)) {
                obj = RequestUser.createFromResultSet(aResultSet);

                switch (obj.getUserTypeId()) {
                case UserType.LOGGER :
                    loggerList.add(obj);

                    break;

                case UserType.ASSIGNEE :
                    assigneeList.add(obj);

                    break;

                case UserType.SUBSCRIBER :
                    subscriberList.add(obj);

                    break;

                case UserType.TO :
                    toList.add(obj);

                    break;

                case UserType.CC :
                    ccList.add(obj);

                    break;
                }

                if (aResultSet.next() == false) {
                    break;
                }
            } else {

                // We came to a row whose requestId is different from that of
                // the current one. So move back.

                /* aResultSet.previous(); */
                break;
            }
        }

        aRequest.setLoggers(loggerList);
        aRequest.setAssignees(assigneeList);
        aRequest.setSubscribers(subscriberList);
        aRequest.setTos(toList);
        aRequest.setCcs(ccList);

        return aRequest;
    }

    public ArrayList<Request> getRequestsWorkedOn(int aSystemId, String startTime, String endTime) throws DatabaseException, SQLException {
        ArrayList<Request> list       = new ArrayList<Request>();
        Connection         connection = null;

        try {
        	connection = DataSourcePool.getConnection();
            CallableStatement cs = connection.prepareCall("stp_report_requestsWorkedOn ?,?,?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            cs.setInt(1, aSystemId);
            cs.setString(2, startTime);
            cs.setString(3, endTime);

            //
            // The returns from the procedure are
            // 1. updatecount of number of requests that turned up for the
            // query which we ignore.
            // 2. ResultSet of requests.
            // 3. ResultSet of requestusers.
            //
            boolean flag = cs.execute();

            if (flag == false) {
                cs.getMoreResults();

                ResultSet rsRequests = cs.getResultSet();

                if (rsRequests != null) {
                    while (rsRequests.next() != false) {
                        Request request = (Request) createFromResultSet(rsRequests);

                        list.add(request);
                    }

                    cs.getMoreResults();

                    ResultSet rsRequestUsers = cs.getResultSet();
                    int       listSize       = list.size();

                    for (int i = 0; (rsRequestUsers.next() != false) && (i < listSize); i++) {
                        Request request = (Request) list.get(i);

                        getRequestUsers(request.getSystemId(), request.getRequestId(), request, rsRequestUsers);
                        list.set(i, request);
                    }
                }

                rsRequests.close();
            }

            cs.close();
        } catch (SQLException sqle) {
            throw new DatabaseException("The application encountered an exception while trying to " + "retrieve the Search results.", sqle);
        } finally {
            try {
            	if(connection != null)
            		connection.close();
            } catch (SQLException sqle) {}
        }

        return list;
    }

    public static String getUserLogins(Collection<RequestUser> reqUsers) throws DatabaseException {
        if (reqUsers == null)  {
            return "-";
        }

        String userLogins = "" ;
        for (RequestUser ru : reqUsers ) 
        {
        	User user = User.lookupAllByUserId(ru.getUserId());
            userLogins = userLogins + user.getUserLogin() + "; ";
        }

        if( !userLogins.equals("") )
        	userLogins = userLogins.substring(0, userLogins.length() - 2);
        else 
        	userLogins ="-";
        return userLogins;
    }
}
