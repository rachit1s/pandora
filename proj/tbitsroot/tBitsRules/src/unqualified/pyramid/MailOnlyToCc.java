package pyramid;
import java.util.ArrayList;
import java.util.Hashtable;

import transbit.tbits.api.IPostRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;

/**
 * This plugin is for Pyramid transmittal. This clears the assignee and subscriber list and 
 * retains only the cclist in order to avoid sending mails twice:
 * 1. Once when the transmittal is created
 * 2. Once from the latest documents BA.
 */

/**
 * @author Lokesh
 *
 */
public class MailOnlyToCc implements IPostRule {	

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IPostRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean)
	 */
	public RuleResult execute(BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest) {
		
		//String sysPrefix = "DCR343, DCR326,VDCR326,DCR345";
		boolean isRuleApplicable = PyramidUtils.isApplicableToBA(ba.getSystemPrefix());
		
		RuleResult ruleResult = new RuleResult();
		if (isRuleApplicable && (!isAddRequest)){	
			currentRequest.setAssignees(new ArrayList<RequestUser>());
			currentRequest.setSubscribers(new ArrayList<RequestUser>());	
			ArrayList<RequestUser> ccUsers = currentRequest.getCcs();
			if (ccUsers != null){
				for (RequestUser ru : ccUsers){
					ru.setUserTypeId(UserType.CC);
				}
				currentRequest.setCcs(ccUsers);
			}				
		}
		ruleResult.setSuccessful(true);
		return ruleResult;
	}	

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IPostRule#getName()
	 */
	public String getName() {
		return "MailOnlyToCc - Clears the assignee and subscribers list before sending the mail";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IPostRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
