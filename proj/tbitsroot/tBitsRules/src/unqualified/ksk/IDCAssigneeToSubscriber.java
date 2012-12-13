/**
 * 
 */
package ksk;

import java.sql.Connection;
import java.util.ArrayList;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;

/**
 * @author Lokesh
 *
 */
public class IDCAssigneeToSubscriber implements IRule {

	

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "IDCAssigneeToSubscriber - IDC assignee to subscriber on status 'IDC Complete'";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {

		
		//String sysPrefix = "DCR343,DCR326,VDCR326,DCR345";
		boolean isRuleApplicable = KSKUtils.isExistsInString(KSKUtils.IDCBALIST, ba.getSystemPrefix());
		
		RuleResult ruleResult = new RuleResult();		
		if ((!isAddRequest)&& isRuleApplicable && (currentRequest.getParentRequestId() > 0)){
			Field field = null;
			try {
				field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "CommentsComplete");
				if (field == null){
					ruleResult.setCanContinue(true);
					ruleResult.setMessage("Appropriate field not found.");
					return ruleResult;
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			
			
			String isCommentsCompleteString = currentRequest.get(field.getName());
			boolean isIDCComplete = false;
			if (isCommentsCompleteString != null)
				isIDCComplete = Boolean.getBoolean(isCommentsCompleteString);
			if (isIDCComplete){
				String assignees = currentRequest.get(Field.ASSIGNEE);
				if (!(assignees.trim().equals("") || (assignees == null))){				
					ArrayList<RequestUser> assigneeList = (ArrayList<RequestUser>) currentRequest.getAssignees();
					for(RequestUser ru:assigneeList){
						if (user.getUserId() ==  ru.getUserId()){
							ArrayList<RequestUser> tmpList = new ArrayList<RequestUser>();
							tmpList.add(ru);
							currentRequest.setSubscribers(tmpList);
							Field subscriberField = null;
							try {
								subscriberField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), Field.SUBSCRIBER);
							} catch (DatabaseException e) {
								e.printStackTrace();
								ruleResult.setCanContinue(true);
								ruleResult.setMessage("Database exception occurred while looking up for subscriber field.");
								return ruleResult;
							}
							if (subscriberField != null)
								ru.setFieldId(subscriberField.getFieldId());
							else{
								ruleResult.setCanContinue(true);
								ruleResult.setMessage("Rule failed as subscriber field, not be found/invalid field name: "
										+ Field.SUBSCRIBER);
								return ruleResult;
							}
							assigneeList.remove(ru);
							break;
						}
						else
							continue;
					}					
					currentRequest.setAssignees(assigneeList);
					ruleResult.setSuccessful(true);					
				}
			}
			else{
				ruleResult.setCanContinue(true);
				ruleResult.setMessage("Not applicable as no assignees were found.");
			}
		}
		return ruleResult;
	}

}
