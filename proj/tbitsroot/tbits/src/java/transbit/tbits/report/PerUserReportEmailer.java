package transbit.tbits.report;

import static transbit.tbits.Helper.TBitsConstants.PKG_SCHEDULER;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;



import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.Helper.TBitsHelper;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Mail;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.ParameterType;
import transbit.tbits.webapps.UserForm;
import transbit.tbits.webapps.Users;

/**
 * <p>
 * This class executes and emails report to every active user selected.<br/>
 * It consumes certain params and spits out remaining (plus some more) params to the report.
 * <p>
 * It expects the following parameters and consumes them: 
 * <ul>
 *  <li><b>subject</b> - String, default value is blank. The subject line. it may contain "$date" which will be replaced by the actual date . The format of date is "dd MMM, yyyy". And $user is substituted with the display name of user.
 *  <li><b>fromAddress</b> - String, default value is blank. The address from which the emails should be sent.
 *  <li><b>reportfile</b> - String, default value is blank. The name of the file.
 *  <li><b>essentialData</b> - It is a comma separated list of names of the elements of a page. Default value is empty.
 * 			Remember to give name to the element. For example: "overduetasks,upcomingtasks". 
 *  <li><b>leaveOutputFile</b> - true/false, default is false. Should the HTML file be left in tmp directory, this is specifically useful 
 * 			when when we want to dry run and see what is being sent to who.
 *  <li><b>includeAllUsers</b> - true/false, default is false. Whether to include all the users or not
 *  <li><b>extraRecipients</b> - comma separated logins, The logins of users to be included additionally. To just select these users, mark includeAllUsers as false.
 *  <li><b>includeExternalUsers</b> - true/false, default is false. out of all the selected users through includeAllUsers and extraRecipients, exclude the external users or not.
 *  <li><b>userFilterQuery</b> - SQL, default is empty. it should select all the columns of Users table only. If it is not empty, The users selected by this sql are the final set of users. It overrides the params 
 *  	includeAllUsers, extraRecipients, includeExternalUsers.
 *  <li><b>sendmails</b> - true/false, default is true. If marked false, no emails will be sent. This is required for testing.
 *  <li><b>outputFileFormat</b> - pdf/html, default or non-pdf is html. The format in which you want the output. As of now, only pdf and html are supported.
 *  <li><b>mailBody</b> - Text, default is empty. The content of the email if the report is being sent as attachment. This is only valid for non-html formats.
 *  <li><b>extraTos</b> - comma separated logins, default is empty. If in every email, you want to send the emails to some more people, add them in extraTos.
 *  <li><b>batchSize</b> - integer, default value is 0, the number of reports to be run using a single instance of engine. If it is 0, it means all the reports will be run using a single instance of report engine.
 *  <li><b>outputFilename</b> - 
 * </ul>
 * It feeds the following to the reports so report should accept the following parameters:
 *  <ul>
 *  <li><b>user_id</b> - For which user the report is being run.
 *  <li><b>tbits_base_url</b> - The base URL of tBits.
 *  </ul>
 * Rest of the parameters which are passed to the job would be passed as such. 
 * Out of remaining params, if some params do not exist in the report, they are simply ignored
 */
public class PerUserReportEmailer implements ITBitsJob {
	
	private static final String DATE_FORMAT = "dd MMM, yyyy";
	private static ArrayList<String> reportList;
	//To Consume
	private static final String CMD_PARAM_SUBJECT = "subject";
	private static final String CMD_PARAM_FROM = "fromAddress";
	private static final String CMD_PARAM_REPORT_FILE = "reportfile";
	private static final String CMD_PARAM_ESSENTIAL_DATA = "essentialData";
	private static final String CMD_PARAM_LEAVE_OUTPUT_FILE = "leaveOutputFile";
	private static final String CMD_PARAM_INCLUDE_ALL_USERS = "includeAllUsers";
	private static final String CMD_PARAM_EXTRA_RECIPIENTS = "extraRecipients";
	private static final String CMD_PARAM_INCLUDE_EXTERNAL_USERS = "includeExternalUsers";
	private static final String CMD_PARAM_USER_FILETER_QUERY = "userFilterQuery";
	
	
	private static final String CMD_PARAM_SEND_MAILS = "sendmails";
	private static final String CMD_PARAM_DISPLAY_NAME = "Report Mailer";
	
