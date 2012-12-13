package kskQlt;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Field;
import transbit.tbits.exception.TBitsException;

public class QltConstants 
{	
	public static final TBitsLogger LOG = TBitsLogger.getLogger("kskQlt");
	public static final String QLT_SYSPREFIX = "QLT";
	
	public static final String QLT_GEN_INSP_NO_FIELD_NAME = "GenerateICNo";
	public static final String QLT_INSP_NO_FIELD_NAME = "SEPCOICNo";
	public static final String QLT_LLOYDS_DOC_NO_FIELD_NAME = "LloydsDocNumber";
	public static final String QLT_GEN_MDCC_NO_FIELD_NAME = "GenerateMDCCNo";
	public static final String QLT_MDCC_NO_FIELD_NAME = "MDCCNumber";
	public static final String QLT_UNIT_NO_FIELD_NAME = Field.CATEGORY ;
	public static final String QLT_SUB_AREA_CODE_FIELD_NAME = Field.REQUEST_TYPE ;
	public static final String QLT_DECISION_FIELD_NAME = Field.SEVERITY ;
	public static final String QLT_FLOW_STATUS_FIELD_NAME = Field.STATUS ;
	public static final String QLT_SUBJECT_FIELD_NAME = Field.SUBJECT ;
	public static final String QLT_INSP_FILE_FIELD_NAME = "InspectionCall" ;
	public static final String QLT_LLOYDS_FILE_FIELD_NAME = "LLoydsDecisionFile" ;
	public static final String QLT_MDCC_FILE_FIELD_NAME = "MDCCFile" ;
	public static final String QLT_OTHER_ATT_FIELD_NAME = Field.ATTACHMENTS ;
	public static final String QLT_PDFI_FIELD_NAME = "PreDispatchFinalInspection" ; 
	
	public static final String QLT_DEC_NONE = "None" ;
	public static final String QLT_DEC_PEND_INSP = "PendingInspection" ;
	public static final String QLT_DEC_IRN = "IRN";
	public static final String QLT_DEC_NAN = "NAN" ;
	
	public static final String QLT_PDFI_NA = "NotApplicable";
	public static final String QLT_PDFI_YES = "Yes" ;
	public static final String QLT_PDFI_NO = "No" ;
	
	public static final String QLT_FS_PEND_ISS_IC = "PendingIssueofIC" ;
	public static final String QLT_FS_INSP_CALL_ISS = "InspectionCallIssued" ;
	public static final String QLT_FS_PEND_ISS_RE_INSP_CALL = "PendingIssueOfReInspectionCall";
	public static final String QLT_FS_REINSP_CALL_ISS = "ReInspectionCallIssued";
	public static final String QLT_FS_PEND_ISS_MDCC = "PendingIssueOfMDCC" ;
	public static final String QLT_FS_MDCC_ISS = "MDCCIssued" ;
	
	public static final String QLT_INSP_CALL_NO_PREFIX = "WPCL" ;
	
	public static final String QLT_MDCC_CALL_NO_PREFIX = "WCG" ;

	public static int incrAndGetMaxId(String prefix, Connection con) throws TBitsException 
	{		
		LOG.info("generating next number for : " + prefix );
		try {	
			CallableStatement stmt = con.prepareCall("stp_getAndIncrMaxId ?");
			stmt.setString(1, prefix );
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("max_id");
				LOG.info("Returning the next number = " + id );
				return id;
			} else {
				throw new TBitsException("Cannot generate the next " + prefix + " number ");
			}
		} catch (SQLException e) {
			throw new TBitsException("Cannot generate the next " + prefix + " number ");
		}	
	}

	public static String showError( String errorMsg )
	{
		 String html = "<script type='text/javascript'> \n" +
	 			"function appendMessage(msg)\n"+
				"{		\n"+
				"		var existingMsg = document.getElementById('exceptions').innerHTML ;\n"+
				"		if( existingMsg == null )\n"+
				"			existingMsg = '' ;\n"+
				"		var newMsg = existingMsg + \"<table id='table6' cellpadding='0' cellspacing='0' width='100%'>\" \n"+
				"		+ \"<span style='{font-weight: bold;color:blue}'>Messages:</span><br><span style='color:blue'>\" + msg + \"</span>\" + \"</table>\";\n"+
				"		document.getElementById('exceptions').innerHTML = newMsg ;\n"+
				"		document.getElementById('exceptionBlock').style.display = 'block';\n"+
				"		window.location = '#top';\n"+
				"}\n" +
		 		" function prefillException() \n" +
		 		" { \n" +
		 		"   // alert( 'prefillException called' ) ; \n" +
		 		"	appendMessage( \" " + errorMsg + " \" ) ;\n" +
		 		" } \n" +
		 		" YAHOO.util.Event.addListener( window, 'load', prefillException ) ; \n" +
		 		" </script> \n"	; 
		 
		 return html ;		 
	}

}
