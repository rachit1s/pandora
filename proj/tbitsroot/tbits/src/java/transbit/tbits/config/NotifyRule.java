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
 * NotifyRule.java
 *
 * $Header:
 */
package transbit.tbits.config;

import java.io.Serializable;

/**
 * This class represents a rule in the NotificationRule XML Schema
 *
 * @author  : Vinod Gupta
 * @version : $Id: $
 */
public class NotifyRule implements Serializable{
    private String myDay;
    private String myEndTime;
    private String myId;
    private String myStartTime;
    private String myZone;

    //~--- constructors -------------------------------------------------------

    /**
     * The Default Constructor.
     */
    public NotifyRule() {
        myId        = "";
        myDay       = "";
        myStartTime = "";
        myEndTime   = "";
        myZone      = "";
    }

    /**
     * The parameterized constructor that initializes its members with the
     * values passed.
     *
     * @param  aId
     * @param  aDay
     * @param  aStartTime
     * @param  aEndTime
     * @param  aZone
     */
    public NotifyRule(String aId, String aDay, String aStartTime, String aEndTime, String aZone) {
        myId        = aId;
        myDay       = aDay;
        myStartTime = aStartTime;
        myEndTime   = aEndTime;
        myZone      = aZone;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method returns the string representation of the object
     */
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("[ ").append(myId).append(" ").append(myDay).append(" ").append(myStartTime).append(" ").append(myEndTime).append(" ").append(myZone).append(" ]");

        return str.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This is an accessor method of the Day Property.
     *
     * @return Current Value of Day Property.
     */
    public String getDay() {
        return myDay;
    }

    /**
     * This is an accessor method of the endTime Property.
     *
     * @return Current Value of endTime Property.
     */
    public String getEndTime() {
        return myEndTime;
    }

    /**
     * This is an accessor method of the Id Property.
     *
     * @return Current Value of Id Property.
     */
    public String getId() {
        return myId;
    }

    /**
     * This is an accessor method of the startTime Property.
     *
     * @return Current Value of startTime Property.
     */
    public String getStartTime() {
        return myStartTime;
    }

    /**
     * This is an accessor method of the zone Property.
     *
     * @return Current Value of zone Property.
     */
    public String getZone() {
        return myZone;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * This is a mutator method of the Day Property.
     *
     * @param aDay  new value of Day Property.
     */
    public void setDay(String aDay) {
        myDay = aDay;
    }

    /**
     * This is a mutator method of the endTime Property.
     *
     * @param aEndTime new value of endTime Property.
     */
    public void setEndTime(String aEndTime) {
        myEndTime = aEndTime;
    }

    /**
     * This is a mutator method of the Id Property.
     *
     * @param aId  new value of Id Property.
     */
    public void setId(String aId) {
        myId = aId;
    }

    /**
     * This is a mutator method of the startTime Property.
     *
     * @param aStartTime  new value of startTime Property.
     */
    public void setStartTime(String aStartTime) {
        myStartTime = aStartTime;
    }

    /**
     * This is a mutator method of the zone Property.
     *
     * @param aZone  new value of zone Property.
     */
    public void setZone(String aZone) {
        myZone = aZone;
    }
}
