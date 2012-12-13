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
 * MailListUser.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;

import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

//Static imports.
import static transbit.tbits.api.Mapper.ourMailListUserMap;
import static transbit.tbits.api.Mapper.ourUserMailListMap;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;

//~--- classes ----------------------------------------------------------------

/**
 * This class is the domain object corresponding to the mail_list_users table
 * in the database.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 *
 */
public class MailListUser implements Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    //~--- fields -------------------------------------------------------------

    // Attributes of this Domain Object.
    private int myMailListId;
    private int myUserId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public MailListUser() {}

    /**
     * The complete constructor.
     *
     *  @param aMailListId
     *  @param aUserId
     */
    public MailListUser(int aMailListId, int aUserId) {
        myMailListId = aMailListId;
        myUserId     = aUserId;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method returns the list of user after expanding it completely.
     *
     * @param aMailListId Id of the mailing list to be expanded.
     * @param aMemberList OUT Param to hold the list of members.
     */
    private static void expand(int aMailListId, ArrayList<User> aMemberList) {
        ArrayList<User> members = ourMailListUserMap.get(aMailListId);

        // Handle the null case.
        if (members == null) {
            return;
        }

        for (User user : members) {
            int userTypeId = user.getUserTypeId();
            int userId     = user.getUserId();

            if (userTypeId == UserType.INTERNAL_MAILINGLIST) {

                /*
                 * We do not add mailing lists to the MemberList. Instead we
                 * expand them and add the users.
                 */
                expand(userId, aMemberList);
            } else {

                /*
                 * Add this entry to the MemberList if it is not added already.
                 */
                if (aMemberList.contains(user) == false) {
                    aMemberList.add(user);
                }
            }
        }

        return;
    }

    /**
     * Main method for testing.
     *
     * @param arg Command line arguments
     */
    public static void main(String arg[]) {
        try {
            long            start = 0,
                            end   = 0;
            if(arg.length != 1)
            {
            	System.out.println("Usage: <application> <user_id>");
            	return;
            }
            ArrayList<User> list  = getMemberUsers(Integer.parseInt(arg[0]));

            start = System.currentTimeMillis();
            System.out.println("Direct Membership:");
            list  = getMailListsByDirectMembership(Integer.parseInt(arg[0]));
            end   = System.currentTimeMillis();
            LOG.info("Time spent: " + (end - start));

            for (User user : list) {
                System.out.println(user.getUserLogin() + "\t\t");
            }

            System.out.println("Recursive Membership:");
            start = System.currentTimeMillis();
            list  = getMailListsByRecursiveMembership(Integer.parseInt(arg[0]));
            end   = System.currentTimeMillis();
            LOG.info("Time spent: " + (end - start));

            for (User user : list) {
                System.out.println(user.getUserLogin() + "\t\t");
            }

            System.out.println("Members:");
           for(User user:getImmediateMembers(Integer.parseInt(arg[0])))
           {
        	   System.out.println(user.getUserLogin());
           }
           
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            
        }
        return;
    }

    /**
     * This method returns the list of user after expanding it completely.
     *
     * @param aMailListId Id of the mailing list to be expanded.
     * @param aMemberList OUT Param to hold the list of members.
     */
    private static void reverseExpand(int aMemberId, ArrayList<User> aList) {
        ArrayList<User> members = ourUserMailListMap.get(aMemberId);

        if (members == null) {
            return;
        }

        for (User user : members) {
        	if(user == null)
        		continue;
            if (aList.contains(user) == false) {
                aList.add(user);

                /*
                 * Get the mailing lists where this user is a member.
                 */
                reverseExpand(user.getUserId(), aList);
            }
        }

        return;
    }

    /**
     * String representation of MailListUser object.
     */
    public String toString() {
        return "[ " + myMailListId + ", " + myUserId + " ]\n";
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the immediate members in the mailing list
     * that corresponds to the given mailListId.
     *
     * @param aMailListId  Id of the mailing list.
     * @return List of immediate members.
     */
    public static ArrayList<User> getImmediateMembers(int aMailListId) {
        ArrayList<User> members = null;

        if (ourMailListUserMap != null) {
            members = ourMailListUserMap.get(aMailListId);

            if (members != null) {
                return members;
            } else {
                return new ArrayList<User>();
            }
        }

        return members;
    }

    /**
     * This method returns the immediate members in the mailing lists whose
     * email starts with the specified value.
     *
     * @param aEmail Email of the mailing list.
     *
     * @return List of Users  that form the mailing list.
     *
     * @exception DatabaseException incase of any database related errors.
     */
    public static ArrayList<User> getImmediateMembersByEmailLike(String aEmail) throws DatabaseException {
        Connection      connection = null;
        ArrayList<User> list       = new ArrayList<User>();

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_mlu_getMembersByEmailLike ?");

            cs.setString(1, aEmail);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    int  userId = rs.getInt("user_id");
                    User user   = User.lookupAllByUserId(userId);

                    list.add(user);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while expanding the mailing ").append("list.").append("\nMail List Email: ").append(aEmail);

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.info("Exception while closing the connection");
            }
        }

        return list;
    }

    /**
     * Accessor method for MailListId property.
     *
     * @return Current Value of MailListId
     *
     */
    public int getMailListId() {
        return myMailListId;
    }

    /**
     * This method returns the mailing lists where user is a direct member.
     *
     * @param userId    Id of the user.
     * @return          List of mailing groups the user is member of.
     */
    public static ArrayList<User> getMailListsByDirectMembership(int userId) {
        ArrayList<User> directMailingLists = new ArrayList<User>();

        if (ourUserMailListMap != null) {
            ArrayList<User> tempList = ourUserMailListMap.get(userId);

            if (tempList != null) {

                /*
                 * Make it easy for GC to collect the already allocated space.
                 */
                directMailingLists = null;
                directMailingLists = tempList;
            }

            return directMailingLists;
        } else {

            /*
             * Seems the mapper is not built. Get this data from the database.
             */
        }

        return directMailingLists;
    }

    /**
     * This method returns the mailing lists the user is a member of at any
     * level.
     *
     * @param userId    Id of the user.
     * @return          List of mailing groups where user is a member at any
     *                  level.
     */
    public static ArrayList<User> getMailListsByRecursiveMembership(int userId) {
        ArrayList<User> directMailingLists = new ArrayList<User>();

        if (ourUserMailListMap != null) {
            reverseExpand(userId, directMailingLists);

            return directMailingLists;
        } else {

            /*
             * Seems the mapper is not built. Get this data from the database.
             */
            LOG.info("Connecting to the database ...");
        }

        return directMailingLists;
    }

    /**
     * This method completely expands all the mailing lists present within this
     * mailing list as members recursively and returns the final list of users.
     *
     * @param aMailListId Id of Mailing list to be recursively expanded.
     *
     * @return List of member users.
     */
    public static ArrayList<User> getMemberUsers(int aMailListId) {
        ArrayList<User> members = null;

        if (ourMailListUserMap != null) {
            members = new ArrayList<User>();
            expand(aMailListId, members);

            return members;
        }

        return members;
    }

    /**
     * This method returns the user ids by expanding the given mailing list
     * completely to contain only non-mailing list entries.
     *
     * @param aEmail Email of the mailing list.
     *
     * @return List of Users  that form the mailing list.
     *
     * @exception DatabaseException incase of any database related errors.
     */
    public static ArrayList<User> getMemberUsersByEmailLike(String aEmail) throws DatabaseException {
        Connection      connection = null;
        ArrayList<User> list       = new ArrayList<User>();

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_user_expandMailingListByEmail ?");

            cs.setString(1, aEmail);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    int  userId = rs.getInt("user_id");
                    User user   = User.lookupAllByUserId(userId);

                    list.add(user);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while expanding the mailing ").append("list.").append("\nMail List Email: ").append(aEmail);

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.info("Exception while closing the connection");
            }
        }

        return list;
    }

    /**
     * Accessor method for UserId property.
     *
     * @return Current Value of UserId
     *
     */
    public int getUserId() {
        return myUserId;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for MailListId property.
     *
     * @param aMailListId New Value for MailListId
     *
     */
    public void setMailListId(int aMailListId) {
        myMailListId = aMailListId;
    }

    /**
     * Mutator method for UserId property.
     *
     * @param aUserId New Value for UserId
     *
     */
    public void setUserId(int aUserId) {
        myUserId = aUserId;
    }
}
