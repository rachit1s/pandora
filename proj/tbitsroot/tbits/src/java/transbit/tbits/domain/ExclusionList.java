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
 * ExclusionList.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

//Imports from the current package.
//Other TBits Imports.
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.User;

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

//Static imports from the mapper.
import static transbit.tbits.api.Mapper.ourExclusionListMap;

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
 * This class is the domain object corresponding to the exclusion_list table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class ExclusionList implements Comparable<ExclusionList>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID   = 1;
    private static final int USERID     = 2;
    private static final int USERTYPEID = 3;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    // Attributes of this Domain Object.
    private int mySystemId;

    // Objects corresponding to the foreign keys in this table.
    private User myUser;
    private int  myUserId;
    private int  myUserTypeId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public ExclusionList() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aUserId
     *  @param aUserTypeId
     */
    public ExclusionList(int aSystemId, int aUserId, int aUserTypeId) {
        mySystemId   = aSystemId;
        myUserId     = aUserId;
        myUserTypeId = aUserTypeId;
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
    public int compareTo(ExclusionList aObject) {
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

        case USERTYPEID : {
            Integer i1 = myUserTypeId;
            Integer i2 = aObject.myUserTypeId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }
        }

        return 0;
    }

    /**
     * This method is used to create the ExclusionList object from ResultSet
     *
     * @param  aResultSet the result object containing the fields
     * corresponding to a row of the ExclusionList table in the database
     * @return the corresponding ExclusionList object
     */
    public static ExclusionList createFromResultSet(ResultSet aResultSet) throws SQLException {
        ExclusionList el = new ExclusionList(aResultSet.getInt("sys_id"), aResultSet.getInt("user_id"), aResultSet.getInt("user_type_id"));

        return el;
    }

    /**
     * This method returns the List of exclusion list objects
     * corresponding to the given SystemId and user_type_id.
     *
     * @param aSystemId   BusinessArea Id.
     * @param aUserTypeId User Type Id
     *
     * @return List of User objects.
     *
     * @exception DatabaseException
     */
    public static ArrayList<User> lookupBySystemIdAndUserTypeId(int aSystemId, int aUserTypeId) throws DatabaseException {
        ArrayList<User> list = new ArrayList<User>();

        // Look in the mapper.
        if (ourExclusionListMap != null) {
            String                   key  = Integer.toString(aSystemId);
            ArrayList<ExclusionList> temp = ourExclusionListMap.get(key);

            if (temp != null) {
                for (ExclusionList el : temp) {
                    if (el.getUserTypeId() == aUserTypeId) {
                        list.add(el.getUserId());
                    }
                }
            }

            temp = ourExclusionListMap.get(Integer.toString(0));

            if (temp != null) {
                for (ExclusionList el : temp) {
                    if (el.getUserTypeId() == aUserTypeId) {
                        list.add(el.getUserId());
                    }
                }
            }
        }

        return list;
    }

    /**
     * This method returns the List of exclusion list objects
     * corresponding to the given SystemId and user_type_id.
     * It also includes global list with sys_id/user_type_id 0
     *
     * @param aSystemId   BusinessArea Id.
     * @param aUserTypeId User Type Id
     *
     * @return List of User objects.
     *
     * @exception DatabaseException
     */
    public static ArrayList<User> lookupBySystemIdAndUserTypeIdAndGlobalList(int aSystemId, int aUserTypeId) throws DatabaseException {
        ArrayList<User> list = new ArrayList<User>();

        // Look in the mapper.
        if (ourExclusionListMap != null) {
            String                   key  = Integer.toString(aSystemId);
            ArrayList<ExclusionList> temp = ourExclusionListMap.get(key);

            if (temp != null) {
                for (ExclusionList el : temp) {
                    if ((el.getUserTypeId() == aUserTypeId) || (el.getUserTypeId() == 0) || (el.getUserTypeId() == -2)) {
                        list.add(el.getUserId());
                    }
                }
            }

            //
            // 1) user_type_id = 0 means globally excluded but explicitly added
            // rather than by ADsync script.
            // 2) user_type_id = -2 means globally excluded mailing lists
            // having BA request mails Ids as members
            // and is added by ADsync script.
            //
            temp = ourExclusionListMap.get(Integer.toString(0));

            if (temp != null) {
                for (ExclusionList el : temp) {
                    if ((el.getUserTypeId() == aUserTypeId) || (el.getUserTypeId() == 0) || (el.getUserTypeId() == -2)) {
                        list.add(el.getUserId());
                    }
                }
            }
        }

        return list;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the ExclusionList objects in sorted order
     */
    public static ArrayList<ExclusionList> sort(ArrayList<ExclusionList> source) {
        int             size     = source.size();
        ExclusionList[] srcArray = new ExclusionList[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new ExclusionListComparator());

        ArrayList<ExclusionList> target = new ArrayList<ExclusionList>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

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
    public User getUserId() throws DatabaseException {
        if (myUser == null) {
            myUser = User.lookupByUserId(myUserId);
        }

        return myUser;
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
        aCS.setInt(SYSTEMID, mySystemId);
        aCS.setInt(USERID, myUserId);
        aCS.setInt(USERTYPEID, myUserTypeId);
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
 * exclusion_list table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class ExclusionListComparator implements Comparator<ExclusionList>, Serializable {
    public int compare(ExclusionList obj1, ExclusionList obj2) {
        return obj1.compareTo(obj2);
    }
}
