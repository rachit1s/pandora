package corrGeneric.com.tbitsGlobal.shared.objects;

import java.io.Serializable;


public class GenericParams implements Serializable
{
	/**
	 * BaField constants 
	 * field Mappings : also includes constants
	 */ 

	public static final String OnBehalfType1 = "onbehalf_type1";
	public static final String OnBehalfType2 = "onbehalf_type2";
	public static final String OnBehalfType3 = "onbehalf_type3";
	
	public static final String NumType1 = "num_type1";
	public static final String NumType2 = "num_type2";
	public static final String NumType3 = "num_type3";
	
	public static final String UserMapType1 = "user_map_type1";
	public static final String UserMapType2 = "user_map_type2";
	public static final String UserMapType3 = "user_map_type3";

	public static final String ReportType1 = "report_type1";
	public static final String ReportType2 = "report_type2";
	public static final String ReportType3 = "report_type3";
	public static final String ReportType4 = "report_type4";
	public static final String ReportType5 = "report_type5";
	
	public static final String LoggerFieldName = "onbehalf_of_login";
	public static final String OriginatorFieldName = "originator_agency";
	public static final String RecepientAgencyFieldName = "recepient_agency";
	public static final String GenerationAgencyFieldName = "generation_agency";
	public static final String GenerateCorrespondenceFieldName = "generate_correspondence";
	public static final String CorrespondenceNumberFieldName = "correspondence_number";
	public static final String CorrespondenceFileFieldName = "correspondence_file";
	public static final String RecepientUserTypeFieldName = "recepient_usertype";
	public static final String OtherAttachmentFieldName = "other_attachment";
	public static final String DisableProtocolFieldName = "disable_protocol";
	
	public static final String CcUserTypeFieldName = "cc_usertype";
	public static final String StatusFieldName = "status";

	public static final String CorrDescriptionFieldName = "corr_description";
	
	/**
	 * properties constants : to be included in corr_properties
	 */

	public static final String PropOnBehalfUserCacheSize = "onbehalf_user_cache_size";
	public static final String PropOnBehalfUserCacheWindowSize = "onbehalf_user_cache_window_size";

	public static final String PropOnBehalfSysCacheSize = "onbehalf_sys_cache_size";
	public static final String PropOnBehalfSysCacheWindowSize = "onbehalf_sys_cache_window_size";

	public static final String PropFieldNameCacheSize = "field_name_cache_size";
	public static final String PropFieldNameCacheWindowSize = "field_name_cache_window_size";
	
	public static final String PropBaFieldMapCacheSize = "ba_field_cache_size";
	public static final String PropBaFieldMapCacheWindowSize = "ba_field_cache_window_size";
	
	public static final String PropUserMapCacheSize = "user_map_cache_size";
	public static final String PropUserMapCacheWindowSize = "user_map_cache_window_size";
	
	public static final String PropReportMapCacheSize = "report_map_cache_size";
	public static final String PropReportMapCacheWindowSize = "report_map_cache_window_size";
	
	public static final String PropReportNameMapCacheSize = "report_name_map_cache_size";
	public static final String PropReportNameMapCacheWindowSize = "report_name_map_cache_window_size";
	
	public static final String PropReportParamsCacheSize = "report_params_cache_size";
	public static final String PropReportParamsCacheWindowSize = "report_params_cache_window_size";
	
	public static final String PropProtocolOptionsCacheSize = "protocol_options_cache_size";
	public static final String PropProtocolOptionsCacheWindowSize = "protocol_options_cache_window_size";
	
	public static final String PropProtocolOptionsNameCacheSize = "protocol_options_name_cache_size";
	public static final String PropProtocolOptionsNameCacheWindowSize = "protocol_options_name_cache_window_size";
	
	public static final String PropCorrNumCacheSize = "corr_number_config_cache_size";
	public static final String PropCorrNumCacheWindowSize = "corr_number_config_cache_window_size";
	
	public static final String PropSignatureImageDir = "signature_image_dir"; 	// defalt : tbitsreport
	public static final String PropReportImageDir = "report_image_dir"; 	// defalt : tbitsreport
	
	public static final String ProtSetLoggerToSelf = "set_logger_to_self" ; // default no
	public static final String ProtSetLoggerToSelf_Yes = "yes";
	public static final String ProtSetLoggerToSelf_No = "no";
	
	/**
	 *  TBits Properties
	 */
	public static final String CommaSeparatedListOfApplicableBa = "comma_separated_list_of_applicable_bas_for_correspondence" ;
	
	public static final String CorrBaList = "comma_separated_list_of_correspondence_BAs";
	
	public static final String DIBaList = "comma_separated_list_of_di_BAs";
	/**
	 * protocol options
	 */
	
	public static final String OnBehalf1_Other = "onbehalf1_other";
	public static final String OnBehalf2_Other = "onbehalf2_other";
	public static final String OnBehalf3_Other = "onbehalf3_other";
	public static final String UserMap1_Other = "usermap1_other";
	public static final String UserMap2_Other = "usermap2_other";
	public static final String UserMap3_Other = "usermap3_other";
	
	/** 
	 * required for updating the corresponding di request when the corr
	 * request is submitted from tranfer from di
	 */
	public static final String ProtMappedDIBA = "mapped_to_di";
	public static final String SuperUser = "super_user";
	
	public static final String ProtFollowOnBehalf = "protocol_follow_on_behalf";
	public static final String ProtFollowOnBehalf_Yes = "yes"; // default
	public static final String ProtFollowOnBehalf_No = "no";
	
	public static final String MoreThanOneLoggerAllowed = "more_than_one_logger_allowed";
	public static final String MoreThanOneLoggerAllowed_Yes = "yes";
	public static final String MoreThanOneLoggerAllowed_No = "no"; // default

	public static final String ProtTransferTo_WithUpdate = "transfer_to_with_update";
	public static final String ProtTransferTo_WithoutUpdate = "transfer_to_without_update" ;
	
	public static final String ProtSendMeEmail = "send_me_email";
	public static final String ProtSendMeEmail_Yes = "yes";
	public static final String ProtSendMeEmail_No = "no";
	
	public static final String ProtShowConfirmationOnSubmit = "show_confirmation_on_submit";
	public static final String ProtShowConfirmationOnSubmit_Yes = "yes";
	public static final String ProtShowConfirmationOnSubmit_No = "no";
	
	/**
	 *  corr_report_param_map table constants.
	 */
	
	public static final String ParamType_Variable = "variable";
	public static final String ParamType_ReportParameter = "report_parameter";
	
	public static final String ParamValueType_Const = "constant";
	public static final String ParamValueType_JavaClass = "java_class";
	public static final String ParamValueType_JavaObject = "java_object";
	
	/**
	 *  specific field value.
	 */
	
	public static final String GenerateCorr_BothNumberAndPdf             =   "BothNumberAndPdf"         ; 
	public static final String GenerateCorr_OnlyPdfWithSpecifiedNumber   =   "OnlyPdfWithSpecifiedNumber";
	public static final String GenerateCorr_NoPdforCorrNumber            =   "NoPdforCorrNumber" ;         
	public static final String GenerateCorr_OnlyNumber					 =   "OnlyNumber";
	
	public static final String DisableProtocol_True = "true";
	public static final String DisableProtocol_False = "false";
	
	public static final String StatusClosed = "closed";

	/**
	 * message constants.
	 */
	public static final String FAILED_TO_RETRIEVE = "Exception occured while retrieving ";
	public static final String FAILED_CON = "Failed to obtain connection.";
}
