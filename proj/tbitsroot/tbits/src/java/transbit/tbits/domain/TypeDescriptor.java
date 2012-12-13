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
 * TypeDescriptor.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

//Other TBits Imports.
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;

//Imports from the current package.
import transbit.tbits.domain.Field;

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;
import static transbit.tbits.api.Mapper.ourTypeDescListMap;

//Static imports if any..
import static transbit.tbits.api.Mapper.ourTypeDescMap;

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
 * This class is the domain object corresponding to the type_descriptors table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class TypeDescriptor implements Comparable<TypeDescriptor>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    public static final int SYSTEMID   = 1;
    public static final int FIELDID    = 2;
    public static final int TYPEID     = 3;
    public static final int IS_PRIMARY = 5;
    public static final int DESCRIPTOR = 4;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private String  myDescriptor;
    private int     myFieldId;
    private boolean myIsPrimary;

    // Attributes of this Domain Object.
    private int mySystemId;
    private int myTypeId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public TypeDescriptor() {}

    /**
     * The complete constructor.
     *
     * @param aSystemId
     * @param aFieldId
     * @param aTypeId
     * @param aDescriptor
     * @param aIsPrimary
     */
    public TypeDescriptor(int aSystemId, int aFieldId, int aTypeId, String aDescriptor, boolean aIsPrimary) {
        mySystemId   = aSystemId;
        myFieldId    = aFieldId;
        myTypeId     = aTypeId;
        myDescriptor = aDescriptor;
        myIsPrimary  = aIsPrimary;
    }

    //~--- methods ------------------------------------------------------------

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
    public int compareTo(TypeDescriptor aObject) {
        switch (ourSortField) {
        case SYSTEMID : {
            Integer i1 = mySystemId;
            Integer i2 = aObject.mySystemId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            } else {
                return i2.compareTo(i1);
            }
        }

        case FIELDID : {
            Integer i1 = myFieldId;
            Integer i2 = aObject.myFieldId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            } else {
                return i2.compareTo(i1);
            }
        }

        case TYPEID : {
            Integer i1 = myTypeId;
            Integer i2 = aObject.myTypeId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            } else {
                return i2.compareTo(i1);
            }
        }

        case DESCRIPTOR : {
            if (ourSortOrder == ASC_ORDER) {
                return myDescriptor.compareTo(aObject.myDescriptor);
            } else {
                return aObject.myDescriptor.compareTo(myDescriptor);
            }
        }

        case IS_PRIMARY : {
            Boolean b1 = myIsPrimary;
            Boolean b2 = aObject.myIsPrimary;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            } else {
                return b1.compareTo(b2);
            }
        }
        }

        return 0;
    }

    /**
     * This method constructs a TypeDescriptor object from the resultset.
     *
     * @param aRS
     *            ResultSet which points to a TypeDescriptor record.
     *
     * @return TypeDescriptor object
     *
     * @exception SQLException
     */
    public static TypeDescriptor createFromResultSet(ResultSet aRS) throws SQLException {
        TypeDescriptor fd = new TypeDescriptor(aRS.getInt("sys_id"), aRS.getInt("field_id"), aRS.getInt("type_id"), aRS.getString("type_descriptor"), aRS.getBoolean("is_primary"));

        return fd;
    }

    /**
     * Method to insert a TypeDescriptor object into database.
     *
     * @param aObject
     *            Object to be inserted
     *
     */
    public static boolean delete(TypeDescriptor aObject) {

        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();

            CallableStatement cs = aCon.prepareCall("stp_td_delete ?, ?, ?, ?, ?");

            aObject.setCallableParameters(cs);
            cs.execute();
            cs.close();
            returnValue = true;
        } catch (SQLException sqle) {
            returnValue = false;
            LOG.severe("An exception has occured while deleting type " + "descriptor", sqle);
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
     * Method to insert a TypeDescriptor object into database.
     *
     * @param aObject
     *            Object to be inserted
     *
     */
    public static boolean insert(TypeDescriptor aObject) {

        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_td_insert ?, ?, ?, ?, ?");

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
            LOG.severe("An exception has occured while inserting type " + "descriptor", sqle);
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
     * This method returns the Type object corresponding to the given system id
     * and field Id and Type Descriptor.
     *
     * @param aSystemId
     *            Business Area Id.
     * @param aFieldId
     *            Field Id.
     * @param aDescriptor
     *            Type Descriptor.
     *
     * @return Type object corresponding to this descriptor in the given
     *         Business area and field.
     *
     * @exception DatabaseException
     */
    public static Type lookupBySystemIdAndFieldIdAndDescriptor(int aSystemId, int aFieldId, String aDescriptor) throws DatabaseException {
        Type type = null;

        // Look in the mapper first.
        if (ourTypeDescMap != null) {
            String         key = aSystemId + "-" + aFieldId + "-" + aDescriptor.toUpperCase().trim();
            TypeDescriptor td  = ourTypeDescMap.get(key);

            if (td == null) {

                // Look in the types table if this descriptor is name of
                // the type
                type = Type.lookupBySystemIdAndFieldIdAndTypeName(aSystemId, aFieldId, aDescriptor);

                return type;
            } else {

                // Look in the Types table by system id and field id and
                // type id.
                type = Type.lookupBySystemIdAndFieldIdAndTypeId(aSystemId, aFieldId, td.getTypeId());

                return type;
            }
        }

        // Try getting this from the database
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_td_lookupBySystemIdAndFieldIdAndTypeDescriptor ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aFieldId);
            cs.setString(3, aDescriptor);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {

                    // Output of the procedure is a Type record.
                    type = Type.createFromResultSet(rs);
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

            message.append("An exception occured while retrieving the type").append(" with the following descriptor information.").append("\nSystem Id : ").append(aSystemId).append(
                "\nField Id : ").append(aFieldId).append("\nDescriptor: ").append(aDescriptor).append("\n");

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

        return type;
    }

    /**
     * This method returns the Type object corresponding to the given system id
     * and field Name and Type Descriptor.
     *
     * @param aSystemId
     *            Business Area Id.
     * @param aFieldName
     *            Field Name.
     * @param aDescriptor
     *            Type Descriptor.
     *
     * @return Type object corresponding to this descriptor in the given
     *         Business area and field.
     *
     * @exception DatabaseException
     */
    public static Type lookupBySystemIdAndFieldNameAndDescriptor(int aSystemId, String aFieldName, String aDescriptor) throws DatabaseException {
        Type type = null;

        if (aFieldName == null) {
            return type;
        }

        if (aDescriptor == null) {
            return type;
        }

        aFieldName  = aFieldName.trim();
        aDescriptor = aDescriptor.trim();

        // Get the field object corresponding to the field name.
        Field field = Field.lookupBySystemIdAndFieldName(aSystemId, aFieldName);

        if (field == null) {
            LOG.info("Invalid field name: " + aFieldName);

            return null;
        }

        int fieldId = field.getFieldId();

        type = TypeDescriptor.lookupBySystemIdAndFieldIdAndDescriptor(aSystemId, fieldId, aDescriptor);

        //
        // Trying to match as field value/field-value/field_value
        //
        if ((type == null) && (aDescriptor.indexOf(" ") != -1)) {
            type = TypeDescriptor.lookupBySystemIdAndFieldIdAndDescriptor(aSystemId, fieldId, aDescriptor.replaceAll(" ", "-"));

            if (type == null) {
                type = TypeDescriptor.lookupBySystemIdAndFieldIdAndDescriptor(aSystemId, fieldId, aDescriptor.replaceAll(" ", "_"));
            }
        } else if ((type == null) && (aDescriptor.indexOf("-") != -1)) {
            type = TypeDescriptor.lookupBySystemIdAndFieldIdAndDescriptor(aSystemId, fieldId, aDescriptor.replaceAll("-", " "));

            if (type == null) {
                type = TypeDescriptor.lookupBySystemIdAndFieldIdAndDescriptor(aSystemId, fieldId, aDescriptor.replaceAll("-", "_"));
            }
        } else if ((type == null) && (aDescriptor.indexOf("_") != -1)) {
            type = TypeDescriptor.lookupBySystemIdAndFieldIdAndDescriptor(aSystemId, fieldId, aDescriptor.replaceAll("_", " "));

            if (type == null) {
                type = TypeDescriptor.lookupBySystemIdAndFieldIdAndDescriptor(aSystemId, fieldId, aDescriptor.replaceAll("_", "-"));
            }
        }

        if (type == null) {
            type = Type.lookupBySystemIdAndMinimalMatch(aSystemId, aDescriptor);
        }

        return type;
    }

    /**
     * This method returns the list of Type Descriptor objects corresponding to
     * the given system id, field Id and type Id
     *
     * @param aSystemId
     *            Business Area Id.
     * @param aFieldId
     *            Field Id.
     * @param aTypeId
     *            Type Id
     *
     * @return List of Type Descriptor objects corresponding to this type.
     */
    public static ArrayList<TypeDescriptor> lookupListBySystemIdAndFieldIdAndTypeId(int aSystemId, int aFieldId, int aTypeId) throws DatabaseException {
        ArrayList<TypeDescriptor> tdList = null;

        // Look in the mapper first.
        if (ourTypeDescListMap != null) {
            String key = aSystemId + "-" + aFieldId + "-" + aTypeId;

            tdList = ourTypeDescListMap.get(key);
        } else {
            tdList = lookupTDListBySystemIdAndFieldIdAndTypeId(aSystemId, aFieldId, aTypeId);
        }

        return tdList;
    }

    /**
     * This method returns the TypeDescriptor object corresponding to the given
     * system id and field Id.
     *
     * @param aSystemId
     *            Business Area Id.
     * @param aFieldId
     *            Field Id.
     * @param aTypeId
     *            Type Id
     *
     * @return ArrayList of TypeDescriptor object corresponding to this fieldId
     *         and typeId in the given Business area.
     */
    public static ArrayList<TypeDescriptor> lookupTDListBySystemIdAndFieldIdAndTypeId(int aSystemId, int aFieldId, int aTypeId) throws DatabaseException {
        ArrayList<TypeDescriptor> tdList = new ArrayList<TypeDescriptor>();

        // Get the details from database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_td_getTypeDescriptorsBySystemIdAndFieldIdAndTypeId ?,?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aFieldId);
            cs.setInt(3, aTypeId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    TypeDescriptor fd = TypeDescriptor.createFromResultSet(rs);

                    tdList.add(fd);
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

            message.append("An exception occured while retrieving the type").append(" descriptor information.").append("\nSystem Id : ").append(aSystemId).append("\nField Id : ").append(
                aFieldId).append("\nType Id : ").append(aTypeId).append("\n");

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

        return tdList;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param source
     *            the array list of Type objects
     * @return the ArrayList of the TypeDescriptor objects in sorted order
     */
    public static ArrayList<TypeDescriptor> sort(ArrayList<TypeDescriptor> source) {
        int              size     = source.size();
        TypeDescriptor[] srcArray = new TypeDescriptor[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new TypeDescriptorComparator());

        ArrayList<TypeDescriptor> target = new ArrayList<TypeDescriptor>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for Descriptor property.
     *
     * @return Current Value of Descriptor
     *
     */
    public String getDescriptor() {
        return myDescriptor;
    }

    /**
     * This method returns the Descriptor table for the given business area. The
     * key in the table is the type descriptor name and the value is the
     * corresponding type object.
     *
     * @param aSystemId
     *            Id of the Business Area.
     * @param aDuplicates
     *            list of all dupliacte type descriptors.
     *
     * @return Table of Type Descriptor and the corresponding Type object.
     *
     * @exception DatabaseException
     *                in case of any database related errors.
     */
    public static Hashtable<String, Type> getDescriptorTable(int aSystemId, ArrayList<String> aDuplicates) throws DatabaseException {
        Hashtable<String, Type> table = new Hashtable<String, Type>();

        // Try to retrieve from the mapper.
        if (ourTypeDescListMap != null) {
            String                    key  = Integer.toString(aSystemId);
            ArrayList<TypeDescriptor> list = ourTypeDescListMap.get(key);

            if (list != null) {
                for (TypeDescriptor td : list) {
                    Type type = Type.lookupBySystemIdAndFieldIdAndTypeId(aSystemId, td.getFieldId(), td.getTypeId());

                    if (type == null) {
                        continue;
                    }

                    if (table.get(td.getDescriptor().toLowerCase()) != null) {
                        aDuplicates.add(td.getDescriptor().toLowerCase());
                    }

                    if (table.get(type.getName().toLowerCase()) != null) {
                        aDuplicates.add(type.getName().toLowerCase());
                    }

                    if (table.get(type.getDisplayName().toLowerCase()) != null) {
                        aDuplicates.add(type.getDisplayName().toLowerCase());
                    }

                    table.put(td.getDescriptor().toLowerCase(), type);
                    table.put(type.getName().toLowerCase(), type);
                    table.put(type.getDisplayName().toLowerCase(), type);
                }
            }
        }

        return table;
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
     * Accessor method for IsPrimary property.
     *
     * @return Current Value of IsPrimary
     *
     */
    public boolean getIsPrimary() {
        return myIsPrimary;
    }

    /**
     * This method returns the primary TypeDescriptor object corresponding to
     * the given system id and field name and type name
     *
     * @param aSystemId   Business Area Id.
     * @param aFieldName  Field Name.
     * @param aTypeName   Type Name
     *
     * @return TypeDescriptor object corresponding to this type
     *         in the given Business area.
     */
    public static TypeDescriptor getPrimaryDescriptor(int aSystemId, String aFieldName, String aTypeName) throws DatabaseException {
        TypeDescriptor td   = null;
        Type           type = Type.lookupBySystemIdAndFieldNameAndTypeName(aSystemId, aFieldName, aTypeName);

        // If field object itself is not found, then we cannot get a descriptor
        if (type == null) {
            return td;
        }

        int aFieldId = type.getFieldId();
        int aTypeId  = type.getTypeId();

        // Look in the mapper first.
        if (ourTypeDescMap != null) {
            String key = aSystemId + "-" + aFieldId + "-" + aTypeId + "__PRIMARY_";
            ;

            td = ourTypeDescMap.get(key);

            if (td == null) {
                td = new TypeDescriptor(aSystemId, aFieldId, aTypeId, aTypeName, true);
            }

            return td;
        }

        LOG.info("Connecting to the database to get the primary descriptor " + "for " + aTypeName + " in " + aSystemId + " BA");

        // Check in the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_td_getPrimaryDescriptorByFieldIdAndTypeId ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aFieldId);
            cs.setInt(3, aTypeId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() == true) {
                    td = createFromResultSet(rs);
                }

                rs.close();
            }

            rs = null;
            cs.close();
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the primary").append(" field descriptor with the following information.").append("\nSystem Id : ").append(aSystemId).append(
                "\nField Name: ").append(aFieldName).append("\nType Name: ").append(aTypeName).append("\n");

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

        return td;
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
        aCS.setString(DESCRIPTOR, myDescriptor);
        aCS.setBoolean(IS_PRIMARY, myIsPrimary);
    }

    /**
     * Mutator method for Descriptor property.
     *
     * @param aDescriptor
     *            New Value for Descriptor
     *
     */
    public void setDescriptor(String aDescriptor) {
        myDescriptor = aDescriptor;
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
     * Mutator method for TypeId property.
     *
     * @param aTypeId
     *            New Value for TypeId
     *
     */
    public void setTypeId(int aTypeId) {
        myTypeId = aTypeId;
    }
}


/**
 * This class is the comparator for domain object corresponding to the
 * type_descriptors table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class TypeDescriptorComparator implements Comparator<TypeDescriptor>, Serializable {
    public int compare(TypeDescriptor obj1, TypeDescriptor obj2) {
        return obj1.compareTo(obj2);
    }
}
