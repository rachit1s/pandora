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
 * DataSourcePool.java
 *
 * $Header:
 */
package transbit.tbits.common;

//~--- non-JDK imports --------------------------------------------------------

//TBits Imports.
import static transbit.tbits.Helper.TBitsConstants.poolList;
import static transbit.tbits.Helper.TBitsConstants.primaryPool;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.logicalcobwebs.proxool.ProxoolDataSource;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.configuration.PropertyConfigurator;

import uk.org.primrose.pool.jmx.PoolController;
import uk.org.primrose.pool.standalone.PoolControllerStandalone;

//~--- classes ----------------------------------------------------------------

/**
 * <p>
 * This class maintains a pool of connections to the database through the
 * <a href="http://www.primrose.org.uk" target="_blank">Primrose</a>
 * connection pooler. The name of the connection properties file is obtained
 * from <code>{@link Configuration#getDBPoolPropertiesFileName}</code>.
 * This file should be present in <code>APP_HOME/etc</code> directory.
 * </p>
 *
 * <p>
 * Primrose is capable of maintaining multiple pools, each connecting to a
 * different database. It also provides an admin interface to dynamically
 * change the properties of these pools. The properties that can appear in the
 * configuration file are basically divided into two categories:
 * <UL>
 * <LI>Admin properties<br>&nbsp;</LI>
 *     <UL type="square">
 *         <LI>
 *         <code>adminPoolControllerLogFile: [Required]</code><br>
 *         Absolute path to the file where pool events like start/stop should
 *         be logged. <br>
 *         <I>We advise you to create this file before using this class
 *         (probably during installation itself) if you are planning to run
 *         setuid scripts that use the connection pooler.
 *         .</I>
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>adminUser: [Required if you plan to use admin interface]
 *         </code><br>
 *         The user name for administering the pool via the web management
 *         interface.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>adminPassword:
 *         [Required if you plan to use admin interface]</code><br>
 *         The user name for administering the pool via the web management
 *         interface.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>adminWebManagementPort:
 *         [Required if you plan to use admin interface]</code><br>
 *         The port from which the admin interface should run.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>adminEmailAddress: [Required if you plan to use admin
 *         interface with email notification]</code><br>
 *         Email address of the admin users who should be notified about
 *         different pool events. Multiple email addresses can be specified
 *         by separating them with a comma.<br>
 *         e.g. <br>
 *         adminEmailAddress=<code>sandeep@transbittech.com</code>
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>adminEmailNotifications: [Required if you plan to use
 *         admin interface with email notification]</code><br>
 *         Set this to true if you want the pool to send emails for pool
 *         events like start/stop etc.,
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>adminEmailSMTPServer: [Required if you plan to use admin
 *         interface with email notification]</code><br>
 *         SMTP server to be used by Javamail to send email notifications.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>adminEmailCrisisAddress: [Required if you plan to use
 *         admin interface with email notification]</code><br>
 *         Crisis email address which will be used to send warnings to, after
 *         the adminEmailMaxWarningNumber property has been exceeded.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>adminEmailNotificationPeriod: [Required if you plan to
 *         use admin interface with email notification]</code><br>
 *         Time in milliseconds between warning emails.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>adminEmailMaxWarningNumber: [Required if you plan to
 *         use admin interface with email notification]</code><br>
 *         The maximum number of emails to be sent about a specific problem,
 *         before the crisis address is used.
 *         <br>&nbsp;</LI>
 *     </UL>
 * <LI>Pool properties<br>&nbsp;</LI>
 *     <UL type="square">
 *         <LI>
 *         <code>poolName: [Required]</code><br>
 *         Name of the pool.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>base: [Required]</code><br>
 *         Number of connections to be held to start with.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>overflow: [Required]</code><br>
 *         Maximum number of connection that can be held in this pool.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>user: [Required]</code><br>
 *         Database login.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>password: [Required]</code><br>
 *         Database password.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>log: [Required]</code><br>
 *         Absolute path to the file where the activities of this pool should
 *         be logged.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>idleTime: [Required]</code><br>
 *         Number of milliseconds to wait before releasing an idle connection.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>messageLogging: [Required]</code><br>
 *         Set this to true to enable logging the activities of this pool.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>sizeLogging: [Required]</code><br>
 *         Set this to true to enable logging the size of the pool.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>driverClass: [Required]</code><br>
 *         FQCN of the database driver class.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>driverURL: [Required]</code><br>
 *         JDBC url to be used with the above driver class.
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>cycleConnections: [Required]</code><br>
 *         Number of transactions to be performed through a connection object
 *         before it is refreshed (-1 to disable this).
 *         <br>&nbsp;</LI>
 *         <LI>
 *         <code>killActiveConnectionsOverAge: [Required]</code><br>
 *         Number of milliseconds over which connections will be killed if
 *         still active (-1 to disable this)
 *         <br>&nbsp;</LI>
 *     </UL>
 * </UL>
 * </p>
 *
 * <p>
 * To use this class in a web application that runs under <code>
 * <a href="http://jakarta.apache.org/tomcat" target="_blank">
 * Tomcat</a></code>, the following entries must be added
 * under the tag named <code>&lt;GloablNamingResources&gt;</code> in
 * <code>CATALINA_HOME/conf/server.xml</code>.
 *
 * <p>
 * <code>masterPool</code> resource is required and must be part of the
 * configuration in server.xml
 * </p>
 * <style>
 * .t{color: brown;}
 * .a{color:blue;}
 * .v{color:black;font-weight:bold;}
 * .b{text-decoration:underline; color:red}
 * </style>
 * <p style="margin-left: 50px; font-weight:bold;">
 * <code>
 * &lt;<span class="t">Resource</span>
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <span class="a">name</span>="masterPool"
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <span class="a">auth</span>="Container"
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <span class="a">type</span>="java.util.ArrayList"
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <span class="a">description</span>="This holds the list of all the pools."&gt;
 * <br>
 * &lt;<span class="t">/Resource</span>&gt;
 * <br>
 * &lt;<span class="t">ResourceParams</span>
 * <span class="a">name</span>="masterPool"&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">parameter</span>&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">name</span>&gt;
 * factory
 * &lt;<span class="t">/name</span>&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">value</span>&gt;
 * uk.org.primrose.pool.datasource.MasterPoolDataSourceFactory
 * &lt;<span class="t">/value</span>&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">/parameter</span>&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">parameter</span>&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">name</span>&gt;
 * configFile
 * &lt;<span class="t">/name</span>&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">value</span>&gt;
 * <span class='b'>/proj/myApp/myAppHome/etc/poolConfig.properties</span>
 * &lt;<span class="t">/value</span>&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">/parameter</span>&gt;<br>
 * &lt;<span class="t">/ResourceParams</span>&gt;
 * </code>
 * </p>
 * <p>
 * Now, for each pool defined in the pool configuration file, a
 * <code>&lt;Resource&gt;</code> entry must be specified as follows:
 * </p>
 * <p style="margin-left: 50px; font-weight:bold;">
 * <code>
 * &lt;<span class="t">Resource</span>
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <span class="a">name</span>="poolOne"
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <span class="a">auth</span>="Container"
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <span class="a">type</span>="uk.org.primrose.pool.datasource.PoolDataSource"
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <span class="a">description</span>="The first database pool"&gt;<br>
 * &lt;<span class="t">/Resource</span>&gt;<br>
 * &lt;<span class="t">ResourceParams</span>
 * <span class="a">name</span>="poolOne"&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">parameter</span>&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">name</span>&gt;
 * factory
 * &lt;<span class="t">/name</span>&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">value</span>&gt;
 * uk.org.primrose.pool.datasource.PoolDataSourceFactory
 * &lt;<span class="t">/value</span>&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">/parameter</span>&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">parameter</span>&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">name</span>&gt;
 * poolName
 * &lt;<span class="t">/name</span>&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">value</span>&gt;
 * poolOne
 * &lt;<span class="t">/value</span>&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;<span class="t">/parameter</span>&gt;
 * <br>
 * &lt;<span class="t">/ResourceParams</span>&gt;
 * </code>
 * </p>
 * <p>
 * Following entries must be added under the <code>&lt;Context&gt;</code> tag
 * of your application in
 * <code>CATALINA_HOME/conf/server.xml</code>:
 * </p>
 * <p style="margin-left: 50px; font-weight:bold;">
 * <code>
 * &lt;<span class="t">ResourceLink</span>
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <span class="a">global</span>="masterPool"
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <span class="a">name</span>="masterPool"
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <span class="a">type</span>="java.util.ArrayList"
 * <br>
 * /&gt;
 * <br>
 * </code>
 * </p>
 * <p>
 * Again for each pool defined in the pool configuration file, a
 * <code>&lt;ResourceLink&gt;</code> similar to the following
 * should be added:
 * </p>
 * <p style="margin-left: 50px; font-weight:bold;">
 * <code>
 * &lt;<span class="t">ResourceLink</span>
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <span class="a">global</span>="poolOne"
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <span class="a">name</span>="poolOne"
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <span class="a">type</span>="javax.sql.DataSource"
 * <br>
 * /&gt;
 * <br>
 * </code>
 * </p>
 *
 * <br>
 * <p>
 * Applications using primrose v2.7.0 have to call the
 * DataSourcePool.shutdownPooling() method to stop all the monitor threads
 * started by primrose for connection management.
 * <br><br>
 * </p>
 *
 *
 * @author Vaibhav
 * @version $Id: $
 */
