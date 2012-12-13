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
 * RefreshMapper.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.SysPrefixes;

//Imports from TBits.
import transbit.tbits.api.Mapper;
import transbit.tbits.api.RuleFactory;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.CaptionsProps;
import transbit.tbits.domain.BAMailAccount;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

//~--- JDK imports ------------------------------------------------------------

//Java Imports
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//~--- classes ----------------------------------------------------------------

/**
 * This class refreshes the mapper.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public class RefreshMapper extends HttpServlet {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

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

        aResponse.setContentType("text/xml");
        out.println("<TBits>");

        String data = aRequest.getParameter("data");

        if ((data == null) || data.trim().equals("")) {
            Mapper.refreshUserMapper();
            Mapper.refreshBOMapper();
            PropertiesHandler.reload();
            SysPrefixes.reload();
            CaptionsProps.reloadCaptions();
            BAMailAccount.refreshAccounts();
            out.println("<Message>" + "User Mapper, Business Object mapper, Properties " + "and Prefixes files refreshed successfully." + "</Message>");
        } else if (data.equals("bo")) {
            Mapper.refreshBOMapper();
            BAMailAccount.refreshAccounts();
            out.println("<Message>" + "Business Object mapper is refreshed successfully." + "</Message>");
        } else if (data.equals("user")) {
            Mapper.refreshUserMapper();
            out.println("<Message>" + "User mapper is refreshed successfully." + "</Message>");
        } else if (data.equals("gc")) {
            Mapper.runGC();
            out.println("<Message>" + "Garbage collector is invoked successfully." + "</Message>");
        } else if (data.equals("prop")) {
            PropertiesHandler.reload();
            SysPrefixes.reload();
            CaptionsProps.reloadCaptions();
            out.println("<Message>" + "Properties and Prefixes files reloaded successfully." + "</Message>");
        } else if (data.equals("msg")) {
            Messages.reload();
            out.println("<Message>" + "Message strings reloaded successfully." + "</Message>");
        } else if (data.equals("dtags")) {
            DTagReplacer.clearFileContentMap();
            out.println("<Message>" + "DTagReplacer's content map cleared." + "</Message>");
        }
        else if(data.equals("rules"))
        {
        	RuleFactory.refreshFactory();
        	out.println("<Message>" + "Rules got refreshed." + "</Message>");
        }

        out.println("<Help>");
        out.println(getXMLHelp());
        out.println("</Help></TBits>");
    }

    //~--- get methods --------------------------------------------------------

    private String getXMLHelp() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("<Param name='data' value='bo'>")
        .append("Refreshes the Business Objects Mapper.")
        .append("</Param>")
        .append("<Param name='data' value='user'>")
        .append("Refreshes the User Mapper.")
        .append("</Param>")
        .append("<Param name='data' value='gc'>")
        .append("Invokes the Garbage Collector.")
        .append("</Param>")
        .append("<Param name='data' value='prop'>")
        .append("Reloads the properties files.")
        .append("</Param>").append("<Param name='data' value='msg'>")
        .append("Reloads the message strings.")
        .append("</Param>")
        .append("<Param name='data' value='dtags'>")
        .append("Reloads the html interfaces.")
        .append("</Param>")
//        .append("<Param name='data' value='dtags'>")
//        .append("Reloads the rules.")
//        .append("</Param>")
        ;
        return buffer.toString();
    }
}
