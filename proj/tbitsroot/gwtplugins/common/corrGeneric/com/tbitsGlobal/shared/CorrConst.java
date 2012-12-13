package corrGeneric.com.tbitsGlobal.shared;

import java.io.Serializable;
import java.util.ArrayList;

import corrGeneric.com.tbitsGlobal.client.plugins.CorrAddReqGWTPlugin;
import corrGeneric.com.tbitsGlobal.client.plugins.CorrUpdateReqGWTPlugin;
import corrGeneric.com.tbitsGlobal.client.plugins.CorrViewReqGWTPlugin;
import corrGeneric.com.tbitsGlobal.client.services.CorrDBServiceAsync;

public class CorrConst implements Serializable
{
//	public static final String InitOnBehalfList = "InitOnBehalfList";
	public static final String InitOnBehalfMap = "InitOnBehalfMap";
	public static final String InitFieldNameMap = "InitFieldNameMap";
	public static final String InitOptionsMap = "InitOptionsMap";
	
	public static CorrDBServiceAsync corrDBService = null;

//	public static final String ShowConfirmation = "ShowConfirmation";
	public static final String ApplicableBas	= "ApplicableBas";
//	public static final String GenerateCorrFieldNames	= "GenerateCorrFieldNames";
	
//	public static HashMap<String,String> showConfirmationValues = null;
//	public static HashMap<String,String> genCorrFieldNamesMap = null;
	public static ArrayList<String> applicableBas = null ;
	
//	public static final String TransferToOptions = "TransferToOptions";
//	public static final String SendMeEmailBas = "SendMeEmailBas";
//	public static final String StatusFieldNames = "StatusFieldNames";

//	public static ArrayList<String> transferToAppBas = null;
//	public static ArrayList<ProtocolOptionEntry> transferToOptions = null;
//	public static HashMap<String,String> statusFieldName = null; // sysPrefix,statusFieldName
//	public static HashMap<String,String> sendMeEmail = null; // sysprefix where send-me-email to be shown
	
	public static CorrAddReqGWTPlugin corrAddReqGWTPlugin = null;
	public static CorrUpdateReqGWTPlugin corrUpdateReqGWTPlugin = null;
	public static CorrViewReqGWTPlugin corrViewReqGWTPlugin = null;
	

	public static final String SendMeEmailLinkName = "Send-Me-Email";
	public static final String TransferToLinkName = "Transfer-To";
	/**
	 * prefill constants
	 */
	public static final String REQUEST_DATA = "request-data";
	public static final String PREFILL_FIELDS = "prefill_fieldS"; 
}
