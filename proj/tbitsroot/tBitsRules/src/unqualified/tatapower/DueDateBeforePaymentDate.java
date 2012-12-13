/**
 * 
 */
package tatapower;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.sql.Connection;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
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
public class DueDateBeforePaymentDate implements IRule {
	
	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		RuleResult ruleResult = new RuleResult();
		Timestamp dd = currentRequest.getDueDate();
		String sysPrefix = ba.getSystemPrefix();
		boolean isApplicableToBA = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(sysPrefix);
		int aSystemId = ba.getSystemId();
		if (isApplicableToBA){
			Field paymentDateField;
			try {
				paymentDateField = Field.lookupBySystemIdAndFieldName(aSystemId, TataPowerUtils.INVOICE_PAYMENT_DATE);
				if (paymentDateField == null){
					ruleResult.setCanContinue(true);
					ruleResult.setMessage("No payment field found, hence continuing...");
				}
				else{
					RequestEx pdRequestEx = extendedFields.get(paymentDateField);
					Timestamp pdTS = pdRequestEx.getDateTimeValue(); 
					if ((dd !=  null) && (pdTS != null)){
						if (dd.getTime() > pdTS.getTime()){
							LOG.info("Changed due date from " + dd.toString() + " to " + pdTS);
							System.out.println("Changed due date from " + dd.toString() + " to " + pdTS);
							currentRequest.setDueDate(pdTS);
							ruleResult.setCanContinue(true);
							ruleResult.setMessage("Due date cannot be after invoice payment date. Please select proper payment date or due date.");
						}
						else{
							ruleResult.setCanContinue(true);
							ruleResult.setMessage("Due date is less than payment date, hence continue.");
						}
					}
					else{
						ruleResult.setCanContinue(true);
						ruleResult.setMessage("Due date or payment date is empty.");
					}
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		else{			
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Not applicable to the current business area");
		}
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "DueDateAlwaysBeforePaymentDate - Due date after calculation should always be below invoice payment date";
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
	 * @throws DatabaseException 
	 */
	public static void main(String[] args) throws DatabaseException {
		BusinessArea ba =BusinessArea.lookupBySystemPrefix("Bill");
		Request req = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), 14);
		User root = User.lookupAllByUserId(1);
		IRule irule = new StatusBasedAssigneesFor60Percent();
		/*irule.execute(ba, null, req, TBitsConstants.SOURCE_CMDLINE, root, 
				req.getExtendedFields(), false, "");*/

	}

}
