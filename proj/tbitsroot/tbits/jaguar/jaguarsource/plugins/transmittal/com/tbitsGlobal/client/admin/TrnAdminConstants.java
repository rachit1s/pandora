package transmittal.com.tbitsGlobal.client.admin;

import com.google.gwt.core.client.GWT;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
/**
 * The constants and other declarations used throught the 
 * admin panel
 * @author devashish
 *
 */
public interface TrnAdminConstants {
	public final TrnAdminServiceAsync trnAdminService = GWT.create(TrnAdminService.class);
	
	
	public static final LinkIdentifier TRN_PROCESS_PARAMS 			= new LinkIdentifier("Transmittal Process Params", "tpp");
	public static final LinkIdentifier TRN_PROCESSES 				= new LinkIdentifier("Transmittal Processes", "tps");
	public static final LinkIdentifier POST_TRN_FIELD_MAP			= new LinkIdentifier("Post Transmittal Field Values", "tptfv");
	public static final LinkIdentifier SRC_TARGET_FIELD_MAP 		= new LinkIdentifier("Source Target Field Map", "tstfm");
	public static final LinkIdentifier DIST_LIST 					= new LinkIdentifier("Distribution Table", "tdt");
	public static final LinkIdentifier ATTACHMENT_LIST 				= new LinkIdentifier("Attachment Selection Table", "tast");
	public static final LinkIdentifier DROPDOWN_LIST 				= new LinkIdentifier("Trn Dropdown List", "tdl");
	public static final LinkIdentifier DRAWING_NUMBER_FIELD 		= new LinkIdentifier("Drawing Number Table", "tdnt");
	public static final LinkIdentifier VALIDATION_RULES 			= new LinkIdentifier("Validation Rules Table", "tvrt");
	public static final LinkIdentifier REPLICATE_PROCESS 			= new LinkIdentifier("Replicate Process", "trp");
	public static final LinkIdentifier CREATE_PROCESS				= new LinkIdentifier("Create Process", "tcp");
}
