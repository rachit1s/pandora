/**
 * 
 */
package ncc;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IllegalFormatException;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.TransmittalHandler;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.PluginManager;
import transbit.tbits.webapps.TBitsError;
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
public class NCCTransmittalPlugin implements TransmittalHandler {

	private static final String DD_MMM_YYYY = "dd-MMM-yyyy";

	private static final String TRANSBIT_TBITS_TRANSMITTAL_DOCUMENT_ORIGINATING_DCRBA_LIST = "transbit.tbits.transmittal.documentOriginatingDCRBAList";

	private static final String FIELD_SUBMISSION_NO = "SubmissionNo";

	private static final String EMAIL = "email";

	private static final String TO_ADDRESS = "toAddress";

	private static final String DTN = "DTN";

	private static final String DTN_NUMBER_SELECT_INFIX = "dtnNumberSelectInfix";

	private static final String CONTRACT_REFERENCE = "Contract Reference";

	private static final String DELIMETER_DASH = "-";

	private static final String LOCATION = "Location";

	private static final String DTN_NUMBER_TEXT_INFIX_DISPLAY = "dtnNumberTextInfixDisplay";

	private static final String DTN_NUMBER_SELECT_INFIX_DISPLAY = "dtnNumberSelectInfixDisplay";

	private static final String DTN_NUMBER_INFIX_OPTIONS = "dtnNumberInfixOptions";

	private static final String DTN_NUMBER_TEXT_INFIX = "dtnNumberTextInfix";

	private static final String DTN_NUMBER_INFIX_NAME = "dtnNumberInfixName";

	private static final String TRANSMITTAL_TEMPLATE_NAME = "transmittal_template_name";

	private static final String RECIPIENT = "recipient";

	private static final String ORIGINATOR = "originator";

	private static final String DISPLAY_INLINE = "inline";
	
	private static final String DISPLAY_NONE = "none";

	private static final String REFERENCE = "reference";

	private static final String USERS_LIST = "usersList";

	private static final String SUMMARY_LIST = "summaryList";

	private static final String INWARD_TRANSMITTAL_NO = "IncomingTransmittalNo";

	private static final String DTN_STATUS_TRANSMITTAL_COMPLETE = "TransmittalComplete";
	
	//private static final String STP_GET_REQUEST_ID_BY_DOC_NO = "stp_get_requestIdByDocNo";

	//Transmittal attachment selection
	private static final String SELECTION_FILE = "ncc-transmittal-wizard.htm";
	
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
			
	//Miscellaneous	
	private static final String COPIES = "copies";
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
	
	private static final String APP_CATEGORIES ="[{name:\"G-Good for construction\",value:\"G\"},{name:\"P-Preliminary\",value:\"P\"}," +
												"{name:\"R-Review\",value:\"R\"},{name:\"I-For information\",value:\"I\"}," +
												"{name:\"T-Tender purpose\",value:\"T\"},{name:\"A-Approval\",value:\"A\"}]";
						
	private static final String DOC_TYPES = "[{name:\"RP-Reproducible\",value:\"RP\"},{name:\"SC-Soft copy\",value:\"SC\"}," +
											"{name:\"TR-Tracings\",value:\"TR\"},{name:\"TC-Transmittal copy\",value:\"TC\"}," +
											"{name:\"ZD-Zip disk\",value:\"ZD\"},{name:\"HC-Hard Copy\",value:\"HC\"}]";
		
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
	private String[] deliverableFileNames;
	private int deliverableFieldId;
	
	//Final result holder.
	JSONObject result = new JSONObject();
	
	//LOG to capture logs in webapps package.
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

	private static final String WORKFLOW_ORIGINATOR = "workflowOriginator";
	
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
			
