package transbit.tbits.scheduler;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.Escalation.EscalationUtils;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.PluginManager;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.ParameterType;

public class SeverityBasedEscalation implements ITBitsJob {

	public static final TBitsLogger LOG = TBitsLogger
			.getLogger(TBitsConstants.PKG_SCHEDULER);
	public static final String CMD_PARAM_USER = "user";
	public static final String CMD_PARAM_DESCRIPTION = "description";
	public static final String CMD_DISPLAY_NAME = "Severity Based Escalation";

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// Get the JobDetail object.
		JobDetail jd = arg0.getJobDetail();

		// Read the properties of the Job from the JobDetail object.
		JobDataMap jdm = jd.getJobDataMap();

		Hashtable<String, String> updatefieldValues = new Hashtable<String, String>();
		String keysList[] = jdm.getKeys();

		for (String key : keysList) {

			updatefieldValues.put(key, jdm.getString(key));
		}
		Escalate(updatefieldValues);
	}
	/**
	 * Gets the next escalation time for a request.
	 * @param request
	 * @return the span or -1 if escalation time for the request is not found.
	 * @throws TBitsException
	 */
	public static int getEscalationSpan(Request request) throws TBitsException
	{
		if(request == null)
			throw new IllegalArgumentException("Request should not be null");
		Connection aCon = null;
		try {
			aCon = DataSourcePool.getConnection();
			CallableStatement cs = aCon.prepareCall("stp_get_escalation_span ?, ?, ?, ?, ?");
			cs.setInt(1, request.getSystemId());
			cs.setInt(2, request.getCategoryId().getTypeId());
			cs.setInt(3, request.getStatusId().getTypeId());
			cs.setInt(4, request.getSeverityId().getTypeId());
			cs.setInt(5, request.getRequestTypeId().getTypeId());
			ResultSet rs = cs.executeQuery();
			if(rs.next())
			{
				return rs.getInt("span");
			}
		} catch (SQLException e) {
			throw new TBitsException("Unable to get the next escalation time. " +
					"please ensure that the tables related to escalation exist in DB", e);
		}
		finally
		{
			try {
				if(aCon != null)
				aCon.close();
			} catch (SQLException e) {
				LOG.warn("SeverityEscalation: Unable to close connection after getting next escalation.", e);
			}
		}
		LOG.debug("Could not find the span corresponding to the sys_id '" 
				+ request.getSystemId() 
				+ "' status_id '" + request.getStatusId().getTypeId()
				+ "' category_id '" + request.getCategoryId().getTypeId()
				+ "' severity_id '" + request.getSeverityId().getTypeId()
				+ "' type_id '" + request.getRequestTypeId().getTypeId() + "'");
				
		return -1;
	}

	/**
	 *  1. Check if the request needs to be escalated and who are the new assignees
	 *  2. Get the new due date.
	 *  3. Update the request with the new values (Assignees, Description, Due Date and values passes in job).
	 * @param updatefieldValues
	 */
	public synchronized void Escalate(Hashtable<String, String> updatefieldValues) {
		Connection aCon = null;
		try {
			aCon = DataSourcePool.getConnection();

			aCon.setAutoCommit(false);

			CallableStatement cs = aCon.prepareCall("stp_severity_escalation");
			ResultSet rs = cs.executeQuery();
			Hashtable<String, EscResult> results = new Hashtable<String, EscResult>();
			while (rs.next()) {
				int systemId = rs.getInt("sys_id");
				int requestId = rs.getInt("request_id");
				String assignee = rs.getString("new_assignee");
				String key = Integer.toString(systemId) + "-"
						+ Integer.toString(requestId);
				EscResult escResult;
				if (results.containsKey(key)) {
					escResult = results.get(key);
					boolean exists = false;
					boolean isFirst = true;
					String newStr = escResult.newAssignee;
					for (String str : newStr.split(",")) {
						if (isFirst)
							isFirst = true;
						if (str.trim() == assignee) {
							exists = true;
							break;
						}
					}
					if (!exists) {
						if (escResult.newAssignee.length() > 0)
							escResult.newAssignee += ",";
						escResult.newAssignee += assignee;
						results.put(key, escResult);
					}
				} else {
					escResult = new EscResult();
					escResult.sysId = systemId;
					escResult.requestId = requestId;
					escResult.newAssignee = assignee;
					results.put(key, escResult);
				}
			}
			

			for (String key : results.keySet()) {
				EscResult result = results.get(key);
				Request request = Request.lookupBySystemIdAndRequestId(
						result.sysId, result.requestId);
				ArrayList<RequestUser> currentAssgnees = new ArrayList<RequestUser>() ;
				if( null != request.getAssignees()) 
					currentAssgnees.addAll(request.getAssignees());
				StringBuilder assignees = new StringBuilder();
				for (RequestUser ru : currentAssgnees) {
					assignees.append(ru.getUser().getUserLogin()).append(",");
				}
				assignees.append(result.newAssignee);
				updatefieldValues.put(Field.ASSIGNEE, assignees.toString());
				
				int span;
				try {
					span = getEscalationSpan(request);
					if(span == -1)
					{
						continue;
					}
					
					Date newDueDate = EscalationUtils.getNextDueDate(span);
					if (newDueDate != null) {
						SimpleDateFormat sdf = new SimpleDateFormat(
								APIUtil.API_DATE_FORMAT);
						String newDuedate = sdf.format(newDueDate);

						System.out.println("New Due Date: " + newDuedate);
						updatefieldValues.put(Field.DUE_DATE, newDuedate);
					}
					updatefieldValues.put(Field.BUSINESS_AREA, Integer
							.toString(result.sysId));
					updatefieldValues.put(Field.REQUEST, Integer
							.toString(result.requestId));
					
					LOG.info("Escalating: " + result.sysId + ":" + result.requestId
							+ " to " + assignees);
					//Call the plugins to update the updateFieldValues
					RuleResult ruleResult = new RuleResult(false, "Exception in execution of handlers.", false);
					try{
						ruleResult = executeHandlers(updatefieldValues, request);
					}catch(Exception e)
					{
						e.printStackTrace();
					}
						
					if(ruleResult.isSuccessful())
						LOG.info("Execution of escalation handlers was successfull. " 
								+ ruleResult.getMessage());
					else
						LOG.error("Execution of escalation handlers was unsuccessfull. " 
								+ ruleResult.getMessage());
					if(ruleResult.canContinue())
					{
						updateRequest(updatefieldValues);
						updateLastEscalationTime(result.sysId, result.requestId, new Date());
					}
				} catch (TBitsException e) {
					LOG.error("Unable to get the span for sys_id: " 
							+ request.getSystemId() 
							+ " and request_id: " + request.getRequestId());
				}
			}
			aCon.commit() ;
		} catch (DatabaseException e) {
			try {
        		if(aCon != null)
					aCon.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			LOG.error("Database exception occurred. " + e.getMessage(), e);
		} catch (SQLException e) {
			try {
        		if(aCon != null)
					aCon.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			LOG.error("Database exception occurred. " + e.getMessage(), e);
		}
		finally
		{
			try {
				if(aCon != null)
				aCon.close();
			} catch (SQLException e) {
				LOG.warn("SeverityEscalation: Unable to close connection.", e);
			}
		}
	}

	private RuleResult executeHandlers(
			Hashtable<String, String> updatefieldValues, Request request) {
		PluginManager pm = PluginManager.getInstance();
		ArrayList<Class> handlers = pm.findPluginsByInterface(EscalationHandler.class.getName());
		RuleResult aggregate = new RuleResult(true, "", true);
		for(Class handler:handlers)
		{
			try {
				EscalationHandler eh = (EscalationHandler) handler.newInstance();
				RuleResult result = eh.beforeEsclation(updatefieldValues, request);
				if(!result.canContinue())
				{
					break;
				}
				aggregate.setMessage(aggregate.getMessage() + "." + result.getMessage());
				aggregate.setSuccessful(aggregate.isSuccessful() && result.isSuccessful());
			} catch (InstantiationException e) {
				LOG.error("Unable to instantiate Escalation handler: '" 
						+ handler.getName() + "'", e);
			} catch (IllegalAccessException e) {
				LOG.error("Unable to instantiate Escalation handler: '" 
						+ handler.getName() + "' due to illegal access.", e);
			}
			
		}
		return aggregate;
	}
	private void updateLastEscalationTime(int sysId, int requestId, Date datetimeValue) throws SQLException {
		Connection aCon = null;
		
		try
		{
			aCon = DataSourcePool.getConnection();
			CallableStatement cs = aCon
					.prepareCall("stp_update_last_escalation_time ?, ?, ?");
			cs.setInt(1, sysId);
			cs.setInt(2, requestId);
			cs.setTimestamp(3, new java.sql.Timestamp(datetimeValue.getTime()));
			cs.executeUpdate();
		}
		finally
		{
			if( null != aCon )
				aCon.close();
		}
	}

	private void updateRequest(Hashtable<String, String> updateFieldValues) {
		UpdateRequest app = new UpdateRequest();
		app.setSource(TBitsConstants.SOURCE_CMDLINE);
		try {
			Request updatedRequest = app.updateRequest(updateFieldValues);

			LOG.info("Request Id: " + updatedRequest.getRequestId());
			LOG.info("Action  Id: " + updatedRequest.getMaxActionId());
			// System.out.println(updatedRequest.getDescription());
		}

		catch (APIException api) {
			LOG.error("",(api));
			return;
		} catch (Exception e) {
			LOG.warn("",(e));
			return;
		}
	}

	public static void testEscalate() {
		Hashtable<String, String> updatefieldValues = new Hashtable<String, String>();

		updatefieldValues.put(Field.DESCRIPTION,
				"[The request is being escalated.]");
		updatefieldValues.put(Field.USER, "1");
		SeverityBasedEscalation sch = new SeverityBasedEscalation();
		sch.Escalate(updatefieldValues);
	}
	
	public Hashtable<String, JobParameter> getParameters() throws SQLException{
		Hashtable<String, JobParameter> params = new Hashtable<String, JobParameter>();
		JobParameter param;
		
		param = new JobParameter();
		param.setName(CMD_PARAM_USER);
		param.setType(ParameterType.Text);
		param.setMandatory(true);
		params.put(CMD_PARAM_USER, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_DESCRIPTION);
		param.setType(ParameterType.TextArea);
		params.put(CMD_PARAM_DESCRIPTION, param);
		
		return params;
	}
    
    public String getDisplayName(){
    	return CMD_DISPLAY_NAME;
    }
    
    public boolean validateParams(Hashtable<String,String> params) 
    	throws IllegalArgumentException{
    	if(null == params.get(CMD_PARAM_USER)|| "".equals(params.get(CMD_PARAM_USER).trim()) ){
    		throw new IllegalArgumentException("Illegal Argument in " + CMD_PARAM_USER + " field.");
    	}
    	String userStr = params.get(CMD_PARAM_USER) ;
    	User user;
		try {
			user = User.lookupAllByUserLogin( userStr );
		} catch (DatabaseException e) {
 			
			e.printStackTrace();
			throw new IllegalArgumentException( "Database Exception occured! Please try again.");
		}
    	if( null == user )
    		throw new IllegalArgumentException( "The user: " + userStr + " does not exist." ) ;
    	
    	return true;
    }

	public static void main(String arg[]) {

		if (arg.length != 1) {
			System.err.println("Usage: UpdateRequest <Comma-separated list of "
					+ "colon separated field-value pairs.>");
			return;
		}

		StringTokenizer st = new StringTokenizer(arg[0], ",");
		Hashtable<String, String> paramTable = new Hashtable<String, String>();

		while (st.hasMoreTokens()) {
			String str = st.nextToken();
			StringTokenizer ist = new StringTokenizer(str, ":=");

			try {
				String key = ist.nextToken().trim();
				String val = ist.nextToken().trim();

				paramTable.put(key, val);
			} catch (Exception e) {
				LOG.warn("",(e));
			}
		}
		if (paramTable.get(Field.USER) == null) {
			String user = System.getProperties().getProperty("user.name");
			LOG.warn("User not supplied. Assuming '" + user + "'.");
			paramTable.put(Field.USER, user);
		}

		SeverityBasedEscalation sch = new SeverityBasedEscalation();
		sch.Escalate(paramTable);
	}

	public class EscResult {
		public int sysId;
		public int requestId;
		public String newAssignee;
	}
}
