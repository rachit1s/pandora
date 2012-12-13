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
 * @author Lokesh
 *
 */
public class DocumentUploadWithPendingStatus implements IRule {	
	private static final String PENDING = "Pending";
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		RuleResult ruleResult = new RuleResult();
		String sysPrefix = ba.getSystemPrefix();
		boolean isApplicable = PyramidUtils.inProperty345SysPrefixes(sysPrefix);
		if (isApplicable){
			Type curStatusId = currentRequest.getStatusId();
			if (curStatusId.getName().equals(PENDING))
				if (((attachments != null) && (!attachments.equals("")) )){
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("Cannot upload a document/drawing if the status is  still \"Pending\"");
				}
				else{
					ruleResult.setCanContinue(true);
				}
			else
				ruleResult.setCanContinue(true);
		}		
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "DocumentUploadWithPendingStatus - If status is pending, user cannot upload a document.";
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
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
