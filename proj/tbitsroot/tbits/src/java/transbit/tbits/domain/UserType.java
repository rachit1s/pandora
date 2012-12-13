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
 * UserType.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

//Imports from the current package.
//Other TBits Imports.
import transbit.tbits.common.TBitsLogger;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the user_types table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class UserType implements Comparable<UserType>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Static Variables
    public static final int USER                 = 1;
    public static final int TO                   = 5;
    public static final int SUBSCRIBER           = 4;
    public static final int LOGGER               = 2;
    public static final int INTERNAL_USER        = 7;
    public static final int INTERNAL_MAILINGLIST = 8;
    public static final int INTERNAL_HIDDEN_LIST = 11;
    public static final int INTERNAL_CONTACT     = 10;
    public static final int EXTERNAL_USER        = 9;
    public static final int CC                   = 6;
    public static final int ASSIGNEE             = 3;
    public static final int USERTYPE             =12;

    // Enum sort of fields for Attributes.
    private static final int USERTYPEID = 1;
    private static final int NAME       = 2;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private String myName;

    // Attributes of this Domain Object.
    private int myUserTypeId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public UserType() {}

    /**
     * The complete constructor.
     *
     *  @param aUserTypeId
     *  @param aName
     */
    public UserType(int aUserTypeId, String aName) {
        myUserTypeId = aUserTypeId;
        myName       = aName;
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
    public int compareTo(UserType aObject) {
        switch (ourSortField) {
        case USERTYPEID : {
            Integer i1 = myUserTypeId;
            Integer i2 = aObject.myUserTypeId;

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
        }

        return 0;
    }

    /**
     * This method constructs a UserType object from the resultset.
     *
     * @param aRS     ResultSet which points to a UserType record.
     *
     * @return UserType object
     *
     * @exception SQLException
     */
    public static UserType createFromResultSet(ResultSet aRS) throws SQLException {
        UserType ut = new UserType(aRS.getInt("user_type_id"), aRS.getString("name"));

        return ut;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the UserType objects in sorted order
     */
    public static ArrayList<UserType> sort(ArrayList<UserType> source) {
        int        size     = source.size();
        UserType[] srcArray = new UserType[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new UserTypeComparator());

        ArrayList<UserType> target = new ArrayList<UserType>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

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
     * Accessor method for UserTypeId property.
     *
     * @return Current Value of UserTypeId
     *
     */
    public int getUserTypeId() {
        return myUserTypeId;
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
        aCS.setInt(USERTYPEID, myUserTypeId);
        aCS.setString(NAME, myName);
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
     * Mutator method for UserTypeId property.
     *
     * @param aUserTypeId New Value for UserTypeId
     *
     */
    public void setUserTypeId(int aUserTypeId) {
        myUserTypeId = aUserTypeId;
    }
}


/**
 * This class is the comparator for domain object corresponding to the
 * user_types table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class UserTypeComparator implements Comparator<UserType>, Serializable {
    public int compare(UserType obj1, UserType obj2) {
        return ((UserType) obj1).compareTo(obj2);
    }
}
