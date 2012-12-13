/*
 * FBField.java
 *
 * $Header:
 *
 */

/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd.  All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */

// Imports from the current package.

// Other TBits Imports.

// Java Imports.
package transbit.tbits.config;

import java.io.Serializable;
import java.util.ArrayList;

// Third party imports.

/**
 * FBField is a conceptual union that represent different types of fields in a
 * form. An FBField object can hold the details of one such field at a time.
 * The type of the field is determined by the FBFieldType enum value of the
 * FBField object.
 * 
 * @author  : Vaibhav
 * @version : $Id: $
 * 
 */
public class FBField implements Serializable
{
    /*
     * Enum to represent the type of the FBField object.
     */
    public enum FBFieldType
    {
        ATTACHMENT,
        BULLET_LIST,
        CHECKBOX,
        DATE,
        DYNAMIC_LIST,
        PARAGRAPH,
        RADIO,
        STATIC_LIST,
        TEXT_AREA,
        TEXT_BOX,
        USER
    }
    
    /*
     * Attributes of different type of fields.
     */
    private String            myName;
    private String            myLabel;
    private String            myWidth;
    private String            myRowCount;
    private FBFieldType       myType;
    private String            myToolTip;
    private boolean           myValidate;
    private String            myParagraph;
    private String            myHeading;
    private ArrayList<String> myPointsList; 
    private ArrayList<FBType> myTypeList;
    private ArrayList<String> myRadioList;
    private String            myBAField;
    private boolean           myIsMultiple;
    private boolean           myPrePopulate;
    private String            myValue;
    private boolean           myIsReadOnly;

    /**
     * 
     */
    private FBField()
    {
    }
    
    /**
     * Factory method to create an attachment field.
     * 
     * @param aLabel  Label to be displayed for the attachment field.
     * 
     * @return FBField object.
     */
    public static FBField getAttachmentField(String aLabel)
    {
        FBField field = new FBField();
        field.setType(FBFieldType.ATTACHMENT);
        field.setLabel(aLabel);
        return field;
    }
    
    /**
     * Factory method to create a Bulleted list.
     * 
     * @param aHeading    Heading to be displayed above the bulleted list.
     * @param aPointList  List of bulleted points.
     * 
     * @return FBField object.
     */
    public static FBField getBulletListField(String            aHeading, 
                                             ArrayList<String> aPointList)
    {
        FBField field = new FBField();
        field.setType(FBFieldType.BULLET_LIST);
        field.setHeading(aHeading);
        field.setPointsList(aPointList);
        return field;
    }
    
    /**
     * Factory method to create a checkbox field.
     * 
     * @param aName             Name of the field.
     * @param aLabel            Label to displayed in the form.
     * @param aBAField          BAField this should be mapped to.
     * 
     * @return FBField object.
     */
    public static FBField getCheckBoxField(String aName, 
                                           String aLabel, 
                                           String aBAField)
    {
        FBField field = new FBField();
        field.setType(FBFieldType.CHECKBOX);
        field.setName(aName);
        field.setLabel(aLabel);
        field.setBAField(aBAField);
        return field;
    }
    
    /**
     * Factory method to create a Date field.
     * 
     * @param aName             Name of the field.
     * @param aLabel            Label to displayed in the form.
     * @param aToolTip          Tooltip if any.
     * @param aBAField          BAField this should be mapped to.
     * @param aValidate         True if the date should be validated.
     * 
     * @return FBField object.
     */
    public static FBField getDateField(String  aName,
                                       String  aLabel,
                                       String  aWidth,
                                       String  aToolTip,
                                       String  aBAField,
                                       boolean aValidate)
    {
        FBField field = new FBField();
        field.setType(FBFieldType.DATE);
        field.setName(aName);
        field.setLabel(aLabel);
        field.setWidth(aWidth);
        field.setToolTip(aToolTip);
        field.setBAField(aBAField);
        field.setValidate(aValidate);
        return field;
    }
    
