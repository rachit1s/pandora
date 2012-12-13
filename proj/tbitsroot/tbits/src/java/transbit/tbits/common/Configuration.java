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
 * Configuration.java
 *
 * $Header:
 */
package transbit.tbits.common;

//~--- JDK imports ------------------------------------------------------------

//Imports from the current package.
//Java Imports
import java.io.File;

import java.util.Properties;

//~--- classes ----------------------------------------------------------------

/**
 * This class looks for the following system properties passed to the
 * application through <code> java -D</code> command line option:
 * <UL>
 *     <LI>
 *         <code>app.name: [Mandatory]</code><br>
 *         This property should hold the name of the application. We'll refer
 *         to the value of this property as <code>APP_NAME</code> from here on.
 *         <br>&nbsp;
 *     </LI>
 *     <LI>
 *         <code>APP_NAME.home: [Mandatory]</code><br>
 *          This property should hold the home directory of the application.
 *          We'll refer to the value of this property as <code>APP_HOME</code>
 *          from here on.
 *         <br>&nbsp;
 *     </LI>
 *      <LI>
 *         <code>binaries.home: [Optional]</code><br>
 *          This property should hold the path of binaries used like
 *          htmlToText. If not specified it defaults to
 *          /prod/tbits/commons/bin
 *          <br>&nbsp;
 *     </LI>
 *     <LI>
 *         <code>app.propFile: [Optional]</code><br>
 *         This property holds the name of the file where the properties
 *         of the application are listed. If this property is not specified,
 *         the file name is assumed to be
 *         <code>APP_NAME.properties</code>. This file must be present in
 *         <code>APP_HOME/etc</code> directory.
 *         <br>&nbsp;
 *     </LI>
 *     <LI>
 *         <code>app.authFile: [Optional]</code><br>
 *         This property should hold the name of the file where the database
 *         authentication details are listed. If this property is not
 *         specified, the file name is assumed to be
 *         <code>APP_NAME.auth</code>. This file must be present in
 *         <code>APP_HOME/etc</code> directory.
 *         <br>&nbsp;
 *     </LI>
 *     <LI>
 *         <code>app.poolFile: [Optional]</code><br>
 *         This property should hold the name of the file where the properties
 *         related to database connection pooling  are listed. This file is
 *         used by the<a href="http://www.primrose.org.uk" target="_blank">
 *         Primrose</a> connection pooler when it is invoked from a standalone
 *         application. If this property is not specified, the file name
 *         is assumed to be <code>APP_NAME.dbpool</code>. This file must be
 *         present in <code>APP_HOME/etc</code> directory.
 *         <br>&nbsp;
 *     </LI>
 *     <LI>
 *         <code>app.logConfFile: [Optional]</code><br>
 *         This property should hold the name of the file where Log4Java's
 *         configuration properties are listed. This file is
 *         used by the {@link DESLogger}. If this property is not specified,
 *         the file name is assumed to be <code>log4java.conf</code>. This
 *         file must be present in <code>APP_HOME/etc</code> directory.
 *         <br>&nbsp;
 *     </LI>
 * </UL>
 * <br>
 * <p>
 * Following are the ways of invoking applications that use TBits-commons.
 * <br>
 * <UL>
 * <LI>Minimal way:<br>
 * e.g.<br><code>
 * &nbsp;&nbsp;&nbsp;&nbsp;
 *     /usr/local/java/jdk1.5/bin/java<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *         -Dapp.name=myApp <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *         -DmyApp.home=/proj/myApp/myAppHome
 *         <br>&nbsp;&nbsp;&nbsp;&nbsp;
 *         &lt;tomcat's bootstrap.jar&gt;
 * </code>
 * <br>
 * The following assumptions are made in the above case:<br>
 *
 * <table border=1 cellspacing=0 cellpadding=3>
 *     <THEAD>
 *         <td>Item</code></td>
 *         <td>Default value</td>
 *     </THEAD>
 *     <tr>
 *         <td><code>Application Properties</code></td>
 *         <td><code>/proj/myApp/myAppHome/etc/myApp.properties</code></td>
 *     </tr>
 *     <tr>
 *         <td><code>Database Authentication Details</code></td>
 *         <td><code>/proj/myApp/myAppHome/etc/myApp.auth</code></td>
 *     </tr>
 *     <tr>
 *         <td><code>Connection Pool Configuration</code></td>
 *         <td><code>/proj/myApp/myAppHome/etc/myApp.dbpool</code></td>
 *     </tr>
 *     <tr>
 *         <td><code>Log4Java Configuration</code></td>
 *         <td><code>/proj/myApp/myAppHome/etc/log4java.conf</code></td>
 *     </tr>
 * </table>
 * </LI>
 * <LI>
 * Specifying all the properties:<br>
 * e.g.<br><code>
 * &nbsp;&nbsp;&nbsp;&nbsp;
 *     /usr/local/java/jdk1.5/bin/java<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *         -Dapp.name=tbits <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *         -tbits.home=/proj/tbits/tbits-home
 *         <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *         -Dapp.propFile=myApp.conf
 *         <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *         -Dapp.authFile=myApp_authentication.conf
 *         <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *         -Dapp.poolFile=myApp_pooling.conf
 *         <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *         -Dapp.logConfFile=log4j.properties
 *         <br>&nbsp;&nbsp;&nbsp;&nbsp;
 *         &lt;tomcat's bootstrap.jar&gt;
 * </code>
 * <table border=1 cellspacing=0 cellpadding=3>
 *     <THEAD>
 *         <td>Item</code></td>
 *         <td>Default value</td>
 *     </THEAD>
 *     <tr>
 *         <td><code>Application Properties</code></td>
 *         <td><code>/proj/myApp/myAppHome/etc/myApp.conf</code></td>
 *     </tr>
 *     <tr>
 *         <td><code>Database Authentication Details</code></td>
 *         <td>
 *             <code>
 *                 /proj/myApp/myAppHome/etc/myApp_authentication.conf
 *             </code>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td><code>Connection Pool Configuration</code></td>
 *         <td><code>/proj/myApp/myAppHome/etc/myApp_pooling.conf</code></td>
 *     </tr>
 *     <tr>
 *         <td><code>Log4Java Configuration</code></td>
 *         <td><code>/proj/myApp/myAppHome/etc/log4j.properties</code></td>
 *     </tr>
 * </table>
 * </LI>
 * </UL>
 * </p>
 *
 * <p>
 * Note:<br>
 * If <code>app.authFile</code> is not specified and the default file is not
 * found, it is assumed that the authentication related properties are
 * present in the application's properties file.<br>
 * <i>
 * But, it is a good practice to separate the database authentication details
 * from the main properties and place them in a separate file.
 * </i>
 * </p>
 * TODO: to be converted to singleton
 * @author  : Vaibhav.
 * @version : $Id: $
 * @see transbit.tbits.common.PropertiesHandler
 */
