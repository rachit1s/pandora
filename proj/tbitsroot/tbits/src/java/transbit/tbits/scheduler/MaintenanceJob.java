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
 * MaintenanceJob.java
 *
 * $Header:
 */
package transbit.tbits.scheduler;

//~--- non-JDK imports --------------------------------------------------------

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.Mail;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.StreamGobbler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.JobUtil;
import transbit.tbits.scheduler.ui.ParameterType;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_SCHEDULER;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_APP_NAME;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_APP_VERSION;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_LOGGER_NOTIFY_FROM;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_LOGGER_NOTIFY_TO;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.InputStream;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

//~--- classes ----------------------------------------------------------------

/**
 * This job runs the classes that perform the following jobs:
 *      <ul>
 *          <li>Sync'ing users from Active Directory to TBits User DB.</li>
 *          <li>Sync'ing users across TBits instances.</li>
 *          <li>Incrementally indexing TBits requests/updates.</li>
 *          <li>Optimizing the TBits's Lucene index periodically.</li>
 *          <li>Sync'ing the transferred and related request records.</li>
 *      </ul>
 *
 * @author  Vaibhav.
 * @version $Id: $
 */
public class MaintenanceJob implements ITBitsJob {

    // Application logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SCHEDULER);

    // Property keys to be read from JobDataMap.
    public static final String FQCN          = "FQCN";
    public static final String CMD_ARG_NAMES = "CmdLineArgumentNameCSV";
    public static final String CMD_DISPLAY_NAME = "Maintenance Job";

    // Class that runs the java classes.
    public static final String SCRIPT_PATH = "etc/run-class";

    // Parameters used while forming mail report.
    public static String ourSubject     = "";
    public static String ourToAddress   = "";
    public static String ourFromAddress = "";

    //~--- static initializers ------------------------------------------------

    static {
        try {
            ourSubject     = "[" + PropertiesHandler.getProperty(KEY_APP_NAME) + " " + PropertiesHandler.getProperty(KEY_APP_VERSION) + "] ";
            ourToAddress   = PropertiesHandler.getProperty(KEY_LOGGER_NOTIFY_TO);
            ourFromAddress = PropertiesHandler.getProperty(KEY_LOGGER_NOTIFY_FROM);
        } catch (Exception e) {
            LOG.severe("Exception while reading notification details: " + "",(e));
        }
    }

    //~--- methods ------------------------------------------------------------

    /**
     * The method that gets executed when the job is triggered.
     *
     * @param arg0  JobExecutionContext that holds the JobDetail and Trigger
     *              information.
     *
     * @exception   JobExecutionException
     */
    public void execute(JobExecutionContext arg0) throws JobExecutionException {

        // Get the JobDetail object.
        JobDetail jd = arg0.getJobDetail();

        // Read the properties of the Job from the JobDetail object.
        String     jobName  = jd.getName();
        String     jobGroup = jd.getGroup();
        JobDataMap jdm      = jd.getJobDataMap();

        if (jobName.equalsIgnoreCase("addholidaylist") == true) {
            Mail.send("tbits-requests@transbittech.com", "tbits-dev@transbittech.com", "Add company's holiday list for the next year to TBits.", "/severity:high\n\n");

            return;
        }

        /*
         * Get the class to be executed. Check if the class exists in the
         * given classpath.
         */
        String fqcn = jdm.getString(FQCN);

        if ((fqcn == null) || fqcn.trim().equals("")) {
            throw new JobExecutionException("Invalid class name.");
        }

        try {
            Class.forName(fqcn);
        } catch (ClassNotFoundException cnfe) {
            throw new JobExecutionException("Invalid class name: " + fqcn);
        }

        /*
         * Get the name of command line arguments.
         * Then, get the values of these arguments from the JDM and pass them
         * to the executeScript method.
         */
        String cmdArgNameList = jdm.getString(CMD_ARG_NAMES);

        if ((cmdArgNameList == null) || cmdArgNameList.trim().equals("")) {
            cmdArgNameList = "";
        }

        ArrayList<String> argNameList = Utilities.toArrayList(cmdArgNameList);

        /*
         * Form the command line argument list. The first one is the fully
         * qualified class name of the class to be run followed by the
         * command line arguments specified in the order.
         */
        ArrayList<String> list = new ArrayList<String>();

        list.add(fqcn);

        for (String arg : argNameList) {
            String argValue = jdm.getString(arg);

            if (argValue == null) {
                argValue = "";
            } else {
                argValue = argValue.trim();
            }

            list.add(argValue);
        }

        /*
         * Execute the script in a different process.
         */
        StringBuffer error  = new StringBuffer();
        
        String       output = executeMyScript(list, error);
        String       data   = "Details:" + "\n\tJob Name:                " + jobName + "\n\tJob Group:               " + jobGroup + "\n\tClassName and Arguments: " + list.toString() + "\n\nOutput:\n"
                              + output.toString() + "\n\nError:\n" + error.toString();

        LOG.info(data);

        if (error.toString().trim().equals("") == false) {
            ourSubject = ourSubject + "Error while running a maintenance job";
            Mail.sendWithHtml(ourToAddress, ourFromAddress, ourSubject, data);
        }

        return;
    }
    
