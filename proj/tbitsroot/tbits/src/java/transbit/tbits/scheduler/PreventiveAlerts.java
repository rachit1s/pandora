package transbit.tbits.scheduler;

import static transbit.tbits.Helper.TBitsConstants.PKG_SCHEDULER;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Hashtable;
import java.util.TimeZone;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Mail;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DateTimeFormat;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.ParameterType;
import transbit.tbits.webapps.WebUtil;


public class PreventiveAlerts implements ITBitsJob {

	// Application logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SCHEDULER);
    public static final String CMD_DISPLAY_NAME = "Preventive Alerts";
  //  public static final String CMD_PARAM_RECIPIENTS = "recipients";
    public static final String CMD_PARAM_ASSIGNEE = "Send alert to assignee" ;
    public static final String CMD_PARAM_LOGGER = "Send alert to logger" ;
    public static final String CMD_PARAM_SUBSCRIBERS = "Send alert to subscribers" ;
    public static final String CMD_PARAM_CCs = "Send alert to CCs" ;
    public static final String CMD_PARAM_SUBJECT = "subject";
    public static final String CMD_PARAM_FROM = "fromAddress";
    public static final String CMD_PARAM_SCHFREQ = "schFreq";
    public static final String CMD_PARAM_ALERT_TIME = "alertTime";
    
//	String recipients = "";
	static String  ALERT_FILE = "web/preventive-alerts.htm";
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		JobDataMap jobData = arg0.getJobDetail().getJobDataMap();
		String alertStr = jobData.getString(CMD_PARAM_ALERT_TIME);
		int alertTime = Integer.parseInt(alertStr);
		String schFreqStr = jobData.getString(CMD_PARAM_SCHFREQ);
		int schFreq = Integer.parseInt(schFreqStr);
		
		boolean alert_assignee = Boolean.parseBoolean(jobData.getString(CMD_PARAM_ASSIGNEE));
		boolean alert_subscribers = Boolean.parseBoolean(jobData.getString(CMD_PARAM_SUBSCRIBERS));
		boolean alert_CCs = Boolean.parseBoolean(jobData.getString(CMD_PARAM_CCs));
		boolean alert_logger = Boolean.parseBoolean(jobData.getString(CMD_PARAM_LOGGER));
		