public class Configuration {

    /*
     * To start with, the list of directories we can search for is empty.
     * If the TBits commons environment is properly initialized, then this
     * will contain the directories from the application that's using this
     * library.
     */
    private static File[] ourDirectoriesToSearch = new File[] {};

    /*
     * Name of the file that holds the primrose connection pooler properties.
     * Default name would be <appName>.dbpool. But clients can specify the
     * filename, if different, using the "app.dbPoolFile" command line option.
     */
    private static String ourDBPoolPropFileName = null;

    /*
     * Name of the application's authentication properties file. Default name
     * would be <appName>.auth. But clients can specify the filename,
     * if different, using the "app.authFile" command line option.
     */
    private static String ourDBAuthFileName = null;

    /*
     * location of Build directory of the application that's using this library.
     */
    private static File ourBuildHome = null;

    /*
     * Name of the application properties file. Default name would be
     * <appName>.properties. But clients can specify the filename, if different,
     * using the "app.propFile" command line option.
     */
    private static String ourAppPropFileName = null;

    /*
     * Name of the application as passed on the command line. Package Scope.
     */
    static String ourAppName = null;

    /*
     * Home location of the application that's using this library.
     */
    private static File ourAppHome = null;

    /*
     * Name of the application's Logger properties. Default value would be
     * log4java.conf. But clients can specify the filename, if different,
     * using the "app.logConfFile" command line option. This will be used to
     * initialize the Apache's Log4Java environment.
     */
    private static String ourLogConfFileName = "log4java.conf";

    /*
     * Package level variable to inform other classes in this package about
     * the initialization process.
     */
    static boolean ourIsEnvInitialized = false;

    /*
     * Location of the binaries used for extracting text out of html. Default
     * location is /prod/tbits/commons/bin. Users can specify a different
     * locaiton using "binaries.home" system property.
     */
    private static final String TBits_COMMONS_PATH = "/usr/bin";

