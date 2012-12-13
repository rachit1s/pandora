/**
 * 
 */
package pyramid;

import java.sql.Connection;
import java.util.ArrayList;
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
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;

/**
 * @author Lokesh
 *
 */
public class IDCAssigneeToSubscriber implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		
		System.out.println("Executing IDCAssignee plugin");
		
		//String sysPrefix = "DCR343,DCR326,VDCR326,DCR345";
		boolean isRuleApplicable = PyramidUtils.isExistsInCommons(ba.getSystemPrefix());
		
		RuleResult ruleResult = new RuleResult();		
		if ((!isAddRequest)&& isRuleApplicable && (currentRequest.getParentRequestId() > 0)){
			Field field = null;
			try {
				field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), "IDCCompleted");
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			RequestEx idcReq = extendedFields.get(field);
			boolean isIDCComplete = idcReq.getBitValue();
			if (isIDCComplete){
				String assignees = currentRequest.get(Field.ASSIGNEE);
				if (!(assignees.trim().equals("") || (assignees == null))){				
					ArrayList<RequestUser> assigneeList = currentRequest.getAssignees();
					for(RequestUser ru:assigneeList){
						if (user.getUserId() ==  ru.getUser11Id()){
							ArrayList<RequestUser> tmpList = new ArrayList<RequestUser>();
							tmpList.add(ru);
							currentRequest.setSubscribers(tmpList);
							ru.setUserTypeId(UserType.SUBSCRIBER);
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

}
