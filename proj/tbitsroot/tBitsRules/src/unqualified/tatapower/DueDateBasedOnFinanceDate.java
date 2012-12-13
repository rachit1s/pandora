/**
 * 
 */
package tatapower;

import java.sql.Connection;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * @author Lokesh
 *
 */
public class DueDateBasedOnFinanceDate implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		RuleResult ruleResult = new RuleResult();
		try {
			String sysPrefix = ba.getSystemPrefix();
			int systemId = ba.getSystemId();
			boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(sysPrefix);
			Type categoryType = currentRequest.getCategoryId();
			String deptName = categoryType.getName();			
			boolean isAllowedDept = TataPowerUtils.isExistsInString(TataPowerUtils.getProperty(
										TataPowerUtils.TATAPOWER_DEPT_NAMES_FOR_FINANCE), deptName);
			if (isApplicable && isAllowedDept){
				int finDateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.TATAPOWER_FINANCE_DATE_OFFSET);
				Field financeDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FINANCE_RECIEPT_DATE);
				TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, financeDateField, finDateOffset, extendedFields, isAddRequest);
				ruleResult.setCanContinue(true);
			}
			else{
				ruleResult.setCanContinue(true);
				ruleResult.setMessage("Not applicable.");
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Sets due date to 3 days from finance recieved date in case of 'HR/procurement/ Health & safety' bills.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
