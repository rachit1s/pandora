/**
 * 
 */
package tatapower;

import java.sql.Connection;
import java.util.ArrayList;
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
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * @author Lokesh
 *
 */
public class StatusBasedAssigneesForTypeElecThirty implements IRule {
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		
			RuleResult ruleResult = new RuleResult();
			String sysPrefix = ba.getSystemPrefix();
			int aSystemId = ba.getSystemId();
			int aRequestId = currentRequest.getSystemId();
			boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(sysPrefix );
			Type requestType = currentRequest.getRequestTypeId();
			if (isApplicable && (requestType.getName().equals(TataPowerUtils.TYPE_ELECTRICAL_THIRTY))){
				Type statusType = currentRequest.getStatusId();
				String curStatus = statusType.getName();
				ArrayList<RequestUser> ruList = null;
				try {
					if (curStatus.equals(TataPowerUtils.STATUS_DCC)){
						ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_ELECTRICAL_30_DCC_ASSIGNEES);
					}
					else if (curStatus.equals(TataPowerUtils.STATUS_UDSI)){
						ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_ELECTRICAL_30_SI_ASSIGNEES);
					}
					else if (curStatus.equals(TataPowerUtils.STATUS_QS)){
						ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_ELECTRICAL_30_QS_ASSIGNEES);
					}
					else if (curStatus.equals(TataPowerUtils.STATUS_GROUP_HEAD)){
						ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_ELECTRICAL_30_GH_ASSIGNEES);
					}		
					else if (curStatus.equals(TataPowerUtils.STATUS_HOC)){
						ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_ELECTRICAL_30_HOC_ASSIGNEES);
					}
					else if (curStatus.equals(TataPowerUtils.STATUS_FINANCE)){
						ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_ELECTRICAL_30_FINANCE_ASSIGNEES);
					}				
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
				currentRequest.setAssignees(ruList);
			}
			ruleResult.setCanContinue(true);
			return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " - Status based assignees for 'electrical_thirty'";
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
