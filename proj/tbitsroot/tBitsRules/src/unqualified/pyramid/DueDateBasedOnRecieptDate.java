/**
 * 
 */
package pyramid;

import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DefaultHolidayCalendar;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;
import transbit.tbits.domain.CalenderUtils;

/**
 * @author Lokesh
 *
 */
public class DueDateBasedOnRecieptDate implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		
		//String sysPrefix = "DCR343,DCR326,VDCR326,DCR345";
		boolean isApplicable = PyramidUtils.isExistsInCommons(ba.getSystemPrefix());
		
		RuleResult ruleResult = new RuleResult();
		if(isApplicable && (!(currentRequest.getParentRequestId() > 0))){
			Field receiptDateField = null;
			try {
				receiptDateField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "hardcopyreceiptdate");
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			if (receiptDateField != null){
				RequestEx receiptDateReq = extendedFields.get(receiptDateField);
				if (receiptDateReq != null){
					Timestamp rTimeStamp = receiptDateReq.getDateTimeValue();	
					if (rTimeStamp != null){
						Date date = new Date(rTimeStamp.getTime()); 			
						Date dueDate = CalenderUtils.slideDate(date, 6, new DefaultHolidayCalendar());	
						Timestamp duedateTS = Timestamp.getTimestamp(dueDate);
						currentRequest.setDueDate(duedateTS);
					}
				}
			}
		}		
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "DueDateBasedOnReferenceDate - To set due date to reciept date + 6 working days";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
