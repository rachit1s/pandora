/**
 * 
 */
package tatapower;

import java.sql.Connection;
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
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * @author Lokesh
 *
 */
public class StatusClosedBasedDueDateReset implements IRule {	
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		
		RuleResult ruleResult = new RuleResult();
		String sysPrefix = ba.getSystemPrefix();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(sysPrefix );
		Type statusType = currentRequest.getStatusId();
		
		if (isApplicable){
			if (statusType.getName().equals("Closed")){
				currentRequest.setDueDate(null);
			}			
		}
		ruleResult.setCanContinue(true);
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return  this.getClass().getSimpleName() + " - If status is closed, due-date should be set to null.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}
		
	/**
	 * @param args
	 * @throws DatabaseException 
	 */
	public static void main(String[] args) throws DatabaseException {
		// TODO Auto-generated method stub
		BusinessArea ba =BusinessArea.lookupBySystemPrefix("Bill");
		Request req = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), 10);
		User root = User.lookupAllByUserId(1);
		IRule irule = new StatusClosedBasedDueDateReset();
		/*irule.execute(ba, null, req, TBitsConstants.SOURCE_CMDLINE, root, 
				req.getExtendedFields(), false, "");*/
	 }
}
