package lntEscalation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.Escalation.EscalationUtils;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.HolidaysList;
import transbit.tbits.domain.IHolidayCalender;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.PluginManager;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.ParameterType;
import transbit.tbits.searcher.DqlSearcher;

public class LnTEscalationJob implements ITBitsJob
{
	public static final TBitsLogger LOG = TBitsLogger.getLogger("LnTEscalationJob");
	
	public static final String BaList = "BaList";
	public static final String Dql= "SingleDqlForAllBa" ;
	public static final String DateFieldNameList = "DateFieldNameList" ;
	public static final String EscalatedFromUserTypeFieldNameList = "EscalatedFromUserTypeFieldNameList" ;
	public static final String EscalatedToUserTypeFieldNameList = "EscalatedToUserTypeFieldNameList" ;
	public static final String DefaultUserLoginList = "DefaultUserLoginList" ;
	public static final String SpanList = "SpanList";
	public static final String CalendarNameList = "CalendarNameList" ;
	public static final String SuperUserLoginList = "SuperUserLoginList" ;

	private static final String BaListMsg = "Please provide a comma separate list of BA in variable with name :" + BaList;

	private static final String EmptyBaList = "BaList cannot be empty." + BaListMsg;

	private static final String DateFieldListMsg = "Please provide a comma separate list of Date type field names. One correspondeing to each BA in variable with name :" + DateFieldNameList;

	private static final String EmptyDateField = "DateFieldNameList cannot be empty." + DateFieldListMsg;

	private static final String UserTypeListMsg = "Please provide a comma separate list of User type field names. One correspondeing to each BA in variable with name :" + EscalatedToUserTypeFieldNameList;

	private static final String EmptyUserField = "UserTypeFieldNameList cannot be empty." + UserTypeListMsg;

	private static final String EscFromUserTypeListMsg = "Please provide a comma separate list of User type field names where the new user needs to be added. One correspondeing to each BA in variable with name :" + EscalatedFromUserTypeFieldNameList;

	private static final String EmptyEscFromUserField = EscalatedFromUserTypeFieldNameList + " cannot be empty." + EscFromUserTypeListMsg;
	
	private static final String DefaultLoginMsg = "Please provide a comma separate list of logins of default escalated-to-users. One correspondeing to each BA in variable with name :" + DefaultUserLoginList;

	private static final String EmptyDefaultLogin = "DefaultUserLoginList cannot be empty." + DefaultLoginMsg;
	
	private static final String SpanListMsg = "Please provide a comma separate list of integers representing no. of days for span. One correspondeing to each BA in variable with name :" + SpanList;

	private static final String EmptySpanList = "SpanList cannot be empty." + SpanListMsg;

	private static final String CalendarNameMsg = "Please provide a comma separate list of Calendar name to follow present in holidays_list (column_name ='office') table. One correspondeing to each BA in variable with name :" + CalendarNameList;

	private static final String EmptyCalendarName = "CalendarClassList cannot be empty." + CalendarNameMsg;
	
	private static final String DqlMsg = "Please provide a single DQL for all the BAs in variable with name :" + Dql;

	private static final String EmptyDql = "Dql cannot be empty." + DqlMsg;
	
	private static final String SuperUserMsg = "Please provide a comma separate list of user-logins on whose behalf the request has to be updated. One correspondeing to each BA in variable with name :" + SuperUserLoginList + ". Note that these users must have permission to change the all fields in this BA";

	private static final String EmptySuperUser = "SuperUserLoginList cannot be empty." + SuperUserMsg;
	
	public String getDisplayName() {
		return "LnTExcalationJob";
	}