public class DataSourcePool implements PropertiesEnum {

    // String to hold the package name.
    public static final String PKG_COMMON = "transbit.tbits.common";

    // Package-Wise logger to log application's errors/warnings/info.
 //   public static final TBitsLogger LOG                  = TBitsLogger.getLogger(PKG_COMMON);
    private static String           ourPrimaryPool       = "";
    private static boolean          ourIsPoolInitialized = false;
    private static boolean          firstAttemptOfInitialize = true;

    /*
     * This is a table of DataSource objects each keyed by the poolName.
     */
    private static Hashtable<String, DataSource> ourDSTable;
    private static String                        ourPoolConfigFileName;
    private static Hashtable<String, String>     ourPoolInitMessages;
    private static ArrayList<String>             ourPoolList;

    //~--- methods ------------------------------------------------------------

    /**
     * This method initializes all the connection pools.
     */
    private static synchronized void initializePools() {

    	 /*
         * Initialize the tables that hold the datasource for each poolName
         * if it is successfully created or the message if the attempt fails.
         */
        ourDSTable          = new Hashtable<String, DataSource>();
        ourPoolInitMessages = new Hashtable<String, String>();
        ourPoolList         = new ArrayList<String>();

        /*
         * Populate the list of pool names from the properties file.
         */
        getPoolList();

        /*
         * Print the warning on the error stream if the pool list is empty.
         * We will still continue and see if the PrimaryPool name is
         * available.
         */
        if (ourPoolList.size() == 0) {
            System.err.println("[ " + DataSourcePool.class.getName() + " ]: " + "Pool list is not specified");
        }

        /*
         * Check if the primary pool is specified in the properties file.
         * If not, use the first one in the list as the primary one.
         * Print a message about the same.
         */
        try {
          //  ourPrimaryPool = PropertiesHandler.getProperty(KEY_DB_PRIMARY_POOL);
        	ourPrimaryPool = primaryPool;
        } catch (IllegalArgumentException iae) {
            if (ourPoolList.size() != 0) {
                ourPrimaryPool = ourPoolList.get(0);
       //         LOG.warn(KEY_DB_PRIMARY_POOL + " property is not found. " + "Using the first one as the primary pool: " + ourPrimaryPool);
                System.err.println(KEY_DB_PRIMARY_POOL + " property is not found. " + "Using the first one as the primary pool: " + ourPrimaryPool);
            } else {
           System.err.println("Primary Pool Not specified and Pool list has zero pools");
            /*	String appName = "";

                try {
                    appName = PropertiesHandler.getProperty(KEY_APP_NAME);
                } catch (Exception e) {

                    *//*
                     * Consider the application name specified on the command
                     * line instead.
                     *//*
                    appName = Configuration.ourAppName;
                    System.out.println("[ " + DataSourcePool.class.getName() + " ]: " + "Using app.name instead..." + appName);
                }

                if ((appName != null) && (appName.trim().equals("") == false)) {
                    ourPrimaryPool = appName + "Pool";
                    ourPoolList.add(ourPrimaryPool);
                } else {
                    System.err.println("[ " + DataSourcePool.class.getName() + " ]: " + "Application name could not be obtained to form the " + "name of the primary pool.");
                } */
            }	
        }

        /*
         * Get the pool configuration file name. This is needed when user
         * requests for a connection from command line. If the poolConfig
         * filename is not found or is invalid, then print a warning message
         * mentioning the same.
         */
        getPoolConfigFileName();

        if ((ourPoolConfigFileName == null) || (ourPoolConfigFileName.trim().equals("") == true)) {
            System.err.println("[ " + DataSourcePool.class.getName() + " ]: " + "Invalid pool configuration file. " + "Pooler cannot be used in standalone application mode.");
        }

        /*
         * For each poolName present in the list, trying obtaining the data
         * source. If not, store the error message thrown out. This will be
         * displayed to the user whenever a connection is requested from
         * such pools.
         */
        for (String poolName : ourPoolList) {
        	if(ourDSTable.get(poolName) != null)
        		continue;
            try {
            	DataSource ds = getDataSource(poolName);

                ourDSTable.put(poolName, ds);
            } catch (Exception e) {
                String message = e.getMessage();

                if (message == null) {
                    message = e.toString();
                }

                ourPoolInitMessages.put(poolName, message);
         //       LOG.warn("Exception while initializing the pool : " + "\nPool Name: " + poolName + "\nMessage: " + message);
                System.err.println("Exception while initializing the pool : " + "\nPool Name: " + poolName + "\nMessage: " + message);
            }
        }
    }

