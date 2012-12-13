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
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * <code>IssuedForPurchaseRule</code> ensures that if a drawing has to be issued for procurement then its status should be
 * IFC/IFR.
 *
 */
public class IssuedForPurchaseRule implements IRule {

	private static final String APPROVED = "Approved";
	private static final String IFR = "IFR";
	private static final String IFC = "ifc";
	private static final String ISSUEDFORPROCUREMENT = "issuedforprocurement";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		String sysPrefix = ba.getSystemPrefix();
		int aSystemId = ba.getSystemId();
		RuleResult ruleResult = new RuleResult();
		boolean isApplicable = PyramidUtils.inProperty345SysPrefixes(sysPrefix);
		if (isApplicable){
			try {
				Field ifpField = Field.lookupBySystemIdAndFieldName(aSystemId, ISSUEDFORPROCUREMENT);
				if (ifpField == null){
					ruleResult.setCanContinue(true);
					ruleResult.setMessage("Not applicable to this BA: " + sysPrefix + "as issued for purchase field is not found");
				}
				else{
					RequestEx ifpReqEx = extendedFields.get(ifpField);
					if (ifpReqEx.getBitValue()){
						Type curStatusId = currentRequest.getStatusId();
						if (isAdmissibleStatus(curStatusId.getName())){
							ruleResult.setCanContinue(true);
							ruleResult.setSuccessful(true);
						}
						else{
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("If \"Issued for purchase\" has to be set to true, then the status should be IFC/IFR/Approved. So, please select appropriate status or uncheck \"Issued for purchase\"");
						}
					}
					else{
						ruleResult.setCanContinue(true);
						ruleResult.setMessage("Not applicable");
					}
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		else
		{
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Not applicable to this BA: " + sysPrefix);
		}
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "IssuedForPurchaseRule - \"Issued for purchase\" can only be true if status is either IFC/IFR/Approved";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private boolean isAdmissibleStatus(String statusName){
		if (statusName.equals(IFC) || statusName.equals (IFR) || statusName.equals(APPROVED))
			return true;
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
