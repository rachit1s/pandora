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
 * GetAuditInfo.java
*  $Header:


 */
package transbit.tbits.report;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DataSourcePool;

//TBits Imports
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;

//Static imports
import static transbit.tbits.Helper.TBitsConstants.PKG_REPORT;

//~--- JDK imports ------------------------------------------------------------

//Java imports
import java.io.IOException;
import java.io.PrintWriter;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//~--- classes ----------------------------------------------------------------

/**
 * This servlet is used to return the status,lastupdated and updated time of a
 * particular request or all requests in that business area
 *
 * @author : Vaibhav.
 * @version : $Id: $
 */
public class GetAuditInfo extends HttpServlet {

    // Private variables
    private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_REPORT);

    //~--- methods ------------------------------------------------------------

    /**
     * This method services the Http-GET Requests for this Servlet.
     *
     * @param aRequest the HttpServlet Request Object
     * @param aResponse the HttpServlet Response Object
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        PrintWriter out = aResponse.getWriter();

        aResponse.setContentType("text/plain");

        int          systemId  = 0;
        String       requestId = "";
        BusinessArea ba        = null;
        String       prefix    = "";
        String       pathInfo  = aRequest.getPathInfo();

        if ((pathInfo == null) || pathInfo.trim().equals("") || pathInfo.trim().equals("/") || pathInfo.trim().equals("\\")) {
            out.println("Please specify the Business area prefix");

            return;
        } else {

            // There is something in the pathInfo we received.
            StringTokenizer st = new StringTokenizer(pathInfo, "/\\");

            //
            // Once the pathInfo is tokenized, the first token is expected
            // to be the sysPrefix and second requestId if mentioned.
            //
            if (st.hasMoreTokens() == true) {
                prefix = st.nextToken().trim();

                try {
                    ba = BusinessArea.lookupBySystemPrefix(prefix);
                } catch (DatabaseException de) {
                    LOG.warn("",(de));
                }

                if (ba == null) {
                    out.println("The Business area specified does not exist");

                    return;
                } else {
                    systemId = ba.getSystemId();
                }
            }

            if (st.hasMoreTokens() == true) {
                requestId = st.nextToken().trim();
            }

            if (requestId.equals("")) {
                requestId = "all";
            }
        }

        ArrayList<String> list = null;

        try {
            list = getRequestsInfo(systemId, requestId);
        } catch (DatabaseException e) {
            LOG.severe("Error while getting AuditInfo for requestId: " + requestId + "\n\n" + "",(e));
            out.println(TBitsLogger.getStackTrace(e));

            return;
        }

        int size = list.size();

        if (size == 0) {
            out.println("No Information available for requestId: " + requestId);

            return;
        }

        StringBuffer buffer = new StringBuffer();

        prefix = ba.getSystemPrefix() + "#";

        for (int i = 0; i < size; i++) {
            buffer.append(prefix).append(list.get(i)).append("\n");
        }

        out.println(buffer.toString());

        return;
    }

    /**
     * This method services the Http-POST Requests for this Servlet.
     *
     * @param aRequest the HttpServlet Request Object
     * @param aResponse the HttpServlet Response Object
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        doGet(aRequest, aResponse);
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns an arraylist of concatenated string information of
     * requestId,status,lastUpdatedBy,lastupdated for requests
     *
     * @param aSystemId Business area Id
     * @param aRequestId requestId or 'all' for all requests
     * @throws DatabaseException
     * @return ArrayList of requests info as strings
     */
    private ArrayList<String> getRequestsInfo(int aSystemId, String aRequestId) throws DatabaseException {
        ArrayList<String> list       = new ArrayList<String>();
        String            temp       = "";
        Connection        connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("Call stp_report_gbo_getAuditInfo ?,?");

            cs.setInt(1, aSystemId);
            cs.setString(2, aRequestId);

            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                temp = rs.getLong("requestId") + "- " + rs.getString("status") + ", " + rs.getString("lastupdatedby") + ", " + rs.getString("lastupdated");
                list.add(temp);
            }

            rs.close();
            cs.close();
        } catch (SQLException sqle) {
            throw new DatabaseException("The application encountered an exception while trying " + "to retrieve the audit Info.", sqle);
        } finally {
        	if( null != connection )
            try {
                connection.close();
            } catch (SQLException sqle) {
                LOG.severe("",(sqle));
            }
        }

        return list;
    }
}
