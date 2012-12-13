/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/**
 * CustomLink.java
 *
 * $Header:
 */
package transbit.tbits.config;

import java.io.Serializable;

/**
 * This class represents a custom link in the XML Schema that stores the
 * BusinessArea-Specific Configuration.
 *
 * @author  : Vaibhav
 * @version : $Id: $
 */
public class CustomLink implements Serializable{
    private String myName;
    private String myValue;

    //~--- constructors -------------------------------------------------------

    /**
     * The Default Constructor.
     */
    public CustomLink() {
        myName  = "";
        myValue = "";
    }

    /**
     * The parameterized constructor that initializes its members with the
     * values passed.
     *
     * @param  aName   Name of the Custom Link.
     * @param  aValue  Value of the Custom Link.
     */
    public CustomLink(String aName, String aValue) {
        myName  = aName;
        myValue = aValue;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This is an accessor method of the Name property of CustomLink.
     *
     * @return Current Value of Name Property.
     */
    public String getName() {
        return myName;
    }

    /**
     * This is an accessor method of the Value property of CustomLink.
     *
     * @return Current Value of Value Property.
     */
    public String getValue() {
        return myValue;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * This is a mutator method of the Name property of CustomLink.
     *
     * @param aName  new value of Name Property.
     */
    public void setName(String aName) {
        myName = aName;
    }

    /**
     * This is a mutator method of the Value property of CustomLink.
     *
     * @param aValue  new value of Value Property.
     */
    public void setValue(String aValue) {
        myValue = aValue;
    }
}
