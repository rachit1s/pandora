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
 * RuleAction.java
 *
 * $Header:
 *
 */
package transbit.tbits.config;

//~--- non-JDK imports --------------------------------------------------------

import java.io.Serializable;

import transbit.tbits.config.BusinessRule.Operator;

//~--- classes ----------------------------------------------------------------

/**
 * This class encapsulates the condition part of a BusinessRule in TBits.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public class RuleAction implements Serializable {
    private ActionType myActionType;
    private String     myFieldName;
    private Operator   myOperator;
    private String     myValue;

    //~--- constant enums -----------------------------------------------------

    // Enums for the type of action.
    public enum ActionType { VALIDATE, MODIFY; }

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public RuleAction() {}

    /**
     * The complete constructor.
     *
     *  @param aActionType
     *  @param aFieldName
     *  @param aValue
     *  @param aOperator
     */
    public RuleAction(ActionType aActionType, String aFieldName, String aValue, Operator aOperator) {
        myActionType = aActionType;
        myFieldName  = aFieldName;
        myValue      = aValue;
        myOperator   = aOperator;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for ActionType property.
     *
     * @return Current Value of ActionType
     *
     */
    public ActionType getActionType() {
        return myActionType;
    }

    /**
     * Accessor method for FieldName property.
     *
     * @return Current Value of FieldName
     *
     */
    public String getFieldName() {
        return myFieldName;
    }

    /**
     * Accessor method for Operator property.
     *
     * @return Current Value of Operator
     *
     */
    public Operator getOperator() {
        return myOperator;
    }

    /**
     * Accessor method for Value property.
     *
     * @return Current Value of Value
     *
     */
    public String getValue() {
        return myValue;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for ActionType property.
     *
     * @param aActionType New Value for ActionType
     *
     */
    public void setActionType(ActionType aActionType) {
        myActionType = aActionType;
    }

    /**
     * Mutator method for FieldName property.
     *
     * @param aFieldName New Value for FieldName
     *
     */
    public void setFieldName(String aFieldName) {
        myFieldName = aFieldName;
    }

    /**
     * Mutator method for Operator property.
     *
     * @param aOperator New Value for Operator
     *
     */
    public void setOperator(Operator aOperator) {
        myOperator = aOperator;
    }

    /**
     * Mutator method for Value property.
     *
     * @param aValue New Value for Value
     *
     */
    public void setValue(String aValue) {
        myValue = aValue;
    }
}
