package pm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.CalenderUtils;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
/*
 * This rule is executed in Projects BA which has aenddate and astartdate:
 * It does the following on closure of the request:
 * 1. Sets the aenddate of current request to now()
 * 2. Sets the astartdate of all the open subrequest to the now() taking the calendar into consideration.
 */
public class UpdateActualDatesOnClosure implements IRule {
	
	private static final String AENDDATE = "aenddate";
	private static final String ASTARTDATE = "astartdate";
	private static final String CLOSED = "closed";
	
	public RuleResult execute(BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			String attachments) {
		// TODO Auto-generated method stub
		Field actualStartDateF = null;
		try {
			actualStartDateF = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), ASTARTDATE);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Field actualEndDateF = null;
		try {
			actualEndDateF = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), AENDDATE);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(actualEndDateF == null)
		{
			return new RuleResult(true, "UpdateActualDatesOnClosure: Rule doesn't have actual end date. So skipping the rule");
		}
		if(actualStartDateF == null)
		{
			return new RuleResult(true, "UpdateActualDatesOnClosure: Rule doesn't have actual start date. So skipping the rule");
		}
		
		if(
				currentRequest.getStatusId().getName().equalsIgnoreCase(CLOSED)
				&& !oldRequest.getStatusId().getName().equalsIgnoreCase(CLOSED)
		)
		{
			// Calculate the upcoming date
			Date now = Calendar.getInstance().getTime();
			now = CalenderUtils.slideDate(now, 0);
			
			Hashtable<String, String> subrequests = currentRequest.getSubRequests();
			for(String subReqWithSysPrefix:subrequests.keySet())
			{
				int loc = subReqWithSysPrefix.indexOf('#');
				if(loc > -1)
				{
					String reqStr = subReqWithSysPrefix.substring(loc+1);
					int subReqId = -1;
					try{	
						subReqId = Integer.parseInt(reqStr);
						Request subRequest = Request.lookupBySystemIdAndRequestId(currentRequest.getSystemId(), subReqId);
						if(!subRequest.getStatusId().getName().equalsIgnoreCase(CLOSED))
						{
						
							Hashtable<String, String> fields = new Hashtable<String, String>();
							fields.put(Field.BUSINESS_AREA, subRequest.getSystemId() + "");
							fields.put(Field.REQUEST, subRequest.getRequestId() + "");
							fields.put(Field.USER, "root");
							fields.put(Field.DESCRIPTION, "[tBits: Updating the actual start date as parent request with id '" 
									+ currentRequest.getRequestId() + "' is closed.]");
							
							DateFormat df = new SimpleDateFormat(APIUtil.API_DATE_FORMAT);
							fields.put(ASTARTDATE, df.format(now));
							//set actual start date
							UpdateRequest ur = new UpdateRequest();
							ur.setSource(TBitsConstants.SOURCE_CMDLINE);
							ur.updateRequest(fields);
						}
					}catch (NumberFormatException e) {
						System.out.println("UpdateActualDatesOnClosure: Invalid requestId: '" + reqStr + "'");
						continue;
					} catch (DatabaseException e) {
						// TODO Auto-generated catch block
						System.out.println("UpdateActualDatesOnClosure: Error while getting the subrequest for '" + subReqId + "'");
						e.printStackTrace();
						continue;
					} catch (TBitsException e) {
						// TODO Auto-generated catch block
						System.out.println("UpdateActualDatesOnClosure: Error while updating the subrequest. '" + subReqId + "'");
						e.printStackTrace();
					} catch (APIException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			//set actual end date of current request
			currentRequest.getExtendedFields().get(actualEndDateF).setDateTimeValue(new Timestamp());
		}
		return new RuleResult();
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Set the actual end date and sub request's start date on closure";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
