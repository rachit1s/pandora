package nccCorres;

import static nccCorres.CorresConstants.CORR_ORIGINATOR_FIELD_NAME;
import static nccCorres.CorresConstants.CORR_ORIG_NCCP;
import static nccCorres.CorresConstants.CORR_SYSPREFIX;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

public class CorresConstants 
{
	public static final TBitsLogger LOG = TBitsLogger.getLogger("nccCorres");
	
	public static final String CORR_CORR_TYPE_FIELD_NAME = Field.CATEGORY ;
	public static final String CORR_ORIGINATOR_FIELD_NAME = Field.REQUEST_TYPE ;
	public static final String CORR_RECEPIENT_FIELD_NAME  = Field.STATUS ; 
	public static final String CORR_CORR_STATUS_FIELD_NAME = Field.SEVERITY ;
	public static final String CORR_LOGGER_FIELD_NAME = Field.LOGGER ;
	public static final String CORR_ASSIGNEE_FIELD_NAME = Field.ASSIGNEE ;
	public static final String CORR_SUBSCRIBER_FIELD_NAME = Field.SUBSCRIBER ;
	public static final String CORR_GENERATE_FIELD_NAME = "GenerateCorrespondance" ;
	public static final String CORR_PROTOCOL_APPLICABLE_FIELD_NAME = "ProtocolApplicable" ;
	public static final String CORR_CONTRACT_REFERENCE_FIELD_NAME = "ContractReference" ;
	public static final String CORR_CORRESPONDANCE_FILE_FIELD_NAME = "CorrespondanceFile" ;
	public static final String CORR_CORRESPONDENCE_NUMBER_FIELD = "CorrespondanceNumber" ;
	public static final String CORR_OTHER_ATTACHMENTS_FIELD_NAME = Field.ATTACHMENTS ;
	public static final String CORR_DESCRIPTION_FIELD_NAME = Field.DESCRIPTION ;
	public static final String CORR_SUMMARY_FIELD_NAME = Field.SUMMARY ;
	public static final String CORR_CC_FIELD_NAME = Field.CC ;
	public static final String CORR_CHECKED_BY_FIELD_NAME = "CheckedBy" ;
	public static final String CORR_SUBJECT_FIELD_NAME = Field.SUBJECT ;
	public static final String CORR_PACKAGE_FIELD_NAME = "Package";
	public static final String CORR_LOGIN_USER_FIELD_NAME = Field.USER ;
	public static final String CORR_WBSATT_FIELD_NAME = "WBSAttributes" ;
	public static final String CORR_GENATT_FIELD_NAME = "GeneralAttributes" ;
	public static final String CORR_DISCIPLINE_FIELD_NAME = "Discipline";
	public static final String CORR_LINKED_REQUESTS_FIELD_NAME = Field.RELATED_REQUESTS; // Billing
	public static final String CORR_TEMP_FIELD_BILLING = "tempFieldBilling"; // Billing
	public static final String CORR_CLIENT_BILLING_SYSPREFIX = "CL_BILL"; // Billing
	public static final String CORR_VENDOR_BILLING_SYSPREFIX = "VN_BILL"; // Billing
	
	public static final String CORR_LOC_FIELD_NAME = "Location" ;
	public static final String CORR_LOC_HYD = "hyd" ;
	public static final String CORR_LOC_DELHI = "del" ;
	
	public static final String CORR_GEN_BOTH_FILE_AND_NUMBER = "Correspondencewithfilenumber" ;
	public static final String CORR_GEN_FILE_WITH_GIVEN_NUMBER = "Correspondencewithusernumberandfile" ;
	public static final String CORR_GEN_DONT_GEN_ANYTHING = "ActionOnly" ;
	
	public static final String CORR_CORR_TYPE_NONE = "None";
	public static final String CORR_CORR_TYPE_ION = "ION" ;
	
	public static final String CORR_CORR_STATUS_NONE = "None" ;
	
	public static final String CORR_WBSATT_NONE = "None" ;
	
