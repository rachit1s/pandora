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
 * MigrateUserTracking.java
 *
 *
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.ActionUser;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;

import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;

//~--- JDK imports ------------------------------------------------------------

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

//~--- classes ----------------------------------------------------------------

/**
 * This class migrates the tracking records for users in 6.0 format to those in
 * 3.1 format.
 * <pre>
 *  Assumptions:
 * ============
 * 1. Ids and names of logger/assignee/subscriber fields are assumed to be the
 *    current values.
 * 2. The tracking options of logger/assignee/subscriber are assumed to be 3,3
 *    and 5 respectively.
 *
 *
 * Steps to be followed:
 * =====================
 * 1. Confirm that the system prefix is passed on the command line.
 * 2. Establish a connection with the database.
 * 3. Get the corresponding business area id.
 * 4. For each 5000 requests in the business area:
 *
 *     4.1. Get the user records for the actions within the current request
 *              range in the order of action_id, request_id. Store this in a table
 *              with [request_id-action_id-user_type_id, ArrayList of action users]
 *
 *     4.2. Get the header description for the actions within the current
 *              request range in the order of action_id, request_id.
 *
 *     4.3. Tokenize the header description on newlines.
 *
 *     4.4. Iterate through the token list:
 *
 *          4.4.1. if the token corresponds to logger
 *                 4.4.1.1. Check if this record corresponds to header about
 *                                      logging on behalf of someone else. If so add it to
 *                                      new header.
 *
 *                 4.4.1.2. Otherwise this cannot be first action of the request.
 *                                      So take the previous action-user list and the
 *                                      current action-user list and call the
 *                                      formTrackRecord() method. Append the return value of
 *                                      this method to the new header.
 *
 *          4.4.2. if the token corresponds to assignee then this cannot be the
 *                 first action. Do as in 4.4.1.2.
 *
 *          4.4.3. if the token corresponds to subscriber
 *                 4.4.3.1 Check if this record corresponds to first action of
 *                                 the request. If so add it to the new header as is.
 *                                 If not call the formTrackUserRecord method that takes
 *                                 the current and the previous action user lists as
 *                                 parameters.
 *
 * </pre>
 *
 * @author Vaibhav.
 * @version $Id: $
 *
 */
public class MigrateUserTracking {

    // Application logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_UTIL);

    //~--- fields -------------------------------------------------------------

    // Table that holds the action users for the current cycle.
    private Hashtable<String, ArrayList<ActionUser>> myActionUserTable;
    private BusinessArea                             myBA;

    // BA details.
    private String mySysPrefix;

    //~--- constructors -------------------------------------------------------

