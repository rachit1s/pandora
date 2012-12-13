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
 * RefreshWebMapper.java
 *
 * $Header:
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.TBitsConstants.Source;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.webapps.WebUtil;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedInputStream;

import java.net.URL;
import java.net.URLConnection;

//~--- classes ----------------------------------------------------------------

/**
 * This method refreshes the web mapper only if called from an email invocation
 * or a command line invocation. It does nothing when called from a web
 * invocation.
 *
 * @author  Vaibhav
 * @version $Id: $
 *
 */
public class RefreshWebMapper implements TBitsPropEnum {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_UTIL);

    //~--- methods ------------------------------------------------------------

    /**
     * @param args
     */
    public static int main(String[] args) {
        RefreshWebMapper.refresh();
        return 0;
        //System.exit(0);
    }

    public static void refresh() {
        StringBuilder propValue = new StringBuilder();
        Source        source    = TBitsHelper.detectSource(propValue);

        if (source == Source.WEB) {

            /*
             * We won't refresh the web mapper from web process through this
             * method. Mapper.refresh() can be called directly instead.
             * So, return.
             */
            LOG.info("Since this is a web invocation, " + "please call Mapper.refresh instead.");

            return;
        }

        String urlRefreshMapper = WebUtil.getServletPath("refresh-mapper");

        try {
            URL           url = new URL(urlRefreshMapper);
            URLConnection con = url.openConnection();

            con.connect();
            LOG.info("A request has been sent to " + urlRefreshMapper + "...");

            BufferedInputStream bis     = new BufferedInputStream(con.getInputStream());
            StringBuffer        content = new StringBuffer();
            int                 ch      = -1;

            while ((ch = bis.read()) != -1) {
                content.append((char) ch);
            }

            LOG.info(content.toString());
            bis.close();
        } catch (Exception e) {
            LOG.severe("An exception has occurred while refreshing the mapper" + " in the web process running at " + urlRefreshMapper + "\n" + "",(e));
        }
    }
}