    //~--- static initializers ------------------------------------------------

    static {
        load();
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Return the path to search for relative pathnames.
     *
     * @return The list of directories to search.
     */
    public static File[] directoriesToSearch() {
        if (ourDirectoriesToSearch == null) {
            throw new IllegalStateException("ourDirectoriesToSearch == null");
        }

        File[] dirs = new File[ourDirectoriesToSearch.length];

        for (int i = 0; i < ourDirectoriesToSearch.length; i++) {
            dirs[i] = new File(ourDirectoriesToSearch[i].getPath());
        }

        return dirs;
    }

    /**
     * Return the list of possible pathname resolutions, in the order
     * from most-specialized to most default.
     *
     * An absolute pathname will simply be returned.  A relative pathname
     * will include the results of {@link #directoriesToSearch()},
     * each suffixed by the pathname.
     *
     * @param pathname The pathname to resolve.
     * @return The list of possible file locations.
     */
    public static File[] expandPath(String pathname) {
        File pn = new File(pathname);

        if (pn.isAbsolute()) {
            return new File[] { pn };
        } else {
            File[] dirs = directoriesToSearch();

            for (int i = 0; i < dirs.length; i++) {
                dirs[i] = new File(dirs[i], pathname);
            }

            return dirs;
        }
    }

    /**
     * Resolves a pathname.
     *
     * Search the result of {@link #expandPath(String)} in order
     * from first to last, and return the first entry that actually exists.
     *
     * @param pathname Pathname to be resolved.
     * @return  Returns the resolved pathname.  Returns null if the
     *          specified pathname could not be resolved (i.e. the file
     *          does not exist).
     */
    public static File findPath(String pathname) {
        File[] pnames = expandPath(pathname);

        for (int i = 0; i < pnames.length; i++) {
            if (pnames[i].exists()) {
                return pnames[i];
            }
        }

        // Looks for the directory.
        for (int i = 0; i < pnames.length; i++) {
            if (pnames[i].isDirectory()) {
                return pnames[i];
            }
        }

        return null;
    }

    /**
     * Loads configuration files if they have not been loaded yet.
     * This function does nothing if load() or reload() has
     * already been invoked.
     */
    private static synchronized void load() {
        Properties sysProps = System.getProperties();

        /*
         * Following are the mandatory system properties that should be passed
         * from command line using -D option:
         *      - app.name
         *      - <appName>.property
         */
        ourAppName = sysProps.getProperty("app.name");

        if ((ourAppName == null) || (ourAppName.trim().equals("") == true)) {
            System.err.println("[ " + Configuration.class.getName() + " ]: " + "The mandatory system property app.name " + "is not passed from command line using -D " + "option. \n" + "[ "
                               + Configuration.class.getName() + " ]: " + "So, TBits Commons' configuration " + "and ProperpertiesHandler classes are not " + "properly initialized");
            ourIsEnvInitialized = false;

            return;
        }

//        System.out.println("The available properties are:");
//        
//        for(Object keyObj: sysProps.keySet())
//        {
//            String key = (String) keyObj;
//            System.out.println(key + ":" + sysProps.getProperty(key));
//        }
        // Get the application Home.
        String appHomePropName = ourAppName + ".home";
        String appHome         = sysProps.getProperty(appHomePropName);
        
        if (appHome == null) {
            System.err.println("[ " + Configuration.class.getName() + " ]: " + "The mandatory system property " + ourAppName + ".home is not passed from command line using " + "-D option. \n" + "[ "
                               + Configuration.class.getName() + " ]: " + "So, TBits Commons' configuration" + " and ProperpertiesHandler classes are not " + "properly initialized");
            ourIsEnvInitialized = false;

            return;
        }
        
        ourIsEnvInitialized = true;

        /*
         * Following is system properties to state comma separated list of
         * binaries home path and should be passed from command line
         * using -D option.
         *      - binaries.home
         */
        String binariesHome = sysProps.getProperty("binaries.home");

        if ((binariesHome == null) || (binariesHome.trim().equals("") == true)) {
            binariesHome = TBits_COMMONS_PATH;
        }

        // Get the file object to Application Home.
        ourAppHome = new File(appHome);

        String temp = appHome;

        temp = temp + "/build";

        // Get the File Object to the Build Directory.
        ourBuildHome = new File(temp);

        // Get the File Object to the WebApps Directory.
        File webappsHome = new File(ourAppHome + "/webapps");
        File etcFile     = new File(ourBuildHome + "/etc");

        // for install directory there wont be a build
        File etcInstallFile = new File(ourAppHome + "/etc");

        // Get file object to binaries path
        File binaryFile = new File(binariesHome);

        /*
         * Form the list of directories to search in when user wants to
         * find a file.
         */
        ourDirectoriesToSearch = new File[] {
            ourBuildHome,      // Location of build directory.
            ourAppHome,        // Location of Application Home.
            webappsHome,       // Location of webapps directory.
            etcFile,           // Location of Application's property file.
            etcInstallFile,    // Location of Install Directory.
            binaryFile         // Location of Binaries Home.
        };

        String key      = "";
        String defValue = "";

        /*
         * Check if app.propFile property is specified on the command line.
         * Otherwise take the <appName>.properties as the file name by default.
         */
        key                = "app.propFile";
        defValue           = ourAppName + ".properties";
        ourAppPropFileName = getValue(sysProps, key, defValue);

        /*
         * Check if app.authFile property is specified on the command line.
         * Otherwise take the <appName>.auth as the file name by default.
         */
        key               = "app.authFile";
        defValue          = ourAppName + ".auth";
        ourDBAuthFileName = getValue(sysProps, key, defValue);

        /*
         * Check if app.dbPoolFile property is specified on the command line.
         * Otherwise take the <appName>.dbpool as the file name by default.
         */
        key                   = "app.dbPoolFile";
        defValue              = ourAppName + ".dbpool";
        ourDBPoolPropFileName = getValue(sysProps, key, defValue);

        /*
         * Check if app.logConfFile property is specified on the command line.
         * Otherwise take log4java.conf as the file name by default.
         */
        key                = "app.logConfFile";
        defValue           = "log4java.conf";
        ourLogConfFileName = getValue(sysProps, key, defValue);
    }

    /**
     * The Main Method.
     */
    public static void main(String arg[]) {}

    //~--- get methods --------------------------------------------------------

    /**
     * Returns the value of &lt;app.home&gt;.
     *
     * @exception IllegalArgumentException If the property is not defined.
     */
    public static File getAppHome() {
        if (ourAppHome == null) {
            throw new IllegalStateException("ourAppHome == null");
        }

        return ourAppHome;
    }

    /**
     * This is an accessor method that returns the name of the file that holds
     * the database authentication details.
     *
     * @return The name of the authentication file of the application.
     */
    public static String getAuthenticationFileName() {
        return (ourDBAuthFileName == null)
               ? ""
               : ourDBAuthFileName;
    }

    /**
     * This is an accessor method for getting the name of the file that holds
     * the properties related to database connection pooling.
     *
     * @return The name of the db pooling properties file.
     */
    public static String getDBPoolPropertiesFileName() {
        return (ourDBPoolPropFileName == null)
               ? ""
               : ourDBPoolPropFileName;
    }

    /**
     * This is an accessor method for getting the name of the file that holds
     * the DESLogger configuration settings.
     *
     * @return The name of the Log4java's configuration file.
     */
    public static String getLogConfFileName() {
        return (ourLogConfFileName == null)
               ? ""
               : ourLogConfFileName;
    }

    /**
     * This is an accessor method for getting the file name containing the
     * Application's properties
     *
     * @return The name of the property file of the application.
     */
    public static String getPropertyFileName() {
        return ourAppPropFileName;
    }

    public static String findAbsolutePath(String path) {
    	File file = findPath(path);
    	if(file == null)
    		return null;
    	return file.getAbsolutePath();
    }
    /**
     * This method checks if the given key is present in the specified list
     * of properties. If present and the value is non-null and non-empty, then
     * this value is returned. Otherwise, the default value is returned.
     *
     * @param prop
     * @param key
     * @param defValue
     * @return
     */
    private static String getValue(Properties prop, String key, String defValue) {
        String value = defValue;

        // If prop object itself is null, then return the default value.
        if (prop == null) {
            return defValue;
        }

        value = prop.getProperty(key);

        /*
         * Use the default value if the is key is not associated with any other
         * value in the properties object.
         */
        if ((value == null) || (value.trim().equals("") == true)) {
            value = defValue;
        } else {
            value = value.trim().toString();
        }

        return value;
    }
}
