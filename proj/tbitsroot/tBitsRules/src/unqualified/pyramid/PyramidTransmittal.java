package pyramid;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_ATTACHMENTDIR;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IRenderTask;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.config.Attachment;
import transbit.tbits.dms.AttachmentUtils;
import transbit.tbits.dms.TransmittalTemplate;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.TransmittalHandler;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.report.TBitsReportEngine;
import transbit.tbits.webapps.WebUtil;

/**
 * PyramidTransmittal is the plugin(class) which facilitates the whole transmittal process. During the transmittal process it
 * creates transmittal note, logs it into the transmittal business area (DTN), updates the corresponding 'Document Control 
 * Register'(DCR) drawings/documents after the transmittal creation and finally, creates a new/updates the existing documents/
 * drawings in the 'Latest' Business Area (LATEST).
 *  
 * <p>
 * For the transmittal process, the following are required,
 * <ul>
 * <li> Mapping of the corresponding DCR, DTN and LATEST business areas where, LATEST is optional.  
 * <li> Corresponding HTML templates used for the generation of transmittal note.
 * </ul>
 */
public class PyramidTransmittal implements TransmittalHandler {
	
	private static final String TRANSMITTAL_TYPES = "transmittal_types";
	private static final String TRANSMITTAL_CODE = "code";
	private static final String TRANSMITTAL_DOC_TYPE = "type";
	private static final String SHEETS = "sheets";
	private static final String ATTACHMENTS = "attachments";
	private static final String DESP = "desp";
	private static final String REV_NO = "rev_no";
	private static final String DOC_NO = "doc_no";
	private static final String REQUEST_ID = "request_id";
	private static final long serialVersionUID = 1L;
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	String[] reqFileNames;
	String tSysPrefix;
	String copySysPrefix;
	private String remarks;	
	private String emailBody;
	JSONObject result = new JSONObject();
	//JSONObject result = null;
		
	private static final String PYRAMID_LETTERS_FILE = "templates/pyramid-attachment-selection.htm";
		
	//Keywords
	private static final String NEAREST_PATH = "nearestPath";
	private static final String KEYWORD_SUCCESS = "success";
	private static final String KEYWORD_VALUE = "value";
	private static final String KEYWORD_TRUE = "true";
	private static final String KEYWORD_FALSE = "false";
	
	private static final String REQUEST_TYPE_REQUEST = "request";
	private static final String REQUEST_TYPE_QUESTION = "question";
	private static final String REQUEST_TYPE_VENDOR = "vendor";
	
	//private static final String STATUS_PENDING = "Pending";
	private static final String STATUS_CLOSED = "Closed";
	
	//Request parameters
	private static final String DOC_REGISTER_BA = "ba";
	private static final String TABLE_DATA = "tableData";
	private static final String REQUEST_LIST = "requestList";
	private static final String ATTACHMENT_LIST = "attachmentList";
	private static final String DRAWINGS_LIST = "docList";
	private static final String ACTION_LIST = "actionList";
	private static final String REVISION_LIST = "revList";
	private static final String COPIES_LIST = "copiesList";
	private static final String EMAIL_TO = "emailTo";
	private static final String EMAIL_CC = "emailCc";
	private static final String TRANSMIT_TO = "to";
	private static final String ASSIGNEE_LIST = "toList";
	private static final String SUBSCRIBER_LIST = "ccList";	
	private static final String CURRENT_DATE = "currentDate";
	private static final String SEND_TO_CLIENT = "Client";
	private static final String SEND_TO_FIELD = "Field";
	private static final String SEND_TO_VENDOR = "Vendor";
	private static final String PRINT_CLIENT = "ClientPrint";
	private static final String PRINT_FIELD = "FieldPrint";
	private static final String PRINT_VENDOR = "VendorPrint";
	private static final String PRINT = "Print";
	private static final String REQUEST_TYPE = "requestType";
	private static final String TRANSMITTAL_NUMBER = "TransmittalNumber";
	
	//Extended field names
	private static final String FIELD_DRAWING_NO = "DocumentNo";
	private static final String TRANSMITTED_TO_CLIENT = "TransmittedToClient";
	private static final String TRANSMIT_DOC_CLIENT = "TransmitDocClient";
	private static final String TRANSMIT_TO_FIELD = "TransmitToField";
	private static final String TRANSMITTED_TO_FIELD = "transmittedtofield";
	private static final String TRANSMITTED_TO_VENDOR = "TransmittedToVendor";
	private static final String TRANSMIT_TO_VENDOR = "TransmitToVendor";
	private static final String IFC_ISSUED = "IFCIssued";
	private static final String PAPER_SIZE = "PaperSize";
	private static final String NOOF_SHEETS = "NoofSheets";
	private static final String REVISION = "Revision";	
	private static final String DOC_TYPE = "DocType";
	
	//Status	
	private static final String STATUS_RECEIVED_FOR_INFORMATION = "ReceivedForInformation";
	private static final String STATUS_RESUBMISSION_REQUIRED = "ReSubmissionRequired";
	private static final String STATUS_APPROVED_WITH_COMMENTS = "ApprovedWithComments";
	private static final String STATUS_APPROVED = "Approved";
	private static final Object STATUS_APPROVAL = "active";
	private static final Object STATUS_IFC = "ifc";
	private static final Object STATUS_AS_BUILT = "AsBuilt";
	
	//Delimeters
	private static final String DELIMETER_COMMA = ",";
	private static final String DELIMETER_SEMICOLON = ";";
	private static final String DELIMETER_HASH = "#";	
	private static final String EMPTY_STRING = "";
	
	//DTag replacement keys
	private static final String SYS_PREFIX = "sysPrefix";
	private static final String REMARKS = "remarks";
	private static final String REQUEST_TABLE = "requestTable";
	private static final String TRANSMITTAL_NO = "transmittalNo";
	
	//File Extensions
	private static final String PDF = ".pdf";
	
	User user;
	private int transReqId;	
	
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		System.out.println("Transmittal get request##############################");
		PrintWriter out = aResponse.getWriter();
		