    /**
     * Main method for testing.
     */
    public static void main(String arg[]) throws Exception {
            Connection con  = getConnection();
            Statement  stmt = con.createStatement();
            ResultSet  rs   = stmt.executeQuery("SELECT sys_id, display_name FROM business_areas");

            while (rs.next() != false) {
                System.out.println(rs.getInt("sys_id") + ". " + rs.getString("display_name"));
            }

            rs.close();
            stmt.close();
            con.close();
    }

//    /**
//     * This is a hook to shutdown all the connection pools.
//     * TODO: No longer required. It bulbs when this class is called by the same process (scheduler for eg.) again.
//       * 
//     */
//    public static void shutdownPooling() {
//        System.out.println("Shutting down pool. Is pool initialized: " + ourIsPoolInitialized);
//        //PoolControllerStandalone.shutdown();
//        //ourIsPoolInitialized = false;
//    }

    //~--- get methods --------------------------------------------------------
    private static ProxoolDataSource dataSource;
    private static boolean isInit = false;
    
    private synchronized static void init() throws ProxoolException{
    	Properties props = new Properties();
    	
    	props.setProperty("jdbc-0.proxool.alias", "property-test");
    	
    	setProperty(props, "user", "jdbc-0.user");
    	setProperty(props, "password", "jdbc-0.password");
    	setProperty(props, "driverURL", "jdbc-0.proxool.driver-url");
    	setProperty(props, "driverClass", "jdbc-0.proxool.driver-class");
    	setProperty(props, "killActiveConnectionsOverAge", "jdbc-0.proxool.maximum-connection-lifetime");
    	setProperty(props, "base", "jdbc-0.proxool.minimum-connection-count");
    	setProperty(props, "overflow", "jdbc-0.proxool.maximum-connection-count");
    	
    	props.putAll(ConnectionProperties.getDBPoolProperties());
    	
    	PropertyConfigurator.configure(props);
    	
    	dataSource = new ProxoolDataSource("property-test");
    	isInit = true;
    }
    
