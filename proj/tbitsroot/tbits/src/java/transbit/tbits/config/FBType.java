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
 * FBType.java
 *
 * $Header:
 *
 */
package transbit.tbits.config;

import java.io.Serializable;

//Imports from the current package.
//Other TBits Imports.
//Java Imports.
//Third party imports.

/**
 * This class is the domain object corresponding to the  table
 * in the database.
 *
 * @author  :
 * @version : $Id: $
 *
 */
public class FBType implements Serializable{
    private String  myIsChecked;
    private boolean myIsDefault;

    // Attributes of this Domain Object.
    private String myValue;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public FBType() {}

    /**
     * The complete constructor.
     *
     *  @param aValue
     *  @param aIsDefault
     *  @param aIsChecked
     */
    public FBType(String aValue, boolean aIsDefault, String aIsChecked) {
        myValue     = aValue;
        myIsDefault = aIsDefault;
        myIsChecked = aIsChecked;
    }

    //~--- methods ------------------------------------------------------------

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[").append("Value: ").append(myValue).append(", Default: ").append(myIsDefault).append(", Checked: ").append(myIsChecked).append("] ");

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for IsChecked property.
     *
     * @return Current Value of IsChecked
     *
     */
    public String getIsChecked() {
        return myIsChecked;
    }

    /**
     * Accessor method for IsDefault property.
     *
     * @return Current Value of IsDefault
     *
     */
    public boolean getIsDefault() {
        return myIsDefault;
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
     * Mutator method for IsChecked property.
     *
     * @param aIsChecked New Value for IsChecked
     *
     */
    public void setIsChecked(String aIsChecked) {
        myIsChecked = aIsChecked;
    }

    /**
     * Mutator method for IsDefault property.
     *
     * @param aIsDefault New Value for IsDefault
     *
     */
    public void setIsDefault(boolean aIsDefault) {
        myIsDefault = aIsDefault;
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
