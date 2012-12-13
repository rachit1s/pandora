package pm;

import java.util.Hashtable;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;

public class ActionItemsValidation implements IRule {
	static String SYS_PREFIX = "CR";
	public RuleResult execute(BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			String attachments) {
		if(!ba.getSystemPrefix().equalsIgnoreCase(SYS_PREFIX))
		{
			return new RuleResult(true, "ActionItemsValidation: skipping as the business area is not '" + SYS_PREFIX + "'");
		}
		if(
				currentRequest.getStatusId().getName().equalsIgnoreCase(PMConstants.CLOSED)
				&& !oldRequest.getStatusId().getName().equalsIgnoreCase(PMConstants.CLOSED)
		)
		{
			Hashtable<String, String> subrequests = currentRequest.getSubRequests();
			for(String subReqWithSysPrefix:subrequests.keySet())
			{
				int loc = subReqWithSysPrefix.indexOf('#');
				if(loc > -1)
				{
					String reqStr = subReqWithSysPrefix.substring(loc+1);
					int subReqId = -1;
					try{	
						subReqId = Integer.parseInt(reqStr);
						Request subRequest = Request.lookupBySystemIdAndRequestId(currentRequest.getSystemId(), subReqId);
						if(!subRequest.getStatusId().getName().equalsIgnoreCase(PMConstants.CLOSED))
						{
							return new RuleResult(false, "You can not close a request untill all subrequests are closed.", true);
						}
					}catch (NumberFormatException e) {
						System.out.println("ActionItemsValidation: Invalid requestId: '" + reqStr + "'");
						continue;
					} catch (DatabaseException e) {
						// TODO Auto-generated catch block
						System.out.println("Error while getting the subrequest for '" + subReqId + "'");
						e.printStackTrace();
						continue;
					}
				}
			}
			return new RuleResult(true, "ActionItemsValidation: All subrequests are closed. Finished running the rule.", true);
		}
		else
		{
			return new RuleResult(true, "ActionItemsValidation: Request is not being closed so no need to check subrequests.", true);
		}
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Action Items should be closed before";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 1;
	}

}
