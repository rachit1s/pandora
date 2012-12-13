package tpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
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
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.dms.AttachmentUtils;
import transbit.tbits.dms.TransmittalTemplate;
import transbit.tbits.domain.BAUser;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.TransmittalHandler;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.report.TBitsReportEngine;
import transbit.tbits.webapps.WebUtil;
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

public class TPLTransmittal implements TransmittalHandler {
	
	private static final String FIELD_REVISION_NUMBER = "Revision_Number";
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	static final String PREPARE_LETTERS_FILE = "templates/tpl-attachment-selection.htm";
	static final String TSCR_PREPARE_LETTERS_FILE = "templates/tscr-attachment-selection.htm";
	/*private static final String CLIENT_TRANSMITTAL_NOTE_FILE = "web/tpl-transmittal-note-client.htm";
	private static final String FIELD_TRANSMITTAL_NOTE_FILE = "web/tpl-transmittal-note-field.htm";
	private static final String TRANSMITTAL_NOTE_PRINT_FILE = "web/tpl-transmittal-note-print.htm";*/
	
	//Keywords
	private static final String KEYWORD_SUCCESS = "success";
	private static final String KEYWORD_VALUE = "value";
	private static final String KEYWORD_TRUE = "true";
	private static final String KEYWORD_FALSE = "false";	
	private static final String SUBMITTED_TO_CLIENT = "Submitted To Client";
	private static final String REQUEST_TYPE_REQUEST = "request";
	private static final String REQUEST_TYPE_QUESTION = "question";	
	private static final String STATUS_PENDING = "Pending";
	private static final String STATUS_CLOSED = "Closed";
	
	//Request parameters
	private static final String DOC_REGISTER_BA = "ba";
	private static final String LOG_NUMBER = "logNo";
	private static final String REQUEST_LIST = "requestList";
	private static final String ATTACHMENT_LIST = "attachmentList";
	private static final String DRAWINGS_LIST = "docList";
	private static final String ACTION_LIST = "actionList";
	private static final String REVISION_LIST = "revList";
	private static final String COPIES_LIST = "copiesList";
	private static final String EMAIL_TO = "emailTo";
	private static final String EMAIL_CC = "emailCc";
	private static final String DISPLAY_DATE = "dispDate";
	private static final String SEND_TO = "to";
	
	private static final String SEND_TO_CLIENT = "Client";
	private static final String SEND_TO_FIELD = "Field";
	
	//Extended field names
	private static final String FIELD_SI_1 = "SI_1";
	private static final String FIELD_SI_2 = "SI_2";
	private static final String FIELD_DRAWING_NO = "Drawing_Number";
	
	public static final String EMPTY_STRING = "";
	
	private static final String TPL_DOCUMENT_NO = "TPLDocumentNo";
	private static final String TPL_SUBMISSION_ATTACHMENT_FIELD= "tplsubmission";
	private static final Object DELIVERABLES = "deliverables";
	private static final String DELIVERABLE_LIST = "deliverableList";
	
	User user;
	private String tSysPrefix;
	private String copySysPrefix;
	JSONObject result = new JSONObject();

	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {
		TransmittalTemplate trnTemplate = null;
		PrintWriter out = aResponse.getWriter();
		aResponse.setContentType("text/html");
		
		String requestType = aRequest.getParameter("requestType");
		if ((requestType == null) || (requestType.trim().equals("") == true)){
			out.println ("invalid request type");
			return;
		}else{
			requestType = requestType.trim();			
		}		
		
		String sysPrefix = aRequest.getParameter("ba");
		if ((sysPrefix == null) || (sysPrefix.trim().equals("") == true)) {
			out.println("Invalid Business Area.");
			return;
		} else {
			sysPrefix = sysPrefix.trim();
		}
		
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);		

			if (ba == null) {
				out.println("Invalid Business Area.");
				return;
			}
			
			int systemId = ba.getSystemId();
			
			String requestList = aRequest.getParameter("requestList");

			if ((requestList == null) || requestList.trim().equals("")) {
				out.println("Empty Requests List.");
				return;
			}
			requestList = requestList.trim();
			
