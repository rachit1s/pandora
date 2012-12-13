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
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * IDCStatusComplete is the plug-in which enforces the rule that, the person who initiated the IDC, should
 * complete the IDC. Any other user who tries to close the rule will not be allowed to so and will be 
 * prompted with an error message.
 */
public class IDCStatusComplete implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		
		//String sysPrefix = "DCR343,DCR326,VDCR326,DCR345";
		boolean isRuleApplicable = PyramidUtils.isExistsInCommons(ba.getSystemPrefix());
		
		if (isRuleApplicable){
			int sysId = ba.getSystemId();
			Type curStatus = currentRequest.getStatusId();
			if ((currentRequest.getParentRequestId() > 0) && (curStatus.getName().equals("IDCComplete"))){			
				int maxActionId = oldRequest.getMaxActionId();
				while(maxActionId > 0){				
					try {
						Action action = Action.lookupBySystemIdAndRequestIdAndActionId(sysId, currentRequest.getRequestId(), maxActionId);
						if (action!=null){
							System.out.println("Action id:" + action.getActionId());
							int statusId = action.getStatusId();
							Type statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(sysId, Field.STATUS, "IDCInitiated");

							if (statusType.getTypeId()== statusId){
								boolean isLogger = false;
								ArrayList<Integer> loggersList = action.getLoggerIds();
								for (Integer loggerId: loggersList){									
									if (user.getUserId()== loggerId){
										isLogger = true;
										break;
									}
									else{
										continue;
									}
								}
								
								if (isLogger){
									return new RuleResult(true, "Found the logger", true);									
								}
								else{
									return new RuleResult(false, "Cannot set the status to \"IDC complete\" as you are not authorized", false);
								}
							}
							else{
								maxActionId--;
								continue;
							}
						}
						else{
							maxActionId--;
							continue;
						}
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
				}
				return new RuleResult (true, "IDC was not initiated previous to this");
			}
			else{
				return new RuleResult(true);
			}
		}
		else
			return new RuleResult(true);
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "IDCStatusComplete - \"Status Complete\" can only be set by the person who initiated the IDC";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}	
}
