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
import transbit.tbits.common.Timestamp;
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
public class DatesOfPredecessorSuccessorForAdmin implements IRule {
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		int systemId = ba.getSystemId();
		RuleResult ruleResult = new RuleResult();
		String sysPrefix = ba.getSystemPrefix();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(sysPrefix);
		String categoryName = currentRequest.getCategoryId().getName();
		if (isApplicable && (!isAddRequest) && categoryName.equals(TataPowerUtils.ADMINISTRATION)){
			try {		
				Field dccAckField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.DCC_ACKNOWLEDGE);			
				Field udsiDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.UDSI_RECIEPT_DATE);
				boolean setSuccessorDateField = setSuccessorDateField(oldRequest, extendedFields, ruleResult,
						dccAckField, udsiDateField);
				if (setSuccessorDateField)
					return ruleResult;

				Field udsiCertifiedField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.SITE_IC_CERTIFIED);			
				Field qsDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.QS_RECIEPT_DATE);				

				Type reqType = currentRequest.getRequestTypeId();
				Field qsVerifiedField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.QS_VERIFIED);			
				Field ghDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.GH_RECIEPT_DATE);
				//Field.lookupBySystemIdAndFieldId(systemId, 58);
				Field ghApprovedField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.UH_APPROVED);			
				Field hocDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.HOC_RECIEPT_DATE);
				Field financeDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FINANCE_RECIEPT_DATE);
				Field hocApprovedField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.HOC_APPROVED);
				
				if (reqType.getName().equals(TataPowerUtils.TYPE_ADMIN_WITH_SAP_PO)){					
					setSuccessorDateField = setSuccessorDateField(oldRequest, extendedFields, ruleResult,
							udsiCertifiedField, qsDateField);
					if (setSuccessorDateField)
						return ruleResult;
					
					RequestEx ghReqEx = extendedFields.get(ghDateField);
					RequestEx hocReqEx = extendedFields.get(hocDateField);
					if ((ghReqEx != null) && (hocReqEx != null)){
						Timestamp ghDateTimeValue = ghReqEx.getDateTimeValue();
						Timestamp hocDateTimeValue = hocReqEx.getDateTimeValue();
						if (ghDateTimeValue != null){
							setSuccessorDateField = setSuccessorDateField(oldRequest, extendedFields, ruleResult,
									qsVerifiedField, ghDateField);
							if (setSuccessorDateField)
								return ruleResult;
							
							setSuccessorDateField = setSuccessorDateField(oldRequest, extendedFields, ruleResult,
									ghApprovedField, financeDateField);					
							if (setSuccessorDateField)
								return ruleResult;
						}
						else if (hocDateTimeValue != null){
							setSuccessorDateField = setSuccessorDateField(oldRequest, extendedFields, ruleResult,
									qsVerifiedField, hocDateField);
							if (setSuccessorDateField)
								return ruleResult;
							
							setSuccessorDateField = setSuccessorDateField(oldRequest, extendedFields, ruleResult,
									hocApprovedField, financeDateField);					
							if (setSuccessorDateField)
								return ruleResult;
						}						
					}
					else{
						ruleResult.setCanContinue(true);
						ruleResult.setMessage("Required fields not found.");
					}				
				} else if (reqType.getName().equals(TataPowerUtils.TYPE_ADMIN_WITHOUT_SAP_PO)){
					setSuccessorDateField = setSuccessorDateField(oldRequest, extendedFields, ruleResult,
							udsiCertifiedField, financeDateField);
					if (setSuccessorDateField)
						return ruleResult;
				}

				ruleResult.setCanContinue(true);
				ruleResult.setMessage("Setting successor date was not needed");			
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		else{
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Not applicable to this business area: " + sysPrefix);
		}
		return ruleResult;
	}

	/**
	 * @param oldRequest
	 * @param extendedFields
	 * @param ruleResult
	 * @param actionDateField
	 * @param recieptDateField
	 */
	private boolean setSuccessorDateField(Request oldRequest,
			Hashtable<Field, RequestEx> extendedFields, RuleResult ruleResult,
			Field actionDateField, Field recieptDateField) {
		if ((actionDateField != null) && (recieptDateField != null)){			
			Hashtable<Field, RequestEx> extendedFields2 = oldRequest.getExtendedFields();
			RequestEx preReqAckActionEx = extendedFields2.get(actionDateField);
			RequestEx prevReqRecievedDateEx = extendedFields2.get(recieptDateField);
			RequestEx reqExActionDateField = extendedFields.get(actionDateField);
			RequestEx reqExRecievedDateField = extendedFields.get(recieptDateField);
			
			if ((reqExActionDateField != null) && (reqExRecievedDateField != null)
					&& (preReqAckActionEx != null) && (prevReqRecievedDateEx != null)){
				Timestamp aDateTimeValue = reqExRecievedDateField.getDateTimeValue();
				if ((aDateTimeValue != null) && (preReqAckActionEx.getDateTimeValue() == null) && (prevReqRecievedDateEx.getDateTimeValue() == null)){						
					reqExActionDateField.setDateTimeValue(aDateTimeValue);
					extendedFields.put(actionDateField, reqExActionDateField);
					ruleResult.setCanContinue(true);
					ruleResult.setSuccessful(true);
					ruleResult.setMessage("Successfully set the action date of: " + actionDateField.getDisplayName());
					return true;
				}
			}				
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName();
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
