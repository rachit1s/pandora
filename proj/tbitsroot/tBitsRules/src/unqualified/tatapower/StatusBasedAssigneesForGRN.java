/**
 * 
 */
package tatapower;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import org.eclipse.birt.core.script.functionservice.impl.Category;

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
public class StatusBasedAssigneesForGRN implements IRule {
			
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
		//Type categoryType = currentRequest.getCategoryId();
		if (isApplicable && (requestType.getName().startsWith(TataPowerUtils.TYPE_GRN))){
				//&& categoryType.getName().equals(TataPowerUtils.MATERIAL_MANAGEMENT)){
			Type statusType = currentRequest.getStatusId();
			String curStatus = statusType.getName();
			ArrayList<RequestUser> ruList = null;
			try {
				if (curStatus.equals(TataPowerUtils.STATUS_DCC)){
					ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_DCC_ASSIGNEES);				
				}
				else if (curStatus.equals(TataPowerUtils.STATUS_STORE)){
					ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_STORE_ASSIGNEES);				
				}
				else if (curStatus.equals(TataPowerUtils.STATUS_UDSI)){
					ruList = getAssigneesBasedOnDeptType(currentRequest, aSystemId, aRequestId, statusType.getName(), ruList);
				}
				/*else if (curStatus.equals(TataPowerUtils.STATUS_QS)){
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_QTY_ASSIGNEES);
				}*/
				else if (curStatus.equals(TataPowerUtils.STATUS_QUALITY)){
					ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_QUALITY_ASSIGNEES);				
				}				
				else if (curStatus.equals(TataPowerUtils.STATUS_GROUP_HEAD)){
					ruList = getAssigneesBasedOnDeptType(currentRequest, aSystemId, aRequestId, statusType.getName(), ruList);
				}
				else if (curStatus.equals(TataPowerUtils.STATUS_HOC)){
					ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_HEAD_OF_CONST_ASSIGNEES);
				}
				else if (curStatus.equals(TataPowerUtils.STATUS_FINANCE)){
					ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_FINANCE_ASSIGNEES);
				}				
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			currentRequest.setAssignees(ruList);
		}
		ruleResult.setCanContinue(true);
		return ruleResult;
	}

	/**
	 * @param currentRequest
	 * @param aSystemId
	 * @param aRequestId
	 * @param statusType 
	 * @param ruList
	 * @return
	 * @throws DatabaseException
	 */
	private ArrayList<RequestUser> getAssigneesBasedOnDeptType(
			Request currentRequest, int aSystemId, int aRequestId,
			String statusType, ArrayList<RequestUser> ruList) throws DatabaseException {
		Type deptType = currentRequest.getCategoryId();
		String deptTypeName = deptType.getName();
		if (statusType.equals(TataPowerUtils.STATUS_UDSI)){
			if (deptTypeName.equals(TataPowerUtils.CIVIL))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_CIVIL_SI_ASSIGNEES);
			else if (deptTypeName.equals(TataPowerUtils.MECHANICAL))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_MECHANICAL_SI_ASSIGNEES);
			else if (deptTypeName.equals(TataPowerUtils.ELECTRICAL))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_ELECTRICAL_SI_ASSIGNEES);
			else if (deptTypeName.equals(TataPowerUtils.HR))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_HR_SI_ASSIGNEES);
			else if (deptTypeName.equals(TataPowerUtils.ADMINISTRATION))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_ADMIN_SI_ASSIGNEES);
			else if (deptTypeName.equals(TataPowerUtils.PROCUREMENT))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_PROC_SI_ASSIGNEES);
			else if (deptTypeName.equals(TataPowerUtils.SAFETY))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_SAFETY_SI_ASSIGNEES);
			else if (deptTypeName.equals(TataPowerUtils.SECURITY))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_SECURITY_SI_ASSIGNEES);
		}
		if (statusType.equals(TataPowerUtils.STATUS_GROUP_HEAD)){
			if (deptTypeName.equals(TataPowerUtils.CIVIL))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_CIVIL_GH_ASSIGNEES);
			else if (deptTypeName.equals(TataPowerUtils.MECHANICAL))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_MECHANICAL_GH_ASSIGNEES);
			else if (deptTypeName.equals(TataPowerUtils.ELECTRICAL))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_ELECTRICAL_GH_ASSIGNEES);
			else if (deptTypeName.equals(TataPowerUtils.HR))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_HR_GH_ASSIGNEES);
			else if (deptTypeName.equals(TataPowerUtils.ADMINISTRATION))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_ADMIN_GH_ASSIGNEES);
			else if (deptTypeName.equals(TataPowerUtils.PROCUREMENT))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_PROC_GH_ASSIGNEES);
			else if (deptTypeName.equals(TataPowerUtils.SAFETY))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_SAFETY_GH_ASSIGNEES);
			else if (deptTypeName.equals(TataPowerUtils.SECURITY))
				ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_GRN_SECURITY_GH_ASSIGNEES);
		}
		return ruList;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return  this.getClass().getSimpleName() + " - Assignees will be set based on the status type for GRN Type";
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
