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
 * SyncTBitsUserDBs.java
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
import transbit.tbits.domain.User;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Hashtable;

//~--- classes ----------------------------------------------------------------

/**
 * This class synchornizes the user databases across different instances of
 * TBits. One of the TBits instances is termed primary.Changes in the
 * attributes of the users are updated in all the instances.  But new users
 * from ActiveDirectory are added only to this primary instances. Hence is the
 * need to sync the user databases. Following are the steps followed to
 * accomplish this:
 *   1. Get the database driver details.
 *   2. Read the XML that contains the details of the instances.
 *   3. Parse this XML.
 *   4. Segregate the instances into Primary and Secondary.
 *   5. Get the Max User Id from the primary instance.
 *   6. Maintain eTable of [MaxUserId, UserList] where userList contains the
 *      users between MaxUserId and PrimMaxUserId.
 *   7. For each secondary instance
 *      a. Get the max user id from its User DB and call it secMaxUserId.
 *      b. If the secMaxUserId is less than primMaxUserId,
 *         1. Check if there is an entry in the eTable. If present goto (c).
 *         2. If not, get the userlist from the primary instance.
 *         3. Put the userList in the eTable keyed by secMaxUserId.
 *      c. Insert users in the list into the user db of the secondary instance.
 *   8. Prepare a report to be sent to the notification address.
 *
 *
 * @author : Vaibhav.
 * @version : $Id: $
 */
