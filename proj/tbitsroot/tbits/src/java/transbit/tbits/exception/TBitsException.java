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
 * TBitsException.java
 *
 * $Id: $
 *
 */
package transbit.tbits.exception;

//~--- non-JDK imports --------------------------------------------------------

//Imports from the current package.
//Imports from TBits.
import transbit.tbits.common.TBitsLogger;

//~--- classes ----------------------------------------------------------------

//Java Imports.
//Third Party Imports.

/**
 * Exception sub-class that is used in TBits to throw exceptions that occur
 * in general.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 *
 */
public class TBitsException extends Exception {
    protected String myDescription;
    protected String myException;
    protected String myMessage;
    protected String myType;

    //~--- constructors -------------------------------------------------------

    /**
     * This method forms the exception object from the given exception
     *
     * @param aException Exception
     */
    public TBitsException(Exception aException) {
        myType        = "General";
        myMessage     = "TBits Application Exception.";
        myDescription = aException.toString();
        myException   = TBitsLogger.getStackTrace(aException);
    }

    /**
     * This method forms the exception object from the given message.
     *
     * @param aMessage Message to be shown when printing the exception.
     */
    public TBitsException(String aMessage) {
        myType        = "General";
        myMessage     = "TBits Application Exception.";
        myDescription = aMessage;
    }

    /**
     * This method forms the exception object from the given message.
     *
     * @param aMessage Message to be shown when printing the exception.
     * @param aException Exception
     *
     */
    public TBitsException(String aMessage, Exception aException) {
        myType        = "General";
        myMessage     = "TBits Application Exception.";
        myDescription = aMessage;
        myException   = TBitsLogger.getStackTrace(aException);
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method returns the string representation of the exception.
     *
     */
    public String toString() {
        StringBuilder message = new StringBuilder();

        message.append("Type        : ").append(myType).append("\n").append("Message     : ").append(myMessage).append("\n").append("Description : ").append(myDescription).append("\n");

        return message.toString();
    }

    //~--- get methods --------------------------------------------------------

    public String getDescription() {
        return myDescription;
    }

    public String getMessage() {
        return myMessage;
    }

    public String getType() {
        return myType;
    }

    //~--- set methods --------------------------------------------------------

    public void setDescription(String aDescription) {
        myDescription = aDescription;;
    }
}