    /**
     * Factory method to create a Dynamic List field.
     * 
     * @param aName             Name of the field.
     * @param aLabel            Label to displayed in the form.
     * @param aToolTip          Tooltip if any.
     * @param aBAField          BAField this should be mapped to.
     * 
     * @return FBField object.
     */
    public static FBField getDynamicListField(String aName,
                                              String aLabel,
                                              String aWidth,
                                              String aToolTip,
                                              String aBAField)
    {
        FBField field = new FBField();
        field.setType(FBFieldType.DYNAMIC_LIST);
        field.setName(aName);
        field.setLabel(aLabel);
        field.setWidth(aWidth);
        field.setToolTip(aToolTip);
        field.setBAField(aBAField);
        return field;
    }
    
    /**
     * Factory method to create a Paragraph field.
     * 
     * @param aPara Paragraph text.
     * 
     * @return FBField object.
     */
    public static FBField getParagraphField(String aPara)
    {
        FBField field = new FBField();
        field.setType(FBFieldType.PARAGRAPH);
        field.setParagraph(aPara);
        return field;
    }
    
    /**
     * Factory method to create a Radio List field.
     * 
     * @param aName  Name of the radio group.
     * @param aLabel Label of the radio group.
     * @param aBAField    BA field this should be mapped to.
     * @param aRadioList  List of labels for the radio buttons.
     * 
     * @return FBField object.
     */
    public static FBField getRadioField(String            aName,
                                        String            aLabel,
                                        String            aBAField,
                                        ArrayList<String> aRadioList)
    {
        FBField field = new FBField();

        field.setType(FBFieldType.RADIO);
        field.setName(aName);
        field.setLabel(aLabel);
        field.setBAField(aBAField);
        field.setRadioList(aRadioList);
        return field;
    }

    /**
     * Factory method to create a static list field.
     * 
     * @param aName             Name of the field.
     * @param aLabel            Label to displayed in the form.
     * @param aToolTip          Tooltip if any.
     * @param aBAField          BAField this should be mapped to.
     * @param aTypeList         List of option values
     * 
     * @return FBField object.
     */
    public static FBField getStaticListField(String             aName,
                                             String             aLabel,
                                             String             aWidth,
                                             String             aToolTip,
                                             String             aBAField,
                                             ArrayList<FBType>  aTypeList)
    {
        FBField field = new FBField();
        field.setType(FBFieldType.STATIC_LIST);
        field.setName(aName);
        field.setLabel(aLabel);
        field.setWidth(aWidth);
        field.setToolTip(aToolTip);
        field.setBAField(aBAField);
        field.setTypeList(aTypeList);
        return field;
    }
    
    /**
     * Factory method to create a text area field.
     * 
     * @param aName             Name of the field.
     * @param aLabel            Label to displayed in the form.
     * @param aToolTip          Tooltip if any.
     * 
     * @return FBField object.
     */
    public static FBField getTextAreaField(String aName, 
                                           String aLabel, 
                                           String aWidth,
                                           String aRowCount,
                                           String aToolTip)
    {
        FBField field = new FBField();
        field.setType(FBFieldType.TEXT_AREA);
        field.setName(aName);
        field.setLabel(aLabel);
        field.setWidth(aWidth);
        field.setRowCount(aRowCount);
        field.setToolTip(aToolTip);
        return field;
    }
    
    /**
     * Factory method to create a text box field.
     * 
     * @param aName             Name of the field.
     * @param aLabel            Label to displayed in the form.
     * @param aToolTip          Tooltip if any.
     * @param aBAField          BAField this should be mapped to.
     * 
     * @return FBField object.
     */
    public static FBField getTextBoxField(String  aName, 
                                          String  aLabel, 
                                          String  aWidth,
                                          String  aToolTip,
                                          String  aBAField,
                                          boolean aPrePopulate,
                                          String  aValue,
                                          boolean aIsReadOnly)
    {
        FBField field = new FBField();
        field.setType(FBFieldType.TEXT_BOX);
        field.setName(aName);
        field.setLabel(aLabel);
        field.setWidth(aWidth);
        field.setToolTip(aToolTip);
        field.setBAField(aBAField);
        field.setPrePopulate(aPrePopulate);
        field.setValue(aValue);
        field.setIsReadOnly(aIsReadOnly);
        return field;
    }
    
