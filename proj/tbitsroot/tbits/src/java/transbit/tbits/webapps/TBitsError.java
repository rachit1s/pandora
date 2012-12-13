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
 * ReadAttachment.java
 *
 * $Header:
 *
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DTagReplacer;

//TBits Imports.
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.exception.TBitsException;

//Static imports.
import static transbit.tbits.Helper.TBitsPropEnum.KEY_DOMAIN;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_HYDURL;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_NYCURL;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//~--- classes ----------------------------------------------------------------

/**
 * Error page for TBits.
 *
 *
 * @author Vaibhav.
 * @version $Id: $
 */
public class TBitsError extends HttpServlet {

    public static final String EXCEPTION_OBJECT = "ExceptionObject";
	// Html Interface.
    private static final String HTML_FILE = "web/tbits-error.htm";

    //~--- methods ------------------------------------------------------------

    /**
     * This method serves the HTTP-GET requests.
     *
     * @param aRequest  Request Object which contains the parameters passed.
     * @param aResponse Response Object which carries the output to the client.
     *
     * @exception ServletException
     * @exception IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, FileNotFoundException {
        PrintWriter out = aResponse.getWriter();

        aResponse.setContentType("text/html");

        HttpSession  session     = aRequest.getSession(true);
        DTagReplacer hp          = new DTagReplacer(HTML_FILE);
        String       domain      = "";
        String       hydUrl      = "";
        String       nycUrl      = "";
        StringBuffer domainLinks = new StringBuffer();

        try {
            domain = PropertiesHandler.getProperty(KEY_DOMAIN);
            hydUrl = PropertiesHandler.getProperty(KEY_HYDURL);
            nycUrl = PropertiesHandler.getProperty(KEY_NYCURL);
        } catch (Exception e) {
            domain = "";
            hydUrl = "";
            nycUrl = "";
        }

        domain = ((domain == null)
                  ? ""
                  : domain.trim());
        hydUrl = ((hydUrl == null)
                  ? ""
                  : hydUrl.trim());
        nycUrl = ((nycUrl == null)
                  ? ""
                  : nycUrl.trim());

        hp.replace("domainLinks", domainLinks.toString());
        hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));

        Object    obj = session.getAttribute(EXCEPTION_OBJECT);
        Exception e   = null;

        if (obj != null) {
            e = (Exception) obj;
            session.setAttribute(EXCEPTION_OBJECT, null);
        } else {
            e = new Exception("Unknown error.");
        }

        if (e instanceof TBitsException) {
            TBitsException de = (TBitsException) e;

            hp.replace("type", de.getType());
            hp.replace("message", de.getMessage());
            hp.replace("description", de.getDescription());
        } else if (e instanceof DatabaseException) {
            DatabaseException de = (DatabaseException) e;

            hp.replace("type", de.getType());
            hp.replace("message", de.getMessage());
            hp.replace("description", de.getDescription());
        } else {
            String str = e.toString();

            if (str.indexOf(":") > 0) {
                str = str.substring(str.indexOf(":") + 1);
            }

            hp.replace("type", "General");
            hp.replace("message", "Application Exception");
            hp.replace("description", str);
        }

        out.println(hp.parse(0));

        return;
    }
}
