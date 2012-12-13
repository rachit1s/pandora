/**
 * 
 */
package ipl;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import ipl.IPLUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.report.engine.api.EngineException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
public class IPLTransmittalPlugin implements TransmittalHandler {

	private static final String NSK_LN_T = "NSKLnT";

	private static final String AMR_LN_T = "AMRLnT";

	private static final String TYPE_OF_DOCUMENT_HTM = "type-of-document.htm";

	private static final String SYSTEM_CODES_HTM = "system-codes.htm";

	private static final String SYSTEM_CODE = "System Code:";

	private static final String TYPE_OF_DOCUMENT = "Type of Document:";

	private static final String TRANSMITTAL_TEMPLATE_NAME = "transmittal_template_name";

	private static final String RECIPIENT = "recipient";

	private static final String ORIGINATOR = "originator";

	private static final String MAPPED_BUSINESS_AREAS = "mappedBusinessAreas";

	private static final String DTN_NUMBER_INFIX_DISPLAY = "dtnNumberInfixDisplay";

	private static final String DTN_NUMBER_INFIX = "dtnNumberInfix";

	private static final String DISPLAY_INLINE = "inline";

	private static final String REFERENCE = "reference";

	private static final String USERS_LIST = "usersList";

	private static final String SUMMARY_LIST = "summaryList";

	private static final String INWARD_TRANSMITTAL_NO = "IncomingTransmittalNo";

	private static final String DTN_STATUS_TRANSMITTAL_COMPLETE = "TransmittalComplete";

	//Transmittal attachment selection
	private static final String SELECTION_FILE = "ipl-transmittal-wizard.htm";
	
	//Tag-replace strings in document selection window.
	private static final String TABLE_DATA = "tableData";
	private static final String OTHER_ATTACHMENT_FIELD_TAG = "otherAttachmentField";
	private static final String DELIVERABLE_FIELD_TAG = "deliverableField";
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
	private static final String WPCL_HEAD_OFFICE = "HeadOffice01";
	private static final String WPCL_SITE_OFFICE = "SiteOffice02";
	private static final String WPCL_CEC = "ChennaiEngineeringOffice03";
	private static final String WPCL_DCPL = "DCPL04";
	private static final String LLC = "LCC05";
	private static final String PMC = "PMC06";
	private static final String THIRD_PARTY_INSPECTION = "ThirdPartyInspectionAuthority07";
	private static final String SEPCO_NEPDI = "NEPDI08";
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
		System.out.println("IPL Transmittal get request##############################");
		
