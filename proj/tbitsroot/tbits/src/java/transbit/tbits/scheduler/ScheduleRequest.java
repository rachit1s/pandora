package transbit.tbits.scheduler;

/**
 * This class is used as to add request as per scheduled by the quartz scheduler
 */

import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.apache.batik.dom.util.HashTable;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AddRequest;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.CalenderUtils;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;

/*
 * Adds a job with the supplied params.
 * It requires one more param with name duedate_lag so that it can slide date with that time.
 */
public class ScheduleRequest implements ITBitsJob {
	public static final String CMD_DISPLAY_NAME = "Schedule Request";
	
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_UTIL);
	private static final String DUEDATE_LAG = "duedate_lag";
	private static String[] toBeVarSubstitued = {Field.SUBJECT, Field.DESCRIPTION};
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println("Bootstrapping to attempt to start to load the class.");
		JobDetail myJob = new JobDetail();
		myJob = arg0.getJobDetail();
		JobDataMap jdm = myJob.getJobDataMap();
		Hashtable<String, String> paramTable = new Hashtable<String, String>();

		for (String myKey : jdm.getKeys()) {
			paramTable.put(myKey, jdm.getString(myKey));
		}
		System.out.println("Executing with : " + paramTable);
		try
		{
			execute(paramTable);
		}
		catch(Exception e)
		{
			LOG.error("Unable to add request ", e);
		}
	}
	/**
	 * Returns the variables to be substituted with their values for the specified calcnder.
	 * The various variables are: date, month, year, quarter
	 */
	public static Map<String, String> getVars()
	{
		Calendar c = Calendar.getInstance();
		return getVars(c);
	}
	public static Map<String, String> getVars(Calendar c) {
		Map<String, String> vars = new Hashtable<String, String>();
		vars.put("date", new SimpleDateFormat().format(c.getTime()));
		vars.put("month", new SimpleDateFormat("MMMMMMMMMMMMM").format(c.getTime()));
		vars.put("year", new SimpleDateFormat("yyyy").format(c.getTime()));
		
		int month = c.get(Calendar.MONTH) + 1;
		int quarter = month / 3;
		vars.put("quarter", quarter + "");
		return vars;
	}
	/**
	 * Substitute each $key with its value in input.
	 * @return The string after substitution
	 */
	public static String subsDateVars(String input, Map<String, String> vars)
	{
		for(String s:vars.keySet())
		{
			String pat = "\\$"+s;
			String with = vars.get(s);
			input = input.replaceAll(pat, with);
		}
		return input;
	}
	private static String subsDateVars(String input)
	{
		return subsDateVars(input, getVars());
	}
	
	/**
	 * Copies the current attachment to a temporary directory and returns tab separated file location and name.
	 * @param attachments
	 * @return
	 * @throws TBitsException 
	 * @throws FileNotFoundException 
	 */
	static String translateAndCopyAttachments(String attachment) throws TBitsException, FileNotFoundException
	{
		File attachmentF = new File(attachment);
		if (!attachmentF.exists())
			throw new FileNotFoundException("The attachment '" + attachment
					+ "' not found.");
		String attachmentFileName = attachmentF.getName();
			
		// Deal With attachments
		String ourTmpLocation = APIUtil.getTMPDir();
//			Configuration
//				.findAbsolutePath(PropertiesHandler.getProperty(KEY_TMPDIR));
		File tmpLocFile = new File(ourTmpLocation);
		if (!tmpLocFile.exists()) {
			LOG
					.warn("Temporary directory doesnt exists. Using the system default tmp.");
			tmpLocFile = null;
		}

		int dotIndex = attachmentFileName.lastIndexOf(".");
		String proposedPrefix = null;
		String proposedSuffix = ".tmp";
		if (dotIndex > -1) {
			proposedPrefix = attachmentFileName.substring(0, dotIndex);
			proposedSuffix = attachmentFileName.substring(dotIndex);
		} else {
			proposedPrefix = attachmentFileName;
		}
		if(proposedPrefix.length() < 3)
		{
			proposedPrefix = "file_" + proposedPrefix;
		}

		File fTarget;
		try {
			fTarget = File.createTempFile(proposedPrefix, proposedSuffix,
					tmpLocFile);
			System.out.println("Created the file: " + fTarget.getAbsolutePath());
			//fTarget.deleteOnExit();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new TBitsException(
					"Unable to create file in the attachments folder. Please check if you have permission.",
					e1);
		}

		if (fTarget == null) {
			throw new TBitsException(
					"Unable to create file in the attachments folder. Please check if you have permission.");
		}

		try {
			Utilities.copyFile(attachmentF, fTarget);
			return fTarget.getAbsolutePath() + "\t" + attachmentFileName;
		} catch (Exception e) {
			LOG.severe("Unable to copy the file from '" + attachment + "' to '"
					+ fTarget.getAbsolutePath() + "'", e);
		}
		return null;
	}
	public static void execute(Hashtable<String, String> paramTable)
	{
		System.out.println("Going to add the request..");
		String dueDateLagStr = paramTable.get(DUEDATE_LAG);

		if (dueDateLagStr != null) {
			paramTable.remove(DUEDATE_LAG);
			try {
				int dueDateLag = Integer.parseInt(dueDateLagStr.trim());
				Calendar c = Calendar.getInstance();

				Date dueDate = CalenderUtils.slideDate(c.getTime(), dueDateLag);
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String due_date = df.format(dueDate);
				paramTable.put(Field.DUE_DATE, due_date);
			} catch (NumberFormatException nfe) {
				LOG.warn("Error while parsing due date.");
			}
		}
		
		System.out.println("Finished data substitution.");
		//Substitute
		List<String> toBeSubs = Arrays.asList(toBeVarSubstitued);
		for(String s:toBeSubs)
		{
			String inputStr = paramTable.get(s);
			if(inputStr != null)
			{
				paramTable.put(s, subsDateVars(inputStr));
			}
		}
		
		//Handle Attachment Part
		String attachments = paramTable.get(Field.ATTACHMENTS);
		if(attachments != null)
		{
			StringBuilder sb = new StringBuilder();
			boolean isFirst = true;
			for (String s : attachments.split(",")) {
				String attachmentLine = null;
				try {

					attachmentLine = translateAndCopyAttachments(s);
					if (attachmentLine == null)
						continue;

					if (isFirst)
						isFirst = false;
					else
						sb.append("\n");
					sb.append(attachmentLine);

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (TBitsException e) {
					e.printStackTrace();
				}
			}
			
			if(sb.length() > 0)
				paramTable.put(Field.ATTACHMENTS, sb.toString());
		}
		
		AddRequest addRequest = new AddRequest();
		addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
		
		try {
			Request request = addRequest.addRequest(paramTable);
		} catch (APIException e) {
			LOG.severe("Error While inserting a request from the "
					+ " TBitsScheduler:\nDetails of command:\n"
					+ paramTable.toString().replaceAll(", ", "\n")
					+ e.toString() + "",(e));
		} catch (Exception de) {
			LOG.severe("Error While inserting a request from the "
					+ " TBitsScheduler:\nDetails of command:\n"
					+ paramTable.toString().replaceAll(", ", "\n")
					+ de.toString() + "",(de));
		}
	}

	public static void main(String[] args) {
		Hashtable<String, String> paramTable = new Hashtable<String, String>();
		paramTable.put(Field.BUSINESS_AREA, "tbits");
		paramTable.put(Field.USER, "root");
		paramTable.put(DUEDATE_LAG, ""+ 5);
		paramTable.put(Field.SUBJECT, "The quarter3: $quarter. The Month: $month. The Year: $year. The date: $date");
//		paramTable.put(Field.ATTACHMENTS, "/Users/sandeepgiri/tmp/tbitsfile.txt,/Users/sandeepgiri/tmp/tbitsfile1.txt");
		ScheduleRequest.execute(paramTable);
	}

	public String getDisplayName() {
		return CMD_DISPLAY_NAME;
	}
	
	public Hashtable<String, JobParameter> getParameters() throws SQLException {
		Hashtable<String, JobParameter> params = new Hashtable<String, JobParameter>();
		return params;
	}
	public boolean validateParams(Hashtable<String, String> params)
			throws IllegalArgumentException {
		return true;
	}
}