	public Hashtable<String, JobParameter> getParameters() throws SQLException {
		Hashtable<String,JobParameter> params = new Hashtable<String,JobParameter>() ;
		
		JobParameter jp = new JobParameter();
		jp.setType(ParameterType.Text);
		jp.setName(BaList);
		jp.setMandatory(true);		
		params.put(BaList, jp);
		
		jp = new JobParameter();
		jp.setType(ParameterType.Text);
		jp.setName(Dql);
		jp.setMandatory(true);		
		params.put(Dql, jp);
		
		jp = new JobParameter();
		jp.setType(ParameterType.Text);
		jp.setName(DateFieldNameList);
		jp.setMandatory(true);		
		params.put(DateFieldNameList, jp);
		
		jp = new JobParameter();
		jp.setType(ParameterType.Text);
		jp.setName(EscalatedToUserTypeFieldNameList);
		jp.setMandatory(true);		
		params.put(EscalatedToUserTypeFieldNameList, jp);
		
		jp = new JobParameter();
		jp.setType(ParameterType.Text);
		jp.setName(DefaultUserLoginList);
		jp.setMandatory(true);		
		params.put(DefaultUserLoginList, jp);
		
		jp = new JobParameter();
		jp.setType(ParameterType.Text);
		jp.setName(SpanList);
		jp.setMandatory(true);		
		params.put(SpanList, jp);
		
		jp = new JobParameter();
		jp.setType(ParameterType.Text);
		jp.setName(CalendarNameList);
		jp.setMandatory(true);		
		params.put(CalendarNameList, jp);
		
		jp = new JobParameter();
		jp.setType(ParameterType.Text);
		jp.setName(SuperUserLoginList);
		jp.setMandatory(true);		
		params.put(SuperUserLoginList, jp);
		
		return params;
	}

	private boolean illegal(String msg)
	{
		LOG.info(msg);
		throw new IllegalArgumentException(msg);
	}
	
	public boolean validateParams(Hashtable<String, String> params) throws IllegalArgumentException 
	{
		String baList = params.get(BaList);
		String dql = params.get(Dql);
		String dateFieldList = params.get(DateFieldNameList);
		String userTypeFieldList = params.get(EscalatedToUserTypeFieldNameList);
		String escalatedFromUserTypeFieldList = params.get(EscalatedFromUserTypeFieldNameList);
		String spanList = params.get(SpanList);
		String defaultUserList = params.get(DefaultUserLoginList);
		String calendarNameList = params.get(CalendarNameList);
		String superUserLoginList = params.get(SuperUserLoginList);
		
		if( null == baList )
		{
			illegal(EmptyBaList);
		}
		
		String [] baArray = null;		
		String [] dfArray = null ;
		String [] utArray = null ;
		String [] eutArray = null;
		String [] spanArray = null ;
		String [] duArray = null ;
		String [] ccArray = null ;
		String [] suArray = null ;
		
		if( null == dql)
		{
			illegal(EmptyDql);
		}
		
		if( null == dateFieldList)
		{
			illegal(EmptyDateField);
		}		
		
		if( null == userTypeFieldList )
		{
			illegal(EmptyUserField);
		}
		
		if( null == escalatedFromUserTypeFieldList )
		{
			illegal(EmptyEscFromUserField);
		}
		
		if( null == spanList )
		{
			illegal(EmptySpanList);
		}
		
		if( null == defaultUserList )
		{
			illegal(EmptyDefaultLogin);
		}
		
		if( null == calendarNameList )
		{
			illegal(EmptyCalendarName);
		}
		
		if( null == superUserLoginList )
		{
			illegal(EmptySuperUser);
		}
		
		baArray = baList.split(",");
		if( baArray == null || baArray.length == 0)
		{
			illegal(EmptyBaList);
		}
		
		dfArray = dateFieldList.split(",");
		if( dfArray == null || dfArray.length != baArray.length )
		{
			illegal(DateFieldListMsg);
		}
		
		utArray = userTypeFieldList.split(",");
		if( utArray == null || utArray.length != baArray.length )
		{
			illegal(UserTypeListMsg);
		}

		eutArray = escalatedFromUserTypeFieldList.split(",");
		if( eutArray == null || eutArray.length != baArray.length )
		{
			illegal(EscFromUserTypeListMsg);
		}
		
		spanArray = spanList.split(",");
		if( spanArray == null || spanArray.length != baArray.length )
		{
			illegal(SpanListMsg);
		}
		
		duArray = defaultUserList.split(",");
		if( duArray == null || duArray.length != baArray.length )
		{
			illegal(DefaultLoginMsg);
		}
		
		ccArray = calendarNameList.split(",");
		if( ccArray == null || ccArray.length != baArray.length)
		{
			illegal(CalendarNameMsg);			
		}
		
		suArray = superUserLoginList.split(",");
		if( suArray == null || suArray.length != baArray.length)
		{
			illegal(SuperUserMsg);
		}
		
		for( int i = 0 ; i < baArray.length ; i++ )
		{
			String sp = baArray[i];
			String sysPrefix = sp.trim() ;
			BusinessArea ba = null ;
			try {
				ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			if( null == ba )
			{
				illegal( "BusinessArea with sysPrefix : " + sysPrefix + " does not exists.");				
			}
		
			String df = dfArray[i].trim();
			
			Field f = null;
			try {
				f = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), df);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			
			if( null == f )
			{
				illegal( "Field with name='" + df + "' not found in businessArea with sysPrefix='" + ba.getSystemPrefix() + "'");
			}

			if( f.getDataTypeId() != DataType.DATETIME)
			{
				illegal("Field '" + df + "' of businessArea '" + ba.getSystemPrefix() + "' is not of DataType Date-Time." );
			}
			
			String ut = utArray[i].trim();
			
			Field utf = null;
			
			try {
				utf = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), ut);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			
			if( null == utf )
			{
				illegal( "Field with name='" + utf + "' not found in businessArea with sysPrefix='" + ba.getSystemPrefix()+"'");
			}
			
			if( utf.getDataTypeId() != DataType.USERTYPE )
			{
				illegal("Field '" + ut + "' of businessArea '" + ba.getSystemPrefix() + "' is not of DataType UserType." );
			}
			
			String eut = eutArray[i].trim();
			
			Field eutf = null;
			
			try {
				eutf = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), eut);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			
			if( null == eutf )
			{
				illegal( "Field with name='" + eutf + "' not found in businessArea with sysPrefix='" + ba.getSystemPrefix()+"'");
			}
			
