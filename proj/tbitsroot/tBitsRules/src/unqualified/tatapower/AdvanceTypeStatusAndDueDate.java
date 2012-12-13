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
public class AdvanceTypeStatusAndDueDate implements IRule {

	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);	
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		int systemId = ba.getSystemId();
		Type statusType = null;
		Field dccDateField = null;
		Field udsiDateField = null;
		Field qsDateField = null;
		Field hocDateField = null;
		Field financeDateField = null;
		Field finPaymentField = null;
		Type requestType = currentRequest.getRequestTypeId();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(ba.getSystemPrefix());
		
		if (isApplicable && (requestType.getName().equals(TataPowerUtils.TYPE_ADVANCE))){
			try {
				if (currentRequest.getDueDate() == null){					
					return new RuleResult(false, "Due date cannot be empty. Please provide a due date.");
				}
				else{
					dccDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.DCC_RECEIPT_DATE);
					udsiDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.UDSI_RECIEPT_DATE);
					qsDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.QS_RECIEPT_DATE);
					hocDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.HOC_RECIEPT_DATE);
					financeDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FINANCE_RECIEPT_DATE);
					statusType = currentRequest.getStatusId();					
					
					if (dccDateField != null){
						if (isFieldExistsAndNotEmpty(extendedFields, dccDateField))
							statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_DCC);						
					}
					
					if (udsiDateField != null){
						if (isFieldExistsAndNotEmpty(extendedFields, udsiDateField))
							statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_UDSI);						
					}
					
					if(qsDateField != null){
						if (isFieldExistsAndNotEmpty(extendedFields, qsDateField))
							statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_QS);						
					}
					
					if(hocDateField != null){
						if (isFieldExistsAndNotEmpty(extendedFields, hocDateField))
							statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_HOC);						
					}
					
					if(financeDateField != null){
						if (isFieldExistsAndNotEmpty(extendedFields, financeDateField))
							statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_FINANCE);						
					}
					
					//Type tmpStatusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_FINANCE);
					finPaymentField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FIN_PAYMENT_DATE);
					RequestEx finReqEx = extendedFields.get(finPaymentField);				
					//int curTypeId = currentRequest.getStatusId().getTypeId();
					//int finTypeId = tmpStatusType.getTypeId();
					//(curTypeId == finTypeId) && 
					if ((finReqEx.getDateTimeValue() != null)){
						statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_CLOSED);
					}	
					
					int oldStatusId = -1;
					if(!isAddRequest)
						oldStatusId = oldRequest.getStatusId().getTypeId();
					
					//If previous action status is the same don't change the status
					if (oldStatusId != statusType.getTypeId())
						currentRequest.setStatusId(statusType);	
					
					return new RuleResult(true, "successful");
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
				return new RuleResult(true, "Error occurred, hence continuing with adding the request action");
			}
		}
		else
			return new RuleResult(true, "Not applicable to this business area");		
	}

	/**
	 * @param extendedFields
	 * @param dccDateField
	 * @return
	 */
	private boolean isFieldExistsAndNotEmpty(
			Hashtable<Field, RequestEx> extendedFields, Field dccDateField) {
		RequestEx tmpReqEx;
		tmpReqEx = extendedFields.get(dccDateField);
		if ((tmpReqEx != null) && (tmpReqEx.getDateTimeValue()!= null))
			return true;
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "AdvanceTypeStatusAndDueDate -  disallows due date to be null and sets status depending on the various reciept dates";
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
