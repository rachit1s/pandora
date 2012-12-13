/**
 * 
 */
package tatapower;

import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.CalenderUtils;
import transbit.tbits.domain.DefaultHolidayCalendar;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * @author Lokesh
 *
 */
public class CalculatedInvoicePaymentDate implements IRule {	
	
	
	final static String HR_SAP_DATE_OFFSET = "tatapower.hr_date_offset";
	final static String HR_NON_SAP_DATE_OFFSET = "tatapower.hr_non_sap_po_date_offset";
	final static String PROCUREMENT_SAP_DATE_OFFSET = "tatapower.procurement_date_offset";
	final static String PROCUREMENT_NON_SAP_DATE_OFFSET = "tatapower.procurement_non_sap_po_date_offset";
	final static String SAFETY_SAP_DATE_OFFSET = "tatapower.safety_date_offset";
	final static String SAFETY_NON_SAP_DATE_OFFSET = "tatapower.safety_non_sap_po_date_offset";
	final static String SECURITY_SAP_DATE_OFFSET = "tatapower.security_date_offset";
	final static String SECURITY_NON_SAP_DATE_OFFSET = "tatapower.security_non_sap_po_date_offset";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		RuleResult ruleResult = new RuleResult();
		int systemId = ba.getSystemId();
		String sysPrefix = ba.getSystemPrefix();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(sysPrefix);
		RequestEx receiptDateReq = null;
		RequestEx paymentDateReq = null;
		if (isApplicable){
			Type requestIdType = currentRequest.getRequestTypeId();
			String curType = requestIdType.getName();
			try {
				Field invoiceDateField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.INVOICE_PAYMENT_DATE);
				if (invoiceDateField == null){
					ruleResult.setCanContinue(true);
					return ruleResult;
				}
				else{
					paymentDateReq = extendedFields.get(invoiceDateField);
				}
				
				Field dccReceiptDate = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.DCC_RECEIPT_DATE);
				if (dccReceiptDate == null){
					ruleResult.setCanContinue(true);
					return ruleResult;
				}
				else{
					receiptDateReq = extendedFields.get(dccReceiptDate);
					if (receiptDateReq.getDateTimeValue() != null){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("Please set receipt date. Receipt date cannot be empty");
					}						
				}				
				
