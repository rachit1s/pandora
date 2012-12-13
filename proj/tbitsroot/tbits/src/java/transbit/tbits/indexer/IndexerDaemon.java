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

import transbit.tbits.Helper.TBitsPropEnum;

//TBits Imports
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.Mail;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;

//Imports from the current package.
import transbit.tbits.indexer.RequestIndexer;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_INDEXER;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_ATTACHMENTDIR;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

//~--- classes ----------------------------------------------------------------

/**
 * This class retrieves the Requests that are updated in the previous interval
 * and submits them for indexing.
 * TODO: this class should have shutdown hook such that it removes the lock and updates the timestamp correctly.
 * TODO: This updates the timestamp only once all the requests are updated. 
 * TODO: Rather it should update the timestamp as soon as a request is indexed. 
 * TODO: It updates the timestamp with the now() which wrong. It should ideally stamp the last updated time of the request which was just indexed.
 * TODO: A better technique would be to scan the index and figure out what is indexed and what is not.
 *
 * @author : Vaibhav.
 * @version : $Id: $
 */
public class IndexerDaemon implements TBitsPropEnum, Job {

    // Name of the logger.
    public static final String LOGGER_NAME = "indexer";

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(LOGGER_NAME, PKG_INDEXER);

    // File which contains the time when indexer dameon last ran successfully.
    private static final String LOCK_FILE = "indexer.lock";

    // File which contains the time when indexer dameon last ran successfully.
    private static String ourTimestampFile = "indexer.ts";

    // Subject of the mail report.
    public static String ourSubject = "TBits: Incremental Indexing Report";

    // Sender's Address.
    public static String ourFromAddress = "";

    // Receiver's address.
    public static String ourToAddress = "";

    // Location of temporary directory.
//    private static String ourTempDirectory = "";

    // Location where the Lucene Index is present.
    private static String ourPrimaryIndexLocation = "";

    // Current Domain
    private static String ourDomain;

    //~--- static initializers ------------------------------------------------

    static {
        try {
            ourTimestampFile = APIUtil.getTMPDir() + "/indexer.ts";
        } catch (Exception e) {
            LOG.fatal("Exception while finding the indexer.ts file: " + "",(e));
        }
    }

    //~--- fields -------------------------------------------------------------

    // Content of the report.
    public StringBuffer myContent = new StringBuffer();

    // Current time when this started running.
    private Timestamp myCurrentTime;

    // List of requests that are updated in the previous interval.

    // Time when the indexer ran last time successfully.
    private Timestamp myTimeLastRun;

    //~--- constructors -------------------------------------------------------

    /**
     * The only constructor that starts the proceedings.
     */
    public IndexerDaemon() {
        
    }

    public boolean index()
    {
        if (!readProperties())
            return false;
        if(!start())
            return false;
        
        if ((myContent != null) &&!myContent.toString().trim().equals("")) {

            // Send mail to the notification address.
            Mail.send(ourToAddress, ourFromAddress, ourSubject, myContent.toString());
        }
        return true;
    }
    //~--- methods ------------------------------------------------------------

    /**
     * This method creates a timestamp file.
     *
     * @throws Exception
     */
    private void createTimestampFile( ) throws Exception {
    	createTimestampFile(myCurrentTime);
    }
    /**
     * This method creates a timestamp file.
     *
     * @throws Exception
     */
    private void createTimestampFile(Timestamp timeStamp) throws Exception {
        File file = new File(ourTimestampFile);

        if (file.createNewFile() == false) {
            throw new Exception("Unable to create timestamp file.");
        }

        FileOutputStream fos = new FileOutputStream(file);

        fos.write(timeStamp.toCustomFormat("yyyyMMddHHmmss").getBytes());
        fos.close();
        LOG.debug("Timestamp file is created successfully...");
    }

    /**
     * This method removes the lock file.
     *
     * @param lockFile
     * @throws Exception
     */
    private void deleteLockFile(String lockFile) {
        File f = new File(lockFile);

        if (f.exists() == true) {
            if (f.delete() == false) {
                LOG.severe("Could not delete indexer.lock file...");
                System.out.println("Could not delete indexer.lock file...");
                myContent.append("Could not delete indexer.lock file...");
                return;
            }
            LOG.debug("Deleted the lock file used by indexer daemon: " + f.toString());
            //System.out.println("Deleted the lock file used by indexer daemon: " + f.toString());
        }
    }
    
    
    /**
     * This method removes the lock file 
     *
     * @param 
     * @throws Exception
     */
    public static void deleteLockFile() {
    	
    	IndexerDaemon indexer = new IndexerDaemon();
    	if(!indexer.readProperties()) return;
    	String lockfile = APIUtil.getTMPDir() + "/" + LOCK_FILE;
    	File f = new File(lockfile);

        if (f.exists() == true) {
            if (f.delete() == false) {
                LOG.severe("Could not delete indexer.lock file...");
                System.out.println("Could not delete indexer.lock file...");
                return;
            }
            LOG.debug("Deleted the lock file used by indexer daemon: " + f.toString());
        }
    }

