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
 * DatabaseException.java
 *
 * $Header:
 */
package transbit.tbits.common;

//~--- non-JDK imports --------------------------------------------------------

//Imports from current package.
import transbit.tbits.common.TBitsLogger;

//~--- JDK imports ------------------------------------------------------------

//Java imports
import java.sql.SQLException;

//~--- classes ----------------------------------------------------------------

/**
 * This class encapsulates the exceptions thrown during interaction with the
 * database.
 *
 * @author  : Vinod Gupta
 * @version : $Id: $
 */
public class DatabaseException extends Exception {

    // Constants to denote the type of Database Error.
    public static final int GENERAL_ERROR        = 1;
    public static final int CONNECTION_ERROR     = 2;
    public static final int AUTHENTICATION_ERROR = 3;

    //~--- fields -------------------------------------------------------------

    protected String myDescription;
    protected String myException;
    protected String myMessage;

    // Properties of DatabaseException.
    protected int myType;

    //~--- constructors -------------------------------------------------------

    /*
     * The only constructor of the object that takes the message about the
     * the place of occurence of the exception and the actual SQLException
     * object.
     *
     * @param aDescription String describing the error message.
     * @param sql          SQL Exception object that actually encapsulates the
     *                     error.
     */
    public DatabaseException(String aDescription, Exception sqle) {
        myMessage     = sqle.getMessage();
        myDescription = aDescription;
        myException   = TBitsLogger.getStackTrace(sqle);

        if (myMessage != null) {
            if ((myMessage.toLowerCase().indexOf("shutdown") >= 0) || (myMessage.toLowerCase().indexOf("connection refused") >= 0)) {
                myType = CONNECTION_ERROR;
            } else if (myMessage.toLowerCase().indexOf("authentic") >= 0) {
                myType = AUTHENTICATION_ERROR;
            } else {
                myType = GENERAL_ERROR;
            }
        } else {
            myType = GENERAL_ERROR;
        }
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method to Display the error message.
     *
     * @return String representation of this exception object.
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\nType: ").append(getType()).append("\nMessage: ").append(myMessage).append("\nDescription: ").append(myDescription).append("\nException: ").append(myException);

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method to get the description.
     *
     * @return Description of the exception.
     */
    public String getDescription() {
        return myDescription;
    }

    /**
     * Accessor method to get the message.
     *
     * @return Returns the message.
     */
    public String getMessage() {
        return myMessage;
    }

    /**
     * Accessor method to get the type of the error.
     *
     * @return Type of error.
     */
    public String getType() {
        String type = "";

        switch (myType) {
        case GENERAL_ERROR :
            type = "General Database Error";

            break;

        case AUTHENTICATION_ERROR :
            type = "Database Authentication Error";

            break;

        case CONNECTION_ERROR :
            type = "Database Connection Error";

            break;
        }

        return type;
    }
}
