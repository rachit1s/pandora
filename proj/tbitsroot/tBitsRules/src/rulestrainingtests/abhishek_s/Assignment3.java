package sharan;

import java.sql.Connection;
import java.util.*;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;

public class Assignment3 implements IRule {

	TBitsLogger logger = TBitsLogger.getLogger("sharan");

	String status = "";
	User u;
	Field fassignee;
	Field fsubscribers;

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		// TODO Auto-generated method stub

		if (!ba.getSystemPrefix().equalsIgnoreCase("MyTest"))
			return new RuleResult();

		int sysid = ba.getSystemId();
		RuleResult ruleResult = new RuleResult();

		// Field balogger = logger;

		try {
			fsubscribers = Field.lookupBySystemIdAndFieldName(sysid,
					Field.SUBSCRIBER);
			int id = fsubscribers.getFieldId();
			fassignee = Field.lookupBySystemIdAndFieldName(sysid,
					Field.ASSIGNEE);
			status = currentRequest.get(Field.STATUS);

			if (status.equalsIgnoreCase("Pending")) {

				Collection<RequestUser> assignee = (Collection<RequestUser>) currentRequest
						.getObject(Field.ASSIGNEE);
				u = User.lookupAllByUserLogin("sharan");
				RequestUser newReqUser = new RequestUser(ba.getSystemId(),
						currentRequest.getRequestId(), u.getUserId(), 1, false,
						fassignee.getFieldId());
				assignee.add(newReqUser);
				// currentRequest.setObject(Field.ASSIGNEE, assignee);
			}

			else if (status.equalsIgnoreCase("Closed")) {
				ArrayList<RequestUser> assignees = (ArrayList<RequestUser>) currentRequest
						.getAssignees();
				// ArrayList<RequestUser> subscribers = (ArrayList<RequestUser>)
				// currentRequest.getSubscribers();
				// ArrayList<RequestUser> assignees1 = (ArrayList<RequestUser>)
				// currentRequest.getAssignees();
				for (RequestUser ru : assignees) {
					// ru.setUserTypeId(UserType.SUBSCRIBER);
					// int id =ru.getUserTypeI
					ru.setFieldId(id);
				}
				currentRequest.setSubscribers(assignees);
				assignees.clear();

			}

		} catch (Exception e) {
			System.out.println("rule fail");
		}
		return new RuleResult(true, "Succesful", true);
	}

	private String StringTokenizer(Collection<RequestUser> assignees) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
