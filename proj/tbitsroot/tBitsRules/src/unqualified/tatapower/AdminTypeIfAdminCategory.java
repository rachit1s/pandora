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
public class AdminTypeIfAdminCategory implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		Type requestType = currentRequest.getRequestTypeId();
		String reqTypeName = requestType.getName();
		Type categoryType = currentRequest.getCategoryId();
		String catTypeName = categoryType.getName();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(ba.getSystemPrefix());
		RuleResult ruleResult = new RuleResult();
		if (isApplicable){			
			if ((reqTypeName.equals(TataPowerUtils.TYPE_ADMIN_WITH_SAP_PO) || reqTypeName.equals(TataPowerUtils.TYPE_ADMIN_WITHOUT_SAP_PO)) 
					&& ( (!catTypeName.equals(TataPowerUtils.ADMINISTRATION)) && (!catTypeName.equals(TataPowerUtils.HR)) 
							&& (!catTypeName.equals(TataPowerUtils.SAFETY)) && (!catTypeName.equals(TataPowerUtils.SECURITY))
							&&  (!catTypeName.equals(TataPowerUtils.PROCUREMENT))) ){
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("If 'Type' is '" + requestType.getDisplayName() + "' then 'Department' has to be 'Administration' and vice versa.");
			}
			else{
				ruleResult.setCanContinue(true);
				ruleResult.setSuccessful(true);
			}
		}
		else{
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Not applicable to this business area");
		}		
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "AdminTypeIfAdminCategory - If Type = Administration then Category has to be Administration";
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