    private static void setProperty(Properties props, String fromName, String toName){
    	String s = ConnectionProperties.getDBPoolProperty(fromName);
    	if(s != null){
    		props.setProperty(toName, s);
    	}
    }
    
    public synchronized static Connection getConnection() throws SQLException{
    	if(!isInit){
    		try {
				init();
			} catch (ProxoolException e) {
				e.printStackTrace();
				throw new SQLException();
			}
    	}
    	Connection conn = dataSource.getConnection();
		
    	return conn;
    }
    
//    /**
//     * This method obtains a connection from the DataSource object corresponding
//     * to the default connection pool.
//     *
//     * @return Connection object.
//     * @throws SQLException Incase of errors.
//     */
//    public static Connection getConnection() throws SQLException {
//
//        /*
//         * Since we are not sure which method will be called first, check for
//         * proper initialization in both the getConnection methods.
//         */
//    	/*
//    	 * If first attempt of initialization: 
//    	 * 		then Only one thread should be allowed to initialize the pools 
//    	 * 			and get connection for the first time ,and others should be waiting. 
//    	 * else get connection 
//    	 * 
//    	 */
//   // 	System.out.println(firstAttemptOfInitialize);
//    	if(firstAttemptOfInitialize)
//    	{
//    //		System.out.println("***First Attempt");
//    		
//    		if ( !ourIsPoolInitialized ) 	//lock: only one thread should initialize pools
//    		{
//    			ourIsPoolInitialized = true; 
//    //        	System.out.println("\n\nThe pool is not initialized *****");
//            	initializePools();
//            	Connection aConnection = null;
//            	try{
//            		aConnection  = getConnection(ourPrimaryPool); 
//            	}
//            	finally
//            	{
//            		firstAttemptOfInitialize = false;
//            	}
//            	            	
//            	return aConnection;
//            	            	                
//            }
//    		else {
//    			System.out.println("Going to wait!");
//    			while(firstAttemptOfInitialize)
//    			{  
//    				//wait for 10ms until pools initialized and connection got by other thread
//    				try {
//						Thread.sleep(10);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						System.out.println("Thread got intruppted.");
//						break;
//					}
//    			}
//    //			System.out.println("Finished waiting.");
//    		}
//    	}
//    	
//    	return getConnection(ourPrimaryPool);
//    }