	public static final String CORR_GENATT_NONE = "None" ;

	public static final String CORR_ORIG_NCCP = "NCCP";
	public static final String CORR_ORIG_NCCB = "NCCB" ;
	public static final String CORR_ORIG_KNPL = "KNPL";
	public static final String CORR_ORIG_DCPL = "DCPL";
	public static final String CORR_ORIG_DESEIN = "DESN";
	public static final String CORR_ORIG_EDTD = "EDTD";
	public static final String CORR_ORIG_CSEPDI = "CSEP";
	public static final String CORR_ORIG_PTPL = "PTPL";
	
	public static final String [] VALID_ION_AGENCY = {CORR_ORIG_NCCP , CORR_ORIG_NCCB} ;
	
	public static final String CORR_PACK_NONE = "None" ;
	
	public static final String CORR_DISC_NONE= "None";	
	
//	public static final String FC_LOGIN_USER_FIELD_NAME = Field.USER ;
	public static final String FC_SUBJECT_FIELD_NAME = Field.SUBJECT ;
	public static final String FC_CORR_TYPE_FIELD_NAME = Field.CATEGORY ;
	public static final String FC_RECEPIENT_FIELD_NAME = Field.STATUS ;	
	public static final String FC_ORIGINATOR_FIELD_NAME = Field.REQUEST_TYPE ;
	public static final String FC_YEAR_FIELD_NAME = Field.SEVERITY ;	
	public static final String FC_LOGGER_FIELD_NAME = Field.LOGGER ; 
	public static final String FC_ASSIGNEE_FIELD_NAME = Field.ASSIGNEE ;
	public static final String FC_SUBSCRIBER_FIELD_NAME = Field.SUBSCRIBER ;
	public static final String FC_CC_FIELD_NAME = Field.CC ;
	public static final String FC_CHECKED_BY_FIELD_NAME = "CheckedBy" ;
	public static final String FC_CONTRACT_REFERENCE_FIELD_NAME = "ContractReference" ;
	public static final String FC_CORRESPONDANCE_FILE_FIELD_NAME = "CorrespondanceFile" ;
	public static final String FC_CORRESPONDANCE_NUMBER_FIELD_NAME = "CorrespondanceNumber" ;
	public static final String FC_OTHER_ATTACHMENT_FIELD_NAME = Field.ATTACHMENTS ;
	public static final String FC_DESCRIPTION_FIELD_NAME = Field.DESCRIPTION ;
	public static final String FC_SUMMARY_FIELD_NAME = Field.SUMMARY ;
	public static final String FC_PACKAGE_FIELD_NAME = "Package";
	public static final String FC_WBSATT_FIELD_NAME = "WBSAttributes" ;
	public static final String FC_GENATT_FIELD_NAME = "GeneralAttributes" ;
	public static final String FC_DISCIPLINE_FIELD_NAME = "Discipline";
	
	public static final String DI_CORR_TYPE_FIELD_NAME = Field.CATEGORY ;
	public static final String DI_ORIGINATOR_FIELD_NAME = Field.STATUS ;
	public static final String DI_RECEPIENT_FIELD_NAME  = Field.SEVERITY ; 
	public static final String DI_CORR_STATUS_FIELD_NAME = Field.REQUEST_TYPE ;
	public static final String DI_LOGGER_FIELD_NAME = Field.LOGGER ;
	public static final String DI_ASSIGNEE_FIELD_NAME = Field.ASSIGNEE ;
	public static final String DI_SUBSCRIBER_FIELD_NAME = Field.SUBSCRIBER ;
	public static final String DI_GENERATE_FIELD_NAME = "GenerateCorrespondance" ;
	public static final String DI_PROTOCOL_APPLICABLE_FIELD_NAME = "ProtocolApplicable" ;
	public static final String DI_CONTRACT_REFERENCE_FIELD_NAME = "ContractReference" ;
	public static final String DI_CORRESPONDANCE_FILE_FIELD_NAME = "CorrespondanceFile" ;
	public static final String DI_CORRESPONDENCE_NUMBER_FIELD = "CorrespondanceNumber" ;
	public static final String DI_OTHER_ATTACHMENTS_FIELD_NAME = Field.ATTACHMENTS ;
	public static final String DI_DESCRIPTION_FIELD_NAME = Field.DESCRIPTION ;
	public static final String DI_SUMMARY_FIELD_NAME = Field.SUMMARY ;
	public static final String DI_CC_FIELD_NAME = Field.CC ;
	public static final String DI_CHECKED_BY_FIELD_NAME = "CheckedBy" ;
	public static final String DI_SUBJECT_FIELD_NAME = Field.SUBJECT ;
	public static final String DI_RELATED_REQUEST_FIELD_NAME = Field.RELATED_REQUESTS ;
	
