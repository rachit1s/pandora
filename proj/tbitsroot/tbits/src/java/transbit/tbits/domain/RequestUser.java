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
 * RequestUser.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.HashCodeUtil;
import transbit.tbits.common.DatabaseException;
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
 * This class is the domain object corresponding to the request_users table
 * in the database.
 *
 * @author  :
 * @version : $Id: $
 *
 */
public class RequestUser implements Comparable<RequestUser>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    public static final int SYSTEMID   = 1;
    public static final int REQUESTID  = 2;
    public static final int USERTYPEID = 3;
    public static final int USERID     = 4;
    public static final int ORDERING   = 5;
    public static final int ISPRIMARY  = 6;
    public static final int FIELDID    = 7;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private boolean myIsPrimary;
    private int     myOrdering;
    private int     myRequestId;

    // Attributes of this Domain Object.
    private int mySystemId;

    // Objects corresponding to the foreign keys in this table.
    private User myUser;
    private int  myUserId;
    private int  myUserTypeId;
    private int  myFieldId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
//    public RequestUser() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aRequestId
     *  @param aUserTypeId
     *  @param aUserId
     *  @param aOrdering
     *  @param aIsPrimary
     */
//   public RequestUser(int aSystemId, int aRequestId, int aUserTypeId, int aUserId, int aOrdering, boolean aIsPrimary) {
//       mySystemId   = aSystemId;
//       myRequestId  = aRequestId;
//       myUserTypeId = aUserTypeId;
//       myUserId     = aUserId;
//       myOrdering   = aOrdering;
//       myIsPrimary  = aIsPrimary;
//   }

    public RequestUser(int aSystemId, int aRequestId, int aUserId, int aOrdering, boolean aIsPrimary,int aFieldId) {
        mySystemId   = aSystemId;
        myRequestId  = aRequestId;
        myUserTypeId = RequestUser.getCorrespondingUserType(aFieldId);
        myUserId     = aUserId;
        myOrdering   = aOrdering;
        myIsPrimary  = aIsPrimary;
        myFieldId    = aFieldId;
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
    public int compareTo(RequestUser aObject) {
        switch (ourSortField) {
        case SYSTEMID : {
            Integer i1 = mySystemId;
            Integer i2 = aObject.mySystemId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case REQUESTID : {
            Integer i1 = myRequestId;
            Integer i2 = aObject.myRequestId;

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
        
        case FIELDID : {
            Integer i1 = myFieldId;
            Integer i2 = aObject.myFieldId;

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

        case ORDERING : {
            Integer i1 = myOrdering;
            Integer i2 = aObject.myOrdering;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case ISPRIMARY : {
            Boolean b1 = myIsPrimary;
            Boolean b2 = aObject.myIsPrimary;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }
        }

        return 0;
    }

    /*
     * This method creates an RequestUser object from the resultset's
     * current row.
     *
     * @param aRS Resultset object.
     *
     * @return RequestUser Object.
     *
     * @exception SQLException incase of database errors.
     */
    public static RequestUser createFromResultSet(ResultSet aRS) throws SQLException {
        RequestUser ru = new RequestUser(aRS.getInt("sys_id"), aRS.getInt("request_id"), aRS.getInt("user_id"), aRS.getInt("ordering"), aRS.getBoolean("is_primary"),aRS.getInt("field_id"));
        return ru;
    }

    /**
     * This method decides if two RequestUser Objects are same.
     *
     */
    public boolean equals(Object o) {
        RequestUser reqUser = null;

        if (o == null) {
            return false;
        }

        try {
            reqUser = (RequestUser) o;
        } catch (ClassCastException cce) {
            return false;
        }

        boolean result = (reqUser.mySystemId == this.mySystemId) && (reqUser.myRequestId == this.myRequestId) && (reqUser.myUserId == this.myUserId)
                         && (reqUser.myIsPrimary == this.myIsPrimary && (reqUser.getFieldId() == this.getFieldId()));

        return result;
    }

    /**
     * This method decides if two RequestUser Objects are same.
     *
     */
    public boolean equals(RequestUser reqUser) {
        if (reqUser == null) {
            return false;
        }

        boolean result = (reqUser.mySystemId == this.mySystemId) && (reqUser.myRequestId == this.myRequestId) && (reqUser.myUserId == this.myUserId)
                         && (reqUser.myIsPrimary == this.myIsPrimary && (reqUser.getFieldId() == this.getFieldId() ));

        return result;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the RequestUser objects in sorted order
     */
    public static ArrayList<RequestUser> sort(ArrayList<RequestUser> source) {
        int           size     = source.size();
        RequestUser[] srcArray = new RequestUser[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray);

        ArrayList<RequestUser> target = new ArrayList<RequestUser>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    /**
     * This method returns the string representation of an object of this class
     *
     * @return String representation of this object.
     */
    public String toString() {
        StringBuilder message = new StringBuilder();

        message.append("[ ").append(mySystemId).append(",").append(myRequestId).append(",").append(myUserTypeId).append(",").append(myUserId).append(",").append(myOrdering).append(",").append(
            myIsPrimary).append( "," + this.getFieldId()  ).append(" ]");

        return message.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for IsPrimary property.
     *
     * @return Current Value of IsPrimary
     *
     */
    public boolean getIsPrimary() {
        return myIsPrimary;
    }

    /**
     * Accessor method for Ordering property.
     *
     * @return Current Value of Ordering
     *
     */
    public int getOrdering() {
        return myOrdering;
    }

    /**
     * Accessor method for RequestId property.
     *
     * @return Current Value of RequestId
     *
     */
    public int getRequestId() {
        return myRequestId;
    }

    public int getFieldId() {
		return myFieldId;
	}

	public void setFieldId(int myFieldId) {
		this.myFieldId = myFieldId;
		this.myUserTypeId = getCorrespondingUserType(this.myFieldId);
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
     * Accessor method for UserId property.
     *
     * @return Current Value of UserId
     *
     */
    public User getUser() throws DatabaseException {
        if (myUser == null) {
            myUser = User.lookupAllByUserId(myUserId);
        }

        return myUser;
    }

    public void setUser(User user){
     this.myUser=user;
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
        aCS.setInt(REQUESTID, myRequestId);
        aCS.setInt(USERTYPEID, myUserTypeId);
        aCS.setInt(USERID, myUserId);
        aCS.setInt(ORDERING, myOrdering);
        aCS.setBoolean(ISPRIMARY, myIsPrimary);
        aCS.setInt(FIELDID, myFieldId);
    }

    /**
     * Mutator method for IsPrimary property.
     *
     * @param aIsPrimary New Value for IsPrimary
     *
     */
    public void setIsPrimary(boolean aIsPrimary) {
        myIsPrimary = aIsPrimary;
    }

    /**
     * Mutator method for Ordering property.
     *
     * @param aOrdering New Value for Ordering
     *
     */
    public void setOrdering(int aOrdering) {
        myOrdering = aOrdering;
    }

    /**
     * Mutator method for RequestId property.
     *
     * @param aRequestId New Value for RequestId
     *
     */
    public void setRequestId(int aRequestId) {
        myRequestId = aRequestId;
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
    private void setUserTypeId(int aUserTypeId) {
        myUserTypeId = aUserTypeId;
    }
    
    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hc = HashCodeUtil.SEED ;
		hc = HashCodeUtil.hash(hc, this.getSystemId());
		hc = HashCodeUtil.hash(hc, this.getRequestId());
		hc = HashCodeUtil.hash(hc, this.getUserId());
		hc = HashCodeUtil.hash(hc, this.getIsPrimary());
		hc = HashCodeUtil.hash(hc, this.getFieldId());
		
		return hc ;
	}

	private static int getCorrespondingUserType(int aFieldId) 
	{
		/*field_id		field_name
		 * 7			logger_ids	
		 * 8			assignee_ids
		 * 9			subscriber_ids	
		 * 10			to_ids
		 * 11			cc_ids
		 * 16			user_id
		 */
		switch(aFieldId)
		{
			case 7 :
				return UserType.LOGGER ;
			case 8 :
				return UserType.ASSIGNEE ;
			case 9 : 
				return UserType.SUBSCRIBER ;
			case 10 :
				return UserType.TO ;
			case 11 :
				return UserType.CC ;
			case 16 : 
				return UserType.USER ;
			default :
				return UserType.USERTYPE ;
		}
	}
}

