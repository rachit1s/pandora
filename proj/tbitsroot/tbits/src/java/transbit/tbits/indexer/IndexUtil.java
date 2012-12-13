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
 * IndexUtil.java
 *
 * $Header:
 */
package transbit.tbits.indexer;

//~--- non-JDK imports --------------------------------------------------------

//Lucene Imports.
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import transbit.tbits.api.APIUtil;
import transbit.tbits.common.PropertiesHandler;

//TBits Imports
import transbit.tbits.common.Configuration;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;

import static transbit.tbits.Helper.TBitsConstants.LUCENE_DATE_FORMAT;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_INDEXER;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR;

//~--- JDK imports ------------------------------------------------------------

//Java Imports
import java.io.File;
import java.io.IOException;

//~--- classes ----------------------------------------------------------------

/**
 * This is an utility class that has functionality related to basic operations
 * over a lucene index.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public class IndexUtil {

    // Name of the logger.
    public static final String LOGGER_NAME = "indexer";

    // Application Logger.
    public static final TBitsLogger LOG              = TBitsLogger.getLogger(LOGGER_NAME, PKG_INDEXER);
    private static final int        ourIndexWaitTime = 600;

    //~--- static initializers ------------------------------------------------

    /*
     * Static block that reads the application's property file.
     */
    static {
        setLockDirToAppTmpDir();
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method checks the presence of a Lucene Index at the given location
     *
     * @param aLocation Location of index.
     * @param bCreate   Flag if the directory should be created if not present
     *
     * @exception IOException Throws IOException on error.
     */
    public synchronized static boolean checkIndex(String aLocation, boolean bCreate) throws IOException {
        File indexDir = new File(aLocation);

        // Check if the index directory is present.
        // If not, create it if bCreate is true.
        if (indexDir.exists() == false) {
            if (bCreate == true) {
                createIndex(aLocation);
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * This method creates an Index in the given location.
     *
     * @param aLocation  Location where index should be created.
     *
     * @exception IOException Throws IOException on error.
     */
    public synchronized static void createIndex(String aLocation) throws IOException {
        String reqStore = aLocation + "/RequestStore";
        String actStore = aLocation + "/ActionStore";

        createLuceneIndex(reqStore);
        createLuceneIndex(actStore);

        return;
    }

    /**
     * This method creates a Lucene Index in the given location.
     *
     * @param aLocation  Location where index should be created.
     *
     * @exception IOException Throws IOException on error.
     */
    private synchronized static void createLuceneIndex(String aLocation) throws IOException {

        // Location of the Index.
        String lStore = aLocation;

        // File object to the location of the store.
        File fStore = new File(lStore);

        // Create the Index Directory if it does not exist.
        if (fStore.exists() == false) {
            fStore.mkdirs();
        }

        // Create the segments directory also.
        File fSegments = new File(lStore + "/segments");

        if (fSegments.exists() == false) {
            fSegments.mkdirs();
        }

        // Create the lucene index in the request store location.
        IndexWriter iStore = new IndexWriter(lStore, new TBitsAnalyzer(), true);

        iStore.close();
        LOG.debug("Created a Index Store Successfully: " + lStore);

        return;
    }

    /**
     * This method deletes a given index directory.
     *
     * @param aDirectory Name of the temporary index directory to be deleted.
     */
    private static boolean delete(File aDirectory) {
        boolean status = true;

        try {
            if (aDirectory.exists() == true) {
                if (aDirectory.isFile() == true) {
                    status = aDirectory.delete();
                } else {
                    File[] list = aDirectory.listFiles();

                    for (int i = 0; i < list.length; i++) {
                        if (list[i].isFile() == true) {
                            list[i].delete();
                        } else if (list[i].isDirectory() == true) {
                            delete(list[i]);
                        }
                    }

                    status = aDirectory.delete();
                }
            } else {
                LOG.info("Directory does not exist: " + aDirectory.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }

    /**
     * This method delete the documents with the given Term(key, value) at
     * the specified index location.
     *
     * @param aLocation  Location of Primary Index
     * @param aTermKey   Key
     * @param aTermValue Value
     *
     * @return Status of Merge Operation.
     */
    public synchronized static int deleteDocuments(String aLocation, String aTermKey, String aTermValue) {
        IndexReader reader         = null;
//        IndexWriter writer         = null;
//        int         iterationCount = 0;
        int         deleteCount    = 0;

        try {
//            while (iterationCount < ourIndexWaitTime) {
//                try {
//                    if (IndexReader.isLocked(aLocation) == true) {
//                        try {
//                            Thread.sleep(1000);
//                        } catch (Exception e) {}
//                    } else {
//                        break;
//                    }
//                } catch (Exception e) {
//                    LOG.warn("",(e));
//                }
//
//                iterationCount++;
//            }
//
//            if (iterationCount == ourIndexWaitTime) {
//
//                //
//                // We came out of the above loop because we exceeded the
//                // maximum number of retries, so LOG this and return null.
//                //
//                LOG.warn("Timed out while opening IndexWriter." + "\nLocation: " + aLocation);
//
//                return -1;
//            } else {

                //
                // We came out of the above loop because we obtained lock on
                // the Index Directory. Delete the document.
                //
                try {

                    //
                    // We somehow found that the index is free now and no one
                    // is accessing it. Now, we can try reading the index. But,
                    // someone can obtain lock on the index in the meantime.
                    // And, we may end up with a stale version of index at the
                    // end of reading it which cannot be used to
                    // delete/undelete/setnorms on the index using this stale
                    // IndexReader. So, to lock this We open a writer and
                    // hold the lock on this till the end of reading operation
                    // and then release the lock.
                    //
//                    writer = new IndexWriter(aLocation, new TBitsAnalyzer(), false);
                    reader = IndexReader.open(aLocation);

                    Term term = new Term(aTermKey, aTermValue.toLowerCase());

                    LOG.debug("Deleting the documents with term: " + term.toString());

                    //
                    // Now we completed reading the index and the
                    // IndexReader is fresh to delete the required documents.
                    // Close the index because, delete operation
                    // cannot be done while a writer has lock on the index.
                    //
//                    writer.close();

                    // Quickly delete the docuemnt!!!!!!!!!!
                    deleteCount = reader.delete(term);

                    // Hah...
                } catch (IOException ioe) {
                    LOG.warn("",(ioe));

                    return -1;
                }
//            }
        } catch (Exception e) {
        	e.printStackTrace() ;
            LOG.warn("",(e));
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } 
            catch (IOException ioe) 
            {
            	ioe.printStackTrace() ;
            }
        }

        return deleteCount;
    }

    /**
     * This method deletes a given index directory.
     *
     * @param aLocation Name of the temporary index directory to be deleted.
     */
    public static boolean deleteIndex(String aLocation) {
        return delete(new File(aLocation));
    }

    /**
     * This is the main method, the entry point into the program.
     *
     * @param arg List of command-line arguments.
     */
    public static int main(String arg[]) {
        if (arg.length < 2) {
            System.out.println(printUsage());
            return 1;
        }

        try {
            String cmd  = arg[0];
            String arg1 = arg[1];

            if (arg.length == 2) {
                if (cmd != null) {
                    if (cmd.equalsIgnoreCase("create")) {
                        if (arg1 != null) {
                            IndexUtil.createIndex(arg1);
                        }
                    } else if (cmd.equalsIgnoreCase("exists")) {
                        if (arg1 != null) {
                            boolean flag = IndexUtil.checkIndex(arg1, false);

                            System.out.println("Check status: " + flag);
                        }
                    }
                }
            } else if (arg.length == 3) {
                if (cmd != null) {
                    if (cmd.equalsIgnoreCase("merge")) {
                        String arg2 = arg[2];

                        if ((arg1 != null) && (arg2 != null)) {
                            boolean flag = IndexUtil.merge(arg1, arg2);

                            System.out.println("Merging status: " + flag);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    /**
     * This method merges the index at Temp Location with the one at the
     * Primary Location.
     *
     * @param aPrimaryLocation  Location of Primary Index
     * @param aTempLocation     Location of Temporary Index
     *
     * @return Status of Merge Operation.
     *
     * @exception IOException
     */
    public synchronized static boolean merge(String aPrimaryLocation, String aTempLocation) throws IOException {
//        int         iterationCount = 0;
        IndexWriter writer         = null;

        try {
            Directory[] list = { FSDirectory.getDirectory(aTempLocation, false) };

//            while (iterationCount < ourIndexWaitTime) {
//                try {
//
//                    // Check if the index directory is locked.
//                    if (IndexReader.isLocked(aPrimaryLocation) == true) {
//                        try {
//                            Thread.sleep(1000);
//                        } catch (Exception e) {}
//                    } else {
//                        break;
//                    }
//                } catch (Exception e) {
//                    LOG.warn("",(e));
//                }
//
//                iterationCount++;
//            }

//            if (iterationCount == ourIndexWaitTime) {
//
//                //
//                // We came out of the above loop because we exceeded the
//                // maximum number of retries, so LOG this and return null.
//                //
//                LOG.warn("Timed out while opening IndexWriter." + "\nLocation: " + aPrimaryLocation);
//
//                return false;
//            } else {

                //
                // We came out of the above loop because we obtained lock on
                // the Index Directory. Open a writer and return it.
                //
                try {
                    writer = new IndexWriter(aPrimaryLocation, new TBitsAnalyzer(), false);
                    writer.addIndexes(list);
                } catch (IOException ioe) {
                    throw ioe;
                }
//            }
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe) {
                    LOG.warn("",(ioe));

                    return false;
                }
            }
        }

        return true;
    }

    /**
     * This method opens an IndexWriter to the given location.
     *
     * @param aPrimaryLocation  Location of Index
     *
     * @return IndexWriter.
     */
    public synchronized static IndexWriter openWriter(String aPrimaryLocation) {
        IndexWriter writer         = null;
//        int         iterationCount = 0;

//        while (iterationCount < ourIndexWaitTime) {
//            try {
//
//                // Check if the index directory is locked.
//                if (IndexReader.isLocked(aPrimaryLocation) == true) {
//                    try {
//
//                        // Wait for a second if it is really locked.
//                        Thread.sleep(1000);
//                    } catch (InterruptedException ie) {
//                        LOG.warn("",(ie));
//                    }
//                } else {
//                    break;
//                }
//            } catch (Exception e) {
//                LOG.warn("",(e));
//            }
//
//            iterationCount++;
//        }
//
//        if (iterationCount == ourIndexWaitTime) {
//
//            //
//            // We came out of the above loop because we exceeded the maximum
//            // number of retries, so LOG this and return null.
//            //
//            LOG.warn("Timed out while opening IndexWriter." + "\nLocation: " + aPrimaryLocation);
//
//            return null;
//        } else {

            //
            // We came out of the above loop because we obtained lock on the
            // Index Directory. Open a writer and return it.
            //
            try {
//            	System.out.println("Nitiraj : IndexUtil : Trying to open a new IndexWriter");
                writer = new IndexWriter(aPrimaryLocation, new TBitsAnalyzer(), false);
//                System.out.println("Nitiraj : IndexUtil : Opened new IndexWriter");
                return writer;
            } catch (IOException ioe) {
                LOG.warn("Error while opening a writer." + "",(ioe));
            }
//        }

        return null;
    }

    /**
     *
     */
    public static String printUsage() {
        StringBuilder message = new StringBuilder();

        message.append("Usage:").append("\n    IndexUtil <options>\n").append("\n    options are").append("\n        create <index location>").append("\n        exists <index location>").append(
            "\n        merge  <primary location> <temporary location>").append("\n");

        return message.toString();
    }

    public static String toLuceneBoolean(boolean value) {
        if (value == true) {
            return "true";
        }

        return "false";
    }

    /**
     * This method returns the date in the format used for indexing/searching
     * in lucene.
     *
     * @param ts
     *
     * @return Date in format indexed in lucene.
     */
    public static String toLuceneDate(Timestamp ts) {
        if (ts == null) {
            return "00000000000000";
        }

        // yyyyMMddHHmmss
        return ts.toCustomFormat(LUCENE_DATE_FORMAT);
    }

    /**
     * This method returns the real in the format used for indexing/searching
     * in lucene.
     *
     * @param value
     *
     * @return Double in the format as indexed in lucene.
     */
    public static String toLuceneDouble(double value) {
        String strValue = Double.toString(value);

        if (strValue.indexOf('.') < 0) {
            strValue = strValue + ".0";
        }

        /*
         * dot here needs to be escaped as it is a special character in
         * Posix Regular expression language.
         */
        String[]     parts    = strValue.split("\\.");
        String       left     = parts[0];
        String       right    = parts[1];
        int          len      = 0;
        int          stuffLen = 0;
        StringBuffer stuff    = new StringBuffer();

        len      = left.length();
        stuffLen = 32 - len;

        for (int i = 0; i < stuffLen; i++) {
            stuff.append("0");
        }

        left     = stuff.toString() + left;
        len      = 0;
        stuffLen = 0;
        stuff    = new StringBuffer();
        len      = right.length();
        stuffLen = 32 - len;

        for (int i = 0; i < stuffLen; i++) {
            stuff.append("0");
        }

        right    = right + stuff.toString();
        strValue = left + "." + right;

        return strValue;
    }

    /**
     * This method returns the integer in the format used for indexing/searching
     * in lucene.
     *
     * @param value
     *
     * @return Integer in the format as indexed in lucene.
     */
    public static String toLuceneInt(int value) {
        String       strValue = Integer.toString(value);
        int          len      = strValue.length();
        int          stuffLen = 32 - len;
        StringBuffer stuff    = new StringBuffer();

        for (int i = 0; i < stuffLen; i++) {
            stuff.append("0");
        }

        return stuff.toString() + strValue;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * This method sets the java.io.tmpdir and lucene's lock dir to the
     * application's tmp directory.
     */
    public static void setLockDirToAppTmpDir() {
        try {

            // Get the TBITS_HOME property.
            String tempDir = APIUtil.getTMPDir();//Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_TMPDIR));
            File   f       = new File(tempDir);

            if (f.exists() == false) {

                //
                // In case, the user TBits runs as, has no permissions to
                // create temp directory, then mkdirs() throws and
                // exception
                //
                f.mkdirs();
            }

            //
            // At this point, TBITS_HOME/temp is either present from
            // the beginning or is successfully created.
            //
            System.setProperty("java.io.tmpdir", tempDir);
            System.setProperty("org.apache.lucene.lockDir", tempDir);
        } catch (Exception e) {

            // Any Exception during the above process is severe. Log It.
            LOG.severe("",(e));
        }
    }
}
