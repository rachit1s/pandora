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
 * JobNotifier.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.TBitsLogger;

//Imports from the current package.
//Other TBits Imports.
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
 * This class is the domain object corresponding to the job_notifiers table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class JobNotifier implements Comparable<JobNotifier>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int JOBID  = 1;
    private static final int USERID = 2;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    // Attributes of this Domain Object.
    private int myJobId;
    private int myUserId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public JobNotifier() {}

    /**
     * The complete constructor.
     *
     *  @param aJobId
     *  @param aUserId
     */
    public JobNotifier(int aJobId, int aUserId) {
        myJobId  = aJobId;
        myUserId = aUserId;
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
    public int compareTo(JobNotifier aObject) {
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
        }

        return 0;
    }

    /**
     * This method constructs a JobNotifier object from the resultset.
     *
     * @param aRS     ResultSet which points to a JobNotifier record.
     *
     * @return JobNotifier object
     *
     * @exception SQLException
     */
    public static JobNotifier createFromResultSet(ResultSet aRS) throws SQLException {
        JobNotifier jobDef = new JobNotifier(aRS.getInt("job_id"), aRS.getInt("user_id"));

        return jobDef;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the JobNotifier objects in sorted order
     */
    public static ArrayList<JobNotifier> sort(ArrayList<JobNotifier> source) {
        int           size     = source.size();
        JobNotifier[] srcArray = new JobNotifier[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new JobNotifierComparator());

        ArrayList<JobNotifier> target = new ArrayList<JobNotifier>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

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
 * job_notifiers table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class JobNotifierComparator implements Comparator<JobNotifier>, Serializable {
    public int compare(JobNotifier obj1, JobNotifier obj2) {
        return obj1.compareTo(obj2);
    }
}
