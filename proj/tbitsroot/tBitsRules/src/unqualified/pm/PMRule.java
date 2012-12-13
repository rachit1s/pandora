package pm;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/*
 * Computes the following variables and sets them to the current request:
 *   + start date, end date, actual start date, actual end date
 *   + due date (if the current task has no open predecessor, the due date is marked as actual end date). 
 */
public class PMRule implements IRule {

	Field taskIdField = null;
	Field startDateField = null;
	Field endDateField = null;
	Field duraField = null;
	Field predField = null;

	// The start date feild as specified by user. This is to be recorded in
	// order to keep
	Field userSpecStartDateF = null;

	// The field for storing the end date specified by user.
	Field userSpecEndDateF = null;
	private Field astartDateField;
	private Field aendDateField;
	private Field userSpecAStartDateF;
	private Field userSpecAEndDateF;
	
	boolean markDueDate = false;
	public static final TBitsLogger LOG = TBitsLogger
			.getLogger(TBitsConstants.PKG_COMMON);

	public RuleResult execute(BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			String attachments) {
		// Validation
		RuleResult ruleResult = new RuleResult();
		try {
			taskIdField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(),
					PMConstants.TASK_ID);
			startDateField = Field.lookupBySystemIdAndFieldName(ba
					.getSystemId(), PMConstants.START_DATE);
			endDateField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(),
					PMConstants.END_DATE);
			duraField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(),
					PMConstants.DURATION);
			predField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(),
					PMConstants.PREDECESSORS);
			userSpecStartDateF = Field.lookupBySystemIdAndFieldName(ba
					.getSystemId(), PMConstants.USER_SPEC_START_DATE);
			userSpecEndDateF = Field.lookupBySystemIdAndFieldName(ba
					.getSystemId(), PMConstants.USER_SPEC_END_DATE);

			astartDateField = Field.lookupBySystemIdAndFieldName(ba
					.getSystemId(), PMConstants.ASTART_DATE);
			aendDateField = Field.lookupBySystemIdAndFieldName(
					ba.getSystemId(), PMConstants.AEND_DATE);
			userSpecAStartDateF = Field.lookupBySystemIdAndFieldName(ba
					.getSystemId(), PMConstants.USER_SPEC_ASTART_DATE);
			userSpecAEndDateF = Field.lookupBySystemIdAndFieldName(ba
					.getSystemId(), PMConstants.USER_SPEC_AEND_DATE);

		} catch (DatabaseException e1) {
			ruleResult.setMessage("Either of " + PMConstants.TASK_ID + ", "
					+ PMConstants.START_DATE + ", " + PMConstants.END_DATE
					+ ", " + PMConstants.DURATION + ", "
					+ PMConstants.PREDECESSORS
					+ PMConstants.USER_SPEC_START_DATE + ", "
					+ PMConstants.USER_SPEC_END_DATE + PMConstants.ASTART_DATE
					+ ", " + PMConstants.AEND_DATE + ", "
					+ PMConstants.USER_SPEC_ASTART_DATE + ", "
					+ PMConstants.USER_SPEC_AEND_DATE
					+ " fields are not defined in this BA. "
					+ "Please make sure that you specified various dates");
			ruleResult.setSuccessful(false);
			return ruleResult;
		}

		if ((taskIdField == null) || (startDateField == null)
				|| (endDateField == null) || (duraField == null)
				|| (predField == null) || (userSpecStartDateF == null)
				|| (userSpecEndDateF == null) || (astartDateField == null)
				|| (aendDateField == null) || (userSpecAStartDateF == null)
				|| (userSpecAEndDateF == null)) {
			ruleResult.setMessage("Either of " + PMConstants.TASK_ID + ", "
					+ PMConstants.START_DATE + ", " + PMConstants.END_DATE
					+ ", " + PMConstants.DURATION + ", "
					+ PMConstants.PREDECESSORS
					+ PMConstants.USER_SPEC_START_DATE + ", "
					+ PMConstants.USER_SPEC_END_DATE + PMConstants.ASTART_DATE
					+ ", " + PMConstants.AEND_DATE + ", "
					+ PMConstants.USER_SPEC_ASTART_DATE + ", "
					+ PMConstants.USER_SPEC_AEND_DATE
					+ " fields are not defined in this BA. "
					+ "Please make sure that you specified various dates");
			ruleResult.setSuccessful(false);
			System.out.println(" *** PLUGIN : " + ruleResult.getMessage());
			return ruleResult;
		}

		// task id should not be zero
		int currentTaskId = 0;
		try {
			currentTaskId = currentRequest.getExtendedFields().get(taskIdField)
					.getIntValue();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		if (currentTaskId == 0) {
			ruleResult.setMessage("Task Id can not be zero.");
			ruleResult.setSuccessful(false);
			ruleResult.setCanContinue(false);
			return ruleResult;
		}

		// Parse predecessors
		String predStr = currentRequest.getExtendedFields().get(predField)
				.getVarcharValue();
		if ((predStr != null) && predStr.equals("-")) {
			predStr = "";
			currentRequest.getExtendedFields().get(predField).setVarcharValue(
					predStr);
		}
		ArrayList<Predecessor> preds = null;
		try {
			preds = PredeccesorParser.parse(predStr);
		} catch (Exception e) {
			ruleResult.setCanContinue(false);
			ruleResult.setMessage("Invalid predecessors : " + predStr);
			ruleResult.setSuccessful(false);
			return ruleResult;
		}

		// Check if the predecesors exist
		if (preds != null) {
			boolean isFirst = true;
			StringBuilder taskIdSb = new StringBuilder();
			for (Predecessor p : preds) {
				if(isFirst)
					isFirst = false;
				else
					taskIdSb.append(",");
				taskIdSb.append(p.taskId);
			}
			boolean isAnyOpenPredecessor = false;
			if(!isFirst) //There is at least on predecessor
			{
				try {
					ArrayList<TaskDetails>tds = TaskDetailsRegistry.getTaskDetails(taskIdSb.toString(), ba
							.getSystemId());
					
					//Now check each pred in the returned values. This is done to minimize db trips
					for(Predecessor p:preds)
					{
						boolean found = false;
						for(TaskDetails td:tds)
						{
							if(td.taskId == p.taskId)
							{
								found = true;
								break;
							}
						}
						if(!found)
						{
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Predecessor '" + p.taskId
									+ "' does not exist.");
							ruleResult.setSuccessful(false);
							return ruleResult;
						}
					}
					
					//Now check whether these predecessors are to all closed
					boolean isAnyOpen = false;
					for(TaskDetails td: tds)
					{
						try {
							Type status = Type.lookupBySystemIdAndFieldNameAndTypeId(ba.getSystemId(), Field.STATUS, td.statusId);
							if(!status.getName().equalsIgnoreCase(PMConstants.CLOSED))
							{
								System.out.println("The status is not closed for :" + td.requestId + "==========");
								isAnyOpen = true;
								break;
							}
						} catch (DatabaseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if(isAnyOpen && (preds.size() > 0))
					{
						System.out.println("isAnyOpenPredecessor ========");
						isAnyOpenPredecessor = true;
					}
				} catch (SQLException e) {
					ruleResult.setCanContinue(false);
					ruleResult
							.setMessage("Error while checking about the predecessor. Please check with the administrator.");
					ruleResult.setSuccessful(false);
					return ruleResult;
				}
			}
			
			if(!isAnyOpenPredecessor)
			{
				System.out.println("setting markDueDate = true ====");
				this.markDueDate = true;
			}
			else
			{
				System.out.println("setting markDueDate = false ====");
				this.markDueDate = false;
			}
		}

		if (isAddRequest) {

			try {
				// Check if it already exists.
				if ((currentTaskId != 0)
						&& TaskDetailsRegistry.alreadyExists(currentTaskId, ba
								.getSystemId())) {
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Task ID: " + currentTaskId
							+ " already exists");
					return ruleResult;
				}
			} catch (Exception e) {
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("Unable to read task id.");
				ruleResult.setSuccessful(false);
				e.printStackTrace();
				return ruleResult;
			}
			// Just add it silently.
			return updateCurrentRequest(ba, currentRequest, Source, user,
					extendedFields, oldRequest);
		} else {
			// Do not allow changing the taskid
			if (currentTaskId != oldRequest.getExtendedFields()
					.get(taskIdField).getIntValue()) {
				ruleResult.setMessage("You can not change the taskid.");
				ruleResult.setSuccessful(false);
				ruleResult.setCanContinue(false);
				return ruleResult;
			}

			// Check for cyclic dependency
			TaskDetails td = new TaskDetails(null, null, null, null, 0,
					currentTaskId, 0, ba.getSystemId());
			td.predList = preds;

			try {
				boolean isCyclic = false;
				isCyclic = TaskDetailsRegistry.checkForCycleDependency(td,
						TaskDetailsRegistry.getAllTasks());
				if (isCyclic) {
					ruleResult
							.setMessage("There is cyclic depenency being created because of the predecessors");
					ruleResult.setCanContinue(false);
					ruleResult.setSuccessful(false);
					return ruleResult;
				}
			} catch (IllegalAccessException e) {
				ruleResult.setMessage(e.getMessage());
				ruleResult.setCanContinue(false);
				ruleResult.setSuccessful(false);
				return ruleResult;
			}

			// update current request
			ruleResult = updateCurrentRequest(ba, currentRequest, Source, user,
					extendedFields, oldRequest);
			return ruleResult;
		}
	}

	public RuleResult updateCurrentRequest(BusinessArea ba,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, Request oldRequest) {
		RuleResult ruleResult = new RuleResult();
		ruleResult.setCanContinue(true);
		Integer taskId = null;
		Integer duration = null;
		String predecessors = null;
		
		/* planned */
		Date startDate = null;
		Date endDate = null;
		Date userSpecStartDate = null;
		Date userSpecEndDate = null;
		
		/* Actuals */
		Date astartDate = null;
		Date aendDate = null;
		Date userSpecAStartDate = null;
		Date userSpecAEndDate = null;
		
		
		Date initDate = null;
		Date initADate = null;
		
		Hashtable<Field, RequestEx> oldReqExtFields = null;
		boolean isAddRequest = false;
		
		if (oldRequest != null)
			oldReqExtFields = oldRequest.getExtendedFields();
		else
			isAddRequest = true;

		
		
		RequestEx startDateO = extendedFields.get(startDateField);
		if (startDateO != null) {
			startDate = startDateO.getDateTimeValue();
		}

		RequestEx endDateO = extendedFields.get(endDateField);
		if (endDateO != null) {
			endDate = endDateO.getDateTimeValue();
		}

		RequestEx astartDateO = extendedFields.get(astartDateField);
		if (astartDateO != null) {
			astartDate = astartDateO.getDateTimeValue();
		}

		RequestEx aendDateO = extendedFields.get(aendDateField);
		if (aendDateO != null) {
			aendDate = aendDateO.getDateTimeValue();
		}
		
		// Read old startdate/endate
		Date startDateOld = null;
		Date endDateOld = null;
		Date usStartDateOld = null;
		Date usEndDateOld = null;
		
		Date astartDateOld = null;
		Date aendDateOld = null;
		Date usAStartDateOld = null;
		Date usAEndDateOld = null;

		boolean predChanged = false;
		
		if (!isAddRequest) {
			
			String oldPred = ((RequestEx) oldReqExtFields.get(predField)).getVarcharValue();
			String newPred = ((RequestEx) extendedFields.get(predField)).getVarcharValue();
			if(!nullEquals(oldPred, newPred))
			{
				predChanged = true;
			}
			
			RequestEx startDateOldO = oldReqExtFields.get(startDateField);
			if (startDateOldO != null) {
				startDateOld = startDateOldO.getDateTimeValue();
			}

			RequestEx endDateOldO = oldReqExtFields.get(endDateField);
			if (endDateOldO != null) {
				endDateOld = endDateOldO.getDateTimeValue();
			}

			RequestEx usStartDateOldO = oldReqExtFields.get(userSpecStartDateF);
			if (usStartDateOldO != null) {
				usStartDateOld = usStartDateOldO.getDateTimeValue();
			}

			RequestEx usEndDateOldO = oldReqExtFields.get(userSpecEndDateF);
			if (usEndDateOldO != null) {
				usEndDateOld = usEndDateOldO.getDateTimeValue();
			}
			
			/* Actuals */
			RequestEx astartDateOldO = oldReqExtFields.get(astartDateField);
			if (astartDateOldO != null) {
				astartDateOld = astartDateOldO.getDateTimeValue();
			}

			RequestEx aendDateOldO = oldReqExtFields.get(aendDateField);
			if (aendDateOldO != null) {
				aendDateOld = aendDateOldO.getDateTimeValue();
			}

			RequestEx usAStartDateOldO = oldReqExtFields.get(userSpecAStartDateF);
			if (usAStartDateOldO != null) {
				usAStartDateOld = usAStartDateOldO.getDateTimeValue();
			}

			RequestEx usAEndDateOldO = oldReqExtFields.get(userSpecAEndDateF);
			if (usAEndDateOldO != null) {
				usAEndDateOld = usAEndDateOldO.getDateTimeValue();
			}
			
			// On changing predecessors, since there is a chance of sliding it back, so dont set the initDates.
			if(!predChanged)
			{
				initDate = startDate;
				initADate = astartDate;
			}
		}

		RequestEx durationO = extendedFields.get(duraField);
		if (durationO != null)
			duration = (int) durationO.getRealValue();

		RequestEx predecessorsO = extendedFields.get(predField);
		if (predecessorsO != null)
			predecessors = predecessorsO.getVarcharValue();
		/*
		 * Since even when algo is updating the request the dates specified by
		 * algo are becoming constraints. So, in case the predecessor has
		 * finished earlier, the successor wasnt being slided to an earlier date
		 */
		if (Source == PMConstants.SOURCE_PM) {
			LOG.info("PM: Source is PM.");
			RequestEx userSpecStartDateO = extendedFields
					.get(userSpecStartDateF);
			if (userSpecStartDateO != null) {
				userSpecStartDate = userSpecStartDateO.getDateTimeValue();
			}

			RequestEx userSpecEndDateO = extendedFields.get(userSpecEndDateF);
			if (userSpecEndDateO != null) {
				userSpecEndDate = userSpecEndDateO.getDateTimeValue();
			}
			
			LOG.info("PM: Source is PM.");
			RequestEx userSpecAStartDateO = extendedFields
					.get(userSpecAStartDateF);
			if (userSpecAStartDateO != null) {
				userSpecAStartDate = userSpecAStartDateO.getDateTimeValue();
			}

			RequestEx userSpecAEndDateO = extendedFields.get(userSpecEndDateF);
			if (userSpecAEndDateO != null) {
				userSpecAEndDate = userSpecAEndDateO.getDateTimeValue();
			}

		} else {
			LOG.info("PM: Source is not PM.");
			if (!isAddRequest) {
				/* Planned */
				if ((startDate != null) && (startDateOld != null)
						&& DateEquals(startDate, startDateOld)) {
					userSpecStartDate = usStartDateOld;
				} else {
					userSpecStartDate = startDate;
				}

				if ((endDate != null) && (endDateOld != null)
						&& DateEquals(endDate, endDateOld)) {
					userSpecEndDate = usEndDateOld;
				} else {
					userSpecEndDate = endDate;
				}
				
				/** Actuals */
				if ((astartDate != null) && (astartDateOld != null)
						&& DateEquals(astartDate, astartDateOld)) {
					userSpecAStartDate = usAStartDateOld;
				} else {
					userSpecAStartDate = astartDate;
				}

				if ((aendDate != null) && (aendDateOld != null)
						&& DateEquals(aendDate, aendDateOld)) {
					userSpecAEndDate = usAEndDateOld;
				} else {
					userSpecAEndDate = aendDate;
				}
				
			} else {
				userSpecStartDate = startDate;
				userSpecEndDate = endDate;
				
				userSpecAStartDate = astartDate;
				userSpecAEndDate = aendDate;
				
			}

			// Save user spec date
			/* Planned */
			if (userSpecStartDate != null)
				extendedFields.get(userSpecStartDateF).setDateTimeValue(
						new Timestamp(userSpecStartDate.getTime()));
			else
				extendedFields.get(userSpecStartDateF).setDateTimeValue(null);

			if (userSpecEndDate != null)
				extendedFields.get(userSpecEndDateF).setDateTimeValue(
						new Timestamp(userSpecEndDate.getTime()));
			else
				extendedFields.get(userSpecEndDateF).setDateTimeValue(null);
			
			/* Actuals */
			if (userSpecAStartDate != null)
				extendedFields.get(userSpecAStartDateF).setDateTimeValue(
						new Timestamp(userSpecAStartDate.getTime()));
			else
				extendedFields.get(userSpecAStartDateF).setDateTimeValue(null);

			if (userSpecAEndDate != null)
				extendedFields.get(userSpecAEndDateF).setDateTimeValue(
						new Timestamp(userSpecAEndDate.getTime()));
			else
				extendedFields.get(userSpecAEndDateF).setDateTimeValue(null);
		}
		
		// Load Predecessors Predecessors
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		ArrayList<Predecessor> preds = PredeccesorParser.parse(predecessors);

		for (Predecessor p : preds) {
			if (isFirst)
				isFirst = false;
			else
				sb.append(",");
			sb.append(p.taskId);
		}

		ProjectManagement pm = new ProjectManagement();
		ArrayList<TaskDetails> td = null;
		// Get a list of taskids
		if (sb.length() > 0) {

			// Get list of requests wrt taskids
			try {
				td = TaskDetailsRegistry.getTaskDetails(sb.toString(), ba
						.getSystemId());
			} catch (SQLException e) {
				ruleResult.setMessage("Unable to get the task details for : "
						+ sb.toString() + "\n");
				System.out.println(e);
				ruleResult.setSuccessful(false);
				return ruleResult;
			}
		}
		MyIssueReader msr = new MyIssueReader(td);

		// Get a TaskReader for list of request
		pm.issueReader = msr;
		pm.holidayCal = new MyHolidayCalendar();
		// Get the start-date, end date, duration using project management.

		System.out.println("**************JUST BEFORE CALCULATION************************");
		System.out.println("userSpecStartDate");
		System.out.println(userSpecStartDate);
		System.out.println("userSpecEndDate");
		System.out.println(userSpecEndDate);
		System.out.println("userSpecAStartDate");
		System.out.println(userSpecAStartDate);
		System.out.println("userSpecAEndDate");
		System.out.println(userSpecAEndDate);
		System.out.println("initDate");
		System.out.println(initDate);
		System.out.println("initADate");
		System.out.println(initADate);
		System.out.println("preds");
		System.out.println(preds);
		TaskDetails newTD = pm.getSchedule(userSpecStartDate, userSpecEndDate,userSpecAStartDate, userSpecAEndDate,
				duration, preds, initDate, initADate);
		SimpleDateFormat sdf = new SimpleDateFormat();

		System.out.println("NEW START DATE: " + sdf.format(newTD.startDate));
		System.out.println("NEW END DATE: " + sdf.format(newTD.endDate));
		System.out.println("NEW ACTUAL START DATE: " + sdf.format(newTD.actualStartDate));
		System.out.println("NEW ACTUAL END DATE: " + sdf.format(newTD.actualEndDate));

		// This get can be reduced by keep RequestEx instead of Field which is
		// used above.
		if (extendedFields == null) {
			System.out.println("Extended Fields are null");
			extendedFields = new Hashtable<Field, RequestEx>();
		}
		
		extendedFields.get(startDateField).setDateTimeValue(
				new Timestamp(newTD.startDate.getTime()));
		extendedFields.get(endDateField).setDateTimeValue(
				new Timestamp(newTD.endDate.getTime()));

		//Setting the actuals only if not specified by user
		if (userSpecAStartDate == null) {
			extendedFields.get(astartDateField).setDateTimeValue(
					new Timestamp(newTD.actualStartDate.getTime()));
		}
		if (userSpecAEndDate == null) {
			extendedFields.get(aendDateField).setDateTimeValue(
					new Timestamp(newTD.actualEndDate.getTime()));
		}
		
		if(markDueDate)
		{
			System.out.println("SETTING THE DUE DATE AS AENDDATE =====");
			System.out.println(preds);
			if (userSpecAEndDate == null) 
				currentRequest.setDueDate(Timestamp.getTimestamp(newTD.actualEndDate));
			else
				currentRequest.setDueDate(Timestamp.getTimestamp(userSpecAEndDate));
		}
		else
		{
			System.out.println("SETTING THE DUE DATE AS NULL ====");
			System.out.println(preds);
			currentRequest.setDueDate(null);
		}
		try {
			currentRequest.setExtendedFields(extendedFields);
			System.out.println("****After settings****");
			System.out
					.println("Start Date: " + currentRequest.get("startdate"));
			System.out.println("End Date: " + currentRequest.get("enddate"));
		} catch (DatabaseException e) {
			ruleResult
					.setMessage("Unable to update the extended fields. Please check the database connectivity.");
			ruleResult.setSuccessful(false);
			return ruleResult;
		}
		ruleResult.setSuccessful(true);
		return ruleResult;
	}

	/*
	 * Checks whether the two dates are equal ignoring the seconds and
	 * milliseconds.
	 */
	public static boolean DateEquals(Date d1, Date d2) {
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d1);

		c1.set(Calendar.SECOND, 0);
		c1.set(Calendar.MILLISECOND, 0);

		Calendar c2 = Calendar.getInstance();
		c2.setTime(d2);
		c2.set(Calendar.SECOND, 0);
		c2.set(Calendar.MILLISECOND, 0);

		return c1.getTime().equals(c2.getTime());
	}

	public String getName() {
		return "Project Management - Calculation of Start Date, End Date and Duration";
	}

	public double getSequence() {
		return 1;
	}

	public static void main(String[] args) {

		// Date d1 = new Date();
		// Calendar c1 = Calendar.getInstance();
		// c1.setTime(d1);
		// c1.set(Calendar.MINUTE, 0);
		// c1.set(Calendar.SECOND, 0);
		// c1.set(Calendar.MONTH, 0);
		// c1.set(Calendar.YEAR, 0);
		// c1.set(Calendar.DAY_OF_MONTH, 0);
		//		
		//		
		// Date d2 = new Date();
		// Calendar c2 = Calendar.getInstance();
		// c2.setTime(d2);
		// c2.set(Calendar.MINUTE, 0);
		//		
		// System.out.println("Are dates equal: " + DateEquals(c1.getTime(),
		// c2.getTime()));
	}

	public static void printDate(String msg, Date d) {
		System.out.println("PMRule.printDate()");
		if (d == null) {
			LOG.info(msg + " is null");
			return;
		}
		DateFormat df = new SimpleDateFormat();
		LOG.info(msg + "(" + df.getTimeZone().getDisplayName() + "): "
				+ df.format(d));
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		LOG.info(msg + "(" + df.getTimeZone().getDisplayName() + "): "
				+ df.format(d));
	}
	private boolean nullEquals(String s1, String s2)
	{
		if((s1 == null) && (s2 == null))
			return true;
		else if((s1 == null) || (s2 == null))
			return false;
		else 
			return s1.equals(s2);
	}
}
