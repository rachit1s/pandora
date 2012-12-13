package corrGeneric.com.tbitsGlobal.client;

import com.google.gwt.core.client.GWT;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;

import corrGeneric.com.tbitsGlobal.client.services.CorrAdminService;
import corrGeneric.com.tbitsGlobal.client.services.CorrAdminServiceAsync;

public interface CorrConstants {
	public final CorrAdminServiceAsync corrAdminService = GWT.create(CorrAdminService.class);

	public static final LinkIdentifier CORR_PROPERTIES				= new LinkIdentifier("Correspondence Properties", "cccp");
	public static final LinkIdentifier CORR_PROTOCOL_OPTIONS 		= new LinkIdentifier("Corr Protocol Options", "cpo");
	public static final LinkIdentifier CORR_REPORT_MAP 				= new LinkIdentifier("Report Map", "crm");
	public static final LinkIdentifier CORR_REPORT_NAME_MAP 		= new LinkIdentifier("Report Name Map", "crnm");
	public static final LinkIdentifier CORR_REPORT_PARAM_NAME_MAP 	= new LinkIdentifier("Report Params Map", "crpm");
	public static final LinkIdentifier BA_FIELD_MAP 				= new LinkIdentifier("BA Field Map", "cbfm");
	public static final LinkIdentifier FIELD_NAME_MAP				= new LinkIdentifier("Field Name Map", "cfnm");
	public static final LinkIdentifier ON_BEHALF_MAP 				= new LinkIdentifier("On Behalf Map", "cobm");
	public static final LinkIdentifier USER_MAP 					= new LinkIdentifier("User Map", "cum");
	public static final LinkIdentifier CORR_NUMBER_CONFIG           = new LinkIdentifier("Corr Number Configuration", "ccnc");
}
