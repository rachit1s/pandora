package mom.com.tbitsGlobal.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import mom.com.tbitsGlobal.client.DraftData;
import mom.com.tbitsGlobal.client.IFormConstants;
import mom.com.tbitsGlobal.client.MOMConstants;
import mom.com.tbitsGlobal.client.PrintData;
import mom.com.tbitsGlobal.client.service.MOMAdminService;
import mom.com.tbitsGlobal.client.service.MOMService;
import mom.com.tbitsGlobal.client.service.MOMServiceAsync;

import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.model.api.activity.SemanticException;

import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.IActivator;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;
import transbit.tbits.report.TBitsReportEngine;
import transbit.tbits.webapps.WebUtil;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.AttachmentInfoClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;
import commons.com.tbitsGlobal.utils.client.pojo.POJOBoolean;
import commons.com.tbitsGlobal.utils.client.pojo.POJODate;
import commons.com.tbitsGlobal.utils.client.pojo.POJOString;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;
import commons.com.tbitsGlobal.utils.server.plugins.GWTProxyServletManager;

public class Activator extends TbitsRemoteServiceServlet implements MOMService, IFormConstants,MOMConstants, IActivator, TBitsPropEnum {

	private static final long serialVersionUID = 1L;
	
	public static TBitsLogger  LOG	= TBitsLogger.getLogger("mom.com.tbitsGlobal.server");
	
	public void activate() {
		GWTProxyServletManager.getInstance().subscribe(MOMService.class.getName(), Activator.class);
		
		/*
		 * Register the service calling class
		 */
		GWTProxyServletManager.getInstance().subscribe(MOMAdminService.class.getName(), "MOM_Admin", MOMAdminServiceImpl.class);
	}
	
