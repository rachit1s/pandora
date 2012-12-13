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
 * WorkflowRule.java
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

//Static imports from the mapper.
import static transbit.tbits.api.Mapper.ourWorkflowRuleMap;

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
 * This class is the domain object corresponding to the workflow_rules table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class WorkflowRule implements Comparable<WorkflowRule>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int RULEID         = 1;
    private static final int RULEDEFINITION = 3;
    private static final int NAME           = 2;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private BusinessRule myBusinessRule;
    private String       myName;
    private String       myRuleDefinition;

    // Attributes of this Domain Object.
    private int myRuleId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public WorkflowRule() {}

    /**
     * The complete constructor.
     *
     *  @param aRuleId
     *  @param aName
     *  @param aRuleDefinition
     */
    public WorkflowRule(int aRuleId, String aName, String aRuleDefinition) {
        myRuleId         = aRuleId;
        myName           = aName;
        myRuleDefinition = aRuleDefinition;
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
    public int compareTo(WorkflowRule aObject) {
        switch (ourSortField) {
        case RULEID : {
            Integer i1 = myRuleId;
            Integer i2 = aObject.myRuleId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case NAME : {
            if (ourSortOrder == ASC_ORDER) {
                return myName.compareTo(aObject.myName);
            }

            return aObject.myName.compareTo(myName);
        }

        case RULEDEFINITION : {
            if (ourSortOrder == ASC_ORDER) {
                return myRuleDefinition.compareTo(aObject.myRuleDefinition);
            }

            return aObject.myRuleDefinition.compareTo(myRuleDefinition);
        }
        }

        return 0;
    }

    /**
     * This method create WorkflowRule object from the current row of the given
     * resultset.
     *
     * @param aRS ResultSet whose current row points to a WorkflowRule row.
     *
     * @return WorkflowRule object.
     *
     * @exception SQLException incase of any database related errors.
     */
    public static WorkflowRule createFromResultSet(ResultSet aRS) throws SQLException {
        WorkflowRule wr = new WorkflowRule(aRS.getInt("rule_id"), aRS.getString("rule_name"), aRS.getString("rule_definition"));

        return wr;
    }

    /**
     * This method is used to compare two WorkFlowRule objects.
     *
     * @param o WorkflowRule object.
     *
     * @return True if equal and False otherwise.
     *
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        WorkflowRule aWorkflowRule = null;

        try {
            aWorkflowRule = (WorkflowRule) o;
        } catch (ClassCastException cce) {
            return false;
        }

        return (myRuleId == aWorkflowRule.getRuleId());
    }

    /**
     * This method is used to compare two WorkFlowRule objects.
     *
     * @param aWorkflowRule WorkflowRule object.
     *
     * @return True if equal and False otherwise.
     *
     */
    public boolean equals(WorkflowRule aWorkflowRule) {
        if (aWorkflowRule == null) {
            return false;
        }

        return (myRuleId == aWorkflowRule.getRuleId());
    }

    /**
     * This method returns the workflow rule corresponding to the given id.
     *
     * @param aRuleId Id of the rule.
     *
     * @return WorkflowRule corresponding to the aRuleId.
     *
     * @exception DatabaseException incase of any database-related errors.
     */
    public static WorkflowRule lookupByRuleId(int aRuleId) throws DatabaseException {
        WorkflowRule wr = null;

        // Look in the mapper first.
        String key = Integer.toString(aRuleId);

        if (ourWorkflowRuleMap != null) {
            wr = ourWorkflowRuleMap.get(key);

            return wr;
        }

        // else go the databsae to fetch the record.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_wr_lookupByRuleId ?");

            cs.setInt(1, aRuleId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    wr = createFromResultSet(rs);
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

            message.append("An exception occurred while retrieving the ").append("WorkflowRule Object.").append("\nRule Id: ").append(aRuleId).append("\n");

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

        return wr;
    }
    
    /**
     * This method returns the workflow ruleId corresponding to the given workflowRule object.
     *
     * @param aObject - WorkflowRule Object .
     *
     * @return aRuleId after inserting the aObject.
     *
     * @exception DatabaseException in case of any database-related errors.
     */
    public static int insert (WorkflowRule aObject) throws DatabaseException {
    	int wrId = -1;
    	
    	if (aObject == null)
    		return wrId;
    	
        Connection connection = null;
        try {
            connection = DataSourcePool.getConnection();
            connection.setAutoCommit(false);
            
            CallableStatement cs = connection.prepareCall("{call dbo.stp_wr_insert (?, ?, ?)}");
            cs.setString(1, aObject.getName());
            cs.setString(2, aObject.getRuleDefinition());
            cs.registerOutParameter(3, java.sql.Types.INTEGER);
            cs.execute();
            wrId = cs.getInt(3);
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

            message.append("An exception occurred while inserting ").append("WorkflowRule Object.").append("\nRule Name: ").append(aObject.getName()).append("\n");

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

        return wrId;
    }    
    
    

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the WorkflowRule objects in sorted order
     */
    public static ArrayList<WorkflowRule> sort(ArrayList<WorkflowRule> source) {
        int            size     = source.size();
        WorkflowRule[] srcArray = new WorkflowRule[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new WorkflowRuleComparator());

        ArrayList<WorkflowRule> target = new ArrayList<WorkflowRule>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Returns the BusinessRule object corresponding to the Rule Definition.
     *
     * @return BusinessRule Object.
     *
     */
    public BusinessRule getBusinessRule() {
        if (myBusinessRule == null) {
            try {
                myBusinessRule = BusinessRule.parseRule(myRuleDefinition);
            } catch (Exception e) {
                LOG.error("An exception has occurred while parsing the " + "workflow rule: " + myName + "\n" + "",(e));
            }
        }

        return myBusinessRule;
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
     * Accessor method for RuleDefinition property.
     *
     * @return Current Value of RuleDefinition
     *
     */
    public String getRuleDefinition() {
        return myRuleDefinition;
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

    //~--- set methods --------------------------------------------------------

    /**
     * This method sets the parameters in the CallableStatement.
     *
     * @param aCS     CallableStatement whose params should be set.
     *
     * @exception SQLException
     */
    public void setCallableParameters(CallableStatement aCS) throws SQLException {
        aCS.setInt(RULEID, myRuleId);
        aCS.setString(NAME, myName);
        aCS.setString(RULEDEFINITION, myRuleDefinition);
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
     * Mutator method for RuleDefinition property.
     *
     * @param aRuleDefinition New Value for RuleDefinition
     *
     */
    public void setRuleDefinition(String aRuleDefinition) {
        myRuleDefinition = aRuleDefinition;
    }

    /**
     * Mutator method for RuleId property.
     *
     * @param aRuleId New Value for RuleId
     *
     */
    public void setRuleId(int aRuleId) {
        myRuleId = aRuleId;
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
 * workflow_rules table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class WorkflowRuleComparator implements Comparator<WorkflowRule>, Serializable {
    public int compare(WorkflowRule obj1, WorkflowRule obj2) {
        return obj1.compareTo(obj2);
    }
}
