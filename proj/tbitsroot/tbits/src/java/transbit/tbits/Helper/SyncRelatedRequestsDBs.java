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
 * SyncRelatedRequestsDBs.java
 *
 * $Header:
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

//TBits Imports.
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.Mail;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.TBitsInstance;
import transbit.tbits.domain.RelatedRequest;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Hashtable;

//~--- classes ----------------------------------------------------------------

/**
 * This class checks the records related to Related-Requests and
 * Transferred-Requests across instances and syncs them appropriately.
 *
 * @author  : Vinod Gupta
 * @version : $Id: $
 */
public class SyncRelatedRequestsDBs implements TBitsPropEnum {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_UTIL);

    // XML File that holds the attributes of instances.
    public static final String INSTANCES_XML = "etc/instances.xml";

    // Subject of the mail report.
    public static final String SUBJECT        = "Sync'ing TBits related requests databases.";
    public static String       ourDriverClass = "";
    public static String       ourDriverTag   = "";
    public static String       ourDbUser      = "";
    public static String       ourDbPass      = "";
    public static String       ourToAddress   = "";
    public static String       ourFromAddress = "";

    //~--- fields -------------------------------------------------------------

    public StringBuffer                                        myContent               = new StringBuffer();
    public Hashtable<TBitsInstance, ArrayList<RelatedRequest>> myPrimaryRRExistingList = new Hashtable<TBitsInstance, ArrayList<RelatedRequest>>();
    public Hashtable<TBitsInstance, ArrayList<RelatedRequest>> myRelatedRRExistingList = new Hashtable<TBitsInstance, ArrayList<RelatedRequest>>();
    public Hashtable<TBitsInstance, ArrayList<RelatedRequest>> myInsertList            = new Hashtable<TBitsInstance, ArrayList<RelatedRequest>>();
    public Hashtable<TBitsInstance, ArrayList<RelatedRequest>> myDeleteList            = new Hashtable<TBitsInstance, ArrayList<RelatedRequest>>();
    public ArrayList<TBitsInstance>                            myInstances;

    //~--- constructors -------------------------------------------------------

    /**
     * The only constructor of this class that starts the proceedings.
     */
    public SyncRelatedRequestsDBs() {
        start();
    }

    //~--- methods ------------------------------------------------------------

    /*
     */
    public void insertAndDelete(TBitsInstance aInstance, ArrayList<RelatedRequest> aInsertList, ArrayList<RelatedRequest> aDeleteList) {
        StringBuilder batch = new StringBuilder();

        if (aDeleteList != null) {
            for (RelatedRequest rr : aDeleteList) {
                batch.append("delete from related_requests").append(" where (primary_sys_prefix = '").append(rr.getPrimarySysPrefix()).append("' and primary_request_id = ").append(
                    rr.getPrimaryRequestId()).append(" and primary_action_id = ").append(rr.getPrimaryActionId()).append(" and related_sys_prefix = '").append(rr.getRelatedSysPrefix()).append(
                    "' and related_request_id = ").append(rr.getRelatedRequestId()).append(" and related_action_id = ").append(rr.getRelatedActionId()).append(")\n");
            }
        }

        if (aInsertList != null) {
            for (RelatedRequest rr : aInsertList) {
                batch.append("Insert into related_requests values").append("('").append(rr.getPrimarySysPrefix()).append("',").append(rr.getPrimaryRequestId()).append(",").append(
                    rr.getPrimaryActionId()).append(",'").append(rr.getRelatedSysPrefix()).append("',").append(rr.getRelatedRequestId()).append(",").append(rr.getRelatedActionId()).append(")\n");
            }
        }

        Connection con = null;

        try {
            con = DataSourcePool.getConnection(aInstance.getDBServer(), aInstance.getDBName(), ourDbUser, ourDbPass, ourDriverClass, ourDriverTag);

            Statement stmt = con.createStatement();

            stmt.addBatch(batch.toString());
            stmt.executeBatch();
            stmt.close();
            stmt = null;

            if (batch.length() > 0) {
                myContent.append("\n----------------------------------------------").append("\nSynced into Instance: ").append(aInstance.toString()).append("\n").append(batch.toString()).append(
                    "\n--------------------------------------------\n");
            }
        } catch (Exception e) {
            StringBuffer message = new StringBuffer();

            message.append("Exception occurred while Syncing the ").append("instance: ").append(aInstance.toString()).append("\n").append(TBitsLogger.getStackTrace(e));

            // Log this to the application logger.
            LOG.error(message.toString());

            // Append this to the mail.
            myContent.append(message.toString());

            return;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {}
            }
        }

        return;
    }

    /**
     * Main method
     *
     * @param arg
     */
    public static int main(String arg[]) {
        new SyncRelatedRequestsDBs();
        return 0;//System.exit(0);
    }

    private int start() {

        /*
         * Step 1:
         *      Obtain the driver and db details.
         */
        if (getProperties() == false) {
            return 1;// System.exit(1);
        }

        LOG.debug("Obtained database details....");

        /*
         * Step 2:
         *      Read the XML that contains the details of the instances.
         */
        String xml = "";

        try {
            xml = getInstancesXML();
        } catch (Exception e) {
            LOG.error("Exception while reading the instances.xml file: " + "",(e));
            return 1;
            //System.exit(1);
        }

        LOG.debug("Read instances.xml....");

        /*
         * Step 3:
         *      Parse the XML that contains the details of the instances.
         */
        try {
            myInstances = TBitsInstance.xmlDeSerialize(xml);
        } catch (Exception e) {
            LOG.error("Exception while de-serializing the XML:" + "",(e));
            return 1;
        }

        LOG.debug("Parsed the xml data....");

        if ((myInstances == null) || (myInstances.size() < 2)) {
            LOG.error("No instances found.");
            return 1;//System.exit(1);
        }

        syncDatabases();

        if ((myContent != null) && (myContent.toString().trim().equals("") == false)) {
            Mail.send(ourToAddress, ourFromAddress, SUBJECT, myContent.toString());
        }
        return 0;
    }

    /**
     * This method runs the algorithm for syncing the databases.
     *
     */
    private void syncDatabases() {
        for (TBitsInstance instance : myInstances) {
            getExistingList(instance);
        }

        for (TBitsInstance instance : myInstances) {
            getInsertList(instance);
        }

        for (TBitsInstance instance : myInstances) {
            getDeleteList(instance);
        }

        for (TBitsInstance instance : myInstances) {
            insertAndDelete(instance, myInsertList.get(instance), myDeleteList.get(instance));
        }
    }

    //~--- get methods --------------------------------------------------------

    /*
     */
    public void getDeleteList(TBitsInstance aInstance) {
        ArrayList<RelatedRequest> list = myRelatedRRExistingList.get(aInstance);

        if (list == null) {
            return;
        }

        ArrayList<RelatedRequest> combinedList = new ArrayList<RelatedRequest>();

        for (TBitsInstance instance : myInstances) {
            if (instance.equals(aInstance) == false) {
                ArrayList<RelatedRequest> tempList = myPrimaryRRExistingList.get(instance);

                if (tempList == null) {
                    continue;
                }

                for (RelatedRequest rr : tempList) {
                    if (combinedList.contains(rr) == false) {
                        combinedList.add(rr);
                    }
                }
            }
        }

        ArrayList<RelatedRequest> rrList = new ArrayList<RelatedRequest>();

        for (RelatedRequest rr : list) {
            if (combinedList.contains(rr) == false) {
                rrList.add(rr);
            }
        }

        if (rrList.size() > 0) {
            myDeleteList.put(aInstance, rrList);
        }

        return;
    }

    /*
     */
    public void getExistingList(TBitsInstance aInstance) {
        Connection con = null;

        try {
            con = DataSourcePool.getConnection(aInstance.getDBServer(), aInstance.getDBName(), ourDbUser, ourDbPass, ourDriverClass, ourDriverTag);

            ArrayList<RelatedRequest> rrList = new ArrayList<RelatedRequest>();
            Statement                 stmt   = con.createStatement();
            String                    query  = "SELECT * FROM related_requests where " + "primary_sys_prefix in " + "(select sys_prefix from business_areas where is_active=1)";
            ResultSet                 rs     = stmt.executeQuery(query);

            if (rs != null) {
                while (rs.next() == true) {
                    RelatedRequest rr = RelatedRequest.createFromResultSet(rs);

                    rrList.add(rr);
                }

                rs.close();
                rs = null;
            }

            myPrimaryRRExistingList.put(aInstance, rrList);
            stmt.close();
            stmt   = null;
            rrList = new ArrayList<RelatedRequest>();
            stmt   = con.createStatement();
            query  = "SELECT * FROM related_requests where " + "primary_sys_prefix not in " + "(select sys_prefix from business_areas where is_active=1)";
            rs     = stmt.executeQuery(query);

            if (rs != null) {
                while (rs.next() == true) {
                    RelatedRequest rr = RelatedRequest.createFromResultSet(rs);

                    rrList.add(rr);
                }

                rs.close();
                rs = null;
            }

            myRelatedRRExistingList.put(aInstance, rrList);
            stmt.close();
            stmt = null;
        } catch (Exception e) {
            StringBuffer message = new StringBuffer();

            message.append("Exception occurred while loading info of the ").append("instance: ").append(aInstance.toString()).append("\n").append(TBitsLogger.getStackTrace(e));

            // Log this to the application logger.
            LOG.error(message.toString());

            // Append this to the mail.
            myContent.append(message.toString());
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {}
            }
        }
    }

    /*
     */
    public void getInsertList(TBitsInstance aInstance) {
        ArrayList<RelatedRequest> list = myRelatedRRExistingList.get(aInstance);

        if (list == null) {
            return;
        }

        ArrayList<RelatedRequest> combinedList = new ArrayList<RelatedRequest>();

        for (TBitsInstance instance : myInstances) {
            if (instance.equals(aInstance) == false) {
                ArrayList<RelatedRequest> tempList = myPrimaryRRExistingList.get(instance);

                if (tempList == null) {
                    continue;
                }

                for (RelatedRequest rr : tempList) {
                    if (combinedList.contains(rr) == false) {
                        combinedList.add(rr);
                    }
                }
            }
        }

        ArrayList<RelatedRequest> rrList = new ArrayList<RelatedRequest>();

        for (RelatedRequest rr : combinedList) {
            if (list.contains(rr) == false) {
                rrList.add(rr);
            }
        }

        if (rrList.size() > 0) {
            myInsertList.put(aInstance, rrList);
        }

        return;
    }

    /**
     * This method read the instances.xml file and returns the content xml.
     *
     * @return XML content from instances.xml file.
     */
    private String getInstancesXML() throws Exception {
        StringBuffer   buffer = new StringBuffer();
        File           file   = Configuration.findPath(INSTANCES_XML);
        BufferedReader br     = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String         str    = "";

        while ((str = br.readLine()) != null) {
            buffer.append(str).append("\n");
        }

        br.close();

        return buffer.toString();
    }

    /**
     * This method reads the required properties from the Properties Handler.
     *
     * @return true  if the operation was successful.
     *         false otherwise.
     */
    private boolean getProperties() {

        // Get the driver details from the properties handler.
        try {
            ourDriverClass = PropertiesHandler.getProperty(KEY_DRIVER_NAME);
            ourDriverTag   = PropertiesHandler.getProperty(KEY_DRIVER_TAG);
            ourDbUser      = PropertiesHandler.getProperty(KEY_DB_LOGIN);
            ourDbPass      = PropertiesHandler.getProperty(KEY_DB_PASSWORD);
            ourToAddress   = PropertiesHandler.getProperty(KEY_LOGGER_NOTIFY_TO);
            ourFromAddress = PropertiesHandler.getProperty(KEY_LOGGER_NOTIFY_FROM);

            return true;
        } catch (Exception e) {
            LOG.severe("Exception while retrieving the Driver details." + "",(e));
        }

        return false;
    }
}
