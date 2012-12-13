package lancoCorres;

import java.util.Enumeration;
import java.util.Hashtable;

import transbit.tbits.domain.Field;

public class KskConstants 
{
	
	// user id of boss of KSK, other users who are in the same request_type_id and ksk wpcl member 
	// will be allowed to reply on his behalf
	public static final String KSK_BOSS_LOGIN = "suresh" ;

	public static final String WPCL_FULL_FIRM_NAME = "LANCO InfraTech Limited" ;
	public static final String CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME = "category_id" ;
	public static final String CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME = "status_id" ;
	public static final String CORR_TYPE_FIELD_NAME  = "severity_id" ; 
	public static final String CORR_LOGGER_FIELD_NAME = "logger_ids" ;
	public static final String CORR_ASSIGNEE_FIELD_NAME = "assignee_ids" ;
	public static final String CORR_SUBSCRIBER_FIELD_NAME = "subscriber_ids" ;
	public static final String CORR_GENERATE_CORRESPONDANCE_FIELD_NAME = "GenerateCorrespondance" ;
	public static final String CORR_CORR_GENERATION_AGENCY_FIELD_NAME = "CorrGenerationAgency" ;
	public static final String CORR_CORRESPONDANCE_FILE_FIELD_NAME = "CorrespondanceFile" ;
	public static final String CORR_CORRESPONDANCE_NUMBER_FIELD = "CorrespondanceNumber" ;
	public static final String CORR_CORR_PROT_WCPL_SEPCO = "WPCLSEPCO" ;
	public static final String CORR_CORR_PROT_OTHERS = "Others" ;	
	public static final String CORR_OTHER_ATTACHMENTS_FIELD_NAME = Field.ATTACHMENTS ;
	
	public static final String FC_CORRESPONDANCE_PROTOCOL_FIELD_NAME = " " ;
	public static final String FC_CORRESPONDANCE_INITIATOR_FIELD_NAME = "status_id" ;
	public static final String FC_TYPE_FIELD_NAME  = "severity_id" ; 
	public static final String FC_LOGGER_FIELD_NAME = "logger_ids" ;
	public static final String FC_ASSIGNEE_FIELD_NAME = "assignee_ids" ;
	public static final String FC_SUBSCRIBER_FIELD_NAME = "subscriber_ids" ;
	public static final String FC_CORRESPONDANCE_FILE_FIELD_NAME = "CorrespondanceFile" ;
	public static final String FC_CORRESPONDANCE_NUMBER_FIELD = "CorrespondanceNumber" ;
	public static final String FC_RECEPIENT_FIELD_NAME = "request_type_id" ;
	
	public static final String CORR_SYSPREFIX = "corr";
	public static final String FC_SYSPREFIX = "FC";
	
	
	static final String EXT_SUBMIT_BUTTON_LIST = "extSubmitButtonList";
	
	public static final String WPCL_FIRM_NAME = "WPCL" ;
	public static final String SEPCO_FIRM_NAME = "SEPCO" ;
	public static final String ROOT_USER = "root";
	public static String UPDATE_PREFILL_FILE_NAME = "corresUpdatePrefill.js" ;

	public static final String ADD_REQ_PREFILL_FILE = "corresAddPrefill.js";
	
	public static final String REP_TBITS_BASE_URL_KEY = "tbits_base_url";
//	public static final String NEXT_CORRESPONDANCE_NO = "correspondance_no" ;
	static final String REP_RID = "rid";
	static final String REP_CURRENT_DATE = "CurrantDate";
	static final String REP_IMAGE_PATH = "ImagePath";
	static final String REP_ATTACHMENT = "Attachment";
	static final String REP_CC = "Cc";
	static final String REP_DESIGNATION = "Designation";
	static final String REP_LOGGER = "Logger";
	static final String REP_COMPANY = "company";
	static final String REP_DESCRIPTION = "Description";
	static final String REP_TITLE = "Title";
	static final String REP_TO = "To";
	static final String REP_KIND_ATT = "kindAtt";
	static final String REP_CURRENTCORRESPONDENCE_NO = "CurrentcorrespondenceNo";
	static final String REP_DEAR = "Dear" ;
	static final String MSG_NO_MAPPING_FOUND = "No mapping for user found.<br>You are not allowed to add/reply request.";
	static final String MSG_CANNOT_PREFILL = "Cannot pre-fill form. Please fill it manually.";

	public static final int DUEDATE_SLIDE_ADDREQUEST = 14;

	public static final int DUEDATE_SLIDE_UPDATEREQUEST = 10;
	
//	public static final String CORRESPONDANCE_FILE_FIELD = "CorrespondanceFile"; // CorrespondanceFile
	
//	public static final String GENERATE_CORRESPONDANCE_FIELD = "GenerateCorrespondance";
	
	
	
	
//	public static final String CORRESPONDANCE_NUMBER_FIELD = "CorrespondanceNumber";
	
//	public static final String CORR_GENERATION_AGENCY_FIELD = "CorrGenerationAgency";
	
	
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
	
	
}