		String subject = jobData.getString(CMD_PARAM_SUBJECT);
		String fromAddress = jobData.getString(CMD_PARAM_FROM);
		SendAlerts(alertTime, schFreq, alert_logger, alert_assignee, alert_subscribers, alert_CCs, subject, fromAddress);
	}
	
	void SendAlerts(int alertTime,int schFreq, Boolean alert_logger, Boolean alert_assignee, Boolean alert_subscribers, Boolean alert_CCs, String subject, String fromAddress)
	{
		SendAlerts(alertTime, schFreq, alert_logger, alert_assignee, alert_subscribers, alert_CCs, subject, fromAddress, Calendar.getInstance(TimeZone.getTimeZone("GMT")));
	}
	
	void SendAlerts(int alertTime,int schFreq, Boolean alert_logger, Boolean alert_assignee, Boolean alert_subscribers, Boolean alert_CCs, String subject,String fromAddress, Calendar currentDate)
	{
		for(Request req:getUpcomingRequests(alertTime, schFreq, currentDate))
		{
			ArrayList<RequestUser> users = new ArrayList<RequestUser>();
			
			if(true == alert_assignee)
			{
				addUsers(users, req.getAssignees());
			}
			if(true == alert_logger)
			{
				addUsers(users, req.getLoggers());
			}
			if(true == alert_subscribers )
			{
				addUsers(users, req.getSubscribers());
			}
			if(true == alert_CCs )
			{
				addUsers(users, req.getCcs());
			}
			String body;
			try {
				DTagReplacer tp = new DTagReplacer(ALERT_FILE);
				String row = request2TR(req);
				tp.replace("requests", row);
				body = tp.parse(req.getSystemId());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			try {
				SendMailAlert(users, body, subject, fromAddress);
			} catch (AddressException e) {
				LOG.error(e);
			} catch (MessagingException e) {
				LOG.error(e);
			} catch (DatabaseException e) {
				LOG.error(e);
			}
		}
	}
	
	String request2TR(Request request)
	{
		int       requestId = request.getRequestId();
		int sysId = request.getSystemId();
		BusinessArea ba = null;
		try {
			ba = BusinessArea.lookupBySystemId(sysId);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
        String    sysPrefix = ba.getSystemPrefix();
        
		String requestURL = WebUtil.getServletPath("/Q/" + sysPrefix + "/" + requestId);
		
		long diffHours = (request.getDueDate().getTime() - Timestamp.getGMTNow().getTime())/(1000*60*60);
		
		long hours = diffHours % 24;
		long days = diffHours / 24;
		
		String pendingIn = "";
		if(days != 0)
		{
			pendingIn = days + " days ";
		}
		pendingIn += hours + " hours";
		
		 DateTimeFormat dateTimeFormat = DateTimeFormat.lookupByDateTimeFormatId(ba.getSysConfigObject().getEmailDateFormat());

		 String outDate;
		 
         if (dateTimeFormat == null) {
             outDate = Timestamp.toDateMin(request.getDueDate());
         }
         outDate = Timestamp.toCustomFormat(Timestamp.toSiteTimestamp(request.getDueDate()),dateTimeFormat.getFormat());

		String row = String.format("<tr><td>%s</td><td><a href=\"%s\">%d</a></td><td>%s</td><td>%s</td><td>%s</td></tr>", 
				ba.getDisplayName(),  requestURL,request.getRequestId(), request.getSubject(), outDate, pendingIn);
		return row;
	}
	void SendMailAlert(ArrayList<RequestUser> users, String body, String subject, String fromAddress) throws AddressException, MessagingException, DatabaseException
	{
    	MimeMessage message = new MimeMessage(Mail.getSession());
	    message.setFrom(new InternetAddress(fromAddress));
	 
	    for(RequestUser ru:users)
	    {
	    	User u = ru.getUser();
	    	String email = u.getEmail();
	    	if(email.trim().length() > 0)
	    	{
	    		message.addRecipient(Message.RecipientType.TO,
	                         new InternetAddress(email));
	    	}
	    	else
	    	{
	    		LOG.warn("user '" + u.getUserLogin() + "' has blank email id.");
	    	}
	    }
	    
	    message.setSubject(subject);
	    message.setContent(body, "text/html");
	    
	    // Send message
	    Transport.send(message);	
	}
	void addUsers(ArrayList<RequestUser> users, Collection<RequestUser> newUsers)
	{
		for(RequestUser ru:newUsers)
		{
			if(!users.contains(ru))
				users.add(ru);
		}
	}
	
	/**
	 * Generates alerts
	 * @param recipients can be a comma separated list of "assignee,logger, subscribers"
	 * @param alertTime before how long do you need alerts (in minutes) 
	 * @param schdFreq (in minutes) (what is the scheduled frequency? As we are not storeing if the alert has been sent, this is needed.)
	 */
	private ArrayList<Request> getUpcomingRequests(int alertTime, int schdFreq, Calendar currentDate) {
		
		
		// Get the
		Calendar c1 = (Calendar) currentDate.clone();
		c1.add(Calendar.MINUTE, alertTime);

		Calendar c2 = (Calendar) currentDate.clone();
		c2.add(Calendar.MINUTE, alertTime);
		c2.add(Calendar.MINUTE, schdFreq);

		ArrayList<Request> requests = new ArrayList<Request>();
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select r.sys_id sys_id, r.request_id request_id, r.due_datetime due_datetime from requests r "
							+ "join business_areas bas on bas.sys_id = r.sys_id and bas.is_active = 1 "
							+ "join types t on t.field_id = 4 and t.name != 'closed' and r.status_id = t.type_id and t.sys_id = r.sys_id "
							+ "and r.due_datetime is not null and r.due_datetime >= ? and r.due_datetime < ?");
			
			stmt.setTimestamp(1, Timestamp.getTimestamp(c1.getTime()).toGmtTimestamp().toSqlTimestamp());
			stmt.setTimestamp(2, Timestamp.getTimestamp(c2.getTime()).toGmtTimestamp().toSqlTimestamp());
			DateFormat df = SimpleDateFormat.getDateTimeInstance();
			
			LOG.info(String.format("\n\n--\nAlert Duration: %d, Freq: %d, Now: %s", alertTime, schdFreq, df.format(currentDate.getTime())));
			LOG.info(String.format("\nTesting between %s and %s", df.format(c1.getTime()), df.format(c2.getTime())));
			LOG.info(String.format("\nTesting between %d:%d:%d and %d:%d:%d",
					c1.get(Calendar.DATE), c1.get(Calendar.HOUR_OF_DAY), c1.get(Calendar.MINUTE),
					c2.get(Calendar.DATE), c2.get(Calendar.HOUR_OF_DAY), c2.get(Calendar.MINUTE)
					));
			
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				int requestId = resultSet.getInt("request_id");
				int sys_id = resultSet.getInt("sys_id");
				Timestamp dueDate = Timestamp.getTimestamp(resultSet.getTimestamp("due_datetime"));
				LOG.info(String.format("\n sys_id: %d, request: %d, dueby: %s", sys_id, requestId, df.format(dueDate)));
				Request request;
				request = Request.lookupBySystemIdAndRequestId(sys_id,
						requestId);
				requests.add(request);
			}
		
		} catch (DatabaseException e) {
			LOG.error("Error while runing the preventive alerts.",e);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LOG.error("Error while runing the preventive alerts.",e);
		}
		finally
		{
			if(conn != null)
			{
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return requests;
	}
	
	public Hashtable<String, JobParameter> getParameters() throws SQLException{
		Hashtable<String, JobParameter> params = new Hashtable<String, JobParameter>();
		JobParameter param;
	
		/*// NITI : instead added checkboxes
		param = new JobParameter();
		param.setName(CMD_PARAM_RECIPIENTS);
		param.setType(ParameterType.Text);
		params.put(CMD_PARAM_RECIPIENTS, param);
		*/
		param = new JobParameter() ;
		param.setName(CMD_PARAM_LOGGER ) ;
		param.setType(ParameterType.CheckBox) ;
		params.put(CMD_PARAM_LOGGER, param);
		
		param = new JobParameter() ;
		param.setName( CMD_PARAM_ASSIGNEE ) ;
		param.setType(ParameterType.CheckBox) ;
		params.put(CMD_PARAM_ASSIGNEE, param);
		
		param = new JobParameter() ;
		param.setName( CMD_PARAM_SUBSCRIBERS ) ;
		param.setType(ParameterType.CheckBox) ;
		params.put(CMD_PARAM_SUBSCRIBERS, param);
		
		param = new JobParameter() ;
		param.setName( CMD_PARAM_CCs ) ;
		param.setType(ParameterType.CheckBox) ;
		params.put(CMD_PARAM_CCs, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_FROM);
		param.setMandatory(true);
		param.setType(ParameterType.Text);
		params.put(CMD_PARAM_FROM, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_SUBJECT);
		param.setType(ParameterType.Text);
		params.put(CMD_PARAM_SUBJECT, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_SCHFREQ);
		param.setType(ParameterType.Text);
		params.put(CMD_PARAM_SCHFREQ, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_ALERT_TIME);
		param.setType(ParameterType.Text);
		params.put(CMD_PARAM_ALERT_TIME, param);
		
		return params;
	}
    
    public String getDisplayName(){
    	return CMD_DISPLAY_NAME;
    }
    
    public boolean validateParams(Hashtable<String,String> params) 
    	throws IllegalArgumentException
    {
    	if( (null == params.get( CMD_PARAM_ASSIGNEE)) || (null == params.get(CMD_PARAM_SUBSCRIBERS)) 
    		|| (null == params.get(CMD_PARAM_CCs)) || (null == params.get(CMD_PARAM_LOGGER))	
    	  )
    		
    	{
    		throw new IllegalArgumentException( "Select at least one reciever for alert." ) ;    		
    	}
    	
    	if( "false".equalsIgnoreCase( params.get(CMD_PARAM_ASSIGNEE).trim() ) 
    	  && "false".equalsIgnoreCase( params.get(CMD_PARAM_SUBSCRIBERS).trim() )
    	  && "false".equalsIgnoreCase( params.get(CMD_PARAM_CCs).trim() )
    	  && "false".equalsIgnoreCase( params.get(CMD_PARAM_LOGGER).trim() )
    	  )
    	{
    		throw new IllegalArgumentException( "Select at least one reciever for alert." ) ;
    	}
    	
    	if( null == params.get(CMD_PARAM_FROM) || "".equals(params.get(CMD_PARAM_FROM).trim()))
    		throw new IllegalArgumentException( "Illegal Argument in " + CMD_PARAM_FROM + " field.") ;
    	
    	
    	return true;
    }
	
	/*static int duedate = 38;
	static int alertDuration = -5;
	static int freq = 2;
	static void runEvent(int time)
	{
		if( ((duedate  - alertDuration) >= time) && ((duedate - alertDuration) < (time + freq)) )
		{
			System.out.printf("\nExecuting job: Due-Date:%d, Alert Duration: %d, Time: %d, Freq: %d",
					duedate, alertDuration, time, freq);
		}
	}
	public static void main(String [] args)
	{
		System.out.println("Eq: duedate  - alertDuration - time) >= freq");
		
		for(int time=0; time < 100; time+= freq)
		{
			runEvent(time);
		}
	}*/
	
	public static void main(String[] args)
	{
//		PreventiveAlerts pa = new PreventiveAlerts();
//		Calendar currentDate =  Calendar.getInstance(TimeZone.getTimeZone("GMT"));
//			int incrMin = 15;
//		for(int i = 0; i < 50; i++)
//		//while(true)
//		{
//			System.out.print(".");
//			currentDate.add(Calendar.MINUTE, incrMin);
//			pa.SendAlerts(60,15, "a", "The following requests are assigned to you.", "nobody@localhost", currentDate);
//		}
		try {
			Request request = Request.lookupBySystemIdAndRequestId(1, 7);
			BusinessArea ba =BusinessArea.lookupBySystemId(request.getSystemId());
			long diffHours = (request.getDueDate().getTime() - Timestamp.getGMTNow().getTime())/(1000*60*60);
			
			long hours = diffHours % 24;
			long days = diffHours / 24;
			
			String pendingIn = "";
			if(days != 0)
			{
				pendingIn = days + " days ";
			}
			pendingIn += hours + " hours";
			
			 DateTimeFormat dateTimeFormat = DateTimeFormat.lookupByDateTimeFormatId(ba.getSysConfigObject().getEmailDateFormat());

			 String outDate;
			 
	         if (dateTimeFormat == null) {
	             outDate = Timestamp.toDateMin(request.getDueDate());
	         }
	         outDate = Timestamp.toCustomFormat(Timestamp.toSiteTimestamp(request.getDueDate()),dateTimeFormat.getFormat());
	         System.out.println("Pending: " + pendingIn);
	         System.out.println("DueDate: " + outDate);

		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println("done!");
	}
}
