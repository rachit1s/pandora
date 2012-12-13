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
 * DESLogger.java
 *
 * \$Header:
 */
package transbit.tbits.common;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.log4j.Level;

//Log4j imports
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

//Current Package imports
import transbit.tbits.common.Configuration;
import transbit.tbits.common.PropertiesHandler;

//~--- JDK imports ------------------------------------------------------------

//Java imports
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

//~--- classes ----------------------------------------------------------------

/**
 * <p>
 * This class is a wrapper over the Logger class provided by
 * <a href=http://logging.apache.org/log4j/" target="_blank">
 * Log4Java</a>.
 *
 * It
 * <UL>
 *  <LI>handles exceptions and sends a mail if the LOG event has a level
 *      higher than the threshold level specified in the config file.</LI>
 *  <LI>retrieves a logger object for the corresponding package name.</LI>
 * </UL>.
 *  To use TBitsLogger in your applications,
 * <UL>
 *  <LI>configure the logging environment by editing the log4j.conf file.</LI>
 *  <LI>retrieve a logger object in every class you wish to perform logging
 *      by calling one of the TBitsLogger constructors.</LI>
 *   <LI>
 *      use the printing methods for logging the statements wherever required.
 *   </LI>
 * </UL>.
 *
 * The logging environment can be configured through the
 * log4java.conf file in $APP_HOME/etc.The properties that can be found
 * here are
 * <UL>
 *     <LI>
 *         <code>log4j.rootLogger: [ LEVEL, appenderName[1],
 *               appenderName[2], ....]
 *         </code><br>
 *         This property should hold the threshold level of the root logger
 *         followed by the appenders that need to be attached to it.The root
 *         logger is at the top of the hierarchy and all the log statements
 *         which have a level greater than or equal to the level specified
 *         will get logged into all the appenders mentioned here. Level
 *         relates to the importance of the statement as judged by the
 *         developer.Level can take case insensitive values "DEBUG", "INFO",
 *         "WARN", "SEVERE"(alias "ERROR") and "FATAL", the list being in the
 *         increasing order of importance.
 *         <br>&nbsp;
 *     </LI>
 *     <LI>
 *         <code>log4j.logger.&lt;loggerName&gt;: [ LEVEL, appenderName[1],
 *               appenderName[2]....]</code><br>
 *         where loggerName is the name of the logger you wish to configure,
 *         LEVEL is the logger level filter( all log statements in the
 *               corresponding package which have a level higher then this
 *               level will get logged into all the appenders mentioned here.
 *               ) and appenderName[1], appenderName[2]... are the names
 *               of the appenders attached to this logger.
 *         <br>&nbsp;
 *     </LI>
 *     <LI>
 *         <code>log4j.appender.&lt;appenderName[i]&gt; =
 *                             fully.qualified.name.of.appender.class</code>
 *              This needs to be defined for the all the appenders that have
 *              been attached to the loggers.We suggest you use the
 *              transbit.tbits.common.DESAppender class for directing the log
 *              statements to the command line and transbit.tbits.common.
 *              TBitsFileAppender for directing the statements to a specified
 *              file.Note that in case you are using the TBitsFileAppender class
 *              you need to specify the corresponding file by using through
 *             the log4j.appender.appenderName[i].File property.
 *         <br>&nbsp;
 *     </LI>
 *     <LI>
 *         <code>log4j.appender.&lt;appenderName[i]&gt;.layout =
 *                            fully.qualified.name.of.layout.class</code>
 *              This property defines the layout for each of the appender.
 *              The layout class customizes the way the log statement renders
 *              itself before getting printed. We suggest you use the
 *              org.apache.log4j.PatternLayout here.
 *     </LI>
 * </UL>
 *      Here is a sample configuration file
 *
 *     <code>
 *     <br>
 *     ---------------Root logger ----------------------------------------
 *     <br>&nbsp;&nbsp;log4j.rootLogger = DEBUG, aTest
 *     <br>&nbsp;&nbsp;log4j.appender.aTest = transbit.tbits.common.TBitsAppender
 *     <br>&nbsp;&nbsp;log4j.appender.aTest.layout =
 *                                       org.apache.log4j.PatternLayout
 *     <br>&nbsp;&nbsp;log4j.appender.aTest.layout.ConversionPattern =
 *                                       [%r %t %m];
 *     <br>
 *     <br>
 *     ---------------Custom logger -----------------------------------------
 *     <br>&nbsp;&nbsp;log4j.logger.email = INFO, aMail
 *     <br>&nbsp;&nbsp;log4j.appender.aMail =
 *                                        transbit.tbits.common.TBitsFileAppender
 *     <br>&nbsp;&nbsp;log4j.appender.aMail.File =
 *                               /u/Vinod Gupta/tbits-3.0/tbits/tmp.log
 *     <br>&nbsp;&nbsp;log4j.appender.aTest.layout =
 *                                 org.apache.log4j.PatternLayout
 *     <br>&nbsp;&nbsp;log4j.appender.aTest.layout.ConversionPattern =
 *                                 [%r %t %m];</code>
 *
 *     <br>&nbsp;
 *     <br>Here all the log requests above the debug level will be printed
 *     on the command line and all log statements in the package for which
 *     the email logger is defined and whose level is greater than or equal
 *     to INFO are logged into the tmp.log file.The message format specified
 *     by [%r %t %m] corresponds to [ time since the start of the program,
 *     thread that  executed the request, message of the request].
 *
 * </p>
 *
 *
 *
 * @author : Vinod Gupta
 * @author : Vaibhav
 * @version : $Id: $
 */
