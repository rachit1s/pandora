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
public class StatusBasedAssigneesForTypeSAP implements IRule {
		
	//Property Strings
	private static final String TYPE_ADMIN_FINANCE_ASSIGNEES = "tatapower.admin_finance_assignees";
	private static final String TYPE_ADMIN_HEAD_OF_CONST_ASSIGNEES = "tatapower.admin_head_of_const_assignees";
	private static final String TYPE_ADMIN_QTY_SURVEYOR_ASSIGNEES = "tatapower.admin_qty_surveyor_assignees";
	private static final String TYPE_ADMIN_SITE_INCHARGE_ASSIGNEES = "tatapower.admin_site_incharge_assignees";
	private static final String TYPE_ADMIN_DCC_ASSIGNEES = "tatapower.admin_document_cell_assignees";
	private static final String TYPE_ADMIN_GH_ASSIGNEES = "tatapower.admin_group_head";
	
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
			String categoryType = currentRequest.getCategoryId().getName();
			
			if (isApplicable && (requestType.getName().equals(TataPowerUtils.TYPE_ADMIN_WITH_SAP_PO))
					&& categoryType.equals(TataPowerUtils.ADMINISTRATION)){
				Type statusType = currentRequest.getStatusId();
				String curStatus = statusType.getName();
				ArrayList<RequestUser> ruList = null;
				try {
					if (curStatus.equals(TataPowerUtils.STATUS_DCC)){
						ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_ADMIN_DCC_ASSIGNEES);
					}
					else if (curStatus.equals(TataPowerUtils.STATUS_UDSI)){
						ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_ADMIN_SITE_INCHARGE_ASSIGNEES);
					}
					else if (curStatus.equals(TataPowerUtils.STATUS_QS)){
						ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_ADMIN_QTY_SURVEYOR_ASSIGNEES);
					}
					else if (curStatus.equals(TataPowerUtils.STATUS_GROUP_HEAD)){
						ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_ADMIN_GH_ASSIGNEES);
					}
					else if (curStatus.equals(TataPowerUtils.STATUS_HOC)){
						ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_ADMIN_HEAD_OF_CONST_ASSIGNEES);
					}
					else if (curStatus.equals(TataPowerUtils.STATUS_FINANCE)){
						ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_ADMIN_FINANCE_ASSIGNEES);
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
		return this.getClass().getSimpleName() + " - Status based assignees for SAP PO";
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
