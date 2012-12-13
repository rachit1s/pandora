package com.tbitsGlobal.jaguar.server;

import static transbit.tbits.Helper.TBitsPropEnum.IS_AUTOVUE_ENABLED;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.TVN.WebdavConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.BAConfig;
import transbit.tbits.config.DraftConfig;
import transbit.tbits.config.Shortcut;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Report;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserDraft;
import transbit.tbits.domain.UserReadAction;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.PluginManager;
import transbit.tbits.report.TBitsReportEngine;
import transbit.tbits.webapps.ShortcutHandler;
import transbit.tbits.webapps.WebUtil;

import com.tbitsGlobal.jaguar.client.bulkupdate.BulkUpdateService;
import com.tbitsGlobal.jaguar.client.dashboard.GadgetInfo;
import com.tbitsGlobal.jaguar.client.serializables.BARequests;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;
import com.tbitsGlobal.jaguar.client.services.DBService;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.DQLConstants;
import commons.com.tbitsGlobal.utils.client.DQLResults;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.ActionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.ReportClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RolePermissionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.ShortcutClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserDraftClient;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.DQL;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;
import commons.com.tbitsGlobal.utils.server.UtilServiceImpl;

/**
 * The servlet for Jaguar.
 * 
 * @author sourabh
 *
 */
@SuppressWarnings("serial")
public class DBServiceImpl extends UtilServiceImpl implements DBService, BulkUpdateService, TBitsConstants, TBitsPropEnum{
	public static TBitsLogger LOG	= TBitsLogger.getLogger("com.tbitsGlobal.jaguar.server");
	
	static{
		PluginManager.getInstance().loadJaguarPlugins();
	}
	
//	public int getTotalRecords(String sysPrefix, String dql) throws TbitsExceptionClient{
//		try {
//			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
//			User user = WebUtil.validateUser(this.getRequest());
//			Searcher searcher = new Searcher(ba.getSystemId(), user.getUserId(), dql);
//			searcher.search();
//			return searcher.getTotalResultCount();
//		}catch(Exception e){
//			LOG.error(TBitsLogger.getStackTrace(e));
//			throw new TbitsExceptionClient(e);
//		}
//	}
	