    private String executeMyScript(ArrayList<String> list, StringBuffer error)
    {
        LOG.info("Trying to execute following command line: " + list);
        if(list.size() < 1)
        {
            LOG.fatal("List should have at least length one.");
            return "";
        }
        String className = list.remove(0);
        
        String[] args = new String[list.size()];
        list.toArray(args);
        try {
            executeClass(className, args);
        } catch (InvocationTargetException ex) {
            LOG.error("",(ex));
        } catch (ClassNotFoundException ex) {
            LOG.error("",(ex));
        } catch (NoSuchMethodException ex) {
            LOG.error("",(ex));
        } catch (IllegalAccessException ex) {
            LOG.error("",(ex));
        }
        
        return "";
    }
    
     public static void executeClass(String className, String []arguments) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class myClass = Class.forName(className);
        Method m1 = myClass.getMethod("main", String[].class);
        Object oArgs = arguments;
        m1.invoke(myClass, oArgs);
    }
     
    /**
     * This method checks if the run-class script is present and invokes with
     * the command-array list passed.
     *
     * @param list Java class and command-line arguments if any.
     * @return  Output of the class.
     * @throws JobExecutionException If the script is not found.
     */
    private String executeScript(ArrayList<String> list, StringBuffer error) throws JobExecutionException {
        StringBuffer output = new StringBuffer();

        try {

            // Get the complete path of run-class script.
            File file = Configuration.findPath(SCRIPT_PATH);

            if (file == null) {
                throw new JobExecutionException("Script not found: " + SCRIPT_PATH);
            }

            // Add it at the start of the list.
            list.add(0, file.toString());

            // Get the array out of the list.
            String[] cmdArray = new String[list.size()];

            list.toArray(cmdArray);

            // Print it to the log.
            LOG.info(list.toString());

            // Get the runtime and execute this script in a different process.
            Runtime       runtime = Runtime.getRuntime();
            Process       proc    = runtime.exec(cmdArray);
            InputStream   br      = proc.getInputStream();
            StreamGobbler sg      = new StreamGobbler(proc.getErrorStream());
            int           i       = 0;

            while ((i = br.read()) != -1) {
                output.append((char) i);
            }

            br.close();

            // Wait for the process to stop.
            int exitStatus = proc.waitFor();

            // Append the exit status of the script.
            output.append("\nExit Status: ").append(exitStatus);
            error.append(sg.getMessage());
        } catch (Exception e) {
            output.append("Exception while executing a perl script: ").append(TBitsLogger.getStackTrace(e));
        }

        return output.toString();
    }
    
    public Hashtable<String, JobParameter> getParameters() throws SQLException{
		Hashtable<String, JobParameter> params = new Hashtable<String, JobParameter>();
		JobParameter param;
		
		param = new JobParameter();
		param.setName(FQCN);
		param.setMandatory(true);
		param.setType(ParameterType.Text);
		params.put(FQCN, param);
		
		param = new JobParameter();
		param.setName(CMD_ARG_NAMES);
		param.setType(ParameterType.Text);
		params.put(CMD_ARG_NAMES, param);
		
		return params;
	}
    
    public String getDisplayName(){
    	return CMD_DISPLAY_NAME;
    }
    
    public boolean validateParams(Hashtable<String,String> params) 
    	throws IllegalArgumentException{
    	
    	if( null == params.get(FQCN) || "".equals(params.get(FQCN).trim()) )
    		throw new IllegalArgumentException( "Illegal Argument for " + FQCN + " field." ) ;
    	    	
    	return true;
    }

    /**
     * Main method for testing.
     *
     * @param arg
     * @throws Exception
     */
    public static void main(String arg[]) throws Exception {}
}
