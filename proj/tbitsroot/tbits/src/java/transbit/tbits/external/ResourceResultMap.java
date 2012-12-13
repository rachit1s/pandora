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
 * ResourceResultMap.java
 *
 * $Header:
 *
 */
package transbit.tbits.external;

//~--- JDK imports ------------------------------------------------------------

import java.util.Hashtable;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * TODO: Javadoc for this class.
 *
 * @author  :
 * @version : $Id: $
 *
 */
public class ResourceResultMap {

    // Attributes of this Domain Object.
    private Hashtable<String, ResourceAttr> myResultMap;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public ResourceResultMap() {}

    /**
     * The complete constructor.
     *
     *  @param aResultMap
     */
    public ResourceResultMap(Hashtable<String, ResourceAttr> aResultMap) {
        myResultMap = aResultMap;
    }

    //~--- methods ------------------------------------------------------------

    public String toString() {
        if (myResultMap == null) {
            return "{}";
        }

        return myResultMap.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method to obtain the value of the given field.
     *
     * @return Object corresponding to the value.
     */
    public ResourceAttr get(String aAttrName) {

        // Return null if either the map is null or the attrName is invalid.
        if (myResultMap == null) {
            return null;
        }

        ResourceAttr attr = myResultMap.get(aAttrName);

        return attr;
    }

    /**
     * Accessor method for ResultMap property.
     *
     * @return Current Value of ResultMap
     *
     */
    public Hashtable<String, ResourceAttr> getResultMap() {
        return myResultMap;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for ResultMap property.
     *
     * @param aResultMap New Value for ResultMap
     *
     */
    public void setResultMap(Hashtable<String, ResourceAttr> aResultMap) {
        myResultMap = aResultMap;
    }
}
