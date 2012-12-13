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
 *
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

//Imports from other packages.
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.Mail;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.User;
import transbit.tbits.events.BeforeUserInsertEvent;
import transbit.tbits.events.EventFailureException;
import transbit.tbits.events.EventManager;

//Static Imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;
import static transbit.tbits.domain.UserType.INTERNAL_CONTACT;
import static transbit.tbits.domain.UserType.INTERNAL_MAILINGLIST;
import static transbit.tbits.domain.UserType.INTERNAL_USER;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

//~--- classes ----------------------------------------------------------------

/**
 * This script syncs the users in AD to TBits user database.
 *
 *
 * @author :  Vaibhav
 * @version : $Id: $
 */
public class AD2TBitsSync implements TBitsPropEnum {

    // Application logger to log the messages.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_UTIL);

    // Prefixes file needed to get the list of business area email addresses.
    private final static String PREFIX_FILE   = "etc/tbits.prefixes";
    public static final int     MAX_UPDATIONS = 50;

    // Threshold on the number of additions or updations at a time.
    public static final int MAX_ADDITIONS = 50;

    // Constant to hold the LDAP Provider class name.
    public static final String LDAP_PROVIDER = "com.sun.jndi.ldap.LdapCtxFactory";

    // Constant to hold the security_authentication type.
    public static final String AUTH_TYPE = "simple";

    // Parameters used while forming mail report.
    public static String ourSubject     = "";
    public static String ourToAddress   = "";
    public static String ourFromAddress = "";

    //~--- fields -------------------------------------------------------------

    // Search Base.
    //private String mySearchBase = "";

    
    // The search controls.
    SearchControls mySearchControls = null;

    // The Result Controls.
    Control[]           myRequestControls = null;
    public StringBuffer myContent         = new StringBuffer();

    // Flag set if this is a test run.
    boolean isTest = true;

    // Flag set if this run should force adding/updating beyond threshold count.
    boolean force = false;

    // Flag set if this run can update entries in the DB.
    boolean canUpdate = true;

    // Flag set if this run can add entries to the DB.
    boolean                    canAdd = false;
    Hashtable<String, ADEntry> myADContactTable;

    // Table that maps the DN and the ADEntry.
    Hashtable<String, ADEntry> myADDNTable;

    // Table that holds the GroupEmail and its member list.
    Hashtable<String, ArrayList<String>> myADGMTable;
    Hashtable<String, ADEntry>           myADGroupTable;

    // Table that holds the DNs of those entries which are not shown in the
    // address book.
    Hashtable<String, Boolean> myADHiddenEntries;

    // Tables to store the ADEntries based on their Object Class.
    Hashtable<String, ADEntry> myADUserTable;

    // List of attributes we would like to retrieve.
    private String[] myAttributes;

    // Table that holds the email address of current business areas.
    Hashtable<String, Boolean> myBAEmailAddresses;

    // Directory Context.
    private LdapContext     myContext;
    Hashtable<String, User> myDBContactTable;
    Hashtable<String, User> myDBGroupTable;

    // Table that stores the User entries keyed by User Id
    Hashtable<Integer, User> myDBMasterIdTable;
    Hashtable<String, User>  myDBMasterKeyTable;

    // Tables to store the DB Entries based on their User Type.
    Hashtable<String, User> myDBUserTable;

    // Table to hold the environment used for establishing the context.
    private Hashtable<String, String> myEnv;

    // Name of the Active Directory Host.
    private String                           myHost;
    Hashtable<User, Hashtable<String, User>> myMailListTable;

    // Page size of results.
    private int myPageSize;

    // Password to be supplied during binding the LDAP connection.
    private String myPassword;

    // Table that stores the email of those entries either whose name
    // contained 'requests' or whose member's name contained 'requests'
    Hashtable<String, Boolean> myRequestEmails;

    // User Login to be supplied during binding the LDAP connection.
    private String myUserLogin;

    //~--- constructors -------------------------------------------------------

