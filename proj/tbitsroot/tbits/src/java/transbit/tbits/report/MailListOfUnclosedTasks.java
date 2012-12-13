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

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;

//TBits Imports
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Mail;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//~--- classes ----------------------------------------------------------------

/**
 * The MailDailyOfUnclosedTasks class generates reports listing all unclosed
 * tasks.
 *
 * @author Vinod Gupta, Vaibhav
 * @version $Id: $
 */
public class MailListOfUnclosedTasks extends HttpServlet implements TBitsPropEnum {
    private static final String REPORT_TEMPLATE  = "web/tbits-assignee-report.htm";
    private static final String REPORTS_CSS_FILE = "web/css/tbits_report.css";

    // Logger to log the information/error messages to the application log.
    private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_REPORT);

    //~--- fields -------------------------------------------------------------

    private int    systemId      = 0;
    private String myFromAddress = null;
    public String  myAnalyst     = null;
    private String myAddress     = null;

    //~--- methods ------------------------------------------------------------

    public void ReInit() {
        systemId      = 0;
        myAddress     = null;
        myFromAddress = null;
        myAnalyst     = null;
    }

    /**
     * This method is used to create the Request object from the ResultSet
     *
     * @param  rs
     * @return the corresponding Request object created from the ResultSet
     * @throws DatabaseException 
     * @throws SQLException 
     */
    private Request createFromResultSet(ResultSet rs) throws SQLException, DatabaseException {
            return Request.createFromResultSet(rs);
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
                systemId = Integer.parseInt(businessAreaId);
            } catch (NumberFormatException e) {
                LOG.warn("An exception occured while formatting the " + "Business Area Id", e);
                out.println("The Business Area " + "you are trying to " + "access does not exist.");

                return;
            }
        }

        myAnalyst = aRequest.getParameter("analyst");
        myAddress = aRequest.getParameter("address");

        if ((myAnalyst != null) && (myAnalyst.equals(""))) {
            out.println("analyst param empty");

            return;
        } else {
            if ((myAddress != null) && (myAddress.equals(""))) {
                out.println("analyst and address paramter missing");

                return;
            }
        }

        myFromAddress = aRequest.getParameter("fromaddress");

        if ((myFromAddress == null) || myFromAddress.equals("")) {
            myFromAddress = null;
        }

        try {
            out.println(sendmailtoassignee());
        } catch (DatabaseException dbe) {
            LOG.severe("An exception occured while sending mail " + "to assignees", dbe);
        }

        return;
    }

    public String sendmailtoassignee() throws IOException, DatabaseException {
        BusinessArea ba = null;

        try {
            ba = BusinessArea.lookupBySystemId(systemId);

            if (ba == null) {
                return "The Business Area" + "you are trying to access does not exist.";
            }
        } catch (DatabaseException e) {
            LOG.severe("An exception occured while retrieving the business" + "Area with systemId " + systemId, e);

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

        ArrayList<Integer> assigneeIds = new ArrayList<Integer>();
        User               user        = null;

        if (myAnalyst == null) {
            try {
                assigneeIds = getAssigneeList(systemId);
            } catch (DatabaseException e) {
                LOG.severe("An exception occured while getting the assignee " + "list for systemId " + systemId, e);

                return e.toString();
            }
        } else if (myAnalyst.equals("-")) {
            assigneeIds.add(new Integer(-1));
        } else {
            user = null;

            try {
                user = User.lookupByUserLogin(myAnalyst);
            } catch (DatabaseException dbe) {
                LOG.info("An exception has occured while retrieving a " + "user object", dbe);
            }

            if (user == null) {
                return "Analyst does not exist in the User table";
            } else {
                assigneeIds.add(new Integer(user.getUserId()));
            }
        }

        int                reqCount     = 0;
        ArrayList<Request> requestsList = null;
        Request            request      = null;
        DTagReplacer       cssParser    = null;
        DTagReplacer       hp           = null;

        try {
            cssParser = new DTagReplacer(REPORTS_CSS_FILE);
            hp        = new DTagReplacer(REPORT_TEMPLATE);
        } catch (FileNotFoundException e) {
            LOG.severe("The templates for this report havnt been found", e);

            return e.toString();
        }

        String homepage = WebUtil.getServletPath("search/") + ba.getSystemPrefix();

        hp.replace("styleDtd", cssParser.parse(ba.getSystemId()));
        hp.replace("homeLink", homepage);
        hp.replace("baPrefix", ba.getSystemPrefix());

        Timestamp tmst = new Timestamp();
        String    date = tmst.toCustomFormat("yyyyMMdd");

        hp.replace("date", date);

        String analystName = (myAnalyst == null)
                             ? "you"
                             : myAnalyst;
        String subject     = ba.getSystemPrefix() + ": " + date + "- " + "Requests assigned to " + analystName + ".";

        for (int i = 0; i < assigneeIds.size(); i++) {
            StringBuilder reportData = new StringBuilder();

            try {
                if (((Integer) assigneeIds.get(i)).intValue() == -1) {
                    requestsList = getUnassigneedRequests(systemId);
                } else {
                    requestsList = getRequestsForAssignee(systemId, ((Integer) assigneeIds.get(i)).intValue());
                }
            } catch (DatabaseException e) {
                LOG.severe("An exception occured while retrieving the " + "requests for assignee", e);

                return e.toString();
            }

            reqCount = requestsList.size();

            for (int j = 0; j < reqCount; j++) {
                request = (Request) requestsList.get(j);
                reportData.append("<tr><td>").append("<a href=\"").append(WebUtil.getServletPath("Q/")).append(ba.getSystemPrefix()).append("/").append(request.getRequestId()).append(
                    "\" target=\"_blank\">").append(request.getRequestId()).append("</td>");
                reportData.append("<td>").append(getUserLogins(request.getLoggers())).append("</td>");

                Timestamp t = Timestamp.getTimestamp(request.getLoggedDate());

                reportData.append("<td>").append(t.toDateMin()).append("</td>");
                reportData.append("<td>").append(request.getSeverityId().getDisplayName()).append("</td>");
                reportData.append("<td>").append(request.getStatusId().getDisplayName()).append("</td>");
                reportData.append("<td>").append(request.getCategoryId().getDisplayName()).append("</td>");
                reportData.append("<td>").append(request.getRequestTypeId().getDisplayName()).append("</td>");
                t = Timestamp.getTimestamp(request.getLastUpdatedDate());
                reportData.append("<td>").append(t.toDateMin()).append("</td>");

                // ###############################
                // Hack to take care of null appenders due to migration

                /*
                 *       if (request.getUser() != null) {
                 *   reportData.append("<td>").
                 *       append((request.getUser()).getUserLogin());
                 * }
                 * else {
                 *   reportData.append("- ");
                 *   }
                 */

                // ###############################
                reportData.append("</td>");
                reportData.append("<td>");

                if (request.getUserId() != null) {
                    reportData.append((User.lookupAllByUserId(request.getUserId())).getUserLogin());
                } else {
                    reportData.append("- ");
                }

                reportData.append("</td>");
                reportData.append("<td>").append(getUserLogins(request.getAssignees())).append("</td>");
                reportData.append("<td>").append(Utilities.htmlEncode(request.getSubject())).append("</td>");
                reportData.append("</tr>");
            }

            if (reqCount > 0) {
                hp.replace("reqCount", "" + reqCount);
                hp.replace("reportData", reportData.toString());
                hp.replace("analystName", (myAnalyst == null)
                                          ? "you"
                                          : myAnalyst);

                if (myAnalyst != null) {
                    Mail.sendWithHtml(myAddress, myFromAddress, subject.toString(), hp.parse(ba.getSystemId()));
                } else {
                    user = User.lookupByUserId(((Integer) assigneeIds.get(i)).intValue());

                    if (user != null) {
                        Mail.sendWithHtml(user.getEmail(), myFromAddress, subject.toString(), hp.parse(ba.getSystemId()));
                    }
                }
            }

            // Now fetch for next assignee
        }

        return "Request processed successfully";
    }

    //~--- get methods --------------------------------------------------------

    public ArrayList<Integer> getAssigneeList(int aSystemId) throws DatabaseException {
        ArrayList<Integer> list       = new ArrayList<Integer>();
        Connection         connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_report_getAssigneeListToMail ?}");

            cs.setInt(1, systemId);

            ResultSet result = cs.executeQuery();

            while (result.next()) {
                if (!list.contains(new Integer(result.getInt("user_id")))) {
                    list.add(new Integer(result.getInt("user_id")));
                }
            }

            result.close();
            cs.close();
        } catch (SQLException sqle) {
            throw new DatabaseException("The application encountered an exception while trying to " + "retrieve the assignee list.", sqle);
        } finally {
        	if( null != connection )
            try {
                connection.close();
            } catch (SQLException sqle) {}
        }

        return list;
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

 // Nitiraj msg : no such stored procedure.
    @Deprecated
    public ArrayList<Request> getRequestsForAssignee(int aSystemId, int aUserId) throws DatabaseException {
        ArrayList<Request> list       = new ArrayList<Request>();
        Connection         connection = null;

        try {
            connection = DataSourcePool.getConnection();

            // Nitiraj msg : no such stored procedure.
            CallableStatement cs = connection.prepareCall("stp_report_getUnclosedRequestForAnalyst ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aUserId);

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

                        request = getRequestUsers(request.getSystemId(), request.getRequestId(), request, rsRequestUsers);
                        list.set(i, request);
                    }
                }

                rsRequests.close();
            }

            cs.close();
        } catch (SQLException sqle) {
            throw new DatabaseException("The application encountered an exception while trying to " + "retrieve the assignee requests", sqle);
        } finally {
        	if( null != connection )
            try {
                connection.close();
            } catch (SQLException sqle) {}
        }

        return list;
    }

    @Deprecated // Nitiraj msg : no such store procedure
    public ArrayList<Request> getUnassigneedRequests(int aSystemId) throws DatabaseException {
        ArrayList<Request> list       = new ArrayList<Request>();
        Connection         connection = null;

        try {
            connection = DataSourcePool.getConnection();
         // Nitiraj msg : no such store procedure
            CallableStatement cs = connection.prepareCall("{stp_report_getUnassigneedRequests ?");

            cs.setInt(1, aSystemId);

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

                        request = getRequestUsers(request.getSystemId(), request.getRequestId(), request, rsRequestUsers);
                        list.set(i, request);
                    }
                }

                rsRequests.close();
            }

            cs.close();
        } catch (SQLException sqle) {
            throw new DatabaseException("The application encountered an exception while trying to " + "retrieve the assignee requests", sqle);
        } finally {
        	if( null != connection )
            try {
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
