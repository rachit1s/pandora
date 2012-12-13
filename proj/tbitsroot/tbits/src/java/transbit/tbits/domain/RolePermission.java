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
 * RolePermission.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DataSourcePool;
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the roles_permissions table
 * in the database.
 *
 * @author  :
 * @version : $Id: $
 *
 */
public class RolePermission implements Comparable<RolePermission>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID    = 1;
    private static final int ROLEID      = 2;
    private static final int PERMISSION  = 4;
    private static final int FIELDID     = 3;
    private static final int DPERMISSION = 5;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private int myDPermission;
    private int myFieldId;
    private int myPermission;
    private int myRoleId;

    // Attributes of this Domain Object.
    private int mySystemId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public RolePermission() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aRoleId
     *  @param aFieldId
     *  @param aPermission
     *  @param aDPermission
     */
    public RolePermission(int aSystemId, int aRoleId, int aFieldId, int aPermission, int aDPermission) {
        mySystemId    = aSystemId;
        myRoleId      = aRoleId;
        myFieldId     = aFieldId;
        myPermission  = aPermission;
        myDPermission = aDPermission;
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
    public int compareTo(RolePermission aObject) {
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

        case FIELDID : {
            Integer i1 = myFieldId;
            Integer i2 = aObject.myFieldId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case PERMISSION : {
            Integer i1 = myPermission;
            Integer i2 = aObject.myPermission;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case DPERMISSION : {
            Integer i1 = myDPermission;
            Integer i2 = aObject.myDPermission;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }
        }

        return 0;
    }

    /**
     * This method constructs a RolePermission object from the resultset.
     *
     * @param aRS     ResultSet which points to a RolePermission record.
     *
     * @return RolePermission object
     *
     * @exception SQLException
     */
    public static RolePermission createFromResultSet(ResultSet aRS) throws SQLException {
        RolePermission rp = new RolePermission(aRS.getInt("sys_id"), aRS.getInt("role_id"), aRS.getInt("field_id"), aRS.getInt("permission"), aRS.getInt("dpermission"));

        return rp;
    }

    /**
     * This method reverts to default role permission settings in the
     * given Business Area.
     *
     * @param aSystemId  Id of the Business Area.
     * @param aRoleName  Name of the role whose settings need to be
     *                   reverted.
     *
     * @throws DatabaseException incase of any database exceptions.
     *
     */
    public static void revertToDefaultSettings(int aSystemId, String aRoleName) throws DatabaseException {
        Connection con = null;

        try {
            con = DataSourcePool.getConnection();
            con.setAutoCommit(false);

            CallableStatement cs = con.prepareCall("stp_role_permission_defaultPermissions ?,?");

            cs.setInt(1, aSystemId);
            cs.setString(2, aRoleName);
            cs.execute();
            cs.close();
            cs = null;
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

            message.append("An exception occurred while reverting to ").append("default role permissions.").append("\nSystem Id : ").append(aSystemId);

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    LOG.warning("Exception while closing the connection: " + e.toString());
                }

                con = null;
            }
        }
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the RolePermission objects in sorted order
     */
    public static ArrayList<RolePermission> sort(ArrayList<RolePermission> source) {
        int              size     = source.size();
        RolePermission[] srcArray = new RolePermission[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray);

        ArrayList<RolePermission> target = new ArrayList<RolePermission>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    /**
     * Method to update the corresponding RolePermission object in the database
     *
     * @param aObject Object to be updated
     *
     * @return Update domain object.
     *
     */
    public static RolePermission update(RolePermission aObject) throws DatabaseException {

        // Update logic here.
        if (aObject == null) {
            return aObject;
        }

        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_roles_permissions_update ?, ?, ?, ?, ?");

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
        	DatabaseException dbe = new DatabaseException("An exception occured while updating RolePermission", 
        			sqle);
            LOG.severe(dbe);
            throw dbe;
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {
                	LOG.warn("Unable to close the connection.", sqle);
                    // Should this be logged.?
                }
            }
        }

        return aObject;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method gives group of users having private permissions
     * based on this action of the request.
     *
     * @param aSystemId  Id of the Business Area.
     * @param aRequestId Id of the Request.
     * @param aActionId  Id of the Action
     *
     * @return ArrayList of authorized user logins
     *
     * @throws DatabaseException incase of any database exceptions.
     *
     */
    public static ArrayList<String> getAuthUsersBySystemIdAndRequestIdAndActionId(int aSystemId, int aRequestId, int aActionId) throws DatabaseException {
        Connection        con       = null;
        ArrayList<String> authUsers = new ArrayList<String>();

        try {
            con = DataSourcePool.getConnection();

            CallableStatement cs = con.prepareCall("stp_roleperm_getauthUsersBySystemIdAndRequestIdAndActionId " + "?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.setInt(3, aActionId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    authUsers.add(rs.getString("user_login"));
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("auth users for").append("\nSystem  Id : ").append(aSystemId).append("\nRequest Id : ").append(aRequestId).append(
                "\nAction Id  : ").append(aActionId);

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    LOG.warning("Exception while closing the connection: " + e.toString());
                }

                con = null;
            }
        }

        return authUsers;
    }

    /**
     * Accessor method for Permission property.
     *
     * @return Current Value of Permission
     *
     */
    public int getDPermission() {
        return myDPermission;
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
     * Accessor method for Permission property.
     *
     * @return Current Value of Permission
     *
     */
    public int getPermission() {
        return myPermission;
    }

    /**
     * This method deduces the list of consolidated permissions the user has
     * in this business area by virtue of his association with the business
     * area and considering USER role by default and his association
     * with this action of the request.
     *
     * @param aSystemId  Id of the Business Area.
     * @param aRequestId Id of the Request.
     * @param aActionId  Id of the Action
     * @param aUserId    Id of the user.
     *
     * @return Table of field names and the consolidated permissions.
     *
     * @throws DatabaseException incase of any database exceptions.
     *
     */
    public static Hashtable<String, Integer> getPermissionsBySystemIdAndRequestIdAndActionIdAndUserId(int aSystemId, int aRequestId, int aActionId, int aUserId) throws DatabaseException {
        Connection                 con       = null;
        Hashtable<String, Integer> permTable = new Hashtable<String, Integer>();

        try {
            con = DataSourcePool.getConnection();

            CallableStatement cs = con.prepareCall("stp_roleperm_getPermissionsBySystemIdAndRequestIdAndActionIdAndUserId " + "?, ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.setInt(3, aActionId);
            cs.setInt(4, aUserId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    permTable.put(rs.getString("name"), rs.getInt("permission"));
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("permissions.").append("\nSystem  Id : ").append(aSystemId).append("\nRequest Id : ").append(aRequestId).append(
                "\nAction Id  : ").append(aActionId).append("\nUser Id    : ").append(aUserId);

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    LOG.warning("Exception while closing the connection: " + e.toString());
                }

                con = null;
            }
        }

        return permTable;
    }

    /**
     * This method gives the list of minimum permissions a group of users
     * hasa considering USER role by default and their association
     * with this action of the request.
     *
     * @param aSystemId  Id of the Business Area.
     * @param aRequestId Id of the Request.
     * @param aActionId  Id of the Action
     * @param aUserIdList    List of Id of the users.
     *
     * @return Table of field names and the consolidated permissions.
     *
     * @throws DatabaseException incase of any database exceptions.
     *
     */
    public static Hashtable<String, Integer> getPermissionsBySystemIdAndRequestIdAndActionIdAndUserIdList(int aSystemId, int aRequestId, int aActionId, String aUserIdList) throws DatabaseException {
    	Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			return getPermissionsBySystemIdAndRequestIdAndActionIdAndUserIdList(conn, aSystemId, aRequestId, aActionId, aUserIdList);
		} catch (SQLException e) {
			throw new DatabaseException("Error while getting all the actions.", e);
		} finally {
			try {
				if ((conn != null) && !conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				LOG.error(new Exception(
						"Unable to close the connection to the database.", e));
			}
		}
    }
    public static Hashtable<String, Integer> getPermissionsBySystemIdAndRequestIdAndActionIdAndUserIdList(Connection con, int aSystemId, int aRequestId, int aActionId, String aUserIdList) throws DatabaseException {
        Hashtable<String, Integer> permTable = new Hashtable<String, Integer>();

        try {
//            con = DataSourcePool.getConnection();

            CallableStatement cs = con.prepareCall("stp_roleperm_getPermissionsBySystemIdAndRequestIdAndActionIdAndUserIdList " + "?, ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.setInt(3, aActionId);
            cs.setString(4, aUserIdList);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    permTable.put(rs.getString("name"), rs.getInt("permission"));
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("permissions.").append("\nSystem  Id : ").append(aSystemId).append("\nRequest Id : ").append(aRequestId).append(
                "\nAction Id  : ").append(aActionId).append("\nUser Id List    : ").append(aUserIdList);

            throw new DatabaseException(message.toString(), sqle);
        } 
//        finally {
//            if (con != null) {
//                try {
//                    con.close();
//                } catch (Exception e) {
//                    LOG.warning("Exception while closing the connection: " + e.toString());
//                }
//
//                con = null;
//            }
//        }

        return permTable;
    }

    /**
     * This method deduces the list of consolidated permissions the user has
     * in this business area by virtue of his association with the business
     * area and considering USER/LOGGER roles by default and his association
     * with the request.
     *
     * @param aSystemId  Id of the Business Area.
     * @param aRequestId Id of the Request.
     * @param aUserId    Id of the user.
     *
     * @return Table of field names and the consolidated permissions.
     *
     * @throws DatabaseException incase of any database exceptions.
     * @throws SQLException 
     *
     */
    public static Hashtable<String, Integer> getPermissionsBySystemIdAndRequestIdAndUserId(Connection conn, int aSystemId, int aRequestId, int aUserId) throws DatabaseException, SQLException {
       
        Hashtable<String, Integer> permTable = new Hashtable<String, Integer>();

    	CallableStatement cs = conn.prepareCall("stp_roleperm_getPermissionsBySystemIdAndRequestIdAndUserId " + "?, ?, ?");

        cs.setInt(1, aSystemId);
        cs.setInt(2, aRequestId);
        cs.setInt(3, aUserId);

        ResultSet rs = cs.executeQuery();

        if (rs != null) {
            while (rs.next() != false) {
                permTable.put(rs.getString("name"), rs.getInt("permission"));
            }

            rs.close();
        }

        cs.close();
        rs = null;
        cs = null;
        return permTable;
    }
    
    /**
     * This method deduces the list of consolidated permissions the user has
     * in this business area by virtue of his association with the business
     * area and considering USER/LOGGER roles by default and his association
     * with the request.
     *
     * @param aSystemId  Id of the Business Area.
     * @param aRequestId Id of the Request.
     * @param aUserId    Id of the user.
     *
     * @return Table of field names and the consolidated permissions.
     *
     * @throws DatabaseException incase of any database exceptions.
     *
     */
    public static Hashtable<String, Integer> getPermissionsBySystemIdAndRequestIdAndUserId(int aSystemId, int aRequestId, int aUserId) throws DatabaseException {
        Connection                 con       = null;
        Hashtable<String, Integer> permTable = null;

        try {
        	con = DataSourcePool.getConnection();
        	//con.setAutoCommit(false);            
            permTable = getPermissionsBySystemIdAndRequestIdAndUserId(con, aSystemId, aRequestId, aUserId);
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("permissions.").append("\nSystem  Id : ").append(aSystemId).append("\nRequest Id : ").append(aRequestId).append(
                "\nUser Id    : ").append(aUserId);

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    LOG.warning("Exception while closing the connection: " + e.toString());
                }

                con = null;
            }
        }

        return permTable;
    }

    /**
     * This method deduces the list of  permissions a given role has
     * in this business area
     *
     * @param aSystemId  Id of the Business Area.
     * @param aRoleId    Id of the role.
     *
     * @return Hashtable of fieldNames and the corresponding RolePermission.
     *
     * @throws DatabaseException incase of any database exceptions.
     *
     */
    public static Hashtable<String, RolePermission> getPermissionsBySystemIdAndRoleId(int aSystemId, int aRoleId) throws DatabaseException {
        Connection                        con       = null;
        Hashtable<String, RolePermission> permTable = new Hashtable<String, RolePermission>();

        try {
            con = DataSourcePool.getConnection();

            CallableStatement cs = con.prepareCall("stp_admin_getRolePermissionsBySysIdAndRoleId ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRoleId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    permTable.put(rs.getString("name"), createFromResultSet(rs));
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("permissions.").append("\nSystem Id : ").append(aSystemId).append("\nRole Id   : ").append(aRoleId);

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    LOG.warning("Exception while closing the connection: " + e.toString());
                }

                con = null;
            }
        }

        return permTable;
    }

    /**
     * This method deduces the list of consolidated permissions the user has
     * in this business area by virtue of his association with the business
     * area and considering USER/LOGGER roles by default.
     *
     * @param aSystemId  Id of the Business Area.
     * @param aUserId    Id of the user.
     *
     * @return Table of field names and the consolidated permissions.
     *
     * @throws DatabaseException incase of any database exceptions.
     *
     */
    public static Hashtable<String, Integer> getPermissionsBySystemIdAndUserId(int aSystemId, int aUserId) throws DatabaseException {
        Connection                 con       = null;
        Hashtable<String, Integer> permTable = new Hashtable<String, Integer>();

        try {
            con = DataSourcePool.getConnection();

            CallableStatement cs = con.prepareCall("stp_roleperm_getPermissionsBySystemIdAndUserId ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aUserId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    permTable.put(rs.getString("name"), rs.getInt("permission"));
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("permissions.").append("\nSystem Id : ").append(aSystemId).append("\nUser Id   : ").append(aUserId);

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    LOG.warning("Exception while closing the connection: " + e.toString());
                }

                con = null;
            }
        }

        return permTable;
    }

    /**
     * This method deduces the list of consolidated permissions the user has
     * in this business area by virtue of his association with the business
     * area and considering USER/LOGGER roles by default.
     *
     * @param aSystemId  Id of the Business Area.
     * @param aUserId    Id of the user.
     *
     * @return Table of field names and the consolidated permissions.
     *
     * @throws DatabaseException incase of any database exceptions.
     *
     */
