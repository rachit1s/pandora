/**
 * 
 */
package ksk;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.eclipse.birt.report.engine.api.EngineException;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.dms.AttachmentUtils;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.TransmittalHandler;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.PluginManager;
import transbit.tbits.webapps.ReportUtil;
import transbit.tbits.webapps.WebUtil;

/**
 * @author lokesh
 * 
 * KSKTransmittalPlugin - Is responsible for creating the transmittal process. 
 * Transmittals can be created by SEPCO, PHO, CEC, DCPL.
 * SEPCO's Transmittal Types - Transmit For Approval, Transmit For RFC, Transmit As Built.
 * PHO's Transmittal Types - Transmit To CEC, Transmit to LCC, Transmit To Specialists.
 * CEC's Transmittal Types - Transmit To Site, Transmit To DCPL.
 * DCPL's Transmittal Types - Transmit TO SEPCO, Transmit RFC Validation, Transmit AB Validation.
 * 
 * 1. Status after transmittal process?
 * 2. Serial for each transmittal process or based on transmittal type?
 * 3. Various attachments involved in each transmittal type? 
 * 
 * 4. Transmittal process id.
 * 5. Transmittal type mapping with the process id. 
 * 
 * TODO: 1. Pick Approval Category & Type drop down options in selection table from the transmittal_process_parameters table.
 * TODO: 2. Handle mailing list mapping for transmittal number generation.
 */
public class KSKTransmittalPlugin implements TransmittalHandler {

	private static final String SELECT_MAX_TRANSMITTAL_ID_QUERY = "SELECT max_transmittal_id FROM ba_max_transmittal_ids" +
			" WHERE transmittal_process_name = ?";

	private static final String YYYY_MM_DD = "yyyy-MM-dd";

	private static final String ALTERNATE_DTN_LOGGER = "alternateDtnLogger";

	private static final String ORIGINATING_AGENCY_DISPLAY = "originatingAgencyDisplay";

	private static final String SEPCO_BILASPUR = "SEPCO_Bilaspur";

	private static final String SEPCO_DTN_PREFIX_S_02 = "S-02";

	private static final String CREATE_TRANSMITTAL = "createTransmittal";
	
	private static final String FIELD_DRAFTED_BY = "DraftedBy";

	private static final String STP_GET_MATCHED_REQUEST_ID = "stp_get_matched_request_id";

	private static final String STP_GET_REQUEST_ID_BY_DOC_NO = "stp_get_requestIdByDocNo";

	private static final String KSK_IRULE_OTHER_BA_LIST = "ksk.irule.other_ba_list";

	private static final String DTN_SUBJECT = "dtnSubject";

	private static final String DTN_LOGGER = "dtnLogger";

	private static final String DEFAULT_APPROVAL_CATEGORY = "defaultApprovalCategory";

	private static final String SUMMARY_LIST = "summaryList";

	private static final String INWARD_TRANSMITTAL_NO = "IncomingTransmittalNo";

	private static final String DTN_STATUS_TRANSMITTAL_COMPLETE = "TransmittalComplete";

	//Transmittal attachment selection
	private static final String SELECTION_FILE = "ksk-transmittal-wizard.htm";
	
	//Tag-replace strings in document selection window.
	private static final String TABLE_DATA = "tableData";
	private static final String OTHER_ATTACHMENT_FIELD = "otherAttachmentField";
	private static final String DELIVERABLE_FIELD = "deliverableField";
	private static final String EMAIL_BODY = "emailBody";
	private static final String TRANSMITTAL_SUBJECT = "transmittalSubject";
	private static final String DELIVERABLE_FIELD_ID = "deliverableFieldId";
	private static final String SUMMARY = "summary";
	private static final String SYS_PREFIX = "sysPrefix";
	
	//URL parameters
	private static final String REQUEST_TYPE = "requestType";	
	private static final String DCR_BA = "dcrBA";	
	private static final String TRANSMITTAL_TYPE = "trnType";
	
	//DTN category types
	private static final String SEPCO_HEAD_OFFICE = "SEPCOHeadOffice";
	private static final String WPCL_HEAD_OFFICE = "WPCLHeadOffice-01";
	private static final String WPCL_CEC = "WPCLChennaiEngineering-03";
	private static final String WPCL_DCPL = "WPCLDCPL";
	private static final String LLC = "WPCLLCC-05";
	private static final String PMC = "PMC06";
	private static final String THIRD_PARTY_INSPECTION = "ThirdPartyInspectionAuthority07";
	private static final String SEPCO_NEPDI = "SEPCONEPDI-08";
	private static final String PENDING = "pending";
	
	//Miscellaneous	
	private static final String SHEETS = "sheets";
	private static final String ATTACHMENTS = "attachments";
	private static final String DELIVERABLES = "deliverables";
	private static final String DESP = "desp";
	private static final String REV_NO = "rev_no";
	private static final String DOC_NO = "doc_no";	
	private static final Object TRANSMITTAL_APP_CATEGORY = "appCategory";
	private static final Object TRANSMITTAL_DOC_TYPE = "docType";

	private static final String KEYWORD_SUCCESS = "success";
	private static final String KEYWORD_VALUE = "value";
	private static final String KEYWORD_TRUE = "true";
	private static final String KEYWORD_FALSE = "false";
	private static final String NEAREST_PATH = "nearestPath";
	
	private static final String REQUEST_LIST = "requestList";
	private static final String ATTACHMENT_LIST = "attachmentList";
	private static final String DRAWINGS_LIST = "docList";
	private static final String REVISION_LIST = "revList";
	private static final String COPIES_LIST = "copiesList";
	private static final String CAT_LIST = "catList";
	private static final String TYPE_LIST = "typeList";
	
	private static final String REMARKS = "remarks";
	private static final String TO_LIST = "toList";
	private static final String CC_LIST = "ccList";
	
	private static final String DELIVERABLES_LIST = "deliverablesList";	
	private static final String TRANSMITTAL_PROCESS_NAME = "transmittalProcessName";
	
	private static final String MAPPED_BUSINESS_AREAS = "mappedBusinessAreas";
	
	private static final String APP_CATEGORIES = "[{name:\"PL - Preliminary issue\" , value:\"PL\"},{name:\"IN - For Information\" , value:\"IN\"}," +
												  "{name:\"FA - For Apporval\", value:\"FA\"},{name:\"AP - Approved Drg./Doc\" , value:\"AP\"}," +
												  "{name:\"RC - Released For Construction\" , value:\"RC\"},{name:\"AB - As Built Drawing\" , value:\"AB\"}]";
	
	private static final String DOC_TYPES = "[{name:\"CD - Compact Disk\" , value:\"CD\"},{name:\"SC - Soft Copy by e-Mail\" , value:\"SC\"}," +
			"									{name:\"RT - Reproducible Tracing\" , value:\"RT\"},{name:\"PR - Paper Prints\" , value:\"PR\"}]";
	
	
	static final String approvalCategory = "CD - Compact Disk, SC - Soft Copy by e-Mail, RT - Reproducible Tracing, PR - Paper Prints";
	static final String documentList = "PL - Preliminary issue,IN - For Information,FA - For Apporval,AP - Approved Drg./Doc,RC - Released For Construction,AB - As Built Drawing";
	//Column names of db tables
	private static final String TARGET_SYS_ID = "target_sys_id";
	
	//Field names in transmittal business area
	private static final String DTN_FILE = "DTNFile";
	private static final String COMMENTED_FILES = "CommentedFiles";
	private static final String DTN_NUMBER = "DTNNumber";
	
	//Servlet request types
	private static final String SELECTION = "selection";
	private static final String PREVIEW = "preview";
	
	User user = null;
	
	//Parameters required by the transmittal process.
	String[] reqFileNames;
	private int transReqId;
	private String[] deliverableFileNames;
	private int deliverableFieldId;
	
	//Final result holder.
	JSONObject result = new JSONObject();
	