public class TBitsLogger implements PropertiesEnum {

    // Fully Qualified Class Name.
    private static String      FQCN           = TBitsLogger.class.getName();
    public static final int    SOURCE_WEB     = 101;
    public static final int    SOURCE_EMAIL   = 102;
    public static final int    SOURCE_CMDLINE = 103;
    public static final String PROP_BA_PREFIX = "tbits.cmd.sysPrefix";
    public static final String PROP_BA_NAME   = "tbits.email.sysName";

    // Source of the app that called the TBitsLogger.
    public static int      ourSource                = SOURCE_WEB;
    private static boolean ourIsLog4JavaInitialized = false;

    // List of Property keys that will be used in this file.
    private static final String PROP_LOG4J_INIT = "log4j.defaultInitOverride";
    private static final String PROP_LOG4J_CONF = "log4j.configuration";

    //~--- fields -------------------------------------------------------------

    // The underlying Logger object that provides the functionality to log
    // info/error/warning messages.
    private Logger myLogger;

    //~--- constructors -------------------------------------------------------

    /**
     *
     * Constructor for TBitsLogger.This returns the logger object based on
     * the source of the logging event.
     */
    private TBitsLogger() {
        if (ourIsLog4JavaInitialized == false) {
            initialize();
        }

        try {
            switch (ourSource) {
            case SOURCE_WEB :
                myLogger = Logger.getLogger("web");

                break;

            case SOURCE_EMAIL :
                myLogger = Logger.getLogger("email");

                break;

            case SOURCE_CMDLINE :
                myLogger = Logger.getLogger("cmdline");

                break;

            default :
                myLogger = Logger.getRootLogger();

                break;
            }
        } catch (Exception e) {
            myLogger = Logger.getRootLogger();
        }
    }

    /**
     * This is a constructor which creates a Logger object  with the name
     * specified.
     *
     */
    private TBitsLogger(String name) {
        if (ourIsLog4JavaInitialized == false) {
            initialize();
        }

        try {
            String loggerName = (((name == null) || (name.trim().equals("") == true))
                                 ? ""
                                 : "." + name);

            switch (ourSource) {
            case SOURCE_WEB :
                myLogger = Logger.getLogger("web" + loggerName);

                break;

            case SOURCE_EMAIL :
                myLogger = Logger.getLogger("email" + loggerName);

                break;

            case SOURCE_CMDLINE :
                myLogger = Logger.getLogger("cmdline" + loggerName);

                break;

            default :
                myLogger = Logger.getLogger(name);

                break;
            }
        } catch (Exception e) {
            myLogger = Logger.getLogger("name");
        }
    }

