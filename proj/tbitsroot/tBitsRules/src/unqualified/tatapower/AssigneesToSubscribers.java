/**
 * 
 */
package tatapower;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;

/**
 * @author Lokesh
 *
 */
public class AssigneesToSubscribers implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		RuleResult ruleResult = new RuleResult();
		String sysPrefix = ba.getSystemPrefix();
		boolean isApplicableToBA = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(sysPrefix);
		if (isApplicableToBA && (!isAddRequest)){
			ArrayList<RequestUser> assignees = currentRequest.getAssignees();
			for (RequestUser ru : assignees){
				ru.setUserTypeId(UserType.SUBSCRIBER);
			}
			currentRequest.setSubscribers(assignees);
			//currentRequest.setAssignees(new ArrayList<RequestUser>());
			ruleResult.setCanContinue(true);
			ruleResult.setSuccessful(true);
			ruleResult.setMessage("Successfully changed assignees to subscribers");			
		}
		else{
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Not applicable to this business area: " + ba.getDisplayName());
		}
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " - Changes assignees to subscribers";
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
