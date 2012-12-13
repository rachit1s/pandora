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

/**
 * This plugin is for Pyramid transmittal. This clears the CC, loggers and subscribers lists and 
 * retains only the assignees.
 */

/**
 * @author Lokesh
 *
 */
public class MailOnlyToAssignees implements IRule {	

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IPostRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		boolean isRuleApplicable = TataPowerUtils.isExistsInOnlyAssigneesRuleProperty(ba.getSystemPrefix());
		
		RuleResult ruleResult = new RuleResult();
		if (isRuleApplicable){
			currentRequest.setCcs(new ArrayList<RequestUser>());
			currentRequest.setSubscribers(new ArrayList<RequestUser>());
			currentRequest.setNotifyLoggers(false);
		}
		ruleResult.setSuccessful(true);
		return ruleResult;
	}
	

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IPostRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " - Clears the CC, loggers and subscribers list before sending the mail";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IPostRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
