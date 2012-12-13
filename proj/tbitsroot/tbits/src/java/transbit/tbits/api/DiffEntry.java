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
 * DiffEntry.java
 *
 * $Header:
 *
 */
package transbit.tbits.api;

//Imports from the current package.
//Java Imports.
//Third party imports.

/**
 * This class encapsulates the entries in the Diff generated between two states
 * of a request.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 *
 */
public class DiffEntry {
    private String myDisplayName;

    // Attributes of this Domain Object.
    private String myName;
    private String myNewValue;
    private String myOldValue;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public DiffEntry() {}

    /**
     * The complete constructor.
     *
     *  @param aName
     *  @param aDisplayName
     *  @param aOldValue
     *  @param aNewValue
     */
    public DiffEntry(String aName, String aDisplayName, String aOldValue, String aNewValue) {
        myName        = aName;
        myDisplayName = aDisplayName;
        myOldValue    = aOldValue;
        myNewValue    = aNewValue;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for DisplayName property.
     *
     * @return Current Value of DisplayName
     *
     */
    public String getDisplayName() {
        return myDisplayName;
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
     * Accessor method for NewValue property.
     *
     * @return Current Value of NewValue
     *
     */
    public String getNewValue() {
        return myNewValue;
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

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for DisplayName property.
     *
     * @param aDisplayName New Value for DisplayName
     *
     */
    public void setDisplayName(String aDisplayName) {
        myDisplayName = aDisplayName;
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
     * Mutator method for NewValue property.
     *
     * @param aNewValue New Value for NewValue
     *
     */
    public void setNewValue(String aNewValue) {
        myNewValue = aNewValue;
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
}
