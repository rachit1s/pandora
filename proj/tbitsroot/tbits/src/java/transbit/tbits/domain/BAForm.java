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
 * BAForm.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;

//Other TBits Imports.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.FBForm;

//Static Imports
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

//~--- classes ----------------------------------------------------------------

/**
 * This class is the domain object corresponding to the ba_forms table
 * in the database.
 *
 * @author  : Vaibhav
 * @version : $Id: $
 *
 */
public class BAForm implements Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID   = 1;
    private static final int NAME       = 3;
    private static final int FORMID     = 2;
    private static final int TITLE      = 4;
    private static final int SHORTNAME  = 5;
    private static final int FORMCONFIG = 6;

    //~--- fields -------------------------------------------------------------

    private String myFormConfig;
    private FBForm myFormConfigObject;
    private int    myFormId;
    private String myName;
    private String myShortName;

    // Attributes of this Domain Object.
    private int    mySystemId;
    private String myTitle;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor
     */
    private BAForm() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aFormId
     *  @param aName
     *  @param aTitle
     *  @param aShortName
     *  @param aFormConfig
     */
    public BAForm(int aSystemId, int aFormId, String aName, String aTitle, String aShortName, String aFormConfig) {
        mySystemId   = aSystemId;
        myFormId     = aFormId;
        myName       = aName;
        myTitle      = aTitle;
        myShortName  = aShortName;
        myFormConfig = aFormConfig;

        /*
         * Convert this XML form configuration to an FBForm object.
         */
        try {
            myFormConfigObject = FBForm.parseFormConfig(myFormConfig);
        } catch (Exception e) {
            StringBuilder message = new StringBuilder();

            message.append("An exception has occured while parsing the form ").append("configuration.").append("\nSystem Id: ").append(mySystemId).append("\nForm Name: ").append(myName).append(
                "\n").append(TBitsLogger.getStackTrace(e));
            LOG.severe(message.toString());
        }
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method creates a BAForm object from the current row in the resultset
     * object.
     *
     * @param rs        ResultSet object.
     *
     * @return          BAForm object
     *
     * @throws SQLException
     */
    public static BAForm createFromResultSet(ResultSet rs) throws SQLException {
        BAForm baForm = new BAForm(rs.getInt("sys_id"), rs.getInt("form_id"), rs.getString("name"), rs.getString("title"), rs.getString("shortname"), rs.getString("form_config"));

        return baForm;
    }

    /**
     * Method to insert a BAForm object into database.
     *
     * @param aObject Object to be inserted
     *
     */
    public static boolean insert(BAForm aObject) {

        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();

            CallableStatement cs = aCon.prepareCall("stp_ba_forms_insert ?, ?, ?, ?, ?, ?");

            aObject.setCallableParameters(cs);
            cs.execute();
            cs.close();
            returnValue = true;
        } catch (SQLException sqle) {
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
     * Method to lookup the forms list by system id and name. Name will be
     * looked up in both name and shortname column in the database.
     *
     * @param aSystemId  Business Area ID
     * @param aName      Name of the form.
     *
     * @return BAForm object.
     * @throws DatabaseException
     */
    public static BAForm lookupBySystemIdAndName(int aSystemId, String aName) throws DatabaseException {
        BAForm     baForm = null;
        Connection con    = null;

        try {
            con = DataSourcePool.getConnection();

            CallableStatement cs = con.prepareCall("stp_ba_forms_lookupBySystemIdAndName ?, ?");

            cs.setInt(1, aSystemId);
            cs.setString(2, aName);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() == true) {
                    baForm = createFromResultSet(rs);
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception has occurred while retrieving the form ").append("details").append("\nSystem ID: ").append(aSystemId).append("\nForm Name: ").append(aName);

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException sqle) {

                /*
                 * This should not be thrown as close on connection would merely
                 * result in informing the pooler that connection can be reused.
                 */
            }
        }

        return baForm;
    }

    /**
     * Main method for testing.
     *
     * @param arg Command line arguments.
     *
     * @throws Exception
     */
    public static int main(String arg[]) throws Exception {
        BAForm baForm = BAForm.lookupBySystemIdAndName(1, "drf");

        System.out.println(baForm);
        return 0;
    }

    /**
     * String representation of BAForm object.
     */
    public String toString() {
        StringBuilder data = new StringBuilder();

        data.append("\nSystem ID:  ").append(mySystemId).append("\nForm Name:  ").append(myName).append("\nForm Title: ").append(myTitle).append("\nShort Name: ").append(myShortName).append(
            "\nForm Config: ").append(myFormConfigObject);

        return data.toString();
    }

    /**
     * Method to update the corresponding BAForm object in the database.
     *
     * @param aObject Object to be updated
     *
     * @return Update domain object.
     *
     */
    public static BAForm update(BAForm aObject) {

        // Update logic here.
        if (aObject == null) {
            return aObject;
        }

        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();

            CallableStatement cs = aCon.prepareCall("stp_ba_forms_update ?, ?, ?, ?, ?, ?");

            aObject.setCallableParameters(cs);
            cs.execute();
            cs.close();
        } catch (SQLException sqle) {}
        finally {
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
     * Accessor method for FormConfig property.
     *
     * @return Current Value of FormConfig
     *
     */
    public String getFormConfig() {
        return myFormConfig;
    }

    /**
     * Accessor method for FormConfigObject property.
     *
     * @return Current Value of FormConfigObject
     *
     */
    public FBForm getFormConfigObject() {
        return myFormConfigObject;
    }

    /**
     * Accessor method for FormId property.
     *
     * @return Current Value of FormId
     *
     */
    public int getFormId() {
        return myFormId;
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
     * Accessor method for ShortName property.
     *
     * @return Current Value of ShortName
     *
     */
    public String getShortName() {
        return myShortName;
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
     * Accessor method for Title property.
     *
     * @return Current Value of Title
     *
     */
    public String getTitle() {
        return myTitle;
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
        aCS.setInt(FORMID, myFormId);
        aCS.setString(NAME, myName);
        aCS.setString(TITLE, myTitle);
        aCS.setString(SHORTNAME, myShortName);
        aCS.setString(FORMCONFIG, myFormConfig);
    }

    /**
     * Mutator method for FormConfig property.
     *
     * @param aFormConfig New Value for FormConfig
     *
     */
    public void setFormConfig(String aFormConfig) {
        myFormConfig = aFormConfig;
    }

    /**
     * Mutator method for FormId property.
     *
     * @param aFormId New Value for FormId
     *
     */
    public void setFormId(int aFormId) {
        myFormId = aFormId;
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
     * Mutator method for ShortName property.
     *
     * @param aShortName New Value for ShortName
     *
     */
    public void setShortName(String aShortName) {
        myShortName = aShortName;
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
     * Mutator method for Title property.
     *
     * @param aTitle New Value for Title
     *
     */
    public void setTitle(String aTitle) {
        myTitle = aTitle;
    }
}
