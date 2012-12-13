/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/**
 * Type.java
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
import transbit.tbits.domain.Field;

import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

//Static imports from mapper.
import static transbit.tbits.api.Mapper.ourTypeIdMap;
import static transbit.tbits.api.Mapper.ourTypeListMap;
import static transbit.tbits.api.Mapper.ourTypeNameMap;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;

import static java.sql.Types.INTEGER;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the types table in the
 * database.
 *
 * @author : Vinod Gupta, Vaibhav, g
 * @version : $Id: $
 *
 */
public class Type implements Comparable<Type>, Serializable{
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    public static final int                   SYSTEMID    = 1;
    public static final int                   FIELDID     = 2;
    public static final int                   TYPEID      = 3;
    public static final int                   ORDERING    = 7;
    public static final int                   NAME        = 4;
    public static final int                   ISPRIVATE   = 11;
    public static final int                   ISFINAL     = 12;
    public static final int                   ISDEFAULT   = 9;
    public static final int                   ISCHECKED   = 10;
    public static final int                   ISACTIVE    = 8;
    public static final int                   DISPLAYNAME = 5;
    public static final int                   DESCRIPTION = 6;
    private static Hashtable<String, Integer> ourDataTypeMap;

    // Static attributes related to sorting.
    public static int ourSortField;
    public static int ourSortOrder;

    //~--- static initializers ------------------------------------------------

    static {
        ourDataTypeMap = new Hashtable<String, Integer>();
        ourDataTypeMap.put("sys_id", DataType.INT);
        ourDataTypeMap.put("field_id", DataType.INT);
        ourDataTypeMap.put("type_id", DataType.INT);
        ourDataTypeMap.put("name", DataType.STRING);
        ourDataTypeMap.put("display_name", DataType.STRING);
        ourDataTypeMap.put("description", DataType.STRING);
        ourDataTypeMap.put("ordering", DataType.INT);
        ourDataTypeMap.put("is_active", DataType.BOOLEAN);
        ourDataTypeMap.put("is_default", DataType.BOOLEAN);
        ourDataTypeMap.put("is_checked", DataType.BOOLEAN);
        ourDataTypeMap.put("is_private", DataType.BOOLEAN);
        ourDataTypeMap.put("is_final", DataType.BOOLEAN);
    }

    //~--- fields -------------------------------------------------------------

    private String  myDescription;
    private String  myDisplayName;
    private int     myFieldId;
    private boolean myIsActive;
    private boolean myIsChecked;
    private boolean myIsDefault;
    private boolean myIsFinal;
    private boolean myIsPrivate;
    private String  myName;
    private int     myOrdering;

    // Attributes of this Domain Object.
    private int                       mySystemId;
    private int                       myTypeId;
    private Hashtable<String, Object> myValueMap;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public Type() {
        myValueMap = new Hashtable<String, Object>();
        setSystemId(0);
        setFieldId(0);
        setTypeId(0);
        setName("");
        setDisplayName("");
        setDescription("");
        setOrdering(0);
        setIsActive(true);
        setIsDefault(false);
        setIsChecked(true);
        setIsPrivate(false);
        setIsFinal(false);
    }

