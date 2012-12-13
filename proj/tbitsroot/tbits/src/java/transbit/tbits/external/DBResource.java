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
 * DBResource.java
 *
 * $Header:
 *
 */
package transbit.tbits.external;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;

//Other TBits Imports.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.exception.TBitsException;

//Static Imports 
import static transbit.tbits.Helper.TBitsConstants.PKG_EXTERNAL;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;

//~--- classes ----------------------------------------------------------------

/**
 * This class interacts with an external database resource through a stored
 * procedure with given input values and retrieves the output.
 *
 * @author  : Vaibhav
 * @version : $Id: $
 *
 */
public class DBResource implements Resource {

    // Application Logger
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_EXTERNAL);

    //~--- fields -------------------------------------------------------------

    private int                     myOutputCounter = 0;
    private int                     myResultsSize   = 0;
    private String                  myDBPoolName;
    private ArrayList<ResourceAttr> myInputAttrList;
    private ArrayList<ResourceAttr> myOutputAttrList;
    private String                  myProcName;

    // Attributes of this Object.
    private int    myResourceId;
    private String myResourceName;

    // This will hold the results.
    private ArrayList<ResourceResultMap> myResults;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public DBResource() {}

    /**
     * The complete constructor.
     *
     *  @param aResourceId
     *  @param aResourceName
     *  @param aDBPoolName
     *  @param aProcName
     *  @param aInputAttrList
     *  @param aOutputAttrList
     */
    public DBResource(int aResourceId, String aResourceName, ArrayList<ResourceAttr> aInputAttrList, ArrayList<ResourceAttr> aOutputAttrList, String aDBPoolName, String aProcName) {
        myResourceId     = aResourceId;
        myResourceName   = aResourceName;
        myInputAttrList  = aInputAttrList;
        myOutputAttrList = aOutputAttrList;
        myDBPoolName     = aDBPoolName;
        myProcName       = aProcName;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This is an abstract method in the Iterator interface that returns the
     * current element pointed to by the iterator and moves the pointer one
     * position to the right. If the pointer is already at the end of the list,
     * it throws NoSuchElementException.
     *
     * @return Current element pointed to by the iterator.
     *
     * @exception NoSuchElementException If already at the end of the list.
     */
    public ResourceResultMap next() throws NoSuchElementException {
        if (myOutputCounter < myResults.size()) {
            ResourceResultMap value = myResults.get(myOutputCounter);

            myOutputCounter = myOutputCounter + 1;

            return value;
        } else {
            throw new NoSuchElementException("Already at the end of the list.");
        }
    }

    /**
     * This method realizes the resource.
     *
     *
     * @exception DatabaseException In case of any database errors.
     * @exception TBitsException  In case of any other errors.
     */
    public void realizeResource(Hashtable<String, Object> aInputMap) throws TBitsException, DatabaseException {
        myResults = new ArrayList<ResourceResultMap>();

        if (aInputMap == null) {
            aInputMap = new Hashtable<String, Object>();
        }

        /*
         * We cannot move futher without any of these two. So check them.
         */
        if ((myDBPoolName == null) || myDBPoolName.trim().equals("") || (myProcName == null) || myProcName.trim().equals("")) {
            throw new TBitsException("Either the DBPool name or the procedure name are not " + "specified");
        }

        Connection con = null;

        try {
            con = DataSourcePool.getConnection(myDBPoolName);

            if (con == null) {
                throw new SQLException("No connection obtained from pool: " + myDBPoolName);
            }

            /*
             * Form the argument for the prepareCall method on the connection
             * to create the CallableStatement. Append as many ? as the number
             * of input parameters.
             */
            StringBuffer procDef = new StringBuffer();

            procDef.append(myProcName);

            int     inputSize = myInputAttrList.size();
            boolean first     = true;

            for (int i = 0; i < inputSize; i++) {
                if (first == false) {
                    procDef.append(", ");
                } else {
                    first = false;
                }

                procDef.append("?");
            }

            CallableStatement cs = con.prepareCall(procDef.toString());

            /*
             * Set the values to those input parameters using the setters
             * corresponding to the data type of these input parameters.
             */
            for (ResourceAttr attr : myInputAttrList) {
                setValue(cs, aInputMap, attr);
            }

            ResultSet rs = cs.executeQuery();

            if (rs != null) {

                /*
                 * The results will be stored in the myResults list. Each
                 * result is a table of the output parameter name and its
                 * ResourceAttr object that contains the information about
                 * this output parameter and its value.
                 */
                int counter = 0;

                while (rs.next() != false) {
                    counter = counter + 1;

                    ResourceResultMap rrm = getResultMap(rs, myOutputAttrList);

                    myResults.add(rrm);
                }

                rs.close();
                LOG.info(myResourceName + ": (" + counter + ") records .");
            }

            cs.close();
            rs = null;
            cs = null;
        }    // End Try
                catch (SQLException sqle) {
            StringBuffer message = new StringBuffer();

            message.append("An exception has occured while realizing the resource").append(": " + myResourceName).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {

            // In any case, we want to release the connection.
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception sqle) {
                LOG.info("Exception while closing the connection: " + sqle.toString());
            }
        }

        /*
         * Reset the counter and size for the new results.
         */
        if (myResults != null) {
            myResultsSize   = myResults.size();
            myOutputCounter = 0;
        }
    }

    /**
     * This method is not implemented and will never be.
     */
    public void remove() {

        // Not implemented.
    }

    /**
     * String representation of this Object.
     */
    public String toString() {
        StringBuffer message = new StringBuffer();

        message.append("\n[").append("\n\tId: ").append(myResourceId).append("\n\tName: ").append(myResourceName).append("\n\tPoolName: ").append(myDBPoolName).append("\n\tProcName: ").append(
            myProcName).append("\n\tInput:").append("\n\t[");

        for (ResourceAttr attr : myInputAttrList) {
            message.append("\n\t\tName: ").append(attr.getAttrName()).append("\n\t\tType: ").append(attr.getAttrType()).append("\n\t\tSequence: ").append(attr.getAttrSequence());

            switch (attr.getAttrType()) {
            case BOOLEAN :
                message.append("\n\t\tValue: ").append(attr.getBitValue());

                break;

            case DATETIME :
                message.append("\n\t\tValue: ").append(attr.getDateValue());

                break;

            case INTEGER :
                message.append("\n\t\tValue: ").append(attr.getIntValue());

                break;

            case NUMERIC :
                message.append("\n\t\tValue: ").append(attr.getRealValue());

                break;

            case STRING :
                message.append("\n\t\tValue: ").append(attr.getStringValue());

                break;
            }

            message.append("\n");
        }

        message.append("\n\t]").append("\n\tOutput:").append("\n\t[");

        for (ResourceAttr attr : myOutputAttrList) {
            message.append("\n\t\tName: ").append(attr.getAttrName()).append("\n\t\tType: ").append(attr.getAttrType()).append("\n\t\tSequence: ").append(attr.getAttrSequence());

            switch (attr.getAttrType()) {
            case BOOLEAN :
                message.append("\n\t\tValue: ").append(attr.getBitValue());

                break;

            case DATETIME :
                message.append("\n\t\tValue: ").append(attr.getDateValue());

                break;

            case INTEGER :
                message.append("\n\t\tValue: ").append(attr.getIntValue());

                break;

            case NUMERIC :
                message.append("\n\t\tValue: ").append(attr.getRealValue());

                break;

            case STRING :
                message.append("\n\t\tValue: ").append(attr.getStringValue());

                break;
            }

            message.append("\n");
        }

        message.append("\n\t]").append("\n]");

        return message.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for DBPoolName property.
     *
     * @return Current Value of DBPoolName
     *
     */
    public String getDBPoolName() {
        return myDBPoolName;
    }

    /**
     * Accessor method for InputAttrList property.
     *
     * @return Current Value of InputAttrList
     *
     */
    public ArrayList<ResourceAttr> getInputAttrList() {
        return myInputAttrList;
    }

    /**
     * This method returns the iterator for this object.
     */
    public Iterator getIterator() {
        return this;
    }

    /**
     * Accessor method for OutputAttrList property.
     *
     * @return Current Value of OutputAttrList
     *
     */
    public ArrayList<ResourceAttr> getOutputAttrList() {
        return myOutputAttrList;
    }

    /**
     * Accessor method for ProcName property.
     *
     * @return Current Value of ProcName
     *
     */
    public String getProcName() {
        return myProcName;
    }

    /**
     * Accessor method for ResourceId property.
     *
     * @return Current Value of ResourceId
     *
     */
    public int getResourceId() {
        return myResourceId;
    }

    /**
     * Accessor method for ResourceName property.
     *
     * @return Current Value of ResourceName
     *
     */
    public String getResourceName() {
        return myResourceName;
    }

    /**
     * This method builds a ResourceResultMap from the current row in the
     * resultset.
     *
     * @param rs                Resultset object.
     * @param outputAttrList    List of output attributes.
     * @return  ResourceResultMap
     */
    private ResourceResultMap getResultMap(ResultSet rs, ArrayList<ResourceAttr> outAttrList) {
        Hashtable<String, ResourceAttr> table = new Hashtable<String, ResourceAttr>();

        // Get the value for each of the output attributes
        for (ResourceAttr attr : outAttrList) {
            ResourceAttr  curAttr  = new ResourceAttr(attr);
            String        attrName = curAttr.getAttrName();
            AttributeType attrType = curAttr.getAttrType();

            /*
             * During the process of reading the value of the
             * output parameter by name, there is a chance of
             * exception getting thrown. LOG this exception and
             * continue processing the other results.
             */
            try {
                switch (attrType) {
                case BOOLEAN :
                    curAttr.setBitValue(rs.getBoolean(attrName));

                    break;

                case DATETIME :
                    curAttr.setDateValue(Timestamp.getTimestamp(rs.getTimestamp(attrName)));

                    break;

                case INTEGER :
                    curAttr.setIntValue(rs.getInt(attrName));

                    break;

                case NUMERIC :
                    curAttr.setRealValue(rs.getDouble(attrName));

                    break;

                case STRING :
                    curAttr.setStringValue(rs.getString(attrName));

                    break;
                }    // End Switch

                table.put(attrName, curAttr);
            }        // End Try
                    catch (SQLException sqle) {
                StringBuffer message = new StringBuffer();

                message.append("An exception has occured while ").append("retrieving the values of output ").append("attributes\n").append("Resource Name: " + myResourceName).append("\n");
                LOG.severe(sqle.toString());

                continue;
            }        // End Catch
        }            // End For

        ResourceResultMap rrm = new ResourceResultMap(table);

        return rrm;
    }

    /**
     * This is an abstract method in the Iterator interface that returns true
     * if the iterator is not at the end of the list.
     */
    public boolean hasNext() {
        if (myResults == null) {
            LOG.warn("Either the resource is not realized or the resource is" + " empty.");

            return false;
        }

        if (myOutputCounter >= myResultsSize) {
            LOG.info(" Output counter: " + myOutputCounter + " ResultSize: " + myResultsSize);

            return false;
        }

        return true;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for DBPoolName property.
     *
     * @param aDBPoolName New Value for DBPoolName
     *
     */
    public void setDBPoolName(String aDBPoolName) {
        myDBPoolName = aDBPoolName;
    }

    /**
     * Mutator method for InputAttrList property.
     *
     * @param aInputAttrList New Value for InputAttrList
     *
     */
    public void setInputAttrList(ArrayList<ResourceAttr> aInputAttrList) {
        myInputAttrList = aInputAttrList;
    }

    /**
     * Mutator method for OutputAttrList property.
     *
     * @param aOutputAttrList New Value for OutputAttrList
     *
     */
    public void setOutputAttrList(ArrayList<ResourceAttr> aOutputAttrList) {
        myOutputAttrList = aOutputAttrList;
    }

    /**
     * Mutator method for ProcName property.
     *
     * @param aProcName New Value for ProcName
     *
     */
    public void setProcName(String aProcName) {
        myProcName = aProcName;
    }

    /**
     * Mutator method for ResourceId property.
     *
     * @param aResourceId New Value for ResourceId
     *
     */
    public void setResourceId(int aResourceId) {
        myResourceId = aResourceId;
    }

    /**
     * Mutator method for ResourceName property.
     *
     * @param aResourceName New Value for ResourceName
     *
     */
    public void setResourceName(String aResourceName) {
        myResourceName = aResourceName;
    }

    /**
     * This method sets the input values to the callable statement of the
     * resource.
     *
     * @param cs
     * @param aInputMap
     * @param attr
     * @throws SQLException
     */
    private void setValue(CallableStatement cs, Hashtable<String, Object> aInputMap, ResourceAttr attr) throws SQLException {
        int           counter    = attr.getAttrSequence();
        AttributeType attrType   = attr.getAttrType();
        boolean       isVariable = attr.getIsVariable();

        /*
         * Check if this input attribute is a variable. In such case,
         * the value will be found in the aInputMap.
         */
        if (isVariable == true) {
            String varName = attr.getVarName();
            Object varVal  = aInputMap.get(varName);

            LOG.info("var:" + varName + " value: " + varVal);

            switch (attrType) {
            case BOOLEAN :
                if ((varVal == null) || (varVal instanceof Boolean) == false) {
                    cs.setBoolean(counter, false);
                } else {
                    boolean value = (Boolean) varVal;

                    cs.setBoolean(counter, value);
                }

                break;

            case DATETIME :
                if ((varVal == null) || (varVal instanceof Timestamp) == false) {
                    cs.setTimestamp(counter, null);
                } else {
                    Timestamp value = (Timestamp) varVal;

                    cs.setTimestamp(counter, value.toSqlTimestamp());
                }

                break;

            case INTEGER :
                if ((varVal == null) || (varVal instanceof Integer) == false) {
                    cs.setInt(counter, 0);
                } else {
                    int value = (Integer) varVal;

                    cs.setInt(counter, value);
                }

                break;

            case NUMERIC :
                if ((varVal == null) || (varVal instanceof Double) == false) {
                    cs.setDouble(counter, 0);
                } else {
                    double value = (Double) varVal;

                    cs.setDouble(counter, value);
                }

                break;

            case STRING :
                if ((varVal == null) || (varVal instanceof String) == false) {
                    cs.setString(counter, "");
                } else {
                    String value = varVal.toString();

                    cs.setString(counter, value);
                }

                break;
            }
        } else {
            switch (attrType) {
            case BOOLEAN :
                cs.setBoolean(counter, attr.getBitValue());

                break;

            case DATETIME :
                cs.setTimestamp(counter, attr.getDateValue().toSqlTimestamp());

                break;

            case INTEGER :
                cs.setInt(counter, attr.getIntValue());

                break;

            case NUMERIC :
                cs.setDouble(counter, attr.getRealValue());

                break;

            case STRING :
                cs.setString(counter, attr.getStringValue());

                break;
            }
        }

        return;
    }
}
