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
 * BAUser.java
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
import transbit.tbits.domain.User;

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;

//Static Imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

//Static TBits Imports
import static transbit.tbits.api.Mapper.ourBAUserListMap;

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
 * This class is the domain object corresponding to the business_area_users table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class BAUser implements Comparable<BAUser>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID = 1;
    private static final int USERID   = 2;
    private static final int ISACTIVE = 3;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private boolean myIsActive;

    // Attributes of this Domain Object.
    private int mySystemId;
    private int myUserId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public BAUser() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aUserId
     */
    public BAUser(int aSystemId, int aUserId, boolean aIsActive) {
        mySystemId = aSystemId;
        myUserId   = aUserId;
        myIsActive = aIsActive;
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
    public int compareTo(BAUser aObject) {
        switch (ourSortField) {
        case SYSTEMID : {
            Integer i1 = mySystemId;
            Integer i2 = aObject.mySystemId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case USERID : {
            Integer i1 = myUserId;
            Integer i2 = aObject.myUserId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case ISACTIVE : {
            Boolean b1 = myIsActive;
            Boolean b2 = aObject.myIsActive;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }
        }

        return 0;
    }

    /**
     * This method constructs a BAUser object from the resultset.
     *
     * @param aRS     ResultSet which points to a BAUser record.
     *
     * @return BAUser object
     *
     * @exception SQLException
     */
    public static BAUser createFromResultSet(ResultSet aRS) throws SQLException {
        BAUser baUser = new BAUser(aRS.getInt("sys_id"), aRS.getInt("user_id"), aRS.getBoolean("is_active"));

        return baUser;
    }

    /**
     * Method to insert a BAUser object into database.
     *
     * @param aObject Object to be inserted
     *
     */
    public static boolean delete(BAUser aObject) {

        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_business_area_users_delete ?, ?, ?");

            aObject.setCallableParameters(cs);
            cs.execute();
            cs.close();
            aCon.commit();
            returnValue = true;
        } catch (SQLException sqle) {
        	try {
        		if(aCon != null)
					aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            LOG.severe("An exception occured while deleting a Business " + "Area user", sqle);
            returnValue = false;
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {

                    // Should this be logged.?
                }
            }
        }

        return returnValue;
    }

    /**
     * Method to insert a BAUser object into database.
     *
     * @param aObject Object to be inserted
     *
     */
    public static boolean insert(BAUser aObject) {

        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_business_area_users_insert ?, ?, ?");

            aObject.setCallableParameters(cs);
            cs.execute();
            cs.close();
            returnValue = true;
            aCon.commit();
        } catch (SQLException sqle) {
        	try {
        		if(aCon != null)
					aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            LOG.severe("An exception occured while inserting a Business " + "Area user", sqle);
            returnValue = false;
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {

                    // Should this be logged.?
                }
            }
        }

        return returnValue;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the BAUser objects in sorted order
     */
    public static ArrayList<BAUser> sort(ArrayList<BAUser> source) {
        int      size     = source.size();
        BAUser[] srcArray = new BAUser[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new BAUserComparator());

        ArrayList<BAUser> target = new ArrayList<BAUser>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method to get all the User objects for a business Area
     *
     * @return the List of User objects for a business Area
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<User> getBusinessAreaUsers(int aSystemId) throws DatabaseException {

        // Return Value.
        ArrayList<User> userList = new ArrayList<User>();
        User            user     = null;

        // Will try to retrieve the object from the Mapper.If its
        // not available We'll call the stored procedure to retrieve the object
        if (ourBAUserListMap != null) {
            String            key   = Integer.toString(aSystemId);
            ArrayList<BAUser> value = ourBAUserListMap.get(key);

            if (value != null) {
                for (BAUser baUser : value) {
                    user = User.lookupByUserId(baUser.getUserId());

                    if (user != null) {
                        userList.add(user);
                    }
                }

                User.setSortParams(2, 0);
                userList = User.sort(userList);
            }

            return userList;
        }

        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_bauser_lookupBySystemId ?");

            cs.setInt(1, aSystemId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next()) {
                    BAUser baUser = BAUser.createFromResultSet(rs);

                    user = User.lookupByUserId(baUser.getUserId());

                    if (user != null) {
                        userList.add(user);
                    }
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the BAUsers.").append("\nSystemId : ").append(aSystemId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOG.warn("Exception while closing the connection.");
                }

                connection = null;
            }
        }

        return userList;
    }

    /**
     * Accessor method for IsActive property.
     *
     * @return Current Value of IsActive
     *
     */
    public boolean getIsActive() {
        return myIsActive;
    }

    /**
     * Accessor method for SystemId property.
     *
     * @return Current Value of SystemId
     *
     */
    public int getSystemId() {
        return mySystemId;
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

    /**
     * This method returns true if the given user is a BA user in the specified
     * BA>
     *
     * @param systemId  BA Id
     * @param userId    User Id
     * @return  True if the user is a BA User.
     */
    public static boolean isBAUser(int systemId, int userId) {
        if (ourBAUserListMap != null) {
            StringBuilder key = new StringBuilder();

            key.append("SystemId: ").append(systemId).append(" - ").append("UserId: ").append(userId);

            if (ourBAUserListMap.containsKey(key.toString()) == true) {
                return true;
            }

            return false;
        }

        return false;
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
        aCS.setInt(SYSTEMID, mySystemId);
        aCS.setInt(USERID, myUserId);
        aCS.setBoolean(ISACTIVE, myIsActive);
    }

    /**
     * Mutator method for IsActive property.
     *
     * @param aIsActive New Value for IsActive
     *
     */
    public void setIsActive(boolean aIsActive) {
        myIsActive = aIsActive;
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

    /**
     * Mutator method for SystemId property.
     *
     * @param aSystemId New Value for SystemId
     *
     */
    public void setSystemId(int aSystemId) {
        mySystemId = aSystemId;
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


/**
 * This class is the comparator for domain object corresponding to the
 * business_area_users table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class BAUserComparator implements Comparator<BAUser>, Serializable {
    public int compare(BAUser obj1, BAUser obj2) {
        return obj1.compareTo(obj2);
    }
}
