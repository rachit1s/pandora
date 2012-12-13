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
 * TypeUser.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DataSourcePool;

//Other TBits Imports.
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.User;

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;
import static transbit.tbits.api.Mapper.ourTypeUserListMap;

//Static TBits Imports
import static transbit.tbits.api.Mapper.ourTypeUserMap;

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
 * This class is the domain object corresponding to the types_users table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class TypeUser implements Comparable<TypeUser>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID       = 1;
    private static final int FIELDID        = 2;
    private static final int TYPEID         = 3;
    private static final int USERID         = 4;
    private static final int USERTYPEID     = 5;
    private static final int RRVOLUNTEER    = 8;
    private static final int NOTIFICATIONID = 6;
    private static final int ISVOLUNTEER    = 7;
    private static final int ISACTIVE       = 9;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private int     myFieldId;
    private boolean myIsActive;
    private boolean myIsVolunteer;
    private int     myNotificationId;
    private boolean myRRVolunteer;

    // Attributes of this Domain Object.
    private int mySystemId;
    private int myTypeId;

    // Objects corresponding to the foreign keys in this table.
    private User myUser;
    private int  myUserId;
    private int  myUserTypeId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public TypeUser() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aFieldId
     *  @param aTypeId
     *  @param aUserId
     *  @param aUserTypeId
     *  @param aNotificationId
     *  @param aIsVolunteer
     *  @param aRRVolunteer
     *  @param aIsActive
     */
    public TypeUser(int aSystemId, int aFieldId, int aTypeId, int aUserId, int aUserTypeId, int aNotificationId, boolean aIsVolunteer, boolean aRRVolunteer, boolean aIsActive) {
        mySystemId       = aSystemId;
        myFieldId        = aFieldId;
        myTypeId         = aTypeId;
        myUserId         = aUserId;
        myUserTypeId     = aUserTypeId;
        myNotificationId = aNotificationId;
        myIsVolunteer    = aIsVolunteer;
        myRRVolunteer    = aRRVolunteer;
        myIsActive       = aIsActive;
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
    public int compareTo(TypeUser aObject) {
        switch (ourSortField) {
        case SYSTEMID : {
            Integer i1 = mySystemId;
            Integer i2 = aObject.mySystemId;

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

        case TYPEID : {
            Integer i1 = myTypeId;
            Integer i2 = aObject.myTypeId;

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

        case NOTIFICATIONID : {
            Integer i1 = myNotificationId;
            Integer i2 = aObject.myNotificationId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case ISVOLUNTEER : {
            Boolean b1 = myIsVolunteer;
            Boolean b2 = aObject.myIsVolunteer;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }

        case RRVOLUNTEER : {
            Boolean b1 = myRRVolunteer;
            Boolean b2 = aObject.myRRVolunteer;

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
        }

        return 0;
    }

    /**
     * This method is used to create the TypeUser object from the ResultSet
     *
     * @param  aResultSet the result object containing the Type Users
     *  corresponding to a row of the TypeUsers table in the database
     * @return the corresponding TypeUser object created from the ResutlSet
     */
    public static TypeUser createFromResultSet(ResultSet aResultSet) throws SQLException {
        TypeUser typeUser = new TypeUser(aResultSet.getInt("sys_id"), aResultSet.getInt("field_id"), aResultSet.getInt("type_id"), aResultSet.getInt("user_id"), aResultSet.getInt("user_type_id"),
                                         aResultSet.getInt("notification_id"), aResultSet.getBoolean("is_volunteer"), aResultSet.getBoolean("rr_volunteer"), aResultSet.getBoolean("is_active"));

        return typeUser;
    }

    /**
     * Method to delete the corresponding TypeUser object in the database.
     *
     * @param aObject Object to be updated
     *
     * @return Delete domain object.
     *
     */
    public static TypeUser delete(TypeUser aObject) {

        // Update logic here.
        if (aObject == null) {
            return aObject;
        }

        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();

            CallableStatement cs = aCon.prepareCall("stp_typeuser_delete ?, ?, ?, ?, ?, ?, ?, ?, ?");

            aObject.setCallableParameters(cs);
            cs.execute();
            cs.close();
        } catch (SQLException sqle) {
            LOG.severe("An exception occured while deleting a type_user", sqle);
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
     * This method decides if two TypeUser Objects are same.
     *
     */
    public boolean equals(Object o) {
        TypeUser typeUser = null;

        if (o == null) {
            return false;
        }

        try {
            typeUser = (TypeUser) o;
        } catch (ClassCastException cce) {
            return false;
        }

        boolean result = (typeUser.mySystemId == this.mySystemId) && (typeUser.myFieldId == this.myFieldId) && (typeUser.myTypeId == this.myTypeId) && (typeUser.myUserId == this.myUserId)
                         && (typeUser.myUserTypeId == this.myUserTypeId);

        return result;
    }

    /**
     * This method decides if two TypeUser Objects are same.
     *
     */
    public boolean equals(TypeUser typeUser) {
        if (typeUser == null) {
            return false;
        }

        boolean result = (typeUser.mySystemId == this.mySystemId) && (typeUser.myFieldId == this.myFieldId) && (typeUser.myTypeId == this.myTypeId) && (typeUser.myUserId == this.myUserId)
                         && (typeUser.myUserTypeId == this.myUserTypeId);

        return result;
    }

    /**
     * Method to insert a TypeUser object into database.
     *
     * @param aObject Object to be inserted
     *
     */
    public static boolean insert(TypeUser aObject) {

        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_typeuser_insert ?, ?, ?, ?, ?, ?, ?, ?, ?");

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
     * Method to get the TypeUser Objects for a corresponding
     * TypeId , FieldId and sysId
     *
     * @param  aSystemId  the sysId for the TypeUser Object needed
     * @param  aFieldId the FieldId for which the Typeuser Objects  needed
     * @param  aTypeId the TypeId for which the Typeuser Objects  needed
     * @return ArrayList of the corresponding Typeuser domain objects
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<TypeUser> lookupBySystemIdAndFieldIdAndTypeId(int aSystemId, int aFieldId, int aTypeId) throws DatabaseException {
        ArrayList<TypeUser> tuList = null;

        // First Look in the mapper.
        String key = Integer.toString(aSystemId) + "-" + Integer.toString(aFieldId) + "-" + Integer.toString(aTypeId);

        if (ourTypeUserListMap != null) {
            tuList = ourTypeUserListMap.get(key);

            return tuList;
        }

        // else fetch the records from the database.
        tuList = new ArrayList<TypeUser>();

        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_typeuser_lookupBySystemIdAndFieldIdAndTypeId ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aFieldId);
            cs.setInt(3, aTypeId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next()) {
                    TypeUser tu = (TypeUser) createFromResultSet(rs);

                    tuList.add(tu);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("type users.").append("\nSystem Id  : ").append(aSystemId).append("\nField  Id : ").append(aFieldId).append(
                "\nType   Id : ").append(aTypeId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.warning("Exception occurred while closing the connection.");
            }
        }

        return tuList;
    }

    /**
     * Method to get the TypeUser Objects for a corresponding systemId, fieldId
     * typeId and userId
     *
     * @param aSystemId the sysId for the TypeUser Object needed
     * @param aFieldId  the FieldId for which the Typeuser Objects  needed
     * @param aTypeId   the TypeId for which the Typeuser Objects  needed
     * @param aUserId   the userId for which the typeuser object is needed.
     *
     * @return ArrayList of the corresponding Typeuser domain objects
     * @throws DatabaseException In case of any database related error
     */
    public static TypeUser lookupBySystemIdAndFieldIdAndTypeIdAndUserId(int aSystemId, int aFieldId, int aTypeId, int aUserId) throws DatabaseException {
        TypeUser typeUser = null;

        // First Look in the mapper.
        String key = Integer.toString(aSystemId) + "-" + Integer.toString(aFieldId) + "-" + Integer.toString(aTypeId) + "-" + Integer.toString(aUserId);

        if (ourTypeUserMap != null) {
            typeUser = ourTypeUserMap.get(key);

            return typeUser;
        }

        // else fetch the records from the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_typeuser_lookupBySystemIdAndFieldIdAndTypeIdAndUserId " + " ?, ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aFieldId);
            cs.setInt(3, aTypeId);
            cs.setInt(4, aUserId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                typeUser = createFromResultSet(rs);
                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("type user.").append("\nSystem Id  : ").append(aSystemId).append("\nField  Id : ").append(aFieldId).append(
                "\nType   Id : ").append(aTypeId).append("\nUser   Id : ").append(aUserId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.warning("Exception occurred while closing the connection.");
            }
        }

        return typeUser;
    }

    /**
     * Method to get the TypeUser Objects for a corresponding
     * TypeId , FieldName and System Id.
     *
     * @param  aSystemId  the sysId for the TypeUser Object needed
     * @param  aFieldName the FieldName for which the Typeuser Objects  needed
     * @param  aTypeId the TypeId for which the Typeuser Objects  needed
     *
     * @return ArrayList of the corresponding Typeuser domain objects
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<TypeUser> lookupBySystemIdAndFieldNameAndTypeId(int aSystemId, String aFieldName, int aTypeId) throws DatabaseException {

        // First Look in the mapper for the field.
        Field field = Field.lookupBySystemIdAndFieldName(aSystemId, aFieldName);

        if (field != null) {
            return lookupBySystemIdAndFieldIdAndTypeId(aSystemId, field.getFieldId(), aTypeId);
        } else {
            return null;
        }
    }

    /**
     * Method to get the TypeUser Objects for a corresponding System Id,
     * fieldName, typeId and userId.
     *
     * @param  aSystemId  the sysId for the TypeUser Object needed
     * @param  aFieldName the FieldName for which the Typeuser Objects  needed
     * @param  aTypeId the TypeId for which the Typeuser Objects  needed
     * @param aUserId   the userId for which the typeuser object is needed.
     *
     * @return ArrayList of the corresponding Typeuser domain objects
     * @throws DatabaseException In case of any database related error
     */
    public static TypeUser lookupBySystemIdAndFieldNameAndTypeIdAndUserId(int aSystemId, String aFieldName, int aTypeId, int aUserId) throws DatabaseException {

        // First Look in the mapper for the field.
        Field field = Field.lookupBySystemIdAndFieldName(aSystemId, aFieldName);

        if (field != null) {
            return lookupBySystemIdAndFieldIdAndTypeIdAndUserId(aSystemId, field.getFieldId(), aTypeId, aUserId);
        } else {
            return null;
        }
    }

    /**
     * Method to get the TypeUser Objects for a corresponding
     * TypeId , FieldId and sysId
     *
     * @param  aSystemId  the sysId for the TypeUser Object needed
     * @param  aUserId the FieldId for which the Typeuser object needed
     * @return ArrayList of the corresponding Typeuser domain objects
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<TypeUser> lookupTypeUsersBySystemIdAndUserId(int aSystemId, int aUserId) throws DatabaseException {
        ArrayList<TypeUser> tuList = null;

        // First Look in the mapper.
        String key = Integer.toString(aSystemId) + "-3-" + Integer.toString(aUserId) + "-USER";

        if (ourTypeUserListMap != null) {
            tuList = ourTypeUserListMap.get(key);

            if (tuList == null) {
                return new ArrayList<TypeUser>();
            } else {
                return tuList;
            }
        }

        // else fetch the records from the database.
        tuList = new ArrayList<TypeUser>();

        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_admin_getUserCategoriesForBA" + " ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aUserId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next()) {
                    TypeUser tu = (TypeUser) createFromResultSet(rs);

                    tuList.add(tu);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("type users.").append("\nSystem Id  : ").append(aSystemId).append("\nUser   Id : ").append(aUserId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.warning("Exception occurred while closing the connection.");
            }
        }

        return tuList;
    }

    /**
     * Method to get the Volunteers TypeUser Objects  for a corresponding
     * TypeId , FieldId and sysId
     *
     * @param  aSystemId  the sysId for the TypeUser Object needed
     * @param  aFieldId the FieldId for which the Typeuser Objects  needed
     * @param  aTypeId the TypeId for which the Typeuser Objects  needed
     * @return ArrayList of the corresponding Typeuser domain objects
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<TypeUser> lookupVolunteersBySystemIdAndFieldIdAndTypeId(int aSystemId, int aFieldId, int aTypeId) throws DatabaseException {
        ArrayList<TypeUser> tuList = null;

        tuList = new ArrayList<TypeUser>();

        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_typeuser_lookupVolunteersBySystemIdAndFieldIdAndTypeId " + " ?,?,?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aFieldId);
            cs.setInt(3, aTypeId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next()) {
                    TypeUser tu = (TypeUser) createFromResultSet(rs);

                    tuList.add(tu);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("type users.").append("\nSystem Id  : ").append(aSystemId).append("\nField  Id : ").append(aFieldId).append(
                "\nType   Id : ").append(aTypeId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.warning("Exception occurred while closing the connection.");
            }
        }

        return tuList;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the TypeUser objects in sorted order
     */
    public static ArrayList<TypeUser> sort(ArrayList<TypeUser> source) {
        int        size     = source.size();
        TypeUser[] srcArray = new TypeUser[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new TypeUserComparator());

        ArrayList<TypeUser> target = new ArrayList<TypeUser>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    /**
     * Method to update the corresponding TypeUser object in the database.
     *
     * @param aObject Object to be updated
     *
     * @return Update domain object.
     *
     */
    public static TypeUser update(TypeUser aObject) {

        // Update logic here.
        if (aObject == null) {
            return aObject;
        }

        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_typeuser_update ?, ?, ?, ?, ?, ?, ?, ?, ?");

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
            LOG.severe("An exception occured while updating a type_user", sqle);
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
     * Method to update the corresponding TypeUser object in the database.
     *
     * @param aSystemId Business Area Id.
     * @param aFieldId  Id of the field.
     * @param aTypeId   Id of the type.
     * @param aUserId   Id of the user.
     *
     * @exception DatabaseException Incase of any database errors.
     */
    public static void updateNextVolunteer(int aSystemId, int aFieldId, int aTypeId, int aUserId) throws DatabaseException {
        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_tu_updateNextVolunteer ?, ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aFieldId);
            cs.setInt(3, aTypeId);
            cs.setInt(4, aUserId);
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
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while updating the ").append("next volunteer.").append("\nSystem Id  : ").append(aSystemId).append("\nField  Id : ").append(aFieldId).append(
                "\nType   Id : ").append(aTypeId).append("\nUser   Id : ").append(aUserId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {
                    LOG.warn("",(sqle));
                }
            }
        }
    }

    //~--- get methods --------------------------------------------------------

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
     * Method to get the Global TypeUser Objects with TypeId 0.
     *
     * @param  aSystemId  the sysId for the TypeUser Object needed
     *
     * @return ArrayList of the corresponding Typeuser domain objects
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<TypeUser> getGlobalListBySystemId(int aSystemId) throws DatabaseException {
        ArrayList<TypeUser> globalList = lookupBySystemIdAndFieldNameAndTypeId(aSystemId, Field.CATEGORY, 0);

        return globalList;
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
     * Accessor method for IsVolunteer property.
     *
     * @return Current Value of IsVolunteer
     *
     */
    public boolean getIsVolunteer() {
        return myIsVolunteer;
    }

    /**
     * Accessor method for NotificationId property.
     *
     * @return Current Value of NotificationId
     *
     */
    public int getNotificationId() {
        return myNotificationId;
    }

    /**
     * Accessor method for RRVolunteer property.
     *
     * @return Current Value of RRVolunteer
     *
     */
    public boolean getRRVolunteer() {
        return myRRVolunteer;
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
     * Accessor method for TypeId property.
     *
     * @return Current Value of TypeId
     *
     */
    public int getTypeId() {
        return myTypeId;
    }

    /**
     * Accessor method for UserId property.
     *
     * @return Current Value of UserId
     *
     */
    public int getUserId() throws DatabaseException {
        return myUserId;
    }
    
    public User getUser() throws DatabaseException{
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
        aCS.setInt(FIELDID, myFieldId);
        aCS.setInt(TYPEID, myTypeId);
        aCS.setInt(USERID, myUserId);
        aCS.setInt(USERTYPEID, myUserTypeId);
        aCS.setInt(NOTIFICATIONID, myNotificationId);
        aCS.setBoolean(ISVOLUNTEER, myIsVolunteer);
        aCS.setBoolean(RRVOLUNTEER, myRRVolunteer);
        aCS.setBoolean(ISACTIVE, myIsActive);
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
     * Mutator method for IsActive property.
     *
     * @param aIsActive New Value for IsActive
     *
     */
    public void setIsActive(boolean aIsActive) {
        myIsActive = aIsActive;
    }

    /**
     * Mutator method for IsVolunteer property.
     *
     * @param aIsVolunteer New Value for IsVolunteer
     *
     */
    public void setIsVolunteer(boolean aIsVolunteer) {
        myIsVolunteer = aIsVolunteer;
    }

    /**
     * Mutator method for NotificationId property.
     *
     * @param aNotificationId New Value for NotificationId
     *
     */
    public void setNotificationId(int aNotificationId) {
        myNotificationId = aNotificationId;
    }

    /**
     * Mutator method for RRVolunteer property.
     *
     * @param aRRVolunteer New Value for RRVolunteer
     *
     */
    public void setRRVolunteer(boolean aRRVolunteer) {
        myRRVolunteer = aRRVolunteer;
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
     * Mutator method for TypeId property.
     *
     * @param aTypeId New Value for TypeId
     *
     */
    public void setTypeId(int aTypeId) {
        myTypeId = aTypeId;
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
 * types_users table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class TypeUserComparator implements Comparator<TypeUser>, Serializable {
    public int compare(TypeUser obj1, TypeUser obj2) {
        return obj1.compareTo(obj2);
    }
}
