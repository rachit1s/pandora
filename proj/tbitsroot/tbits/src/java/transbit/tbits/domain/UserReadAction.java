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
 * UserReadAction.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DataSourcePool;

//Imports from the current package.
//Other TBits Imports.
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;

//Static imports
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
 * This class is the domain object corresponding to the user_read_actions table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class UserReadAction implements Comparable<UserReadAction>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID  = 1;
    private static final int REQUESTID = 2;
    private static final int ACTIONID  = 3;
    private static final int USERID    = 4;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private int myActionId;
    private int myRequestId;

    // Attributes of this Domain Object.
    private int mySystemId;
    private int myUserId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public UserReadAction() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aRequestId
     *  @param aActionId
     *  @param aUserId
     */
    public UserReadAction(int aSystemId, int aRequestId, int aActionId, int aUserId) {
        mySystemId  = aSystemId;
        myRequestId = aRequestId;
        myActionId  = aActionId;
        myUserId    = aUserId;
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
    public int compareTo(UserReadAction aObject) {
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

        case ACTIONID : {
            Integer i1 = myActionId;
            Integer i2 = aObject.myActionId;

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
        }

        return 0;
    }

    /**
     * This method constructs a UserReadAction object from the resultset.
     *
     * @param aRS     ResultSet which points to a UserReadAction record.
     *
     * @return UserReadAction object
     *
     * @exception SQLException
     */
    public static UserReadAction createFromResultSet(ResultSet aRS) throws SQLException {
        UserReadAction ura = new UserReadAction(aRS.getInt("sys_id"), aRS.getInt("request_id"), aRS.getInt("action_id"), aRS.getInt("user_id"));

        return ura;
    }

    /**
     * This method looks up the max action read by the user for specified
     * systemId and requestId.
     *
     * @param aSystemId  System Id.
     * @param aRequestId Request Id.
     * @param aUserId    User Id.
     *
     * @return UserReadAction object
     */
    public static UserReadAction lookupBySystemIdAndRequestIdAndUserId(int aSystemId, int aRequestId, int aUserId) throws DatabaseException {
        UserReadAction ura = null;
        Connection     con = null;

        try {
            con = DataSourcePool.getConnection();

            CallableStatement cs = con.prepareCall("stp_ura_lookupBySystemIdAndRequestIdAndUserId  ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.setInt(3, aUserId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    ura = createFromResultSet(rs);
                }

                // Close the result set.
                rs.close();
            }

            cs.close();
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while reading an ").append("action read by the user.").append("\nBusiness Area Id : ").append(aSystemId).append("\nRequest Id       : ").append(
                aRequestId).append("\nUser Id          : ").append(aUserId);

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection.");
                }
            }
        }

        return ura;
    }

    /**
     * This method registers the action read by the user.
     *
     * @param aSystemId  System Id.
     * @param aRequestId
     * @param aActionId
     * @param aUserId
     */
    public static void registerUserReadAction(int aSystemId, int aRequestId,
			int aActionId, int aUserId) throws DatabaseException {
    	
		Connection con = null;
		try {
			con = DataSourcePool.getConnection();
			con.setAutoCommit(false);
			registerUserReadAction(con, aSystemId, aRequestId, aActionId, aUserId);
			con.commit();
		} catch (SQLException sqle) {
			try {
        		if(con != null)
					con.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			StringBuilder message = new StringBuilder();

			message.append("An exception occurred while registering an ")
					.append("action read by the user.").append(
							"\nBusiness Area Id : ").append(aSystemId).append(
							"\nRequest Id       : ").append(aRequestId).append(
							"\nAction Id        : ").append(aActionId).append(
							"\nUser Id          : ").append(aUserId);

			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection.");
				}
			}
		}
	}
    public static void registerUserReadAction(Connection con, int aSystemId, int aRequestId, int aActionId, int aUserId) throws DatabaseException {
        try {

            CallableStatement cs = con.prepareCall("stp_ura_registerUserReadAction ?, ?, ?, ?");
            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.setInt(3, aActionId);
            cs.setInt(4, aUserId);
            cs.execute();
            cs.close();
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while registering an ").append("action read by the user.").append("\nBusiness Area Id : ").append(aSystemId).append("\nRequest Id       : ").append(
                aRequestId).append("\nAction Id        : ").append(aActionId).append("\nUser Id          : ").append(aUserId);

            throw new DatabaseException(message.toString(), sqle);
        }
    }

    /**
     * This method deletes the request record for all users.
     *
     * @param aSystemId  System Id.
     * @param aRequestId
     */
    public static void removeRequestEntry(int aSystemId, int aRequestId) throws DatabaseException {
        Connection con = null;

        try {
            con = DataSourcePool.getConnection();
            con.setAutoCommit(false);

            CallableStatement cs = con.prepareCall("stp_ura_removeRequestEntry ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.execute();
            cs.close();
            con.commit();
        } catch (SQLException sqle) {
        	try {
        		if(con != null)
					con.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while deleting request ").append("entry").append("\nBusiness Area Id : ").append(aSystemId).append("\nRequest Id       : ").append(aRequestId);

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection.");
                }
            }
        }
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the UserReadAction objects in sorted order
     */
    public static ArrayList<UserReadAction> sort(ArrayList<UserReadAction> source) {
        int              size     = source.size();
        UserReadAction[] srcArray = new UserReadAction[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new UserReadActionComparator());

        ArrayList<UserReadAction> target = new ArrayList<UserReadAction>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

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

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for ActionId property.
     *
     * @param aActionId New Value for ActionId
     *
     */
    public void setActionId(int aActionId) {
        myActionId = aActionId;
    }

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
        aCS.setInt(ACTIONID, myActionId);
        aCS.setInt(USERID, myUserId);
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
}


/**
 * This class is the comparator for domain object corresponding to the
 * user_read_actions table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class UserReadActionComparator implements Comparator<UserReadAction>, Serializable {
    public int compare(UserReadAction obj1, UserReadAction obj2) {
        return ((UserReadAction) obj1).compareTo(obj2);
    }
}
