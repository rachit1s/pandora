/*
 * DisplayGroup.java
 *
 * $Header:
/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd.  All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */
package transbit.tbits.domain;

// Imports from the current package.

// Other TBits Imports.
import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsHelper;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;

// Java Imports.
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import static java.sql.Types.INTEGER;

// Third party imports.

/**
 * This class is the domain object corresponding to the display_group table in
 * the database.
 * 
 * @author : giris
 * @version : $Id: $
 * 
 * Added By -- Syeda 
 * added a is_default column because there was only one default display group for all the BA's present 
 * so when any one tried to delete the default display group the system with not respond properly
 * Now the default display group would be ba specific and  no one will be allowed to delete it .
 * 
 * 
 * 
 */
public class DisplayGroup implements Comparable<DisplayGroup>, Serializable {
	//private static final int DEFAULT_DISPLAY_GROUP_ID = 1;

	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

	// Enum sort of fields for Attributes.
	public static final int SYSID = 1;
	public static final int ID = 2;
	public static final int DISPLAYNAME = 3;
	public static final int DISPLAYORDER = 4;
	public static final int ISACTIVE = 5;
	public static final int ISDEFAULT = 6;

	// Static attributes related to sorting.
	private static int ourSortField = DISPLAYORDER;
	private static int ourSortOrder = TBitsConstants.ASC_ORDER;

	// Attributes of this Domain Object.
	private int myId = -1;
	private String myDisplayName = "";
	private int myDisplayOrder = 0;
	private boolean myIsDefault = false;
	private boolean myIsActive = true;
	private Integer mySystemId = -1;

	public static int displayGroupIndex = 0;
	   /**
     * The default constructor.
     */
    public DisplayGroup()
    {
    }

    /**
     * The complete constructor.
     * 
     *  @param aSystemId
     *  @param aId
     *  @param aDisplayName
     *  @param aDisplayOrder
     *  @param aIsActive
     */
    /* public DisplayGroup(int 	  aSystemId,
    		 			 int      aId,
                         String   aDisplayName,
                         int      aDisplayOrder,
                          boolean aIsActive)
    {
    	mySystemId	   = aSystemId;
        myId           = aId;
        myDisplayName  = aDisplayName;
        myDisplayOrder = aDisplayOrder;
        myIsActive     = aIsActive;
       // mytempsysid=mySystemId;
    }*/
    public DisplayGroup(int 	  aSystemId,
			 int      aId,
            String   aDisplayName,
            int      aDisplayOrder,
             boolean aIsActive,
             boolean aIsDefault)
{
mySystemId	   = aSystemId;
myId           = aId;
myDisplayName  = aDisplayName;
myDisplayOrder = aDisplayOrder;
myIsActive     = aIsActive;
myIsDefault    = aIsDefault;
}

     /**
      * The complete constructor.
      * 
      *  @param aDisplayName
      *  @param aDisplayOrder
      *  @param aIsActive
      */
    /*  public DisplayGroup(int aSystemId, String   aDisplayName,
                          int      aDisplayOrder,
                           boolean aIsActive)
     {
         this(aSystemId, -1, aDisplayName, aDisplayOrder, aIsActive);
     }*/
    public DisplayGroup(int aSystemId, String   aDisplayName,
            int      aDisplayOrder,
             boolean aIsActive,boolean aIsDefault)
{
this(aSystemId, -1, aDisplayName, aDisplayOrder, aIsActive,aIsDefault);
}
     /**
      * The complete constructor.
      * 
      *  @param aDisplayName
      *  @param aDisplayOrder
      *  @param aIsActive
      */
    /*  public DisplayGroup(String   aDisplayName,
                          int      aDisplayOrder,
                           boolean aIsActive)
     {
         this(-1, aDisplayName, aDisplayOrder, aIsActive);
     }*/
    public DisplayGroup(String   aDisplayName,
            int      aDisplayOrder,
             boolean aIsActive,boolean aIsDefault)
{
this(-1, aDisplayName, aDisplayOrder, aIsActive,aIsDefault);
}
      
      /**
       * The complete constructor.
       * 
       *  @param aDisplayName
       *  @param aDisplayOrder
       *  @param aIsActive
       */
     /*  public DisplayGroup(String   aDisplayName,
                           int      aDisplayOrder)
      {
          this(aDisplayName, aDisplayOrder, true);
      }*/
    public DisplayGroup(String   aDisplayName,
            int      aDisplayOrder)
{
this(aDisplayName, aDisplayOrder, true,false);
}
      