    /**
     * The complete constructor.
     *
     * @param aSystemId
     * @param aFieldId
     * @param aTypeId
     * @param aName
     * @param aDisplayName
     * @param aDescription
     * @param aOrdering
     * @param aIsActive
     * @param aIsDefault
     * @param aIsChecked
     * @param aIsPrivate
     * @param aIsFinal
     */
    public Type(int aSystemId, int aFieldId, int aTypeId, String aName, String aDisplayName, String aDescription, int aOrdering, boolean aIsActive, boolean aIsDefault, boolean aIsChecked,
                boolean aIsPrivate, boolean aIsFinal) {
        myValueMap = new Hashtable<String, Object>();
        setSystemId(aSystemId);
        setFieldId(aFieldId);
        setTypeId(aTypeId);
        setName(aName);
        setDisplayName(aDisplayName);
        setDescription(aDescription);
        setOrdering(aOrdering);
        setIsActive(aIsActive);
        setIsDefault(aIsDefault);
        setIsChecked(aIsChecked);
        setIsPrivate(aIsPrivate);
        setIsFinal(aIsFinal);
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method that compares this object with the one passed W.R.T ourSortField.
     *
     * @param aObject
     *            Object to be compared.
     *
     * @return 0 - If they are equal. 1 - If this is greater. -1 - If this is
     *         smaller.
     *
     */
    public int compareTo(Type aObject) {
        switch (ourSortField) {
        case SYSTEMID : {
            Integer i1 = mySystemId;
            Integer i2 = aObject.mySystemId;

            return i1.compareTo(i2);
        }

        case FIELDID : {
            Integer i1 = myFieldId;
            Integer i2 = aObject.myFieldId;

            return i1.compareTo(i2);
        }

        case TYPEID : {
            Integer i1 = myTypeId;
            Integer i2 = aObject.myTypeId;

            return i1.compareTo(i2);
        }

        case NAME : {
            return myName.compareToIgnoreCase(aObject.myName);
        }

        case DISPLAYNAME : {
            return myDisplayName.compareToIgnoreCase(aObject.myDisplayName);
        }

        case DESCRIPTION : {
            return myDescription.compareToIgnoreCase(aObject.myDescription);
        }

        case ORDERING : {
            Integer i1 = myOrdering;
            Integer i2 = aObject.myOrdering;

            return i1.compareTo(i2);
        }

        case ISACTIVE : {
            Boolean b1 = myIsActive;
            Boolean b2 = aObject.myIsActive;

            return b1.compareTo(b2);
        }

        case ISDEFAULT : {
            Boolean b1 = myIsDefault;
            Boolean b2 = aObject.myIsDefault;

            return b1.compareTo(b2);
        }

        case ISCHECKED : {
            Boolean b1 = myIsChecked;
            Boolean b2 = aObject.myIsChecked;

            return b1.compareTo(b2);
        }

        case ISPRIVATE : {
            Boolean b1 = myIsPrivate;
            Boolean b2 = aObject.myIsPrivate;

            return b1.compareTo(b2);
        }

        case ISFINAL : {
            Boolean b1 = myIsFinal;
            Boolean b2 = aObject.myIsFinal;

            return b1.compareTo(b2);
        }
        }

        return 0;
    }

    /**
     * This method is used to create the Type object from the ResultSet
     *
     * @param aResultSet
     *            the result object containing the fields corresponding to a row
     *            of the Type table in the database
     * @return the corresponding Type object created from the ResutlSet
     */
    public static Type createFromResultSet(ResultSet aResultSet) throws SQLException {
        Type type = new Type(aResultSet.getInt("sys_id"), aResultSet.getInt("field_id"), aResultSet.getInt("type_id"), aResultSet.getString("name"), aResultSet.getString("display_name"),
                             aResultSet.getString("description"), aResultSet.getInt("ordering"), aResultSet.getBoolean("is_active"), aResultSet.getBoolean("is_default"),
                             aResultSet.getBoolean("is_checked"), aResultSet.getBoolean("is_private"), aResultSet.getBoolean("is_final"));

        return type;
    }

    /**
     * Method to delete the corresponding Type object in the database.
     *
     * @param aObject
     *            Object to be deleted
     *
     * @return Delete domain object.
     *
     */
    public static Type delete(Type aObject) {

        // Update logic here.
        if (aObject == null) {
            return aObject;
        }

        Connection aCon        = null;
        int        returnValue = 0;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_type_delete ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");

            cs.registerOutParameter(13, INTEGER);
            aObject.setCallableParameters(cs);
            cs.execute();
            returnValue = cs.getInt(13);
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
            LOG.severe("An exception occured while delete type.", sqle);
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
     * This method return true if the object passed is equal this.
     *
     * @param aObject
     *            Object to be checked.
     *
     * @return True if equal, False otherwise.
     *
     */
    public boolean equals(Object aObject) {
        if (aObject == null) {
            return false;
        }

        Type type = null;

        try {
            type = (Type) aObject;
        } catch (ClassCastException cce) {
            return false;
        }

        if ((this.mySystemId == type.mySystemId) && (this.myFieldId == type.myFieldId) && (this.myTypeId == type.myTypeId)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method return true if the object passed is equal this.
     *
     * @param aObject
     *            Object to be checked.
     *
     * @return True if equal, False otherwise.
     *
     */
    public boolean equals(Type aObject) {
        if (aObject == null) {
            return false;
        }

        if ((this.mySystemId == aObject.mySystemId) && (this.myFieldId == aObject.myFieldId) && (this.myTypeId == aObject.myTypeId)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method to insert a Type object into database.
     *
     * @param aObject
     *            Object to be inserted
     *
     */
    public static Type insert(Type aObject) {

        // Insert logic here.
        if (aObject == null) {
            return null;
        }

        Connection aCon        = null;
        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_type_insert ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");

            aObject.setCallableParameters(cs);
            cs.registerOutParameter(TYPEID, Types.INTEGER);
            cs.execute();
            aObject.setTypeId(cs.getInt(TYPEID));
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
            // returnValue = false;
            LOG.severe("An exception occured while inserting type", sqle);
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
     * Method to get the All the Types for a corresponding BusinessArea.
     *
     * @param  aSystemId  the id of the business area for which the Types are
     *                    to be obtained.
     * @param  aFieldName the Field for which the Types are to be found out
     *
     * @return the arraylist containing the corresponding Type domain objects
     *
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<Type> lookupAllBySystemIdAndFieldName(int aSystemId, String aFieldName) throws DatabaseException {
        ArrayList<Type> typesList = new ArrayList<Type>();

        if (aFieldName == null) {
            return typesList;
        }

        aFieldName = aFieldName.trim();

        // Get the field object corresponding to the field name.
        Field field = Field.lookupBySystemIdAndFieldName(aSystemId, aFieldName);

        // If the field is invalid, return an empty list.
        if (field == null) {
            return typesList;
        }

        // form the key.
        String key = aSystemId + "-" + field.getFieldId();

        if (ourTypeListMap != null) {
            ArrayList<Type> tempList = ourTypeListMap.get(key);

            Type.setSortParams(DISPLAYNAME, 0);

            if (tempList != null) {
                tempList = Type.sort(tempList);
            }

            Type.setSortParams(ORDERING, 0);

            if (tempList != null) {
                tempList = Type.sort(tempList);

                for (Type type : tempList) {
                    typesList.add(type);
                }
            }

            return typesList;
        }

        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_type_lookupAllBySystemIdAndFieldName ?, ?");

            cs.setInt(1, aSystemId);
            cs.setString(2, aFieldName);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next()) {
                    Type type = (Type) createFromResultSet(rs);

                    typesList.add(type);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the types.").append("\nSystem Id  : ").append(aSystemId).append("\nField Name : ").append(aFieldName).append("\n");

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

        return typesList;
    }

    /**
     * Method to lookup the Type table by the sys id, field name and type name
     *
     * @param aSystemId
     *            the SysId by which the table has to be looked up
     * @param aFieldName
     *            the fieldname by which the table has to be looked up
     * @param aTypeName
     *            the typename by which the table has to be looked up
     *
     * @return the Type object associated with the typename
     *
     * @throws DatabaseException
     *             In case of any database related error
     */
    public static Type lookupAllBySystemIdAndFieldNameAndTypeName(int aSystemId, String aFieldName, String aTypeName) throws DatabaseException {
        Type type = null;

        if (aFieldName == null) {
            return type;
        }

        if (aTypeName == null) {
            return type;
        }

        aFieldName = aFieldName.trim();
        aTypeName  = aTypeName.trim();

        // Get the field object corresponding to the field name.
        Field field = Field.lookupBySystemIdAndFieldName(aSystemId, aFieldName);

        if (field == null) {
            return null;
        }

        // Form the key <SystemId>-<FieldId>-<TypeName>;
        String key = aSystemId + "-" + field.getFieldId() + "-" + aTypeName.toUpperCase();

        // Look up in the Type Name mapper first if it is not null.
        if (ourTypeNameMap != null) {
            type = ourTypeNameMap.get(key);

            return type;
        }

        // else we should try to get the record from the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_type_lookupBySystemIdAndFieldNameAndTypeName ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setString(2, aFieldName);
            cs.setString(3, aTypeName);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    type = (Type) createFromResultSet(rs);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the type.").append("\nSystem Id  : ").append(aSystemId).append("\nField Name : ").append(aFieldName).append(
                "\nType Name  : ").append(aTypeName).append("\n");

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

        return type;
    }

    /**
     * Method to lookup the Type table by the type id,field id and sys id
     *
     * @param aSystemId
     *            the SysId by which the table has to be looked up
     * @param aFieldId
     *            the fieldId by which the table has to be looked up
     * @param aTypeId
     *            the typeId by which the table has to be looked up
     *
     * @return the Type object associated with the typeID
     *
     * @throws DatabaseException
     *             In case of any database related error
     */
    public static Type lookupBySystemIdAndFieldIdAndTypeId(int aSystemId, int aFieldId, int aTypeId) throws DatabaseException {
        Type type = null;

        // Look in the Type ID mapper first.
        String key = aSystemId + "-" + aFieldId + "-" + aTypeId;

        if (ourTypeIdMap != null) {
            type = ourTypeIdMap.get(key);

            if (type != null) {

                // Return the type if it is active. Otherwise return null.
                if (type.getIsActive() == true) {

                    // LOG.info("Returning: " + type.toString());
                    return type;
                } else {

                    // LOG.info("Type is not active: " + key);
                    return null;
                }
            } else {

                // LOG.info("Type not found in the mapper: " + key);
                return null;
            }
        }

        // else we should try to get the record from the database.
        // LOG.info("Going to the database: " + key);
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_type_lookupBySystemIdAndFieldIdAndTypeId ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aFieldId);
            cs.setInt(3, aTypeId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    type = (Type) createFromResultSet(rs);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the type.").append("\nSystem Id : ").append(aSystemId).append("\nField Id  : ").append(aFieldId).append("\nType Id   : ").append(
                aTypeId).append("\n");

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

        return type;
    }

    /**
     * Method to lookup the Type table by the sys id, field Id and type name
     *
     * @param aSystemId
     *            the SysId by which the table has to be looked up
     * @param aFieldId
     *            the fieldId by which the table has to be looked up
     * @param aTypeName
     *            the typename by which the table has to be looked up
     *
     * @return the Type object associated with the typename
     *
     * @throws DatabaseException
     *             In case of any database related error
     */
    public static Type lookupBySystemIdAndFieldIdAndTypeName(int aSystemId, int aFieldId, String aTypeName) throws DatabaseException {
        Type type = null;

        if (aTypeName == null) {
            return type;
        }

        aTypeName = aTypeName.trim();

        // Form the key <SystemId>-<FieldId>-<TypeName>;
        String key = aSystemId + "-" + aFieldId + "-" + aTypeName.toUpperCase();

        // Look up in the Type Name mapper first if it is not null.
        if (ourTypeNameMap != null) {
            type = ourTypeNameMap.get(key);

            // Check if the value is not null.
            if (type != null) {

                // Return the type if it is active. Otherwise return null.
                if (type.getIsActive() == true) {

                    // LOG.info("Returning: " + type.toString());
                    return type;
                } else {

                    // LOG.info("Inactive Type: " + aTypeName);
                    return null;
                }
            } else {

                // LOG.info("Type object not found for key: " + key);
                return null;
            }
        }

        // else we should try to get the record from the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_type_lookupBySystemIdAndFieldIdAndTypeName ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aFieldId);
            cs.setString(3, aTypeName);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    type = (Type) createFromResultSet(rs);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the type.").append("\nSystem Id  : ").append(aSystemId).append("\nField Id : ").append(aFieldId).append("\nType Name  : ").append(
                aTypeName).append("\n");

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

        return type;
    }

    /**
     * Method to get the Types for a corresponding BusinessArea.
     *
     * @param aSystemId
     *            the id of the business area for which the Types are to be
     *            obtained.
     * @param aFieldName
     *            the Field for which the Types are to be found out
     *
     * @return the arraylist containing the corresponding Type domain objects
     *
     * @throws DatabaseException
     *             In case of any database related error
     */
    public static ArrayList<Type> lookupBySystemIdAndFieldName(int aSystemId, String aFieldName) throws DatabaseException {
        ArrayList<Type> typesList = new ArrayList<Type>();

        if (aFieldName == null) {
            return typesList;
        }

        aFieldName = aFieldName.trim();

        // Get the field object corresponding to the field name.
        Field field = Field.lookupBySystemIdAndFieldName(aSystemId, aFieldName);

        // If the field is invalid, return an empty list.
        if (field == null) {
            return typesList;
        }

        // form the key.
        String key = aSystemId + "-" + field.getFieldId();

        if (ourTypeListMap != null) {
            ArrayList<Type> tempList = ourTypeListMap.get(key);

            Type.setSortParams(DISPLAYNAME, 0);

            if (tempList != null) {
                tempList = Type.sort(tempList);
            }

            Type.setSortParams(ORDERING, 0);

            if (tempList != null) {
                tempList = Type.sort(tempList);

                for (Type type : tempList) {
                    if (type.getIsActive() == true) {
                        typesList.add(type);
                    }
                }
            }

            return typesList;
        }

        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_type_lookupBySystemIdAndFieldName ?, ?");

            cs.setInt(1, aSystemId);
            cs.setString(2, aFieldName);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next()) {
                    Type type = (Type) createFromResultSet(rs);

                    typesList.add(type);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the types.").append("\nSystem Id  : ").append(aSystemId).append("\nField Name : ").append(aFieldName).append("\n");

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

        return typesList;
    }

    /**
     * Method to lookup the Type table by the type id,field name and sys id
     *
     * @param aSystemId
     *            the SysId by which the table has to be looked up
     * @param aFieldName
     *            the fieldname by which the table has to be looked up
     * @param aTypeId
     *            the typeId by which the table has to be looked up
     *
     * @return the Type object associated with the typeid
     *
     * @throws DatabaseException
     *             In case of any database related error
     */
    public static Type lookupBySystemIdAndFieldNameAndTypeId(int aSystemId, String aFieldName, int aTypeId) throws DatabaseException {
        Type type = null;

        if (aFieldName == null) {
            return type;
        }

        aFieldName = aFieldName.trim();

        // Looks into the Mapper to retrieve the object. If it is not found in
        // the Mapper, it calls the stored procedure and constructs the object
        Field field = Field.lookupBySystemIdAndFieldName(aSystemId, aFieldName);

        if (field == null) {
            return null;
        }

        return lookupBySystemIdAndFieldIdAndTypeId(aSystemId, field.getFieldId(), aTypeId);
    }

    /**
     * Method to lookup the Type table by the sys id, field name and type name
     *
     * @param aSystemId
     *            the SysId by which the table has to be looked up
     * @param aFieldName
     *            the fieldname by which the table has to be looked up
     * @param aTypeName
     *            the typename by which the table has to be looked up
     *
     * @return the Type object associated with the typename
     *
     * @throws DatabaseException
     *             In case of any database related error
     */
    public static Type lookupBySystemIdAndFieldNameAndTypeName(int aSystemId, String aFieldName, String aTypeName) throws DatabaseException {
        Type type = null;

        if (aFieldName == null) {
            return type;
        }

        if (aTypeName == null) {
            return type;
        }

        aFieldName = aFieldName.trim();
        aTypeName  = aTypeName.trim();

        // Get the field object corresponding to the field name.
        Field field = Field.lookupBySystemIdAndFieldName(aSystemId, aFieldName);

        if (field == null) {

            // LOG.info("Invalid field name: " + aFieldName);
            return null;
        }

        // Form the key <SystemId>-<FieldId>-<TypeName>;
        String key = aSystemId + "-" + field.getFieldId() + "-" + aTypeName.toUpperCase();

        // Look up in the Type Name mapper first if it is not null.
        if (ourTypeNameMap != null) {
            type = ourTypeNameMap.get(key);

            // Check if the value is not null.
            if (type != null) {

                // Return the type if it is active. Otherwise return null.
                if (type.getIsActive() == true) {

                    // LOG.info("Returning: " + type.toString());
                    return type;
                } else {

                    // LOG.info("Inactive Type: " + aTypeName);
                    return null;
                }
            } else {

                // LOG.info("Type object not found for key: " + key);
                return null;
            }
        }

        // else we should try to get the record from the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_type_lookupBySystemIdAndFieldNameAndTypeName ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setString(2, aFieldName);
            cs.setString(3, aTypeName);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    type = (Type) createFromResultSet(rs);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the type.").append("\nSystem Id  : ").append(aSystemId).append("\nField Name : ").append(aFieldName).append(
                "\nType Name  : ").append(aTypeName).append("\n");

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

        return type;
    }

    /**
     * This method returns the type object minimal and uniquely matching to the
     * given SystemId and TypeName.
     *
     * @param aSystemId  BusinessArea Id.
     * @param aName   Type Name.
     *
     * @return Type object.
     *
     * @exception DatabaseException incase of any database error.
     */
    public static Type lookupBySystemIdAndMinimalMatch(int aSystemId, String aName) throws DatabaseException {
        aName = aName.toLowerCase();

        Type                    type         = null;
        int                     i            = 0;
        ArrayList<Type>         matchedTypes = new ArrayList<Type>();
        ArrayList<String>       duplicates   = new ArrayList<String>();
        Hashtable<String, Type> typesTable   = TypeDescriptor.getDescriptorTable(aSystemId, duplicates);

        // return null if duplicate (not unique) descriptor
        if (duplicates.contains(aName) == true) {
            return null;
        }

        // look up by descriptor
        type = typesTable.get(aName);

        if (type != null) {
            return type;
        }

        //
        // Match type names irrespective of _, ,-
        //
        if (type == null) {
            matchedTypes = new ArrayList<Type>();
            i            = 0;

            String              name = aName.replaceAll("-", " ").replaceAll("_", " ");
            Enumeration<String> keys = typesTable.keys();

            while (keys.hasMoreElements()) {
                if (i > 1) {
                    return null;
                }

                String str = keys.nextElement().toLowerCase().replaceAll("-", " ").replaceAll("_", " ");

                if (str.startsWith(name) == true) {
                    type = typesTable.get(str);

                    if (matchedTypes.contains(type) == false) {
                        i++;
                        matchedTypes.add(type);
                    }
                }
            }
        }

        //
        // For minimal match descriptor should be atleast 3 char long
        //
        if (aName.length() < 3) {
            return null;
        }

        Enumeration<String> keys = typesTable.keys();

        while (keys.hasMoreElements()) {
            if (i > 1) {
                return null;
            }

            String str = keys.nextElement().toLowerCase();

            if (str.startsWith(aName) == true) {
                type = typesTable.get(str);

                if (matchedTypes.contains(type) == false) {
                    i++;
                    matchedTypes.add(type);
                }
            }
        }

        return type;
    }

    /**
     * Main method for testing.
     *
     * @param args Commandline arguments.
     */
    public static void main(String[] args) throws Exception {
        LOG.info(Type.getStatusAndSeverities("2,3").toString());
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param source
     *            the array list of Type objects
     * @return the ArrayList of the Type objects in sorted order
     */
    public static ArrayList<Type> sort(ArrayList<Type> source) {
        int    size     = source.size();
        Type[] srcArray = new Type[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new TypeComparator());

        ArrayList<Type> target = new ArrayList<Type>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    /**
     * This method returns the String Representation of object of this class.
     *
     * @return String representation.
     */
    public String toString() {
        StringBuilder message = new StringBuilder();

        message.append("[ ").append(myTypeId).append(", ").append(myName).append(", ").append(myDisplayName).append(" ]");

        return message.toString();
    }

    /**
     * Method to update the corresponding Type object in the database.
     *
     * @param aObject
     *            Object to be updated
     *
     * @return Update domain object.
     *
     */
    public static Type update(Type aObject) {

        // Update logic here.
        if (aObject == null) {
            return aObject;
        }

        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_type_update ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");

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
            LOG.severe("An exception occured while updating type.", sqle);
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

    /*
     *
     * Generic Set andGet methods
     *
     */

    /**
     * Generic Accessor method for any field in this object.
     *
     * @return Value of the corresponding column.
     */
    public Object get(String aColumnName) {
        return myValueMap.get(aColumnName);
    }

    /**
     * Method to get the default type for a given field id.
     *
     * @param aSystemId
     *            the SysId by which the table has to be looked up
     * @param aFieldId
     *            the fieldId by which the table has to be looked up
     *
     * @return the Type object associated with the typeID
     *
     * @throws DatabaseException
     *             In case of any database related error
     */
    public static Type getDefaultTypeBySystemIdAndFieldId(int aSystemId, int aFieldId) throws DatabaseException {
        Type type = null;

        // Look in the mapper first.
        String key = aSystemId + "-" + aFieldId + "-DEFAULT";

        /*
         * Default types are maintained in both Type ID and TypeName maps.
         * So we can lookup in any of the two maps.
         */
        if (ourTypeNameMap != null) {
            type = ourTypeNameMap.get(key);

            if (type != null) {

                // Return the type if it is active. Otherwise return null.
                if (type.getIsActive() == true) {

                    // LOG.info("Returning: " + type.toString());
                    return type;
                } else {

                    // LOG.info("Inactive type: " + key);
                    return null;
                }
            } else {

                // LOG.info("Key not found: " + key);
                return null;
            }
        }

        // else we should try to get the record from the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_type_getDefaultTypeBySystemIdAndFieldId ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aFieldId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    type = (Type) createFromResultSet(rs);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the default").append(" type.").append("\nSystem Id : ").append(aSystemId).append("\nField Id  : ").append(aFieldId).append("\n");

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

        return type;
    }

    /**
     * Method to get the default type for this field.
     *
     * @param aSystemId
     *            the SysId by which the table has to be looked up
     * @param aFieldName
     *            the fieldname by which the table has to be looked up
     *
     * @return the Type object associated with the typename
     *
     * @throws DatabaseException
     *             In case of any database related error
     */
    public static Type getDefaultTypeBySystemIdAndFieldName(int aSystemId, String aFieldName) throws DatabaseException {
        Type type = null;

        if (aFieldName == null) {
            return type;
        }

        aFieldName = aFieldName.trim();

        // Get the field object corresponding to the field name.
        Field field = Field.lookupBySystemIdAndFieldName(aSystemId, aFieldName);

        if (field == null) {
            return null;
        }

        /*
         * Now that we have the field id call the
         * getDefaultTypeBySystemIdandFieldId method to get the default type.
         */
        return getDefaultTypeBySystemIdAndFieldId(aSystemId, field.getFieldId());
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
     * Accessor method for FieldId property.
     *
     * @return Current Value of FieldId
     *
     */
    public int getFieldId() {
        return myFieldId;
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
     * Accessor method for IsChecked property.
     *
     * @return Current Value of IsChecked
     *
     */
    public boolean getIsChecked() {
        return myIsChecked;
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
     * Accessor method for IsFinal property.
     *
     * @return Current Value of IsFinal
     *
     */
    public boolean getIsFinal() {
        return myIsFinal;
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
     * Accessor method for Name property.
     *
     * @return Current Value of Name
     *
     */
    public String getName() {
        return myName;
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
     * This method retrieves the distinct status and severities from the
     * business areas whose sys_id is present in the given comma-separated
     * list. The underlying stored procedure filters out the private types
     * irrespective of if the user has permissions.
     *
     * @param aSysIdList  List of BA ids.
     *
     * @return Table of status and severities.
     *
     * @exception DatabaseException incase of any database related errors.
     */
    public static Hashtable<String, ArrayList<String>> getStatusAndSeverities(String aSysIdList) throws DatabaseException {
        Connection                           con   = null;
        Hashtable<String, ArrayList<String>> table = new Hashtable<String, ArrayList<String>>();

        try {
            con = DataSourcePool.getConnection();

            CallableStatement cs = con.prepareCall("stp_type_getDistinctStatusSeverities ?");

            cs.setString(1, aSysIdList);

            boolean flag = cs.execute();

            if (flag != false) {
                ResultSet rsStatus = cs.getResultSet();

                if (rsStatus != null) {
                    ArrayList<String> statusList = new ArrayList<String>();

                    while (rsStatus.next() != false) {
                        statusList.add(rsStatus.getString("display_name"));
                    }

                    rsStatus.close();
                    rsStatus = null;
                    table.put(Field.STATUS, statusList);
                }

                flag = cs.getMoreResults();

                if (flag != false) {
                    ResultSet rsSeverity = cs.getResultSet();

                    if (rsSeverity != null) {
                        ArrayList<String> sevList = new ArrayList<String>();

                        while (rsSeverity.next() != false) {
                            sevList.add(rsSeverity.getString("display_name"));
                        }

                        rsSeverity.close();
                        rsSeverity = null;
                        table.put(Field.SEVERITY, sevList);
                    }
                }
            }

            cs.close();
            cs = null;
        } catch (SQLException sqle) {}
        finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException sqle) {
                LOG.warning("Exception occurred while closing the connection.");
            }
        }

        return table;
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

    //~--- set methods --------------------------------------------------------

    /**
     * Generic Mutator method for any field in this object.
     *
     * @param aColumnName
     *            Name of the field.
     * @param aValue
     *            Value to be assigned to this field.
     */
    public void set(String aColumnName, String aValue) {
        if (aValue == null) {
            return;
        }

        Integer temp = ourDataTypeMap.get(aColumnName);

        if (temp == null) {
            return;
        }

        int dataType = temp.intValue();

        switch (dataType) {
        case DataType.BOOLEAN : {
            boolean value = false;

            if (aValue.toString().equals("true") || aValue.toString().equals("1") || aValue.toString().equals("yes")) {
                value = true;
            }

            if (aColumnName.equals("is_active")) {
                setIsActive(value);
            } else if (aColumnName.equals("is_default")) {
                setIsDefault(value);
            } else if (aColumnName.equals("is_checked")) {
                setIsChecked(value);
            } else if (aColumnName.equals("is_private")) {
                setIsPrivate(value);
            } else if (aColumnName.equals("is_final")) {
                setIsFinal(value);
            }
        }

        break;

        case DataType.INT : {
            int value = 0;

            try {
                value = Integer.parseInt(aValue);
            } catch (NumberFormatException nfe) {
                LOG.info(nfe.toString());
                value = 0;
            }

            if (aColumnName.equals("sys_id")) {
                setSystemId(value);
            } else if (aColumnName.equals("field_id")) {
                setFieldId(value);
            } else if (aColumnName.equals("type_id")) {
                setTypeId(value);
            } else if (aColumnName.equals("ordering")) {
                setOrdering(value);
            }
        }

        break;

        case DataType.STRING : {
            String value = (String) aValue;

            if (aColumnName.equals("name")) {
                setName(value);
            } else if (aColumnName.equals("display_name")) {
                setDisplayName(value);
            } else if (aColumnName.equals("description")) {
                setDescription(value);
            }
        }

        break;
        }
    }

    /**
     * This method sets the parameters in the CallableStatement.
     *
     * @param aCS
     *            CallableStatement whose params should be set.
     *
     * @exception SQLException
     */
    public void setCallableParameters(CallableStatement aCS) throws SQLException {
        aCS.setInt(SYSTEMID, mySystemId);
        aCS.setInt(FIELDID, myFieldId);
        aCS.setInt(TYPEID, myTypeId);
        aCS.setString(NAME, myName);
        aCS.setString(DISPLAYNAME, myDisplayName);
        aCS.setString(DESCRIPTION, myDescription);
        aCS.setInt(ORDERING, myOrdering);
        aCS.setBoolean(ISACTIVE, myIsActive);
        aCS.setBoolean(ISDEFAULT, myIsDefault);
        aCS.setBoolean(ISCHECKED, myIsChecked);
        aCS.setBoolean(ISPRIVATE, myIsPrivate);
        aCS.setBoolean(ISFINAL, myIsFinal);
    }

    /**
     * Mutator method for Description property.
     *
     * @param aDescription
     *            New Value for Description
     *
     */
    public void setDescription(String aDescription) {
        myDescription = aDescription;
        myValueMap.put("description", myDescription);
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
        myValueMap.put("display_name", myDisplayName);
    }

    /**
     * Mutator method for FieldId property.
     *
     * @param aFieldId
     *            New Value for FieldId
     *
     */
    public void setFieldId(int aFieldId) {
        myFieldId = aFieldId;
        myValueMap.put("field_id", myFieldId);
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
        myValueMap.put("is_active", myIsActive);
    }

    /**
     * Mutator method for IsChecked property.
     *
     * @param aIsChecked
     *            New Value for IsChecked
     *
     */
    public void setIsChecked(boolean aIsChecked) {
        myIsChecked = aIsChecked;
        myValueMap.put("is_checked", myIsChecked);
    }

    /**
     * Mutator method for IsDefault property.
     *
     * @param aIsDefault
     *            New Value for IsDefault
     *
     */
    public void setIsDefault(boolean aIsDefault) {
        myIsDefault = aIsDefault;
        myValueMap.put("is_default", myIsDefault);
    }

    /**
     * Mutator method for IsFinal property.
     *
     * @param aIsFinal
     *            New Value for IsFinal
     *
     */
    public void setIsFinal(boolean aIsFinal) {
        myIsFinal = aIsFinal;
        myValueMap.put("is_final", myIsFinal);
    }

    /**
     * Mutator method for IsPrivate property.
     *
     * @param aIsPrivate
     *            New Value for IsPrivate
     *
     */
    public void setIsPrivate(boolean aIsPrivate) {
        myIsPrivate = aIsPrivate;
        myValueMap.put("is_private", myIsPrivate);
    }

    /**
     * Mutator method for Name property.
     *
     * @param aName
     *            New Value for Name
     *
     */
    public void setName(String aName) {
        myName = aName;
        myValueMap.put("name", myName);
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
        myValueMap.put("ordering", myOrdering);
    }

    /**
     * Mutator method for SortField property.
     *
     * @param aSortField
     *            New Value of SortField
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
        myValueMap.put("sys_id", mySystemId);
    }

    /**
     * Mutator method for TypeId property.
     *
     * @param aTypeId
     *            New Value for TypeId
     *
     */
    public void setTypeId(int aTypeId) {
        myTypeId = aTypeId;
        myValueMap.put("type_id", myTypeId);
    }
}


/**
 * This class is the comparator for domain object corresponding to the types
 * table in the database.
 *
 * @author  : Vinod Gupta,Vaibhav,g
 * @version : $Id: $
 *
 */
class TypeComparator implements Comparator<Type>, Serializable {
    public int compare(Type obj1, Type obj2) {
        return obj1.compareTo(obj2);
    }
}
