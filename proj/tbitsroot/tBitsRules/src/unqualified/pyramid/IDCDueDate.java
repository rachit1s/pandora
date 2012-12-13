/**
 * 
 */
package pyramid;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.sql.Connection;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;

/**
 * IDCDueDate is the plugin which ensures that the IDC's due date does not exceed that of the 
 * parent document's due date.
 * 
 * Ex: If the due date of a drawing in DCR is 26th Jan 2009 then the due date of the IDC created 
 * for that drawing, should be before 26th Jan 2009 else, an error message is prompted to the user. 
 */
public class IDCDueDate implements IRule {
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		
		//String sysPrefix = "DCR343,DCR326,VDCR326,DCR345";
		boolean isRuleApplicable = PyramidUtils.isExistsInCommons(ba.getSystemPrefix());
		
		Request parentRequest = null;
		RuleResult ruleResult = new RuleResult();
		int parentId = currentRequest.getParentRequestId();
		if ((parentId > 0) && isRuleApplicable){
			try {
				parentRequest = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), parentId);
			} catch (DatabaseException e) {
				LOG.severe("Database error occurred while retrieving a parent id.");
				e.printStackTrace();
			}
			Timestamp parentTS = parentRequest.getDueDate();
			Timestamp currentTS = currentRequest.getDueDate();
			
			if (parentTS != null){			
				if (currentTS.compareTo(parentTS) > 0){
					ruleResult.setSuccessful(false);
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Due date: " + currentTS.toCustomFormat("MM/dd/yyyy") +
							" should not be later than the parent due date: " + parentTS.toCustomFormat("MM/dd/yyyy"));
				}			
				else{
					ruleResult.setSuccessful(true);
				}
			}
		}
		else{
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Rule not applicable");			
		}
		return ruleResult;
	}	

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {		
		return "IDCDueDate - Checks if sub-request's(IDC) due date is not later than parent's due date.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static void main(String[] args){
		
	}
}
