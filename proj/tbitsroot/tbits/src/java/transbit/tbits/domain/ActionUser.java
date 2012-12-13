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
 * ActionUser.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

//TBits Imports.
import transbit.tbits.Helper.HashCodeUtil;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

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
 * This class is the domain object corresponding to the action_users table
 * in the database.
 *
 * @author  :
 * @version : $Id: $
 * 
 */
public class ActionUser implements Comparable<ActionUser>, Serializable {
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

	// Enum sort of fields for Attributes.
	private static final int SYSTEMID = 1;
	private static final int REQUESTID = 2;
	private static final int ACTIONID = 3;
	private static final int USERTYPEID = 4;
	private static final int USERID = 5;
	private static final int ORDERING = 6;
	private static final int ISPRIMARY = 7;
	private static final int FIELDID = 8;

	// Static attributes related to sorting.
	private static int ourSortField;
	private static int ourSortOrder;

	// ~--- fields -------------------------------------------------------------

	private int myActionId;
	private boolean myIsPrimary;
	private int myOrdering;
	private int myRequestId;

	// Attributes of this Domain Object.
	private int mySystemId;
	private int myUserId;
	private int myUserTypeId;
	private int myFieldId ;

	// ~--- constructors -------------------------------------------------------

	/**
	 * The default constructor.
	 */
//	public ActionUser() {
//	}

	/**
	 * The complete constructor.
	 * 
	 * @param aSystemId
	 * @param aRequestId
	 * @param aActionId
	 * @param aUserTypeId
	 * @param aUserId
	 * @param aOrdering
	 * @param aIsPrimary
	 */
//	public ActionUser(int aSystemId, int aRequestId, int aActionId,
//			int aUserTypeId, int aUserId, int aOrdering, boolean aIsPrimary) {
//		mySystemId = aSystemId;
//		myRequestId = aRequestId;
//		myActionId = aActionId;
//		myUserTypeId = aUserTypeId;
//		myUserId = aUserId;
//		myOrdering = aOrdering;
//		myIsPrimary = aIsPrimary;
//	}
	
	public ActionUser(int aSystemId, int aRequestId, int aActionId, int aUserId, int aOrdering, boolean aIsPrimary, int aFieldId) {
		mySystemId = aSystemId;
		myRequestId = aRequestId;
		myActionId = aActionId;
		// just using the RequestUser's method. to avoid changes at multiple places.
		myUserTypeId = getCorrespondingUserType(aFieldId);
		myUserId = aUserId;
		myOrdering = aOrdering;
		myIsPrimary = aIsPrimary;
		myFieldId = aFieldId ;
	}

	// ~--- methods ------------------------------------------------------------

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
	
	public boolean equals( Object obj )
	{
		if( null == obj )
			return false ;
		
		if( !(obj instanceof ActionUser ))
			return false ;
		
		ActionUser au = (ActionUser)obj ;
		
		if( this.getSystemId() == au.getSystemId() 
		  && this.getRequestId() == au.getRequestId()
		  && this.getActionId() == au.getActionId()
		  && this.getUserId() == au.getUserId() 
		  && this.getIsPrimary() == au.getIsPrimary() // Nitiraj ? : should is primary be checked ?
		  && this.getFieldId() == au.getFieldId() 
		  )
		{
			return true; 
		}
		
		return false ;		
	}
	
	public int hashCode()
	{
		int hc = HashCodeUtil.SEED ;
		hc = HashCodeUtil.hash(hc, this.getSystemId());
		hc = HashCodeUtil.hash(hc, this.getRequestId());
		hc = HashCodeUtil.hash(hc, this.getActionId());
		hc = HashCodeUtil.hash(hc, this.getUserId());
		hc = HashCodeUtil.hash(hc, this.getIsPrimary());
		hc = HashCodeUtil.hash(hc, this.getFieldId());
		
		return hc;		
	}
	
