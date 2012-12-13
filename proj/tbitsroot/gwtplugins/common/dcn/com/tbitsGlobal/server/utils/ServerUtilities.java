/**
 * 
 */
package dcn.com.tbitsGlobal.server.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.birt.core.exception.BirtException;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.report.TBitsReportEngine;
import transbit.tbits.webapps.WebUtil;

import com.tbitsGlobal.jaguar.client.serializables.RequestData;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.AttachmentInfoClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

import dcn.com.tbitsGlobal.shared.ChangeNoteConfig;

/**
 * @author Lokesh
 *
 */
public class ServerUtilities {
	
	private static final String ENG_MANAGER_FIELD = "EngManager";
	private static final String PROJECT_DIRECTOR_FIELD = "ProjectDirector";
	private static final String PROJECT_DIRECTOR_ROLE = "ProjectDirector";
	private static final String ENGINEERING_MANAGER_ROLE = "EngineeringManager";
	private static final String FIELD_MAP = "FieldMap";
	private static final String TRN_CHANGE_NOTE_FIELD_MAP = "trn_change_note_field_map";
	private static final String FIELD_NAME = "field_name";
	private static final String TEMPLATE_FIELD_NAME = "template_field_name";
	private static final String ASSIGNEE = "Assignee";
	private static final String PROJECT = "Project";
	private static final int FIRST_INDEX = 0;
	public static String generatePdf(HttpServletRequest httpRequest, int aSystemId, int aRequestId, 
			ChangeNoteConfig cnc) throws TbitsExceptionClient{
			
		try{
			Request request = Request.lookupBySystemIdAndRequestId(aSystemId, aRequestId);
			if ((request != null) && (cnc != null)){				
				ArrayList<Request> srcReqList = getSourceRequestsListFromRelatedRequests(request);				
				return ServerUtilities.generatePDFUsingBirt(httpRequest, request, 
						srcReqList, cnc);
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient("Database error occurred while generating pdf.", e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient("Error occurred while generating pdf. " + e.getMessage(), e);
		} catch (BirtException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient("Birt exception occurred while generating pdf.", e);
		}
		return "";
	}

	public static ArrayList<Request> getSourceRequestsListFromRelatedRequests(Request request)
		throws DatabaseException, TBitsException {
		
		String srcSysPrefix = null;
		BusinessArea srcBA = null;
		ArrayList<Request> srcReqList = new ArrayList<Request>();
		String relatedRequests = request.getRelatedRequests();
		
		if ((relatedRequests != null) && (relatedRequests.trim().length() != 0)){
			String[] srcRequestsSmartTags = relatedRequests.split(",");
			if (srcRequestsSmartTags != null){						
				for (String srcReqSmartTag : srcRequestsSmartTags){
					
					if(srcReqSmartTag != null){
						int requestId = 0;
						String[] part = srcReqSmartTag.split("#");
						if (part != null){
							if (srcSysPrefix == null){
								srcSysPrefix = part[0];
								srcBA = BusinessArea.lookupBySystemPrefix(srcSysPrefix);
								if (srcBA == null)
									throw new TBitsException("Invalid business area: " + srcSysPrefix);
							}
							
							requestId = Integer.parseInt(part[1]);
							
							if (requestId > 0){
								Request tmpRequest = Request.lookupBySystemIdAndRequestId(srcBA.getSystemId(), requestId);
								srcReqList.add(tmpRequest);
							}
						}
					}
				}
			}
		}
		return srcReqList;
	}

	public static String generatePDFUsingBirt(HttpServletRequest httpRequest, 
			Request request, ArrayList<Request> srcRequestsList, ChangeNoteConfig cnc)
	throws BirtException, TBitsException {
		
		String pdfUrl = "";
		TBitsReportEngine tBitsEngine = TBitsReportEngine.getInstance();
		Map<Object, Object> reportVariables = new HashMap<Object, Object>();
		Map<String, Object> reportParams = new HashMap<String, Object>();
		
		reportParams.put(REQUEST_ID, request.getRequestId());
		reportParams.put(SYS_ID, request.getSystemId());
		
		File tempDir = Configuration.findPath("webapps/tmp");
		String outputFileName = cnc.getBaType().replaceAll("[^A-Za-z0-9]+", "_");		
		String pdfFilePath = tempDir + File.separator + outputFileName + "_" + request.getRequestId() + PDF;
		File outFile = new File(pdfFilePath);
		
		reportVariables.put(REQUEST_HANDLER, request);
		reportVariables.put(SOURCE_REQUESTS, srcRequestsList);
		
		if ((srcRequestsList != null) && (srcRequestsList.size() != 0)) {
			int srcSystemId = srcRequestsList.get(FIRST_INDEX).getSystemId();
			BusinessArea srcBA = null;
			try {
				srcBA = BusinessArea.lookupBySystemId(srcSystemId);				
			} catch (DatabaseException e) {
				e.printStackTrace();				
			}
			if (srcBA != null){
				reportVariables.put(PROJECT, srcBA.getDescription());
				
				Hashtable<String, String> changeNoteFieldMap = getChangeNoteFieldMap(srcBA.getSystemId());
				if (changeNoteFieldMap != null)
					reportVariables.put(FIELD_MAP, changeNoteFieldMap);
			}
			
		}
				
		ArrayList<RequestUser> assignees = (ArrayList<RequestUser>)request.getAssignees();
		if ((assignees != null) && assignees.size() != 0){
			User assignee = null;
			try {
				assignee = assignees.get(0).getUser();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			reportVariables.put(ASSIGNEE, assignee);
		}
		
		File generatedPDFFile = tBitsEngine.generatePDFFile(cnc.getTemplateName(), reportVariables,
				reportParams, outFile);
		pdfUrl = getPdfUrl(httpRequest, generatedPDFFile);
		return pdfUrl;
	}

	private static String getPdfUrl(HttpServletRequest httpRequest,
			File generatedPDFFile) {
		String prot = (httpRequest.getProtocol().toLowerCase().contains("https") ? "https" : "http" );
		String toreturn = prot +"://" + httpRequest.getServerName() + ":"
								+ httpRequest.getServerPort()
								+ httpRequest.getContextPath() + "/tmp/" + generatedPDFFile.getName();
		return toreturn;
	}
	
	private static ArrayList<AttachmentInfo> uploadFileToTheSystem(ChangeNoteConfig cnc,
			File generatedPDFFile) {
		ArrayList<AttachmentInfo> changeNoteAttCollection = new ArrayList<AttachmentInfo>();	
		if (generatedPDFFile != null){
			Uploader uploader = new Uploader();
			uploader.setFolderHint(cnc.getTargetSysPrefix());
			AttachmentInfo trnNoteInfo = uploader.moveIntoRepository(generatedPDFFile);				
			changeNoteAttCollection.add(trnNoteInfo);
		}
		return changeNoteAttCollection;
	}
	
	public static RequestData getRequestData(HttpServletRequest request, ArrayList<Integer> requestIdList,
			ChangeNoteConfig changeNoteConfig) {
		try{
			if (changeNoteConfig == null)
				throw new TBitsException("Invalid configuration object of: " + ChangeNoteConfig.class.getSimpleName());
	
			String srcSysPrefix = changeNoteConfig.getSrcSysPrefix();
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(srcSysPrefix);
	
			if(ba==null)
				throw new TBitsException("Invalid source BA.");

			User user  = WebUtil.validateUser(request);
	
			BusinessArea targetBA = BusinessArea.lookupBySystemPrefix(changeNoteConfig.getTargetSysPrefix());
			if (targetBA == null)
				throw new TBitsException("Invalid target BA system prefix: " + changeNoteConfig.getTargetSysPrefix());
			
			String targetSysPrefix = targetBA.getSystemPrefix();
	
			Field srcAttachmentField = Field.lookupBySystemIdAndFieldId(ba.getSystemId(), changeNoteConfig.getSrcAttachmentFieldId());
			
			String srcRequestTags = "";
			ArrayList<AttachmentInfo> allFilesInfo = new ArrayList<AttachmentInfo>() ;
			
			TbitsTreeRequestData ttrd = new TbitsTreeRequestData();
			ttrd.set(Field.LOGGER, user.getUserLogin());
			
						
			boolean isFirstRequest = true;
			for (Integer requestId : requestIdList)
			{
				Request req = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), requestId);
				srcRequestTags = (srcRequestTags.equals("")) ?  changeNoteConfig.getSrcSysPrefix() + "#" + requestId 
						: srcRequestTags + "," + srcSysPrefix + "#" + requestId;
				ttrd.set(Field.RELATED_REQUESTS, srcRequestTags);
				
				fillAttachments(srcAttachmentField, allFilesInfo, req);
				
				Hashtable<String, String> fieldMapping = getSourceTargetFieldMapping(srcSysPrefix, targetSysPrefix);
				
				insertMappedFieldValuesIntoTTRD(srcSysPrefix, ba,
						targetSysPrefix, ttrd, isFirstRequest, req, fieldMapping);				
			}
			
			
			ArrayList<User> engManagers = getRoleByRoleName(targetBA.getSystemId(), ENGINEERING_MANAGER_ROLE);
			if (engManagers != null){
				String assigneeList = "";
				for (User engManager : engManagers){
					assigneeList = (assigneeList.equals("")) 
										? engManager.getUserLogin() 
												: assigneeList + "," + engManager.getUserLogin(); 
					
				}
				ttrd.set(Field.ASSIGNEE, assigneeList);
				Field engManagerField = Field.lookupBySystemIdAndFieldName(targetBA.getSystemId(), ENG_MANAGER_FIELD);
				if (engManagerField != null)
					ttrd.set(ENG_MANAGER_FIELD, assigneeList);
			}
			
//			User projectDirector = getRoleByRoleName(targetBA.getSystemId(), PROJECT_DIRECTOR_ROLE);
//			if (projectDirector != null){
//				Field prjDirectorField = Field.lookupBySystemIdAndFieldName(targetBA.getSystemId(), PROJECT_DIRECTOR_FIELD);
//				if (prjDirectorField != null)
//					ttrd.set(PROJECT_DIRECTOR_FIELD, projectDirector.getUserLogin());
//			}
						
			Field targetAttachmentField = Field.lookupBySystemIdAndFieldId(targetBA.getSystemId(),
												changeNoteConfig.getTargetAttachmentFieldId());
			if (targetAttachmentField != null)
				ttrd.set( targetAttachmentField.getName(), AttachmentInfo.toJson(allFilesInfo));
				
			ArrayList<BAField> baFields= new ArrayList<BAField>();
			try {
				baFields = (ArrayList<BAField>)GWTServiceHelper.getFields(targetBA, user);
			} catch (TbitsExceptionClient e) {
				e.printStackTrace();
			}
			ArrayList<DisplayGroupClient> dgc = new ArrayList<DisplayGroupClient>();
			try {
				dgc = (ArrayList<DisplayGroupClient>)GWTServiceHelper.getDisplayGroups(targetBA);
			} catch (TbitsExceptionClient e) {
				e.printStackTrace();
			}
	
			RequestData rd = new RequestData(targetSysPrefix, 0, ttrd, dgc, baFields);
			return rd;
		} catch(DatabaseException dbe){
			dbe.printStackTrace();
		} catch(TBitsException tbe){
			tbe.printStackTrace();
		}
		return null;
	}

	private static void insertMappedFieldValuesIntoTTRD(String srcSysPrefix,
			BusinessArea ba, String targetSysPrefix, TbitsTreeRequestData ttrd,
			boolean isFirstRequest, Request req, Hashtable<String,String> fieldMapping) throws DatabaseException {
		if (isFirstRequest){
			if ((fieldMapping != null) && (!fieldMapping.isEmpty()))
			{
				for (String srcFieldName : fieldMapping.keySet())
				{
					Field srcField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), srcFieldName);
					if (srcField != null)
					{
						Object fieldValue = null;
						if (srcField.getDataTypeId() != DataType.ATTACHMENTS){
							fieldValue = req.get(srcFieldName);
						}
						else{
							Collection<AttachmentInfo> srcAttachments = req.getAttachmentsOfType(srcField.getName());
							fieldValue = toAttList(getAttachments(srcAttachments));
						}
						String targetFieldName = fieldMapping.get(srcFieldName);
						if (fieldValue != null && targetFieldName != null)
							ttrd.set(targetFieldName, fieldValue);
					}
				}
			}
		}
	}

	private static void fillAttachments(Field srcAttachmentField,
			ArrayList<AttachmentInfo> allFilesInfo, Request req) {
		if (srcAttachmentField != null){
			Collection<AttachmentInfo> srcAttachments = req.getAttachmentsOfType(srcAttachmentField.getName());
			if (srcAttachments != null){
				int count = 0;
				for(AttachmentInfo attInfo : srcAttachments)
				{  
					AttachmentInfo newAI = new AttachmentInfo() ;
					newAI.name = attInfo.name ;
					newAI.repoFileId = attInfo.repoFileId ;
					newAI.requestFileId = count++ ;
					newAI.size = attInfo.size ;
					allFilesInfo.add(newAI) ;
				}
			}
		}
	}
	
	private static List<AttachmentInfo> getAttachments(Collection<AttachmentInfo> srcAttachments) {
		List<AttachmentInfo> attInfoList = new ArrayList<AttachmentInfo>();
		if (srcAttachments != null){
			for(AttachmentInfo attInfo : srcAttachments)
			{  
				AttachmentInfo newAI = new AttachmentInfo() ;
				newAI.name = attInfo.name ;
				newAI.repoFileId = attInfo.repoFileId ;
				newAI.requestFileId = 0 ;
				newAI.size = attInfo.size ;
				attInfoList.add(newAI) ;
			}
		}
		return attInfoList;
	}

	public static List<FileClient> toAttList(List<AttachmentInfo> attList){
		List<FileClient>attcList=new ArrayList<FileClient>();
		if(attList!=null){
			for(AttachmentInfo att:attList)
			{
				FileClient fc = new FileClient(toAttachmentInfoClient(att));
				attcList.add(fc);
			}
			return attcList;
		}
		return null;
	}
	
	public static AttachmentInfoClient toAttachmentInfoClient(AttachmentInfo att){
		if(att!=null){
			AttachmentInfoClient attc = new AttachmentInfoClient();
			attc.setRepoFileId(att.getRepoFileId());
			attc.setFileName(att.getName());
			attc.setSize(att.getSize());			 
			return attc;
		}		
		return null;

	}
	
	public static Hashtable<String, String> getChangeNoteFieldMap(int aSystemId){
		Hashtable<String, String> cnFieldMap = new Hashtable<String, String>();
		
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT " + TEMPLATE_FIELD_NAME
					+ ", " + FIELD_NAME + " from " + TRN_CHANGE_NOTE_FIELD_MAP + " WHERE src_sys_id = ?");
			ps.setInt(1, aSystemId);
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while(rs.next()){
					String tFieldName = rs.getString(TEMPLATE_FIELD_NAME);
					String srcFieldName = rs.getString(FIELD_NAME);
					cnFieldMap.put(tFieldName, srcFieldName);
				}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		
		return cnFieldMap;
	}
		
	public static ArrayList<User> getRoleByRoleName(int aSystemId, String aRoleName){
		ArrayList<User> userList = new ArrayList<User>();
		try {
			Role role = Role.lookupBySystemIdAndRoleName(aSystemId, aRoleName);
			if (role != null){
				ArrayList<RoleUser> ruList = RoleUser.lookupBySystemIdAndRoleId(aSystemId, role.getRoleId());
				if ((ruList != null) && (ruList.size() > 0)) {
					for (RoleUser ru : ruList){
						if (ru != null){
							User user = User.lookupByUserId(ru.getUserId());
							if (user != null)
								userList.add(user);
						}
					}
				}
			}			
		} catch (DatabaseException e) {
			e.printStackTrace();
		}	
		
		return userList;	
	}
	
	public static Hashtable<String, String> getSourceTargetFieldMapping(
			String srcSysPrefix, String targetSysPrefix){
		Hashtable<String, String> fieldMapping = new Hashtable<String, String>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM change_note_source_target_field_map " +
										"WHERE src_sys_prefix=? and target_sys_prefix=?");
			ps.setString(1, srcSysPrefix);
			ps.setString(2, targetSysPrefix);
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				while(rs.next()){
					String srcFieldName = rs.getString("src_field_name");
					String targetFieldName = rs.getString("target_field_name");
					if ((srcFieldName != null) && (targetFieldName != null)){
						fieldMapping.put(srcFieldName, targetFieldName);
					}
				}
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return fieldMapping;
	}
	

	public static final String PDF 				= ".pdf";
	public static final Object REQUEST_HANDLER 	= "RequestHandler";
	private static final String SOURCE_REQUESTS = "SourceRequests";
	private static final String SYS_ID 			= "sys_id";
	private static final String REQUEST_ID 		= "request_id";
	
}