	public static final String DI_SYSPREFIX = "DI" ;	
	
	public static final String DI_SEND_BUTTON_NAME = "Send Reply" ;
	public static final  String CORR_LINK_NAME = "Discuss-Internally" ;
	public static  final String DI_LINK_NAME = "Send-Reply" ;
	public static  final String CORR_SYS_ID = "corrSysId" ;
	public static  final String CORR_REQUEST_ID = "corrRequestId" ;
	public static final  String DI_SYS_ID="diSysId" ;
	public static final  String DI_REQUEST_ID = "diRequestId" ;
	public static final  String DI_CORR_STATUS_OPEN = "Open" ;
	
	public static final String CORR_SYSPREFIX = "CORR";
	public static final String FC_SYSPREFIX = "FC";	
	public static final String ROOT_USER = "root";
	//public static final String CORR_NCC_MAX_ID = "NCC_CORR_NO_MAX_ID" ;
	
	public static final String EXT_SUBMIT_BUTTON_LIST = "extSubmitButtonList";	
	public static final String PREFILL_JS_FILE = "corresPrefill.js";
	public static final String NCC_REPORT_FILE = "Ncc_corr_template.rptdesign" ;
	public static final String KNPL_REPORT_FILE = "Ncc_kvk_corr_template.rptdesign" ;
	public static final String NCC_ION_REPORT_FILE = "NCC_corr_ION_template.rptdesign" ;
	public static final String DCPL_REPORT_FILE = "NCC_dcpl_corr_template.rptdesign" ;
	public static final String DESIGN_REPORT_FILE = "Ncc_desein_corr_template.rptdesign" ;
	public static final String CSEPDI_REPORT_FILE = "Ncc_csepdi_corr_template.rptdesign";
	public static final String EDTD_REPORT_FILE = "Ncc_edtd_corr_template.rptdesign";
	public static final String NCC_CLIENT_BILLING_REPORT_FILE = "NCC_client_invoice_template.rptdesign"; // billing
	public static final String NCC_VENDOR_BILLING_REPORT_FILE = "NCC_vendor_invoice_template.rptdesign"; // billing
	
	public static final String REP_TBITS_BASE_URL_KEY = "tbits_base_url";
	public static final String REP_REF_NO = "refNo" ;
	public static final String REP_TO = "to" ;
	public static final String REP_KIND_ATTN = "kindAttn" ;
	public static final String REP_PROJECT = "project" ;
	public static final String REP_SUBJECT = "subject" ;
	public static final String REP_REFERENCE = "reference" ;
	public static final String REP_DESCRIPTION = "description" ;
	public static final String REP_CC = "Cc" ;
	public static final String REP_ATT = "Att" ;
	public static final String REP_RID = "rid";	
	public static final String REP_DEAR = "dear" ;
	public static final String REP_IMAGE_PATH = "ImagePath";
	public static final String REP_FOR_COMPANY = "for_company" ;
	public static final String REP_LOGGER = "logger" ;
	public static final String REP_SUBS = "subscriber" ;
	
