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
 * XTransfer.java
 *
 * $Header:
 *
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.exception.TBitsException;

import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

//~--- classes ----------------------------------------------------------------

public class XTransfer {

    // Application Logger
    public static TBitsLogger LOG = TBitsLogger.getLogger(PKG_UTIL);

    //~--- fields -------------------------------------------------------------

    private int        myMaxRequestId = 0;
    private int        myOldSystemId  = 0;
    private int        myNewSystemId  = 0;
    private String     baList;
    private String     driverClass;
    private String     driverTag;
    private Connection myNewCon;
    private String     myNewDB;
    private String     myNewPassword;

    // Parameters of the new database
    private String myNewServer;
    private String myNewUser;

    // Connection objects to connect to the old and new databases
    private Connection myOldCon;
    private String     myOldDB;
    private String     myOldPassword;

    // Parameters of the old database
    private String myOldServer;
    private String myOldUser;

    // Data members that hold the properties of current BA under transfer.
    private String                     mySystemName;
    private Hashtable<Integer, String> myUserMap;
    private Hashtable<String, Integer> newEmailMap;
    private Hashtable<String, Integer> newLoginMap;
    private Hashtable<Integer, String> oldLoginMap;
    private ArrayList<String>          sysList;

    //~--- methods ------------------------------------------------------------

    /**
     * This method is used to map the user logins in the new database to the
     * user ids
     */
    private void buildNewUserMap() throws SQLException {

        // Initialize the maps.
        myUserMap   = new Hashtable<Integer, String>();
        newLoginMap = new Hashtable<String, Integer>();
        newEmailMap = new Hashtable<String, Integer>();

        // Execute the procedure and get the list of users.
        CallableStatement stmt = myNewCon.prepareCall("stp_xtransfer_getAllUsers");
        ResultSet         rs   = stmt.executeQuery();

        while (rs.next() != false) {
            String  userLogin = rs.getString("user_login");
            Integer userId    = new Integer(rs.getInt("user_id"));
            String  userEmail = rs.getString("email");

            newLoginMap.put(userLogin, userId);
            newEmailMap.put(userEmail, userId);
            myUserMap.put(userId, userLogin);
        }

        rs.close();
        stmt.close();
        rs   = null;
        stmt = null;
        LOG.info("New User map is built successfully....\n");
    }

    /**
     * This method is used to map the user ids in the old database to the
     * user logins
     */
    private void buildOldUserMap() throws SQLException {

        // Initialize the map.
        oldLoginMap = new Hashtable<Integer, String>();

        // Execute the procedure and get the list of users.
        CallableStatement stmt = myOldCon.prepareCall("stp_xtransfer_getAllUsers");
        ResultSet         rs   = stmt.executeQuery();

        while (rs.next() != false) {
            String  userLogin = rs.getString("user_login");
            Integer userId    = new Integer(rs.getInt("user_id"));

            oldLoginMap.put(userId, userLogin);
        }

        rs.close();
        stmt.close();
        rs   = null;
        stmt = null;
        LOG.info("Old User map is built successfully....\n");
    }

    /**
     * This method inserts the new user given the user login the old system.
     *
     * @param userLogin Login of the user in old system.
     * @return Id of the user in new system.
     * @exception SQLException in case of any database related errors.
     */
    private int insertNewUser(String userLogin) throws SQLException {

        // Get the details corresponding to this userLogin from the old system.
        CallableStatement stmt = myOldCon.prepareCall("stp_xtransfer_lookupByUserLogin ? ");

        stmt.setString(1, userLogin);

        ResultSet rs = stmt.executeQuery();

        if ((rs == null) || (rs.next() == false)) {
            LOG.info(userLogin + " not found in old system.\n");

            return 0;
        }

        CallableStatement stmt1 = myNewCon.prepareCall("stp_xtransfer_insertuser ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");

        stmt1.registerOutParameter(1, Types.INTEGER);
        stmt1.setString(2, rs.getString("user_login"));
        stmt1.setString(3, rs.getString("first_name"));
        stmt1.setString(4, rs.getString("last_name"));
        stmt1.setString(5, rs.getString("display_name"));
        stmt1.setString(6, rs.getString("email"));
        stmt1.setBoolean(7, rs.getBoolean("is_active"));
        stmt1.setInt(8, rs.getInt("user_type_id"));
        stmt1.setString(9, rs.getString("web_config"));
        stmt1.setString(10, rs.getString("windows_config"));
        stmt1.setBoolean(11, rs.getBoolean("is_on_vacation"));
        stmt1.execute();

        int userId = stmt1.getInt(1);

        LOG.info("Inserted new user:" + "[ Login: " + userLogin + ", Active: " + rs.getBoolean("is_active") + " ]");
        rs.close();
        stmt.close();
        stmt1.close();

        return userId;
    }