    /**
     * The only constructor of this class.
     *
     * @param aForce  Flag set if this run can be force addition/updation of
     *                entries if the count is greater than the threshold.
     * @param aAdd    Flag set if this run can insert entries into the DB.
     * @param aUpdate Flag set if this run can update entries in the DB.
     * @param aTest   Flag set if this is a test run.
     */
    public AD2TBitsSync(boolean aForce, boolean aAdd, boolean aUpdate, boolean aTest) {
        force     = aForce;
        canAdd    = aAdd;
        canUpdate = aUpdate;
        isTest    = aTest;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method closes the context.
     */
    private void closeContext() {
        try {
            if (myContext != null) {
                myContext.close();
            }
        } catch (NamingException ne) {
            LOG.warn("Exception while closing the context: " + "",(ne));
        }

        return;
    }

    /**
     * This method compares the group members from AD and DB in the following
     * manner.
     *  I. For each email
     *     1. Get the dbMember list.
     *
     *     2. Check if the email is present in the ADGMTable..
     *
     *     3. If it is not present, it implies that this group is no
     *        more existing in AD. So, add the ID of this group to list
     *        of group ids to be completely deleted.
     *
     *     4. If present, then get the corresponding DN list and remove
     *        this entry from the ADGMTable.
     *
     *     5. For each member in the above list which is a DN,
     *        a. get the corresponding ADEntry object.
     *        b. get the mail attribute of the ADEntry if entry is not a
     *           user entry. Otherwise get the value of name attribute. Call
     *           this value "key".
     *        c. Check if there is an entry with this key in the
     *           memberTable.
     *        d. If not present, then prepare a MailListUser object and
     *           put this in addEntries list.
     *        e. If present, then remove the entry from dbMember table.
     *
     *     6. Now all the remaining entries in dbMember are stale and
     *        should be removed. So, prepare a new MailListUser object
     *        for each entry and add it to the delEntries list.
     *
     * II. Now all the remaining entries in ADGMTable are newly added
     *     groups. For each such group
     *     1. Get the user object from DBMasterKeyTable.
     *     2. Get the DNList.
     *     3. For each DN in the list
     *        a. Get the corresponding ADEntry.
     *        b. Get the mail attribute if it is not a User entry.
     *           Otherwise, get the name. Call this key.
     *        c. Get the user object corresponding to this key from
     *           DBMasterKeyTable.
     *        d. Prepare a MailListUser entry with the group id and
     *           user id and add it to the addEntries list.
     *
     */
    private void compareADDBGroupMembers() {
        if (isTest == true) {
            LOG.info("Skipping GROUP-MEMBER verification....");

            return;
        }

        // List to hold the mailListUser entries to be added.
        ArrayList<MailListUser> addEntries = new ArrayList<MailListUser>();

        // List to hold the mailListUser entries to be deleted.
        ArrayList<MailListUser> delEntries = new ArrayList<MailListUser>();

        // List to hold the Ids of groups to be completely removed.
        ArrayList<Integer> delGroups = new ArrayList<Integer>();
        Enumeration<User>  groupList = myMailListTable.keys();

        // Step I
        while (groupList.hasMoreElements() == true) {
            User   groupUser = groupList.nextElement();
            String mail      = groupUser.getEmail();

            // Step 1.
            Hashtable<String, User> dbMemberTable = myMailListTable.get(groupUser);

            if (dbMemberTable == null) {
                LOG.warn("No db member table for group: " + mail);

                continue;
            }

            // Step 2.
            ArrayList<String> adMemberList = null;

            if (myADGMTable.containsKey(mail) == false) {

                // Step 3.
                delGroups.add(groupUser.getUserId());

                continue;
            } else {

                // Step 4.
                adMemberList = myADGMTable.get(mail);
                myADGMTable.remove(mail);
            }

            // Step 5.
            for (String dn : adMemberList) {

                // Step 5.a
                ADEntry entry = myADDNTable.get(dn);

                if (entry == null) {

                    // LOG.warn( "No ADEntry for DN: " + dn);
                    continue;
                }

                // Step 5.b
                String key = "";

                if (entry.getEntryType() == ADEntry.USER_ENTRY) {
                    key = entry.getName();
                } else {
                    key = entry.getMail();
                }

                // Step 5.c
                User tmp = dbMemberTable.get(key);

                if (tmp == null) {

                    // Step 5.d
                    User newMember = myDBMasterKeyTable.get(key);

                    if (newMember == null) {
                        LOG.warn("No User object for " + key);

                        continue;
                    }

                    MailListUser mlu = new MailListUser(groupUser.getUserId(), newMember.getUserId());

                    addEntries.add(mlu);
                } else {

                    // Step 5.e
                    dbMemberTable.remove(key);
                }

                /*
                 * Check if this group member has -request/-requests/staff
                 * in its email address. If so, add the group email to
                 * the exclusion list.
                 */
                String email = entry.getMail();

                if ((email.indexOf("-request") > 0) || (email.indexOf("-requests") > 0) || (email.startsWith("staff@") == true) || (email.startsWith("staff-") == true)) {
                    myRequestEmails.put(groupUser.getEmail(), true);
                }

                if (myBAEmailAddresses.get(email) != null) {
                    myRequestEmails.put(groupUser.getEmail(), true);
                }
            }    // End of for loop iterating over DN list of a group.

            // Step 6.
            Enumeration<String> staleEntries = dbMemberTable.keys();

            while (staleEntries.hasMoreElements() == true) {
                User tmp = dbMemberTable.get(staleEntries.nextElement());

                if (tmp != null) {
                    MailListUser mlu = new MailListUser(groupUser.getUserId(), tmp.getUserId());

                    delEntries.add(mlu);
                }
            }    // End of while loop iterating over the stale members of the group.
        }        // End of while loop iterating over the group entries from DB.

        if (myADGMTable.size() > 0) {
            Enumeration<String> groupEmailList = myADGMTable.keys();

            while (groupEmailList.hasMoreElements() == true) {
                String groupEmail = groupEmailList.nextElement();

                // Step 1.
                User groupUser = myDBMasterKeyTable.get(groupEmail);

                if (groupUser == null) {
                    LOG.warn("No user object for mailing list: " + groupEmail);

                    continue;
                }

                // Step 2.
                ArrayList<String> dnList = myADGMTable.get(groupEmail);

                if (dnList == null) {

                    // Cannot be the case... but then...
                    continue;
                }

                // Step 3.
                for (String dn : dnList) {

                    // Step 3.a.
                    ADEntry dnEntry = myADDNTable.get(dn);

                    if (dnEntry == null) {

                        // TODO: resolve this completely.
                        continue;
                    }

                    // Step 3.b.
                    String key = "";

                    if (dnEntry.getEntryType() != ADEntry.USER_ENTRY) {
                        key = dnEntry.getMail();
                    } else {
                        key = dnEntry.getName();
                    }

                    // Step 3.c.
                    User member = myDBMasterKeyTable.get(key);

                    if (member == null) {
                        LOG.warn("No User obejct for " + key);

                        continue;
                    }

                    // Step 3.d.
                    MailListUser mlu = new MailListUser(groupUser.getUserId(), member.getUserId());

                    // Step 3.e.
                    addEntries.add(mlu);

                    /*
                     * Check if this group member has -request/-requests/staff
                     * in its email address. If so, add the group email to
                     * the exclusion list.
                     */
                    String email = dnEntry.getMail();

                    if ((email.indexOf("-request") > 0) || (email.indexOf("-requests") > 0) || (email.startsWith("staff@") == true) || (email.startsWith("staff-") == true)) {
                        myRequestEmails.put(groupUser.getEmail(), true);
                    }

                    if (myBAEmailAddresses.get(email) != null) {
                        myRequestEmails.put(groupUser.getEmail(), true);
                    }
                }    // End of for loop iterating over DN list of a group
            }        // End of while loop iterating over remaining group entries.
        }            // End of If to check if there are any entries remaining in ADGMTable.

        StringBuffer addSQLBatch = new StringBuffer();
        StringBuffer delSQLBatch = new StringBuffer();

        for (MailListUser mlu : addEntries) {
            addSQLBatch.append("INSERT INTO mail_list_users (mail_list_id, user_id) ").append("VALUES(").append(mlu.getMailListId()).append(",").append(mlu.getUserId()).append(")\n");
        }

        for (MailListUser mlu : delEntries) {
            delSQLBatch.append("DELETE mail_list_users WHERE mail_list_id = ").append(mlu.getMailListId()).append(" AND user_id = ").append(mlu.getUserId()).append("\n");
        }

        for (Integer gid : delGroups) {
            delSQLBatch.append("DELETE mail_list_users WHERE mail_list_id = ").append(gid).append("\n");
        }

        int aUCount = addEntries.size();

        if (aUCount > 0) {
            executeBatch(addSQLBatch.toString());
            LOG.info("No of group-users added: " + aUCount);
        }

        int dUCount = delEntries.size();
        int dGCount = delGroups.size();

        if ((dUCount > 0) || (dGCount > 0)) {
            executeBatch(delSQLBatch.toString());

            if (dUCount > 0) {
                LOG.info("No of group-users deleted: " + dUCount);
            }

            if (dGCount > 0) {
                LOG.info("No of groups completely be deleted: " + dGCount);
            }
        }
    }    // End of compareADDBGroupMemebers.

    /**
     * This method compares the lists from AD and DB.
     */
    private void compareADDBLists() {

        // Compare the User entries in AD and DB.
        compareLists(myADUserTable, myDBUserTable, "Users", INTERNAL_USER);

        // Compare the Mailing List entries in AD and DB.
        compareLists(myADGroupTable, myDBGroupTable, "MailingLists", INTERNAL_MAILINGLIST);

        // Compare the Contact Entries in AD and DB.
        compareLists(myADContactTable, myDBContactTable, "Contacts", INTERNAL_CONTACT);
    }

    /**
     * This method compares the entries from AD and databse.
     *
     * @param adTable     Table of entries obtained from AD.
     * @param dbTable     Table of entries obtained from DB.
     * @param userType    Type of the users under comparison as string.
     * @param userTypeId  Type of the users under comparison as integer.
     */
    public void compareLists(Hashtable<String, ADEntry> adTable, Hashtable<String, User> dbTable, String userType, int userTypeId) {

        // LOG.info( "Comparing " + userType + " ...");
        /*
         * Maintain a List of user objects to be updated - UpdateList.
         *
         * Get the enumeration of the user entries from DB.
         * Compare the following properties of the User from DB with those from
         * AD.
         *      - First Name
         *      - Last Name
         *      - Display Name
         *      - Email
         *
         * 1. If the DB entry is not found in the AD table, then mark this as
         *    inactive and add this to the UpdateList.
         * 2. If the comparison is negative i.e. if there is change in any of
         *    the above attributes, update them and add to the UpdateList.
         *    Remove the entry from AD Table.
         * 3. For each entry in the AD Table, create a new User object and add
         *    to the AddList.
         */
        ArrayList<User>     updateList     = new ArrayList<User>();
        ArrayList<User>     deactivateList = new ArrayList<User>();
        int                 aCount         = 0;
        int                 uCount         = 0;
        int                 dCount         = 0;
        Enumeration<String> users          = dbTable.keys();

        while (users.hasMoreElements()) {
            String userLogin = users.nextElement();

            // Get the corresponding DBEntry.
            User dbEntry = dbTable.get(userLogin);

            // Get the corresponding ADEntry.
            ADEntry adEntry = adTable.get(userLogin);

            // Get the email address.

            /*
             * If the ADEntry is null and the dbEntry is still active, then mark
             * the user object as inactive and add it to the deactivate list.
             */
            if (adEntry == null) {
                LOG.debug("Ad Entry is null.");
                if (dbEntry.getIsActive() == true) {
                    dCount++;
                    dbEntry.setIsActive(false);
                    deactivateList.add(dbEntry);
                }
                continue;
            }

            /*
             * Check if there is a change in any of the attributes that we
             * store in our DB.
             */
            if (AdSyncUtils.isChanged(adEntry, dbEntry, userTypeId) == true) {
                uCount++;
                updateList.add(dbEntry);
            }

            // Remove the entry from the ADTable.
            if (adTable.remove(userLogin) == null) {
                LOG.warn("Entry for " + userLogin + " is not properly removed from the table.");
            }

            /*
             * Since this dbEntry is going to be retained as an active item
             * in our database, check if the email address of this DBEntry has
             * "-request"/"-requests"/"staff". If so, put this in the
             * requestEmails table.
             *
             * Also exclude this if present in the myBAEmailAddresses table.
             */
            String email = dbEntry.getEmail();

            if ((email.indexOf("-request") > 0) || (email.indexOf("-requests") > 0) || (email.startsWith("staff@") == true) || (email.startsWith("staff-") == true)) {
                myRequestEmails.put(email, true);
            }

            if (myBAEmailAddresses.get(email) != null) {
                myRequestEmails.put(email, true);
            }
        }

        ArrayList<ADEntry> newEntries = new ArrayList<ADEntry>(adTable.values());

        aCount = newEntries.size();

        /*
         * We can add entries only under following conditions
         *      1. force is set to true
         *      2. if force is set to false then
         *         a. canAdd is set to true
         *         b. addCount is less than threshold.
         */
        if ((force == true) || ((canAdd == true) && (aCount < MAX_ADDITIONS))) {

            // Sort entries by name.
            newEntries = ADEntry.sort(newEntries);

            if (aCount > 0) {
                myContent.append("<P>Added " + aCount + " " + userType + ".</P>").append("\n<TABLE cellspacing='0' cellpadding='0'>").append("\n\t<TR class='header'>").append(
                    "<TD>User Id</TD>").append("<TD>Login</TD>").append("<TD>Display Name</TD>").append("<TD>Email</TD>").append("<TD>User Type</TD>").append("</TR>");
            }

            /**
             * Add all the entries remained in the ADUserTable as User objects
             * to the AddList.
             */
            for (ADEntry entry : newEntries) {
                User user = new User();

                user.setUserId(0);

                if (userTypeId == INTERNAL_USER) {
                    user.setUserLogin(entry.getName());
                } else {

                    /*
                     * In the case of a mailing list and internal contacts, we
                     * want the user_login to be the email itself.
                     */
                    user.setUserLogin(entry.getMail());
                }

                user.setFirstName(entry.getGivenName());
                user.setLastName(entry.getSurName());
                user.setDisplayName(entry.getDisplayName());
                user.setEmail(entry.getMail());
                user.setIsActive(true);
                user.setUserTypeId(userTypeId);
                user.setWebConfig((new WebConfig()).xmlSerialize());
                user.setExtension(entry.getExtension());
                user.setHomePhone(entry.getHomePhone());
                user.setMobile(entry.getMobile());
                insertUser(user);
                myContent.append("\n\t<TR>").append("\n\t\t<TD>").append(user.getUserId()).append("\n\t\t</TD>").append("\n\t\t<TD>").append(user.getUserLogin()).append("\n\t\t</TD>").append(
                "\n\t\t<TD>").append(user.getDisplayName()).append("\n\t\t</TD>").append("\n\t\t<TD>").append(user.getEmail()).append("\n\t\t</TD>").append("\n\t\t<TD>").append(
                user.getUserTypeId()).append("\n\t\t</TD>").append("\n\t\t</TR>");
                
                if ((user != null) && (user.getUserId() != 0)) {
                    myDBMasterIdTable.put(user.getUserId(), user);
                    myDBMasterKeyTable.put(user.getUserLogin(), user);

                    /*
                     * Check if the email address of this new user entry has
                     * "-request"/"-requests"/"staff". If so, put this in the
                     * requestEmails table.
                     */
                    String email = user.getEmail();

                    if ((email.indexOf("-request") > 0) || (email.indexOf("-requests") > 0) || (email.startsWith("staff@") == true) || (email.startsWith("staff-") == true)) {
                        myRequestEmails.put(email, true);
                    }

                    if (myBAEmailAddresses.get(email) != null) {
                        myRequestEmails.put(email, true);
                    }
                }
            }

            if (aCount > 0) {
                myContent.append("\n</TABLE>");
                LOG.info("Added " + aCount + " " + userType + ".");
            }
        } else {
            if (aCount > 0) {
                LOG.info("Skipped adding " + aCount + " " + userType + ".");

                for (ADEntry entry : newEntries) {
                    LOG.info(entry.toString());
                }
            }
        }

        /*
         * We can update entries only under following conditions
         *      1. force is set to true
         *      2. if force is set to false then
         *         a. canUpdate is set to true
         *         b. uCount is less than threshold.
         */
        if ((force == true) || ((canUpdate == true) && (uCount < MAX_UPDATIONS))) {

            // Update the entries present in the updateList.
            if (uCount > 0) {
                for (User user : updateList) {
                    AdSyncUtils.updateUser(user);
                }

                LOG.info("Updated " + uCount + " " + userType + ".");
            }

            if (dCount > 0) {
                for (User user : deactivateList) {
                    deactivateUser(user);
                }

                LOG.info("Deactivated " + dCount + " " + userType + ".");
            }
        } else {
            if (uCount > 0) {
                LOG.info("Skipped updating " + uCount + " " + userType + ".");

                for (User user : updateList) {
                    LOG.info(user.toString());
                }
            }

            if (dCount > 0) {
                LOG.info("Skipped deactivating " + dCount + " " + userType + ".");
            }

            for (User user : deactivateList) {
                LOG.info(user.toString());
            }
        }
    }

    /**
     * This method deactivates the user object in the database.
     *
     * @param user
     */
    private void deactivateUser(User user) {
        Connection con = null;

        try {

            // Obtain a connection from the pool.
            con = DataSourcePool.getConnection();
            con.setAutoCommit(true);

            // Execute the procedure to get the list of all users.
            CallableStatement cs = con.prepareCall("stp_adsync_deactivateUser ?");

            cs.setInt(1, user.getUserId());
            cs.execute();
        } catch (SQLException sqle) {
        	try {
        		if(con != null)
					con.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            LOG.info("Exception while deactivating a user in the database:" + "\nUserLogin: " + user.getUserLogin() + "\n" + "",(sqle));
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException sqle) {

                // Ignore this exception. Good to log this as INFO.
                LOG.info(sqle.toString());
            }
        }

        return;
    }

    /**
     * This method creates the request controls and creates the context then
     * creates the search controls.
     *
     * @return True  if the operation is successful.
     *         False otherwise.
     */
    private boolean establishContext() {
        myPageSize = 1000;

        try {

            /*
             * AD can return atmost 1000 records just like TBits. So, we
             * use a pager control to page the results.
             */
            myRequestControls = new Control[] { new PagedResultsControl(myPageSize, Control.CRITICAL) };

            // Establish the context.
            myContext = new InitialLdapContext(myEnv, null);
            myContext.setRequestControls(myRequestControls);
        } catch (IOException ioe) {
            LOG.error("Exception while creating request controls:" + "",(ioe));
            return false;
        } catch (NamingException ne) {
            LOG.error("Exception while establishing the context: \n" + "",(ne));

            return false;
        }

        return true;
    }

    /**
     * This method executes the query using the context and retrieves the
     * results.
     *
     * @return True  if the operation is successful.
     *         False otherwise.
     */
    private boolean executeADQuery() {

        // This is resultset equivalent to hold the results of the AD Search.
        NamingEnumeration results = null;
        int               count   = 0;

        LOG.info("Sending query to Active Directory....");

        Hashtable<String, Integer> table = new Hashtable<String, Integer>();

        try {

            /*
             * This array holds the cookie returned by AD if we are paging the
             * search results.
             */
            byte[] cookie = null;

            
            Hashtable<String, String> queriesTable = new Hashtable<String, String>();
            queriesTable.put(Integer.toString(ADEntry.USER_ENTRY), PropertiesHandler.getProperty(TRANSBIT_TBITS_ADUSERSEARCHQUERY));
            queriesTable.put(Integer.toString(ADEntry.GROUP_ENTRY), PropertiesHandler.getProperty(TRANSBIT_TBITS_ADGROUPSEARCHQUERY));
            queriesTable.put(Integer.toString(ADEntry.CONTACT_ENTRY), PropertiesHandler.getProperty(TRANSBIT_TBITS_ADCONTACTSEARCHQUERY));
            // The base of the search is transbittech.com.
            //mySearchBase = "DC=transbit2000,DC=com";
            String mySearchBase  = PropertiesHandler.getProperty(TRANSBIT_TBITS_ADSEARCHBASE);
        
            for(String entryType:queriesTable.keySet())
            {
                String query = queriesTable.get(entryType);
                int entryTypeInt = Integer.parseInt(entryType);
                LOG.debug("AD Search Query: " + query);
                LOG.debug("AD Search Controls: " + mySearchControls);

                do
                {
                    results = myContext.search(mySearchBase, query, mySearchControls);
                    if(results == null)
                        LOG.info("The resultset returned from AD is null.");
                    else 
                    {
                        while (results.hasMoreElements() == true) {
                            SearchResult sr   = (SearchResult) results.nextElement();
                             Attributes   attr = sr.getAttributes();
                             String       type = AdSyncUtils.getAllValues(attr, "objectClass");
                             ADEntry entry = null;
                             switch(entryTypeInt)
                             {
                                 case ADEntry.CONTACT_ENTRY:
                                     String key = AdSyncUtils.getValue(attr, "mail");
                                     entry = AdSyncUtils.toADEntry(attr, ADEntry.CONTACT_ENTRY);
                                     myADContactTable.put(key, entry);
                                     break;
                                 case ADEntry.GROUP_ENTRY:
                                     String myKey = AdSyncUtils.getValue(attr, "mail");
                                     entry = AdSyncUtils.toADEntry(attr, ADEntry.GROUP_ENTRY);
                                     myADGroupTable.put(myKey, entry);
                                     ArrayList<String> dnList = AdSyncUtils.getList(attr, "member");
                                     myADGMTable.put(myKey, dnList);
                                     break;
                                 case ADEntry.USER_ENTRY:
                                      myKey = AdSyncUtils.getValue(attr, "name");
                                      entry = AdSyncUtils.toADEntry(attr, ADEntry.USER_ENTRY);
                                      myADUserTable.put(myKey, entry);
                                      break;                                 
                             }
                        }
                    }
                        
//                System.out.println("AD Search Base: " + mySearchBase);
//                System.out.println("AD Search Filter: " + mySearchFilter);
//                System.out.println("AD Search Controls: " + mySearchControls);
//                results = myContext.search(mySearchBase, mySearchFilter, mySearchControls);
                    
//                if (results == null) {
//                    System.out.println("The resultset returned from AD is null.");
//                    LOG.info("The resultset returned from AD is null.");
//                }
//                else
//                {
//
//
//                    // Iterate through the enumeration and process the results.
//                    while (results.hasMoreElements() == true) {
//                        SearchResult sr   = (SearchResult) results.nextElement();
//                        System.out.println("Search Result: " + sr.toString());
//                        Attributes   attr = sr.getAttributes();
//                        String       type = getAllValues(attr, "objectClass");
//
//                        if (type != null) {
//                            type = type.toLowerCase();
//
//                            String dn = getValue(attr, "distinguishedName");
//
//                            /*
//                             * Ignore the entries that are not meant for showing
//                             * in the Global Address List.
//                             */
//                            if (getValue(attr, "showInAddressBook").trim().equals("") == true) {
//                                myADHiddenEntries.put(dn, true);
//
//                                continue;
//                            }
//
//                            ADEntry entry = null;
//
//                            /*
//                             * Check if the type of the entry is contact.
//                             * If so, put this in the contact table.
//                             * The key for the contact is the mail.
//                             */
//                            if (type.indexOf("contact") > 0) {
//                                String key = getValue(attr, "mail");
//
//                                entry = toADEntry(attr, ADEntry.CONTACT_ENTRY);
//                                myADContactTable.put(key, entry);
//                            }
//
//                            /*
//                             * Check if the type of the entry is group.
//                             * If so, put this in the group table.
//                             * The key for the contact is the mail.
//                             */
//                            else if (type.indexOf("group") > 0) {
//                                String key = getValue(attr, "mail");
//
//                                entry = toADEntry(attr, ADEntry.GROUP_ENTRY);
//                                myADGroupTable.put(key, entry);
//
//                                ArrayList<String> dnList = getList(attr, "member");
//
//                                myADGMTable.put(key, dnList);
//                            }
//
//                            /*
//                             * Check if the type of the entry is user.
//                             * If so, put this in the user table.
//                             * The key for the contact is the name.
//                             */
//                            else if (type.indexOf("user") > 0) {
//                                String key = getValue(attr, "name");
//
//                                entry = toADEntry(attr, ADEntry.USER_ENTRY);
//                                myADUserTable.put(key, entry);
//                            }
//
//                            /*
//                             * This is an unknown type. Report it by logging
//                             * it at severe level.
//                             */
//                            else {
//                                LOG.error("Unknown entry type: " + type);
//                            }
//
//                            if (entry != null) {
//                                myADDNTable.put(dn, entry);
//                            }
//                        }
//
//                        count++;
//                    }
//                }
                    //LOG.debug("Count: " + count);
                    // Get the response controls.
                    Control[] controls = myContext.getResponseControls();
                    
                    if (controls != null) {
                        for (int i = 0; i < controls.length; i++) {
                            if (controls[i] instanceof PagedResultsResponseControl) {
                                PagedResultsResponseControl ctrl = (PagedResultsResponseControl) controls[i];
                                
                            /*
                             * Get the cookie returned by AD. This must be
                             * produced to get the rest of the pages.
                             */
                                cookie = ctrl.getCookie();
                            }
                        }
                    }
                    
                    myRequestControls = null;
                    
                /*
                 * Create the pagecontrol again but this time with the cookie
                 * obtained from AD.
                 */
                    myRequestControls = new Control[] { new PagedResultsControl(myPageSize, cookie, Control.CRITICAL) };
                    myContext.setRequestControls(myRequestControls);
                    
                    //
                    // Iterate until the cookie returned is null which means there
                    // are no more results for the query supplied.
                    //
                } while (cookie != null);
            }
        } catch (IOException ioe) {
            LOG.error("Exception while creating request controls:" + "",(ioe));

            return false;
        } catch (NamingException ne) {
            LOG.error("Exception while executing the LDAP Query: " + "",(ne));

            return false;
        }

        // Print the information related to the retrieved entries.
        LOG.debug("Retrieved " + count + " entries from the AD.");
        LOG.debug( "AD Contacts: " + myADContactTable.values().size());
        LOG.debug( "AD Groups: " + myADGroupTable.values().size());
        LOG.debug( "AD Users: " + myADUserTable.values().size());
         
        return true;
    }

    /**
     * This method executes the specified SQL batch in the underlying TBits
     * database.
     *
     * @param batch  SQL batch to be executed.
     * @return True  if the batch is successfully executed.
     *         False otherwise.
     */
    private boolean executeBatch(String batch) {

        // Do not accept a null or empty batch.
        if ((batch == null) || (batch.trim().equals("") == true)) {
            return true;
        }

        boolean    flag = false;
        Connection con  = null;

        try {
            con = DataSourcePool.getConnection();

            Statement stmt = con.createStatement();

            flag = stmt.execute(batch);
            stmt.close();
        } catch (SQLException sqle) {
            LOG.warn("Exception while executing SQL batch: \n\n\n" + batch + "\n\n" + "",(sqle));
            flag = false;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection:" + "",(sqle));
                }
            }
        }

