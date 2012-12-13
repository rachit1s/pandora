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
 * FBAction.java
 *
 * $Header:
 *
 */

//Imports from the current package.
//Other TBits Imports.
//Java Imports.
package transbit.tbits.config;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;
import java.util.ArrayList;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the  table
 * in the database.
 *
 * @author  :
 * @version : $Id: $
 *
 */
public class FBAction implements Serializable{
    private ArrayList<String> myCcList;
    private String            myDescription;

    // Attributes of this Domain Object.
    private String            myFrom;
    private String            mySubject;
    private ArrayList<String> mySubscriberList;
    private ArrayList<String> myToList;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public FBAction() {
        myFrom           = "";
        myToList         = new ArrayList<String>();
        myCcList         = new ArrayList<String>();
        mySubscriberList = new ArrayList<String>();
        mySubject        = "";
        myDescription    = "";
    }

    /**
     * The complete constructor.
     *
     *  @param aToList
     *  @param aCcList
     *  @param aFrom
     *  @param aSubject
     *  @param aDescription
     */
    public FBAction(String aFrom, ArrayList<String> aToList, ArrayList<String> aCcList, ArrayList<String> aSubscriberList, String aSubject, String aDescription) {
        myFrom           = aFrom;
        myToList         = aToList;
        myCcList         = aCcList;
        mySubscriberList = aSubscriberList;
        mySubject        = aSubject;
        myDescription    = aDescription;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method adds the given email to the CcList if it is
     *          - not null
     *          - not empty
     *          - not added to the list already.
     *
     * @param email Email address to be added.
     */
    public void addtoCcList(String email) {
        if ((email == null) || (email.trim().equals("") == true)) {
            return;
        }

        if (myCcList.contains(email) == false) {
            myCcList.add(email);
        }
    }

    /**
     * This method adds the given email to the SubscriberList if it is
     *          - not null
     *          - not empty
     *          - not added to the list already.
     *
     * @param email Email address to be added.
     */
    public void addtoSubscriberList(String email) {
        if ((email == null) || (email.trim().equals("") == true)) {
            return;
        }

        if (mySubscriberList.contains(email) == false) {
            mySubscriberList.add(email);
        }
    }

    /**
     * This method adds the given email to the ToList if it is
     *          - not null
     *          - not empty
     *          - not added to the list already.
     *
     * @param email Email address to be added.
     */
    public void addtoToList(String email) {
        if ((email == null) || (email.trim().equals("") == true)) {
            return;
        }

        if (myToList.contains(email) == false) {
            myToList.add(email);
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\n[ ").append("\nFrom: ").append(myFrom).append("\nTo: ").append(myToList.toString()).append("\nCc: ").append(myCcList.toString()).append("\nSubscriber: ").append(
            mySubscriberList.toString()).append("\nSubject: ").append(mySubject).append("\nDescription: \n").append(myDescription).append("\n]");

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for CcList property.
     *
     * @return Current Value of CcList
     *
     */
    public ArrayList<String> getCcList() {
        return myCcList;
    }

    /**
     * Accessor method for Description property.
     *
     * @return Current Value of Description
     *
     */
    public String getDescription() {
        return myDescription;
    }

    /**
     * Accessor method for From property.
     *
     * @return Current Value of From
     *
     */
    public String getFrom() {
        return myFrom;
    }

    /**
     * Accessor method for Subject property.
     *
     * @return Current Value of Subject
     *
     */
    public String getSubject() {
        return mySubject;
    }

    /**
     * Accessor method for SubscriberList property.
     *
     * @return Current Value of SubscriberList
     *
     */
    public ArrayList<String> getSubscriberList() {
        return mySubscriberList;
    }

    /**
     * Accessor method for ToList property.
     *
     * @return Current Value of ToList
     *
     */
    public ArrayList<String> getToList() {
        return myToList;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for CcList property.
     *
     * @param aCcList New Value for CcList
     *
     */
    public void setCcList(ArrayList<String> aCcList) {
        myCcList = aCcList;
    }

    /**
     * Mutator method for Description property.
     *
     * @param aDescription New Value for Description
     *
     */
    public void setDescription(String aDescription) {
        myDescription = aDescription;
    }

    /**
     * Mutator method for From property.
     *
     * @param aFrom New Value for From
     *
     */
    public void setFrom(String aFrom) {
        myFrom = aFrom;
    }

    /**
     * Mutator method for Subject property.
     *
     * @param aSubject New Value for Subject
     *
     */
    public void setSubject(String aSubject) {
        mySubject = aSubject;
    }

    /**
     * Mutator method for SubscriberList property.
     *
     * @param aSubscriberList New Value for SubscriberList
     *
     */
    public void setSubscriberList(ArrayList<String> aSubscriberList) {
        mySubscriberList = aSubscriberList;
    }

    /**
     * Mutator method for ToList property.
     *
     * @param aToList New Value for ToList
     *
     */
    public void setToList(ArrayList<String> aToList) {
        myToList = aToList;
    }
}
