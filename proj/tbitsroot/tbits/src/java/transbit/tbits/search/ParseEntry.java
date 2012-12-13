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
 * ParseEntry.java
 *
 * $Header:
 *
 */
package transbit.tbits.search;

//~--- non-JDK imports --------------------------------------------------------

//Static imports.
import static transbit.tbits.search.SearchConstants.Connective;

//~--- JDK imports ------------------------------------------------------------

//Imports from the current package.
//Other TBits Imports.
//Java Imports.
import java.io.Serializable;
import java.util.ArrayList;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class encapsulates the information of an entry in the parse table.
 *
 * @author  : Vaibhav
 * @version : $Id: $
 *
 */
public class ParseEntry implements Serializable{
    private ArrayList<String> myArgList;
    private String            myClause;
    private Connective        myConnective;
    private int               myDepth;
    private String            myDescriptor;

    // Attributes of this Domain Object.
    private ParseEntryType    myEntryType;
    private ArrayList<String> myExtraInfo;
    private String            myFieldName;

    //~--- constant enums -----------------------------------------------------

    public enum ParseEntryType { NESTING_OPEN, NESTING_CLOSE, DATA }

    ;

    //~--- constructors -------------------------------------------------------

    // Types of entries.

    /*
     * public static final int NESTING_OPEN  = 101;
     * public static final int NESTING_CLOSE = 102;
     * public static final int DATA          = 103;
     */

    /**
     * The default constructor.
     */
    public ParseEntry() {
        myEntryType  = ParseEntryType.DATA;
        myDescriptor = "";
        myFieldName  = "";
        myDepth      = 0;
        myConnective = Connective.C_AND;
        myArgList    = null;
    }

    /**
     * The complete constructor.
     *
     *  @param aDescriptor
     *  @param aFieldName
     *  @param aDepth
     *  @param aConnective
     *  @param aArgList
     */
    public ParseEntry(String aDescriptor, String aFieldName, int aDepth, Connective aConnective, ArrayList<String> aArgList) {
        myEntryType  = ParseEntryType.DATA;
        myDescriptor = aDescriptor;
        myFieldName  = aFieldName;
        myDepth      = aDepth;
        myConnective = aConnective;
        myArgList    = aArgList;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method returns the string representation of object of this class.
     *
     * @return String representation of object of this class.
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        if (myEntryType == ParseEntryType.DATA) {
            buffer.append("[ ").append(myDescriptor).append(", ").append(myFieldName).append(", ").append(myDepth).append(", ").append(myConnective).append(", ");

            if (myArgList == null) {
                buffer.append("NULL");
            } else {
                buffer.append(myArgList.toString());
            }

            buffer.append(" ]").append("\n")
            ;
        } else if (myEntryType == ParseEntryType.NESTING_OPEN) {
            buffer.append("[ ------ Nesting Open ------- ]").append(myConnective).append("\n");
        } else if (myEntryType == ParseEntryType.NESTING_CLOSE) {
            buffer.append("[ ------ Nesting Close ------ ]").append("\n");
        }

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for ArgList property.
     *
     * @return Current Value of ArgList
     *
     */
    public ArrayList<String> getArgList() {
        return myArgList;
    }

    /**
     * Accessor method for Clause property.
     *
     * @return Current Value of Clause.
     */
    public String getClause() {
        return myClause;
    }

    /**
     * Accessor method for Connective property.
     *
     * @return Current Value of Connective
     *
     */
    public Connective getConnective() {
        return myConnective;
    }

    /**
     * Accessor method for Depth property.
     *
     * @return Current Value of Depth
     *
     */
    public int getDepth() {
        return myDepth;
    }

    /**
     * Accessor method for Descriptor property.
     *
     * @return Current Value of Descriptor
     *
     */
    public String getDescriptor() {
        return myDescriptor;
    }

    /**
     * Accessor method for EntryType property.
     *
     * @return Current Value of EntryType
     *
     */
    public ParseEntryType getEntryType() {
        return myEntryType;
    }

    /**
     * Accessor method for ExtraInfo.
     *
     * @return Current Value of ExtraInfo.
     */
    public ArrayList<String> getExtraInfo() {
        return myExtraInfo;
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

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for ArgList property.
     *
     * @param aArgList New Value for ArgList
     *
     */
    public ParseEntry setArgList(ArrayList<String> aArgList) {
        myArgList = aArgList;

        return this;
    }

    /**
     * Mutator method for Clause property.
     *
     * @param aClause New Value for clause.
     *
     * @return The current object.
     */
    public ParseEntry setClause(String aClause) {
        myClause = aClause;

        return this;
    }

    /**
     * Mutator method for Connective property.
     *
     * @param aConnective New Value for Connective
     *
     */
    public ParseEntry setConnective(Connective aConnective) {
        myConnective = aConnective;

        return this;
    }

    /**
     * Mutator method for Depth property.
     *
     * @param aDepth New Value for Depth
     *
     */
    public ParseEntry setDepth(int aDepth) {
        myDepth = aDepth;

        return this;
    }

    /**
     * Mutator method for Descriptor property.
     *
     * @param aDescriptor New Value for Descriptor
     *
     */
    public ParseEntry setDescriptor(String aDescriptor) {
        myDescriptor = aDescriptor;

        return this;
    }

    /**
     * Mutator method for EntryType property.
     *
     * @param aEntryType New Value for EntryType
     *
     */
    public ParseEntry setEntryType(ParseEntryType aEntryType) {
        myEntryType = aEntryType;

        return this;
    }

    /**
     * Mutator method for ExtraInfo property.
     *
     * @param aExtraInfo New Value for ExtraInfo
     *
     */
    public ParseEntry setExtraInfo(ArrayList<String> aExtraInfo) {
        myExtraInfo = aExtraInfo;

        return this;
    }

    /**
     * Mutator method for FieldName property.
     *
     * @param aFieldName New Value for FieldName
     *
     */
    public ParseEntry setFieldName(String aFieldName) {
        myFieldName = aFieldName;

        return this;
    }
}