        return flag;
    }

    /**
     * This method initializes the data that will be used during searching.
     */
    private void initializeADData() {

        // Initialize the tables to be filled by the executeADQuery method.
        myADUserTable     = new Hashtable<String, ADEntry>();
        myADGroupTable    = new Hashtable<String, ADEntry>();
        myADContactTable  = new Hashtable<String, ADEntry>();
        myADDNTable       = new Hashtable<String, ADEntry>();
        myADGMTable       = new Hashtable<String, ArrayList<String>>();
        myADHiddenEntries = new Hashtable<String, Boolean>();

        
        // The following are the attributes we are retrieving from AD.
        myAttributes = new String[] {
            "name", "sn", "givenName", "displayName", "mailNickname", "mail", "objectClass", "showInAddressBook", "sAMAccountName", "msExchHideFromAddressLists", "member", "distinguishedName",
            "physicalDeliveryOfficeName",    // Location
            "telephoneNumber",    // Extension of the user
            "mobile",             // Mobile No.
            "homePhone"           // Residential Phone No.
        };

        // These search controls are used when searching AD.
        mySearchControls = new SearchControls();
        mySearchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        mySearchControls.setReturningAttributes(myAttributes);
    }

    /**
     * This method initializes the variables used for storing the DB related
     * data.
     */
    private void initializeDBData() {

        // Initialize the tables.
        myDBMasterIdTable  = new Hashtable<Integer, User>();
        myDBMasterKeyTable = new Hashtable<String, User>();
        myDBUserTable      = new Hashtable<String, User>();
        myDBGroupTable     = new Hashtable<String, User>();
        myDBContactTable   = new Hashtable<String, User>();
        myMailListTable    = new Hashtable<User, Hashtable<String, User>>();
        myRequestEmails    = new Hashtable<String, Boolean>();
        myBAEmailAddresses = new Hashtable<String, Boolean>();
    }

    /**
     * This method inserts the user into the database.
     *
     * @param user
     */
    public static void insertUser(User user) {
        Connection con = null;
        try {
	        BeforeUserInsertEvent buie = new BeforeUserInsertEvent(user);
	        EventManager.getInstance().fireEvent(buie);

            // Obtain a connection from the pool.
            con = DataSourcePool.getConnection();
            con.setAutoCommit(true);

            // Execute the procedure to get the list of all users.
            CallableStatement cs = con.prepareCall("stp_adsync_insertUser " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?, " + " ?, ?, ?, ?, ?, ?");

            cs.registerOutParameter(1, Types.INTEGER);
            user.setCallableParametersAll(cs);
            cs.execute();

            int userId = cs.getInt(1);

            user.setUserId(userId);
            
        } catch (SQLException sqle) {
//        	try {
//        		if(con != null)
//					con.rollback();
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
        	sqle.printStackTrace();
            LOG.info("Exception while inserting a new user into the database:" + "\nUserLogin: " + user.getUserLogin() + "\n" + "",(sqle));
            
            // I had to throw the RuntimeException because there is no throws clause in the given method.
            // Adding throws clause will generate lots of compilatoin error. This problem is because of 
            // not following proper coding practices.
            throw new IllegalArgumentException("Exception while inserting a new user into the database. Cause :" + sqle.getMessage() );
        } catch (EventFailureException e) {
			e.printStackTrace();
            // I had to throw the RuntimeException because there is no throws clause in the given method.
            // Adding throws clause will generate lots of compilatoin error. This problem is because of 
            // not following proper coding practices.
            throw new IllegalArgumentException("Exception while inserting a new user into the database. Cause :" + e.getMessage() );
		} finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException sqle) {

                // Ignore this exception. Good to log this as INFO(
                LOG.info(sqle.toString());
            }
        }

        return;
    }

    private void mailReport() {
        try {
            ourSubject     = "Sync'ing TBits user database with Active Directory.";
            ourToAddress   = PropertiesHandler.getProperty(KEY_LOGGER_NOTIFY_TO);
            ourFromAddress = PropertiesHandler.getProperty(KEY_LOGGER_NOTIFY_FROM);
        } catch (IllegalArgumentException iae) {
            LOG.info("",(iae));

            return;
        }

        if ((myContent != null) &&!myContent.toString().trim().equals("")) {
            String html = "\n<HTML>" + "\n<HEAD>" + "\n       <TITLE>TBits User DB Sync Report</TITLE>" + "\n       <style>" + "\nbody,table,p,span,div" + "\n{" + "\n    font-family: tahoma;"
                          + "\n    font-size: 13px;" + "\n} " + "\np" + "\n{" + "\n    font-family: trebuchet ms;" + "\n    color: midnightblue;" + "\n} " + "\nTABLE" + "\n{" + "\n    width: 100%;"
                          + "\n    border-right: 1px solid lightgrey;" + "\n    border-bottom: 1px solid lightgrey;" + "\n}" + "\nTABLE TD" + "\n{" + "\n    padding-left: 4px;"
                          + "\n    border: 1px solid lightgrey;" + "\n    border-right: none;" + "\n    border-bottom: none;" + "\n}" + "\nTABLE TR.header TD" + "\n{"
                          + "\n    border: 1px solid darkgray;" + "\n    border-right: none;" + "\n    background: midnightblue;" + "\n    color: lightgrey;" + "\n    font-weight: bold;" + "\n}"
                          + "\n       </style>" + "\n</HEAD>" + "\n<BODY>" + "\n<P>Database: " + PropertiesHandler.getProperty(KEY_DB_NAME) + "@" + PropertiesHandler.getProperty(KEY_DB_SERVER)
                          + "</P>" + myContent.toString() + "\n</BODY></HTML>";

            Mail.sendWithHtml(ourToAddress, ourFromAddress, ourSubject, html);
        }
    }

    /**
     * The main method for testing.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            Hashtable<String, Boolean> argTable = parseCmdArgs(args);

            if ((argTable == null) || (argTable.size() == 0)) {
                printUsage();
                return;
                //System.exit(1);
            }

            boolean force  = false;
            boolean add    = false;
            boolean update = false;
            boolean test   = false;

            if (argTable.get("FORCE") != null) {
                force = true;
            }

            if (argTable.get("ADD") != null) {
                add = true;
            }

            if (argTable.get("UPDATE") != null) {
                update = true;
            }

            if (argTable.get("TEST") != null) {

                /*
                 * Test is noticed only if none of the above is passed on
                 * the command line.
                 */
                if ((force != true) && (add != true) && (update != true)) {
                    test = true;
                }
            }
            LOG.info("force: " + force);
            LOG.info("add: " + add);
            LOG.info("update: " + update);
            LOG.info("test: " + test);
            AD2TBitsSync aD2TBitsSync = new AD2TBitsSync(force, add, update, test);
            if(!aD2TBitsSync.startProcess()) {
                LOG.error("Error during Sync AD.");
            }
            LOG.debug("End of program...");
        } catch (Exception e) {
            LOG.error("Error while executing the AD2TBitsSync.", e);
            e.printStackTrace();
        }
        return;
    }

    /**
     * This method parses the command line arguments to check for the switches
     * that are passed.
     *
     * @param args Array of command line arguments.
     * @return Table of switches present on the command line.
     */
    public static Hashtable<String, Boolean> parseCmdArgs(String[] args) {
        Hashtable<String, Boolean> result = new Hashtable<String, Boolean>();

        // Return if the argument array is empty.
        if ((args == null) || (args.length == 0)) {
            return null;
        }

        /*
         * Iterate through the array. What we exepect as arguments are
         *      /force
         *      -force
         *      -f
         *      /f
         *
         *      /add
         *      -add
         *      -a
         *      /a
         *
         *      /update
         *      -update
         *      -u
         *      /u
         *
         *      /test
         *      -test
         *      -t
         *      /t
         */
        for (int i = 0; i < args.length; i++) {
            String value = args[i].toLowerCase();

            if (value.equals("/force") || value.equals("-force") || value.equals("/f") || value.equals("-f")) {
                result.put("FORCE", true);
            }

            if (value.equals("/add") || value.equals("-add") || value.equals("/a") || value.equals("-a")) {
                result.put("ADD", true);
            } else if (value.equals("/update") || value.equals("-update") || value.equals("/u") || value.equals("-u")) {
                result.put("UPDATE", true);
            } else if (value.equals("/test") || value.equals("-test") || value.equals("/t") || value.equals("-t")) {
                result.put("TEST", true);
            }
        }

        return result;
    }

    /**
     * This method prepares the environment for creating the context.
     */
    private void prepareEnv() {
        myEnv = new Hashtable<String, String>();
        myEnv.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_PROVIDER);
        myEnv.put(Context.SECURITY_AUTHENTICATION, AUTH_TYPE);
        myEnv.put(Context.PROVIDER_URL, myHost);
        myEnv.put(Context.SECURITY_PRINCIPAL, myUserLogin);
        myEnv.put(Context.SECURITY_CREDENTIALS, myPassword);

        return;
    }

    /**
     * This method prints the usage info of this script.
     */
    public static void printUsage() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\nUsage: java AD2TBitsSync [[Options]]").append("\n").append("\nOptions are").append("\n\t[/add, /a, -add, -a ]:       ").append(
            "Add entries if missing in the User DB").append("\n\t[/update, /u, -update, -u ]: ").append("Update the stale entries in the User DB").append("\n\t[/test, /t, -test, -t ]:     ").append(
            "Run the script in test mode without modifying the User DB").append("\n");
        System.err.println(buffer.toString());
    }

    /**
     * This method obtains the domain credentials from the properties. These are
     * used for establishing a connection with the Active Directory server.
     *
     * @return True If the operation is successful. False otherwise.
     */
    private boolean readDomainCredentials() {
        try {
            myUserLogin = PropertiesHandler.getProperty(KEY_DOMAIN_LOGIN);
            myPassword  = PropertiesHandler.getProperty(KEY_DOMAIN_PASSWORD);
            myHost      = PropertiesHandler.getProperty(KEY_AD_HOST);
        } catch (Exception e) {
            LOG.error("Error while obtaining the user credentials to be used during " + "binding the LDAP connection:\n" + "",(e));

            return false;
        }

        return true;
    }

    /**
     * This method populates the list of business area email addresses in the
     * myBAEmailAddresses table.
     *
     * @return True if the operation is successful.
     */
    private boolean retrieveBAEmailAddresses() {
        try {
            File file = Configuration.findPath(PREFIX_FILE);

            if (file == null) {
                throw new FileNotFoundException(PREFIX_FILE + " not found.");
            }

            BufferedReader br   = new BufferedReader(new FileReader(file));
            String         line = "";

            /*
             * Each line in the file is in the following format.
             *
             *  PREFIX,DISPLAY_NAME,SITE,PRIMARY_EMAIL,OTHER_EMAILS_AS_CSV.
             *
             * We are interested in the email addresses of the BA.
             */
            while ((line = br.readLine()) != null) {
                String record = line.trim();

                if (record.startsWith("#") == true) {

                    // Ignore commented out lines in the prefixes file.
                    continue;
                }

                String[] arr = record.split("\\|");

                if ((arr == null) || (arr.length < 3)) {
                    continue;
                }

                String            strEmailList = arr[3];
                ArrayList<String> emailList    = Utilities.toArrayList(strEmailList, ",");

                for (String email : emailList) {
                    myBAEmailAddresses.put(email, true);
                }
            }
        } catch (FileNotFoundException fnfe) {
            LOG.severe("",(fnfe));

            return false;
        } catch (IOException ioe) {
            LOG.severe("",(ioe));

            return false;
        }

        return true;
    }

    /**
     * This method obtains the mailing list entries from the database.
     *
     * @return True  If the operation is successful.
     *         False Otherwise.
     */
    private boolean retrieveDBMailingList() {
        Connection con = null;

        try {

            // Obtain a connection from the pool.
            con = DataSourcePool.getConnection();

            // Execute the procedure to get the list of all users.
            CallableStatement cs = con.prepareCall("stp_mlu_getMailingLists");
            ResultSet         rs = cs.executeQuery();

            if (rs != null) {
                int                     oldId        = 0;
                int                     curId        = 0;
                User                    mailListUser = null;
                Hashtable<String, User> userTable    = new Hashtable<String, User>();

                while (rs.next() == true) {
                    curId = rs.getInt("mail_list_id");

                    // Get the corresponding user object.
                    User tmp = myDBMasterIdTable.get(curId);

                    if (tmp == null) {
                        LOG.warn("No user object for MailingList ID: " + curId);

                        continue;
                    }

                    mailListUser = tmp;

                    int userId = rs.getInt("user_id");

                    tmp = myDBMasterIdTable.get(userId);

                    if (tmp == null) {
                        if (userId != 0) {
                            LOG.warn("No user object for User ID: " + userId);
                        }

                        continue;
                    }

                    if ((oldId != 0) && (oldId != curId)) {

                        // Its time to insert into the table.
                        // Get the Group User object corresponding to the oldId
                        User oldGroupUser = myDBMasterIdTable.get(oldId);

                        myMailListTable.put(oldGroupUser, userTable);
                        userTable = new Hashtable<String, User>();
                    }

                    userTable.put(tmp.getUserLogin(), tmp);
                    oldId = curId;
                }

                //
                // If the while loop is executed atleast once, then the
                // mailListUser is not null. Otherwise it can be null.
                //
                if (mailListUser != null) {
                    myMailListTable.put(mailListUser, userTable);
                }
            }
        } catch (SQLException de) {
            LOG.error(de.toString());

            return false;
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException sqle) {

                // Can ignore this exception. Good to log this as Warning.
                LOG.warn("Exception while closing the connection: " + sqle);
            }
        }

        return true;
    }

    /**
     * This method retrieves the user data from the database.
     *
     * @return True  If the operation is successful.
     *         False Otherwise.
     */
    private boolean retrieveDBUserList() {
        int        count = 0;
        Connection con   = null;

        try {

            // Obtain a connection from the pool.
            con = DataSourcePool.getConnection();

            // Execute the procedure to get the list of all users.
            CallableStatement cs = con.prepareCall("stp_user_getAllUsers");
            ResultSet         rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() == true) {

                    /*
                     * Insert the users into their corresponding tables
                     * based on the user type.
                     */
                    User user   = User.createFromResultSetAll(rs);
                    int  userId = user.getUserId();

                    // Put this in the User Table keyed by User Id.
                    myDBMasterIdTable.put(userId, user);
                    myDBMasterKeyTable.put(user.getUserLogin(), user);

                    int userTypeId = user.getUserTypeId();

                    switch (userTypeId) {
                    case INTERNAL_USER :
                        myDBUserTable.put(user.getUserLogin(), user);
                        count++;

                        break;

                    case INTERNAL_MAILINGLIST :
                        myDBGroupTable.put(user.getUserLogin(), user);
                        count++;

                        break;

                    case INTERNAL_CONTACT :
                        myDBContactTable.put(user.getUserLogin(), user);
                        count++;

                        break;

                    default :

                        // Ignore these. These are probably external contacts.
                        break;
                    }
                }
            }
        } catch (SQLException de) {
            LOG.error("",(de));

            return false;
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException sqle) {

                // Can ignore this exception. Good to log this as Warning.
                LOG.warn("Exception while closing the connection: " + sqle);
            }
        }

        // Print the information related to the retrieved entries.
        LOG.info("Retrieved " + count + " entries from the Database.");

        /*
         * LOG.info( "DB Contacts: " + myDBContactTable.values().size());
         * LOG.info( "DB Groups: " + myDBGroupTable.values().size());
         * LOG.info( "DB Users: " + myDBUserTable.values().size());
         */
        return true;
    }

    /**
     * This method starts the process of Sync'ing users from AD to our DB.
     *
     */
    private boolean startProcess() {

        // 1. Get the user credentials.
        if (readDomainCredentials() == false) {
            LOG.error("Invalid credentials.");
            return false;
            //System.exit(1);
        }

        // 2. Prepare the environment.
        prepareEnv();

        // 3. Initialize the input data to be used while querying the AD.
        initializeADData();

        // 4. Obtain the context object.
        if (establishContext() == false) {
            LOG.error("Unable to obtain context.");
            return false;
            //System.exit(1);
        }

        // 5. Execute the AD Query and retrieve the User and Mailing list
        // entries from AD.
        if (executeADQuery() == false) {
            LOG.error("Unable to execute AD query.");
            return false;
            //System.exit(1);
        }

        // 6. Close the context.
        closeContext();

        // 7. Initialize the DB related input and storage variables.
        initializeDBData();

        // 8. Obtain the User Data from the DB.
        if (retrieveDBUserList() == false) {
            LOG.error("Unable to obtain the user data from DB.");
            return false;
            //System.exit(1);
        }

        // 9. Obtain the MailingList data from the DB.
        if (retrieveDBMailingList() == false) {
            LOG.error("Unable to retrieve mailing list data from db.");
            return false;
            //System.exit(1);
        }

        // 10. Obtain the email addresses of business areas.
        if (retrieveBAEmailAddresses() == false) {
            LOG.error("Unable to obtain the addresses of business areas.");
            return false;
            //System.exit(1);
        }

        // 11. Compare AD and DB lists.
        compareADDBLists();

        // 12. Compare AD and DB Group Members.
        compareADDBGroupMembers();

        // 13. Syncing the Exclusion list.
        syncExclusionList();

        // 14. Mail the report.
        mailReport();
        return true;
    }

    /**
     * This method checks if any emails whose name or whose member's name
     * contains "-request/-requests"/staff is not present in the exclusion list.
     * If not present, it adds them to the list.
     */
    private void syncExclusionList() {
        LOG.info("Checking the exclusion list for request emails...");

        ArrayList<Integer> staleEntries = new ArrayList<Integer>();
        ArrayList<Integer> newEntries   = new ArrayList<Integer>();

        /*
         * Get the email addresses in the exclusion list whose user_type_id is
         * -1 which implies that these are either request emails or one of
         * their member has request email address
         */
        Connection        con           = null;
        ArrayList<String> exclusionList = new ArrayList<String>();

        try {
            con = DataSourcePool.getConnection();

            CallableStatement cs = con.prepareCall("stp_adsync_getExcludedEntries");
            ResultSet         rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    String key  = rs.getString(1);
                    User   user = myDBMasterKeyTable.get(key);

                    if (user != null) {
                        exclusionList.add(user.getEmail());
                    } else {
                        LOG.info("User object not found for key: " + key);
                    }
                }

                rs.close();
                rs = null;
            }

            cs.close();
        } catch (SQLException sqle) {
            LOG.warn("Exception while executing SQL batch: " + "",(sqle));
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection:" + "",(sqle));
                }
            }
        }

        /*
         * Now check if each entry obtained from DB is present in the
         * myRequestEmails list.
         */
        for (String email : exclusionList) {
            if (myRequestEmails.containsKey(email) == true) {
                myRequestEmails.remove(email);
            }
        }

        /*
         * The entries remained in myRequestEmails must to be added to Ex List.
         */
        Enumeration<String> rEmails = myRequestEmails.keys();

        while (rEmails.hasMoreElements()) {
            String email = rEmails.nextElement();
            User   user  = myDBMasterKeyTable.get(email);

            if (user == null) {
                LOG.info("User object not found for key: " + email);

                continue;
            }

            newEntries.add(user.getUserId());
        }

        StringBuffer addStmts = new StringBuffer();

        for (Integer userId : newEntries) {
            addStmts.append("\n").append("INSERT INTO exclusion_list").append("(sys_id, user_id, user_type_id) VALUES(0, ").append(userId.intValue()).append(", -1)");
        }

        executeBatch(addStmts.toString());
    }
    
     
}
