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
public class DueDateAndTypeNonSAP implements IRule {
	
	final static String HR_SAP_DCC_DATE_OFFSET = "tatapower.hr_sap_dcc_date_offset";
	final static String HR_SAP_SI_DATE_OFFSET = "tatapower.hr_sap_site_incharge_date_offset";
	final static String HR_SAP_QS_DATE_OFFSET = "tatapower.hr_sap_qs_date_offset";
	final static String HR_SAP_GH_DATE_OFFSET = "tatapower.hr_sap_group_head_offset";
	final static String HR_SAP_HOC_DATE_OFFSET = "tatapower.hr_sap_hoc_date_offset";
	final static String HR_SAP_FINANCE_DATE_OFFSET = "tatapower.hr_sap_finance_date_offset";
	final static String HR_NON_SAP_DCC_DATE_OFFSET = "tatapower.hr_non_sap_no_dcc_date_offset";
	final static String HR_NON_SAP_SI_DATE_OFFSET = "tatapower.hr_non_sap_po_site_incharge_date_offset";
	final static String HR_NON_SAP_FINANCE_DATE_OFFSET = "tatapower.hr_non_sap_po_finance_date_offset";
	
	final static String PROCUREMENT_SAP_DCC_DATE_OFFSET = "tatapower.procurement_sap_dcc_date_offset";
	final static String PROCUREMENT_SAP_SI_DATE_OFFSET = "tatapower.procurement_sap_site_incharge_date_offset";
	final static String PROCUREMENT_SAP_QS_DATE_OFFSET = "tatapower.procurement_sap_qs_date_offset";
	final static String PROCUREMENT_SAP_GH_DATE_OFFSET = "tatapower.procurement_sap_group_head_offset";
	final static String PROCUREMENT_SAP_HOC_DATE_OFFSET = "tatapower.procurement_sap_hoc_date_offset";
	final static String PROCUREMENT_SAP_FINANCE_DATE_OFFSET = "tatapower.procurement_sap_finance_date_offset";
	final static String PROCUREMENT_NON_SAP_DCC_DATE_OFFSET = "tatapower.procurement_non_sap_no_dcc_date_offset";
	final static String PROCUREMENT_NON_SAP_SI_DATE_OFFSET = "tatapower.procurement_non_sap_po_site_incharge_date_offset";
	final static String PROCUREMENT_NON_SAP_FINANCE_DATE_OFFSET = "tatapower.procurement_non_sap_po_finance_date_offset";

	final static String SAFETY_SAP_DCC_DATE_OFFSET = "tatapower.safety_sap_dcc_date_offset";
	final static String SAFETY_SAP_SI_DATE_OFFSET = "tatapower.safety_sap_site_incharge_date_offset";
	final static String SAFETY_SAP_QS_DATE_OFFSET = "tatapower.safety_sap_qs_date_offset";
	final static String SAFETY_SAP_GH_DATE_OFFSET = "tatapower.safety_sap_group_head_offset";
	final static String SAFETY_SAP_HOC_DATE_OFFSET = "tatapower.safety_sap_hoc_date_offset";
	final static String SAFETY_SAP_FINANCE_DATE_OFFSET = "tatapower.safety_sap_finance_date_offset";
	final static String SAFETY_NON_SAP_DCC_DATE_OFFSET = "tatapower.safety_non_sap_no_dcc_date_offset";
	final static String SAFETY_NON_SAP_SI_DATE_OFFSET = "tatapower.safety_non_sap_po_site_incharge_date_offset";
	final static String SAFETY_NON_SAP_FINANCE_DATE_OFFSET = "tatapower.safety_non_sap_po_finance_date_offset";

	final static String SECURITY_SAP_DCC_DATE_OFFSET = "tatapower.security_sap_dcc_date_offset";
	final static String SECURITY_SAP_SI_DATE_OFFSET = "tatapower.security_sap_site_incharge_date_offset";
	final static String SECURITY_SAP_QS_DATE_OFFSET = "tatapower.security_sap_qs_date_offset";
	final static String SECURITY_SAP_GH_DATE_OFFSET = "tatapower.security_sap_group_head_offset";
	final static String SECURITY_SAP_HOC_DATE_OFFSET = "tatapower.security_sap_hoc_date_offset";
	final static String SECURITY_SAP_FINANCE_DATE_OFFSET = "tatapower.security_sap_finance_date_offset";
	final static String SECURITY_NON_SAP_DCC_DATE_OFFSET = "tatapower.security_non_sap_no_dcc_date_offset";
	final static String SECURITY_NON_SAP_SI_DATE_OFFSET = "tatapower.security_non_sap_po_site_incharge_date_offset";
	final static String SECURITY_NON_SAP_FINANCE_DATE_OFFSET = "tatapower.security_non_sap_po_finance_date_offset";	
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		int systemId = ba.getSystemId();
		int dateOffset = 0;
		Type statusType = null;
		Field dccDateField = null;
		Field udsiDateField = null;
		Field financeDateField = null;
		Field finPaymentField = null;	
		
		
		Type requestType = currentRequest.getRequestTypeId();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(ba.getSystemPrefix());
		
