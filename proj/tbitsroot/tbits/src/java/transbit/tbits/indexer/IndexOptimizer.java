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
 * IndexOptimizer.java
 *
 * $Header:
 *
 */
package transbit.tbits.indexer;

//~--- non-JDK imports --------------------------------------------------------

//Lucene Imports
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.Mail;
import transbit.tbits.common.PropertiesHandler;

//TBits Imports
import transbit.tbits.common.TBitsLogger;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_INDEXER;

//~--- JDK imports ------------------------------------------------------------

//Java Imports
import java.io.File;
import java.io.IOException;

//~--- classes ----------------------------------------------------------------

/**
 * This class is used for optimizing the index.
 *
 * @author   Vaibhav.
 * @version  $Id: $
 *
 */
public class IndexOptimizer implements TBitsPropEnum {

    // Name of the logger.
    public static final String LOGGER_NAME = "indexer";

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(LOGGER_NAME, PKG_INDEXER);

    // Subject of the mail report.
    public static String     ourSubject       = "TBits: Optimizing lucene index";
    private static final int ourIndexWaitTime = 600;
    public static String     ourToAddress     = "";
    public static String     ourFromAddress   = "";
    private static String    ourDomain;
    private static String    ourPrimaryIndexLocation;
//    private static String    ourTempLocation;

    //~--- fields -------------------------------------------------------------

    public StringBuffer myContent = new StringBuffer();

    //~--- constructors -------------------------------------------------------

    public IndexOptimizer() {
         if (readProperties() == true) {
            String indexLocation = ourPrimaryIndexLocation;
            File   file          = new File(indexLocation);

            /*
             * Get the List of directories in this directory.
             */
            File[] list = file.listFiles();

            if (list != null) {
                int len = list.length;

                /*
                 * Now each directory contains the index for that business area.
                 */
                for (int i = 0; i < len; i++) {
                    File prefixDir = list[i];

                    if (prefixDir != null) {

                        /*
                         * Directories within the index directory for a BA
                         * contain two stores:
                         *      - Request Store.
                         *      - Action Store.
                         */
                        File[] storeList = prefixDir.listFiles();

                        if (storeList != null) {
                            int sLen = storeList.length;

                            for (int j = 0; j < sLen; j++) {
                                File store = storeList[j];

                                if (store.isDirectory() == true) {

                                    /*
                                     * Send this to the optimizeIndex method.
                                     */
                                    String path = store.toString();

                                    myContent.append(optimizeIndex(path));
                                }    // store is not a directory.
                            }        // End For
                        }            // prefixDir is not a directory or has no contents
                    }                // prefixDir is null.
                }                    // End For
            }                        // indexLocation is not a directory or has no contents.
        }

        if ((myContent != null) &&!myContent.toString().trim().equals("")) {

            // Send mail to the notification address.
            Mail.send(ourToAddress, ourFromAddress, ourSubject, myContent.toString());
        }
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Main method.
     *
     * @param arg Command line arguments.
     */
    public static int main(String arg[]) {
        new IndexOptimizer();
        return 0;
    }

    /**
     * This method is used to optimize the lucene index.
     */
    public static String optimizeIndex(String indexLocation) {
        StringBuilder error          = new StringBuilder();
        IndexWriter   writer         = null;
        int           iterationCount = 0;

        LOG.debug("Location of index: " + indexLocation);

        while (iterationCount < ourIndexWaitTime) {
            try {

                // Check if the index directory is locked.
                if (IndexReader.isLocked(indexLocation) == true) {
                    try {

                        // Wait for a second if it is locked.
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        LOG.warn("",(ie));
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                LOG.warn("",(e));

                return "";
            }

            iterationCount++;
        }

        if (iterationCount == ourIndexWaitTime) {

            //
            // We came out of the above loop because we exceeded the maximum
            // number of retries, so LOG this and return null.
            //
            StringBuffer message = new StringBuffer();

            LOG.warn(message.toString());
            message.append("\n").append("Timed out while opening IndexWriter for optimization.").append("\nLocation: ").append(indexLocation);
            error.append(message.toString());

            return error.toString();
        } else {

            //
            // We came out of the above loop because we obtained lock on the
            // Index Directory. Open a writer and return it.
            //
            if (iterationCount != 0) {
                LOG.debug("Waited for " + iterationCount + " seconds before " + "obtaining the lock on Index...");
            }

            try {
                writer = new IndexWriter(indexLocation, new TBitsAnalyzer(), false);
                writer.optimize();
                LOG.info("Optimization completed: " + indexLocation);
            } catch (Exception e) {
                StringBuffer message = new StringBuffer();

                message.append("\n").append("Exception while optimizing the index:\n").append(TBitsLogger.getStackTrace(e));
                error.append(message.toString());
            } finally {
                if (writer != null) {
                    try {
                        LOG.debug("Closing the Index Writer...");
                        writer.close();
                    } catch (IOException e) {
                        LOG.warn("",(e));
                    }
                }
            }
        }

        return error.toString();
    }

    /**
     * This method reads the properties needed to send a report of this process.
     *
     */
    private boolean readProperties() {
        boolean returnValue = true;

        try {
            ourDomain               = PropertiesHandler.getProperty(KEY_DOMAIN);
            ourSubject              = ourSubject + " at " + ourDomain.toUpperCase() + ".";
            ourToAddress            = PropertiesHandler.getProperty(KEY_LOGGER_NOTIFY_TO);
            ourFromAddress          = PropertiesHandler.getProperty(KEY_LOGGER_NOTIFY_FROM);
            ourPrimaryIndexLocation = Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_INDEXDIR));
//            ourTempLocation         = Configuration.findAbsolutePath(PropertiesHandler.getProperty(TBitsPropEnum.KEY_TMPDIR));
            System.setProperty("java.io.tmpdir", APIUtil.getTMPDir());
            System.setProperty("org.apache.lucene.lockDir", APIUtil.getTMPDir());
        } catch (Exception e) {

            // Any Exception during the above process is severe. Log It.
            LOG.warn("",(e));
            myContent.append("Exception while reading properties: " + TBitsLogger.getStackTrace(e));
            returnValue = false;
        }

        return returnValue;
    }
}
