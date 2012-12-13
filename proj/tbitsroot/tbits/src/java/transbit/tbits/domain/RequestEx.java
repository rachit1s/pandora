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
 * RequestEx.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.TimeZone;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the requests_ex table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class RequestEx implements Comparable<RequestEx>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID      = 1;
    private static final int REQUESTID     = 2;
    private static final int REALVALUE     = 7;
    private static final int INTVALUE      = 6;
    private static final int FIELDID       = 3;
    private static final int DATETIMEVALUE = 5;
    private static final int BITVALUE      = 4;
    private static final int VARCHARVALUE  = 8;
    private static final int TYPEVALUE     = 10;
    private static final int TEXTVALUE     = 9;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private boolean   myBitValue;
    private Timestamp myDateTimeValue;
    private int       myFieldId;
    private int       myIntValue;
    private double    myRealValue;
    private int       myRequestId;

    // Attributes of this Domain Object.
    private int    mySystemId;
    private String myTextValue;
    private int    myTextContentType;
    private int    myTypeValue;
    private String myVarcharValue;
    //ArrayList of RequestUser maintained corresponding to myVarcharValue
//    private ArrayList<RequestUser> myUserTypes;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public RequestEx() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aRequestId
     *  @param aFieldId
     *  @param aBitValue
     *  @param aDateTimeValue
     *  @param aIntValue
     *  @param aRealValue
     *  @param aVarcharValue
     *  @param aTextValue
     *  @param aTypeValue
     */
    public RequestEx(int aSystemId, int aRequestId, int aFieldId, boolean aBitValue, Timestamp aDateTimeValue, int aIntValue, double aRealValue, String aVarcharValue, String aTextValue,
                     int aTypeValue) {
        mySystemId      = aSystemId;
        myRequestId     = aRequestId;
        myFieldId       = aFieldId;
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
     * @param aObject Object to be compared.
     * @return 0 - If they are equal. 1 - If this is greater. -1 - If this is
     *         smaller.
     */
    public int compareTo(RequestEx aObject) {
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

            return myTextValue.compareTo(aObject.myTextValue);
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

    /**
     * This method creates a RequestEx object from the resultset's current row.
     *
     * @param aRS Resultset object.
     *
     * @return RequestEx Object.
     *
     * @exception SQLException incase of database errors.
     */
    public static RequestEx createFromResultSet(ResultSet aRS) throws SQLException {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		RequestEx rex = new RequestEx(aRS.getInt("sys_id"), aRS
				.getInt("request_id"), aRS.getInt("field_id"), aRS
				.getBoolean("bit_value"), Timestamp.getTimestamp(aRS
				.getTimestamp("datetime_value", cal)), aRS.getInt("int_value"),
				aRS.getDouble("real_value"),
				(aRS.getString("varchar_value") != null) ? aRS.getString("varchar_value") : null, 
				(aRS.getString("text_value") != null) ? aRS.getString("text_value") : null, 
				aRS.getInt("type_value"));
		try {
			rex.setTextContentType(aRS.getInt("text_content_type"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rex;

    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the RequestEx objects in sorted order
     */
    public static ArrayList<RequestEx> sort(ArrayList<RequestEx> source) {
        int         size     = source.size();
        RequestEx[] srcArray = new RequestEx[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new RequestExComparator());

        ArrayList<RequestEx> target = new ArrayList<RequestEx>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    /**
     * This method returns the string representation of an object of this class
     *
     * @return String representation of this object.
     */
    public String toString() {
        StringBuilder message = new StringBuilder();

        message.append("[ ").append(mySystemId).append(",").append(myRequestId).append(",").append(myFieldId).append(",").append(myBitValue).append(", ").append(myDateTimeValue).append(", ").append(
            myIntValue).append(", ").append(myRealValue).append(", ").append(myVarcharValue).append(", ").append(myTextValue).append(", ").append(myTypeValue).append(" ]");

        return message.toString();
    }

    //~--- get methods --------------------------------------------------------

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
     * Accessor method for FieldId property.
     *
     * @return Current Value of FieldId
     *
     */
    public int getFieldId() {
        return myFieldId;
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
     * Mutator method for BitValue property.
     *
     * @param aBitValue New Value for BitValue
     *
     */
    public void setBitValue(boolean aBitValue) {
        myBitValue = aBitValue;
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
     * Mutator method for FieldId property.
     *
     * @param aFieldId New Value for FieldId
     *
     */
    public void setFieldId(int aFieldId) {
        myFieldId = aFieldId;
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
     * Mutator method for VarcharValue property.
     *
     * @param aVarcharValue New Value for VarcharValue
     *
     */
    public void setVarcharValue(String aVarcharValue) {
        myVarcharValue = aVarcharValue;
    }

//	public void setUserTypes(ArrayList<RequestUser> reqUsers) {
//			myUserTypes=reqUsers;
//		
//	}

//	public ArrayList<RequestUser> getUserTypes() {
//		  return myUserTypes;
//	}

	public void setTextContentType(int myTextContentType) {
		this.myTextContentType = myTextContentType;
	}

	public int getTextContentType() {
		return myTextContentType;
	}
    
}


/**
 * This class is the comparator for domain object corresponding to the
 * requests_ex table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class RequestExComparator implements Comparator<RequestEx>, Serializable {
    public int compare(RequestEx obj1, RequestEx obj2) {
        return obj1.compareTo(obj2);
    }
}