		HttpSession session = aRequest.getSession();
		try {	
			handleGetRequest(aRequest, aResponse);
			/*}catch (DatabaseException e1) {
			LOG.error("Error while creating transmittal note:\n" + e1.getDescription());
			e1.printStackTrace();
		} catch (TBitsException e) {
			e.printStackTrace();
		}*/
		} catch (DatabaseException de) {
			session.setAttribute(TBitsError.EXCEPTION_OBJECT, de);
			aResponse.sendRedirect(WebUtil.getNearestPath(aRequest,"/error")) ;
			return;
		}  catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (TBitsException de) {
			session.setAttribute(TBitsError.EXCEPTION_OBJECT, de);
			aResponse.sendRedirect(WebUtil.getNearestPath(aRequest,"/error")) ;
			return;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see transbit.tbits.domain.TransmittalHandler#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */ 
	public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {
		String requestType = aRequest.getParameter(REQUEST_TYPE);
		requestType = requestType.trim();		
		
		if (requestType.equalsIgnoreCase(PREVIEW))
			try {
				handlePreviewPostRequest(aRequest, aResponse);
			} catch (DatabaseException e) {
				e.printStackTrace();
			} catch (TBitsException e) {
				e.printStackTrace();
			}
		else
			try {
				handlePostRequest(aRequest, aResponse);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
	}

	
	public void handleGetRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) 
									throws ServletException, IOException, DatabaseException,
									NumberFormatException, TBitsException {
		
		String requestType = aRequest.getParameter(REQUEST_TYPE);
		requestType = requestType.trim();
		HttpSession session = aRequest.getSession();
		
		if (requestType.equalsIgnoreCase(SELECTION)){
			aResponse.setContentType("text/html");
			//PrintWriter out = aResponse.getWriter();
			
			String dcrSysPrefix = aRequest.getParameter(DCR_BA);
			if ((dcrSysPrefix == null) || (dcrSysPrefix.trim().equals(TransmittalUtils.EMPTY_STRING) == true)) {
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
			if ((requestList == null) || requestList.trim().equals(TransmittalUtils.EMPTY_STRING)) {
				aResponse.getWriter().println("Empty Requests List.");
				return;
			}
			requestList = requestList.trim();		
						
			String trnDropDownIdStr = aRequest.getParameter(TRANSMITTAL_TYPE);
			if((trnDropDownIdStr == null) || (trnDropDownIdStr.trim().equals(""))){
				aResponse.getWriter().println("Select appropriate Transmittal type. '" + trnDropDownIdStr + "' is invalid.");
				return;
			}
			else{
				trnDropDownIdStr = trnDropDownIdStr.trim();				
			}
			
			DTagReplacer prepareTransmittal = null;
			File selectionFile = TransmittalUtils.getResourceFile(SELECTION_FILE);
			prepareTransmittal = new DTagReplacer(selectionFile);
			prepareTransmittal.replace(SYS_PREFIX, dcrSysPrefix);
			prepareTransmittal.replace(REQUEST_LIST, requestList);
			prepareTransmittal.replace(NEAREST_PATH, WebUtil.getNearestPath(aRequest, TransmittalUtils.EMPTY_STRING));
			prepareTransmittal.replace(USERS_LIST, getJSONArrayOfUsers().toString());//ReportUtil.getJSONArrayOfUsers().toString());
			
			NCCTransmittalProcess selectedTransmittalType = null;
			int trnDropDownId = 0;
			Hashtable<String, String> tpParams;
			try {
				trnDropDownId = Integer.parseInt(trnDropDownIdStr);			    
				ArrayList<NCCTransmittalProcess> ttList = NCCTransmittalProcess.lookupTransmittalTypeBySystemIdAndtrnDropDownId(
																dcrSystemId, trnDropDownId);
				if ((ttList != null)&& (!ttList.isEmpty())){
					ttList.trimToSize();
					selectedTransmittalType = ttList.get(0);
				}
				else
					throw new TBitsException("Invalid transmittal type.");
				
				ArrayList<Request> dcrRequests = new ArrayList<Request>();
				for (String requestIdStr : requestList.split(TransmittalUtils.DELIMETER_COMMA)){
					Request request = Request.lookupBySystemIdAndRequestId(dcrSystemId, Integer.parseInt(requestIdStr));
					dcrRequests.add(request);
				}
				dcrRequests.trimToSize();
				
				//Run controller plugin here and get the appropriate transmittal process
				Connection connection = null;
				try {
					// If only one transmittal process exists, no need to run controller to choose a transmittalType
					//if (ttList.size() > 1){
						connection = DataSourcePool.getConnection();
						NCCTransmittalDropDownOption ntp = NCCTransmittalDropDownOption.lookupTransmittalProcessBySystemId(dcrSystemId,
								trnDropDownId);
						runIPreControllers(connection, dcrBA, dcrRequests, ntp, ttList, selectedTransmittalType);
					//}
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw new DatabaseException("Error occurred while getting database connection.", sqle);
				} finally{
					if (connection != null)
						try {
							connection.close();
						} catch (SQLException sqle) {
							sqle.printStackTrace();
							throw new DatabaseException("Error occurred while closing database connection.", sqle);
						}
				}			
				if (selectedTransmittalType == null){
					throw new TBitsException("Invalid transmittal type.");
				}
				tpParams = NCCTransmittalProcess.getTransmittalProcessParameters(dcrSystemId, selectedTransmittalType.getTrnProcessId());
				
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DatabaseException("Database error occurred while retrieving transmittal type.", e);
			} catch (IllegalFormatException ife){
				ife.printStackTrace();
				throw new TBitsException("Invalid targetSystemId.");
			} catch (TBitsException tbe) {
				tbe.printStackTrace();
				//aResponse.getWriter().print(tbe.getMessage());
				session.setAttribute(TBitsError.EXCEPTION_OBJECT, tbe);
				aResponse.sendRedirect(WebUtil.getNearestPath(aRequest,"/error")) ;
				return;
			} 
			/*catch (TBitsException tbe) {
				tbe.printStackTrace(); 
				tbe.printStackTrace(aResponse.getWriter());				
				//aResponse.getWriter().print(tbe.getStackTrace()));
				return;
			}*/
			
			prepareTransmittal.replace(TRANSMITTAL_TYPE, selectedTransmittalType.getName());
			
			if ((tpParams != null) && !tpParams.isEmpty()){
				//Check if a value is found in the hashtable loaded from db. If no value is found set value to "".
				prepareTransmittal.replace(TO_LIST, (tpParams.get(TO_LIST)== null) ? "" : tpParams.get(TO_LIST));
				prepareTransmittal.replace(CC_LIST, (tpParams.get(CC_LIST) == null)? "" : tpParams.get(CC_LIST));
				prepareTransmittal.replace(EMAIL_BODY, (tpParams.get(EMAIL_BODY) == null)? "" : tpParams.get(EMAIL_BODY));
				prepareTransmittal.replace(REMARKS, (tpParams.get(REMARKS) == null)? "" : tpParams.get(REMARKS));
				prepareTransmittal.replace(TRANSMITTAL_SUBJECT, (tpParams.get(TRANSMITTAL_SUBJECT) == null)? "" : tpParams.get(TRANSMITTAL_SUBJECT));
				prepareTransmittal.replace(CAT_LIST, (tpParams.get("approvalCategories") == null) ?"[]" : tpParams.get("approvalCategories"));
				prepareTransmittal.replace(TYPE_LIST, (tpParams.get("documentTypes") == null) ? DOC_TYPES : tpParams.get("documentTypes"));
				prepareTransmittal.replace(TO_ADDRESS, (tpParams.get(TO_ADDRESS) == null) ? "" : tpParams.get(TO_ADDRESS)); 
			}
			else{
				prepareTransmittal.replace(TO_LIST, "");
				prepareTransmittal.replace(CC_LIST, "");
				prepareTransmittal.replace(EMAIL_BODY, "");
				prepareTransmittal.replace(REMARKS, "");
				prepareTransmittal.replace(TRANSMITTAL_SUBJECT, "");
				prepareTransmittal.replace(CAT_LIST, APP_CATEGORIES);
				prepareTransmittal.replace(TYPE_LIST, DOC_TYPES);
				prepareTransmittal.replace(TO_ADDRESS, "");
			}
			
			String actualDateProperty = "";
			String actualDateUsers = "";
			try{				
				actualDateProperty = PropertiesHandler.getProperty("transbit.tbits.transmittal.isActualDateEnabled");
				actualDateUsers = PropertiesHandler.getProperty("transbit.tbits.transmittal.actualDateUsers");
				
				user = WebUtil.validateUser(aRequest);
				
				if((actualDateProperty != null) && (actualDateUsers != null) 
						&& Boolean.parseBoolean(actualDateProperty)
						&& TransmittalUtils.isExistsInString(actualDateUsers, user.getUserLogin())){
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
			
			prepareTransmittal.replace(REFERENCE, "");
			String tableData = null;
			try{
				ArrayList<Request> dcrRequests = new ArrayList<Request>();
				for (String requestIdStr : requestList.split(TransmittalUtils.DELIMETER_COMMA)){
					Request request = Request.lookupBySystemIdAndRequestId(dcrSystemId, Integer.parseInt(requestIdStr));
					dcrRequests.add(request);
				}
				if ((dcrRequests == null) || (dcrRequests.isEmpty()))
					throw new TBitsException("No drawings/documents selected for transmittal.");
				else{
					tableData = getTableData(dcrSystemId, dcrRequests, tpParams);
				}
			}catch (TBitsException tbe) {
				tbe.printStackTrace();
				//aResponse.getWriter().print(tbe.getMessage());
				session.setAttribute(TBitsError.EXCEPTION_OBJECT, tbe);
				aResponse.sendRedirect(WebUtil.getNearestPath(aRequest,"/error")) ;
				return;
			}catch (DatabaseException dbe) {
				dbe.printStackTrace();
				session.setAttribute(TBitsError.EXCEPTION_OBJECT, dbe);
				aResponse.sendRedirect(WebUtil.getNearestPath(aRequest,"/error")) ;
				//aResponse.getWriter().print(dbe.getMessage());
				return;
			}
			
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
				prepareTransmittal.replace(TRANSMITTAL_PROCESS_NAME, selectedTransmittalType.getTrnMaxSnKey());
				prepareTransmittal.replace(DELIVERABLE_FIELD_ID, deliverableField.getFieldId() + "");
			}catch(NumberFormatException nfe){
				nfe.printStackTrace();
				aResponse.getWriter().print(nfe.getMessage());
				return;
			}
			
			handleDTNNumberInfixLogic(dcrSystemId, prepareTransmittal);
			
			aResponse.getWriter().print(prepareTransmittal.parse(dcrSystemId));
			return;
		}
	}
	
	public static JSONArray getJSONArrayOfUsers(){
		JSONArray userArray = new JSONArray();
		for(User user : User.getActiveUsers()){
			userArray.add(user.getUserLogin() + "<" + user.getDisplayName() + ">");
		}
		return userArray;
	}
	
	private void handleDTNNumberInfixLogic(int systemId, DTagReplacer prepareTransmittal) {
		switch (systemId){
			case DESEIN_SYS_ID: {
				prepareTransmittal.replace(DTN_NUMBER_INFIX_NAME, LOCATION);
				prepareTransmittal.replace(DTN_NUMBER_SELECT_INFIX, DELIMETER_DASH);
				prepareTransmittal.replace(DTN_NUMBER_INFIX_OPTIONS, getDTNNumberInfixOptions(DESEIN_SYS_ID));
				prepareTransmittal.replace(DTN_NUMBER_SELECT_INFIX_DISPLAY, DISPLAY_INLINE);
				prepareTransmittal.replace(DTN_NUMBER_TEXT_INFIX_DISPLAY, DISPLAY_NONE);
				break;			
			}
			//case EDTD_SYS_ID:
			case UC_NCC_SYS_ID:
			case NCC_SYS_ID: {
				prepareTransmittal.replace(DTN_NUMBER_INFIX_NAME, CONTRACT_REFERENCE);
				prepareTransmittal.replace(DTN_NUMBER_SELECT_INFIX, "4");
				prepareTransmittal.replace(DTN_NUMBER_INFIX_OPTIONS, getDTNNumberInfixOptions(NCC_SYS_ID));
				prepareTransmittal.replace(DTN_NUMBER_SELECT_INFIX_DISPLAY, DISPLAY_INLINE);
				prepareTransmittal.replace(DTN_NUMBER_TEXT_INFIX_DISPLAY, DISPLAY_NONE);
				break;			
			}			
			case DCPL_SYS_ID: {
				prepareTransmittal.replace(DTN_NUMBER_INFIX_NAME, "Discipline");
				prepareTransmittal.replace(DTN_NUMBER_SELECT_INFIX, DELIMETER_DASH);
				prepareTransmittal.replace(DTN_NUMBER_INFIX_OPTIONS, getDTNNumberInfixOptions(DCPL_SYS_ID));
				prepareTransmittal.replace(DTN_NUMBER_SELECT_INFIX_DISPLAY, DISPLAY_INLINE);
				prepareTransmittal.replace(DTN_NUMBER_TEXT_INFIX_DISPLAY, DISPLAY_NONE);
				break;			
			}
			case CSEPDI_SYS_ID:{
				prepareTransmittal.replace(DTN_NUMBER_TEXT_INFIX, "");
				prepareTransmittal.replace(DTN_NUMBER_SELECT_INFIX_DISPLAY, DISPLAY_NONE);
				prepareTransmittal.replace(DTN_NUMBER_TEXT_INFIX_DISPLAY, DISPLAY_INLINE);
				break;			
			}
			default:{
				prepareTransmittal.replace(DTN_NUMBER_SELECT_INFIX_DISPLAY, DISPLAY_NONE);
				prepareTransmittal.replace(DTN_NUMBER_TEXT_INFIX_DISPLAY, DISPLAY_NONE);
			}
		}		
	}


	public void handlePreviewPostRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, 
		IOException, DatabaseException, TBitsException {
		
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
		if ((dcrSysPrefix == null) || (dcrSysPrefix.trim().equals(TransmittalUtils.EMPTY_STRING) == true)) {
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
		
		NCCTransmittalProcess transmittalType;
		Hashtable<String, String> tpParams;
		try {
			transmittalType = NCCTransmittalProcess.lookupTransmittalTypeBySystemIdAndName(dcrSystemId, trnType);
			tpParams = NCCTransmittalProcess.getTransmittalProcessParameters(dcrSystemId, transmittalType.getTrnProcessId());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Database error occurred while retrieving transmittal type.", e);
		}
		
		String requestList = aRequest.getParameter(REQUEST_LIST + 1);
		if ((requestList == null) || requestList.trim().equals(TransmittalUtils.EMPTY_STRING)) {
			aResponse.getWriter().println("Empty Requests List.");
			return;
		}
		requestList = requestList.trim();
		
		String approvalCategoryList = aRequest.getParameter(CAT_LIST + 1);
		if((approvalCategoryList == null) || approvalCategoryList.trim().equals(TransmittalUtils.EMPTY_STRING)){
			aResponse.getWriter().println("Empty approval categories List.");
			return;
		}
		
		String typeList = aRequest.getParameter(TYPE_LIST + 1);
		if((typeList == null) || typeList.trim().equals(TransmittalUtils.EMPTY_STRING)){
			aResponse.getWriter().println("Empty document types List.");
			return;
		}
		typeList = typeList.trim();
		
		/*String copiesList = aRequest.getParameter(COPIES_LIST);
		if (copiesList == null)
			copiesList = "";
		else
			copiesList = copiesList.trim();*/			
					
		String quantityList = aRequest.getParameter (COPIES_LIST + 1);
		if ((quantityList == null) || quantityList.trim().equals(TransmittalUtils.EMPTY_STRING)) {
			aResponse.getWriter().print("Please enter the quantity.");
			return;
		}			
		quantityList = quantityList.trim();
		
		String toList = aRequest.getParameter (TO_LIST + 1);
		if ((toList == null) || toList.trim().equals(TransmittalUtils.EMPTY_STRING)){
			LOG.info("Distribution List was empty, hence ignoring it.");				
			toList = "";
		}
		toList = toList.trim();
		
		String ccList = aRequest.getParameter (CC_LIST + 1);
		if ((ccList == null) || ccList.trim().equals(TransmittalUtils.EMPTY_STRING)){
			LOG.info("Distribution List was empty, hence ignoring it.");				
			ccList = "";
		}
		ccList = ccList.trim();

		String summaryList = aRequest.getParameter(SUMMARY_LIST + 1);
		if((summaryList == null) || summaryList.trim().equals(TransmittalUtils.EMPTY_STRING)){
			summaryList = TransmittalUtils.EMPTY_STRING;
		}
		summaryList = summaryList.trim();			
		
		String transmittalDate = aRequest.getParameter("ActualDate" + 1);
		if ((transmittalDate == null) || transmittalDate.trim().equals("")){								
			transmittalDate = Timestamp.getGMTNow().toCustomFormat(DD_MMM_YYYY);
		}
		
		String preDefinedDTNNumber = aRequest.getParameter("preDefinedDTNNumber" + 1);
		if ((preDefinedDTNNumber == null) || preDefinedDTNNumber.trim().equals(""))
			preDefinedDTNNumber = "";
			
		String transmittalSubject = aRequest.getParameter("transmittalSubject1");
		if (transmittalSubject == null)
			transmittalSubject = "Transmittal Note";
		else
			transmittalSubject = transmittalSubject.trim();
		
		String remarks = aRequest.getParameter (REMARKS + 1);
		if ((remarks == null) || remarks.trim().equals(TransmittalUtils.EMPTY_STRING)){	
			remarks = "";
		}
		remarks = remarks.trim();
		
		String toAddress = aRequest.getParameter (TO_ADDRESS +1);
		if ((toAddress == null) || toAddress.trim().equals(TransmittalUtils.EMPTY_STRING)){	
			toAddress = "";
		}
		toAddress = toAddress.trim();
		
		String emailBody = aRequest.getParameter("emailBody1");
		if (emailBody == null)
			emailBody = "";
							
		String trnIdPrefix = tpParams.get(TransmittalUtils.TRANSMITTAL_ID_PREFIX);
		String dtnNumberInfix = getDTNInfixString(aRequest, dcrSystemId, trnIdPrefix, tpParams);
		if ((dtnNumberInfix != null) && (!dtnNumberInfix.equals("")))
			trnIdPrefix = dtnNumberInfix;		
		
		int preDefinedMaxId = 0;
		if (!preDefinedDTNNumber.equals("")){
			//int lIndex = preDefinedDTNNumber.lastIndexOf("-");
			//String dtnNumberStr = preDefinedDTNNumber.substring(lIndex + 1);
			//if ((dtnNumberStr != null) && (!dtnNumberStr.trim().equals(""))){
			if (!preDefinedDTNNumber.trim().equals(EMAIL)){
				String preDefinedDTNSerial = preDefinedDTNNumber.substring(preDefinedDTNNumber.lastIndexOf("-") + 1);
				preDefinedMaxId = Integer.parseInt(preDefinedDTNSerial);
			}
			//}
		}
		else
			preDefinedMaxId = 0;
		
		String dtnNumber = "";
		if (dcrSystemId == CSEPDI_SYS_ID)
			dtnNumber = trnIdPrefix;		
		else{
			int transmittalMaxId  = 0;
			if (preDefinedMaxId > 0){
				transmittalMaxId = preDefinedMaxId;
			}
			else
				transmittalMaxId = getTransmittalMaxId(transmittalType) + 1;
				//transmittalMaxId = transmittalMaxId + 1;
			
			if (!preDefinedDTNNumber.trim().equals("")){
				if (preDefinedDTNNumber.trim().equals(EMAIL))
					dtnNumber = EMAIL;
				else
					dtnNumber = preDefinedDTNNumber;
			}
			else
				dtnNumber = trnIdPrefix + TransmittalUtils.getFormattedStringFromNumber(transmittalMaxId) + "{Likely}";
		}
		
		
		String trnTemplateName = tpParams.get(TRANSMITTAL_TEMPLATE_NAME);
		if ((trnTemplateName == null) || (trnTemplateName.trim().equals(""))){
			throw new TBitsException("Invalid template(.rptdesign) file name.");
		}
		
		//int dcrSystemId, String requestList, String catList, String typeList, String qtyList, 
		//String transmittalId, String transmittalSubject, User user, String inwardDTNFieldName		
		NCCTemplateHelper kth = TransmittalUtils.getKskTemplateHelper(dcrSystemId, requestList.split(","), approvalCategoryList, 
				typeList,quantityList, summaryList, toList, ccList, dtnNumber, transmittalSubject, remarks, user, INWARD_TRANSMITTAL_NO,
				toAddress, emailBody, "", tpParams, transmittalDate);//, reference);		
		
		try {                                                
			htmlOS = TransmittalUtils.generateTransmittalNoteInHtml(aRequest, trnTemplateName, kth);
			if (htmlOS != null)
				aResponse.getWriter().println(htmlOS);
			else
				aResponse.getWriter().println("Could not generate the preview.");				
		} catch (EngineException e) {
			e.printStackTrace();
			aResponse.getWriter().println("Error occurred while generating preview. \nPlease contact tBits support.");
		}
			
	}
	
	private String getDTNInfixString(HttpServletRequest aRequest, int systemId, String dtnNumberPrefix, Hashtable<String, String> tpParams) {
		
		String infixString = dtnNumberPrefix + DELIMETER_DASH;
		
		switch (systemId){
			//D-3034/VC-20/HYD/SN
			case DESEIN_SYS_ID:{
				String location = aRequest.getParameter(DTN_NUMBER_SELECT_INFIX + 1);
				if (location==null)
					location = aRequest.getParameter(DTN_NUMBER_SELECT_INFIX);
				if (!location.equals(DELIMETER_DASH))
					infixString = infixString + location + DELIMETER_DASH;				
				break;
			}
			case EDTD_SYS_ID:{
				String recipient = tpParams.get(RECIPIENT);
				if (recipient == null)
					recipient = "";
				infixString = infixString + recipient + DELIMETER_DASH + DTN + DELIMETER_DASH;				
				String currentYear = getCurrentFinancialYearString(DELIMETER_DASH);
				infixString = infixString + currentYear + DELIMETER_DASH;			
				break;
			}
			case NCC_SYS_ID:{
				String contractReference = aRequest.getParameter(DTN_NUMBER_SELECT_INFIX + 1);	
				if (contractReference==null)
					contractReference = aRequest.getParameter(DTN_NUMBER_SELECT_INFIX);
				if (contractReference != null)
					infixString = infixString + contractReference + DELIMETER_DASH;
				String recipient = tpParams.get(RECIPIENT);
				if (recipient == null)
					recipient = "";
				infixString = infixString + recipient + DELIMETER_DASH + DTN + DELIMETER_DASH;				
				String currentYear = getCurrentFinancialYearString(DELIMETER_DASH);
				infixString = infixString + currentYear + DELIMETER_DASH;			
				break;
			}
			case UC_NCC_SYS_ID:{
				String contractReference = aRequest.getParameter(DTN_NUMBER_SELECT_INFIX + 1);	
				if (contractReference==null)
					contractReference = aRequest.getParameter(DTN_NUMBER_SELECT_INFIX);
				if (contractReference != null)
					infixString = infixString + contractReference + DELIMETER_DASH;
				String recipient = tpParams.get(RECIPIENT);
				if (recipient == null)
					recipient = "";
				infixString = infixString + recipient + DELIMETER_DASH + "UNCN" + DELIMETER_DASH + DTN + DELIMETER_DASH;				
				/*String currentYear = getCurrentFinancialYearString(DELIMETER_DASH);
				infixString = infixString + currentYear + DELIMETER_DASH;*/			
				break;
			}		
			case DCPL_SYS_ID:{
				//K9210/M or E or G/S.No
				String discipline = aRequest.getParameter(DTN_NUMBER_SELECT_INFIX + 1);
				if (discipline==null)
					discipline = aRequest.getParameter(DTN_NUMBER_SELECT_INFIX);
				if (discipline != null)
					infixString = infixString + discipline + DELIMETER_DASH;
				break;
			}
			case KNPL_SYS_ID:{
				//KNPL/Party/10-11/0001
				String recipient = tpParams.get(RECIPIENT);
				if (recipient == null)
					recipient = "";
				infixString = infixString + recipient + DELIMETER_DASH;
				String currentYear = getCurrentFinancialYearString(DELIMETER_DASH);
				infixString = infixString + currentYear + DELIMETER_DASH;				
				break;
			}
			case CSEPDI_SYS_ID:{
				//KNTPP/CSEP/KNPL/EML/1055-100226
				String recipient = tpParams.get(RECIPIENT);
				if (recipient == null)
					recipient = "";
				//infixString = infixString + recipient + DELIMETER_DASH + DTN;
				String serialNumber = aRequest.getParameter(DTN_NUMBER_TEXT_INFIX + 1);
				if (serialNumber == null)
					serialNumber = aRequest.getParameter(DTN_NUMBER_TEXT_INFIX);
				if ((serialNumber != null) && (!serialNumber.equals("")))
					infixString = (infixString.equals("") || infixString.trim().equals("-")) ? serialNumber
														   : infixString + DELIMETER_DASH + serialNumber;	
				break;
			}		
		}
		return infixString;
	}

	private static String getCurrentFinancialYearString(String delimeter)
	{
		Calendar ndd = Calendar.getInstance() ;
		int currMonth = ndd.get(Calendar.MONTH) ;
		
		if( currMonth < Calendar.APRIL )
		{
			Calendar other = Calendar.getInstance(); 
			other.add(Calendar.YEAR, -1);
			Timestamp nowTs = new Timestamp(ndd.getTimeInMillis()) ;
			Timestamp otherTs = new Timestamp(other.getTimeInMillis()) ;
			return otherTs.toCustomFormat("yy") + "-" + nowTs.toCustomFormat("yy") ;
		}
		else
		{
			Calendar other = Calendar.getInstance(); 
			other.add(Calendar.YEAR, 1);
			Timestamp nowTs = new Timestamp(ndd.getTimeInMillis()) ;
			Timestamp otherTs = new Timestamp(other.getTimeInMillis()) ;
			return nowTs.toCustomFormat("yy") + "-" + otherTs.toCustomFormat("yy") ;
		}		
	}


	/**
	 * @param transmittalType
	 * @return
	 * @throws DatabaseException
	 * @throws TBitsException
	 */
	private int getTransmittalMaxId(NCCTransmittalProcess transmittalType)
			throws DatabaseException, TBitsException {
		Connection connection = null;	
		int transmittalMaxId = 0;
		try {								
			connection = DataSourcePool.getConnection();
			String queryString = "SELECT id FROM max_ids WHERE name=?";//trn_max_sn FROM trn_max_serial WHERE trn_max_sn_key = ?";
			PreparedStatement ps = connection.prepareStatement(queryString);
			ps.setString(1, transmittalType.getTrnMaxSnKey());
			ResultSet rs = ps.executeQuery();				
			if (rs.next()){
				//Increment it to point to the next number.
				transmittalMaxId = rs.getInt("id");
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
	 * This method handles the transmittal creation and updating of respective DCR and DTR processes.
	 * 1. Give unique transmittal number for each transmittal type in each BA DCR.
	 * 2. Generation of transmittal note.
	 * 3. Add to proper attachment fields in DTN as well as DTR & DCRs.
	 * 4. Create appropriate user for the process.
	 * 
	 * @throws ServletException
	 * @throws IOException
	 * @throws DatabaseException 
	 * @throws NumberFormatException 
	 */

	public void handlePostRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, NumberFormatException, DatabaseException {
		//HttpSession aSession = aRequest.getSession(true);
		
		TBitsResourceManager tBitsResMgr = new TBitsResourceManager();
		PrintWriter out = aResponse.getWriter(); 

		aResponse.setContentType("text/plain");	
		try {
			user = WebUtil.validateUser(aRequest);
		} catch (TBitsException e2) {
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "TBitsException: \n" + "User validation unssuccessful");
			out.print(result.toString());
			return;
		} catch (DatabaseException e) {
			e.printStackTrace();
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "DatabaseException: \n" + "User validation unssuccessful");
			out.print(result.toString());
			return;
		}

		String dcrSysPrefix = aRequest.getParameter(DCR_BA);

		if ((dcrSysPrefix == null) || (dcrSysPrefix.trim().equals(TransmittalUtils.EMPTY_STRING))) {
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "Invalid Business Area.");
			out.print(result.toString());
			return;
		} else {
			dcrSysPrefix = dcrSysPrefix.trim();
		}
		
		String requestList = aRequest.getParameter(REQUEST_LIST);

		if ((requestList == null) || requestList.trim().equals(TransmittalUtils.EMPTY_STRING)) {
			out.print(KEYWORD_FALSE + TransmittalUtils.DELIMETER_COMMA +"No document selected to be sent to transmittal. Please select the documents to be sent");
			return;
		}
		requestList = requestList.trim();
		String[] dcrRequestList = requestList.split(TransmittalUtils.DELIMETER_COMMA);

		String docList = aRequest.getParameter (DRAWINGS_LIST);
		if ((docList == null) || docList.trim().equals(TransmittalUtils.EMPTY_STRING)) {
			out.print("Please select documents to be sent to transmittal");
			return;
		}			
		docList = docList.trim();

		String revList = aRequest.getParameter (REVISION_LIST);
		if ((revList == null) || revList.trim().equals(TransmittalUtils.EMPTY_STRING)) {
			out.print("Revision numbers list not provided. Please enter proper revision numbers");
			return;
		}			
		revList = revList.trim();

		String quantityList = aRequest.getParameter (COPIES_LIST);
		if ((quantityList == null) || quantityList.trim().equals(TransmittalUtils.EMPTY_STRING)) {
			out.print("Please enter the quantity.");
			return;
		}			
		quantityList = quantityList.trim();

		String approvalCategoryList = aRequest.getParameter (CAT_LIST);
		if ((approvalCategoryList == null) || approvalCategoryList.trim().equals(TransmittalUtils.EMPTY_STRING)) {
			out.print("No actions found for the selected documents. Please select appropriate actions");
			return;
		}			
		approvalCategoryList = approvalCategoryList.trim();

		String summaryList = aRequest.getParameter(SUMMARY_LIST);
		if((summaryList == null) || summaryList.trim().equals(TransmittalUtils.EMPTY_STRING)){
			summaryList = TransmittalUtils.EMPTY_STRING;
		}
		summaryList = summaryList.trim();

		String documentTypeList = aRequest.getParameter (TYPE_LIST);
		if ((documentTypeList == null) || documentTypeList.trim().equals(TransmittalUtils.EMPTY_STRING)) {
			out.print("Please enter the document type.");
			return;
		}			
		documentTypeList = documentTypeList.trim();

		String deliverableAttachments = "";
		String deliverableList = aRequest.getParameter(DELIVERABLES_LIST);
		if ((deliverableList == null) || deliverableList.trim().equals(TransmittalUtils.EMPTY_STRING)) {
			LOG.warn("No deliverable attachments selected.");
			deliverableList = TransmittalUtils.EMPTY_STRING;
		}
		else{
			deliverableFileNames = deliverableList.split("<br2>");//TransmittalUtils.DELIMETER_COMMA);
			deliverableAttachments = getTransmittalAttachments(deliverableFileNames);
		}

		String otherAttachments = "";
		String attachmentList = aRequest.getParameter(ATTACHMENT_LIST);
		if ((attachmentList == null) || attachmentList.trim().equals(TransmittalUtils.EMPTY_STRING)) {
			LOG.warn("No attachments selected.");
			attachmentList = TransmittalUtils.EMPTY_STRING;
		}
		else{
			reqFileNames = attachmentList.split("<br2>");//TransmittalUtils.DELIMETER_COMMA);
			otherAttachments = getTransmittalAttachments(reqFileNames);
		}
		
		String toList = aRequest.getParameter(TO_LIST);
		if ((toList == null)||(toList.trim().equals(TransmittalUtils.EMPTY_STRING))){
			System.out.println ("No mailing address provided");
		}
		toList = toList.trim();

		String ccList = aRequest.getParameter(CC_LIST);
		if ((ccList ==null) || (ccList.trim().equals(TransmittalUtils.EMPTY_STRING))){
			ccList = TransmittalUtils.EMPTY_STRING;
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
		if ((remarks ==null) || (remarks.trim().equals(TransmittalUtils.EMPTY_STRING))){
			remarks = TransmittalUtils.EMPTY_STRING;			
		}
		else{
			remarks = remarks.trim();
		}

		String reference = aRequest.getParameter(REFERENCE);
		if (reference == null)
			reference = "";
		else
			reference = reference.trim();

		String transmittalSubject = aRequest.getParameter(TRANSMITTAL_SUBJECT);
		if ((transmittalSubject == null) || (transmittalSubject.trim().equals(TransmittalUtils.EMPTY_STRING))){
			transmittalSubject = TransmittalUtils.EMPTY_STRING;		
			LOG.info("No email body found for transmittal process");
		}
		else{
			transmittalSubject = transmittalSubject.trim();
		}

		String emailBody = aRequest.getParameter(EMAIL_BODY);
		if ((emailBody == null) || (emailBody.trim().equals(TransmittalUtils.EMPTY_STRING))){
			emailBody = TransmittalUtils.EMPTY_STRING;		
			LOG.info("No email body found for transmittal process");
		}
		else{
			emailBody = emailBody.trim();
		}

		String transmittalProcessSerialKey = aRequest.getParameter(TRANSMITTAL_PROCESS_NAME);			
		if ((transmittalProcessSerialKey == null) || (transmittalProcessSerialKey.trim().equals(TransmittalUtils.EMPTY_STRING))){
			LOG.fatal("Could not find corresponding transmittal process. Please contact admin/tBits support team.");
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
		
		String toAddress = aRequest.getParameter(TO_ADDRESS);
		if(toAddress != null)
			toAddress = toAddress.trim();

		/*String mappedBusinessAreas = aRequest.getParameter("mappedBusinessAreas");
			if ((mappedBusinessAreas == null) || (mappedBusinessAreas.trim().equals("")))
				LOG.warn("No business areas mapped for updating post transmittal.");
			else
				mappedBusinessAreas = mappedBusinessAreas.trim();*/
		Connection connection = null;
		Connection maxIdConn = null;		

		try {
			BusinessArea dcrBA = BusinessArea.lookupBySystemPrefix(dcrSysPrefix);
			if (dcrBA == null) {
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, dcrSysPrefix + ": Invalid Business Area or business area does not exist");
				out.print(result.toString());
				return;
			}		
			NCCTransmittalProcess transmittalProcess;
			Hashtable<String, String> tpParams;
			try {
				transmittalProcess = NCCTransmittalProcess.lookupTransmittalTypeBySystemIdAndName(dcrBA.getSystemId(), trnTypeName);
				tpParams = NCCTransmittalProcess.getTransmittalProcessParameters(dcrBA.getSystemId(), transmittalProcess.getTrnProcessId());
				if (tpParams == null){
					throw new TBitsException("Transmittal process parameters not found. Hence cannot continue.");
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DatabaseException("Database error occurred while retrieving transmittal type.", e);
			}

			int trnSystemId = transmittalProcess.getDtnSysId();			
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
				
			String trnIdPrefix = tpParams.get(TransmittalUtils.TRANSMITTAL_ID_PREFIX);
			if ((trnIdPrefix == null) || (trnIdPrefix.trim().equals("")) && (dcrBA.getSystemId() != CSEPDI_SYS_ID)){
				throw new TBitsException("Invalid transmittal number prefix, please contact admin or tBits support.");
			}
			
			String dtnNumberInfix = getDTNInfixString(aRequest, dcrBA.getSystemId(), trnIdPrefix, tpParams);
			if ((dtnNumberInfix != null) && (!dtnNumberInfix.equals("")))
				trnIdPrefix = dtnNumberInfix;		
			
			//String linkedRequests = TransmittalUtils.getLinkedRequests(dcrBA, dcrRequestList);
			//Hashtable <String,String> aParamTable = new Hashtable<String, String>();
			
			try {
				maxIdConn = DataSourcePool.getConnection();
				maxIdConn.setAutoCommit(false);
				//String dtnNumber = "";
				int preDefinedTrnId = 0;
				String formattedTrnReqId = TransmittalUtils.EMPTY_STRING;
				if (!preDefinedDTNNumber.trim().equals("")){
					if (preDefinedDTNNumber.trim().equals(EMAIL))
						trnIdPrefix = EMAIL;
					else{
						preDefinedTrnId = Integer.parseInt(preDefinedDTNNumber.substring(preDefinedDTNNumber.lastIndexOf("-")+1));						
						if (preDefinedTrnId > 0){
							trnIdPrefix = preDefinedDTNNumber;
							//transReqId = preDefinedTrnId;
						}
					}					
				}
				else if (dcrBA.getSystemId() != CSEPDI_SYS_ID){
					int transReqId = NCCTransmittalProcess.getMaxTransmittalNumber(maxIdConn, transBA.getSystemId(),
							transmittalProcessSerialKey);
					formattedTrnReqId = TransmittalUtils.getFormattedStringFromNumber(transReqId);
					trnIdPrefix = trnIdPrefix + formattedTrnReqId;
				}					
				//System.out.println("#### After connection for max Id."); 
				//transReqId = NCCTransmittalProcess.getMaxTransmittalNumber(maxIdConn, transBA.getSystemId(), transmittalProcess);			

				//Create Connection, FileResourceManager and MailResourceManager.
				connection = DataSourcePool.getConnection();
				connection.setAutoCommit(false);

			} catch (SQLException e1) {
				e1.printStackTrace();
				throw new DatabaseException("Error occurred while fetching database connection.", e1);
			}
			
			String contextPath = aRequest.getContextPath();
			
			/*Connection connection,
			String contextPath, TBitsResourceManager tBitsResMgr,
			BusinessArea dcrBA, NCCTransmittalProcess transmittalProcess,
			Hashtable<String, String> tpParams, BusinessArea transBA,
			String trnIdPrefix, String[] dcrRequestList, String deliverableAttachments,
			String otherAttachments, String quantityList, String approvalCategoryList, 
			String summaryList,	String documentTypeList, String toList, String ccList,
			String remarks, String transmittalSubject, String emailBody*/
			
			String formattedTrnReqId = createTransmittal(connection, contextPath,
					tBitsResMgr, dcrBA, transmittalProcess, tpParams, transBA,
					trnIdPrefix, dcrRequestList, deliverableAttachments, otherAttachments,
					quantityList, approvalCategoryList, summaryList, documentTypeList,  
					toList, ccList, remarks, transmittalSubject, emailBody, toAddress, 
					transmittalDate);

			LOG.info("%%%%%%%%%%%%%%%%%%%%%%%%Finished updating all BA's&&&&&&&&&&&&&&&&&&&");

			result.put(KEYWORD_SUCCESS, KEYWORD_TRUE);
			result.put(KEYWORD_VALUE, formattedTrnReqId);
			out.print(result.toString());	
			try{
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
		}catch (NullPointerException npe) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr); 
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "TBits Exception: \n" + npe.getMessage());
			out.print(result.toString());
			npe.printStackTrace();
			return;							
		} catch (ParseException e) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr); 
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "Parse Exception: \n" + e.getMessage());
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

	/**
	 * Called by handlePostRequest.
	 * @param connection - connection object
	 * @param contextPath 
	 * @param tBitsResMgr
	 * @param dcrBA
	 * @param transmittalProcess
	 * @param tpParams
	 * @param transBA
	 * @param trnIdPrefix
	 * @param aParamTable
	 * @param requestList
	 * @param dcrRequestList
	 * @param quantityList
	 * @param approvalCategoryList
	 * @param summaryList
	 * @param documentTypeList
	 * @param toList
	 * @param ccList
	 * @param remarks
	 * @param transmittalSubject
	 * @param emailBody
	 * @param linkedRequests
	 * @return
	 * @throws NumberFormatException
	 * @throws DatabaseException
	 * @throws TBitsException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws APIException
	 * @throws ParseException 
	 */
	private String createTransmittal(Connection connection,
			String contextPath, TBitsResourceManager tBitsResMgr,
			BusinessArea dcrBA, NCCTransmittalProcess transmittalProcess,
			Hashtable<String, String> tpParams, BusinessArea transBA,
			String trnIdPrefix, String[] dcrRequestList, String deliverableAttachments,
			String otherAttachments, String quantityList, String approvalCategoryList, 
			String summaryList,	String documentTypeList, String toList, String ccList,
			String remarks, String transmittalSubject, String emailBody, String toAddress,
			String transmittalDate) 
			throws NumberFormatException, DatabaseException, 
			TBitsException, FileNotFoundException,
			IOException, APIException, ParseException {		
		
		String dtnAttachment = TransmittalUtils.EMPTY_STRING;
		String linkedRequests = TransmittalUtils.getLinkedRequests(dcrBA, dcrRequestList);
		//int dcrSystemId, String requestList, String catList, String typeList, String qtyList, 
		//String transmittalId, String transmittalSubject, User user, String inwardDTNFieldName
		try{		
			String dtnDate = "";
			if ((transmittalDate == null) || transmittalDate.equals("")){
				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("IST"));			
				Date d = c.getTime();
				SimpleDateFormat sdf = new SimpleDateFormat(DD_MMM_YYYY);				
				dtnDate = sdf.format(d);
			}
			
			NCCTemplateHelper kth = TransmittalUtils.getKskTemplateHelper(dcrBA.getSystemId(), dcrRequestList, approvalCategoryList, 
					documentTypeList, quantityList, summaryList, toList, ccList, trnIdPrefix, transmittalSubject,
					remarks, user, INWARD_TRANSMITTAL_NO, toAddress, emailBody, "", tpParams, dtnDate);//, reference);
			
			String templateName = tpParams.get(TRANSMITTAL_TEMPLATE_NAME);
			if ((templateName == null) || (templateName.trim().equals(""))){
				throw new TBitsException("Please provide appropriate DTN template for generating transmittal note.");
			}
			dtnAttachment = getDTNAttachment(kth, templateName);


		} catch (EngineException e) {
			e.printStackTrace();
			throw new TBitsException(e);
		}		
		
		String toUserLoginList = getUserLoginList(toList);
		String ccUserLoginList = getUserLoginList(ccList);
		
		String logDate = getLoggedDate(transmittalDate);
		Request trnRequest = logRequestInTransmittalBA(connection, contextPath, 
				tBitsResMgr, tpParams, transBA, trnIdPrefix, dtnAttachment, 
				deliverableAttachments, otherAttachments, toUserLoginList, ccUserLoginList, 
				transmittalSubject, emailBody, linkedRequests, logDate);
		
		ArrayList<Request> dcrRequests = updateSourceDCRBusinessArea(connection, tBitsResMgr,
				dcrRequestList, dcrBA, transmittalProcess, tpParams, transBA,  
				trnIdPrefix, trnRequest, logDate);

		//int dtrSysId = transmittalType.getDtrSysId();

		//try {
			/*Hashtable<Integer, Integer> dtrRequestsMap = new Hashtable<Integer, Integer>();
			Hashtable<Integer, Integer> iDtrRequestsMap = new Hashtable<Integer, Integer>();
			Hashtable<Integer, Hashtable<Integer, Integer>> otherBARequestsMap = new Hashtable<Integer, Hashtable<Integer,Integer>>();
			 */
			ArrayList<Integer> targetBAList = getTargetBusinessAreas(connection, dcrBA.getSystemId(), transmittalProcess.getTrnProcessId());
			for (Integer targetBAId : targetBAList){
				//String logDate = getLoggedDate(transmittalDate);
				updateTargetBA(connection, trnRequest, trnIdPrefix, dcrBA, targetBAId, dcrRequests, transmittalProcess,
						tBitsResMgr, contextPath, logDate);//, dtrRequestsMap, iDtrRequestsMap, otherBARequestsMap);
				/*String mappedBizAreas = mappedBusinessAreas;//;tpParams.get(TransmittalUtils.MAPPED_BUSINESS_AREAS);
				if ((mappedBizAreas == null) || (mappedBizAreas.trim().equals("")))
					throw new TBitsException("Invalid mapped business areas.");
				String[] mappedBAList = mappedBizAreas.split(TransmittalUtils.DELIMETER_COMMA);
				for (String tempIdStr : mappedBAList){
					int tempId = 0;
					if (!tempIdStr.trim().equals("")){
						tempId = Integer.parseInt(tempIdStr);
						if (targetBAId == tempId)
							updateTargetBA(connection, trnRequest, trnIdPrefix + formattedTrnReqId, dcrBA, targetBAId, dcrRequests, transmittalType, tBitsResMgr,
												aRequest.getContextPath(), dtrSysId);//, dtrRequestsMap, iDtrRequestsMap, otherBARequestsMap);
					}
				}*/			
			}

			//Insert all other required mappings of DTR request with other BA request Ids involved in this transmittal process.
			/*addOtherMappingsForDTRAndIDTRBA(connection, dcrBA, transmittalType, dtrSysId,
					dtrRequestsMap, otherBARequestsMap, false);
			addOtherMappingsForDTRAndIDTRBA(connection, dcrBA, transmittalType, 9,
					iDtrRequestsMap, otherBARequestsMap, true);*/

			//Secondary requests mapping.
			/*for(int dcrReqId : otherBARequestsMap.keySet()){
				Hashtable<Integer, Integer> tempMap = otherBARequestsMap.get(dcrReqId);
				for (int otherBASysId1 : tempMap.keySet()){
					for(int otherBASysId2 : tempMap.keySet()){
						if (otherBASysId1 != otherBASysId2){
							insertIntoRequestMapping(connection, otherBASysId1, tempMap.get(otherBASysId1), 
									otherBASysId2, tempMap.get(otherBASysId2), 0, 0, false);
						}
					}
				}
			}*/
		return trnIdPrefix;
	}


	private static String getUserLoginList (String toList) {
		String toUserLoginList = "";
		if (toList != null)
			for (String to : toList.split(",")){
				String userLogin = TransmittalUtils.getUserLoginFromAutoCompleteString(to);
				if (!userLogin.trim().equals(""))
					toUserLoginList = (toUserLoginList.equals("")) ? userLogin : toUserLoginList + "," + userLogin;
			}
		return toUserLoginList;
	}


	private ArrayList<Request> updateSourceDCRBusinessArea(Connection connection,
			TBitsResourceManager tBitsResMgr, String[] dcrRequestList,
			BusinessArea dcrBA, NCCTransmittalProcess transmittalType,
			Hashtable<String, String> tpParams, BusinessArea transBA,
			String trnIdPrefix, Request trnRequest, String transmittalDate) 
			throws DatabaseException, TBitsException, APIException, ParseException {
		
		int dcrSysId = dcrBA.getSystemId();
		String outwardDTNFieldIdStr = tpParams.get("dtnFieldId");
		Field dcrDTNField = null;		
		
		if ((outwardDTNFieldIdStr == null) || outwardDTNFieldIdStr.trim().equals(""))
			LOG.warn("Skipped populating outgoing DTN field as the appropriate field Id for the field was not found.");
		else{
			outwardDTNFieldIdStr = outwardDTNFieldIdStr.trim();
			try{
				dcrDTNField = Field.lookupBySystemIdAndFieldId(dcrSysId, Integer.parseInt(outwardDTNFieldIdStr));					
			}catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				throw new TBitsException(nfe);
			}
		}
		
		ArrayList<Field> extAttachmentFieldList = Field.lookupBySystemId(dcrSysId, true, DataType.ATTACHMENTS);

		ArrayList<Request> dcrRequests = new ArrayList<Request>(dcrRequestList.length);
		for (int index = 0;index < dcrRequestList.length; index++){
			UpdateRequest dcrUpdateRequest = new UpdateRequest();

			// Update the drawings sent to transmittal by changing the status and description. 
			Hashtable <String,String> tempParamTable = new Hashtable<String, String>();

			tempParamTable.put (Field.BUSINESS_AREA,  + dcrSysId + TransmittalUtils.EMPTY_STRING);
			tempParamTable.put (Field.REQUEST, dcrRequestList[index]);
			tempParamTable.put(Field.USER, "root");
			//String logDate = getLoggedDate(transmittalDate);
			if ((transmittalDate != null) && (!transmittalDate.trim().equals(""))) 
				tempParamTable.put (Field.LASTUPDATED_DATE, transmittalDate);

			int dcrReqId = Integer.parseInt(dcrRequestList[index]);
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
					
			//String baList = PropertiesHandler.getProperty(TRANSBIT_TBITS_TRANSMITTAL_DOCUMENT_ORIGINATING_DCRBA_LIST);
			//if (baList != null)
				//if (TransmittalUtils.isExistsInString(baList, dcrBA.getSystemPrefix())){
			String workflowOriginator = tpParams.get(WORKFLOW_ORIGINATOR);
			if ((workflowOriginator != null) && Boolean.parseBoolean(workflowOriginator.trim())){
				int submissionNo = dcrRequest.getExInt(FIELD_SUBMISSION_NO);
				submissionNo = submissionNo + 1;
				tempParamTable.put(FIELD_SUBMISSION_NO, submissionNo + "");
			}

			updatePostTransmittalFieldValues(connection, dcrSysId, dcrRequest, dcrSysId, transmittalType.getTrnProcessId(),
					trnIdPrefix, transBA.getSystemPrefix(), trnRequest.getRequestId(), tempParamTable, false);

			//String outwardDTNFieldIdStr = tpParams.get("dtnFieldId");
			/*if ((outwardDTNFieldIdStr == null) || outwardDTNFieldIdStr.trim().equals(""))
				LOG.warn("Skipped populating outgoing DTN field as the appropriate field Id for the field was not found.");
			else{
				outwardDTNFieldIdStr = outwardDTNFieldIdStr.trim();
				try{
					Field dcrDTNField = Field.lookupBySystemIdAndFieldId(dcrSysId, Integer.parseInt(outwardDTNFieldIdStr));
					if (dcrDTNField == null)
						throw new NullPointerException("Invalid field id provided through transmittal parameter, dtnFielId: " 
							+ outwardDTNFieldIdStr);*/
			if (dcrDTNField != null)
				tempParamTable.put(dcrDTNField.getName(), trnIdPrefix);
			/*}catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					throw new TBitsException(nfe);
				}
			}*/

			runTransmittalRules(connection, trnRequest.getRequestId(), trnRequest, dcrBA, dcrBA, dcrRequest, 
					tempParamTable, transmittalType, TransmittalUtils.DCR_BUSINESS_AREA, false);
			try{
				Request updatedDcrRequest = dcrUpdateRequest.updateRequest(connection, tBitsResMgr, tempParamTable);
				dcrRequests.add(index, updatedDcrRequest);
			}catch (NullPointerException npe){
				throw new TBitsException(npe);
			}
		}

		dcrRequests.trimToSize();
		return dcrRequests;
	}


	/**
	 * @param connection
	 * @param aRequest
	 * @param tBitsResMgr
	 * @param tpParams
	 * @param transBA
	 * @param aParamTable
	 * @param trnIdPrefix
	 * @param toList
	 * @param ccList
	 * @param transmittalSubject
	 * @param emailBody
	 * @param formattedTrnReqId
	 * @param dtnAttachment
	 * @param linkedRequests
	 * @return
	 * @throws TBitsException
	 * @throws NumberFormatException
	 * @throws DatabaseException
	 * @throws APIException
	 * @throws ParseException 
	 */
	private Request logRequestInTransmittalBA(Connection connection,
			String contextPath, TBitsResourceManager tBitsResMgr,
			Hashtable<String, String> tpParams, BusinessArea transBA,
			String trnIdPrefix,	String dtnAttachment, String deliverableAttachments,
			String otherAttachments, String toList, String ccList, 
			String transmittalSubject, String emailBody, String linkedRequests,
			String transmittalDate)
			throws TBitsException, NumberFormatException, DatabaseException,
			APIException, ParseException {
		//Create new AddRequest object to add new transmittal.
		AddRequest addReq = new AddRequest();
		addReq.setContext(contextPath);
		addReq.setSource(TBitsConstants.SOURCE_CMDLINE);
		Hashtable<String, String> aParamTable = new Hashtable<String, String>();
		aParamTable.put(Field.BUSINESS_AREA, transBA.getSystemPrefix());
		//aParamTable.put(Field.USER, "root");
		String dtnLogger = tpParams.get("dtnLogger");
		if ((dtnLogger == null) || dtnLogger.trim().equals("")){
			LOG.info("dtnLogger not found in transmittal process parameters, hence considering the logger of tBits.");
			if (user != null)
				dtnLogger = user.getUserLogin();
		}
		aParamTable.put(Field.USER, dtnLogger.trim());
		//aParamTable.put(Field.REQUEST, transReqId + TransmittalUtils.EMPTY_STRING);			
		aParamTable.put(Field.DESCRIPTION, "Transmittal for documents: " + linkedRequests); 
		aParamTable.put(Field.SUBJECT, transmittalSubject);

		aParamTable.put(Field.ASSIGNEE, toList);
		aParamTable.put(Field.SUBSCRIBER, ccList);	
		aParamTable.put(Field.STATUS, DTN_STATUS_TRANSMITTAL_COMPLETE);
		
		aParamTable.put(Field.ATTACHMENTS, otherAttachments);
		aParamTable.put(Field.IS_PRIVATE, TransmittalUtils.TRUE);

		aParamTable.put(COMMENTED_FILES, deliverableAttachments);
		aParamTable.put(DTN_FILE, dtnAttachment);
		aParamTable.put(Field.NOTIFY, KEYWORD_TRUE);
		aParamTable.put(DTN_NUMBER, trnIdPrefix);
		
		/*String dtnDate = "";
		if (transmittalDate.equals("")){
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("IST"));			
			Date d = c.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");				
			dtnDate = sdf.format(d);	
		}*/
				
		aParamTable.put(Field.LOGGED_DATE, transmittalDate);
		aParamTable.put(Field.LASTUPDATED_DATE, transmittalDate);
		
		
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
		//runPreTransmittalRules(connection, transReqId, aParamTable, tpParams, dcrBA, dcrRequestList, trnTypeName, true);

		StringBuffer desSB = new StringBuffer();
		desSB.append(emailBody);
		if ((emailBody!= null) && (!emailBody.equals(TransmittalUtils.EMPTY_STRING))){
			//desSB.append("\n\nDocument Control Register references: " + linkedRequests);
		}

		aParamTable.put(Field.DESCRIPTION,  desSB.toString());
		Request trnRequest = addReq.addRequest(connection, tBitsResMgr, aParamTable);
		return trnRequest;
	}


	/**
	 * @param transmittalDate
	 * @return
	 * @throws ParseException
	 */
	private String getLoggedDate(String transmittalDate) throws ParseException {
		String logDate = "";
		if((transmittalDate == null) || transmittalDate.trim().equals("")){
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("IST"));			
			Date d = c.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");				
			logDate = sdf.format(d);			
		}
		else {
			DateFormat df = new SimpleDateFormat(DD_MMM_YYYY);
			Date d = df.parse(transmittalDate);
			logDate = Timestamp.toCustomFormat(d, "yyyy-MM-dd HH:mm:ss");				
		}
		return logDate;
	}