    /**
     * A Constructor which creates a Logger object for the package specified.
     *
     * @param aLoggerName    Name of the Logger
     * @param aPackageName   Name of the package.
     *
     */
    private TBitsLogger(String aLoggerName, String aPackageName) {
        if (ourIsLog4JavaInitialized == false) {
            initialize();
        }

        boolean lValid = false;
        boolean pValid = false;
        String  name   = "";

        if (aLoggerName != null & aLoggerName.trim().equals("") == false) {
            lValid = true;
        }

        if (aPackageName != null & aPackageName.trim().equals("") == false) {
            pValid = true;
        }

        if ((pValid == true) && (lValid == true)) {
            name     = aLoggerName + "." + aPackageName;
            myLogger = Logger.getLogger(name);
        } else if (pValid == true) {
            name     = aPackageName;
            myLogger = Logger.getLogger(name);
        } else if (lValid == true) {
            name     = aLoggerName;
            myLogger = Logger.getLogger(name);
        }

        return;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Printing Method on Debug Level
     *
     * @param msg   The obj to be logged
     *
     */
    public void debug(Object msg) {
        myLogger.log(FQCN, Level.DEBUG, msg, null);
    }

    /**
     * Printing Method on Debug Level including stack trace of
     *  Throwable t
     *
     * @param msg   The obj to be logged
     * @param t     Throwable
     */
    public void debug(Object msg, Throwable t) {
        myLogger.log(FQCN, Level.DEBUG, msg, t);
    }

    /**
     * This methods detects the source of the logging event.(viz web, email
     * or command line.)
     */
    private static void detectSource() {

        // We assume that this is a request from app server.
        ourSource = SOURCE_WEB;

        // Check if this is a call from an email.
        try {
            try {
                PropertiesHandler.getProperty(PROP_BA_NAME);
            } catch (IllegalArgumentException iae) {
                String propName = KEY_APP_NAME + ".email";

                PropertiesHandler.getProperty(propName);
            }

            ourSource = SOURCE_EMAIL;
        } catch (IllegalArgumentException iae1) {
            try {
                try {
                    PropertiesHandler.getProperty(PROP_BA_PREFIX);
                } catch (IllegalArgumentException iae) {
                    String propName = KEY_APP_NAME + ".cmdline";

                    PropertiesHandler.getProperty(propName);
                }

                ourSource = SOURCE_CMDLINE;
            } catch (IllegalArgumentException iae2) {

                // This is not for command-line invocation.
                ourSource = SOURCE_WEB;
            }
        }
    }

    /**
     * Printing Method on Severe Level.This also sends an email
     * once a Logging event of a severe level occurs.
     *
     * @param msg   The obj to be logged
     *
     */
    public void error(Object msg) {
        myLogger.log(FQCN, Level.ERROR, msg, null);
    }

    /**
     * Printing Method on Severe Level including stack trace of
     * Throwable t.This also sends an email once a Logging
     * event of a severe level occurs.
     *
     * @param msg   The obj to be logged
     * @param t     Throwable
     */
    public void error(Object msg, Throwable t) {
        myLogger.log(FQCN, Level.ERROR, msg, t);
    }

    /**
     * Printing Method on Fatal Level
     *
     * @param msg   The obj to be logged
     *
     */
    public void fatal(Object msg) {
        myLogger.log(FQCN, Level.FATAL, msg, null);
    }

    /**
     * Printing Method on Fatal Level including stack trace of
     *  Throwable t
     *
     * @param msg   The obj to be logged
     * @param t     Throwable
     */
    public void fatal(Object msg, Throwable t) {
        myLogger.log(FQCN, Level.FATAL, msg, t);
    }

    /**
     * Printing Method on Info Level
     *
     * @param msg   The obj to be logged
     *
     */
    public void info(Object msg) {
        myLogger.log(FQCN, Level.INFO, msg, null);
    }

    /**
     * Printing Method on Info Level including stack trace of
     *  Throwable t
     *
     * @param msg   The obj to be logged
     * @param t     Throwable
     */
    public void info(Object msg, Throwable t) {
        myLogger.log(FQCN, Level.INFO, msg, t);
    }

    private void initialize() {

        /*
         * PropertiesHandler will initialize the logging properties and will 
         * pass these properties to the PropertyConfigurator
         *  
         */
        
   /* 	String logConfFileName = Configuration.getLogConfFileName();
        File   logConfFile     = Configuration.findPath("etc/" + logConfFileName);

        if (logConfFile == null) {
            System.err.println("[ " + TBitsLogger.class.getName() + " ]: " + "Log4J Configuration file not found: " + logConfFileName);
        } else {
            String confFilePath = logConfFile.toString();
            System.setProperty(PROP_LOG4J_INIT, "true");
            System.setProperty(PROP_LOG4J_CONF, confFilePath);
            PropertyConfigurator.configure(PropertiesHandler.getLoggingProperties());
            ourIsLog4JavaInitialized = true;
            detectSource();
        }
*/
            System.setProperty(PROP_LOG4J_INIT, "true");
            PropertyConfigurator.configure(PropertiesHandler.getLoggingProperties());
            ourIsLog4JavaInitialized = true;
            detectSource();
       
    }

    /**
     * Main method.
     */
    public static void main(String arg[]) {
        TBitsLogger LOG = TBitsLogger.getLogger("transbit.tbits.common");

        LOG.info("Test Info Message...");
    }

    /**
     * Method for removing an appender with the name passed as parameter
     * from the list of appenders.
     *
     */
    public void removeAppender(String aName) {
        myLogger.removeAppender(aName);
    }

    /**
     * Printing Method on Severe Level.This also sends an email
     * once a Logging event of a severe level occurs.
     *
     * @param msg   The obj to be logged
     *
     */
    public void severe(Object msg) {
        myLogger.log(FQCN, Level.ERROR, msg, null);
    }

    /**
     * Printing Method on Severe Level including stack trace of
     * Throwable t.This also sends an email once a Logging
     * event of a severe level occurs.
     *
     * @param msg   The obj to be logged
     * @param t     Throwable
     */
    public void severe(Object msg, Throwable t) {
        myLogger.log(FQCN, Level.ERROR, msg, t);
    }

    /**
     * Printing Method on Warn Level
     *
     * @param msg   The obj to be logged
     *
     */
    public void warn(Object msg) {
        myLogger.log(FQCN, Level.WARN, msg, null);
    }

    /**
     * Printing Method on Warn Level including stack trace of
     *  Throwable t
     *
     * @param msg   The obj to be logged
     * @param t     Throwable
     */
    public void warn(Object msg, Throwable t) {
        myLogger.log(FQCN, Level.WARN, msg, t);
    }

    /**
     * Printing Method on Warn Level
     *
     * @param msg   The obj to be logged
     *
     */
    public void warning(Object msg) {
        myLogger.log(FQCN, Level.WARN, msg, null);
    }

    /**
     * Printing Method on Warn Level including stack trace of
     *  Throwable t
     *
     * @param msg   The obj to be logged
     * @param t     Throwable
     */
    public void warning(Object msg, Throwable t) {
        myLogger.log(FQCN, Level.WARN, msg, t);
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Factory method to create the TBitsLogger objects.
     *
     *
     * @param aPackageName Name of the package.
     */
    public static TBitsLogger getLogger(String aPackageName) {
        return new TBitsLogger(aPackageName);
    }

    /**
     * A Constructor which returns a Logger object for the package specified.
     * This Logger object can be retrieved by name.All the log statements
     * under the package will get logged into the appenders of
     * this object.
     *
     * @param aLoggerName    Name of the Logger
     * @param aPackageName   Name of the pakage
     *
     */
    public static TBitsLogger getLogger(String aLoggerName, String aPackageName) {
        return new TBitsLogger(aLoggerName, aPackageName);
    }

    /**
     * This method returns the root Logger.This object is at the top of the
     * Logger hierarchy and all the log statements which have a level higher
     * than the threshold level specified in the config file will get logged
     * into the appenders of this object.
     */
    public static TBitsLogger getRootLogger() {
        return new TBitsLogger();
    }

    /**
     * This method gets the exception and prints the stack trace
     *
     * @param  aThrowable throwable object for which stack trace to be printed
     * @return returns the stacktrace as a string
     */
    public static String getStackTrace(Throwable aThrowable) {
        StringWriter sw = new StringWriter();
        PrintWriter  pw = new PrintWriter(sw);

        pw.print("\n");
        aThrowable.printStackTrace(pw);

        String ex    = sw.toString();
        // nitiraj : removed this as it is concealing the actual source line of exception.
//        int    index = ex.indexOf("at org.apache.catalina.core");
//
//        if (index > 0) {
//
//            // truncate that part of the stack trace as it is not needed.
//            ex = ex.substring(0, index).trim();
//        }

        return ex;
    }

    /*
     * Method to determine if the threshold level of the logger is higher
     * than or equal to DEBUG.
     */
    public boolean isDebugEnabled() {
        return myLogger.isDebugEnabled();
    }

    /*
     * Method to determine if the threshold level of the logger is higher
     * than or equal to INFO.
     */
    public boolean isInfoEnabled() {
        return myLogger.isInfoEnabled();
    }
}
