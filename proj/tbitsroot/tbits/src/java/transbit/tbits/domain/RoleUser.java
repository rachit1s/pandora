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
 * RoleUser.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;

//Other TBits Imports.
import transbit.tbits.common.TBitsLogger;

//Static Imports.
import static transbit.tbits.api.Mapper.ourRoleUserMap;

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
 * This class is the domain object corresponding to the roles_users table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class RoleUser implements Comparable<RoleUser>, TBitsConstants, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID = 1;
    private static final int ROLEID   = 2;
    private static final int USERID   = 3;
    private static final int ISACTIVE = 4;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private boolean myIsActive;
    private int     myRoleId;

    // Attributes of this Domain Object.
    private int mySystemId;
    private int myUserId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public RoleUser() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aRoleId
     *  @param aUserId
     *  @param aIsActive
     */
    public RoleUser(int aSystemId, int aRoleId, int aUserId, boolean aIsActive) {
        mySystemId = aSystemId;
        myRoleId   = aRoleId;
        myUserId   = aUserId;
        myIsActive = aIsActive;
    }
    
    public String toString()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("[");
    	sb.append(",SystemId: ").append(mySystemId);
    	sb.append(",RoleId  : ").append(myRoleId  );
    	sb.append(",UserId  : ").append(myUserId  );
    	sb.append(",IsActive: ").append(myIsActive);
    	sb.append("[");
    	return sb.toString();
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
    public int compareTo(RoleUser aObject) {
        switch (ourSortField) {
        case SYSTEMID : {
            Integer i1 = mySystemId;
            Integer i2 = aObject.mySystemId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case ROLEID : {
            Integer i1 = myRoleId;
            Integer i2 = aObject.myRoleId;

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
     * This method constructs a RoleUser object from the resultset.
     *
     * @param aRS     ResultSet which points to a RoleUser record.
     *
     * @return RoleUser object
     *
     * @exception SQLException
     */
    public static RoleUser createFromResultSet(ResultSet aRS) throws SQLException {
        RoleUser ru = new RoleUser(aRS.getInt("sys_id"), aRS.getInt("role_id"), aRS.getInt("user_id"), aRS.getBoolean("is_active"));

        return ru;
    }

    /**
     * Method to delete a RoleUser object from the database.
     *
     * @param aObject Object to be inserted
     *
     */
    public static boolean delete(RoleUser aObject) {

        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();

            CallableStatement cs = aCon.prepareCall("stp_admin_delete_roles_users ?, ?, ?, ?");

            aObject.setCallableParameters(cs);
            cs.execute();
            cs.close();
//            aCon.commit();
            returnValue = true;
        } catch (SQLException sqle) {
            returnValue = false;
            sqle.printStackTrace();
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
     * Method to insert a RoleUser object into database.
     *
     * @param aObject Object to be inserted
     *
     */
    public static boolean insert(RoleUser aObject) {

        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_admin_insert_roles_users ?, ?, ?, ?");

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
            sqle.printStackTrace();
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
     * Method to get the RoleUser List  for a corresponding
     * RoleId and SystemId
     *
     * @param  aSystemId the SysId for which the RoleUser Objects  needed
     * @param  aRoleId   the RoleId for which the RoleUser Objects  needed
     * @return ArrayList of the corresponding RoleUser domain objects
     * @exception DatabaseException In case of any database related error
     */
    public static ArrayList<RoleUser> lookupBySystemIdAndRoleId(int aSystemId, int aRoleId) throws DatabaseException {
        ArrayList<RoleUser> roleUserList = new ArrayList<RoleUser>();

        if (ourRoleUserMap != null) {
            String key = Integer.toString(aSystemId) + "-" + Integer.toString(aRoleId);

            roleUserList = ourRoleUserMap.get(key);

            if (roleUserList != null) {
                return roleUserList;
            }
        }

        RoleUser   roleUser   = null;
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_ru_lookupBySystemIdAndRoleId ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRoleId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next()) {
                    roleUser = createFromResultSet(rs);
                    roleUserList.add(roleUser);
                }

                rs.close();
                cs.close();
                rs = null;
                cs = null;
            }
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the" + "RoleUserList").append("\nSystem Id : ").append(aSystemId).append("\nRole Id  : ").append(aRoleId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.warning("Exception occured while closing the request");
            }
        }

        return roleUserList;
    }

    
     public static boolean isSuperUser(int aUserId)  throws DatabaseException
    {
         boolean isSuperUser = false;
         Connection        connection = null;
         try {
             //System.out.println("Trying to get connection.");
            connection = DataSourcePool.getConnection();
            if(connection == null)
                throw new SQLException("Unable to get the connection. The connection object is null.");
            //System.out.println("Got connection.");
            CallableStatement cs = connection.prepareCall("stp_is_super_user ?");
            cs.setInt(1, aUserId);
            cs.execute();
            ResultSet rs = cs.getResultSet();
            if((rs != null) && (rs.next()))
            {
                ///System.out.println("rs is not null and has next.");
                int isSuperUserInt = rs.getInt("usercount");
                if(isSuperUserInt > 0)
                    isSuperUser = true;
            }
//            else
//                if(rs == null)
//                    System.out.println("RS is null.");
//                else
//                    System.out.println("RS doesnt have a row.");
            if(rs != null)
                rs.close();
            cs.close();
            rs = null;
            cs = null;
            
            
         }
        catch (SQLException sqle) {
            sqle.printStackTrace();
            StringBuilder message = new StringBuilder();
            
            message.append("An exception occurred while checking if the User Id: '" + aUserId + "' is superuser");
            throw new DatabaseException(message.toString(), sqle);
        } finally {
//            System.out.println("Came in the finally.");
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.warning("Exception occured while closing the connection");
                sqle.printStackTrace();
            }
        }
        return isSuperUser;
    }
    /**
     * Method to get the RoleUser List  for a corresponding
     * RoleId and SystemId
     *
     * @param  aSystemId the SysId for which the RoleUser Objects  needed
     * @param  aUserId   the UserId for which the RoleUser Objects needed
     *
     * @return ArrayList of the corresponding RoleUser domain objects
     * @exception DatabaseException In case of any database related error
     */
    public static ArrayList<String> lookupBySystemIdAndUserId(int aSystemId, int aUserId) throws DatabaseException {
        ArrayList<String> roleList   = new ArrayList<String>();
        RoleUser          roleUser   = null;
        Connection        connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_admin_getRolesBySysIdAndUserId ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aUserId);

            boolean flag = cs.execute();

            // cs.getMoreResults();
            ResultSet rs1 = cs.getResultSet();

            if (rs1 != null) {
                while (rs1.next()) {
                    roleUser = createFromResultSet(rs1);

                    int role = roleUser.getRoleId();

                    if (role == ADMIN) {
                        roleList.add("ADMIN");
                    } else if (role == PERMISSION_ADMIN) {
                        roleList.add("PERMISSION_ADMIN");
                    }
                }
            }

            cs.getMoreResults();

            ResultSet rs2 = cs.getResultSet();

            if ((rs2 != null) && (rs2.next() == true)) {
                roleList.add("SUPER_ADMIN");
            }

            rs1.close();
            rs2.close();
            cs.close();
            rs1 = null;
            rs2 = null;
            cs  = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the"
                           + "Roles by SystemId and UserId").append("\nSystem Id : ").append(aSystemId).append("\nRole Id  : ").append(aUserId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.warning("Exception occured while closing the connection");
            }
        }

        return roleList;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the RoleUser objects in sorted order
     */
    public static ArrayList<RoleUser> sort(ArrayList<RoleUser> source) {
        int        size     = source.size();
        RoleUser[] srcArray = new RoleUser[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new RoleUserComparator());

        ArrayList<RoleUser> target = new ArrayList<RoleUser>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    /**
     * Method to update the corresponding RoleUser object in the database.
     *
     * @param aObject Object to be updated
     *
     * @return Update domain object.
     *
     */
    public static RoleUser update(RoleUser aObject) {

        // Update logic here.
        if (aObject == null) {
            return aObject;
        }

        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_roles_users_update ?, ?, ?, ?");

            aObject.setCallableParameters(cs);
            cs.execute();
            cs.close();
            aCon.commit();
        } catch (SQLException sqle) {
        	try {
        		if(aCon != null)
					aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            sqle.printStackTrace();
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

    //~--- get methods --------------------------------------------------------

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
     * Accessor method for RoleId property.
     *
     * @return Current Value of RoleId
     *
     */
    public int getRoleId() {
        return myRoleId;
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
     * This method sets the parameters in the CallableStatement.
     *
     * @param aCS     CallableStatement whose params should be set.
     *
     * @exception SQLException
     */
    public void setCallableParameters(CallableStatement aCS) throws SQLException {
        aCS.setInt(SYSTEMID, mySystemId);
        aCS.setInt(ROLEID, myRoleId);
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
     * Mutator method for RoleId property.
     *
     * @param aRoleId New Value for RoleId
     *
     */
    public void setRoleId(int aRoleId) {
        myRoleId = aRoleId;
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
 * roles_users table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class RoleUserComparator implements Comparator<RoleUser>, Serializable {
    public int compare(RoleUser obj1, RoleUser obj2) {
        return obj1.compareTo(obj2);
    }
}
