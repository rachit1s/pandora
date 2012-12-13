package ccr;

import transbit.tbits.domain.Field;

public class CCRConstants
{
	final public static String CCR_SYSPREFIX = "CCR" ;
	final public static String CCREXT_SYSPREFIX = "ExtCCR" ;
	
	final public static String  CCR_PROJECT_FIELD_NAME = Field.CATEGORY ;
	final public static String  CCR_PREPARED_BY_FIELD_NAME = Field.LOGGER ;
	final public static String  CCR_ASSIGNEE_FIELD_NAME = Field.ASSIGNEE ;
	final public static String CCR_CCR_FIELD_NAME = Field.SEVERITY ;
	final public static String CCR_CCD_FIELD_NAME = Field.STATUS ;
	final public static String CCR_EPMD_FIELD_NAME = Field.REQUEST_TYPE ;
	final public static String  CCR_CC_FIELD_NAME = Field.CC ;
	final public static String CCR_SUBS_FIELD_NAME = Field.SUBSCRIBER ;
	final public static String CCR_CCR_NO_FIELD_NAME = "CCRNo" ;
	final public static String CCR_CONTRACT_DOC_FIELD_NAME = "ContractDocument" ;
	final public static String CCR_SECTION_FIELD_NAME = "Section" ;
	final public static String CCR_LOCATION_FIELD_NAME = "Location" ;
	final public static String CCR_PROJECT_NO_FIELD_NAME = "ProjectNo" ;
	final public static String CCR_CON_CLA_REQ_FIELD_NAME = "ContractClarificationRequest" ;
	final public static String CCR_CON_REQ_FIELD_NAME = "ContractRequirement" ;
	final public static String CCR_CLIENT_RESP_FILE_FIELD_NAME = "ResponseFromClient" ;
	final public static String CCR_SMB_SUBMITION_FILE_FIELD_NAME = Field.ATTACHMENTS ;
	
	final public static String CCREXT_PROJECT_FIELD_NAME = Field.CATEGORY ;	
	final public static String  CCREXT_PROJECT_NO_FIELD_NAME = Field.SEVERITY ;
	final public static String CCREXT_ASSIGNEE_FIELD_NAME = Field.ASSIGNEE ;
	final public static String CCREXT_PREPARED_BY_FIELD_NAME = Field.LOGGER ;
	final public static String  CCREXT_CORR_STATUS_FIELD_NAME = Field.STATUS ;
	final public static String CCREXT_CORR_TYPE_FIELD_NAME = Field.REQUEST_TYPE ;
	final public static String CCREXT_CC_FIELD_NAME = Field.CC ;
	final public static String CCREXT_SUBS_FIELD_NAME = Field.SUBSCRIBER ;
	final public static String CCREXT_CORR_NO_FIELD_NAME = "CorrespondenceNo" ;
	final public static String CCREXT_CLIENT_DEC_FIELD_NAME = "ClientDecision" ;
	final public static String CCREXT_CLIENT_RESP_FILE_FIELD_NAME = Field.ATTACHMENTS ;
	final public static String CCREXT_SMB_SUBMIT_FIELD_NAME = "SBMSubmissionFile" ;	
	
	final public static String CCREXT_CORR_STC = "SubmittedToClient" ;
//	final public static String CCREXT_CD_PENDING = "Pending" ;
	
	final public static String CCR_CCR_PEPMD = "Pending" ;
	final public static String CCR_CCR_CCR = "ClientContactRejected" ;
	final public static String CCR_CCR_EPMR = "EPMRejected" ;
	final public static String CCR_CCR_PCCD = "PendingClientContactDecision" ;
	final public static String CCR_CCR_PFR =  "PendingForRectification" ;
	final public static String CCR_CCR_RSR = "RevisedSubmissionRequired" ;
	final public static String CCR_CCR_STCPR = "SentToClientPendingResponse" ;
	final public static String CCR_CCR_DR = "DiscussionResolved" ;
	final public static String CCR_CCR_DRCR = "DiscussionResolvedClientRejected" ;
	
	final public static String CCR_CCD_PENDING = "Pending" ;
	final public static String CCR_CCD_REJECTED = "Rejected" ;
	final public static String CCR_CCD_RECTIFY = "Rectify";
	final public static String CCR_CCD_APPROVED = "Approved" ;
	
	final public static String CCR_EPMD_PENDING = "Pending" ;
	final public static String CCR_EPMD_REJECTED = "Rejected" ;
	final public static String CCR_EPMD_RECTIFY = "Rectify" ;
	final public static String CCR_EPMD_APPROVED = "Approved" ;	
		
	final public static String CCR_MAP_TYPE_NAME = "CCR_MAP" ; // EPM 
	final public static String CCR_CC_MAP_TYPE_NAME = "CC_MAP" ;// client
//	final public static String CCR_SMB_MAP_TYPE_NAME = "SMB_MAP" ; // smb coordinator
	
	final public static String RPT_PROJECT = "Project" ;
	final public static String RPT_PROJECTNO = "ProjectNo" ;
	final public static String RPT_LOCATION = "Location" ;
	final public static String RPT_CCRNO = "CcrNo" ;
	final public static String RPT_CONTRACT_DOCUMENT = "ContractDocument" ;
	final public static String RPT_SECTION = "Section" ;
	final public static String RPT_SUBJECT = "Subject" ;
	final public static String RPT_CONTRACT_REQUIREMENT = "ContractRequirement" ;
	final public static String RPT_CONTRACT_CLARIFICATION_REQUEST = "ContractClarificationRequest" ;
	final public static String RPT_PETROBRAS_RESPONSE = "PetrobrasResponse" ;
	final public static String RPT_PREPAREDBY = "PreparedBy" ;
	final public static String RPT_AUTHORIZEDBY = "AuthorisedBy" ;
	final public static String RPT_APPROVEDBY = "ApprovedBy" ;
	final public static String RPT_AUTHORISED_NAME = "AuthorisedName" ;
	final public static String RPT_AUTHORISED_DATE = "AuthorisedDate" ;
	final public static String RPT_APPROVED_NAME = "ApprovedName" ;
	final public static String RPT_APPROVED_DATE = "ApprovedDate" ;
	final public static String RPT_AUTHORIZED_BY_IMAGE = "AuthorisedByImage" ;
	final public static String RPT_APPROVED_BY_IMAGE = "ApprovedByImage" ;
	
	final public static String CCR_CCR_NO_PREFIX = "CCR-" ; // format CCR-PROJECTNO-YY-XXXX
	
	final public static String TBITS_ROOT = "root" ;
	final public static String CCREXT_CS_SUBMITTEDTOCLIENT = "SubmittedToClient" ;
	final public static String CCREXT_CS_REVISEDSUBMISSION = "RevisedSubmissionRequired" ;
	final public static String CCREXT_CS_CONCLUDED = "DiscussionResolved" ;
	
	final public static String CCREXT_CD_FIELD_NAME = "ClientDecision" ;
	final public static String CCREXT_CD_PENDING = "Pending" ;
	final public static String CCREXT_CD_REJECTED = "ClientRejected" ;
	final public static String CCREXT_CD_NOTAPPLICABLE = "NotApplicable";
	final public static String CCREXT_CD_APPROVED = "ClientApproved" ;
	public static final String CLIENT = "peter";
	
}
