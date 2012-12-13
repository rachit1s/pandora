/**
 * 
 */
package tatapower;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.sql.Connection;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
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
public class DueDateAndTypeGRNStatus implements IRule {
	
	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);	

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
		Field storeRecievedField = null;
		Field udsiDateField = null;
		Field qualityDateField = null;
		//Field qsDateField = null;
		Field hocDateField = null;
		Field financeDateField = null;
		Field finPaymentField = null;
		Field ghDateField = null;
		Type requestType = currentRequest.getRequestTypeId();
		Type categoryType = currentRequest.getCategoryId();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(ba.getSystemPrefix());
		
		if (isApplicable && (requestType.getName().startsWith(TataPowerUtils.TYPE_GRN))){ 
				//&& (categoryType.getName().equals(TataPowerUtils.MATERIAL_MANAGEMENT))){

			try {	
				dccDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.DCC_RECEIPT_DATE);
				storeRecievedField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.STORE_RECEIVED_DATE);
				udsiDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.UDSI_RECIEPT_DATE);
				qualityDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.QUALITY_RECIEVED_DATE);
				ghDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.GH_RECIEPT_DATE);
				hocDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.HOC_RECIEPT_DATE);
				financeDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FINANCE_RECIEPT_DATE);
				statusType = currentRequest.getStatusId();

				boolean isDueDateChanged = false;				
				if (dccDateField != null){
					dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.GRN_DCC_OFFSET);	
					Timestamp dccAckDateTime = currentRequest.getExDateTime(TataPowerUtils.DCC_ACKNOWLEDGE);
					if (dccAckDateTime == null){
						isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, dccDateField, dateOffset, extendedFields, isAddRequest);
						if (isDueDateChanged){
							statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_DCC);
							//isDueDateChanged = false;
							if (isCorrectDeptType(statusType.getName(), categoryType.getName())){
								isDueDateChanged = false;
							}
							else{
								return new RuleResult(false, "If 'Type' is: " + TataPowerUtils.TYPE_GRN + " and 'Pending With' is: " +
										TataPowerUtils.STATUS_DCC + ", then Department has to be: " + TataPowerUtils.MATERIAL_MANAGEMENT);
							}						
						}
					}
				}				
								
				if(storeRecievedField != null){
					dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.GRN_STORE_OFFSET);
					isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, storeRecievedField, dateOffset, extendedFields, isAddRequest);
					if (isDueDateChanged){
						statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_STORE);
						if (isCorrectDeptType(statusType.getName(), categoryType.getName())){
							isDueDateChanged = false;
						}
						else{
							return new RuleResult(false, "If 'Type' is: " + TataPowerUtils.TYPE_GRN + " and 'Pending With' is:" +
									TataPowerUtils.STATUS_STORE + ", then Department has to be other than: " + TataPowerUtils.MATERIAL_MANAGEMENT);
						}	
					}
				}
				
				if (udsiDateField != null){
					dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.GRN_SI_OFFSET);
					isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, udsiDateField, dateOffset, extendedFields, isAddRequest);
					if (isDueDateChanged){
						statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_UDSI);
						if (isCorrectDeptType(statusType.getName(), categoryType.getName())){
							isDueDateChanged = false;
						}
						else{
							return new RuleResult(false, "If 'Type' is: " + TataPowerUtils.TYPE_GRN + " and 'Pending With' is:" +
									TataPowerUtils.STATUS_UDSI + ", then Department has to be other than: " + TataPowerUtils.MATERIAL_MANAGEMENT);
						}	
					}
				}
				if(qualityDateField != null){
					dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.GRN_QUALITY_OFFSET);
					isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, qualityDateField, dateOffset, extendedFields, isAddRequest);
					if (isDueDateChanged){
						statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_QUALITY);
						if (isCorrectDeptType(statusType.getName(), categoryType.getName())){
							isDueDateChanged = false;
						}
						else{
							return new RuleResult(false, "If 'Type' is: " + TataPowerUtils.TYPE_GRN + " and 'Pending With' is:" +
									TataPowerUtils.STATUS_QUALITY + ", then Department has to be other than: " + TataPowerUtils.MATERIAL_MANAGEMENT);
						}
					}
				}
				if(ghDateField != null){
					dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.GRN_GH_OFFSET);
					isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, ghDateField, dateOffset, extendedFields, isAddRequest);
					if (isDueDateChanged){
						statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_GROUP_HEAD);
						if (isCorrectDeptType(statusType.getName(), categoryType.getName())){
							isDueDateChanged = false;
						}
						else{
							return new RuleResult(false, "If 'Type' is: " + TataPowerUtils.TYPE_GRN + " and 'Pending With' is:" +
									TataPowerUtils.STATUS_GROUP_HEAD + ", then Department has to be other than: " + TataPowerUtils.MATERIAL_MANAGEMENT);
						}	
					}
				}
				if(hocDateField != null){
					dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.GRN_HOC_OFFSET);
					isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, hocDateField, dateOffset, extendedFields, isAddRequest);
					if (isDueDateChanged){
						statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_HOC);
						if (isCorrectDeptType(statusType.getName(), categoryType.getName())){
							isDueDateChanged = false;
						}
						else{
							return new RuleResult(false, "If 'Type' is: " + TataPowerUtils.TYPE_GRN + " and 'Pending With' is:" +
									TataPowerUtils.STATUS_HOC + ", then Department has to be other than: " + TataPowerUtils.MATERIAL_MANAGEMENT);
						}	
					}
				}
				if(financeDateField != null){
					dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.GRN_FINANCE_OFFSET);
					isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, financeDateField, dateOffset, extendedFields, isAddRequest);
					if (isDueDateChanged){
						statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_FINANCE);
						if (isCorrectDeptType(statusType.getName(), categoryType.getName())){
							isDueDateChanged = false;
						}
						else{
							return new RuleResult(false, "If 'Type' is: " + TataPowerUtils.TYPE_GRN + " and 'Pending With' is:" +
									TataPowerUtils.STATUS_FINANCE + ", then Department has to be other than: " + TataPowerUtils.MATERIAL_MANAGEMENT);
						}	
					}
				}

				//Type tmpStatusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_FINANCE);
				Type closedStatusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_CLOSED);
				finPaymentField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FIN_PAYMENT_DATE);
				RequestEx tempReqEx = extendedFields.get(finPaymentField);
				//(currentRequest.getStatusId().getTypeId() == tmpStatusType.getTypeId()) &&
				//(oldRequest.getStatusId().getTypeId()!= closedStatusType.getTypeId()) &&
				if (tempReqEx.getDateTimeValue() != null){
					statusType = closedStatusType;
				}	

				currentRequest.setStatusId(statusType);
				return new RuleResult(true, "successful");

			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		else
			return new RuleResult(true, "Not applicable to this business area");

		return null;
	}

	private boolean isCorrectDeptType(String statusType, String categoryName) {
		if (statusType.equals(TataPowerUtils.STATUS_DCC) && (!categoryName.equals(TataPowerUtils.MATERIAL_MANAGEMENT)))
			return false;
		else if ((!statusType.equals(TataPowerUtils.STATUS_DCC)) && categoryName.equals(TataPowerUtils.MATERIAL_MANAGEMENT))
			return false;
		else 
			return true;			
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return  this.getClass().getSimpleName() + " - sets due date and status depending on the various reciept dates";
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
