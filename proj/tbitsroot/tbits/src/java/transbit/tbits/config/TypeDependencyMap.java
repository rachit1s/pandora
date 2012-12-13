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
 * TypeDependencyMap.java
 *
 * $Header:
 *
 */
package transbit.tbits.config;

//~--- JDK imports ------------------------------------------------------------

//Imports from the current package.
//Java Imports
import java.io.Serializable;
import java.util.ArrayList;

//~--- classes ----------------------------------------------------------------

/**
 * TODO: Appropriate Javadoc needs to be written here.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 *
 */
public class TypeDependencyMap implements Serializable{
    private ArrayList<String> myDependentList;
    private boolean           myExclude;

    // Attributes of this Domain Object.
    private String myPrimaryType;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public TypeDependencyMap() {}

    /**
     * The complete constructor.
     *
     *  @param aPrimaryType
     *  @param aExclude
     *  @param aDependentList
     */
    public TypeDependencyMap(String aPrimaryType, boolean aExclude, ArrayList<String> aDependentList) {
        myPrimaryType   = aPrimaryType;
        myExclude       = aExclude;
        myDependentList = aDependentList;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * String representation of this object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\n[ ").append(myPrimaryType).append(", ").append(myExclude).append(", ").append(myDependentList.toString()).append(" ]");

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for DependentList property.
     *
     * @return Current Value of DependentList
     *
     */
    public ArrayList<String> getDependentList() {
        return myDependentList;
    }

    /**
     * Accessor method for Exclude property.
     *
     * @return Current Value of Exclude
     *
     */
    public boolean getExclude() {
        return myExclude;
    }

    /**
     * Accessor method for PrimaryType property.
     *
     * @return Current Value of PrimaryType
     *
     */
    public String getPrimaryType() {
        return myPrimaryType;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for DependentList property.
     *
     * @param aDependentList New Value for DependentList
     *
     */
    public void setDependentList(ArrayList<String> aDependentList) {
        myDependentList = aDependentList;
    }

    /**
     * Mutator method for Exclude property.
     *
     * @param aExclude New Value for Exclude
     *
     */
    public void setExclude(boolean aExclude) {
        myExclude = aExclude;
    }

    /**
     * Mutator method for PrimaryType property.
     *
     * @param aPrimaryType New Value for PrimaryType
     *
     */
    public void setPrimaryType(String aPrimaryType) {
        myPrimaryType = aPrimaryType;
    }
}
