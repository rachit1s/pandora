package expressit;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import sun.util.calendar.CalendarDate;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.Timestamp;
import transbit.tbits.dataProducer.StaticalDataProducer;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;

public class SetDuedate implements IRule{
      
	private static final String sys_prefix = "sys_prefix";
	private static final String corpsalesupport = "corpsalesupport";
	private static final String corpoperation = "corpoperation";
	
	public RuleResult execute(BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			String attachments) {
		
		
		RuleResult ruleresult = new RuleResult();
		String Sysprefix = ba.getSystemPrefix();
		int SysId  =   ba.getSystemId();
		int RequestId = currentRequest.getRequestId();
		
		if((Sysprefix.equalsIgnoreCase(corpsalesupport)||Sysprefix.equalsIgnoreCase(corpoperation))
				&&(isAddRequest))
		{
			Date when = new Date();
			int Today = when.getDate();
			when.setDate(Today+2);
			
			Timestamp dued = Timestamp.getTimestamp(when);
			System.out.println("date + 2 days:"+when);
			
			
			
			currentRequest.setDueDate(dued);
			
		}
		ruleresult.setCanContinue(true);
		
		return ruleresult;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "rule set due date running ";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