	/**
	 * Method that compares this object with the one passed W.R.T the
	 * ourSortField.
	 * 
	 * @param aObject
	 *            Object to be compared.
	 * 
	 * @return 0 - If they are equal. 1 - If this is greater. -1 - If this is
	 *         smaller.
	 * 
	 */
	public int compareTo(ActionUser aObject) {
		switch (ourSortField) {
		case SYSTEMID: {
			Integer i1 = mySystemId;
			Integer i2 = aObject.mySystemId;

			if (ourSortOrder == ASC_ORDER) {
				return i1.compareTo(i2);
			}

			return i2.compareTo(i1);
		}

		case REQUESTID: {
			Integer i1 = myRequestId;
			Integer i2 = aObject.myRequestId;

			if (ourSortOrder == ASC_ORDER) {
				return i1.compareTo(i2);
			}

			return i2.compareTo(i1);
		}

		case ACTIONID: {
			Integer i1 = myActionId;
			Integer i2 = aObject.myActionId;

			if (ourSortOrder == ASC_ORDER) {
				return i1.compareTo(i2);
			}

			return i2.compareTo(i1);
		}

		case USERTYPEID: {
			Integer i1 = myUserTypeId;
			Integer i2 = aObject.myUserTypeId;

			if (ourSortOrder == ASC_ORDER) {
				return i1.compareTo(i2);
			}

			return i2.compareTo(i1);
		}

		case USERID: {
			Integer i1 = myUserId;
			Integer i2 = aObject.myUserId;

			if (ourSortOrder == ASC_ORDER) {
				return i1.compareTo(i2);
			}

			return i2.compareTo(i1);
		}

		case ORDERING: {
			Integer i1 = myOrdering;
			Integer i2 = aObject.myOrdering;

			if (ourSortOrder == ASC_ORDER) {
				return i1.compareTo(i2);
			}

			return i2.compareTo(i1);
		}

		case ISPRIMARY: {
			Boolean b1 = myIsPrimary;
			Boolean b2 = aObject.myIsPrimary;

			if (ourSortOrder == ASC_ORDER) {
				return b1.compareTo(b2);
			}

			return b2.compareTo(b1);
		}
		
		case FIELDID: {
			Integer i1 = this.getFieldId();
			Integer i2 = aObject.getFieldId();

			if (ourSortOrder == ASC_ORDER) {
				return i1.compareTo(i2);
			}

			return i2.compareTo(i1);
		}
		}

		return 0;
	}

	/*
	 * This method creates an ActionUser object from the resultset's current
	 * row.
	 * 
	 * @param aRS Resultset object.
	 * 
	 * @return ActionUser Object.
	 * 
	 * @exception SQLException incase of database errors.
	 */
	public static ActionUser createFromResultSet(ResultSet aRS)
			throws SQLException {
		ActionUser au = new ActionUser(aRS.getInt("sys_id"), aRS
				.getInt("request_id"), aRS.getInt("action_id"), aRS.getInt("user_id"), aRS
				.getInt("ordering"), aRS.getBoolean("is_primary"), aRS.getInt("field_id"));

		return au;
	}