	public RequestData getRequestData(String sysPrefix, int requestId) throws TbitsExceptionClient {
		User user = null;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
		ArrayList<DisplayGroupClient> displayGroups = getDisplayGroups(sysPrefix);
		ArrayList<BAField> fields = GWTServiceHelper.getFields(ba, user);
		
		if(displayGroups == null || fields == null)
			return null;
		
		TbitsTreeRequestData model = GWTServiceHelper.getDataByRequestId(user, ba, requestId);
		if(model == null)
			return null;
		
		RequestData data = new RequestData(sysPrefix, requestId, model, displayGroups, fields);
		return data;
		}catch( TbitsExceptionClient tec )
		{
			LOG.error(tec.getMessage(),tec);
			throw tec;
		}
		catch (Exception e) {
			LOG.error(e.getMessage(),e);
			throw new TbitsExceptionClient(e);
		} 
	}
	
	private static String FILE_ACTION = "file_action";
	
	/**
	 * Get action items for a specified request.
	 * @throws TbitsExceptionClient 
	 */
	public ArrayList<ActionClient> getActions(String sysPrefix, int requestId) throws TbitsExceptionClient{
		User user = null;
		BusinessArea ba = null;
		Request request = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			request = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), requestId);
		} catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		if(user == null || ba == null || request == null)
			return null;

		String contextPath = this.getRequest().getContextPath();
		try {
			ArrayList<Action> actions = Action.getAllActions(ba.getSystemId(), requestId, "desc");
			Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash = Action.getAllActionFiles(ba.getSystemId(), requestId);
			
			ArrayList<ActionClient> response = new ArrayList<ActionClient>();
			for(Action action : actions){
				ActionClient actionClient = new ActionClient();
				GWTServiceHelper.setValuesInDomainObject(action, actionClient);
				
				User actionUser = User.lookupAllByUserId(action.getUserId());
				UserClient actionUserClient = GWTServiceHelper.fromUser(actionUser);
				actionClient.setActionUser(actionUserClient);
				
				String  actionLog = (action.getHeaderDescription() == null)
                        ? ""
                        : action.getHeaderDescription();
				actionClient.setHeaderDescription(actionLog);
				
				if(action.getDescriptionContentType() == CONTENT_TYPE_TEXT){
					String description = ClientUtils.htmlify(action.getDescription());
					actionClient.setDescription(description);
				}
				
				Collection<ActionFileInfo> actionFiles = actionFileHash.get(action.getActionId());
	            if((actionFiles != null) && (actionFiles.size() > 0))
	            {
	            	Collection<FileClient> attachments = new ArrayList<FileClient>();
	            	for(ActionFileInfo actionFile : actionFiles){
	            		FileClient attachment = new FileClient();
	            		attachment.setSysPrefix(ba.getSystemPrefix());
	            		attachment.setFieldId(actionFile.getFieldId());
	            		attachment.setFileName(actionFile.getName());
	            		attachment.setRepoFileId(actionFile.getFileId());
	            		attachment.setRequestFileId(actionFile.getRequestFileId());
	            		attachment.setRequestId(actionFile.getRequestId());
	            		attachment.setSize(actionFile.getSize());
	            		attachment.set(FILE_ACTION, actionFile.getFileAction());
	            		attachment.set("hash", actionFile.getHash());
	            		attachments.add(attachment);
	            	}
	            	String attachStr = formatAttachments(ba, requestId, action, actionFiles, contextPath);
	            	actionClient.setAttachmentHTML(attachStr);
	            }
	            
	            response.add(actionClient);
			}
			
			return response;
		} catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	private String formatAttachments(BusinessArea ba, int requestId, Action action, Collection<ActionFileInfo> attachments, String contextPath) throws DatabaseException{
		
		
	    if((attachments == null) || (attachments.size() == 0))
        	return "";
        
    	String                LINE_BREAK     = "<br>";
        String                CLASS          = "";
        String aPath=WebUtil.getNearestPath("");
        String aSysPrefix=ba.getSystemPrefix();
        int aFormat=TBitsConstants.HTML_FORMAT;
        
       StringBuilder sbf = new StringBuilder();
        for (ActionFileInfo attachment : attachments) {
        	String size = "";
        	int attSize = attachment.getSize();
        	
        	if (attSize >= (1024*1024))
        		size = "[ " + String.format("%.0f", ((float)attSize)/(1024*1024)) + " MB ]";
        	else if (attSize < (1024*1024) && attSize >= 1024 )
        		size = "[ " + String.format("%.0f", ((float)attSize)/1024) + " KB ]";        	
        	else if (attSize < 1024)
        		size = "[ " + attSize + " B ]";
        	
//            String conversion  = (attachment.getIsConverted() == true)
//                                 ? " (converted from .bmp)"
//                                 : "";
//            String extraction  = (attachment.getIsExtracted() == true)
//                                 ? " (extracted from .msg)"
//                                 : "";
            String encodedName = "";

            try {
                encodedName = URLEncoder.encode(attachment.getName(), "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                LOG.severe(TBitsLogger.getStackTrace(uee));
            }

            String httpLink = aPath + "read-attachment/" + aSysPrefix + "?" + "request_id=" + requestId + "&request_file_id=" + attachment.getRequestFileId() +"&field_id=" + attachment.getFieldId() + "&action_id=" + action.getActionId();
            String saveLink = "<A " + CLASS + " HREF='" + httpLink + "&saveAs=true" + "' title='Hash : " + attachment.getHash() + "'>Save..." + "</A>&nbsp;";
            
            //AutoVue Added By Abhishek on 21 May
               String autovueProperty = new String();
               boolean isAutovueEnabled = false;
               
            try {
            	autovueProperty = PropertiesHandler.getProperty(IS_AUTOVUE_ENABLED);
                if(autovueProperty.trim().equalsIgnoreCase("true") == true) {
                	isAutovueEnabled = true;
                	}
            	}
            catch(IllegalArgumentException e) {
            	LOG.severe(e.toString());
            }	
            
           
          //AutoDesk FreeWheel  Integration by Nirmal Agarwal on 29-12-11
            
            String httpFreeWheelLink=aPath + "download-file/" + aSysPrefix + "?" + "filerepoid=" + attachment.getFileId() + "&hash=" + attachment.getHash() +"&securitycode=" + attachment.getSecurityCode();
            
            try {
				httpFreeWheelLink = URLEncoder.encode(httpFreeWheelLink, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String freeWheelUrl = "";
			boolean isFreeWheelEnable = false;
			String freewheelProperty = "false";
			boolean isAutodeskEnable = false;
			String freeWheelUrlProperty="";
			
			 try {
	            	freewheelProperty = PropertiesHandler.getProperty(TBitsPropEnum.IS_FREEWHEEL_ENABLED);
	            	freeWheelUrlProperty=PropertiesHandler.getProperty(TBitsPropEnum.FREEWHEEL_URL);
	                if(freewheelProperty != null && freewheelProperty.trim().equalsIgnoreCase("true") == true) {
	                	isAutodeskEnable = true;
	                	}
	                if(freeWheelUrlProperty != null && !freeWheelUrlProperty.trim().equals(""))
	                {
	                	freeWheelUrl=freeWheelUrlProperty.toString();
	                }
	                else
	                {
	                	isAutodeskEnable=false;
	                }
	                	
	                }
	            	
	            catch(IllegalArgumentException e) {
	            	LOG.severe(e.toString());
	            }	
			
            String FreeWheelLink="<A "  + CLASS + " TARGET=\"BLANK\" HREF=\""+ freeWheelUrl +"?path="+ httpFreeWheelLink +"\">[View in FreeWheel...]" + "</A>&nbsp;";
            
           
            String fileName=attachment.getName().trim();
            
            if( fileName.toUpperCase().endsWith(".DWF") || fileName.toUpperCase().endsWith(".DWFX"))
            isFreeWheelEnable=true;
           
         //AutoDesk FreeWheel  Integration 
            String autoVueHttpParams = "/" + requestId + "/" + action.getActionId() + "/" + attachment.getFieldId() + "/" + attachment.getRequestFileId() ;
//            String autoVueHttpParams = "request_id=" + requestId + "&request_file_id=" + attachment.getRequestFileId() +"&field_id=" + attachment.getFieldId() + "&action_id=" + action.getActionId();;
            String autoVueLink=  "<A "  + CLASS + " TARGET=\"BLANK\" HREF='" + aPath + "open-attachment/" + aSysPrefix + autoVueHttpParams + "'>[View in Browser...]" + "</A>&nbsp;";
            
            Field f = Field.lookupBySystemIdAndFieldId(ba.getSystemId(), attachment.getFieldId());
            // Start the Anchor element for HTML format.
            if (aFormat == TBitsConstants.HTML_FORMAT) {
            	sbf.append("\n [ " + f.getDisplayName() + " ] ");
            	if(attachment.getFileAction().equals(WebdavConstants.FILE_ADDED))
            	{
            		sbf.append("Added ");
            	}
            	else if(attachment.getFileAction().equals(WebdavConstants.FILE_MODIFIED))
            	{
            		sbf.append("Modified ");
            	}else if(attachment.getFileAction().equals(WebdavConstants.FILE_DELETED))
            	{
            		sbf.append("Deleted ");
            	}
                sbf.append("\n" + "<a " + CLASS + " href='" + httpLink + "' title='Hash : " + attachment.getHash() + "' target='_blank'>");
            }

            sbf.append(attachment.getName());

            // Close the Anchor element for HTML format.
            if (aFormat == HTML_FORMAT) {
                sbf.append("</a>");
            }

            sbf.append(" " + size);
            //sbf.append(conversion).append(extraction);

            if (aFormat == HTML_FORMAT) {
                sbf.append("&nbsp;&nbsp;").append(saveLink);
            }
            if(isFreeWheelEnable==true && isAutodeskEnable==true)
            {
            sbf.append("&nbsp;&nbsp;").append(FreeWheelLink);
            }
            
            if (aFormat == HTML_FORMAT && isAutovueEnabled == true) {
                sbf.append("&nbsp;&nbsp;").append(autoVueLink);
            }
            
            if ((aFormat == TEXT_FORMAT) && (aPath.trim().equals("") == false)) {
                sbf.append(" <").append(httpLink).append(">");
            }

            sbf.append(LINE_BREAK);
        }

        return sbf.toString();
		
	}
	
	/**
	 * get My requests for all BAs for set of filter fields.
	 * @throws TbitsExceptionClient 
	 */
	public HashMap<String,BARequests> getMyRequests(ArrayList<String> filterFields, int pageSize, int pageNo) throws TbitsExceptionClient{
		User user = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			
			if(user == null)
				return null;
			
			HashMap<String,BARequests> response = new HashMap<String,BARequests>();
			
			ArrayList<BusinessArea> baList = BusinessArea.getBusinessAreasByUserId(user.getUserId());
			for(BusinessArea ba : baList){
				ArrayList<BAField> gridCols = GWTServiceHelper.getFields(ba, user);
				DQLResults results = getMyRequestsByBA(ba.getSystemPrefix(), filterFields, pageSize, pageNo);
				if(gridCols != null && results != null){
					response.put(ba.getSystemPrefix(), new BARequests(gridCols, results));
				}
			}
			
			return response;
		}catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	/**
	 * get My requests for all BAs for set of filter fields for a specific BA.
	 * @throws TbitsExceptionClient 
	 */
	public DQLResults getMyRequestsByBA(String sysPrefix, List<String> filterFields, int pageSize, int pageNo) throws TbitsExceptionClient{
		BusinessArea ba = null;
		try {
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);	
		
			if(ba == null)
				return null;
			
			String dqlString = getDQLfromFilterFields(filterFields);
			DQL dql = new DQL();
			dql.dql = dqlString;
			
			DQLResults results = getRequestsForDQL(sysPrefix, dql, pageSize, pageNo);
			return results;
		}catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	/**
	 * Generate DQL from fields.
	 * @param ba 
	 * 
	 * @param filterFields
	 * @return The DQL.
	 */
	private String getDQLfromFilterFields(List<String> filterFields){
		String userLogin = this.getRequest().getRemoteUser();
		String dql = "";
		for(String field : filterFields){
			if(!dql.equals(""))
				dql += " OR ";
			dql += field + ":(" + userLogin + ")";
		}
		
		return DQLConstants.NON_TEXT + "(" + dql + ")";
	}
	
	/**
	 * Gets My Reports for the current user.
	 * @throws TbitsExceptionClient 
	 */
	public ArrayList<ReportClient> getUserReports() throws TbitsExceptionClient{
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		ArrayList<ReportClient> userReports = new ArrayList<ReportClient>();
		
		ArrayList<Report> userReportsList = null;
		ArrayList<Report> publicReportsList = null;
		try {
			userReportsList = Report.lookupByUserlogin(user.getUserLogin());
			if(userReportsList != null){
				for(Report report : userReportsList){
					ReportClient reportClient = new ReportClient();
					GWTServiceHelper.setValuesInDomainObject(report, reportClient);
					try{
						HashMap<String, String> params = getReportParams(report.getReportId());
						reportClient.setParams(params);
					}catch(TbitsExceptionClient e){
						LOG.error(TBitsLogger.getStackTrace(e));
					}
					userReports.add(reportClient);
				}
			}
			
			publicReportsList = Report.lookupPublicReports();
			if(publicReportsList != null){
				for(Report report : publicReportsList){
					ReportClient reportClient = new ReportClient();
					GWTServiceHelper.setValuesInDomainObject(report, reportClient);
					try{
						HashMap<String, String> params = getReportParams(report.getReportId());
						reportClient.setParams(params);
					}catch(TbitsExceptionClient e){
						LOG.error(TBitsLogger.getStackTrace(e));
					}
					userReports.add(reportClient);
				}
			}
		} catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		return userReports;
	}
	
	private HashMap<String, String> getReportParams(int reportId) throws TbitsExceptionClient{
		HashMap<String, String> params = new HashMap<String, String>();
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			String sql = "SELECT param_name, param_value " +
						"FROM report_params " +
						"where report_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, reportId);
			
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String name = rs.getString(1);
				String value = rs.getString(2);
				params.put(name, value);
			}
		} catch (SQLException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.error(TBitsLogger.getStackTrace(e));
					throw new TbitsExceptionClient(e);
				}
			}
		}
		return params;
	}
	
	/**
	 * Performs Bulk Update.
	 * 
	 * @param updateData. Requests to be updated.
	 * @param addList. Requests to be added.
	 * @throws TbitsExceptionClient 
	 * 
	 */
	public HashMap<Integer, TbitsTreeRequestData> bulkUpdate(String sysPrefix, List<TbitsTreeRequestData> models) throws TbitsExceptionClient {
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		return BulkUpdateServiceHelper.bulkUpdate(user, sysPrefix, models);
	}
	
	/**
	 * Get default search query for a BA for current user.
	 * @throws TbitsExceptionClient 
	 */
	public ShortcutClient getDefaultSearch(String sysPrefix) throws TbitsExceptionClient{
		User user = null;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);	
		}catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		if(user == null || ba == null)
			return null;
		
		/*
         * Get the user details.
         */
        int       userId    = user.getUserId();
        String    userLogin = user.getUserLogin();
        WebConfig webConfig = user.getWebConfigObject();
        BAConfig  baConfig  = webConfig.getBAConfig(sysPrefix);
        
        /*
         * Get the details of the default shortcut if any.
         */
        String   defaultShortcutName     = baConfig.getDefaultShortcutName();
        Shortcut defaultShortcut = null;
		try {
			defaultShortcut = ShortcutHandler.getShortcutByName(userLogin, sysPrefix, defaultShortcutName);
		} catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		if(defaultShortcut != null){
			ShortcutClient scClient = new ShortcutClient();
        	GWTServiceHelper.setValuesInDomainObject(defaultShortcut, scClient);
			return scClient;
		}
		
		return null;
	}
	
	/**
	 * Get all the saved searches for a BA for current user.
	 * @throws TbitsExceptionClient 
	 */
	public ArrayList<ShortcutClient> getSavedSearches(String sysPrefix) throws TbitsExceptionClient{
		ArrayList<ShortcutClient> resp = new ArrayList<ShortcutClient>();
		
		User user = null;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);	
		}catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		if(user == null || ba == null)
			return null;
		
		/*
         * Get the user details.
         */
        WebConfig webConfig = user.getWebConfigObject();
        BAConfig  baConfig  = webConfig.getBAConfig(sysPrefix);
        
        Hashtable<String, Shortcut> usTable = baConfig.getShortcuts();
        ArrayList<Shortcut>         usList  = null;
     
        
        if ((usTable != null) && (usTable.size() > 0)) {
            usList = new ArrayList<Shortcut>(usTable.values());
            usList = Shortcut.sort(usList);
        }
        
        if(usList != null){
	        for(Shortcut sc : usList){
	        	ShortcutClient scClient = new ShortcutClient();
	        	GWTServiceHelper.setValuesInDomainObject(sc, scClient);
	        	resp.add(scClient);
	        }
        }
             
        return resp;
	}
	
	/**
	 * delete a saved search.
	 * @throws TbitsExceptionClient 
	 */
	public boolean deleteSavedSearch(String sysPrefix, String scName) throws TbitsExceptionClient{
		User user = null;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);	
		}catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		if(user == null || ba == null)
			return false;
		
		WebConfig webConfig = user.getWebConfigObject();
        BAConfig  baConfig  = webConfig.getBAConfig(sysPrefix);

        baConfig.deleteShortcut(scName);
        webConfig.setBAConfig(sysPrefix, baConfig);

        String xml = webConfig.xmlSerialize();

        user.setWebConfig(xml);
        try {
			User.update(user);
		} catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		LOG.info("Deleted shortcut: [ " + scName + ", " + sysPrefix + ", " + user.getUserLogin() + ", User Shortcut ]");
		
		return true;
	}
	
	/**
	 * Mark a saved search as shared.
	 * @throws TbitsExceptionClient 
	 */