	public static final String REP_ION_FROM = "from" ;
	public static final String REP_ION_TO = "to" ;
	public static final String REP_ION_REF = "ref" ;
	public static final String REP_ION_SUBJECT = "subject" ;
	public static final String REP_ION_LOGGER = "logger" ;
	public static final String REP_ION_IMAGE_PATH = "imagePath" ;
	public static final String REP_ION_DESCRIPTION = "description" ;
	public static final String REP_ION_DEAR = "dear" ;
	
	// billing
	public static final String CL_CLIENT_LETTER_REF				= "ClientLtrRefNo";
	public static final String CL_CLIENT_LETTER_REF_DATED		= "AmendmentDated";
	public static final String CL_INVOICENO						= "InvoiceNo";
	public static final String CL_CONTRACT_REFERENCE			= "ContractRef";
	public static final String CL_PENDING_FROM					= "PendingFrom";
	public static final String CL_BILLDETAILS					= "BillRemarks";
	public static final String CL_NET_PAYABLE					= "NetPayable";
	public static final String CL_CORRESPONDANCE_NO				= "CorresNo";
	public static final String CL_PENDINGWITH_CLIENT 			= "Client";
	public static final String CL_CLIENT_DECISION				= "ClientAction";
	public static final String CL_CLIENT_DECISION_PENDING		= "Pending";
	public static final int CL_CLIENT_KVK_DURATION				= 7;
	public static final String CL_FINAL_REPORT					= "GeneratedReport";
	public static final String CL_INVOICE_COVER_LETTER			= "InvoiceLetter";
	public static final String CL_MEMORANDUM_RABILL				= "MemoRABill";
	public static final String CL_PAYMENT_ADVICE				= "PaymentAdvice";
	public static final String CL_CURRENCY_TYPE					= "CurrencyType";
	
	public static final String REP_CLBILL_CORRESNO_DATE			= ""; // variable
	public static final String REP_CLBILL_BILL_DETAILS			= "DetailsofBill"; // ok
	public static final String REP_CLBILL_CONTRACT_REFERENCE	= "ContractRef"; // ok
	public static final String REP_CLBILL_LETTER_REFERENCE		= "YourLetterRef"; // ok
	public static final String REP_CLBILL_REF_DATED				= "AmendmentDate"; // ok
	public static final String REP_CLBILL_INVOICE_NO			= "InvoiceNo"; // ok
	public static final String REP_CLBILL_INVOICE_NO_DATE		= "date"; // ok
	public static final String REP_CLBILL_NET_PAYABLE			= "Amount"; // ok
	public static final String REP_CLBILL_MEMO_RA_FILE			= "Att"; // ok
	public static final String REP_CLBILL_SERIAL_NO				= "SNo"; // ok
	
	
	public static final String VN_VENDOR_INVOICE_NO					= "VendorInvNo";
	public static final String VN_VENDOR_INVOICE_DATED				= "VendorInvNoDated";
	public static final String VN_TOTAL_WORKORDER_VALUE				= "TotalWorkOrderValue";
	public static final String VN_TOTAL_TAXES_APPLICABLE			= "TotalTaxesAppl";
	public static final String VN_TOTAl_OTHER_DED					= "TotalOtherDed";
	public static final String VN_NETPAYABLE						= "NetPayable";
	public static final String VN_WORK_ORDER_NUMBER					= "WorkOrderNo";
	public static final String VN_SPLITTER_DESCRIPTION_BOX			= "~~";
	public static final String VN_CORRESPONDANCE_NUMBER				= "CorrespondanceNo";
	public static final String VN_ATTCH_VN_PYMT_COVER_LTR			= "VendorWOCoverLtr";
	public static final String VN_ATTCH_MEMO_VN_BILL				= "MemorandumVnBill";
	public static final String VN_ATTCH_MRN_CERT_BILL_DC			= "ScannedMatRecptDoc";
	public static final String VN_ATTCH_SCANNED_COPY_CHEQUE			= "ScannedCpyPaymtChq";
	public static final String VN_CURRENCY_TYPE						= "CurrencyType";
	
