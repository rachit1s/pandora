/**
 * 
 */
package transbit.tbits.webapps;

import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import transbit.tbits.Helper.Messages;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.report.TBitsReportEngine;
import transbit.tbits.scheduler.TBitsScheduler;

/**
 * @author Lokesh
 * 
 */
public class ReportCirculation extends HttpServlet {	
	
	private static final long serialVersionUID = 1L;
	
	//Local parameters
	private String jobName;
	private String jobGroup;
	private String jobClassName;
	private String jobDescription;
	private Class<?> jobClass;	
	private String emailBody;
	private String subject;
	/*private String runAsUsersBARoles;
	private String runAsIncludeUsers;
	private String runAsExcludeUsers;*/
	private String runAsUser;
	private String recipientBARoles;
	private String includeRecipients;
	private String excludeRecipients;
	private String reportFileName;
	private String reportName;

	private User user;

	//Report jobs table related constants
	private static final String REPORT_JOBS = "report_jobs";
	private static final String COLUMN_JOB_GROUP = "job_group";
	private static final String COLUMN_JOB_NAME = "job_name";
	private static final String COLUMN_REPORT_NAME = "report_name";
	
	//URL query parameters
	private static final String USERS_LIST = "usersList";
	private static final String BA_ROLE_MAP = "baRoleMap";
	private static final String NEAREST_PATH = "nearestPath";
	private static final String REPORT_PARAM_LIST = "reportParamList";
	private static final String EMAIL_SUBJECT = "emailSubject";
	private static final String CRON_EXPRESSION = "cronExpression";
	private static final String DESCRIPTION = "description";
	private static final String CLASS_NAME = "className";
	private static final String JOB_GROUP = "jobGroup";
	private static final String JOB_NAME = "jobName";
	private static final String EMAIL_BODY = "emailBody";	
	/*private static final String RUN_AS_USERS_BA_ROLES = "runAsUsersBARoles";
	private static final String RUN_AS_INCLUDE_USERS = "runAsIncludeUsers";
	private static final String RUN_AS_EXCLUDE_USERS = "runAsExcludeUsers";*/
	private static final String RUN_AS_USER = "runAsUser";
	private static final String RECIPIENT_BA_ROLES = "recipientsBARoles";	
	private static final String INCLUDE_RECIPIENTS = "includeRecipients";
	private static final String EXCLUDE_RECIPIENTS = "excludeRecipients";
	private static final String REPORT_NAME = "reportName";	
	private static final String REPORT_FILE_NAME = "reportfile";	
	private static final String PARAMETER_VALUES = "parameterValues";
	private static final String PARAMETER_NAMES = "parameterNames";
	
	//Type of actions to take when this servlet is called
	private static final String ACTION_TYPE = "actionType";
	private static final String EDIT = "edit";	
	private static final String SAVE = "save";
	
	private static final String SAVE_TYPE = "saveType";	
	private static final String CREATE = "create";
	private static final String UPDATE = "update";
	
	//Miscellaneous
	private static final String USER_ID = "user_id";
	private static final String TBITS_BASE_URL = "tbits_base_url";

	private static final String USER_LOGIN = "userLogin";
	private static final String EMPTY_STRING = "";
	private static final String EMPTY_JSON_ARRAY = "[]";
	
	private static final String ROOT = "root";
	private static final String DISPLAY_RECIPIENTS = "display_recipients";
	private static final String IS_PER_USER_REPORT = "isPerUserReport";
	
	private static final String REPORT_CIRCULATION_HTML = "web/tbits-report-circulation.htm";
	
	private static Scheduler reportScheduler = TBitsScheduler.getScheduler();
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

	private String isPerUserReportString;

	private boolean isPerUserReport;	
	
