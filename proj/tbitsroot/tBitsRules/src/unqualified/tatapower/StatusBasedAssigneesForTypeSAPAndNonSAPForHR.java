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
public class StatusBasedAssigneesForTypeSAPAndNonSAPForHR implements IRule {
		
	//Property Strings
	private static final String TYPE_ADMIN_FINANCE_ASSIGNEES = "tatapower.admin_finance_assignees";
	private static final String TYPE_ADMIN_HEAD_OF_CONST_ASSIGNEES = "tatapower.admin_head_of_const_assignees";
	private static final String TYPE_ADMIN_QTY_SURVEYOR_ASSIGNEES = "tatapower.admin_qty_surveyor_assignees";
	private static final String TYPE_ADMIN_SITE_INCHARGE_ASSIGNEES = "tatapower.admin_site_incharge_assignees";
	private static final String TYPE_ADMIN_DCC_ASSIGNEES = "tatapower.admin_document_cell_assignees";
	private static final String TYPE_ADMIN_GH_ASSIGNEES = "tatapower.admin_group_head";
	
	final static String HR_SAP_DOCUMENT_CELL_ASSIGNEES="tatapower.hr_sap_document_cell_assignees";
	final static String HR_SAP_SI_ASSIGNEES="tatapower.hr_sap_site_incharge_assignees";
	final static String HR_SAP_QS_ASSIGNEES="tatapower.hr_sap_qty_surveyor_assignees";
	final static String HR_SAP_GH_ASSIGNEES="tatapower.hr_sap_group_head_assignees";
	final static String HR_SAP_HOC_ASSIGNEES="tatapower.hr_sap_head_of_const_assignees";
	final static String HR_SAP_FINANCE_ASSIGNEES="tatapower.hr_sap_finance_assignees";

	final static String PROCUREMENT_SAP_DOCUMENT_CELL_ASSIGNEES="tatapower.procurement_sap_document_cell_assignees";
	final static String PROCUREMENT_SAP_SI_ASSIGNEES="tatapower.procurement_sap_site_incharge_assignees";
	final static String PROCUREMENT_SAP_QS_ASSIGNEES="tatapower.procurement_sap_qty_surveyor_assignees";
	final static String PROCUREMENT_SAP_GH_ASSIGNEES="tatapower.procurement_sap_group_head_assignees";
	final static String PROCUREMENT_SAP_HOC_ASSIGNEES="tatapower.procurement_sap_head_of_const_assignees";
	final static String PROCUREMENT_SAP_FINANCE_ASSIGNEES="tatapower.procurement_sap_finance_assignees";

	final static String SAFETY_SAP_DOCUMENT_CELL_ASSIGNEES="tatapower.safety_sap_document_cell_assignees";
	final static String SAFETY_SAP_SI_ASSIGNEES="tatapower.safety_sap_site_incharge_assignees";
	final static String SAFETY_SAP_QS_ASSIGNEES="tatapower.safety_sap_qty_surveyor_assignees";
	final static String SAFETY_SAP_GH_ASSIGNEES="tatapower.safety_sap_group_head_assignees";
	final static String SAFETY_SAP_HOC_ASSIGNEES="tatapower.safety_sap_head_of_const_assignees";
	final static String SAFETY_SAP_FINANCE_ASSIGNEES="tatapower.safety_sap_finance_assignees";

	final static String SECURITY_SAP_DOCUMENT_CELL_ASSIGNEES="tatapower.security_sap_document_cell_assignees";
	final static String SECURITY_SAP_SI_ASSIGNEES="tatapower.security_sap_site_incharge_assignees";
	final static String SECURITY_SAP_QS_ASSIGNEES="tatapower.security_sap_qty_surveyor_assignees";
	final static String SECURITY_SAP_GH_ASSIGNEES="tatapower.security_sap_group_head_assignees";
	final static String SECURITY_SAP_HOC_ASSIGNEES="tatapower.security_sap_head_of_const_assignees";
	final static String SECURITY_SAP_FINANCE_ASSIGNEES="tatapower.security_sap_finance_assignees";
	
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
			String categoryType = currentRequest.getCategoryId().getName();
			boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(sysPrefix );
			Type requestType = currentRequest.getRequestTypeId();
			if (isApplicable 
					&& (requestType.getName().equals(TataPowerUtils.TYPE_ADMIN_WITH_SAP_PO) 
							|| requestType.getName().equals(TataPowerUtils.TYPE_ADMIN_WITHOUT_SAP_PO))
					&& ((!categoryType.equals(TataPowerUtils.TYPE_ADMIN_WITH_SAP_PO))
							&& (!categoryType.equals(TataPowerUtils.TYPE_ADMIN_WITHOUT_SAP_PO)))){
				Type statusType = currentRequest.getStatusId();
				String curStatus = statusType.getName();
				ArrayList<RequestUser> ruList = null;			
				
				try {
					
					if (categoryType.equals(TataPowerUtils.HR)){
						if (curStatus.equals(TataPowerUtils.STATUS_DCC)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, HR_SAP_DOCUMENT_CELL_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_UDSI)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, HR_SAP_SI_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_QS)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, HR_SAP_QS_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_GROUP_HEAD)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, HR_SAP_GH_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_HOC)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, HR_SAP_HOC_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_FINANCE)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, HR_SAP_FINANCE_ASSIGNEES);
						}	
					}
					else if (categoryType.equals(TataPowerUtils.PROCUREMENT)){
						if (curStatus.equals(TataPowerUtils.STATUS_DCC)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, PROCUREMENT_SAP_DOCUMENT_CELL_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_UDSI)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, PROCUREMENT_SAP_SI_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_QS)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, PROCUREMENT_SAP_QS_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_GROUP_HEAD)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, PROCUREMENT_SAP_GH_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_HOC)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, PROCUREMENT_SAP_HOC_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_FINANCE)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, PROCUREMENT_SAP_FINANCE_ASSIGNEES);
						}	
					}
					else if (categoryType.equals(TataPowerUtils.SAFETY)){
						if (curStatus.equals(TataPowerUtils.STATUS_DCC)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, SAFETY_SAP_DOCUMENT_CELL_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_UDSI)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, SAFETY_SAP_SI_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_QS)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, SAFETY_SAP_QS_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_GROUP_HEAD)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, SAFETY_SAP_GH_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_HOC)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, SAFETY_SAP_HOC_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_FINANCE)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, SAFETY_SAP_FINANCE_ASSIGNEES);
						}	
					}
					else if (categoryType.equals(TataPowerUtils.SECURITY)){
						if (curStatus.equals(TataPowerUtils.STATUS_DCC)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, SECURITY_SAP_DOCUMENT_CELL_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_UDSI)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, SECURITY_SAP_SI_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_QS)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, SECURITY_SAP_QS_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_GROUP_HEAD)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, SECURITY_SAP_GH_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_HOC)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, SECURITY_SAP_HOC_ASSIGNEES);
						}
						else if (curStatus.equals(TataPowerUtils.STATUS_FINANCE)){
							ruList = TataPowerUtils.getRUList(aSystemId, aRequestId, SECURITY_SAP_FINANCE_ASSIGNEES);
						}	
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
		return this.getClass().getSimpleName() + " - Status based assignees for SAP PO in HR/Procurement/Safety/Security";
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
