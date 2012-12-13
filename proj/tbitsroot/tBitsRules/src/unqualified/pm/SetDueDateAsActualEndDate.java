package pm;

import java.util.Date;
import java.util.Hashtable;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;

public class SetDueDateAsActualEndDate implements IRule {

	public RuleResult execute(BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			String attachments) {
		RuleResult ruleResult = new RuleResult();
		Field aendDateField = null;
		try {
			aendDateField = Field.lookupBySystemIdAndFieldName(
					ba.getSystemId(), PMConstants.AEND_DATE);
		} catch (DatabaseException e1) {
			ruleResult.setMessage(PMConstants.ASTART_DATE
					+ " field is not defined in this BA. "
					+ "Please make sure that you specified actual");
			ruleResult.setSuccessful(false);
			return ruleResult;
		}

		if (aendDateField == null) {
			ruleResult.setMessage(PMConstants.ASTART_DATE
					+ " field is not defined in this BA. "
					+ "Please make sure that you specified actual");
			ruleResult.setSuccessful(false);
			return ruleResult;
		}
		
		Date aendDate = null;
		RequestEx aendDateO = extendedFields.get(aendDateField);
		if (aendDateO != null) {
			aendDate = aendDateO.getDateTimeValue();
		}
		Date aendDateOld = null;
		if(!isAddRequest)
		{
			RequestEx aendDateOldO = oldRequest.getExtendedFields().get(aendDateField);
			if (aendDateOldO != null) {
				aendDateOld = aendDateOldO.getDateTimeValue();
			}
			if ((aendDateOld == null)
					|| ((aendDate != null) && (!aendDateOld.equals(aendDate))))
			{
				currentRequest.setDueDate(Timestamp.getTimestamp(aendDate));
				ruleResult.setSuccessful(true);
			}
		}
		else if(aendDate != null)
		{
			currentRequest.setDueDate(Timestamp.getTimestamp(aendDate));
			ruleResult.setSuccessful(true);
		}
		
		return ruleResult;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Sets the due date as actual end date";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 2;
	}

}