    /**
     * @param args
     */
    public static int main(String[] args) {
        XTransfer xm = new XTransfer();

        boolean isSuccess = xm.transfer();
        return (isSuccess ? 0: 1);
    }

    /**
     * This method reads the configuration file "xtransfer.rc"
     */
    private boolean readConfigurationFile() {
        Properties prop = new Properties();

        try {
            File            transferConfFile = Configuration.findPath("xtransfer.rc");
            FileInputStream fis              = new FileInputStream(transferConfFile);

            prop.load(fis);
            fis.close();
            fis = null;
        } catch (IOException ioe) {
            LOG.info("An exception occured while setting config file", ioe);
            return false;
        } catch (NullPointerException npe) {
            LOG.info("\n\tPlease Check if xtransfer.rc is present." + "\n\tExiting transfer Process\n");
            return false;
        }

        // Properties of old database.
        myOldServer   = prop.getProperty("OLD_DB_SERVER_NAME");
        myOldDB       = prop.getProperty("OLD_DB_NAME");
        myOldUser     = prop.getProperty("OLD_DB_LOGIN");
        myOldPassword = prop.getProperty("OLD_DB_PASSWORD");

        // Properties of new database.
        myNewServer   = prop.getProperty("NEW_DB_SERVER_NAME");
        myNewDB       = prop.getProperty("NEW_DB_NAME");
        myNewUser     = prop.getProperty("NEW_DB_LOGIN");
        myNewPassword = prop.getProperty("NEW_DB_PASSWORD");

        // SQL Database Driver properties.
        driverClass = prop.getProperty("DRIVER_CLASS");
        driverTag   = prop.getProperty("DRIVER_TAG");

        // Comma separated list of business areas to be transferred.
        baList  = prop.getProperty("BUSINESS_AREA_LIST");
        sysList = Utilities.toArrayList(baList);
        
        return true;
    }