	/**
	 * This method is used to create the DisplayGroup object from the ResultSet
	 * 
	 * @param aResultSet
	 *            the result object containing the DisplayGroups corresponding
	 *            to a row of the DisplayGroup table in the database
	 * @return the corresponding DisplayGroup object created from the ResultSet
	 */
	public static DisplayGroup createFromResultSet(ResultSet aResultSet)
			throws SQLException {
		DisplayGroup displayGroup = new DisplayGroup(
				aResultSet.getInt("sys_id"), aResultSet.getInt("id"),
				aResultSet.getString("display_name"),
				aResultSet.getInt("display_order"),
				aResultSet.getBoolean("is_active"),
				aResultSet.getBoolean("is_default"));
		return displayGroup;
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

	public String toString() {
		return "[" + mySystemId + "," + myId + "," + myDisplayName + ","
				+ myDisplayOrder + "," + myIsActive + ","+ myIsDefault +"]";
	}

	/**
	 * Method to return the source arraylist in the sorted order
	 * 
	 * @param source
	 *            the array list of Type objects
	 * @return the ArrayList of the Field objects in sorted order
	 */
	public static ArrayList<DisplayGroup> sort(Collection<DisplayGroup> source) {
		DisplayGroup[] srcArray = source.toArray(new DisplayGroup[0]);
		Arrays.sort(srcArray, new DisplayGroupComparator());

		ArrayList<DisplayGroup> target = new ArrayList<DisplayGroup>();

		for (DisplayGroup dg : srcArray) {
			target.add(dg);
		}
		return target;
	}

	/**
	 * This method returns the List of DisplayGroup objects for all BAs
	 * 
	 * @param aDisplayGroupId
	 *            System Id.
	 * 
	 * @return List of DisplayGroup objects.
	 * 
	 * @exception DatabaseException
	 *                incase of any database error.
	 */
	public static ArrayList<DisplayGroup> lookupAll() throws DatabaseException {
		ArrayList<DisplayGroup> displayGroupList = null;

		displayGroupList = new ArrayList<DisplayGroup>();

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();

			CallableStatement cs = connection
					.prepareCall("stp_display_group_lookupAll");
			ResultSet rs = cs.executeQuery();

			if (rs != null) {
				while (rs.next() != false) {
					DisplayGroup displayGroup = createFromResultSet(rs);
					displayGroupList.add(displayGroup);
				}

				// Close the result set.
				rs.close();
			}

			// Close the statement.
			cs.close();

			//
			// Release the memory by nullifying the references so that these
			// are recovered by the Garbage collector.
			//
			rs = null;
			cs = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append(
					"An exception occured while retrieving the display groups.")
					.append("\n");
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}

				connection = null;
			}
		}

		return displayGroupList;
	}

	/**
	 * This methods looks up display group by system id and display name.
	 * 
	 * @param aSystemId
	 * @param aDisplayName
	 * @return
	 * @throws DatabaseException
	 */

	public static DisplayGroup lookupBySystemIdAndDisplayName(int aSystemId,
			String aDisplayName) throws DatabaseException {
		DisplayGroup displayGroup = null;

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();

			PreparedStatement ps = connection
					.prepareStatement("SELECT * FROM display_groups WHERE sys_id="
							+ aSystemId
							+ " AND display_name='"
							+ aDisplayName
							+ "'");
			ResultSet rs = ps.executeQuery();

			if ((rs != null) && (rs.next() != false)) {
				displayGroup = createFromResultSet(rs);
			}

			// Close the result set.
			rs.close();

			// Close the statement.
			ps.close();

			//
			// Release the memory by nullifying the references so that these
			// are recovered by the Garbage collector.
			//
			rs = null;
			ps = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append(
					"An exception occured while retrieving the display group.")
					.append("\n");
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				connection = null;
			}
		}
		return displayGroup;
	}

	/**
	 * This methods looks up display group by system id and display name.
	 * 
	 * @param aSystemId
	 * @param aDisplayName
	 * @return
	 * @throws DatabaseException
	 */

	public static DisplayGroup lookupBySystemIdAndDisplayGroupId(int aSystemId,
			int aDisplayGroupId) throws DatabaseException {
		DisplayGroup displayGroup = null;

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();

			PreparedStatement ps = connection
					.prepareStatement("SELECT * FROM display_groups WHERE sys_id=?"
							+ " AND id=?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aDisplayGroupId);
			ResultSet rs = ps.executeQuery();

			if ((rs != null) && (rs.next() != false)) {
				displayGroup = createFromResultSet(rs);
			}

			// Close the result set.
			rs.close();

			// Close the statement.
			ps.close();

			//
			// Release the memory by nullifying the references so that these
			// are recovered by the Garbage collector.
			//
			rs = null;
			ps = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append(
					"An exception occured while retrieving the display group.")
					.append("\n");
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				connection = null;
			}
		}
		return displayGroup;
	}

	/**
	 * @deprecated
	 * @param aDisplayName
	 * @return
	 * @throws DatabaseException
	 */
	public static DisplayGroup lookupByDisplayName(String aDisplayName)
			throws DatabaseException {
		DisplayGroup displayGroup = null;

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();

			CallableStatement cs = connection
					.prepareCall("stp_display_group_lookupByDisplayName ?");
			cs.setString(1, aDisplayName);
			ResultSet rs = cs.executeQuery();

			if ((rs != null) && (rs.next() != false)) {
				displayGroup = createFromResultSet(rs);
			}

			// Close the result set.
			rs.close();

			// Close the statement.
			cs.close();

			//
			// Release the memory by nullifying the references so that these
			// are recovered by the Garbage collector.
			//
			rs = null;
			cs = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append(
					"An exception occured while retrieving the display group.")
					.append("\n");
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				connection = null;
			}
		}
		return displayGroup;
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
	public int compareTo(DisplayGroup aObject) {
		switch (ourSortField) {
		case SYSID: {
			Integer i1 = mySystemId;
			Integer i2 = aObject.mySystemId;
			if (ourSortOrder == ASC_ORDER) {
				return i1.compareTo(i2);
			}
			return i2.compareTo(i1);
		}
		case ID: {
			Integer i1 = myId;
			Integer i2 = aObject.myId;
			if (ourSortOrder == ASC_ORDER) {
				return i1.compareTo(i2);
			}
			return i2.compareTo(i1);
		}
		case DISPLAYNAME: {
			if (ourSortOrder == ASC_ORDER) {
				return myDisplayName.compareTo(aObject.myDisplayName);
			}
			return aObject.myDisplayName.compareTo(myDisplayName);
		}
		case DISPLAYORDER: {
			Integer i1 = myDisplayOrder;
			Integer i2 = aObject.myDisplayOrder;
			if (ourSortOrder == ASC_ORDER) {
				return i1.compareTo(i2);
			}
			return i2.compareTo(i1);
		}

		case ISACTIVE: {
			Boolean b1 = myIsActive;
			Boolean b2 = aObject.myIsActive;
			if (ourSortOrder == ASC_ORDER) {
				return b1.compareTo(b2);
			}
			return b2.compareTo(b1);
		}
		case ISDEFAULT: {
			Boolean d1 = myIsDefault;
			Boolean d2 = aObject.myIsDefault;
			if (ourSortOrder == ASC_ORDER) {
				return d1.compareTo(d2);
			}
			return d2.compareTo(d1);
		}
		}
		return 0;
	}

	/**
	 * Method to insert a DisplayGroup object into database. Note: It update the
	 * aObject's id to that of generated id of newly inserted row.
	 * 
	 * @param aObject
	 *            Object to be inserted
	 * @throws DatabaseException
	 */
	public static boolean insert(DisplayGroup aObject) throws DatabaseException {
		// Insert logic here.
		if (aObject == null)
			return false;

		Connection aCon = null;
		boolean returnValue = false;
		try {
			aCon = DataSourcePool.getConnection();
			CallableStatement cs = aCon
					.prepareCall("stp_display_group_insert  ?, ?, ?, ?,?, ?,?");
			aObject.setCallableParameters(cs);
			cs.registerOutParameter(7, INTEGER);
			cs.execute();
			aObject.setId(cs.getInt(7));
			cs.close();
			returnValue = true;
		} catch (SQLException sqle) {
			String message = "An exception occured while deleting the display group: "
					+ aObject.getDisplayName() + ".\n";
			throw new DatabaseException(message, sqle);
		} finally {
			if (aCon != null) {
				try {
					aCon.close();
				} catch (SQLException sqle) {
					System.out
							.print("Unable to close the connection after inserting display group");
					sqle.printStackTrace();
					returnValue = false;
				}
			}
		}
		return returnValue;
	}

	/**
	 * Method to update the corresponding DisplayGroup object in the database.
	 * 
	 * @param aObject
	 *            Object to be updated
	 * 
	 * @return Update domain object.
	 * @throws DatabaseException
	 * 
	 */
	public static DisplayGroup update(DisplayGroup aObject)
			throws DatabaseException {
		// Update logic here.
		DisplayGroup displayGroup = null;
		if (aObject == null)
			return aObject;

		Connection aCon = null;

	    try
        {	
        	int iddis=aObject.getDisplayOrder();
           if(iddis!=0)
            {	displayGroupIndex++;
        	   DisplayGroup dp=DisplayGroup.lookupByDisplayGroupId(aObject.getId());
      		   aObject.setSystemId(dp.getSystemId());  
        	        
           		aCon = DataSourcePool.getConnection();
           		aObject.setDisplayOrder(displayGroupIndex);
           	/*	if(aObject.getIsDefault()==true)
           		{
           			ArrayList<DisplayGroup>  dgDefault= DisplayGroup.lookupBySystemId(aObject.getSystemId());
           			for(DisplayGroup dg : dgDefault)
           			{
           				if(dg.getIsDefault()==true && (dg.getId() != aObject.getId()))
           				{
           					dg.setIsDefault(false);
           					
           				}
           			}
           			
           		}*/
           		
           		
               CallableStatement cs = aCon.prepareCall("stp_display_group_update ?, ?, ?, ?,?, ?");
               aObject.setCallableParameters(cs);
               cs.execute();
               cs.close();
           		
                         
               
           }
        } catch (SQLException sqle) {
			String message = "An exception occured while updating the display group: "
					+ aObject.getDisplayName() + ".\n";
			throw new DatabaseException(message, sqle);
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

	/**
	 * Accessor method for SystemId property.
	 * 
	 * @return Current Value of Id
	 * 
	 */
	public int getSystemId() {
		return mySystemId;
	}

	/**
	 * Accessor method for Id property.
	 * 
	 * @return Current Value of Id
	 * 
	 */
	public int getId() {
		return myId;
	}

	/**
	 * Accessor method for DisplayName property.
	 * 
	 * @return Current Value of DisplayName
	 * 
	 */
	public String getDisplayName() {
		return myDisplayName;
	}

	/**
	 * Accessor method for DisplayOrder property.
	 * 
	 * @return Current Value of DisplayOrder
	 * 
	 */
	public int getDisplayOrder() {
		return myDisplayOrder;
	}

	/**
	 * Accessor method for IsDefault property.
	 * 
	 * @return Current Value of IsDefault
	 * 
	 */
	public boolean getIsDefault() {
		return myIsDefault;
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
	 * Mutator method for SystemId property.
	 * 
	 * @param aId
	 *            New Value for SystemId
	 * 
	 */
	public void setSystemId(int aSystemId) {
		mySystemId = aSystemId;
	}

	/**
	 * Mutator method for Id property.
	 * 
	 * @param aId
	 *            New Value for Id
	 * 
	 */
	public void setId(int aId) {
		myId = aId;
	}

	/**
	 * Mutator method for DisplayName property.
	 * 
	 * @param aDisplayName
	 *            New Value for DisplayName
	 * 
	 */
	public void setDisplayName(String aDisplayName) {
		myDisplayName = aDisplayName;
	}

	/**
	 * Mutator method for DisplayOrder property.
	 * 
	 * @param aDisplayOrder
	 *            New Value for DisplayOrder
	 * 
	 */
	public void setDisplayOrder(int aDisplayOrder) {
		myDisplayOrder = aDisplayOrder;
	}

	/**
	 * Mutator method for IsDefault property.
	 * 
	 * @param aIsActive
	 *            New Value for IsDefault
	 * 
	 */
	public void setIsDefault(boolean aIsDefault) {
		myIsDefault = aIsDefault;
	}

	/**
	 * Mutator method for IsActive property.
	 * 
	 * @param aIsActive
	 *            New Value for IsActive
	 * 
	 */
	public void setIsActive(boolean aIsActive) {
		myIsActive = aIsActive;
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
		aCS.setInt(SYSID, mySystemId);
		aCS.setInt(ID, myId);
		aCS.setString(DISPLAYNAME, myDisplayName);
		aCS.setInt(DISPLAYORDER, myDisplayOrder);
		aCS.setBoolean(ISACTIVE, myIsActive);
		aCS.setBoolean(ISDEFAULT, myIsDefault);
	}

	/**
	 * Method to delete the corresponding Field object in the database.
	 * 
	 * @param aObject
	 *            Object to be updated
	 * 
	 * @return Delete domain object.
	 * 
	 */
	public static DisplayGroup delete(DisplayGroup aObject)
			throws DatabaseException {

		// Update logic here.
		if (aObject == null) {
			return aObject;
		}

		Connection aCon = null;
		int returnValue = 0;

		try {
			aCon = DataSourcePool.getConnection();
			if(aObject.getIsDefault()==false)
			{
			CallableStatement cs = aCon.prepareCall("stp_display_group_delete "
					+ "?, ?, ?, ?, ?,?,?");

			aObject.setCallableParameters(cs);
			cs.registerOutParameter(7, INTEGER);
			cs.execute();
			returnValue = cs.getInt(7);
			cs.close();
		}
			
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append(
					"An exception occured while deleting the display group.")
					.append("\n");

			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (aCon != null) {
				try {
					aCon.close();
				} catch (SQLException sqle) {

					// Should this be logged.?
				}
			}
		}

		if (returnValue == 1) {
			return aObject;
		} else {
			return null;
		}
	}

	/**
	 * This looks up display groups for a business area based on the system id
	 * provided.
	 * 
	 * @param systemId
	 * @return ArrayList of display groups
	 * @throws DatabaseException
	 */

	public static ArrayList<DisplayGroup> lookupBySystemId(int systemId)
			throws DatabaseException {
		ArrayList<DisplayGroup> displayGroupList = new ArrayList<DisplayGroup>();
		DisplayGroup displayGroup = null;

		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();

			PreparedStatement ps = connection
					.prepareStatement("SELECT * FROM display_groups WHERE sys_id="
							+ systemId);
			ResultSet rs = ps.executeQuery();

			if (rs != null)
				while (rs.next() != false) {
					displayGroup = createFromResultSet(rs);
					if (displayGroup != null)
						displayGroupList.add(displayGroup);
				}

			// Close the result set.
			rs.close();

			// Close the statement.
			ps.close();

			//
			// Release the memory by nullifying the references so that these
			// are recovered by the Garbage collector.
			//
			rs = null;
			ps = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append(
					"An exception occured while retrieving the display group.")
					.append("\n");
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				connection = null;
			}
		}
		return displayGroupList;
	}

	/**
	 * This retrieves all the display groups pertaining to a business area based
	 * on the system id along with the default display group with display group
	 * id=1, which is reserved for it and should not be used by any other
	 * display group.
	 * 
	 * @param aSystemId
	 * @return
	 * @throws DatabaseException
	 */
	public static ArrayList<DisplayGroup> lookupIncludingDefaultForSystemId(
			int aSystemId) throws DatabaseException {
		ArrayList<DisplayGroup> dgList = null;
		dgList = lookupBySystemId(aSystemId);
		//dgList.add(lookupByDisplayGroupId(DEFAULT_DISPLAY_GROUP_ID));
		return dgList;
	}

	/**
	 * This retrieves the display group with the given display group id, which
	 * is unique for each display group irrespective of the business area they
	 * belong to.
	 * 
	 * @param displayGroupId
	 * @return
	 * @throws DatabaseException
	 */
	public static DisplayGroup lookupByDisplayGroupId(int displayGroupId)
			throws DatabaseException {
		DisplayGroup displayGroup = null;
		// TODO: Use Caching to improve performance.
		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();

			PreparedStatement ps = connection
					.prepareStatement("SELECT * FROM display_groups WHERE id="
							+ displayGroupId);

			ResultSet rs = ps.executeQuery();

			if ((rs != null) && (rs.next() != false)) {
				displayGroup = createFromResultSet(rs);
			}

			// Close the result set.
			rs.close();

			// Close the statement.
			ps.close();

			//
			// Release the memory by nullifying the references so that these
			// are recovered by the Garbage collector.
			//
			rs = null;
			ps = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append(
					"An exception occured while retrieving the display group.")
					.append("\n");
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				connection = null;
			}
		}
		return displayGroup;
	}

	public static void main(String[] args) throws DatabaseException {
		// DisplayGroup aObject = new DisplayGroup(19, "test20", 20, true);
		// DisplayGroup.insert(aObject);
		// DisplayGroup.delete(aObject);
		// System.out.println("Done : " +
		// lookupBySystemIdAndDisplayName(aObject.getSystemId(),
		// aObject.getDisplayName()));
		// System.out.println("Done : " + aObject.getSystemId() + ", " +
		// aObject.getDisplayName());
		System.out.println("DGID: " + lookupBySystemIdAndDisplayGroupId(1, 97));
	}

}

/**
 * This class is the comparator for domain object corresponding to the
 * display_group table in the database.
 * 
 * @author : giris
 * @version : $Id: $
 * 
 */
class DisplayGroupComparator implements Comparator<DisplayGroup>, Serializable {
	public int compare(DisplayGroup obj1, DisplayGroup obj2) {
		return obj1.compareTo(obj2);
	}
}
