/**
 * 
 */
package transbit.tbits.report;

import static transbit.tbits.Helper.TBitsConstants.PKG_SCHEDULER;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.Helper.TBitsHelper;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Mail;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.ReportCirculation;

/**
 * @author Lokesh
 *
 */
public class TBitsReportMailer implements Job {

	// Application logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SCHEDULER);

  //To Consume
	private static final String CMD_PARAM_REPORT_FILE = "reportfile";
	private static final String CMD_PARAM_SUBJECT = "subject";
	private static final String CMD_PARAM_FROM = "fromAddress";
	private static final String CMD_PARAM_ESSENTIAL_DATA = "essentialData";
	private static final String CMD_PARAM_LEAVE_OUTPUT_FILE = "leaveOutputFile";
	private static final String CMD_PARAM_INCLUDE_EXTERNAL_USERS = "includeExternalUsers";
	private static final String IS_PER_USER_REPORT = "isPerUserReport";
	//To spit out
	private static final String USER_ID = "user_id";
	private static final String TBITS_BASE_URL_KEY = "tbits_base_url";
	//Users
/*	private static final String CMD_PARAM_RUN_AS_USERS_BA_ROLES = "runAsUsersBARoles";	
	private static final String CMD_PARAM_RUN_AS_INCLUDE_USERS = "runAsIncludeUsers";
	private static final String CMD_PARAM_RUN_AS_EXCLUDE_USERS = "runAsExcludeUsers";*/
	private static final String CMD_PARAM_RECIPIENT_BA_ROLES = "recipientsBARoles";
	private static final String CMD_PARAM_INCLUDE_RECIPIENTS = "includeRecipients";
	private static final String CMD_PARAM_EXCLUDE_RECIPIENTS = "excludeRecipients";
	private static final String BA = "ba";
	private static final String ROLES = "roles";
	private String jobName;
	
	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		JobDetail jd = arg0.getJobDetail();
		jobName = jd.getName();
		JobDataMap jdm = jd.getJobDataMap();
		this.execute(jdm);
	}

	public void execute(JobDataMap jdm) throws JobExecutionException {
		//ArrayList<String> runAsUsers = new ArrayList<String>();		
		ArrayList<String> recipients = new ArrayList<String>();
		LOG.debug("=== Starting the daily agenda. ===");
		String[] keys = jdm.getKeys();
		
		String runAsUser = "";
		HashMap<String, String> params = new HashMap<String, String>();
		String fromAddress = "donotreply@tbitsreports";
		String subject = "";
		String [] essentialData = new String[]{};
		boolean leaveOutputFile = false;
		String reportFile = null;
		boolean includeExternalUsers = false;
		boolean isPerUserReport = false;
		
		try{
			for(String key:keys)
			{
				String val = jdm.getString(key);
				if(key.equals(CMD_PARAM_FROM)){
					fromAddress = val;

				}
				else if(key.equals(CMD_PARAM_INCLUDE_EXTERNAL_USERS)){
					String includeExternalUsersStr = val;
					try
					{
						includeExternalUsers = Boolean.parseBoolean(includeExternalUsersStr);
					}
					catch (Exception e) {
						LOG.warn("Unable to get key: " + CMD_PARAM_INCLUDE_EXTERNAL_USERS);
					}
					
				}else if(key.equals(CMD_PARAM_SUBJECT)){
					subject = val.trim();
					Timestamp ts = new Timestamp();
					if (subject.indexOf("$date") >= 0) {
						subject = subject.replace("$date", ts.toCustomFormat("yyyy-MM-dd"));
					}

				}else if(key.equals(CMD_PARAM_ESSENTIAL_DATA)){
					String essentialDataStr = val;
					essentialData = new String[]{};
					if(essentialDataStr != null)
						essentialData = essentialDataStr.split(",");

				}else if(key.equals(CMD_PARAM_LEAVE_OUTPUT_FILE)){
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
				}
				else if(key.equals(IS_PER_USER_REPORT))
				{
					try
					{
						isPerUserReport = Boolean.parseBoolean(val);
					}
					catch(Exception e)
					{
						LOG.warn("'" + IS_PER_USER_REPORT + "' param is not specified in the report. so assuming it to be false.");
					}
				}
				else 
				{
					params.put(key, val);
				}
			}
			
			Hashtable<String, String> jdmHashtable = ReportCirculation.lookupReportJobDataByJobName(jobName);
			
			if (jdmHashtable != null){
				Set<String> keySet = jdmHashtable.keySet();
				System.out.println("Before rjdm:" + keySet.size() + ", " + jdmHashtable.toString());
				for (String key : keySet){
					String val = jdmHashtable.get(key);System.out.println("key: " + key + ", Val: " + val);
					/*if(key.equals(CMD_PARAM_RUN_AS_USERS_BA_ROLES))
					{
						setUsersList(runAsUsers, val);					
					}
					else if(key.equals(CMD_PARAM_RUN_AS_INCLUDE_USERS))
					{
						insertUsersIntoList(runAsUsers, val);
					}
					else if(key.equals(CMD_PARAM_RUN_AS_EXCLUDE_USERS))
					{
						removeUsersFromList(runAsUsers, val);
					}*/
					if (key.equals("runAsUser"))
						runAsUser = val;
					
					if(key.equals(CMD_PARAM_RECIPIENT_BA_ROLES))
					{
						setRecipientsList(recipients, val);
					}
					else if(key.equals(CMD_PARAM_INCLUDE_RECIPIENTS))
					{
						insertUsersIntoList(recipients, val);
					}
					else if(key.equals(CMD_PARAM_EXCLUDE_RECIPIENTS))
					{
						removeUsersFromList(recipients, val);
					}					
				}System.out.println("After rjdm:");
			}
			
		} catch (DatabaseException e) {
			LOG.error("Database error occurred while retrieving users for runAsUsers/recipients", e);
			e.printStackTrace();
		}
		if(reportFile == null)
		{
			LOG.error("The report file parameter is missing. Please provide the correct value of parameter '" + CMD_PARAM_REPORT_FILE + "'");
			return;
		}
		try {System.out.println("Before running report");
			runReport(subject, fromAddress,	reportFile, leaveOutputFile, includeExternalUsers, 
					essentialData, params, isPerUserReport, runAsUser, listToString(recipients));System.out.println("After running report");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			LOG.error(e);
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public void runReport(String subject, String fromAddress, String reportName, boolean leaveOutputFile, 
			boolean includeExternalUsers, String[] essentialData, HashMap<String, String> params,boolean isPerUserReport,
			String runAsUser, String recipients)
			throws JobExecutionException, DatabaseException, BirtException 
	{
		
		//get a list of users if its not per user report else user runAsUser
		ArrayList<User> users = null;
		if (isPerUserReport){
			try {
				users = User.lookupAll();
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
				LOG.error(e);
			}
		}
		else{
			users = getUsers(runAsUser);
		}	
		
		if (users == null)
			return;

		TBitsReportEngine tRE = null;
		try {
			tRE = TBitsReportEngine.getInstance();
		} catch (TBitsException e3) {
			LOG.error(e3);
			e3.printStackTrace();
		}
		if(tRE == null)
		{
			LOG.error("Unable to get the instance of ReportEngine.");
			return;
		}
		IReportRunnable reportDesign = null;
		try {
			reportDesign = tRE.getReportDesign(reportName);
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(reportDesign == null)
		{
			LOG.error("Unable to get the design instance of " + reportName);
			return;
		}
		params.put(TBITS_BASE_URL_KEY, PropertiesHandler
				.getProperty(TBitsPropEnum.KEY_NEAREST_INSTANCE));
		
		//for (User u : users) {
		for(User u : users){
			if (!u.getIsActive())
				continue;
			if((!includeExternalUsers) && (u.getUserTypeId() == UserType.EXTERNAL_USER))
				continue;
			
			params.put(USER_ID, Integer.toString(u.getUserId()));
			LOG.info("Trying for :" + u.getDisplayName());
			LOG.info("User Type: " + u.getUserTypeId());
			
			File outFile;
			IReportDocument ird = null;
			String irdFilePath;
			File irdFile;
			try {
				ird = tRE.getReportDocument(reportDesign, params);
				if(!tRE.containsResults(ird, essentialData)){
					LOG.info("Not sending the report as report doesnt have data.");
					continue;
				}
				outFile = tRE.getHTMLReport(ird);
				
				//TODO:delete report doc.
				try {					
					String htmlText = TBitsHelper.ReadFileToEnd(outFile.getAbsolutePath());
					if(isPerUserReport)
					{
						recipients = u.getEmail();
						if (recipients.length() == 0)
							LOG.warn("Email id not specified for user: "
									+ u.getUserLogin());
					}
					
					LOG.info("Sending mail to " + recipients);System.out.println("Sending mail to " + recipients);
					Mail.sendWithHtml(recipients, fromAddress, subsSubject(subject, u), htmlText);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOG.error(e);
				}
				if(!leaveOutputFile)
				{
					if (!outFile.delete())
						LOG.warn("Can not delete the temporary file: "
								+ outFile.getAbsolutePath());
				}
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				LOG.error(e1);
			} catch (EngineException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				LOG.error(e1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				irdFilePath = ird.getName();
				ird.close();
				ird = null;
				irdFile = new File (irdFilePath);
				if (irdFile.isFile() && irdFile.exists()){
					irdFile.delete();
				}
				irdFile = null;
				irdFilePath = "";
			}
		}
//		if (tRE != null)
//			tRE.destroy();
		System.out.println("Running TBitsMailer finished Successfully.");
		//for all active users 
		//prepare report and send it
	}
	
	private String listToString (ArrayList<?> userList){
		StringBuffer userBuffer = new StringBuffer("");
		for (Object obj : userList.toArray()){
			if (userBuffer.toString().equals(""))
				userBuffer.append((String)obj);
			else
				userBuffer.append(",").append((String)obj);
		}
		return userBuffer.toString();	
	}

	/**
	 * @param usersList
	 * @param val
	 * @throws DatabaseException
	 */
	private void removeUsersFromList(ArrayList<String> usersList, String val) 
			throws DatabaseException {
		JSONArray excludeUsers = JSONArray.fromObject(val);
		if (!excludeUsers.isEmpty())
			for(String userLogin : (String [])excludeUsers.toArray()){
				User user = User.lookupAllByUserLogin(userLogin);
				if (usersList.contains(user.getEmail()))			
					usersList.remove(user.getEmail());
				else
					continue;
			}
	}

	/**
	 * @param userList
	 * @param val
	 * @throws DatabaseException
	 */
	private void insertUsersIntoList(ArrayList<String> userList, String val)
			throws DatabaseException {
		JSONArray includeUsers = JSONArray.fromObject(val);
		if (!includeUsers.isEmpty())
			for(Object userLogin : includeUsers){System.out.println("Userlogin: " + userLogin);
				User user = User.lookupAllByUserLogin((String)userLogin);
				if (!userList.contains(user.getEmail()))			
					userList.add(user.getEmail());
				else
					continue;
			}
	}

	/**
	 * @param usersList
	 * @param baRolesJSONArray
	 * @throws DatabaseException
	 */
	private void setRecipientsList(ArrayList<String> usersList, String baRolesJSONArray)
			throws DatabaseException {		
		JSONArray userArray = JSONArray.fromObject(baRolesJSONArray);
		for (Object obj : userArray){
			JSONObject jsonObj = (JSONObject)obj;
			String baName = (String)jsonObj.get(BA);
			String rolesList = jsonObj.getString(ROLES);
			JSONArray rolesArray = JSONArray.fromObject(rolesList);
			for (Object roleName : rolesArray){
				BusinessArea ba = BusinessArea.lookupByName(baName);
				int aSystemId = ba.getSystemId();
				Role role = Role.lookupBySystemIdAndRoleName(aSystemId, (String)roleName);						
				ArrayList<RoleUser> roleUsers = RoleUser.lookupBySystemIdAndRoleId(aSystemId, role.getRoleId());
				for(RoleUser ru : roleUsers){
					User user = User.lookupByUserId(ru.getUserId());
					usersList.add(user.getEmail());
				}
			}
		}		
	}	
	
	private ArrayList<User> getUsers(String usersString) throws DatabaseException{
		ArrayList<User> userList = new ArrayList<User>();
		if (usersString != null)
			for (String userString : usersString.trim().split(",")){
				String userTrim = userString.trim();
				User user = User.lookupByUserLogin(userTrim);
				if (user == null){
					user = User.lookupAllByEmail(userTrim);
				}
				userList.add(user);
			}
		return userList;
	}
	
	public static String subsSubject(String subject, User u)
	{
		return subject.replace("$user", u.getDisplayName());
	}
	
	public static void main(String[] args) throws DatabaseException{
		TBitsReportMailer trm = new TBitsReportMailer();
		ArrayList<String> usersList = new ArrayList<String>();
		trm.insertUsersIntoList(usersList , "[\"ajithnair\",\"patilas\"]");
	}
}