    public boolean transfer() {

        // Initialize the list to hold the BA Names.
        sysList = new ArrayList<String>();

        //
        // Read the configuration file to get the details of the source and
        // target databases and the driver details.
        //
        if(!readConfigurationFile())
            return false;

        if (verifyInput() == false) {
            LOG.info("Transfer Cancelled....\n");

            return false;
        }

        // Establish connection to the old database.
        try {
            myOldCon = DataSourcePool.getConnection(myOldServer, myOldDB, myOldUser, myOldPassword, driverClass, driverTag);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (myOldCon == null) {
            LOG.info("Connection to " + myOldDB + "@" + myOldServer + "Failed");
            return false;
        }

        LOG.info("Connection to " + myOldDB + "@" + myOldServer + " established .\n");

        // Establish connection to the new database.
        try {
            myNewCon = DataSourcePool.getConnection(myNewServer, myNewDB, myNewUser, myNewPassword, driverClass, driverTag);
        } catch (Exception e) {
            LOG.error("Connection to " + myNewDB + "@" + myNewServer + "Failed");
            LOG.error("",(e));
            e.printStackTrace();
            return false;
        }

        if (myNewCon == null) {
            LOG.error("Connection to " + myNewDB + "@" + myNewServer + "Failed");
            return false;
        }

        LOG.info("Connection to " + myNewDB + "@" + myNewServer + "established .\n");

        try {

            // Build the user maps.
            buildOldUserMap();
            buildNewUserMap();

            // Iterate through the list of business areas, transferring one at a
            // time.
            for (int i = 0; i < sysList.size(); i++) {
                mySystemName = (String) sysList.get(i);
                LOG.info("Transferring " + mySystemName + ".....\n");
                transferBusinessArea();
                LOG.info("Transferring Fields.....\n");
                transferFields();
                LOG.info("Transferring Types.....\n");
                transferTypes();

                // Transfer User related tables.
                LOG.info("Transferring Type Users.....\n");
                transferTypeUsers();
                LOG.info("Transferring Business Area Users.....\n");
                transferBAUsers();

                // Transfer role related tables.
                LOG.info("Transferring Roles.....\n");
                transferRoles();
                LOG.info("Transferring Role Permissions.....\n");
                transferRolePermissions();
                LOG.info("Transferring Role Users.....\n");
                transferRoleUsers();

                // Transfer request related tables.
                LOG.info("Transferring Requests....\n");
                transferRequests();
                LOG.info("Transferring Requests Users....\n");
                transferRequestUsers();

                // Transfer Action related tables.
                LOG.info("Transferring Actions....\n");
                transferActions();
                LOG.info("Transferring Action Users....\n");
                transferActionUsers();
                LOG.info("Transferring Requests Ex....\n");
                transferRequestEx();
                LOG.info("Transferring Actions Ex....\n");
                transferActionEx();
            }
        } catch (SQLException sqle) {
            LOG.severe("\n" + "",(sqle));

            return false;
        } catch (Exception de) {
            LOG.severe("\n" + "",(de));

            return false;
        }
        return true;
    }

    /**
     * This method transfers the actions_ex records of the businessarea
     * under transfer.
     */
    private void transferActionEx() throws TBitsException, SQLException {
        int start    = 0;
        int interval = 5000;
        int end      = myMaxRequestId + interval;

        for (int i = start; i <= end; i = i + interval) {
            LOG.info("Transferring Actions Ex in request range [ " + i + ", " + (i + interval) + " ] ....\n");

            CallableStatement stmt = myOldCon.prepareCall("stp_xtransfer_getActionsExBySystemId ?, ?, ?");

            stmt.setInt(1, myOldSystemId);
            stmt.setInt(2, i);
            stmt.setInt(3, i + interval);

            ResultSet rs = stmt.executeQuery();

            while (rs.next() != false) {
                try {
                    CallableStatement stmt1 = myNewCon.prepareCall("stp_xtransfer_insertActionEx " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?");

                    stmt1.setInt(1, myNewSystemId);
                    stmt1.setInt(2, rs.getInt("request_id"));
                    stmt1.setInt(3, rs.getInt("action_id"));
                    stmt1.setInt(4, rs.getInt("field_id"));
                    stmt1.setBoolean(5, rs.getBoolean("bit_value"));
                    stmt1.setTimestamp(6, rs.getTimestamp("datetime_value"));
                    stmt1.setInt(7, rs.getInt("int_value"));
                    stmt1.setDouble(8, rs.getInt("real_value"));
                    stmt1.setString(9, rs.getString("varchar_value"));
                    stmt1.setString(10, rs.getString("text_value"));
                    stmt1.setInt(11, rs.getInt("type_value"));
                    stmt1.execute();
                    stmt1.close();
                } catch (Exception e) {
                    LOG.info("",(e));
                }
            }

            rs.close();
            stmt.close();
        }
    }

    /**
     * This method transfers the action_users records of the businessarea
     * under transfer.
     */
    private void transferActionUsers() throws TBitsException, SQLException {
        int start    = 0;
        int interval = 5000;
        int end      = myMaxRequestId + interval;

        for (int i = start; i <= end; i = i + interval) {
            LOG.info("Transferring Action Users in request range [ " + i + ", " + (i + interval) + " ] ....\n");

            CallableStatement stmt = myOldCon.prepareCall("stp_xtransfer_getActionUsersBySystemId ?, ?, ?");

            stmt.setInt(1, myOldSystemId);
            stmt.setInt(2, i);
            stmt.setInt(3, i + interval);

            ResultSet rs = stmt.executeQuery();

            while (rs.next() != false) {
                try {
                    CallableStatement stmt1 = myNewCon.prepareCall("stp_xtransfer_insertActionUser ?, ?, ?, ?, ?, ?, ?");

                    stmt1.setInt(1, myNewSystemId);
                    stmt1.setInt(2, rs.getInt("request_id"));
                    stmt1.setInt(3, rs.getInt("action_id"));
                    stmt1.setInt(4, rs.getInt("user_type_id"));
                    stmt1.setInt(5, getUserIdInNewSystem(rs.getInt("user_id")));
                    stmt1.setInt(6, rs.getInt("ordering"));
                    stmt1.setBoolean(7, rs.getBoolean("is_primary"));
                    stmt1.execute();
                    stmt1.close();
                } catch (Exception e) {
                    LOG.info("",(e));
                }
            }

            rs.close();
            stmt.close();
        }
    }

    /**
     * This method transfers the action records of the businessarea
     * under transfer.
     */
    private void transferActions() throws TBitsException, SQLException {
        int start    = 0;
        int interval = 5000;
        int end      = myMaxRequestId + interval;

        for (int i = start; i <= end; i = i + interval) {
            LOG.info("Transferring Actions in request range [ " + i + ", " + (i + interval) + " ] ....\n");

            CallableStatement stmt = myOldCon.prepareCall("stp_xtransfer_getActionsBySystemId ?, ?, ?");

            stmt.setInt(1, myOldSystemId);
            stmt.setInt(2, i);
            stmt.setInt(3, (i + interval));

            ResultSet rs = stmt.executeQuery();

            while (rs.next() != false) {
                try {
                    CallableStatement stmt1 = myNewCon.prepareCall("stp_xtransfer_insertAction " + "?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?");

                    stmt1.setInt(1, myNewSystemId);
                    stmt1.setInt(2, rs.getInt("request_id"));
                    stmt1.setInt(3, rs.getInt("action_id"));
                    stmt1.setInt(4, rs.getInt("category_id"));
                    stmt1.setInt(5, rs.getInt("status_id"));
                    stmt1.setInt(6, rs.getInt("severity_id"));
                    stmt1.setInt(7, rs.getInt("request_type_id"));
                    stmt1.setString(8, rs.getString("subject"));
                    stmt1.setString(9, rs.getString("description"));
                    stmt1.setBoolean(10, rs.getBoolean("is_private"));
                    stmt1.setInt(11, rs.getInt("parent_request_id"));
                    stmt1.setInt(12, getUserIdInNewSystem(rs.getInt("user_id")));
                    stmt1.setTimestamp(13, rs.getTimestamp("due_datetime"));
                    stmt1.setTimestamp(14, rs.getTimestamp("logged_datetime"));
                    stmt1.setTimestamp(15, rs.getTimestamp("lastupdated_datetime"));
                    stmt1.setString(16, rs.getString("header_description"));
                    stmt1.setString(17, rs.getString("attachments"));
                    stmt1.setString(18, rs.getString("summary"));
                    stmt1.setString(19, rs.getString("memo"));
                    stmt1.setInt(20, rs.getInt("append_interface"));
                    stmt1.setInt(21, rs.getInt("notify"));
                    stmt1.setBoolean(22, rs.getBoolean("notify_loggers"));
                    stmt1.setInt(23, rs.getInt("replied_to_action"));
                    stmt1.execute();
                    stmt1.close();
                } catch (Exception e) {
                    LOG.info("",(e));
                }
            }

            rs.close();
            stmt.close();
        }
    }

    /**
     * This method transfers the BA-user records of the business area under
     * transfer.
     */
    private void transferBAUsers() throws TBitsException, SQLException {
        CallableStatement stmt = myOldCon.prepareCall("stp_xtransfer_getBAUsersBySystemId ?");

        stmt.setInt(1, myOldSystemId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next() != false) {
            try {
                CallableStatement stmt1 = myNewCon.prepareCall("stp_xtransfer_insertBAUser ?, ?");

                stmt1.setInt(1, myNewSystemId);
                stmt1.setInt(2, getUserIdInNewSystem(rs.getInt("user_id")));
                stmt1.execute();
                stmt1.close();
            } catch (Exception e) {
                LOG.info("",(e));
            }
        }

        rs.close();
        stmt.close();
    }

    /**
     * This method transfers the business area record.
     */
    private void transferBusinessArea() throws TBitsException, SQLException {
        CallableStatement ocs = myOldCon.prepareCall("stp_xtransfer_lookupBusinessAreaByName ?");

        ocs.setString(1, mySystemName);

        ResultSet rs = ocs.executeQuery();

        if (rs.next() != false) {
            myOldSystemId  = rs.getInt("sys_id");
            myMaxRequestId = rs.getInt("max_request_id");

            CallableStatement ncs = myNewCon.prepareCall("stp_xtransfer_insertBusinessArea " + "?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?, ?");

            ncs.registerOutParameter(1, Types.INTEGER);
            ncs.setString(2, rs.getString("name"));
            ncs.setString(3, rs.getString("display_name"));
            ncs.setString(4, rs.getString("email"));
            ncs.setString(5, rs.getString("sys_prefix"));
            ncs.setString(6, rs.getString("description"));
            ncs.setString(7, "Development");
            ncs.setString(8, rs.getString("location"));
            ncs.setTimestamp(9, rs.getTimestamp("date_created"));
            ncs.setInt(10, rs.getInt("max_request_id"));
            ncs.setInt(11, rs.getInt("max_email_actions"));
            ncs.setBoolean(12, rs.getBoolean("is_email_active"));
            ncs.setBoolean(13, rs.getBoolean("is_active"));
            ncs.setBoolean(14, rs.getBoolean("is_private"));
            ncs.setString(15, rs.getString("sys_config"));
            ncs.setString(16, "");

            boolean flag = ncs.execute();

            myNewSystemId = ncs.getInt(1);
            LOG.info("Business Area ID in New DB: " + myNewSystemId + "\n");
            rs.close();
            ocs.close();
            ncs.close();
            rs  = null;
            ocs = null;
            ncs = null;
        } else {
            throw new TBitsException("This Business Area you are trying to transfer does not exist." + mySystemName);
        }
    }

    /**
     * This method transfers the field records of the Business area under
     * transfer.
     */
    private void transferFields() throws TBitsException, SQLException {
        CallableStatement ocs = myOldCon.prepareCall("stp_xtransfer_getFieldsBySystemId ?");

        ocs.setInt(1, myOldSystemId);

        ResultSet rs = ocs.executeQuery();

        if (rs != null) {
            while (rs.next() != false) {
                CallableStatement ncs = myNewCon.prepareCall("stp_xtransfer_insertField " + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");

                ncs.setInt(1, myNewSystemId);
                ncs.setInt(2, rs.getInt("field_id"));
                ncs.setString(3, rs.getString("name"));
                ncs.setString(4, rs.getString("display_name"));
                ncs.setString(5, rs.getString("description"));
                ncs.setInt(6, rs.getInt("data_type_id"));
                ncs.setBoolean(7, rs.getBoolean("is_active"));
                ncs.setBoolean(8, rs.getBoolean("is_extended"));
                ncs.setBoolean(9, rs.getBoolean("is_active"));
                ncs.setInt(10, rs.getInt("tracking_option"));
                ncs.setInt(11, rs.getInt("permission"));
                ncs.setString(12, rs.getString("regex"));
                ncs.execute();
                ncs.close();
            }

            rs.close();
            ocs.close();
        }
    }

    /**
     * This method transfers the requests_ex records of the businessarea
     * under transfer.
     */
    private void transferRequestEx() throws TBitsException, SQLException {
        int start    = 0;
        int interval = 5000;
        int end      = myMaxRequestId + interval;

        for (int i = start; i <= end; i = i + interval) {
            LOG.info("Transferring Requests Ex in request range [ " + i + ", " + (i + interval) + " ] ....\n");

            CallableStatement stmt = myOldCon.prepareCall("stp_xtransfer_getRequestsExBySystemId ?, ?, ?");

            stmt.setInt(1, myOldSystemId);
            stmt.setInt(2, i);
            stmt.setInt(3, i + interval);

            ResultSet rs = stmt.executeQuery();

            while (rs.next() != false) {
                try {
                    CallableStatement stmt1 = myNewCon.prepareCall("stp_xtransfer_insertRequestEx " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?");

                    stmt1.setInt(1, myNewSystemId);
                    stmt1.setInt(2, rs.getInt("request_id"));
                    stmt1.setInt(3, rs.getInt("field_id"));
                    stmt1.setBoolean(4, rs.getBoolean("bit_value"));
                    stmt1.setTimestamp(5, rs.getTimestamp("datetime_value"));
                    stmt1.setInt(6, rs.getInt("int_value"));
                    stmt1.setDouble(7, rs.getInt("real_value"));
                    stmt1.setString(8, rs.getString("varchar_value"));
                    stmt1.setString(9, rs.getString("text_value"));
                    stmt1.setInt(10, rs.getInt("type_value"));
                    stmt1.execute();
                    stmt1.close();
                } catch (Exception e) {
                    LOG.info("",(e));
                }
            }

            rs.close();
            stmt.close();
        }
    }

    /**
     * This method transfers the requests_users records of the businessarea
     * under transfer.
     */
    private void transferRequestUsers() throws TBitsException, SQLException {
        int start    = 0;
        int interval = 5000;
        int end      = myMaxRequestId + interval;

        for (int i = start; i <= end; i = i + interval) {
            LOG.info("Tranferring Request Users in request range [ " + i + ", " + (i + interval) + " ] ....\n");

            CallableStatement stmt = myOldCon.prepareCall("stp_xtransfer_getRequestUsersBySystemId ?, ?, ?");

            stmt.setInt(1, myOldSystemId);
            stmt.setInt(2, i);
            stmt.setInt(3, i + interval);

            ResultSet rs       = stmt.executeQuery();
            int       ordering = 0;
            int       oldReqId = 0;
            int       oldUTId  = 0;
            int       curReqId = 0;
            int       curUTId  = 0;

            while (rs.next() != false) {
                try {
                    CallableStatement stmt1 = myNewCon.prepareCall("stp_xtransfer_insertRequestUser ?, ?, ?, ?, ?, ?");

                    stmt1.setInt(1, myNewSystemId);
                    stmt1.setInt(2, rs.getInt("request_id"));
                    stmt1.setInt(3, rs.getInt("user_type_id"));
                    stmt1.setInt(4, getUserIdInNewSystem(rs.getInt("user_id")));
                    stmt1.setInt(5, rs.getInt("ordering"));
                    stmt1.setBoolean(6, rs.getBoolean("is_primary"));
                    stmt1.execute();
                    stmt1.close();
                } catch (Exception e) {
                    LOG.info("",(e));
                }
            }

            rs.close();
            stmt.close();
        }
    }

    /**
     * This method transfers the requests records of the businessarea
     * under transfer.
     */
    private void transferRequests() throws TBitsException, SQLException {
        CallableStatement stmt = myOldCon.prepareCall("stp_xtransfer_getRequestsBySystemId ?");

        stmt.setInt(1, myOldSystemId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next() != false) {
            try {
                CallableStatement stmt1 = myNewCon.prepareCall("stp_xtransfer_insertRequest " + "?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?");

                stmt1.setInt(1, myNewSystemId);
                stmt1.setInt(2, rs.getInt("request_id"));
                stmt1.setInt(3, rs.getInt("category_id"));
                stmt1.setInt(4, rs.getInt("status_id"));
                stmt1.setInt(5, rs.getInt("severity_id"));
                stmt1.setInt(6, rs.getInt("request_type_id"));
                stmt1.setString(7, rs.getString("subject"));
                stmt1.setString(8, rs.getString("description"));
                stmt1.setBoolean(9, rs.getBoolean("is_private"));
                stmt1.setInt(10, rs.getInt("parent_request_id"));
                stmt1.setInt(11, getUserIdInNewSystem(rs.getInt("user_id")));
                stmt1.setInt(12, rs.getInt("max_action_id"));
                stmt1.setTimestamp(13, rs.getTimestamp("due_datetime"));
                stmt1.setTimestamp(14, rs.getTimestamp("logged_datetime"));
                stmt1.setTimestamp(15, rs.getTimestamp("lastupdated_datetime"));
                stmt1.setString(16, rs.getString("header_description"));
                stmt1.setString(17, rs.getString("attachments"));
                stmt1.setString(18, rs.getString("summary"));
                stmt1.setString(19, rs.getString("memo"));
                stmt1.setInt(20, rs.getInt("append_interface"));
                stmt1.setInt(21, rs.getInt("notify"));
                stmt1.setBoolean(22, rs.getBoolean("notify_loggers"));
                stmt1.setInt(23, rs.getInt("replied_to_action"));
                stmt1.execute();
                stmt1.close();
            } catch (Exception e) {
                LOG.info("",(e));
            }
        }

        rs.close();
        stmt.close();
    }

    /**
     * This method transfers the role-permission records of the businessarea
     * under transfer.
     */
    private void transferRolePermissions() throws TBitsException, SQLException {
        CallableStatement stmt = myOldCon.prepareCall("stp_xtransfer_getRolePermissionsBySystemId ?");

        stmt.setInt(1, myOldSystemId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next() != false) {
            try {
                CallableStatement stmt1 = myNewCon.prepareCall("stp_xtransfer_insertRolePermission ?, ?, ?, ?, ?");

                stmt1.setInt(1, myNewSystemId);
                stmt1.setInt(2, rs.getInt("role_id"));
                stmt1.setInt(3, rs.getInt("field_id"));
                stmt1.setInt(4, rs.getInt("gpermissions"));
                stmt1.setInt(5, rs.getInt("dpermissions"));
                stmt1.execute();
                stmt1.close();
            } catch (Exception e) {
                LOG.info("",(e));
            }
        }

        rs.close();
        stmt.close();
        stmt = null;
    }

    /**
     * This method transfers the role-user records of the businessarea
     * under transfer.
     */
    private void transferRoleUsers() throws TBitsException, SQLException {
        CallableStatement stmt = myOldCon.prepareCall("stp_xtransfer_getRoleUsersBySystemId ?");

        stmt.setInt(1, myOldSystemId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next() != false) {
            try {
                CallableStatement stmt1 = myNewCon.prepareCall("stp_xtransfer_insertRoleUser ?, ?, ?");

                stmt1.setInt(1, myNewSystemId);
                stmt1.setInt(2, rs.getInt("role_id"));
                stmt1.setInt(3, getUserIdInNewSystem(rs.getInt("user_id")));
                stmt1.execute();
                stmt1.close();
            } catch (Exception e) {
                LOG.info("",(e));
            }
        }

        rs.close();
        stmt.close();
    }

    /**
     * This method transfers the role records of the businessarea
     * under transfer.
     */
    private void transferRoles() throws TBitsException, SQLException {
        CallableStatement stmt = myOldCon.prepareCall("stp_xtransfer_getRolesBySystemId ?");

        stmt.setInt(1, myOldSystemId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next() != false) {
            try {
                CallableStatement stmt1 = myNewCon.prepareCall("stp_xtransfer_insertRole ?, ?, ?, ?");

                stmt1.setInt(1, myNewSystemId);
                stmt1.setInt(2, rs.getInt("role_id"));
                stmt1.setString(3, rs.getString("rolename"));
                stmt1.setString(4, rs.getString("description"));
                stmt1.execute();
                stmt1.close();
            } catch (Exception e) {
                LOG.info("",(e));
            }
        }

        rs.close();
        stmt.close();
    }

    /**
     * This method transfers the category-user records of the businessarea under
     * transfer.
     */
    private void transferTypeUsers() throws TBitsException, SQLException {
        CallableStatement stmt = myOldCon.prepareCall("stp_xtransfer_getTypeUsersBySystemId ?");

        stmt.setInt(1, myOldSystemId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next() != false) {
            try {
                CallableStatement stmt1 = myNewCon.prepareCall("stp_xtransfer_insertTypeUser ?, ?, ?, ?, ?, ?, ?, ?, ?");

                stmt1.setInt(1, myNewSystemId);
                stmt1.setInt(2, rs.getInt("field_id"));
                stmt1.setInt(3, rs.getInt("type_id"));
                stmt1.setInt(4, getUserIdInNewSystem(rs.getInt("user_id")));

                // Since these are category assignees, User type id is ASSIGNEE
                stmt1.setInt(5, rs.getInt("user_type_id"));
                stmt1.setInt(6, rs.getInt("notification_id"));
                stmt1.setBoolean(7, rs.getBoolean("is_volunteer"));
                stmt1.setBoolean(8, rs.getBoolean("rr_volunteer"));
                stmt1.setBoolean(9, rs.getBoolean("is_active"));
                stmt1.execute();
                stmt1.close();
            } catch (Exception e) {
                LOG.info("",(e));
            }
        }

        rs.close();
        stmt.close();
    }

    /**
     * This method transfers the types records of the business area under
     * transfer.
     */
    private void transferTypes() throws TBitsException, SQLException {
        CallableStatement ocs = myOldCon.prepareCall("stp_xtransfer_getTypesBySystemId ?");

        ocs.setInt(1, myOldSystemId);

        ResultSet rs = ocs.executeQuery();

        if (rs != null) {
            while (rs.next() != false) {
                CallableStatement ncs         = myNewCon.prepareCall("stp_xtransfer_insertType " + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
                String            description = rs.getString("description");

                description = (description == null)
                              ? ""
                              : description;
                ncs.setInt(1, myNewSystemId);
                ncs.setInt(2, rs.getInt("field_id"));
                ncs.setInt(3, rs.getInt("type_id"));
                ncs.setString(4, rs.getString("name"));
                ncs.setString(5, rs.getString("display_name"));
                ncs.setString(6, description);
                ncs.setInt(7, rs.getInt("ordering"));
                ncs.setBoolean(8, rs.getBoolean("is_active"));
                ncs.setBoolean(9, rs.getBoolean("is_default"));
                ncs.setBoolean(10, rs.getBoolean("is_checked"));
                ncs.setBoolean(11, rs.getBoolean("is_private"));
                ncs.setBoolean(12, rs.getBoolean("is_final"));
                ncs.execute();
                ncs.close();
            }

            rs.close();
            ocs.close();
        }
    }

    /**
     * This method prints the information read from the configuration file
     * and obtains the confirmation to proceed.
     */
    private boolean verifyInput() {
        System.out.print("/~~~~~~~~~~~~~~~~" + "Configuration info being used for transfer" + "~~~~~~~~~~~~~~~~~/\n\n" + "\t\tOld Database Machine Name : " + myOldServer + "\n"
                         + "\t\tOld Database Name         : " + myOldDB + "\n" + "\t\tOld User Name             : " + myOldUser + "\n" + "\t\tOld Password              : " + "*****" + "\n\n"
                         + "\t\tNew Database Machine Name : " + myNewServer + "\n" + "\t\tNew Database Name         : " + myNewDB + "\n" + "\t\tNew User Name             : " + myNewUser + "\n"
                         + "\t\tNew Password              : " + "*****" + "\n\n" + "\t\tBusiness Area(s)          : " + baList + "\n\n");

        try {
            System.out.print("Would you like to start transfer with this info (y/n): ");

            BufferedReader br    = new BufferedReader(new InputStreamReader(System.in));
            String         input = br.readLine();

            if ((input == null) || input.startsWith("n") || input.startsWith("N")) {
                return false;
            }

            return true;
        } catch (IOException ioe) {
            LOG.error("An exception occured while verifying the config file" + "",(ioe));

            return false;
        }
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the id of the user in the new database given his
     * id in the old database.
     *
     * @param oldId UserId in the old database.
     *
     * @return UserId in the new database.
     *
     * @exception SQLException in case of any database related errors.
     */
    private int getUserIdInNewSystem(int oldId) throws SQLException {
        Object temp = oldLoginMap.get(new Integer(oldId));

        if (temp == null) {
            if (oldId == 0) {
                return 0;
            }

            // ID is not found in the old system itself. return unknown.
            LOG.info("User-Id not found in the old system itself: " + oldId);

            return 0;
        }

        String value = temp.toString();

        // Assuming oldId corresponds to normal-user.
        Object tempId = newLoginMap.get(value);

        if (tempId != null) {

            // Found a normal-user, so return the id.
            return ((Integer) tempId).intValue();
        }

        // OldId may be corresponding to a mailing-list
        tempId = newEmailMap.get(value);

        if (tempId != null) {

            // found a mailing-list, so return the id.
            return ((Integer) tempId).intValue();
        }

        /*
         * Insert the user into the database and get the userid.
         */
        int userId = insertNewUser(value);

        newLoginMap.put(value, new Integer(userId));
        myUserMap.put(new Integer(userId), value);

        return userId;
    }
}
