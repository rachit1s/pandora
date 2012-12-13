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
 * Permission.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

/**
 * This is the permission class that would hold the enum values for different
 * types of permissions.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 *
 */
public class Permission {
    public static final int ADD       = 1;
    public static final int CHANGE    = 2;
    public static final int VIEW      = 4;
    public static final int DISPLAY   = 8;
    public static final int EMAIL_VIEW = 8;
    public static final int D_ACTION  = 16;
    public static final int SEARCH    = 32;
    public static final int SET       = 64;
    public static final int HYPERLINK = 128;
    public static final int IS_REQUEST_UNIQUE = 256;
    public static final int IS_ACTION_UNIQUE = 512;
}
