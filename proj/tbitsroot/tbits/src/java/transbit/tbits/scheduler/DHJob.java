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
 * DHJob.java
 *
 * $Header:
 */
package transbit.tbits.scheduler;

//~--- non-JDK imports --------------------------------------------------------

//Quartz Imports.
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

//TBits Imports.
import transbit.tbits.common.Configuration;
import transbit.tbits.common.Mail;
import transbit.tbits.common.PropertiesEnum;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.StreamGobbler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.ParameterType;

//Static Imports
import static transbit.tbits.Helper.TBitsConstants.PKG_SCHEDULER;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.sql.SQLException;
import java.util.Date;
import java.util.Hashtable;

//~--- classes ----------------------------------------------------------------

public class DHJob implements ITBitsJob, PropertiesEnum {

    // Application logger.
    public static final TBitsLogger LOG          = TBitsLogger.getLogger(PKG_SCHEDULER);
    public static final String      DH_CRON_FILE = "etc/dh-cron";

    // Parameters used while forming mail report.
    public static String ourSubject     = "";
    public static String ourToAddress   = "";
    public static String ourFromAddress = "";
    public static final String CMD_DISPLAY_NAME = "DH Job";

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
        LOG.info("Running DH Cron at " + Timestamp.toCustomFormat(new Date(), "yyyy-MM-dd HH:mm:ss.sss"));

        try {
            File file = Configuration.findPath(DH_CRON_FILE);

            if (file == null) {
                throw new Exception(DH_CRON_FILE + " not found");
            }

            LOG.info("DH Cron File location: " + file.toString());

            Runtime        runtime = Runtime.getRuntime();
            Process        process = runtime.exec(file.toString());
            InputStream    is      = process.getInputStream();
            InputStream    es      = process.getErrorStream();
            StreamGobbler  sg      = new StreamGobbler(es);
            BufferedReader br      = new BufferedReader(new InputStreamReader(is));
            StringBuffer   output  = new StringBuffer();
            int            i       = 0;

            while ((i = br.read()) != -1) {
                output.append((char) i);
            }

            String error    = sg.getMessage();
            int    exitCode = process.waitFor();

            LOG.info("Output of dh-cron: " + output.toString());
            LOG.info("Error from dh-cron: " + error.toString());
            LOG.info("Exit code of dh-cron: " + exitCode);
            ourSubject = ourSubject + "Script logging Data(HYD) Jobs";

            String content = "ExitCode: " + exitCode + "\n\nOutput: " + output + "\n\nError: " + error;

            if ((error != null) && (error.trim().equals("") == false)) {
                Mail.send(ourToAddress, ourFromAddress, ourSubject, content);
            }
        } catch (Exception e) {
            throw new JobExecutionException(e.toString());
        }

        return;
    }
    
    public Hashtable<String, JobParameter> getParameters() throws SQLException{
		Hashtable<String, JobParameter> params = new Hashtable<String, JobParameter>();
		JobParameter param;
		
		return params;
	}
    
    public String getDisplayName(){
    	return CMD_DISPLAY_NAME;
    }
    
    public boolean validateParams(Hashtable<String,String> params) 
    	throws IllegalArgumentException{
    	return true;
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        (new DHJob()).execute(null);
    }
}
