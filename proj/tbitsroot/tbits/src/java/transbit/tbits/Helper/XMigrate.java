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
 * XMigrate.java
 *
 * $Header:
 *
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

//Xerces Imports.
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//TBits Imports
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.Attachment;
import transbit.tbits.config.CustomLink;
import transbit.tbits.config.SysConfig;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.UserType;
import transbit.tbits.exception.TBitsException;

import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.SOURCE_WEB;

//~--- JDK imports ------------------------------------------------------------

//Java Imports
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//~--- classes ----------------------------------------------------------------

/**
 * This class migrates a given business area from Database in
 * TBits 3.1 format to the database in TBits 2.2 format
 *
 * @author : Vinod Gupta.
 * @author : Vaibhav.
 * @version $Id: $
 */
public class XMigrate {

    // Application Logger
    public static TBitsLogger                  LOG = TBitsLogger.getLogger(PKG_UTIL);
    private static Hashtable<String, Integer>  ourNameToIdMap;
    private static Hashtable<String, Integer>  ourNameToPermMap;
    private static Hashtable<Integer, Integer> ourTrackingMap;

    //~--- static initializers ------------------------------------------------

    static {
        ourTrackingMap   = new Hashtable<Integer, Integer>();
        ourNameToIdMap   = new Hashtable<String, Integer>();
        ourNameToPermMap = new Hashtable<String, Integer>();
        ourNameToIdMap.put("sys_id", 1);
        ourNameToIdMap.put("request_id", 2);
        ourNameToIdMap.put("category_id", 3);
        ourNameToIdMap.put("status_id", 4);
        ourNameToIdMap.put("severity_id", 5);
        ourNameToIdMap.put("request_type_id", 6);
        ourNameToIdMap.put("logger_ids", 7);
        ourNameToIdMap.put("assignee_ids", 8);
        ourNameToIdMap.put("subscriber_ids", 9);
        ourNameToIdMap.put("to_ids", 10);
        ourNameToIdMap.put("cc_ids", 11);
        ourNameToIdMap.put("subject", 12);
        ourNameToIdMap.put("description", 13);
        ourNameToIdMap.put("is_private", 14);
        ourNameToIdMap.put("parent_request_id", 15);
        ourNameToIdMap.put("user_id", 16);
        ourNameToIdMap.put("max_action_id", 17);
        ourNameToIdMap.put("due_datetime", 18);
        ourNameToIdMap.put("logged_datetime", 19);
        ourNameToIdMap.put("lastupdated_datetime", 20);
        ourNameToIdMap.put("header_description", 21);
        ourNameToIdMap.put("attachments", 22);
        ourNameToIdMap.put("summary", 23);
        ourNameToIdMap.put("memo", 24);
        ourNameToIdMap.put("append_interface", 25);
        ourNameToIdMap.put("mails_send", 26);
        ourNameToPermMap.put("sys_id", 6);
        ourNameToPermMap.put("request_id", 47);
        ourNameToPermMap.put("category_id", 254);
        ourNameToPermMap.put("status_id", 126);
        ourNameToPermMap.put("severity_id", 126);
        ourNameToPermMap.put("request_type_id", 126);
        ourNameToPermMap.put("logger_ids", 191);
        ourNameToPermMap.put("assignee_ids", 191);
        ourNameToPermMap.put("subscriber_ids", 63);
        ourNameToPermMap.put("to_ids", 125);
        ourNameToPermMap.put("cc_ids", 125);
        ourNameToPermMap.put("subject", 127);
        ourNameToPermMap.put("description", 103);
        ourNameToPermMap.put("is_private", 118);
        ourNameToPermMap.put("parent_request_id", 127);
        ourNameToPermMap.put("user_id", 44);
        ourNameToPermMap.put("max_action_id", 0);
        ourNameToPermMap.put("due_datetime", 111);
        ourNameToPermMap.put("logged_datetime", 44);
        ourNameToPermMap.put("lastupdated_datetime", 44);
        ourNameToPermMap.put("header_description", 4);
        ourNameToPermMap.put("attachments", 103);
        ourNameToPermMap.put("summary", 103);
        ourNameToPermMap.put("memo", 103);
        ourNameToPermMap.put("append_interface", 0);
        ourNameToPermMap.put("notify", 126);
        ourNameToPermMap.put("notify_loggers", 126);
        ourNameToPermMap.put("replied_to_action", 70);
        ourNameToPermMap.put("related_requests", 87);
        ourTrackingMap.put(0, 0);
        ourTrackingMap.put(1, 1);
        ourTrackingMap.put(2, 3);
        ourTrackingMap.put(3, 4);
        ourTrackingMap.put(4, 3);
        ourTrackingMap.put(5, 4);
    }

    //~--- fields -------------------------------------------------------------

    private int        myMaxRequestId = 0;
    private int        myOldSystemId  = 0;
    private int        myNewSystemId  = 0;
    private String     baList;
    private String     driverClass;
    private String     driverTag;
    private String     myNewAttachmentPath;
    private Connection myNewCon;
    private String     myNewDB;
    private String     myNewPassword;

    // Parameters of the new database
    private String myNewServer;
    private String myNewUser;
    private String myOldAttachmentPath;

    // Connection objects to connect to the old and new databases
    private Connection myOldCon;
    private String     myOldDB;
    private String     myOldPassword;

    // Parameters of the old database
    private String myOldServer;
    private String myOldUser;
    private String mySystemName;

