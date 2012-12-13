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
public class DatesOfPredecessorSuccessorGRN implements IRule {
	
	//Indices for receipt and acknowledge fields
	static final int INDEX_RECEIPT_FIELD = 0;
	static final int INDEX_ACK_FIELD = 1;

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
		Type reqTypeId = currentRequest.getRequestTypeId();
		//&& (!categoryId.equals(TataPowerUtils.ADMINISTRATION))
		if (isApplicable && (!isAddRequest) && (reqTypeId.getName().startsWith("GRN") 
												|| reqTypeId.getName().equals(TataPowerUtils.TYPE_DESP))){
			try {				
				Hashtable<Integer, String> fieldHierarchyHT = TataPowerUtils.getFieldHierarchyGRN();
				boolean isPredecessorAckDateFieldSet = false;
				//Check if DCC acknowledge date is null, if null check which of the 
				//succeeding reciept date fields are not null and set the ack date
				//with the non-null receipt date. Check if the succeeding receipt 
				//date was null in the previous action
				int key = TataPowerUtils.GRN_DCC_INDEX;
				switch (key){
					case TataPowerUtils.GRN_DCC_INDEX : {						
						isPredecessorAckDateFieldSet = setPredecessorAckField(
								oldRequest, extendedFields, systemId,
								ruleResult, fieldHierarchyHT, 
								TataPowerUtils.GRN_DCC_INDEX);
						if (isPredecessorAckDateFieldSet)
							return ruleResult;
					}
					case TataPowerUtils.GRN_STORE_INDEX :{
						isPredecessorAckDateFieldSet = setPredecessorAckField(
								oldRequest, extendedFields, systemId,
								ruleResult, fieldHierarchyHT, 
								TataPowerUtils.GRN_STORE_INDEX);
						if (isPredecessorAckDateFieldSet)
							return ruleResult;
					}
					case TataPowerUtils.GRN_SITE_IC_INDEX:{
						isPredecessorAckDateFieldSet = setPredecessorAckField(
								oldRequest, extendedFields, systemId,
								ruleResult, fieldHierarchyHT, 
								TataPowerUtils.GRN_SITE_IC_INDEX);
						if (isPredecessorAckDateFieldSet)
							return ruleResult;
					}
					case TataPowerUtils.GRN_QUALITY_INDEX :{
						isPredecessorAckDateFieldSet = setPredecessorAckField(
								oldRequest, extendedFields, systemId,
								ruleResult, fieldHierarchyHT, 
								TataPowerUtils.GRN_QUALITY_INDEX);
						if (isPredecessorAckDateFieldSet)
							return ruleResult;
					}
					case TataPowerUtils.GRN_GH_INDEX :{
						isPredecessorAckDateFieldSet = setPredecessorAckField(
								oldRequest, extendedFields, systemId,
								ruleResult, fieldHierarchyHT, 
								TataPowerUtils.GRN_GH_INDEX);
						if (isPredecessorAckDateFieldSet)
							return ruleResult;
					}
					case TataPowerUtils.GRN_HOC_INDEX:{
						isPredecessorAckDateFieldSet = setPredecessorAckField(
								oldRequest, extendedFields, systemId,
								ruleResult, fieldHierarchyHT, 
								TataPowerUtils.GRN_HOC_INDEX);
						if (isPredecessorAckDateFieldSet)
							return ruleResult;
					}					
					default: break;
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

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getName() + " - Sets recieved date of successor group equal to the action date of the predecessor.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * @param oldRequest
	 * @param extendedFields
	 * @param ruleResult
	 * @param predActionDateField
	 * @param recieptDateField
	 * @param predRecDateField 
	 */
	static boolean setPredecessorDateField(Request oldRequest,
			Hashtable<Field, RequestEx> extendedFields, RuleResult ruleResult,
			 Field predRecDateField, Field predActionDateField, Field recieptDateField){
		
		if ((predRecDateField != null) &&(predActionDateField != null) && (recieptDateField != null)){			
			Hashtable<Field, RequestEx> extendedFields2 = oldRequest.getExtendedFields();
			
			RequestEx preReqAckActionEx = extendedFields2.get(predActionDateField);
			RequestEx prevReqRecievedDateEx = extendedFields2.get(recieptDateField);			
			
			RequestEx reqExPredRecDateField = extendedFields.get(predRecDateField);
			RequestEx reqExActionDateField = extendedFields.get(predActionDateField);
			RequestEx reqExRecievedDateField = extendedFields.get(recieptDateField);
			
			if ((reqExPredRecDateField != null) && (reqExActionDateField != null) && (reqExRecievedDateField != null)
					&& (preReqAckActionEx != null) && (prevReqRecievedDateEx != null)){
				Timestamp aDateTimeValue = reqExRecievedDateField.getDateTimeValue();
				if ((aDateTimeValue != null) && (preReqAckActionEx.getDateTimeValue() == null) && 
						(prevReqRecievedDateEx.getDateTimeValue() == null) && 
						(reqExPredRecDateField.getDateTimeValue() != null)){						
					reqExActionDateField.setDateTimeValue(aDateTimeValue);
					extendedFields.put(predActionDateField, reqExActionDateField);
					ruleResult.setCanContinue(true);
					ruleResult.setSuccessful(true);
					ruleResult.setMessage("Successfully set the action date of: " + predActionDateField.getDisplayName());
					return true;
				}
			}				
		}
		return false;
	}
	
	/**
	 * @param oldRequest
	 * @param extendedFields
	 * @param systemId
	 * @param ruleResult
	 * @param fieldHierarchyHT
	 * @param isFieldSet
	 * @param receiptDateIndex
	 * @return
	 * @throws DatabaseException
	 */
	static boolean setPredecessorAckField(Request oldRequest,
			Hashtable<Field, RequestEx> extendedFields, int systemId,
			RuleResult ruleResult, Hashtable<Integer, String> fieldHierarchyHT,
			int receiptDateIndex)
			throws DatabaseException {
		
		boolean isFieldSet = false;
		String receiptAckDatesString = fieldHierarchyHT.get(receiptDateIndex);
		String[] predecessorDates = receiptAckDatesString.split(",");	
		for (int i=(receiptDateIndex + 1); i<=TataPowerUtils.GRN_FINANCE_INDEX; i++){													
			String[] successorDates = fieldHierarchyHT.get(i).split(",");
			Field predecesserRecField = Field.lookupBySystemIdAndFieldName(systemId, predecessorDates[INDEX_RECEIPT_FIELD]);
			Field predecessorAckField = Field.lookupBySystemIdAndFieldName(systemId, predecessorDates[INDEX_ACK_FIELD]);
			Field successorField =  Field.lookupBySystemIdAndFieldName(systemId, successorDates[INDEX_RECEIPT_FIELD]);							
			isFieldSet = setPredecessorDateField(oldRequest, extendedFields, ruleResult,
					predecesserRecField, predecessorAckField, successorField);			
		}
		return isFieldSet;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