    /**
     * This method obtains a connection from the DataSource object corresponding
     * to the given poolName.
     *
     * @return Connection object.
     * @throws SQLException Incase of errors.
     */
    public static Connection getConnection(String aPoolName) throws SQLException {

    	 /*
         * Since we are not sure which method will be called first, check for
         * proper initialization in both the getConnection methods.
         */
    	//System.out.println("Getting connection from " + aPoolName + ". ourIsPoolInitialized: "+ ourIsPoolInitialized);
    	if ( !ourIsPoolInitialized ) {
            initializePools();
            ourIsPoolInitialized = true;
        }
    	
    	Connection aConnection = null;
		
        DataSource ds          = ourDSTable.get(aPoolName);
        //System.out.println("ourDSTable.get(aPoolName)");

        if (ds == null) {
        	//System.out.println("ds == null");
            String message = ourPoolInitMessages.get(aPoolName);

            if (message == null) {
                message = "Could not find DataSource object corresponding to " + "the pool: " + aPoolName;
            }

            System.err.println("[ " + DataSourcePool.class.getName() + " ]: " + message);

            return null;
        }

        try {
        	//System.out.println("aConnection = ds.getConnection();");
        	//ds.setLoginTimeout(10);
            aConnection = ds.getConnection();
            //System.out.println("After aConnection = ds.getConnection();");
        } catch (SQLException sqle) {
        	//System.out.println(sqle);
            sqle.printStackTrace();
            
            String message = sqle.toString().toLowerCase();

            // Check if the error is due to incorrent authentication details.
            if (message.indexOf("Login failed") >= 0) {
                throw sqle;
            }

            // Check if the error is due to inavailability of the server.
            if (message.indexOf("connection failed") >= 0) {

                /*
                 * Lets try pooling once again assuming there was a reset
                 * or a restart from the Database server side.
                 * Also lets log this as a warning.
                 */
            //    LOG.warning("Setting up the Connection Pool again assuming " + "a reset/restart from the Database Server side.");
            	System.err.println("Setting up the Connection Pool again assuming " + "a reset/restart from the Database Server side.");
                PoolController.restartSinglePool(aPoolName);
                aConnection = ds.getConnection();
            }
        }

        if (aConnection == null) {
            throw new SQLException("Unable to get connection to the database.");
        }
	
        return aConnection;
    }

