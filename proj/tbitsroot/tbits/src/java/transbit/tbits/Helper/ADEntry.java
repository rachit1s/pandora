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
 * ADEntry.java
 *
 * $Header:
 *
 */
package transbit.tbits.Helper;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

//~--- classes ----------------------------------------------------------------

/**
 * This class is the domain object corresponding to the  table
 * in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
public class ADEntry {

    // Types of AD Entries we deal with
    public static final int USER_ENTRY    = 1;
    public static final int GROUP_ENTRY   = 2;
    public static final int CONTACT_ENTRY = 3;

    //~--- fields -------------------------------------------------------------

    private String myDisplayName;

    // Attributes of this Domain Object.
    private int    myEntryType;
    private String myExtension;
    private String myGivenName;
    private String myHomePhone;
    private String myLocation;
    private String myMail;
    private String myMailNickName;
    private String myMobile;
    private String myName;
    private String myObjectClass;
    private String mySAMAccountName;
    private String myShowInAddressBox;
    private String mySurName;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public ADEntry() {}

    /**
     * The complete constructor.
     *
     *  @param aName
     *  @param aSurName
     *  @param aGivenName
     *  @param aDisplayName
     *  @param aMailNickName
     *  @param aMail
     *  @param aObjectClass
     *  @param aShowInAddressBox
     *  @param aSAMAccountName
     *  @param aLocation
     *  @param aExtension
     *  @param aMobile
     *  @param aHomePhone
     */
    public ADEntry(int aEntryType, String aName, String aSurName, String aGivenName, String aDisplayName, String aMailNickName, String aMail, String aObjectClass, String aShowInAddressBox,
                   String aSAMAccountName, String aLocation, String aExtension, String aMobile, String aHomePhone) {
        myEntryType        = aEntryType;
        myName             = aName;
        mySurName          = aSurName;
        myGivenName        = aGivenName;
        myDisplayName      = aDisplayName;
        myMailNickName     = aMailNickName;
        myMail             = aMail;
        myObjectClass      = aObjectClass;
        myShowInAddressBox = aShowInAddressBox;
        mySAMAccountName   = aSAMAccountName;
        myLocation         = aLocation;
        myExtension        = aExtension;
        myMobile           = aMobile;
        myHomePhone        = aHomePhone;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method that compares this object with the one passed W.R.T the
     * ourSortField.
     *
     * @param aObject  Object to be compared.
     *
     * @return 0 - If they are equal.
     *         1 - If this is greater.
     *        -1 - If this is smaller.
     *
     */
    public int compareTo(ADEntry aObject) {
        return myName.compareToIgnoreCase(aObject.myName);
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the Role objects in sorted order
     */
    public static ArrayList<ADEntry> sort(ArrayList<ADEntry> source) {
        int       size     = source.size();
        ADEntry[] srcArray = new ADEntry[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new ADEntryComparator());

        ArrayList<ADEntry> target = new ArrayList<ADEntry>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    /**
     * String representation of this object.
     */
    public String toString() {
        return "[ " + this.myName + ", " + this.myDisplayName + ", " + this.myMail + ", Type: " + ((myEntryType == USER_ENTRY)
                ? "USER"
                : (myEntryType == GROUP_ENTRY)
                  ?"GROUP"
                  :(myEntryType==CONTACT_ENTRY)
                  ?"CONTACT"
                  :"UNKNOWN")+" ]";
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
     * Accessor method for EntryType property.
     *
     * @return Current Value of EntryType
     *
     */
    public int getEntryType() {
        return myEntryType;
    }

    /**
     * Accessor method for Extension property.
     *
     * @return Current Value of Extension
     *
     */
    public String getExtension() {
        return myExtension;
    }

    /**
     * Accessor method for GivenName property.
     *
     * @return Current Value of GivenName
     *
     */
    public String getGivenName() {
        return myGivenName;
    }

    /**
     * Accessor method for HomePhone property.
     *
     * @return Current Value of HomePhone
     *
     */
    public String getHomePhone() {
        return myHomePhone;
    }

    /**
     * Accessor method for Location property.
     *
     * @return Current Value of Location
     *
     */
    public String getLocation() {
        return myLocation;
    }

    /**
     * Accessor method for Mail property.
     *
     * @return Current Value of Mail
     *
     */
    public String getMail() {
        return myMail;
    }

    /**
     * Accessor method for MailNickName property.
     *
     * @return Current Value of MailNickName
     *
     */
    public String getMailNickName() {
        return myMailNickName;
    }

    /**
     * Accessor method for Mobile property.
     *
     * @return Current Value of Mobile
     *
     */
    public String getMobile() {
        return myMobile;
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
     * Accessor method for ObjectClass property.
     *
     * @return Current Value of ObjectClass
     *
     */
    public String getObjectClass() {
        return myObjectClass;
    }

    /**
     * Accessor method for SAMAccountName property.
     *
     * @return Current Value of SAMAccountName
     *
     */
    public String getSAMAccountName() {
        return mySAMAccountName;
    }

    /**
     * Accessor method for ShowInAddressBox property.
     *
     * @return Current Value of ShowInAddressBox
     *
     */
    public String getShowInAddressBox() {
        return myShowInAddressBox;
    }

    /**
     * Accessor method for SurName property.
     *
     * @return Current Value of SurName
     *
     */
    public String getSurName() {
        return mySurName;
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
     * Mutator method for EntryType property.
     *
     * @param aEntryType New Value for EntryType
     *
     */
    public void setEntryType(int aEntryType) {
        myEntryType = aEntryType;
    }

    /**
     * Mutator method for Extension property.
     *
     * @param aExtension New Value for Extension
     *
     */
    public void setExtension(String aExtension) {
        myExtension = aExtension;
    }

    /**
     * Mutator method for GivenName property.
     *
     * @param aGivenName New Value for GivenName
     *
     */
    public void setGivenName(String aGivenName) {
        myGivenName = aGivenName;
    }

    /**
     * Mutator method for HomePhone property.
     *
     * @param aHomePhone New Value for HomePhone
     *
     */
    public void setHomePhone(String aHomePhone) {
        myHomePhone = aHomePhone;
    }

    /**
     * Mutator method for Location property.
     *
     * @param aLocation New Value for Location
     *
     */
    public void setLocation(String aLocation) {
        myLocation = aLocation;
    }

    /**
     * Mutator method for Mail property.
     *
     * @param aMail New Value for Mail
     *
     */
    public void setMail(String aMail) {
        myMail = aMail;
    }

    /**
     * Mutator method for MailNickName property.
     *
     * @param aMailNickName New Value for MailNickName
     *
     */
    public void setMailNickName(String aMailNickName) {
        myMailNickName = aMailNickName;
    }

    /**
     * Mutator method for Mobile property.
     *
     * @param aMobile New Value for Mobile
     *
     */
    public void setMobile(String aMobile) {
        myMobile = aMobile;
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
     * Mutator method for ObjectClass property.
     *
     * @param aObjectClass New Value for ObjectClass
     *
     */
    public void setObjectClass(String aObjectClass) {
        myObjectClass = aObjectClass;
    }

    /**
     * Mutator method for SAMAccountName property.
     *
     * @param aSAMAccountName New Value for SAMAccountName
     *
     */
    public void setSAMAccountName(String aSAMAccountName) {
        mySAMAccountName = aSAMAccountName;
    }

    /**
     * Mutator method for ShowInAddressBox property.
     *
     * @param aShowInAddressBox New Value for ShowInAddressBox
     *
     */
    public void setShowInAddressBox(String aShowInAddressBox) {
        myShowInAddressBox = aShowInAddressBox;
    }

    /**
     * Mutator method for SurName property.
     *
     * @param aSurName New Value for SurName
     *
     */
    public void setSurName(String aSurName) {
        mySurName = aSurName;
    }
}


/**
 * This class is the comparator for ADEntry objects
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class ADEntryComparator implements Comparator<ADEntry>, Serializable {
    public int compare(ADEntry obj1, ADEntry obj2) {
        return obj1.compareTo(obj2);
    }
}
