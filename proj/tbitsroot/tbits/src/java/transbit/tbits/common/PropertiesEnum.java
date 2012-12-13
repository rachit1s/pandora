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
 * PropertiesEnum.java
 *
 * $Header:
 */
package transbit.tbits.common;

/**
 * This class has the list of keys for the database related properties, which
 * the {@link DataSourcePool} will look for in the properties file through the
 * {@link PropertiesHandler} for using DataSourcePool as a static factory of
 * connections.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public interface PropertiesEnum {

    // Application name and version.
    public static final String KEY_APP_NAME    = "transbit.app.name";
    public static final String KEY_APP_VERSION = "transbit.app.version";

    // Server where the TBits database is residing.
    public static final String KEY_DB_SERVER = "transbit.database.server";

    // Database password.
    public static final String KEY_DB_PASSWORD = "transbit.database.password";

    // Name of the TBits database.
    public static final String KEY_DB_NAME = "transbit.database.name";

    // Database login.
    public static final String KEY_DB_LOGIN = "transbit.database.login";

    // Database Driver
    public static final String KEY_DRIVER_NAME = "transbit.database.driverName";

    // Database driver tag.
    public static final String KEY_DRIVER_TAG = "transbit.database.driverTag";

    // Primary Pool Name
    public static final String KEY_DB_PRIMARY_POOL = "transbit.database.primaryPool";

    // List of database pools maintained by the Connection Pooler.
    public static final String KEY_DB_POOL_LIST = "transbit.database.poolList";

    // From address on failure notification message
    public static final String KEY_LOGGER_NOTIFY_FROM = "transbit.logger.notifyFailureFrom";

    // To address on failure notification message
    public static final String KEY_LOGGER_NOTIFY_TO = "transbit.logger.notifyFailureTo";

    public static final String TRANSBIT_TBITS_INCLUDE_RECIPIENTS = "transbit.tbits.includeRecipients";
	
}