	/**
	 * Method to insert a ActionUser object into database.
	 * 
	 * @param aObject
	 *            Object to be inserted
	 * 
	 */
	@Deprecated
	public static boolean insert(ActionUser aObject) {

		// Insert logic here.
		if (aObject == null) {
			return false;
		}

		Connection aCon = null;
		boolean returnValue = false;

		try {
			aCon = DataSourcePool.getConnection();
			aCon.setAutoCommit(false);

			// Nitiraj msg : no such store procedure.
			CallableStatement cs = aCon
					.prepareCall("stp_action_users_insert ?, ?, ?, ?, ?, ?, ?");

			aObject.setCallableParameters(cs);
			cs.execute();
			cs.close();
			returnValue = true;
			aCon.commit();
		} catch (SQLException sqle) {
			try {
				if (aCon != null)
					aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	 * @param source
	 *            the array list of Type objects
	 * @return the ArrayList of the ActionUser objects in sorted order
	 */
	public static ArrayList<ActionUser> sort(ArrayList<ActionUser> source) {
		int size = source.size();
		ActionUser[] srcArray = new ActionUser[size];

		for (int i = 0; i < size; i++) {
			srcArray[i] = source.get(i);
		}

		Arrays.sort(srcArray);

		ArrayList<ActionUser> target = new ArrayList<ActionUser>();

		for (int i = 0; i < size; i++) {
			target.add(srcArray[i]);
		}

		return target;
	}

	/**
	 * Method to update the corresponding ActionUser object in the database.
	 * 
	 * @param aObject
	 *            Object to be updated
	 * 
	 * @return Update domain object.
	 * 
	 */
	@Deprecated
	public static ActionUser update(ActionUser aObject) {

		// Update logic here.
		if (aObject == null) {
			return aObject;
		}

		Connection aCon = null;

		try {
			aCon = DataSourcePool.getConnection();
			aCon.setAutoCommit(false);

			// Nitiraj msg : no such stored procedure
			CallableStatement cs = aCon
					.prepareCall("stp_action_users_update ?, ?, ?, ?, ?, ?, ?");

			aObject.setCallableParameters(cs);
			cs.execute();
			cs.close();
			aCon.commit();
		} catch (SQLException sqle) {
			try {
				if (aCon != null)
					aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} finally {
			if (aCon != null) {
				try {
					aCon.close();
				} catch (SQLException sqle) {

					// Should this be logged.?
				}
			}
		}

		return aObject;
	}

	// ~--- get methods --------------------------------------------------------

	/**
	 * Accessor method for ActionId property.
	 * 
	 * @return Current Value of ActionId
	 * 
	 */
	public int getActionId() {
		return myActionId;
	}

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
	 * Accessor method for FieldId property.
	 * 
	 * @return Current Value of FieldId
	 * 
	 */
	public int getFieldId() {
		return myFieldId;
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
	 * Accessor method for UserTypeId property.
	 * 
	 * @return Current Value of UserTypeId
	 * 
	 */
	public int getUserTypeId() {
		return myUserTypeId;
	}

	// ~--- set methods --------------------------------------------------------

	/**
	 * Mutator method for ActionId property.
	 * 
	 * @param aActionId
	 *            New Value for ActionId
	 * 
	 */
	public void setActionId(int aActionId) {
		myActionId = aActionId;
	}

	/**
	 * This method sets the parameters in the CallableStatement.
	 * 
	 * @param aCS
	 *            CallableStatement whose params should be set.
	 * 
	 * @exception SQLException
	 */
	public void setCallableParameters(CallableStatement aCS)
			throws SQLException {
		aCS.setInt(SYSTEMID, mySystemId);
		aCS.setInt(REQUESTID, myRequestId);
		aCS.setInt(ACTIONID, myActionId);
		aCS.setInt(USERTYPEID, myUserTypeId);
		aCS.setInt(USERID, myUserId);
		aCS.setInt(ORDERING, myOrdering);
		aCS.setBoolean(ISPRIMARY, myIsPrimary);
		aCS.setInt(FIELDID, this.getFieldId());
	}

	/**
	 * Mutator method for IsPrimary property.
	 * 
	 * @param aIsPrimary
	 *            New Value for IsPrimary
	 * 
	 */
	public void setIsPrimary(boolean aIsPrimary) {
		myIsPrimary = aIsPrimary;
	}
	
	/**
	 * Mutator method for FieldId property.
	 * 
	 * @param aIsPrimary
	 *            New Value for FieldId
	 * 
	 */
	public void setIsPrimary(int aFieldId) {
		myFieldId = aFieldId;
	}

	/**
	 * Mutator method for Ordering property.
	 * 
	 * @param aOrdering
	 *            New Value for Ordering
	 * 
	 */
	public void setOrdering(int aOrdering) {
		myOrdering = aOrdering;
	}

	/**
	 * Mutator method for RequestId property.
	 * 
	 * @param aRequestId
	 *            New Value for RequestId
	 * 
	 */
	public void setRequestId(int aRequestId) {
		myRequestId = aRequestId;
	}

	/**
	 * Mutator method for SortField property.
	 * 
	 * @param aSortField
	 *            New Value of SortField
	 * 
	 */
	public static void setSortField(int aSortField) {
		ourSortField = aSortField;
	}

	/**
	 * Mutator method for SortOrder property.
	 * 
	 * @param aSortOrder
	 *            New Value of SortOrder
	 * 
	 */
	public static void setSortOrder(int aSortOrder) {
		ourSortOrder = aSortOrder;
	}

	/**
	 * Mutator method for ourSortField and ourSortOrder properties.
	 * 
	 * @param aSortField
	 *            New Value of SortField
	 * @param aSortOrder
	 *            New Value of SortOrder
	 * 
	 */
	public static void setSortParams(int aSortField, int aSortOrder) {
		ourSortField = aSortField;
		ourSortOrder = aSortOrder;
	}

	/**
	 * Mutator method for SystemId property.
	 * 
	 * @param aSystemId
	 *            New Value for SystemId
	 * 
	 */
	public void setSystemId(int aSystemId) {
		mySystemId = aSystemId;
	}

	/**
	 * Mutator method for UserId property.
	 * 
	 * @param aUserId
	 *            New Value for UserId
	 * 
	 */
	public void setUserId(int aUserId) {
		myUserId = aUserId;
	}

	/**
	 * Mutator method for FieldId property.
	 * 
	 * @param aUserTypeId
	 *            New Value for FieldId
	 * 
	 */
	private void setUserTypeId(int aUserTypeId) {
		myUserTypeId = aUserTypeId;
	}
}