    /**
     * Only constructor of the class that expects the prefix.
     *
     * @param sysPrefix
     */
    public MigrateUserTracking(String sysPrefix) {
        mySysPrefix = sysPrefix;
        migrate();
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method returns the action log for the first action.
     *
     * @param aField    User field.
     * @param aList     List of action users.
     * @return  Track record.
     */
    private String formTrackRecord(Field aField, ArrayList<ActionUser> aList) {
        StringBuilder aRecord = new StringBuilder();
        StringBuilder newList = new StringBuilder();
        boolean       cFirst  = true;

        if ((aList != null) && (aList.size() != 0)) {
            for (ActionUser au : aList) {
                User user = null;

                try {
                    user = User.lookupAllByUserId(au.getUserId());
                } catch (DatabaseException de) {
                    continue;
                }

                if (user == null) {
                    continue;
                }

                String login    = ((au.getIsPrimary() == true)
                                   ? "*"
                                   : "") + user.getUserLogin().replace(".transbittech.com", "");
                int    ordering = au.getOrdering();

                if (cFirst == false) {
                    newList.append(",");
                } else {
                    cFirst = false;
                }

                // Add this entry as ordering:login.
                newList.append(ordering).append(":").append(login);
            }    // End For

            StringBuffer entryPrefix = new StringBuffer();

            entryPrefix.append(aField.getName()).append("##").append(aField.getFieldId()).append("##");

            int trackingOption = aField.getTrackingOption();

            if (newList.toString().trim().equals("") == false) {
                aRecord.append("+").append(entryPrefix).append("[").append(newList.toString().trim()).append("]").append("\n");
            }
        }

        return aRecord.toString();
    }

    /**
     * This method prepares the tracking record for a user field.
     *
     * @param aField                Field object of the user field.
     * @param aOldList              Old list of action users.
     * @param aNewList              New list of action users.
     *
     * @return Track record.
     */
    private String formTrackRecord(Field aField, ArrayList<ActionUser> aOldList, ArrayList<ActionUser> aNewList) {
        StringBuilder aRecord = new StringBuilder();

        /*
         * Steps to deduce the diff for these user fields:
         *  1. Prepare newTable to hold request users as [Login, ActionUser]
         *     records.
         *  2. Maintain three StringBuilders: addedList, commonList, deleteList.
         *     - addedList will hold the user logins added in this update.
         *     - commonList will hold the user logins retained in this update.
         *     - deleteList will hold the user logins removed in this update.
         *  3. Iterate through the old list.
         *     - Check if the entry is present in new list also.
         *     - If present, get the ordering of this entry in the new list and
         *       prefix it to the login and add this to commonList.
         *     - If not present, get the ordering of this entry in the old list
         *       and prefix it to the login and add this to deletedList.
         */
        String                        login    = "";
        Hashtable<String, ActionUser> newTable = new Hashtable<String, ActionUser>();

        if (aNewList != null) {
            for (ActionUser au : aNewList) {
                User user = null;

                try {
                    user = User.lookupAllByUserId(au.getUserId());
                } catch (DatabaseException de) {
                    continue;
                }

                if (user == null) {
                    continue;
                }

                login = ((au.getIsPrimary() == true)
                         ? "*"
                         : "") + user.getUserLogin();
                newTable.put(login, au);
            }
        } else {
            aNewList = new ArrayList<ActionUser>();
        }

        StringBuilder addedList   = new StringBuilder();
        StringBuilder commonList  = new StringBuilder();
        StringBuilder deletedList = new StringBuilder();
        boolean       cFirst      = true;
        boolean       aFirst      = true;
        boolean       dFirst      = true;
        boolean       changed     = false;

        if (aOldList != null) {
            for (ActionUser au : aOldList) {
                User user = null;

                try {
                    user = User.lookupAllByUserId(au.getUserId());
                } catch (DatabaseException de) {
                    continue;
                }

                if (user == null) {
                    continue;
                }

                login = ((au.getIsPrimary() == true)
                         ? "*"
                         : "") + user.getUserLogin();

                ActionUser ruNew = newTable.get(login);

                if (ruNew != null) {

                    /*
                     * This is a common entry. take the ordering from the new
                     * one.
                     */
                    int ordering = ruNew.getOrdering();

                    if (cFirst == false) {
                        commonList.append(",");
                    } else {
                        cFirst = false;
                    }

                    /*
                     * Add this entry as ordering:login.
                     */
                    commonList.append(ordering).append(":").append(login);

                    /*
                     * Remove this entry from the newTable.
                     */
                    newTable.remove(login);
                } else {

                    /*
                     * This entry is deleted during this append.
                     */
                    int ordering = au.getOrdering();

                    if (dFirst == false) {
                        deletedList.append(",");
                    } else {
                        dFirst = false;
                    }

                    /*
                     * Add this entry as ordering:login.
                     */
                    deletedList.append(ordering).append(":").append(login);
                    changed = true;
                }    // End If
            }        // End For
        } else {
            aOldList = new ArrayList<ActionUser>();
        }

        /*
         * Now, the entries left in the newTable are added during this append.
         * Added these to the addedList.
         */
        ArrayList<ActionUser> newList = new ArrayList<ActionUser>(newTable.values());

        for (ActionUser au : newList) {
            User user = null;

            try {
                user = User.lookupAllByUserId(au.getUserId());
            } catch (DatabaseException de) {
                continue;
            }

            if (user == null) {
                continue;
            }

            login = ((au.getIsPrimary() == true)
                     ? "*"
                     : "") + user.getUserLogin();

            int ordering = au.getOrdering();

            if (aFirst == false) {
                addedList.append(",");
            } else {
                aFirst = false;
            }

            addedList.append(ordering).append(":").append(login);
            changed = true;
        }

        StringBuffer entryPrefix = new StringBuffer();

        entryPrefix.append(aField.getName()).append("##").append(aField.getFieldId()).append("##");

        /*
         * Add the common list only in atleas one of the following cases: 1.
         * There is a change. 2. Tracking option is 4. 3. Tracking option is 5
         * and the newList is not empty.
         */
        int     trackingOption = aField.getTrackingOption();
        boolean isNewListEmpty = ((aNewList.size() > 0)
                                  ? false
                                  : true);

        if ((changed == true) || (trackingOption == 4) || ((trackingOption == 5) && (isNewListEmpty == false))) {
            if (commonList.toString().trim().equals("") == false) {
                aRecord.append("*").append(entryPrefix).append("[").append(commonList.toString().trim()).append("]").append("\n");
            }
        }

        /*
         * Add the addedList and deleteList unless they are empty.
         */
        if (addedList.toString().trim().equals("") == false) {
            aRecord.append("+").append(entryPrefix).append("[").append(addedList.toString().trim()).append("]").append("\n");
        }

        if (deletedList.toString().trim().equals("") == false) {
            aRecord.append("-").append(entryPrefix).append("[").append(deletedList.toString().trim()).append("]").append("\n");
        }

        return aRecord.toString();
    }

    /**
     * @param args
     */
    public static int main(String[] args) {
        if (args.length != 1) {
            System.err.println("\n\tUsage: MigrateUserTracking <BA Prefix>\n");
           return 1;// System.exit(1);
        }

        new MigrateUserTracking(args[0]);
        return 0;//System.exit(0);
    }

    /**
     *  Method that initiates the process.
     */
    private void migrate() {
        if (validateBusinessArea() == false) {
            return;//System.exit(1);
        }

        Connection con = null;

        try {
            con = DataSourcePool.getConnection();

            /*
             * Process the action headers taking 5000 requests at a time.
             */
            int maxRequestId = myBA.getMaxRequestId();
            int blockSize    = 5000;
            int iterations   = maxRequestId / blockSize + 2;

            for (int i = 0; i < iterations; i++) {
                int start = i * blockSize;
                int end   = (i + 1) * blockSize;

                LOG.info(mySysPrefix + ": Request Range: [ " + start + ", " + end + " ]");
                myActionUserTable = getActionUserTable(con, start, end);
                processActionLog(con, start, end);
            }
        } catch (SQLException sqle) {
            LOG.severe("An exception has occurred while processing the " + "action headers of requests in " + mySysPrefix + " BA.", sqle);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException sqle) {
                    LOG.error("Exception while closing the connection", sqle);
                }
            }
        }
    }

    /**
     * This method migrates the user records present in the old header and
     * returns the new one.
     *
     * @param requestId     Request Id
     * @param actionId      Action Id
     * @param oldHeader Old Update Log.
     * @return New Update Log.
     */
    private String migrateUserTrackRecords(int requestId, int actionId, String oldHeader) {
        StringBuilder newHeader = new StringBuilder();
        boolean       show      = false;

        /*
         * Tokenize the header on newline.
         */
        StringTokenizer st = new StringTokenizer(oldHeader, "\n");

        while (st.hasMoreTokens()) {
            String record = st.nextToken().trim();

            if ((record.startsWith("logger_ids##") == true) || (record.indexOf("[ Logger ") >= 0)) {

                /*
                 * If this record corresponds to action 1, then leave it
                 * as it is.
                 */
                if (actionId == 1) {

                    // retain the record as is.
                } else {

                    /*
                     * Get the action users of the previous append and that of
                     * the current append and get the track record.
                     */
                    int                   userTypeId  = 2;    // assumed for now.
                    String                oldKey      = requestId + "-" + (actionId - 1) + "-" + userTypeId;
                    String                newKey      = requestId + "-" + actionId + "-" + userTypeId;
                    ArrayList<ActionUser> oldList     = myActionUserTable.get(oldKey);
                    ArrayList<ActionUser> newList     = myActionUserTable.get(newKey);
                    Field                 loggerField = null;

                    try {
                        loggerField = Field.lookupBySystemIdAndFieldName(myBA.getSystemId(), Field.LOGGER);
                    } catch (DatabaseException de) {
                        LOG.severe("An exception has occurred while " + "retrieving the Field object corresponding" + "to Logger.", de);
                        loggerField = null;
                    }

                    if (loggerField != null) {
                        record = formTrackRecord(loggerField, oldList, newList);
                        show   = true;
                    } else {

                        // retain the old record instead of just losing it.
                    }
                }

                newHeader.append(record).append("\n");
            } else if ((record.startsWith("assignee_ids##") == true) || (record.indexOf("[ Assignee ") >= 0)) {
                if (actionId == 1) {

                    // retain the record as is.
                } else {

                    /*
                     * Get the action users of the previous append and that of
                     * the current append and get the track record.
                     */
                    int                   userTypeId    = 3;    // That of assignee.
                    String                oldKey        = requestId + "-" + (actionId - 1) + "-" + userTypeId;
                    String                newKey        = requestId + "-" + actionId + "-" + userTypeId;
                    ArrayList<ActionUser> oldList       = myActionUserTable.get(oldKey);
                    ArrayList<ActionUser> newList       = myActionUserTable.get(newKey);
                    Field                 assigneeField = null;

                    try {
                        assigneeField = Field.lookupBySystemIdAndFieldName(myBA.getSystemId(), Field.ASSIGNEE);
                    } catch (DatabaseException de) {
                        LOG.severe("An exception has occurred while " + "retrieving the Field object corresponding" + "to Assignee.", de);
                        assigneeField = null;
                    }

                    if (assigneeField != null) {
                        record = formTrackRecord(assigneeField, oldList, newList);
                        show   = true;
                    } else {

                        // retain the old record instead of just losing it.
                    }
                }

                newHeader.append(record).append("\n");
            } else if ((record.startsWith("subscriber_ids##") == true) || (record.indexOf("[ Subscriber ") >= 0)) {
                int   userTypeId      = 4;                      // That of subscriber.
                Field subscriberField = null;

                try {
                    subscriberField = Field.lookupBySystemIdAndFieldName(myBA.getSystemId(), Field.SUBSCRIBER);
                } catch (DatabaseException de) {
                    LOG.severe("An exception has occurred while " + "retrieving the Field object corresponding" + "to Subscriber.", de);
                    subscriberField = null;
                }

                if (actionId == 1) {
                    String                key  = requestId + "-" + actionId + "-" + userTypeId;
                    ArrayList<ActionUser> list = myActionUserTable.get(key);

                    if (subscriberField != null) {
                        record = formTrackRecord(subscriberField, list);
                        show   = true;
                    } else {

                        // retain the old record instead of just losing it.
                    }
                } else {

                    /*
                     * Get the action users of the previous append and that of
                     * the current append and get the track record.
                     */
                    String                oldKey  = requestId + "-" + (actionId - 1) + "-" + userTypeId;
                    String                newKey  = requestId + "-" + actionId + "-" + userTypeId;
                    ArrayList<ActionUser> oldList = myActionUserTable.get(oldKey);
                    ArrayList<ActionUser> newList = myActionUserTable.get(newKey);

                    if (subscriberField != null) {
                        record = formTrackRecord(subscriberField, oldList, newList);
                        show   = true;
                    } else {

                        // retain the old record instead of just losing it.
                    }
                }

                newHeader.append(record).append("\n");
            } else {

                /*
                 * This record corresponds to a field which we are not
                 * interested in modifying. So carry this as is.
                 */
                newHeader.append(record).append("\n");
            }
        }

        if (show == true) {

            /*
             * Print the old and new values only when there is a change.
             */
            LOG.info("\n" + requestId + ": OldHeader:\n" + oldHeader + requestId + ": NewHeader:\n" + newHeader);
        }

        return newHeader.toString();
    }

    /**
     * This method processes the update logs of the action in the given
     * request range.
     *
     * @param con           Connection to the database.
     * @param start         Starting request id of current range.
     * @param end       Ending request id of current range.
     */
    private void processActionLog(Connection con, int start, int end) {
        Hashtable<String, ArrayList<ActionUser>> actionLogTable = new Hashtable<String, ArrayList<ActionUser>>();
        StringBuilder                            query          = new StringBuilder();

        query.append("SELECT sys_id, ").append("request_id, ").append("action_id, ").append("header_description ").append("FROM actions WHERE sys_id = ").append(myBA.getSystemId()).append(
            " AND request_id > ").append(start).append(" AND request_id <= ").append(end);

        try {
            Statement stmt = con.createStatement();
            ResultSet rs   = stmt.executeQuery(query.toString());

            if (rs != null) {
                while (rs.next() == true) {
                    int    requestId = rs.getInt("request_id");
                    int    actionId  = rs.getInt("action_id");
                    String oldHeader = rs.getString("header_description");

                    /*
                     * We have nothing to do with null/empty headers.
                     */
                    if ((oldHeader == null) || (oldHeader.trim().equals("") == true)) {
                        continue;
                    }

                    /*
                     * We have nothing to do with headers that do not contain
                     * records for any of the following:
                     *      - logger
                     *  - assignee
                     *  - subscribers
                     */
                    if ((oldHeader.indexOf("logger_ids##7") < 0) && (oldHeader.indexOf("assignee_ids##8") < 0) && (oldHeader.indexOf("subscriber_ids##9") < 0) && (oldHeader.indexOf("[ Logger ") < 0)
                            && (oldHeader.indexOf("[ Assignee ") < 0) && (oldHeader.indexOf("[ Subscriber ") < 0)) {
                        continue;
                    }

                    String newHeader = migrateUserTrackRecords(requestId, actionId, oldHeader);

                    updateHeader(con, requestId, actionId, newHeader);
                }

                rs.close();
                rs = null;
            }

            stmt.close();
            stmt = null;
        } catch (SQLException e) {
            LOG.severe("An exception while retrieving action log for " + "requests in the range [" + start + ", " + end + "].", e);
        }

        return;
    }

    /**
     * This method updates the header description of the specified action in
     * the database.
     *
     * @param con           Connection to the database.
     * @param reqId         Request Id.
     * @param actId     Action Id.
     * @param hdr           new header description for this action.
     */
    private void updateHeader(Connection con, int reqId, int actId, String hdr) {
        try {

            /*
             * Using a prepared statement here as it can take care of escaping
             * special characters in header description. Since this is a one
             * time script, stored procedure is not needed.
             */
            StringBuilder query = new StringBuilder();

            query.append("\nUPDATE actions ").append("\nSET header_description = ? ").append("\nWHERE ").append("\n\tsys_id = ? AND ").append("\n\trequest_id = ? AND ").append("\n\taction_id = ? ");

            PreparedStatement ps = con.prepareStatement(query.toString());

            ps.setString(1, hdr);
            ps.setInt(2, myBA.getSystemId());
            ps.setInt(3, reqId);
            ps.setInt(4, actId);

            boolean flag = ps.execute();
        } catch (SQLException sqle) {
            LOG.severe("An exception has occurred while updating the header " + "description of an action of a request: [Req: " + reqId + ", Act: " + actId + "]", sqle);
        }
    }

    /**
     * This method checks if the prefix corresponds to a valid business area.
     *
     * @return True if prefix is valid. False otherwise.
     */
    private boolean validateBusinessArea() {

        /*
         * Validate the prefix.
         */
        try {
            myBA = BusinessArea.lookupBySystemPrefix(mySysPrefix);
        } catch (DatabaseException de) {
            LOG.severe("An exception has occurred while validating the " + "business area prefix: " + mySysPrefix, de);

            return false;
        }

        if (myBA == null) {
            LOG.info("No business area found with prefix: " + mySysPrefix);

            return false;
        }

        return true;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method obtains the action users for requests in the given range
     * and constructs the action users table.
     *
     * @param con           Connection to the database.
     * @param start         Starting request id of current range.
     * @param end       Ending request id of current range.
     *
     * @return                      ActionUserTable.
     */
    private Hashtable<String, ArrayList<ActionUser>> getActionUserTable(Connection con, int start, int end) {
        Hashtable<String, ArrayList<ActionUser>> auTable = new Hashtable<String, ArrayList<ActionUser>>();
        StringBuilder                            query   = new StringBuilder();

        query.append("SELECT * FROM action_users WHERE sys_id = ").append(myBA.getSystemId()).append(" AND request_id > ").append(start).append(" AND request_id <= ").append(end);

        try {

            /*
             * Using a prepared statement will be good, but for now its okay.
             */
            Statement stmt = con.createStatement();
            ResultSet rs   = stmt.executeQuery(query.toString());

            if (rs != null) {
                while (rs.next() == true) {
                    ActionUser au = ActionUser.createFromResultSet(rs);

                    /*
                     * Store the action users by their request and action and
                     * user type.
                     */
                    String                key    = rs.getInt("request_id") + "-" + rs.getInt("action_id") + "-" + rs.getInt("user_type_id");
                    ArrayList<ActionUser> auList = auTable.get(key);

                    /*
                     * If there is no list corresponding to this action and
                     * user type, then create a new one.
                     */
                    if (auList == null) {
                        auList = new ArrayList<ActionUser>();
                    }

                    auList.add(au);
                    auTable.put(key, auList);
                }

                rs.close();
                rs = null;
            }

            stmt.close();
            stmt = null;
        } catch (SQLException e) {
            LOG.severe("An exception while retrieving action users for " + "requests in the range [" + start + ", " + end + "].", e);
        }

        return auTable;
    }
}
