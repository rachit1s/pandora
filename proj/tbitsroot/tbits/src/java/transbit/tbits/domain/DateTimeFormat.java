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
 * DateTimeFormat.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

//Other TBits Imports.
import transbit.tbits.common.TBitsLogger;

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

//Imports from the current package.
//Static TBits Imports
import static transbit.tbits.api.Mapper.ourDateFormatMap;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the datetime_formats table
 * in the database.
 *
 * @author  : Vinod Gupta,Vaibhav,g
 * @version : $Id: $
 *
 */
public class DateTimeFormat implements Comparable<DateTimeFormat>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int FORMATID = 1;
    private static final int FORMAT   = 2;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private String myFormat;

    // Attributes of this Domain Object.
    private int myFormatId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public DateTimeFormat() {}

    /**
     * The complete constructor.
     *
     *  @param aFormatId
     *  @param aFormat
     */
    public DateTimeFormat(int aFormatId, String aFormat) {
        myFormatId = aFormatId;
        myFormat   = aFormat;
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
    public int compareTo(DateTimeFormat aObject) {
        switch (ourSortField) {
        case FORMATID : {
            Integer i1 = myFormatId;
            Integer i2 = aObject.myFormatId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case FORMAT : {
            if (ourSortOrder == ASC_ORDER) {
                return myFormat.compareTo(aObject.myFormat);
            }

            return aObject.myFormat.compareTo(myFormat);
        }
        }

        return 0;
    }

    /**
     * This method is used to create the DateTimeFormat  object
     * from the ResultSet
     *
     * @param  aResultSet the result object containing the fields
     * corresponding to a row of the DateTimeFormat table in the database
     * @return the corresponding User object created from the ResutlSet
     */
    public static DateTimeFormat createFromResultSet(ResultSet aResultSet) throws SQLException {
        DateTimeFormat dfFormat = new DateTimeFormat(aResultSet.getInt("format_id"), aResultSet.getString("format"));

        return dfFormat;
    }

    /**
     * This method returns Date Time Format object for a given Id
     *
     * @return  Date Time Format Object
     */
    public static DateTimeFormat lookupByDateTimeFormatId(int aFormatId) {
        DateTimeFormat dtFormat = null;

        if (ourDateFormatMap != null) {
            dtFormat = ourDateFormatMap.get(Integer.toString(aFormatId));
        }

        return dtFormat;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the DateTimeFormat objects in sorted order
     */
    public static ArrayList<DateTimeFormat> sort(ArrayList<DateTimeFormat> source) {
        int              size     = source.size();
        DateTimeFormat[] srcArray = new DateTimeFormat[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new DateTimeFormatComparator());

        ArrayList<DateTimeFormat> target = new ArrayList<DateTimeFormat>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns all Date Time Format objects
     *
     * @return Arraylist of Date Time Format Object
     */
    public static ArrayList<DateTimeFormat> getAllDateTimeFormats() {
        ArrayList<DateTimeFormat> dtfList    = new ArrayList<DateTimeFormat>();
        DateTimeFormat            dtFormat   = null;
        Collection                collection = ourDateFormatMap.values();
        Iterator                  iterator   = collection.iterator();

        while (iterator.hasNext()) {
            dtFormat = (DateTimeFormat) iterator.next();
            dtfList.add(dtFormat);
        }

        DateTimeFormat.setSortParams(0, 0);
        dtfList = DateTimeFormat.sort(dtfList);

        return dtfList;
    }

    /**
     * Accessor method for Format property.
     *
     * @return Current Value of Format
     *
     */
    public String getFormat() {
        return myFormat;
    }

    /**
     * Accessor method for FormatId property.
     *
     * @return Current Value of FormatId
     *
     */
    public int getFormatId() {
        return myFormatId;
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
        aCS.setInt(FORMATID, myFormatId);
        aCS.setString(FORMAT, myFormat);
    }

    /**
     * Mutator method for Format property.
     *
     * @param aFormat New Value for Format
     *
     */
    public void setFormat(String aFormat) {
        myFormat = aFormat;
    }

    /**
     * Mutator method for FormatId property.
     *
     * @param aFormatId New Value for FormatId
     *
     */
    public void setFormatId(int aFormatId) {
        myFormatId = aFormatId;
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
}


/**
 * This class is the comparator for domain object corresponding to the
 * datetime_formats table in the database.
 *
 * @author  :
 * @version : $Id: $
 *
 */
class DateTimeFormatComparator implements Comparator<DateTimeFormat> {
    public int compare(DateTimeFormat obj1, DateTimeFormat obj2) {
        return obj1.compareTo(obj2);
    }
}