	public static final String REP_VNBILL_VENDOR_ADDRESS			= "To"; // ok
	public static final String REP_VNBILL_VENDOR_NAME				= "KindAttn"; // ok
	public static final String REP_VNBILL_SUBJECT					= "Subject"; // ok
	public static final String REP_VNBILL_TOTAL_WORKORDER_VAL		= "YourInvoiceAmount"; // ok
	public static final String REP_VNBILL_TAXES_N_DEDUCTION			= "TaxesDeduction"; // ok
	public static final String REP_VNBILL_ATTACHMENTS				= "Att"; // ok
	public static final String REP_VNBILL_VENDOR_INVOICE_NO			= "YourInvoiceNo"; // ok
	public static final String REP_VNBILL_VENDOR_INVOICENO_DATED	= "Dated"; // ok
	public static final String REP_VNBILL_NET_PAYABLE				= "NetAmountPayable"; // ok
	
	// billing
	
//	public static final String KNPL_CC_USER = "knplteam";

	public static final String DCPL_PROJECT_CONSTANT = "K9210" ;
	public static final String DCPL_CORRES_MAX_ID =  "DCPL-"+ DCPL_PROJECT_CONSTANT ;
	
	public static final String DESEIN_CORRES_MAX_ID = "DESEIN";
	public static final String DESEIN_PROJECT_CONSTANT = "D3034-VC20" ;
	
	public static final String[] CONT_REFS = {"General","CIF","ExWorks","CivilErection", "EngineeringTestingCommissioning", "NonEPC"};
	
	static final String MSG_NO_MAPPING_FOUND = "No mapping for user found.<br>You are not allowed to add/reply request.";
	static final String MSG_CANNOT_PREFILL = "Cannot pre-fill form. Please fill it manually.";

	public static final int DUEDATE_SLIDE_ADDREQUEST = 14;
	public static final int DUEDATE_SLIDE_UPDATEREQUEST = 10;
	
	public static String disableSubmit(String errorMsg) 
	{
		String html = "<script type='text/javascript'> \n" +		 		
		" function prefillException() \n" +
		" { \n" +
		"	//alert( 'disableReply called' ) ; \n" +
		"	document.getElementById('btnSubmit2').disabled = true ; \n" +
		"	showAutomaticRestrictions( '" + errorMsg + "' ) ;\n" +
		" } \n" +
		" YAHOO.util.Event.addListener( window, 'load', prefillException ) ; \n" +
		" </script> \n"	; 
	
		return html ;
		
	}
	public static String showError( String errorMsg )
	 {
		 String html = "<script type='text/javascript'> \n" +		 		
		 		" function prefillException() \n" +
		 		" { \n" +
		 		"   // alert( 'prefillException called' ) ; \n" +
		 		"	showAutomaticRestrictions( \" " + errorMsg + " \" ) ;\n" +
		 		" } \n" +
		 		" YAHOO.util.Event.addListener( window, 'load', prefillException ) ; \n" +
		 		" </script> \n"	; 
		 
		 return html ;		 
	 }
	
	/**
	 * take the fields name and returns the display name for the field for corr.ba
	 * @param fn
	 * @return
	 */
	public static String cfdn(String fn)
	{
		if(fn == null)
			return fn ;
		
		try {
			return Field.lookupBySystemIdAndFieldName(BusinessArea.lookupBySystemPrefix(CORR_SYSPREFIX).getSystemId(), fn ).getDisplayName() ;
		} catch (Exception e) {
			e.printStackTrace();
			return fn ;
		} 
	}
	