//	public boolean shareSavedSearch(String sysPrefix, String scName, boolean share) throws TbitsExceptionClient{
//		User user = null;
//		BusinessArea ba = null;
//		try {
//			user = WebUtil.validateUser(this.getRequest());
//			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);	
//		}catch (DatabaseException e) {
//			LOG.error(TBitsLogger.getStackTrace(e));
//			throw new TbitsExceptionClient(e);
//		} catch (TBitsException e) {
//			LOG.error(TBitsLogger.getStackTrace(e));
//			throw new TbitsExceptionClient(e);
//		}
//		
//		if(user == null || ba == null)
//			return false;
//		
//		WebConfig webConfig = user.getWebConfigObject();
//        BAConfig  baConfig  = webConfig.getBAConfig(sysPrefix);
//        
//        Shortcut sc = baConfig.getShortcut(scName);
//        sc.setIsPublic(share);
//        
//        webConfig.setBAConfig(sysPrefix, baConfig);
//
//        String xml = webConfig.xmlSerialize();
//
//        user.setWebConfig(xml);
//        try {
//			User.update(user);
//		} catch (DatabaseException e) {
//			LOG.error(TBitsLogger.getStackTrace(e));
//			throw new TbitsExceptionClient(e);
//		}
//		
//		return true;
//	}
	