		try {	
			//User logger = WebUtil.validateUser(aRequest);			

			String requestType = aRequest.getParameter(REQUEST_TYPE);
			if ((requestType == null) || (requestType.trim().equals(EMPTY_STRING) == true)){
				out.println ("invalid request type");
				return;
			}else{
				requestType = requestType.trim();			
			}		
						
			handleGetRequest(aRequest, aResponse);
		}catch (DatabaseException e1) {
			LOG.error("Error while creating transmittal note:\n" + e1.getDescription());
			e1.printStackTrace();
		} /*catch (TBitsException e) {
			LOG.error("Exception occurred while validating user:\n" + e.getDescription());
			e.printStackTrace();
		}	*/
	}
	
	public void handleGetRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, DatabaseException {
		TransmittalTemplate trnTemplate = null;
		aResponse.setContentType("text/html");
		PrintWriter out = aResponse.getWriter();
		String sysPrefix = aRequest.getParameter(DOC_REGISTER_BA);
		
		String requestType = aRequest.getParameter(REQUEST_TYPE);
		requestType = requestType.trim();

		if ((sysPrefix == null) || (sysPrefix.trim().equals(EMPTY_STRING) == true)) {
			out.println("Invalid Business Area.");
			return;
		} else {
			sysPrefix = sysPrefix.trim();
		}

		BusinessArea ba;		

		ba = BusinessArea.lookupBySystemPrefix(sysPrefix);		

		if (ba == null) {
			out.println("Invalid Business Area or does not exist with the system prefix: " + sysPrefix);
			return;
		}
		int systemId = ba.getSystemId();
		
		/*if (!isDocumentController(logger, ba.getSystemId())){
			out.println("You are not authorized to create transmittal");
			return;
		}	*/		

		String requestList = aRequest.getParameter(REQUEST_LIST);

		if ((requestList == null) || requestList.trim().equals(EMPTY_STRING)) {
			out.println("Empty Requests List.");
			return;
		}
		requestList = requestList.trim();

		if (requestType.equalsIgnoreCase("selection")){
			DTagReplacer prepareTransmittal = null;
			File selectionFile = PyramidUtils.getResourceFile(PYRAMID_LETTERS_FILE);
			prepareTransmittal = new DTagReplacer(selectionFile);
			prepareTransmittal.replace(SYS_PREFIX, sysPrefix);
			prepareTransmittal.replace(REQUEST_LIST, requestList);
			prepareTransmittal.replace(NEAREST_PATH, WebUtil.getNearestPath(aRequest, EMPTY_STRING));
			String tableData = getTableData (ba.getSystemId(), requestList);
			if (tableData == null){
				out.println ("Please enter appropriate document number");
				return;
			}			
			prepareTransmittal.replace(TABLE_DATA, tableData);
			StringBuffer sb = getTransmittalTypeOptionsList(systemId);			
			prepareTransmittal.replace(TRANSMITTAL_TYPES, sb.toString());
			out.print(prepareTransmittal.parse(systemId));
		}
		else if (requestType.equalsIgnoreCase("preview")){
			String transmittalType = aRequest.getParameter(TRANSMIT_TO);
			if ((transmittalType == null) || (transmittalType.trim().equals(EMPTY_STRING))) {
				out.println("Please select client/field appropriately, to whom the transmittal has to be sent");
				return;
			} else {
				transmittalType = transmittalType.trim();				
				trnTemplate = TransmittalTemplate.lookupBySystemIdAndTemplateName(systemId, transmittalType);
				if (trnTemplate == null){
					out.print(KEYWORD_FALSE + DELIMETER_COMMA + "Could not find a transmittal template for " + transmittalType + " for DCR " + sysPrefix);
					return;
				}
			}	

			String actionList = aRequest.getParameter (ACTION_LIST);
			if ((actionList == null) || actionList.trim().equals(EMPTY_STRING)) {
				out.println("Actions to be taken for the selected documents not provided");
				return;
			}			
			actionList = actionList.trim();

			String copiesList = aRequest.getParameter (COPIES_LIST);
			if ((copiesList == null) || copiesList.trim().equals(EMPTY_STRING)) {
				out.println("Please enter the appropriate value for number of copies");
				return;
			}			
			copiesList = copiesList.trim();

			String attachmentList = aRequest.getParameter(ATTACHMENT_LIST);
			if ((attachmentList == null) || attachmentList.trim().equals(EMPTY_STRING)) {
				LOG.warn("No attachments selected");
				attachmentList = "";
			}
			else
				attachmentList = attachmentList.trim();

			DTagReplacer transmittalNote = new DTagReplacer(trnTemplate.getTemplateFileName());
						
			String dateStr = getCurrentDate();
			transmittalNote.replace(SYS_PREFIX, sysPrefix);
			transmittalNote.replace("sysPrefix-trunc", sysPrefix.substring(sysPrefix.length() - 3));
			transmittalNote.replace(REQUEST_LIST, requestList);
			transmittalNote.replace(TABLE_DATA, getTableData (ba.getSystemId(), requestList));
			transmittalNote.replace(TRANSMIT_TO, transmittalType);
			transmittalNote.replace(NEAREST_PATH, WebUtil.getNearestPath(aRequest, ""));
			transmittalNote.replace(ACTION_LIST, actionList);
			transmittalNote.replace(COPIES_LIST, copiesList);
			transmittalNote.replace(ATTACHMENT_LIST, attachmentList);	
			transmittalNote.replace(ASSIGNEE_LIST, trnTemplate.getAssigneeList());
			transmittalNote.replace(SUBSCRIBER_LIST, trnTemplate.getSubscribersList());
			transmittalNote.replace(CURRENT_DATE, dateStr); 
			out.print(transmittalNote.parse(systemId));				
		}	
	}

	/**
	 * @param systemId
	 * @return
	 * @throws DatabaseException
	 */
	@SuppressWarnings("unchecked")
	private StringBuffer getTransmittalTypeOptionsList(int systemId)
			throws DatabaseException {
		ArrayList<TransmittalTemplate>ttList = (ArrayList<TransmittalTemplate>) TransmittalTemplate.lookupBySystemId(systemId);			
		StringBuffer sb = new StringBuffer();
		String templateName = EMPTY_STRING;
		for (TransmittalTemplate tt : ttList){
			if (tt != null){
				templateName = tt.getTemplateName();
				if ((templateName != null) && (!templateName.trim().equals(EMPTY_STRING))){
					templateName = templateName.trim();
					sb.append("<Option value='").append(templateName).append("'>")
						.append(templateName).append("</Option>");
				}
				else
					continue;	
			}		
		}
		return sb;
	}

	/**
	 * @param logger
	 * @param ba
	 * @return
	 * @throws DatabaseException
	 */
	public static boolean isDocumentController(User logger, int aSystemId)
			throws DatabaseException {
		boolean isDocumentController = false;
		ArrayList<String> roleUserList= RoleUser.lookupBySystemIdAndUserId (aSystemId, logger.getUserId());
		for (String roleUser : roleUserList){
			if (roleUser.equals("Document Controller")){
				isDocumentController = true;
				break;
			}
			else
				continue;
		}
		return isDocumentController;
	}
	
	private String getTableData(int aSystemId, String requestList) throws IllegalArgumentException, DatabaseException{
		JSONArray reqArray = new JSONArray();
		for (String requestId : requestList.split(DELIMETER_COMMA)) {				
			Request request = Request.lookupBySystemIdAndRequestId(
					aSystemId, Integer.parseInt(requestId));
			JSONObject obj = new JSONObject();
			String docNo = request.get (FIELD_DRAWING_NO);
			if ((docNo == null) || (docNo.trim().equals(EMPTY_STRING)))
				return null;
			
			obj.put(REQUEST_ID, request.getRequestId());
			obj.put(DOC_NO,docNo);
			obj.put(REV_NO, request.get(REVISION));
			obj.put(DESP,request.getSubject());	
			obj.put(ATTACHMENTS, AttachmentUtils.getAttachmentList(aSystemId, request, REVISION));		
			obj.put(SHEETS, request.get (NOOF_SHEETS));
			obj.put(TRANSMITTAL_DOC_TYPE, request.get (DOC_TYPE));
			obj.put(TRANSMITTAL_CODE, getTransmittalCode(aSystemId, request));
			reqArray.add(obj);
		}
		return reqArray.toString();
	}
	
	private String getTransmittalCode(int aSystemId, Request request) throws DatabaseException{
		/*Field ifcField = Field.lookupBySystemIdAndFieldName(aSystemId, "IFCIssued");
		RequestEx ifcReqEx = request.getExtendedFields().get(ifcField);
		if (ifcReqEx.getBitValue())
			return "A";		*/
		
		Type statusType = request.getStatusId();
		if (statusType.getName().equals(STATUS_APPROVAL))
			return "1";
		if (statusType.getName().equals(STATUS_IFC))
			return "2";
		if (statusType.getName().equals(STATUS_AS_BUILT))
			return "3";
		if (statusType.getName().equals(STATUS_APPROVED))
			return "A";
		if (statusType.getName().equals(STATUS_APPROVED_WITH_COMMENTS))
			return "B";
		if (statusType.getName().equals(STATUS_RESUBMISSION_REQUIRED))
			return "C";
		if (statusType.getName().equals(STATUS_RECEIVED_FOR_INFORMATION))
			return "D";
				
		return "";
	}
		
	public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		handlePostRequest(aRequest, aResponse);
	}	
	
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
			
			String sysPrefix = aRequest.getParameter(DOC_REGISTER_BA);

			if ((sysPrefix == null) || (sysPrefix.trim().equals(EMPTY_STRING))) {
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, "Invalid Business Area.");
				out.print(result.toString());
				return;
			} else {
				sysPrefix = sysPrefix.trim();
			}
				 
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);		

			if (ba == null) {
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, sysPrefix + ": Invalid Business Area or business area does not exist");
				out.print(result.toString());
				return;
			}
								
			String mappedBusinessAreas = TransmittalTemplate.getMappedBusinessAreas(sysPrefix);
			String[] mappedBA = mappedBusinessAreas.split(",");
			tSysPrefix = mappedBA[0];
			copySysPrefix = mappedBA[1];			
			
			if ((tSysPrefix == null) || (tSysPrefix.trim().equals(EMPTY_STRING))) {
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, "Invalid system prefix of Transmittal Business Area");
				out.print(result.toString());				
				return;
			} else {
				tSysPrefix = tSysPrefix.trim();
			}
				 
			BusinessArea transBA = BusinessArea.lookupBySystemPrefix(tSysPrefix);		

			if (transBA == null) {
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, tSysPrefix + ": Invalid transmittal Business Area or business area does not exist");
				out.print(result.toString());				
				return;
			}		
				
			BusinessArea latestDrawingsBA = null;		
			if ((copySysPrefix == null) || copySysPrefix.trim().equals(EMPTY_STRING)){
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, "Invalid system prefix of latest Business Area");
				out.print(result.toString());				
				return;				
			}else{
				copySysPrefix = copySysPrefix.trim();
				latestDrawingsBA = BusinessArea.lookupBySystemPrefix(copySysPrefix);	
				if (latestDrawingsBA == null) {
					LOG.warn("Latest business area was not specified hence, ignoring updation of Latest business area after transmittal.");
					System.out.println("Latest business area was not specified hence, ignoring updation of Latest business area after transmittal.");
				}		
			}	
								
			String transmittalType = aRequest.getParameter(TRANSMIT_TO);
			if ((transmittalType == null) || (transmittalType.trim().equals(EMPTY_STRING))) {
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, transmittalType + "Please choose \"client/field\" to whom the transmittal letter has to be sent");
				out.print(result.toString());				
				return;
			} else {
				transmittalType = transmittalType.trim();
			}

			String requestList = aRequest.getParameter(REQUEST_LIST);

			if ((requestList == null) || requestList.trim().equals(EMPTY_STRING)) {
				out.print(KEYWORD_FALSE + DELIMETER_COMMA +"No document selected to be sent to transmittal. Please select the documents to be sent");
				return;
			}
			requestList = requestList.trim();
			String[] reqNo = requestList.split(DELIMETER_COMMA);
			
			String docList = aRequest.getParameter (DRAWINGS_LIST);
			if ((docList == null) || docList.trim().equals(EMPTY_STRING)) {
				out.print("Please select documents to be sent to transmittal");
				return;
			}			
			docList = docList.trim();
						
			String revList = aRequest.getParameter (REVISION_LIST);
			if ((revList == null) || revList.trim().equals(EMPTY_STRING)) {
				out.print("Revision numbers list not provided. Please enter proper revision numbers");
				return;
			}			
			revList = revList.trim();
			
			String actionList = aRequest.getParameter (ACTION_LIST);
			if ((actionList == null) || actionList.trim().equals(EMPTY_STRING)) {
				out.print("No actions found for the selected documents. Please select appropriate actions");
				return;
			}			
			actionList = actionList.trim();
			
			String copiesList = aRequest.getParameter (COPIES_LIST);
			if ((copiesList == null) || copiesList.trim().equals(EMPTY_STRING)) {
				out.print("Please enter the number of copies");
				return;
			}			
			copiesList = copiesList.trim();
			
			String attachmentList = aRequest.getParameter(ATTACHMENT_LIST);
			if ((attachmentList == null) || attachmentList.trim().equals(EMPTY_STRING)) {
				LOG.warn("No attachments selected.");
				attachmentList = EMPTY_STRING;
			}
			else{
				attachmentList = attachmentList.trim();
				reqFileNames = attachmentList.split(DELIMETER_COMMA);
			}
			
			String toAddress = aRequest.getParameter(EMAIL_TO);
			if ((toAddress == null)||(toAddress.trim().equals(EMPTY_STRING))){
				System.out.println ("No mailing address provided");
			}
			toAddress = toAddress.trim();
			
			String ccAddress = aRequest.getParameter(EMAIL_CC);
			if ((ccAddress ==null) || (ccAddress.trim().equals(EMPTY_STRING))){
				ccAddress = EMPTY_STRING;
			}
			else{
				ccAddress = ccAddress.trim();
			}
			
			remarks = aRequest.getParameter(REMARKS);
			if ((remarks ==null) || (remarks.trim().equals(EMPTY_STRING))){
				remarks = EMPTY_STRING;			
			}
			else{
				remarks = remarks.trim();
			}
			
			emailBody = aRequest.getParameter("emailBody");
			if ((emailBody ==null) || (emailBody.trim().equals(EMPTY_STRING))){
				emailBody = EMPTY_STRING;		
				LOG.info("No email body found for transmittal process");
			}
			else{
				emailBody = emailBody.trim();
			}			
			
			String ref = EMPTY_STRING;
			String transmitAttachments = EMPTY_STRING;
			String [] requestId = requestList.split(DELIMETER_COMMA); //request ids being logged
			String linkedRequests = getLinkedRequests(ba, requestId);
			Hashtable <String,String> aParamTable = new Hashtable<String, String>();
			try {			
				maxIdConn = DataSourcePool.getConnection();
				maxIdConn.setAutoCommit(false);
				transReqId = TransmittalTemplate.getMaxTransmittalNumber(maxIdConn, transBA.getSystemId());
				ref = getStringFromNumber(transReqId);
				transmitAttachments = getTransmittalAttachments(ba, requestList, docList, revList, copiesList, 
						actionList, attachmentList, ref, transmittalType);

				//Create Connection, FileResourceManager and MailResourceManager.
				connection = DataSourcePool.getConnection();
				connection.setAutoCommit(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
				throw new DatabaseException("Error occurred while fetching database connection.", e1);
			} catch (EngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			//Create new AddRequest object to add new transmittal.
			AddRequest addReq = new AddRequest();
			addReq.setContext(aRequest.getContextPath());
			addReq.setSource(TBitsConstants.SOURCE_CMDLINE);
			aParamTable.put(Field.BUSINESS_AREA, transBA.getSystemPrefix());
			aParamTable.put(Field.USER, user.getUserLogin());
			aParamTable.put(Field.REQUEST, transReqId + EMPTY_STRING);			
			aParamTable.put(Field.DESCRIPTION, "Transmittal for documents: " + linkedRequests); 
			aParamTable.put(Field.SUBJECT, "Transmittal Note to - " 
					+ transmittalType + ", Ref- " + ref);
			
			aParamTable.put(TRANSMITTAL_NUMBER, ref);
			if (transmittalType.equals(SEND_TO_CLIENT))
				aParamTable.put(Field.REQUEST_TYPE, REQUEST_TYPE_REQUEST);
			else if (transmittalType.equals(SEND_TO_FIELD))
				aParamTable.put(Field.REQUEST_TYPE, REQUEST_TYPE_QUESTION);
			else if (transmittalType.equals(SEND_TO_VENDOR))
				aParamTable.put(Field.REQUEST_TYPE, REQUEST_TYPE_VENDOR);
			
			aParamTable.put(Field.ASSIGNEE, toAddress);
			aParamTable.put(Field.SUBSCRIBER, ccAddress);	
			aParamTable.put(Field.STATUS, STATUS_CLOSED);			
			aParamTable.put(Field.ATTACHMENTS, transmitAttachments);
			aParamTable.put(Field.NOTIFY, KEYWORD_TRUE);	
					
			StringBuffer desSB= new StringBuffer();
			if ((emailBody!= null) || (!emailBody.equals(EMPTY_STRING))){	
				createEmailBody(ba, requestList, docList, revList, desSB, ref);	
				desSB.append("\n\nPyramid Document Control Register references: " + linkedRequests);
			}
			else{
				desSB.append("Transmittal Info: \n Updated ref number to:" + ref + "\n\n" + getTextTable(ba,requestList, docList, revList) 
						+ "\n\n The following document control register entries were transmitted: " + linkedRequests);
			}
			
			aParamTable.put(Field.DESCRIPTION,  desSB.toString());
			addReq.addRequest(connection, tBitsResMgr, aParamTable);
						
			for (int index = 0;index < requestId.length; index++){
				UpdateRequest dcrUpdateRequest = new UpdateRequest();
				
				// Update the drawings sent to transmittal by changing the status and description. 
				Hashtable <String,String> tempParamTable = new Hashtable<String, String>();
				int dcrSysId = ba.getSystemId();
				tempParamTable.put (Field.BUSINESS_AREA,  + dcrSysId + EMPTY_STRING);
				tempParamTable.put (Field.USER, user.getUserLogin());
				tempParamTable.put (Field.REQUEST, requestId[index]);
				tempParamTable.put (Field.DESCRIPTION,"Transmittal letter ref: " + ref 
						+ "(" + transBA.getSystemPrefix() + DELIMETER_HASH + transReqId + ")"
						+ "\n\nAssignee: "+ toAddress + "\n" + "Subscriber: "+ ccAddress + "\n");	
				tempParamTable.put (Field.DUE_DATE, EMPTY_STRING);
				if (transmittalType.equals(SEND_TO_CLIENT)){					
					tempParamTable.put (TRANSMITTED_TO_CLIENT, true + EMPTY_STRING);
					tempParamTable.put (TRANSMIT_DOC_CLIENT, false + EMPTY_STRING);
				}
				else if (transmittalType.equals(SEND_TO_FIELD)){
					tempParamTable.put (TRANSMIT_TO_FIELD, false + EMPTY_STRING);
					Field tmpField = Field.lookupBySystemIdAndFieldName(dcrSysId, TRANSMITTED_TO_FIELD);
					if (tmpField != null)
						tempParamTable.put (TRANSMITTED_TO_FIELD, true + EMPTY_STRING);
					tmpField = null;
				}
				else if (transmittalType.equals(SEND_TO_VENDOR)){
					Field vField1 = Field.lookupBySystemIdAndFieldName(dcrSysId, TRANSMIT_TO_VENDOR);
					if (vField1 != null)
						tempParamTable.put(TRANSMIT_TO_VENDOR,  false + EMPTY_STRING);
					Field vField2 = Field.lookupBySystemIdAndFieldName(dcrSysId, TRANSMITTED_TO_VENDOR);
					if (vField2 != null)
						tempParamTable.put(TRANSMITTED_TO_VENDOR,  true + EMPTY_STRING);
					vField1 = null;
					vField2 = null;
				}
				tempParamTable.put(Field.NOTIFY, KEYWORD_FALSE);
				
				dcrUpdateRequest.updateRequest(connection, tBitsResMgr, tempParamTable);
			}
						
			if (latestDrawingsBA != null)
				updateLatestBA(connection, tBitsResMgr, aRequest. getContextPath(), ba, latestDrawingsBA, 
						reqNo, toAddress, ccAddress, user, ref);
			
			result.put(KEYWORD_SUCCESS, KEYWORD_TRUE);
			result.put(KEYWORD_VALUE, ref);
			out.print(result.toString());	
			/*}
			else
			{
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, "You are not authorized to create transmittal. Please contact your system administrator.");
				out.print(result.toString());
			}*/
			try {				
				connection.commit();		
				maxIdConn.commit();
			} catch (SQLException e) {
				rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
				APIException apie = new APIException();
				apie.addException(new TBitsException(
						"Unable to get connection to the database"));
				throw apie;
			}		
			tBitsResMgr.commit();
		}catch (FileNotFoundException fnfe) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "FileNotFoundException: \n" + fnfe.getMessage());
			out.print(result.toString());
			return;
		}catch (DatabaseException dbe) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);			
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "Database Exception: \n" + dbe.getMessage());
			out.print(result.toString());		
			return;
		}catch (IOException ioe) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "IOException: \n" + ioe.getMessage());
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
	
	/**
	 * @param ba
	 * @param requestId
	 * @return
	 */
	private String getLinkedRequests(BusinessArea ba, String[] requestId) {
		String linkedRequests = "";
		for (int index=0; index<requestId.length; index++){
			if (index == 0)
				linkedRequests = ba.getSystemPrefix() + DELIMETER_HASH + requestId[index];
			else
				linkedRequests=linkedRequests + DELIMETER_COMMA + ba.getSystemPrefix() + DELIMETER_HASH + requestId[index];
		}
		return linkedRequests;
	}

	/**
	 * @param transId	Transmittal Id.
	 * @return			returns a String representing transmittal request id
	 */
	private String getStringFromNumber(int transId) {
		String ref;
		if (transId < 10)
			ref = "00" + transId;
		else if ((transId>=10) && (transId < 100))
			ref = "0" + transId;
		else
			ref = transId + EMPTY_STRING;
		return ref;
	}

	/**
	 * @param ba
	 * @param requestList
	 * @param docList
	 * @param revList
	 * @param desSB
	 */
	private void createEmailBody(BusinessArea ba, String requestList,
			String docList, String revList, StringBuffer desSB, String ref) {
		//desSB.append("\n Transmittal Letter #").append("PCE(IN)/ONGC/343 - ").append(ref).append("\n\n");
		int index1 = emailBody.indexOf("Sl. No.");
		int index2 = emailBody.indexOf ("Title");
		String tmpBody = emailBody.substring(0, index1);
		tmpBody = tmpBody + "\n" + getTextTable(ba,requestList, docList, revList);
		tmpBody = tmpBody + emailBody.substring(index2 + 5);
		desSB.append(tmpBody);
	}	
	
	//Replaced this method with the method in TransmittalTemplate class
	@SuppressWarnings("unused")
	private void getMappedBusinessAreas(String sysPrefix) throws DatabaseException{
		Connection connection = null;
    	try{
    		connection = DataSourcePool.getConnection();
    		
            CallableStatement cs = connection.prepareCall("stp_get_mapped_business_areas ?");
            cs.setString(1, sysPrefix);
            
            // execute method returns a flag . It is true if the first
            // result is a resultSet object.
            boolean flag = cs.execute();
            
            if (flag == true) {		            	
                ResultSet rs = cs.getResultSet();
                if ((rs != null) && (rs.next()!= false)){
                	tSysPrefix = rs.getString("transmittal_sys_prefix");
                	copySysPrefix = rs.getString("latest_ba_sys_prefix");
                }		                
                else{
                	System.out.println("Resultset is null");
                }
            }
            else{
            	System.out.println("Did not found any matches");		            	
            }	   
            cs.close();
            cs = null; 
    		
	    } catch (SQLException sqle) {
	        StringBuilder message = new StringBuilder();	
	        message.append("An exception occurred while retrieving mapped Business Areas ");	
	        throw new DatabaseException(message.toString(), sqle);
	    } finally {
	        try {
	            if (connection != null) {
	                connection.close();
	            }
	        } catch (SQLException sqle) {
	            LOG.warning("An Exception has occured while closing a request");
	        }
	    }		
	}	
	
	/**
	 * @param ba
	 * @param requestList
	 * @param docList
	 * @param revList
	 * @param copiesList
	 * @param actionList
	 * @param attachmentList
	 * @param refNo
	 * @param transmittalType
	 * @return Returns all the attachments to be included in a transmittal
	 * @throws FileNotFoundException
	 * @throws DatabaseException
	 * @throws IOException
	 * @throws EngineException 
	 */	
	private String getTransmittalAttachments(BusinessArea ba,
			String requestList, String docList, String revList,
			String copiesList, String actionList, String attachmentList,
			String refNo, String transmittalType) throws FileNotFoundException, DatabaseException, IOException, EngineException {
		String attachments = getTransmittalAttachments(ba, requestList, docList, revList, copiesList, actionList, attachmentList, refNo, EMPTY_STRING, transmittalType);
		return attachments;
	}
	
	
	/** 
	 * @param ba
	 * @param requestList
	 * @param docList
	 * @param revList
	 * @param copiesList
	 * @param actionList
	 * @param attachmentList
	 * @param refNo
	 * @param displayDate
	 * @param transmittalType
	 * @return Returns all the attachments to be included in a transmittal
	 * @throws DatabaseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws EngineException 
	 */
	private String getTransmittalAttachments (BusinessArea ba, String requestList, String docList,
			String revList, String copiesList, String actionList, String attachmentList, String refNo,
			String displayDate, String transmittalType) 
			throws DatabaseException, FileNotFoundException,IOException, EngineException{
		StringBuilder tempAttachments = new StringBuilder();
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>();
		//try {
		//First attaches all the selected attachments
		String[] requestArray = requestList.split(DELIMETER_COMMA);
		if (reqFileNames != null)
			for (int i=0; i<reqFileNames.length; i++){
				String reqFileList = reqFileNames[i];
				if (reqFileList.trim().equals(EMPTY_STRING))
					continue;
				else{					
					for (String reqFileInfo : reqFileList.split(DELIMETER_SEMICOLON)){
						Request req = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), Integer.parseInt(requestArray[i]));
						Collection<AttachmentInfo> reqAttachments = req.getAttachments();
						String[] reqAttInfo = reqFileInfo.split(":");
						String attName = reqAttInfo[0];
						int repoFileId = Integer.parseInt(reqAttInfo[1]);
						int attSize = Integer.parseInt(reqAttInfo[2]);
						
						for (AttachmentInfo ai : reqAttachments){
							if (ai.name.equals(attName) && (ai.size == attSize) && (ai.repoFileId == repoFileId)){
								AttachmentInfo tAI = new AttachmentInfo();
								tAI.name = attName;
								tAI.size = attSize;
								tAI.repoFileId = repoFileId;
								tAI.requestFileId = 0;
								trnAttCollection.add(tAI);
							}							
						}
					}					
				}//getRequestAttachments(ba, fileName, tempAttachments);
			}

		//Finally attaches the transmittal note		
		String tempDir = Configuration.findAbsolutePath (PropertiesHandler.getProperty(
				transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR));
		
		//Check if number of requests are more than 8, if so, divide it into multiple pages.
		// To do so after, create html for each request till you reach <=8.
		/*ArrayList<String> htmlPages = new ArrayList<String>();
		ArrayList<String> htmlRows = getHtmlTable(ba, requestList, docList, revList, copiesList, actionList);
		for (int i=0,count=1; i<htmlRows.size(); i++,count++){	
			buf.append(getHtmlContent (ba, refNo, displayDate, transmittalType, htmlRows.get(i), count, htmlRows.size()));
			htmlPages.add(buf.toString());
			buf.delete(0, buf.length());
		}*/
		
		DrawingList dl = new DrawingList(ba, requestList, docList, revList, copiesList, actionList);
		TBitsReportEngine tBitsEngine = new TBitsReportEngine();
		IReportEngine engine = tBitsEngine.getEngine();
		engine.getConfig().getAppContext().put("MyJavaScriptItem", dl);
		
		engine.changeLogLevel(Level.WARNING);

		// Report Design
		System.out.println("opening design.");
		IReportRunnable design = engine.openReportDesign(tempDir + "\\..\\tbitsreports\\TSR_ScriptedDataReport.rptdesign");

		IRunTask task = engine.createRunTask(design);
		task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, DrawingList.class.getClassLoader()); 
		
		String rptdoc = tempDir + "\\output.rptdocument";
		task.run(rptdoc);
		task.close();
		
		IReportDocument iReportDocument = engine.openReportDocument(rptdoc);
		IRenderTask rendertask = engine.createRenderTask(iReportDocument);
		
		//Set parent classloader report engine
		rendertask.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, DrawingList.class.getClassLoader());			
		System.out.println("Rendering...");
		
		
		String pdfFilePath = tempDir + File.separator + "Transmittal-note-" + refNo + PDF;		
		//convertHtmlToPdf(htmlPages, pdfFilePath);
		
		// Setup rendering to HTML
		PDFRenderOption options = new PDFRenderOption();
		//HTMLRenderOption options = new HTMLRenderOption();
		options.setOutputFileName(pdfFilePath);
		options.setOutputFormat("pdf");
		
		// Setting this to true removes html and body tags
		//options.setEmbeddable(false);
		rendertask.setRenderOption(options);

		// run and render report
		rendertask.render();
		if(rendertask.getStatus() != IRenderTask.STATUS_SUCCEEDED)
			throw new EngineException("The task didnt succeed.");
		
		System.out.println("Finished rendering and hence closing.");
		rendertask.close();
		System.out.println("Closed. Shuting down engine...");
		tBitsEngine.destroy();
		System.out.println("destroyed.");	
		
		File pdfFile = new File(pdfFilePath);
		Uploader uploader = new Uploader();
		AttachmentInfo trnNoteInfo = uploader.moveIntoRepository(pdfFile);
		trnAttCollection.add(trnNoteInfo);
		tempAttachments.append(AttachmentInfo.toJson(trnAttCollection));
		return tempAttachments.toString();		
	}
	
	@SuppressWarnings("unused")
	/*private void convertHtmlToPdf(String htmlContent, String pdfFilePath){
		OutputStream outputStream;
		Document doc = null;
		DocumentBuilder builder = null;
		try {
			outputStream = new FileOutputStream(pdfFilePath);		
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();			
			doc = builder.parse (new ByteArrayInputStream (htmlContent.getBytes()));
			
		    ITextRenderer renderer = new ITextRenderer();
		    renderer.setDocument(doc, null);		    
		    renderer.layout();
		    renderer.createPDF(outputStream);
		    outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			LOG.error("Exception while converting to transmittal note from 'html' to 'pdf'." );
			e.printStackTrace();
		}		
	}*/
	
	public class DrawingList{
		public int listSize = 0;
		String [] docName, revNo, copies, actionFor;
		public DrawingList(BusinessArea ba, String requestList, String docList, String revList, String copiesList, String actionList){			
		
			this.docName = docList.split(DELIMETER_COMMA);
			this.revNo = revList.split(DELIMETER_COMMA);
			this.copies= copiesList.split(DELIMETER_COMMA);		
			this.actionFor = actionList.split(DELIMETER_COMMA);
			this.listSize = docName.length;			
		}
		
		public String[] getDrawingDetail(int index){
			if ((index < this.listSize) && (index >= 0)){
				String[] drwDetail = new String[]{this.docName[index], this.revNo[index],
						this.copies[index], this.actionFor[index]};
				return drwDetail;
			}
			else
				return new String[]{};
		}
	}
		
	/**
	 * 
	 * @param htmlContent
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private static Document getDocumentFromStringUsingDocBuilder(String htmlContent)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();			
		Document doc = builder.parse (new ByteArrayInputStream (htmlContent.getBytes()));		
		return doc;
	}
	
	/**
	 * 
	 * @param htmlContent
	 * @return Document object from String containing HTML page using XMLResource 
	 */	
	/*private static Document getDocumentFromString(String htmlContent) {
        InputSource is = new InputSource(new BufferedReader(new StringReader(htmlContent)));
        Document dom = XMLResource.load(is).getDocument();
        return dom;
    }

	private static void convertHtmlToPdf(ArrayList<String> htmlContentPages, String pdfFilePath) {
		OutputStream os = null;
		try
		{
			os = new FileOutputStream(pdfFilePath);
			ITextRenderer renderer = new ITextRenderer();	

			for (int i=0; i<htmlContentPages.size(); i++)
			{
				Document dom = getDocumentFromString(htmlContentPages.get(i));
				renderer.setDocument(dom, null);
				renderer.layout();				
				if (i == 0)
					renderer.createPDF(os, false);
				else
					renderer.writeNextDocument();
			}
			renderer.finishPDF();
			os.close();
			os = null;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			LOG.error("Exception while converting to transmittal note from 'html' to 'pdf'." + e.getMessage());
			e.printStackTrace();
		}finally{
			if (os != null)
			{
				try
				{
					os.close();
				}
				catch (IOException e)
				{
					// ignore
				}
			}
		}

	}	*/
	
	//Gets the attachments of a request involved in the transmittal by coping it from the Document Control Register
	//BA to a temporary folder 
	private void getRequestAttachments (BusinessArea ba, String fileName, StringBuilder attachments){
		File inFile = new File (Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_ATTACHMENTDIR))
							+ "/" + ba.getSystemPrefix().toLowerCase() + "/" + fileName);
		
		//Extract displayName from the actual file name by truncating the request and action details in the
		//actual file name.
		String displayName = fileName.substring(fileName.indexOf("-",(fileName.indexOf("-",0) + 1))+ 1);

		if(!inFile.exists())
		{     		
			try {
				throw new FileNotFoundException("The attachment doesn't exist: " + inFile.getAbsolutePath());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}				
		}
		else
		{
			File outFile = new File (Configuration.findAbsolutePath(PropertiesHandler.getProperty(transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR)) + "/" + fileName);

			FileInputStream fromFile = null;
			try {
				fromFile = new FileInputStream (inFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			FileOutputStream toFile = null;
			try {
				toFile = new FileOutputStream (outFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			FileChannel fromChannel = fromFile.getChannel();
			FileChannel toChannel = toFile.getChannel();

			try {
				fromChannel.transferTo(0, fromChannel.size(), toChannel);	
				fromChannel.close();
				toChannel.close();
				fromFile.close();
				toFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			attachments.append(outFile.getAbsolutePath() + "\t" + displayName + "\n");
		}		
	}	
	
	public static void getAttachments (String fileName, StringBuilder attachments){
		File inFile = new File (Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_ATTACHMENTDIR))
							+ "/" + fileName);
		
		//Extract displayName from the actual file name by truncating the request and action details in the
		//actual file name.
		String displayName = fileName.substring(fileName.indexOf("-",(fileName.indexOf("-",0) + 1))+ 1);

		if(!inFile.exists())
		{     		
			try {
				throw new FileNotFoundException("The attachment doesn't exist: " + inFile.getAbsolutePath());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}				
		}
		else
		{
			File outFile = new File (Configuration.findAbsolutePath(PropertiesHandler.getProperty(transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR)) + "/" + fileName);

			FileInputStream fromFile = null;
			try {
				fromFile = new FileInputStream (inFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			FileOutputStream toFile = null;
			try {
				toFile = new FileOutputStream (outFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			FileChannel fromChannel = fromFile.getChannel();
			FileChannel toChannel = toFile.getChannel();

			try {
				fromChannel.transferTo(0, fromChannel.size(), toChannel);	
				fromChannel.close();
				toChannel.close();
				fromFile.close();
				toFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			attachments.append(outFile.getAbsolutePath() + "\t" + displayName + "\n");
		}		
	}	
	
	//Updates the latest drawings BA, once a transmittal is created.
	private void updateLatestBA (Connection connection, TBitsResourceManager tBitsResMgr, String contextPath, 
			BusinessArea ba, BusinessArea latestDrawingsBA, String[] reqNo, String assigneeList,
			String subscribersList, User curUser, String transmittalRefNumber) 
		throws TBitsException, APIException, DatabaseException, NumberFormatException{
		int sysId, prevSysId;
		sysId = latestDrawingsBA.getSystemId();
		prevSysId = ba.getSystemId();
		Hashtable <String, String> userListMapping = getUserMailListMapping(assigneeList + DELIMETER_COMMA + subscribersList);

		for (int i=0; i<reqNo.length; i++){
			Set<Entry<String, String>> userListSet = userListMapping.entrySet();
			Request dcrRequest = Request.lookupBySystemIdAndRequestId (connection, prevSysId, Integer.parseInt(reqNo[i].trim()));
			String dNumValue = dcrRequest.get(FIELD_DRAWING_NO);				
			String revNumber = dcrRequest.get(REVISION);
			String pSize = dcrRequest.get(PAPER_SIZE);
			String numSheets = dcrRequest.get(NOOF_SHEETS);
			String docGroup = dcrRequest.get(Field.REQUEST_TYPE);
			String discipline = dcrRequest.get(Field.CATEGORY);
			String docType = dcrRequest.get(DOC_TYPE);				
			String ifcIssued = dcrRequest.get(IFC_ISSUED);
			String status = dcrRequest.get(Field.STATUS);
			Type revType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(sysId, REVISION, revNumber);
			Type paperType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(sysId, PAPER_SIZE, pSize);			    
			Type dType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(sysId, DOC_TYPE, docType);
			
			Field dNumField = Field.lookupBySystemIdAndFieldName(sysId, FIELD_DRAWING_NO);
			Hashtable <String,String> aParamTable = new Hashtable<String, String>();

			Connection conn = null;
			Request matchedRequest = null;

			try{
				//conn = DataSourcePool.getConnection();
				CallableStatement cs = connection.prepareCall("stp_get_requestIdByDocNo ?, ?, ?");
				cs.setInt(1, sysId);
				cs.setInt(2, dNumField.getFieldId());
				cs.setString(3, dNumValue);
				// execute method returns a flag . It is true if the first
				// result is a resultSet object.
				cs.execute(); 
				ResultSet reqIdRS = cs.getResultSet();
				ArrayList<String> tmpList = getLatestBAAssigneeList(userListMapping);
				if (reqIdRS != null){
					while(reqIdRS.next()){System.out.println("Updating request in DTR: " + reqIdRS.getInt(REQUEST_ID));
						matchedRequest = Request.lookupBySystemIdAndRequestId(connection, sysId, reqIdRS.getInt(REQUEST_ID));		                	
						if (matchedRequest != null){
							Iterator<Entry<String, String>> iter = userListSet.iterator();
							ArrayList<RequestUser> matchedReqAssignees = matchedRequest.getAssignees();
							boolean existsRequest = isExistsRequest(matchedReqAssignees, iter, tmpList);
							if(existsRequest){
								fillRequestFieldValues(latestDrawingsBA, dcrRequest,dNumValue,
										numSheets, docGroup, discipline, ifcIssued, status,
										revType, paperType, dType, aParamTable, curUser.getUserLogin());
								aParamTable.put(Field.REQUEST, matchedRequest.getRequestId() + EMPTY_STRING);
								Collection<AttachmentInfo> selectedAttachments = getSelectedAttachments(ba, dcrRequest, i);
								Collection<AttachmentInfo> prevAttachments = matchedRequest.getAttachments();
								mergeAttachmentsLists(selectedAttachments, prevAttachments);
								String sAttachments = "";
								if(selectedAttachments != null)
									sAttachments = AttachmentInfo.toJson(selectedAttachments);
								updateSubscribers (connection, tBitsResMgr, ba, matchedRequest,
										sAttachments, userListMapping, aParamTable, contextPath, transmittalRefNumber);
							} 
						}		            		
					}

					if (!tmpList.isEmpty()){
						for (String usrLogin : tmpList){
							//Fills all the common fields into the param table
							String assignees = usrLogin.trim();
							fillRequestFieldValues(latestDrawingsBA, dcrRequest,dNumValue,
									numSheets, docGroup, discipline, ifcIssued, status,
									revType, paperType, dType, aParamTable, curUser.getUserLogin());
							aParamTable.put(Field.IS_PRIVATE, KEYWORD_TRUE);
							
							//Fills the remaining required params and returns AddRequest object
							String subscribers = userListMapping.get(assignees);
							aParamTable.put(Field.SUBJECT, dcrRequest.getSubject() + "-" + assignees);//", Date - " + getDate()			    		
							aParamTable.put(Field.ASSIGNEE, assignees); 
							if (!assignees.equals(subscribers))			    			
								aParamTable.put(Field.SUBSCRIBER, subscribers);	
							aParamTable.put(Field.NOTIFY, KEYWORD_FALSE);
							aParamTable.put(Field.DESCRIPTION, "Transmitted via transmittal number: " + 
									tSysPrefix + "#" + transmittalRefNumber);
							String latestAttachments = getSelectedAttachmentsJSONString(ba, dcrRequest, i);
							if ((latestAttachments != null) && (!latestAttachments.equals(EMPTY_STRING)))
								aParamTable.put(Field.ATTACHMENTS, latestAttachments);
							AddRequest addRequest = new AddRequest();            	           	
							addRequest.setContext(contextPath);
							addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
							//Finally add the request
							addRequest.addRequest(connection, tBitsResMgr, aParamTable);		            			
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
			} catch (ParseException e) {					
				e.printStackTrace();
			} finally {
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException sqle) {
					LOG.warning("An Exception has occured while closing a request");
				}
			}
		}
	}

	/**
	 * Merges two Collections. To be used when adding new attachments into a request. This method, checks
	 * if an attachments with the same name existed previously and modifies the new attachments collection.
	 * @param newAttachments
	 * @param prevAttachments
	 */
	private void mergeAttachmentsLists(Collection<AttachmentInfo> newAttachments,
			Collection<AttachmentInfo> prevAttachments) {
		if ((newAttachments != null) && (!newAttachments.isEmpty())){
			Collection<AttachmentInfo> oldAI = new ArrayList<AttachmentInfo>();
			for(AttachmentInfo ai : prevAttachments){
				for(AttachmentInfo cAI : newAttachments)
					if(ai.name.equals(cAI.name))
						cAI.requestFileId = ai.requestFileId;
					else
						oldAI.add(ai);     			
			}
			newAttachments.addAll(oldAI);     		
		}
	}

	/**
	 * @param ba
	 * @param index
	 * @return
	 */
	@SuppressWarnings("unused")
	private String getSelectedAttachments(BusinessArea ba, int index) {
		StringBuilder latestAttBuf = new StringBuilder();
		if (((reqFileNames != null) && (reqFileNames.length > 0)) && 
				(!reqFileNames[index].trim().equals(EMPTY_STRING))){
			for (String fileName : reqFileNames[index].split(DELIMETER_SEMICOLON))
				getRequestAttachments(ba, fileName, latestAttBuf);				
		}
		String latestAttachments = latestAttBuf.toString().trim();
		return latestAttachments;
	}
	
	private String getSelectedAttachmentsJSONString(BusinessArea ba, Request request, int index){
		Collection<AttachmentInfo> attachmentCollection = getSelectedAttachments(ba, request, index);
		return AttachmentInfo.toJson(attachmentCollection).toString();
	}
	
	private Collection<AttachmentInfo> getSelectedAttachments(BusinessArea ba, Request request, int index) {
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>();
		String reqFileList = reqFileNames[index];
		if (reqFileList.trim().equals(EMPTY_STRING))
			return trnAttCollection;
		else{
			for (String reqFileInfo : reqFileList.split(DELIMETER_SEMICOLON)){
				Collection<AttachmentInfo> reqAttachments = request.getAttachments();				
				String[] reqAttInfo = reqFileInfo.split(":");
				String attName = reqAttInfo[0];
				int repoFileId = Integer.parseInt(reqAttInfo[1]);
				int attSize = Integer.parseInt(reqAttInfo[2]);

				for (AttachmentInfo ai : reqAttachments){
					if (ai.name.equals(attName) && (ai.size == attSize) && (ai.repoFileId == repoFileId)){
						AttachmentInfo tAI = new AttachmentInfo();
						tAI.name = attName;
						tAI.size = attSize;
						tAI.repoFileId = repoFileId;
						tAI.requestFileId = 0;
						trnAttCollection.add(tAI);
					}							
				}
			}	
		}
		return trnAttCollection;
	}
	
	private ArrayList<String> getLatestBAAssigneeList(
			Hashtable<String, String> userListMapping) {
		ArrayList<String> assingneeList = new ArrayList<String>();
		Set<String> keySet = userListMapping.keySet();
		for (String key : keySet){
			assingneeList.add(key.trim());
		}
		return assingneeList;
	}

	/**
	 * @param iter
	 * @return
	 */
	@SuppressWarnings("unused")
	private ArrayList<String> getLatestBAAssigneeList(
			Iterator<Entry<String, String>> iter) {
		ArrayList<String> tmpList = new ArrayList<String>();
		while (iter.hasNext()){
			Entry<String, String> nextAssignee = iter.next();
			tmpList.add(nextAssignee.getKey().trim());
		}
		return tmpList;
	}
	
	/**
	 * @param matchedReqAssignees 
	 * @param iter 
	 * @param assigneeList TODO
	 * @throws DatabaseException 
	 * 
	 */
	private boolean isExistsRequest(ArrayList<RequestUser> matchedReqAssignees,
			Iterator<Entry<String, String>> iter, 
			ArrayList<String> assigneeList) throws DatabaseException{
		
		for (RequestUser ru : matchedReqAssignees){
			while(iter.hasNext()){	            					
				Entry<String, String> next = iter.next();
				String key = next.getKey();
				User tmpUser = ru.getUser();
				if (key.equals(tmpUser.getUserLogin()) || key.equals(tmpUser.getEmail())){
					assigneeList.remove(key.trim());
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @param contextPath
	 * @param ba
	 * @param iter
	 * @param prevReq
	 * @param attList
	 * @param aParamTable
	 * @return
	 */
	@SuppressWarnings("unused")
	private AddRequest fillAddReqParams(String contextPath, BusinessArea ba,
			Iterator<Entry<String, String>> iter, Request prevReq,
			ArrayList<Attachment> attList, Hashtable<String, String> aParamTable) {
		Entry<String, String> entry = iter.next();
		aParamTable.put(Field.SUBJECT, prevReq.getSubject() + "-" + entry.getKey());//", Date - " + getDate()			    		
		aParamTable.put(Field.ASSIGNEE, entry.getKey());
		String assignees = entry.getKey().trim();
		String subscribers = entry.getValue().trim();
		if (!assignees.equals(subscribers))			    			
			aParamTable.put(Field.SUBSCRIBER, entry.getValue());	
		aParamTable.put(Field.NOTIFY, KEYWORD_FALSE);
		aParamTable.put(Field.ATTACHMENTS, getAttachmentString(ba, attList));
		AddRequest addRequest = new AddRequest();            	           	
		addRequest.setContext(contextPath);
		addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
		return addRequest;
	}

	/**
	 * @param latestDrawingsBA
	 * @param dcrRequest 
	 * @param dNumValue
	 * @param numSheets
	 * @param docGroup
	 * @param discipline
	 * @param ifcIssued
	 * @param status
	 * @param revType
	 * @param paperType
	 * @param dType
	 * @param aParamTable
	 * @throws DatabaseException 
	 * @throws ParseException 
	 */
	private void fillRequestFieldValues(BusinessArea latestDrawingsBA,
			Request dcrRequest, String dNumValue, String numSheets, String docGroup,
			String discipline, String ifcIssued, String status, Type revType,
			Type paperType, Type dType, Hashtable<String, String> aParamTable, String userLogin) throws DatabaseException, ParseException {
		
		aParamTable.put(Field.BUSINESS_AREA, latestDrawingsBA.getSystemPrefix());
		aParamTable.put(Field.USER, userLogin);
		aParamTable.put(Field.CATEGORY, discipline);
		aParamTable.put(Field.STATUS, status);		
		if (docGroup!=null)
			aParamTable.put(Field.REQUEST_TYPE, docGroup);
		
		//int dcrSystemId = dcrRequest.getSystemId();
		ArrayList<Field> trnExtendedFields = Field.getExtendedFieldsBySystemId(latestDrawingsBA.getSystemId());
		for (Field f : trnExtendedFields){
			String fName = f.getName();
			String fieldValue = dcrRequest.get(fName);
			if ((fieldValue != null) && (!fieldValue.trim().equals(EMPTY_STRING))){
				int dataTypeId = f.getDataTypeId();				
				if ((dataTypeId == DataType.DATE) || (dataTypeId == DataType.DATETIME)){					
					User user = User.lookupAllByUserLogin(userLogin);
					Timestamp ts = new Timestamp(fieldValue, "yyyy-MM-dd HH:mm:ss");
					fieldValue = ts.toCustomFormat(user.getWebConfigObject().getWebDateFormat());	
				}
				aParamTable.put(fName, fieldValue);
			}
		}
	}
	
	private String getAttachmentString(BusinessArea ba, ArrayList<Attachment> attList){
		StringBuilder newAtt = new StringBuilder();
		ListIterator<Attachment> attListIterator = attList.listIterator();
		while (attListIterator.hasNext()){
			Attachment tempAtt = attListIterator.next();
			getRequestAttachments(ba, tempAtt.getName(), newAtt);					
		}	
		return newAtt.toString();
	}
	
	@SuppressWarnings("unused")
	private static String getDate(){
		Date date = Calendar.getInstance().getTime();
    	String dateStr = date.toString();
    	dateStr = dateStr.substring(4);
    	dateStr = dateStr.substring(0,(dateStr.length() - 4));
    	return dateStr.replace(":", ".");
	}
	
	private void updateSubscribers (Connection connection, TBitsResourceManager tBitsResMgr, BusinessArea ba, Request matchedRequest, 
			String attachments, Hashtable <String, String> userListMapping, Hashtable<String, String> aParamTable, String contextPath, 
			String transmittalRefNumber)
			throws TBitsException, APIException{		
		String ccList = EMPTY_STRING;
		String subscribers = EMPTY_STRING;
		String assignee = EMPTY_STRING;
		ArrayList<RequestUser> assignees = matchedRequest.getAssignees();
		if (assignees != null){
			for (RequestUser tempUser : assignees){		
				try {
					assignee = tempUser.getUser().getUserLogin();
				} catch (DatabaseException e) {
					e.printStackTrace();
				}

				//if (isExistsInAnyMailList(assigneeEmail)){														
				subscribers = userListMapping.get(assignee);
				if (subscribers == null)
					subscribers = EMPTY_STRING;
				ccList = prepareCcList(matchedRequest.getSubscribers(), subscribers);		
				if ((assignee != null) && (!assignee.equals(EMPTY_STRING))){
						String tempSubsList = EMPTY_STRING;
						for (String subscriber : subscribers.split(DELIMETER_COMMA)){
							if (isUserExists(matchedRequest.getSubscribers(), subscriber)){
								continue;
							}
							else{
								tempSubsList = (tempSubsList.equals(EMPTY_STRING)) 
								? subscriber : tempSubsList + DELIMETER_COMMA + subscriber; 
							}
						}

					//Add subscribers
					if (!tempSubsList.equals(EMPTY_STRING)){
						ArrayList <RequestUser> rList = matchedRequest.getSubscribers();								
						aParamTable.put(Field.SUBSCRIBER, getSubscribers(rList) + DELIMETER_COMMA +  subscribers);
					}
					else{
						aParamTable.remove(Field.SUBSCRIBER);
					}
					//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%CCLIST: " + ccList);							
					aParamTable.put(Field.CC, ccList);
					aParamTable.put(Field.NOTIFY, KEYWORD_FALSE);
					aParamTable.put(Field.DESCRIPTION, "Transmitted via transmittal number: " + 
							tSysPrefix + "#" + transmittalRefNumber);
					if(attachments != null)
						if (!attachments.equals(EMPTY_STRING))
							aParamTable.put(Field.ATTACHMENTS, attachments);
					UpdateRequest updateRequest = new UpdateRequest();
					updateRequest.setContext(contextPath);
					updateRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
					//update matching requests
					updateRequest.updateRequest(connection, tBitsResMgr, aParamTable);	
				}
				break;
			}
		}
	}
	
	private String getSubscribers(ArrayList<RequestUser> subscribersList){
		String existingSubscribers = EMPTY_STRING;
		for (RequestUser rUser: subscribersList){
			try {
				existingSubscribers = (existingSubscribers.equals(EMPTY_STRING)) 
											? rUser.getUser().getUserLogin() 
													: existingSubscribers + DELIMETER_COMMA + rUser.getUser().getUserLogin();
			} catch (DatabaseException e) {
				LOG.severe("Exception occured while retrieving subscribers");
				e.printStackTrace();
			}
		}
		return existingSubscribers;
	}
		
	private boolean isUserExists(ArrayList<RequestUser> ruList, String userName){
		for (RequestUser ru : ruList){
			try {
				User usr = ru.getUser();				
				if ((usr.getUserLogin().equals(userName) || (usr.getEmail().equals(userName)))){
					return true;
				}
				else 
					return false;
			} catch (DatabaseException e) {				
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private String prepareCcList(ArrayList<RequestUser> ruList, String userList){
		String ccList = EMPTY_STRING;		
		User user = null;
		for (RequestUser ru : ruList){
			boolean flag = false;
			try {
				user = ru.getUser();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			for (String usrName : userList.split(DELIMETER_COMMA)){				
				if ((usrName.equals(user.getEmail())) || usrName.equals(user.getName()))
					flag = true;
				else 
					continue;
			}
			if (!flag)
				ccList = (ccList.equals(EMPTY_STRING)) ? user.getName() : ccList + user.getName();
		}
		return ccList;
	}
		
	private Hashtable<String, String> getUserMailListMapping(String usersList) throws DatabaseException{
		Hashtable<String,String> userListMapping = new Hashtable<String,String>();
		for (String usr : usersList.split(DELIMETER_COMMA)){
    		User mailListUsr = null;
    		if (usr.trim().equals(EMPTY_STRING))
    			continue;
    		
    		ArrayList<User> mailingLists = getMailList(usr);
    		if (mailingLists != null){
    			Iterator<User> mailingListsIter = mailingLists.iterator();
    			if (mailingListsIter.hasNext())
    				mailListUsr = mailingListsIter.next();           	

    			if (mailListUsr != null){           
    				String key = mailListUsr.getUserLogin();
    				if (userListMapping.containsKey(key)){
    					String value = userListMapping.get(key);
    					value = value + DELIMETER_COMMA + usr;
    					userListMapping.put(key, value);
    				}
    				else            				
    					userListMapping.put(mailListUsr.getUserLogin(), usr);
    			}
    			else{
    				if (!userListMapping.containsKey(usr))
    					userListMapping.put(usr,usr);            			
    			}
    		}
    		else{
    			userListMapping.put(usr, usr);
    		}
		}
		return userListMapping;
	}
	
	private static ArrayList<User> getMailList(String userName) {
		User user = null;
		ArrayList<User> mailList = null;
		try {
			user = User.lookupByEmail(userName);
			if (user == null)
				user = User.lookupAllByUserLogin(userName);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		if (user != null){
			mailList = MailListUser.getMailListsByDirectMembership(user.getUserId());
		}
		return mailList;
	}
		
	//Creates a text table to be added into the description in the transmittal BA during creation of a transmittal
	private String getTextTable(BusinessArea ba, String reqList, String docList, String revList){
		int serialNo= 0;
		Request req = null;
		StringBuffer tableRows= new StringBuffer();		
		String [] reqNo = reqList.split(DELIMETER_COMMA);
		String [] docName = docList.split(DELIMETER_COMMA);
		String [] revNo = revList.split(DELIMETER_COMMA);
		//String [] desp = despList.split(DELIMETER_COMMA);
		//String [] copies= copiesList.split(DELIMETER_COMMA);		
		//String [] actionFor = actionList.split(DELIMETER_COMMA);
		tableRows.append("Sl. No.  Document Name  Rev. No. \t Description \n");
		
		for (int i=0; i <docName.length; i++){	
			serialNo = serialNo + 1;
			try {
				req = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), Integer.parseInt(reqNo[i]));
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			tableRows.append("\t" + serialNo + "\t" + docName[i] + "\t" + revNo[i] + "\t\t" + req.getSubject() + "\n");
		}
		return (tableRows.toString());
	}
	
	//Create HTML table rows to be added into transmittal note HTML attachment
	private ArrayList<String> getHtmlTable(BusinessArea ba, String requestList, String docList, String revList, String copiesList, String actionList){
		int serialNo= 0;	
		Request req = null;
		int numOfRows = 8;
		ArrayList<String> rows = new ArrayList<String>();
		StringBuffer tableRows = new StringBuffer();
		String [] reqNo = requestList.split(DELIMETER_COMMA);
		String [] docName = docList.split(DELIMETER_COMMA);
		String [] revNo = revList.split(DELIMETER_COMMA);
		String [] copies= copiesList.split(DELIMETER_COMMA);		
		String [] actionFor = actionList.split(DELIMETER_COMMA);	
		int length = docName.length;
		
		for (int i=0,rowCount=1; i <length; i++,rowCount++){	
			serialNo = serialNo + 1;

			try {
				req = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), Integer.parseInt(reqNo[i]));
			} catch (DatabaseException e) {
				e.printStackTrace();
			}

			tableRows.append("<tr align=\"center\" style=\"height:10%; font-family:Arial; font-size:12px;\"><td style=\"width:7%;text-align:center; border: solid 1px #000000\">" + serialNo + "</td>" +
					"<td style=\"width:20%; text-align:center; border: solid 1px #000000\">" + docName[i] + "</td>" +
					"<td style=\"width:5%;text-align:center; border: solid 1px #000000\">" + req.get(NOOF_SHEETS)+ "</td>" + 
					"<td style=\"width:35%;text-align:left; border: solid 1px #000000\">" + req.getSubject().replaceAll("&", "&amp;")+ "</td>" +
					"<td style=\"width:6%;text-align:center; border: solid 1px #000000\">" + revNo[i]+ "</td>" + 
					"<td style=\"width:10%;text-align:center; border: solid 1px #000000\">" + req.get(DOC_TYPE)+ "</td>" + 
					"<td style=\"width:7%;text-align:center; border: solid 1px #000000\">" + copies[i] + "</td>" +
					"<td style=\"width:10%;text-align:center; border: solid 1px #000000\">" + actionFor[i] + "</td></tr>");
			
			if (rowCount == numOfRows){
				insertEmptyRows(rowCount, tableRows);
				rows.add(tableRows.toString());
				rowCount = 0;
				tableRows.delete(0, tableRows.length());
			}
			else if (i == (length - 1)){
				insertEmptyRows(rowCount, tableRows);
				rows.add(tableRows.toString());
			}
			else
				continue;
		}
				
		return rows;			
	}

	/**
	 * @param numOfRows
	 * @param tableRows
	 */
	private void insertEmptyRows(int numOfRows, StringBuffer tableRows) {
		int emptyRowCount = 10 - numOfRows;
		for (int j=0; j<emptyRowCount; j++){
			tableRows.append("<tr style=\"height:10%\"><td><br /></td></tr>");
		}
	}
	
	private String getHtmlContent (BusinessArea ba, String ref, String displayDate, String transmittalType, 
			String htmlTable, int pageNumber, int pageCount) 
			throws DatabaseException, FileNotFoundException, IOException{
		DTagReplacer transmittalNotePrint = null;
		String dateStr = "";
		if (displayDate.trim().equals(""))
			dateStr = getCurrentDate();
		else 
			dateStr = displayDate;
		TransmittalTemplate template = null;
		int systemId = ba.getSystemId();
		if (transmittalType.equals(SEND_TO_CLIENT))
			template = TransmittalTemplate.lookupBySystemIdAndTemplateName(systemId, PRINT_CLIENT);
		else if (transmittalType.equals(SEND_TO_FIELD))
			template = TransmittalTemplate.lookupBySystemIdAndTemplateName(systemId, PRINT_FIELD);
		else if (transmittalType.equals(SEND_TO_VENDOR))
			template = TransmittalTemplate.lookupBySystemIdAndTemplateName(systemId, PRINT_VENDOR);
		if (template == null)
			template = TransmittalTemplate.lookupBySystemIdAndTemplateName(systemId, PRINT);
		transmittalNotePrint = new DTagReplacer(template.getTemplateFileName());	
		transmittalNotePrint.replace(NEAREST_PATH, WebUtil.getNearestPath(EMPTY_STRING));
		String systemPrefix = ba.getSystemPrefix();
		transmittalNotePrint.replace(SYS_PREFIX, systemPrefix.substring(systemPrefix.length() - 3));
		transmittalNotePrint.replace(TRANSMITTAL_NO, ref);
		transmittalNotePrint.replace(REQUEST_TABLE, htmlTable);
		transmittalNotePrint.replace(REMARKS, remarks.replaceAll("&", "&amp;"));
		transmittalNotePrint.replace(CURRENT_DATE, dateStr);
		transmittalNotePrint.replace("pageCount", pageNumber + " of " + pageCount);
		return(transmittalNotePrint.parse(0));
	}

	/**
	 * @return
	 */
	private String getCurrentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");		
		String dateStr = sdf.format(new Date());
		return dateStr;
	}
		
	public static void main(String[] args) throws DatabaseException, TBitsException, APIException{
		/*BusinessArea ba = BusinessArea.lookupBySystemPrefix("DCR343");
		BusinessArea latestDrawingsBA = BusinessArea.lookupBySystemPrefix("LATEST343");
		String[] reqNo = "5, 6".split(",");
		//"41, 42, 44, 159, 45, 162, 157, 116, 2, 3, 4, 5, 6 , 223,224"
		String assigneeList = "ajitjaink,guest";
		String subscribersList = "santoshmahajan,sajint,gaurav.patel,prashant.dalwale";
		User usr = User.lookupAllByUserLogin("document.controller");
		
		PyramidTransmittal pt = new PyramidTransmittal();		
		pt.updateLatestBA("", ba, latestDrawingsBA, reqNo, assigneeList, subscribersList, usr);*/
		/*User user;
		try {
			user = User.lookupAllByUserLogin("document.controller");
			System.out.println("User: " + user.getUserLogin() + " id: " + user.getUserId());			
			boolean isTrue = PyramidTransmittal.isDocumentController(user, 6);
			System.out.println("Is doc controller: " + isTrue);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		testAddRequest(args);
	}
		
	/**
	 * @param args
	 */
	private static void testAddRequest(String[] args) {
		Connection connection = null;
		TBitsResourceManager tBitsResMgr = new TBitsResourceManager();
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String[] ba = {"DTN345", "DCR345", "DTR345"};
			
			Hashtable<String, String> aParamTable = new Hashtable<String, String>();
			aParamTable.put(Field.USER, "root");
			aParamTable.put(Field.BUSINESS_AREA, ba[0]);
			for (int i=2; i<4; i++){
				aParamTable.put(Field.SUBJECT, "DTN-TEST" + i);
				aParamTable.put(Field.DESCRIPTION, "Testing update in DTN" + i);
				UpdateRequest dtnReq = new UpdateRequest();
				dtnReq.setSource(TBitsConstants.SOURCE_CMDLINE);
				dtnReq.updateRequest(connection, tBitsResMgr, aParamTable);
			}
			
			System.out.println("Completed adding requests");
			
			/*aParamTable.clear();
			UpdateRequest dcrReq = new UpdateRequest();
			aParamTable.put(Field.USER, "root");
			aParamTable.put(Field.BUSINESS_AREA, ba[1]);
			aParamTable.put(Field.DESCRIPTION, "Testing update in DCR");
			aParamTable.put(Field.REQUEST, "139");
			dcrReq.setSource(TBitsConstants.SOURCE_CMDLINE);
			dcrReq.updateRequest(connection, fileResMgr, mailResMgr, aParamTable);
			
			aParamTable.clear();			
			aParamTable.put(Field.USER, "root");			
			aParamTable.put(DOC_TYPE, "Document");
			aParamTable.put(PAPER_SIZE, "A4");
			aParamTable.put(NOOF_SHEETS, "1");
			aParamTable.put(REVISION, "A");
			aParamTable.put(Field.NOTIFY, KEYWORD_FALSE);
			aParamTable.put(Field.STATUS, "Approved");
			aParamTable.put(TRANSMIT_DOC_CLIENT, true + "");
			System.out.println("Before adding request");				
			AddRequest testReq = new AddRequest();				
			aParamTable.put(Field.BUSINESS_AREA, ba[2]);
			aParamTable.put(Field.SUBJECT, "sub"+3);
			//aParamTable.put(Field.CATEGORY, args[i]);
			StringBuilder sb = new StringBuilder();
			getAttachments("tbitsPool_2009-04-14.log", sb);
			aParamTable.put(Field.ATTACHMENTS, sb.toString());		
			testReq.setSource(TBitsConstants.SOURCE_CMDLINE);
			Request req = testReq.addRequest(connection, fileResMgr, mailResMgr, aParamTable);*/
			//System.out.println("Added request: " + req.getSubject());
			
			//throw new APIException();
			/*connection.commit();
			mailResMgr.commit();
			fileResMgr.commit();
			connection.setAutoCommit(true);*/
		}catch (APIException e) {
			e.printStackTrace();
			try {
				if(connection != null){
					connection.rollback();
				}
				tBitsResMgr.rollback();				
			} catch (SQLException e1) {
				e1.printStackTrace();
			}	
		}catch (SQLException sqle) {
			try {
				if(connection != null){
					connection.rollback();
				}
				tBitsResMgr.rollback();			
			} catch (SQLException e1) {
				e1.printStackTrace();
			}		
		} catch (TBitsException tbe) {
			tbe.printStackTrace();
			try {
				if(connection != null){
					connection.rollback();
				}
				tBitsResMgr.rollback();				
			} catch (SQLException e1) {
				e1.printStackTrace();
			}		
		}finally{
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}			
	}
	
	@SuppressWarnings("unused")
	private static void printMLName(){
		ArrayList<User> ml = getMailList("ritesh");		
		if (ml.listIterator().hasNext()){
			System.out.println("%%%%%%%%Mail list: " + ml.listIterator().next().getUserLogin());
		}
	}	
}

