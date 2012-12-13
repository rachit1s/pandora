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
public class StatusBasedAssigneesFor40Percent implements IRule {
	
	protected static final String TYPE_40_FINANCE_ASSIGNEES = "tatapower.40_finance_assignees";
	protected static final String TYPE_40_HEAD_OF_CONST_ASSIGNEES = "tatapower.40_head_of_const_assignees";
	protected static final String TYPE_40_QTY_SURVEYOR_ASSIGNEES = "tatapower.40_qty_surveyor_assignees";
	protected static final String TYPE_40_SITE_INCHARGE_ASSIGNEES = "tatapower.40_site_incharge_assignees";
	protected static final String TYPE_40_DCC_ASSIGNEES = "tatapower.40_document_cell_assignees";
	protected static final String TYPE_40_MECH_GH_ASSIGNEES = "tatapower.40_mechanical_group_head";
	protected static final String TYPE_40_CIVIL_GH_ASSIGNEES = "tatapower.40_civil_group_head";
	protected static final String TYPE_40_ELEC_GH_ASSIGNEES = "tatapower.40_electrical_group_head";
	protected static final String TYPE_40_SAFETY_GH_ASSIGNEES= "tatapower.40_safety_group_head";
	protected static final String TYPE_40_HR_GH_ASSIGNEES= "tatapower.40_hr_group_head";
	protected static final String TYPE_40_MM_GH_ASSIGNEES= "tatapower.40_mm_group_head";
	protected static final String TYPE_40_PROC_GH_ASSIGNEES= "tatapower.40_procurement_group_head";
	
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
		if (isApplicable && (requestType.getName().equals(TataPowerUtils.TYPE_FORTY_PERCENT))){
			Type statusType = currentRequest.getStatusId();
			String curStatus = statusType.getName();
			ArrayList<RequestUser> ruList = null;
			try {
				if (curStatus.equals(TataPowerUtils.STATUS_DCC)){
					ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_40_DCC_ASSIGNEES);				
				}
				else if (curStatus.equals(TataPowerUtils.STATUS_UDSI)){
					String assigneeList = TataPowerUtils.getProperty(TYPE_40_SITE_INCHARGE_ASSIGNEES);
					if ((assigneeList== null) || assigneeList.trim().equals("")){
						ruList = getAssigneesBasedOnCategory(currentRequest,
								aSystemId, aRequestId, ruList);
					}
					else
						ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_40_SITE_INCHARGE_ASSIGNEES);
				}
				else if (curStatus.equals(TataPowerUtils.STATUS_QS)){
					ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_40_QTY_SURVEYOR_ASSIGNEES);
				}
				else if (curStatus.equals(TataPowerUtils.STATUS_GROUP_HEAD)){
					ruList = getAssigneesBasedOnCategory(currentRequest,
							aSystemId, aRequestId, ruList);
				}
				else if (curStatus.equals(TataPowerUtils.STATUS_HOC)){
					ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_40_HEAD_OF_CONST_ASSIGNEES);
				}
				else if (curStatus.equals(TataPowerUtils.STATUS_FINANCE)){
					ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_40_FINANCE_ASSIGNEES);
				}				
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			currentRequest.setAssignees(ruList);				
			System.out.println("####################################ruList: " + ruList);
		}
		ruleResult.setCanContinue(true);
		return ruleResult;
	}

	/**
	 * @param currentRequest
	 * @param aSystemId
	 * @param aRequestId
	 * @param ruList
	 * @return
	 * @throws DatabaseException
	 */
	private ArrayList<RequestUser> getAssigneesBasedOnCategory(
			Request currentRequest, int aSystemId, int aRequestId,
			ArrayList<RequestUser> ruList) throws DatabaseException {
		Type catType = currentRequest.getCategoryId();
		String catName = catType.getName();
		if (catName.equals(TataPowerUtils.MECHANICAL))
			ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_40_MECH_GH_ASSIGNEES);
		else if (catName.equals(TataPowerUtils.CIVIL))
			ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_40_CIVIL_GH_ASSIGNEES);
		else if (catName.equals(TataPowerUtils.ELECTRICAL))
			ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_40_ELEC_GH_ASSIGNEES);
		else if (catName.equals(TataPowerUtils.HR))
			ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_40_HR_GH_ASSIGNEES);
		else if (catName.equals(TataPowerUtils.MATERIAL_MANAGEMENT))
			ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_40_MM_GH_ASSIGNEES);
		else if (catName.equals(TataPowerUtils.PROCUREMENT))
			ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_40_PROC_GH_ASSIGNEES);
		else if (catName.equals(TataPowerUtils.SAFETY))
			ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TYPE_40_SAFETY_GH_ASSIGNEES);
		return ruList;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return  this.getClass().getSimpleName() + " - Assignees will be set based on the status type for 40Percent Type";
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