    /**
     * Factory method to create a user field.
     * 
     * @param aName             Name of the field.
     * @param aLabel            Label to displayed in the form.
     * @param aToolTip          Tooltip if any.
     * @param aBAField          BAField this should be mapped to.
     * @param aMultiple         True if allows multiple users.
     * 
     * @return FBField object.
     */
    public static FBField getUserField(String  aName,
                                       String  aLabel,
                                       String  aWidth,
                                       String  aToolTip,
                                       String  aBAField,
                                       boolean aMultiple)
    {
        FBField field = new FBField();
        field.setType(FBFieldType.USER);
        field.setName(aName);
        field.setLabel(aLabel);
        field.setWidth(aWidth);
        field.setToolTip(aToolTip);
        field.setBAField(aBAField);
        field.setIsMultiple(aMultiple);
        return field;
    }

    /**
     * Accessor method for Name property.
     *
     * @return Current Value of Name
     *
     */
    public String getName()
    {
        return myName;
    }
    
    /**
     * Accessor method for Label property.
     *
     * @return Current Value of Label
     *
     */
    public String getLabel()
    {
        return myLabel;
    }
    
    /**RowCount
     * Accessor method for Width property.
     *
     * @return Current Value of Width
     *
     */
    public String getWidth()
    {
        return myWidth;
    }
    
    /**
     * Accessor method for RowCount property.
     *
     * @return Current Value of RowCount
     *
     */
    public String getRowCount()
    {
        return myRowCount;
    }
    
    /**
     * Accessor method for Type property.
     *
     * @return Current Value of Type
     *
     */
    public FBFieldType getType()
    {
        return myType;
    }
    
    /**
     * Accessor method for ToolTip property.
     *
     * @return Current Value of ToolTip
     *
     */
    public String getToolTip()
    {
        return myToolTip;
    }
    
    /**
     * Accessor method for Validate property.
     *
     * @return Current Value of Validate
     *
     */
    public boolean getValidate()
    {
        return myValidate;
    }
    
    /**
     * Accessor method for Paragraph property.
     *
     * @return Current Value of Paragraph 
     *
     */
    public String getParagraph()
    {
        return myParagraph;
    }
    
    /**
     * Accessor method for Heading property.
     *
     * @return Current Value of Heading
     *
     */
    public String getHeading()
    {
        return myHeading;
    }
    
    /**
     * Accessor method for PointsList property.
     *
     * @return Current Value of PointsList
     *
     */
    public ArrayList<String> getPointsList()
    {
        return myPointsList;
    }
    
    /**
     * Accessor method for TypeList property.
     *
     * @return Current Value of TypeList
     *
     */
    public ArrayList<FBType> getTypeList()
    {
        return myTypeList;
    }
    
    /**
     * Accessor method for RadioList property.
     *
     * @return Current Value of RadioList
     *
     */
    public ArrayList<String> getRadioList()
    {
        return myRadioList;
    }
    
    /**
     * Accessor method for BAField property.
     *
     * @return Current Value of BAField
     *
     */
    public String getBAField()
    {
        return myBAField;
    }
    
    /**
     * Accessor method for IsMultiple property.
     *
     * @return Current Value of IsMultiple
     *
     */
    public boolean getIsMultiple()
    {
        return myIsMultiple;
    }
    
    /**
     * Accessor method for PrePopulate property.
     *
     * @return Current Value of PrePopulate
     *
     */
    public boolean getPrePopulate()
    {
        return myPrePopulate;
    }
    
