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
 * RelatedRequest.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.TBitsLogger;

//Imports from the current package.
//Other TBits Imports.
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
 * This class is the domain object corresponding to the related_requests table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class RelatedRequest implements Comparable<RelatedRequest>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int PRIMARYSYSPREFIX = 1;
    private static final int PRIMARYREQUESTID = 2;
    private static final int PRIMARYACTIONID  = 3;
    private static final int RELATEDSYSPREFIX = 4;
    private static final int RELATEDREQUESTID = 5;
    private static final int RELATEDACTIONID  = 6;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private int myPrimaryActionId;
    private int myPrimaryRequestId;

    // Attributes of this Domain Object.
    private String myPrimarySysPrefix;
    private int    myRelatedActionId;
    private int    myRelatedRequestId;
    private String myRelatedSysPrefix;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public RelatedRequest() {}

    /**
     * The complete constructor.
     *
     *  @param aPrimarySysPrefix
     *  @param aPrimaryRequestId
     *  @param aPrimaryActionId
     *  @param aRelatedSysPrefix
     *  @param aRelatedRequestId
     *  @param aRelatedActionId
     */
    public RelatedRequest(String aPrimarySysPrefix, int aPrimaryRequestId, int aPrimaryActionId, String aRelatedSysPrefix, int aRelatedRequestId, int aRelatedActionId) {
        myPrimarySysPrefix = aPrimarySysPrefix;
        myPrimaryRequestId = aPrimaryRequestId;
        myPrimaryActionId  = aPrimaryActionId;
        myRelatedSysPrefix = aRelatedSysPrefix;
        myRelatedRequestId = aRelatedRequestId;
        myRelatedActionId  = aRelatedActionId;
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
    public int compareTo(RelatedRequest aObject) {
        switch (ourSortField) {
        case PRIMARYSYSPREFIX : {
            if (ourSortOrder == ASC_ORDER) {
                return myPrimarySysPrefix.compareTo(aObject.myPrimarySysPrefix);
            }

            return aObject.myPrimarySysPrefix.compareTo(myPrimarySysPrefix);
        }

        case PRIMARYREQUESTID : {
            Integer i1 = myPrimaryRequestId;
            Integer i2 = aObject.myPrimaryRequestId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case RELATEDSYSPREFIX : {
            if (ourSortOrder == ASC_ORDER) {
                return myRelatedSysPrefix.compareTo(aObject.myRelatedSysPrefix);
            }

            return aObject.myRelatedSysPrefix.compareTo(myRelatedSysPrefix);
        }

        case RELATEDREQUESTID : {
            Integer i1 = myRelatedRequestId;
            Integer i2 = aObject.myRelatedRequestId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }
        }

        return 0;
    }

    /**
     * This method constructs a RelatedRequest object from the resultset.
     *
     * @param aRS     ResultSet which points to a RelatedRequest record.
     *
     * @return RelatedRequest object
     *
     * @exception SQLException
     */
    public static RelatedRequest createFromResultSet(ResultSet aRS) throws SQLException {
        RelatedRequest rr = new RelatedRequest(aRS.getString("primary_sys_prefix"), aRS.getInt("primary_request_id"), aRS.getInt("primary_action_id"), aRS.getString("related_sys_prefix"),
                                aRS.getInt("related_request_id"), aRS.getInt("related_action_id"));

        return rr;
    }

    /**
     * This method is used to compare two RelatedRequest objects.
     *
     * @param o RelatedRequest object.
     *
     * @return True if equal and False otherwise.
     *
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        RelatedRequest aRelatedRequest = null;

        try {
            aRelatedRequest = (RelatedRequest) o;
        } catch (ClassCastException cce) {
            return false;
        }

        return ((myPrimarySysPrefix.equalsIgnoreCase(aRelatedRequest.myPrimarySysPrefix)) && (myPrimaryRequestId == aRelatedRequest.myPrimaryRequestId)
                && (myPrimaryActionId == aRelatedRequest.myPrimaryActionId) && (myRelatedSysPrefix.equalsIgnoreCase(aRelatedRequest.myRelatedSysPrefix))
                && (myRelatedRequestId == aRelatedRequest.myRelatedRequestId) && (myRelatedActionId == aRelatedRequest.myRelatedActionId));
    }

    /**
     * This method is used to compare two RelatedRequest objects.
     *
     * @param aRelatedRequest RelatedRequest object.
     *
     * @return True if equal and False otherwise.
     *
     */
    public boolean equals(RelatedRequest aRelatedRequest) {
        if (aRelatedRequest == null) {
            return false;
        }

        return ((myPrimarySysPrefix.equalsIgnoreCase(aRelatedRequest.myPrimarySysPrefix)) && (myPrimaryRequestId == aRelatedRequest.myPrimaryRequestId)
                && (myPrimaryActionId == aRelatedRequest.myPrimaryActionId) && (myRelatedSysPrefix.equalsIgnoreCase(aRelatedRequest.myRelatedSysPrefix))
                && (myRelatedRequestId == aRelatedRequest.myRelatedRequestId) && (myRelatedActionId == aRelatedRequest.myRelatedActionId));
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the RelatedRequest objects in sorted order
     */
    public static ArrayList<RelatedRequest> sort(ArrayList<RelatedRequest> source) {
        int              size     = source.size();
        RelatedRequest[] srcArray = new RelatedRequest[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new RelatedRequestComparator());

        ArrayList<RelatedRequest> target = new ArrayList<RelatedRequest>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for PrimaryActionId property.
     *
     * @return Current Value of PrimaryActionId
     *
     */
    public int getPrimaryActionId() {
        return myPrimaryActionId;
    }

    /**
     * Accessor method for PrimaryRequestId property.
     *
     * @return Current Value of PrimaryRequestId
     *
     */
    public int getPrimaryRequestId() {
        return myPrimaryRequestId;
    }

    /**
     * Accessor method for PrimarySysPrefix property.
     *
     * @return Current Value of PrimarySysPrefix
     *
     */
    public String getPrimarySysPrefix() {
        return myPrimarySysPrefix;
    }

    /**
     * Accessor method for RelatedActionId property.
     *
     * @return Current Value of RelatedActionId
     *
     */
    public int getRelatedActionId() {
        return myRelatedActionId;
    }

    /**
     * Accessor method for RelatedRequestId property.
     *
     * @return Current Value of RelatedRequestId
     *
     */
    public int getRelatedRequestId() {
        return myRelatedRequestId;
    }

    /**
     * Accessor method for RelatedSysPrefix property.
     *
     * @return Current Value of RelatedSysPrefix
     *
     */
    public String getRelatedSysPrefix() {
        return myRelatedSysPrefix;
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
        aCS.setString(PRIMARYSYSPREFIX, myPrimarySysPrefix);
        aCS.setInt(PRIMARYREQUESTID, myPrimaryRequestId);
        aCS.setInt(PRIMARYACTIONID, myPrimaryActionId);
        aCS.setString(RELATEDSYSPREFIX, myRelatedSysPrefix);
        aCS.setInt(RELATEDREQUESTID, myRelatedRequestId);
        aCS.setInt(RELATEDACTIONID, myRelatedActionId);
    }

    /**
     * Mutator method for PrimaryActionId property.
     *
     * @param aPrimaryActionId New Value for PrimaryActionId
     *
     */
    public void setPrimaryActionId(int aPrimaryActionId) {
        myPrimaryActionId = aPrimaryActionId;
    }

    /**
     * Mutator method for PrimaryRequestId property.
     *
     * @param aPrimaryRequestId New Value for PrimaryRequestId
     *
     */
    public void setPrimaryRequestId(int aPrimaryRequestId) {
        myPrimaryRequestId = aPrimaryRequestId;
    }

    /**
     * Mutator method for PrimarySysPrefix property.
     *
     * @param aPrimarySysPrefix New Value for PrimarySysPrefix
     *
     */
    public void setPrimarySysPrefix(String aPrimarySysPrefix) {
        myPrimarySysPrefix = aPrimarySysPrefix;
    }

    /**
     * Mutator method for RelatedActionId property.
     *
     * @param aRelatedActionId New Value for RelatedActionId
     *
     */
    public void setRelatedActionId(int aRelatedActionId) {
        myRelatedActionId = aRelatedActionId;
    }

    /**
     * Mutator method for RelatedRequestId property.
     *
     * @param aRelatedRequestId New Value for RelatedRequestId
     *
     */
    public void setRelatedRequestId(int aRelatedRequestId) {
        myRelatedRequestId = aRelatedRequestId;
    }

    /**
     * Mutator method for RelatedSysPrefix property.
     *
     * @param aRelatedSysPrefix New Value for RelatedSysPrefix
     *
     */
    public void setRelatedSysPrefix(String aRelatedSysPrefix) {
        myRelatedSysPrefix = aRelatedSysPrefix;
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
 * related_requests table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class RelatedRequestComparator implements Comparator<RelatedRequest>, Serializable {
    public int compare(RelatedRequest obj1, RelatedRequest obj2) {
        return obj1.compareTo(obj2);
    }
}
