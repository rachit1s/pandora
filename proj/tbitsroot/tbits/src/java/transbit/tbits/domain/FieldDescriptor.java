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
 * FieldDescriptor.java
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
import static transbit.tbits.api.Mapper.ourFieldDescListMap;

//Static imports if any..
import static transbit.tbits.api.Mapper.ourFieldDescMap;

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
 * This class is the domain object corresponding to the field_descriptors table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class FieldDescriptor implements Comparable<FieldDescriptor>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    public static final int SYSTEMID   = 1;
    public static final int IS_PRIMARY = 4;
    public static final int FIELDID    = 2;
    public static final int DESCRIPTOR = 3;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private String  myDescriptor;
    private int     myFieldId;
    private boolean myIsPrimary;

    // Attributes of this Domain Object.
    private int mySystemId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public FieldDescriptor() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aFieldId
     *  @param aDescriptor
     *  @param aIsPrimary
     */
    public FieldDescriptor(int aSystemId, int aFieldId, String aDescriptor, boolean aIsPrimary) {
        mySystemId   = aSystemId;
        myFieldId    = aFieldId;
        myDescriptor = aDescriptor;
        myIsPrimary  = aIsPrimary;
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
    public int compareTo(FieldDescriptor aObject) {
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
     * This method constructs a FieldDescriptor object from the resultset.
     *
     * @param aRS     ResultSet which points to a FieldDescriptor record.
     *
     * @return FieldDescriptor object
     *
     * @exception SQLException
     */
    public static FieldDescriptor createFromResultSet(ResultSet aRS) throws SQLException {
        FieldDescriptor fd = new FieldDescriptor(aRS.getInt("sys_id"), aRS.getInt("field_id"), aRS.getString("field_descriptor"), aRS.getBoolean("is_primary"));

        return fd;
    }

    /**
     * Method to insert a FieldDescriptor object into database.
     *
     * @param aObject Object to be inserted
     *
     */
    public static boolean delete(FieldDescriptor aObject) {

        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();

            CallableStatement cs = aCon.prepareCall("stp_field_descriptor_delete ?, ?, ?, ?");

            aObject.setCallableParameters(cs);
            cs.execute();
            cs.close();
            returnValue = true;
        } catch (SQLException sqle) {
            returnValue = false;
            LOG.severe("An exception has occured while deleting field " + "descriptor", sqle);
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
     * Method to insert a FieldDescriptor object into database.
     *
     * @param aObject Object to be inserted
     *
     */
    public static boolean insert(FieldDescriptor aObject) {

        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_field_descriptor_insert ?, ?, ?, ?");

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
            LOG.severe("An exception has occured while inserting field " + "descriptor", sqle);
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
     * This method returns the Field object corresponding to the given system
     * id and field descriptor.
     *
     * @param aSystemId   Business Area Id.
     * @param aDescriptor Field Descriptor.
     *
     * @return Field object corresponding to this descriptor in the given
     *         Business area.
     *
     * @exception DatabaseException
     */
    public static Field lookupBySystemIdAndDescriptor(int aSystemId, String aDescriptor) throws DatabaseException {
        Field field = null;

        // Look in the mapper first.
        if (ourFieldDescMap != null) {
            String          key = aSystemId + "-" + aDescriptor.toUpperCase().trim();
            FieldDescriptor fd  = ourFieldDescMap.get(key);

            if (fd == null) {

                // Look in the fields table if this descriptor is name of
                // the field.
                field = Field.lookupBySystemIdAndFieldName(aSystemId, aDescriptor);

                return field;
            } else {

                // Look in the fields table by system id and field id.
                field = Field.lookupBySystemIdAndFieldId(aSystemId, fd.getFieldId());

                return field;
            }
        }

        // Try getting this from the database
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_fd_lookupBySystemIdAndFieldDescriptor ?, ?");

            cs.setInt(1, aSystemId);
            cs.setString(2, aDescriptor);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {

                    // Output of the procedure is a field record.
                    field = Field.createFromResultSet(rs);
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

            message.append("An exception occured while retrieving the field").append(" with the following descriptor information.").append("\nSystem Id : ").append(aSystemId).append(
                "\nDescriptor: ").append(aDescriptor).append("\n");

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

        return field;
    }

    /**
     * This method returns the FieldDescriptor object corresponding to
     * the given system id and field Id.
     *
     * @param aSystemId   Business Area Id.
     * @param aFieldId     Field Id.
     *
     * @return FieldDescriptor object corresponding to this fieldId
     *         in the given Business area.
     */
    public static ArrayList<FieldDescriptor> lookupFDListBySystemIdAndFieldId(int aSystemId, int aFieldId) throws DatabaseException {
        ArrayList<FieldDescriptor> fdList = new ArrayList<FieldDescriptor>();

        // Get the details from database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_fd_getFieldDescriptorsBySystemIdAndFieldId ?,?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aFieldId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    FieldDescriptor fd = FieldDescriptor.createFromResultSet(rs);

                    fdList.add(fd);
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

            message.append("An exception occured while retrieving the field").append(" descriptor information.").append("\nSystem Id : ").append(aSystemId).append("\nField Id : ").append(
                aFieldId).append("\n");

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

        return fdList;
    }

    /**
     * This method returns the FieldDescriptor object corresponding to
     * the given system id and field Id.
     *
     * @param aSystemId   Business Area Id.
     * @param aFieldId     Field Id.
     *
     * @return FieldDescriptor object corresponding to this fieldId
     *         in the given Business area.
     */
    public static ArrayList<FieldDescriptor> lookupListBySystemIdAndFieldId(int aSystemId, int aFieldId) throws DatabaseException {
        ArrayList<FieldDescriptor> fdList = null;

        // Look in the mapper first.
        if (ourFieldDescListMap != null) {
            String key = aSystemId + "-" + aFieldId;

            fdList = ourFieldDescListMap.get(key);
        } else {
            fdList = lookupFDListBySystemIdAndFieldId(aSystemId, aFieldId);
        }

        return fdList;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the FieldDescriptor objects in sorted order
     */
    public static ArrayList<FieldDescriptor> sort(ArrayList<FieldDescriptor> source) {
        int               size     = source.size();
        FieldDescriptor[] srcArray = new FieldDescriptor[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new FieldDescriptorComparator());

        ArrayList<FieldDescriptor> target = new ArrayList<FieldDescriptor>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method finds all the distinct descriptors present in the current
     * tbits instance.
     *
     * @param aDescTypes   Out Param. Table of Descriptor and the data type.
     * @param aDescNames   Out Param. Table of Descriptor and the field name.
     * @param aDescField   Out Param. Table of Descriptor and the field object.
     *
     * @exception DatabaseException in case of any database related errors.
     */
    public static void getAllDescriptors(Hashtable<String, Integer> aDescTypes, Hashtable<String, String> aDescNames, Hashtable<String, Field> aDescField) throws DatabaseException {

        // Get the details from database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_fd_getAllDescriptors");
            ResultSet         rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    Field  field      = Field.createFromResultSet(rs);
                    String fieldName  = field.getName();
                    String descriptor = rs.getString("field_descriptor");

                    if (descriptor != null) {
                        aDescNames.put(descriptor.toLowerCase(), fieldName);
                        aDescTypes.put(descriptor.toLowerCase(), field.getDataTypeId());
                    }

                    aDescNames.put(fieldName.toLowerCase(), fieldName);
                    aDescNames.put(field.getDisplayName().toLowerCase(), fieldName);
                    aDescField.put(fieldName.toLowerCase(), field);
                }

                // Close the result set.
                rs.close();
            }

            // Close the statement.
            cs.close();

            //
            // Release the memory by nullifying the references so that these
            // can be recovered by the Garbage collector.
            //
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the field").append(" descriptor information.").append("\n");

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

        return;
    }

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
     * This method returns the Descriptor table for the given business area.
     * The key in the table is the field descriptor name and the value is the
     * corresponding field object.
     *
     * @param aSystemId  Id of the Business Area.
     *
     * @return Table of Field Descriptor and the corresponding Field object.
     *
     * @exception DatabaseException in case of any database related errors.
     */
    public static Hashtable<String, Field> getDescriptorTable(int aSystemId) throws DatabaseException {
        Hashtable<String, Field> table = new Hashtable<String, Field>();

        // Try to retrieve from the mapper.
        if (ourFieldDescListMap != null) {
            String                     key  = Integer.toString(aSystemId);
            ArrayList<FieldDescriptor> list = ourFieldDescListMap.get(key);

            if (list != null) {
                for (FieldDescriptor fd : list) {
                    Field field = Field.lookupBySystemIdAndFieldId(aSystemId, fd.getFieldId());

                    if (field == null) {
                        continue;
                    }

                    table.put(fd.getDescriptor().toLowerCase(), field);
                    table.put(field.getName().toLowerCase(), field);
                    table.put(field.getDisplayName().toLowerCase(), field);
                }
            }

            return table;
        }

        // Get the details from database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_fd_getDescriptorTable ?");

            cs.setInt(1, aSystemId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    Field  field      = Field.createFromResultSet(rs);
                    String descriptor = rs.getString("field_descriptor");

                    if (descriptor != null) {
                        table.put(descriptor.toLowerCase(), field);
                    }

                    table.put(field.getName().toLowerCase(), field);
                    table.put(field.getDisplayName().toLowerCase(), field);
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

            message.append("An exception occured while retrieving the field").append(" descriptor information.").append("\nSystem Id : ").append(aSystemId).append("\n");

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
     * This method returns the primary FieldDescriptor object corresponding to
     * the given system id and field name.
     *
     * @param aSystemId   Business Area Id.
     *
     * @return FieldDescriptor object corresponding to this field
     *         in the given Business area.
     */
    public static Hashtable<String, String> getPrimaryDescTable(int aSystemId) throws DatabaseException {
        Hashtable<String, String> descTable = new Hashtable<String, String>();
        ArrayList<Field>          fieldList = Field.lookupBySystemId(aSystemId);

        if (fieldList == null) {
            return descTable;
        }

        for (Field field : fieldList) {
            String          fieldName = field.getName();
            FieldDescriptor fd        = getPrimaryDescriptor(aSystemId, fieldName);

            if (fd == null) {
                descTable.put(fieldName, fieldName);
            } else {
                String primaryDesc = fd.getDescriptor();

                descTable.put(fieldName, primaryDesc);
            }
        }

        return descTable;
    }

    /**
     * This method returns the primary FieldDescriptor object corresponding to
     * the given system id and field name.
     *
     * @param aSystemId   Business Area Id.
     * @param aFieldName  Field Name.
     *
     * @return FieldDescriptor object corresponding to this field
     *         in the given Business area.
     */
    public static FieldDescriptor getPrimaryDescriptor(int aSystemId, String aFieldName) throws DatabaseException {
        FieldDescriptor fd    = null;
        Field           field = Field.lookupBySystemIdAndFieldName(aSystemId, aFieldName);

        // If field object itself is not found, then we cannot get a descriptor
        if (field == null) {
            return fd;
        }

        int aFieldId = field.getFieldId();

        // Look in the mapper first.
        if (ourFieldDescMap != null) {
            String key = aSystemId + "-" + aFieldId + "__PRIMARY_";;

            fd = ourFieldDescMap.get(key);

            if (fd == null) {
                fd = new FieldDescriptor(aSystemId, aFieldId, aFieldName, true);
            }

            return fd;
        }

        LOG.info("Connecting to the database to get the primary descriptor " + "for " + aFieldName + " in " + aSystemId + " BA");

        // Check in the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_fd_getPrimaryDescriptorByFieldId ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aFieldId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() == true) {
                    fd = createFromResultSet(rs);
                }

                rs.close();
            }

            rs = null;
            cs.close();
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the primary").append(" field descriptor with the following information.").append("\nSystem Id : ").append(aSystemId).append(
                "\nField Name: ").append(aFieldName).append("\n");

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

        return fd;
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
        aCS.setInt(FIELDID, myFieldId);
        aCS.setString(DESCRIPTOR, myDescriptor);
        aCS.setBoolean(IS_PRIMARY, myIsPrimary);
    }

    /**
     * Mutator method for Descriptor property.
     *
     * @param aDescriptor New Value for Descriptor
     *
     */
    public void setDescriptor(String aDescriptor) {
        myDescriptor = aDescriptor;
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
     * Mutator method for IsPrimary property.
     *
     * @param aIsPrimary New Value for IsPrimary
     *
     */
    public void setIsPrimary(boolean aIsPrimary) {
        myIsPrimary = aIsPrimary;
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


/**
 * This class is the comparator for domain object corresponding to the
 * field_descriptors table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class FieldDescriptorComparator implements Comparator<FieldDescriptor>, Serializable {
    public int compare(FieldDescriptor obj1, FieldDescriptor obj2) {
        return obj1.compareTo(obj2);
    }
}
