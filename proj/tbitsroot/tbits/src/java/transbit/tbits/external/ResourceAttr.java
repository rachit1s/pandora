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
 * ResourceAttr.java
 *
 * $Header:
 *
 */
package transbit.tbits.external;

//~--- non-JDK imports --------------------------------------------------------

//TBits Imports.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_EXTERNAL;
import static transbit.tbits.external.Resource.AttributeMode;
import static transbit.tbits.external.Resource.AttributeType;

//~--- classes ----------------------------------------------------------------

/**
 * This class encapsulates the information related to input/output attributes
 * of a resource.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 *
 */
public class ResourceAttr {
    public static final TBitsLogger LOG         = TBitsLogger.getLogger(PKG_EXTERNAL);
    public static final int         MODE_INPUT  = 0;
    public static final int         MODE_OUTPUT = 1;

    //~--- fields -------------------------------------------------------------

    private AttributeMode myAttrMode;

    // Attributes of this Domain Object.
    private String        myAttrName;
    private int           myAttrSequence;
    private AttributeType myAttrType;
    private boolean       myBitValue;
    private Timestamp     myDateValue;
    private int           myIntValue;
    private boolean       myIsVariable;
    private double        myRealValue;
    private String        myStringValue;
    private String        myVarName;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public ResourceAttr() {}

    /**
     * The copy constructor.
     *
     *  @param aAttr
     */
    public ResourceAttr(ResourceAttr aAttr) {
        myAttrMode     = aAttr.myAttrMode;
        myAttrName     = aAttr.myAttrName;
        myAttrType     = aAttr.myAttrType;
        myAttrSequence = aAttr.myAttrSequence;
        myBitValue     = aAttr.myBitValue;
        myDateValue    = aAttr.myDateValue;
        myIntValue     = aAttr.myIntValue;
        myRealValue    = aAttr.myRealValue;
        myStringValue  = aAttr.myStringValue;
    }

