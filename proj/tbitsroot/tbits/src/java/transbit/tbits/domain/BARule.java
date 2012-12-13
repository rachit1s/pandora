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
 * BARule.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DataSourcePool;

//Imports from the current package.
//Other TBits Imports.
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.BusinessRule;

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

//Static imports.
import static transbit.tbits.api.Mapper.ourBARuleMap;

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

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the ba_rules table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class BARule implements Comparable<BARule>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID       = 1;
    private static final int SEQUENCENUMBER = 3;
    private static final int RULEID         = 2;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private WorkflowRule myRule;
    private int          myRuleId;
    private int          mySequenceNumber;

    // Attributes of this Domain Object.
    private int mySystemId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public BARule() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aRuleId
     *  @param aSequenceNumber
     */
    public BARule(int aSystemId, int aRuleId, int aSequenceNumber) {
        mySystemId       = aSystemId;
        myRuleId         = aRuleId;
        mySequenceNumber = aSequenceNumber;
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
    public int compareTo(BARule aObject) {
        switch (ourSortField) {
        case SYSTEMID : {
            Integer i1 = mySystemId;
            Integer i2 = aObject.mySystemId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case RULEID : {
            Integer i1 = myRuleId;
            Integer i2 = aObject.myRuleId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i1.compareTo(i1);
        }

        case SEQUENCENUMBER : {
            Integer i1 = mySequenceNumber;
            Integer i2 = aObject.mySequenceNumber;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }
        }

        return 0;
    }

    /**
     * This method create BARule object from the current row of the given
     * resultset.
     *
     * @param aRS ResultSet whose current row points to a BARule row.
     *
     * @return BARule object.
     *
     * @exception SQLException incase of any database related errors.
     */
    public static BARule createFromResultSet(ResultSet aRS) throws SQLException, DatabaseException {
        BARule br = new BARule(aRS.getInt("sys_id"), aRS.getInt("rule_id"), aRS.getInt("sequence_no"));

        return br;
    }

    /**
     * This method returns the BARule corresponding to the given id.
     *
     * @param aSystemId Id of the BA.
     *
     * @return BARule corresponding to the aRuleId.
     *
     * @exception DatabaseException incase of any database-related errors.
     */
    public static ArrayList<BARule> lookupBySystemId(int aSystemId) throws DatabaseException {
        ArrayList<BARule> ruleList = null;

        // Look in the mapper first.
        String key = Integer.toString(aSystemId);

        if (ourBARuleMap != null) {
            ruleList = ourBARuleMap.get(key);

            if (ruleList == null) {
                ruleList = new ArrayList<BARule>();
            }

            BARule.setSortParams(BARule.SEQUENCENUMBER, ASC_ORDER);
            ruleList = BARule.sort(ruleList);

            return ruleList;
        }

        // else go the databsae to fetch the record.
        LOG.info("Contacting the database.");
        ruleList = new ArrayList<BARule>();

        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_barules_lookupBySystemIdAndRuleId ?");

            cs.setInt(1, aSystemId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    BARule br = createFromResultSet(rs);

                    ruleList.add(br);
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

            message.append("An exception occurred while retrieving the ").append("WorkflowRule Object.").append("\nSystemId Id: ").append(aSystemId).append("\n");

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

        return ruleList;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the BARule objects in sorted order
     */
    public static ArrayList<BARule> sort(ArrayList<BARule> source) {
        int      size     = source.size();
        BARule[] srcArray = new BARule[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new BARuleComparator());

        ArrayList<BARule> target = new ArrayList<BARule>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for Rule property.
     *
     * @return Current Value of Rule
     *
     */
    public BusinessRule getRule() {
        if (myRule == null) {
            try {
                myRule = WorkflowRule.lookupByRuleId(myRuleId);
            } catch (DatabaseException de) {
                LOG.info("",(de));
            }
        }

        if (myRule == null) {
            return null;
        }

        return myRule.getBusinessRule();
    }
    
    /**
     * Accessor method for RuleId property.
     *
     * @return Current Value of RuleId
     *
     */
    public int getRuleId() {
        return myRuleId;
    }

    /**
     * Accessor method for Workflow rule property.
     *
     * @return Current Value of WorkflowRule
     *
     */
    public WorkflowRule getWorkFlowRule() {
        return myRule;
    }

    /**
     * Accessor method for SequenceNumber property.
     *
     * @return Current Value of SequenceNumber
     *
     */
    public int getSequenceNumber() {
        return mySequenceNumber;
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
     * This method inserts a given BARule object.
     *
     * @param aObject - BARule Object .
     *
     * @return 
     *
     * @exception DatabaseException in case of any database-related errors.
     */
    public static void insert (BARule aObject) throws DatabaseException {    	
    	if (aObject == null)
    		return;
    	
        Connection connection = null;
        try {
            connection = DataSourcePool.getConnection();
            connection.setAutoCommit(false);
            
            CallableStatement cs = connection.prepareCall("stp_barule_insert ?, ?, ?");
            cs.setInt(1, aObject.mySystemId);
            cs.setInt(2, aObject.myRuleId);
            cs.setInt(3, aObject.mySequenceNumber);
            cs.execute();
            cs.close();
            connection.commit();
            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //           
            cs = null;
            
        } catch (SQLException sqle) {
        	
        	try {
        		if(connection != null)
					connection.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while inserting ").append("BARule Object.").append("\nRule Id: ").append(aObject.getRuleId()).append("\n");

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
        aCS.setInt(RULEID, myRuleId);
        aCS.setInt(SEQUENCENUMBER, mySequenceNumber);
    }

    /**
     * Mutator method for RuleId property.
     *
     * @param aRuleId New Value for RuleId
     *
     */
    public void setRuleId(int aRuleId) throws DatabaseException {
        myRuleId = aRuleId;

        // Update the underlying workflow rule object.
        myRule = null;
        getRule();
    }

    /**
     * Mutator method for SequenceNumber property.
     *
     * @param aSequenceNumber New Value for SequenceNumber
     *
     */
    public void setSequenceNumber(int aSequenceNumber) {
        mySequenceNumber = aSequenceNumber;
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
 * This class is the comparator for domain object corresponding to the ba_rules
 * table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class BARuleComparator implements Comparator<BARule>, Serializable {
    public int compare(BARule obj1, BARule obj2) {
        return obj1.compareTo(obj2);
    }
}