	/*private void addOtherMappingsForDTRAndIDTRBA(Connection connection, BusinessArea dcrBA,
			NCCTransmittalType transmittalType, int dtrSysId, Hashtable<Integer, Integer> dtrRequestsMap,
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
*/
	private void updateTargetBA (Connection connection, Request transmittalRequest, String formattedDtnNumber, BusinessArea dcrBA, 
								int targetBASysId, ArrayList<Request> dcrRequests, NCCTransmittalProcess transmittalType, 
								TBitsResourceManager tBitsResMgr, String contextPath, String logDate)
									/*, Hashtable<Integer, Integer> dtrRequestsMap, Hashtable<Integer, Integer>iDtrRequestsMap,
									Hashtable<Integer, Hashtable<Integer, Integer>> otherRequestsMap) */
								throws DatabaseException, TBitsException, APIException{
		
		boolean isMatched = false;
		int dcrSysId = dcrBA.getSystemId();
		int dtrSystemId = transmittalType.getDtrSysId();
		
		//Get the target BA.
		BusinessArea currentBA = TransmittalUtils.lookupBySystemId(connection, targetBASysId);		
		//Field deliverableField = TransmittalUtils.lookupBySystemIdAndFieldId(connection, dcrSysId, deliverableFieldId);
		Field dNumField = TransmittalUtils.lookupBySystemIdAndFieldName(connection, targetBASysId, TransmittalUtils.FIELD_DRAWING_NO);
		Field targetDelAttachmentField = TransmittalUtils.getTargetBusinessAreaField(connection, transmittalType.getTrnProcessId(),
				dcrSysId, deliverableFieldId, targetBASysId);
		String dtnSysPrefix = BusinessArea.lookupBySystemId(transmittalRequest.getSystemId()).getSystemPrefix();
		ArrayList<Field> extAttachmentFieldList = Field.lookupBySystemId(targetBASysId, true, DataType.ATTACHMENTS);
		
		for (int i=0; i<dcrRequests.size(); i++){
			
			LOG.info("Updating target business area with Id: " + targetBASysId + ", post transmittal.");
			isMatched = false;
			
			int baType = TransmittalUtils.OTHER_BUSINESS_AREA;
			if(dtrSystemId == targetBASysId){
				baType = TransmittalUtils.DTR_BUSINESS_AREA;			
			}
			
			Request dcrRequest = dcrRequests.get(i);//Request.lookupBySystemIdAndRequestId (connection, dcrSysId, Integer.parseInt(dcrRequests[i].trim()));
			//int dcrRequestId = dcrRequest.getRequestId();
			String dNumValue = dcrRequest.get(TransmittalUtils.FIELD_DRAWING_NO);
			
			
			/*boolean isIDTRMapping = false;
			// Remove the hard coding, once the current design for mapping is accepted.
			if (currentBA.getSystemId() == 9){
				isIDTRMapping = true;
			}*/
			
			Hashtable <String,String> aParamTable = new Hashtable<String, String>();						
			Request matchedRequest = null;				 

			try{				
				/*CallableStatement cs = connection.prepareCall("stp_get_matched_request_id ?, ?, ?, ?, ?");
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
				}*/
				String matchDrawingByDrawingNumberQuery = "SELECT request_id from requests_ex where sys_id=? " +
															"and field_id=? and varchar_value=?";
				
				PreparedStatement cs = connection.prepareStatement(matchDrawingByDrawingNumberQuery);
				cs.setInt(1, targetBASysId);
				cs.setInt(2, dNumField.getFieldId());
				cs.setString(3, dNumValue);	
				ResultSet reqIdRS = cs.executeQuery();
				
				if (reqIdRS != null){
					while(reqIdRS.next()){
						int matchedRequestId = reqIdRS.getInt(1);
						if (matchedRequestId > 0)
							matchedRequest = Request.lookupBySystemIdAndRequestId(connection, targetBASysId, matchedRequestId);	
						
						if (matchedRequest != null){
							LOG.info("Found a match....." + matchedRequestId);
							isMatched = true;
							
							BusinessArea targetBA = TransmittalUtils.lookupBySystemId(connection, targetBASysId);
							aParamTable.put(Field.BUSINESS_AREA, targetBA.getSystemPrefix());
							aParamTable.put(Field.REQUEST, matchedRequest.getRequestId() + TransmittalUtils.EMPTY_STRING);							
							if ((logDate != null) && (!logDate.trim().equals("")))
								aParamTable.put (Field.LASTUPDATED_DATE, logDate);
							
							/*String delPrevAttachmentEx = AttachmentUtils.getAttachmentEx(connection, matchedRequest, deliverableFieldId);
							
							Collection<AttachmentInfo> prevDelAttachments = null;
							if ((delPrevAttachmentEx != null) && (!delPrevAttachmentEx.trim().equals(TransmittalUtils.EMPTY_STRING)))
								prevDelAttachments = AttachmentInfo.fromJson(delPrevAttachmentEx);
							else
								prevDelAttachments = null;*/
							
							//Retain existing attachments, during update.
							if(extAttachmentFieldList != null){
								for (Field extAttachmentField : extAttachmentFieldList){					
									if((extAttachmentField != null) 
											&& (targetDelAttachmentField != null)){
										String fieldName = extAttachmentField.getName();
										String fieldValue = matchedRequest.get(fieldName);
										 if((fieldValue != null) 
												 && (extAttachmentField.getFieldId() != targetDelAttachmentField.getFieldId()))
											aParamTable.put(fieldName, fieldValue);
									}
								}
							}
														
							//String delPrevAttachmentEx = AttachmentUtils.getAttachmentEx(connection, matchedRequest, deliverableFieldId);
							//Field deliverableField = Field.lookupBySystemIdAndFieldId(targetBASysId, deliverableFieldId);
							Collection<AttachmentInfo> prevDelAttachments = null;
							if (targetDelAttachmentField == null)
								throw new TBitsException("Deliverable field with id: " + deliverableFieldId +
										", could not be found, in Business Area with Id: " + targetBASysId);	
							String delAttJSONStr = matchedRequest.get(targetDelAttachmentField.getName());
							if(delAttJSONStr != null)
								prevDelAttachments = AttachmentInfo.fromJson(delAttJSONStr);
							
							Collection<AttachmentInfo> selectedDelAttachments = TransmittalUtils.getSelectedAttachments(
																									deliverableFileNames, i);
							TransmittalUtils.mergeAttachmentsLists(selectedDelAttachments, prevDelAttachments);
							String delAttachments = "[]";
							if (selectedDelAttachments != null)
								delAttachments = AttachmentInfo.toJson(selectedDelAttachments);
							
							//Update other attachments field.
							Collection<AttachmentInfo> prevAttachments = matchedRequest.getAttachments();
							Collection<AttachmentInfo> selectedAttachments = TransmittalUtils.getSelectedAttachments(reqFileNames, i);							
							TransmittalUtils.mergeAttachmentsLists(selectedAttachments, prevAttachments);							
							String sAttachments = "[]";
							if(selectedAttachments != null)
								sAttachments = AttachmentInfo.toJson(selectedAttachments);
														
							UpdateRequest updateRequest = new UpdateRequest();
							updateRequest.setContext(contextPath);
							updateRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
							
							aParamTable.put(Field.ATTACHMENTS, sAttachments);
							//Update deliverable attachment field.
							if (targetDelAttachmentField != null)
								aParamTable.put(targetDelAttachmentField.getName(), delAttachments);
							//aParamTable.put(deliverableField.getName(), delAttachments);
							updateFields(connection, transmittalType.getTrnProcessId(), dcrSysId, dcrRequest, targetBASysId,
									delAttachments, sAttachments, aParamTable);							
							updatePostTransmittalFieldValues(connection, dcrSysId, dcrRequest, targetBASysId, 
									transmittalType.getTrnProcessId(), formattedDtnNumber, dtnSysPrefix,
									transmittalRequest.getRequestId(), aParamTable, false);
							
							runTransmittalRules(connection, transmittalRequest.getRequestId(), transmittalRequest,
									BusinessArea.lookupBySystemId(targetBASysId), 
									dcrBA, dcrRequest, aParamTable, transmittalType, baType, false);
							
							updateRequest.updateRequest(connection, tBitsResMgr, aParamTable);							
							
							/*if(isIDTRMapping)
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
							}*/
						}		            		
					}					
				}
				else{
					LOG.info("No resultset found for the given drawing number: " + dNumValue 
							+ ", hence proceeding with logging a new request.");		            
				}

				cs.close();
				cs = null;				
			} catch (SQLException sqle) {
				StringBuilder message = new StringBuilder();

				message.append("An exception occurred while retrieving a matching request with matching drawing number: ")
				.append(dNumValue).append(" for Business area with systemId: " + targetBASysId);

				throw new DatabaseException(message.toString(), sqle);
			} catch (NullPointerException npe){
				npe.printStackTrace();
				throw new TBitsException("Could not update BA: " + currentBA.getSystemPrefix(), npe);
			} catch (Exception e){
				e.printStackTrace();
				throw new TBitsException("Could not update BA: " + currentBA.getSystemPrefix(), e);
			}
			
			if (!isMatched){

				LOG.info("No matching request exist, hence adding new...... " + i);	
				//updateFields(connection, dcrSysId, dcrRequest, targetBASysId, delAttachments, sAttachments, aParamTable);
				try{
					AddRequest addRequest = new AddRequest();            	           	
					addRequest.setContext(contextPath);
					addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);

					aParamTable.put(Field.BUSINESS_AREA, currentBA.getSystemPrefix());
					aParamTable.put(Field.USER, "root");				
					aParamTable.put(Field.SUBJECT, dcrRequest.getSubject());
					if ((logDate != null) && (!logDate.trim().equals(""))){
						aParamTable.put(Field.LOGGED_DATE, logDate);
						aParamTable.put(Field.LASTUPDATED_DATE, logDate);
					}

					Collection<AttachmentInfo> selectedDelAttachments = TransmittalUtils.getSelectedAttachments(deliverableFileNames, i);
					String delAttachments = "";
					if (selectedDelAttachments != null)
						delAttachments = AttachmentInfo.toJson(selectedDelAttachments);				

					//Update other attachments field.
					Collection<AttachmentInfo> selectedAttachments = TransmittalUtils.getSelectedAttachments(reqFileNames, i);				
					String sAttachments = "[]";
					if(selectedAttachments != null)
						sAttachments = AttachmentInfo.toJson(selectedAttachments);

					aParamTable.put(Field.ATTACHMENTS, sAttachments);				
					//Field dField = TransmittalUtils.lookupBySystemIdAndFieldId(connection, dcrSysId, deliverableFieldId);
					
					if (targetDelAttachmentField != null)
						aParamTable.put(targetDelAttachmentField.getName(), delAttachments);

					//Finally add the request					
					updateFields(connection, transmittalType.getTrnProcessId(), dcrSysId, dcrRequest, targetBASysId, 
							delAttachments, sAttachments, aParamTable);

					updatePostTransmittalFieldValues(connection, dcrSysId, dcrRequest, targetBASysId, transmittalType.getTrnProcessId(),
							formattedDtnNumber, dtnSysPrefix, transmittalRequest.getRequestId(), aParamTable, true);
					
					runTransmittalRules(connection, transmittalRequest.getRequestId(), transmittalRequest, currentBA, dcrBA, dcrRequest, aParamTable,
							transmittalType, baType, true);

					//Request newMappedRequest = 
					addRequest.addRequest(connection, tBitsResMgr, aParamTable);

					// Set-up to handle mapping of the request ids.				
					//Insert the mapping for the newly created requests.
					/*int newRequestId = newMappedRequest.getRequestId();
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
					 */
					isMatched = false;
				}catch(NullPointerException npe){
					npe.printStackTrace();
					throw new TBitsException("Could not update BA: " + currentBA.getSystemPrefix(), npe);
				}catch(Exception e){
					e.printStackTrace();
					throw new TBitsException("Could not update BA: " + currentBA.getSystemPrefix(), e);
				}
			}
		}
	}
	
	private void updateFields(Connection connection, int trnProcessId, int dcrSystemId, Request dcrRequest, int targetSystemId, String deliverableAttachments, 
								String otherAttachments, Hashtable<String, String>aParamTable) throws DatabaseException{
		// If its SEPCO BA delete all previous attachments in extended field attachment types.
		//clearAttachments(connection, dcrSystemId, dcrRequest, targetSystemId, aParamTable);
		Hashtable<String,String> targetBAFields = TransmittalUtils.getTargetBusinessAreaFields(connection, trnProcessId, dcrSystemId, targetSystemId);
		for(String fieldName : targetBAFields.keySet()){
			String tFieldName = targetBAFields.get(fieldName);
			String dcrFieldValue = dcrRequest.get(fieldName);
			aParamTable.put(tFieldName, dcrFieldValue);
		}		
	}
	
	private void updatePostTransmittalFieldValues(Connection connection, int dcrSystemId, Request dcrRequest, int targetSystemId, 
			int transmittalTypeId, String formattedDTNNumber, String dtnSysPrefix, int dtnRequestId, Hashtable<String, String> aParamTable, 
			boolean isAddRequest) throws DatabaseException, TBitsException{
		TransmittalUtils.getTargetBusinessAreaFieldsAndValues(connection, dcrSystemId, dcrRequest, targetSystemId, transmittalTypeId, 
				formattedDTNNumber, dtnSysPrefix, dtnRequestId, isAddRequest, aParamTable);	
	}
	
	@SuppressWarnings("unused")
	private void clearAttachments(Connection connection, int dcrSystemId, Request dcrRequest, 
			int targetSystemId, Hashtable<String, String> paramTable) throws DatabaseException{
		if (dcrSystemId == 5){			
			String[] attachmentFieldNames = {"AsBuilt","DCPLDecision","IPLEDecision","IPLPDecision","RFC","SiteDecision","TPSCDecision"};
			for(String fieldName : attachmentFieldNames){
				Field targetField = Field.lookupBySystemIdAndFieldName(targetSystemId, fieldName);
				if (targetField != null){
					paramTable.put(fieldName, "");
				}
			}		
		}
	}
	
	private String getTransmittalAttachments(String[] reqFileNames) 
					throws NumberFormatException, DatabaseException {
		StringBuilder tempAttachments = new StringBuilder();
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>();
				 
		if (reqFileNames != null)
			for (int i=0; i<reqFileNames.length; i++){
				String reqFileList = reqFileNames[i];
				if (reqFileList.trim().equals(TransmittalUtils.EMPTY_STRING))
					continue;
				else{					
					for (String reqFileInfo : reqFileList.split("<br3>")){
						String[] reqAttInfo = reqFileInfo.split("<br1>");
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

	private HashMap<Integer, String> getSelectedAttachmentsMap(String[] reqFileNames) 
	throws NumberFormatException, DatabaseException {
		HashMap<Integer, String>attachments = new HashMap<Integer, String>();
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>();

		if (reqFileNames != null)
			for (int i=0; i<reqFileNames.length; i++){
				String reqFileList = reqFileNames[i];
				if (reqFileList.trim().equals(TransmittalUtils.EMPTY_STRING))
					continue;
				else{
					StringBuilder tempAttachments = new StringBuilder();
					for (String reqFileInfo : reqFileList.split("<br3>")){
						String[] reqAttInfo = reqFileInfo.split("<br1>");
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
					tempAttachments.append(AttachmentInfo.toJson(trnAttCollection));
					attachments.put(i, tempAttachments.toString());
					trnAttCollection.clear();
				}					
			}
		return attachments;
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
	
	private String getTableData(int aSystemId, ArrayList<Request> requests,
			Hashtable<String, String> transmittalParams) throws NumberFormatException, DatabaseException, TBitsException {
		
		JSONArray reqArray = new JSONArray();
		for (Request request : requests) {
			
			JSONObject obj = new JSONObject();
			String docNo = request.get (TransmittalUtils.FIELD_DRAWING_NO);
			if ((docNo == null) || (docNo.trim().equals(TransmittalUtils.EMPTY_STRING)))
				throw new TBitsException("Drawing/Document number is missing for request id: " + request.getRequestId());//return null;
			
			int requestId = request.getRequestId();
			
			obj.put(Field.REQUEST, requestId);
			obj.put(DOC_NO,docNo);
			obj.put(REV_NO, request.get(TransmittalUtils.REVISION));
			obj.put(DESP,request.getSubject());
			
			String defaultAppCategory = "";
			if ((transmittalParams != null) && (!transmittalParams.isEmpty())){
				String delFieldIdStr = transmittalParams.get(DELIVERABLE_FIELD_ID);
				if ((delFieldIdStr == null) || (delFieldIdStr.trim().equals("")))
					throw new TBitsException("Could not find deliverable(attachment) field id.");
				try{
					int delAttFieldId = Integer.parseInt(delFieldIdStr);
					obj.put(DELIVERABLES, TransmittalUtils.getAttachmentList(aSystemId, request, delAttFieldId, TransmittalUtils.REVISION));
					defaultAppCategory = transmittalParams.get("defaultApprovalCategory");
				}catch(NumberFormatException nfe){
					throw new TBitsException("Invalid deliverable(attachment) field id.", nfe);
				}
			}
			else 
				obj.put(DELIVERABLES, "[]");
				
			obj.put(ATTACHMENTS, TransmittalUtils.getAttachmentList(aSystemId, request, TransmittalUtils.REVISION));		
			obj.put(COPIES, "1");//DELIMETER_DASH);
			obj.put(TRANSMITTAL_DOC_TYPE, "SC");			
			obj.put(TRANSMITTAL_APP_CATEGORY, defaultAppCategory);
			obj.put(SUMMARY, request.getSummary());
			reqArray.add(obj);
		}
		return reqArray.toString();
	}	
	
	/** 
	 * @param kth
	 * @throws DatabaseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws EngineException 
	 */
	private String getDTNAttachment (NCCTemplateHelper kth, String templateName) throws DatabaseException, 
			FileNotFoundException,IOException, EngineException{
		
		StringBuilder tempAttachments = new StringBuilder();
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>();		
		String pdfFilePath = TransmittalUtils.generateTransmittalNoteUsingBirt(templateName, kth, "Transmittal-Note");	
		File pdfFile = new File(pdfFilePath );
		Uploader uploader = new Uploader();
		AttachmentInfo trnNoteInfo = uploader.moveIntoRepository(pdfFile);
		trnAttCollection.add(trnNoteInfo);		
		tempAttachments.append(AttachmentInfo.toJson(trnAttCollection));
		return tempAttachments.toString();		
	}
		
	private ArrayList<Integer> getTargetBusinessAreas(Connection connection, int dcrSystemId, int trnProcessId) throws DatabaseException{
		
		ArrayList<Integer> targetSysIdList = new ArrayList<Integer>();
		try{
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT DISTINCT target_sys_id FROM "
					+ TransmittalUtils.TRN_SRC_TARGET_FIELD_MAPPING_TABLE + " where trn_process_id=? and src_sys_id=?");
			ps.setInt(1, trnProcessId);
			ps.setInt(2, dcrSystemId);
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
	private void runTransmittalRules(Connection connection, int transmittalId, Request transmittalRequest, BusinessArea currentBA,
			BusinessArea dcrBA, Request dcrRequest, Hashtable <String,String> paramTable, NCCTransmittalProcess transmittalType, int businessAreaType, 
			boolean isAddRequest)
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
	
	/* This methods runs various preLoader. It essentials is run just before rendering.
	 * @param businessAreaType 
	 * @param aRequest
	 * @param aResponse
	 * @param tagTable
	 * @throws TBitsException 
	 */
	
	private void runIPreControllers(Connection connection, BusinessArea dcrBA, ArrayList<Request> dcrRequestList, 
			NCCTransmittalDropDownOption ntp, ArrayList<NCCTransmittalProcess> transmittalTypes, NCCTransmittalProcess transmittalType) 
	throws TBitsException {
		PluginManager pm = PluginManager.getInstance();
        ArrayList<Class> transmittalRuleClasses = pm.findPluginsByInterface(ITransmittalController.class.getName());
        ArrayList<ITransmittalController> preTransmittalRuleLoaders = new ArrayList<ITransmittalController>();
        if(transmittalRuleClasses != null)
        {
        	for(Class preTransmittalRuleClass:transmittalRuleClasses)
        	{
        		ITransmittalController preTransmittalRule;
				try {
					preTransmittalRule = (ITransmittalController) preTransmittalRuleClass.newInstance();
					preTransmittalRuleLoaders.add(preTransmittalRule);
				} catch (InstantiationException e) {
					LOG.error("Could not instantiate the pre controller class: " + preTransmittalRuleClass.getClass().getName());
				} catch (IllegalAccessException e) {
					LOG.error("Could not access the controller class: " + preTransmittalRuleClass.getClass().getName());
				}
        	}
        }
        
        Comparator<ITransmittalController> c = new Comparator<ITransmittalController>(){

			public int compare(ITransmittalController arg0, ITransmittalController arg1) {
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
		
        for(ITransmittalController trnPreRuleLoader:preTransmittalRuleLoaders)
        {
        	trnPreRuleLoader.process(connection, dcrBA, dcrRequestList, ntp, transmittalTypes, transmittalType);
        }
	}
	
	static final int DESEIN_SYS_ID = 2;
	static final int NCC_SYS_ID = 4;
	static final int DCPL_SYS_ID = 5;
	static final int KNPL_SYS_ID = 6;
	static final int CSEPDI_SYS_ID = 8;
	static final int EDTD_SYS_ID = 7;
	static final int UC_NCC_SYS_ID = 19;
	
	static int getBusinessAreaId (){
		return 0;
		
	}
	
	static String getDTNNumberInfixOptions(int systemId){
		String options = null;
		switch (systemId){
			case DESEIN_SYS_ID: {
				options = getOptionString("DELHI", DELIMETER_DASH);
				options =  options + getOptionString("HYD", "HYD");
				break;			
			}
			case UC_NCC_SYS_ID:
			case NCC_SYS_ID: {
				options = getOptionString("Contract for Engineering, Transportation, Testing and Commissioning Services", "4");
				options = options + getOptionString("General", "0");
				options = options + getOptionString("Contract for supply(CIF)", "1");
				options = options + getOptionString("Contract for Supply(Ex-Works)", "2");
				options = options + getOptionString("Civil Construction and Erection works", "3");
				options = options + getOptionString(DELIMETER_DASH, DELIMETER_DASH);
				break;			
			}
			case DCPL_SYS_ID: {
				options = getOptionString(DELIMETER_DASH, DELIMETER_DASH);
				options = options + getOptionString("Mechanical", "M");
				options = options + getOptionString("Electrical", "E");
				options = options + getOptionString("General", "G");
				options = options + getOptionString("Instrumentation", "I");
				options = options + getOptionString("AC & Ventilation", "V");
				break;			
			}		
		}		
		return options;
	}
	
	static String getOptionString(String displayName, String value){
		String option = "";
		option = "<option value='" + value + "'>" + displayName + "</option>\n";
		return option;
	}
		
	public static void main(String[] args){
		/*NCCTransmittalPlugin iplPlugin = new NCCTransmittalPlugin();
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
		}*/
		/*SimpleDateFormat sdf = new SimpleDateFormat("yy");
		System.out.println("Current year: " + sdf.format(new Date()));
		System.out.println("Current Fin year: " + getCurrentFinancialYearString("-"));*/
		System.out.println("user list: " + getUserLoginList("saiprasad<A.V.Sai Prasad>, psrao<P.S.Rao>, hemasundar<B.Hema Sundar> "));
		
		
	}
	
//	public boolean createTransmittal(BusinessArea dcrBA, BusinessArea dtnBA, String[] dcrRequestList, 
//			String transmittalProcessName, String deliverableAttachments, String otherAttachments, 
//			String preDefinedTransmittalNumber ) throws TBitsException, APIException{
//		Connection connection = null;
//		Connection maxIdConn = null;
//		TBitsResourceManager tBitsResMgr = new TBitsResourceManager();
//
//		String trnIdPrefix = "";		
//		try {
//			int maxTransmittalId = 0;
//			NCCTransmittalProcess transmittalProcess = NCCTransmittalProcess.lookupTransmittalTypeBySystemIdAndName(
//					dcrBA.getSystemId(), transmittalProcessName);
//			maxIdConn = DataSourcePool.getConnection();
//			maxIdConn.setAutoCommit(false);
//
//			String formattedTrnReqId = TransmittalUtils.EMPTY_STRING;
//			if (transmittalProcess != null)
//				maxTransmittalId = NCCTransmittalProcess.getMaxTransmittalNumber(maxIdConn,
//						dtnBA.getSystemId(), transmittalProcess.getTrnMaxSnKey());
//			else
//				throw new TBitsException("No tranmsittal process exists with the name: " + transmittalProcessName);
//
//			formattedTrnReqId = TransmittalUtils.getFormattedStringFromNumber(maxTransmittalId);
//			trnIdPrefix = trnIdPrefix + formattedTrnReqId;
//
//			//Create Connection.
//			connection = DataSourcePool.getConnection();
//			connection.setAutoCommit(false);
//
//			String contextPath = "";//aRequest.getContextPath();
//			
//			Hashtable<String, String> tpParams = NCCTransmittalProcess.getTransmittalProcessParameters(dcrBA.getSystemId(), transmittalProcess.getTrnProcessId());
//			if (tpParams == null){
//				throw new TBitsException("Transmittal process parameters not found. Hence cannot continue.");
//			}
//
//			String toList = tpParams.get(TO_LIST);
//			String ccList = tpParams.get(CC_LIST);;
//			String remarks = tpParams.get(REMARKS);
//			String transmittalSubject = tpParams.get(TRANSMITTAL_SUBJECT);
//			String emailBody = tpParams.get(EMAIL_BODY);
//			String toAddress = tpParams.get(TO_ADDRESS);
//			String quantityList = "";
//			String approvalCategoryList = "";
//			String summaryList = "";
//			String documentTypeList = "";
//			String transmittalDate = "";
//			/*Connection connection,
//			String contextPath, TBitsResourceManager tBitsResMgr,
//			BusinessArea dcrBA, NCCTransmittalProcess transmittalProcess,
//			Hashtable<String, String> tpParams, BusinessArea transBA,
//			String trnIdPrefix, String[] dcrRequestList, String deliverableAttachments,
//			String otherAttachments, String quantityList, String approvalCategoryList, 
//			String summaryList,	String documentTypeList, String toList, String ccList,
//			String remarks, String transmittalSubject, String emailBody*/
//			formattedTrnReqId = createTransmittal(connection, contextPath,
//					tBitsResMgr , dcrBA, transmittalProcess, tpParams, dtnBA,
//					trnIdPrefix, dcrRequestList, deliverableAttachments, otherAttachments,
//					quantityList , approvalCategoryList , summaryList , documentTypeList ,  
//					toList, ccList, remarks, transmittalSubject, emailBody, toAddress, 
//					transmittalDate);
//
//			LOG.info("%%%%%%%%%%%%%%%%%%%%%%%%Finished updating all BA's&&&&&&&&&&&&&&&&&&&");
//
//			connection.commit();		
//			maxIdConn.commit();
//			tBitsResMgr.commit();
//		} catch (NumberFormatException e1) {			
//			e1.printStackTrace();
//			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);		
//			throw new TBitsException("Error occurred while creating transmittal: \n" + e1.getMessage());
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (DatabaseException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (TBitsException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (APIException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (SQLException e) {
//			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
//			APIException apie = new APIException();
//			apie.addException(new TBitsException("Unable to get connection to the database"));
//			throw apie;
//		} catch (ParseException e) {
//			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
//			e.printStackTrace();
//		} finally{
//			try {
//				if((connection != null) && (!connection.isClosed()))			
//					connection.close();
//				if((maxIdConn != null) && (!maxIdConn.isClosed()))
//					maxIdConn.close();
//			} catch (SQLException e) {
//				LOG.error(new Exception("Unable to close the connection to the database.", e));
//			}
//		}		
//		return true;
//	}
}
 
