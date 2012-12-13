package transbit.tbits.scheduler;

/*
 * Author: @Abhishek Agarwal
 * TODO List
 * Triggers remaining
 * Proper Exception Handling
 * How to get current Scheduler : done
 * Back End complete : Now its the interface part
 * List jobs,delete jobs is working
 * 
 */


import static transbit.tbits.Helper.TBitsConstants.PKG_SCHEDULER;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import transbit.tbits.Helper.Messages;
import transbit.tbits.admin.common.AdminProxyServlet;
import transbit.tbits.admin.common.MenuItem;
import transbit.tbits.admin.common.NavMenu;
import transbit.tbits.admin.common.URLRegistry;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.PreDefinedJobFactory;
import transbit.tbits.webapps.WebUtil;



public class AllJobActions extends HttpServlet {
	
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SCHEDULER);
//	private static final String EDIT_JOB_HTML = "web/tbits-admin-editJob.html";
//	private static final String CREATE_JOB_HTML = "web/tbits-admin-createJob.html";
	private static final String LIST_JOB_HTML = "web/tbits-admin-listJobs.html";
	private static final String EDIT = "edit";
	private String jobName;
	private String jobGroup;
	private JobDataMap jdm;
	private String jobClassName;
	private String jobDescription;
	private boolean isRecoverable = false;
	private boolean isDurable = true;
	private boolean isVolatile = false;
	private Class jobClass;
	private String[] parameterNames;
	private String[] parameterValues;
	private Hashtable<String,String> params ;
	private static Scheduler myScheduler = TBitsScheduler.getScheduler();
//	private static PreDefinedJobFactory jobFactory = PreDefinedJobFactory.getInstance();
	
	// store cron expressoin exception here
