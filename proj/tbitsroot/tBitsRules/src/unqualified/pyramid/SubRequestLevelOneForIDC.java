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
import transbit.tbits.domain.User;

/**
 *<code>SubRequestLevelOneForIDC</code> ensures that the IDC is created only if its 
 *parent is document.  
 *
 */
public class SubRequestLevelOneForIDC implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		
		//String sysPrefix = "DCR343,DCR326,VDCR326,DCR345";
		boolean isRuleApplicable = PyramidUtils.isExistsInCommons(ba.getSystemPrefix());
		
		//System.out.println("#######################Level One");
		RuleResult ruleResult = new RuleResult();
		if (isRuleApplicable){
			int parentId = currentRequest.getParentRequestId();
			//Check if new sub-request/IDC being added
			if (isAddRequest && (parentId > 0)){
				Request parentRequest = null;
				try {
					parentRequest = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), parentId);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
				//Check if parent's parent exists/not
				if (parentRequest.getParentRequestId() > 0){
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Cannot create IDC as current IDC's parent is also an IDC");
					return ruleResult;
				}
				else{
					ruleResult.setSuccessful(true);
					return ruleResult;
				}
			}
			else{
				ruleResult.setCanContinue(true);
				return ruleResult;
			}
		}else{
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Not applicable to this Business Area");
			return ruleResult;
		}				
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {		
		return "SubRequestLevelOneForIDC - IDC can only be created if its parent's parent id is 0";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