/**
 * default saved search
*/
	public boolean defaultSavedSearch(String sysPrefix, String scName, boolean _default) throws TbitsExceptionClient{
		User user = null;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);	
		}catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		if(user == null || ba == null)
			return false;
		
		WebConfig webConfig = user.getWebConfigObject();
        BAConfig  baConfig  = webConfig.getBAConfig(sysPrefix);
        
        Shortcut sc = baConfig.getShortcut(scName);
        sc.setIsDefault(_default);
        
        webConfig.setBAConfig(sysPrefix, baConfig);

        String xml = webConfig.xmlSerialize();

        user.setWebConfig(xml);
        try {
			User.update(user);
		} catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
	return true;
	}
	
	
	public int getActionIdbysysIdbyrequestIdbyuserId(int sysId,int requestId, int userId)
	{   
		int actionId;
		UserReadAction check = new UserReadAction();
		try
		{
			check = UserReadAction.lookupBySystemIdAndRequestIdAndUserId(sysId,requestId,userId);
			actionId = check.getActionId();
			return actionId;
		}
		catch(Exception e)
		{
			return 0;
		}
	
	}
	
	public Boolean registerReadAction(int sysId,int requestId, int actionId,int userId)
	{
			int maxActionId=getActionIdbysysIdbyrequestIdbyuserId(sysId,requestId,userId);
			if(maxActionId<actionId)
			{
				try
				{
					UserReadAction.registerUserReadAction(sysId, requestId, actionId, userId);
				}
				catch (Exception e)
				{}
				return true;
			}
			else return false;
	}
	
	// TODO : this serialization thing is not going to work. We need a more dynamic mechanism
	public List<UserDraftClient> getUserDrafts(String sysPrefix) throws TbitsExceptionClient{
		User user;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);	
		
		ArrayList<UserDraft> drafts	= null;

        try {
            drafts = UserDraft.lookupByUserId(user.getUserId());
        } catch (Exception e) {
            LOG.error(TBitsLogger.getStackTrace(e));
        }
        
        List<UserDraftClient> draftsClient = null;
        if(drafts != null){
        	draftsClient = new ArrayList<UserDraftClient>();
        	for(UserDraft draft : drafts){
        		if(draft.getSystemId() == ba.getSystemId()){
	        		UserDraftClient draftClient = new UserDraftClient();
	        		GWTServiceHelper.setValuesInDomainObject(draft, draftClient);
	        		try {
						Hashtable<String, String> fieldValues = DraftConfig.xmlDeSerialize(draft.getDraft());
						// Retrieve the permission of the user
						Hashtable<String, Integer> perms = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(ba.getSystemId(), draft.getRequestId(), user.getUserId());
						if(perms == null)
							throw new TbitsExceptionClient("Unable to retrieve user permissions"); // can not continue without permissions

						// how about the drafts whose requets are not yet created ? 
						TbitsTreeRequestData model = GWTServiceHelper.createRequestData(user, ba, draft.getRequestId(), fieldValues, perms);
						draftClient.setModel(model);
	        		} catch (TBitsException e) {
	        			LOG.error(TBitsLogger.getStackTrace(e));
	        			throw new TbitsExceptionClient(e);
					}
	        		draftsClient.add(draftClient);
        		}
        	}
        }
        
        return draftsClient;
		} catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	public boolean showOldVersion() {
		Properties props = PropertiesHandler.getAppAndSysProperties();
		String showOldVersion = (String) props.get("transbit.tbits.showOldVersion");
		if(showOldVersion != null && (showOldVersion.trim().equals("0") || showOldVersion.trim().equals("false")))
			return false;
		return true;
	}
	
	public List<GadgetInfo> getGadgetInfo() throws TbitsExceptionClient{
		List<ReportClient> reports = getUserReports();
		String reportIdString = "";
		for(ReportClient report : reports){
			if(!reportIdString.equals(""))
				reportIdString += ",";
			reportIdString += report.getReportId();
		}
		
		List<GadgetInfo> gadgetInfoList = new ArrayList<GadgetInfo>();
		
		User user = null;
		Connection conn = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			conn = DataSourcePool.getConnection();
			
			String sql = "SELECT gadget_user_config.id, " +
						"reports.report_name, " +
						"reports.file_name, " +
						"gadget_user_config.col, " +
						"gadget_user_config.height, " +
						"gadget_user_config.width, " +
						"gadget_user_config.is_visible, " +
						"gadget_user_config.is_minimized, " +
						"gadget_user_config.refresh_rate, " +
						"gadget_user_config.user_id " +
						"FROM gadget_user_config join reports on gadget_user_config.id = reports.report_id " +
						"where reports.report_id in (" + reportIdString +  ") and " +
						"(gadget_user_config.user_id = ? OR gadget_user_config.user_id = 0)" +
						"order by reports.report_id";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, user.getUserId());
			
			List<Integer> ids = new ArrayList<Integer>();
			
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				int gadgetId = rs.getInt(1);
				String caption = rs.getString(2);
				String reportFile = rs.getString(3);
				int column = rs.getInt(4);
				int height = rs.getInt(5);
				int width = rs.getInt(6);
				int isVisble = rs.getInt(7);
				int isMinimized = rs.getInt(8);
				int refreshRate = rs.getInt(9);
				int userId = rs.getInt(10);
