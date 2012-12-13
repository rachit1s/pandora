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
public class CategoryElectricalAndCorrespondingTypes implements IRule {
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		Type elecType1, elecType2, elecType3;
		Type requestType = currentRequest.getRequestTypeId();
		String reqTypeName = requestType.getName();
		Type categoryType = currentRequest.getCategoryId();
		String catTypeName = categoryType.getName();		
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(ba.getSystemPrefix());
		boolean isPermissibleReqType = (reqTypeName.equals(TataPowerUtils.TYPE_ELECTRICAL_RUNNING) || 
											reqTypeName.equals(TataPowerUtils.TYPE_ELECTRICAL_FINAL)) ||
												reqTypeName.equals(TataPowerUtils.TYPE_ELECTRICAL_THIRTY);
		RuleResult ruleResult = new RuleResult();
		try {
			elecType1 = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), Field.REQUEST_TYPE, TataPowerUtils.TYPE_ELECTRICAL_RUNNING);
			elecType2 = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), Field.REQUEST_TYPE, TataPowerUtils.TYPE_ELECTRICAL_FINAL);
			elecType3 = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), Field.REQUEST_TYPE, TataPowerUtils.TYPE_ELECTRICAL_THIRTY);
		} catch (DatabaseException e) {
			e.printStackTrace();
			return new RuleResult(false, "Cannot continue as database error occurred.\n" + e.getMessage());
		}
		if (isApplicable){			
			if (isPermissibleReqType && (!catTypeName.equals(TataPowerUtils.ELECTRICAL))){
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("If 'Type' is '"+ elecType1.getDisplayName()+ "/" + elecType2.getDisplayName() + "/" + elecType3.getDisplayName() 
						+ "' then 'Department' has to be 'Electrical'.");
			}/*else if (catTypeName.equals(TataPowerUtils.ELECTRICAL) && (!isPermissibleReqType || )){
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("If 'Department' is 'Electrical' then 'Type' also has to be '" + elecType1.getDisplayName()+ "/" + elecType2.getDisplayName() 
						+ "/" + elecType3.getDisplayName() + "/Others'.");				
			}*/
			else{
				ruleResult.setCanContinue(true);
				ruleResult.setSuccessful(true);
			}
		}
		else{
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Not applicable to this business area.");
		}		
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " - Checks if the 'Type' is one of the Electrical types" +
				"then category should be 'Electrical'.";
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