	/**
	 * return the display name of the type corresponding to tn for corr ba
	 * @param tn
	 * @return
	 */
	public static String ctdn(String fn, String tn)
	{
		if(tn == null || fn == null )
			return tn ;
		
		try {
			return Type.lookupBySystemIdAndFieldNameAndTypeName(BusinessArea.lookupBySystemPrefix(CORR_SYSPREFIX).getSystemId(),fn, tn ).getDisplayName() ;
		} catch (Exception e) {
			e.printStackTrace();
			return tn ;
		} 
	}
	public static String tagReplacer(Hashtable<String,String>tagtable, String filedata )
	{
		for( Enumeration<String> keys = tagtable.keys() ; keys.hasMoreElements() ; )
		{
			String key = keys.nextElement() ;
			String value = tagtable.get(key) ;
			filedata = filedata.replaceAll("<%=" + key + "%>", value ) ;
		}
		
		return filedata ;
	}
	public static String getExtSubmitButtonHTML( String id, String name, String value, String onClick )
	{
		String submitButtonHTML = "<input class=\"cw b bn sxs lsb\" value=\"" + value + "\" id= \"" + id 
							+ "\" onclick=\"" + onClick + "\" height=\"25\" type=\"button\"  name=\"" + name +"\" />" ;    	
	
		return submitButtonHTML ;
	}
	public static String getContRef(Type contractReference) throws TBitsException 
	{
		int cr = -1 ;
		for( int i = 0 ; i < CONT_REFS.length ; i++ )
		{			
			if( CONT_REFS[i].equals(contractReference.getName()) )
					cr = i ;
		}
		
		if( cr == -1 )
			throw new TBitsException("Cannot find the code corresponding to the contract reference : " + contractReference.getName());
		
		return cr+"";
	}

	public static JsonObject getAttachmentJson(BusinessArea businessArea,Hashtable<String, Integer> permissions, Hashtable<Field,Collection<AttachmentInfo>> attachTable)
	   {
	       JsonObject rootNode = new JsonObject();
	       JsonParser jsonParser = new JsonParser();
	       for(Enumeration<Field> fields = attachTable.keys() ; fields.hasMoreElements() ;)
	       {
	               Field f = fields.nextElement() ;
	               Integer fieldPermInteger = permissions.get(f.getName());
	               int fieldPerm = 0;
	               if(fieldPermInteger != null)
	                   fieldPerm = fieldPermInteger.intValue();
	               
	               if( (fieldPerm & Permission.VIEW) != 0)
	               {
	                   Boolean canChange = ( (fieldPerm & Permission.CHANGE) != 0);
	                   Boolean canAdd = ( (fieldPerm & Permission.ADD) != 0);
	                   JsonObject fieldNode = new JsonObject();
	                   fieldNode.addProperty("fieldDisplayName", f.getDisplayName());
	                   fieldNode.addProperty("canChange", canChange);
	                   fieldNode.addProperty("canAdd", canAdd);
	                   fieldNode.addProperty("displayOder", f.getDisplayOrder());
	                   fieldNode.addProperty("fieldId", f.getFieldId());
	                   fieldNode.addProperty("numberOfFiles", 0);
	                   String filesStr = null;
	                   
	                   if (attachTable != null) 
	                   {
	                       Collection<AttachmentInfo> attInfo = attachTable.get(f) ;                           
	                       filesStr = AttachmentInfo.toJson(attInfo);                     
	                   }
	                   
	                   if( (filesStr == null) || (filesStr.trim().length() == 0))
	                       filesStr = "[]";
	                   
	                   fieldNode.add("files", jsonParser.parse(filesStr));
	                   rootNode.add(f.getName(), fieldNode);
	             }
	       }
	       return rootNode;
	   }
	
	public static String getDisciplineConstant( CorresObject co )
	{
		Type disc = co.discipline ;
		if( null == disc || disc.getName().equals(CORR_DISC_NONE))
			return "ILLEGAL" ;		
		else
			return disc.getName() ;

	}
	public static boolean isValidIONUser(User user ) 
	{
		// if this function is changed then you will also have to change the allowedAgencies parameter value in CorresPrefill.java
//		String firmCode = user.getFirmCode() ;
		for( String agency : VALID_ION_AGENCY)
		{
			if(user.getFirmCode().equals(agency) )		
				return true ;				
		}
		
		return false;
	}	
}