    /**
     * Accessor method for Value property.
     *
     * @return Current Value of Value property
     *
     */
    public String getValue()
    {
        return myValue;
    }
    
    /**
     * Accessor method for IsReadOnly property.
     *
     * @return Current Value of IsReadOnly
     *
     */
    public boolean getIsReadOnly()
    {
        return myIsReadOnly;
    }
    
    /**
     * Mutator method for Name property.
     *
     * @param aName New Value for Name
     *
     */
    private void setName(String aName)
    {
        myName = aName;
    }
    
    /**
     * Mutator method for Label property.
     *
     * @param aLabel New Value for Label
     *
     */
    private void setLabel(String aLabel)
    {
        myLabel = aLabel;
    }
    
    /**
     * Mutator method for Width property.
     *
     * @param aWidth New Value for Width
     *
     */
    private void setWidth(String aWidth)
    {
        myWidth = aWidth;
    }
    
    /**
     * Mutator method for RowCount property.
     *
     * @param aWidth New Value for RowCount
     *
     */
    private void setRowCount(String aRowCount)
    {
        myRowCount = aRowCount;
    }
    
    /**
     * Mutator method for Type property.
     *
     * @param aType New Value for Type
     *
     */
    private void setType(FBFieldType aType)
    {
        myType = aType;
    }
    
    /**
     * Mutator method for ToolTip property.
     *
     * @param aToolTip New Value for ToolTip
     *
     */
    private void setToolTip(String aToolTip)
    {
        myToolTip = aToolTip;
    }
    
    /**
     * Mutator method for Validate property.
     *
     * @param aValidate New Value for Validate
     *
     */
    private void setValidate(boolean aValidate)
    {
        myValidate = aValidate;
    }
    
    /**
     * Mutator method for Paragraph property.
     *
     * @param aParagraph New Value for Paragraph
     *
     */
    private void setParagraph(String aParagraph)
    {
        myParagraph = aParagraph;
    }
    
    /**
     * Mutator method for Heading property.
     *
     * @param aHeading New Value for Heading
     *
     */
    private void setHeading(String aHeading)
    {
        myHeading = aHeading;
    }
    
    /**
     * Mutator method for PointsList property.
     *
     * @param aPointsList New Value for PointsList
     *
     */
    private void setPointsList(ArrayList<String> aPointsList)
    {
        myPointsList = aPointsList;
    }
    
    /**
     * Mutator method for TypeList property.
     *
     * @param aTypeList New Value for TypeList
     *
     */
    private void setTypeList(ArrayList<FBType> aTypeList)
    {
        myTypeList = aTypeList;
    }
    
    /**
     * Mutator method for RadioList property.
     *
     * @param aRadioList New Value for RadioList
     *
     */
    private void setRadioList(ArrayList<String> aRadioList)
    {
        myRadioList = aRadioList;
    }
    
    /**
     * Mutator method for BAField property.
     * 
     * @param aBAField New value of BAField
     */
    private void setBAField(String aBAField)
    {
        myBAField = aBAField;
    }
    
    /**
     * Mutator method for IsMultiple property.
     * 
     * @param aIsMultiple New value of IsMultiple
     */
    private void setIsMultiple(boolean aIsMultiple)
    {
        myIsMultiple = aIsMultiple;
    }

    /**
     * Mutator method for PrePopulate property.
     * 
     * @param aIsMultiple New value of PrePopulate
     */
    private void setPrePopulate(boolean aPrePopulate)
    {
        myPrePopulate = aPrePopulate;
    }

    /**
     * Mutator method for Value property.
     * 
     * @param aBAField New value of Value
     */
    private void setValue(String aValue)
    {
        myValue = aValue;
    }
    
    /**
     * Mutator method for IsReadOnly property.
     * 
     * @param aIsReadOnly New value of IsReadOnly
     */
    private void setIsReadOnly(boolean aIsReadOnly)
    {
        myIsReadOnly = aIsReadOnly;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        switch(myType)
        {
        }
        return buffer.toString();
    }
}
