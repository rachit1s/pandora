package gmrMom;

import java.sql.Connection;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;

import com.ibm.icu.text.SimpleDateFormat;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;

/**
 * 
 * @author sharan
 */
/**
 As we know that in MOM module we have two date fields,

 Target Date-- The date,by which that particular action has to be closed after completing the things on which the logger and assignees has agreed upon.

 Closure Date-- The date,on which the assignees closes this particular action.

 If the assignees doesn't closes the request by the target date+ 7 more days, then he must give an explanation in the text
field (Field name: reasonfordelay ,added as a new extended field). This must be a compulsion when the user tries to update the status to close( field_id=3,type_id=1),
without this the request must not be allowed to be submitted.
 */
public class ClosureDate  implements IRule{
	

	TBitsLogger logger = TBitsLogger.getLogger("gmrRules.mom");
//	String sysPrefix = "";
	String closureDateFieldName="closuredate";
	String reasonfordelay="reasonfordelay";
	String status="";
	
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		if(!isAddRequest)
		{
//		sysPrefix = ;
//		int basysid= ba.getSystemId();
		
		if(ba.getSystemPrefix().equalsIgnoreCase("GMR_MOM"))
		{
		
			status= currentRequest.get(Field.STATUS);
			if(status.equalsIgnoreCase("Closed"))
			{
			Date duedate=(Date)currentRequest.getObject(Field.DUE_DATE);
			Date closureDate=(Date)currentRequest.getObject(closureDateFieldName);
			 
			Calendar newduedate = Calendar.getInstance();
			Calendar newclosuredate = Calendar.getInstance();
			newduedate.setTime(duedate);
			newduedate.set(Calendar.MINUTE, 0);
			newduedate.set(Calendar.SECOND, 0);
			newduedate.set(Calendar.HOUR, 0);
			newduedate.set(Calendar.MILLISECOND,0);
			
			System.out.println("newduedate : " + newduedate);
			newclosuredate.setTime(closureDate);
			newclosuredate.set(Calendar.MINUTE, 0);
			newclosuredate.set(Calendar.SECOND, 0);
			newclosuredate.set(Calendar.HOUR, 0);
			newclosuredate.set(Calendar.MILLISECOND,0);
			System.out.println("newclosuredate : " + newclosuredate);
//			newclosuredate.
			double diff=newclosuredate.getTimeInMillis()- newduedate.getTimeInMillis();
			double days = diff/(24*60*60*1000);
			String	reasonforDelay =(String)currentRequest.get(reasonfordelay);
			if(days>7.0)
			{			
				
				reasonforDelay =(String)currentRequest.get(reasonfordelay);
				 if(reasonforDelay==null || reasonforDelay.trim().equalsIgnoreCase(""))
				 {
					 
					/* here the description is not filled . so we have to inform user to fill the description 
					 * so showing an error message to the user at the top of the 
					 *request and not logging the current request 
					 * 
					 */
					 return new RuleResult(false, "Reason for delay is mandatory as closure date has crossed it's limit. Please give your reason for delay in the appropriate field.",true);
					 
				 }		 
					
				
			}
			
			}
		}
			
			
	
		}
		return new RuleResult(true, "Succesful", true);
	}

	@Override
	public double getSequence() {
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "reason for delay is mandatory when closure date is more than target date + 7 days";
	}

}
