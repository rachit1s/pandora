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
public class DueDateAndTypeDESPStatus implements IRule {
	
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
		Field financeDateField = null;
		Field finPaymentField = null;
		Type requestType = currentRequest.getRequestTypeId();
		Type categoryType = currentRequest.getCategoryId();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(ba.getSystemPrefix());
		
		if (isApplicable && (requestType.getName().equals(TataPowerUtils.TYPE_DESP)) 
				&& (categoryType.getName().equals(TataPowerUtils.MATERIAL_MANAGEMENT))){

			try {	
				dccDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.DCC_RECEIPT_DATE);
				storeRecievedField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.STORE_RECEIVED_DATE);
				financeDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FINANCE_RECIEPT_DATE);
				statusType = currentRequest.getStatusId();

				boolean isDueDateChanged = false;				
				if (dccDateField != null){
					dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.DESP_DCC_OFFSET);				
					isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, dccDateField, dateOffset, extendedFields, isAddRequest);
					if (isDueDateChanged){
						statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_DCC);
						isDueDateChanged = false;
					}
				}
				
				if(storeRecievedField != null){
					dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.DESP_STORE_OFFSET);
					isDueDateChanged = TataPowerUtils.setDueDateBasedOnRecieptDate(currentRequest, storeRecievedField, dateOffset, extendedFields, isAddRequest);
					if (isDueDateChanged){
						statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_STORE);
						isDueDateChanged = false;
					}
				}

				if(financeDateField != null){
					dateOffset = TataPowerUtils.getPropertyIntValue(TataPowerUtils.DESP_FINANCE_OFFSET);
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