//				int left = rs.getInt(10);
//				int top = rs.getInt(11);
				
				if(!(userId == 0 && ids.contains(userId))){
					GadgetInfo info = new GadgetInfo();
					info.setGadgetId(gadgetId);
					info.setCaption(caption);
					info.setReportFile(reportFile);
					info.setColumn(column);
					info.setHeight(height);
					info.setWidth(width);
					info.setIsVisble(isVisble == 1);
					info.setIsMinimized(isMinimized == 1);
					info.setRefreshRate(refreshRate);
	//				info.setLeft(left);
	//				info.setTop(top);
					
					gadgetInfoList.add(info);
					ids.add(gadgetId);
				}
			}
		} catch (SQLException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.error(TBitsLogger.getStackTrace(e));
					throw new TbitsExceptionClient(e);
				}
			}
		}
		
		return gadgetInfoList;
	}
	
	public GadgetInfo updateGadgetInfo(GadgetInfo info) throws TbitsExceptionClient{
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			
//			String sql = "update gadgets set caption = ?, report_file = ? where id = ?";
//			PreparedStatement statement = conn.prepareStatement(sql);
//			statement.setString(1, info.getCaption());
//			statement.setString(2, info.getReportFile());
//			statement.setInt(3, info.getGadgetId());
			
//			statement.execute();
			
			String sql = "update gadget_user_config set col = ?, height = ?, width = ?, is_visible = ?, is_minimized = ?, refresh_rate = ? where id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, info.getColumn());
			statement.setInt(2, info.getHeight());
			statement.setInt(3, info.getWidth());
			statement.setInt(4, info.isVisible() ? 1 : 0);
			statement.setInt(5, info.isMinimized() ? 1 : 0);
			statement.setInt(6, info.getRefreshRate());
//			statement.setInt(7, info.getLeft());
//			statement.setInt(8, info.getTop());
			statement.setInt(7, info.getGadgetId());
			
			statement.execute();
				
			return info;
		} catch (SQLException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.error(TBitsLogger.getStackTrace(e));
					throw new TbitsExceptionClient(e);
				}
			}
		}
	}
	
	public String getGadgetContent(int reportId) throws TbitsExceptionClient {// used to get content of an
		String html = null; // creates an html page and gets its content
		
		User user = null;
		Connection conn = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			conn = DataSourcePool.getConnection();
			
			Report report = Report.lookupByReportId(reportId);
			
			TBitsReportEngine tre = TBitsReportEngine.getInstance();
			
			HashMap<String, Object> reportParams = new HashMap<String, Object>();
			
			String sql = "select gadget_user_params.name, gadget_user_params.value, gadget_user_params.type, gadget_user_params.user_id " +
					"from gadget_user_params join reports on gadget_user_params.id = reports.report_id " +
					"where (gadget_user_params.user_id = ? OR gadget_user_params.user_id = 0) and reports.report_id = ?";
			
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, user.getUserId());
			statement.setInt(2, reportId);
			
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String name = rs.getString(1);
				String value = rs.getString(2);
				String type = rs.getString(3);
				int userId = rs.getInt(4);
				
				if(!(userId == 0 && reportParams.containsKey(name))){
					if(value != null && !value.equals("") && type != null){
						if(type.equals("Date")){
							DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
							Date date = formatter.parse(value);
							java.sql.Date sqlDate =  new java.sql.Date(date.getTime());
							reportParams.put(name, sqlDate);
						}
						else if(type.equals("Time"))
						{
							DateFormat formatter = new SimpleDateFormat("hh:mm:ss a");
							Date date = formatter.parse(value);
							java.sql.Time sqlTime =  new java.sql.Time(date.getTime());
							reportParams.put(name, sqlTime);
						}
						else if(type.equals("DateTime"))
						{
							DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss a");
							Date date = formatter.parse(value);
							java.sql.Date sqlDate =  new java.sql.Date(date.getTime());
							reportParams.put(name, sqlDate);
						}else
							reportParams.put(name, value);
					}
				}
			}
			String tempDir = APIUtil.getTMPDir();
				//Configuration.findAbsolutePath (PropertiesHandler.getProperty(
				//	transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR));
			
			HTMLRenderOption options = new HTMLRenderOption();
			options.setImageHandler(new HTMLServerImageHandler());
			options.setBaseImageURL(getContextPath() + "/web/images/dashboard_images");
			options.setImageDirectory(tempDir + "/../webapps/web/images/dashboard_images");
			
			ByteArrayOutputStream htmlOS = new ByteArrayOutputStream();		
			options.setOutputStream( htmlOS);
			options.setOutputFormat("html");
			options.setEmbeddable(true);
			
			tre.generateReportFile(report.getFileName(), null, reportParams, options);
			
			html = htmlOS.toString();
			htmlOS.close();
			
		} catch (ParseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (SQLException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (IllegalArgumentException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (Exception e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.error(TBitsLogger.getStackTrace(e));
					throw new TbitsExceptionClient(e);
				}
			}
		}
		return html;

	}
	
