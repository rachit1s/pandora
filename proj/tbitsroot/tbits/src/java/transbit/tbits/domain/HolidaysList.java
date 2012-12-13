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
 * HolidaysList.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

//Imports from the current package.
//Other TBits Imports.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;

//Static imports from the mapper.
import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;
import static transbit.tbits.api.Mapper.ourHolidaysListMap;
import static transbit.tbits.api.Mapper.ourHolidaysMap;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the holidays_list table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class HolidaysList implements Comparable<HolidaysList>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    public static final int OFFICE       = 1;
    public static final int HOLIDAY_DATE = 2;
    public static final int OFFICE_ZONE  = 3;
    public static final int DESCRIPTION  = 4;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private String    myDescription;
    private Timestamp myHolidayDate;

    // Attributes of this Domain Object.
    private String myOffice;
    private String myOfficeZone;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public HolidaysList() {}

    /**
     * The complete constructor.
     *
     *  @param aOffice
     *  @param aHolidayDate
     *  @param aOfficeZone
     *  @param aDescription
     */
    public HolidaysList(String aOffice, Timestamp aHolidayDate, String aOfficeZone, String aDescription) {
        myOffice      = aOffice;
        myHolidayDate = aHolidayDate;
        myOfficeZone  = aOfficeZone;
        myDescription = aDescription;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method that compares this object with the one passed W.R.T the ourSortField.
     *
     * @param aObject  Object to be compared.
     *
     * @return 0 - If they are equal.
     *         1 - If this is greater.
     *        -1 - If this is smaller.
     *
     */
    public int compareTo(HolidaysList aObject) {
        switch (ourSortField) {
        case OFFICE : {
            if (ourSortOrder == ASC_ORDER) {
                return myOffice.compareTo(aObject.myOffice);
            } else {
                return aObject.myOffice.compareTo(myOffice);
            }
        }

        case HOLIDAY_DATE : {
            if (ourSortOrder == ASC_ORDER) {
                return myHolidayDate.compareTo(aObject.myHolidayDate);
            } else {
                return aObject.myHolidayDate.compareTo(myHolidayDate);
            }
        }

        case OFFICE_ZONE : {
            if (ourSortOrder == ASC_ORDER) {
                return myOfficeZone.compareTo(aObject.myOfficeZone);
            } else {
                return aObject.myOfficeZone.compareTo(myOfficeZone);
            }
        }

        case DESCRIPTION : {
            if (ourSortOrder == ASC_ORDER) {
                return myDescription.compareTo(aObject.myDescription);
            } else {
                return aObject.myDescription.compareTo(myDescription);
            }
        }
        }

        return 0;
    }

    /**
     * This method is used to create the HolidaysList  object
     * from the ResultSet
     *
     * @param  aResultSet the result object containing the fields
     * corresponding to a row of the HolidaysList table in the database
     * @return the corresponding User object created from the ResutlSet
     */
    public static HolidaysList createFromResultSet(ResultSet aResultSet) throws SQLException {
        HolidaysList hl = new HolidaysList(aResultSet.getString("office"), Timestamp.getTimestamp(aResultSet.getTimestamp("holiday_date")), aResultSet.getString("office_zone"),
                                           aResultSet.getString("description"));

        return hl;
    }

    /**
     * This method is used to compare two HolidaysList objects.
     *
     * @param aHolidaysList HolidaysList object.
     *
     * @return True if equal and False otherwise.
     *
     */
    public boolean equals(HolidaysList aHolidaysList) {
        if (aHolidaysList == null) {
            return false;
        }

        return (myHolidayDate.equals(aHolidaysList.myHolidayDate) && myOfficeZone.equalsIgnoreCase(aHolidaysList.myOfficeZone));
    }

    /**
     * This method is used to compare two Field objects.
     *
     * @param o Field object.
     *
     * @return True if equal and False otherwise.
     *
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        HolidaysList hl = null;

        try {
            hl = (HolidaysList) o;
        } catch (ClassCastException cce) {
            return false;
        }

        return (myHolidayDate.equals(hl.myHolidayDate) && myOfficeZone.equalsIgnoreCase(hl.myOfficeZone));
    }

    /*
     */
    public static HolidaysList lookupByDateAndZone(Date aDate, String aZone) {
        if (ourHolidaysListMap != null) {
            String       date = Timestamp.toCustomFormat(aDate, "MM/dd/yyyy");
            String       key  = aZone + "-" + date;
            HolidaysList hl   = ourHolidaysMap.get(key);
        }

        return null;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the HolidaysList objects in sorted order
     */
    public static ArrayList<HolidaysList> sort(ArrayList<HolidaysList> source) {
        int            size     = source.size();
        HolidaysList[] srcArray = new HolidaysList[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new HolidaysListComparator());

        ArrayList<HolidaysList> target = new ArrayList<HolidaysList>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

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
     * Accessor method for holiday_date property.
     *
     * @return Current Value of holiday_date
     *
     */
    public Timestamp getHolidayDate() {
        return myHolidayDate;
    }

    /**
     * This method returns the list of holidays.
     *
     * @return  List of holidays.
     */
    public static ArrayList<HolidaysList> getHolidays() {
        ArrayList<HolidaysList> finalList = new ArrayList<HolidaysList>();

        if (ourHolidaysListMap != null) {

            // Get the list of zones and sort them.
            String[] zoneList = new String[ourHolidaysListMap.size()];

            ourHolidaysListMap.keySet().toArray(zoneList);
            Arrays.sort(zoneList);

            /*
             *  For each zone get the list of holidays and sort them on the
             * date and add them to the finallist.
             */
            for (int i = 0; i < zoneList.length; i++) {
                String                  zone = zoneList[i];
                ArrayList<HolidaysList> list = ourHolidaysListMap.get(zone);

                if (list == null) {
                    continue;
                }

                HolidaysList.setSortParams(HolidaysList.HOLIDAY_DATE, ASC_ORDER);
                list = HolidaysList.sort(list);
                finalList.addAll(list);
            }
        } else {
            LOG.info("List Map is null!!!");
        }

        return finalList;
    }

    /**
     * This method returns the list of holidays for the given list of zones.
     */
    public static void getHolidays(ArrayList<String> zoneList, Hashtable<String, ArrayList<HolidaysList>> holidayTable) {
        if (ourHolidaysListMap != null) {

            // Get the list of zones and sort them.
            String[] zList = new String[ourHolidaysListMap.size()];

            ourHolidaysListMap.keySet().toArray(zList);
            Arrays.sort(zList);

            /*
             *  For each zone get the list of holidays and sort them on the
             * date and add them to the finallist.
             */
            for (int i = 0; i < zList.length; i++) {
                String zone = zList[i];

                zoneList.add(zone);

                ArrayList<HolidaysList> list = ourHolidaysListMap.get(zone);

                if (list == null) {
                    continue;
                }

                HolidaysList.setSortParams(HolidaysList.HOLIDAY_DATE, ASC_ORDER);
                list = HolidaysList.sort(list);
                holidayTable.put(zone, list);
            }
        } else {
            LOG.info("List Map is null!!!");
        }
    }
    
    public static boolean isHoliday(Date date, String timeZone)
    {
    	Hashtable<String, ArrayList<HolidaysList>> holidayTable = new Hashtable<String, ArrayList<HolidaysList>>();
    	ArrayList<String> al = new ArrayList<String>();
    	al.add(timeZone);
    	getHolidays(al, holidayTable);
    	ArrayList<HolidaysList> holidays = holidayTable.get(timeZone);
    	//System.out.println("The holidays are: " + holidays);
    	if(holidays == null)
	{
		return false;
	}
		Calendar c1 = Calendar.getInstance();
		
		Calendar c2 = Calendar.getInstance();
		c2.setTimeInMillis(date.getTime());
    	for(HolidaysList hl:holidays)
    	{
    		long time = hl.getHolidayDate().getTime();
    		c1.setTimeZone(TimeZone.getTimeZone(hl.myOfficeZone));
    		c1.setTimeInMillis(time);
    		
    		if(
    				(c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) 
    				&& (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)) 
    				&& (c1.get(Calendar.DATE) == c2.get(Calendar.DATE))
    		  )
    		{
    			return true;
    		}
    	}
    	return false;
    }
    /**
     * Accessor method for office property.
     *
     * @return Current Value of office
     *
     */
    public String getOffice() {
        return myOffice;
    }

    /**
     * Accessor method for office_zone property.
     *
     * @return Current Value of office_zone
     *
     */
    public String getOfficeZone() {
        return myOfficeZone;
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
        aCS.setString(OFFICE, myOffice);
        aCS.setTimestamp(HOLIDAY_DATE, myHolidayDate.toSqlTimestamp());
        aCS.setString(OFFICE_ZONE, myOfficeZone);
        aCS.setString(DESCRIPTION, myDescription);
    }

    /**
     * Mutator method for Description property.
     *
     * @param aDescription New Value for Description
     *
     */
    public void setDescription(String aDescription) {
        myDescription = aDescription;
    }

    /**
     * Mutator method for holiday_date property.
     *
     * @param aHolidayDate New Value for holiday_date
     *
     */
    public void setHolidayDate(Timestamp aHolidayDate) {
        myHolidayDate = aHolidayDate;
    }

    /**
     * Mutator method for office property.
     *
     * @param aOffice New Value for office
     *
     */
    public void setOffice(String aOffice) {
        myOffice = aOffice;
    }

    /**
     * Mutator method for office_zone property.
     *
     * @param aOfficeZone New Value for office_zone
     *
     */
    public void setOfficeZone(String aOfficeZone) {
        myOfficeZone = aOfficeZone;
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
 * holidays_list table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class HolidaysListComparator implements Comparator<HolidaysList>, Serializable {
    public int compare(HolidaysList obj1, HolidaysList obj2) {
        return obj1.compareTo(obj2);
    }
}
