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
 * NotificationRule.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DataSourcePool;

//Other TBits Imports.
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.NotificationConfig;

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;
import static transbit.tbits.api.Mapper.ourNotificationRuleListMap;

//Static imports from the Mapper.
import static transbit.tbits.api.Mapper.ourNotificationRuleMap;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the notification_rules
 * table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class NotificationRule implements Comparable<NotificationRule>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int NOTIFICATIONRULEID = 1;
    private static final int NAME               = 2;
    private static final int DISPLAYNAME        = 3;
    private static final int RULESCONFIG        = 4;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private String             myDisplayName;
    private String             myName;
    private NotificationConfig myNotificationConfigObject;

    // Attributes of this Domain Object.
    private int    myNotificationRuleId;
    private String myRulesConfig;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public NotificationRule() {}

    /**
     * The complete constructor.
     *
     *  @param aNotificationRuleId
     *  @param aName
     *  @param aDisplayName
     *  @param aRulesConfig
     */
    public NotificationRule(int aNotificationRuleId, String aName, String aDisplayName, String aRulesConfig) {
        myNotificationRuleId = aNotificationRuleId;
        myName               = aName;
        myDisplayName        = aDisplayName;
        myRulesConfig        = aRulesConfig;

        try {
            myNotificationConfigObject = NotificationConfig.getNotificationConfig(myRulesConfig);
        } catch (Exception e) {
            LOG.warn(e.toString(), e);
        }
    }

    //~--- methods ------------------------------------------------------------
                                       
    /**
     * Method that compares this object with the one passed W.R.T the
     * ourSortField.
     *
     * @param aObject  Object to be compared.
     *
     * @return 0 - If they are equal.
     *         1 - If this is greater.
     *        -1 - If this is smaller.
     *
     */
    public int compareTo(NotificationRule aObject) {
        switch (ourSortField) {
        case NOTIFICATIONRULEID : {
            Integer i1 = myNotificationRuleId;
            Integer i2 = aObject.myNotificationRuleId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case NAME : {
            if (ourSortOrder == ASC_ORDER) {
                return myName.compareTo(aObject.myName);
            }

            return aObject.myName.compareTo(myName);
        }

        case DISPLAYNAME : {
            if (ourSortOrder == ASC_ORDER) {
                return myDisplayName.compareTo(aObject.myDisplayName);
            }

            return aObject.myDisplayName.compareTo(myDisplayName);
        }

        case RULESCONFIG : {
            if (ourSortOrder == ASC_ORDER) {
                return myRulesConfig.compareTo(aObject.myRulesConfig);
            }

            return aObject.myRulesConfig.compareTo(myRulesConfig);
        }
        }

        return 0;
    }

    /**
     * This method constructs a NotificationRule object from the resultset.
     *
     * @param aRS     ResultSet which points to a NotificationRule record.
     *
     * @return NotificationRule object
     *
     * @exception SQLException
     */
    public static NotificationRule createFromResultSet(ResultSet aRS) throws SQLException {
        NotificationRule nr = new NotificationRule(aRS.getInt("notification_id"), aRS.getString("name"), aRS.getString("display_name"), aRS.getString("rules_config"));

        return nr;
    }

    /**
     * This method returns the NotificationRule object corresponding to
     * the given NotificationRule Id.
     *
     * @param aNotificationRuleId Rule Id.
     *
     * @return  NotificationRule object corresponding to this
     *          NotificationRuleId
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
    public static NotificationRule lookupByNotificationRuleId(int aNotificationRuleId) throws DatabaseException {
        NotificationRule nr = null;

        // Look in the mapper first.
        String key = Integer.toString(aNotificationRuleId);

        if (ourNotificationRuleMap != null) {
            nr = ourNotificationRuleMap.get(key);

            return nr;
        }

        // else try to get the Rule record from the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_nr_lookupByNotificationRuleId ?");

            cs.setInt(1, aNotificationRuleId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    nr = createFromResultSet(rs);
                }

                // Close the result set.
                rs.close();
            }

            // Close the statement.
            cs.close();

            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("NotificationRule Object.").append("\nNotificationRule Id: ").append(aNotificationRuleId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection:", sqle);
                }

                connection = null;
            }
        }

        return nr;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the NotificationRule objects in sorted order
     */
    public static ArrayList<NotificationRule> sort(ArrayList<NotificationRule> source) {
        int                size     = source.size();
        NotificationRule[] srcArray = new NotificationRule[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new NotificationRuleComparator());

        ArrayList<NotificationRule> target = new ArrayList<NotificationRule>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the NotificationRule object corresponding to
     * the given NotificationRule Id.
     *
     *
     * @return  NotificationRule object corresponding to this
     *          NotificationRuleId
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
    public static ArrayList<NotificationRule> getAllNotificationRules() throws DatabaseException {
        ArrayList<NotificationRule> nrList = null;

        // Look in the mapper first.
        String key = "ALLRULES";

        if (ourNotificationRuleListMap != null) {
            nrList = ourNotificationRuleListMap.get(key);

            if (nrList != null) {
                return nrList;
            } else {
                return new ArrayList<NotificationRule>();
            }
        }

        // else try to get the records directly from the database.
        nrList = new ArrayList<NotificationRule>();

        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_nr_getAllNotificationRules");
            ResultSet         rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    NotificationRule nRule = createFromResultSet(rs);

                    nrList.add(nRule);
                }

                // Close the result set.
                rs.close();
            }

            // Close the statement.
            cs.close();

            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("List of all NotificationRule Objects.").append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection:", sqle);
                }

                connection = null;
            }
        }

        return nrList;
    }

    /**
     * Accessor method for DisplayName property.
     *
     * @return Current Value of DisplayName
     *
     */
    public String getDisplayName() {
        return myDisplayName;
    }

    /**
     * Accessor method for Name property.
     *
     * @return Current Value of Name
     *
     */
    public String getName() {
        return myName;
    }

    /**
     *  Accessor method for NotificationConfig Object property.
     *
     *  @return Current Value of NotificationConfig Object
     *
     */
    public NotificationConfig getNotificationConfigObject() {
        return myNotificationConfigObject;
    }

    /**
     * Accessor method for NotificationRuleId property.
     *
     * @return Current Value of NotificationRuleId
     *
     */
    public int getNotificationRuleId() {
        return myNotificationRuleId;
    }

    /**
     * Accessor method for RulesConfig property.
     *
     * @return Current Value of RulesConfig
     *
     */
    public String getRulesConfig() {
        return myRulesConfig;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * This method sets the parameters in the CallableStatement.
     *
     * @param aCS     CallableStatement whose params should be set.
     *
     * @exception SQLException
     */
    public void setCallableParameters(CallableStatement aCS) throws SQLException {
        aCS.setInt(NOTIFICATIONRULEID, myNotificationRuleId);
        aCS.setString(NAME, myName);
        aCS.setString(DISPLAYNAME, myDisplayName);
        aCS.setString(RULESCONFIG, myRulesConfig);
    }

    /**
     * Mutator method for DisplayName property.
     *
     * @param aDisplayName New Value for DisplayName
     *
     */
    public void setDisplayName(String aDisplayName) {
        myDisplayName = aDisplayName;
    }

    /**
     * Mutator method for Name property.
     *
     * @param aName New Value for Name
     *
     */
    public void setName(String aName) {
        myName = aName;
    }

    /**
     * Mutator method for NotificationConfig Object property.
     *
     * @param aNotificationConfigObject New Value of NotificationConfig Object
     *
     */
    public void setNotificationConfigObject(NotificationConfig aNotificationConfigObject) {
        myNotificationConfigObject = aNotificationConfigObject;
    }

    /**
     * Mutator method for NotificationRuleId property.
     *
     * @param aNotificationRuleId New Value for NotificationRuleId
     *
     */
    public void setNotificationRuleId(int aNotificationRuleId) {
        myNotificationRuleId = aNotificationRuleId;
    }

    /**
     * Mutator method for RulesConfig property.
     *
     * @param aRulesConfig New Value for RulesConfig
     *
     */
    public void setRulesConfig(String aRulesConfig) {
        myRulesConfig = aRulesConfig;
    }

    /**
     * Mutator method for SortField property.
     *
     * @param aSortField New Value of SortField
     *
     */
    public static void setSortField(int aSortField) {
        ourSortField = aSortField;
    }

    /**
     * Mutator method for SortOrder property.
     *
     * @param aSortOrder New Value of SortOrder
     *
     */
    public static void setSortOrder(int aSortOrder) {
        ourSortOrder = aSortOrder;
    }

    /**
     * Mutator method for ourSortField and ourSortOrder properties.
     *
     * @param aSortField New Value of SortField
     * @param aSortOrder New Value of SortOrder
     *
     */
    public static void setSortParams(int aSortField, int aSortOrder) {
        ourSortField = aSortField;
        ourSortOrder = aSortOrder;
    }
}


/**
 * This class is the comparator for domain object corresponding to the
 * notification_rules table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class NotificationRuleComparator implements Comparator<NotificationRule>, Serializable {
    public int compare(NotificationRule obj1, NotificationRule obj2) {
        return obj1.compareTo(obj2);
    }
}
