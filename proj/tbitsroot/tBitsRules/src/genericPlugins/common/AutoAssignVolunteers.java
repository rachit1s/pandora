package common;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;

public class AutoAssignVolunteers implements IRule {

	public static final TBitsLogger LOG = TBitsLogger
	.getLogger(TBitsConstants.PKG_API);
	
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		RuleResult ruleResult = new RuleResult(true,
				"Auto assigning the volunteer.", true);
		;
		if(oldRequest != null)
		{
			ruleResult.setMessage("Ignore auto assigning on update.");
			return ruleResult;
		}
		RequestUser mustAssignees = null;
		try {
			Field assigneeField = Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), Field.ASSIGNEE);
			String newAssigneeUsers = APIUtil.getVolunteer(currentRequest
					.getSystemId(), currentRequest.getCategoryId().getTypeId(),
					ba.getSysConfigObject().getVolunteer());
			if ((newAssigneeUsers == null) || (newAssigneeUsers.length() == 0))
				LOG.error("No volunteer found corresponding to categort: "
						+ currentRequest.getCategoryId().getTypeId());
			else {
				String newAssigneeUser = newAssigneeUsers.split(",")[0];
				User u = User.lookupAllByUserLogin(newAssigneeUser);
				if (u == null)
					LOG.error("No user found corresponding to volunteer: "
							+ newAssigneeUser);
				else {
					mustAssignees = new RequestUser(currentRequest.getSystemId(), currentRequest.getRequestId(), u.getUserId(), 
													0, true, assigneeField.getFieldId());
					mustAssignees.setUser(u);
					// mustAssignees.setSystemId(currentRequest.getSystemId());
					// mustAssignees.setRequestId(currentRequest.getRequestId());
					// mustAssignees.setUserTypeId(UserType.ASSIGNEE);
					// mustAssignees.setUserId(u.getUserId());
					// mustAssignees.setOrdering(0);
					// mustAssignees.setIsPrimary(true);
				}
			}
		} catch (DatabaseException e) {
			LOG
					.error(
							"Database error occurred while getting the volunteer.",
							e);
			ruleResult = new RuleResult(true, "Unable to get the volunteer.",
					false);
		}
		if (mustAssignees != null) {
			Collection<RequestUser> assignees = currentRequest.getAssignees();
			if (assignees == null) {
				assignees = new ArrayList<RequestUser>();
			}
			boolean alreadyAssigned = false;
			for (RequestUser ru : assignees) {
				if (ru.getUserId() == mustAssignees.getUserId()) {
					System.out.println(" Assignee '"
							+ mustAssignees.getUserId() + "' matches with "
							+ ru.getUserId());
					alreadyAssigned = true;
					break;
				}
			}
			if (!alreadyAssigned) {
				assignees.add(mustAssignees);
				currentRequest.setAssignees(assignees);
			}
		}
		return ruleResult;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Auto Assign Volunteers";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
