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
 * Resource.java
 * $Header:
 */
package transbit.tbits.external;

//~--- non-JDK imports --------------------------------------------------------

//TBits Imports
import transbit.tbits.common.DatabaseException;
import transbit.tbits.exception.TBitsException;

//~--- JDK imports ------------------------------------------------------------

//JDK Imports
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

//~--- interfaces -------------------------------------------------------------

/**
 * This interface defines the responsibilities of all the Resources. It extends
 * the Iterator interface.
 *
 * @author  Vaibhav.
 * @version  $Id: $
 */
public interface Resource extends Iterator<ResourceResultMap> {

    /*
     * Indicates the mode of the attributes.
     */
    public static enum AttributeMode { INPUT, OUTPUT }

    /*
     * Indicates the data type of the attributes.
     */
    public static enum AttributeType {
        BOOLEAN, INTEGER, DATETIME, NUMERIC, STRING;
    }

    //~--- methods ------------------------------------------------------------

    public void realizeResource(Hashtable<String, Object> aInputAttr) throws TBitsException, DatabaseException;
    ;
    ;

    //~--- get methods --------------------------------------------------------

    public ArrayList<ResourceAttr> getInputAttrList();

    public ArrayList<ResourceAttr> getOutputAttrList();

    /*
     * Accessor methods for the properties all resources will have in common.
     */
    public int getResourceId();

    public String getResourceName();

    //~--- set methods --------------------------------------------------------

    /*
     * Mutator methods.
     */
    public void setResourceId(int aResourceId);

    public void setResourceName(String aResourceName);
}
