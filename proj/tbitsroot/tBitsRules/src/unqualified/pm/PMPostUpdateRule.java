package pm;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.CalenderUtils;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

/**
 * Updates the successors in case of change in start date and end date. 
 * These successors would execute this rule to update their successors making it recursive.
 * This is not executed in case of an Add Request.
 * @author sandeepgiri
 *
 */
public class PMPostUpdateRule implements IPostRule {
	Field taskIdField = null;
	Field predField = null;
	Field duraField = null;
	
	Field startDateField = null;
	Field endDateField = null;
	
	//The start date feild as specified by user. This is to be recorded in order to keep
	Field userSpecStartDateF = null; 
	
	//The field for storing the end date specified by user.
	Field userSpecEndDateF = null; 
	
	Field astartDateField = null;
	Field aendDateField = null;
	
	//The start date feild as specified by user. This is to be recorded in order to keep
	Field userSpecAStartDateF = null; 
	
	//The field for storing the end date specified by user.
	Field userSpecAEndDateF = null; 
	
	public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_COMMON);
	/**
	 * Should only be executed if it is an update not add and if there is change in start-date, end-date. 
	 */
	public RuleResult execute(BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest) {
		boolean markDueDate = false;
		RuleResult ruleResult = new RuleResult();
		if(isAddRequest)
		{
			return ruleResult;
		}
		try {
			if(
					currentRequest.getStatusId().getName().equalsIgnoreCase(PMConstants.CLOSED)
					&& !oldRequest.getStatusId().getName().equalsIgnoreCase(PMConstants.CLOSED)
			)
			{
				markDueDate = true;
			}
			taskIdField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), PMConstants.TASK_ID);
			duraField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), PMConstants.DURATION);
			predField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), PMConstants.PREDECESSORS);
			
			startDateField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), PMConstants.START_DATE);
			endDateField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), PMConstants.END_DATE);
			userSpecStartDateF = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), PMConstants.USER_SPEC_START_DATE);
			userSpecEndDateF = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), PMConstants.USER_SPEC_END_DATE);
			
			astartDateField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), PMConstants.ASTART_DATE);
			aendDateField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), PMConstants.AEND_DATE);
			
			userSpecAStartDateF = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), PMConstants.USER_SPEC_ASTART_DATE);
			userSpecAEndDateF = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), PMConstants.USER_SPEC_AEND_DATE);
			
		} catch (DatabaseException e1) {
			ruleResult.setMessage("Exception:Either of " + PMConstants.TASK_ID + ", " + ", " + PMConstants.DURATION + ", " + PMConstants.PREDECESSORS
					+ ", " + PMConstants.START_DATE + ", " + PMConstants.END_DATE + ", " + PMConstants.USER_SPEC_START_DATE + ", " + PMConstants.USER_SPEC_END_DATE
					+ ", " + PMConstants.START_DATE + ", " + PMConstants.END_DATE + ", " + PMConstants.USER_SPEC_ASTART_DATE + ", " + PMConstants.USER_SPEC_AEND_DATE
					+ " fields are not defined in this BA. " + 
			"Please make sure that you specified various dates");
			ruleResult.setSuccessful(false);
			return ruleResult;
		}
		
		if((taskIdField == null)  || (duraField == null) || (predField == null) 
				|| (startDateField == null) || (endDateField == null)|| (userSpecStartDateF == null) || (userSpecEndDateF == null)
			|| (astartDateField == null) || (aendDateField == null)|| (userSpecAStartDateF == null) || (userSpecAEndDateF == null))
		{
			ruleResult.setMessage("Either of " + PMConstants.TASK_ID + ", " + ", " + PMConstants.DURATION + ", " + PMConstants.PREDECESSORS
					+ ", " + PMConstants.START_DATE + ", " + PMConstants.END_DATE + ", " + PMConstants.USER_SPEC_START_DATE + ", " + PMConstants.USER_SPEC_END_DATE
					+ ", " + PMConstants.START_DATE + ", " + PMConstants.END_DATE + ", " + PMConstants.USER_SPEC_ASTART_DATE + ", " + PMConstants.USER_SPEC_AEND_DATE
					+ " fields are not defined in this BA. " + 
			"Please make sure that you specified various dates");
			ruleResult.setSuccessful(false);
			return ruleResult;
		}
		
		//Is there a change in start-date/end-date/actual start-date/actual end-date/status?
		//If not skip this rule.
		if(
				currentRequest.getExtendedFields().get(startDateField).getDateTimeValue().equals(oldRequest.getExtendedFields().get(startDateField).getDateTimeValue())
				&& currentRequest.getExtendedFields().get(endDateField).getDateTimeValue().equals(oldRequest.getExtendedFields().get(endDateField).getDateTimeValue())
				&& currentRequest.getExtendedFields().get(astartDateField).getDateTimeValue().equals(oldRequest.getExtendedFields().get(astartDateField).getDateTimeValue())
				&& currentRequest.getExtendedFields().get(aendDateField).getDateTimeValue().equals(oldRequest.getExtendedFields().get(aendDateField).getDateTimeValue())
				&& currentRequest.getStatusId().equals(oldRequest.getStatusId())
				)
		{
			ruleResult.setMessage("There is no change in Start-Date, End-Date, Actual Start-Date and Actual End-Date the request.");
			return ruleResult;
		}
		
		int taskId = extendedFields.get(taskIdField).getIntValue();
		
		//Find dependent requests and update them
		Integer[] dependentRequests = TaskDetailsRegistry.getImmediateSuccessors(taskId, ba.getSystemId());
		for(int requestId:dependentRequests)
		{
			System.out.println("Updating dep requests.");
			Request depRequest;
			try {
				depRequest = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), requestId);
				
				Hashtable<Field, RequestEx> depRequestExtFields = depRequest.getExtendedFields();
				Date depNewStartDate = depRequestExtFields.get(startDateField).getDateTimeValue();
				Date depNewEndDate = depRequestExtFields.get(endDateField).getDateTimeValue();
				Date usDepStartDate = depRequestExtFields.get(userSpecStartDateF).getDateTimeValue();
				Date usDepEndDate = depRequestExtFields.get(userSpecEndDateF).getDateTimeValue();

				Date depNewAStartDate = depRequestExtFields.get(astartDateField).getDateTimeValue();
				Date depNewAEndDate = depRequestExtFields.get(aendDateField).getDateTimeValue();
				Date usDepAStartDate = depRequestExtFields.get(userSpecAStartDateF).getDateTimeValue();
				Date usDepAEndDate = depRequestExtFields.get(userSpecAEndDateF).getDateTimeValue();

				String depNewPredsStr = depRequestExtFields.get(predField).getVarcharValue();
				ArrayList<Predecessor> preds = PredeccesorParser.parse(depNewPredsStr);
				
				String durationStr = depRequest.get(PMConstants.DURATION);
				int depNewDuration = (int) Double.parseDouble(durationStr);
				
				StringBuilder sb  = new StringBuilder();
				boolean isFirst = true;
				for(Predecessor p: preds)
				{
					if(isFirst)
						isFirst = false;
					else 
						sb.append(",");
					sb.append(p.taskId);
				}

				ProjectManagement pm = new ProjectManagement();
				ArrayList<TaskDetails> predsOfSuccessor = null;
				//Get a list of taskids
				if(sb.length() > 0)
				{
					//Get list of requests wrt taskids
					try {
						predsOfSuccessor = TaskDetailsRegistry.getTaskDetails(sb.toString(), ba.getSystemId());
					} catch (SQLException e) {
						ruleResult.setMessage("Unable to get the task details for : " + sb.toString() + "\n");
						System.out.println(e);
						ruleResult.setSuccessful(false);
						return ruleResult;
					}
				}
				MyIssueReader msr = new MyIssueReader(predsOfSuccessor);

				//Get a TaskReader for list of request
				pm.issueReader = msr;
				pm.holidayCal = new MyHolidayCalendar();
				
				TaskDetails td = pm.getSchedule(usDepStartDate, usDepEndDate, usDepAStartDate, usDepAEndDate, depNewDuration, preds, depNewStartDate, depNewAStartDate);
				
				if(!td.startDate.equals(usDepStartDate) && !td.endDate.equals(usDepEndDate) 
						&& !td.actualStartDate.equals(usDepAStartDate) && !td.actualEndDate.equals(usDepAEndDate))
				{
					LOG.info("Updating the successor " + depRequest.getRequestId());
					Hashtable<String, String> params = new Hashtable<String, String>();
					params.put(Field.REQUEST, depRequest.getRequestId() + "");
					params.put(Field.BUSINESS_AREA, depRequest.getSystemId() + "");
					params.put(Field.NOTIFY, "false");
					params.put(Field.DESCRIPTION, "[ The request is being changed because of change in Predecessor with Task Id: " + taskId + " ]");
					
					DateFormat df = new SimpleDateFormat(TBitsConstants.API_DATE_FORMAT);
					df.setTimeZone(TimeZone.getTimeZone("GMT"));
					
					String startDateStr = null;
					if(td.startDate != null)
						startDateStr = df.format(td.startDate);
					
					String endDateStr = null;
					if(td.endDate != null)
						endDateStr = df.format(td.endDate);
					
					String astartDateStr = null;
					if(td.actualStartDate != null)
						astartDateStr = df.format(td.actualStartDate);
					
					String aendDateStr = null;
					if(td.actualEndDate != null)
						aendDateStr = df.format(td.actualEndDate);
					
					params.put(PMConstants.START_DATE, startDateStr);
					params.put(PMConstants.END_DATE, endDateStr);
					if(usDepAStartDate != null)
						params.put(PMConstants.ASTART_DATE, astartDateStr);
					if(usDepAEndDate != null)
						params.put(PMConstants.AEND_DATE, aendDateStr);
					if(markDueDate)
					{
						Date nextDate;
						if(usDepAEndDate == null)
						{
							nextDate = td.actualEndDate;
						}
						else
						{
							nextDate = usDepAEndDate;
						}
						params.put(Field.DUE_DATE, df.format(nextDate));
					}
					params.put(PMConstants.DURATION, td.duration.toString());
					params.put(Field.USER, user.getUserLogin());
					UpdateRequest ur = new UpdateRequest();
					ur.setSource(PMConstants.SOURCE_PM);
					try {
						ur.updateRequest(params);
					} catch (TBitsException e) {
						ruleResult.setSuccessful(false);
						ruleResult.setMessage("Error while updating the dependent task");
						ruleResult.setCanContinue(true);
						LOG.error(ruleResult.getMessage(), e);
						break;
					} catch (APIException e) {
						ruleResult.setSuccessful(false);
						ruleResult.setMessage("Error while updating the dependent task");
						ruleResult.setCanContinue(true);
						LOG.error(ruleResult.getMessage(), e);
						break;
					}
				}
			} catch (DatabaseException e1) {
				
				ruleResult.setCanContinue(false);
				ruleResult.setSuccessful(false);
				ruleResult.setMessage("Unable to get the request corresponding to ba: " 
						+ ba.getSystemId() + " and request id: " + requestId + ". " + e1.getMessage());
				LOG.error(ruleResult.getMessage(), e1);
				break;
			}
		}
		return ruleResult;
	}

	public String getName() {
		return "Update the successors's schedule";
	}

	public double getSequence() {
		return 0;
	}
	public static void main(String[] args) throws DatabaseException {
		IPostRule rule = new PMPostUpdateRule();
		BusinessArea ba = BusinessArea.lookupBySystemPrefix("ABC");
		Request req = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), 6);
		User user = User.lookupAllByUserId(1);
		rule.execute(ba, null, req, 101, user, req.getExtendedFields(), true);
	}
}