    /**
     * This method retrieves a list of requests updated since the previous run
     * of the indexer daemon.
     */
    private ArrayList<ReqInfo> findUpdatedRequests() {
        ArrayList<ReqInfo> myRequestsList = new ArrayList<ReqInfo>();
        Connection con = null;
        try {
            String query = "stp_action_getUpdatedRequests " + myTimeLastRun.toSqlTimestamp();
            LOG.debug("Executing '" + query + "'");

            con = DataSourcePool.getConnection();

            CallableStatement cs = con.prepareCall("stp_action_getUpdatedRequests ?");

            LOG.info(myTimeLastRun.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
            cs.setTimestamp(1, myTimeLastRun.toSqlTimestamp());

            ResultSet rs    = cs.executeQuery();
            int       count = 0;

            while (rs.next() != false) {
                ReqInfo req = new ReqInfo();

                req.systemId  = rs.getInt("sys_id");
                req.requestId = rs.getInt("request_id");
                req.sysPrefix = rs.getString("sys_prefix");
                myRequestsList.add(req);
                count++;
            }

            rs.close();
            cs.close();
            LOG.info("Indexing " + count + " requests updated during " + "last interval.");
            System.out.println("Indexing " + count + " requests updated during " + "last interval.");
        } catch (SQLException sqle) {
            LOG.severe("",(sqle));
//            System.out.println("",(sqle));
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException sqle) {
                    LOG.warn("",(sqle));
                }
            }
        }
        return myRequestsList;
    }

    /**
     * The main method.
     */
    public static void main(String arg[]) {
        LOG.debug("Starting indexer daemon...");
        System.out.println("Starting indexer daemon...");
        IndexerDaemon id = new IndexerDaemon();
        boolean isSuccess = id.index();
        //return (isSuccess ? 0:1);
		return;
    }

    /**
     * This method read the properties required, from the Properties Handler.
     *
     * @return  True  if the operation is successful.
     *          False Otherwise.
     */
    public boolean readProperties() {
        boolean returnValue = true;

        try {
            LOG.debug("Reading the properties");

            // Load the properties.
            ourDomain               = PropertiesHandler.getProperty(KEY_DOMAIN);
            ourSubject              = ourSubject + " From " + ourDomain.toUpperCase() + ".";
            ourToAddress            = PropertiesHandler.getProperty(KEY_LOGGER_NOTIFY_TO);
            ourFromAddress          = PropertiesHandler.getProperty(KEY_LOGGER_NOTIFY_FROM);
            ourPrimaryIndexLocation = Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_INDEXDIR));