		String requestTypeName = requestType.getName();
		String categoryName = currentRequest.getCategoryId().getName();
		if (isApplicable && requestTypeName.equals(TataPowerUtils.TYPE_ADMIN_WITHOUT_SAP_PO)){
				//&& categoryName.equals(TataPowerUtils.ADMINISTRATION)){

			try {	
				dccDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.DCC_RECEIPT_DATE);
				udsiDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.UDSI_RECIEPT_DATE);
				financeDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FINANCE_RECIEPT_DATE);
				statusType = currentRequest.getStatusId();

				boolean isDueDateChanged = false;
				
				if (categoryName.equals(TataPowerUtils.ADMINISTRATION)){
					if (dccDateField != null){
						dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.NON_SAP_DCC_DATE_OFFSET);				
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, dccDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_DCC);
							isDueDateChanged = false;
						}
					}

					if (udsiDateField != null){
						dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.NON_SAP_SITE_INCHARGE_DATE_OFFSET);
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, udsiDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_UDSI);
							isDueDateChanged = false;
						}
					}

					if(financeDateField != null){
						dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.NON_SAP_FINANCE_DATE_OFFSET);
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, financeDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_FINANCE);
							isDueDateChanged = false;
						}
					}
				}
				
				else if (categoryName.equals(TataPowerUtils.HR)){
					if (dccDateField != null){
						dateOffset = TataPowerUtils.getPropertyIntValue(HR_NON_SAP_DCC_DATE_OFFSET);				
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, dccDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_DCC);
							isDueDateChanged = false;
						}
					}

					if (udsiDateField != null){
						dateOffset = TataPowerUtils.getPropertyIntValue(HR_NON_SAP_SI_DATE_OFFSET);
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, udsiDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_UDSI);
							isDueDateChanged = false;
						}
					}

					if(financeDateField != null){
						dateOffset = TataPowerUtils.getPropertyIntValue(HR_NON_SAP_FINANCE_DATE_OFFSET);
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, financeDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_FINANCE);
							isDueDateChanged = false;
						}
					}
				}
				
				else if (categoryName.equals(TataPowerUtils.PROCUREMENT)){
					if (dccDateField != null){
						dateOffset = TataPowerUtils.getPropertyIntValue(PROCUREMENT_NON_SAP_DCC_DATE_OFFSET);				
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, dccDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_DCC);
							isDueDateChanged = false;
						}
					}

					if (udsiDateField != null){
						dateOffset = TataPowerUtils.getPropertyIntValue(PROCUREMENT_NON_SAP_SI_DATE_OFFSET);
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, udsiDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_UDSI);
							isDueDateChanged = false;
						}
					}

					if(financeDateField != null){
						dateOffset = TataPowerUtils.getPropertyIntValue(PROCUREMENT_NON_SAP_FINANCE_DATE_OFFSET);
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, financeDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_FINANCE);
							isDueDateChanged = false;
						}
					}
				}
				
				else if (categoryName.equals(TataPowerUtils.SAFETY)){
					if (dccDateField != null){
						dateOffset = TataPowerUtils.getPropertyIntValue(SAFETY_NON_SAP_DCC_DATE_OFFSET);				
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, dccDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_DCC);
							isDueDateChanged = false;
						}
					}

					if (udsiDateField != null){
						dateOffset = TataPowerUtils.getPropertyIntValue(SAFETY_NON_SAP_SI_DATE_OFFSET);
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, udsiDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_UDSI);
							isDueDateChanged = false;
						}
					}

					if(financeDateField != null){
						dateOffset = TataPowerUtils.getPropertyIntValue(SAFETY_NON_SAP_FINANCE_DATE_OFFSET);
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, financeDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_FINANCE);
							isDueDateChanged = false;
						}
					}
				}
				
				else if (categoryName.equals(TataPowerUtils.SECURITY)){
					if (dccDateField != null){
						dateOffset = TataPowerUtils.getPropertyIntValue(SECURITY_NON_SAP_DCC_DATE_OFFSET);				
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, dccDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_DCC);
							isDueDateChanged = false;
						}
					}

					if (udsiDateField != null){
						dateOffset = TataPowerUtils.getPropertyIntValue(SECURITY_NON_SAP_SI_DATE_OFFSET);
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, udsiDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_UDSI);
							isDueDateChanged = false;
						}
					}

					if(financeDateField != null){
						dateOffset = TataPowerUtils.getPropertyIntValue(SECURITY_NON_SAP_FINANCE_DATE_OFFSET);
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, financeDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_FINANCE);
							isDueDateChanged = false;
						}
					}
				}

				//Type tmpStatusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_FINANCE);
				Type closedStatusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_CLOSED);
				finPaymentField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FIN_PAYMENT_DATE);
				RequestEx tempReqEx = extendedFields.get(finPaymentField);				
				//int currentTypeId = currentRequest.getStatusId().getTypeId();
				//(currentTypeId == tmpStatusType.getTypeId()) &&	
				//((oldRequest.getStatusId().getTypeId()!= closedStatusType.getTypeId()) &&
				if(tempReqEx.getDateTimeValue() != null){
					statusType = closedStatusType;
				}	

				currentRequest.setStatusId(statusType);
				return new RuleResult(true, "successful");

			} catch (DatabaseException e) {
				e.printStackTrace();
				return new RuleResult(false, "Cannot continue as database error occurred.\n" + e.getMessage());
			}
		}
		else
			return new RuleResult(true, "Not applicable to this business area");

		//return new RuleResult(true, "Not applicable");
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " - Sets the due date for NON-SAP PO";
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

