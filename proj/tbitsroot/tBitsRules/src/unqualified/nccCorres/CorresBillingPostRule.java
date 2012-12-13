package nccCorres;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

public class CorresBillingPostRule implements IPostRule {

	public static final String PKG_NCC = "transbit.tbits.NCC.Nilanchal";
	/**
	 * logger for this class
	 */
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_NCC);
	
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		if( (null != ba) && (null != ba.getSystemPrefix()) &&  ba.getSystemPrefix().equalsIgnoreCase(CorresConstants.CORR_SYSPREFIX)  )				
		{
			
			String generate = currentRequest.get(CorresConstants.CORR_GENERATE_FIELD_NAME);
			if( generate.equals(CorresConstants.CORR_GEN_DONT_GEN_ANYTHING))
				return new RuleResult(true, "Rule says no corr file to be generated. Hence nothing to be logged into Client/Vendor Billing" , true);
			
			//String tempFieldBill = currentRequest.get(CorresConstants.CORR_TEMP_FIELD_BILLING);
			String tempFieldBill = currentRequest.get(Field.RELATED_REQUESTS);
			try {
				if (tempFieldBill != null && tempFieldBill.length() > 0) {
					
					String [] parts = tempFieldBill.split("#");
					if(parts != null && parts.length > 1) {
						String baPrefix = parts[0].trim();
						String reqStr = parts[1].trim();
						int reqId = Integer.parseInt(reqStr);
						
						String logger = currentRequest.get(CorresConstants.CORR_LOGGER_FIELD_NAME) ;
						String assignee = currentRequest.get(CorresConstants.CORR_ASSIGNEE_FIELD_NAME) ;
						String subscribers = currentRequest.get(CorresConstants.CORR_SUBSCRIBER_FIELD_NAME) ;				
						//String corrType = currentRequest.get(CorresConstants.CORR_CORR_TYPE_FIELD_NAME) ;
						String corrNo = currentRequest.get(CorresConstants.CORR_CORRESPONDENCE_NUMBER_FIELD) ;
						String corrFile = currentRequest.get(CorresConstants.CORR_CORRESPONDANCE_FILE_FIELD_NAME) ;
						String otherFile = currentRequest.get(CorresConstants.CORR_OTHER_ATTACHMENTS_FIELD_NAME) ;
						String cc = currentRequest.get(CorresConstants.CORR_CC_FIELD_NAME);
						
						
						if(baPrefix != null && baPrefix.equals(CorresConstants.CORR_CLIENT_BILLING_SYSPREFIX)) {
							BusinessArea clBillBA = BusinessArea.lookupBySystemPrefix(baPrefix);
							if(clBillBA == null)
								throw new TBitsException ("Cannot get Business area from Database: "+baPrefix);
							Request reqClBill = Request.lookupBySystemIdAndRequestId(connection, clBillBA.getSystemId(), reqId);
							if (reqClBill == null)
								throw new TBitsException ("Cannot get Request from Database: " +clBillBA.getSystemPrefix()+"#"+reqStr);
							
							Hashtable<String,String> paramsClientBill = new Hashtable<String,String>() ;
							
							if( null != logger)
								paramsClientBill.put(Field.LOGGER, logger ) ;
						//	if( null != assignee )
							//	paramsClientBill.put(Field.ASSIGNEE, assignee ) ;
							if( null != subscribers )
								paramsClientBill.put(Field.SUBSCRIBER, subscribers) ;
							if( null != cc)
								paramsClientBill.put(Field.CC, cc);
							if( null != corrNo )
								paramsClientBill.put(CorresConstants.CL_CORRESPONDANCE_NO, corrNo ) ;
							if( null != corrFile )
								paramsClientBill.put(CorresConstants.CL_INVOICE_COVER_LETTER, corrFile ) ;
							if( null != otherFile ) {
								Collection<AttachmentInfo> newAttachment = AttachmentInfo.fromJson(otherFile);
								String oldAttachment = reqClBill.get(Field.ATTACHMENTS);
								Collection<AttachmentInfo> colOldAttachment = AttachmentInfo.fromJson(oldAttachment);
								if(newAttachment.isEmpty())
									paramsClientBill.put(Field.ATTACHMENTS, oldAttachment);
								else {
									mergeAttachmentsLists(newAttachment, colOldAttachment);
									//colOldAttachment.addAll( (Collection<AttachmentInfo>)(AttachmentInfo.fromJson(otherFile)) );
									String jsonAdd = AttachmentInfo.toJson(newAttachment);
									paramsClientBill.put(Field.ATTACHMENTS, jsonAdd ) ;
								}
							}
							String jsonOReqMemoRABill = reqClBill.get(CorresConstants.CL_MEMORANDUM_RABILL);
							if(jsonOReqMemoRABill != null)
								paramsClientBill.put(CorresConstants.CL_MEMORANDUM_RABILL, jsonOReqMemoRABill);
							
							String jsonOReqFinalRep = reqClBill.get(CorresConstants.CL_FINAL_REPORT);
							if(jsonOReqFinalRep != null)
								paramsClientBill.put(CorresConstants.CL_FINAL_REPORT, jsonOReqFinalRep);
							
							String jsonOReqPaymtadvice = reqClBill.get(CorresConstants.CL_PAYMENT_ADVICE);
							if(jsonOReqPaymtadvice != null)
								paramsClientBill.put(CorresConstants.CL_PAYMENT_ADVICE, jsonOReqPaymtadvice);
							
							
							//Calendar nowCal = Calendar.getInstance();
							//nowCal.setTimeZone(TimeZone.getTimeZone("IST"));
							//Date nowDatenTime = new Date();
							
							//nowDatenTime.setTime(nowCal.getTimeInMillis());
							//String nowDateStr = Timestamp.toCustomFormat(nowDatenTime, "yyyy-MM-dd HH:mm:ss");
							//paramsClientBill.put(CorresConstants.CL_PENDING_FROM, nowDateStr);
							
							//paramsClientBill.put(Field.CATEGORY, CorresConstants.CL_PENDINGWITH_CLIENT);
							//paramsClientBill.put(CorresConstants.CL_CLIENT_DECISION, CorresConstants.CL_CLIENT_DECISION_PENDING);
										
							//nowCal.add(Calendar.DAY_OF_MONTH, CorresConstants.CL_CLIENT_KVK_DURATION);
							//nowDatenTime.setTime(nowCal.getTimeInMillis());
							//String dueDateStr = Timestamp.toCustomFormat(nowDatenTime, "yyyy-MM-dd HH:mm:ss");
							//paramsClientBill.put(Field.DUE_DATE, dueDateStr);
							
							String desc = "The Generated Invoice cover letter is sent via Correspondance: " ;
							desc += ba.getSystemPrefix()+"#"+currentRequest.getRequestId() + "#" + currentRequest.getMaxActionId() ;
							paramsClientBill.put(Field.DESCRIPTION, desc ) ;
							
							paramsClientBill.put(Field.REQUEST, reqStr);
							paramsClientBill.put(Field.BUSINESS_AREA, clBillBA.getSystemId()+"");
							paramsClientBill.put(Field.USER, CorresConstants.ROOT_USER);
							
							
							// update Request at Client Billing
							TBitsResourceManager trmgr = new TBitsResourceManager();
							UpdateRequest ur = new UpdateRequest() ;
							ur.setSource(TBitsConstants.SOURCE_CMDLINE);
							Request nr = new Request();
							nr = ur.updateRequest(connection, trmgr, paramsClientBill);
							trmgr.commit() ;
							
						}
						
						//  for Vendor Billing
						if(baPrefix != null && baPrefix.equals(CorresConstants.CORR_VENDOR_BILLING_SYSPREFIX)) {
							BusinessArea vnBillBA = BusinessArea.lookupBySystemPrefix(baPrefix);
							if(vnBillBA == null)
								throw new TBitsException ("Cannot get Business area from Database: "+baPrefix);
							Request reqVnBill = Request.lookupBySystemIdAndRequestId(connection, vnBillBA.getSystemId(), reqId);
							if (reqVnBill == null)
								throw new TBitsException ("Cannot get Request from Database: " +vnBillBA.getSystemPrefix()+"#"+reqStr);
							
							Hashtable<String,String> paramsVendorBill = new Hashtable<String,String>();
							
							if( null != logger)
								paramsVendorBill.put(Field.LOGGER, logger ) ;
							if( null != subscribers )
								paramsVendorBill.put(Field.SUBSCRIBER, subscribers) ;
							if( null != cc)
								paramsVendorBill.put(Field.CC, cc);
							if( null != corrNo )
								paramsVendorBill.put(CorresConstants.VN_CORRESPONDANCE_NUMBER, corrNo ) ;
							if( null != corrFile )
								paramsVendorBill.put(CorresConstants.VN_ATTCH_VN_PYMT_COVER_LTR, corrFile ) ;
							if( null != otherFile ) {
								Collection<AttachmentInfo> newAttachment = AttachmentInfo.fromJson(otherFile);
								String oldAttachment = reqVnBill.get(Field.ATTACHMENTS);
								Collection<AttachmentInfo> colOldAttachment = AttachmentInfo.fromJson(oldAttachment);
								if(newAttachment.isEmpty())
									paramsVendorBill.put(Field.ATTACHMENTS, oldAttachment);
								else {
									mergeAttachmentsLists(newAttachment, colOldAttachment);
									String jsonAdd = AttachmentInfo.toJson(newAttachment);
									paramsVendorBill.put(Field.ATTACHMENTS, jsonAdd ) ;
								}
							}
							
							String jsonOReqMemoVnBill = reqVnBill.get(CorresConstants.VN_ATTCH_MEMO_VN_BILL);
							if(jsonOReqMemoVnBill != null)
								paramsVendorBill.put(CorresConstants.VN_ATTCH_MEMO_VN_BILL, jsonOReqMemoVnBill);
							
							String jsonOReqMrnVnBill = reqVnBill.get(CorresConstants.VN_ATTCH_MRN_CERT_BILL_DC);
							if(jsonOReqMrnVnBill != null)
								paramsVendorBill.put(CorresConstants.VN_ATTCH_MRN_CERT_BILL_DC, jsonOReqMrnVnBill);
							
							String jsonOReqScnCpyVnBill = reqVnBill.get(CorresConstants.VN_ATTCH_SCANNED_COPY_CHEQUE);
							if(jsonOReqScnCpyVnBill != null)
								paramsVendorBill.put(CorresConstants.VN_ATTCH_SCANNED_COPY_CHEQUE, jsonOReqScnCpyVnBill);
							
							String desc = "The Generated Work Order cover letter is sent via Correspondance: " ;
							desc += ba.getSystemPrefix()+"#"+currentRequest.getRequestId() + "#" + currentRequest.getMaxActionId() ;
							paramsVendorBill.put(Field.DESCRIPTION, desc ) ;
							
							paramsVendorBill.put(Field.REQUEST, reqStr);
							paramsVendorBill.put(Field.BUSINESS_AREA, vnBillBA.getSystemId()+"");
							paramsVendorBill.put(Field.USER, CorresConstants.ROOT_USER);
							
							
							// update Request at Client Billing
							TBitsResourceManager trmgr = new TBitsResourceManager();
							UpdateRequest ur = new UpdateRequest() ;
							ur.setSource(TBitsConstants.SOURCE_CMDLINE);
							Request nr = new Request();
							nr = ur.updateRequest(connection, trmgr, paramsVendorBill);
							trmgr.commit() ;
								
						}
						
					}
				}
			
				
			} catch (TBitsException te) {
				te.printStackTrace();
				return new RuleResult( false , te.getDescription(), false );
			} catch (Exception e) {			
				e.printStackTrace();
				return new RuleResult( true , "Exception while adding the correspondance to the Individual Filed Letters business Area", false ) ;
			} catch (APIException e) {
				e.printStackTrace();
				return new RuleResult( true , "Exception while adding the correspondance to the Individual Filed Letters business Area", false ) ;
			}
			
			//currentRequest.setObject(CorresConstants.CORR_TEMP_FIELD_BILLING, "");
		}
		
		
		return new RuleResult (true, "client/vendor billing Corres Post Rule runs succesfully.", true);
	}

	public String getName() {
		return "Update Request for Client/Vendor billing.";
	}

	public double getSequence() {
		return 1;
	}

	 /**
	* Merges two Collections. To be used when adding new attachments into a request. This method, checks
	* if an attachments with the same name existed previously and modifies the new attachments collection.
	* @param newAttachments
	* @param prevAttachments
	*/
	public static void mergeAttachmentsLists(Collection<AttachmentInfo> newAttachments,
					Collection<AttachmentInfo> prevAttachments) {
		//If no previous attachments were found return without adding anything to newAttachments.
		if ((prevAttachments == null) || prevAttachments.isEmpty())
			return;

		//If no new attachments are there, add previous attachments to the new attachments collection,
		//so they are retained in the request.
		if ((newAttachments == null) || newAttachments.isEmpty())
			newAttachments = prevAttachments;
		else{
			Collection<AttachmentInfo> oldAI = new ArrayList<AttachmentInfo>();
			if ((newAttachments != null) && (!newAttachments.isEmpty())){
				for(AttachmentInfo ai : prevAttachments){
					boolean isFound = false;
					for(AttachmentInfo cAI : newAttachments){
						if(ai.name.equals(cAI.name)){
							cAI.requestFileId = ai.requestFileId;
							isFound = true;
							break;
						}
						else
							isFound = false;
					}
					if (!isFound)
						oldAI.add(ai);
				}
				newAttachments.addAll(oldAI);
			}
		}
	}
	
	
}