	private static final String CMD_PARAM_FORMAT = "outputFileFormat";
	private static final String CMD_PARAM_MAIL_BODY = "mailBody";
	private static final String CMD_OUTPUT_FILENAME = "outputFilename";
	private static final String CMD_PARAM_EXTRA_TOS = "extraTos";
//	private static final String CMD_PARAM_EXTRA_CCS = "extraCCs";
	private static final String CMD_PARAM_BATCH_SIZE = "batchSize";
	
	
	//To spit out
	private static final String USER_ID = "user_id";
	private static final String TBITS_BASE_URL_KEY = "tbits_base_url";
	
	// Application logger.
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SCHEDULER);

	// Name of the html interface that renders the search results.
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		JobDetail jd = arg0.getJobDetail();
		JobDataMap jdm = jd.getJobDataMap();
		String jobName=jd.getFullName();
		String jobGroup=jd.getGroup();
		
		
		
		
		System.out.println("Job name : " + jobName + " is being executed....");
		System.out.println("Job Parameters are : " + "Job Group : "+ jobGroup + "," + "Job Class : " + CMD_PARAM_DISPLAY_NAME);
		this.execute(jdm);
		System.out.println("job: "+jobName + " has finish....");
		
		
	}

	public void execute(JobDataMap jdm) throws JobExecutionException {
		
		//System.out.println("Job name : " +  + ", Job Group : " + jobGroup + " is being executed");
		LOG.debug("=== Starting the daily agenda. ===");
		String[] keys = jdm.getKeys();
		
		HashMap<String, String> params = new HashMap<String, String>();
		String fromAddress = "donotreply@tbitsreports";
		String subject = "";
		String [] essentialData = new String[]{};
		boolean leaveOutputFile = false;
		String reportFile = null;
		boolean includeExternalUsers = false;
		boolean includeAllUsers = false;
		String [] extraRecipients = new String[]{};
		String[] extraTos = new String[]{};
//		String[] extraCCs = new String[]{};
		String userFilterQuery = "";
		int batchSize = 0;
		
		boolean sendMails = true;
		
		String outputFileFormat = "";
		String mailBody = "";
		String outputFilename = "";
		
		
		for(String key:keys)
		{
			String val = jdm.getString(key);
			if(key.equals(CMD_PARAM_FROM)){
				fromAddress = val;
				
			}else if(key.equals(CMD_PARAM_INCLUDE_EXTERNAL_USERS)){
				String includeExternalUsersStr = val;
				try
				{
					includeExternalUsers = Boolean.parseBoolean(includeExternalUsersStr);
				}
				catch (Exception e) {
					LOG.warn("Unable to get key: " + CMD_PARAM_INCLUDE_EXTERNAL_USERS);
				}
			}else if(key.equals(CMD_PARAM_INCLUDE_ALL_USERS)){
				String includeAllUsersStr = val;
				try
				{
					includeAllUsers = Boolean.parseBoolean(includeAllUsersStr);
				}
				catch (Exception e) {
					LOG.warn("Unable to get key: " + CMD_PARAM_INCLUDE_ALL_USERS);
				}
				
			}else if(key.equals(CMD_PARAM_SUBJECT)){
				subject = val.trim();
				Timestamp ts = new Timestamp();
				if (subject.indexOf("$date") >= 0) {
					subject = subject.replace("$date", ts.toCustomFormat(DATE_FORMAT));
				}
				
			}else if(key.equals(CMD_PARAM_ESSENTIAL_DATA)){
				String essentialDataStr = val;
				essentialData = new String[]{};
				if(essentialDataStr != null)
					essentialData = essentialDataStr.split(",");
			}else if(key.equals(CMD_PARAM_EXTRA_RECIPIENTS)){
				String extraRecipientStr = val;
				extraRecipients = new String[]{};
				if(extraRecipientStr != null)
					extraRecipients = extraRecipientStr.split(",");
			}else if(key.equals(CMD_PARAM_EXTRA_TOS)){
				String extraTosStr = val;
				extraTos = new String[]{};
				if(extraTosStr != null)
					extraTos = extraTosStr.split(",");
			}else if(key.equals(CMD_PARAM_USER_FILETER_QUERY)){
				String filterQuery = val;
				if(filterQuery != null)
					userFilterQuery = filterQuery.trim();
			}
			
			else if(key.equals(CMD_PARAM_LEAVE_OUTPUT_FILE)){
				try
				{
					leaveOutputFile = Boolean.parseBoolean(val);
				}
				catch(Exception e)
				{
					LOG.warn("'" + CMD_PARAM_LEAVE_OUTPUT_FILE + "' param is not specified in the report. so assuming it to be false.");
				}
				
			}else if(key.equals(CMD_PARAM_REPORT_FILE)){
					reportFile = val;
			}else if(key.equals(CMD_PARAM_FORMAT)){
				outputFileFormat = val;
				
			}else if(key.equals(CMD_PARAM_MAIL_BODY)){
				mailBody = val;
			}
             else if(key.equals(CMD_OUTPUT_FILENAME)){
 				
 				outputFilename = val;
 				 
 				
 			}
             
			else if(key.equals(CMD_PARAM_SEND_MAILS))
			{
				try
				{
					sendMails = Boolean.parseBoolean(val);
				}
				catch(Exception e)
				{
					LOG.warn("'" + CMD_PARAM_SEND_MAILS + "' param is not specified in the report. so assuming it to be true.");
				}
			}
			else if(key.equals(CMD_PARAM_BATCH_SIZE))
			{
				try
				{
					batchSize = Integer.parseInt(val);
				}
				catch(Exception e)
				{
					LOG.warn("'" + CMD_PARAM_SEND_MAILS + "' param is not specified in the report. so assuming it to be true.");
				}
			}
			
			else
			{
				params.put(key, val);
				
			}
		}
		if(reportFile == null)
		{
			LOG.error("The report file parameter is missing. Please provide the correct value of parameter '" + CMD_PARAM_REPORT_FILE + "'");
			System.out.println("The report file parameter is missing. Please provide the correct value of parameter '" + CMD_PARAM_REPORT_FILE + "'");
			return;
		}
		try {
			runDailyTaskReport(subject, fromAddress, reportFile, leaveOutputFile, includeExternalUsers, includeAllUsers, 
					extraRecipients, essentialData, params, sendMails,outputFileFormat,mailBody,outputFilename, userFilterQuery, extraTos, batchSize);
			LOG.info("Running the report finished successfully.");
			
			System.out.println("Running the report finished successfully.");
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			LOG.error(e);
			e.printStackTrace();
		}
		
	}
	
	public void runDailyTaskReport(String subject, String fromAddress, String reportName, boolean leaveOutputFile, 
			boolean includeExternalUsers, boolean includeAllUsers, String[] extraRecipients, String[] essentialData, 
			HashMap<String, String> params,boolean sendMails,String outputFormat, String mailBody,String outputFilename, 
			String userFilterQuery, String[] extraTos, int batchSize) throws JobExecutionException
	{
		Set<User> reportUsersSet = getConsolidatedUsers(includeExternalUsers,
				includeAllUsers, extraRecipients, userFilterQuery);
		if ((reportUsersSet == null) || (reportUsersSet.size() == 0))
			return;
		
		List<User> reportUsers = new ArrayList<User>();
		reportUsers.addAll(reportUsersSet);
		
		String extraTosUsers = getUserEmailWrtToLogins(extraTos);
		
		if(batchSize <= 0)
			batchSize = reportUsers.size();

		for(int i=0;i < reportUsers.size();i+=batchSize)
		{
			int start = i;
			int end = start+batchSize;
			runDailyTaskReportInUserRange(subject, fromAddress, reportName, leaveOutputFile, reportUsers, 
					essentialData, params, sendMails, outputFormat, mailBody, outputFilename, extraTosUsers, start, end);
		}

	}
	public void runDailyTaskReportInUserRange(String subject, String fromAddress, String reportName, boolean leaveOutputFile, 
			List<User> reportUsers, String[] essentialData, HashMap<String, String> params,boolean sendMails,
			String outputFormat, String mailBody,String outputFilename, 
			String extraToUsers, int start, int end)
			throws JobExecutionException {
		//get a list of users
		
		if (reportUsers.size() == 0)
			return;

		
		TBitsReportEngine tRE = null;
		
		try{
			tRE = TBitsReportEngine.getInstance();
		}catch (TBitsException e) {
			e.printStackTrace();
		} 
		if(tRE == null)
		{
			LOG.error("Unable to get the instance of ReportEngine.");
			
			System.out.println("Unable to get the instance of ReportEngine.");
			return;
		}
		IReportRunnable reportDesign = null;
		
		try {
			reportDesign = tRE.getReportDesign(reportName);
		} catch (TBitsException e2) {
			e2.printStackTrace();
		}
		
		if(reportDesign == null)
		{
			LOG.error("Unable to get the design instance of " + reportName);
			return;
		}
		params.put(TBITS_BASE_URL_KEY, PropertiesHandler
				.getProperty(TBitsPropEnum.KEY_NEAREST_INSTANCE));
		
		int totalUsers = reportUsers.size();
		int countUsers = 0;
		for(int j=start; j < end &&  j< reportUsers.size();j++)
		{
			User u = reportUsers.get(j);
			params.put(USER_ID, Integer.toString(u.getUserId()));
			LOG.info("[" + (j+1) + "/" + end +  "] Trying for :" + "[" + u.getUserLogin() +"]" );
			LOG.info("User Type: " + u.getUserTypeId());
			
			File outFile;
	
			IReportDocument ird = null;			
			try {
				ird = tRE.getReportDocument(reportDesign, params);
				if(!tRE.containsResults(ird, essentialData)){
					LOG.info("Not sending the report as report does not have data.");
					
					System.out.println("Not sending the report as report does not have data.");
					continue;
				}
				if(outputFormat.equalsIgnoreCase("PDF"))
				{
					outFile = tRE.getPDFReport(ird); 
				}
				else
				{
					outFile = tRE.getHTMLReport(ird);
				}
				
				//TODO:delete report doc.
				try {
					String email = u.getEmail().trim();
					
					if(extraToUsers.length() > 0)
					{
						if(email.length() > 0)
							email += ",";
						email = email + extraToUsers;
					}
					
					if (email.length() == 0)
						LOG.warn("Email id not specified for user: " + u.getUserLogin());
					else {
						
						if(sendMails)
						{   
							String htmlText = TBitsHelper.ReadFileToEnd(outFile
									.getAbsolutePath());

							LOG.info("Sending mail to " + email);
							
							System.out.println("Sending mail to " + email);


							if(outputFormat.equalsIgnoreCase("PDF"))
							{
								String textBody  =  mailBody;
								String tmpFilePath = outFile.getAbsolutePath().replaceAll(outFile.getName(),outputFilename);

								File file = new File(tmpFilePath);
								file.delete();
								if(!outFile.renameTo(new File(tmpFilePath))){
									LOG.warn("Could not delete the file : '" + tmpFilePath + "'. Just copying the file.");
									try
									{
										LOG.debug("Trying to copy the output report file using nio.");
										Utilities.copyFile(outFile,new File(tmpFilePath));
									}
									catch(Throwable t)
									{
										LOG.debug("Trying to copy the output report file 1mb at a time.");
										Utilities.copyFileSlow(outFile,new File(tmpFilePath));
									}
								}
								
								Mail.sendWithAttachment(email, fromAddress, subsSubject(subject, u), textBody, tmpFilePath);
							}
							else
								Mail.sendWithHtml(email, fromAddress, subsSubject(subject, u), htmlText);


						}
						else
						{
							LOG.info("Skipping mail sending.");
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					LOG.error(e);
				}
				
				if(!leaveOutputFile)
				{
					if (outFile.exists() && !outFile.delete())
						LOG.warn("Can not delete the temporary file: "
								+ outFile.getAbsolutePath());
				}
				
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				LOG.error(e1);
			} catch (EngineException e1) {
				e1.printStackTrace();
				LOG.error(e1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOG.error(e);
			}finally{
				String irdFilePath = ird.getName();
				ird.close();
				ird = null;
				File irdFile = new File (irdFilePath);
				if (irdFile.isFile() && irdFile.exists()){
					irdFile.delete();
				}
				irdFile = null;
				irdFilePath = "";
			}
		}
//		if (tRE != null)
//			tRE.destroy();
		LOG.info("Running the batch finished successfully.");
	}

	private String getUserEmailWrtToLogins(String[] userLogins) {
		StringBuilder users = new StringBuilder();
		boolean isFirst = true;
		for(String s:userLogins)
		{
			if((s == null) || (s.trim().length() == 0))
					continue;
			User u = null;
			try {
				u = User.lookupAllByUserLogin(s);
				if(u.getIsActive())
				{
					String email = u.getEmail();
					if((email == null) || (email.trim().length() == 0))
						continue;
					
					if(!isFirst)
						users.append(",");
					else
						isFirst = false;
					users.append(email);
				}
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOG.error("Unable to get the user wrt to : " + s);
			}
		
		}
		return users.toString();
	}

	private Set<User> getConsolidatedUsers(boolean includeExternalUsers,
			boolean includeAllUsers, String[] extraRecipients,
			String userFilterQuery) {
		Set<User> reportUsers = new HashSet<User>();
		try {
				if(userFilterQuery.length() != 0)
				{
					reportUsers = getUserForFilteringQuery(userFilterQuery);
				}
				else
				{
					ArrayList<User> users = new ArrayList<User>();
					
					if(includeAllUsers)
						users.addAll(User.lookupAll());
					
					for(String eu: extraRecipients)
					{
						User u = User.lookupAllByUserLogin(eu);
						if(u == null)
							continue;
						users.add(u);
					}
					for(User u:users)
					{
						if (!u.getIsActive())
							continue;
						
						if((!includeExternalUsers) && (u.getUserTypeId() == UserType.EXTERNAL_USER))
							continue;
						
						reportUsers.add(u);
					}
				}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			LOG.error(e);
		}
		return reportUsers;
	}

	private Set<User> getUserForFilteringQuery(String userFilterQuery) throws DatabaseException {
		Set<User> reportUsers = new HashSet<User>();
		Connection conn = null;
		try
		{
			conn = DataSourcePool.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs =  stmt.executeQuery(userFilterQuery);
			if(rs != null)
			{
				while(rs.next())
				{
					reportUsers.add(User.createFromResultSetAll(rs));
				}
			}
		}
		catch(SQLException t)
		{
			throw new DatabaseException("Unable to get the users corresponding to " + userFilterQuery + ". " + t.getMessage(), t);
		}
		finally
		{
			try {
				if((conn != null) && !conn.isClosed())
				{
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return reportUsers;
	}
	
	public static String subsSubject(String subject, User u)
	{
		return subject.replace("$user", u.getDisplayName());
	}
	
	
	public Hashtable<String, JobParameter> getParameters() throws SQLException{
		Hashtable<String, JobParameter> params = new Hashtable<String, JobParameter>();
		JobParameter param;
		
		param = new JobParameter();
		param.setName(CMD_PARAM_REPORT_FILE);
		param.setType(ParameterType.Text);
		param.setMandatory(true);
		params.put(CMD_PARAM_REPORT_FILE, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_SUBJECT);
		param.setType(ParameterType.Text);
		params.put(CMD_PARAM_SUBJECT, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_FROM);
		param.setType(ParameterType.Text);
		param.setMandatory( true ) ;
		params.put(CMD_PARAM_FROM, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_ESSENTIAL_DATA);
		param.setType(ParameterType.Text);
		params.put(CMD_PARAM_ESSENTIAL_DATA, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_INCLUDE_EXTERNAL_USERS);
		param.setType(ParameterType.CheckBox);
		params.put(CMD_PARAM_INCLUDE_EXTERNAL_USERS, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_EXTRA_TOS);
		param.setType(ParameterType.Text);
		params.put(CMD_PARAM_EXTRA_TOS, param);
		
//		param = new JobParameter();
//		param.setName(CMD_PARAM_EXTRA_CCS);
//		param.setType(ParameterType.Text);
//		params.put(CMD_PARAM_EXTRA_CCS, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_USER_FILETER_QUERY);
		param.setType(ParameterType.TextArea);
		params.put(CMD_PARAM_USER_FILETER_QUERY, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_INCLUDE_ALL_USERS);
		param.setType(ParameterType.CheckBox);
		params.put(CMD_PARAM_INCLUDE_ALL_USERS, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_EXTRA_RECIPIENTS);
		param.setType(ParameterType.Text);
		params.put(CMD_PARAM_EXTRA_RECIPIENTS, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_LEAVE_OUTPUT_FILE);
		param.setType(ParameterType.CheckBox);
		params.put(CMD_PARAM_LEAVE_OUTPUT_FILE, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_SEND_MAILS);
		param.setType(ParameterType.CheckBox);
		params.put(CMD_PARAM_SEND_MAILS, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_FORMAT);
		param.setType(ParameterType.Select);
		ArrayList<String> supportedFormats = new ArrayList<String>();
		supportedFormats.add("HTML");
		supportedFormats.add("PDF");
//		supportedFormats.add("xls");
//		supportedFormats.add("doc");
		
		param.setValues(supportedFormats);
		
		params.put(CMD_PARAM_FORMAT, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_MAIL_BODY);
		param.setType(ParameterType.TextArea);
		params.put(CMD_PARAM_MAIL_BODY, param);
		
		param = new JobParameter();
		param.setName(CMD_OUTPUT_FILENAME);
		param.setType(ParameterType.Text);
		params.put(CMD_OUTPUT_FILENAME, param);

		param = new JobParameter();
		param.setName(CMD_PARAM_BATCH_SIZE);
		param.setType(ParameterType.Text);
		params.put(CMD_PARAM_BATCH_SIZE, param);

		return params;
	}
    
	 
    public String getDisplayName(){
    	return CMD_PARAM_DISPLAY_NAME;
    }
    
    public boolean validateParams(Hashtable<String,String> params)    	
    	throws IllegalArgumentException{
  //  	File f = Configuration.findPath("tbitsreports/" + reportFileName);
    	if( null == params.get(CMD_PARAM_FROM) || "".equals(params.get(CMD_PARAM_FROM).trim()))
    		throw new IllegalArgumentException( "Illegal Argument in " + CMD_PARAM_FROM + " field.") ;
    	
    	if( null == params.get(CMD_PARAM_REPORT_FILE) || "".equals(params.get(CMD_PARAM_REPORT_FILE).trim()))
    		throw new IllegalArgumentException( "Illegal Argument in " + CMD_PARAM_REPORT_FILE + " field." ) ;
    	
    	String fileName = params.get(CMD_PARAM_REPORT_FILE) ;
    	
    	File f = Configuration.findPath( "tbitsreports/" + fileName ) ;
    	if( null == f )
    		throw new IllegalArgumentException( "The file in " + CMD_PARAM_REPORT_FILE + " field does not exist." );
    	
		String userFileringQuery = params.get(CMD_PARAM_USER_FILETER_QUERY);
		if((userFileringQuery != null) && (userFileringQuery.trim().length() > 0))
		{
			Set<User> users = null;
			boolean error = false;
			try
			{
				users = getUserForFilteringQuery(userFileringQuery);
			}catch(DatabaseException de)
			{
				throw new IllegalArgumentException("Invalid user filter sql query: "+ de.getMessage());
			}
		}
    	return true;
    }
	
	public static void main(String[] args) throws SemanticException,
	IllegalArgumentException, EngineException {
		JobDataMap jdm = new JobDataMap();
		/*jdm.put(CMD_PARAM_SUBJECT, "Testing daily task report");
		jdm.put(CMD_PARAM_FROM, "root@ibm.mshome.net");
		jdm.put(CMD_PARAM_ESSENTIAL_DATA, "overduetasks,upcomingtasks,taskswithoutduedate");
		jdm.put(CMD_PARAM_INCLUDE_EXTERNAL_USERS, "false");
		jdm.put(CMD_PARAM_REPORT_FILE, "dailytasklist.rptdesign");
		jdm.put(CMD_PARAM_LEAVE_OUTPUT_FILE, "true");
		jdm.put("upcomingreqsdays", "1000");*/
//		jdm.put(CMD_PARAM_SUBJECT, "Testing loggedbyme report");
//		jdm.put(CMD_PARAM_FROM, "root@ibm.mshome.net");
//		jdm.put(CMD_PARAM_ESSENTIAL_DATA, "overduetasks,upcomingtasks,taskswithoutduedate");
//		jdm.put(CMD_PARAM_INCLUDE_EXTERNAL_USERS, "true");
//		jdm.put(CMD_PARAM_REPORT_FILE, "taskslogggedbyme.rptdesign");
//		jdm.put(CMD_PARAM_INCLUDE_ALL_USERS, "true");
//		jdm.put(CMD_PARAM_USER_FILETER_QUERY, "select * from users u where u.user_id in (select distinct user_id from request_users)");
//		jdm.put(CMD_PARAM_LEAVE_OUTPUT_FILE, "true");
////		jdm.put(CMD_PARAM_EXTRA_TOS, "expediter");
//		
//		jdm.put(CMD_PARAM_SEND_MAILS, "true");
//		jdm.put("upcomingreqsdays", "1000");
		
		//jdm.put("essentialData","subscriberdata");
		
		jdm.put("fromAddress","donotreply@tBits");
		jdm.put("includeExternalUsers","true");
		jdm.put(CMD_PARAM_INCLUDE_ALL_USERS, "true");
		jdm.put(CMD_PARAM_EXTRA_RECIPIENTS, "expediter, aagarawal");
//		jdm.put("reportfile","tBits_Bill_Tracking_Delay_prevention_Overdue_Report.rptdesign");
		jdm.put("reportfile", "tBits_Bill_Tracking_Delay_prevention_Upcoming_Report.rptdesign");
//		jdm.put(CMD_PARAM_REPORT_FILE, "taskslogggedbyme.rptdesign");
//		jdm.put(CMD_PARAM_ESSENTIAL_DATA, "tatapower");
		jdm.put("subject","Per User Digest Report Where $user as Subscriber on $date");
		//jdm.put("userFilterQuery", "select * from users where user_id in (select distinct user_id from request_users where user_type_id = 4 and sys_id = 19)");
//		jdm.put(CMD_PARAM_SEND_MAILS, "false");
		jdm.put(CMD_OUTPUT_FILENAME, "xyz.pdf");
//		jdm.put(CMD_PARAM_FORMAT, "PDF");
		jdm.put(CMD_PARAM_MAIL_BODY, "Find the crap attached.");
		
		jdm.put(CMD_PARAM_BATCH_SIZE, 0+"");
		
		PerUserReportEmailer dtj = new PerUserReportEmailer();	

		try {
			dtj.execute(jdm);
		} catch (JobExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