    /**
     * This method creates a connection to the given DB Server and DB Name
     * using the DriverManager. Closing this connection is an obligation
     * on the caller's part.
     *
     * @param aDbServer  Database Server.
     * @param aDbName    Database Name.
     * @return Connection object.
     * @throws ClassNotFoundException If the DB Driver class is not found.
     * @throws SQLException           Database related errors.
     */
    public static Connection getConnection(String aDbServer, String aDbName, String aDbUser, String aDbPass, String aDriverClass, String aDriverTag) throws ClassNotFoundException, SQLException {
        Connection con = null;

        // Load the database driver.
        Class.forName(aDriverClass);

        // Get the JBDC URL.
        String url = getJDBCURL(aDriverTag, aDbServer, aDbName);

        // Get the connection
        con = DriverManager.getConnection(url, aDbUser, aDbPass);

        // Return it. Closing this is an obligation on the caller's side.
        return con;
    }

    /**
     * This method attempts to obtain the DataSource object from the Context if
     * the client accessing this class is in a Tomcat context. If the client
     * is in a standalone application mode, then it tries to load the
     * pool configuration properties file and then create a datasource object.
     *
     * @param aPoolName Name of the pool.
     * @return  DataSource object corresponding to this pool.
     * @throws Exception Incase of any errors.
     */
    private static DataSource getDataSource(String aPoolName) throws Exception {
        String     contextName = "java:comp/env/" + aPoolName;
        Context    ctx         = null;
        DataSource ds          = null;

//        try {
//
//            /*
//             * This is the code path in case the pooler is invoked from a tomcat
//             * context.
//             */
//            ctx = new InitialContext();
//            ds  = (DataSource) ctx.lookup(contextName);
//        } catch (NoInitialContextException nice) {
        	System.out.println("----------- getting data source -------------");
            /*
             * Code path when the invoker is a standalone application.
             */
        	System.out.println(ourPoolConfigFileName);
            PoolControllerStandalone.load(ourPoolConfigFileName);
            ctx = new InitialContext();
            ds  = (DataSource) ctx.lookup(contextName);
            System.out.println("----------- got data source -------------");
           
//        }

        return ds;
    }

    /**
     * This method forms the JDBC url that will be used while making connection
     * with the database.
     *
     * @return JDBCUrl to be used during connection establishment.
     */
    public static String getJDBCURL(String aDriverTag, String aDbServer, String aDbName) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("jdbc:").append(aDriverTag).append("://").append(aDbServer).append("/").append(aDbName);

        return buffer.toString();
    }

    /**
     * This method reads the name of connection pool configuration file from
     * the Configuration object.
     */
    public static void getPoolConfigFileName() {
        try {
            ourPoolConfigFileName = "";

            String fileName = Configuration.getDBPoolPropertiesFileName();

            if ((fileName == null) || (fileName.trim().equals("") == true)) {
                return;
            }

            File file = Configuration.findPath("etc/" + fileName);

            if (file == null) {
                return;
            }

            ourPoolConfigFileName = file.toString();
        } catch (Exception e) {
            ourPoolConfigFileName = "";
        }

        return;
    }

    /**
     * This method reads the poolList property value from the PropertiesHandler.
     *
     * @return List of pools.
     */
    private static void getPoolList() {
        try {
            String str = poolList;

            if ((str != null) && (str.trim().equals("") == false)) {
                ourPoolList = Utilities.toArrayList(str, ",");
            }
        } catch (IllegalArgumentException iae) {
            iae.getStackTrace();
        }

        return;
    }
}