	public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException{
		HttpSession aSession = aRequest.getSession(true);
		try {
			handleRequest(aRequest, aResponse);
		} catch (DatabaseException de) {
			aResponse.sendRedirect(WebUtil.getServletPath("/error"));

			return;
		} catch (TBitsException tbe) {        	
			aSession.setAttribute("ExceptionObject", tbe);
			aResponse.sendRedirect(WebUtil.getServletPath("/error"));
			return;
		}
	}
	
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException{
		HttpSession aSession = aRequest.getSession(true);
		try {
            handleRequest(aRequest, aResponse);
        } catch (DatabaseException de) {
            aResponse.sendRedirect(WebUtil.getServletPath("/error"));
            return;
        } catch (TBitsException tbe) {        	
        	aSession.setAttribute("ExceptionObject", tbe);
            aResponse.sendRedirect(WebUtil.getServletPath("/error"));
            return;
        }
	}
	
	public void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, DatabaseException, TBitsException{
		aResponse.setContentType("text/html");
		String actionType = aRequest.getParameter(ACTION_TYPE);
		
		user = WebUtil.validateUser(aRequest);
		if (!RoleUser.isSuperUser(user.getUserId()))
			throw new TBitsException(Messages.getMessage("ADMIN_NO_PERMISSION"));
		
		if ((actionType == null) || actionType.trim().equals(EMPTY_STRING))
			throw new TBitsException("Proper actionType(edit/save) not provided");
		
		if (actionType.equals(EDIT)){
			reportName = aRequest.getParameter(REPORT_NAME);
			if ((reportName == null)||reportName.trim().equals(EMPTY_STRING))
				throw new TBitsException("Please provide a report name whose job is being scheduled");
			
			String reportJob = lookupReportJobsByReportName(reportName);
			if (reportJob.trim().equals(EMPTY_STRING) || (reportJob == null))
				handleNewSchedule(aRequest, aResponse);
			else{
				reportJob = reportJob.trim();
				String[] rpJob = reportJob.split(",");
				jobName = rpJob[1];
				jobGroup = rpJob[2];
				handleEditSchedule(aRequest, aResponse);
			}
		}		
		else if (actionType.equals(SAVE))
			handleSaveSchedule(aRequest, aResponse);
	}
	
