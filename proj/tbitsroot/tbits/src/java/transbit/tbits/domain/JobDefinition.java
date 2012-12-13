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
 * JobDefinition.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

//TBits Imports.
import transbit.tbits.common.TBitsLogger;

//Static Imports.
import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the job_definitions table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class JobDefinition implements Comparable<JobDefinition>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int JOBID    = 1;
    private static final int USERID   = 2;
    private static final int MONTH    = 4;
    private static final int MINUTE   = 8;
    private static final int HOUR     = 7;
    private static final int DAY      = 6;
    private static final int DATE     = 5;
    private static final int CRITERIA = 3;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private String myCriteria;
    private int    myDate;
    private int    myDay;
    private int    myHour;

    // Attributes of this Domain Object.
    private int myJobId;
    private int myMinute;
    private int myMonth;
    private int myUserId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public JobDefinition() {}

    /**
     * The complete constructor.
     *
     *  @param aJobId
     *  @param aUserId
     *  @param aCriteria
     *  @param aMonth
     *  @param aDate
     *  @param aDay
     *  @param aHour
     *  @param aMinute
     */
    public JobDefinition(int aJobId, int aUserId, String aCriteria, int aMonth, int aDate, int aDay, int aHour, int aMinute) {
        myJobId    = aJobId;
        myUserId   = aUserId;
        myCriteria = aCriteria;
        myMonth    = aMonth;
        myDate     = aDate;
        myDay      = aDay;
        myHour     = aHour;
        myMinute   = aMinute;
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
    public int compareTo(JobDefinition aObject) {
        switch (ourSortField) {
        case JOBID : {
            Integer i1 = myJobId;
            Integer i2 = aObject.myJobId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case USERID : {
            Integer i1 = myUserId;
            Integer i2 = aObject.myUserId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case CRITERIA : {
            if (ourSortOrder == ASC_ORDER) {
                return myCriteria.compareTo(aObject.myCriteria);
            }

            return aObject.myCriteria.compareTo(myCriteria);
        }

        case MONTH : {
            Integer i1 = myMonth;
            Integer i2 = aObject.myMonth;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case DATE : {
            Integer i1 = myDate;
            Integer i2 = aObject.myDate;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case DAY : {
            Integer i1 = myDay;
            Integer i2 = aObject.myDay;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case HOUR : {
            Integer i1 = myHour;
            Integer i2 = aObject.myHour;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case MINUTE : {
            Integer i1 = myMinute;
            Integer i2 = aObject.myMinute;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }
        }

        return 0;
    }

    /**
     * This method constructs a JobDefinition object from the resultset.
     *
     * @param aRS     ResultSet which points to a JobNotifier record.
     *
     * @return JobDefinition object
     *
     * @exception SQLException
     */
    public static JobDefinition createFromResultSet(ResultSet aRS) throws SQLException {
        JobDefinition jobDef = new JobDefinition(aRS.getInt("job_id"), aRS.getInt("job_user_id"), aRS.getString("job_criteria"), aRS.getInt("job_month"), aRS.getInt("job_date"),
                                   aRS.getInt("job_day"), aRS.getInt("job_hour"), aRS.getInt("job_minute"));

        return jobDef;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the JobDefinition objects in sorted order
     */
    public static ArrayList<JobDefinition> sort(ArrayList<JobDefinition> source) {
        int             size     = source.size();
        JobDefinition[] srcArray = new JobDefinition[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new JobDefinitionComparator());

        ArrayList<JobDefinition> target = new ArrayList<JobDefinition>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for Criteria property.
     *
     * @return Current Value of Criteria
     *
     */
    public String getCriteria() {
        return myCriteria;
    }

    /**
     * Accessor method for Date property.
     *
     * @return Current Value of Date
     *
     */
    public int getDate() {
        return myDate;
    }

    /**
     * Accessor method for Day property.
     *
     * @return Current Value of Day
     *
     */
    public int getDay() {
        return myDay;
    }

    /**
     * Accessor method for Hour property.
     *
     * @return Current Value of Hour
     *
     */
    public int getHour() {
        return myHour;
    }

    /**
     * Accessor method for JobId property.
     *
     * @return Current Value of JobId
     *
     */
    public int getJobId() {
        return myJobId;
    }

    /**
     * Accessor method for Minute property.
     *
     * @return Current Value of Minute
     *
     */
    public int getMinute() {
        return myMinute;
    }

    /**
     * Accessor method for Month property.
     *
     * @return Current Value of Month
     *
     */
    public int getMonth() {
        return myMonth;
    }

    /**
     * Accessor method for UserId property.
     *
     * @return Current Value of UserId
     *
     */
    public int getUserId() {
        return myUserId;
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
        aCS.setInt(JOBID, myJobId);
        aCS.setInt(USERID, myUserId);
        aCS.setString(CRITERIA, myCriteria);
        aCS.setInt(MONTH, myMonth);
        aCS.setInt(DATE, myDate);
        aCS.setInt(DAY, myDay);
        aCS.setInt(HOUR, myHour);
        aCS.setInt(MINUTE, myMinute);
    }

    /**
     * Mutator method for Criteria property.
     *
     * @param aCriteria New Value for Criteria
     *
     */
    public void setCriteria(String aCriteria) {
        myCriteria = aCriteria;
    }

    /**
     * Mutator method for Date property.
     *
     * @param aDate New Value for Date
     *
     */
    public void setDate(int aDate) {
        myDate = aDate;
    }

    /**
     * Mutator method for Day property.
     *
     * @param aDay New Value for Day
     *
     */
    public void setDay(int aDay) {
        myDay = aDay;
    }

    /**
     * Mutator method for Hour property.
     *
     * @param aHour New Value for Hour
     *
     */
    public void setHour(int aHour) {
        myHour = aHour;
    }

    /**
     * Mutator method for JobId property.
     *
     * @param aJobId New Value for JobId
     *
     */
    public void setJobId(int aJobId) {
        myJobId = aJobId;
    }

    /**
     * Mutator method for Minute property.
     *
     * @param aMinute New Value for Minute
     *
     */
    public void setMinute(int aMinute) {
        myMinute = aMinute;
    }

    /**
     * Mutator method for Month property.
     *
     * @param aMonth New Value for Month
     *
     */
    public void setMonth(int aMonth) {
        myMonth = aMonth;
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
     * Mutator method for UserId property.
     *
     * @param aUserId New Value for UserId
     *
     */
    public void setUserId(int aUserId) {
        myUserId = aUserId;
    }
}


/**
 * This class is the comparator for domain object corresponding to the
 * job_definitions table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class JobDefinitionComparator implements Comparator<JobDefinition>, Serializable {
    public int compare(JobDefinition obj1, JobDefinition obj2) {
        return obj1.compareTo(obj2);
    }
}