			if( eutf.getDataTypeId() != DataType.USERTYPE )
			{
				illegal("Field '" + eut + "' of businessArea '" + ba.getSystemPrefix() + "' is not of DataType UserType." );
			}
			
			String du = duArray[i].trim();
			User duu = null ;
			try {
				duu = User.lookupByUserLogin(du);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			
			if( duu == null )
			{
				illegal("Default User with login='" + du +"' does not exists.");
			}
			
			// this has been changed to calendar name directly instead of IHolidayCalendar class.
//			String ccl = ccArray[i].trim();
//			Object obj = null ;
//			try {
//				obj = getHolidayCalendarInstance(ccl);
//			} catch (Exception e) {
//				e.printStackTrace();
//				illegal(e.getMessage());
//			}
//			
//			if( obj == null )
//			{
//				illegal("The calendar class with fully qualified name='" + "' not found for the ba='" + ba.getSystemPrefix() + "'" );
//			}
			
			String su = suArray[i].trim();
			User suu = null ;
			
			try {
				suu = User.lookupByUserLogin(su);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			
			if( null == suu )
			{
				illegal("Super-User with login='" + su + "' does not exists for ba='" + ba.getSystemPrefix() +"'");
			}
		}
		
		return true;
	}

	public void execute(JobExecutionContext jec) throws JobExecutionException 
	{
		String baList = null ;
		String dql = null; 
		String dateFieldNameList = null ;
		String userTypeFieldNameList = null;
		String escalatedFromUserTypeFieldNameList = null;
		String spanList = null;
		String defaultUserLoginList = null; 
		String calendarNameList = null ;
		String superUserLoginList = null;
		
		JobDetail jd = jec.getJobDetail() ;
		JobDataMap jdm = jd.getJobDataMap() ;
		
		baList = jdm.getString(BaList);
		
		dql = jdm.getString(Dql);
		
		dateFieldNameList = jdm.getString(DateFieldNameList);
		
		userTypeFieldNameList = jdm.getString(EscalatedToUserTypeFieldNameList);
		
		escalatedFromUserTypeFieldNameList = jdm.getString(EscalatedFromUserTypeFieldNameList);
		
		spanList = jdm.getString(SpanList);
		
		defaultUserLoginList = jdm.getString(DefaultUserLoginList);
		
		calendarNameList = jdm.getString(CalendarNameList);
		
		superUserLoginList = jdm.getString(SuperUserLoginList);
		
		process(baList,dql,dateFieldNameList,escalatedFromUserTypeFieldNameList,userTypeFieldNameList,spanList,defaultUserLoginList,calendarNameList,superUserLoginList) ;
}
/*
 
 Inputs 

0 : comma separated list of ba's to be applicable

1. comma separated dqls (to select requests to escalate)
includes the due-date criterion and any other fields that needs
to be checked corresponding to each of the above ba.

2. comma separated list of date-field-name to increase and set

2.5 comma separated list of span corresponding to each above ba. (span in days ? or minutes ? )

3. comma separated user-type-field-name to include the next assignee

4. comma separated default user to be put in above user-type

5. comma separated  IHolidayInstance (class) to be used as Calendar implementation.

Other : 

1. Heirarchy to be taken from tbits. for this ba.

 
	Algo

	for each i in ba[]
	do
		dql = dql[i]
		dfn = date-field-name[i]
		span = span[i]
		utfn = user-type-field-name[i]
		dfltUser = defaultUser[i]
		holidayInstance = new HolidayInstance[i]
	
		Request [] reqs = Searcher.search( ba[i], dql[i] )

		for each r in reqs[]
		do
			newDate = escalatedDate( dfn,span,holidayInstance)
			r.setDate(dfn,newDate);
			
			myAssignee = r.getAssignee();
			if( null != myAssignee ) 
				supervisor = tbits.getSuperviser(myAssignee);
			
			if( null == supervisor )
				supervisor = dfltUser ;
			
			r.setUserType(supervisor)
		
			udpateRequest(r);
		done
	done
	*/

