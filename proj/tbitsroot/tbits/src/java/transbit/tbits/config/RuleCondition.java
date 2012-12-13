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
 * RuleCondition.java
 *
 * $Header:
 *
 */
package transbit.tbits.config;

//~--- non-JDK imports --------------------------------------------------------

import static transbit.tbits.config.BusinessRule.Operator;

import java.io.Serializable;

//~--- classes ----------------------------------------------------------------

/**
 * This class encapsulates the condition part of a BusinessRule in TBits.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public class RuleCondition implements Serializable{
    private Operator myCurrentOperator;
    private String   myCurrentValue;
    private String   myFieldName;
    private Operator myNewOperator;
    private String   myNewValue;
    private Operator myOldOperator;
    private String   myOldValue;
    private State    myState;

    //~--- constant enums -----------------------------------------------------

    public enum State { CURRENT, CHANGE }

    ;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor is private and so objects of this class can
     * be created using factory methods.
     */
    private RuleCondition() {}

    //~--- methods ------------------------------------------------------------

    /**
     *
     *
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\n<Check state=\"");

        switch (getState()) {
        case CURRENT :
            buffer.append("current");

            break;

        case CHANGE :
            buffer.append("change");

            break;
        }

        buffer.append("\" fieldName=\"").append(getFieldName()).append("\">");

        switch (getState()) {
        case CURRENT : {
            buffer.append("\n<Current value=\"").append(getCurrentValue()).append("\" operator=\"").append(getCurrentOperator()).append("\" />");
        }

        break;

        case CHANGE : {
            buffer.append("\n<Old value=\"").append(getOldValue()).append("\" operator=\"").append(getOldOperator()).append("\" />").append("\n<New value=\"").append(getNewValue()).append(
                "\" operator=\"").append(getNewOperator()).append("\" />");
        }

        break;
        }

        buffer.append("\n</Check>");

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * The Factory method to create a change rule.
     *
     *  @param aFieldName
     *  @param aOldValue
     *  @param aNewValue
     *  @param aOldOperator
     *  @param aNewOperator
     */
    public static RuleCondition getChangeRuleCondition(String aFieldName, String aOldValue, String aNewValue, Operator aOldOperator, Operator aNewOperator) {
        RuleCondition rc = new RuleCondition();

        rc.setState(State.CHANGE);
        rc.setFieldName(aFieldName);
        rc.setOldValue(aOldValue);
        rc.setNewValue(aNewValue);
        rc.setOldOperator(aOldOperator);
        rc.setNewOperator(aNewOperator);

        return rc;
    }

    /**
     * Accessor method for CurrentOperator property.
     *
     * @return Current Value of CurrentOperator
     *
     */
    public Operator getCurrentOperator() {
        return myCurrentOperator;
    }

    /**
     * The constructor to create a current rule.
     *
     *  @param aFieldName
     *  @param aCurValue
     *  @param aCurOperator
     */
    public static RuleCondition getCurrentRuleCondition(String aFieldName, String aCurValue, Operator aCurOperator) {
        RuleCondition rc = new RuleCondition();

        rc.setState(State.CURRENT);
        rc.setFieldName(aFieldName);
        rc.setCurrentValue(aCurValue);
        rc.setCurrentOperator(aCurOperator);

        return rc;
    }

    /**
     * Accessor method for CurrentValue property.
     *
     * @return Current Value of CurrentValue
     *
     */
    public String getCurrentValue() {
        return myCurrentValue;
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
     * Accessor method for NewOperator property.
     *
     * @return Current Value of NewOperator
     *
     */
    public Operator getNewOperator() {
        return myNewOperator;
    }

    /**
     * Accessor method for NewValue property.
     *
     * @return Current Value of NewValue
     *
     */
    public String getNewValue() {
        return myNewValue;
    }

    /**
     * Accessor method for OldOperator property.
     *
     * @return Current Value of OldOperator
     *
     */
    public Operator getOldOperator() {
        return myOldOperator;
    }

    /**
     * Accessor method for OldValue property.
     *
     * @return Current Value of OldValue
     *
     */
    public String getOldValue() {
        return myOldValue;
    }

    /**
     * Accessor method for State property.
     *
     * @return Current Value of State
     *
     */
    public State getState() {
        return myState;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for CurrentOperator property.
     *
     * @param aCurrentOperator New Value for CurrentOperator
     *
     */
    public void setCurrentOperator(Operator aCurrentOperator) {
        myCurrentOperator = aCurrentOperator;
    }

    /**
     * Mutator method for CurrentValue property.
     *
     * @param aCurrentValue New Value for CurrentValue
     *
     */
    public void setCurrentValue(String aCurrentValue) {
        myCurrentValue = aCurrentValue;
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
     * Mutator method for NewOperator property.
     *
     * @param aNewOperator New Value for NewOperator
     *
     */
    public void setNewOperator(Operator aNewOperator) {
        myNewOperator = aNewOperator;
    }

    /**
     * Mutator method for NewValue property.
     *
     * @param aNewValue New Value for NewValue
     *
     */
    public void setNewValue(String aNewValue) {
        myNewValue = aNewValue;
    }

    /**
     * Mutator method for OldOperator property.
     *
     * @param aOldOperator New Value for OldOperator
     *
     */
    public void setOldOperator(Operator aOldOperator) {
        myOldOperator = aOldOperator;
    }

    /**
     * Mutator method for OldValue property.
     *
     * @param aOldValue New Value for OldValue
     *
     */
    public void setOldValue(String aOldValue) {
        myOldValue = aOldValue;
    }

    /**
     * Mutator method for State property.
     *
     * @param aState New Value for State
     *
     */
    public void setState(State aState) {
        myState = aState;
    }
}
