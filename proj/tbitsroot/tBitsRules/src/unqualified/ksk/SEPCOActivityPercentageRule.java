/**
 * 
 */
package ksk;

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
import transbit.tbits.domain.User;

/**
 * @author lokesh
 *
 */
public class SEPCOActivityPercentageRule implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getName() + ": calculates activity percentage for SEPCO submission.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		return 1;
	}

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		return handleExecute(ba, oldRequest, currentRequest, isAddRequest);
	}

	/**
	 * @param ba
	 * @param oldRequest
	 * @param currentRequest
	 * @param isAddRequest
	 * @return
	 * @throws NumberFormatException
	 */
	private RuleResult handleExecute(BusinessArea ba, Request oldRequest,
			Request currentRequest, boolean isAddRequest)
			throws NumberFormatException {
		RuleResult ruleResult = new RuleResult();
		
		if (ba.getSystemPrefix().trim().equals("SEPCO")){
			if (!isAddRequest)			
				if (oldRequest.getStatusId().getName().trim().equals(KSKUtils.STATUS_PENDING_SUBMISSION) 
						&& currentRequest.getStatusId().getName().trim().equals(KSKUtils.STATUS_DOCUMENT_RECEIVED)){				
					String activityWeightageStr = currentRequest.get("Weightage");
					if ((activityWeightageStr != null) && (!activityWeightageStr.trim().equals(""))){
						Float activityWeightage = Float.parseFloat(activityWeightageStr);
						try {
							currentRequest.setExReal("ActualPercentageComplete", (0.3 * activityWeightage));
						} catch (DatabaseException e) {
							e.printStackTrace();
							ruleResult.setCanContinue(true);
							System.out.println("Activity percentage could not be set due to database exception.");
						}
						ruleResult.setCanContinue(true);
						ruleResult.setSuccessful(true);
					}
				}
				else{
					ruleResult.setCanContinue(true);
					ruleResult.setMessage("Not applicable for this business area.");
				}
		}
		return ruleResult;
	}

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		return handleExecute(ba, oldRequest, currentRequest, isAddRequest);
	}

}