	public void handleNewSchedule (HttpServletRequest aRequest,HttpServletResponse aResponse) throws TBitsException {
		try {
			PrintWriter out = aResponse.getWriter();
			reportFileName = aRequest.getParameter(REPORT_FILE_NAME);
			reportFileName = (reportFileName == null)? EMPTY_STRING : reportFileName.trim();
			System.out.println("reportfilename: " + reportFileName + ", " + reportFileName);

			File rFile = new File(Configuration.findPath("/tbitsreports").getAbsolutePath()+ "\\"+reportFileName);
			if ((rFile == null) || (!rFile.exists())){
				out.println("Report file not found: " + reportFileName);
				return;
			}		

			jobName = EMPTY_STRING;
			jobGroup = EMPTY_STRING;

			DTagReplacer hp = new DTagReplacer(REPORT_CIRCULATION_HTML);
			hp.replace(USER_LOGIN, user.getUserLogin());
			hp.replace(REPORT_NAME, reportName);
			hp.replace(REPORT_FILE_NAME, reportFileName);
			hp.replace(JOB_NAME, EMPTY_STRING);
			hp.replace(JOB_GROUP, EMPTY_STRING);
			hp.replace(CLASS_NAME, EMPTY_STRING);
			hp.replace(DESCRIPTION, EMPTY_STRING);
			hp.replace(CRON_EXPRESSION, EMPTY_STRING);
			hp.replace(EMAIL_SUBJECT, EMPTY_STRING);
			hp.replace(EMAIL_BODY, EMPTY_STRING);
			hp.replace(NEAREST_PATH, WebUtil.getNearestPath(aRequest, EMPTY_STRING));
			hp.replace(BA_ROLE_MAP, ReportUtil.getBARoles ());	
			hp.replace(USERS_LIST, ReportUtil.getJSONArrayOfUsers().toString());
			hp.replace(IS_PER_USER_REPORT, " ");
			hp.replace(DISPLAY_RECIPIENTS, "block");
			/*hp.replace(RUN_AS_USERS_BA_ROLES, EMPTY_JSON_ARRAY);
			hp.replace(RUN_AS_EXCLUDE_USERS, EMPTY_JSON_ARRAY);
			hp.replace(RUN_AS_INCLUDE_USERS, EMPTY_JSON_ARRAY);	*/	
			hp.replace(RUN_AS_USER, ROOT);
			hp.replace(RECIPIENT_BA_ROLES, EMPTY_JSON_ARRAY);
			hp.replace(INCLUDE_RECIPIENTS, EMPTY_JSON_ARRAY);
			hp.replace(EXCLUDE_RECIPIENTS, EMPTY_JSON_ARRAY);
			hp.replace(SAVE_TYPE, CREATE);

			TBitsReportEngine tre = TBitsReportEngine.getInstance();
			String reportParams = tre.getReportParameters(reportFileName);
			JSONArray tempJSONArray = new JSONArray();
			for (String param : reportParams.split(",")){
				param = param.trim();
				if (!param.equals(TBITS_BASE_URL) && !param.equals(USER_ID)){
					JSONObject obj = new JSONObject();
					obj.accumulate(PARAMETER_NAMES, param);
					obj.accumulate(PARAMETER_VALUES, EMPTY_STRING);
					tempJSONArray.add(obj);
				}
				else
					continue;				
			}
			hp.replace(REPORT_PARAM_LIST, tempJSONArray.toString());
			
			out.print(hp.parse(0));
			out.close();
			out.flush();
		} catch (FileNotFoundException e) {
			throw new TBitsException(REPORT_CIRCULATION_HTML + " file not Found");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void handleEditSchedule (HttpServletRequest aRequest,HttpServletResponse aResponse) throws TBitsException, DatabaseException {		

		try {
			JobDetail tempJob = null;
			try {
				tempJob = reportScheduler.getJobDetail(jobName, jobGroup);
			} catch (SchedulerException e) {
				e.printStackTrace();
				throw new TBitsException("Error occurred while retrieving the job, " + jobName + ":" + jobGroup);					
			}
			if(tempJob == null) {
				throw new TBitsException(jobName + ":" + jobGroup + " job does not exist");
			}

			DTagReplacer hp = new DTagReplacer(REPORT_CIRCULATION_HTML);
			hp.replace(USER_LOGIN, user.getUserLogin());
			hp.replace(REPORT_NAME, reportName);
			hp.replace(JOB_NAME, tempJob.getName());
			hp.replace(JOB_GROUP, tempJob.getGroup());
			hp.replace(CLASS_NAME, tempJob.getJobClass().getName());
			hp.replace(DESCRIPTION, tempJob.getDescription());

			JobDataMap jdm = tempJob.getJobDataMap();			
			hp.replace(EMAIL_SUBJECT, jdm.getString(EMAIL_BODY));
			hp.replace(EMAIL_BODY, jdm.getString(EMAIL_BODY));
			reportFileName = jdm.getString(REPORT_FILE_NAME);
			hp.replace(REPORT_FILE_NAME, reportFileName);
			boolean isPerUserReport = Boolean.parseBoolean(jdm.getString(IS_PER_USER_REPORT));
			
			//TODO: Remaining parameters
			TBitsReportEngine tre = TBitsReportEngine.getInstance();
			String reportParams = tre.getReportParameters(reportFileName);
			JSONArray tempJSONArray = new JSONArray();
			for (String param : reportParams.split(",")){
				param = param.trim();
				if (!param.equals(TBITS_BASE_URL) && !param.equals(USER_ID) && (!param.equals(""))){
					JSONObject obj = new JSONObject();
					obj.accumulate(PARAMETER_NAMES, param);
					obj.accumulate(PARAMETER_VALUES, jdm.getString(param));
					tempJSONArray.add(obj);
				}
				else
					continue;				
			}
			hp.replace(REPORT_PARAM_LIST, tempJSONArray.toString());

			Trigger[] trigger = reportScheduler.getTriggersOfJob(jobName,jobGroup);

			String cronExpression = "";
			if(trigger.length > 0){
				CronTrigger myTrigger = (CronTrigger) trigger[0];

				//TODO find a way to find Cron expression
				cronExpression = myTrigger.getCronExpression();
			}
			hp.replace(CRON_EXPRESSION, cronExpression);
			hp.replace(NEAREST_PATH, WebUtil.getNearestPath(aRequest,EMPTY_STRING));
			hp.replace(BA_ROLE_MAP, ReportUtil.getBARoles ());	
			hp.replace(USERS_LIST, ReportUtil.getJSONArrayOfUsers().toString());

			//All users
			/*hp.replace(RUN_AS_USERS_BA_ROLES, lookupReportJobData(jobName, jobGroup, RUN_AS_USERS_BA_ROLES));
			hp.replace(RUN_AS_EXCLUDE_USERS, getOptionsList(lookupReportJobData(jobName, jobGroup, RUN_AS_EXCLUDE_USERS)));
			hp.replace(RUN_AS_INCLUDE_USERS, getOptionsList(lookupReportJobData(jobName, jobGroup, RUN_AS_INCLUDE_USERS)));*/

						
			
			if (isPerUserReport){
				hp.replace(IS_PER_USER_REPORT, "checked");
				hp.replace(DISPLAY_RECIPIENTS, "none");
			}
			else{
				hp.replace(IS_PER_USER_REPORT, " ");
				hp.replace(DISPLAY_RECIPIENTS, "block");
			}
			hp.replace(RUN_AS_USER, lookupReportJobData(jobName, jobGroup, RUN_AS_USER));
			hp.replace(RECIPIENT_BA_ROLES, lookupReportJobData(jobName, jobGroup, RECIPIENT_BA_ROLES));
			hp.replace(INCLUDE_RECIPIENTS, getOptionsList(lookupReportJobData(jobName, jobGroup, INCLUDE_RECIPIENTS)));
			hp.replace(EXCLUDE_RECIPIENTS, getOptionsList(lookupReportJobData(jobName, jobGroup, EXCLUDE_RECIPIENTS)));

			hp.replace(SAVE_TYPE, UPDATE);
			PrintWriter out = aResponse.getWriter();
			out.print(hp.parse(0));
			out.close();
			out.flush();
		} catch (FileNotFoundException e) {
			throw new TBitsException(REPORT_CIRCULATION_HTML + " file not Found");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	private String getOptionsList(String users) {
		ListIterator<?> iter = JSONArray.fromObject(users).listIterator();
		StringBuffer optionsBuffer = new StringBuffer();
		String userLogin;
		while (iter.hasNext()){
			userLogin = (String)iter.next();
			optionsBuffer.append("<option value=\"").append(userLogin).append("\">")
			.append(userLogin).append("</option>");					
		}
		return optionsBuffer.toString();
	}

	public void handleSaveSchedule (HttpServletRequest aRequest,HttpServletResponse aResponse) throws TBitsException, DatabaseException {		 
		
		reportName = aRequest.getParameter(REPORT_NAME);
		reportName = (reportName == null)?EMPTY_STRING:reportName;
		if(reportName == null) 
			throw new TBitsException("Report Name is required ");
		
		jobName = aRequest.getParameter(JOB_NAME);
		jobName = (jobName == null)?EMPTY_STRING:jobName;
		if((null == jobName) || (jobName.trim().length() == 0))
			throw new TBitsException("Job Name is required ");
		
		jobGroup = aRequest.getParameter(JOB_GROUP);
		jobGroup = (jobGroup == null)?EMPTY_STRING:jobGroup;
		if((null == jobGroup) || (jobGroup.trim().length() == 0))
			throw new TBitsException("Job Group is required ");
		
		/*if ((tempReportJob == null) || tempReportJob.trim().equals(EMPTY_STRING))
			insertReportJob(reportName, jobName, jobGroup);
		else
			throw new TBitsException("Report job \"" + tempReportJob + "\" already exists.");*/
		
		String saveType = aRequest.getParameter(SAVE_TYPE);
		saveType = (saveType == null)?EMPTY_STRING:saveType.trim();
		
		String tempReportJob = lookupReportJobsByReportName(reportName);
		tempReportJob = (tempReportJob == null)?EMPTY_STRING:tempReportJob.trim();
		
		if (saveType.equals(EMPTY_STRING))
		{
			throw new TBitsException("SaveType is required to create new/update existing job.");
		}
		else if (saveType.equals(UPDATE) && tempReportJob.trim().equals(EMPTY_STRING))
		{
			throw new TBitsException("Invalid report " + reportName + " is being scheduled");
		}
		else if (saveType.equals(CREATE))
		{
			if (tempReportJob.trim().equals(EMPTY_STRING))
				insertReportJob(reportName, jobName, jobGroup);
			else 
				throw new TBitsException("Job with job name " + jobName + " already exists.");
		}
		
		/*String subaction = aRequest.getParameter("subaction");
		if(null == subaction) subaction = EMPTY_STRING;
		 */
		jobClassName = aRequest.getParameter(CLASS_NAME);
		try {
			jobClass = Class.forName(jobClassName);
		} catch (ClassNotFoundException cnfe) {
			throw new TBitsException(jobClassName + " :Class Not Found ",cnfe);
		}
		
		jobDescription = aRequest.getParameter(DESCRIPTION);
		/*String sysPrefix = aRequest.getPathInfo();
		if(null == sysPrefix) sysPrefix = EMPTY_STRING;*/	
				
		subject = aRequest.getParameter(EMAIL_SUBJECT);
		subject = (subject == null)? EMPTY_STRING : subject.trim();	
		
		emailBody = aRequest.getParameter(EMAIL_BODY);
		emailBody = (emailBody == null)? EMPTY_STRING : emailBody.trim();
		
		/*runAsUsersBARoles = aRequest.getParameter(RUN_AS_USERS_BA_ROLES);
		runAsUsersBARoles = (runAsUsersBARoles == null) ? EMPTY_STRING : runAsUsersBARoles.trim();
		
		runAsIncludeUsers = aRequest.getParameter(RUN_AS_INCLUDE_USERS);
		runAsIncludeUsers = (runAsIncludeUsers == null) ? EMPTY_STRING : runAsIncludeUsers.trim();
		
		runAsExcludeUsers = aRequest.getParameter(RUN_AS_EXCLUDE_USERS);
		runAsExcludeUsers = (runAsExcludeUsers == null) ? EMPTY_STRING : runAsExcludeUsers.trim();*/
		
		isPerUserReportString = aRequest.getParameter(IS_PER_USER_REPORT);
		isPerUserReportString = (isPerUserReportString == null)? EMPTY_STRING : isPerUserReportString.trim();
		isPerUserReport = (isPerUserReportString.equals(""))? false : true;
		
		runAsUser = aRequest.getParameter(RUN_AS_USER);
		runAsUser = (runAsUser == null) ? EMPTY_STRING : runAsUser.trim();
		
		recipientBARoles = aRequest.getParameter(RECIPIENT_BA_ROLES);
		recipientBARoles = (recipientBARoles == null) ? EMPTY_STRING : recipientBARoles.trim();
		
		includeRecipients = aRequest.getParameter(INCLUDE_RECIPIENTS);
		includeRecipients = (includeRecipients == null) ? EMPTY_STRING : includeRecipients.trim();
		
		excludeRecipients = aRequest.getParameter(EXCLUDE_RECIPIENTS);
		excludeRecipients = (excludeRecipients == null) ? EMPTY_STRING : excludeRecipients.trim();
		
		JobDataMap jdm = new JobDataMap();
		jdm.put(EMAIL_SUBJECT, subject);
		jdm.put(EMAIL_BODY, emailBody);
		jdm.put(IS_PER_USER_REPORT, isPerUserReport + "");
				
		//Saving all the following information in report job data map table instead of job data map
		if (saveType.equals("update")){
			/*updateReportJobData(jobName, jobGroup, RUN_AS_USERS_BA_ROLES, runAsUsersBARoles);
			updateReportJobData(jobName, jobGroup, RUN_AS_INCLUDE_USERS, runAsIncludeUsers);
			updateReportJobData(jobName, jobGroup, RUN_AS_EXCLUDE_USERS, runAsExcludeUsers);*/
			updateReportJobData(jobName, jobGroup, RUN_AS_USER, runAsUser);
			updateReportJobData(jobName, jobGroup, RECIPIENT_BA_ROLES, recipientBARoles);
			updateReportJobData(jobName, jobGroup, INCLUDE_RECIPIENTS, includeRecipients);
			updateReportJobData(jobName, jobGroup, EXCLUDE_RECIPIENTS, excludeRecipients);
		}
		else if (saveType.equals("create")){
			/*insertReportJobData(jobName, jobGroup, RUN_AS_USERS_BA_ROLES, runAsUsersBARoles);
			insertReportJobData(jobName, jobGroup, RUN_AS_INCLUDE_USERS, runAsIncludeUsers);
			insertReportJobData(jobName, jobGroup, RUN_AS_EXCLUDE_USERS, runAsExcludeUsers);*/
			insertReportJobData(jobName, jobGroup, RUN_AS_USER, runAsUser);
			insertReportJobData(jobName, jobGroup, RECIPIENT_BA_ROLES, recipientBARoles);
			insertReportJobData(jobName, jobGroup, INCLUDE_RECIPIENTS, includeRecipients);
			insertReportJobData(jobName, jobGroup, EXCLUDE_RECIPIENTS, excludeRecipients);
		}
		
		/*try {
			if((subaction.equalsIgnoreCase("create")) && (null != myScheduler.getJobDetail(jobName, jobGroup))) {
				throw new TBitsException("Job with given name and group Already exists: " + jobName + ":" + jobGroup);
			}

		} catch (SchedulerException e1) { }*/
		
		//	isRecoverable = aRequest.getParameter("recoverable");
		//	isDurable = aRequest.getParameter("durable");
		String[] parameterNames = aRequest.getParameterValues(PARAMETER_NAMES);
		String[] parameterValues = aRequest.getParameterValues(PARAMETER_VALUES);
		if (parameterNames!=null){
			System.out.println(parameterNames.length);
			System.out.println(parameterValues.length);
			for (int i =0; i < parameterNames.length; i++) {
				parameterNames[i] = parameterNames[i].trim();
				parameterValues[i] = parameterValues[i].trim();
				if (parameterNames[i].equals(EMPTY_STRING) && parameterValues[i].equals(EMPTY_STRING)) {
					continue;
				}
				else{
					//TODO array index out of bounds	
					jdm.put(parameterNames[i], parameterValues[i]);
				}
			}
		}
		
		boolean isRecoverable = false, isDurable = true, isVolatile = false;
		JobDetail mainJob = new JobDetail();
		mainJob.setName(jobName);
		mainJob.setGroup(jobGroup);
		mainJob.setJobClass(jobClass);
		mainJob.setDescription(jobDescription);			
		mainJob.setRequestsRecovery(isRecoverable);
		mainJob.setDurability(isDurable);
		mainJob.setVolatility(isVolatile );
		mainJob.setJobDataMap(jdm);

		try {
			reportScheduler.addJob(mainJob, true);
		} catch (SchedulerException e) {
			e.printStackTrace();
			throw new TBitsException("Job Could not be added",e);
		}
		
		scheduleJob(aRequest, aResponse);
		try {
			PrintWriter out = aResponse.getWriter();
			out.println("Saved schedule successfully. Closing the window....");			
			//aResponse.sendRedirect( WebUtil.getServletPath(aRequest, "/reportscirculation"));
		} catch (IOException e) {
			throw new TBitsException("Unable to redirect to reports page");
		}
	}
	
	public static void insertReportJobData(String jobName, String jobGroup, String key, String value) throws DatabaseException{

		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();			
			PreparedStatement ps = connection.prepareStatement("INSERT INTO REPORT_JOB_DATA_MAP (JOB_NAME, " +
					"JOB_GROUP, ENTRY, VALUE) VALUES ('" + jobName + "', '" + jobGroup + "', '" 
					+ key + "', '" + value + "')");
			ps.execute();
			ps.close();
			ps = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while inserting report data for scheduling.\n");
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				connection = null;
			}            
		}
	}
	
	public static void updateReportJobData(String jobName, String jobGroup, String key, String value) throws DatabaseException{

		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("IF EXISTS(SELECT VALUE FROM REPORT_JOB_DATA_MAP WHERE JOB_NAME='"
					+ jobName + "' AND JOB_GROUP='" + jobGroup + "' AND ENTRY='" + key + "') "
					+ "UPDATE REPORT_JOB_DATA_MAP SET VALUE='" + value + "' WHERE JOB_NAME='"
					+ jobName + "' AND JOB_GROUP='" + jobGroup + "' AND ENTRY='" + key + "'");
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while updating report job data.");
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException sqle) {
				LOG.warning("An Exception has occured while closing a request");
			}
		}
	}
	
