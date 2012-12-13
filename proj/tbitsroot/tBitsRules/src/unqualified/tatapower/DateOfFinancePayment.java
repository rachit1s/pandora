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
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;

/**
 * @author Lokesh
 *
 */
public class DateOfFinancePayment implements IRule {
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		RuleResult ruleResult = new RuleResult();
		String sysPrefix = ba.getSystemPrefix();
		int systemId = ba.getSystemId();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(sysPrefix);
		if (isApplicable){
			try {
				Field finRecieptDate = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FINANCE_RECIEPT_DATE);
				Field finPaymentDate = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FIN_PAYMENT_DATE);
				if ((finRecieptDate != null) && (finPaymentDate != null)){
					RequestEx frDateEx = extendedFields.get(finRecieptDate);
					RequestEx fpDateEx = extendedFields.get(finPaymentDate);
					if ((frDateEx != null) && (fpDateEx != null)){
						Timestamp dateTimeValue = frDateEx.getDateTimeValue();
						Timestamp dateTimeValue2 = fpDateEx.getDateTimeValue();
						if ((dateTimeValue != null) && (dateTimeValue2 != null)){
							if (dateTimeValue2.getTime() < dateTimeValue.getTime()){
								ruleResult.setCanContinue(false);
								ruleResult.setMessage("Finance payment date cannot occur before finance recieved date.");
							}
							else{
								ruleResult.setCanContinue(true);
								ruleResult.setSuccessful(true);
							}
						}
						else{
							ruleResult.setCanContinue(true);
						}
					}
					else{
						ruleResult.setCanContinue(true);
						ruleResult.setMessage("Required extended fields were not found.");
					}
				}
				else{
					ruleResult.setCanContinue(true);
					ruleResult.setMessage("Required date fields where not found");
				}				
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		else{
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Not applicalbe to the BA: " + ba.getDisplayName());
		}
		
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " - Finance Payment date cannot occur before Finance reciept date.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
