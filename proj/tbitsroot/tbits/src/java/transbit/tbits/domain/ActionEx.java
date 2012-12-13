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
 * ActionEx.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

//TBits Imports.
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the actions_ex table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class ActionEx implements Comparable<ActionEx>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID      = 1;
    private static final int REQUESTID     = 2;
    private static final int ACTIONID      = 3;
    private static final int FIELDID       = 4;
    private static final int REALVALUE     = 8;
    private static final int INTVALUE      = 7;
    private static final int DATETIMEVALUE = 6;
    private static final int BITVALUE      = 5;
    private static final int VARCHARVALUE  = 9;
    private static final int TYPEVALUE     = 11;
    private static final int TEXTVALUE     = 10;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private int       myActionId;
    private boolean   myBitValue;
    private Timestamp myDateTimeValue;
    private int       myIntValue;
    private double    myRealValue;
    private int       myRequestId;

    // Attributes of this Domain Object.
    private int    mySystemId;
    private String myTextValue;
    private int    myTextValueContentType;
    private int    myTypeValue;
    private int    myFieldId;
    private String myVarcharValue;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public ActionEx() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aRequestId
     *  @param aActionId
     *  @param aFieldId
     *  @param aBitValue
     *  @param aDateTimeValue
     *  @param aIntValue
     *  @param aRealValue
     *  @param aVarcharValue
     *  @param aTextValue
     *  @param aTypeValue
     */
    public ActionEx(int aSystemId, int aRequestId, int aActionId, int aFieldId, boolean aBitValue, Timestamp aDateTimeValue, int aIntValue, double aRealValue, String aVarcharValue,
                    String aTextValue, int aTypeValue) {
        mySystemId      = aSystemId;
        myRequestId     = aRequestId;
        myActionId      = aActionId;
        myFieldId    	= aFieldId;
        myBitValue      = aBitValue;
        myDateTimeValue = aDateTimeValue;
        myIntValue      = aIntValue;
        myRealValue     = aRealValue;
        myVarcharValue  = aVarcharValue;
        myTextValue     = aTextValue;
        myTypeValue     = aTypeValue;
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
    public int compareTo(ActionEx aObject) {
        switch (ourSortField) {
        case SYSTEMID : {
            Integer i1 = mySystemId;
            Integer i2 = aObject.mySystemId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case REQUESTID : {
            Integer i1 = myRequestId;
            Integer i2 = aObject.myRequestId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case ACTIONID : {
            Integer i1 = myActionId;
            Integer i2 = aObject.myActionId;

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

        case BITVALUE : {
            Boolean b1 = myBitValue;
            Boolean b2 = aObject.myBitValue;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }

        case DATETIMEVALUE : {
            if (ourSortOrder == ASC_ORDER) {
                return myDateTimeValue.compareTo(aObject.myDateTimeValue);
            }

            return aObject.myDateTimeValue.compareTo(myDateTimeValue);
        }

        case INTVALUE : {
            Integer i1 = myIntValue;
            Integer i2 = aObject.myIntValue;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case REALVALUE : {
            Double d1 = myRealValue;
            Double d2 = aObject.myRealValue;

            if (ourSortOrder == ASC_ORDER) {
                return d1.compareTo(d2);
            }

            return d2.compareTo(d1);
        }

        case VARCHARVALUE : {
            if (ourSortOrder == ASC_ORDER) {
                return myVarcharValue.compareTo(aObject.myVarcharValue);
            }

            return aObject.myVarcharValue.compareTo(myVarcharValue);
        }

        case TEXTVALUE : {
            if (ourSortOrder == ASC_ORDER) {
                return myTextValue.compareTo(aObject.myTextValue);
            }

            return aObject.myTextValue.compareTo(myTextValue);
        }

        case TYPEVALUE : {
            Integer i1 = myTypeValue;
            Integer i2 = aObject.myTypeValue;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }
        }

        return 0;
    }

    /*
     * This method creates an ActionEx object from the resultset's current row.
     *
     * @param aRS Resultset object.
     *
     * @return ActionEx Object.
     *
     * @exception SQLException incase of database errors.
     */
    public static ActionEx createFromResultSet(ResultSet aRS) throws SQLException {
        ActionEx aex = new ActionEx(aRS.getInt("sys_id"), aRS.getInt("request_id"), aRS.getInt("action_id"), aRS.getInt("field_id"), aRS.getBoolean("bit_value"),
                                    Timestamp.getTimestamp(aRS.getTimestamp("datetime_value")), aRS.getInt("int_value"), aRS.getDouble("real_value"), aRS.getString("varchar_value"),
                                    aRS.getString("text_value"), aRS.getInt("type_value"));
        try{
        	aex.setTextValueContentType(aRS.getInt("text_value_content_type"));
        }catch(Exception e){
        	e.printStackTrace();
        }
        return aex;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the ActionEx objects in sorted order
     */
    public static ArrayList<ActionEx> sort(ArrayList<ActionEx> source) {
        int        size     = source.size();
        ActionEx[] srcArray = new ActionEx[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new ActionExComparator());

        ArrayList<ActionEx> target = new ArrayList<ActionEx>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for ActionId property.
     *
     * @return Current Value of ActionId
     *
     */
    public int getActionId() {
        return myActionId;
    }

    /**
     * Accessor method for BitValue property.
     *
     * @return Current Value of BitValue
     *
     */
    public boolean getBitValue() {
        return myBitValue;
    }

    /**
     * Accessor method for DateTimeValue property.
     *
     * @return Current Value of DateTimeValue
     *
     */
    public Timestamp getDateTimeValue() {
        return myDateTimeValue;
    }

    /**
     * Accessor method for IntValue property.
     *
     * @return Current Value of IntValue
     *
     */
    public int getIntValue() {
        return myIntValue;
    }

    /**
     * Accessor method for RealValue property.
     *
     * @return Current Value of RealValue
     *
     */
    public double getRealValue() {
        return myRealValue;
    }

    /**
     * Accessor method for RequestId property.
     *
     * @return Current Value of RequestId
     *
     */
    public int getRequestId() {
        return myRequestId;
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
     * Accessor method for TextValue property.
     *
     * @return Current Value of TextValue
     *
     */
    public String getTextValue() {
        return myTextValue;
    }

    /**
     * Accessor method for TypeValue property.
     *
     * @return Current Value of TypeValue
     *
     */
    public int getTypeValue() {
        return myTypeValue;
    }

    /**
     * Accessor method for UserTypeId property.
     *
     * @return Current Value of UserTypeId
     *
     */
    public int getFieldId() {
        return myFieldId;
    }

    /**
     * Accessor method for VarcharValue property.
     *
     * @return Current Value of VarcharValue
     *
     */
    public String getVarcharValue() {
        return myVarcharValue;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for ActionId property.
     *
     * @param aActionId New Value for ActionId
     *
     */
    public void setActionId(int aActionId) {
        myActionId = aActionId;
    }

    /**
     * Mutator method for BitValue property.
     *
     * @param aBitValue New Value for BitValue
     *
     */
    public void setBitValue(boolean aBitValue) {
        myBitValue = aBitValue;
    }

    /**
     * This method sets the parameters in the CallableStatement.
     *
     * @param aCS     CallableStatement whose params should be set.
     *
     * @exception SQLException
     */
    public void setCallableParameters(CallableStatement aCS) throws SQLException {
        aCS.setInt(SYSTEMID, mySystemId);
        aCS.setInt(REQUESTID, myRequestId);
        aCS.setInt(ACTIONID, myActionId);
        aCS.setInt(FIELDID, myFieldId);
        aCS.setBoolean(BITVALUE, myBitValue);
        aCS.setTimestamp(DATETIMEVALUE, myDateTimeValue.toSqlTimestamp());
        aCS.setInt(INTVALUE, myIntValue);
        aCS.setDouble(REALVALUE, myRealValue);
        aCS.setString(VARCHARVALUE, myVarcharValue);
        aCS.setString(TEXTVALUE, myTextValue);
        aCS.setInt(TYPEVALUE, myTypeValue);
    }

    /**
     * Mutator method for DateTimeValue property.
     *
     * @param aDateTimeValue New Value for DateTimeValue
     *
     */
    public void setDateTimeValue(Timestamp aDateTimeValue) {
        myDateTimeValue = aDateTimeValue;
    }

    /**
     * Mutator method for IntValue property.
     *
     * @param aIntValue New Value for IntValue
     *
     */
    public void setIntValue(int aIntValue) {
        myIntValue = aIntValue;
    }

    /**
     * Mutator method for RealValue property.
     *
     * @param aRealValue New Value for RealValue
     *
     */
    public void setRealValue(double aRealValue) {
        myRealValue = aRealValue;
    }

    /**
     * Mutator method for RequestId property.
     *
     * @param aRequestId New Value for RequestId
     *
     */
    public void setRequestId(int aRequestId) {
        myRequestId = aRequestId;
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
     * Mutator method for TextValue property.
     *
     * @param aTextValue New Value for TextValue
     *
     */
    public void setTextValue(String aTextValue) {
        myTextValue = aTextValue;
    }

    /**
     * Mutator method for TypeValue property.
     *
     * @param aTypeValue New Value for TypeValue
     *
     */
    public void setTypeValue(int aTypeValue) {
        myTypeValue = aTypeValue;
    }

    /**
     * Mutator method for UserTypeId property.
     *
     * @param aUserTypeId New Value for UserTypeId
     *
     */
    public void setUserTypeId(int aUserTypeId) {
        myFieldId = aUserTypeId;
    }

    /**
     * Mutator method for VarcharValue property.
     *
     * @param aVarcharValue New Value for VarcharValue
     *
     */
    public void setVarcharValue(String aVarcharValue) {
        myVarcharValue = aVarcharValue;
    }
    
    public static ActionEx lookupBySystemIdRequestIdActionIdFieldId (int aSystemId, int aRequestId, int aActionId, int aFieldId) throws DatabaseException{
	    ActionEx actionEx = null;
	    Connection connection = null;
    	
    	try{
    		connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_actionex_lookupById ?, ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.setInt(3, aActionId);
            cs.setInt(4, aFieldId);
            
            // execute method returns a flag . It is true if the first
            // result is a resultSet object.
            boolean flag = cs.execute();

            if (flag == true) {
                ResultSet rsActionEx = cs.getResultSet();

                if (rsActionEx != null) {
                    while (rsActionEx.next() != false) {
                        actionEx = createFromResultSet(rsActionEx);
                    }

                    // This statement is not required here as the Statement
                    // object closes this resultset when we request for the
                    // next result in the row.
                    // rsAction.close();
                }                
            }

            cs.getMoreResults();
            cs.close();
            cs = null;
    		
	    } catch (SQLException sqle) {
	        StringBuilder message = new StringBuilder();
	
	        message.append("An exception occurred while retrieving the Action ").append("details for").append("\nSystem Id : ").append(aSystemId).append("\nRequest Id  : ").append(aRequestId).append(
	            "\nAction Id   : ").append(aActionId).append("\n");
	
	        throw new DatabaseException(message.toString(), sqle);
	    } finally {
	        try {
	            if (connection != null) {
	                connection.close();
	            }
	        } catch (SQLException sqle) {
	            LOG.warning("An Exception has occured while closing a request");
	        }
	    }
    	return actionEx;
	}    
    
    public static void main (String[] args){
    	
    	try {
    		ActionEx actionEx = null;
			actionEx = lookupBySystemIdRequestIdActionIdFieldId(2, 612, 1 ,45);
			System.out.println("ActionEx : \n" + "SysId:" + actionEx.getSystemId() +
					"reqId: "+ actionEx.getRequestId() + "actionId: " + actionEx.getActionId()
					+ "field_id: " + actionEx.getFieldId() + "typeId: " + actionEx.getTypeValue());
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

	public void setTextValueContentType(int myTextValueContentType) {
		this.myTextValueContentType = myTextValueContentType;
	}

	public int getTextValueContentType() {
		return myTextValueContentType;
	}
}



/**
 * This class is the comparator for domain object corresponding to the
 * actions_ex table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class ActionExComparator implements Comparator<ActionEx>, Serializable {
    public int compare(ActionEx obj1, ActionEx obj2) {
        return obj1.compareTo(obj2);
    }
}