/*
		int systemId = ba.getSystemId();
		int dateOffset = 0;
		Type statusType = null;
		Field dccDateField = null;
		Field udsiDateField = null;
		Field financeDateField = null;
		Field finPaymentField = null;
		
		Type requestType = currentRequest.getRequestTypeId();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(ba.getSystemPrefix());
		
		String requestTypeName = requestType.getName();
		String categoryName = currentRequest.getCategoryId().getName();
		if (isApplicable && requestTypeName.equals(TataPowerUtils.TYPE_ADMIN_WITHOUT_SAP_PO) 
				&& categoryName.equals(TataPowerUtils.ADMINISTRATION)){

			try {	
				dccDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.DCC_RECEIPT_DATE);
				udsiDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.UDSI_RECIEPT_DATE);
				financeDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FINANCE_RECIEPT_DATE);
				statusType = currentRequest.getStatusId();

				boolean isDueDateChanged = false;				
				if (dccDateField != null){
					dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.NON_SAP_DCC_DATE_OFFSET);				
					isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, dccDateField, dateOffset, extendedFields, isAddRequest);
					if (isDueDateChanged){
						statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_DCC);
						isDueDateChanged = false;
					}
				}
				
				if (udsiDateField != null){
					dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.NON_SAP_SITE_INCHARGE_DATE_OFFSET);
					isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, udsiDateField, dateOffset, extendedFields, isAddRequest);
					if (isDueDateChanged){
						statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_UDSI);
						isDueDateChanged = false;
					}
				}
				
				if(financeDateField != null){
					dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.NON_SAP_FINANCE_DATE_OFFSET);
					isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, financeDateField, dateOffset, extendedFields, isAddRequest);
					if (isDueDateChanged){
						statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_FINANCE);
						isDueDateChanged = false;
					}
				}

				//Type tmpStatusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_FINANCE);
				Type closedStatusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_CLOSED);
				finPaymentField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FIN_PAYMENT_DATE);
				RequestEx tempReqEx = extendedFields.get(finPaymentField);				
				//int currentTypeId = currentRequest.getStatusId().getTypeId();
				//(currentTypeId == tmpStatusType.getTypeId()) &&	
				//((oldRequest.getStatusId().getTypeId()!= closedStatusType.getTypeId()) &&
				if(tempReqEx.getDateTimeValue() != null){
					statusType = closedStatusType;
				}	

				currentRequest.setStatusId(statusType);
				return new RuleResult(true, "successful");

			} catch (DatabaseException e) {
				e.printStackTrace();
				return new RuleResult(false, "Cannot continue as database error occurred.\n" + e.getMessage());
			}
		}
		else
			return new RuleResult(true, "Not applicable to this business area");

		//return new RuleResult(true, "Not applicable");
	*/	
	
}