	public void process(String baList, String dql, String dateFieldNameList, String escalatedFromUserTypeFieldNameList, String userTypeFieldNameList, String spanList, String defaultUserLoginList, String calendarNameList, String superUserLoginList) {
		
		disAllowNull( baList, BaList);
		disAllowNull( dql, Dql);
		disAllowNull( dateFieldNameList, DateFieldNameList);
		disAllowNull( escalatedFromUserTypeFieldNameList, EscalatedFromUserTypeFieldNameList);
		disAllowNull( userTypeFieldNameList, EscalatedToUserTypeFieldNameList);
		disAllowNull( spanList, SpanList);
		disAllowNull( defaultUserLoginList, DefaultUserLoginList);
		disAllowNull( calendarNameList, CalendarNameList);
		disAllowNull(superUserLoginList, SuperUserLoginList);
		
		Hashtable<String,String> params = new Hashtable<String,String>() ;
		
		params.put(BaList, baList);
		params.put(Dql,dql);
		params.put(DateFieldNameList, dateFieldNameList);
		params.put(EscalatedToUserTypeFieldNameList, userTypeFieldNameList);
		params.put(EscalatedFromUserTypeFieldNameList, escalatedFromUserTypeFieldNameList);
		params.put(SpanList, spanList);
		params.put(DefaultUserLoginList, defaultUserLoginList);
		params.put(CalendarNameList, calendarNameList);
		params.put(SuperUserLoginList, superUserLoginList);
		
		if( !validateParams(params) )
		{
			LOG.severe("Job Parameters for Job class =  " + this.getClass().getName() + " : job name = " + this.getDisplayName() + " : could not be validated. Hence NOT executing the job.");
			return; 			
		}
		
		String[] sysPrefixArray = baList.split(",");
		String[] datefnArray = dateFieldNameList.split(",");
		String[] userTypefnArray = userTypeFieldNameList.split(",");
		String[] escUserTypefnArray = escalatedFromUserTypeFieldNameList.split(",");
		String[] spanArray = spanList.split(",");
		String[] userLoginArray = defaultUserLoginList.split(",");
		String[] calendarNameArray = calendarNameList.split(",");
		String[] superUserArray = superUserLoginList.split(","); 
	
		for( int i = 0 ; i < sysPrefixArray.length ; i++ )
		{
			try
			{
			String sp = sysPrefixArray[i].trim() ;
			String dfn = datefnArray[i].trim() ;
			String utfn = userTypefnArray[i].trim() ;
			String eutfn = escUserTypefnArray[i].trim() ;
			String defaultLogin = userLoginArray[i].trim() ;
			String span = spanArray[i].trim() ;
			String calName = calendarNameArray[i].trim() ;
			String superUserLogin = superUserArray[i].trim() ;
			
			User superUser = null;
			try {
				superUser = User.lookupByUserLogin(superUserLogin);
			} catch (DatabaseException e1) {
				e1.printStackTrace();
			}

			if( null == superUser )
			{
				LOG.severe("Cannot find the user with login : " + superUserLogin + ".\nAborting the Escalations.");
				return;
			}

			//span
			Integer spanInt = null ;
			try
			{
				spanInt = Integer.parseInt(span);
			}
			catch(Exception e)
			{
				e.printStackTrace() ;
			}

			if( null == spanInt )
			{
				LOG.severe("The span was null. Hence not continuing with the Escalation for the ba : " + sp);
				continue;
			}
			
			BusinessArea ba = null;
			try 
			{
				ba = BusinessArea.lookupBySystemPrefix(sp);
			} catch (DatabaseException e) 
			{
				e.printStackTrace();
			}
			
			if( null == ba )
			{
				LOG.severe("Cannot find the BusinessArea with sysprefix : " + sp + ". So skipping the Escalations on this BusinessArea.");
				return ;
			}
			
			DqlSearcher searcher = new DqlSearcher(ba.getSystemId(),superUser.getUserId(),dql);
			try {
				searcher.search() ;
			} catch (Exception e1) {
				e1.printStackTrace();
				LOG.severe("Exception occurred while searching. The Escalations might not work correctly.");
			}

			ArrayList<Integer> reqIds = new ArrayList<Integer>();
			if(searcher.getResult().containsKey(ba.getSystemId())){
				Collection<Integer> requestIdsFetchedColl = searcher.getResult().get(ba.getSystemId()).keySet();
				if(requestIdsFetchedColl != null){
					reqIds.addAll(requestIdsFetchedColl);
				}
			}
			
			LOG.info("Following requests will be Escalated for ba=(" + ba.getSystemPrefix() + ") and dql=(" + dql +")" + " : " + reqIds) ;
			
			ArrayList<Request> requestList = null ;
			try {
				requestList = Request.lookupBySystemIdAndRequestIdList(ba.getSystemId(), reqIds);
			} catch (DatabaseException e) 
			{
				LOG.info("Exception while retrieving requests.");
				e.printStackTrace();
			}
			
			if( null != requestList )
			{
				for( Request req : requestList )
				{
					try
					{
					LOG.info("Strating to escalate request : " + ba.getSystemPrefix() + "#" + req.getRequestId() + "#" + req.getMaxActionId());
					
					if( null == req )
					{
						LOG.info("The returned request was null.");
						continue;
					}
					
				
					Collection<RequestUser> asses = (Collection<RequestUser>) req.getObject(eutfn);
					HashSet<String> userSet = new HashSet<String>() ;
					
					Collection<RequestUser> escUser = (Collection<RequestUser>) req.getObject(utfn);
					// add all the existing users in userList
					if( null != escUser )
					{
						for( RequestUser ru : escUser )
						{
							User user = null ;
							
							try {
								user = User.lookupByUserId(ru.getUserId());
							} catch (DatabaseException e) 
							{
								e.printStackTrace();
							}
							
							// it the user is inactive now then it will be removed from this
							// list
							if( null != user ) 
							{
								userSet.add(user.getUserLogin());
							}
						}
					}
					
					// if there is any assignee for this request
					if( null != asses && asses.size() != 0 )
					{		
						// iterate for all assignees
						for( RequestUser ru : asses)
						{
							User user = null;
							try {
								user = User.lookupByUserId(ru.getUserId());
							} catch (DatabaseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							if( null != user )
							{
								// add the assignee into the list
								userSet.add(user.getUserLogin());
								
								ArrayList<String> parents = null ;
								try 
								{
									// get his/her parents
									 parents = EscalationUtils.getParentUsers(ba.getSystemId(), user.getUserId());
								} catch (TBitsException e) {
									e.printStackTrace();
								}
								
								// if parents exists
								if( null != parents )
								{
									// removing the parents which are not active
									for( Iterator<String> iter = parents.iterator() ; iter.hasNext() ; )
									{
										String l = iter.next() ;
										User p = null;
										try {
											p = User.lookupByUserLogin(l);
										} catch (DatabaseException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										if( null == p )
											iter.remove() ;
									}
								
									// add all parents into userlist
									userSet.addAll(parents);
								}		
							}						
						}					
					}
					else // there is no assignee for this request.
					{
						// take the given default parent user
						User defaultUser = null ;
						
						try {
							defaultUser = User.lookupByUserLogin(defaultLogin);
						} catch (DatabaseException e) {
							e.printStackTrace();
						}
						
						if( null != defaultUser )
						{
							userSet.add(defaultLogin);
						}
						else
						{
							LOG.severe("The default user provided : " + defaultLogin + " does not exists or is not active anymore.");
						}
					}
					
					Field f = null ;
					try {
						f = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), dfn);
					} catch (DatabaseException e1) {
						e1.printStackTrace();
					}
					if( null == f )
					{
						LOG.severe("Cannot find the date field to escalate for the ba : " + ba.getSystemPrefix() + " and request_id : " + req.getRequestId() + ".\nSo this request will not be escalated.");
						continue ;
					}
					
					Date currDate = (Date) req.getObject(f);
					
					if( null == currDate )
					{
						LOG.severe("The date in the field (" + f.getName() + ") was null" + " for the request " + ba.getSystemPrefix() + "#" + req.getRequestId() + "#" + req.getMaxActionId() +". Hence not escalating this request.");
						continue;
					}
					
					Date escalationDate = null;
					try {
						escalationDate = getEscalationDate(currDate,spanInt,calName);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					if( null == escalationDate )
					{
						LOG.severe("The escalation date was found to be null. Hence not escalating the request : " + ba.getSystemPrefix() + "#" + req.getRequestId() + "#" + req.getMaxActionId());
						continue;
					}
					
					String escalationDateStr = Timestamp.toCustomFormat(escalationDate,TBitsConstants.API_DATE_FORMAT);
					
					Hashtable<String,String> allParams = new Hashtable<String,String>() ;

					ArrayList<Field> fields = null;
					try 
					{
						fields = Field.lookupBySystemId(ba.getSystemId());
					} catch (DatabaseException e) 
					{
						e.printStackTrace();
					}
					
					if( null == fields )
					{
						LOG.info("No fields where found for the ba so carry over during Escalation might not work for ba : " + ba.getSystemPrefix() );						
					}
					else
					{
						for( Field field : fields )
						{
							String value = req.get(field.getName());
							if( null != value )
								allParams.put(field.getName(), value);
						}
					}
					
					allParams.put(Field.BUSINESS_AREA, ba.getSystemId()+"");
					allParams.remove(Field.APPEND_INTERFACE);
					allParams.remove(Field.REPLIED_TO_ACTION);
					allParams.put(Field.USER,superUser.getUserLogin());
					allParams.put(Field.REQUEST, req.getRequestId()+"");
					allParams.remove(Field.MEMO);
					allParams.remove(Field.ACTION);
					allParams.remove(Field.MAX_ACTION_ID);
					allParams.put(Field.NOTIFY,"false");
					allParams.put(Field.NOTIFY_LOGGERS, "false");
					allParams.put(dfn, escalationDateStr);					
					String newAssList = getCommaSeparated(userSet);
					allParams.put(utfn, newAssList);
					
					Calendar cal = Calendar.getInstance() ;
					Date today = cal.getTime();
					String dateStr = Timestamp.toCustomFormat(today, APIUtil.API_DATE_ONLY_FORMAT);

					// update header description not working from here.
					// will have to update it after request addition.
//					allParams.put(Field.HEADER_DESCRIPTION, "This request has been automatically escalated on " + dateStr + " " + cal.getTimeZone().getDisplayName() +"\n");
					
					UpdateRequest ur = new UpdateRequest() ;
					ur.setSource(TBitsConstants.SOURCE_CMDLINE);
					
					Request newReq = null ;
					try
					{
						newReq = ur.updateRequest(allParams);
						String header = newReq.getHeaderDescription();
						Request.updateHeaderDesc(newReq.getSystemId(),newReq.getRequestId(), newReq.getMaxActionId(), "This request has been automatically escalated on " + dateStr + " " + cal.getTimeZone().getDisplayName() +"\n" + header);
					}
					catch (APIException e) {
						e.printStackTrace();
						LOG.severe("Exception occured while Escalating the request.\nMessage : " + e.getMessage());
					} catch (TBitsException e) {
						e.printStackTrace();
						LOG.severe("Exception occured while Escalating the request.\nMessage : " + e.getDescription());
					} catch (Exception e)
					{
						e.printStackTrace();
						LOG.severe("Exception occured while Escalating the request.\nMessage : " + e.getMessage());
					}	
					
					if( null == newReq )
					{
						LOG.info("The returned request was null after update for request : " + ba.getSystemPrefix()+"#"+req.getRequestId());
					}
					}
					catch( Exception e)
					{
						LOG.info("Not escalating the request because of the following reason.");
						e.printStackTrace();
					}
				}
			}			
			}
			catch( Exception e)
			{
				LOG.warn("Exception occured while escalating request for business_area : " + sysPrefixArray[i]);
				e.printStackTrace();
			}
		}
	}

	private String getCommaSeparated(HashSet<String> userSet) 
	{
		String userList = "";
		if( null != userSet )
		{
			boolean first = true ;
			for(String str : userSet)
			{
				if( first )
				{
					first = false;
					userList += str;
				}
				else
				{
					userList += "," + str;
				}
			}
			
			return userList;
		}
		
		return null;
	}

	private IHolidayCalender getHolidayCalendarInstance( String calClass ) throws Exception
	{
		Class klass = PluginManager.getInstance().findPluginsByClassName(calClass);
		if( null == klass )
		{
			LOG.severe("Calendar plugin with name : " + calClass + " not found." );
			throw new Exception("Calendar plugin with name : " + calClass + " not found.");
		}
		
		Object obj = null ;
		try 
		{
			obj = klass.newInstance() ;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		if( obj == null )
		{
			LOG.severe("Cannot create the instance of the class : " + calClass );
			throw new Exception("Cannot create the instance of the class : " + calClass);
		}
		
		if( obj instanceof IHolidayCalender )
		{
			return (IHolidayCalender)obj;
		}
		else
		{
			LOG.info("The class with name " + calClass + " does not implement : " + IHolidayCalender.class.getName());
			throw new Exception("The class with name " + calClass + " does not implement : " + IHolidayCalender.class.getName());
		}
	}
	
	private Date getEscalationDate(Date currDate, int span, String calName) throws Exception 
	{
//		IHolidayCalender holical = getHolidayCalendarInstance(calName);
		
		int left = span ;
		
		// assuming every thing is in default time zone of server.		
		Calendar cal = Calendar.getInstance() ;
		cal.setTime(currDate);
		
		while( left != 0 )
		{
			cal.add(Calendar.DATE, 1);
			//HolidaysList.isHoliday(date, "LntStandardCalendar")
			if( !HolidaysList.isHoliday(cal.getTime(), calName.toUpperCase()))
			{
				left-- ;
			}
		}
		
		return cal.getTime() ;
	}
	
	public void disAllowNull( Object obj, String name )
	{
		if( null == obj )
			illegal( name + " cannot be null.");
	}
	// private void process(String baList, String dql, String dateFieldNameList, String userTypeFieldNameList, String spanList, String defaultUserLoginList, String calendarClassNameList, String superUserLogin) 
	public static void main(String[] argv)
	{
		String baList = "kdi_di,kdi_corr" ;
		String dql = "dueby:yesterday" ;
		String dateFieldNameList = Field.DUE_DATE + "," + Field.DUE_DATE ;
		String userTypeFieldNameList = "CorrTo" + "," + Field.SUBSCRIBER;
		String escUserTypeFieldNameList =  Field.ASSIGNEE + "," + Field.ASSIGNEE;
		String spanList = "8,9" ;
		String defaultUserLoginList = "darshan_shukla,root" ;
		String calendarClassNameList = "LntStandardCalendar,LntStandardCalendar" ;
		String superUserLogin = "root,root" ;
		
		LnTEscalationJob lej = new LnTEscalationJob() ;
		lej.process(baList, dql, dateFieldNameList, escUserTypeFieldNameList, userTypeFieldNameList, spanList, defaultUserLoginList, calendarClassNameList, superUserLogin);
	}
}
