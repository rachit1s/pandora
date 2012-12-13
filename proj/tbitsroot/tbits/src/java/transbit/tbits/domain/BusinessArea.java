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
 * BusinessArea.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;
import static transbit.tbits.api.Mapper.ourBAMap;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.SysConfig;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the business_areas table
 * in the database.
 *
 * @author  :
 * @version : $Id: $
 *
 */
public class BusinessArea implements Comparable<BusinessArea>, Serializable {
    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BusinessArea [mySystemPrefix=" + mySystemPrefix
				+ ", mySystemId=" + mySystemId + ", myName=" + myName
				+ ", myDisplayName=" + myDisplayName + ", myIsActive="
				+ myIsActive + ", myIsEmailActive=" + myIsEmailActive
				+ ", myIsPrivate=" + myIsPrivate + ", myDescription="
				+ myDescription + ", myEmail=" + myEmail + "]";
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    public static final int SYSTEMID        = 1;
    public static final int NAME            = 2;
    public static final int EMAIL           = 4;
    public static final int DISPLAYNAME     = 3;
    public static final int SYSTEMPREFIX    = 5;
    public static final int DESCRIPTION     = 6;
    public static final int TYPE            = 7;
    public static final int SYSCONFIG       = 15;
    public static final int MAXREQUESTID    = 10;
    public static final int MAXEMAILACTIONS = 11;
    public static final int LOCATION        = 8;
    public static final int ISPRIVATE       = 14;
    public static final int ISEMAILACTIVE   = 12;
    public static final int ISACTIVE        = 13;
    public static final int FIELDCONFIG     = 16;
    public static final int DATECREATED     = 9;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private Timestamp myDateCreated;
    private String    myDescription;
    private String    myDisplayName;
    private String    myEmail;
    private String    myFieldConfig;
    private boolean   myIsActive;
    private boolean   myIsEmailActive;
    private boolean   myIsPrivate;
    private String    myLocation;
    private int       myMaxEmailActions;
    private int       myMaxRequestId;
    private String    myName;
    private String    mySysConfig;
    private SysConfig mySysConfigObject;

    // Attributes of this Domain Object.
    private int    mySystemId;
    private String mySystemPrefix;
    private String myType;

    //~--- constant enums -----------------------------------------------------

    public static enum BAColumn {
        SYS_ID, NAME, DISPLAY_NAME, EMAIL, SYS_PREFIX, DESCRIPTION, TYPE, LOCATION, DATE_CREATED, MAX_REQUEST_ID, MAX_EMAIL_ACTIONS, IS_EMAIL_ACTIVE, IS_ACTIVE, IS_PRIVATE, SYS_CONFIG, FIELD_CONFIG
    }

    ;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public BusinessArea() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aName
     *  @param aDisplayName
     *  @param aEmail
     *  @param aSystemPrefix
     *  @param aDescription
     *  @param aType
     *  @param aLocation
     *  @param aDateCreated
     *  @param aMaxRequestId
     *  @param aMaxEmailActions
     *  @param aIsEmailActive
     *  @param aIsActive
     *  @param aIsPrivate
     *  @param aSysConfig
     *  @param aFieldConfig
     */
    public BusinessArea(int aSystemId, String aName, String aDisplayName, String aEmail, String aSystemPrefix, String aDescription, String aType, String aLocation, Timestamp aDateCreated,
                        int aMaxRequestId, int aMaxEmailActions, boolean aIsEmailActive, boolean aIsActive, boolean aIsPrivate, String aSysConfig, String aFieldConfig) {
        mySystemId        = aSystemId;
        myName            = aName;
        myDisplayName     = aDisplayName;
        myEmail           = aEmail;
        mySystemPrefix    = aSystemPrefix;
        myDescription     = aDescription;
        myType            = aType;
        myLocation        = aLocation;
        myDateCreated     = aDateCreated;
        myMaxRequestId    = aMaxRequestId;
        myMaxEmailActions = aMaxEmailActions;
        myIsEmailActive   = aIsEmailActive;
        myIsActive        = aIsActive;
        myIsPrivate       = aIsPrivate;
        mySysConfig       = aSysConfig;
        myFieldConfig     = aFieldConfig;

        try {
            mySysConfigObject = SysConfig.getSysConfig(mySysConfig);
        } catch (Exception e) {
            LOG.warn(e.toString(), e);
        }
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
    public int compareTo(BusinessArea aObject) {
        switch (ourSortField) {
        case SYSTEMID : {
            Integer i1 = mySystemId;
            Integer i2 = aObject.mySystemId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case NAME : {
            if (ourSortOrder == ASC_ORDER) {
                return myName.compareToIgnoreCase(aObject.myName);
            }

            return aObject.myName.compareToIgnoreCase(myName);
        }

        case DISPLAYNAME : {
            if (ourSortOrder == ASC_ORDER) {
                return myDisplayName.compareToIgnoreCase(aObject.myDisplayName);
            }

            return aObject.myDisplayName.compareToIgnoreCase(myDisplayName);
        }

        case EMAIL : {
            if (ourSortOrder == ASC_ORDER) {
                return myEmail.compareToIgnoreCase(aObject.myEmail);
            }

            return aObject.myEmail.compareToIgnoreCase(myEmail);
        }

        case SYSTEMPREFIX : {
            if (ourSortOrder == ASC_ORDER) {
                return mySystemPrefix.compareToIgnoreCase(aObject.mySystemPrefix);
            }

            return aObject.mySystemPrefix.compareToIgnoreCase(mySystemPrefix);
        }

        case DESCRIPTION : {
            if (ourSortOrder == ASC_ORDER) {
                return myDescription.compareToIgnoreCase(aObject.myDescription);
            }

            return aObject.myDescription.compareToIgnoreCase(myDescription);
        }

        case TYPE : {
            if (ourSortOrder == ASC_ORDER) {
                return myType.compareToIgnoreCase(aObject.myType);
            }

            return aObject.myType.compareToIgnoreCase(myType);
        }

        case LOCATION : {
            if (ourSortOrder == ASC_ORDER) {
                return myLocation.compareToIgnoreCase(aObject.myLocation);
            }

            return aObject.myLocation.compareToIgnoreCase(myLocation);
        }

        case DATECREATED : {
            if (ourSortOrder == ASC_ORDER) {
                return myDateCreated.compareTo(aObject.myDateCreated);
            }

            return aObject.myDateCreated.compareTo(myDateCreated);
        }

        case MAXREQUESTID : {
            Integer i1 = myMaxRequestId;
            Integer i2 = aObject.myMaxRequestId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case MAXEMAILACTIONS : {
            Integer i1 = myMaxEmailActions;
            Integer i2 = aObject.myMaxEmailActions;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case ISEMAILACTIVE : {
            Boolean b1 = myIsEmailActive;
            Boolean b2 = aObject.myIsEmailActive;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }

        case ISACTIVE : {
            Boolean b1 = myIsActive;
            Boolean b2 = aObject.myIsActive;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }

        case ISPRIVATE : {
            Boolean b1 = myIsPrivate;
            Boolean b2 = aObject.myIsPrivate;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }

        case SYSCONFIG : {
            if (ourSortOrder == ASC_ORDER) {
                return mySysConfig.compareTo(aObject.mySysConfig);
            }

            return aObject.mySysConfig.compareTo(mySysConfig);
        }

        case FIELDCONFIG : {
            if (ourSortOrder == ASC_ORDER) {
                return myFieldConfig.compareTo(aObject.myFieldConfig);
            }

            return aObject.myFieldConfig.compareTo(myFieldConfig);
        }
        }

        return 0;
    }

    /**
     * This method creates a Business Area with the given name and prefix.
     *
     * @param aSystemName The name of the Business Area.
     * @param aSystemPrefix The prefix of the Business Area.
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
    public static void createBusinessArea(String aSystemName, String aSystemPrefix) throws DatabaseException {
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_tbits_createBusinessArea ?, ?");

            cs.setString(1, aSystemName);
            cs.setString(2, aSystemPrefix);
            cs.execute();

            // Close the statement.
            cs.close();

            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while creating the ").append("BusinessArea").append("\nSystem Name: ").append(aSystemName).append("\nSystem Prefix: ").append(aSystemPrefix).append(
                "\n");

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
    }

    /**
     * This method is used to create the BusinessArea object
     * from the ResultSet
     *
     * @param  aResultSet the result object containing the fields
     * correspondin to a row of the business area table in the database
     * @return the corresponding BusinessArea object
     * created from the ResultSet
     */
    public static BusinessArea createFromResultSet(ResultSet aResultSet) throws SQLException {
        BusinessArea ba = new BusinessArea(aResultSet.getInt("sys_id"), aResultSet.getString("name"), aResultSet.getString("display_name"), aResultSet.getString("email"),
                                           aResultSet.getString("sys_prefix"), aResultSet.getString("description"), aResultSet.getString("type"), aResultSet.getString("location"),
                                           Timestamp.getTimestamp(aResultSet.getTimestamp("date_created")), aResultSet.getInt("max_request_id"), aResultSet.getInt("max_email_actions"),
                                           aResultSet.getBoolean("is_email_active"), aResultSet.getBoolean("is_active"), aResultSet.getBoolean("is_private"), aResultSet.getString("sys_config"),
                                           aResultSet.getString("field_config"));

        return ba;
    }

    /**
     * This method is used to compare two BusinessArea objects.
     *
     * @param aObject Business Area object.
     *
     * @return True if equal and false if not.
     */
    public boolean equals(BusinessArea aObject) {
        if (aObject == null) {
            return false;
        }

        // Autoboxing makes mySystemId as Integer.
        return (mySystemId == aObject.mySystemId);
    }
 
    public int hashCode(){
    	return this.getSystemId();
    }
    
    /**
     * This method is used to compare two BusinessArea objects.
     *
     * @param aObject  object.
     *
     * @return True if equal and false if not.
     */
    public boolean equals(Object aObject) {
        if (aObject == null) {
            return false;
        }

        BusinessArea ba = null;

        try {
            ba = (BusinessArea) aObject;
        } catch (ClassCastException cce) {
            return false;
        }

        return (mySystemId == ba.mySystemId);
    }
    
    

    /**
     * This method returns the BusinessArea object corresponding to the given
     * email address.
     *
     * @param aEmail Business Area Email Address
     *
     * @return BusinessArea object corresponding to this email
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
    public static BusinessArea lookupByEmail(String aEmail) throws DatabaseException {
        BusinessArea ba = null;

        // Look in the mapper first.
        String key = aEmail.toUpperCase();

        if (ourBAMap != null) {
            ba = ourBAMap.get(key);

            return ba;
        }

        // else try to get the BA record from the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_ba_lookupByEmail ?");

            cs.setString(1, aEmail);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    ba = createFromResultSet(rs);
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

            message.append("An exception occurred while retrieving the ").append("BusinessArea Object.").append("\nSystem Email: ").append(aEmail).append("\n");

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

        return ba;
    }

    /**
     * This method returns the BusinessArea object corresponding to the given
     * Name.
     *
     * @param aName Business Area Name
     *
     * @return BusinessArea object corresponding to this Name
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
    public static BusinessArea lookupByName(String aName) throws DatabaseException {
        BusinessArea ba = null;

        // Look in the mapper first.
        String key = aName.toUpperCase();

        if (ourBAMap != null) {
            ba = ourBAMap.get(key);

            return ba;
        }

        // else try to get the BA record from the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_ba_lookupByName ?");

            cs.setString(1, aName);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    ba = createFromResultSet(rs);
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

            message.append("An exception occurred while retrieving the ").append("BusinessArea Object.").append("\nSystem Name: ").append(aName).append("\n");

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

        return ba;
    }

    /**
     * This method returns the BusinessArea object corresponding to the given
     * System Id.
     *
     * @param aSystemId Business Area Id.
     *
     * @return BusinessArea object corresponding to this SystemId
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
    public static BusinessArea lookupBySystemId(int aSystemId) throws DatabaseException {
        BusinessArea ba = null;

        // Look in the mapper first.
        String key = Integer.toString(aSystemId);

        if (ourBAMap != null) {
            ba = ourBAMap.get(key);

            return ba;
        }

        // else try to get the BA record from the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_ba_lookupBySystemId ?");

            cs.setInt(1, aSystemId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    ba = createFromResultSet(rs);
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

            message.append("An exception occurred while retrieving the ").append("BusinessArea Object.").append("\nSystem Id: ").append(aSystemId).append("\n");

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

        return ba;
    }

    /**
     * This method returns the BusinessArea object corresponding to the given
     * System Prefix.
     *
     * @param aSystemPrefix Business Area Prefix
     *
     * @return BusinessArea object corresponding to this System Prefix
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
    public static BusinessArea lookupBySystemPrefix(String aSystemPrefix) throws DatabaseException {
        BusinessArea ba = null;

        // Look in the mapper first.
        String key = aSystemPrefix.toUpperCase();

        if (ourBAMap != null) {
            ba = ourBAMap.get(key);

            //
            // If key matched to any BA attribute other than prefix
            // return null
            //
            if ((ba != null) && (ba.getSystemPrefix().toUpperCase().equals(key) == false)) {
                return null;
            }

            return ba;
        }

        // else try to get the BA record from the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_ba_lookupBySystemPrefix ?");

            cs.setString(1, aSystemPrefix);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    ba = createFromResultSet(rs);
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

            message.append("An exception occurred while retrieving the ").append("BusinessArea Object.").append("\nSystem Prefix: ").append(aSystemPrefix).append("\n");

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

        return ba;
    }

    /**
     * The main method.
     */
    public static void main(String arg[]) throws Exception {
        BusinessArea            ba   = BusinessArea.lookupBySystemId(1);
        ArrayList<BusinessArea> list = BusinessArea.getAllBusinessAreas();

        //transbit.tbits.api.Mapper.stop();
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the BusinessArea objects in sorted order
     */
    public static ArrayList<BusinessArea> sort(ArrayList<BusinessArea> source) {
        int            size     = source.size();
        BusinessArea[] srcArray = new BusinessArea[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray);

        ArrayList<BusinessArea> target = new ArrayList<BusinessArea>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    /**
     * Method to update the corresponding BusinessArea object in the database.
     *
     * @param aObject Object to be updated
     *
     * @return Update domain object.
     *
     */
    public static BusinessArea update(BusinessArea aObject) {

        // Update logic here.
        if (aObject == null) {
            return aObject;
        }

        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_ba_update " + "?, ?, ?, ?, ?, ?, ?, ?," + "?, ?, ?, ?, ?, ?, ?, ? ");

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
            LOG.severe("Exception while updating the Business Area ", sqle);
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
     * Method to insert the corresponding BusinessArea object in the database.
     *
     * @param aObject Object to be inserted
     *
     * @return Update domain object.
     *
     */
    public static int insert(BusinessArea aObject) {
        // Update logic here.
		int newSystemId = -1;
        if (aObject == null) {
            return newSystemId;
        }
        
        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("{call dbo.stp_ba_insert (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.registerOutParameter(16, java.sql.Types.INTEGER);
            aObject.setCallableParametersWithoutSysId(cs);            
            cs.execute();
            newSystemId = cs.getInt(16);
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
            System.out.println("Exception while inserting the Business Area " + sqle);
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {

                    // Should this be logged.?
                }
            }
        }
        return newSystemId;
    }
    
    
    //~--- get methods --------------------------------------------------------

    /**
     * Generic Get Method.
     */
    public String get(BAColumn column) {
        String value = "";

        switch (column) {
        case SYS_ID :
            value = Integer.toString(mySystemId);

            break;

        case NAME :
            value = myName;

            break;

        case DISPLAY_NAME :
            value = myDisplayName;

            break;

        case EMAIL :
            value = myEmail;

            break;

        case SYS_PREFIX :
            value = mySystemPrefix;

            break;

        case DESCRIPTION :
            value = myDescription;

            break;

        case TYPE :
            value = myType;

            break;

        case LOCATION :
            value = myLocation;

            break;

        case DATE_CREATED :
            value = myDateCreated.toCustomFormat(TBitsConstants.API_DATE_FORMAT);

            break;

        case MAX_REQUEST_ID :
            value = Integer.toString(myMaxRequestId);

            break;

        case MAX_EMAIL_ACTIONS :
            value = Integer.toString(myMaxEmailActions);

            break;

        case IS_EMAIL_ACTIVE :
            value = Boolean.toString(myIsEmailActive);

            break;

        case IS_ACTIVE :
            value = Boolean.toString(myIsActive);

            break;

        case IS_PRIVATE :
            value = Boolean.toString(myIsPrivate);

            break;

        case SYS_CONFIG :
            value = mySysConfig;

            break;

        case FIELD_CONFIG :
            value = myFieldConfig;

            break;
        }

        return value;
    }

    /**
     * Method to get the list of BusinesAreas in which the user can view the
     * admin pages.
     *
     * @param  aUserId the userId for which the related BusinessAreas are
     *                 associated with
     * @return the ArrayList containing the list of the BusineesArea objects
     *         in which the user is an admin.
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<BusinessArea> getAdminBusinessAreas(int aUserId) throws DatabaseException {
        ArrayList<BusinessArea> businessAreaList = new ArrayList<BusinessArea>();
        Connection              connection       = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("{Call stp_admin_getBusinessAreasByUserId ( ? ) }");

            cs.setInt(1, aUserId);

            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                BusinessArea businessArea = createFromResultSet(rs);

                businessAreaList.add(businessArea);
            }

            rs.close();
            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("The application encountered an exception while ").append("trying to retrieve the list of Business Areas").append("the user " + aUserId).append(" has Admin Permissions on");

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

        return businessAreaList;
    }

    public static ArrayList<BusinessArea> getActiveBusinessAreas() throws DatabaseException {
    	ArrayList<BusinessArea> activeBAList = new ArrayList<BusinessArea>();
    	ArrayList<BusinessArea> allBAList = getAllBusinessAreas();
    	for(BusinessArea ba: allBAList)
    	{
    		if(ba.getIsActive())
    		{
    			activeBAList.add(ba);
    		}
    	}
    	return activeBAList;
    }
    
    /**
     * This method returns all the BusinessArea objects in the database.
     *
     * @return List of all the BusinessAreas in the database.
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
    public static ArrayList<BusinessArea> getAllBusinessAreas() throws DatabaseException {
        ArrayList<BusinessArea> baList = new ArrayList<BusinessArea>();

        // Try to get from the mapper.
        if (ourBAMap != null) {
            ArrayList<BusinessArea> tempList = new ArrayList<BusinessArea>(ourBAMap.values());
                
            for (BusinessArea ba : tempList) {
                if (baList.contains(ba) == false) {
                	 
                    baList.add(ba);
                    
                }
                
            }
            
            return sort(baList);
        }

        // Else Try to get the list from the Business Area.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_ba_getAllBusinessAreas");
            ResultSet         rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    BusinessArea ba = createFromResultSet(rs);

                    baList.add(ba);
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

            message.append("An exception occurred while retrieving all the ").append("BusinessArea Objects.").append("\n");

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

        return baList;
    }

    /**
     * Method to get the list of BusinesAreas that an user is associated with.
     *
     * @param  aUserId the userId for which the related BusinessAreas are
     *                 associated with
     * @return the ArrayList containing the list of the BusineesArea objects
     *         associated with the analyst.
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<BusinessArea> getAnalystBusinessAreas(int aUserId) throws DatabaseException {
        ArrayList<BusinessArea> businessAreaList = new ArrayList<BusinessArea>();
        Connection              connection       = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("{Call stp_ba_getAnalystBusinessAreas ( ? ) }");

            cs.setInt(1, aUserId);

            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                BusinessArea businessArea = createFromResultSet(rs);

                businessAreaList.add(businessArea);
            }

            rs.close();
            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("The application encountered an exception while ").append("trying to retrieve the list of Business Areas").append("the analyst with user Id "
                           + aUserId).append(" is associated with");

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

        return businessAreaList;
    }

    /**
     * This method returns all the BusinessArea objects in the database that
     * the user has permissions to view.
     *
     * @param aUserId Id of the user.
     * @return List of all the BusinessAreas in the database.
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
    public static ArrayList<BusinessArea> getBusinessAreasByUserId(int aUserId) throws DatabaseException {
        ArrayList<BusinessArea> baList = new ArrayList<BusinessArea>();

        // To get this list, we should go to the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_ba_getBusinessAreasByUserId ?");

            cs.setInt(1, aUserId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    BusinessArea ba = createFromResultSet(rs);

                    baList.add(ba);
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

            message.append("An exception occurred while retrieving the ").append("BusinessArea Objects.").append("\nUser Id   : ").append(aUserId).append("\n");

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

        return baList;
    }

    /**
     * Accessor method for DateCreated property.
     *
     * @return Current Value of DateCreated
     *
     */
    public Timestamp getDateCreated() {
        return myDateCreated;
    }

    /**
     * This method returns the property values from the XML Configuration.
     *
     *
     *
     */
    public static int getDefaultDueDate() {
        int defaultDueDate = 0;

        // Read it from the config.
        return defaultDueDate;
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
     * Accessor method for DisplayName property.
     *
     * @return Current Value of DisplayName
     *
     */
    public String getDisplayName() {
        return myDisplayName;
    }

    /**
     * Accessor method for Email property.
     *
     * @return Current Value of Email
     *
     */
    public String getEmail() {
        return myEmail;
    }

    /**
     * Accessor method for Email property.
     *
     * @return Current Value of Email
     *
     */
    public ArrayList<String> getEmailList() {
        ArrayList<String> emails = Utilities.toArrayList(myEmail);

        if ((emails == null) || (emails.size() == 0)) {
            return (new ArrayList<String>());
        } else {
            return emails;
        }
    }

    /**
     * Accessor method for FieldConfig property.
     *
     * @return Current Value of FieldConfig
     *
     */
    public String getFieldConfig() {
        return myFieldConfig;
    }

    /**
     * This method returns the first active Business Area object.
     *
     * @return BA Object
     */
    public static BusinessArea getFirstActiveBusinessArea() {
        BusinessArea ba         = null;
        Collection   collection = ourBAMap.values();
        Iterator     iterator   = collection.iterator();

        while (iterator.hasNext()) {
            ba = (BusinessArea) iterator.next();

            if (ba.getIsActive() == true) {
                return ba;
            }
        }

        return ba;
    }

    /**
     * Accessor method for Email property.
     *
     * @return Current Value of Email
     *
     */
    public String getFirstEmail() {
        ArrayList<String> emails = Utilities.toArrayList(myEmail);

        if ((emails == null) || (emails.size() == 0)) {
            return "";
        } else {
            return emails.get(0);
        }
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
     * Accessor method for IsEmailActive property.
     *
     * @return Current Value of IsEmailActive
     *
     */
    public boolean getIsEmailActive() {
        return myIsEmailActive;
    }

    /**
     * Accessor method for IsPrivate property.
     *
     * @return Current Value of IsPrivate
     *
     */
    public boolean getIsPrivate() {
        return myIsPrivate;
    }

    /**
     * Accessor method for Location property.
     *
     * @return Current Value of Location
     *
     */
    public String getLocation() {
        return myLocation;
    }

    /**
     * Accessor method for MaxEmailActions property.
     *
     * @return Current Value of MaxEmailActions
     *
     */
    public int getMaxEmailActions() {
        return myMaxEmailActions;
    }

    /**
     * Accessor method for MaxRequestId property.
     *
     * @return Current Value of MaxRequestId
     *
     */
    public int getMaxRequestId() {
        return myMaxRequestId;
    }

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
     * Accessor method for SysConfig property.
     *
     * @return Current Value of SysConfig
     *
     */
    public String getSysConfig() {
        return mySysConfig;
    }

    /**
     * Accessor method for SysConfig property.
     *
     * @return Current Value of SysConfig
     *
     */
    public SysConfig getSysConfigObject() {
        return mySysConfigObject;
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
     * Accessor method for SystemPrefix property.
     *
     * @return Current Value of SystemPrefix
     *
     */
    public String getSystemPrefix() {
        return mySystemPrefix;
    }

    /**
     * Accessor method for Type property.
     *
     * @return Current Value of Type
     *
     */
    public String getType() {
        return myType;
    }

    /**
     * This method returns the prefixes of all the business areas where the
     * user is present in the given list of contextual roles in the requests.
     *
     * @param aUserId           Id of the user.
     * @param aUserTypeIdList   List of contextual role ids.
     * @param aIsPrimary        Primary field.
     *
     * @return List of the BusinessArea prefixes.
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
    public static ArrayList<String> getUserBAList(int aUserId, String aUserTypeIdList, boolean aIsPrimary) throws DatabaseException {
        ArrayList<String> sysIdList = new ArrayList<String>();

        // To get this list, we should go to the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_ru_getUserBAList ?, ?, ?");

            cs.setInt(1, aUserId);
            cs.setString(2, aUserTypeIdList);
            cs.setBoolean(3, aIsPrimary);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    sysIdList.add(rs.getString("sys_prefix"));
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

            message.append("An exception occurred while retrieving the ").append("BusinessAreas where the given user is present in .").append("the specified contextual roles.").append(
                "\nUser Id   : ").append(aUserId).append("\nUser Types: ").append(aUserTypeIdList).append("\nIs Primary: ").append(aIsPrimary).append("\n");

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

        return sysIdList;
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
        aCS.setString(NAME, myName);
        aCS.setString(DISPLAYNAME, myDisplayName);
        aCS.setString(EMAIL, myEmail);
        aCS.setString(SYSTEMPREFIX, mySystemPrefix);
        aCS.setString(DESCRIPTION, myDescription);
        aCS.setString(TYPE, myType);
        aCS.setString(LOCATION, myLocation);
        aCS.setTimestamp(DATECREATED, myDateCreated.toSqlTimestamp());
        aCS.setInt(MAXREQUESTID, myMaxRequestId);
        aCS.setInt(MAXEMAILACTIONS, myMaxEmailActions);
        aCS.setBoolean(ISEMAILACTIVE, myIsEmailActive);
        aCS.setBoolean(ISACTIVE, myIsActive);
        aCS.setBoolean(ISPRIVATE, myIsPrivate);
        aCS.setString(SYSCONFIG, mySysConfig);
        aCS.setString(FIELDCONFIG, myFieldConfig);
    }

    public void setCallableParametersWithoutSysId(CallableStatement aCS) throws SQLException {    	    	
        aCS.setString(1, myName);
        aCS.setString(2, myDisplayName);
        aCS.setString(3, myEmail);
        aCS.setString(4, mySystemPrefix);
        aCS.setString(5, myDescription);
        aCS.setString(6, myType);
        aCS.setString(7, myLocation);
        aCS.setTimestamp(8, myDateCreated.toSqlTimestamp());
        aCS.setInt(9, myMaxRequestId);
        aCS.setInt(10, myMaxEmailActions);
        aCS.setBoolean(11, myIsEmailActive);
        aCS.setBoolean(12, myIsActive);
        aCS.setBoolean(13, myIsPrivate);
        aCS.setString(14, mySysConfig);
        aCS.setString(15, myFieldConfig);
    }
    
    
    /**
     * Mutator method for DateCreated property.
     *
     * @param aDateCreated New Value for DateCreated
     *
     */
    public void setDateCreated(Timestamp aDateCreated) {
        myDateCreated = aDateCreated;
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
     * Mutator method for DisplayName property.
     *
     * @param aDisplayName New Value for DisplayName
     *
     */
    public void setDisplayName(String aDisplayName) {
        myDisplayName = aDisplayName;
    }

    /**
     * Mutator method for Email property.
     *
     * @param aEmail New Value for Email
     *
     */
    public void setEmail(String aEmail) {
        myEmail = aEmail;
    }

    /**
     * Mutator method for FieldConfig property.
     *
     * @param aFieldConfig New Value for FieldConfig
     *
     */
    public void setFieldConfig(String aFieldConfig) {
        myFieldConfig = aFieldConfig;
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
     * Mutator method for IsEmailActive property.
     *
     * @param aIsEmailActive New Value for IsEmailActive
     *
     */
    public void setIsEmailActive(boolean aIsEmailActive) {
        myIsEmailActive = aIsEmailActive;
    }

    /**
     * Mutator method for IsPrivate property.
     *
     * @param aIsPrivate New Value for IsPrivate
     *
     */
    public void setIsPrivate(boolean aIsPrivate) {
        myIsPrivate = aIsPrivate;
    }

    /**
     * Mutator method for Location property.
     *
     * @param aLocation New Value for Location
     *
     */
    public void setLocation(String aLocation) {
        myLocation = aLocation;
    }

    /**
     * Mutator method for MaxEmailActions property.
     *
     * @param aMaxEmailActions New Value for MaxEmailActions
     *
     */
    public void setMaxEmailActions(int aMaxEmailActions) {
        myMaxEmailActions = aMaxEmailActions;
    }

    /**
     * Mutator method for MaxRequestId property.
     *
     * @param aMaxRequestId New Value for MaxRequestId
     *
     */
    public void setMaxRequestId(int aMaxRequestId) {
        myMaxRequestId = aMaxRequestId;
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
     * Mutator method for SysConfig property.
     *
     * @param aSysConfig New Value for SysConfig
     *
     */
    public void setSysConfig(String aSysConfig) {
        mySysConfig = aSysConfig;
    }

    /**
     * Mutator method for SysConfigObject property.
     *
     * @param aSysConfigObject New Value for SysConfigObject
     *
     */
    public void setSysConfigObject(SysConfig aSysConfigObject) {
        mySysConfigObject = aSysConfigObject;
        mySysConfig       = mySysConfigObject.xmlSerialize();
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
     * Mutator method for SystemPrefix property.
     *
     * @param aSystemPrefix New Value for SystemPrefix
     *
     */
    public void setSystemPrefix(String aSystemPrefix) {
        mySystemPrefix = aSystemPrefix;
    }

    /**
     * Mutator method for Type property.
     *
     * @param aType New Value for Type
     *
     */
    public void setType(String aType) {
        myType = aType;
    }    
   
}