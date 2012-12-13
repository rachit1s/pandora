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
 * TextDiff.java
 *
 * $Header:
 *
 */
package transbit.tbits.Helper;

//~--- JDK imports ------------------------------------------------------------

//Imports from the current package.
//Java Imports.
import java.util.ArrayList;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class encapsulates the entries in the Diff generated between two states
 * of a request.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 *
 */
public class TextDiff {
    private ArrayList<Integer> myArrayList;
    private String             myCurrentText;
    private int                myOther;

    // Attributes of this Domain Object.
    private String myText;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public TextDiff() {
        myOther     = -1;
        myArrayList = new ArrayList<Integer>();
        myText      = null;
    }

    /**
     * The complete constructor.
     *
     *  @param aText
     *  @param aArrayList
     *  @param aOther
     */
    public TextDiff(String aText, String aCurrentText, ArrayList<Integer> aArrayList, int aOther) {
        myText        = aText;
        myCurrentText = aCurrentText;
        myArrayList   = aArrayList;
        myOther       = aOther;
    }

    //~--- methods ------------------------------------------------------------

    public void pushArrayList(Integer aInteger) {
        myArrayList.add(aInteger);
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for ArrayList property.
     *
     * @return Current Value of ArrayList
     *
     */
    public ArrayList<Integer> getArrayList() {
        return myArrayList;
    }

    /**
     * Accessor method for Text property.
     *
     * @return Current Value of Text
     *
     */
    public String getCurrentText() {
        return myCurrentText;
    }

    /**
     * Accessor method for Other property.
     *
     * @return Current Value of Other
     *
     */
    public int getOther() {
        return myOther;
    }

    /**
     * Accessor method for Text property.
     *
     * @return Current Value of Text
     *
     */
    public String getText() {
        return myText;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for ArrayList property.
     *
     * @param aArrayList New Value for ArrayList
     *
     */
    public void setArrayList(ArrayList<Integer> aArrayList) {
        myArrayList = aArrayList;
    }

    /**
     * Mutator method for CurrentText property.
     *
     * @param aCurrentText New Value for Text
     *
     */
    public void setCurrentText(String aCurrentText) {
        myCurrentText = aCurrentText;
    }

    /**
     * Mutator method for Other property.
     *
     * @param aOther New Value for Other
     *
     */
    public void setOther(int aOther) {
        myOther = aOther;
    }

    /**
     * Mutator method for Text property.
     *
     * @param aText New Value for Text
     *
     */
    public void setText(String aText) {
        myText = aText;
    }
}
