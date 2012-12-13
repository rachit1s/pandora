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
 * Role.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.exception.TBitsException;

//Imports from the current package.
//Other TBits Imports.
import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the roles table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class Role implements Comparable<Role>, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID    = 1;
    private static final int ROLEID      = 2;
    private static final int ROLENAME    = 3;
    private static final int DESCRIPTION = 4;
    private static final int FIELDID     = 5;
    private static final int CANBEDELETED =6;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private String myDescription;
    private int    myRoleId;
    private String myRoleName;

    // Attributes of this Domain Object.
    private int mySystemId;
    private int myFieldId;
    private int canBeDeleted;
    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public Role() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     * @param aRoleId
     * @param aRoleName
     * @param aDescription
     * @param aFieldId TODO
     * @param canBeDeleted TODO
     */
    public Role(int aSystemId, int aRoleId, String aRoleName, String aDescription, int aFieldId, int aflag) {
        mySystemId    = aSystemId;
        myRoleId      = aRoleId;
        myRoleName    = aRoleName;
        myDescription = aDescription;
        myFieldId     = aFieldId;
        canBeDeleted = aflag;
    }

    public Role(Role role)
    {
    	setSystemId(role.getSystemId());
    	setRoleId(role.getRoleId());    
    	setRoleName(role.getRoleName());
    	setDescription(role.getDescription());
    	setFieldId(role.getFieldId());
    	setCanBeDeleted(role.getCanBeDeleted());
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
    public int compareTo(Role aObject) {
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

        case ROLENAME : {
            if (ourSortOrder == ASC_ORDER) {
                return myRoleName.compareTo(aObject.myRoleName);
            }

            return aObject.myRoleName.compareTo(myRoleName);
        }

        case DESCRIPTION : {
            if (ourSortOrder == ASC_ORDER) {
                return myDescription.compareTo(aObject.myDescription);
            }

            return aObject.myDescription.compareTo(myDescription);
        }
        }

        return 0;
    }

    /**
     * This method constructs a Role object from the resultset.
     *
     * @param aRS     ResultSet which points to a Role record.
     *
     * @return Role object
     *
     * @exception SQLException
     */
    public static Role createFromResultSet(ResultSet aRS) throws SQLException {
        Role role = new Role(aRS.getInt("sys_id"), aRS.getInt("role_id"), aRS.getString("rolename"), aRS.getString("description"),aRS.getInt("field_id"),aRS.getInt("can_be_deleted"));

        return role;
    }
    
    /**
     * 
     * @param aSystemId
     * @param aRoleName
     * @param aDescription
     * @param fieldId
     * @param canBeDeleted
     * @param con
     * @return the role object if successful, else throws exception.
     * @throws SQLException 
     * @throws DatabaseException 
     * @throws TBitsException 
     */
    public static Role insert(int aSystemId, String aRoleName, String aDescription,int fieldId,int canBeDeleted, Connection con ) throws SQLException, TBitsException, DatabaseException
    {
    	Role role = new Role(aSystemId,-1,aRoleName, aDescription,fieldId,canBeDeleted);
    	return insert(role,con);
    }
    
    public static Role insert(Role role) throws TBitsException{
    	Connection con = null;
    	if (role == null)
    	{
    		LOG.info("The supplied role was null.");
    		throw new TBitsException("The supplied role was null.");
    	}
    	else
    	{
    		try {
    			con = DataSourcePool.getConnection();
    			con.setAutoCommit(false);
    			Role retRole = insert(role, con);
    			con.commit();
    			return retRole ;
			} 
    		catch(TBitsException e)
    		{
    			try 
				{
					con.rollback();
				} 
				catch (SQLException sqle) 
				{
					LOG.info("",(sqle));
				}
				throw e;
    		}
    		catch (Exception e) 
			{
				try 
				{
					con.rollback();
				} 
				catch (SQLException sqle) 
				{
					LOG.error("",(sqle));
				}
				TBitsException te = new TBitsException("An exception occured while inserting the Role",e);	            
				LOG.severe(te);
	            throw te;
	        }
    		finally 
    		{
	           if (con != null) 
	           {
	                try 
	                {
	                    con.close();
	                } catch (SQLException sqle) {
	                	LOG.error("Unable to close the connection.", sqle);
	                }
	            }
	        }
    	}
    }
    
    /**     
     * @param role : the role to be deleted
     * @param con : the connection object to be used.
     * @return : return the inserted role with the roleId
     * @throws TBitsException : the exception that can be shown to the user.
     * @throws SQLException : unexpected sql exception
     * @throws DatabaseException : exception concerning the developers.
     */
    public static Role insert(Role role, Connection con) throws TBitsException, SQLException, DatabaseException
    {
    	if( null == con || con.isClosed() == true )
    	{
    		LOG.info("The connection object supplied was null or closed.");
    		throw new DatabaseException("The connection object supplied was null or closed.", new SQLException());    		
    	}
    	if( null == role )
    	{
    		throw new TBitsException("The role supplied was null.");
    	}
    	int roleId = -1 ;
    	CallableStatement aCS = con.prepareCall("stp_roles_insert ?,?,?,?,?");	
		//2 and 3 represent column indices for role name and role description
		aCS.setInt(SYSTEMID, role.getSystemId());
        aCS.setString(2, role.getRoleName());
        aCS.setString(3, role.getDescription());
        aCS.setInt(4,role.getFieldId());
        aCS.setInt(5,role.getCanBeDeleted());
        ResultSet rs = aCS.executeQuery();
        //1 represents column role id in the result set	        
        if ((rs != null) && rs.next())
        {
        	roleId = rs.getInt(1);
        	LOG.info("Role was inserted properly and the returned roleId was = " + roleId ) ;
        	role.setRoleId(roleId);        	
        }
        else
        {
        	throw new TBitsException("The Role insertion failed for role : " + role.getRoleName() ) ;
        }
        aCS.close();	       
        return role;
    }
    /**
     * Inserts a Role into the database and returns its new role id.
     * 
     * @param aSystemId
     * @param aRoleName
     * @param aDescription
     * @return the role object if insertion succeeded else throws exception.
     * @throws TBitsException : the exception containing the message of failure 
     */
    public static Role insert (int aSystemId, String aRoleName, String aDescription,int fieldId,int canBeDeleted) throws TBitsException{
    	Connection aCon = null;
    	Role role = null;
    	try {
			aCon = DataSourcePool.getConnection();
			aCon.setAutoCommit(false);
			role = insert(aSystemId, aRoleName, aDescription, fieldId, canBeDeleted, aCon);
	        aCon.commit();
	        return role ;
		} catch (TBitsException e) {
			try {
				aCon.rollback() ;
			} catch (SQLException sqle) {
				LOG.info("",(sqle));
			}
			throw e; 
        }
		catch (DatabaseException e) {
			try {
				aCon.rollback() ;
			} catch (SQLException sqle) {
				LOG.info("",(sqle));
			}
			TBitsException te = new TBitsException("A database exception occured while inserting the role.");
			throw te ;
        }
		catch (SQLException e) {
			try {
				aCon.rollback() ;
			} catch (SQLException sqle) {
				LOG.info("",(sqle));
			}		
			LOG.info("",(e));
			TBitsException te = new TBitsException("A database exception occured while inserting the role.");
			throw te ;
        }
		finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {
                	LOG.warn("Unable to close the connection.", sqle);
                    // Should this be logged.?
                }
            }
        }	
    }
    
   
    
    
    /**
     * Used to insert a role object whose role id is already known, mostly used mostly during importing objects 
     * from an external source during creation of a new BA. So, be cautious if you want use this method, instead
     * user insert which generates a new role id for a role being added.
     * 
     * @param aObject object of type Role
     * @return role object after insertion
     * @throws DatabaseException
     */
    
    public static Role insertExistingRole(Role aObject) throws DatabaseException{
    	if (aObject == null) {
            return aObject;
        }

        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_roles_insert_existing_role ?, ?, ?, ?,?,?");

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
        	DatabaseException dbe = new DatabaseException("An exception occured while inserting Role", 
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

    /** 
     * @param ro : the role to be deleted.
     * @param con: the connection object to be used.
     * @return : returns the role object supplied
     * @throws SQLException : unexpected sql exception 
     * @throws DatabaseException : execption concerning the developers
     * @throws TBitsException : exception that can be shown to the users.
     */
    public static Role delete( Role ro, Connection con) throws SQLException, DatabaseException, TBitsException
    {
    	if( null == con || con.isClosed() == true )    
		{				
				LOG.info("The Connection supplied was either null or closed. Not deleting the role");				
				throw new DatabaseException("The Connection supplied was either null or closed. Not deleting the role", new SQLException());
		}	
		
    	if( ro.canBeDeleted == 0 )
    	{
    		LOG.info("This role cannot be deleted as its canBeDeleted Flag is 0");
    		throw new TBitsException("This role is NOT deletable.");
    	}
		CallableStatement cs = con.prepareCall("stp_role_delete ?,?");
		int systemId = ro.getSystemId();
		int roleId = ro.getRoleId();
		cs.setInt(SYSTEMID, systemId);
		cs.setInt(ROLEID, roleId);
		cs.execute();
		
		return ro ;
    }
    
    /**
     * @param roleObj : role to be deleted.
     * @return the deleted role
     * @throws DatabaseException : exception concerning the developers 
     * @throws TBitsException : exception that can be shown to the user.
     */
    public static Role delete(Role roleObj) throws DatabaseException, TBitsException{
    	Connection con = null;
//    	boolean isDeleted = false;
    	if (roleObj == null)
    	{
    		LOG.info("The supplied role was null.");
    		throw new TBitsException("The supplied role was null.");
    	}
    	else
    	{
    		try {
    			con = DataSourcePool.getConnection();
    			con.setAutoCommit(false);
    			Role retRole = delete(roleObj, con);
    			con.commit();
    			return retRole ;
			} 
    		catch(TBitsException e)
    		{
    			try 
				{
					con.rollback();
				} 
				catch (SQLException sqle) 
				{
					LOG.info("",(sqle));
				}
				throw e;
    		}
    		catch (Exception e) 
			{
				try 
				{
					con.rollback();
				} 
				catch (SQLException sqle) 
				{
					LOG.info("",(sqle));
				}
				TBitsException te = new TBitsException("An exception occured while deleting the Role",e);	            
				LOG.severe(te);
	            throw te;
	        }
    		finally 
    		{
	           if (con != null) 
	           {
	                try 
	                {
	                    con.close();
	                } catch (SQLException sqle) {
	                	LOG.warn("Unable to close the connection.", sqle);
	                    // Should this be logged.?
	                }
	            }
	        }
    	}
    }
    
    public static Role update(Role role) throws TBitsException{
    	Connection con = null;
    	if (role == null)
    	{
    		LOG.info("The supplied role was null.");
    		throw new TBitsException("The supplied role was null.");
    	}
    	else
    	{
    		try {
    			con = DataSourcePool.getConnection();
    			con.setAutoCommit(false);
    			Role retRole = update(role, con);
    			con.commit();
    			return retRole ;
			} 
    		catch(TBitsException e)
    		{
    			try 
				{
					con.rollback();
				} 
				catch (SQLException sqle) 
				{
					LOG.info("",(sqle));
				}
				throw e;
    		}
    		catch (Exception e) 
			{
				try 
				{
					con.rollback();
				} 
				catch (SQLException sqle) 
				{
					LOG.error("",(sqle));
				}
				TBitsException te = new TBitsException("An exception occured while inserting the Role",e);	            
				LOG.severe(te);
	            throw te;
	        }
    		finally 
    		{
	           if (con != null) 
	           {
	                try 
	                {
	                    con.close();
	                } catch (SQLException sqle) {
	                	LOG.error("Unable to close the connection.", sqle);
	                }
	            }
	        }
    	}
    }
    
    public static Role update(Role role, Connection conn) throws TBitsException, SQLException, DatabaseException{
    	if (role == null)
    	{
    		LOG.info("The supplied role was null.");
    		throw new TBitsException("The supplied role was null.");
    	}
    	if( null == conn || conn.isClosed() == true )
    	{
    		LOG.info("The connection object supplied was null or closed.");
    		throw new DatabaseException("The connection object supplied was null or closed.", new SQLException());    		
    	}
    	
    	CallableStatement aCS = conn.prepareCall("stp_roles_update ?,?,?,?,?,?");	
		//2 and 3 represent column indices for role name and role description
		aCS.setInt(SYSTEMID, role.getSystemId());
		aCS.setInt(ROLEID, role.getRoleId());
        aCS.setString(ROLENAME, role.getRoleName());
        aCS.setString(DESCRIPTION, role.getDescription());
        aCS.setInt(FIELDID,role.getFieldId());
        aCS.setInt(CANBEDELETED,role.getCanBeDeleted());
        ResultSet rs = aCS.executeQuery();
        //1 represents column role id in the result set	        
        if ((rs != null) && rs.next())
        {
        	LOG.info("Role was updated properly and the roleId was = " + role.getRoleId() ) ;
        }
        else
        {
        	throw new TBitsException("The Role updation failed for role : " + role.getRoleName() ) ;
        }
        aCS.close();	       
        return role;
    }
    
    public static Role lookupBySystemIdAndRoleName(int aSystemId, String aRoleName) throws DatabaseException {
        Role       role       = null;
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_admin_role_lookupBySystemIdAndRoleName ?, ? ");

            cs.setInt(1, aSystemId);
            cs.setString(2, aRoleName);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    role = createFromResultSet(rs);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the Role.").append("\nSystem Id: ").append(aSystemId).append("\nRole Name : ").append(aRoleName).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warning("Exception occurred while closing the connection.");
                }
            }

            connection = null;
        }

        return role;
    }
    
    public static ArrayList<Role> lookupRolesBySystemIdAndUserId(int aSystemId, int aUserId) throws DatabaseException {
        ArrayList<Role> roleList   = new ArrayList<Role>();
        Role         role   = null;
        Connection        connection = null;

        try {
            connection = DataSourcePool.getConnection();

            /*
             * TODO : Make the procedure
             */
            CallableStatement cs = connection.prepareCall("stp_ru_lookupRolesBySystemIdAndUserId ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aUserId);

            boolean flag = cs.execute();

            // cs.getMoreResults();
            ResultSet rs = cs.getResultSet();

            if (rs != null) {
                while (rs.next()) {
                    role = createFromResultSet(rs);
                    roleList.add(role);
                }
            }

            rs.close();
            cs.close();
            rs = null;
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
    
    public static Role lookupBySystemIdAndRoleId(int aSystemId, int aRoleId) throws DatabaseException {
        Role       role       = null;
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            PreparedStatement ps = connection.prepareStatement("select * from roles where sys_id = " 
            		+ aSystemId + " and role_id = " + aRoleId);
            
            ResultSet rs = ps.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    role = createFromResultSet(rs);
                }

                rs.close();
            }

            ps.close();
            rs = null;
            ps = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the Role.").append("\nSystem Id: ").append(aSystemId).append("\nRole Id : ").append(aRoleId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warning("Exception occurred while closing the connection.");
                }
            }

            connection = null;
        }

        return role;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the Role objects in sorted order
     */
    public static ArrayList<Role> sort(ArrayList<Role> source) {
        int    size     = source.size();
        Role[] srcArray = new Role[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new RoleComparator());

        ArrayList<Role> target = new ArrayList<Role>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

    public int getFieldId()
    {
    	return this.myFieldId ;
    }
    
    public int getCanBeDeleted()
    {
    	return this.canBeDeleted ;
    }
    /**
     * Accessor method for Description property.
     *
     * @return Current Value of Description
     *
     */
    public String getDescription() {
        return myDescription;
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
     * Accessor method for RoleName property.
     *
     * @return Current Value of RoleName
     *
     */
    public String getRoleName() {
        return myRoleName;
    }

    /**
     * Method to get the list of all roles for a business area.
     *
     * @param  aSystemId the systemId for which the list of roles are
     *                 seeked.
     * @return the ArrayList containing the list of the BusineesArea objects
     *         associated with the analyst.
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<Role> getRolesBySysId(int aSystemId) throws DatabaseException {
        ArrayList<Role> roleList   = new ArrayList<Role>();
        Connection      connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("{Call stp_admin_getRolesBySysId ? }");

            cs.setInt(1, aSystemId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next()) {
                    Role role = createFromResultSet(rs);

                    roleList.add(role);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("The application encountered an exception while ").append("trying to retrieve the list of all Roles");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.warn("Exception while closing the connection:", sqle);
            }

            connection = null;
        }

        return roleList;
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
     * Method to get the list of all roles for a business area.
     *
     * @param  aSystemId the systemId for which the list of roles are
     *                 seeked.
     * @return the ArrayList containing the list of the BusineesArea objects
     *         associated with the analyst.
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<String> getUserRolesBySysIdAndUserId(int aSystemId, int aUserId) throws DatabaseException {
        ArrayList<String> arrayList  = new ArrayList<String>();
        Connection        connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("{Call stp_admin_getUserRolesBySysIdAndUserId ?,? }");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aUserId);

            ResultSet rs       = cs.executeQuery();
            String    roleName = null;
            int       usrId;
            int       roleId;

            if (rs != null) {
                while (rs.next()) {
                    roleName = rs.getString("rolename");
                    usrId    = rs.getInt("user_id");
                    roleId   = rs.getInt("role_id");

                    if (usrId != -1) {
                        arrayList.add(roleName + "," + "true" + "," + roleId);
                    } else {
                        arrayList.add(roleName + "," + "false" + "," + roleId);
                    }
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("The application encountered an exception while ").append("trying to retrieve the list of admin Roles");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.warn("Exception while closing the connection:", sqle);
            }

            connection = null;
        }

        return arrayList;
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
        aCS.setString(ROLENAME, myRoleName);
        aCS.setString(DESCRIPTION, myDescription);
        aCS.setInt(FIELDID, myFieldId);
        aCS.setInt(CANBEDELETED,canBeDeleted);
    }

    /**
     * Mutator method for Description property.
     *
     * @param aDescription New Value for Description
     *
     */
    public void setDescription(String aDescription) {
        myDescription = aDescription;
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
     * Mutator method for RoleName property.
     *
     * @param aRoleName New Value for RoleName
     *
     */
    public void setRoleName(String aRoleName) {
        myRoleName = aRoleName;
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
    
    public void setFieldId(int aFieldId){
    	myFieldId=aFieldId;
    }
    
    public void setCanBeDeleted(int aFlag){
    	canBeDeleted=aFlag;
    	
    }
    public static void main(String[] args) throws DatabaseException{
    //	Role.insert(6, "testRoleName", "testDescription");
    }
}


/**
 * This class is the comparator for domain object corresponding to the roles
 * table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class RoleComparator implements Comparator<Role>, Serializable {
    public int compare(Role obj1, Role obj2) {
        return obj1.compareTo(obj2);
    }
}

