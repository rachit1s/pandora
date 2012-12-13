/**
 * 
 */
package pyramid;

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
public class RuleActivityPercentage implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		
		String actPctgComp = "actualpercentagecompleted";
		String actWtg = "ActivityPercentage";
		Field actPctgField = null;
		Field actWtgField = null;
		int systemId = ba.getSystemId();
		
		//String sysPrefix = "DCR343, DCR326,VDCR326,DCR345";
		boolean isRuleApplicable = PyramidUtils.isExistsInCommons(ba.getSystemPrefix());
		
		if (isRuleApplicable){
			try {
				Type statusType = currentRequest.getStatusId();
				actPctgField = Field.lookupBySystemIdAndFieldName(systemId, actPctgComp);		
				actWtgField = Field.lookupBySystemIdAndFieldName(systemId, actWtg);
				if ((actPctgField != null) && (actWtgField != null)){
					RequestEx aPReqEx = extendedFields.get(actPctgField);
					RequestEx aWReqEx = extendedFields.get(actWtgField);
					if ((aPReqEx != null) && (aWReqEx != null)){
						if (statusType.getName().equals("ReSubmissionRequired")){
							aPReqEx.setRealValue(aWReqEx.getRealValue());
						}
						else if (statusType.getName().equals("Approved")|| statusType.getName().equals("ApprovedWithComments")){
							aPReqEx.setRealValue(aWReqEx.getRealValue());
						}
						else{
							return new RuleResult(true, "Required fields do not exist, hence ignoring");
						}
						return new RuleResult(true, "Executed RuleActivityPercentage");
					}					
				}
				else{
					return new RuleResult(true, "Required fields do not exist, hence ignoring");
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
				return new RuleResult(true, e.getMessage());
			}
			return new RuleResult(true, "Executed RuleActivityPercentage");
		}
		else{			
			return new RuleResult(true, "Not applicable to this Business Area");
		}
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "RuleActivityPercentage -  Sets acitvity % complete relative to activity weightage if status is " +
				"rejected, approved or approved with comments";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
