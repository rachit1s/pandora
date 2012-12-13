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
 * DSQLParser.java
 *
 * $Header:
 *
 */
package transbit.tbits.exception;

/**
 * This is an exception class that is thrown when an error occurs in parsing.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public class DSQLParseException extends Exception {
    public DSQLParseException(String aMessage) {
        super(aMessage);
    }
}