				Timestamp rTimeStamp = receiptDateReq .getDateTimeValue();
				String categoryName = currentRequest.getCategoryId().getName();
				String property = "";
				if (curType.equals(TataPowerUtils.TYPE_SIXTY_PERCENT) || curType.equals(TataPowerUtils.TYPE_ELECTRICAL_RUNNING)||
						curType.equals(TataPowerUtils.TYPE_ELECTRICAL_FINAL)){
					property = TataPowerUtils.getProperty(TataPowerUtils.TATAPOWER_60PERCENT_DATE_OFFSET);					
					setPaymentDate(extendedFields, invoiceDateField, paymentDateReq, rTimeStamp, property);
					currentRequest.setExtendedFields(extendedFields);
					ruleResult.setCanContinue(true);
				}
				else if (curType.equals(TataPowerUtils.TYPE_ELECTRICAL_THIRTY)){
					property = TataPowerUtils.getProperty(TataPowerUtils.TYPE_ELECTRICAL_30_DATE_OFFSET);					
					setPaymentDate(extendedFields, invoiceDateField, paymentDateReq, rTimeStamp, property);
					currentRequest.setExtendedFields(extendedFields);
					ruleResult.setCanContinue(true);
				}
				else if (curType.equals(TataPowerUtils.TYPE_FORTY_PERCENT)){
					property = TataPowerUtils.getProperty(TataPowerUtils.TATAPOWER_40PERCENT_DATE_OFFSET);					
					setPaymentDate(extendedFields, invoiceDateField, paymentDateReq, rTimeStamp, property);
					currentRequest.setExtendedFields(extendedFields);
					ruleResult.setCanContinue(true);
				}
				else if (curType.equals(TataPowerUtils.TYPE_RA100)){
					property = TataPowerUtils.getProperty(TataPowerUtils.TATAPOWER_RA100_DATE_OFFSET);					
					setPaymentDate(extendedFields, invoiceDateField, paymentDateReq, rTimeStamp, property);
					currentRequest.setExtendedFields(extendedFields);
					ruleResult.setCanContinue(true);
				}
				else if (curType.startsWith(TataPowerUtils.TYPE_GRN)){
					property = TataPowerUtils.getProperty(TataPowerUtils.TATAPOWER_GRN_DATE_OFFSET);					
					setPaymentDate(extendedFields, invoiceDateField, paymentDateReq, rTimeStamp, property);
					currentRequest.setExtendedFields(extendedFields);
					ruleResult.setCanContinue(true);
				}
				else if (curType.equals(TataPowerUtils.TYPE_DESP)){
					property = TataPowerUtils.getProperty(TataPowerUtils.TATAPOWER_DESP_DATE_OFFSET);					
					setPaymentDate(extendedFields, invoiceDateField, paymentDateReq, rTimeStamp, property);
					currentRequest.setExtendedFields(extendedFields);
					ruleResult.setCanContinue(true);
				}	
				else if (curType.equals(TataPowerUtils.TYPE_ADMIN_WITH_SAP_PO)){
					
					if (categoryName.equals(TataPowerUtils.ADMINISTRATION))
						property = TataPowerUtils.getProperty(TataPowerUtils.TATAPOWER_ADMIN_DATE_OFFSET);
					else if (categoryName.equals(TataPowerUtils.HR))
						property = TataPowerUtils.getProperty(HR_SAP_DATE_OFFSET);
					else if (categoryName.equals(TataPowerUtils.PROCUREMENT))
						property = TataPowerUtils.getProperty(PROCUREMENT_SAP_DATE_OFFSET);
					else if (categoryName.equals(TataPowerUtils.SAFETY))
						property = TataPowerUtils.getProperty(SAFETY_SAP_DATE_OFFSET);
					else if (categoryName.equals(TataPowerUtils.SECURITY))
						property = TataPowerUtils.getProperty(SECURITY_SAP_DATE_OFFSET);
					setPaymentDate(extendedFields, invoiceDateField, paymentDateReq, rTimeStamp, property);
					currentRequest.setExtendedFields(extendedFields);
					ruleResult.setCanContinue(true);
				}
				else if (curType.equals(TataPowerUtils.TYPE_ADMIN_WITHOUT_SAP_PO)){
					if (categoryName.equals(TataPowerUtils.ADMINISTRATION))
						property = TataPowerUtils.getProperty(TataPowerUtils.TATAPOWER_ADMIN_NON_SAP_DATE_OFFSET);
					else if (categoryName.equals(TataPowerUtils.HR))
						property = TataPowerUtils.getProperty(HR_NON_SAP_DATE_OFFSET);
					else if (categoryName.equals(TataPowerUtils.PROCUREMENT))
						property = TataPowerUtils.getProperty(PROCUREMENT_NON_SAP_DATE_OFFSET);
					else if (categoryName.equals(TataPowerUtils.SAFETY))
						property = TataPowerUtils.getProperty(SAFETY_NON_SAP_DATE_OFFSET);
					else if (categoryName.equals(TataPowerUtils.SECURITY))
						property = TataPowerUtils.getProperty(SECURITY_NON_SAP_DATE_OFFSET);					
					setPaymentDate(extendedFields, invoiceDateField, paymentDateReq, rTimeStamp, property);
					currentRequest.setExtendedFields(extendedFields);
					ruleResult.setCanContinue(true);
				}
				else if(curType.equals(TataPowerUtils.TYPE_OTHERS)){
					Field siField = Field.lookupBySystemIdAndFieldName(systemId, TataPowerUtils.UDSI_RECIEPT_DATE);
					RequestEx siReqEx = extendedFields.get(siField);
					if (siReqEx.getDateTimeValue() != null){
						if (paymentDateReq.getDateTimeValue() == null){
							ruleResult.setCanContinue(false);
							ruleResult.setMessage("Please specify the payment date.");
						}
						else
							ruleResult.setCanContinue(true);							
					}
					else
						ruleResult.setCanContinue(true);
				}				
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		else{
			ruleResult.setCanContinue(true);
		}		
		return ruleResult;
	}

	/**
	 * @param extendedFields 
	 * @param invoiceDateField 
	 * @param invoiceDateReq
	 * @param rTimeStamp
	 * @return 
	 */
	private void setPaymentDate(Hashtable<Field, RequestEx> extendedFields, Field invoiceDateField, RequestEx invoiceDateReq, Timestamp rTimeStamp, String property) {
		if (rTimeStamp != null){
			int dateOffset = Integer.parseInt(property);
			Date date = new Date(rTimeStamp.getTime());
			Date paymentDate = CalenderUtils.slideDate(date, dateOffset, new DefaultHolidayCalendar());
		    Timestamp duedateTS = Timestamp.getTimestamp(paymentDate);
		    duedateTS.setTime(duedateTS.getTime() + TataPowerUtils.getISTDiffTime(duedateTS)-60000);//TataPowerUtils.getRemainingMilliSecondsInTheDay() - 40000);//
			invoiceDateReq.setDateTimeValue(duedateTS);
			extendedFields.put(invoiceDateField, invoiceDateReq);
		}
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "CalculatedInvoicePaymentDate - Payment date is set based on Type";
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
		Hashtable<Field, RequestEx> extendedFields = req.getExtendedFields();
		Field tmpF = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), TataPowerUtils.DCC_RECEIPT_DATE);
		RequestEx r = extendedFields.get(tmpF);
		r.setDateTimeValue(Timestamp.getGMTNow());
		extendedFields.put(tmpF, r);
		req.setExtendedFields(extendedFields);
		/*irule.execute(ba, null, req, TBitsConstants.SOURCE_CMDLINE, root, 
				extendedFields, false, "");*/
	}
}