    // Data members that hold the properties of current BA under migration.
    private String                     mySystemPrefix;
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
        CallableStatement stmt = myNewCon.prepareCall("stp_xmigrate_getAllUsers");
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
        CallableStatement stmt = myOldCon.prepareCall("stp_xmigrate_getAllUsers");
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
     * This method is used to copy attachments from the old directory
     * to the new directory.
     *
     * @param src  name of attachment in old system.
     * @param dest name of attachment in new system.
     */
    private boolean copyFile(String src, String dest) {
        BufferedReader br   = null;
        Process        proc = null;

        try {
            String cmd = new String("cp " + src + " " + dest + " ");

            proc = Runtime.getRuntime().exec(cmd);
            proc.waitFor();

            if (proc.exitValue() != 0) {
                br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

                StringBuilder errorString = new StringBuilder();
                String        error       = null;

                while ((error = br.readLine()) != null) {
                    errorString.append(error);
                }

                // LOG.info("\nFile Not Found: " + src + "\n");
                return false;
            }

            proc.destroy();

            return true;
        } catch (IOException ioe) {
            LOG.info("Error: copyFile method :" + ioe.toString());

            return false;
        } catch (InterruptedException ie) {
            LOG.info("Interrupted Exception : " + ie.toString());

            return false;
        } catch (IllegalThreadStateException itse) {
            LOG.info("IllegalThreadStateException : " + itse.toString());

            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                    br = null;
                } catch (Exception e) {}
            }

