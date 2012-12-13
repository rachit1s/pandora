/**
 * 
 */
package pyramid;

import java.sql.Connection;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * IDCStatusIntiated is the plug-in which ensures that once an IDC is initiated, then the IDC's next status 
 * should only be IDC complete. It cannot have any other status apart from IDC complete.
 */
public class IDCStatusIntiated implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		
		//String sysPrefix = "DCR343,DCR326,VDCR326,DCR345";
		boolean isRuleApplicable = PyramidUtils.isExistsInCommons(ba.getSystemPrefix());
		
		if (isRuleApplicable && (currentRequest.getParentRequestId() > 0) && (!isAddRequest)){
			Type oldStatusType = oldRequest.getStatusId();
			if (oldStatusType.getName().equals("IDCInitiated")){
				Type curStatusType = currentRequest.getStatusId();
				if (curStatusType.getName().equals("IDCInitiated") ||
					curStatusType.getName().equals("IDCComplete")){
					return new RuleResult (true);
				}
				else{
					return new RuleResult(false, "Either \"IDC Initiated\" or \"IDC Complete\" can be selected once IDC is initiated");
				}
			}
		}		
		return new RuleResult(true);
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "IDCStatusInitiated - If IDC is initiated status can only be \"IDC Complete\"";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
