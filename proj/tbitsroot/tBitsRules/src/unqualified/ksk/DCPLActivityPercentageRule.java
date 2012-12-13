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
public class DCPLActivityPercentageRule implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		
		return handleExecute(ba, oldRequest, currentRequest, isAddRequest);
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getName() + ": calculates activity percentage for DCPL submission.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		return 2;
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
		double activityPercentage = 0.0;
		
		if (ba.getSystemPrefix().trim().equals("DCPL")){
			if (!isAddRequest) {
				String prevStatus = oldRequest.getStatusId().getName().trim();
				String curStatus = currentRequest.getStatusId().getName().trim();
				
				if (!prevStatus.equals(KSKUtils.STATUS_A2) 
					&& curStatus.equals(KSKUtils.STATUS_A2)){				
					activityPercentage = 1.0;					
				}
				else if (!prevStatus.equals(KSKUtils.STATUS_A3) 
						&& curStatus.equals(KSKUtils.STATUS_A3)){
					activityPercentage = 0.8;
				}
				else if (!prevStatus.equals(KSKUtils.STATUS_A4) 
						&& curStatus.equals(KSKUtils.STATUS_A4)){
					activityPercentage = 0.5;
				}
				else if (!prevStatus.equals(KSKUtils.STATUS_A5) 
						&& curStatus.equals(KSKUtils.STATUS_A5)){
					activityPercentage = 0.3;
				}
				else{
					ruleResult.setCanContinue(true);
					ruleResult.setMessage("Not applicable for this business area.");
				}
				
				if (activityPercentage > 0){
					String activityWeightageStr = currentRequest.get("Weightage");
					if ((activityWeightageStr != null) && (!activityWeightageStr.trim().equals(""))){
						Float activityWeightage = Float.parseFloat(activityWeightageStr);
						try {
							currentRequest.setExReal("ActualPercentageComplete", (activityPercentage * activityWeightage));
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
					ruleResult.setMessage("Not applicable for the current status.");
				}
			}
		}
		return ruleResult;
	}

}