//    public static Hashtable<String, Integer> getPermissionsBySystemIdAndUserId(int aSystemId, int aUserId, boolean aAdd) throws DatabaseException {
//        if (aAdd == false) {
//            return getPermissionsBySystemIdAndUserId(aSystemId, aUserId);
//        }
//
//        Connection                 con       = null;
//        Hashtable<String, Integer> permTable = new Hashtable<String, Integer>();
//
//        try {
//            String spName = "stp_roleperm_getPermissionsBySystemIdAndUserIdForAddRequest";
//
//            con = DataSourcePool.getConnection();
//
//            CallableStatement cs = con.prepareCall(spName + " ?, ?");
//
//            cs.setInt(1, aSystemId);
//            cs.setInt(2, aUserId);
//
//            ResultSet rs = cs.executeQuery();
//
//            if (rs != null) {
//                while (rs.next() != false) {
//                    permTable.put(rs.getString("name"), rs.getInt("permission"));
//                }
//
//                rs.close();
//            }
//
//            cs.close();
//            rs = null;
//            cs = null;
//        } catch (SQLException sqle) {
//            StringBuilder message = new StringBuilder();
//
//            message.append("An exception occurred while retrieving the ").append("permissions.").append("\nSystem Id : ").append(aSystemId).append("\nUser Id   : ").append(aUserId);
//
//            throw new DatabaseException(message.toString(), sqle);
//        } finally {
//            if (con != null) {
//                try {
//                    con.close();
//                } catch (Exception e) {
//                    LOG.warning("Exception while closing the connection: " + e.toString());
//                }
//
//                con = null;
//            }
//        }
//
//        return permTable;
//    }

    /**
     * This method returns the permissions the given user has in the specified
     * business areas.
     *
     * @param aUserId      User Id.
     * @param aPrefixList  Comma separated List of Business Area Prefixes ( each prefix enclosed in '') .
     * 						eg : "'sys_prefix1','sys_prefix2','sys_prefix3'"
     *
     * @return Permission tables of each business area.
     *
     * @exception DatabaseException Incase of database related errors.
     */
    public static Hashtable<String, Hashtable<String, Integer>> getPermissionsByUserId(int aUserId, String aPrefixList) throws DatabaseException {
        Connection                                    con    = null;
        Hashtable<String, Hashtable<String, Integer>> result = new Hashtable<String, Hashtable<String, Integer>>();

        try {
            con = DataSourcePool.getConnection();

            CallableStatement cs = con.prepareCall("stp_roleperm_getPermissionsByUserId ?, ?");

            cs.setInt(1, aUserId);
            cs.setString(2, aPrefixList);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    Hashtable<String, Integer> table     = new Hashtable<String, Integer>();
                    String                     sysPrefix = rs.getString("SysPrefix");
                    int                        oldId     = 0;
                    int                        curId     = rs.getInt("SystemId");

                    do {
                        curId = rs.getInt("SystemId");

                        if ((oldId != 0) && (curId != oldId)) {
                            result.put(sysPrefix.toUpperCase(), table);
                            table = new Hashtable<String, Integer>();
                        }

                        sysPrefix = rs.getString("SysPrefix");

                        String fieldName  = rs.getString("FieldName");
                        int    permission = rs.getInt("Permission");

                        table.put(fieldName, permission);
                        oldId = curId;
                    } while (rs.next() != false);

                    result.put(sysPrefix.toUpperCase(), table);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("permissions.").append("\nUser Id   : ").append(aUserId).append("\nPrefix List : ").append(aPrefixList);

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    LOG.warning("Exception while closing the connection: " + e.toString());
                }

                con = null;
            }
        }

        return result;
    }

    /**
     * This method gives private permissions a group of users
     * hasa considering USER role by default and their association
     * with this action of the request.
     *
     * @param aSystemId  Id of the Business Area.
     * @param aRequestId Id of the Request.
     * @param aActionId  Id of the Action
     * @param aUserIdList    List of Id of the users.
     *
     * @return Table of field names and the consolidated permissions.
     *
     * @throws DatabaseException incase of any database exceptions.
     *
     */
    public static Hashtable<Integer, Integer> getPrivatePermissionsBySystemIdAndRequestIdAndActionIdAndUserIdList(int aSystemId, int aRequestId, int aActionId, String aUserIdList)
            throws DatabaseException {
        Connection                  con       = null;
        Hashtable<Integer, Integer> permTable = new Hashtable<Integer, Integer>();

        try {
            con = DataSourcePool.getConnection();

            CallableStatement cs = con.prepareCall("stp_roleperm_getPrivatePermissionsBySystemIdAndRequestIdAndActionIdAndUserIdList " + "?, ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.setInt(3, aActionId);
            cs.setString(4, aUserIdList);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    permTable.put(rs.getInt("userId"), rs.getInt("permission"));
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("permissions.").append("\nSystem  Id : ").append(aSystemId).append("\nRequest Id : ").append(aRequestId).append(
                "\nAction Id  : ").append(aActionId).append("\nUser Id List    : ").append(aUserIdList);

            throw new DatabaseException(message.toString(), sqle);
        } 
        finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    LOG.warning("Exception while closing the connection: " + e.toString());
                }

                con = null;
            }
        }

        return permTable;
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
        aCS.setInt(FIELDID, myFieldId);
        aCS.setInt(PERMISSION, myPermission);
        aCS.setInt(DPERMISSION, myDPermission);
    }

    public String toString()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("[SYSTEMID: ").append(mySystemId);
    	sb.append(",ROLEID: ").append(myRoleId);
    	sb.append(",FIELDID: ").append(myFieldId);
    	sb.append(",PERMISSION: ").append(myPermission);
    	sb.append(",DPERMISSION: ").append(myDPermission);
    	sb.append("]");
    	return sb.toString();
    }
    /**
     * Mutator method for Permission property.
     *
     * @param aDPermission New Value for Permission
     *
     */
    public void setDPermission(int aDPermission) {
        myDPermission = aDPermission;
    }

    /**
     * Mutator method for FieldId property.
     *
     * @param aFieldId New Value for FieldId
     *
     */
    public void setFieldId(int aFieldId) {
        myFieldId = aFieldId;
    }

    /**
     * Mutator method for Permission property.
     *
     * @param aPermission New Value for Permission
     *
     */
    public void setPermission(int aPermission) {
        myPermission = aPermission;
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
}