			if (requestType.equals("selection")){
				File selectionFile = null;
				if (systemId == 2)
					selectionFile = TPLUtils.getResourceFile(PREPARE_LETTERS_FILE);
				else
					selectionFile = TPLUtils.getResourceFile(TSCR_PREPARE_LETTERS_FILE);
				DTagReplacer prepareTransmittal = new DTagReplacer(selectionFile);
				prepareTransmittal.replace("sysPrefix", sysPrefix);
				prepareTransmittal.replace("requestList", requestList);
				prepareTransmittal.replace("tableData", getTableData(systemId, requestList));
				prepareTransmittal.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));			
				out.print(prepareTransmittal.parse(ba.getSystemId()));
			}
			else if (requestType.equals ("preview")){				
				String transmittalType = aRequest.getParameter("to");				
				if ((transmittalType == null) || (transmittalType.trim().equals("") == true)) {
					out.println("Please select client/field to whom the transmittal has to be sent");
					return;
				} else {
					transmittalType = transmittalType.trim();
					trnTemplate  = TransmittalTemplate.lookupBySystemIdAndTemplateName(ba.getSystemId(), transmittalType);
					if (trnTemplate == null){
						out.print(KEYWORD_FALSE + TPLUtils.DELIMETER_COMMA + "Could not find a transmittal template for " + transmittalType + " for DCR " + sysPrefix);
						return;
					}
				}
				
				String actionList = aRequest.getParameter ("actionList");
				if ((actionList == null) || actionList.trim().equals("")) {
					out.println("Actions to be taken for the selected documents not provided");
					return;
				}			
				actionList = actionList.trim();
				
				String copiesList = aRequest.getParameter ("copiesList");
				if ((copiesList == null) || copiesList.trim().equals("")) {
					out.println("Please enter the appropriate value for number of copies");
					return;
				}			
				copiesList = copiesList.trim();
				
				String deliverableList = "";
					if (systemId != 2){
						deliverableList = aRequest.getParameter("deliverableList");
					if ((deliverableList == null) || deliverableList.trim().equals("")) {
						deliverableList = "";
					}			
					deliverableList = deliverableList.trim();
				}
				
				String attachmentList = aRequest.getParameter("attachmentList");
				if ((attachmentList == null) || attachmentList.trim().equals("")) {
					attachmentList = "";
				}			
				attachmentList = attachmentList.trim();
				
				File previewFile = TPLUtils.getResourceFile("templates/" + trnTemplate.getTemplateFileName());
				DTagReplacer transmittalNote = new DTagReplacer(previewFile);				
											
				transmittalNote.replace("sysPrefix", sysPrefix);
				transmittalNote.replace("requestList", requestList);
				transmittalNote.replace("to", transmittalType);
				transmittalNote.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
				transmittalNote.replace("actionList", actionList);
				transmittalNote.replace("copiesList", copiesList);
				if (systemId != 2)
					transmittalNote.replace("deliverableList", deliverableList);
				transmittalNote.replace("attachmentList", attachmentList);
				transmittalNote.replace("tableData", getTableData(systemId, requestList));
				
				//TODO: Move this logic to db
				if (systemId == 2){
					transmittalNote.replace("toList", trnTemplate.getAssigneeList());
					transmittalNote.replace("ccList", trnTemplate.getSubscribersList());
				}
				else{
					String[] reqNo = requestList.split(TPLUtils.DELIMETER_COMMA);
					Request tempRequest = Request.lookupBySystemIdAndRequestId(systemId, Integer.parseInt(reqNo[0]));
					String transmittalProcessName = TPLUtils.getTransmittalProcessName(systemId, tempRequest);
					Hashtable<String, String> toCcListTable = TPLUtils.getToAndCcList(transmittalProcessName);
					transmittalNote.replace("toList", toCcListTable.get("toList"));
					transmittalNote.replace("ccList", toCcListTable.get("ccList"));
				}
				out.print(transmittalNote.parse(ba.getSystemId()));
			}			
		} catch (DatabaseException e1) {
			e1.printStackTrace();
		}			
	}
	
	private String getTableData(int aSystemId, String requestList) throws IllegalArgumentException, DatabaseException{
		JSONArray reqArray = new JSONArray();
		try {		
			for (String requestId : requestList.split(",")) {				
				Request request = Request.lookupBySystemIdAndRequestId(
						aSystemId, Integer.parseInt(requestId));
				JSONObject obj = new JSONObject();
				obj.put("request_id", request.getRequestId());
				if (aSystemId == 2){
					obj.put("doc_no",request.get("Paper_Size")+"-"+request.get("TPL_Code")+"-"
						+request.get("SI_1")+"-"+request.get("SI_2")+"-"+request.get("Drawing_Number"));
				}
				else if (aSystemId == 8){
					obj.put("doc_no", request.get(TPL_DOCUMENT_NO));
					TPLUtils.getProperty("");
					Field tplDeliverableField = Field.lookupBySystemIdAndFieldName(aSystemId, TPL_SUBMISSION_ATTACHMENT_FIELD);
					obj.put(DELIVERABLES, AttachmentUtils.getAttachmentList(aSystemId, request, tplDeliverableField.getFieldId(), FIELD_REVISION_NUMBER));
				}
				obj.put("rev_no", request.get(FIELD_REVISION_NUMBER));
				obj.put("desp",request.getSubject());	
				obj.put("attachments", AttachmentUtils.getAttachmentList(aSystemId, request,FIELD_REVISION_NUMBER));					
				reqArray.add(obj);
			}	
			return reqArray.toString();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return reqArray.toString();
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		handlePostRequest(req, res);
	}
	
	public void handlePostRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException 
	{
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
				result.put(KEYWORD_VALUE, "TBitsException: \n" + "Error occured while validating user");
				out.print(result.toString());
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
				 
			BusinessArea dcrBA = BusinessArea.lookupBySystemPrefix(sysPrefix);		

			if (dcrBA == null) {
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, sysPrefix + ": Invalid Business Area or business area does not exist");
				out.print(result.toString());
				return;
			}
			
			//System.out.println ("BA: "+ ba.getSystemPrefix());
			//String tSysPrefix = aRequest.getParameter(TRANSMITTAL_BA);
			
			getMappedBusinessAreas(sysPrefix);
						
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
			
			/*boolean isExistsLatestBA = false;
			BusinessArea latestDrawingsBA = null;
			//String copySysPrefix = aRequest.getParameter(LATEST_DRAWINGS_BA);			
			if ((copySysPrefix == null) || copySysPrefix.trim().equals(EMPTY_STRING)|| copySysPrefix.trim().equals("none")){
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, "Invalid system prefix of latest Business Area");
				System.out.println("Invalid system prefix of latest Business Area");			
				//return;
			}else{
				copySysPrefix = copySysPrefix.trim();
				latestDrawingsBA = BusinessArea.lookupBySystemPrefix(copySysPrefix);	
				if (latestDrawingsBA == null) {
					result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
					result.put(KEYWORD_VALUE, copySysPrefix + ": Invalid latest Business Area or business area does not exist");
					out.print(result.toString());				
					return;
				}
				else
					isExistsLatestBA = true;
			}*/
								
			String to = aRequest.getParameter(SEND_TO);
			if ((to == null) || (to.trim().equals(EMPTY_STRING))) {
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, dcrBA.getDisplayName() + "Please choose \"client/field\" to whom the transmittal letter has to be sent");
				out.print(result.toString());				
				return;
			} else {
				to = to.trim();
			}

			String requestList = aRequest.getParameter(REQUEST_LIST);

			if ((requestList == null) || requestList.trim().equals(EMPTY_STRING)) {
				out.print(KEYWORD_FALSE + TPLUtils.DELIMETER_COMMA +"No document selected to be sent to transmittal. Please select the documents to be sent");
				return;
			}
			requestList = requestList.trim();
			String[] reqNo = requestList.split(TPLUtils.DELIMETER_COMMA);
			
			String logNo = aRequest.getParameter(LOG_NUMBER);
			if ((logNo == null) || (logNo.trim().equals(EMPTY_STRING))) {
				out.print("Please enter proper reference number for the transmittal");
				return;
			} else {
				logNo = logNo.trim().replace("/", "-");
			}
						
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
			
			String deliverableList = "";
			String[] delReqFileNames = null;
			if (dcrBA.getSystemId() > 2){
				deliverableList = aRequest.getParameter(DELIVERABLE_LIST);
				if ((deliverableList == null) || deliverableList.trim().equals(EMPTY_STRING)) {
					deliverableList = "";
				}
				else{
					deliverableList = deliverableList.trim();
					delReqFileNames = deliverableList.split(TPLUtils.DELIMETER_COMMA);
				}
			}
			
			String attachmentList = aRequest.getParameter(ATTACHMENT_LIST);
			String[] reqFileNames = null;
			if ((attachmentList == null) || attachmentList.trim().equals(EMPTY_STRING)) {
				attachmentList = "";
			}
			else
			{
				attachmentList = attachmentList.trim();
				reqFileNames = attachmentList.split(TPLUtils.DELIMETER_COMMA);
			}
			
			String toAddress = aRequest.getParameter(EMAIL_TO);
			if ((toAddress == null)||(toAddress.trim().equals(EMPTY_STRING))){
				System.out.println ("No mailing address provided");
				//return;
			}
			toAddress = toAddress.trim();
			
			String ccAddress = aRequest.getParameter(EMAIL_CC);
			if ((ccAddress ==null) || (ccAddress.trim().equals(EMPTY_STRING))){
				ccAddress = EMPTY_STRING;
			}
			else{
				ccAddress = ccAddress.trim();
			}
			
			String displayDate = aRequest.getParameter(DISPLAY_DATE);
			if ((displayDate ==null) || (displayDate.trim().equals(EMPTY_STRING))){
				displayDate = EMPTY_STRING;
			}
			else{
				displayDate = displayDate.trim();
			}
			
			Request tempRequest = Request.lookupBySystemIdAndRequestId(dcrBA.getSystemId(), Integer.parseInt(reqNo[0]));
			
			boolean isBAUser = BAUser.isBAUser(dcrBA.getSystemId(), user.getUserId());
			
			//Currently only the BA users can create a transmittal
			if(isBAUser){		
				
				int transReqId = 0;
				String formattedTransReqId = "";
				String attachments = "";
				String transmittalProcessName = TPLUtils.getTransmittalProcessName(dcrBA.getSystemId(), tempRequest);
				
				//TODO: Move this logic to db.
				if (dcrBA.getSystemId() > 2){
					logNo = TPLUtils.getTransmittalNumberPrefix(transmittalProcessName);
				}
				
				try {			
					maxIdConn = DataSourcePool.getConnection();
					maxIdConn.setAutoCommit(false);
					
					transReqId = TPLUtils.getMaxTransmittalNumber(maxIdConn, transBA.getSystemId(), transmittalProcessName);
					formattedTransReqId = TPLUtils.getFormattedStringFromNumber(transReqId);
					connection = DataSourcePool.getConnection();
					connection.setAutoCommit(false);
					
					ArrayList<AttachmentInfo> transmittalAttachments = getTransmittalAttachments(reqFileNames);
					
					if ((dcrBA.getSystemId() != 2) && (delReqFileNames != null)){						
						transmittalAttachments.addAll(getTransmittalAttachments(delReqFileNames));
					}
					
					ArrayList<AttachmentInfo> transmittalNotes = getTransmittalAttachments(connection, dcrBA, reqNo, docList, revList, copiesList, actionList, 
							attachmentList, reqFileNames, logNo + formattedTransReqId, displayDate);		
					transmittalAttachments.addAll(transmittalNotes);
					attachments = AttachmentInfo.toJson(transmittalAttachments).toString();
					
				} catch (SQLException e1) {
					e1.printStackTrace();
					throw new DatabaseException("Error occurred while fetching database connection.", e1);
				} /*catch (EngineException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	*/	
												
				AddRequest addReq = new AddRequest();
				addReq.setContext(aRequest.getContextPath());
				Hashtable <String,String> aParamTable = new Hashtable<String, String>();
				aParamTable.put(Field.BUSINESS_AREA, transBA.getSystemPrefix());
				aParamTable.put(Field.SUBJECT, "Transmittal Note to - " 
						+ to + ", Ref- " + logNo + formattedTransReqId);
				aParamTable.put(Field.USER, user.getUserLogin());
				
				if (to.equals(SEND_TO_CLIENT))
					aParamTable.put(Field.REQUEST_TYPE, REQUEST_TYPE_REQUEST);
				else 
					aParamTable.put(Field.REQUEST_TYPE, REQUEST_TYPE_QUESTION);	
				
				aParamTable.put(Field.STATUS, STATUS_CLOSED);
				
				aParamTable.put(Field.ATTACHMENTS, attachments);
				
				String linkedRequests=EMPTY_STRING;
				for (int index=0; index<reqNo.length; index++){
					if (index == 0)
						linkedRequests = dcrBA.getSystemPrefix() + TPLUtils.DELIMETER_HASH + reqNo[index];
					else
						linkedRequests=linkedRequests + TPLUtils.DELIMETER_COMMA + dcrBA.getSystemPrefix() + TPLUtils.DELIMETER_HASH + reqNo[index];
				}			
				//aParamTable.put(Field.RELATED_REQUESTS, linkedRequests);

				aParamTable.put(Field.DESCRIPTION,getTextTable(connection, dcrBA.getSystemId(), reqNo, docList, revList, copiesList, actionList) 
						+ "\n\n The following document control register entries were transmitted: " + linkedRequests);
				aParamTable.put(Field.ASSIGNEE, toAddress);
				aParamTable.put(Field.SUBSCRIBER, ccAddress);	

				Request newReq = null;
				try {
					newReq = addReq.addRequest(connection, tBitsResMgr, aParamTable);
				} catch (APIException e1) {
					result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
					result.put(KEYWORD_VALUE, "Error occurred while adding new transmittal.");
					out.print(result.toString());
					e1.printStackTrace();
				}

				/*aParamTable.clear();

				//int transReqId = newReq.getRequestId();
				formattedTransReqId = logNo + transReqId;
				//String attachments = getTransmittalAttachments(ba, reqNo, docList, revList, copiesList, actionList, attachmentList, reqFileNames, ref, displayDate);			

				// Update the request(created above) in transmittal to have the ref. no. by 
				// appending the request id. 
				//String [] requestId = requestList.split(DELIMETER_COMMA); //request ids being logged			

				UpdateRequest updateRequest= new UpdateRequest();
				updateRequest.setContext(aRequest.getContextPath());
				updateRequest.setSource(TBitsConstants.SOURCE_WEB);
				
				aParamTable.put(Field.BUSINESS_AREA, transBA.getSystemPrefix());
				aParamTable.put(Field.USER, user.getUserLogin());
				aParamTable.put(Field.REQUEST, transReqId + EMPTY_STRING);
				//aParamTable.put(Field.SUBJECT, newReq.getSubject() + transReqId);			
				aParamTable.put(Field.STATUS, STATUS_CLOSED);			
				aParamTable.put(Field.ATTACHMENTS, attachments);
				String linkedRequests=EMPTY_STRING;
				for (int index=0; index<reqNo.length; index++){
					if (index == 0)
						linkedRequests = dcrBA.getSystemPrefix() + TPLUtils.DELIMETER_HASH + reqNo[index];
					else
						linkedRequests=linkedRequests + TPLUtils.DELIMETER_COMMA + dcrBA.getSystemPrefix() + TPLUtils.DELIMETER_HASH + reqNo[index];
				}			
				//aParamTable.put(Field.RELATED_REQUESTS, linkedRequests);

				aParamTable.put(Field.DESCRIPTION,getTextTable(connection, dcrBA.getSystemId(), reqNo, docList, revList, copiesList, actionList) 
						+ "\n\n The following document control register entries were transmitted: " + linkedRequests);
				try {
					updateRequest.updateRequest(connection, tBitsResMgr, aParamTable);
				}catch (APIException e) {
					result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
					result.put(KEYWORD_VALUE, "APIException: \n" + "Error occurred while updating transmittal information.");
					out.print(result.toString());
				} catch (TBitsException e) {
					result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
					result.put(KEYWORD_VALUE, "TBitsException: \n" + "Error occured while updating transmittal information.");
					out.print(result.toString());
				}*/
				
				UpdateRequest updateRequest= new UpdateRequest();
				updateRequest.setContext(aRequest.getContextPath());
				updateRequest.setSource(TBitsConstants.SOURCE_WEB);
				
				for (int index = 0;index < reqNo.length; index++){
					
					// Update the drawings sent to transmittal by changing the status and description. 
					aParamTable.clear();
					aParamTable.put (Field.BUSINESS_AREA, dcrBA.getSystemPrefix());
					aParamTable.put (Field.USER, user.getUserLogin());
					aParamTable.put (Field.REQUEST, reqNo[index]);				

					Timestamp ts = newReq.getLastUpdatedDate();				
					Date date = new Date(ts.getTime()); 
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date);
					calendar.add(Calendar.DAY_OF_MONTH, 10);
					date = calendar.getTime();								
					String tempDate = Timestamp.toCustomFormat(date, transbit.tbits.Helper.TBitsConstants.API_DATE_FORMAT);
					aParamTable.put (Field.DUE_DATE, tempDate);		
					
					aParamTable.put (Field.STATUS, SUBMITTED_TO_CLIENT);
					
					if (to.equalsIgnoreCase(SEND_TO_FIELD))
						aParamTable.put ("Transmittal_Field", KEYWORD_TRUE);
					aParamTable.put (Field.DESCRIPTION,"Transmittal letter ref: " + logNo + formattedTransReqId 
							+ "(" + transBA.getSystemPrefix() + TPLUtils.DELIMETER_HASH + transBA.getMaxRequestId() + ")"
							+ "\n\nAssignee: "+ toAddress + "\n" + "Subscriber: "+ ccAddress + "\n");	
					try {
						updateRequest.updateRequest(connection, tBitsResMgr, aParamTable);
					}catch (APIException e) {
						result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
						result.put(KEYWORD_VALUE, "APIException: \n" + "Error occured while updating the requests involving the transmittal");
						out.print(result.toString());
					}catch (TBitsException e) {
						result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
						result.put(KEYWORD_VALUE, "TBitsException: \n" + "Error occured while updating the requests involving the transmittal");
						out.print(result.toString());
					} 

				}	
				
				/*if (isExistsLatestBA)
					updateLatestBA(aRequest, ba, reqNo, latestDrawingsBA);	
				*/
				
				result.put(KEYWORD_SUCCESS, KEYWORD_TRUE);
				result.put(KEYWORD_VALUE, formattedTransReqId);
				out.print(result.toString());	
			}
			else
			{
				result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
				result.put(KEYWORD_VALUE, "You are not authorized to create transmittal. Please contact your system administrator.");
				out.print(result.toString());
			}
			
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
		}/*catch (FileNotFoundException fnfe) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "FileNotFoundException: \n" + fnfe.getMessage());
			out.print(result.toString());
			return;
		}*/catch (DatabaseException dbe) {
			rollbackAllOperations(connection, maxIdConn, tBitsResMgr);			
			result.put(KEYWORD_SUCCESS, KEYWORD_FALSE);
			result.put(KEYWORD_VALUE, "Database Exception: \n" + dbe.getMessage());
			out.print(result.toString());		
			return;
		}/*catch (IOException ioe) {
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
		}*/catch (APIException apie) {
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
	
	
	//TODO: this code assumes that the file should not have semicolon and colon in the file name.
	//On windows, one can not have colon in file name but they can have the semicolon in file name.
	//please rectify this.
	public static ArrayList<AttachmentInfo> getTransmittalAttachments(String[] reqFileNames) 
											throws NumberFormatException, DatabaseException {
		
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>();
		 
		if (reqFileNames != null)
			for (int i=0; i<reqFileNames.length; i++){
			String reqFileList = reqFileNames[i];
			if (reqFileList.trim().equals(""))
				continue;
			else{					
				for (String reqFileInfo : reqFileList.split(TPLUtils.DELIMETER_SEMICOLON)){
					String[] reqAttInfo = reqFileInfo.split(TPLUtils.DELIMETER_COLON);
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
		
		return trnAttCollection;
	}
	
	
	
	/** 
	 * @param kth
	 * @throws DatabaseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws EngineException 
	 */
	private String getDTNAttachment () throws DatabaseException, 
			FileNotFoundException,IOException, EngineException{
		
		StringBuilder tempAttachments = new StringBuilder();
		ArrayList<AttachmentInfo> trnAttCollection = new ArrayList<AttachmentInfo>();		
		String pdfFilePath = "";
		//String pdfFilePath = KSKUtils.generateTransmittalNoteUsingBirt(templateName, kth, "Transmittal-Note");	
		File pdfFile = new File(pdfFilePath );
		Uploader uploader = new Uploader();
		AttachmentInfo trnNoteInfo = uploader.moveIntoRepository(pdfFile);
		trnAttCollection.add(trnNoteInfo);		
		tempAttachments.append(AttachmentInfo.toJson(trnAttCollection));
		return tempAttachments.toString();		
	}
	
		
	//Creates a text table to be added into the description in the transmittal BA during creation of a transmittal
	private String getTextTable(Connection connection, int aSystemId, String[] reqNo,String docList, String revList, String copiesList, String actionList){
		int serialNo= 0;
		Request req = null;
		StringBuffer tableRows= new StringBuffer();		
		String [] docName = docList.split(TPLUtils.DELIMETER_COMMA);
		String [] revNo = revList.split(TPLUtils.DELIMETER_COMMA);
		//String [] desp = despList.split(TPLUtils.DELIMETER_COMMA);
		String [] copies= copiesList.split(TPLUtils.DELIMETER_COMMA);		
		String [] actionFor = actionList.split(TPLUtils.DELIMETER_COMMA);
		tableRows.append("Sl. No.  Document Name \t\t\t Rev. No.  Description \t\t Action For  Copies \n");
		
		for (int i=0; i <docName.length; i++)
		{	
			serialNo = serialNo + 1;
			try {
				req = Request.lookupBySystemIdAndRequestId(connection, aSystemId, Integer.parseInt(reqNo[i]));
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			tableRows.append(serialNo + "\t" + docName[i] + "\t\t" + revNo[i] + "\t" + req.getSubject() +"\t\t" + actionFor[i] + "\t\t" + copies[i] + "\n");
		}
		
		return (tableRows.toString());
	}
	
	//Create HTML table rows to be added into transmittal note HTML attachment
	private String getHtmlTable(int aSystemId, String[] reqNo, String docList, String revList, String copiesList, String actionList){
		int serialNo= 0;	
		Request req = null;
		StringBuffer tableRows= new StringBuffer();
		String [] docName = docList.split(TPLUtils.DELIMETER_COMMA);
		String [] revNo = revList.split(TPLUtils.DELIMETER_COMMA);
		String [] copies= copiesList.split(TPLUtils.DELIMETER_COMMA);		
		String [] actionFor = actionList.split(TPLUtils.DELIMETER_COMMA);	
		
			for (int i=0; i <docName.length;i++)
			{	
				serialNo = serialNo + 1;
				try {
					req = Request.lookupBySystemIdAndRequestId(aSystemId, Integer.parseInt(reqNo[i]));
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tableRows.append("<tr align=\"center\"><td style=\"width:5%;text-align:center; border: solid 1px #000000\">" + serialNo + "</td>" +
						"<td style=\"width:30%;text-align:center; border: solid 1px #000000\">" + docName[i] + "</td>" +
						"<td style=\"width:10%;text-align:center; border: solid 1px #000000\">" + revNo[i]+ "</td>" + 
						"<td style=\"width:50%;text-align:center; border: solid 1px #000000\">" + req.getSubject()+ "</td>" +
						"<td style=\"width:1%;text-align:center; border: solid 1px #000000\">" + actionFor[i] + "</td>" +
						"<td style=\"width:1%;text-align:center; border: solid 1px #000000\">" + copies[i] + "</td></tr>");
			}
			return (tableRows.toString());			
	}
	
	private String getHtmlContent (Connection connection, int aSystemId, String[]reqNo, String docList, String revList, String copiesList, String actionList, String ref, String displayDate) throws DatabaseException{
				
		DTagReplacer transmittalNotePrint = null;		
		try {
			TPLTransmittalTemplate printTT = TPLTransmittalTemplate.lookupBySystemIdAndTemplateName(connection, aSystemId, "Print");
			File printFile = TPLUtils.getResourceFile("templates/" + printTT.getTemplateFileName());
			transmittalNotePrint = new DTagReplacer(printFile);			
			transmittalNotePrint.replace("ref", ref);
			transmittalNotePrint.replace("displayDate", displayDate);
			transmittalNotePrint.replace("requestTable", getHtmlTable(aSystemId, reqNo, docList, revList, copiesList, actionList));
						
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return(transmittalNotePrint.parse(aSystemId));
	}	

	//Returns all the attachments to be included in a transmittal
	private ArrayList<AttachmentInfo> getTransmittalAttachments (Connection connection, BusinessArea ba, String[] reqNo, String docList, 
			String revList, String copiesList, String actionList, String attachmentList, String[] reqFileNames, String refNo,
			String displayDate){
		
		ArrayList<AttachmentInfo> transmittalAttachments = new ArrayList<AttachmentInfo>();
		String[] numOfCopies = copiesList.split(TPLUtils.DELIMETER_COMMA);
		//StringBuilder tempAttachments = new StringBuilder();		
		try {		
			
			String pdfFilePath = "";
			Uploader uploader = new Uploader();
			StringBuffer buf = new StringBuffer();
			
			//Finally attaches the transmittal note		
			String tempDir = Configuration.findAbsolutePath (PropertiesHandler.getProperty(transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR));
			File htmlFile = null;
				
			ArrayList<String> approvalCategory = new ArrayList<String>();				
			approvalCategory.add("A: Submitted for Approval");
			approvalCategory.add("B: Submitted for Final Approval");
			approvalCategory.add("C: For information only");		
			
			if (ba.getSystemId() == 2){
				htmlFile = new File (tempDir + "/" + refNo.replace("/", "-") + ".htm");
				buf.append(getHtmlContent (connection, ba.getSystemId(), reqNo, docList, revList, copiesList, actionList, refNo, displayDate));
				FileWriter outHtml = new FileWriter(htmlFile);
				outHtml.write(buf.toString());
				outHtml.flush();
				outHtml.close();
			}
			else{
				approvalCategory.add("D: For your concurrence");
				
				ArrayList<String[]> drawingsList = new ArrayList<String[]>();
				String[] action = actionList.split(TPLUtils.DELIMETER_COMMA);
				for (int i= 0 ; i < reqNo.length; i++){
					Request tRequest = Request.lookupBySystemIdAndRequestId(connection, ba.getSystemId(), Integer.parseInt(reqNo[i]));
									
					//Create the drawing info.
					String[] drawing = new String[]{tRequest.getSubject(), tRequest.get(TPL_DOCUMENT_NO), 
							tRequest.get(FIELD_REVISION_NUMBER), action[i], numOfCopies[i]};
					drawingsList.add(drawing);
				}
				
				
				
				TPLTemplateHelper tth = new TPLTemplateHelper();
				tth.setDrawingsList(drawingsList);
				tth.setApprovalCategory(approvalCategory);
				tth.setTransmittalRefNumber(refNo);
				tth.setToAddress("");
				tth.setSubject("Transmittal Note - " + refNo);
				tth.setKindAttentionString("");
				tth.setCopyForward("");
				
				//String[] loggerInfo = {"loggerInfo1", "loggerInfo2"};
				//tth.setLoggerInfo(loggerInfo);
				
				String paths = TPLUtils.generateTransmittalNoteUsingBirt("TSR_transmittalTemplate.rptdesign", tth, "Transmittal-Note");
				String[] trnPaths = paths.split(",");
				pdfFilePath = trnPaths[0];
				
				htmlFile = new File (trnPaths[1]);				
			}
			
			if (htmlFile != null){
				AttachmentInfo trnNoteInHtml = uploader.moveIntoRepository(htmlFile);
				transmittalAttachments.add(trnNoteInHtml);
			}
			
			if (ba.getSystemId() == 2){
				pdfFilePath = tempDir + "/" + refNo.replace("/", "-") + ".pdf";
				TPLUtils.convertHtmlToPdf(buf.toString(), pdfFilePath);
			}
			
			File pdfFile = new File(pdfFilePath);			
			AttachmentInfo trnNoteInPdf = uploader.moveIntoRepository(pdfFile);
			transmittalAttachments.add(trnNoteInPdf);
			//tempAttachments.append(AttachmentInfo.toJson(transmittalAttachments));
		} catch (Exception e) {
			e.printStackTrace();
			LOG.severe("Exception occurred while retrieving attachments for transmittal:\n" + e.getMessage());
		}
		return transmittalAttachments;		
	}
	
	
	//This method was used to add attachments into a mail, while mail was not being sent through tBits mail
	//mechanism. Not in use any more. Only for historical purpose
	/*private void addMailAttachmentPart(String aSysPrefix, Request request,MimeMultipart mp, String attachmentList, int reqIndex){
		String [] namesList = attachmentList.split(TPLUtils.DELIMETER_COMMA);
		String reqAttachment = request.getAttachments();     
		String[] tempList = namesList[reqIndex].split(TPLUtils.DELIMETER_SEMICOLON);
		ArrayList<Attachment> attachments;
		try {
			attachments = Attachment.getAttachments(reqAttachment);
		
			int a = 0;								
			for (Attachment attachment : attachments) {	
				for (String attName : tempList){
					if (attName.equals(attachment.getDisplayName())){					
						a++;
						File file = new File (PropertiesHandler.getProperty(transbit.tbits.Helper.TBitsPropEnum.KEY_ATTACHMENTDIR) + "/" + aSysPrefix.toLowerCase() + "/" + attachment.getName());
						FileDataSource fds  = new FileDataSource(file);
						MimeBodyPart mbp2 = new MimeBodyPart();	
						mbp2.setDataHandler (new DataHandler(fds));
						mbp2.setDisposition (Part.ATTACHMENT);
						mbp2.setFileName (attachment.getDisplayName());
						mp.addBodyPart (mbp2, a);
						break;
					}
				}
			}
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
}