            proc = null;
        }
    }

    /**
     * This method inserts a field record into the new database.
     *
     * @param aFieldId Id of the field.
     * @param aName    Name of the field.
     * @param aDisplayName Display name of the field.
     * @param aDataType DataType of the field.
     *
     */
    private void insertField(int aFieldId, String aName, String aDisplayName, int aDataType) throws SQLException {
        CallableStatement ncs = myNewCon.prepareCall("stp_xmigrate_insertField ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");

        ncs.setInt(1, myNewSystemId);
        ncs.setInt(2, aFieldId);
        ncs.setString(3, aName);
        ncs.setString(4, aDisplayName);
        ncs.setString(5, aDisplayName);
        ncs.setInt(6, aDataType);
        ncs.setBoolean(7, true);
        ncs.setBoolean(8, false);
        ncs.setBoolean(9, false);    // is_private is false by default.
        ncs.setInt(10, 1);

        Integer temp       = ourNameToPermMap.get(aName.toLowerCase());
        int     permission = (temp == null)
                             ? 0
                             : temp.intValue();

        ncs.setInt(11, permission);
        ncs.setString(12, "");
        ncs.execute();
        ncs.close();
    }

    /**
     * This method inserts the given user login as a mailing-list in the new
     * system.
     *
     * @param email email address.
     *
     * @return Id of the mailing list in the new system.
     *
     * @exception SQLException in case of any database related errors.
     */
    private int insertNewMailingList(String email) throws SQLException {
        LOG.info("Inserting new mailing list: " + email);

        CallableStatement stmt1 = myNewCon.prepareCall("stp_xmigrate_insertuser ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");

        stmt1.registerOutParameter(1, Types.INTEGER);
        stmt1.setString(2, email);
        stmt1.setString(3, email);
        stmt1.setString(4, email);
        stmt1.setString(5, email);
        stmt1.setString(6, email);
        stmt1.setBoolean(7, false);    // is_active is false for such users.
        stmt1.setInt(8, 8);            // User-Type is 8 for mailing list.
        stmt1.setString(9, "");
        stmt1.setString(10, "");
        stmt1.setBoolean(11, false);
        stmt1.execute();

        int userId = stmt1.getInt(1);

        stmt1.close();

        return userId;
    }

    /**
     * This method inserts the new user given the user login the old system.
     *
     * @param userLogin Login of the user in old system.
     *
     * @return Id of the user in new system.
     *
     * @exception SQLException in case of any database related errors.
     */
    private int insertNewUser(String userLogin) throws SQLException {
        LOG.info("Inserting new user: " + userLogin);

        // Get the details corresponding to this userLogin from the old system.
        CallableStatement stmt = myOldCon.prepareCall("stp_xmigrate_getUserByLogin ? ");

        stmt.setString(1, userLogin);

        ResultSet rs = stmt.executeQuery();

        if ((rs == null) || (rs.next() == false)) {
            LOG.info(userLogin + " not found in old system.\n");

            return 0;
        }

        CallableStatement stmt1 = myNewCon.prepareCall("stp_xmigrate_insertuser ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");

        stmt1.registerOutParameter(1, Types.INTEGER);
        stmt1.setString(2, rs.getString("user_login"));
        stmt1.setString(3, rs.getString("first_name"));
        stmt1.setString(4, rs.getString("last_name"));
        stmt1.setString(5, rs.getString("display_name"));
        stmt1.setString(6, rs.getString("email"));
        stmt1.setBoolean(7, false);    // is_active is false for such users.
        stmt1.setInt(8, 7);            // User-Type is 7 for normal-user
        stmt1.setString(9, "");
        stmt1.setString(10, "");
        stmt1.setBoolean(11, rs.getBoolean("is_on_vacation"));
        stmt1.execute();

        int userId = stmt1.getInt(1);

        rs.close();
        stmt.close();
        stmt1.close();

        return userId;
    }

    public static int main(String args[]) {
        XMigrate xm = new XMigrate();

        boolean isSuccess = xm.migrate();
        return (isSuccess ? 0: 1);
    }

    public boolean migrate() {

        // Initialize the list to hold the BA Names.
        sysList = new ArrayList<String>();

        //
        // Read the configuration file to get the details of the source and
        // target databases and the driver details.
        //
        if(!readConfigurationFile())
            return false;

        if (verifyInput() == false) {
            LOG.info("Migration Cancelled....\n");

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
            e.printStackTrace();
            return false;
        }

        if (myNewCon == null) {
            LOG.info("Connection to " + myNewDB + "@" + myNewServer + "Failed");
            return false;
        }

        LOG.info("Connection to " + myNewDB + "@" + myNewServer + "established .\n");

        try {

            // Build the user maps.
            buildOldUserMap();
            buildNewUserMap();

            // Iterate through the list of business areas, migrating one at a
            // time.
            for (int i = 0; i < sysList.size(); i++) {
                mySystemName = (String) sysList.get(i);
                LOG.info("Migrating " + mySystemName + ".....\n");
                migrateBusinessArea();
                LOG.info("Migrating Fields.....\n");
                migrateFields();
                LOG.info("Migrating Types.....\n");
                migrateTypes();

                // Migrate User related tables.
                LOG.info("Migrating Category Users.....\n");
                migrateCategoryUsers();
                LOG.info("Migrating Business Area Users.....\n");
                migrateBAUsers();

                // Migrate role related tables.
                LOG.info("Migrating Roles.....\n");
                migrateRoles();
                LOG.info("Migrating Role Permissions.....\n");
                migrateRolePermissions();
                LOG.info("Migrating Role Users.....\n");
                migrateRoleUsers();

                // Migrate request related tables.
                LOG.info("Migrating Requests....\n");
                migrateRequests();
                LOG.info("Migrating Requests Ex....\n");
                migrateRequestEx();
                LOG.info("Migrating Requests Users....\n");
                migrateRequestUsers();

                // Migrate Action related tables.
                LOG.info("Migrating Actions....\n");
                migrateActions();
                LOG.info("Migrating Actions Ex....\n");
                migrateActionEx();
                LOG.info("Migrating Action Users....\n");
                migrateActionUsers();
            }
        } catch (SQLException sqle) {
            LOG.severe("\n" + "",(sqle));

            return false;
        } catch (TBitsException de) {
            LOG.severe("\n" + "",(de));

            return false;
        }
        return true;
    }

    /**
     * This method migrates the actions_ex records of the businessarea
     * under migration.
     */
    private void migrateActionEx() throws TBitsException, SQLException {
        int start    = 0;
        int interval = 5000;
        int end      = myMaxRequestId + interval;

        for (int i = start; i <= end; i = i + interval) {
            LOG.info("Migrating Actions Ex in request range [ " + i + ", " + (i + interval) + " ] ....\n");

            CallableStatement stmt = myOldCon.prepareCall("stp_xmigrate_getActionsExBySystemId ?, ?, ?");

            stmt.setInt(1, myOldSystemId);
            stmt.setInt(2, i);
            stmt.setInt(3, i + interval);

            ResultSet rs = stmt.executeQuery();

            while (rs.next() != false) {
                try {
                    CallableStatement stmt1   = myNewCon.prepareCall("stp_xmigrate_insertActionEx " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?");
                    int               fieldId = rs.getInt("field_id");

                    //
                    // Since we added two new fields, displace the ids of
                    // extended fields by 2.
                    //
                    fieldId = fieldId + 2;
                    stmt1.setInt(1, myNewSystemId);
                    stmt1.setInt(2, rs.getInt("request_id"));
                    stmt1.setInt(3, rs.getInt("action_id"));
                    stmt1.setInt(4, fieldId);
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
     * This method migrates the action_users records of the businessarea
     * under migration.
     */
    private void migrateActionUsers() throws TBitsException, SQLException {
        int start    = 0;
        int interval = 5000;
        int end      = myMaxRequestId + interval;

        for (int i = start; i <= end; i = i + interval) {
            LOG.info("Migrating Action Users in request range [ " + i + ", " + (i + interval) + " ] ....\n");

            CallableStatement stmt = myOldCon.prepareCall("stp_xmigrate_getActionUsersBySystemId ?, ?, ?");

            stmt.setInt(1, myOldSystemId);
            stmt.setInt(2, i);
            stmt.setInt(3, i + interval);

            ResultSet rs       = stmt.executeQuery();
            int       ordering = 0;
            int       oldReqId = 0;
            int       oldActId = 0;
            int       oldUTId  = 0;
            int       curReqId = 0;
            int       curActId = 0;
            int       curUTId  = 0;

            while (rs.next() != false) {
                curReqId = rs.getInt("request_id");
                curUTId  = rs.getInt("user_type_id");

                //
                // Reset the ordering if there is a change in any of these
                // - Request Id.
                // - Action Id.
                // - User Type Id.
                //
                if (((oldReqId != 0) && (curReqId != oldReqId)) || ((oldActId != 0) && (curActId != oldActId)) || ((curUTId != 0) && (curUTId != oldUTId))) {
                    ordering = 0;
                }

                oldReqId = curReqId;
                oldActId = curActId;
                oldUTId  = curUTId;
                ordering++;

                try {
                    CallableStatement stmt1 = myNewCon.prepareCall("stp_xmigrate_insertActionUser ?, ?, ?, ?, ?, ?, ?");

                    stmt1.setInt(1, myNewSystemId);
                    stmt1.setInt(2, rs.getInt("request_id"));
                    stmt1.setInt(3, rs.getInt("action_id"));
                    stmt1.setInt(4, rs.getInt("user_type_id"));
                    stmt1.setInt(5, getUserIdInNewSystem(rs.getInt("user_id")));
                    stmt1.setInt(6, ordering);
                    stmt1.setBoolean(7, false);    // Default is_primary is false.
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
     * This method migrates the action records of the businessarea
     * under migration.
     */
    private void migrateActions() throws TBitsException, SQLException {
        int start    = 0;
        int interval = 5000;
        int end      = myMaxRequestId + interval;

        for (int i = start; i <= end; i = i + interval) {
            LOG.info("Migrating Actions in request range [ " + i + ", " + (i + interval) + " ] ....\n");

            CallableStatement stmt = myOldCon.prepareCall("stp_xmigrate_getActionsBySystemId ?, ?, ?");

            stmt.setInt(1, myOldSystemId);
            stmt.setInt(2, i);
            stmt.setInt(3, (i + interval));

            ResultSet rs = stmt.executeQuery();

            while (rs.next() != false) {
                try {
                    CallableStatement stmt1 = myNewCon.prepareCall("stp_xmigrate_insertAction " + "?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?");

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
                    stmt1.setString(17, getNewAttFormat(rs.getString("attachments"), true));
                    stmt1.setString(18, rs.getString("summary"));
                    stmt1.setString(19, rs.getString("memo"));
                    stmt1.setInt(20, SOURCE_WEB);
                    stmt1.setInt(21, rs.getInt("mails_send"));
                    stmt1.setBoolean(22, rs.getBoolean("logger_mail_required"));
                    stmt1.setInt(23, 0);
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
     * This method migrates the BA-user records of the business area under
     * migration.
     */
    private void migrateBAUsers() throws TBitsException, SQLException {
        CallableStatement stmt = myOldCon.prepareCall("stp_xmigrate_getBAUsersBySystemId ?");

        stmt.setInt(1, myOldSystemId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next() != false) {
            try {
                CallableStatement stmt1 = myNewCon.prepareCall("stp_xmigrate_insertBAUser ?, ?");

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
     * This method migrates the business area record.
     */
    private void migrateBusinessArea() throws TBitsException, SQLException {
        CallableStatement ocs = myOldCon.prepareCall("stp_xmigrate_lookupBusinessAreaByName ?");

        ocs.setString(1, mySystemName);

        ResultSet rs = ocs.executeQuery();

        if (rs.next() != false) {
            myOldSystemId  = rs.getInt("sys_id");
            mySystemPrefix = rs.getString("sys_prefix");
            myMaxRequestId = rs.getInt("max_request_id");

            CallableStatement ncs = myNewCon.prepareCall("stp_xmigrate_insertBusinessArea " + "?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?, ?");

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
            ncs.setString(15, getSysConfig(rs));
            ncs.setString(16, "");    // Todo: Field config generation.

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
            throw new TBitsException("This Business Area you are trying to migrate does not exist." + mySystemName);
        }
    }

    /**
     * This method migrates the category-user records of the businessarea under
     * migration.
     */
    private void migrateCategoryUsers() throws TBitsException, SQLException {
        CallableStatement stmt = myOldCon.prepareCall("stp_xmigrate_getCategoryUsersBySystemId ?");

        stmt.setInt(1, myOldSystemId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next() != false) {
            try {
                CallableStatement stmt1 = myNewCon.prepareCall("stp_xmigrate_insertTypeUser ?, ?, ?, ?, ?, ?, ?, ?");

                stmt1.setInt(1, myNewSystemId);
                stmt1.setInt(2, rs.getInt("field_id"));
                stmt1.setInt(3, rs.getInt("category_id"));
                stmt1.setInt(4, getUserIdInNewSystem(rs.getInt("user_id")));

                // Since these are category assignees, User type id is ASSIGNEE
                stmt1.setInt(5, UserType.ASSIGNEE);

                //
                // Get the correct notification id.
                //
                boolean sendMail = rs.getBoolean("always_send_mail");

                if (sendMail == false) {
                    stmt1.setInt(6, 1);
                } else {
                    stmt1.setInt(6, 2);
                }

                stmt1.setBoolean(7, rs.getBoolean("is_volunteer"));
                stmt1.setBoolean(8, rs.getBoolean("rr_volunteer"));
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
     * This method migrates the exclusion-list records of the businessarea
     * under migration.
     */
    public void migrateExclusionList() throws TBitsException, SQLException {
        CallableStatement stmt = myOldCon.prepareCall("stp_xmigrate_getExclusionListBySystemId ?");

        stmt.setInt(1, myOldSystemId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next() != false) {
            try {
                CallableStatement stmt1 = myNewCon.prepareCall("stp_xmigrate_insertExclusionList ?, ?, ?");

                stmt1.setInt(1, myNewSystemId);
                stmt1.setInt(2, getUserIdInNewSystem(rs.getInt("user_id")));

                // Default exclusion is on logger role.
                stmt1.setInt(3, UserType.LOGGER);
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
     * This method migrates the field records of the Business area under
     * migration.
     */
    private void migrateFields() throws TBitsException, SQLException {
        CallableStatement ocs = myOldCon.prepareCall("stp_xmigrate_getFieldsBySystemId ?");

        ocs.setInt(1, myOldSystemId);

        ResultSet rs = ocs.executeQuery();

        if (rs != null) {
            while (rs.next() != false) {
                String fieldName  = rs.getString("name").toLowerCase();
                int    fieldId    = rs.getInt("field_id");
                int    dataType   = 1;
                int    permission = 0;

                //
                // Following fields are removed in 2.2
                // - Sequence Number
                // - mails_send (replaced by notify)
                // - mail_only_loggers_assignees
                //
                if ((fieldId == 16) || (fieldId == 26) || (fieldId == 27)) {
                    continue;
                }

                if (fieldId < 27) {
                    fieldId    = ourNameToIdMap.get(fieldName);
                    permission = ourNameToPermMap.get(fieldName);
                } else {

                    // Since we added two new fields, displace the ids of
                    // extended fields by 2.
                    fieldId    = fieldId + 2;
                    permission = 255;
                }

                CallableStatement ncs = myNewCon.prepareCall("stp_xmigrate_insertField " + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");

                ncs.setInt(1, myNewSystemId);
                ncs.setInt(2, fieldId);
                ncs.setString(3, rs.getString("name"));
                ncs.setString(4, rs.getString("display_name"));
                ncs.setString(5, rs.getString("description"));
                ncs.setInt(6, getDataType(fieldId, rs.getString("data_type"), rs.getBoolean("is_type")));
                ncs.setBoolean(7, rs.getBoolean("is_active"));
                ncs.setBoolean(8, rs.getBoolean("is_extended"));
                ncs.setBoolean(9, false);    // is_private is false by default.
                ncs.setInt(10, ourTrackingMap.get(rs.getInt("is_tracked")));

                //
                // Todo: permission should be adjusted to accomodate
                // bits related to SEARCH, DISPLAY, D_ACTION.
                //
                ncs.setInt(11, permission);
                ncs.setString(12, rs.getString("regex"));
                ncs.execute();
                ncs.close();
            }

            // insert the append_interface and notify options.
            insertField(25, "append_interface", "append_interface", DataType.INT);
            insertField(26, "notify", "notify", DataType.BOOLEAN);
            insertField(27, "notify_loggers", "notify_loggers", DataType.BOOLEAN);
            insertField(28, "replied_to_action", "replied_to_action", DataType.INT);
            insertField(29, "related_requests", "related_requests", DataType.STRING);
            rs.close();
            ocs.close();
        }
    }

    /**
     * This method migrates the requests_ex records of the businessarea
     * under migration.
     */
    private void migrateRequestEx() throws TBitsException, SQLException {
        int start    = 0;
        int interval = 5000;
        int end      = myMaxRequestId + interval;

        for (int i = start; i <= end; i = i + interval) {
            LOG.info("Migrating Requests Ex in request range [ " + i + ", " + (i + interval) + " ] ....\n");

            CallableStatement stmt = myOldCon.prepareCall("stp_xmigrate_getRequestsExBySystemId ?, ?, ?");

            stmt.setInt(1, myOldSystemId);
            stmt.setInt(2, i);
            stmt.setInt(3, i + interval);

            ResultSet rs = stmt.executeQuery();

            while (rs.next() != false) {
                try {
                    CallableStatement stmt1   = myNewCon.prepareCall("stp_xmigrate_insertRequestEx " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?");
                    int               fieldId = rs.getInt("field_id");

                    //
                    // Since we added two new fields, displace the ids of
                    // extended fields by 2.
                    //
                    fieldId = fieldId + 2;
                    stmt1.setInt(1, myNewSystemId);
                    stmt1.setInt(2, rs.getInt("request_id"));
                    stmt1.setInt(3, fieldId);
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
     * This method migrates the requests_users records of the businessarea
     * under migration.
     */
    private void migrateRequestUsers() throws TBitsException, SQLException {
        int start    = 0;
        int interval = 5000;
        int end      = myMaxRequestId + interval;

        for (int i = start; i <= end; i = i + interval) {
            LOG.info("Migrating Request Users in request range [ " + i + ", " + (i + interval) + " ] ....\n");

            CallableStatement stmt = myOldCon.prepareCall("stp_xmigrate_getRequestUsersBySystemId ?, ?, ?");

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
                curReqId = rs.getInt("request_id");
                curUTId  = rs.getInt("user_type_id");

                //
                // Reset the ordering if there is a change in any of these
                // - Request Id.
                // - User Type Id.
                //
                if (((oldReqId != 0) && (curReqId != oldReqId)) || ((curUTId != 0) && (curUTId != oldUTId))) {
                    ordering = 0;
                }

                oldReqId = curReqId;
                oldUTId  = curUTId;
                ordering++;

                try {
                    CallableStatement stmt1 = myNewCon.prepareCall("stp_xmigrate_insertRequestUser ?, ?, ?, ?, ?, ?");

                    stmt1.setInt(1, myNewSystemId);
                    stmt1.setInt(2, rs.getInt("request_id"));
                    stmt1.setInt(3, rs.getInt("user_type_id"));
                    stmt1.setInt(4, getUserIdInNewSystem(rs.getInt("user_id")));
                    stmt1.setInt(5, ordering);
                    stmt1.setBoolean(6, false);    // Default is_primary is false.
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
     * This method migrates the requests records of the businessarea
     * under migration.
     */
    private void migrateRequests() throws TBitsException, SQLException {
        CallableStatement stmt = myOldCon.prepareCall("stp_xmigrate_getRequestsBySystemId ?");

        stmt.setInt(1, myOldSystemId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next() != false) {
            try {
                CallableStatement stmt1 = myNewCon.prepareCall("stp_xmigrate_insertRequest " + "?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?");

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
                stmt1.setString(17, getNewAttFormat(rs.getString("attachments"), false));
                stmt1.setString(18, rs.getString("summary"));
                stmt1.setString(19, rs.getString("memo"));
                stmt1.setInt(20, SOURCE_WEB);
                stmt1.setInt(21, rs.getInt("mails_send"));
                stmt1.setBoolean(22, rs.getBoolean("logger_mail_required"));
                stmt1.setInt(23, 0);
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
     * This method migrates the role-permission records of the businessarea
     * under migration.
     */
    private void migrateRolePermissions() throws TBitsException, SQLException {
        CallableStatement stmt = myOldCon.prepareCall("stp_xmigrate_getRolePermissionsBySystemId ?");

        stmt.setInt(1, myOldSystemId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next() != false) {
            try {
                int fieldId = rs.getInt("field_id");

                //
                // Following fields are removed in 2.2
                // - Sequence Number
                // - mails_send (replaced by notify)
                // - mail_only_loggers_assignees
                //
                if ((fieldId == 16) || (fieldId == 26) || (fieldId == 27)) {
                    continue;
                }

                if (fieldId < 27) {
                    String fieldName = rs.getString("fieldname").toLowerCase();

                    fieldId = ourNameToIdMap.get(fieldName);
                } else {

                    // Since we added two new fields, displace the ids of
                    // extended fields by 2.
                    fieldId = fieldId + 2;
                }

                CallableStatement stmt1 = myNewCon.prepareCall("stp_xmigrate_insertRolePermission ?, ?, ?, ?, ?");

                stmt1.setInt(1, myNewSystemId);
                stmt1.setInt(2, rs.getInt("role_id"));
                stmt1.setInt(3, fieldId);
                stmt1.setInt(4, rs.getInt("permission"));
                stmt1.setInt(5, 0);    // To denied permissions to start with.
                stmt1.execute();
                stmt1.close();
            } catch (Exception e) {
                LOG.info("",(e));
            }
        }

        rs.close();
        stmt.close();
        stmt = null;

        //
        // Insert the permission corresponding to the new fields.
        // This procedure will also insert the default descriptors.
        //
        stmt = myNewCon.prepareCall("stp_xmigrate_insertDefaultRecords ?");
        stmt.setInt(1, myNewSystemId);
        stmt.execute();
        stmt.close();
        stmt = null;
    }

    /**
     * This method migrates the role-user records of the businessarea
     * under migration.
     */
    private void migrateRoleUsers() throws TBitsException, SQLException {
        CallableStatement stmt = myOldCon.prepareCall("stp_xmigrate_getRoleUsersBySystemId ?");

        stmt.setInt(1, myOldSystemId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next() != false) {
            try {
                CallableStatement stmt1 = myNewCon.prepareCall("stp_xmigrate_insertRoleUser ?, ?, ?");

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
     * This method migrates the role records of the businessarea
     * under migration.
     */
    private void migrateRoles() throws TBitsException, SQLException {
        CallableStatement stmt = myOldCon.prepareCall("stp_xmigrate_getRolesBySystemId ?");

        stmt.setInt(1, myOldSystemId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next() != false) {
            try {
                CallableStatement stmt1 = myNewCon.prepareCall("stp_xmigrate_insertRole ?, ?, ?, ?");

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
     * This method migrates the types records of the business area under
     * migration.
     */
    private void migrateTypes() throws TBitsException, SQLException {
        CallableStatement ocs = myOldCon.prepareCall("stp_xmigrate_getTypesBySystemId ?");

        ocs.setInt(1, myOldSystemId);

        ResultSet rs = ocs.executeQuery();

        if (rs != null) {
            int oldFieldId = 0;
            int curFieldId = 0;

            while (rs.next() != false) {
                CallableStatement ncs     = myNewCon.prepareCall("stp_xmigrate_insertType " + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
                int               fieldId = rs.getInt("field_id");

                if (fieldId > 27) {
                    fieldId = fieldId + 2;
                }

                curFieldId = fieldId;
                oldFieldId = curFieldId;

                String description = rs.getString("description");

                description = (description == null)
                              ? ""
                              : description;
                ncs.setInt(1, myNewSystemId);
                ncs.setInt(2, fieldId);
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
     * This method reads the configuration file "xmigrate.rc"
     */
    private boolean readConfigurationFile() {
        Properties prop = new Properties();

        try {
            File            migrateConfFile = Configuration.findPath("xmigrate.rc");
            FileInputStream fis             = new FileInputStream(migrateConfFile);

            prop.load(fis);
            fis.close();
            fis = null;
        } catch (IOException ioe) {
            LOG.info("An exception occured while setting config file", ioe);
            return false;
        } catch (NullPointerException npe) {
            LOG.info("\n\tPlease Check if xmigrate.rc is present." + "\n\tExiting Migration Process\n");
            return false;
        }

        // Properties of old database.
        myOldServer         = prop.getProperty("OLD_DB_SERVER_NAME");
        myOldDB             = prop.getProperty("OLD_DB_NAME");
        myOldUser           = prop.getProperty("OLD_DB_LOGIN");
        myOldPassword       = prop.getProperty("OLD_DB_PASSWORD");
        myOldAttachmentPath = prop.getProperty("OLD_ATTACHMENT_LOCATION");

        // Properties of new database.
        myNewServer         = prop.getProperty("NEW_DB_SERVER_NAME");
        myNewDB             = prop.getProperty("NEW_DB_NAME");
        myNewUser           = prop.getProperty("NEW_DB_LOGIN");
        myNewPassword       = prop.getProperty("NEW_DB_PASSWORD");
        myNewAttachmentPath = prop.getProperty("NEW_ATTACHMENT_LOCATION");

        // SQL Database Driver properties.
        driverClass = prop.getProperty("DRIVER_CLASS");
        driverTag   = prop.getProperty("DRIVER_TAG");

        // Comma separated list of business areas to be migrated.
        baList  = prop.getProperty("BUSINESS_AREA_LIST");
        sysList = Utilities.toArrayList(baList);
        return true;
    }

    /**
     * This method prints the information read from the configuration file
     * and obtains the confirmation to proceed.
     */
    private boolean verifyInput() {
        System.out.print("/~~~~~~~~~~~~~~~~" + "Configuration info being used for migration" + "~~~~~~~~~~~~~~~~~/\n\n" + "\t\tOld Database Machine Name : " + myOldServer + "\n"
                         + "\t\tOld Database Name         : " + myOldDB + "\n" + "\t\tOld User Name             : " + myOldUser + "\n" + "\t\tOld Password              : " + "*****" + "\n\n"
                         + "\t\tNew Database Machine Name : " + myNewServer + "\n" + "\t\tNew Database Name         : " + myNewDB + "\n" + "\t\tNew User Name             : " + myNewUser + "\n"
                         + "\t\tNew Password              : " + "*****" + "\n\n" + "\t\tOld Attachments Folder    : " + myOldAttachmentPath + "\n" + "\t\tNew Attachments Folder    : "
                         + myNewAttachmentPath + "\n\n" + "\t\tBusiness Area(s)          : " + baList + "\n\n");

        try {
            System.out.print("Would you like to start migration with this info (y/n): ");

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
     * This method returns the datatype in the new system given the fieldid
     * data type in old system and boolean to specify if this is a type'd field
     * in the old system.
     *
     * @param aFieldId  field id.
     * @param aDataType Data type of this field in old system.
     * @param aIsType   Boolean to specify if this is a type'd field.
     *
     * @return DataTypeId in the new system.
     */
    private int getDataType(int aFieldId, String aDataType, boolean aIsType) {
        switch (aFieldId) {
        case 14 :    // Confidential.
            return DataType.BOOLEAN;

        case 18 :    // Due Date
        case 19 :    // Logged Date
        case 20 :    // Update Date
            return DataType.DATETIME;

        case 1 :     // System Id.
        case 2 :     // Request Id.
        case 15 :    // Parent RequestId.
        case 17 :    // Max Action Id.
            return DataType.INT;

        case 12 :    // Subject
        case 29 :    // Related Requests
            return DataType.STRING;

        case 13 :    // Description
        case 21 :    // Header Description
        case 22 :    // Attachments
        case 23 :    // Summary
        case 24 :    // Memo
            return DataType.TEXT;

        case 3 :     // Category
        case 4 :     // Status
        case 5 :     // Severity
        case 6 :     // Request Type
            return DataType.TYPE;

        case 7 :     // Logger
        case 8 :     // Assignee
        case 9 :     // Subscriber
        case 10 :    // To
        case 11 :    // Cc
        case 16 :    // User
            return DataType.USERTYPE;

        default :

        // The case of an extended field.
        {
            if (aDataType.trim().equalsIgnoreCase("string") == true) {
                return DataType.STRING;

                //
                // In case of an int datatype, it can be a normal integer field
                // or a type field.
                //
            } else if ((aDataType.trim().equalsIgnoreCase("int") == true) && (aIsType == false)) {
                return DataType.INT;
            } else if ((aDataType.trim().equalsIgnoreCase("int") == true) && (aIsType == true)) {
                return DataType.TYPE;
            } else if (aDataType.trim().equalsIgnoreCase("double") == true) {
                return DataType.REAL;
            } else if (aDataType.trim().equalsIgnoreCase("datetime") == true) {
                return DataType.DATETIME;
            } else if (aDataType.trim().equalsIgnoreCase("boolean") == true) {
                return DataType.BOOLEAN;
            }
        }
        }

        return DataType.STRING;
    }

    /**
     * Method that parses the BARules and obtains the Customlinks and the
     * rules related to Severity.
     *
     * @param aRulesConfig BA Config in old system.
     * @param aSysConfig  Sysconfig object in new system. Out Parameter.
     */
    private void getLinksAndRules(String aRulesConfig, SysConfig aSysConfig) throws TBitsException {
        ArrayList<CustomLink>  customLinks     = new ArrayList<CustomLink>();
        DocumentBuilderFactory factory         = DocumentBuilderFactory.newInstance();
        DocumentBuilder        documentBuilder = null;
        Document               document        = null;

        try {
            documentBuilder = factory.newDocumentBuilder();
            document        = (org.w3c.dom.Document) documentBuilder.parse(new ByteArrayInputStream(aRulesConfig.getBytes()));

            NodeList rootNodeList    = document.getElementsByTagName("BusinessArea");
            Node     rootNode        = rootNodeList.item(0);
            NodeList optionsNodeList = rootNode.getChildNodes();

            for (int i = 0; i < optionsNodeList.getLength(); i++) {
                Node optionsNode = optionsNodeList.item(i);

                if (optionsNode.getNodeName().equals("CustomLinks")) {
                    NodeList linkNodes      = optionsNode.getChildNodes();
                    int      linkNodeLength = linkNodes.getLength();

                    for (int j = 0; j < linkNodeLength; j++) {
                        Node aLinkNode = linkNodes.item(j);

                        if (aLinkNode.getNodeName().equals("Link")) {
                            StringBuilder str      = new StringBuilder();
                            NamedNodeMap  nnmap    = aLinkNode.getAttributes();
                            Node          attrNode = null;
                            CustomLink    cl       = new CustomLink();

                            attrNode = nnmap.getNamedItem("name");
                            cl.setName(attrNode.getNodeValue());
                            attrNode = nnmap.getNamedItem("value");
                            cl.setValue(attrNode.getNodeValue());
                            customLinks.add(cl);
                        }
                    }
                }

                if (optionsNode.getNodeName().equals("Severity")) {
                    NodeList sevNodes      = optionsNode.getChildNodes();
                    int      sevNodeLength = sevNodes.getLength();

                    for (int j = 0; j < sevNodeLength; j++) {
                        Node aSevNode = sevNodes.item(j);

                        if (aSevNode.getNodeName().equals("Incoming")) {
                            NamedNodeMap nnmap        = aSevNode.getAttributes();
                            Node         attrNode     = nnmap.getNamedItem("highValue");
                            String       incomingHigh = attrNode.getNodeValue();

                            incomingHigh = (incomingHigh == null)
                                           ? ""
                                           : incomingHigh.trim();
                            aSysConfig.setIncomingSeverityHigh(incomingHigh.toUpperCase());
                            attrNode = nnmap.getNamedItem("lowValue");

                            String incomingLow = attrNode.getNodeValue();

                            incomingLow = (incomingLow == null)
                                          ? ""
                                          : incomingLow.trim();
                            aSysConfig.setIncomingSeverityLow(incomingLow.toUpperCase());
                        }

                        if (aSevNode.getNodeName().equals("Outgoing")) {
                            NamedNodeMap nnmap        = aSevNode.getAttributes();
                            Node         attrNode     = nnmap.getNamedItem("highValue");
                            String       outgoingHigh = attrNode.getNodeValue();

                            outgoingHigh = (outgoingHigh == null)
                                           ? ""
                                           : outgoingHigh.trim();
                            aSysConfig.setOutgoingSeverityHigh(Utilities.toArrayList(outgoingHigh.toUpperCase()));
                            attrNode = nnmap.getNamedItem("lowValue");

                            String outgoingLow = attrNode.getNodeValue();

                            outgoingLow = (outgoingLow == null)
                                          ? ""
                                          : outgoingLow.trim();
                            aSysConfig.setOutgoingSeverityLow(Utilities.toArrayList(outgoingLow.toUpperCase()));
                        }
                    }
                }
            }

            aSysConfig.setCustomLinks(customLinks);
        } catch (Exception e) {
            throw new TBitsException(e.toString());
        }
    }

    /**
     * This method converts the attachment format in old system to that in the
     * new system.
     *
     * @param attachments Comma separated list of attachments.
     * @param copy        Boolean value to specify if the attachments should
     *                    be copied from old location to the new location.
     *
     * @return String representation of the attachments.
     */
    private String getNewAttFormat(String attachments, boolean copy) {
        if ((attachments == null) || attachments.trim().equals("")) {
            return "";
        }

        ArrayList<Attachment> attList = new ArrayList<Attachment>();

        try {
            ArrayList<String> list = Utilities.toArrayList(attachments);
            int               size = list.size();

            for (int i = 0; i < size; i++) {
                String aAttachment = list.get(i);

                try {
                    StringTokenizer st = new StringTokenizer(aAttachment, "-");

                    // Old Format is
                    // [<SysId>-<ReqId>-<ActId>-<Size>-<CF>-Filename]
                    String        sysId      = st.nextToken();
                    String        reqId      = st.nextToken();
                    String        actId      = st.nextToken();
                    String        fileSize   = st.nextToken();
                    String        CF         = st.nextToken();
                    StringBuilder tempBuffer = new StringBuilder();

                    tempBuffer.append(st.nextToken());

                    while (st.hasMoreTokens()) {
                        tempBuffer.append("-").append(st.nextToken());
                    }

                    String  fname         = tempBuffer.toString();
                    String  oldAttachment = myOldAttachmentPath + "/" + aAttachment;
                    boolean isConverted   = false;

                    if ((CF != null) && (CF.equals("1") == true)) {
                        isConverted = true;
                    }

                    //
                    // New Format is
                    // [<ReqId>-<ActId>-<Size>-<CF>-Filename]
                    // CF is 0 by default.
                    //
                    //
                    // Attachment information should be stored in the form
                    // of an xml which is as follows.
                    // <Attachment
                    // name="<Attachment name in file system."
                    // displayName="Name as entered by the user."
                    // size="Size of the attachment as a string."
                    // bytes="Size of the attachment in bytes."
                    // convert="Boolean set if this is converted."
                    // />
                    //
                    String temp             = reqId + "-" + actId + "-" + fname;
                    String newAttachmentDir = myNewAttachmentPath + "/" + mySystemPrefix.toLowerCase();

                    //
                    // Check if the attachment directory exists. If not,
                    // create it.
                    //
                    File fdir = new File(newAttachmentDir);

                    if (fdir.exists() == false) {
                        fdir.mkdirs();
                    }

                    String     newAttachment = myNewAttachmentPath + "/" + mySystemPrefix.toLowerCase() + "/" + temp;
                    File       file          = new File(newAttachment);
                    String     attId         = reqId + "_" + actId + "_" + i;
                    Attachment obj           = new Attachment();

                    obj.setId(attId);
                    obj.setName(temp);
                    obj.setDisplayName(fname);
                    obj.setSizeInBytes(file.length());
                    obj.setSize(fileSize);
                    obj.setIsConverted(isConverted);
                    attList.add(obj);

                    // Finally copy the file if required.
                    if (copy == true) {
                        copyFile(oldAttachment, newAttachment);
                    }
                } catch (Exception e) {
                    LOG.info("Could not convert the name to new format.: \n" + "Attachment    : " + aAttachment + "\n" + "AttachmentList: " + attachments);
                }
            }
        } catch (Exception e) {
            LOG.info("an exception has occured while copying attachments", e);
            e.printStackTrace();
        }

        String xml = Attachment.xmlSerialize(attList);

        return xml;
    }

    /**
     * This method generates the sysconfig for this business area to be
     * inserted into 2.2 database.
     *
     * @param rs Resultset which is currently pointing to the BA Record.
     *
     * @return SysConfig xml for the new database.
     */
    private String getSysConfig(ResultSet rs) throws TBitsException, SQLException {
        SysConfig sc = new SysConfig();

        sc.setActionNotify(rs.getInt("action_email"));
        sc.setActionNotifyLoggers(rs.getBoolean("user_mail_required"));
        sc.setAllowNullDueDate(false);
        sc.setAssignToAll(rs.getBoolean("assign_to_all"));
        sc.setBmp2Png(rs.getBoolean("attachment_convert_option"));
        sc.setDefaultDueDate(rs.getLong("default_due_date"));
        sc.setEmailDateFormat(rs.getInt("email_datetime_format"));
        sc.setEmailStylesheet("tbits_2_2_email.css");
        sc.setLegacyPrefixList(Utilities.toArrayList(rs.getString("legacy_prefixes")));
        sc.setListDateFormat(rs.getInt("list_datetime_format"));
        sc.setMailFormat(rs.getInt("mail_format"));
        sc.setRequestNotify(rs.getInt("request_email"));
        sc.setRequestNotifyLoggers(rs.getBoolean("logger_mail_required"));
        sc.setVolunteer(rs.getInt("assign_volunteer_by"));
        sc.setWebStylesheet("tbits_2_2_web.css");
        getLinksAndRules(rs.getString("business_rules_config"), sc);

        return sc.xmlSerialize();
    }

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

        if (value.indexOf('@') < 0) {

            // If the old normal-user is not found, then insert him as a
            // user (UserType = 7) and return the new Id after inserting this
            // in our newLoginMap.
            int userId = insertNewUser(value);

            newLoginMap.put(value, new Integer(userId));
            myUserMap.put(new Integer(userId), value);

            return userId;
        } else {

            // If the old mailing list is not found, insert it as a
            // mailing-list (UserType = 8) and return the new Id after
            // inserting this in our newEmailMap.
            int userId = insertNewMailingList(value);

            newLoginMap.put(value, new Integer(userId));
            myUserMap.put(new Integer(userId), value);

            return userId;
        }
    }
}
