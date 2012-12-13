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
 * DSQLParser.java
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

//Static Imports
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;

//~--- classes ----------------------------------------------------------------

public class DataType implements Serializable{
    public static final TBitsLogger LOG         = TBitsLogger.getLogger(PKG_DOMAIN);
    public static final int         DATE        = 2;
    public static final int         BOOLEAN     = 1;
    public static final int         TIME        = 3;
    public static final int         TEXT        = 8;
    public static final int         STRING      = 7;
    public static final int         REAL        = 6;
    public static final int         INT         = 5;
    public static final int         DATETIME    = 4;
    public static final int         TYPE        = 9;
    public static final int         USERTYPE = 10;
    public static final int         ATTACHMENTS = 11;
    
    
    //~--- fields -------------------------------------------------------------

    private String myDataType;

    // Attributes of this Domain Object.
    private int    myDataTypeId;
    private String myDescription;

    //~--- constructors -------------------------------------------------------

    /**
     * The complete constructor.
     *
     *  @param aDataTypeId
     *  @param aDataType
     *  @param aDescription
     */
    public DataType(int aDataTypeId, String aDataType, String aDescription) {
        myDataTypeId  = aDataTypeId;
        myDataType    = aDataType;
        myDescription = aDescription;
    }

    //~--- methods ------------------------------------------------------------

    public static DataType createFromResultSet(ResultSet aRS) throws SQLException {
        DataType dt = new DataType(aRS.getInt("datatype_id"), aRS.getString("name"), aRS.getString("description"));

        return dt;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method to get the list of Data types..
     *
     * @return the ArrayList containing the list of the Datatype objects.
     *
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<DataType> getAllDataTypes() throws DatabaseException {
        ArrayList<DataType> dataTypeList = new ArrayList<DataType>();
        Connection          connection   = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("{Call stp_admin_getAllDataTypes }");
            ResultSet         rs = cs.executeQuery();

            while (rs.next()) {
                DataType dataType = createFromResultSet(rs);

                dataTypeList.add(dataType);
            }

            rs.close();
            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("The application encountered an exception while ").append("trying to retrieve the list of DataTypes");

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

        return dataTypeList;
    }

    /**
     * Accessor method for DataType property.
     *
     * @return Current Value of RoleId
     *
     */
    public String getDataType() {
        return myDataType;
    }

    /**
     * Accessor method for dataTypeId property.
     *
     * @return Current Value of SystemId
     *
     */
    public int getDataTypeId() {
        return myDataTypeId;
    }

    public static int getDataTypeId(String aDataType) {
        int dataTypeId = 0;

        if (aDataType == "BOOLEAN") {
            dataTypeId = BOOLEAN;
        }

        if (aDataType == "DATE") {
            dataTypeId = DATE;
        }

        if (aDataType == "TIME") {
            dataTypeId = TIME;
        }

        if (aDataType == "DATETIME") {
            dataTypeId = DATETIME;
        }

        if (aDataType == "INT") {
            dataTypeId = INT;
        }

        if (aDataType == "REAL") {
            dataTypeId = REAL;
        }

        if (aDataType == "STRING") {
            dataTypeId = STRING;
        }

        if (aDataType == "TEXT") {
            dataTypeId = TEXT;
        }

        if (aDataType == "TYPE") {
            dataTypeId = TYPE;
        }

        if (aDataType == "MULTI_VALUE") {
            dataTypeId = USERTYPE;
        }

        return dataTypeId;
    }

    /**
     * Accessor method for DataType property.
     *
     * @return Current Value of RoleName
     *
     */
    public String getDescription() {
        return myDescription;
    }
}
