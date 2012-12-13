package imp;

import java.sql.Connection;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class ImpWeightageComputationRule implements IRule {

	/**
	 * This rule calculates the factors for IMP.
	 * The Rule is simple:
	 * The whole calculation happens if the submissionfiletype is not of crs type.
	 * 
	 * If the decision is "Approved", the factor is 100.
	 * Else if 
	 * 	First Submission - 70%, 
	 * 	Second submission - 80%, 
	 * 	Third submission - 90%, 
	 * 	Fourth Submission - 95%
	 * 
	 * How to identify which submission is it: 
	 * 	If the flow status changes to UnderReview from PendingReceipt, it is the first submission.
	 * 	Otherwise if the status changes to UnderReview From ReturnedWithDecision, it can be second, third or fourth. 
	 * 	Which can be identified based on previous factor. If previous factor is 70, then current submission is 
	 * 	second and so on ...
	 * 
	 * There are two kinds of factor calculations one for vendor and other for owner. 
	 * The logic remains same, just the fields are different.
	 * 
	 */
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		if(!ba.getSystemPrefix().equalsIgnoreCase("FMG_IMP"))
			return new RuleResult();
		
		String[] crsTypes = new String[]{"CRS"};
		String submissionType =  ((Type) currentRequest.getObject("SubmissionFileType")).getName();
		for(String s:crsTypes)
		{
			if(s.equalsIgnoreCase(submissionType))
			{
				return new RuleResult();
			}
		}
		
		Field factorField = null;
		Field weightageField = null;
		Field percentCompleteField = null;
		String decision = null;
		String currFlowStatus = null;
		String prevFlowStatus = null;
		String originator =  ((Type) currentRequest.getObject("Originator")).getName();
		try {
			if (originator.equalsIgnoreCase("HERZOG")) {
				factorField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "v_Factor");
				weightageField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "Weightage");
				percentCompleteField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "ActualComplete");
				decision = currentRequest.get("DecisionToVendor");
				currFlowStatus = currentRequest.get("FlowStatusWithVendor");
				if(!isAddRequest)
					prevFlowStatus = oldRequest.get("FlowStatusWithVendor");
			} else {
				factorField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "o_Factor");
				weightageField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "OwnerWeightage");
				percentCompleteField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "OwnerActualComplete");
				decision = currentRequest.get("severity_id");
				currFlowStatus = currentRequest.get("status_id");
				if(!isAddRequest)
					prevFlowStatus = oldRequest.get("status_id");
			}
			int factor = 0;
			
			if( (decision != null) && decision.equalsIgnoreCase("Approved") 
					&& (currFlowStatus != null) && currFlowStatus.equalsIgnoreCase("ReturnedWithDecision"))
			{
				factor = 100;
			}
			else
			{
				if(currFlowStatus.equalsIgnoreCase("UnderReview"))
				{
					//Pick the factor next submission after locating the previous submission
					int previousFactor = ((Double) currentRequest.getObject(factorField)).intValue();
					
					if((prevFlowStatus != null) && prevFlowStatus.equalsIgnoreCase("ReturnedWithDecision"))
					{
						int [] submissions = {70, 80, 90, 95};
						
						for(int i=0; i < submissions.length; i++)
						{
							if(submissions[i] == previousFactor)
							{
								if((i + 1) < submissions.length)
								{
									factor = submissions[i + 1];
								}
							}
						}
					}
					else if(previousFactor == 0)
					{
						factor = 70;
					}
				}
			}
			
			if(factor != 0)
			{
				currentRequest.setObject(factorField, new Double(factor));
				double weightage = (Double) currentRequest.getObject(weightageField);
				currentRequest.setObject(percentCompleteField, new Double((double)(factor*weightage)/100));
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			RuleResult rr = new RuleResult(true, "Unable to calculate the weightages. Reason: " + e.getMessage() + "\r\n" + e.getDescription());
			rr.setSuccessful(false);
		}
		RuleResult rr = new RuleResult();
		rr.setCanContinue(true);
		rr.setSuccessful(true);
		return rr;
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "ImpWeightageComputationRule";
	}

}