    /**
     * The complete constructor.
     *
     *  @param aAttrMode
     *  @param aAttrType
     *  @param aAttrSequence
     *  @param aBitValue
     *  @param aDateValue
     *  @param aIntValue
     *  @param aRealValue
     *  @param aStringValue
     */
    public ResourceAttr(AttributeMode aAttrMode, String aAttrName, AttributeType aAttrType, int aAttrSequence, boolean aBitValue, Timestamp aDateValue, int aIntValue, double aRealValue,
                        String aStringValue) {
        myAttrMode     = aAttrMode;
        myAttrName     = aAttrName;
        myAttrType     = aAttrType;
        myAttrSequence = aAttrSequence;
        myBitValue     = aBitValue;
        myDateValue    = aDateValue;
        myIntValue     = aIntValue;
        myRealValue    = aRealValue;
        myStringValue  = aStringValue;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Returns the string representation of this ResourceAttr object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[ ").append(getValue()).append(" ]");

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for AttrMode property.
     *
     * @return Current Value of AttrMode
     *
     */
    public AttributeMode getAttrMode() {
        return myAttrMode;
    }

    /**
     * Accessor method for AttrName property.
     *
     * @return Current Value of AttrName
     *
     */
    public String getAttrName() {
        return myAttrName;
    }

    /**
     * Accessor method for AttrSequence property.
     *
     * @return Current Value of AttrSequence
     *
     */
    public int getAttrSequence() {
        return myAttrSequence;
    }

    /**
     * Accessor method for AttrType property.
     *
     * @return Current Value of AttrType
     *
     */
    public AttributeType getAttrType() {
        return myAttrType;
    }

    /**
     * Accessor method for BitValue property.
     *
     * @return Current Value of BitValue
     *
     */
    public boolean getBitValue() {
        return myBitValue;
    }

    /**
     * Accessor method for DateValue property.
     *
     * @return Current Value of DateValue
     *
     */
    public Timestamp getDateValue() {
        return myDateValue;
    }

    /**
     * Accessor method for IntValue property.
     *
     * @return Current Value of IntValue
     *
     */
    public int getIntValue() {
        return myIntValue;
    }

    /**
     * Accessor method for IsVariable property.
     *
     * @return Current Value of IsVariable
     *
     */
    public boolean getIsVariable() {
        return myIsVariable;
    }

    /**
     * Accessor method for RealValue property.
     *
     * @return Current Value of RealValue
     *
     */
    public double getRealValue() {
        return myRealValue;
    }

    /**
     * Accessor method for StringValue property.
     *
     * @return Current Value of StringValue
     *
     */
    public String getStringValue() {
        return myStringValue;
    }

    /**
     * Utility method to get the value of this attribute as an object.
     *
     * @return Object holding the value of this attribute.
     */
    public Object getValue() {
        Object value = null;

        switch (myAttrType) {
        case BOOLEAN :
            value = new Boolean(myBitValue);

            break;

        case DATETIME :
            value = myDateValue;

            break;

        case INTEGER :
            value = new Integer(myIntValue);

            break;

        case NUMERIC :
            value = new Double(myRealValue);

            break;

        case STRING :
            value = myStringValue;

            break;
        }

        return value;
    }

    /**
     * Accessor method for VarName property.
     *
     * @return Current Value of VarName
     *
     */
    public String getVarName() {
        return myVarName;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for AttrMode property.
     *
     * @param aAttrMode New Value for AttrMode
     *
     */
    public void setAttrMode(AttributeMode aAttrMode) {
        myAttrMode = aAttrMode;
    }

    /**
     * Mutator method for AttrName property.
     *
     * @param aAttrName New Value for AttrName
     *
     */
    public void setAttrName(String aAttrName) {
        myAttrName = aAttrName;
    }

    /**
     * Mutator method for AttrSequence property.
     *
     * @param aAttrSequence New Value for AttrSequence
     *
     */
    public void setAttrSequence(int aAttrSequence) {
        myAttrSequence = aAttrSequence;
    }

    /**
     * Mutator method for AttrType property.
     *
     * @param aAttrType New Value for AttrType
     *
     */
    public void setAttrType(AttributeType aAttrType) {
        myAttrType = aAttrType;
    }

    /**
     * Mutator method for BitValue property.
     *
     * @param aBitValue New Value for BitValue
     *
     */
    public void setBitValue(boolean aBitValue) {
        myBitValue = aBitValue;
    }

    /**
     * Mutator method for DateValue property.
     *
     * @param aDateValue New Value for DateValue
     *
     */
    public void setDateValue(Timestamp aDateValue) {
        myDateValue = aDateValue;
    }

    /**
     * Mutator method for IntValue property.
     *
     * @param aIntValue New Value for IntValue
     *
     */
    public void setIntValue(int aIntValue) {
        myIntValue = aIntValue;
    }

    /**
     * Mutator method for IsVariable property.
     *
     * @param aIsVariable New Value for IsVariable
     *
     */
    public void setIsVariable(boolean aIsVariable) {
        myIsVariable = aIsVariable;
    }

    /**
     * Mutator method for RealValue property.
     *
     * @param aRealValue New Value for RealValue
     *
     */
    public void setRealValue(double aRealValue) {
        myRealValue = aRealValue;
    }

    /**
     * Mutator method for StringValue property.
     *
     * @param aStringValue New Value for StringValue
     *
     */
    public void setStringValue(String aStringValue) {
        myStringValue = aStringValue;
    }

    /**
     * This is a method to set the value of attributes based on their types.
     *
     * @param aAttrType
     * @param aValue
     */
    public void setValue(AttributeType aAttrType, String aValue) {
        myAttrType = aAttrType;

        if (aValue == null) {
            return;
        }

        aValue = aValue.trim();

        switch (myAttrType) {
        case BOOLEAN :
            if (aValue.startsWith("$")) {
                myIsVariable = true;
                myVarName    = aValue.substring(1);
            } else {
                if (aValue.equals("1") || aValue.equalsIgnoreCase("true") || aValue.equalsIgnoreCase("yes")) {
                    setBitValue(true);
                } else {
                    setBitValue(false);
                }
            }

            break;

        case DATETIME :
            if (aValue.startsWith("$")) {
                myIsVariable = true;
                myVarName    = aValue.substring(1);
            } else {
                try {
                    String    format = "yyyyMMdd HH:mm:ss";
                    Timestamp ts     = new Timestamp(aValue, format);

                    setDateValue(ts);
                } catch (Exception e) {
                    LOG.info("Exception while parsing the date. " + "Please ensure that the date " + "is specified in yyyyMMdd HH:mm:ss " + "format only" + aValue);
                }
            }

            break;

        case INTEGER :
            if (aValue.startsWith("$")) {
                myIsVariable = true;
                myVarName    = aValue.substring(1);
            } else {
                try {
                    int value = Integer.parseInt(aValue);

                    setIntValue(value);
                } catch (Exception e) {
                    LOG.info("Exception while parsing int: " + aValue);
                }
            }

            break;

        case NUMERIC :
            if (aValue.startsWith("$")) {
                myIsVariable = true;
                myVarName    = aValue.substring(1);
            } else {
                try {
                    double value = Double.parseDouble(aValue);

                    setRealValue(value);
                } catch (Exception e) {
                    LOG.info("Exception while parsing real: " + aValue);
                }
            }

            break;

        case STRING :
            if (aValue.startsWith("$")) {
                myIsVariable = true;
                myVarName    = aValue.substring(1);
            } else {
                setStringValue(aValue);
            }

            break;
        }
    }

    /**
     * Mutator method for VarName property.
     *
     * @param aVarName New Value for VarName
     *
     */
    public void setVarName(String aVarName) {
        myVarName = aVarName;
    }
}
