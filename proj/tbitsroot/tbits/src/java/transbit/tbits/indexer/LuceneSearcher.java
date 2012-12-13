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
 * LuceneSearcher.java
 *
 * $Header:
 */
package transbit.tbits.indexer;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;

//Lucene Imports.
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.PropertiesHandler;

//TBits Imports.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Field;

//Current Package Imports.
import transbit.tbits.indexer.TBitsAnalyzer;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_INDEXER;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_INDEXDIR;

//~--- JDK imports ------------------------------------------------------------

//Java Imports
import java.io.IOException;

import java.util.ArrayList;
import java.util.Hashtable;

//~--- classes ----------------------------------------------------------------

/**
 * This class searches the index given the query.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public class LuceneSearcher {

    // Name of the logger.
    public static final String LOGGER_NAME = "indexer";

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(LOGGER_NAME, PKG_INDEXER);

    // Location where the Lucene Index is present.
    private static String myIndexLocation = "";

    //~--- static initializers ------------------------------------------------

    /*
     * Static block that reads the application's property file and reads the
     * Index Location value.
     */
    static {
        IndexUtil.setLockDirToAppTmpDir();

        try {
            myIndexLocation = Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_INDEXDIR));
        } catch (Exception e) {
            LOG.severe("",(e));
        }
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method closes the given index searcher if it is not null.
     * @param aSearcher
     */
    private static void closeSearcher(IndexSearcher aSearcher) {
        if (aSearcher != null) {
            try {
                aSearcher.close();
            } catch (IOException ioe) {
                LOG.severe("",(ioe));
            }
        }
    }

    /**
     * This method is used to search from command line.
     *
     * @param store     Name of the store to be searched.
     * @param query     Lucene Query
     */
    public static void cmdSearch(String store, String query) {
        IndexSearcher aSearcher = null;
        int           length    = 0;
        Query         objQuery  = null;
        long          start     = 0,
                      open      = 0,
                      end       = 0,
                      time      = 0;

        try {
            start     = System.currentTimeMillis();
            aSearcher = new IndexSearcher(myIndexLocation + "/" + store);
            objQuery  = QueryParser.parse(query, "", new TBitsAnalyzer());
            open      = System.currentTimeMillis();

            Hits hits = aSearcher.search(objQuery);

            length = hits.length();

            for (int i = 0; i < length; i++) {
                System.out.println(hits.doc(i).get("sysPrefix") + "#" + hits.doc(i).get("requestId"));
            }
        } catch (Exception e) {
            StringBuffer message = new StringBuffer();

            message.append("\nYour Query: ").append(query).append("\nLucene's: ").append((objQuery != null)
                    ? objQuery.toString()
                    : "").append("\nOpened index and parsed query in ").append(open
                                 - start).append(" ms").append("\n(").append(length).append(") results found in ").append(time).append(" ms").append(TBitsLogger.getStackTrace(e));
            LOG.severe(message.toString());
        } finally {
            end  = System.currentTimeMillis();
            time = (end - open);
            closeSearcher(aSearcher);

            StringBuffer message = new StringBuffer();

            message.append("\nYour Query: ").append(query).append("\nLucene's: ").append((objQuery != null)
                    ? objQuery.toString()
                    : "").append("\nOpened index and parsed query in ").append(open - start).append(" ms").append("\n(").append(length).append(") results found in ").append(time).append(" ms");
            LOG.info(message.toString());
        }
    }

    /**
     * The Main Method - for testing.
     */
    public static void main(String arg[]) {
        if (arg.length != 2) {
            System.out.println("Usage: LuceneSearcher <Store> <Query>");
            System.out.println("Examples: ");
            System.out.println("    LuceneSearcher \"crm/RequestStore\" \"+sysPrefix:crm +(+all:vineet)\"");
            System.out.println("    LuceneSearcher \"crm/ActionStore\" \"+sysPrefix:crm +(+all:vineet)\"");
            return;
        }
        LuceneSearcher.cmdSearch(arg[0], arg[1]);
        return;
    }

    /**
     * This method searches in the request store of the lucene index.
     *
     * @param aQuery Lucene query to be run.
     *
     * @return ArrayList of request ids that matched the query.
     */
    public static ArrayList<String> search(String aPrefix, String aQuery, boolean aInActionStore) {
        IndexSearcher              aSearcher = null;
        int                        length    = 0;
        Query                      objQuery  = null;
        long                       start     = 0,
                                   open      = 0,
                                   end       = 0,
                                   time      = 0;
        ArrayList<String>          reqIdList = new ArrayList<String>();
        Hashtable<String, Boolean> idTable   = new Hashtable<String, Boolean>();
        String                     location  = myIndexLocation + "/" + aPrefix.toLowerCase() + "/" + ((aInActionStore == true)
                ? "ActionStore"
                : "RequestStore");

        try {
            start     = System.currentTimeMillis();
            aSearcher = new IndexSearcher(location);
            
            objQuery  = QueryParser.parse(aQuery, "", new TBitsAnalyzer());
            open      = System.currentTimeMillis();
            Hits hits = aSearcher.search(objQuery);

            length = hits.length();

            if (length == 0) {
                reqIdList.add("-1");
            } else {
                for (int i = 0; i < length; i++) {
                    String reqId = hits.doc(i).get(Field.REQUEST);

                    if (reqId != null) {
                        idTable.put(reqId, true);
                    }
                }

                reqIdList = new ArrayList<String>(idTable.keySet());
                length    = reqIdList.size();
            }
        } catch (Exception e) {
            StringBuffer message = new StringBuffer();

            message.append("\nLocation: ").append(location).append("\nTime to open index and parse query: ").append(open
                           - start).append("\nYour Query: ").append(aQuery).append("\nLucene's: ").append((objQuery != null)
                    ? objQuery.toString()
                    : "").append("\n(").append(length).append(") results found in ").append(time).append(" ms\n\n").append(TBitsLogger.getStackTrace(e));
            LOG.severe(message.toString());
            reqIdList = new ArrayList<String>();
            reqIdList.add("-1");
        } finally {
            end  = System.currentTimeMillis();
            time = end - open;
            closeSearcher(aSearcher);
            LOG.info("\nLocation: " + location + "\nTime to open index and parse query: " + (open - start) + "\nYour Query: " + aQuery + "\nLucene's: " + ((objQuery != null)
                    ? objQuery.toString()
                    : "") + "\n(" + length + ") results found in " + time + " ms");
        }

        return reqIdList;
    }
}
