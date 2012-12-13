/**
 * 
 */
package tatapower;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.sql.Connection;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
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
public class ReceiptDatesBasedStatus implements IRule {
		
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
		Field ghDateField = null;
		Type requestType = currentRequest.getRequestTypeId();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(ba.getSystemPrefix());
		boolean isPermissibleType = TataPowerUtils.isExistsInString(
				TataPowerUtils.getProperty(TataPowerUtils.TATAPOWER_TYPE_NAMES_FOR_STATUS), requestType.getName());
		
		if (isApplicable && isPermissibleType){
			try {	
				dccDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.DCC_RECEIPT_DATE);
				udsiDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.UDSI_RECIEPT_DATE);
				qsDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.QS_RECIEPT_DATE);
				ghDateField =  Field.lookupBySystemIdAndFieldName(systemId,TataPowerUtils.GH_RECIEPT_DATE);
				hocDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.HOC_RECIEPT_DATE);
				financeDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FINANCE_RECIEPT_DATE);
				statusType = currentRequest.getStatusId();
				boolean isRecieptDateChanged = false;

				if (dccDateField != null){
					isRecieptDateChanged = TataPowerUtils.setStatusBasedOnRecieptDate(currentRequest, dccDateField, extendedFields, 
							isAddRequest);
					if (isRecieptDateChanged){
						statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_DCC);
						isRecieptDateChanged = false;
					}						
				}
				if (udsiDateField != null){
					isRecieptDateChanged = TataPowerUtils.setStatusBasedOnRecieptDate(currentRequest, udsiDateField, extendedFields, 
							isAddRequest);
					if (isRecieptDateChanged){
						statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_UDSI);
						isRecieptDateChanged = false;
					}		
				}
				if(qsDateField != null){
					isRecieptDateChanged = TataPowerUtils.setStatusBasedOnRecieptDate(currentRequest, qsDateField, extendedFields, 
							isAddRequest);
					if (isRecieptDateChanged){
						statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_QS);
						isRecieptDateChanged = false;
					}		
				}
				if(ghDateField != null){
					isRecieptDateChanged = TataPowerUtils.setStatusBasedOnRecieptDate(currentRequest, ghDateField, extendedFields, 
							isAddRequest);
					if (isRecieptDateChanged){
						statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_GROUP_HEAD);
						isRecieptDateChanged = false;
					}		
				}
				if(hocDateField != null){
					isRecieptDateChanged = TataPowerUtils.setStatusBasedOnRecieptDate(currentRequest, hocDateField, extendedFields, 
							isAddRequest);
					if (isRecieptDateChanged){
						statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_HOC);
						isRecieptDateChanged = false;
					}		
				}

				if(financeDateField != null){
					isRecieptDateChanged = TataPowerUtils.setStatusBasedOnRecieptDate(currentRequest, financeDateField, extendedFields, 
							isAddRequest);
					if (isRecieptDateChanged){
						statusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_FINANCE);
						isRecieptDateChanged = false;
					}		
				}

				Type tmpStatusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_FINANCE);
				Type closedStatusType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TataPowerUtils.STATUS_CLOSED);
				finPaymentField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.FIN_PAYMENT_DATE);
				RequestEx tempReqEx = extendedFields.get(finPaymentField);				
				int currentTypeId = currentRequest.getStatusId().getTypeId();
				if ((currentTypeId == tmpStatusType.getTypeId()) &&
						(oldRequest.getStatusId().getTypeId()!= closedStatusType.getTypeId()) &&
						(tempReqEx.getDateTimeValue() != null)){
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
		return this.getClass().getSimpleName() + " - sets status depending on the various reciept dates";
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
	 * @throws DatabaseException 
	 */
	public static void main(String[] args) throws DatabaseException {
		BusinessArea ba =BusinessArea.lookupBySystemPrefix("Bill");
		Request req = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), 14);
		User root = User.lookupAllByUserId(1);
		IRule irule = new StatusBasedAssigneesFor60Percent();
		/*irule.execute(ba, null, req, TBitsConstants.SOURCE_CMDLINE, root, 
				req.getExtendedFields(), false, "");*/

	}

}