//            ourTempDirectory        = Configuration.findAbsolutePath(PropertiesHandler.getProperty(TBitsPropEnum.KEY_TMPDIR));
            System.setProperty("java.io.tmpdir", APIUtil.getTMPDir());
            System.setProperty("org.apache.lucene.lockDir", APIUtil.getTMPDir());
        } catch (Exception e) {

            // Any Exception during the above process is severe. Log It.
            LOG.severe("",(e));
            myContent.append("Exception while reading properties: " + TBitsLogger.getStackTrace(e));
            returnValue = false;
        }

        return returnValue;
    }

    /**
     * This method reads the last time the indexer daemon is run from the file.
     */
    private boolean readTimeLastRun() {
        try {
        	File file = new File(ourTimestampFile);

        	/*
        	 * If the file is not existing, then warn the caller and create
        	 * a new one and write the Unix epoch time and continue.
        	 */
        	if ((file == null) || (file.exists() == false)) {
        		LOG.warn("Timestamp file is not existing!");
        		createTimestampFile();
        		myTimeLastRun = new Timestamp(new Date(0).getTime());
        		return true;
        	}

        	FileInputStream fis    = new FileInputStream(file);
        	StringBuffer    buffer = new StringBuffer();
        	int             b      = 0;

        	while ((b = fis.read()) != -1) {
        		buffer.append((char) b);
        	}

        	fis.close();

        	try {
        		myTimeLastRun = new Timestamp(buffer.toString());
        	} catch (Exception e) {
        		LOG.severe("Exception while reading the time from the " + "timestamp file: " + "",(e));
        		createTimestampFile();
        		myTimeLastRun = new Timestamp(new Date(0).getTime());
        	}
        	return true;

        } catch (Exception e) {
        	StringBuffer message = new StringBuffer();

        	message.append("Exception occurred while retrieving the time ").append("last run: \n").append(TBitsLogger.getStackTrace(e));
        	LOG.severe(message.toString());
        	myContent.append(message.toString());

            return false;
        }
    }

    /**
     * Method that accomplishes the responsibility of this class.
     */
    private boolean start() {
        
        String lockFile = APIUtil.getTMPDir() + "/" + LOCK_FILE;
        LOG.debug("Checking if the lock file is present: " + lockFile);

        boolean isLocked = false;

        try {
            File f = new File(lockFile);

            /*
             * Create the lock file before proceeding further. If the lock file
             * already exists, then this API returns false.
             */
            if (f.createNewFile() == false) {
                LOG.severe("Lock file exists: " + lockFile);
                isLocked = true;

                return false;
            }

            LOG.debug("Lock file created successfully.");

            // Set the current time.
            setCurrentTime();

            /*
             * If we could not get the last time it ran successfully. Hence, there
             * is no point in continuing. So return.
             */
            if (readTimeLastRun() == false) {
                deleteLockFile(lockFile);
                LOG.info("Exiting without indexing...");
                return false;
            }

            ArrayList<ReqInfo> myRequestsList;
            
            // Get the list of requests that are updated after last run.
            myRequestsList = findUpdatedRequests();
            
            // Submit them for indexing.
            submitForIndexing(myRequestsList);

            // Update the file with the time when we started the process.
            updateTimeLastRun();

            // Bye...
        } catch (Exception e) {
            StringBuffer message = new StringBuffer();

            message.append("Exception while running the indexer daemon: ").append(TBitsLogger.getStackTrace(e));
            LOG.severe(message.toString());
            myContent.append("Exception during incremental indexing: \n").append(message.toString());

            return false;
        } finally {
            if (isLocked == false) {
                deleteLockFile(lockFile);
            }
        }
        return true;
    }

    /**
     *
     */
    public void submitForIndexing(ArrayList<ReqInfo> myRequestsList) {
        int size = myRequestsList.size();

        if (size == 0) {
            return;
        }

        for(Iterator<ReqInfo> iter = myRequestsList.iterator(); iter.hasNext(); )
        {
        	ReqInfo req = iter.next();
            RequestIndexer ri  = new RequestIndexer(req.sysPrefix, req.systemId, req.requestId, ourPrimaryIndexLocation);

            /*
             * We do not want to index in a thread.
             * So, we directly call the run method for synchronous indexing.
             */
            ri.run();
            iter.remove();
        }
        return;
    }

    /**
     * This method reads the last time the indexer daemon is run from the file.
     */
    private boolean updateTimeLastRun() {
        try {
            File   file     = new File(ourTimestampFile);
            String filePath = "";

            //
            // If the file is not existing, then warn the caller and create
            // a new one and write the current time and exit.
            //
            if (file == null) {
                LOG.warn("Timestamp file is not existing!");
                LOG.info("Creating a new one with current time.");
            } else {
                filePath = file.toString();
            }

            FileOutputStream fos = new FileOutputStream(filePath);

            fos.write(myCurrentTime.toGmtTimestamp().toCustomFormat("yyyyMMddHHmmss").getBytes());
            fos.close();
        } catch (Exception e) {
            StringBuffer message = new StringBuffer();

            message.append("Exception occurred while retrieving the time ").append("last run: \n").append(TBitsLogger.getStackTrace(e));
            LOG.severe(message.toString());
            myContent.append("\n").append(message.toString());

            return false;
        }

        return true;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * This method sets the current time.
     */
    private void setCurrentTime() {
        myCurrentTime = new Timestamp();
    }

    //~--- inner classes ------------------------------------------------------

    /**
     * This class holds the System Id and the Request Id of the request that
     * is modified in the previous interval.
     *
     * @author  Vaibhav.
     * @version $Id: $
     */
    private class ReqInfo {
        int    requestId;
        String sysPrefix;
        int    systemId;

        //~--- constructors ---------------------------------------------------

        /**
         * Default constructor.
         */
        public ReqInfo() {}

        //~--- methods --------------------------------------------------------

        /**
         * Overloading the toString() method for this class.
         *
         * @return String representation of this class.
         */
        public String toString() {
            return "[ " + sysPrefix + ", " + requestId + " ]";
        }
    }

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		LOG.debug("Starting indexer daemon...");
        System.out.println("Starting indexer daemon...");
        index();	
	}
}
