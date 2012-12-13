package sharan;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;

import org.jfree.util.Log;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class AssignmentRule2 implements IRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("sharan");
	Field assigneeobj;
	String ass;
	User u;
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest)  {
		// TODO Auto-generated method stub

		if (!ba.getSystemPrefix().equalsIgnoreCase("testba"))
			return new RuleResult();

		int sysid = ba.getSystemId();
		try {
		 assigneeobj = Field.lookupBySystemIdAndFieldName(sysid, Field.ASSIGNEE);
		  ass=currentRequest.get(Field.ASSIGNEE);
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

		
		
			//if(Field.ASSIGNEE.equals(""))
			if(ass.equals(""))
			{
			Collection<RequestUser> assignee = currentRequest.getAssignees();
			//if(assignee==null)
			//{
				
				try {
					u = User.lookupAllByUserLogin("sharan");
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				RequestUser newReqUser = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(),
						u.getUserId(), 1, false, assigneeobj.getFieldId());
				assignee.add(newReqUser);
			}
			//}
			
	
		return new RuleResult(true,"Succesful",true);
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return ("auto assignee");
	}

}
