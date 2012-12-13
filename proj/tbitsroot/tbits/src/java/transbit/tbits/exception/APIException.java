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
 * APIException.java
 *
 * $Header:
 */
package transbit.tbits.exception;

//~--- non-JDK imports --------------------------------------------------------

//TBits Imports.
import transbit.tbits.common.TBitsLogger;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.util.ArrayList;

import com.lowagie.text.html.HtmlEncoder;

//~--- classes ----------------------------------------------------------------

//Third Party Imports.

/**
 * This class holds a list of Exceptions that occurred during API processing.
 *
 * @author  : Vaibhav.
 *
 * @version : $Id: $
 *
 */
public class APIException extends Throwable {
    public static final int INFO      = 0;
    public static final int WARNING   = 1;
    public static final int SEVERE    = 3;
    public static final int PERROR    = 2;
    public static final int FATAL     = 4;
    public static int       min_level = INFO;
    public static int       max_level = FATAL;

    //~--- fields -------------------------------------------------------------

    private ArrayList<ArrayList<TBitsException>> myExceptionList;
    protected int                                myLevel;

    //~--- constructors -------------------------------------------------------

    /**
     * The only constructor of this class.
     */
    public APIException() {
        myExceptionList = new ArrayList<ArrayList<TBitsException>>();

        for (int i = min_level; i <= max_level; i++) {
            myExceptionList.add(new ArrayList<TBitsException>());
        }
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Adds this exception to the end of the list with level as WARNING
     *
     * @param  aException Exception to be added to the list.
     *
     */
    public void addException(TBitsException aException) {

        // Defaulting to warning.
        int aLevel = min_level;

        myExceptionList.get(aLevel).add(aException);
    }

    /**
     * Adds this exception to the end of the list.
     *
     * @param  aException Exception to be added to the list.
     *
     */
    public void addException(TBitsException aException, int aLevel) {

        // Default any invalid levels to warnings.
        if ((aLevel < min_level) || (aLevel > max_level)) {
            aLevel = min_level;
        }

        myExceptionList.get(aLevel).add(aException);
    }

    /**
     * The toString method to display the APIException in a readable format.
     *
     * @return String format of this exception object.
     */
    public String toString() {

        // To Do: Decide on the format for command-line display.
//        StringBuilder message = new StringBuilder();
//        int           counter = min_level;
//
//        for (ArrayList<TBitsException> list : myExceptionList) {
//            for (TBitsException exception : list) {
//                message.append("\nLevel       : ").append(getLevelDesc(counter)).append("\n").append("",(exception));
//            }
//
//            counter++;
//        }
//
//        return message.toString();
    	return getErrorMessage();
    }

    /**
     * The toString method to display the APIException in a readable format.
     *
     * @return String format of this exception object.
     */
    public String getMessage() {

        // To Do: Decide on the format for command-line display.
        StringBuilder message = new StringBuilder();
        int           counter = min_level;

        for (ArrayList<TBitsException> list : myExceptionList) {
            for (TBitsException exception : list) {
                message.append("\nLevel       : ").append(getLevelDesc(counter)).append("\n").append(exception.toString());
            }

            counter++;
        }

        return message.toString();
    }
    
    /**
     * The toString method to display the APIException in a readable format that can be displayed directly on the front end.
     *
     * @return String format of this exception object.
     */
    public String getErrorMessage() {

        // To Do: Decide on the format for command-line display.
        StringBuilder message = new StringBuilder();

        int num = 1 ;
        for (int i = max_level ; i >= min_level  ; i-- ) 
        {
        	ArrayList<TBitsException> list = this.getExceptionList().get(i); 
            for (TBitsException exception : list)
            {
                message.append(num +". ")
                	.append(HtmlEncoder.encode(exception.getDescription()))
                	.append("<br/>");
                
                num++;
            }
        }

        return message.toString();
    }
    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the length of exception list length.
     *
     * @return Length of exception list.
     */
    public int getExceptionCount() {
        int size = 0;

        for (ArrayList<TBitsException> list : myExceptionList) {
            size = size + list.size();
        }

        return size;
    }

    /**
     * This method returns the number of exceptions above the given level.
     *
     * @param  aLevel Level.
     *
     * @return Count of exceptions.
     */
    public int getExceptionCount(int aLevel) {

        // Default any invalid levels to warnings.
        if ((aLevel < min_level) || (aLevel > max_level)) {
            aLevel = min_level;
        }

        int size = 0;

        for (int i = aLevel + 1; i <= max_level; i++) {
            size = size + myExceptionList.get(i).size();
        }

        return size;
    }

    /**
     * This method returns the arraylist of exception objects.
     *
     * @return List of exceptions
     */
    public ArrayList<ArrayList<TBitsException>> getExceptionList() {
        return myExceptionList;
    }

    /**
     * This method returns the string representation of a level.
     *
     * @param aLevel Level Id.
     *
     * @return String representation of level.
     */
    private static String getLevelDesc(int aLevel) {
        switch (aLevel) {
        case INFO :
            return "Information";

        case WARNING :
            return "Warning";

        case PERROR :
            return "Permission Error";

        case SEVERE :
            return "Severe";

        case FATAL :
            return "Fatal";
        }

        return "";
    }
}