	public int addMeeting(String sysPrefix, PrintData printData) throws TbitsExceptionClient {
		try {
			User user = WebUtil.validateUser(this.getRequest());
			return addMeeting(sysPrefix, printData, user);
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		catch(Throwable t)
		{
			LOG.error(TBitsLogger.getStackTrace(t));
			throw new TbitsExceptionClient(t);
		}
	}

	public static int addMeeting(String sysPrefix, PrintData printData, User user) throws TbitsExceptionClient {
		Connection conn = null;
		TBitsResourceManager tbitsResMgr = new TBitsResourceManager();
		
		BusinessArea ba = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			
			TbitsTreeRequestData meetingReq = printData.getHeaderModel();
			List<TbitsTreeRequestData> models = printData.getActions();
			
			// Generate and set Meeting Number
			String meetingId = generateMeetingId(conn, ba, meetingReq);
			if(meetingId != null && !meetingId.equals(""))
				meetingReq.set("meeting_id", meetingId);
			
			List<AttachmentInfo> enclosures = new ArrayList<AttachmentInfo>();
			for (TbitsTreeRequestData model : models) {
				if(meetingId != null && !meetingId.equals(""))
					model.set("meeting_id", new POJOString(meetingId));
				
				if (model.getRequestId() == 0) {
					POJO dueDate =  model.getAsPOJO(Field.DUE_DATE);
					model.set("first_target_date", dueDate);
				}else{
					int rid = model.getRequestId();
					Request req = Request.lookupBySystemIdAndRequestId(conn, ba.getSystemId(), rid);
					String previousMeetingId = req.get("meeting_id");
					if(previousMeetingId != null)
						model.set("previous_meeting_id", new POJOString(previousMeetingId));
					
					try{
						Date firstTargetDate = (Date) req.getObject("first_target_date");
						if(firstTargetDate != null)
							model.set("first_target_date", new POJODate(firstTargetDate));
					}catch(Exception e){
						LOG.info(TBitsLogger.getStackTrace(e));
					}
				}
				if(model.getPropertyNames().contains(Field.ATTACHMENTS)){
					POJOAttachment pojo = (POJOAttachment)model.getAsPOJO(Field.ATTACHMENTS);
					List<FileClient> attachments = pojo.getValue();
					
					ArrayList<AttachmentInfo> atts = new ArrayList<AttachmentInfo>();
					for(AttachmentInfoClient attachment : attachments){
						AttachmentInfo att = new AttachmentInfo();
						att.name = attachment.getFileName();
						att.repoFileId = attachment.getRepoFileId();
						att.requestFileId = attachment.getRequestFileId();
						att.size = attachment.getSize();
						atts.add(att);
					}
					enclosures.addAll(atts);
				}
			}
			
			try {
				POJOAttachment pojo = (POJOAttachment) meetingReq.getAsPOJO(Field.ATTACHMENTS);
				
				if(pojo != null){
					List<FileClient> originalFiles = pojo.getValue();
					if(originalFiles != null){
						ArrayList<AttachmentInfo> atts = new ArrayList<AttachmentInfo>();
						for(AttachmentInfoClient attachment : originalFiles){
							AttachmentInfo att = new AttachmentInfo();
							att.name = attachment.getFileName();
							att.repoFileId = attachment.getRepoFileId();
							att.requestFileId = attachment.getRequestFileId();
							att.size = attachment.getSize();
							atts.add(att);
						}
						enclosures.addAll(atts);
					}
				}
				
				String attStr = AttachmentInfo.toJson(enclosures);
				meetingReq.set(Field.ATTACHMENTS, attStr);
			} catch (Exception e) {
				LOG.info(TBitsLogger.getStackTrace(e));
			}
			
			meetingReq.set(Field.IS_PRIVATE, new POJOBoolean(true));
			
			TbitsTreeRequestData meetingResult = null;
			if (meetingReq.getRequestId() == 0) {
				meetingResult = GWTServiceHelper.addRequest(conn, tbitsResMgr, meetingReq, user, ba);
			} else
				meetingResult = GWTServiceHelper.updateRequest(conn, tbitsResMgr, meetingReq, user, ba);

			if(meetingResult != null)
				meetingReq.setRequestId(meetingResult.getRequestId());
			
			if (meetingResult != null){
				int parentRequestId = 0;
				try {
					parentRequestId = meetingResult.getRequestId();
				} catch (Exception e) {
					LOG.info(TBitsLogger.getStackTrace(e));
				}
				TbitsTreeRequestData result = null;
				for (TbitsTreeRequestData model : models) {
					model.set(Field.PARENT_REQUEST_ID, parentRequestId);
					model.set(Field.IS_PRIVATE, new POJOBoolean(true));
					
					String relRequests = model.getAsString(Field.RELATED_REQUESTS);
					model.set(Field.RELATED_REQUESTS, new POJOString((relRequests.equals("") ? "" : relRequests + ",") + ba.getSystemPrefix() + "#" + meetingResult.getRequestId()));
					
					// Copying values from Meeting/Agenda to Action Items/Agenda Items
					model.set("Venue", meetingReq.getAsPOJO("Venue"));
					model.set("StartDate", meetingReq.getAsPOJO("StartDate"));
					model.set("StartTime", meetingReq.getAsPOJO("StartTime"));
					model.set("EndTime", meetingReq.getAsPOJO("EndTime"));
					model.set("EndDate", meetingReq.getAsPOJO("EndDate"));
					
					if (model.getRequestId() == 0) {
						result = GWTServiceHelper.addRequest(conn, tbitsResMgr, model, user, ba);
					} else
						result = GWTServiceHelper.updateRequest(conn, tbitsResMgr, model, user, ba);
					
					if(result != null)
						model.setRequestId(result.getRequestId());
				}
			}else throw new TBitsException("Unknown Exception");
			
			List<String> enclosureFileNames = new ArrayList<String>();
			for(AttachmentInfo att : enclosures){
				enclosureFileNames.add(att.getName());
			}
			File report = generateReport(user, ba, printData, enclosureFileNames);
			
			Request meetingCommittedRequest = Request.lookupBySystemIdAndRequestId(conn, ba.getSystemId(), meetingResult.getRequestId());
			
			Uploader uploader = new Uploader(meetingCommittedRequest.getRequestId(), meetingCommittedRequest.getMaxActionId(), sysPrefix);
			AttachmentInfo att = uploader.moveIntoRepository(report);
			
			Collection<AttachmentInfo> oldAtts = meetingCommittedRequest.getAttachments();
			oldAtts.add(att);
			String attStr = AttachmentInfo.toJson(oldAtts);
			
			TbitsTreeRequestData meetingUpdate = new TbitsTreeRequestData(meetingCommittedRequest.getSystemId(), meetingCommittedRequest.getRequestId());
			meetingUpdate.setMaxActionId(meetingCommittedRequest.getMaxActionId());
			meetingUpdate.set(Field.DESCRIPTION, new POJOString("Uploading PDF for the record added earlier..."));
			meetingUpdate.set(Field.ATTACHMENTS, attStr);
			
			GWTServiceHelper.updateRequest(conn, tbitsResMgr, meetingUpdate, user, ba);
			
			conn.commit();
			tbitsResMgr.commit();
			
			return meetingResult.getRequestId();
		} catch (DatabaseException dbe) {
			try {
				tbitsResMgr.rollback();
				conn.rollback();
			} catch (SQLException e) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			}
		} catch (TBitsException tbe) {
			try {
				tbitsResMgr.rollback();
				conn.rollback();
			} catch (SQLException e) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			}
		} catch (SQLException e) {
			try {
				tbitsResMgr.rollback();
				if (conn != null)
					conn.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e1));
				throw new TbitsExceptionClient(e1);
			}
			APIException apie = new APIException();
			apie.addException(new TBitsException("Unable to get connection to the database", e));
			return -1;
		} catch (Exception e) {
			try {
				tbitsResMgr.rollback();
				if (conn != null)
					conn.rollback();
			} catch (SQLException e1) {
				LOG.info(TBitsLogger.getStackTrace(e1));
				throw new TbitsExceptionClient(e1);
			}
			throw new TbitsExceptionClient(e);
		} finally {
			try {
				if ((conn != null) && !conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			}
		}
		
		return -1;
	}
	
	private static String generateMeetingId(Connection conn, BusinessArea ba, TbitsTreeRequestData meetingReq) throws DatabaseException, SQLException{
		String meetingId = "";
		String recordType = meetingReq.get(RECORDTYPE);
		if(recordType != null && !recordType.equals("") && !recordType.equals(AGENDA)){
			boolean generateMeetingId = false;
			if(meetingReq.getRequestId() != 0){
				Request req = Request.lookupBySystemIdAndRequestId(conn, ba.getSystemId(), meetingReq.getRequestId());
				
				meetingId = req.get("meeting_id");
				if(meetingId == null || meetingId.equals(""))
					generateMeetingId = true;
			}else
				generateMeetingId = true;
			
			if(generateMeetingId){
				String meetingType = meetingReq.getAsString(Field.REQUEST_TYPE);
				if(meetingType != null && !meetingType.trim().equals("")){
					Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), Field.REQUEST_TYPE, meetingType);
					meetingId = type.getDescription();
				}
				
				int maxId = APIUtil.getMaxId(meetingId);
				String runningId = maxId + "";
				if(runningId.length() < 4){
					int n = 4 - runningId.length();
					for(int i = 0; i < n; i++){
						runningId = "0" + runningId;
					}
				}
				meetingId = meetingId + "-" + runningId;
			}
		}
		return meetingId;
	}
	
	public String preview(String sysPrefix, PrintData printData) throws TbitsExceptionClient {	
		User user = null;
		BusinessArea ba = null;
		
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			
			File file = generateReport(user, ba, printData, new ArrayList<String>());
			if (file != null)
				return "/download-delete?saveAs=true&file=" + file.getName();
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (SemanticException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (EngineException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (IllegalArgumentException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (IOException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		return null;
	}
	
	private static File generateReport(User user, BusinessArea ba, PrintData printData, List<String> enclosures) 
		throws DatabaseException, SemanticException, EngineException, IllegalArgumentException, IOException, TBitsException, TbitsExceptionClient {
		
		if(ba == null || user == null)
			return null;
		
		MOMTemplateHandler templateHandler = new MOMTemplateHandler(ba, new Field(), new Type());
		templateHandler.setEnclosures(enclosures);
		
		Hashtable<String, String> configTable = new Hashtable<String, String>();
		configTable.put(PrintConstants.REP_PROJECT, ba.getDescription());
		for(String key : printData.getParams().keySet()){
			configTable.put(key, printData.getParams().get(key));
		}
		templateHandler.setConfigTable(configTable);
		
		Hashtable<String, String> headerTable = GWTServiceHelper.prepareParamTableforAddandUpdate(printData.getHeaderModel(), user, ba);
		headerTable.putAll(replaceNameWithDisplayName(ba, headerTable));

		templateHandler.setHeaderTable(headerTable);
		ArrayList<Hashtable<String, String>> actionTableList = new ArrayList<Hashtable<String,String>>();
		for(TbitsTreeRequestData model : printData.getActions()){
			Hashtable<String, String> table = GWTServiceHelper.prepareParamTableforAddandUpdate(model, user, ba);
			
			//Replace values with display Names
			table.putAll(replaceNameWithDisplayName(ba, table));
			actionTableList.add(table);
		}
		templateHandler.setActionTableList(actionTableList);
		
		String recordType = headerTable.get(MOMConstants.RECORDTYPE);
		String reportName = getTemplateName(ba, printData.getHeaderModel(), recordType);
		File outFile = generatePreview(reportName, templateHandler, "pdf", headerTable);
		
//		String format = "pdf";
//		HashMap<String, String> reportParamMap = new HashMap<String, String>();
//		String tbits_base_url = WebUtil.getNearestPath("");
//		reportParamMap.put(PrintConstants.REP_TBITS_BASE_URL_KEY,tbits_base_url);
//		File file = PrintHelper.generateReport(reportName, configTable, reportParamMap, format);
		return outFile;
	}

	private static Hashtable<String, String> replaceNameWithDisplayName(BusinessArea ba, Hashtable<String, String> table) throws TbitsExceptionClient {
		Hashtable<String, String> tobereplaced = new Hashtable<String, String>();
		for(String fieldName:table.keySet())
		{
			try
			{
				Field f = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), fieldName);
				String value = table.get(fieldName);
				if(f != null && f.getDataTypeId() == DataType.TYPE)
				{
					Type t = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), fieldName, value);
					value = t.getDisplayName();
					tobereplaced.put(fieldName, value);
				}
			}
			catch(DatabaseException dbe)
			{
				LOG.info(TBitsLogger.getStackTrace(dbe));
				throw new TbitsExceptionClient(dbe);
			}
		}
		return tobereplaced;
	}
	
	private static String getTemplateName(BusinessArea ba, TbitsTreeRequestData headerModel, String recordType) throws TbitsExceptionClient{
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			
			Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), Field.REQUEST_TYPE);
			
			String meetingType = headerModel.getAsString(Field.REQUEST_TYPE);
			Type type = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), Field.REQUEST_TYPE, meetingType);
			
			String sql = "SELECT template " +
						"FROM mom_templates " +
						"where sys_id = ? and field_id = ? and type_id = ? and is_meeting = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, ba.getSystemId());
			statement.setInt(2, field.getFieldId());
			statement.setInt(3, type.getTypeId());
			if (recordType.equals("Agenda"))
				statement.setInt(4, 0);
			else statement.setInt(4, 1);
			
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}else{
				sql = "SELECT template " +
				"FROM mom_templates " +
				"where sys_id = ? and field_id = 0 and type_id = 0 and is_meeting = ?";
				statement = conn.prepareStatement(sql);
				statement.setInt(1, ba.getSystemId());
				if (recordType.equals("Agenda"))
					statement.setInt(2, 0);
				else statement.setInt(2, 1);
				
				rs = statement.executeQuery();
				if(rs.next()) {
					String template = rs.getString(1);
					return template;
				}
			}
		} catch (SQLException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.info(TBitsLogger.getStackTrace(e));
					throw new TbitsExceptionClient(e);
				}
			}
		}
		
		return null;
	}
	
	private static File generatePreview(String rptDesignFileName,
			MOMTemplateHandler ith, String outputFormat, Hashtable<String, String> headerTable) throws TbitsExceptionClient, IOException {
		try {
			TBitsReportEngine tBitsEngine = TBitsReportEngine.getInstance();
	
			HashMap<Object, Object> reportVariables = new HashMap<Object, Object>();
			reportVariables.put("MOMTemplateHandler", ith);
			reportVariables.put("out", System.out);
	
			System.out.println("opening design.");
			
			String outputDir = Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_TMPDIR));
			
			String outputFileName = "";
			if (headerTable.containsKey(IFormConstants.RECORD_TYPE))
				outputFileName = headerTable.get(IFormConstants.RECORD_TYPE) + "-";
	
			if (headerTable.containsKey(Field.SUBJECT))
				outputFileName += headerTable.get(Field.SUBJECT) + "-";
	
			if (headerTable.containsKey(PrintConstants.REP_MEETING_DATE)) {
				String date = headerTable.get(PrintConstants.REP_MEETING_DATE);
				outputFileName += date + "-";
			}
			
			outputFileName = outputFileName.replaceAll("[^a-zA-Z0-9]", "_");
	
			File outputFile = findUniqueFile(outputFormat, outputDir, outputFileName);
		
			outputFile = tBitsEngine.generatePDFFile(rptDesignFileName, reportVariables, null, outputFile);
			return outputFile;
		} catch (TBitsException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	private static File findUniqueFile(String fileExt, String tmpOutputLoc, String prefix) throws IOException {
		File of;
		String outFileName = prefix;
		int i = 0;
		do {
			Random r = new Random();
			outFileName += r.nextInt(1000) + "-" + i++ + "." + fileExt;
			outFileName = outFileName.replaceAll(":", "_");
			of = new File(tmpOutputLoc + "/" + outFileName);
		} while (of.exists());
		of.createNewFile();
		return of;
	}

	public POJO getPrimitiveObject() {
		return null;
	}

	public List<String> getValidBAList() {
		Properties props = PropertiesHandler.getAppAndSysProperties();
		String prefixes = (String) props.get("MOM_PREFIXES");
		if(prefixes != null){
			String[] bas = prefixes.split(",");
			return Arrays.asList(bas);
		}
		return null;
	}
	
	public int saveDraft(int draftId, DraftData printData) throws TbitsExceptionClient{
		try {
			User user = WebUtil.validateUser(this.getRequest());
			draftId = Utils.savePrintData(draftId, user.getUserId(), printData);
		}catch (TBitsException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		return draftId;
	}
	
	public DraftData readDraft(int draftId) throws TbitsExceptionClient{
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
			return Utils.retrivePrintData(draftId, user.getUserId());
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
//		RPCRequest rpcRequest = RPC.decodeRequest("5|0|44|http://127.0.1.1:8888/jaguar/|683A04653A657C2720FF12B01D883363|mom.com.tbitsGlobal.client.service.MOMService|saveDraft|mom.com.tbitsGlobal.client.PrintData/768153542|java.util.ArrayList/3821976829|commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData/1323544019|com.extjs.gxt.ui.client.data.RpcMap/3441186752|summary|commons.com.tbitsGlobal.utils.client.pojo.POJOString/3727562088|java.lang.String/2004016611|fewfef|subject|fewfewf|sys_id|MoM_KDI|ExtAttendee||request_id|commons.com.tbitsGlobal.utils.client.pojo.POJOInt/2705882787|java.lang.Integer/3438268394|Venue|fewfewfewf|StartDate|commons.com.tbitsGlobal.utils.client.pojo.POJODate/3206868584|yyyy-MM-dd HH:mm:ss|java.util.Date/1659716317|description|fewfefewf|EndTime|10:00 PM|attachments|commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment/1976093176|StartTime|request_type_id|ProjectReview|recordtype|Action Item|kwfhewljfh|Meeting|java.util.HashMap/962170901|attendees|[]|organizations|1|2|3|4|1|5|5|6|1|7|0|0|0|6|0|0|1|8|13|9|10|11|12|13|10|11|14|15|10|11|16|17|10|11|18|19|20|21|0|22|10|11|23|24|25|26|27|2.68745792E8|1.279900254208E12|28|10|11|29|30|10|11|31|32|33|6|0|34|10|11|31|35|10|11|36|37|10|11|38|7|0|0|0|6|0|0|1|8|10|19|20|-15|24|25|26|27|2.68745792E8|1.279900254208E12|22|10|11|23|13|10|11|39|15|10|-11|30|10|11|31|34|10|11|31|17|10|11|18|37|10|11|40|35|10|11|36|41|2|11|42|11|43|11|44|11|43|", this.getClass(), this);
//		PrintData printData = (PrintData) rpcRequest.getParameters()[0];
	}
	
	public boolean deleteDraft(int draftId) throws TbitsExceptionClient{
		Connection con = null ;
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
			con = DataSourcePool.getConnection();
			
			String sql = "delete from mom_drafts where user_id = ? and meeting_id = ?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, user.getUserId());
			ps.setInt(2, draftId);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		finally
		{
			try {
				if( null != con && con.isClosed() == false)
				{
					con.close();
				}
			} catch (SQLException e) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			}
		}
		return true;
	}
	
	
	public HashMap<Integer, DraftData> getDrafts() throws TbitsExceptionClient {
		String sql = "select meeting_id,data_blob from mom_drafts where user_id=? ";
		HashMap<Integer, DraftData> drafts = new HashMap<Integer, DraftData>();
		Connection con = null;
		User user;
		try {
			con = DataSourcePool.getConnection();
			
			user = WebUtil.validateUser(this.getRequest());
			
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, user.getUserId());
			ResultSet rs = ps.executeQuery();
			if( rs != null)
			{
				while( rs.next() )
				{
					int draftId = rs.getInt("meeting_id");
					Blob pdBlob = rs.getBlob("data_blob");
					InputStream stream = pdBlob.getBinaryStream();
					
					try{
						ObjectInputStream ois = new ObjectInputStream(stream);
						DraftData pd = (DraftData) ois.readObject();
						
						drafts.put(draftId, pd);
					}catch(Exception e){
						LOG.info(TBitsLogger.getStackTrace(e));
					}
				}
			}
			
			rs.close();
			ps.close();
		} catch (SQLException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		finally
		{
			try {
				if( null != con && con.isClosed() == false)
				{
					con.close();
				}
			} catch (SQLException e) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			}
		}
		
		return drafts;
	}
	
	public static void main(String[] args) throws TbitsExceptionClient, DatabaseException {
		final BusinessArea ba = BusinessArea.lookupBySystemPrefix("MoM_KDI");
		final User user = User.lookupByUserLogin("root");
		for(int i = 0; i < 10; i++){
			Runnable runnable = new Runnable(){
				public void run() {
					PrintData printData = new PrintData();
					printData.setCaption("Meeting");
					printData.setHeaderModel(new TbitsTreeRequestData());
					printData.setActions(new ArrayList<TbitsTreeRequestData>());
					printData.setParams(new HashMap<String, String>());
					
					printData.getHeaderModel().set(Field.REQUEST_TYPE, "ProjectReview");
					printData.getHeaderModel().set(MOMConstants.RECORDTYPE, "Meeting");
					
					for(int i = 0 ; i < 2; i++){
						TbitsTreeRequestData action = new TbitsTreeRequestData();
						action.set(Field.SUBJECT, "test");
						action.set(Field.CATEGORY, "LTSL");
						action.set(Field.DUE_DATE, new Date());
						action.set("access_to", "root");
						printData.getActions().add(action);
					}
					
					try {
						int rid = addMeeting(ba.getSystemPrefix(), printData, user);
//						GWTServiceHelper.getDataByRequestId(user, ba, new ArrayList<BAField>(), rid, "");
						Request.lookupBySystemIdAndRequestId(ba.getSystemId(), rid);
					} catch (TbitsExceptionClient e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (DatabaseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}};
				
			Thread th = new Thread(runnable);
			th.run();
		}
	}

}