public class SyncTBitsUserDBs implements TBitsPropEnum {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_UTIL);

    // XML File that holds the attributes of instances.
    public static final String INSTANCES_XML = "etc/instances.xml";

    // Subject of the mail report.
    public static final String SUBJECT        = "Sync'ing TBits user databases.";
    public static String       ourDriverClass = "";
    public static String       ourDriverTag   = "";
    public static String       ourDbUser      = "";
    public static String       ourDbPass      = "";
    public static String       ourToAddress   = "";
    public static String       ourFromAddress = "";

    //~--- fields -------------------------------------------------------------

    public StringBuffer             myContent = new StringBuffer();
    public TBitsInstance            myPrimInstance;
    public ArrayList<TBitsInstance> mySecInstances;

    //~--- constructors -------------------------------------------------------

    /**
     * The only constructor of this class that starts the proceedings.
     */
    public SyncTBitsUserDBs() {
        mySecInstances = new ArrayList<TBitsInstance>();
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method to insert a User object into database.
     *
     * @param aObject Object to be inserted
     */
    private boolean insert(User aObject, Connection aCon) throws Exception {
        boolean           returnValue = false;
        CallableStatement cs          = aCon.prepareCall("stp_dbSync_insertUser  " + "?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?  ");

        aObject.setCallableParameters(cs);
        cs.execute();
        cs.close();
        returnValue = true;

        return returnValue;
    }

    /**
     * Main method
     *
     * @param arg
     */
    public static int main(String arg[]) {
       SyncTBitsUserDBs syncTbitsUserDBs = new SyncTBitsUserDBs();
       return syncTbitsUserDBs.start();
    }

    public int start() {

        /*
         * Step 1:
         *      Obtain the driver and db details.
         */
        if (getProperties() == false) {
            return 1;
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
        }

        LOG.debug("Read instances.xml....");

        /*
         * Step 3:
         *      Parse the XML that contains the details of the instances.
         */
        ArrayList<TBitsInstance> list = null;

        try {
            list = TBitsInstance.xmlDeSerialize(xml);
        } catch (Exception e) {
            LOG.error("Exception while de-serializing the XML:" + "",(e));

            return 1;
        }

        LOG.debug("Parsed the xml data....");

        /*
         * Step 4:
         *      Segregate the list into prim and sec instances.
         */
        for (TBitsInstance di : list) {
            if ((di.getIsPrimary() == true) && (myPrimInstance == null)) {
                myPrimInstance = di;
            } else {
                mySecInstances.add(di);
            }
        }

        // Check if there is a primary instance.
        if (myPrimInstance == null) {
            LOG.error("No primary instance is found.");
            return 1;
        }

        LOG.debug("Segregated the instances....");

        /*
         * Step 5:
         *      Get the max user id from the primary instance.
         */
        int primMaxUserId = -1;

        try {
            primMaxUserId = getMaxUserId(myPrimInstance);
        } catch (Exception e) {
            LOG.error("Exception occurred while retrieving the max user id " + "from the primary instance: " + "",(e));
            return 1;
        }

        LOG.info("Max user id from Primary Instance at " + myPrimInstance.getDBServer() + "/" + myPrimInstance.getDBName() + ": " + primMaxUserId);
        syncDatabases(primMaxUserId);

        if ((myContent != null) && (myContent.toString().trim().equals("") == false)) {
            String html = "\n<HTML>" + "\n<HEAD>" + "\n       <TITLE>TBits User DB Sync Report</TITLE>" + "\n       <style>" + "\nbody,table,p,span,div" + "\n{" + "\n    font-family: tahoma;"
                          + "\n    font-size: 13px;" + "\n} " + "\np" + "\n{" + "\n    font-family: trebuchet ms;" + "\n    color: midnightblue;" + "\n} " + "\nTABLE" + "\n{" + "\n    width: 100%;"
                          + "\n    border-right: 1px solid lightgrey;" + "\n    border-bottom: 1px solid lightgrey;" + "\n}" + "\nTABLE TD" + "\n{" + "\n    padding-left: 4px;"
                          + "\n    border: 1px solid lightgrey;" + "\n    border-right: none;" + "\n    border-bottom: none;" + "\n}" + "\nTABLE TR.header TD" + "\n{"
                          + "\n    border: 1px solid darkgray;" + "\n    border-right: none;" + "\n    background: midnightblue;" + "\n    color: lightgrey;" + "\n    font-weight: bold;" + "\n}"
                          + "\n       </style>" + "\n</HEAD>" + "\n<BODY>" + myContent.toString() + "\n</BODY></HTML>";

            Mail.sendWithHtml(ourToAddress, ourFromAddress, SUBJECT, html);
        }
        return 0;
    }

    /**
     * This method runs the algorithm for syncing the databases.
     *
     * @param primMaxUserId
     */
    private void syncDatabases(int primMaxUserId) {

        /*
         * Step 6:
         *      Maintain a table.
         */
        Hashtable<Integer, ArrayList<User>> eTable = new Hashtable<Integer, ArrayList<User>>();

        /*
         * Step 7:
         */
        boolean updated = false;

        for (TBitsInstance instance : mySecInstances) {
            Connection con          = null;
            int        secMaxUserId = -1;

            try {

                // Get the max user id in this instance.
                secMaxUserId = getMaxUserId(instance);
                LOG.info("Max User Id from secondary instance at " + instance.getDBServer() + "/" + instance.getDBName() + ": " + secMaxUserId);

                /*
                 * Check if the secMaxUserId is less than primMaxUserId in which
                 * case it has a stale user DB and needs updation.
                 */
                if (secMaxUserId < primMaxUserId) {

                    // Check if there is an entry in the eTable with this
                    // MaxUserId
                    ArrayList<User> userList = eTable.get(secMaxUserId);

                    if (userList == null) {
                        userList = getNewUsers(secMaxUserId);
                        eTable.put(secMaxUserId, userList);
                    }

                    /*
                     * Get a connection to the current secondary database.
                     * This will be used for inserting the users.
                     */
                    con = DataSourcePool.getConnection(instance.getDBServer(), instance.getDBName(), ourDbUser, ourDbPass, ourDriverClass, ourDriverTag);

                    // Insert these new users in the secondary instance.
                    for (User user : userList) {
                        insert(user, con);
                    }

                    /*
                     * Check if any of the previous secondary instances are
                     * updated. If not initialize the myContent buffer and write
                     * the primary instance details into it.
                     */
                    if (updated == false) {
                        myContent.append("<p>User count at the primary site [ ").append(myPrimInstance.getDBServer()).append("/").append(myPrimInstance.getDBName()).append(" ] is ").append(
                            primMaxUserId).append("</p>");
                        updated = true;
                    }

                    // Append the secondary instance details before updation.
                    myContent.append("<p>User count at the secondary site [ ").append(instance.getDBServer()).append("/").append(instance.getDBName()).append(" ] was ").append(secMaxUserId).append(
                        "</p>");
                }
            } catch (Exception e) {
                StringBuffer message = new StringBuffer();

                message.append("Exception occurred while Syncing the secondary ").append("instance: ").append(instance.toString()).append("\n").append(TBitsLogger.getStackTrace(e));

                // Log this to the application logger.
                LOG.error(message.toString());

                // Append this to the mail.
                myContent.append(message.toString());

                continue;
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Exception e) {}
                }
            }
        }
    }

    //~--- get methods --------------------------------------------------------

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
     * This method returns the maximum user id in the User's DB at the given
     * instance.
     *
     * @param  instance  TBits Database Instance.
     * @return Maximum UserId if the operation is successful
     * @throws Exception incase of any errors.
     */
    private int getMaxUserId(TBitsInstance instance) throws Exception {
        Connection con       = null;
        int        maxUserId = -1;

        try {
            con = DataSourcePool.getConnection(instance.getDBServer(), instance.getDBName(), ourDbUser, ourDbPass, ourDriverClass, ourDriverTag);

            Statement stmt  = con.createStatement();
            String    query = "SELECT ISNULL(max(user_id), 0) FROM users " + "WHERE user_id < 50000";
            ResultSet rs    = stmt.executeQuery(query);

            if (rs != null) {
                if (rs.next() != false) {
                    maxUserId = rs.getInt(1);
                }

                rs.close();
                rs = null;
            }

            stmt.close();
            stmt = null;
        } finally {
            if (con != null) {
                con.close();
            }
        }

        return maxUserId;
    }

    /**
     * This method retrieves the users from the Primary Instance whose
     * user id is greater than the specified maxUserId.
     *
     * @param maxUserId
     * @return List of users objects.
     * @throws Exception
     */
    private ArrayList<User> getNewUsers(int maxUserId) throws Exception {
        ArrayList<User> userList = new ArrayList<User>();
        Connection      con      = null;

        try {
            con = DataSourcePool.getConnection(myPrimInstance.getDBServer(), myPrimInstance.getDBName(), ourDbUser, ourDbPass, ourDriverClass, ourDriverTag);

            Statement stmt  = con.createStatement();
            String    query = "SELECT * FROM users WHERE user_id > " + maxUserId + " AND user_id < 50000";
            ResultSet rs    = stmt.executeQuery(query);

            if (rs != null) {
                myContent.append("\n<TABLE cellspacing='0' cellpadding='0'>").append("\n\t<TR class='header'>").append("<TD>User Id</TD>").append("<TD>Login</TD>").append(
                    "<TD>Display Name</TD>").append("<TD>Email</TD>").append("<TD>User Type</TD>").append("</TR>");

                while (rs.next() == true) {
                    User user = User.createFromResultSet(rs);

                    userList.add(user);
                    myContent.append("\n\t<TR>").append("\n\t\t<TD>").append(user.getUserId()).append("\n\t\t</TD>").append("\n\t\t<TD>").append(user.getUserLogin()).append("\n\t\t</TD>").append(
                        "\n\t\t<TD>").append(user.getDisplayName()).append("\n\t\t</TD>").append("\n\t\t<TD>").append(user.getEmail()).append("\n\t\t</TD>").append("\n\t\t<TD>").append(
                        user.getUserTypeId()).append("\n\t\t</TD>").append("\n\t\t</TR>");
                }

                myContent.append("\n</TABLE>");
                rs.close();
                rs = null;
            }

            stmt.close();
            stmt = null;
        } finally {
            if (con != null) {
                con.close();
            }
        }

        return userList;
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
