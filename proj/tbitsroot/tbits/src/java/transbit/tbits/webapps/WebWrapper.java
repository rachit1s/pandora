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
 * WebWrapper.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

//Imports from TBits.
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.DSQLParseException;
import transbit.tbits.exception.TBitsException;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

//~--- JDK imports ------------------------------------------------------------

//Java Imports
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//~--- classes ----------------------------------------------------------------

/**
 * This is a servlet that handles the requests to search in TBits.
 *
 * @author  Vaibhav.
 * @version $Id: $
 */
public class WebWrapper extends HttpServlet {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

    // HTML Interface.
    public static final String HTML_FILE = "web/tbits-search-temp.htm";

    //~--- methods ------------------------------------------------------------

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
        PrintWriter out = aResponse.getWriter();

        aResponse.setContentType("text/html");

        try {
            handleGetRequest(aRequest, aResponse);
        } catch (Throwable e) {
            LOG.warn(e.toString(), e);
            out.println(TBitsLogger.getStackTrace(e).replaceAll("\n", "<BR>").replaceAll("\\s", "&nbsp;"));
        }
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
        PrintWriter out = aResponse.getWriter();

        aResponse.setContentType("text/html");

        try {
            handleGetRequest(aRequest, aResponse);
        } catch (Throwable e) {
            LOG.warn(e.toString(), e);
            out.println(TBitsLogger.getStackTrace(e).replaceAll("\n", "<BR>").replaceAll("\\s", "&nbsp;"));
        }
    }

    /**
     * Method that actually handles the Get Request.
     *
     *
     * @param aRequest          the HttpServlet Request Object
     * @param aResponse         the HttpServlet Response Object
     * @throws ServletException
     * @throws IOException
     * @throws TBitsExceptionhrows DatabaseException
     * @throws FileNotFoundException
     */
    private void handleGetRequest(HttpServletRequest aRequest, HttpServletResponse aResponse)
            throws ServletException, IOException, TBitsException, DatabaseException, DSQLParseException, APIException {
        aResponse.setContentType("text/html");

        PrintWriter  out  = aResponse.getWriter();
        User         user = WebUtil.validateUser(aRequest);
        BusinessArea ba   = getBusinessArea(aRequest);

        if (ba == null) {
            return;
        }

        String query = aRequest.getParameter("query");

        if ((query == null) || (query.trim().equals("") == true)) {
            DTagReplacer hp = new DTagReplacer(HTML_FILE);

            hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
            hp.replace("baPrefix", ba.getSystemPrefix());
            out.println(hp.parse(ba.getSystemId()));

            return;
        }

        String action = aRequest.getParameter("action");

        if (action.equalsIgnoreCase("add") == true) {
            Hashtable<String, String> table = tokenizeInput(query);

            table.put(Field.BUSINESS_AREA, Integer.toString(ba.getSystemId()));
            table.put(Field.USER, user.getUserLogin());

            AddRequest app     = new AddRequest();
            app.setSource(TBitsConstants.SOURCE_WEB);
            app.setContext(aRequest.getContextPath());
            
            Request    request = app.addRequest(table);

            if (request != null) {
                StringBuilder message = new StringBuilder();

                message.append("<br><br><a href='").append(WebUtil.getServletPath(aRequest, "/q/")).append(ba.getSystemPrefix()).append("/").append(request.getRequestId()).append("' target='_blank'>").append(ba.getSystemPrefix()).append(
                    "#").append(request.getRequestId()).append("</a> added successfully.");
                out.println(message.toString());

                return;
            }
        } else if (action.equalsIgnoreCase("update") == true) {
            Hashtable<String, String> table = tokenizeInput(query);

            table.put(Field.BUSINESS_AREA, Integer.toString(ba.getSystemId()));
            table.put(Field.USER, user.getUserLogin());
            LOG.info(table.toString());

            UpdateRequest app     = new UpdateRequest();
            app.setSource(TBitsConstants.SOURCE_WEB);
            app.setContext(aRequest.getContextPath());
            Request       request = app.updateRequest(table);

            if (request != null) {
                StringBuilder message = new StringBuilder();

                message.append("<br><br><a href='").append(WebUtil.getServletPath(aRequest, "/q/")).append(ba.getSystemPrefix()).append("/").append(request.getRequestId()).append("' target='_blank'>").append(ba.getSystemPrefix()).append(
                    "#").append(request.getRequestId()).append("</a> updated successfully.");
                out.println(message.toString());

                return;
            }
        }
    }

    private Hashtable<String, String> tokenizeInput(String input) {
        Hashtable<String, String> paramTable = new Hashtable<String, String>();

        if (input == null) {
            return paramTable;
        }

        StringTokenizer ost = new StringTokenizer(input.trim(), ",");

        while (ost.hasMoreTokens()) {
            String str   = ost.nextToken().trim();
            int    index = str.indexOf(':');

            if (index > 0) {
                String key   = str.substring(0, index);
                String value = str.substring(index + 1);

                paramTable.put(key, value);
            } else {
                LOG.warn("Bad token: " + str);
            }
        }

        return paramTable;
    }

    //~--- get methods --------------------------------------------------------

    public static BusinessArea getBusinessArea(HttpServletRequest aRequest) throws ServletException, TBitsException, DatabaseException {
        BusinessArea ba       = null;
        String       pathInfo = aRequest.getPathInfo();

        LOG.info("Path Information: " + pathInfo);

        // Check if pathInfo is null.
        if (pathInfo == null) {
            LOG.info("Null Path Info");

            // In all other cases, consider the user's Default BA.
            int systemId = 1;

            ba = BusinessArea.lookupBySystemId(systemId);

            if (ba == null) {

                // If user default BA isno longer active,
                // get First available active Ba
                ba = BusinessArea.lookupBySystemId(1);

                if (ba == null) {
                    throw new TBitsException("The business area you are trying to " + "access does not exist.");
                }
            }
        } else {

            // There is something in the pathInfo we received.
            StringTokenizer st = new StringTokenizer(pathInfo, "/\\");

            //
            // Once the pathInfo is tokenized, the first token is expected to
            // be the SystemPrefix.
            //
            if (st.hasMoreTokens() == true) {
                String sysPrefix = st.nextToken().trim();

                LOG.info("Prefix: " + sysPrefix);
                ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

                if (ba == null) {
                    throw new TBitsException("The business area you are trying to " + "access does not exist.");
                }
            } else {

                //
                // If View-Request called this, then there is no use of
                // taking the default BA because we are still without the
                // request id value.
                //
                int systemId = 1;

                LOG.info("My Home Id: " + systemId);
                ba = BusinessArea.lookupBySystemId(systemId);

                if (ba == null) {
                    throw new TBitsException("The business area you are trying to " + "access does not exist.");
                }
            }
        }

        return ba;
    }
}
