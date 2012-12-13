/**
 * 
 */
package tatapower;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
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
public class StatusBasedAssigneesForOthers implements IRule {
	
	//Request_type types
	//private static final String TYPE_OTHERS = "Others";
	
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
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(sysPrefix);
		Type requestType = currentRequest.getRequestTypeId();
		boolean isPermissibleType = TataPowerUtils.isExistsInString(
				TataPowerUtils.getProperty("tatapower.typeNames"), requestType.getName());
		if (isApplicable && isPermissibleType){
			System.out.println("Executing inside: " + this.getClass().getName());
			Type statusType = currentRequest.getStatusId();
			String curStatus = statusType.getName();
			ArrayList<RequestUser> ruList = null;
			try {
				if (curStatus.equals(TataPowerUtils.STATUS_DCC)){
					ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_OTHERS_DCC_ASSIGNEES);
				}
				else if (curStatus.equals(TataPowerUtils.STATUS_UDSI)){
					/*ruList = TataProjectsUtils.getRUList(aSystemId, aRequestId, TYPE_60_SITE_INCHARGE_ASSIGNEES);
					if ((ruList== null) || (ruList.isEmpty()))
						ruList = getAssigneesBasedOnCategory(currentRequest,
								aSystemId, aRequestId, ruList);*/
					String assigneeList = TataPowerUtils.getProperty(TataPowerUtils.TYPE_OTHERS_SITE_INCHARGE_ASSIGNEES);
					if ((assigneeList== null) || assigneeList.trim().equals("")){
						ruList = getAssigneesBasedOnCategory(currentRequest,
								aSystemId, aRequestId, ruList);
					}
					else
						ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_OTHERS_SITE_INCHARGE_ASSIGNEES);
				}
				else if (curStatus.equals(TataPowerUtils.STATUS_QS)){
					ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_OTHERS_QTY_SURVEYOR_ASSIGNEES);
				}
				else if (curStatus.equals(TataPowerUtils.STATUS_GROUP_HEAD)){
					ruList = getAssigneesBasedOnCategory(currentRequest,
							aSystemId, aRequestId, ruList);
				}
				else if (curStatus.equals(TataPowerUtils.STATUS_HOC)){
					ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_OTHERS_HEAD_OF_CONST_ASSIGNEES);
				}
				else if (curStatus.equals(TataPowerUtils.STATUS_FINANCE)){
					ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_OTHERS_FINANCE_ASSIGNEES);
				}				
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			currentRequest.setAssignees(ruList);				
			//System.out.println("####################################ruList: " + ruList);
		}
		ruleResult.setCanContinue(true);
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + "- Assignees will be set based on the status type for Type 'Others'";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
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
			ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_OTHERS_MECH_GH_ASSIGNEES);
		else if (catName.equals(TataPowerUtils.CIVIL))
			ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_OTHERS_CIVIL_GH_ASSIGNEES);
		else if (catName.equals(TataPowerUtils.ELECTRICAL))
			ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, TataPowerUtils.TYPE_OTHERS_ELEC_GH_ASSIGNEES);
		return ruList;
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
		IRule irule = new StatusBasedAssigneesForOthers();
		/*irule.execute(ba, null, req, TBitsConstants.SOURCE_CMDLINE, root, 
				req.getExtendedFields(), false, "");*/
	 }
}
