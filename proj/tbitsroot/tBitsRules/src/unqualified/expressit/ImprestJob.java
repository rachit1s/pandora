package expressit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AddRequest;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

public class ImprestJob implements Job {

	 // Application logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_SCHEDULER);
    
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
    	
    	JobDataMap jdm = arg0.getJobDetail().getJobDataMap();
    	String sysPrefix = null;
    	if(jdm != null)
    	{
    		sysPrefix = jdm.getString("sys_prefix");
    		if(sysPrefix == null)
    		{
    			LOG.error("You must specify the business area.");
    			return;
    		}
	    	sysPrefix = sysPrefix.trim();
	    	
	    	String query = jdm.getString("query");
	    	if(query == null)
    		{
    			LOG.error("You must specify the query.");
    			return;
    		}
	    	execute(sysPrefix, query);
    	}
    	else
    	{
    		LOG.error("You must provide the job data map.");
    	}
    	
    }
    
	public void execute(String sysPrefix, String query) {
		LOG.info("Executing " + ImprestJob.class.getName());
		//Load jobs from db
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			if(conn == null)
				throw new SQLException("Connection object is null.");
			PreparedStatement ps = conn.prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				//Add Job
				String logger = rs.getString("logger");
				if(logger != null)
					logger = logger.trim();
				
				String escalateTo = rs.getString("escalate_to");
				if(escalateTo != null)
					escalateTo = escalateTo.trim();
				
				String assignee = rs.getString("assignee");
				if(assignee != null)
					assignee = assignee.trim();
				
				String category = rs.getString("category");
				if(category != null)
					category = category.trim();
				
				String type = rs.getString("type");
				if(type != null)
					type = type.trim();
				
				String description = rs.getString("description");
				if(description != null)
					description = description.trim();
				
				
				int duedate_lag = rs.getInt("duedate_lag");
				
				Calendar cal = Calendar.getInstance();
				int jobId = rs.getInt("job_id");
				
				String subject = rs.getString("subject");
				
				if (subject != null) {
					subject = subject.trim();

					String today = new SimpleDateFormat().format(cal.getTime());
					System.out.println("before replacing the subject is: "
							+ subject);
					subject = subject.replaceAll("\\$date", today);
					System.out.println("after replacing the subject is: "
							+ subject);
				}
				
				cal.add(Calendar.DATE, duedate_lag);
				Date dueDate = cal.getTime();
				try {
					addJob(sysPrefix, subject, logger, assignee, escalateTo, dueDate, category, type, description);
				} catch (APIException e) {
					LOG.error("Unable to log request corresponding to the imprest job id '" + jobId + "'", e);
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			LOG.error("Unable to execute the Query: " + query);
			LOG.error("Unable to get the connection to the database.", e);
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.warn("Unable to close the connection.");
				}
			}
		}
		
		//Add requests
	}
	
	public void addJob(String sysPrefix, String subject, String logger, String assignee, String escalateTo, Date dueDate, String category, String type, String description) throws APIException
	{
		LOG.info("Called Add Job..for sysPrefix :" + sysPrefix);
		BusinessArea ba = null;
		try {
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			if(ba == null)
			{
				LOG.error("The business area corresponding to the prefix '" 
						+ sysPrefix + "' is null. skipping add job.");
				return;
			}
		} catch (DatabaseException e) {
			APIException apiException = new APIException();
			e.printStackTrace();
			apiException.addException(new TBitsException("Unable to retrieve business area from the database."));
			throw apiException;
		}
		if(subject == null)
		{
			subject = "";
		}
		
		LOG.info("Got the ba");
		Hashtable<String, String> fieldValues = new Hashtable<String, String>();
		fieldValues.put(Field.USER, "root");
		if(logger != null)
			fieldValues.put(Field.LOGGER, logger);
		fieldValues.put(Field.BUSINESS_AREA, "" + ba.getSystemId());
		
		fieldValues.put(Field.SUBJECT, subject);
		
		if(assignee != null)
			fieldValues.put(Field.ASSIGNEE, assignee);
		
		if(escalateTo != null)
			fieldValues.put("escalate_to", escalateTo);
		
		if(type != null)
			fieldValues.put(Field.REQUEST_TYPE, type);
		if(category != null)
			fieldValues.put(Field.CATEGORY, category);
		
		if(description != null)
			fieldValues.put(Field.DESCRIPTION, description);
		
		DateFormat df = new SimpleDateFormat(APIUtil.API_DATE_FORMAT);
		fieldValues.put(Field.DUE_DATE, df.format(dueDate));
		LOG.info("Fields are: " + fieldValues);
		
		AddRequest addRequest = new AddRequest();
		addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
		LOG.info("Adding the request");
		addRequest.addRequest(fieldValues);
		LOG.info("Finished executing addrequest on " + fieldValues);
	}
	
	public static void main(String[] args) {
		ImprestJob job = new ImprestJob();
		job.execute("imptbits123", "select * from expressit_imprest");
	}
}
