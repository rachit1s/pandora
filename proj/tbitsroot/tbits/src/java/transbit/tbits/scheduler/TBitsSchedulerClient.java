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
 * TBitsSchedulerClient.java
 *
 * $Header:
 */
package transbit.tbits.scheduler;

//~--- non-JDK imports --------------------------------------------------------

//Quartz Imports.
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

//TBits Imports.
import transbit.tbits.common.Configuration;
import transbit.tbits.common.TBitsLogger;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_SCHEDULER;

//~--- JDK imports ------------------------------------------------------------

//Java Imports
import java.io.File;
import java.io.FileInputStream;

import java.util.Properties;

//~--- classes ----------------------------------------------------------------

/**
 *
 *
 * @author Vaibhav
 * @version $Id: $
 * 
 */
@Deprecated
public class TBitsSchedulerClient {

    // Application Logger.
    public static final TBitsLogger LOG                     = TBitsLogger.getLogger(PKG_SCHEDULER);
    private static final String     QUARTZ_CLIENT_PROP_FILE = "etc/tbits-quartz.client";

    //~--- fields -------------------------------------------------------------

    private Properties myProperties;
    private String     myRequest;
    private Scheduler  myScheduler;

    //~--- constructors -------------------------------------------------------

        public TBitsSchedulerClient() 
        {
            
        }
    /**
     * Only Constructor of this class.
     *
     */
    public int execute(String arg) {

        /*
         * The only argument passed should neither be null nor be empty.
         */
        if ((arg == null) || (arg.trim().equals("") == true)) {
            System.err.println(getUsage());
            return 1;
        }

        // Convert the option to lowercase.
        arg = arg.toLowerCase();

        /*
         * The only argument passed must be one from the following list.
         *      - ping
         *      - pause
         *      - resume
         *      - stop
         */
        if ((arg.equals("ping") == false) && (arg.equals("pause") == false) && (arg.equals("resume") == false) && (arg.equals("stop") == false)) {
            System.err.println(getUsage());
            return 1;
        }

        myRequest = arg;

        if (readProperties() == false) {
            return 1;
        }

        try {
            getScheduler();
        } catch (SchedulerException se) {
            LOG.warn("Scheduler is not running.");
            System.out.println("[TBits Job Scheduler] Scheduler is not running.");
            return 1;
        }

        if (myRequest.equalsIgnoreCase("ping") == true) {
            LOG.info("The scheduler is alive and running.");
            System.out.println("[TBits Job Scheduler] " + "The scheduler is alive and running.");
            return 0;
        }

        try {
            boolean flag = serviceRequest();

            if (flag == false) {
                return 1;
            } else {
                return 0;
            }
        } catch (SchedulerException se) {
            LOG.severe("An exception occurred while servicing the request:" + myRequest + "\n" + "",(se));
            se.printStackTrace();
            return 1;
        }
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This is the main method that starts the proceeding of this class.
     *
     * @param args
     */
    public static void main(String[] args) {

        // We expect only one argument on the command line.
        if (args.length != 1) {
            System.err.println(getUsage());
            return;
            //System.exit(1);
        }

      TBitsSchedulerClient schedulerClient =  new TBitsSchedulerClient();
      int retValue = schedulerClient.execute(args[0]);
      //System.exit(retValue);
    }

    /**
     * This method reads the client properties of the Quartz Scheduler.
     *
     * @return True  if the process is successful.
     *         False Otherwise.
     */
    private boolean readProperties() {
        try {
            File file = Configuration.findPath(QUARTZ_CLIENT_PROP_FILE);

            if (file == null) {
                LOG.severe("Could not find the Quartz's RMIScheduler Server" + " properties file.");
                System.out.println("[TBits Job Scheduler] Could not find the Quartz's " + "RMIScheduler Server properties file.");

                return false;
            }

            FileInputStream fis = new FileInputStream(file);

            myProperties = new Properties();
            myProperties.load(fis);
        } catch (Exception e) {
            LOG.warn("Exception while reading properties:\n" + "",(e));

            return false;
        }

        return true;
    }

    /**
     * This method services the request
     *
     * @return True  If the request is services successfully.
     *         False Otherwise.
     * @throws SchedulerException
     */
    private boolean serviceRequest() throws SchedulerException {

        /*
         * Check if this is a request to pause the scheduler.
         */
        if (myRequest.equals("pause") == true) {
            if (myScheduler.isInStandbyMode() == true) {
                LOG.warn("The Scheduler is already in standby state!!!");
                System.out.println("[TBits Job Scheduler] The Scheduler is already in paused " + "state!!!");

                return false;
            } else {
                myScheduler.standby();
                LOG.info("The Scheduler is made standby successfully.");
                System.out.println("[TBits Job Scheduler] The Scheduler is paused successfully.");
            }
        }

        /*
         * Check if this is a request to resume the scheduler.
         */
        else if (myRequest.equals("resume") == true) {
            if (myScheduler.isInStandbyMode() == false) {
                LOG.warn("The Scheduler was not standby!!!");
                System.out.println("[TBits Job Scheduler] The Scheduler wasnot standby!!!");

                return false;
            } else {
                myScheduler.start();
                return true;
            }
        }

        /*
         * Check if this is a request to stop the scheduler.
         */
        else if (myRequest.equals("stop") == true) {
            if (myScheduler.isShutdown() == true) {
                LOG.warn("The Scheduler is stopped already!!!");
                System.out.println("[TBits Job Scheduler] The Scheduler is stopped already!!!");

                return false;
            } else {
                myScheduler.shutdown();
                LOG.info("The Scheduler is stopped successfully.");
                System.out.println("[TBits Job Scheduler] The Scheduler is stopped " + "successfully!!!");
            }
        }

        return true;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method
     *  1. Creates an object of StandardSchedulerFactory .
     *  2. Initializes it with the properties object prepared from our file.
     *  3. Obtains the scheduler object from the factory.
     *  4. Starts the scheduler.
     *
     * @throws SchedulerException
     */
    private void getScheduler() throws SchedulerException {

        // Create an instance of Scheduler Factory.
        StdSchedulerFactory factory = new StdSchedulerFactory();

        // Initialize the factory with our properties object.
        factory.initialize(myProperties);

        // Obtain the scheduler object.
        myScheduler = factory.getScheduler();
    }

    /**
     *
     */
    public static String getUsage() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("\nUsage:").append("\n  TBitsSchedulerClient [options]").append("\n    ").append("\n  options:").append("\n    ping     - Check if the scheduler is running.").append(
            "\n    pause    - Pause the scheduler.").append("\n    resume   - Resumes the scheduler if paused.").append("\n    stop     - Stops the scheduler.").append("");

        return buffer.toString();
    }
}