		try {	
			handleGetRequest(aRequest, aResponse);
		}catch (DatabaseException e1) {
			LOG.error("Error while creating transmittal note:\n" + e1.getDescription());
			e1.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TBitsException e) {
			e.printStackTrace();
		} 
	}
	
	private static final String CREATE_TRANSMITTAL = "createTransmittal";
	/* (non-Javadoc)
	 * @see transbit.tbits.domain.TransmittalHandler#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */ 
	public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {		
		/*String requestType = aRequest.getParameter(REQUEST_TYPE);
		requestType = requestType.trim();		
		if (requestType.equalsIgnoreCase(PREVIEW))
			handlePreviewPostRequest(aRequest, aResponse);
		else if (requestType.equalsIgnoreCase(CREATE_TRANSMITTAL))*/
		handlePostRequest(aRequest, aResponse);
		
	}

	
	public void handleGetRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) 
									throws ServletException, IOException, DatabaseException, NumberFormatException, TBitsException {
		
		String requestType = aRequest.getParameter(REQUEST_TYPE);
		requestType = requestType.trim();
		
		if (requestType.equalsIgnoreCase(SELECTION)){
			aResponse.setContentType("text/html");
			//PrintWriter out = aResponse.getWriter();
			
			String dcrSysPrefix = aRequest.getParameter(DCR_BA);
			if ((dcrSysPrefix == null) || (dcrSysPrefix.trim().equals(IPLUtils.EMPTY_STRING) == true)) {
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
			if ((requestList == null) || requestList.trim().equals(IPLUtils.EMPTY_STRING)) {
				aResponse.getWriter().println("Empty Requests List.");
				return;
			}
			requestList = requestList.trim();		
						
			String trnType = aRequest.getParameter(TRANSMITTAL_TYPE);
			if((trnType == null) || (trnType.trim().equals(""))){
				aResponse.getWriter().println("Select appropriate Transmittal type. '" + trnType + "' is invalid.");
				return;
			}
			else{
				trnType = trnType.trim();
			}		
			
			DTagReplacer prepareTransmittal = null;
			File selectionFile = IPLUtils.getResourceFile(SELECTION_FILE);
			prepareTransmittal = new DTagReplacer(selectionFile);
			prepareTransmittal.replace(SYS_PREFIX, dcrSysPrefix);
			prepareTransmittal.replace(REQUEST_LIST, requestList);
			prepareTransmittal.replace(NEAREST_PATH, WebUtil.getNearestPath(aRequest, IPLUtils.EMPTY_STRING));
			prepareTransmittal.replace(USERS_LIST, ReportUtil.getJSONArrayOfUsers().toString());
			
			IPLTransmittalType transmittalType;
			Hashtable<String, String> tpParams;
			try {
				transmittalType = IPLTransmittalType.lookupTransmittalTypeBySystemIdAndName(dcrSystemId, trnType);
				tpParams = IPLTransmittalType.getTransmittalProcessParameters(dcrSystemId, transmittalType.getTransmittalTypeId());
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
			

			try{
				String actualDateProperty = PropertiesHandler.getProperty("transbit.tbits.transmittal.isActualDateEnabled");
				
				if((actualDateProperty != null) && Boolean.parseBoolean(actualDateProperty)){
					prepareTransmittal.replace("actualDateDisplay", "inline");
					prepareTransmittal.replace("actualDateImageDisplay", "inline");
				}
				else{				
					prepareTransmittal.replace("actualDateDisplay", "none");
					prepareTransmittal.replace("actualDateImageDisplay", "none");
				}
			}catch(IllegalArgumentException iae){
				LOG.warn("Property to enable actual date missing, hence ignoring that property and setting actual date field display to none.");
				prepareTransmittal.replace("actualDateDisplay", "none");
				prepareTransmittal.replace("actualDateImageDisplay", "none");
			}
			
			boolean isVTCEBA = IPLUtils.isExistsInString(IPLUtils.getProperty(IPLUtils.IPL_VENDOR_DTN_NUMBER_INFIX_BA_LIST), dcrBA.getSystemPrefix()); 
			boolean isTCEBA = IPLUtils.isExistsInString(IPLUtils.getProperty(IPLUtils.IPL_NON_VENDOR_DTN_NUMBER_INFIX_BA_LIST), dcrBA.getSystemPrefix());
			
			//Special TCE infix for DTN number generation.			
			if (isVTCEBA || isTCEBA){
				prepareTransmittal.replace(DTN_NUMBER_INFIX_DISPLAY, DISPLAY_INLINE);
				prepareTransmittal.replace(DTN_NUMBER_INFIX, "");
				if (isTCEBA){
					prepareTransmittal.replace("dtnNumberInfixName", SYSTEM_CODE);
					prepareTransmittal.replace("dtnNumberInfixOptions", IPLUtils.getFileContents(SYSTEM_CODES_HTM));
				}
				if (isVTCEBA){
					prepareTransmittal.replace("dtnNumberInfixName", TYPE_OF_DOCUMENT);
					prepareTransmittal.replace("dtnNumberInfixOptions", IPLUtils.getFileContents(TYPE_OF_DOCUMENT_HTM));
				}				
			}
			else{
				prepareTransmittal.replace(DTN_NUMBER_INFIX_DISPLAY, IPLUtils.STRING_NONE);
			}
			
			prepareTransmittal.replace(REFERENCE, "");
			String tableData = getTableData(dcrSystemId, requestList, tpParams);
			if (tableData == null){
				aResponse.getWriter().println ("Could not retrieve the documents included for transmittal.");
				return;
			}			
			prepareTransmittal.replace(TABLE_DATA, tableData);
			
			int deliverableFieldId = 0;
			try{
				String delFieldIdStr = tpParams.get(DELIVERABLE_FIELD_ID);
				if (delFieldIdStr == null)
					throw new NullPointerException("Could not find the appropriate deliverable(attachment) field id which is to be " +
							"used from the \"transmittal parameters list\".");
				else
					deliverableFieldId = Integer.parseInt(delFieldIdStr);
						
				Field deliverableField = Field.lookupBySystemIdAndFieldId(dcrSystemId, deliverableFieldId);
				if (deliverableField == null){
					throw new NullPointerException("Invalid field id provided. No deliverable field found with field_id= " + deliverableFieldId);
				}
				Field otherAttachmentsField = Field.lookupBySystemIdAndFieldName(dcrSystemId, Field.ATTACHMENTS);
				prepareTransmittal.replace(DELIVERABLE_FIELD_TAG, deliverableField.getDisplayName());
				prepareTransmittal.replace(OTHER_ATTACHMENT_FIELD_TAG, otherAttachmentsField.getDisplayName());
				prepareTransmittal.replace(TRANSMITTAL_PROCESS_NAME, transmittalType.getTransmittalProcess());
				prepareTransmittal.replace(DELIVERABLE_FIELD_ID, deliverableField.getFieldId() + "");
			}catch(NumberFormatException nfe){
				nfe.printStackTrace();
				aResponse.getWriter().print(nfe.getMessage());
				return;
			}
			
			aResponse.getWriter().print(prepareTransmittal.parse(dcrSystemId));
			return;
		}
		else if (requestType.equalsIgnoreCase(PREVIEW)){
			
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
			if ((dcrSysPrefix == null) || (dcrSysPrefix.trim().equals(IPLUtils.EMPTY_STRING) == true)) {
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
			
			String trnType = aRequest.getParameter(TRANSMITTAL_TYPE);
			if((trnType == null) || (trnType.trim().equals(""))){
				aResponse.getWriter().println("Select appropriate Transmittal type. '" + trnType + "' is invalid.");
				return;
			}
			else{
				trnType = trnType.trim();
			}
			
			IPLTransmittalType transmittalType;
			Hashtable<String, String> tpParams;
			try {
				transmittalType = IPLTransmittalType.lookupTransmittalTypeBySystemIdAndName(dcrSystemId, trnType);
				tpParams = IPLTransmittalType.getTransmittalProcessParameters(dcrSystemId, transmittalType.getTransmittalTypeId());
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DatabaseException("Database error occurred while retrieving transmittal type.", e);
			}
			
			String requestList = aRequest.getParameter(REQUEST_LIST);
			if ((requestList == null) || requestList.trim().equals(IPLUtils.EMPTY_STRING)) {
				aResponse.getWriter().println("Empty Requests List.");
				return;
			}
			requestList = requestList.trim();
			
			String approvalCategoryList = aRequest.getParameter(CAT_LIST);
			if((approvalCategoryList == null) || approvalCategoryList.trim().equals(IPLUtils.EMPTY_STRING)){
				aResponse.getWriter().println("Empty approval categories List.");
				return;
			}
			
			String copiesList = aRequest.getParameter(COPIES_LIST);
			if (copiesList == null)
				copiesList = "";
			else
				copiesList = copiesList.trim();			
						
			String quantityList = aRequest.getParameter (COPIES_LIST);
			if ((quantityList == null) || quantityList.trim().equals(IPLUtils.EMPTY_STRING)) {
				aResponse.getWriter().print("Please enter the quantity.");
				return;
			}			
			quantityList = quantityList.trim();
			
			String toList = aRequest.getParameter (TO_LIST);
			if ((toList == null) || toList.trim().equals(IPLUtils.EMPTY_STRING)){
				LOG.info("Distribution List was empty, hence ignoring it.");				
				toList = "";
			}
			toList = toList.trim();
			
			String ccList = aRequest.getParameter (CC_LIST);
			if ((ccList == null) || ccList.trim().equals(IPLUtils.EMPTY_STRING)){
				LOG.info("Distribution List was empty, hence ignoring it.");				
				ccList = "";
			}
			ccList = ccList.trim();

			String summaryList = aRequest.getParameter(SUMMARY_LIST);
			if((summaryList == null) || summaryList.trim().equals(IPLUtils.EMPTY_STRING)){
				summaryList = IPLUtils.EMPTY_STRING;
			}
			summaryList = summaryList.trim();			
			
			String reference = aRequest.getParameter(REFERENCE);
			if (reference == null)
				reference = "";
			else
				reference = reference.trim();
			
			String dtnNumberInfix = "";
			boolean isInfixRequiredBA = isInfixRequiredBusinessArea(dcrBA);
			if (isInfixRequiredBA){				
				dtnNumberInfix = aRequest.getParameter("dtnNumberInfix");
				if ((dtnNumberInfix == null) || dtnNumberInfix.trim().equals(""))
					dtnNumberInfix = "";
				else
					dtnNumberInfix = dtnNumberInfix.trim();
			}
			
			String transmittalSubject = aRequest.getParameter("transmittalSubject");
			if (transmittalSubject == null)
				transmittalSubject = "Transmittal Note";
			else
				transmittalSubject = transmittalSubject.trim();
			
			String remarks = aRequest.getParameter (REMARKS);
			if ((remarks == null) || remarks.trim().equals(IPLUtils.EMPTY_STRING)){	
				remarks = "";
			}
			remarks = remarks.trim();
			
			String transmittalDate = aRequest.getParameter("ActualDate");
			if ((transmittalDate == null) || transmittalDate.trim().equals("")){								
				transmittalDate = Timestamp.getGMTNow().toCustomFormat("dd-MMM-yyyy");
			}
								
			String trnIdPrefix = tpParams.get(IPLUtils.TRANSMITTAL_ID_PREFIX);
			
			//Add new infix string for TCE
			if (isInfixRequiredBA){
				//Get discipline code.
				String[] requestId = requestList.split(",");
				Request tRequest = Request.lookupBySystemIdAndRequestId(dcrSystemId, Integer.parseInt(requestId[0]));
				String disciplineCode = tRequest.getCategoryId().getDescription().trim();
				if (disciplineCode != null)
					trnIdPrefix = trnIdPrefix + "/" + disciplineCode;	
				//Attach infix provided by user
				trnIdPrefix = trnIdPrefix + "/" + dtnNumberInfix + "/";
				if (IPLUtils.isExistsInString(IPLUtils.getProperty(IPLUtils.IPL_NON_VENDOR_DTN_NUMBER_INFIX_BA_LIST), dcrBA.getSystemPrefix()))
					trnIdPrefix = trnIdPrefix + "VDT/";					
				//End of infix logic
			}
			
			if (dcrBA.getSystemPrefix().equals(AMR_LN_T) || dcrBA.getSystemPrefix().equals(NSK_LN_T)){
				//Get discipline code.
				String[] requestId = requestList.split(",");
				Request tRequest = Request.lookupBySystemIdAndRequestId(dcrSystemId, Integer.parseInt(requestId[0]));
				String disciplineCode = tRequest.getCategoryId().getDescription().trim();
				if ((disciplineCode != null) && (!disciplineCode.equals("")))
					trnIdPrefix = trnIdPrefix + "/" + disciplineCode.charAt(0);	
				trnIdPrefix = trnIdPrefix + "/" + "TR/";
			}		
			
			int transmittalMaxId = getTransmittalMaxId(transmittalType);
						
			String trnTemplateName = tpParams.get(TRANSMITTAL_TEMPLATE_NAME);
			if ((trnTemplateName == null) || (trnTemplateName.trim().equals(""))){
				throw new TBitsException("Invalid template(.rptdeisgn) file name.");
			}
			
			trnIdPrefix = trnIdPrefix + IPLUtils.getFormattedStringFromNumber(transmittalMaxId + 1, 3);
			if (dcrBA.getSystemPrefix().equals(AMR_LN_T) || dcrBA.getSystemPrefix().equals(NSK_LN_T)){
				trnIdPrefix = trnIdPrefix + "/KCS";
			}
				
			//int dcrSystemId, String requestList, String catList, String typeList, String qtyList, 
			//String transmittalId, String transmittalSubject, User user, String inwardDTNFieldName
			//TODO: Get subject value from subject field. 
			IPLTemplateHelper kth = IPLUtils.getKskTemplateHelper(dcrSystemId, requestList, approvalCategoryList, quantityList,
					summaryList, toList, ccList, trnIdPrefix + " {Likely}", transmittalSubject, remarks, user, INWARD_TRANSMITTAL_NO, 
					tpParams, reference, transmittalDate);		
			
			try {                                                
				htmlOS = IPLUtils.generateTransmittalNoteInHtml(aRequest, trnTemplateName, kth);
				if (htmlOS != null)
					aResponse.getWriter().println(htmlOS);
				else
					aResponse.getWriter().println("Could not generate the preview.");				
			} catch (EngineException e) {
				e.printStackTrace();
				aResponse.getWriter().println("Error occurred while generating preview. \nPlease contact tBits support.");
			}
		}
	}


	/**
	 * @param transmittalType
	 * @return
	 * @throws DatabaseException
	 * @throws TBitsException
	 */
	private int getTransmittalMaxId(IPLTransmittalType transmittalType)
			throws DatabaseException, TBitsException {
		Connection connection = null;	
		int transmittalMaxId = 0;
		try {								
			connection = DataSourcePool.getConnection();
			String queryString = "SELECT max_transmittal_id FROM ba_max_transmittal_ids WHERE transmittal_process_name = ?";
			PreparedStatement ps = connection.prepareStatement(queryString);
			ps.setString(1, transmittalType.getTransmittalProcess());
			ResultSet rs = ps.executeQuery();				
			if (rs.next()){
				transmittalMaxId = rs.getInt("max_transmittal_id");
			}
			rs.close();
			ps.close();
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
		return transmittalMaxId;
	}


	/**
	 * @param dcrBA
	 * @return
	 */
	private boolean isInfixRequiredBusinessArea(BusinessArea dcrBA) {
		return (IPLUtils.isExistsInString(IPLUtils.getProperty(IPLUtils.IPL_VENDOR_DTN_NUMBER_INFIX_BA_LIST), dcrBA.getSystemPrefix()) 
				|| IPLUtils.isExistsInString(IPLUtils.getProperty(IPLUtils.IPL_NON_VENDOR_DTN_NUMBER_INFIX_BA_LIST), dcrBA.getSystemPrefix()));
	}

	public void handlePreviewPostRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {

		aResponse.setContentType("text/html");
		try {
			user = WebUtil.validateUser(aRequest);		

			ByteArrayOutputStream htmlOS;
			String dcrSysPrefix = aRequest.getParameter(DCR_BA);
			if ((dcrSysPrefix == null) || (dcrSysPrefix.trim().equals(IPLUtils.EMPTY_STRING) == true)) {
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

			String trnType = aRequest.getParameter(TRANSMITTAL_TYPE);
			if((trnType == null) || (trnType.trim().equals(""))){
				aResponse.getWriter().println("Select appropriate Transmittal type. '" + trnType + "' is invalid.");
				return;
			}
			else{
				trnType = trnType.trim();
			}

			IPLTransmittalType transmittalType;
			Hashtable<String, String> tpParams;
			try {
				transmittalType = IPLTransmittalType.lookupTransmittalTypeBySystemIdAndName(dcrSystemId, trnType);
				tpParams = IPLTransmittalType.getTransmittalProcessParameters(dcrSystemId, transmittalType.getTransmittalTypeId());
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DatabaseException("Database error occurred while retrieving transmittal type.", e);
			}

			String requestList = aRequest.getParameter(REQUEST_LIST);
			if ((requestList == null) || requestList.trim().equals(IPLUtils.EMPTY_STRING)) {
				aResponse.getWriter().println("Empty Requests List.");
				return;
			}
			requestList = requestList.trim();

			String approvalCategoryList = aRequest.getParameter(CAT_LIST);
			if((approvalCategoryList == null) || approvalCategoryList.trim().equals(IPLUtils.EMPTY_STRING)){
				aResponse.getWriter().println("Empty approval categories List.");
				return;
			}

			String copiesList = aRequest.getParameter(COPIES_LIST);
			if (copiesList == null)
				copiesList = "";
			else
				copiesList = copiesList.trim();			

			String quantityList = aRequest.getParameter (COPIES_LIST);
			if ((quantityList == null) || quantityList.trim().equals(IPLUtils.EMPTY_STRING)) {
				aResponse.getWriter().print("Please enter the quantity.");
				return;
			}			
			quantityList = quantityList.trim();

			String toList = aRequest.getParameter (TO_LIST);
			if ((toList == null) || toList.trim().equals(IPLUtils.EMPTY_STRING)){
				LOG.info("Distribution List was empty, hence ignoring it.");				
				toList = "";
			}
			toList = toList.trim();

			String ccList = aRequest.getParameter (CC_LIST);
			if ((ccList == null) || ccList.trim().equals(IPLUtils.EMPTY_STRING)){
				LOG.info("Distribution List was empty, hence ignoring it.");				
				ccList = "";
			}
			ccList = ccList.trim();

			String summaryList = aRequest.getParameter(SUMMARY_LIST);
			if((summaryList == null) || summaryList.trim().equals(IPLUtils.EMPTY_STRING)){
				summaryList = IPLUtils.EMPTY_STRING;
			}
			summaryList = summaryList.trim();			

			String reference = aRequest.getParameter(REFERENCE);
			if (reference == null)
				reference = "";
			else
				reference = reference.trim();

			String dtnNumberInfix = "";
			boolean isInfixRequiredBA = isInfixRequiredBusinessArea(dcrBA);
			if (isInfixRequiredBA){
				dtnNumberInfix = aRequest.getParameter("dtnNumberInfix");
				if ((dtnNumberInfix == null) || dtnNumberInfix.trim().equals(""))
					dtnNumberInfix = "";
				else
					dtnNumberInfix = dtnNumberInfix.trim();
			}

			String transmittalSubject = aRequest.getParameter("transmittalSubject");
			if (transmittalSubject == null)
				transmittalSubject = "Transmittal Note";
			else
				transmittalSubject = transmittalSubject.trim();

			String remarks = aRequest.getParameter (REMARKS);
			if ((remarks == null) || remarks.trim().equals(IPLUtils.EMPTY_STRING)){	
				remarks = "";
			}
			remarks = remarks.trim();

			String trnIdPrefix = tpParams.get(IPLUtils.TRANSMITTAL_ID_PREFIX);
			//Add the infix string(C-TIB1-XXX-01-001) provided by the user for the DTN Number if its TPSC BA.
			//TODO: What about the remaining sub-string that has to be added after the infix and before the running serial number?
			/*if (IPLUtils.isExistsInString("TPSC", dcrBA.getSystemPrefix())){
				trnIdPrefix = trnIdPrefix + dtnNumberInfix + "-01-";
			}*/

			//Add new infix string for TCE			
			if (isInfixRequiredBA){
				//Get discipline code.
				String[] requestId = requestList.split(",");
				Request tRequest = Request.lookupBySystemIdAndRequestId(dcrSystemId, Integer.parseInt(requestId[0]));
				String disciplineCode = tRequest.getCategoryId().getDescription().trim();
				if (disciplineCode != null)
					trnIdPrefix = trnIdPrefix + "-" + disciplineCode;	
				//Attach infix provided by user
				trnIdPrefix = trnIdPrefix + "-" + dtnNumberInfix + "-";
				if (IPLUtils.isExistsInString(IPLUtils.getProperty(IPLUtils.IPL_VENDOR_DTN_NUMBER_INFIX_BA_LIST), dcrBA.getSystemPrefix()))
					trnIdPrefix = trnIdPrefix + "VDT-";					
				//End of infix logic
			}
			
			if (dcrBA.getSystemPrefix().equals(AMR_LN_T) || dcrBA.getSystemPrefix().equals(NSK_LN_T)){
				//Get discipline code.
				String[] requestId = requestList.split(",");
				Request tRequest = Request.lookupBySystemIdAndRequestId(dcrSystemId, Integer.parseInt(requestId[0]));
				String disciplineCode = tRequest.getCategoryId().getDescription().trim();
				if ((disciplineCode != null) && (!disciplineCode.equals("")))
					trnIdPrefix = trnIdPrefix + "/" + disciplineCode.charAt(0);		
				trnIdPrefix = trnIdPrefix + "/" + "TR/";
			}		
			
			String trnTemplateName = tpParams.get(TRANSMITTAL_TEMPLATE_NAME);
			if ((trnTemplateName == null) || (trnTemplateName.trim().equals(""))){
				throw new TBitsException("Invalid template(.rptdesign) file name.");
			}
			
			int transmittalMaxId = 0;
			Connection connection = null;	
			
			try {
				connection = DataSourcePool.getConnection();
				String queryString = "SELECT max_transmittal_id FROM ba_max_transmittal_ids WHERE transmittal_process_name = ?";
				PreparedStatement ps = connection.prepareStatement(queryString);
				ps.setString(1, transmittalType.getTransmittalProcess());
				ResultSet rs = ps.executeQuery();				
				if (rs.next()){
					transmittalMaxId = rs.getInt("max_transmittal_id");
				}
				rs.close();
				ps.close();
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
			
			//String formattedTrnId = IPLUtils.getFormattedStringFromNumber(transmittalMaxId + 1);
			trnIdPrefix = trnIdPrefix + IPLUtils.getFormattedStringFromNumber(transmittalMaxId + 1, 3);
			
			if (dcrBA.getSystemPrefix().equals(AMR_LN_T) || dcrBA.getSystemPrefix().equals(NSK_LN_T)){
				trnIdPrefix = trnIdPrefix + "/KCS";
			}
			
			String transmittalDate = "";
			//int dcrSystemId, String requestList, String catList, String typeList, String qtyList, 
			//String transmittalId, String transmittalSubject, User user, String inwardDTNFieldName
			//TODO: Get subject value from subject field. 
			IPLTemplateHelper kth = IPLUtils.getKskTemplateHelper(dcrSystemId, requestList, approvalCategoryList, quantityList,
					summaryList, toList, ccList, trnIdPrefix + " [Likely]", transmittalSubject, remarks, user, INWARD_TRANSMITTAL_NO, 
					tpParams, reference, transmittalDate);		

			try {                                                
				htmlOS = IPLUtils.generateTransmittalNoteInHtml(aRequest, trnTemplateName, kth);
				if (htmlOS != null)
					aResponse.getWriter().println(htmlOS);
				else
					aResponse.getWriter().println("Could not generate the preview.");				
			} catch (EngineException e) {
				e.printStackTrace();
				aResponse.getWriter().println("Error occurred while generating preview. \nPlease contact tBits support.");
			}

		} catch (TBitsException e2) {
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "TBitsException: \n" + "User validation unssuccessful");
			aResponse.getWriter().print(result.toString());
			return;
		} catch (DatabaseException e1) {
			e1.printStackTrace();
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "TBitsException: \n" + "Database Exception occurred during preview.");			
		}
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

			if ((dcrSysPrefix == null) || (dcrSysPrefix.trim().equals(IPLUtils.EMPTY_STRING))) {
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

			if ((requestList == null) || requestList.trim().equals(IPLUtils.EMPTY_STRING)) {
				out.print(KEYWORD_FALSE + IPLUtils.DELIMETER_COMMA +"No document selected to be sent to transmittal. Please select the documents to be sent");
				return;
			}
			requestList = requestList.trim();
			String[] dcrRequestList = requestList.split(IPLUtils.DELIMETER_COMMA);
			
			String docList = aRequest.getParameter (DRAWINGS_LIST);
			if ((docList == null) || docList.trim().equals(IPLUtils.EMPTY_STRING)) {
				out.print("Please select documents to be sent to transmittal");
				return;
			}			
			docList = docList.trim();
						
			String revList = aRequest.getParameter (REVISION_LIST);
			if ((revList == null) || revList.trim().equals(IPLUtils.EMPTY_STRING)) {
				out.print("Revision numbers list not provided. Please enter proper revision numbers");
				return;
			}			
			revList = revList.trim();
								
			String quantityList = aRequest.getParameter (COPIES_LIST);
			if ((quantityList == null) || quantityList.trim().equals(IPLUtils.EMPTY_STRING)) {
				out.print("Please enter the quantity.");
				return;
			}			
			quantityList = quantityList.trim();
			
			String approvalCategoryList = aRequest.getParameter (CAT_LIST);
			if ((approvalCategoryList == null) || approvalCategoryList.trim().equals(IPLUtils.EMPTY_STRING)) {
				out.print("No actions found for the selected documents. Please select appropriate actions");
				return;
			}			
			approvalCategoryList = approvalCategoryList.trim();
			
			String summaryList = aRequest.getParameter(SUMMARY_LIST);
			if((summaryList == null) || summaryList.trim().equals(IPLUtils.EMPTY_STRING)){
				summaryList = IPLUtils.EMPTY_STRING;
			}
			summaryList = summaryList.trim();
			/*String documentTypeList = aRequest.getParameter (TYPE_LIST);
			if ((documentTypeList == null) || documentTypeList.trim().equals(IPLUtils.EMPTY_STRING)) {
				out.print("Please enter the quantity.");
				return;
			}			
			documentTypeList = documentTypeList.trim();*/
			
			String deliverableList = aRequest.getParameter(DELIVERABLES_LIST);
			if ((deliverableList == null) || deliverableList.trim().equals(IPLUtils.EMPTY_STRING)) {
				LOG.warn("No deliverable attachments selected.");
				deliverableList = IPLUtils.EMPTY_STRING;
			}
			else{
				deliverableFileNames = deliverableList.split("<br2>");
			}
			
			String attachmentList = aRequest.getParameter(ATTACHMENT_LIST);
			if ((attachmentList == null) || attachmentList.trim().equals(IPLUtils.EMPTY_STRING)) {
				LOG.warn("No attachments selected.");
				attachmentList = IPLUtils.EMPTY_STRING;
			}
			else{
				reqFileNames = attachmentList.split("<br2>");//IPLUtils.DELIMETER_COMMA);
			}
			
			String toList = aRequest.getParameter(TO_LIST);
			if ((toList == null)||(toList.trim().equals(IPLUtils.EMPTY_STRING))){
				System.out.println ("No mailing address provided");
			}
			toList = toList.trim();
			
			String ccList = aRequest.getParameter(CC_LIST);
			if ((ccList ==null) || (ccList.trim().equals(IPLUtils.EMPTY_STRING))){
				ccList = IPLUtils.EMPTY_STRING;
			}
			else{
				ccList = ccList.trim();
			}
			
			String transmittalDate = aRequest.getParameter("ActualDate");
			if((transmittalDate == null) || transmittalDate.trim().equals("")){
				transmittalDate = Timestamp.getGMTNow().toCustomFormat("dd-MMM-yyyy");//yyyy-MM-dd");
			}
			transmittalDate = transmittalDate.trim();
			
			String remarks  = aRequest.getParameter(REMARKS);
			if ((remarks ==null) || (remarks.trim().equals(IPLUtils.EMPTY_STRING))){
				remarks = IPLUtils.EMPTY_STRING;			
			}
			else{
				remarks = remarks.trim();
			}
			
			String reference = aRequest.getParameter(REFERENCE);
			if (reference == null)
				reference = "";
			else
				reference = reference.trim();
			
			//Remove the hard-coding for the list of BA sys-prefixes used for identifying the BA's for whom the 
			//extra TPSC number field has to be considered.
			String dtnNumberInfix = "";
			boolean isInfixRequiredBA = isInfixRequiredBusinessArea(dcrBA);
			if (isInfixRequiredBA){
				dtnNumberInfix = aRequest.getParameter("dtnNumberInfix");
				if ((dtnNumberInfix == null) || dtnNumberInfix.trim().equals(""))
					dtnNumberInfix = "";
				else
					dtnNumberInfix = dtnNumberInfix.trim();
			}			
			
			String transmittalSubject = aRequest.getParameter(TRANSMITTAL_SUBJECT);
			if ((transmittalSubject == null) || (transmittalSubject.trim().equals(IPLUtils.EMPTY_STRING))){
				transmittalSubject = IPLUtils.EMPTY_STRING;		
				LOG.info("No email body found for transmittal process");
			}
			else{
				transmittalSubject = transmittalSubject.trim();
			}
			
			String emailBody = aRequest.getParameter(EMAIL_BODY);
			if ((emailBody == null) || (emailBody.trim().equals(IPLUtils.EMPTY_STRING))){
				emailBody = IPLUtils.EMPTY_STRING;		
				LOG.info("No email body found for transmittal process");
			}
			else{
				emailBody = emailBody.trim();
			}
						
			String transmittalProcess = aRequest.getParameter(TRANSMITTAL_PROCESS_NAME);			
			if ((transmittalProcess == null) || (transmittalProcess.trim().equals(IPLUtils.EMPTY_STRING))){
				LOG.fatal("Could not find corresponding transmittal process. Please contact admin/tBits support team.");
			}
			else 
				transmittalProcess = transmittalProcess.trim();
			
			String trnTypeName = aRequest.getParameter(TRANSMITTAL_TYPE);
			if((trnTypeName == null) || (trnTypeName.trim().equals(""))){
				aResponse.getWriter().println("Select appropriate Transmittal type. '" + trnTypeName + "' is invalid.");
				return;
			}
			else{
				trnTypeName = trnTypeName.trim();
			}
			
			IPLTransmittalType transmittalType;
			Hashtable<String, String> tpParams;
			try {
				transmittalType = IPLTransmittalType.lookupTransmittalTypeBySystemIdAndName(dcrBA.getSystemId(), trnTypeName);
				tpParams = IPLTransmittalType.getTransmittalProcessParameters(dcrBA.getSystemId(), transmittalType.getTransmittalTypeId());
				if (tpParams == null){
					throw new TBitsException("Transmittal process parameters not found. Hence cannot continue.");
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DatabaseException("Database error occurred while retrieving transmittal type.", e);
			}
			
			//int[] mappedSysIds;
			int trnSystemId = transmittalType.getDtnSysId();
			/*try {
				mappedSysIds = IPLUtils.getTransmittalSystemId(dcrBA.getSystemId(), transmittalType.getTransmittalTypeId());
				trnSystemId = mappedSysIds[0];
				
			} catch (SQLException e2) {
				e2.printStackTrace();
				throw new DatabaseException("Error occurred while retrieving transmittal id.", e2);
			}*/
			
			if (trnSystemId == 0) {
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, "No corresponding Transmittal Business Area found for the business area: " + dcrBA.getDisplayName());
				out.print(result.toString());				
				return;
			} 
				 
			BusinessArea transBA = BusinessArea.lookupBySystemId(trnSystemId);		

			if (transBA == null) {
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, trnSystemId + ": Invalid transmittal Business Area Id or business area does not exist");
				out.print(result.toString());				
				return;
			}
			
			
			String deliverableFieldIdStr = aRequest.getParameter("deliverableFieldId");
			if ((deliverableFieldIdStr == null) || (deliverableFieldIdStr.trim().equals("")))
				throw new TBitsException("Could not find deliverable field type.");
			else 
				deliverableFieldIdStr = deliverableFieldIdStr.trim();
			deliverableFieldId = Integer.parseInt(deliverableFieldIdStr);
						
			String formattedTrnReqId = IPLUtils.EMPTY_STRING;
			String transmitAttachments = IPLUtils.EMPTY_STRING;			
			String trnIdPrefix = tpParams.get(IPLUtils.TRANSMITTAL_ID_PREFIX);
			if ((trnIdPrefix == null) || (trnIdPrefix.trim().equals(""))){
				throw new TBitsException("Invalid transmittal number prefix, please contact admin or tBits support.");
			}
						
			//Add new infix string for TCE			
			if (isInfixRequiredBA){
				//Get discipline code.
				String[] requestId = requestList.split(",");
				Request tRequest = Request.lookupBySystemIdAndRequestId(dcrBA.getSystemId(), Integer.parseInt(requestId[0]));
				String disciplineCode = tRequest.getCategoryId().getDescription().trim();
				if (disciplineCode != null)
					trnIdPrefix = trnIdPrefix + "/" + disciplineCode;	
				//Attach infix provided by user
				trnIdPrefix = trnIdPrefix + "/" + dtnNumberInfix + "/";
				if (IPLUtils.isExistsInString(IPLUtils.getProperty(IPLUtils.IPL_NON_VENDOR_DTN_NUMBER_INFIX_BA_LIST), dcrBA.getSystemPrefix()))
					trnIdPrefix = trnIdPrefix + "VDT/";					
				//End of infix logic
			}
			
			if (dcrBA.getSystemPrefix().equals(AMR_LN_T) || dcrBA.getSystemPrefix().equals(NSK_LN_T)){
				//Get discipline code.
				String[] requestId = requestList.split(",");
				Request tRequest = Request.lookupBySystemIdAndRequestId(dcrBA.getSystemId(), Integer.parseInt(requestId[0]));
				String disciplineCode = tRequest.getCategoryId().getDescription().trim();
				if ((disciplineCode != null) && (!disciplineCode.equals("")))
					trnIdPrefix = trnIdPrefix + "/" + disciplineCode.charAt(0);		
				trnIdPrefix = trnIdPrefix + "/" + "TR/";
			}		
						
			String linkedRequests = IPLUtils.getLinkedRequests(dcrBA, dcrRequestList);
			Hashtable <String,String> aParamTable = new Hashtable<String, String>();
			try {
				maxIdConn = DataSourcePool.getConnection();
				maxIdConn.setAutoCommit(false);
				//System.out.println("#### After connection for max Id."); 
				transReqId = IPLTransmittalType.getMaxTransmittalNumber(maxIdConn, transBA.getSystemId(), transmittalProcess);
				
				if (isInfixRequiredBA){
					formattedTrnReqId = IPLUtils.getFormattedStringFromNumber(transReqId, 3);	
				}
				else
					formattedTrnReqId = IPLUtils.getFormattedStringFromNumber(transReqId, 5);
				
				trnIdPrefix = trnIdPrefix + formattedTrnReqId;				
				if (dcrBA.getSystemPrefix().equals(AMR_LN_T) || dcrBA.getSystemPrefix().equals(NSK_LN_T)){
					trnIdPrefix = trnIdPrefix + "/KCS";
				}
				
				//int dcrSystemId, String requestList, String catList, String typeList, String qtyList, 
				//String transmittalId, String transmittalSubject, User user, String inwardDTNFieldName
				IPLTemplateHelper kth = IPLUtils.getKskTemplateHelper(dcrBA.getSystemId(), requestList, approvalCategoryList, 
						quantityList, summaryList, toList, ccList, trnIdPrefix, transmittalSubject, remarks,
						user, INWARD_TRANSMITTAL_NO, tpParams, reference, transmittalDate);
				
				//TODO: Transmittal template name, handle null-pointer exception.
				String templateName = tpParams.get(TRANSMITTAL_TEMPLATE_NAME);
				if ((templateName == null) || (templateName.trim().equals(""))){
					throw new TBitsException("Please provide appropriate DTN template for generating transmittal note.");
				}
				transmitAttachments = getDTNAttachment(kth, templateName);

				//Create Connection, FileResourceManager and MailResourceManager.
				connection = DataSourcePool.getConnection();
				connection.setAutoCommit(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
				throw new DatabaseException("Error occurred while fetching database connection.", e1);
			} catch (EngineException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			}
			
			//Create new AddRequest object to add new transmittal.
			AddRequest addReq = new AddRequest();
			addReq.setContext(aRequest.getContextPath());
			addReq.setSource(TBitsConstants.SOURCE_CMDLINE);
			aParamTable.put(Field.BUSINESS_AREA, transBA.getSystemPrefix());
			//aParamTable.put(Field.USER, "root");
			String dtnLogger = tpParams.get("dtnLogger");
			if ((dtnLogger == null) || (dtnLogger.trim().equals(""))){
				LOG.warn("No specific DTN logger name provided, hence considering the current logger creating the transmittal.");
				dtnLogger = user.getUserLogin().trim();
			}
			
			aParamTable.put(Field.USER, dtnLogger.trim());
			aParamTable.put(Field.REQUEST, transReqId + IPLUtils.EMPTY_STRING);			
			aParamTable.put(Field.DESCRIPTION, "Transmittal for documents: " + linkedRequests); 
			aParamTable.put(Field.SUBJECT, transmittalSubject);
			
			aParamTable.put(Field.ASSIGNEE, toList);
			aParamTable.put(Field.SUBSCRIBER, ccList);	
			aParamTable.put(Field.STATUS, DTN_STATUS_TRANSMITTAL_COMPLETE);			
			aParamTable.put(Field.ATTACHMENTS, getTransmittalAttachments(reqFileNames));
			aParamTable.put(Field.IS_PRIVATE, IPLUtils.TRUE);
			
			aParamTable.put(COMMENTED_FILES, getTransmittalAttachments(deliverableFileNames));
			aParamTable.put(DTN_FILE, transmitAttachments);
			aParamTable.put(Field.NOTIFY, KEYWORD_TRUE);
			aParamTable.put(DTN_NUMBER, trnIdPrefix);
			
			String logDate = "";
			if((transmittalDate == null) || transmittalDate.trim().equals("")){
				logDate = Timestamp.getGMTNow().toCustomFormat(TBitsConstants.API_DATE_FORMAT);//"yyyy-MM-dd HH:mm:ss");			
			}
			else {
				DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");//yyyy-MM-dd");
				Date d = df.parse(transmittalDate);
				logDate = Timestamp.toCustomFormat(d, TBitsConstants.API_DATE_FORMAT);//"yyyy-MM-dd HH:mm:ss");				
			}
			aParamTable.put(Field.LOGGED_DATE, logDate);
			aParamTable.put(Field.LASTUPDATED_DATE, logDate);
			
			String originatingFirm = tpParams.get(ORIGINATOR);
			if ((originatingFirm == null) || (originatingFirm.trim().equals(""))){
				throw new TBitsException("Invalid originating firm.");
			}
			
			aParamTable.put(Field.CATEGORY, originatingFirm);
			String recipientFirm = tpParams.get(RECIPIENT);
			if ((recipientFirm == null) || (recipientFirm.trim().equals("")))
				throw new TBitsException("Invalid recipient firm.");
			aParamTable.put(Field.REQUEST_TYPE, recipientFirm);
			
			/*
			 * 1. Connection Object 
			 * 2. Transmittal Number
			 * 3. DCR business area
			 * 4. DCR request list
			 */
			runPreTransmittalRules(connection, transReqId, aParamTable, tpParams, dcrBA, dcrRequestList, trnTypeName, true);
					
			StringBuffer desSB = new StringBuffer();
			desSB.append(emailBody);
			if ((emailBody!= null) && (!emailBody.equals(IPLUtils.EMPTY_STRING))){
				//desSB.append("\n\nDocument Control Register references: " + linkedRequests);
			}
			
			aParamTable.put(Field.DESCRIPTION,  desSB.toString());
			Request trnRequest = addReq.addRequest(connection, tBitsResMgr, aParamTable);
						
			for (int index = 0;index < dcrRequestList.length; index++){
				UpdateRequest dcrUpdateRequest = new UpdateRequest();
				
				// Update the drawings sent to transmittal by changing the status and description. 
				Hashtable <String,String> tempParamTable = new Hashtable<String, String>();
				int dcrSysId = dcrBA.getSystemId();
				tempParamTable.put (Field.BUSINESS_AREA,  + dcrSysId + IPLUtils.EMPTY_STRING);
				tempParamTable.put (Field.REQUEST, dcrRequestList[index]);
				tempParamTable.put(Field.USER, "root");
				if ((logDate != null) && (!logDate.trim().equals(""))) 
					tempParamTable.put (Field.LASTUPDATED_DATE, logDate);
								
				int dcrReqId = Integer.parseInt(dcrRequestList[index]);
				Request dcrRequest = Request.lookupBySystemIdAndRequestId(connection, dcrSysId, dcrReqId);
				
				updatePostTransmittalFieldValues(connection, dcrSysId, dcrRequest, dcrSysId, transmittalType.getTransmittalTypeId(),
						trnIdPrefix, transBA.getSystemPrefix(), trnRequest.getRequestId(), tempParamTable, false);
				
				String outwardDTNFieldIdStr = tpParams.get("dtnFieldId");
				if ((outwardDTNFieldIdStr == null) || outwardDTNFieldIdStr.trim().equals(""))
					LOG.warn("Skipped populating outgoing DTN field as the appropriate field Id for the field was not found.");
				else{
					outwardDTNFieldIdStr = outwardDTNFieldIdStr.trim();
					try{
						Field dcrDTNField = Field.lookupBySystemIdAndFieldId(dcrSysId, Integer.parseInt(outwardDTNFieldIdStr));
						if (dcrDTNField == null)
							throw new NullPointerException("Invalid field id provided through transmittal parameter, dtnFielId: " + outwardDTNFieldIdStr);
						tempParamTable.put(dcrDTNField.getName(), trnIdPrefix);
					}catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						throw new TBitsException(nfe);
					}
				}
				
				runTransmittalRules(connection, transReqId, trnRequest, dcrBA, dcrBA, dcrRequest, 
										tempParamTable, trnTypeName, IPLUtils.DCR_BUSINESS_AREA, false);
				try{
					dcrUpdateRequest.updateRequest(connection, tBitsResMgr, tempParamTable);
				}catch (NullPointerException npe){
					throw new TBitsException(npe);
				}
			}						
			
			int dtrSysId = transmittalType.getDtrSysId();
			
			try {
				Hashtable<Integer, Integer> dtrRequestsMap = new Hashtable<Integer, Integer>();
				Hashtable<Integer, Integer> iDtrRequestsMap = new Hashtable<Integer, Integer>();
				Hashtable<Integer, Hashtable<Integer, Integer>> otherBARequestsMap = new Hashtable<Integer, Hashtable<Integer,Integer>>();
				
				ArrayList<Integer> targetBAList = getTargetBusinessAreas(connection, dcrBA.getSystemId());
				for (Integer targetBAId : targetBAList){
					//int dtrSysId = mappedSysIds[1];					
					String mappedBizAreas = tpParams.get(MAPPED_BUSINESS_AREAS);
					if ((mappedBizAreas == null) || (mappedBizAreas.trim().equals("")))
						throw new TBitsException("Invalid mapped business areas.");
					String[] mappedBAList = mappedBizAreas.split(IPLUtils.DELIMETER_COMMA);
					for (String tempIdStr : mappedBAList){
						int tempId = 0;
						if (!tempIdStr.trim().equals("")){
							tempId = Integer.parseInt(tempIdStr);
							if (targetBAId == tempId)
								updateTargetBA(connection, trnRequest, trnIdPrefix, dcrBA, targetBAId, dcrRequestList, transmittalType,
										tBitsResMgr, aRequest.getContextPath(), dtrSysId, dtrRequestsMap, iDtrRequestsMap,
										otherBARequestsMap, logDate);
						}
					}					
				}
				
				//Insert all other required mappings of DTR request with other BA request Ids involved in this transmittal process.
				addOtherMappingsForDTRAndIDTRBA(connection, dcrBA, transmittalType, dtrSysId,
						dtrRequestsMap, otherBARequestsMap, false);
				addOtherMappingsForDTRAndIDTRBA(connection, dcrBA, transmittalType, 9,
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
		} catch (ParseException e) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "TBits Exception: \n" + e.getMessage());
			out.print(result.toString());
			e.printStackTrace();
		}catch (APIException apie) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr); 
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "Permission Exception: \n" + apie.getMessage());
			out.print(result.toString());
			apie.printStackTrace();
			return;							
		}catch (NullPointerException npe) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr); 
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "TBits Exception: \n" + npe.getMessage());
			out.print(result.toString());
			npe.printStackTrace();
			return;							
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


	private void addOtherMappingsForDTRAndIDTRBA(Connection connection, BusinessArea dcrBA,
			IPLTransmittalType transmittalType, int dtrSysId, Hashtable<Integer, Integer> dtrRequestsMap,
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

	private void updateTargetBA (Connection connection, Request transmittalRequest, String formattedDtnNumber, BusinessArea dcrBA, int targetBASysId, String[] reqNo, 
									IPLTransmittalType transmittalType, TBitsResourceManager tBitsResMgr, String contextPath, int dtrSystemId,
									Hashtable<Integer, Integer> dtrRequestsMap, Hashtable<Integer, Integer>iDtrRequestsMap,
									Hashtable<Integer, Hashtable<Integer, Integer>> otherRequestsMap, String logDate) 
								throws DatabaseException, TBitsException, APIException, SQLException{
		
		boolean isMatched = false;
		int dcrSysId = dcrBA.getSystemId();
		
		for (int i=0; i<reqNo.length; i++){
			
			LOG.info("Updating target business area with Id: " + targetBASysId + ", post transmittal.");
			System.out.println("##########################Target BA sysId: " + targetBASysId);
			isMatched = false;
			
			int baType = IPLUtils.OTHER_BUSINESS_AREA;
			if(dtrSystemId == targetBASysId){
				baType = IPLUtils.DTR_BUSINESS_AREA;			
			}
			
			Request dcrRequest = Request.lookupBySystemIdAndRequestId (connection, dcrSysId, Integer.parseInt(reqNo[i].trim()));
			int dcrRequestId = dcrRequest.getRequestId();
			String dNumValue = dcrRequest.get(IPLUtils.FIELD_DRAWING_NO);
			
			//Get the current BA.
			BusinessArea currentBA = IPLUtils.lookupBySystemId(connection, targetBASysId);
			boolean isIDTRMapping = false;
			// Remove the hard coding, once the current design for mapping is accepted.
			if (currentBA.getSystemId() == 9){
				isIDTRMapping = true;
			}
			
			String dtnSysPrefix = BusinessArea.lookupBySystemId(transmittalRequest.getSystemId()).getSystemPrefix();
			
			//Field dNumField = IPLUtils.lookupBySystemIdAndFieldName(connection, targetBASysId, IPLUtils.FIELD_DRAWING_NO);
			Hashtable <String,String> aParamTable = new Hashtable<String, String>();
						
			Request matchedRequest = null;				 

			try{				
				CallableStatement cs = connection.prepareCall("stp_get_matched_request_id ?, ?, ?, ?, ?");
				cs.setInt(1, dcrSysId);				
				cs.setInt(2, dcrRequestId);
				cs.setInt(3, targetBASysId);
				
				if(isIDTRMapping || (targetBASysId != dtrSystemId)){
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
							System.out.println("Found a match....." + matchedRequestId);
							isMatched = true;
							
							BusinessArea targetBA = IPLUtils.lookupBySystemId(connection, targetBASysId);
							aParamTable.put(Field.BUSINESS_AREA, targetBA.getSystemPrefix());
							aParamTable.put(Field.REQUEST, matchedRequest.getRequestId() + IPLUtils.EMPTY_STRING);							
														
							//Update deliverable attachment field.
							Field deliverableField = IPLUtils.lookupBySystemIdAndFieldId(connection, dcrSysId, deliverableFieldId);
							String delPrevAttachmentEx = AttachmentUtils.getAttachmentEx(connection, matchedRequest, deliverableFieldId);
							
							Collection<AttachmentInfo> prevDelAttachments = null;
							if ((delPrevAttachmentEx != null) && (!delPrevAttachmentEx.trim().equals(IPLUtils.EMPTY_STRING)))
								prevDelAttachments = AttachmentInfo.fromJson(delPrevAttachmentEx);
							else
								prevDelAttachments = null;
							
							Collection<AttachmentInfo> selectedDelAttachments = IPLUtils.getSelectedAttachments(dcrRequest, deliverableFileNames, i);
							IPLUtils.mergeAttachmentsLists(selectedDelAttachments, prevDelAttachments);
							String delAttachments = "[]";
							if (selectedDelAttachments != null)
								delAttachments = AttachmentInfo.toJson(selectedDelAttachments);
							
							//Update other attachments field.
							Collection<AttachmentInfo> prevAttachments = matchedRequest.getAttachments();
							Collection<AttachmentInfo> selectedAttachments = IPLUtils.getSelectedAttachments(dcrRequest, reqFileNames, i);							
							IPLUtils.mergeAttachmentsLists(selectedAttachments, prevAttachments);							
							String sAttachments = "[]";
							if(selectedAttachments != null)
								sAttachments = AttachmentInfo.toJson(selectedAttachments);
														
							UpdateRequest updateRequest = new UpdateRequest();
							updateRequest.setContext(contextPath);
							updateRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
							
							aParamTable.put(Field.ATTACHMENTS, sAttachments);
							aParamTable.put(deliverableField.getName(), delAttachments);
							if ((logDate != null) && (!logDate.trim().equals("")))
								aParamTable.put (Field.LASTUPDATED_DATE, logDate);
							
							updateFields(connection, dcrSysId, dcrRequest, targetBASysId, delAttachments, sAttachments, aParamTable);
							//updateFieldsBytransmittalTypeId(connection, dcrSysId, dcrRequest, targetBASysId, transmittalType.getTransmittalTypeId(), aParamTable);
							
							updatePostTransmittalFieldValues(connection, dcrSysId, dcrRequest, targetBASysId, transmittalType.getTransmittalTypeId(),
									formattedDtnNumber, dtnSysPrefix, transmittalRequest.getRequestId(), aParamTable, false);
							
							runTransmittalRules(connection, transReqId, transmittalRequest, BusinessArea.lookupBySystemId(targetBASysId),
									dcrBA, dcrRequest, aParamTable, transmittalType.getName(), baType, false);
							
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
			} catch (NullPointerException npe){
				npe.printStackTrace();
				throw new TBitsException(npe);
			}
			
			if (!isMatched){

				System.out.println("No matching request exist, hence adding new...... " + i);	
				//updateFields(connection, dcrSysId, dcrRequest, targetBASysId, delAttachments, sAttachments, aParamTable);
				try{
					AddRequest addRequest = new AddRequest();            	           	
					addRequest.setContext(contextPath);
					addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);

					aParamTable.put(Field.BUSINESS_AREA, currentBA.getSystemPrefix());
					//TODO: Change the logger based on the document.
					aParamTable.put(Field.USER, "root");				
					aParamTable.put(Field.SUBJECT, dcrRequest.getSubject());
					if ((logDate != null) && (!logDate.trim().equals(""))){
						aParamTable.put (Field.LOGGED_DATE, logDate);
						aParamTable.put (Field.LASTUPDATED_DATE, logDate);
					}

					Collection<AttachmentInfo> selectedDelAttachments = IPLUtils.getSelectedAttachments(dcrRequest, deliverableFileNames, i);
					String delAttachments = "";
					if (selectedDelAttachments != null)
						delAttachments = AttachmentInfo.toJson(selectedDelAttachments);				

					//Update other attachments field.
					Collection<AttachmentInfo> selectedAttachments = IPLUtils.getSelectedAttachments(dcrRequest, reqFileNames, i);				
					String sAttachments = "[]";
					if(selectedAttachments != null)
						sAttachments = AttachmentInfo.toJson(selectedAttachments);

					aParamTable.put(Field.ATTACHMENTS, sAttachments);				
					Field dField = IPLUtils.lookupBySystemIdAndFieldId(connection, dcrSysId, deliverableFieldId);
					aParamTable.put(dField.getName(), delAttachments);

					//Finally add the request
					runTransmittalRules(connection, transReqId, transmittalRequest, currentBA, dcrBA, dcrRequest, aParamTable,
							transmittalType.getName(), baType, true);
					updateFields(connection, dcrSysId, dcrRequest, targetBASysId, delAttachments, sAttachments, aParamTable);
					//updateFieldsBytransmittalTypeId(connection, dcrSysId, dcrRequest, targetBASysId, transmittalType.getTransmittalTypeId(), aParamTable);

					updatePostTransmittalFieldValues(connection, dcrSysId, dcrRequest, targetBASysId, transmittalType.getTransmittalTypeId(),
							formattedDtnNumber, dtnSysPrefix, transmittalRequest.getRequestId(), aParamTable, true);

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
				}catch(NullPointerException npe){
					throw new TBitsException(npe);
				}
			}
		}
	}
	
	private void updateFields(Connection connection, int dcrSystemId, Request dcrRequest, int targetSystemId,String deliverableAttachments, 
								String otherAttachments, Hashtable<String, String>aParamTable) throws DatabaseException{
		// If its SEPCO BA delete all previous attachments in extended field attachment types.
		//clearAttachments(connection, dcrSystemId, dcrRequest, targetSystemId, aParamTable);
		Hashtable<String,String> targetBAFields = IPLUtils.getTargetBusinessAreaFields(connection, dcrSystemId, targetSystemId);
		for(String fieldName : targetBAFields.keySet()){
			String tFieldName = targetBAFields.get(fieldName);
			String dcrFieldValue = dcrRequest.get(fieldName);
			aParamTable.put(tFieldName, dcrFieldValue);
		}		
	}
		
	private void updatePostTransmittalFieldValues(Connection connection, int dcrSystemId, Request dcrRequest, int targetSystemId, 
			int transmittalTypeId, String formattedDTNNumber, String dtnSysPrefix, int dtnRequestId, Hashtable<String, String> aParamTable, 
			boolean isAddRequest) throws DatabaseException, TBitsException{
		IPLUtils.getTargetBusinessAreaFieldsAndValues(connection, dcrSystemId, dcrRequest, targetSystemId, transmittalTypeId, 
				formattedDTNNumber, dtnSysPrefix, dtnRequestId, isAddRequest, aParamTable);	
	}
		
	private String getTransmittalAttachments(String[] reqFileNames) 
					throws NumberFormatException, DatabaseException {
		StringBuilder tempAttachments = new StringBuilder();
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>();
				 
		if (reqFileNames != null)
			for (int i=0; i<reqFileNames.length; i++){
				String reqFileList = reqFileNames[i];
				if (reqFileList.trim().equals(IPLUtils.EMPTY_STRING))
					continue;
				else{					
					for (String reqFileInfo : reqFileList.split("<br3>")){//IPLUtils.DELIMETER_SEMICOLON)){
						String[] reqAttInfo = reqFileInfo.split("<br1>");//":");
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
					
			insertQuery = insertQuery + " INSERT INTO request_mapping (sys_id_1, request_id_1, sys_id_2, " +
					"request_id_2, party_sys_id_1, " +
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
		for (String requestIdStr : requestList.split(IPLUtils.DELIMETER_COMMA)) {				
			Request request = Request.lookupBySystemIdAndRequestId(aSystemId, Integer.parseInt(requestIdStr));
			
			JSONObject obj = new JSONObject();
			String docNo = request.get (IPLUtils.FIELD_DRAWING_NO);
			if ((docNo == null) || (docNo.trim().equals(IPLUtils.EMPTY_STRING)))
				return null;
			
			int requestId = request.getRequestId();
			
			obj.put(Field.REQUEST, requestId);
			obj.put(DOC_NO,docNo);
			obj.put(REV_NO, request.get(IPLUtils.REVISION));
			obj.put(DESP,request.getSubject());
			
			String defaultAppCategory = "";
			if ((transmittalParams != null) && (!transmittalParams.isEmpty())){
				String delFieldIdStr = transmittalParams.get(DELIVERABLE_FIELD_ID);
				if ((delFieldIdStr == null) || (delFieldIdStr.trim().equals("")))
					throw new TBitsException("Could not find deliverable(attachment) field id.");
				try{
					int delAttFieldId = Integer.parseInt(delFieldIdStr);
					obj.put(DELIVERABLES, AttachmentUtils.getAttachmentList(aSystemId, request, delAttFieldId, IPLUtils.REVISION));
					defaultAppCategory = transmittalParams.get("defaultApprovalCategory");
				}catch(NumberFormatException nfe){
					throw new TBitsException("Invalid deliverable(attachment) field id.", nfe);
				}
			}
			else 
				obj.put(DELIVERABLES, "[]");
				
			obj.put(ATTACHMENTS, AttachmentUtils.getAttachmentList(aSystemId, request, IPLUtils.REVISION));		
			obj.put(SHEETS, "-");
			//obj.put(TRANSMITTAL_DOC_TYPE, "SC");			
			obj.put(TRANSMITTAL_APP_CATEGORY, defaultAppCategory);
			obj.put(SUMMARY, request.getSummary());
			reqArray.add(obj);
		}
		return reqArray.toString();
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
	 */
	private String getDTNAttachment (IPLTemplateHelper kth, String templateName) throws DatabaseException, 
			FileNotFoundException,IOException, EngineException{
		
		StringBuilder tempAttachments = new StringBuilder();
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>();		
		String pdfFilePath = IPLUtils.generateTransmittalNoteUsingBirt(templateName, kth, "Transmittal-Note");	
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
	private void runTransmittalRules(Connection connection, int transmittalId, Request transmittalRequest, 
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest, Hashtable <String,String> paramTable,
			String transmittalType, int businessAreaType, boolean isAddRequest)
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
        	trnRuleLoader.process(connection, transmittalId, transmittalRequest, currentBA, dcrBA, dcrRequest, paramTable, 
        			transmittalType, businessAreaType, isAddRequest);
        }
	}
	
	/**
	 * This methods runs various preLoader. It essentials is run just before rendering.
	 * @param businessAreaType 
	 * @param aRequest
	 * @param aResponse
	 * @param tagTable
	 * @throws TBitsException 
	 */
	
	private void runPreTransmittalRules(Connection connection, int transmittalRequestId,
			Hashtable<String, String> dtnRequestParamTable, Hashtable<String, String> transmittalProcessParams,
			BusinessArea dcrBA, String[] dcrRequestList, String transmittalType, boolean isAddRequest) throws TBitsException {
		PluginManager pm = PluginManager.getInstance();
        ArrayList<Class> transmittalRuleClasses = pm.findPluginsByInterface(IPreTransmittalRule.class.getName());
        ArrayList<IPreTransmittalRule> preTransmittalRuleLoaders = new ArrayList<IPreTransmittalRule>();
        if(transmittalRuleClasses != null)
        {
        	for(Class preTransmittalRuleClass:transmittalRuleClasses)
        	{
        		IPreTransmittalRule preTransmittalRule;
				try {
					preTransmittalRule = (IPreTransmittalRule) preTransmittalRuleClass.newInstance();
					preTransmittalRuleLoaders.add(preTransmittalRule);
				} catch (InstantiationException e) {
					LOG.error("Could not instantiate the pre renderer class: " + preTransmittalRuleClass.getClass().getName());
				} catch (IllegalAccessException e) {
					LOG.error("Could not access the renderer class: " + preTransmittalRuleClass.getClass().getName());
				}
        	}
        }
        
        Comparator<IPreTransmittalRule> c = new Comparator<IPreTransmittalRule>(){

			public int compare(IPreTransmittalRule arg0, IPreTransmittalRule arg1) {
				double diff = arg0.getSequence() - arg1.getSequence();
				if(diff > 0)
					return 1;
				else if(diff == 0)
					return 0;
				else 
					return -1;
			}
		};
		Collections.sort(preTransmittalRuleLoaders, c);
		
        for(IPreTransmittalRule trnPreRuleLoader:preTransmittalRuleLoaders)
        {
        	trnPreRuleLoader.process(connection, transmittalRequestId, dcrBA, dcrRequestList, 
        			dtnRequestParamTable, transmittalProcessParams, transmittalType, isAddRequest);
        }
	}
	
	public static void main(String[] args){
		IPLTransmittalPlugin iplPlugin = new IPLTransmittalPlugin();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			iplPlugin.insertIntoRequestMapping(connection , 4, 3, 3, 4, 4, 3, false);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		
	}
}


/*else if (requestType.equalsIgnoreCase(PREVIEW)){
	
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
	if ((dcrSysPrefix == null) || (dcrSysPrefix.trim().equals(IPLUtils.EMPTY_STRING) == true)) {
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
	
	String trnType = aRequest.getParameter(TRANSMITTAL_TYPE);
	if((trnType == null) || (trnType.trim().equals(""))){
		aResponse.getWriter().println("Select appropriate Transmittal type. '" + trnType + "' is invalid.");
		return;
	}
	else{
		trnType = trnType.trim();
	}
	
	IPLTransmittalType transmittalType;
	Hashtable<String, String> tpParams;
	try {
		transmittalType = IPLTransmittalType.lookupTransmittalTypeBySystemIdAndName(dcrSystemId, trnType);
		tpParams = IPLTransmittalType.getTransmittalProcessParameters(dcrSystemId, transmittalType.getTransmittalTypeId());
	} catch (SQLException e) {
		e.printStackTrace();
		throw new DatabaseException("Database error occurred while retrieving transmittal type.", e);
	}
	
	String requestList = aRequest.getParameter(REQUEST_LIST);
	if ((requestList == null) || requestList.trim().equals(IPLUtils.EMPTY_STRING)) {
		aResponse.getWriter().println("Empty Requests List.");
		return;
	}
	requestList = requestList.trim();
	
	String approvalCategoryList = aRequest.getParameter(CAT_LIST);
	if((approvalCategoryList == null) || approvalCategoryList.trim().equals(IPLUtils.EMPTY_STRING)){
		aResponse.getWriter().println("Empty approval categories List.");
		return;
	}
	
	String copiesList = aRequest.getParameter(COPIES_LIST);
	if (copiesList == null)
		copiesList = "";
	else
		copiesList = copiesList.trim();			
				
	String quantityList = aRequest.getParameter (COPIES_LIST);
	if ((quantityList == null) || quantityList.trim().equals(IPLUtils.EMPTY_STRING)) {
		aResponse.getWriter().print("Please enter the quantity.");
		return;
	}			
	quantityList = quantityList.trim();
	
	String toList = aRequest.getParameter (TO_LIST);
	if ((toList == null) || toList.trim().equals(IPLUtils.EMPTY_STRING)){
		LOG.info("Distribution List was empty, hence ignoring it.");				
		toList = "";
	}
	toList = toList.trim();
	
	String ccList = aRequest.getParameter (CC_LIST);
	if ((ccList == null) || ccList.trim().equals(IPLUtils.EMPTY_STRING)){
		LOG.info("Distribution List was empty, hence ignoring it.");				
		ccList = "";
	}
	ccList = ccList.trim();

	String summaryList = aRequest.getParameter(SUMMARY_LIST);
	if((summaryList == null) || summaryList.trim().equals(IPLUtils.EMPTY_STRING)){
		summaryList = IPLUtils.EMPTY_STRING;
	}
	summaryList = summaryList.trim();			
	
	String reference = aRequest.getParameter(REFERENCE);
	if (reference == null)
		reference = "";
	else
		reference = reference.trim();
	
	String dtnNumberInfix = "";
	if (IPLUtils.isExistsInString("TPSC," + IPLUtils.ADHOC_TPSC, dcrBA.getSystemPrefix())){
		dtnNumberInfix = aRequest.getParameter("dtnNumberInfix");
		if ((dtnNumberInfix == null) || dtnNumberInfix.trim().equals(""))
			dtnNumberInfix = "";
		else
			dtnNumberInfix = dtnNumberInfix.trim();
	}
	
	String transmittalSubject = aRequest.getParameter("transmittalSubject");
	if (transmittalSubject == null)
		transmittalSubject = "Transmittal Note";
	else
		transmittalSubject = transmittalSubject.trim();
	
	String remarks = aRequest.getParameter (REMARKS);
	if ((remarks == null) || remarks.trim().equals(IPLUtils.EMPTY_STRING)){	
		remarks = "";
	}
	remarks = remarks.trim();
						
	String trnIdPrefix = tpParams.get(IPLUtils.TRANSMITTAL_ID_PREFIX);
	//Add the infix string(C-TIB1-XXX-01-001) provided by the user for the DTN Number if its TPSC BA.
	//TODO: What about the remaining sub-string that has to be added after the infix and before the running serial number?
	if (IPLUtils.isExistsInString("TPSC", dcrBA.getSystemPrefix())){
		trnIdPrefix = trnIdPrefix + dtnNumberInfix + "-01-";
	}
				
	String trnTemplateName = tpParams.get(TRANSMITTAL_TEMPLATE_NAME);
	if ((trnTemplateName == null) || (trnTemplateName.trim().equals(""))){
		throw new TBitsException("Invalid template(.rptdeisgn) file name.");
	}
	//int dcrSystemId, String requestList, String catList, String typeList, String qtyList, 
	//String transmittalId, String transmittalSubject, User user, String inwardDTNFieldName
	//TODO: Get subject value from subject field. 
	IPLTemplateHelper kth = IPLUtils.getKskTemplateHelper(dcrSystemId, requestList, approvalCategoryList, quantityList,
			summaryList, toList, ccList, trnIdPrefix + "xxxxx", transmittalSubject, remarks, user, INWARD_TRANSMITTAL_NO, 
			tpParams, reference);		
	
	try {                                                
		htmlOS = IPLUtils.generateTransmittalNoteInHtml(aRequest, trnTemplateName, kth);
		if (htmlOS != null)
			aResponse.getWriter().println(htmlOS);
		else
			aResponse.getWriter().println("Could not generate the preview.");				
	} catch (EngineException e) {
		e.printStackTrace();
		aResponse.getWriter().println("Error occurred while generating preview. \nPlease contact tBits support.");
	}
}*/
 
