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
 * Indexer.java
 *
 * $Header:
 */
package transbit.tbits.indexer;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.PropertiesHandler;

//Imports from other packages of TBits.
import transbit.tbits.common.Configuration;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;

//Imports from current package.
import transbit.tbits.indexer.RequestIndexer;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_INDEXER;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_INDEXDIR;

//~--- JDK imports ------------------------------------------------------------

//Java imports.
import java.util.ArrayList;

//~--- classes ----------------------------------------------------------------

/**
 * This class re-indexes a given businessarea.
 *
 * @author  Vaibhav
 * @version $Id: $
 */
public class ReIndex {

    // Name of the logger.
    public static final String LOGGER_NAME = "indexer";

    // Application Logger.
    public static final TBitsLogger LOG                     = TBitsLogger.getLogger(LOGGER_NAME, PKG_INDEXER);
    private static String           ourPrimaryIndexLocation = "";

    //~--- static initializers ------------------------------------------------

    /*
     * Static block that reads the application's property file and reads the
     * Index Location value.
     */
    static {
        try {
            ourPrimaryIndexLocation = Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_INDEXDIR));
        } catch (Exception e) {

            // Any Exception during the above process is severe. Log It.
            LOG.severe("",(e));
        }
    }

    //~--- fields -------------------------------------------------------------

    private String myBAList = "";

    //~--- constructors -------------------------------------------------------

    /**
     * Default constructor that takes a comma-separated list of business area
     * ids.
     */
    public ReIndex(String aBAList) {
        myBAList = aBAList;
    }

    /**
     * Constructor that takes a comma-separated list of business area ids and
     * the location of primary index.
     */
    public ReIndex(String aBAList, String aPrimaryIndexLocation) {
        myBAList                = aBAList;
        ourPrimaryIndexLocation = aPrimaryIndexLocation;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Main method - Only Entry point into the class from command line.
     */
    public static int main(String arg[]) {
        if ((arg.length != 1) && (arg.length != 2)) {
            StringBuffer usage = new StringBuffer();

            usage.append("Usage:\n").append("\tReIndex <BA Prefix List separated by commas>").append(" [<IndexLocation>]");
            System.err.println(usage);
            return 1;
        }

        if (arg.length == 1) {
            new ReIndex(arg[0]).startReIndexing();
        } else if (arg.length == 2) {
            new ReIndex(arg[0], arg[1]).startReIndexing();
        }

        return 0;
    }

    /**
     * Method that actually invokes the indexer.
     */
    private void startReIndexing() {
        if ((myBAList == null) || myBAList.trim().equals("")) {
            LOG.debug("No Business Areas Specified For Re-indexing....\n");
            LOG.debug("Exiting the process.....\n");

            return;
        }

        ArrayList<String> baList = Utilities.toArrayList(myBAList);

        if (baList == null) {
            LOG.debug("No Business Areas Specified For Re-indexing....\n");
            LOG.debug("Exiting the Re-Indexing process.....\n");

            return;
        }

        int listSize = baList.size();

        if (listSize == 0) {
            LOG.debug("No Business Areas Specified For Re-indexing....\n");
            LOG.debug("Exiting the Re-Indexing process.....\n");

            return;
        }

        for (String sysPrefix : baList) {
            try {
                RequestIndexer app = new RequestIndexer(sysPrefix, ourPrimaryIndexLocation);

                app.run();
            } catch (Exception e) {
                LOG.info("Invalid Business Area: " + sysPrefix + "\n" + "",(e));

                continue;
            }
        }
    }
}