	public static String lookupReportJobData(String jobName, String jobGroup, String key) throws DatabaseException{

		String value = "";
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT VALUE FROM REPORT_JOB_DATA_MAP WHERE JOB_NAME='"
					+ jobName + "' AND JOB_GROUP='" + jobGroup + "' AND ENTRY='" + key + "'");
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next()){
				value = rs.getString("VALUE");
			}
			rs.close();
			ps.close();
			ps = null;
			return value;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while inserting report data for scheduling.\n");
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				connection = null;
			}            
		}
	}
	
	public static Hashtable<String, String> lookupReportJobDataByJobName(String jobName) throws DatabaseException{
		
		Hashtable<String, String> jdmHashtable = new Hashtable<String, String>();
		String key  = "";
		String value = "";
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM REPORT_JOB_DATA_MAP WHERE JOB_NAME='"
					+ jobName + "'");
			ResultSet rs = ps.executeQuery();
			if (rs != null) 
				while (rs.next()){
					key = rs.getString("ENTRY");
					value = rs.getString("VALUE");
					jdmHashtable.put(key, value);
				}
			rs.close();
			ps.close();
			ps = null;
			return jdmHashtable;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while inserting report data for scheduling.\n");
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				connection = null;
			}            
		}
	}
	
	public void scheduleJob(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws TBitsException {

		jobName = aRequest.getParameter(JOB_NAME);
		jobGroup = aRequest.getParameter(JOB_GROUP);
		String triggerName = jobName;
		String triggerGroup = jobGroup;
		String cronExpression = aRequest.getParameter(CRON_EXPRESSION);
		if((triggerName.length() == 0) || (triggerGroup.length() == 0))
			throw new TBitsException("TriggerName Cannot be null");

		CronTrigger trigger = null;
		try {
			trigger = new CronTrigger(triggerName, triggerGroup, jobName, jobGroup, cronExpression);
		} catch (ParseException e) {
			throw new TBitsException("Error in parsing Cron Expression: " +
					triggerName + ". Trigger was not Added",e);
		}
		try {
			reportScheduler.unscheduleJob(triggerName,triggerGroup);	//remove the trigger if already present
			reportScheduler.scheduleJob(trigger);
			System.out.println("Finished scheduling the job>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		} catch (SchedulerException e) {
			throw new TBitsException("Error in Scheduling: " +
					triggerName + "Trigger was not Added",e);
		}		
	}
		 
	public void deleteJob(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws TBitsException {

		jobName = aRequest.getParameter("jobName");
		jobGroup = aRequest.getParameter("jobGroup");

		try {
			reportScheduler.deleteJob(jobName, jobGroup);
		} catch (SchedulerException e) {
			throw new TBitsException("Unable to Delete this job with details " +
					jobName + ":" + jobGroup);
		}

		try {
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/adminReports"));
		} catch (IOException e) {
			throw new TBitsException("Unable to Redirect to list jobs page");
		}
	}

	public void pauseJob(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws TBitsException {

		jobName = aRequest.getParameter("jobName");
		jobGroup = aRequest.getParameter("jobGroup");

		try {

			reportScheduler.pauseJob(jobName, jobGroup);

		} catch (SchedulerException e) {
			throw new TBitsException("Unable to pause this job with details " +
					jobName + ":" + jobGroup);
		}

		try {
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/adminReports"));
		} catch (IOException e) {
			throw new TBitsException("Unable to Redirect to list jobs page");
		}
	}

	public void resumeJob(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws TBitsException {

		jobName = aRequest.getParameter("jobName");
		jobGroup = aRequest.getParameter("jobGroup");

		try {

			reportScheduler.resumeJob(jobName, jobGroup);
		} catch (SchedulerException e) {
			throw new TBitsException("Unable to resume this job with details " +
					jobName + ":" + jobGroup);
		}

		try {
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/adminReports"));
		} catch (IOException e) {
			throw new TBitsException("Unable to Redirect to list jobs page");
		}
	}

	public static String lookupReportJobsByReportName(String reportName){
		Connection con = null;
		String reportJob = EMPTY_STRING;
		try {
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM " + REPORT_JOBS + " WHERE " + COLUMN_REPORT_NAME + "='" + reportName + "'");
			ResultSet rs = ps.executeQuery();
			
			if ((rs != null) && (rs.next()))
				reportJob = rs.getString(COLUMN_REPORT_NAME) + "," + rs.getString(COLUMN_JOB_NAME) + "," + rs.getString(COLUMN_JOB_GROUP);		
			
			rs.close();
			ps.close();
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occured while checking for existing report-jobs").append("\n");
			try {
				throw new DatabaseException(message.toString(), sqle);
			} catch (DatabaseException e) {
				LOG.error("Exception while closing the connection:", sqle);
			}
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				con = null;
			}
		} 
		return reportJob;
	}
	
	public static void insertReportJob (String reportName, String jobName, String jobGroup) throws DatabaseException{
		final String STORED_PROC = "stp_report_job_insert";
		Connection con = null;
		try {
			con = DataSourcePool.getConnection();
			CallableStatement cs = con.prepareCall(STORED_PROC + " ?, ?, ?");
			cs.setString(1, reportName);
			cs.setString(2, jobName);
			cs.setString(3, jobGroup);
			cs.execute();
			cs.close();
			cs = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			LOG.error("Exception while closing the connection:", sqle);	
			message.append("An exception occured while inserting report-job for job: ").append(jobName).append("\n");
			throw new DatabaseException(message.toString(), sqle);						
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				con = null;
			}
		} 
	}
	
	public static void deleteReportJob(String reportName){
		Connection con = null;
		try {
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM " + REPORT_JOBS + " WHERE " + COLUMN_REPORT_NAME + "='" + reportName + "'");
			ps.execute();
			ps.close();
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occured while deleting report-job for report: ").append(reportName).append("\n");
			try {
				throw new DatabaseException(message.toString(), sqle);
			} catch (DatabaseException e) {
				LOG.error("Exception while closing the connection:", sqle);
			}
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}
				con = null;
			}
		}
	}
	
	public static void main (String[] args) throws DatabaseException{
		//ReportCirculation.insertReportJob(3, "report1", "jobName1");
		//System.out.println(ReportCirculation.lookupReportJobsByReportName("report1"));
		//ReportCirculation.deleteReportJob("myloggedrequests");
		//insertReportJobData("testJobName", "testJobGroup", "testKey", "testValue");
		System.out.println(lookupReportJobData("testJobName", "testJobGroup", "testKey"));
		System.out.println("Done......");
	}
}
	
	

	