	//LOG to capture logs in webapps package.
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	
	/* (non-Javadoc)
	 * @see transbit.tbits.domain.TransmittalHandler#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {
		/*
		 * Transmittal id represents transmittal process as an abstraction. 
		 * Why is this abstraction needed? To have a continuous transmittal id for a particular process. 
		 * Different transmittal type from different business areas can have the same transmittal process.
		 * 
		 * 1. On transmittal button click open the transmittal window based on the type of selection. 
		 * 2. Based on the type of transmittal 
		 * 
		 */		
		try {	
			handleGetRequest(aRequest, aResponse);
		}catch (DatabaseException e1) {
			LOG.error("Error while creating transmittal note:\n" + e1.getDescription());
			e1.printStackTrace();
		}catch (TBitsException e2) {
			LOG.error("Error occurred while generating transmittal note:\n" + e2.getDescription());
			e2.printStackTrace();
		} 
	}
	
	
	/* (non-Javadoc)
	 * @see transbit.tbits.domain.TransmittalHandler#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */ 
	public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {
		String requestType = aRequest.getParameter(REQUEST_TYPE);
		requestType = requestType.trim();
		try{
		if (requestType.equalsIgnoreCase(PREVIEW))
			handlePreviewPostRequest(aRequest, aResponse);
		else if (requestType.equalsIgnoreCase(CREATE_TRANSMITTAL))
			handlePostRequest(aRequest, aResponse);
		}catch (DatabaseException e1) {
			LOG.error("Error while previewing transmittal note:\n" + e1.getDescription());
			e1.printStackTrace();
		}catch (TBitsException e2) {
			LOG.error("Error occurred while previewing transmittal note:\n" + e2.getDescription());
			e2.printStackTrace();
		} 
	}

	
	public void handleGetRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) 
									throws ServletException, IOException, DatabaseException, NumberFormatException, TBitsException {
		
		String requestType = aRequest.getParameter(REQUEST_TYPE);
		requestType = requestType.trim();
		
		if (requestType.equalsIgnoreCase(SELECTION)){
			aResponse.setContentType("text/html");
			//PrintWriter out = aResponse.getWriter();
			
			String dcrSysPrefix = aRequest.getParameter(DCR_BA);
			if ((dcrSysPrefix == null) || (dcrSysPrefix.trim().equals(KSKUtils.EMPTY_STRING) == true)) {
				aResponse.getWriter().println("Invalid Business Area.");
				return;
			} else {
				dcrSysPrefix = dcrSysPrefix.trim();
			}

			BusinessArea dcrBA = BusinessArea.lookupBySystemPrefix(dcrSysPrefix);		

			if (dcrBA == null) {
				aResponse.getWriter().println("Invalid Business Area or does not exist with the system prefix: " + dcrSysPrefix);
				return;
			}
			int dcrSystemId = dcrBA.getSystemId();		
			
			String requestList = aRequest.getParameter(REQUEST_LIST);
			if ((requestList == null) || requestList.trim().equals(KSKUtils.EMPTY_STRING)) {
				aResponse.getWriter().println("No drawing/document(s) selected for transmittal.");
				return;
			}
			requestList = requestList.trim();
			
			//Create unique requests list.
			String[] requests = requestList.split(",");				
			if ((requests != null) && (requests.length >1)){
				HashSet<String > requestSet = new HashSet<String>(Arrays.asList(requests));
				requests = (String[])requestSet.toArray(new String[requestSet.size()]);
				requestList = requests[0];
				for (int i = 1; i < requests.length; i++)
			    {
					requestList += "," + requests[i];
			    }				
			}
						
			String trnType = aRequest.getParameter(TRANSMITTAL_TYPE);
			if((trnType == null) || (trnType.trim().equals(""))){
				aResponse.getWriter().println("Select appropriate Transmittal type. '" + trnType + "' is invalid.");
				return;
			}
			else{
				trnType = trnType.trim();
			}		
			
			DTagReplacer prepareTransmittal = null;
			File selectionFile = KSKUtils.getResourceFile(SELECTION_FILE);
			prepareTransmittal = new DTagReplacer(selectionFile);
			prepareTransmittal.replace(SYS_PREFIX, dcrSysPrefix);
			prepareTransmittal.replace(REQUEST_LIST, requestList);
			prepareTransmittal.replace(NEAREST_PATH, WebUtil.getNearestPath(aRequest, KSKUtils.EMPTY_STRING));
			prepareTransmittal.replace("usersList", ReportUtil.getJSONArrayOfUsers().toString());
			
			KSKTransmittalType transmittalType;
			Hashtable<String, String> tpParams;
			try {
				transmittalType = KSKTransmittalType.lookupTransmittalTypeBySystemIdAndName(dcrSystemId, trnType);
				tpParams = KSKTransmittalType.getTransmittalProcessParameters(dcrSystemId, transmittalType.getTransmittalTypeId());
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DatabaseException("Database error occurred while retrieving transmittal type.", e);
			}			
			
			prepareTransmittal.replace(TRANSMITTAL_TYPE, trnType);
			
			if ((tpParams != null) && !tpParams.isEmpty()){
				//Check if a value is found in the hashtable loaded from db. If no value is found set value to "".
				prepareTransmittal.replace(TO_LIST, (tpParams.get(TO_LIST)== null) ? "" : tpParams.get(TO_LIST));
				prepareTransmittal.replace(CC_LIST, (tpParams.get(CC_LIST) == null)? "" : tpParams.get(CC_LIST));
				prepareTransmittal.replace(EMAIL_BODY, (tpParams.get(EMAIL_BODY) == null)? "" : tpParams.get(EMAIL_BODY));
				prepareTransmittal.replace(REMARKS, (tpParams.get(REMARKS) == null)? "" : tpParams.get(REMARKS));
				prepareTransmittal.replace(TRANSMITTAL_SUBJECT, (tpParams.get(TRANSMITTAL_SUBJECT) == null)? "" : tpParams.get(TRANSMITTAL_SUBJECT));
				prepareTransmittal.replace(CAT_LIST, (tpParams.get("approvalCategories") == null) ?"[]" : tpParams.get("approvalCategories"));
				prepareTransmittal.replace(TYPE_LIST, (tpParams.get("documentTypes") == null) ? "[]" : tpParams.get("documentTypes"));
				prepareTransmittal.replace("transmittalPrefix", (tpParams.get(KSKUtils.TRANSMITTAL_ID_PREFIX) == null) 
																		? "" : tpParams.get(KSKUtils.TRANSMITTAL_ID_PREFIX));
			}
			else{
				prepareTransmittal.replace(TO_LIST, "");
				prepareTransmittal.replace(CC_LIST, "");
				prepareTransmittal.replace(EMAIL_BODY, "");
				prepareTransmittal.replace(REMARKS, "");
				prepareTransmittal.replace(TRANSMITTAL_SUBJECT, "");
				prepareTransmittal.replace(CAT_LIST, APP_CATEGORIES);
				prepareTransmittal.replace(TYPE_LIST, DOC_TYPES);
			}
			
			String actualDateProperty = "";
			String actualDateUsers = "";
			try{				
				actualDateProperty = PropertiesHandler.getProperty("transbit.tbits.transmittal.isActualDateEnabled");
				actualDateUsers = PropertiesHandler.getProperty("transbit.tbits.transmittal.actualDateUsers");
				
				user = WebUtil.validateUser(aRequest);
				
				if((actualDateProperty != null) && (actualDateUsers != null) 
						&& Boolean.parseBoolean(actualDateProperty)
						&& KSKUtils.isExistsInString(actualDateUsers, user.getUserLogin())){
					prepareTransmittal.replace("actualDateDisplay", "inline");
					prepareTransmittal.replace("actualDateImageDisplay", "inline");
				}
				else{				
					prepareTransmittal.replace("actualDateDisplay", "none");
					prepareTransmittal.replace("actualDateImageDisplay", "none");
				}
			}catch(IllegalArgumentException iae){
				if (actualDateProperty == null)
					LOG.warn("Property \"transbit.tbits.transmittal.isActualDateEnabled\", " +
							"hence ignoring that property and setting actual date field display to none.");
				if (actualDateUsers == null)
					LOG.warn("Property \"transbit.tbits.transmittal.actualDateUsers\"," +
							" hence ignoring that property and setting actual date field display to none.");
				prepareTransmittal.replace("actualDateDisplay", "none");
				prepareTransmittal.replace("actualDateImageDisplay", "none");
			}
			
			if (KSKUtils.isSEPCODCR(dcrBA))
				prepareTransmittal.replace(ORIGINATING_AGENCY_DISPLAY, "inline");
			else
				prepareTransmittal.replace(ORIGINATING_AGENCY_DISPLAY, "none");	
			
			
			prepareTransmittal.replace("draftedBy", "");
			
			String tableData = getTableData(dcrSystemId, requestList, tpParams);
			
			if (tableData == null){
				aResponse.getWriter().println ("Could not retrieve the documents selected for transmittal.");
				return;
			}			
			prepareTransmittal.replace(TABLE_DATA, tableData);
			
			
			int deliverableAttFieldId = Integer.parseInt(tpParams.get(DELIVERABLE_FIELD_ID));
			if (deliverableAttFieldId != 0){
				Field deliverableField = Field.lookupBySystemIdAndFieldId(dcrSystemId, deliverableAttFieldId);
				prepareTransmittal.replace(DELIVERABLE_FIELD, deliverableField.getDisplayName());
			}
			else{
				prepareTransmittal.replace(DELIVERABLE_FIELD, "Deliverable Attachments");
			}

			Field otherAttachmentsField = Field.lookupBySystemIdAndFieldName(dcrSystemId, Field.ATTACHMENTS);
			prepareTransmittal.replace(OTHER_ATTACHMENT_FIELD, otherAttachmentsField.getDisplayName());
			prepareTransmittal.replace(TRANSMITTAL_PROCESS_NAME, transmittalType.getTransmittalProcess());
			prepareTransmittal.replace(DELIVERABLE_FIELD_ID, deliverableAttFieldId + "");
			
			aResponse.getWriter().print(prepareTransmittal.parse(dcrSystemId));
			return;
		}		
	}
	
	public void handlePreviewPostRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) 
		throws ServletException, IOException, DatabaseException, NumberFormatException, TBitsException {
		//if (requestType.equalsIgnoreCase(PREVIEW)){	
		aResponse.setCharacterEncoding("UTF-8");
		aResponse.setContentType("text/html");
		try {
			user = WebUtil.validateUser(aRequest);
		} catch (TBitsException e2) {
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "TBitsException: \n" + "User validation unssuccessful");
			aResponse.getWriter().print(result.toString());
			return;
		}

		ByteArrayOutputStream htmlOS;
		String dcrSysPrefix = aRequest.getParameter(DCR_BA);
		if ((dcrSysPrefix == null) || (dcrSysPrefix.trim().equals(KSKUtils.EMPTY_STRING) == true)) {
			aResponse.getWriter().println("Invalid Business Area.");
			return;
		} else {
			dcrSysPrefix = dcrSysPrefix.trim();
		}

		BusinessArea dcrBA = BusinessArea.lookupBySystemPrefix(dcrSysPrefix);		

		if (dcrBA == null) {
			aResponse.getWriter().println("Invalid Business Area or does not exist with the system prefix: " + dcrSysPrefix);
			return;
		}
		int dcrSystemId = dcrBA.getSystemId();

		String trnType = aRequest.getParameter(TRANSMITTAL_TYPE + 1);
		if((trnType == null) || (trnType.trim().equals(""))){
			aResponse.getWriter().println("Select appropriate Transmittal type. '" + trnType + "' is invalid.");
			return;
		}
		else{
			trnType = trnType.trim();
		}

		KSKTransmittalType transmittalType;
		Hashtable<String, String> tpParams;
		try {
			transmittalType = KSKTransmittalType.lookupTransmittalTypeBySystemIdAndName(dcrSystemId, trnType);
			tpParams = KSKTransmittalType.getTransmittalProcessParameters(dcrSystemId, transmittalType.getTransmittalTypeId());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Database error occurred while retrieving transmittal type.", e);
		}

		String requestList = aRequest.getParameter(REQUEST_LIST + 1);
		if ((requestList == null) || requestList.trim().equals(KSKUtils.EMPTY_STRING)) {
			aResponse.getWriter().println("Empty Requests List.");
			return;
		}
		requestList = requestList.trim();
		
		String revList = aRequest.getParameter(REVISION_LIST + 1);
		if ((requestList == null) || requestList.trim().equals(KSKUtils.EMPTY_STRING)) {
			aResponse.getWriter().println("Empty Revisions List.");
			return;
		}
		requestList = requestList.trim();

		String approvalCategoryList = aRequest.getParameter(CAT_LIST + 1);
		if((approvalCategoryList == null) || approvalCategoryList.trim().equals(KSKUtils.EMPTY_STRING)){
			aResponse.getWriter().println("Empty approval categories List.");
			return;
		}
		approvalCategoryList = approvalCategoryList.trim();

		String typeList = aRequest.getParameter(TYPE_LIST + 1);
		if((typeList == null) || typeList.trim().equals(KSKUtils.EMPTY_STRING)){
			aResponse.getWriter().println("Empty document types List.");
			return;
		}
		typeList = typeList.trim();

		String quantityList = aRequest.getParameter (COPIES_LIST + 1);
		if ((quantityList == null) || quantityList.trim().equals(KSKUtils.EMPTY_STRING)) {
			aResponse.getWriter().print("Please enter the quantity.");
			return;
		}			
		quantityList = quantityList.trim();
		
		String originatingAgency = "";
		if (KSKUtils.isSEPCODCR(dcrBA)){
			originatingAgency = aRequest.getParameter("originatingAgency1");
			if ((originatingAgency == null) || (originatingAgency.trim().equals("KSKUtils.EMPTY_STRING"))){				
				aResponse.getWriter().println("Please select the originating agency.");
				return;
			}	
			else{
				originatingAgency = originatingAgency.trim();
			}
		}	

		String toList = aRequest.getParameter (TO_LIST + 1);
		if ((toList == null) || toList.trim().equals(KSKUtils.EMPTY_STRING)){
			LOG.info("Distribution List was empty, hence ignoring it.");				
			toList = "";
		}
		toList = toList.trim();

		String ccList = aRequest.getParameter (CC_LIST + 1);
		if ((ccList == null) || ccList.trim().equals(KSKUtils.EMPTY_STRING)){
			LOG.info("Distribution List was empty, hence ignoring it.");				
			ccList = "";
		}
		ccList = ccList.trim();

		String dtnSubject = aRequest.getParameter (DTN_SUBJECT + 1);
		if ((dtnSubject == null) || dtnSubject.trim().equals(KSKUtils.EMPTY_STRING)){	
			dtnSubject = "";
		}
		dtnSubject = dtnSubject.trim();

		String remarks = aRequest.getParameter (REMARKS + 1);
		if ((remarks == null) || remarks.trim().equals(KSKUtils.EMPTY_STRING)){	
			remarks = "";
		}
		remarks = remarks.trim();

		String summaryList = aRequest.getParameter (SUMMARY_LIST + 1);
		summaryList = summaryList.trim();

		String transmittalDate = aRequest.getParameter("ActualDate" + 1);
		if ((transmittalDate == null) || transmittalDate.trim().equals("")){								
			transmittalDate = Timestamp.getGMTNow().toCustomFormat(YYYY_MM_DD);//"dd-MMM-yyyy");
		}
		
		String preDefinedDTNNumber = aRequest.getParameter("preDefinedDTNNumber" + 1);
		if ((preDefinedDTNNumber == null) || preDefinedDTNNumber.trim().equals(""))
			preDefinedDTNNumber = "";
		
		String draftedBy = aRequest.getParameter("draftedBy" + 1);
		if (draftedBy == null){
			LOG.info("\"Drafted By\" was not provided. Hence leaving it empty.");
			draftedBy = "-";
		}
		else 
			draftedBy = draftedBy.trim();

		String trnIdPrefix = tpParams.get(KSKUtils.TRANSMITTAL_ID_PREFIX);
		String dtnLoggerName = tpParams.get(DTN_LOGGER); 
		if (KSKUtils.isSEPCODCR(dcrBA) && (originatingAgency.equals(SEPCO_BILASPUR))){
			trnIdPrefix = SEPCO_DTN_PREFIX_S_02 + "-";
			dtnLoggerName = tpParams.get(ALTERNATE_DTN_LOGGER);
		}
		User dtnLogger = User.lookupByUserLogin(dtnLoggerName);			
		if (dtnLogger == null)
			dtnLogger = user;
		
		String trnTemplateName = tpParams.get("transmittal_template_name");		

		int preDefinedMaxId = 0;
		int transmittalMaxId = 0;
		Connection connection = null;
		if (!preDefinedDTNNumber.equals("")){
			//int lIndex = preDefinedDTNNumber.lastIndexOf("-");
			//String dtnNumberStr = preDefinedDTNNumber.substring(lIndex + 1);
			//if ((dtnNumberStr != null) && (!dtnNumberStr.trim().equals(""))){
			preDefinedMaxId = Integer.parseInt(preDefinedDTNNumber);
			//}
		}
		else
			preDefinedMaxId = 0;
		
		try {			
			connection = DataSourcePool.getConnection();
			String trnProcessSerialKey = getTransmittalProcessNameBasedOnOriginatingAgency(dcrBA, originatingAgency, 
					transmittalType.getTransmittalProcess());			
			
			/*if (KSKUtils.isSEPCODCR(dcrBA) && originatingAgency.equals(SEPCO_BILASPUR))
				trnProcessSerialKey = SEPCO_DTN_PREFIX_S_02;//ps.setString(1, SEPCO_DTN_PREFIX_S_02);
			else
				trnProcessSerialKey = transmittalType.getTransmittalProcess();//ps.setString(1, transmittalType.getTransmittalProcess());
			 */			
			//String queryString = SELECT_MAX_TRANSMITTAL_ID_QUERY;
			transmittalMaxId = getMaxTransmittalIdWithoutIncrement(connection, trnProcessSerialKey);
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new DatabaseException("Database error occurred while retrieving the max_id for preview.", e1);
		} catch (NumberFormatException nfe){
			throw new TBitsException("Illegal transmittal number.", nfe);
		} finally{
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new DatabaseException("Error occurred while closing database connection.", e);
				}
		}
		
		if (preDefinedMaxId > 0){
			
			//Replace the following statement with the commented block to avoid duplicate transmittal number.
			transmittalMaxId = preDefinedMaxId;
			/*
			int idDiff = transmittalMaxId - preDefinedMaxId;
			if(idDiff == 0){
				aResponse.getWriter().println("A DTN with the same number: " 
						+ trnIdPrefix + KSKUtils.getFormattedStringFromNumber(preDefinedMaxId) 
						+ ", already exists in the system. " 
						+ "Please provide a different transmittal number.");
				return;
			}			
			else if (idDiff<0){
				transmittalMaxId = preDefinedMaxId;
			}
			else if (idDiff>0){
				String tempDTNNumber = trnIdPrefix + KSKUtils.getFormattedStringFromNumber(preDefinedMaxId);
				try {
					boolean isExists = isDTNNumberExists(transmittalType.getDtnSysId(), tempDTNNumber);
					if (isExists){
						aResponse.getWriter().println("A DTN with the same number: " 
								+ tempDTNNumber + ", already exists in the system. " 
								+ "Please provide a different transmittal number.");
						return;
					}
					else
						transmittalMaxId = preDefinedMaxId;
				} catch (SQLException e) {
					e.printStackTrace();
					throw new DatabaseException("Error occurred while checking for existing DTN number.", e);
				}				
			}
		*/}
		else
			transmittalMaxId = transmittalMaxId + 1;

		String formattedTrnId = KSKUtils.getFormattedStringFromNumber(transmittalMaxId);

		//int dcrSystemId, String requestList, String catList, String typeList, String qtyList, 
		//String transmittalId, String transmittalSubject, User user, String inwardDTNFieldName
		KskTemplateHelper kth = KSKUtils.getKskTemplateHelper(dcrSystemId, transmittalType.getDtnSysId(),
				requestList, revList, approvalCategoryList, typeList, quantityList, summaryList, toList, ccList, 
				trnIdPrefix + formattedTrnId + " [Likely]", dtnSubject, remarks, dtnLogger, INWARD_TRANSMITTAL_NO,
				tpParams, transmittalDate, draftedBy);		

		try {                                                
			htmlOS = KSKUtils.generateTransmittalNoteInHtml(aRequest, trnTemplateName, kth, aRequest.getContextPath());
			if (htmlOS != null)
				aResponse.getWriter().println(htmlOS);
			else
				aResponse.getWriter().println("Could not generate the preview.");				
		} catch (EngineException e) {
			e.printStackTrace();
			aResponse.getWriter().println("Error occurred while generating preview of transmittal.\n" + e.getMessage() 
					+ "Close the transmittal window and contact tBits team for further assistance.");
			return;
		}
		//}
	}
	
	private boolean isDTNNumberExists(int dtnSysId, String dtnNumber)
			throws SQLException{
		boolean isExists = false;
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			isExists = isDTNNumberExists(connection, dtnSysId, dtnNumber);
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}finally{
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				LOG.warn("Error occurred while checking for existance of dtnNumber: " + dtnNumber);
			}
		}
		return isExists;
	}


	private boolean isDTNNumberExists(Connection connection, int dtnSysId,
			String dtnNumber) throws SQLException {
		boolean isExists = false;
		if (dtnNumber != null){	
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM requests_ex WHERE sys_id=? and varchar_value=?");
			ps.setInt(1, dtnSysId);
			ps.setString(2, dtnNumber);
			ResultSet rs = ps.executeQuery();				
			if (rs.next()){
				String fetchedNumber = rs.getString("varchar_value");
				if (dtnNumber.trim().equals(fetchedNumber))
					return true;
			}
			rs.close();
			ps.close();
		}
		return isExists;
	}


	/**
	 * @param connection
	 * @param trnProcessName
	 * @throws SQLException
	 */
	private int getMaxTransmittalIdWithoutIncrement(Connection connection,
			String trnProcessName) throws SQLException {
		int transmittalMaxId = 0;
		PreparedStatement ps = connection.prepareStatement(SELECT_MAX_TRANSMITTAL_ID_QUERY);
		ps.setString(1, trnProcessName);
		ResultSet rs = ps.executeQuery();				
		if (rs.next()){
			transmittalMaxId = rs.getInt("max_transmittal_id");
		}
		rs.close();
		ps.close();
		return transmittalMaxId;
	}
	 
	
	/**
	 * This method handles the transmittal creation and updating of respective DCR and DTR processes.
	 * 1. Give unique transmittal number for each transmittal type in each BA DCR.
	 * 2. Generation of transmittal note.
	 * 3. Add to proper attachment fields in DTN as well as DTR & DCRs.
	 * 4. Create appropriate user for the process.
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */

	public void handlePostRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		//HttpSession aSession = aRequest.getSession(true);
		Connection connection = null;
		Connection maxIdConn = null;		
		//int transReqId = 0;
		
		TBitsResourceManager tBitsResMgr = new TBitsResourceManager();
		PrintWriter out = aResponse.getWriter(); 
		try {			
			aResponse.setContentType("text/plain");	
			try {
				user = WebUtil.validateUser(aRequest);
			} catch (TBitsException e2) {
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, "TBitsException: \n" + "User validation unssuccessful");
				out.print(result.toString());
				return;
			}
			
			String dcrSysPrefix = aRequest.getParameter(DCR_BA);

			if ((dcrSysPrefix == null) || (dcrSysPrefix.trim().equals(KSKUtils.EMPTY_STRING))) {
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, "Invalid Business Area.");
				out.print(result.toString());
				return;
			} else {
				dcrSysPrefix = dcrSysPrefix.trim();
			}
				 
			BusinessArea dcrBA = BusinessArea.lookupBySystemPrefix(dcrSysPrefix);
			if (dcrBA == null) {
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, dcrSysPrefix + ": Invalid Business Area or business area does not exist");
				out.print(result.toString());
				return;
			}
			
			String requestList = aRequest.getParameter(REQUEST_LIST);

			if ((requestList == null) || requestList.trim().equals(KSKUtils.EMPTY_STRING)) {
				out.print(KEYWORD_FALSE + KSKUtils.DELIMETER_COMMA +"No document selected to be sent to transmittal. Please select the documents to be sent");
				return;
			}
			requestList = requestList.trim();
			String[] dcrRequestList = requestList.split(KSKUtils.DELIMETER_COMMA);
			
			String docList = aRequest.getParameter (DRAWINGS_LIST);
			if ((docList == null) || docList.trim().equals(KSKUtils.EMPTY_STRING)) {
				out.print("Please select documents to be sent to transmittal");
				return;
			}			
			docList = docList.trim();
						
			String revList = aRequest.getParameter (REVISION_LIST);
			if ((revList == null) || revList.trim().equals(KSKUtils.EMPTY_STRING)) {
				out.print("Revision numbers list not provided. Please enter proper revision numbers");
				return;
			}			
			revList = revList.trim();
								
			String quantityList = aRequest.getParameter (COPIES_LIST);
			if ((quantityList == null) || quantityList.trim().equals(KSKUtils.EMPTY_STRING)) {
				out.print("Please enter the quantity.");
				return;
			}			
			quantityList = quantityList.trim();
			
			String approvalCategoryList = aRequest.getParameter (CAT_LIST);
			if ((approvalCategoryList == null) || approvalCategoryList.trim().equals(KSKUtils.EMPTY_STRING)) {
				out.print("No actions found for the selected documents. Please select appropriate actions");
				return;
			}			
			approvalCategoryList = approvalCategoryList.trim();
			
			String documentTypeList = aRequest.getParameter (TYPE_LIST);
			if ((documentTypeList == null) || documentTypeList.trim().equals(KSKUtils.EMPTY_STRING)) {
				out.print("Please enter the quantity.");
				return;
			}			
			documentTypeList = documentTypeList.trim();
			
			String summaryList = aRequest.getParameter (SUMMARY_LIST);
			summaryList = summaryList.trim();
			
			String deliverableList = aRequest.getParameter(DELIVERABLES_LIST);
			if ((deliverableList == null) || deliverableList.trim().equals(KSKUtils.EMPTY_STRING)) {
				LOG.warn("No deliverable attachments selected.");
				deliverableList = KSKUtils.EMPTY_STRING;
			}
			else{
				//Removed trimming, as this operation would remove any trailing comma along with whitespaces, 
				//which represents no attachment selected for the last request.
				//deliverableList = deliverableList.trim();
				deliverableFileNames = deliverableList.split("<br2>");//KSKUtils.DELIMETER_COMMA);
			}
			
			String attachmentList = aRequest.getParameter(ATTACHMENT_LIST);
			if ((attachmentList == null) || attachmentList.trim().equals(KSKUtils.EMPTY_STRING)) {
				LOG.warn("No attachments selected.");
				attachmentList = KSKUtils.EMPTY_STRING;
			}
			else{
				//Same problem as with the above deliverable list.
				//attachmentList = attachmentList.trim();
				reqFileNames = attachmentList.split("<br2>");//KSKUtils.DELIMETER_COMMA);
			}
			
			String originatingAgency = "";
			if (KSKUtils.isSEPCODCR(dcrBA)){
				originatingAgency = aRequest.getParameter("originatingAgency");
				if ((originatingAgency == null) || (originatingAgency.trim().equals("KSKUtils.EMPTY_STRING"))){
					out.print("Please choose the originating agency.");
					return;
				}
				else{
					originatingAgency = originatingAgency.trim();
				}
			}
			
			String toList = aRequest.getParameter(TO_LIST);
			if ((toList == null)||(toList.trim().equals(KSKUtils.EMPTY_STRING))){
				System.out.println ("No mailing address provided");
			}
			toList = toList.trim();
			
			String ccList = aRequest.getParameter(CC_LIST);
			if ((ccList ==null) || (ccList.trim().equals(KSKUtils.EMPTY_STRING))){
				ccList = KSKUtils.EMPTY_STRING;
			}
			else{
				ccList = ccList.trim();
			}
			
			String transmittalDate = aRequest.getParameter("ActualDate");
			if((transmittalDate == null) || transmittalDate.trim().equals("")){
				transmittalDate = "";				
			}
			transmittalDate = transmittalDate.trim();
			
			String preDefinedDTNNumber = aRequest.getParameter("preDefinedDTNNumber");
			if ((preDefinedDTNNumber == null) || preDefinedDTNNumber.trim().equals(""))
				preDefinedDTNNumber = "";
			else 
				preDefinedDTNNumber = preDefinedDTNNumber.trim();
			
			String remarks  = aRequest.getParameter(REMARKS);
			if ((remarks ==null) || (remarks.trim().equals(KSKUtils.EMPTY_STRING))){
				remarks = KSKUtils.EMPTY_STRING;			
			}
			else{
				remarks = remarks.trim();
			}
			
			String transmittalSubject = aRequest.getParameter(TRANSMITTAL_SUBJECT);
			if ((transmittalSubject == null) || (transmittalSubject.trim().equals(KSKUtils.EMPTY_STRING))){
				transmittalSubject = KSKUtils.EMPTY_STRING;		
				LOG.info("No email body found for transmittal process");
			}
			else{
				transmittalSubject = transmittalSubject.trim();
			}
			
			String emailBody = aRequest.getParameter(EMAIL_BODY);
			if ((emailBody == null) || (emailBody.trim().equals(KSKUtils.EMPTY_STRING))){
				emailBody = KSKUtils.EMPTY_STRING;		
				LOG.info("No email body found for transmittal process");
			}
			else{
				emailBody = emailBody.trim();
			}
						
			String draftedBy = aRequest.getParameter("draftedBy");
			if (draftedBy == null){
				LOG.info("\"Drafted By\" was not provided.");
				draftedBy = "-";
			}
			else 
				draftedBy = draftedBy.trim();
			
			String transmittalProcessSerialKey = aRequest.getParameter(TRANSMITTAL_PROCESS_NAME);			
			if ((transmittalProcessSerialKey == null) || (transmittalProcessSerialKey.trim().equals(KSKUtils.EMPTY_STRING))){
				LOG.fatal("Could not find corresponding transmittal process key. Please contact admin/tBits support team.");
			}
			else
				transmittalProcessSerialKey = transmittalProcessSerialKey.trim();
			
			String trnTypeName = aRequest.getParameter(TRANSMITTAL_TYPE);
			if((trnTypeName == null) || (trnTypeName.trim().equals(""))){
				aResponse.getWriter().println("Select appropriate Transmittal type. '" + trnTypeName + "' is invalid.");
				return;
			}
			else{
				trnTypeName = trnTypeName.trim();
			}
						
			String deliverableFieldIdStr = aRequest.getParameter("deliverableFieldId");
			if ((deliverableFieldIdStr == null) || (deliverableFieldIdStr.trim().equals("")))
				throw new TBitsException("Could not find deliverable field type.");
			else 
				deliverableFieldIdStr = deliverableFieldIdStr.trim();
			deliverableFieldId = Integer.parseInt(deliverableFieldIdStr);
						
			KSKTransmittalType transmittalType;
			Hashtable<String, String> tpParams;
			try {
				transmittalType = KSKTransmittalType.lookupTransmittalTypeBySystemIdAndName(dcrBA.getSystemId(), trnTypeName);
				tpParams = KSKTransmittalType.getTransmittalProcessParameters(dcrBA.getSystemId(), transmittalType.getTransmittalTypeId());
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DatabaseException("Database error occurred while retrieving transmittal type.", e);
			}
			
			int trnSystemId = transmittalType.getDtnSysId();
			BusinessArea transBA = BusinessArea.lookupBySystemId(trnSystemId);		

			if (transBA == null) {
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, trnSystemId + ": Invalid transmittal Business Area Id or business area does not exist");
				out.print(result.toString());				
				return;
			}
			
			String formattedTrnReqId = KSKUtils.EMPTY_STRING;
			String transmitAttachments = KSKUtils.EMPTY_STRING;	
			String dtnLoggerName = tpParams.get(DTN_LOGGER);
			String trnIdPrefix = tpParams.get(KSKUtils.TRANSMITTAL_ID_PREFIX);
			if (KSKUtils.isSEPCODCR(dcrBA) && (originatingAgency.equals(SEPCO_BILASPUR))){
				trnIdPrefix = SEPCO_DTN_PREFIX_S_02 + "-";
				dtnLoggerName = tpParams.get(ALTERNATE_DTN_LOGGER);
			}
			
			User dtnLogger = User.lookupByUserLogin(dtnLoggerName);
			
			transmittalProcessSerialKey = getTransmittalProcessNameBasedOnOriginatingAgency(dcrBA, originatingAgency.trim(), 
					transmittalProcessSerialKey);
			
			String linkedRequests = KSKUtils.getLinkedRequests(dcrBA, dcrRequestList);
			Hashtable <String,String> aParamTable = new Hashtable<String, String>();
			try {
				maxIdConn = DataSourcePool.getConnection();
				maxIdConn.setAutoCommit(false);
				LOG.info("#### After connection for max Id.");
				
				/*if (KSKUtils.isSEPCODCR(dcrBA) && originatingAgency.equals(SEPCO_BILASPUR))
					transReqId = KSKTransmittalType.getMaxTransmittalNumber(maxIdConn, transBA.getSystemId(), SEPCO_DTN_PREFIX_S_02);
				else
					transReqId = KSKTransmittalType.getMaxTransmittalNumber(maxIdConn, transBA.getSystemId(), transmittalProcess);*/
				
				int preDefinedTrnId = 0;
				//If number is pre-defined by the user and is greater than the current max id, set this pre-defined value as
				//the current max id, else increment the max id.
				if ((preDefinedDTNNumber != null) && (!preDefinedDTNNumber.equals(""))){
					preDefinedTrnId = Integer.parseInt(preDefinedDTNNumber);
					if (preDefinedTrnId > 0){
						transReqId = preDefinedTrnId;						
						int currentMaxId = getMaxTransmittalIdWithoutIncrement(maxIdConn, transmittalProcessSerialKey);
						//if pre-defined max id is greater than the current max id then only increment.
						if (preDefinedTrnId > currentMaxId)
							setMaxTransmittalIdToCurrentPreDefinedMaxId(maxIdConn, transmittalProcessSerialKey, preDefinedTrnId);
					}
				}
				else{
					/*if (KSKUtils.isSEPCODCR(dcrBA) && originatingAgency.equals(SEPCO_BILASPUR))
						transReqId = KSKTransmittalType.getMaxTransmittalNumber(maxIdConn, transBA.getSystemId(),
								SEPCO_DTN_PREFIX_S_02);
					else*/
					transReqId = KSKTransmittalType.getMaxTransmittalNumber(maxIdConn, transBA.getSystemId(),
							transmittalProcessSerialKey);
				}
									
				formattedTrnReqId = KSKUtils.getFormattedStringFromNumber(transReqId);	
				
				if (dtnLogger == null)
					dtnLogger = user;
				
				String dtnDate = "";
				if (transmittalDate.equals("")){
					Calendar c = Calendar.getInstance(TimeZone.getTimeZone("IST"));			
					Date d = c.getTime();
					SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);				
					dtnDate = sdf.format(d);	
				}
				else
					dtnDate = transmittalDate;
				
				//String transmittalId, String transmittalSubject, User user, String inwardDTNFieldName
				KskTemplateHelper kth = KSKUtils.getKskTemplateHelper(dcrBA.getSystemId(), trnSystemId, requestList, revList, approvalCategoryList,
						documentTypeList, quantityList, summaryList, toList, ccList, trnIdPrefix + formattedTrnReqId, transmittalSubject,
						remarks, dtnLogger, INWARD_TRANSMITTAL_NO, tpParams, dtnDate, draftedBy);
				
				String templateName = tpParams.get("transmittal_template_name");
				if ((templateName == null) || templateName.trim().equals(""))
					throw new TBitsException("The name of the template used for transmittal creation not provided.");
				transmitAttachments = getDTNAttachment(kth, templateName);
			
				//Create Connection, FileResourceManager and MailResourceManager.
				connection = DataSourcePool.getConnection();
				connection.setAutoCommit(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
				throw new DatabaseException("Error occurred while fetching database connection.", e1);
			} catch (EngineException e) {
				e.printStackTrace();
			}
			
			//Create new AddRequest object to add new transmittal.
			AddRequest addReq = new AddRequest();
			addReq.setContext(aRequest.getContextPath());
			addReq.setSource(TBitsConstants.SOURCE_CMDLINE);
			aParamTable.put(Field.BUSINESS_AREA, transBA.getSystemPrefix());
			
			aParamTable.put(Field.USER, dtnLoggerName.trim());
			aParamTable.put(Field.REQUEST, transReqId + KSKUtils.EMPTY_STRING);			
			aParamTable.put(Field.DESCRIPTION, "Transmittal for documents: " + linkedRequests);
			String dtnRequestSubject = trnIdPrefix + formattedTrnReqId;
			dtnRequestSubject = (transmittalSubject.trim().equals(""))
									? dtnRequestSubject 
									: dtnRequestSubject + ": " + transmittalSubject;
			aParamTable.put(Field.SUBJECT, dtnRequestSubject);
			
			aParamTable.put(Field.ASSIGNEE, toList);
			aParamTable.put(Field.SUBSCRIBER, ccList);	
			aParamTable.put(Field.STATUS, DTN_STATUS_TRANSMITTAL_COMPLETE);			
			aParamTable.put(Field.ATTACHMENTS, getTransmittalAttachments(reqFileNames));
			
			String logDate = "";
			if((transmittalDate == null) || transmittalDate.trim().equals("")){
				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("IST"));			
				Date d = c.getTime();
				SimpleDateFormat sdf = new SimpleDateFormat(TBitsConstants.API_DATE_FORMAT);//"yyyy-MM-dd HH:mm:ss");		
				logDate = sdf.format(d);			
			}
			else {
				DateFormat df = new SimpleDateFormat(YYYY_MM_DD);
				Date d = df.parse(transmittalDate);
				logDate = Timestamp.toCustomFormat(d, TBitsConstants.API_DATE_FORMAT);//"yyyy-MM-dd HH:mm:ss");				
			}
			aParamTable.put(Field.LOGGED_DATE, logDate);
			aParamTable.put(Field.LASTUPDATED_DATE, logDate);
			
			if ((dcrBA.getSystemId() != 13) || (dcrBA.getSystemId() != 19))
				aParamTable.put(Field.IS_PRIVATE, KSKUtils.TRUE);
			
			String originatorRecipientString = getOriginatorRecipient(dcrBA.getSystemId());
			if (originatorRecipientString != null){
				String[] srcDestinationArray = originatorRecipientString.trim().split(KSKUtils.DELIMETER_COMMA);
				aParamTable.put(Field.CATEGORY, srcDestinationArray[0]);
				aParamTable.put(Field.REQUEST_TYPE, srcDestinationArray[1]);
			}
			
			String originator = tpParams.get("originator");
			String recipient = tpParams.get("recipient");
			if ((originator != null) && (!originator.trim().equals(""))){
				aParamTable.put(Field.CATEGORY, originator);		
			}
			
			if ((recipient != null) && (!recipient.trim().equals(""))){
				aParamTable.put(Field.REQUEST_TYPE, recipient);
			}			
			
			//TODO: Attachments field names have to be parameterized.
			aParamTable.put(COMMENTED_FILES, getTransmittalAttachments(deliverableFileNames));
			aParamTable.put(DTN_FILE, transmitAttachments);
			aParamTable.put(Field.NOTIFY, KEYWORD_TRUE);
			aParamTable.put(DTN_NUMBER, trnIdPrefix + formattedTrnReqId);
			
			aParamTable.put(FIELD_DRAFTED_BY, draftedBy);
					
			StringBuffer desSB= new StringBuffer();
			desSB.append(emailBody);
			if ((emailBody!= null) && (!emailBody.equals(KSKUtils.EMPTY_STRING))){
				//TODO: if required, put in a description if required in future.
			}
			
			/*desSB.append(emailBody + "\n\n[This transmittal note was created with date, \"" + logDate + "\" on \"" + 
					Timestamp.getGMTNow().toCustomFormat("yyyy-MM-dd") + "\".]");*/
			
			aParamTable.put(Field.DESCRIPTION,  desSB.toString());
			Request trnRequest = addReq.addRequest(connection, tBitsResMgr, aParamTable);
			
			int dcrSysId = dcrBA.getSystemId();
			ArrayList<Field> extAttachmentFieldList = Field.lookupBySystemId(dcrSysId, true, DataType.ATTACHMENTS);
			ArrayList<Request> dcrRequestObjList = new ArrayList<Request>();
			String[] approvalCategory = approvalCategoryList.split(",");
			for (int index = 0;index < dcrRequestList.length; index++){
				UpdateRequest dcrUpdateRequest = new UpdateRequest();
				
				// Update the drawings sent to transmittal by changing the status and description. 
				Hashtable <String,String> tempParamTable = new Hashtable<String, String>();
				
				tempParamTable.put (Field.BUSINESS_AREA,  + dcrSysId + KSKUtils.EMPTY_STRING);
				tempParamTable.put (Field.REQUEST, dcrRequestList[index]);
				if ((logDate != null) && (!logDate.trim().equals(""))) 
					tempParamTable.put (Field.LASTUPDATED_DATE, logDate);
				if (user != null)
					tempParamTable.put (Field.USER, user.getUserLogin());
				
				int dcrReqId = Integer.parseInt(dcrRequestList[index]);
				//Cache these DCR requests, so that they can be used for processing during update other mapped business area requests.
				Request dcrRequest = Request.lookupBySystemIdAndRequestId(connection, dcrSysId, dcrReqId);
				
				//Retain existing attachments, during update.
				if(extAttachmentFieldList != null){
					for (Field extAttachmentField : extAttachmentFieldList){					
						if(extAttachmentField != null){
							String fieldName = extAttachmentField.getName();
							String fieldValue = dcrRequest.get(fieldName);
							if(fieldValue != null)
								tempParamTable.put(fieldName, fieldValue);
						}
					}
				}								
				
				updateStatusBasedOnApprovalCategory(dcrRequest, approvalCategory[index], tempParamTable);
				runTransmittalRules(connection, transReqId, trnRequest, dcrBA, dcrBA, dcrRequest, 
						tempParamTable, trnTypeName, KSKUtils.DCR_BUSINESS_AREA, false);			
				Request updatedRequest = dcrUpdateRequest.updateRequest(connection, tBitsResMgr, tempParamTable);
				dcrRequestObjList.add(index, updatedRequest);
			}
						
			Hashtable<Integer, Integer> dtrRequestsMap = new Hashtable<Integer, Integer>();
			Hashtable<Integer, Integer> iDtrRequestsMap = new Hashtable<Integer, Integer>();
			Hashtable<Integer, Hashtable<Integer, Integer>> otherBARequestsMap = new Hashtable<Integer, Hashtable<Integer,Integer>>();
			dcrRequestObjList.trimToSize();
			int dtrSysId = transmittalType.getDtrSysId();
			try {	
				ArrayList<Integer> targetBAList = getTargetBusinessAreas(connection, dcrBA.getSystemId());
				for (Integer targetBAId : targetBAList){			
					String mappedBizAreas = tpParams.get(MAPPED_BUSINESS_AREAS);
					if (mappedBizAreas == null)
						throw new TBitsException("Mapping of business areas for transmittals missing.");
					String[] mappedBAList = mappedBizAreas.split(KSKUtils.DELIMETER_COMMA);
					for (String tempIdStr : mappedBAList){
						int tempId = 0;
						if (!tempIdStr.trim().equals("")){
							tempId = Integer.parseInt(tempIdStr);
							if (targetBAId == tempId) {
								String otherBAsList = KSKUtils.getProperty(KSK_IRULE_OTHER_BA_LIST);
								if (otherBAsList == null)
									throw new TBitsException("List of business areas belonging to \"Other Documents\" category not found. " +
											"Please check \"app.properties\" file for the property name \"ksk.irule.other_ba_list\".");
								if(KSKUtils.isExistsInString(otherBAsList, dcrBA.getSystemPrefix().trim())){
									updateAdhocTargetBA(connection, trnRequest, dcrBA, targetBAId, dcrRequestObjList, transmittalType, tBitsResMgr,
											aRequest.getContextPath(), dtrSysId, dtrRequestsMap, iDtrRequestsMap, otherBARequestsMap, logDate);
								}
								else{
									updateTargetBA(connection, trnRequest, dcrBA, targetBAId, dcrRequestObjList, trnTypeName, 
											tBitsResMgr, aRequest.getContextPath(), dtrSysId , logDate);
								}
							}
						}
					}					
				}
				
				if (KSKUtils.isExistsInString("", dcrBA.getSystemPrefix().trim())){
					//Insert all other required mappings of DTR request with other BA request Ids involved in this transmittal process.
					addOtherMappingsForDTRAndIDTRBA(connection, dcrBA, transmittalType, dtrSysId,
							dtrRequestsMap, otherBARequestsMap, false);
					addOtherMappingsForDTRAndIDTRBA(connection, dcrBA, transmittalType, 18,
							iDtrRequestsMap, otherBARequestsMap, true);

					//Secondary requests mapping.
					for(int dcrReqId : otherBARequestsMap.keySet()){
						Hashtable<Integer, Integer> tempMap = otherBARequestsMap.get(dcrReqId);
						for (int otherBASysId1 : tempMap.keySet()){
							for(int otherBASysId2 : tempMap.keySet()){
								if (otherBASysId1 != otherBASysId2){
									insertIntoRequestMapping(connection, otherBASysId1, tempMap.get(otherBASysId1), 
											otherBASysId2, tempMap.get(otherBASysId2), 0, 0, false);
								}
							}
						}
					}
				}
				
				System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%Finished updating all BA's&&&&&&&&&&&&&&&&&&&");
				
				result.put(KEYWORD_SUCCESS, KEYWORD_TRUE);
				result.put(KEYWORD_VALUE, formattedTrnReqId);
				out.print(result.toString());	
						
				connection.commit();		
				maxIdConn.commit();
			} catch (SQLException e) {
				rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
				APIException apie = new APIException();
				apie.addException(new TBitsException("Unable to get connection to the database"));
				throw apie;
			}		
			tBitsResMgr.commit();
		}catch (DatabaseException dbe) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);			
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "Database Exception: \n" + dbe.getMessage());
			out.print(result.toString());		
			return;
		}catch (TBitsException tbe) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "TBits Exception: \n" + tbe.getMessage());
			out.print(result.toString());
			tbe.printStackTrace();
			return;
		}catch (APIException apie) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr); 
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "Permission Exception: \n" + apie.getMessage());
			out.print(result.toString());
			apie.printStackTrace();
			return;							
		} catch (ParseException e) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "TBits Exception: \n" + e.getMessage());
			out.print(result.toString());
			e.printStackTrace();
		}finally{
			try {
				if((connection != null) && (!connection.isClosed()))			
					connection.close();
				if((maxIdConn != null) && (!maxIdConn.isClosed()))
					maxIdConn.close();
			} catch (SQLException e) {
				LOG.error(new Exception("Unable to close the connection to the database.", e));
			}
		}
	}
	
	private String getTransmittalProcessNameBasedOnOriginatingAgency(
			BusinessArea dcrBA, String originatingAgency, String transmittalProcessSerialKey) {
		String tempProcessKey = "";
		if (KSKUtils.isSEPCODCR(dcrBA) && originatingAgency.equals(SEPCO_BILASPUR))
			tempProcessKey = SEPCO_DTN_PREFIX_S_02;
		else
			tempProcessKey = transmittalProcessSerialKey;
		return tempProcessKey;
	}


	private boolean setMaxTransmittalIdToCurrentPreDefinedMaxId(
			Connection maxIdConn, String transmittalProcessSerialKey, int preDefinedTrnId) throws DatabaseException {
		try{
			PreparedStatement ps = maxIdConn.prepareStatement("UPDATE ba_max_transmittal_ids SET max_transmittal_id = ?" +
					" WHERE transmittal_process_name=?");
			ps.setInt(1, preDefinedTrnId);
			ps.setString(2, transmittalProcessSerialKey);
			int rowsCount = ps.executeUpdate();
			if (rowsCount > 0)
				return true;
			else {
				throw new SQLException("Error occurred while set current max transmittal number for transmittalProcessKey: " 
						+ transmittalProcessSerialKey
						+ ".\n" + "Could not set current max transmittal number for max transmittal id with name: " 
						+ transmittalProcessSerialKey);
			}
		}catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Error occurred while set current max transmittal number " +
					"for transmittal process serial key: " + transmittalProcessSerialKey, e);
		}		
	}


	private void updateStatusBasedOnApprovalCategory(Request dcrRequest, String approvalCategory, Hashtable<String, String> aParamTable) {
		if (dcrRequest.getSystemId() == 19){
			if(approvalCategory.equals("A1"))
				aParamTable.put(Field.STATUS, "A1ReleaseForConstruction");
			else if(approvalCategory.equals("A2"))	
				aParamTable.put(Field.STATUS, "A2Approved");
			else if(approvalCategory.equals("A3"))
				aParamTable.put(Field.STATUS, "A3ApprovedexceptasnotedForwardFINALdrawing");
			else if(approvalCategory.equals("A4"))
				aParamTable.put(Field.STATUS, "A4ApprovedexceptasnotedResubmissionrequired");
			else if(approvalCategory.equals("A5"))	
				aParamTable.put(Field.STATUS, "A5Disapproved");
			else if(approvalCategory.equals("A6"))		
				aParamTable.put(Field.STATUS, "A6ForInformationReferenceonly");
		}		
	}


	private void addOtherMappingsForDTRAndIDTRBA(Connection connection, BusinessArea dcrBA,
			KSKTransmittalType transmittalType, int dtrSysId, Hashtable<Integer, Integer> dtrRequestsMap,
			Hashtable<Integer, Hashtable<Integer, Integer>> otherBARequestsMap, boolean isIDTR)
			throws DatabaseException {
		if ((!dtrRequestsMap.isEmpty()) && (!otherBARequestsMap.isEmpty())){
			for (int dcrRequestId : dtrRequestsMap.keySet()){
				Integer dtrRequestId = dtrRequestsMap.get(dcrRequestId);
				Hashtable<Integer, Integer> otherBaRequestIds = otherBARequestsMap.get(dcrRequestId);
				for (int otherBASysId : otherBaRequestIds.keySet()){
					Integer otherBAReqId = otherBaRequestIds.get(otherBASysId);
					//false in the parameter list represents that its not IDTR BA.
					if (isIDTR)
						insertIntoRequestMapping(connection, otherBASysId, otherBAReqId, dtrSysId, dtrRequestId, 
								dcrBA.getSystemId(), transmittalType.getTargetSysId(), isIDTR);	
					else if((otherBASysId == transmittalType.getTargetSysId()) && (!isIDTR))
						insertIntoRequestMapping(connection, otherBASysId, otherBAReqId, dtrSysId, dtrRequestId, 
												dcrBA.getSystemId(), transmittalType.getTargetSysId(), isIDTR);					
				}
			}
		}
	}
	
	private void updateTargetBA (Connection connection, Request transmittalRequest, BusinessArea dcrBA, int targetBASysId, 
									ArrayList<Request> dcrRequestObjList, String transmittalType, TBitsResourceManager tBitsResMgr,
									String contextPath, int dtrSystemId, String logDate) 
									throws DatabaseException, TBitsException, APIException, SQLException{
		
		boolean isMatched = false;
		int dcrSysId = dcrBA.getSystemId();
		Field deliverableField = KSKUtils.lookupBySystemIdAndFieldId(connection, dcrSysId, deliverableFieldId);
		/*Field targetDelAttachmentField = KSKUtils.getTargetBusinessAreaField(connection, transmittalType.getTrnProcessId(), 
				dcrSysId, deliverableFieldId, targetBASysId);*/		
		ArrayList<Field> extAttachmentFieldList = Field.lookupBySystemId(targetBASysId, true, DataType.ATTACHMENTS);
				
		for (int i=0; i<dcrRequestObjList.size(); i++){
			LOG.info("##########################Target BA sysId: " + targetBASysId);
			isMatched = false;
			
			int baType = KSKUtils.OTHER_BUSINESS_AREA;
			if(dtrSystemId == targetBASysId)
				baType = KSKUtils.DTR_BUSINESS_AREA;
			
			Request dcrRequest = dcrRequestObjList.get(i);//Request.lookupBySystemIdAndRequestId (connection, dcrSysId, Integer.parseInt(dcrRequestObjList[i].trim()));
			String dNumValue = dcrRequest.get(KSKUtils.FIELD_DRAWING_NO);
			
			Field dNumField = KSKUtils.lookupBySystemIdAndFieldName(connection, targetBASysId, KSKUtils.FIELD_DRAWING_NO);
			Hashtable <String,String> aParamTable = new Hashtable<String, String>();
						
			Request matchedRequest = null;				 

			try{
				CallableStatement cs = connection.prepareCall(STP_GET_REQUEST_ID_BY_DOC_NO + " ?, ?, ?");
				cs.setInt(1, targetBASysId);
				cs.setInt(2, dNumField.getFieldId());
				cs.setString(3, dNumValue);	
				ResultSet reqIdRS = cs.executeQuery();
				LOG.info("###############################Drawing number: " + dNumValue);
				if (reqIdRS != null){
					while(reqIdRS.next()){					
						matchedRequest = Request.lookupBySystemIdAndRequestId(connection, targetBASysId, reqIdRS.getInt(Field.REQUEST));	
						
						if (matchedRequest != null){
							isMatched = true;
							
							BusinessArea targetBA = KSKUtils.lookupBySystemId(connection, targetBASysId);
							aParamTable.put(Field.BUSINESS_AREA, targetBA.getSystemPrefix());
							aParamTable.put(Field.REQUEST, matchedRequest.getRequestId() + KSKUtils.EMPTY_STRING);
							if ((logDate != null) && (!logDate.trim().equals("")))
								aParamTable.put (Field.LASTUPDATED_DATE, logDate);
														
							//Update deliverable attachment field.							
							String delPrevAttachmentEx = matchedRequest.get(deliverableField.getName());
								//AttachmentUtils.getAttachmentEx(connection, matchedRequest, deliverableFieldId);
							
							Collection<AttachmentInfo> prevDelAttachments = null;
							if ((delPrevAttachmentEx != null) && (!delPrevAttachmentEx.trim().equals(KSKUtils.EMPTY_STRING)))
								prevDelAttachments = AttachmentInfo.fromJson(delPrevAttachmentEx);
							else
								prevDelAttachments = null;
							
							//Retain existing attachments, during update.
							if(extAttachmentFieldList != null){
								for (Field extAttachmentField : extAttachmentFieldList){					
									if(extAttachmentField != null){
										String fieldName = extAttachmentField.getName();
										String fieldValue = matchedRequest.get(fieldName);
										 if((fieldValue != null) 
												 && (extAttachmentField.getFieldId() != deliverableFieldId))
											aParamTable.put(fieldName, fieldValue);
									}
								}
							}
														
							//String delPrevAttachmentEx = AttachmentUtils.getAttachmentEx(connection, matchedRequest, deliverableFieldId);
							//Field deliverableField = Field.lookupBySystemIdAndFieldId(targetBASysId, deliverableFieldId);
							//Collection<AttachmentInfo> prevDelAttachments = null;
						/*	if (deliverableField == null)
								throw new TBitsException("Deliverable field with id: " + deliverableFieldId +
										", could not be found, in Business Area with Id: " + targetBASysId);	
							String delAttJSONStr = matchedRequest.getExString(deliverableField.getName());
							if(delAttJSONStr != null)
								prevDelAttachments = AttachmentInfo.fromJson(delAttJSONStr);						
							*/
							Collection<AttachmentInfo> selectedDelAttachments = KSKUtils.getSelectedAttachments(dcrRequest,
									deliverableFileNames, i);
							KSKUtils.mergeAttachmentsLists(selectedDelAttachments, prevDelAttachments);
							String delAttachments = "";
							if (selectedDelAttachments != null)
								delAttachments = AttachmentInfo.toJson(selectedDelAttachments);
							
							//Update other attachments field.
							Collection<AttachmentInfo> prevAttachments = matchedRequest.getAttachments();
							Collection<AttachmentInfo> selectedAttachments = KSKUtils.getSelectedAttachments(dcrRequest, reqFileNames, i);							
							KSKUtils.mergeAttachmentsLists(selectedAttachments, prevAttachments);							
							String sAttachments = "";
							if(selectedAttachments != null)
								sAttachments = AttachmentInfo.toJson(selectedAttachments);
														
							UpdateRequest updateRequest = new UpdateRequest();
							updateRequest.setContext(contextPath);
							updateRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
							
							aParamTable.put(Field.ATTACHMENTS, sAttachments);
							if (deliverableField != null)
								aParamTable.put(deliverableField.getName(), delAttachments);
							updateFields(connection, dcrSysId, dcrRequest, targetBASysId, delAttachments, sAttachments, aParamTable);
							
							runTransmittalRules(connection, transReqId, transmittalRequest, BusinessArea.lookupBySystemId(targetBASysId), dcrBA, dcrRequest, 
									aParamTable, transmittalType, baType, false);
							
							updateRequest.updateRequest(connection, tBitsResMgr, aParamTable);							
						}		            		
					}					
				}
				else{
					System.out.println("No resultset found");		            
				}

				//cs.close();
				//cs = null;				
			} catch (SQLException sqle) {
				StringBuilder message = new StringBuilder();

				message.append("An exception occurred while retrieving a matching request with matching drawing number: ")
				.append(dNumValue);

				throw new DatabaseException(message.toString(), sqle);
			} catch (NullPointerException npe){
				npe.printStackTrace();
				new TBitsException("Error occurred with updating DCRs.");
			}
			
			if (!isMatched){
				
				System.out.println("No matching request exist, hence adding new...... ");
				
				Collection<AttachmentInfo> selectedDelAttachments = KSKUtils.getSelectedAttachments(dcrRequest, deliverableFileNames, i);
				String delAttachments = "";
				if (selectedDelAttachments != null)
					delAttachments = AttachmentInfo.toJson(selectedDelAttachments);				
				
				//Update other attachments field.
				Collection<AttachmentInfo> selectedAttachments = KSKUtils.getSelectedAttachments(dcrRequest, reqFileNames, i);				
				String sAttachments = "";
				if(selectedAttachments != null)
					sAttachments = AttachmentInfo.toJson(selectedAttachments);
				
				aParamTable.put(Field.ATTACHMENTS, sAttachments);				
				//Field dField = KSKUtils.lookupBySystemIdAndFieldId(connection, dcrSysId, deliverableFieldId);
				if (deliverableField != null)
					aParamTable.put(deliverableField.getName(), delAttachments);
				
				//updateFields(connection, dcrSysId, dcrRequest, targetBASysId, delAttachments, sAttachments, aParamTable);
				
				AddRequest addRequest = new AddRequest();            	           	
				addRequest.setContext(contextPath);
				addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
				BusinessArea currentBA = KSKUtils.lookupBySystemId(connection, targetBASysId);
				aParamTable.put(Field.BUSINESS_AREA, currentBA.getSystemPrefix());
				aParamTable.put(Field.SUBJECT, dcrRequest.getSubject());
				if ((logDate != null) && (!logDate.trim().equals(""))){
					aParamTable.put (Field.LOGGED_DATE, logDate);
					aParamTable.put (Field.LASTUPDATED_DATE, logDate);
				}
				
				//Finally add the request
				updateFields(connection, dcrSysId, dcrRequest, targetBASysId, delAttachments, sAttachments, aParamTable);
				
				runTransmittalRules(connection, transReqId, transmittalRequest, currentBA, dcrBA, dcrRequest, aParamTable, transmittalType, baType, true);
				addRequest.addRequest(connection, tBitsResMgr, aParamTable);
				isMatched = false;
			}		
		}
	}
	
	private void updateAdhocTargetBA (Connection connection, Request transmittalRequest, BusinessArea dcrBA, int targetBASysId, 
			ArrayList<Request> dcrRequestObjList, KSKTransmittalType transmittalType, TBitsResourceManager tBitsResMgr, 
			String contextPath, int dtrSystemId, Hashtable<Integer, Integer> dtrRequestsMap, Hashtable<Integer, Integer>iDtrRequestsMap,
			Hashtable<Integer, Hashtable<Integer, Integer>> otherRequestsMap, String logDate) 
			throws DatabaseException, TBitsException, APIException, SQLException{

		boolean isMatched = false;
		int dcrSysId = dcrBA.getSystemId();
		
		Field deliverableField = KSKUtils.lookupBySystemIdAndFieldId(connection, dcrSysId, deliverableFieldId);
		ArrayList<Field> extAttachmentFieldList = Field.lookupBySystemId(targetBASysId, true, DataType.ATTACHMENTS);
		
		for (int i=0; i<dcrRequestObjList.size(); i++){
			
			LOG.info("Updating target business area with Id: " + targetBASysId + ", post transmittal.");
			System.out.println("##########################Target BA sysId: " + targetBASysId + ", post transmittal.");
			isMatched = false;

			int baType = KSKUtils.OTHER_BUSINESS_AREA;
			if(dtrSystemId == targetBASysId)
				baType = KSKUtils.DTR_BUSINESS_AREA;
			
			Request dcrRequest = dcrRequestObjList.get(i);//Request.lookupBySystemIdAndRequestId (connection, dcrSysId, Integer.parseInt(dcrRequestObjList[i].trim()));
			int dcrRequestId = dcrRequest.getRequestId();			
			String dNumValue = dcrRequest.get(KSKUtils.FIELD_DRAWING_NO);

			//Get the current BA.
			BusinessArea currentBA = KSKUtils.lookupBySystemId(connection, targetBASysId);
			
			boolean isIDTRMapping = false;
			// Remove the hard coding, once the current design for mapping is accepted.
			if (currentBA.getSystemId() == 18){
				isIDTRMapping = true;
			}
						
			Hashtable <String,String> aParamTable = new Hashtable<String, String>();
			Request matchedRequest = null;				 

			try{
				CallableStatement cs = connection.prepareCall(STP_GET_MATCHED_REQUEST_ID + " ?, ?, ?, ?, ?");
				cs.setInt(1, dcrSysId);				
				cs.setInt(2, dcrRequestId);
				cs.setInt(3, targetBASysId);
				
				if(targetBASysId != dtrSystemId){
					cs.setInt(4, 0);
					cs.setInt(5, 0);
				}
				else{					
					cs.setInt(4, dcrSysId);
					cs.setInt(5, transmittalType.getTargetSysId());
				}				
				ResultSet reqIdRS = cs.executeQuery();

				if (reqIdRS != null){
					while(reqIdRS.next()){					
						int matchedRequestId = reqIdRS.getInt(1);
						if (matchedRequestId > 0)
							matchedRequest = Request.lookupBySystemIdAndRequestId(connection, targetBASysId, matchedRequestId);	
						
						if (matchedRequest != null){
							isMatched = true;

							BusinessArea targetBA = KSKUtils.lookupBySystemId(connection, targetBASysId);
							aParamTable.put(Field.BUSINESS_AREA, targetBA.getSystemPrefix());
							aParamTable.put(Field.REQUEST, matchedRequest.getRequestId() + KSKUtils.EMPTY_STRING);							

							//Update deliverable attachment field.
							//Field deliverableField = KSKUtils.lookupBySystemIdAndFieldId(connection, dcrSysId, deliverableFieldId);
							String delPrevAttachmentEx = "";
							if (deliverableField != null)
								delPrevAttachmentEx = matchedRequest.get(deliverableField.getName());
							//AttachmentUtils.getAttachmentEx(connection, matchedRequest, deliverableFieldId);

							Collection<AttachmentInfo> prevDelAttachments = null;
							if ((delPrevAttachmentEx != null) && (!delPrevAttachmentEx.trim().equals(KSKUtils.EMPTY_STRING)))
								prevDelAttachments = AttachmentInfo.fromJson(delPrevAttachmentEx);
							else
								prevDelAttachments = null;
							
							//Retain existing attachments, during update.
							if(extAttachmentFieldList != null){
								for (Field extAttachmentField : extAttachmentFieldList){					
									if(extAttachmentField != null){
										String fieldName = extAttachmentField.getName();
										String fieldValue = matchedRequest.get(fieldName);
										 if((fieldValue != null) && (!fieldName.equals(deliverableField.getName()))) 
												 //&& (extAttachmentField.getFieldId() != deliverableFieldId))
											aParamTable.put(fieldName, fieldValue);
									}
								}
							}
														
							//String delPrevAttachmentEx = AttachmentUtils.getAttachmentEx(connection, matchedRequest, deliverableFieldId);
							//Field deliverableField = Field.lookupBySystemIdAndFieldId(targetBASysId, deliverableFieldId);
							/*Collection<AttachmentInfo> prevDelAttachments = null;
							if (targetDelAttachmentField == null)
								throw new TBitsException("Deliverable field with id: " + deliverableFieldId +
										", could not be found, in Business Area with Id: " + targetBASysId);	
							String delAttJSONStr = matchedRequest.getExString(targetDelAttachmentField.getName());
							if(delAttJSONStr != null)
								prevDelAttachments = AttachmentInfo.fromJson(delAttJSONStr);*/
							
							Collection<AttachmentInfo> selectedDelAttachments = KSKUtils.getSelectedAttachments(dcrRequest, 
									deliverableFileNames, i);
							KSKUtils.mergeAttachmentsLists(selectedDelAttachments, prevDelAttachments);
							String delAttachments = "";
							if (selectedDelAttachments != null)
								delAttachments = AttachmentInfo.toJson(selectedDelAttachments);

							//Update other attachments field.
							Collection<AttachmentInfo> prevAttachments = matchedRequest.getAttachments();
							Collection<AttachmentInfo> selectedAttachments = KSKUtils.getSelectedAttachments(dcrRequest, reqFileNames, i);							
							KSKUtils.mergeAttachmentsLists(selectedAttachments, prevAttachments);							
							String sAttachments = "";
							if(selectedAttachments != null)
								sAttachments = AttachmentInfo.toJson(selectedAttachments);

							UpdateRequest updateRequest = new UpdateRequest();
							updateRequest.setContext(contextPath);
							updateRequest.setSource(TBitsConstants.SOURCE_CMDLINE);

							aParamTable.put(Field.ATTACHMENTS, sAttachments);
							if (deliverableField != null)
								aParamTable.put(deliverableField.getName(), delAttachments);
							
							aParamTable.put (Field.LASTUPDATED_DATE, logDate);
							updateFields(connection, dcrSysId, dcrRequest, targetBASysId, delAttachments, sAttachments, aParamTable);

							runTransmittalRules(connection, transReqId, transmittalRequest, BusinessArea.lookupBySystemId(targetBASysId), dcrBA, dcrRequest, 
									aParamTable, transmittalType.getName(), baType, false);

							updateRequest.updateRequest(connection, tBitsResMgr, aParamTable);
							
							if(isIDTRMapping)
								iDtrRequestsMap.put(dcrRequestId, matchedRequestId);
							else{
								Hashtable<Integer, Integer> tempTable = otherRequestsMap.get(dcrRequestId);
								if (tempTable == null){
									tempTable = new Hashtable<Integer, Integer>();
									tempTable.put(targetBASysId, matchedRequestId);
									otherRequestsMap.put(dcrRequestId, tempTable);
								}
								else{
									tempTable.put(targetBASysId, matchedRequestId);
								}
							}
						}		            		
					}					
				}
				else{
					System.out.println("No resultset found");		            
				}

				cs.close();
				cs = null;				
			} catch (SQLException sqle) {
				StringBuilder message = new StringBuilder();

				message.append("An exception occurred while retrieving a matching request with matching drawing number: ")
				.append(dNumValue);

				throw new DatabaseException(message.toString(), sqle);
			} 

			if (!isMatched){

				System.out.println("No matching request exist, hence adding new...... ");

				Collection<AttachmentInfo> selectedDelAttachments = KSKUtils.getSelectedAttachments(dcrRequest, deliverableFileNames, i);
				String delAttachments = "";
				if (selectedDelAttachments != null)
					delAttachments = AttachmentInfo.toJson(selectedDelAttachments);				

				//Update other attachments field.
				Collection<AttachmentInfo> selectedAttachments = KSKUtils.getSelectedAttachments(dcrRequest, reqFileNames, i);				
				String sAttachments = "";
				if(selectedAttachments != null)
					sAttachments = AttachmentInfo.toJson(selectedAttachments);

				aParamTable.put(Field.ATTACHMENTS, sAttachments);				
				//Field dField = KSKUtils.lookupBySystemIdAndFieldId(connection, dcrSysId, deliverableFieldId);
				if (deliverableField != null)
					aParamTable.put(deliverableField.getName(), delAttachments);

				//updateFields(connection, dcrSysId, dcrRequest, targetBASysId, delAttachments, sAttachments, aParamTable);

				AddRequest addRequest = new AddRequest();            	           	
				addRequest.setContext(contextPath);
				addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
				
				aParamTable.put(Field.BUSINESS_AREA, currentBA.getSystemPrefix());
				aParamTable.put(Field.SUBJECT, dcrRequest.getSubject());
				aParamTable.put (Field.LOGGED_DATE, logDate);
				//Finally add the request
				updateFields(connection, dcrSysId, dcrRequest, targetBASysId, delAttachments, sAttachments, aParamTable);

				runTransmittalRules(connection, transReqId, transmittalRequest, currentBA, dcrBA, dcrRequest, aParamTable, 
						transmittalType.getName(), baType, true);
				Request newMappedRequest = addRequest.addRequest(connection, tBitsResMgr, aParamTable);
				
				// Set-up to handle mapping of the request ids.				
				//Insert the mapping for the newly created requests.
				int newRequestId = newMappedRequest.getRequestId();
				if (targetBASysId == dtrSystemId)
					insertIntoRequestMapping(connection, dcrSysId, dcrRequest.getRequestId(), targetBASysId, newRequestId,
							dcrSysId, transmittalType.getTargetSysId(), isIDTRMapping);
				else
					insertIntoRequestMapping(connection, dcrSysId, dcrRequest.getRequestId(), targetBASysId, newRequestId,
						0, 0, isIDTRMapping);
				
				//Maintain map for mapping of request for other BAs involved in the transmittal process with DTR request
				if (targetBASysId == dtrSystemId)
					dtrRequestsMap.put(dcrRequestId, newRequestId);
				else if(isIDTRMapping)
					iDtrRequestsMap.put(dcrRequestId, newRequestId);
				else{
					Hashtable<Integer, Integer> tempTable = otherRequestsMap.get(dcrRequestId);
					if (tempTable == null){
						tempTable = new Hashtable<Integer, Integer>();
						tempTable.put(targetBASysId, newRequestId);
						otherRequestsMap.put(dcrRequestId, tempTable);
					}
					else{
						tempTable.put(targetBASysId, newRequestId);
					}
				}				
				
				isMatched = false;
			}		
		}
	}
	
	private void updateFields(Connection connection, int dcrSystemId, Request dcrRequest, int targetSystemId,String deliverableAttachments, 
			String otherAttachments, Hashtable<String, String>aParamTable) throws DatabaseException, TBitsException{

		Hashtable<String,String> targetBAFields = KSKUtils.getTargetBusinessAreaFields(connection, dcrSystemId, targetSystemId);
		for(String fieldName : targetBAFields.keySet()){
			String tFieldName = targetBAFields.get(fieldName);
			String dcrFieldValue = dcrRequest.get(fieldName);
			if (tFieldName != null)
				aParamTable.put(tFieldName, dcrFieldValue);
		}
	}
	
	/**
	 * 
	 * @param connection
	 * @param sysId1
	 * @param requestId1
	 * @param sysId2
	 * @param requestId2
	 * @param i 
	 * @throws DatabaseException
	 */
	void insertIntoRequestMapping(Connection connection, int sysId1, int requestId1, int sysId2, 
			int requestId2, int dcrSysId, int targetSysId, boolean isIDTRMapping) throws DatabaseException{
		
		try{			
			
			String insertQuery = "";
			
			if (isIDTRMapping){
				insertQuery = "IF NOT EXISTS (SELECT * from request_mapping" +
				" where (sys_id_1=" + sysId1 + " and request_id_1=" + requestId1 + 
				" and sys_id_2=" + sysId2 + " and request_id_2=" + requestId2 + 
				" and party_sys_id_1=0 and party_sys_id_2=0))" + 
				
				" IF NOT EXISTS (SELECT * from request_mapping" +
				" where (sys_id_1=" + sysId2 + " and request_id_1=" + requestId2 + 
				" and sys_id_2=" + sysId1 + " and request_id_2=" + requestId1 + 
				" and party_sys_id_1=0 and party_sys_id_2=0))";
			}
			else{
				insertQuery = "IF NOT EXISTS (SELECT * from request_mapping" +
					" where (sys_id_1=" + sysId1 + " and request_id_1=" + requestId1 + 
					" and sys_id_2=" + sysId2 + " and request_id_2=" + requestId2 + 
					" and party_sys_id_1=" +  + dcrSysId + " and party_sys_id_2=" + targetSysId + "))" +
					
					"IF NOT EXISTS (SELECT * from request_mapping" +
					" where (sys_id_1=" + sysId1 + " and request_id_1=" + requestId1 + 
					" and sys_id_2=" + sysId2 + " and request_id_2=" + requestId2 + 
					" and party_sys_id_1=" + targetSysId + " and party_sys_id_2=" + dcrSysId + "))" + 
					
					" IF NOT EXISTS (SELECT * from request_mapping" +
					" where (sys_id_1=" + sysId2 + " and request_id_1=" + requestId2 + 
					" and sys_id_2=" + sysId1 + " and request_id_2=" + requestId1 + 
					" and party_sys_id_1=" + targetSysId + " and party_sys_id_2=" + dcrSysId + "))" +
					
					" IF NOT EXISTS (SELECT * from request_mapping" +
					" where (sys_id_1=" + sysId2 + " and request_id_1=" + requestId2 + 
					" and sys_id_2=" + sysId1 + " and request_id_2=" + requestId1 + 
					" and party_sys_id_1=" + dcrSysId + " and party_sys_id_2=" + targetSysId + "))";
			}
					
			insertQuery = insertQuery + " INSERT INTO request_mapping (sys_id_1, request_id_1, sys_id_2, request_id_2, party_sys_id_1, " +
					"party_sys_id_2) VALUES (?,?,?,?,?,?)";
			PreparedStatement ps = connection.prepareStatement(insertQuery);
			ps.setInt(1, sysId1);
			ps.setInt(2, requestId1);
			ps.setInt(3, sysId2);
			ps.setInt(4, requestId2);
			if (isIDTRMapping){
				ps.setInt(5, 0);
				ps.setInt(6, 0);
			}
			else{
				ps.setInt(5, dcrSysId);
				ps.setInt(6, targetSysId);
			}
			ps.execute();
			ps.close();
		}catch (SQLException sqle){
			throw new DatabaseException("Error while inserting request mapping.", sqle);
		}		
	}	
	
	private String getTransmittalAttachments(String[] reqFileNames) 
					throws NumberFormatException, ArrayIndexOutOfBoundsException, DatabaseException {
		StringBuilder tempAttachments = new StringBuilder();
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>();
				 
		if (reqFileNames != null)
			for (int i=0; i<reqFileNames.length; i++){
				String reqFileList = reqFileNames[i];
				if (reqFileList.trim().equals(KSKUtils.EMPTY_STRING))
					continue;
				else{					
					for (String reqFileInfo : reqFileList.split("<br3>")){//KSKUtils.DELIMETER_SEMICOLON)){
						String[] reqAttInfo = reqFileInfo.split("<br1>");//:");
						String attName = reqAttInfo[0];
						int repoFileId = Integer.parseInt(reqAttInfo[1]);
						int attSize = Integer.parseInt(reqAttInfo[2]);

						AttachmentInfo tAI = new AttachmentInfo();
						tAI.name = attName;
						tAI.size = attSize;
						tAI.repoFileId = repoFileId;
						tAI.requestFileId = 0;
						trnAttCollection.add(tAI);	
						}
					}					
				}
		
		tempAttachments.append(AttachmentInfo.toJson(trnAttCollection));
		return tempAttachments.toString();
	}
	
	
	
	/**
	 * @param connection
	 * @param maxIdConn
	 * @param tBitsResMgr
	 */
	private void rollbackAllOperations(Connection connection,
			Connection maxIdConn, TBitsResourceManager tBitsResMgr) {
		try {
			if(connection != null){
				connection.rollback();				
			}
			if(maxIdConn != null){
				maxIdConn.rollback();
			}
			
			tBitsResMgr.rollback();
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	private String getTableData(int aSystemId, String requestList,
			Hashtable<String, String> transmittalParams) throws NumberFormatException, DatabaseException, TBitsException {
		JSONArray reqArray = new JSONArray();
		for (String requestIdStr : requestList.split(KSKUtils.DELIMETER_COMMA)) {				
			Request request = Request.lookupBySystemIdAndRequestId(
					aSystemId, Integer.parseInt(requestIdStr));
			JSONObject obj = new JSONObject();
						
			String docNo = request.get (KSKUtils.FIELD_DRAWING_NO);
									
			if (docNo == null)
				return null;
			else if (docNo.trim().equals(KSKUtils.EMPTY_STRING)){
				docNo = "";
			}
			
			int requestId = request.getRequestId();
			
			obj.put(Field.REQUEST, requestId);
			obj.put(DOC_NO,docNo);
			obj.put("sepco_doc_no", request.get("SEPCODocumentNumber"));
			obj.put(REV_NO, request.get(KSKUtils.REVISION));
			obj.put(DESP,request.getSubject());
			
			String defaultAppCategory = "PL";
			if ((transmittalParams != null) && (!transmittalParams.isEmpty())){
				//TODO : what if this field is not found ?? 
				String fieldIdStr = transmittalParams.get(DELIVERABLE_FIELD_ID);			
				int delAttFieldId = Integer.parseInt(fieldIdStr);
				if (delAttFieldId != 0)
					obj.put(DELIVERABLES, AttachmentUtils.getAttachmentList(aSystemId, request, delAttFieldId, KSKUtils.REVISION));
				else
					obj.put(DELIVERABLES, "[]");
				defaultAppCategory = transmittalParams.get(DEFAULT_APPROVAL_CATEGORY);
				//TODO: Remove this hard coding
				if (aSystemId == 19)
					defaultAppCategory = getKSKAppCategoryForRequest(request);
			}
			else 
				obj.put(DELIVERABLES, "[]");
				
			obj.put(ATTACHMENTS, AttachmentUtils.getAttachmentList(aSystemId, request, KSKUtils.REVISION));		
			obj.put(SHEETS, "-");
			obj.put(TRANSMITTAL_DOC_TYPE, "SC");			
			obj.put(TRANSMITTAL_APP_CATEGORY, defaultAppCategory);
			obj.put(SUMMARY, request.getSummary());
			reqArray.add(obj);
		}
		return reqArray.toString();
	}	
	
	private String getKSKAppCategoryForRequest(Request request) {
		//A1, A2, A3, A4, A5, A6
		//A1ReleaseForConstruction, A2Approved, A3ApprovedexceptasnotedForwardFINALdrawing, A5Disapproved, A6ForInformationReferenceonly
		if (request.getStatusId().getName().equals("A1ReleaseForConstruction"))
			return "A1";
		else if (request.getStatusId().getName().equals("A2Approved"))
			return "A2";
		else if (request.getStatusId().getName().equals("A3ApprovedexceptasnotedForwardFINALdrawing"))
			return "A3";
		else if (request.getStatusId().getName().equals("A4ApprovedexceptasnotedResubmissionrequired"))
			return "A4";
		else if (request.getStatusId().getName().equals("A5Disapproved"))
			return "A5";
		else if (request.getStatusId().getName().equals("A6ForInformationReferenceonly"))
			return "A6";
		return "";
	}


	private String getOriginatorRecipient(int dcrSystemId){
		if (dcrSystemId == 13)
			return SEPCO_HEAD_OFFICE + "," + WPCL_HEAD_OFFICE;
		else if (dcrSystemId == 16)
			return WPCL_HEAD_OFFICE + "," + WPCL_CEC;
		else if (dcrSystemId == 17)
			return WPCL_CEC + "," + WPCL_DCPL;
		else if (dcrSystemId == 19)
			return WPCL_DCPL + "," + SEPCO_HEAD_OFFICE;
		return null;
	}
	
		
	/** 
	 * @param kth
	 * @throws DatabaseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws EngineException 
	 * @throws TBitsException 
	 */
	private String getDTNAttachment (KskTemplateHelper kth, String templateName) throws DatabaseException, 
			FileNotFoundException,IOException, EngineException, TBitsException{
		
		StringBuilder tempAttachments = new StringBuilder();
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>();		
		String pdfFilePath = KSKUtils.generateTransmittalNoteUsingBirt(templateName, kth, "Transmittal-Note");	
		File pdfFile = new File(pdfFilePath );
		Uploader uploader = new Uploader();
		AttachmentInfo trnNoteInfo = uploader.moveIntoRepository(pdfFile);
		trnAttCollection.add(trnNoteInfo);		
		tempAttachments.append(AttachmentInfo.toJson(trnAttCollection));
		return tempAttachments.toString();		
	}
		
	private ArrayList<Integer> getTargetBusinessAreas(Connection connection, int dcrSystemId) throws DatabaseException{
		
		ArrayList<Integer> targetSysIdList = new ArrayList<Integer>();
		try{
		connection = DataSourcePool.getConnection();
		PreparedStatement ps = connection.prepareStatement("SELECT DISTINCT target_sys_id FROM transmittal_dcr_target_ba_fields_map where dcr_sys_id=?");
		ps.setInt(1, dcrSystemId);
		ResultSet rs = ps.executeQuery();
		if (rs != null)
			while(rs.next()){
				 targetSysIdList.add(rs.getInt(TARGET_SYS_ID));				 
			}
		}catch (SQLException sqle){
			throw new DatabaseException("Error while retrieving target business area ids.", sqle);
		}
		return targetSysIdList;	
	}
	
	/**
	 * This methods runs various preLoader. It essentials is run just before rendering.
	 * @param businessAreaType 
	 * @param aRequest
	 * @param aResponse
	 * @param tagTable
	 * @throws TBitsException
	 */
	private void runTransmittalRules(Connection connection, int transmittalId, Request transmittalRequest, BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest, 
			Hashtable <String,String> paramTable, String transmittalType, int businessAreaType, boolean isAddRequest)
			throws TBitsException {
		PluginManager pm = PluginManager.getInstance();
        ArrayList<Class> transmittalRuleClasses = pm.findPluginsByInterface(ITransmittalRule.class.getName());
        ArrayList<ITransmittalRule> transmittalRuleLoaders = new ArrayList<ITransmittalRule>();
        if(transmittalRuleClasses != null)
        {
        	for(Class transmittalRuleClass:transmittalRuleClasses)
        	{
        		ITransmittalRule transmittalRule;
				try {
					transmittalRule = (ITransmittalRule) transmittalRuleClass.newInstance();
					transmittalRuleLoaders.add(transmittalRule);
				} catch (InstantiationException e) {
					LOG.error("Could not instantiate the pre renderer class: " + transmittalRuleClass.getClass().getName());
				} catch (IllegalAccessException e) {
					LOG.error("Could not access the renderer class: " + transmittalRuleClass.getClass().getName());
				}
        	}
        }
        
        Comparator<ITransmittalRule> c = new Comparator<ITransmittalRule>(){

			public int compare(ITransmittalRule arg0, ITransmittalRule arg1) {
				double diff = arg0.getSequence() - arg1.getSequence();
				if(diff > 0)
					return 1;
				else if(diff == 0)
					return 0;
				else 
					return -1;
			}
		};
		Collections.sort(transmittalRuleLoaders, c);
		
        for(ITransmittalRule trnRuleLoader:transmittalRuleLoaders)
        {
        	trnRuleLoader.process(connection, transmittalId, transmittalRequest, currentBA, dcrBA, dcrRequest, paramTable, transmittalType, businessAreaType, isAddRequest);
        }
	}
	
}
 