//	private String myExceptionList = new String();
	//Variables Related to rendering of HTML page
	
	private String title = "";
	private String userLogin = "";
	private String nearestPath = "";
	private String cssFile = "";
	private String target = "";
	
	static
	{
        String url = "listjobs";
    	String completeURL = url + ".admin";
    	
        //Create Mapping
		URLRegistry.getInstance().addMapping(AdminProxyServlet.class, url, AllJobActions.class);
		
		//Create Menu
		NavMenu nav = NavMenu.getInstance();
		nav.AppMenu.add(new MenuItem("List Jobs", completeURL, "Manage the scheduled jobs."));
	}

	public void getJobClasses(HttpServletRequest aRequest, HttpServletResponse aResponse){
		Hashtable<JSONArray, ITBitsJob> jobs = PreDefinedJobFactory.getInstance().getPreDefinedJobs();
		List<JSONArray> keys = Collections.list((Enumeration<JSONArray>)jobs.keys());
		
		JSONArray classes = new JSONArray();
		
		for(JSONArray key:keys){
			classes.add(key);
		}
		
		PrintWriter out;
		try {
			out = aResponse.getWriter();
			out.print(classes);
			out.close();
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getJobParams(HttpServletRequest aRequest, HttpServletResponse aResponse){
		String jobClassStr = aRequest.getParameter("job-class");
		String jobParamsStr = aRequest.getParameter("job-params");
		
		JSONArray jobClass = new JSONArray();
		jobClass = JSONArray.fromObject(jobClassStr);
		//jobClass.fromJson(jobClassStr, String.class);
		Hashtable<JSONArray, ITBitsJob> jobs = PreDefinedJobFactory.getInstance().getPreDefinedJobs();
		ITBitsJob job = jobs.get(jobClass);
		
		JSONArray parameters = new JSONArray();
		Hashtable<String, JobParameter> params = new Hashtable<String, JobParameter>();
		try{
			params = job.getParameters();
		}catch(SQLException se)
		{
			se.printStackTrace() ;
		}
		
		JSONArray jobParams = new JSONArray();
		String mode="";
		if(jobParamsStr != null && jobParamsStr != ""){
			jobParams = JSONArray.fromObject(jobParamsStr);
			mode="specific";
		}
		
		Enumeration<String> paramNames= params.keys();
		JobParameter tempParam;
		while(paramNames.hasMoreElements()){
			tempParam = params.get(paramNames.nextElement());
			if(mode.compareTo("specific") == 0)
				if(!jobParams.contains(tempParam.getName()))
					continue;
			parameters.add(tempParam);
		}
		
		PrintWriter out;
		try {
			out = aResponse.getWriter();
			out.print(parameters);
			out.close();
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void doPost(HttpServletRequest aRequest,HttpServletResponse aResponse) throws ServletException,IOException {

		HttpSession session = aRequest.getSession(true);
		
		try {
			handleGetRequest(aRequest, aResponse);
			
		}
		catch(TBitsException e) {
			session.setAttribute("ExceptionObject",e);
			LOG.info("",(e));
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, e.getDescription()));
		}
		catch(SchedulerException se) {
			session.setAttribute("ExceptionObject",se);
			LOG.info("",(se));
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, se.getMessage()));
		}
		catch(IOException e) {
			session.setAttribute("ExceptionObject",e);
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, e.getMessage()));
		}
		catch(DatabaseException d) {
			session.setAttribute("ExceptionObject",d);
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, d.getMessage()));
		}		

	}
	
	public void doGet(HttpServletRequest aRequest,HttpServletResponse aResponse) throws ServletException, IOException {
		doPost(aRequest,aResponse);
	}
	
	public void handleGetRequest(HttpServletRequest aRequest,HttpServletResponse aResponse)throws TBitsException,IOException, SchedulerException, DatabaseException, ServletException {
		
		User user = WebUtil.validateUser(aRequest);
		int userId     = user.getUserId();
		if(!RoleUser.isSuperUser(userId))
            throw new TBitsException(Messages.getMessage("ADMIN_NO_PERMISSION"));
		
		    WebConfig userConfig = user.getWebConfigObject();
        
//        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, 4);
//        BusinessArea              ba         = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);

//        if (ba == null) {
//            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
//        }
	
//        int       systemId  = ba.getSystemId();
//        sysPrefix = ba.getSystemPrefix();
//        SysConfig sc        = ba.getSysConfigObject();
        
        userLogin = user.getUserLogin();
        cssFile = WebUtil.getCSSFile("tbits.css", "", false);
        nearestPath = WebUtil.getNearestPath(aRequest, "");
//        title = "TBits Admin: " + ba.getDisplayName() + " Jobs Scheduler";
//        sys_ids = AdminUtil.getSysIdList(systemId, userId);
        
		target = WebUtil.getServletPath(aRequest, aRequest.getServletPath());
		
		String action = aRequest.getParameter("action");
		if(action == null) action = "";
		
		if(action.equals("save-job")){
			System.out.println("Savejob : " + action);
			saveJob(aRequest, aResponse);
			return;
		}
		
		if(action.equals("create-job")) {
			editJob(aRequest, aResponse);
			return;
		}
	
		if(action.equals("delete-job")){
			deleteJob(aRequest,aResponse);
			return;
			}
		
		if(action.equals("pause-job")) {
			pauseJob(aRequest,aResponse);
			return;
		}
		
		if(action.equals("resume-job")) {
			resumeJob(aRequest, aResponse);
		}
		
		if(action.equals("execute-job")) {
			executeJob(aRequest,aResponse);
			return;
		}
		if(action.equals("testcron"))
		{
			PrintWriter out = aResponse.getWriter();
			String expression = aRequest.getParameter("expression");
			try {
				ArrayList<Date> execs = CronExpressionTester.getNextExecutions(new CronExpression(expression), 10);
				if(execs.isEmpty()){
					out.print("There are no next execution times");
					return;
				}
				out.print("The following are the next 10 execution times: <br/> ");
				for(Date d:execs)
				{
					out.print(d.toString() + "<br/>");
				}
			} catch (ParseException e) {
				out.print("Error while executing cron. <br> " + e.getMessage());
			}
			catch(UnsupportedOperationException usoe)
			{
				out.print("Error while executing cron. <br> " + usoe.getMessage());
			}
			return;
		}
		
		if(action.equals("get-classes")) {
			getJobClasses(aRequest, aResponse);
			return;
		}
		
		if(action.equals("get-job-params")) {
			getJobParams(aRequest, aResponse);
			return;
		}
		
        listJobs(aRequest, aResponse);
		return;
	}

	


	public void deleteJob(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws TBitsException {
		
		jobName = aRequest.getParameter("jobName");
		jobGroup = aRequest.getParameter("jobGroup");
				
		try {
			myScheduler.deleteJob(jobName, jobGroup);
		} catch (SchedulerException e) {
			throw new TBitsException("Unable to Delete this job with details " +
					jobName + ":" + jobGroup);
		}
		
		try {
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/all-job-actions/"));
		} catch (IOException e) {
			throw new TBitsException("Unable to Redirect to list jobs page");
		}
	}

	public void pauseJob(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws TBitsException {
		
		jobName = aRequest.getParameter("jobName");
		jobGroup = aRequest.getParameter("jobGroup");
				
		try {
			
			myScheduler.pauseJob(jobName, jobGroup);
		} catch (SchedulerException e) {
			throw new TBitsException("Unable to pause this job with details " +
					jobName + ":" + jobGroup);
		}
		
		try {
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/all-job-actions/"));
		} catch (IOException e) {
			throw new TBitsException("Unable to Redirect to list jobs page");
		}
	}
	
	public void resumeJob(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws TBitsException {
		
		jobName = aRequest.getParameter("jobName");
		jobGroup = aRequest.getParameter("jobGroup");
				
		try {
			
			myScheduler.resumeJob(jobName, jobGroup);
		} catch (SchedulerException e) {
			throw new TBitsException("Unable to resume this job with details " +
					jobName + ":" + jobGroup);
		}
		
		try {
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/all-job-actions/"));
		} catch (IOException e) {
			throw new TBitsException("Unable to Redirect to list jobs page");
		}
	}
	public void scheduleJob(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws TBitsException {
		
		jobName = aRequest.getParameter("jobName");
		jobGroup = aRequest.getParameter("jobGroup");
		String triggerName = jobName;
		String triggerGroup = jobGroup;
		String cronExpression = aRequest.getParameter("cronExpression");
		System.out.println(cronExpression);
		if((triggerName.length() == 0) || (triggerGroup.length() == 0))
			throw new TBitsException("TriggerName Cannot be null");
		
		CronTrigger trigger = null;
		try {
			trigger = new CronTrigger(triggerName,triggerGroup,jobName,jobGroup,cronExpression);
			myScheduler.unscheduleJob(triggerName,triggerGroup); //remove the trigger if already present
			myScheduler.scheduleJob(trigger);
		} catch (ParseException e) {	
			try {
				PrintWriter out = aResponse.getWriter();
				out.print( "\nError in parsing Cron Expression for " +
						triggerName + ". Please edit this later.\n" );							
			} catch (IOException ex) {
				throw new TBitsException("Unable to Edit the list jobs page");
			}
		}catch (SchedulerException e) {
			try {
				CronExpression exp = new CronExpression(cronExpression);
				Calendar c = Calendar.getInstance();
				c.set(Integer.parseInt(exp.years.first().toString()), 
						Integer.parseInt(exp.months.first().toString()), 
						Integer.parseInt(exp.daysOfMonth.first().toString()), 
						Integer.parseInt(exp.hours.first().toString()), 
						Integer.parseInt(exp.minutes.first().toString()), 
						Integer.parseInt(exp.seconds.first().toString()));
				Date tempDate = c.getTime();
				
				SimpleTrigger simpleTrigger = new SimpleTrigger(triggerName,triggerGroup, jobName, jobGroup, tempDate, tempDate, 0, 0);
				myScheduler.scheduleJob(simpleTrigger);
			}catch (ParseException ex) {	
				try {
					PrintWriter out = aResponse.getWriter();
					out.print( "\nError in parsing Cron Expression for " +
							triggerName + ". Please edit this later.\n" );							
				} catch (IOException IOex) {
					throw new TBitsException("Unable to Edit the list jobs page");
				}
			}catch (SchedulerException se) {
				throw new TBitsException("Error in Scheduling: " +
						triggerName +"Trigger was not Added",se);
			}
		}
		
	}

	
	public void saveJob(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws TBitsException {
		
		String subaction = aRequest.getParameter("subaction");
		if(null == subaction) 
			throw new TBitsException("Unable to identify operation to be carried out");
		
		String preJobName = "";
		String preJobGroup = "";
		if(subaction.equalsIgnoreCase(EDIT)){
			preJobName = aRequest.getParameter("preJobName");
			preJobGroup = aRequest.getParameter("preJobGroup");
			if(preJobName == null || preJobName == "" || preJobGroup == null || preJobGroup == "")
				throw new TBitsException("Unable to identify Job to be edited");
		}
		
		jobName = aRequest.getParameter("jobName");
		jobGroup = aRequest.getParameter("jobGroup");
		jobClassName = aRequest.getParameter("className");
		jobDescription = aRequest.getParameter("description");
		String sysPrefix = aRequest.getPathInfo();
		if(null == sysPrefix) sysPrefix = "";
		
		if((null == jobName) || (jobName.trim().length() == 0))
			throw new TBitsException("Job Name is Required ");
		
		if((null == jobGroup) || (jobGroup.trim().length() == 0))
			throw new TBitsException("Job Group is Required ");
		
		try {
			jobClass = Class.forName(jobClassName);
		} catch (ClassNotFoundException cnfe) {
			throw new TBitsException(jobClassName + " :Class Not Found ",cnfe);
		}
		
		try {
			if((subaction.equalsIgnoreCase("create")) && (null != myScheduler.getJobDetail(jobName, jobGroup))) {
				throw new TBitsException("Job with given name and group Already exists: " + jobName + ":" + jobGroup);
			}
				
		} catch (SchedulerException e1) { e1.printStackTrace(); }
		
		jdm = new JobDataMap();
	//	isRecoverable = aRequest.getParameter("recoverable");
	//	isDurable = aRequest.getParameter("durable");
		
		parameterNames = aRequest.getParameterValues("parameterNames");
		parameterValues = aRequest.getParameterValues("parameterValues");
		params = new Hashtable<String, String>() ;

		if(parameterNames != null)
		{
			for (int i =0; i < parameterNames.length; i++) {
				if (parameterNames[i].trim().length() > 0 && parameterValues[i].trim().length() > 0) {
					jdm.put(parameterNames[i].trim(), parameterValues[i].trim());
					params.put( parameterNames[i].trim(), parameterValues[i].trim()); // GET THE PARAMETES FOR VALIDATION
				}
			}
		}	
		// NITI EDIT
		
		ITBitsJob itbitsjob;
		try {
			itbitsjob = (ITBitsJob) jobClass.newInstance();
			if( itbitsjob.validateParams(params) ){ System.out.println( "All the parameters of JOB are correct."); }	
		}
		catch( IllegalArgumentException e)
		{			
			e.printStackTrace() ;			
			throw new TBitsException( e.getMessage(), e ) ;
		}
		catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new TBitsException( "Error : Cannot Create an instance of " + jobClass + " type.", e1) ;
			
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new TBitsException( "Error : Illegal Access Exception while accessing " + jobClass, e1 ) ;			
		}
		
		
		// x NITI EDIT

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
				if(subaction.equalsIgnoreCase(EDIT)){
					myScheduler.deleteJob(preJobName, preJobGroup);
				}
				myScheduler.addJob(mainJob, true);
			} catch (SchedulerException e) {
				throw new TBitsException("Job Could not be added",e);
			}
			
			scheduleJob(aRequest, aResponse);
			try {
				PrintWriter out = aResponse.getWriter();
				out.print("Job Saved.\n");				
				//aResponse.sendRedirect( WebUtil.getServletPath(aRequest, "/all-job-actions"));
			} catch (IOException e) {
				throw new TBitsException("Unable to Edit the list jobs page");
			}
		}
 
	
	
	public void executeJob(HttpServletRequest aRequest,
		HttpServletResponse aResponse) throws TBitsException {
	 	jobName = aRequest.getParameter("jobName");
		jobGroup = aRequest.getParameter("jobGroup");
		System.out.println("Job name : " + jobName + ", Job Group : " + jobGroup + " is being executed");
		try {
			myScheduler.triggerJob(jobName, jobGroup);
		} catch (SchedulerException e) {
			throw new TBitsException("Unable to execute the Job" +
					jobName + ":" + jobGroup,e);
		}
		try {
			aResponse.sendRedirect(WebUtil.getServletPath(aRequest,"/all-job-actions/?action=list-jobs"));
		} catch (IOException e) {
			throw new TBitsException("Unable to Redirect to list jobs page");
		}
		
}
	
	public void editJob(HttpServletRequest aRequest,HttpServletResponse aResponse) throws SchedulerException, TBitsException {
		
		jobName = aRequest.getParameter("jobName");
		jobName = (jobName == null)?"":jobName;
		jobGroup = aRequest.getParameter("jobGroup");
		jobGroup = (jobGroup == null)?"":jobGroup;
		
		DTagReplacer hp;
		try {
			JobDetail tempJob = myScheduler.getJobDetail(jobName,jobGroup);
			if(tempJob == null) {
				throw new TBitsException(jobName + ":" + jobGroup + " job does not exist");
			}
			
			Hashtable parameterList = getParameterList(tempJob.getJobDataMap());
			
			Trigger[] trigger = myScheduler.getTriggersOfJob(jobName,jobGroup);
			
			String cronExpression = "";
			if(trigger.length > 0)
			{
				CronTrigger myTrigger = (CronTrigger) trigger[0];
								
				//TODO find a way to find Cron expression
				cronExpression = myTrigger.getCronExpression();
			}
			
			JSONArray resp = new JSONArray();
			resp.add(tempJob.getName());
			resp.add(tempJob.getGroup());
			resp.add(tempJob.getJobClass().getName());
			resp.add(tempJob.getDescription());
			resp.add(parameterList);
			resp.add(cronExpression);
			resp.add(title);
			resp.add(target);
			resp.add(cssFile);
			resp.add(userLogin);
			
			String response = "";
			response += "jobName=" + tempJob.getName() + 
				"$jobGroup=" + tempJob.getGroup() + 
				"$className=" + tempJob.getJobClass().getName() + 
				"$description=" + tempJob.getDescription() + 
				"$parameterList=" + parameterList +
				"$cronExpression=" + cronExpression;
			
			String trnProperty = PropertiesHandler.getAppProperties().getProperty("transbit.tbits.transmittal");
			if (Boolean.parseBoolean(trnProperty) == false)
				resp.add("none");
			else
				resp.add("");
			
			PrintWriter out = aResponse.getWriter();
			out.print(resp);
			out.close();
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void listJobs(HttpServletRequest aRequest,
	HttpServletResponse aResponse) throws SchedulerException, TBitsException {
		JobDetail tempJob;
		StringBuilder details = new StringBuilder();
		
		String[] jobGroups = myScheduler.getJobGroupNames();
		if(jobGroups!= null) {
			for(int i=0; i<jobGroups.length; i++) {
				String[] jobNames = myScheduler.getJobNames(jobGroups[i]);
					if(jobNames != null) {
						for(int j=0; j<jobNames.length; j++) {
							tempJob = myScheduler.getJobDetail(jobNames[j],jobGroups[i]);
								if(tempJob != null) 
									details.append(showJob(aRequest, tempJob));
							}
						}
				}
		}
		
		try {
			DTagReplacer hp = new DTagReplacer(LIST_JOB_HTML);
			hp.replace("filesList", details.toString());
			hp.replace("target", target);
			hp.replace("title", title);
			hp.replace("cssFile",cssFile);
//			hp.replace("sys_ids",sys_ids);
			hp.replace("userLogin",userLogin);
			hp.replace("nearestPath",nearestPath);
//			hp.replace("sysPrefix", sysPrefix);
			
			//Added by Lokesh to show/hide transmittal tab based on the transmittal property in app-properties
			String trnProperty = PropertiesHandler.getAppProperties().getProperty("transbit.tbits.transmittal");
			if ((trnProperty == null) || Boolean.parseBoolean(trnProperty) == false)
				hp.replace("trn_display", "none");
			else
				hp.replace("trn_display", "");
			
			PrintWriter out = aResponse.getWriter();
			out.print(hp.parse(0));
			out.close();
			out.flush();
			
		} catch (FileNotFoundException e) {
			throw new TBitsException(LIST_JOB_HTML + " file not Found");
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	public String showJob(HttpServletRequest aRequest, JobDetail tempJob) {
		
		jobName = tempJob.getName();
		jobGroup = tempJob.getGroup();
		jobDescription = tempJob.getDescription();
		jobClassName = tempJob.getJobClass().getName();
		
		String path = WebUtil.getServletPath(aRequest, "/all-job-actions");
		
		StringBuilder sb = new StringBuilder();
		sb.append("<tr class=\"odd\"> ");
		sb.append("<td style=\"width: 200px\">");
		sb.append("<a href='" + path 
				+ "?action=create-job&jobName=" + jobName + "&jobGroup="
				+ jobGroup + "'>Edit</a> |&nbsp");
		sb.append("<a href='" + path 
				+ "?action=execute-job&jobName=" + jobName + "&jobGroup="
				+ jobGroup + "'>Execute</a>| &nbsp");
		sb.append("<a href='" + path 
				+ "?action=delete-job&jobName=" + jobName + "&jobGroup="
				+ jobGroup + "'>Delete</a>| &nbsp");
		try {
			if (myScheduler.getTriggerState(tempJob.getName(), tempJob
					.getGroup()) == Trigger.STATE_PAUSED) {
				sb.append("<a href='" + path 
						+ "?action=resume-job&jobName=" + jobName
						+ "&jobGroup=" + jobGroup + "'>Resume</a>");
			} else {
				sb.append("<a href='" + path 
						+ "?action=pause-job&jobName=" + jobName + "&jobGroup="
						+ jobGroup + "'>Pause</a>");
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
			sb.append("<a href='" + path 
					+ "?action=resume-job&jobName=" + jobName + "&jobGroup="
					+ jobGroup + "'>Resume</a>");
			sb.append("<a href='" + path  + "?action=pause-job&jobName="
					+ jobName + "&jobGroup=" + jobGroup + "'>Pause</a>");
		}
		sb.append("| &nbsp");
		sb.append("</td> <td>" + jobGroup + "</td>");
		sb.append("<td>" + jobName + "</td>");
		sb.append("<td>" + jobDescription + "</td>");
		sb.append("<td>" + jobClassName + "</td></tr>"); 
		
		return sb.toString();
	}
	
	public Hashtable getParameterList(JobDataMap jdm) {
		Hashtable hash = new Hashtable();
		for(String str:jdm.getKeys()) {
			System.out.println(str + "," + jdm.getString(str));
			hash.put(str, jdm.getString(str));
			}
		return hash;
	}
	
// Main Method for this class. This method can be used either for testing or fot 
	// inserting jobs programatically
	
	public static void main(String[] args) throws ClassNotFoundException, SchedulerException {
		int count = 0;
		long l = Long.parseLong("1256993100000");
		Date d = new Date(l);
		System.out.println(d);
//		Class c = Class.forName("transbit.tbits.scheduler.ScheduleRequest");
//		for(int i=0; i<60; i++) {
//			for(int j=0; j<14; j++){
//				
//				if((++count)>183)
//					return;
//				
//				String cronExpression = String.valueOf(j*4)+ " " + String.valueOf(i) +
//											" 0 9,19,29 * ?";
//				
//				JobDetail myJob = new JobDetail();
//				myJob.setName("ExpressIT"+count);
//				myJob.setGroup("ExpressIT");
//				myJob.setDescription("Testing Job for Express IT");
//				myJob.setJobClass(c);
//				myJob.setRequestsRecovery(false);
//				myJob.setDurability(true);
//				myJob.setVolatility(false);
//				
//				System.out.println(myJob.toString());
//				myScheduler.addJob(myJob, true);
//				
//				CronTrigger trigger = null;
//				try {
//					 trigger = new CronTrigger("ExpressIT"+count,"ExpressIT","ExpressIT"+count,"ExpressIT",cronExpression);
//				} catch (ParseException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				System.out.println(trigger.toString());
//				myScheduler.scheduleJob(trigger);
//				
//			}
//		}
		
	}
}
