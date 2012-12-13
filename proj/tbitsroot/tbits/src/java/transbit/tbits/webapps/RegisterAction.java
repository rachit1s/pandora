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
 * RegisterAction.java
 $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

//TBits Imports
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserReadAction;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

//Static imports
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//~--- classes ----------------------------------------------------------------

/**
 * This servlet is used to register user last read action for the Request
 *
 * @author Vinod Gupta
 * @version $Id: $
 */
public class RegisterAction extends HttpServlet {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

    //~--- methods ------------------------------------------------------------

    /**
     * The doGet method of the servlet.
     *
     * @param aHttpRequest the HttpServlet Request Object
     * @param aHttpResponse the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse) throws ServletException, IOException {
        PrintWriter out = null;

        try {
            out = aHttpResponse.getWriter();

            long start = System.currentTimeMillis();

            handleGetRequest(aHttpRequest, aHttpResponse);

            long end = System.currentTimeMillis();

            LOG.debug("Time taken to Register user Action: " + (end - start) + " mecs");
        } catch (TBitsException de) {
            LOG.info("",(de));
        } catch (RuntimeException e) {
            LOG.severe("\nUser: " + aHttpRequest.getRemoteUser() + "\npathInfo: " + aHttpRequest.getPathInfo() + "\n" + "",(e));
            out.println(TBitsLogger.getStackTrace(e));

            return;
        } catch (Exception e) {
            LOG.severe("\nUser: " + aHttpRequest.getRemoteUser() + "\npathInfo: " + aHttpRequest.getPathInfo() + "\n" + "",(e));
            out.println(TBitsLogger.getStackTrace(e));

            return;
        }
    }

    /**
     * The doPost method of the servlet.
     *
     * @param aHttpRequest the HttpServlet Request Object
     * @param aHttpResponse the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse) throws ServletException, IOException {
        doGet(aHttpRequest, aHttpResponse);
    }

    /**
     * Method that actually handles the Get Request.
     *
     * @param aHttpRequest the HttpServlet Request Object
     * @param aHttpResponse the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     * @throws Exception
     */
    public void handleGetRequest(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse) throws ServletException, IOException, Exception {
        aHttpResponse.setContentType("text/html");

        PrintWriter out         = aHttpResponse.getWriter();
        HttpSession httpSession = aHttpRequest.getSession();

        //
        // Validate the user
        //
        User user = WebUtil.validateUser(aHttpRequest);

        if (user == null) {
            LOG.debug("User null: not registering action");

            return;
        }

        ArrayList<String> params = Utilities.toArrayList(aHttpRequest.getPathInfo(), "//");

        if ((params == null) || (params.size() < 3)) {
            LOG.info("Incorrect params: " + params);

            return;
        }

        BusinessArea ba = BusinessArea.lookupBySystemPrefix(params.get(0));

        if (ba == null) {
            LOG.info("The business area does not exist. [Prefix: " + params.get(0));

            return;
        }

        int systemId    = ba.getSystemId();
        int requestId   = Integer.parseInt(params.get(1));
        int maxActionId = Integer.parseInt(params.get(2));

        //
        // Validate the user, get the user Object
        //
        // User user = User.lookupByUserLogin(params.get(3).trim());
        // if (user == null)
        // {
        // LOG.info("User does not exist. Login: " +
        // params.get(3));
        // return;
        // }
        int userId = user.getUserId();

        // register the latest action user read for the request.
        LOG.debug("Register: " + systemId + " " + requestId + " " + maxActionId + " " + userId);
        UserReadAction.registerUserReadAction(systemId, requestId, maxActionId, userId);
        out.print("");

        return;
    }
}