//	public HashMap<String, List<ScalarParam>> getReportParamsforGadgets(String reportFileName) throws TbitsExceptionClient{
//		User user = null;
//		
//		IReportEngine engine = null;
//		
//		HashMap<String, List<ScalarParam>> reportParams = new HashMap<String, List<ScalarParam>>();
//		try {
//			user = WebUtil.validateUser(this.getRequest());
//			
//			TBitsReportEngine a = EngineMan.get();
//			engine = a.getEngine();
//			IReportRunnable design = null;
//			design = a.getReportDesign(reportFileName);
//
//			IGetParameterDefinitionTask task = engine.createGetParameterDefinitionTask(design);
//			Collection params = task.getParameterDefns(true);
//			Iterator iter = params.iterator();
//			
//			HashMap<String, String> paramValues = getParameterValues(user, task, reportFileName);
//			
//			while (iter.hasNext()) {
//				IParameterDefnBase param = (IParameterDefnBase) iter.next();
//				if (param instanceof IParameterGroupDefn) // if its a group
//				{
//
//					IParameterGroupDefn group = (IParameterGroupDefn) param;
//					if(!reportParams.containsKey(group.getName()))
//						reportParams.put(group.getName(), new ArrayList<ScalarParam>());
//					
//					Iterator groupIter = group.getContents().iterator();
//					while (groupIter.hasNext()) // iterates over the group
//					{
//						IScalarParameterDefn scalar = (IScalarParameterDefn) groupIter.next();
//						String dataType = getScalarDataType(scalar);
//						String value = paramValues.get(scalar.getName());
//						
//						ScalarParam reportParam = new ScalarParam(scalar.getName(), dataType, value);
//						reportParams.get(group.getName()).add(reportParam);
//					}
//				} else {
//					IScalarParameterDefn scalar = (IScalarParameterDefn) param;
//					String dataType = getScalarDataType(scalar);
//					String value = paramValues.get(scalar.getName());
//					
//					if(!reportParams.containsKey(""))
//						reportParams.put("", new ArrayList<ScalarParam>());
//					ScalarParam reportParam = new ScalarParam(scalar.getName(), dataType, value);
//					reportParams.get("").add(reportParam);
//				}
//			}
//		} catch (DatabaseException e) {
//			LOG.error(TBitsLogger.getStackTrace(e));
//			throw new TbitsExceptionClient(e);
//		} catch (TBitsException e) {
//			LOG.error(TBitsLogger.getStackTrace(e));
//			throw new TbitsExceptionClient(e);
//		} catch (SemanticException e) {
//			LOG.error(TBitsLogger.getStackTrace(e));
//			throw new TbitsExceptionClient(e);
//		} catch (EngineException e) {
//			LOG.error(TBitsLogger.getStackTrace(e));
//			throw new TbitsExceptionClient(e);
//		} catch (IllegalArgumentException e) {
//			LOG.error(TBitsLogger.getStackTrace(e));
//			throw new TbitsExceptionClient(e);
//		} catch (SQLException e) {
//			LOG.error(TBitsLogger.getStackTrace(e));
//			throw new TbitsExceptionClient(e);
//		}
//		
//		return reportParams;
//	}
	
	private String getScalarDataType(IScalarParameterDefn scalar){
		String dataType = null;
		
		switch (scalar.getDataType()) {
			case IScalarParameterDefn.TYPE_STRING:
				dataType = "String";
				break;
			case IScalarParameterDefn.TYPE_FLOAT:
				dataType = "Float";
				break;
			case IScalarParameterDefn.TYPE_INTEGER:
				dataType = "Integer";
				break;
			case IScalarParameterDefn.TYPE_DECIMAL:
				dataType = "Decimal";
				break;
			case IScalarParameterDefn.TYPE_DATE_TIME:
				dataType = "DateTime";
				break;
			case IScalarParameterDefn.TYPE_TIME:
				dataType = "Time";
				break;
			case IScalarParameterDefn.TYPE_DATE:
				dataType = "Date";
				break;
			case IScalarParameterDefn.TYPE_BOOLEAN:
				dataType = "Boolean";
				break;
			default:
				dataType = "any";
		}
		
		return dataType;
	}
	
	private HashMap<String, String> getParameterValues(User user, IGetParameterDefinitionTask task, String reportFileName) 
			throws SQLException, TbitsExceptionClient {
		int userId = user.getUserId();
		
		HashMap<String, String> nameValueMap = new HashMap<String, String>();
		
		java.sql.Connection conn = null;
		String qs = "SELECT name, value FROM [dbo].[gadget_user_params] WHERE id = (select id from [dbo].[gadgets] " +
				"where report_file = '" + reportFileName + "') and user_id=" + userId;
		try {
			conn = DataSourcePool.getConnection();
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(qs);
			
			while(rs.next()){
				String name = rs.getString(1);
				String value = rs.getString(2);
				
				nameValueMap.put(name, value);
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.error(TBitsLogger.getStackTrace(e));
					throw new TbitsExceptionClient(e);
				}
			}
		}
		
		return nameValueMap;
	}
	
	private int getGadgetCount(User user) throws TbitsExceptionClient{
		Connection conn = null;
		
		try{
			conn = DataSourcePool.getConnection();
			
			String sql = "SELECT count(*) FROM [dbo].[gadget_user_config],[dbo].[gadgets] " +
					"where [dbo].[gadget_user_config].id =[dbo].[gadgets].id and user_id=?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, user.getUserId());
			
			ResultSet rs = statement.executeQuery();
			rs.next();
			int mcount = rs.getInt(1); // number of gadgets for the user
			statement.close();
			return mcount;
		} catch (SQLException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}finally{
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.error(TBitsLogger.getStackTrace(e));
					throw new TbitsExceptionClient(e);
				}
			}
		}
	}

	public List<RoleClient> getRolesForBA(BusinessAreaClient baClient) throws TbitsExceptionClient {
		try {
			if(baClient != null){
				List<Role> roles = Role.getRolesBySysId(baClient.getSystemId());
				
				List<RoleClient> roleClients = new ArrayList<RoleClient>();
				for(Role role : roles){
					RoleClient roleClient = new RoleClient();
					GWTServiceHelper.setValuesInDomainObject(role, roleClient);
					roleClients.add(roleClient);
				}
				
				return roleClients;
			}
		} catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		return null;
	}
	
	public HashMap<String, RolePermissionClient> getRolePermissionsForBA(BusinessAreaClient baClient, RoleClient roleClient) throws TbitsExceptionClient{
		try {
			Hashtable<String, RolePermission> rolePermissions = RolePermission.getPermissionsBySystemIdAndRoleId(baClient.getSystemId(), roleClient.getRoleId());
			HashMap<String, RolePermissionClient> rolePermsClient = new HashMap<String, RolePermissionClient>();
			for(String name : rolePermissions.keySet()){
				RolePermission perm = rolePermissions.get(name);
				RolePermissionClient permClient = new RolePermissionClient();
				GWTServiceHelper.setValuesInDomainObject(perm, permClient);
				rolePermsClient.put(name, permClient);
			}
			
			return rolePermsClient;
		} catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
}
