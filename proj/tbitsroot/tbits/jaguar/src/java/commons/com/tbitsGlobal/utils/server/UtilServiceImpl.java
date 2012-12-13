package commons.com.tbitsGlobal.utils.server;

import static transbit.tbits.search.SearchConstants.NORMAL_VIEW;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.pdfbox.exceptions.COSVisitorException;

import transbit.tbits.Helper.SerialObjectCloner;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.TVN.Services;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.PDFAnnotationMerge;
import transbit.tbits.authentication.AuthConstants;
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsFileInfo;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.config.DraftConfig;
import transbit.tbits.config.Shortcut;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BAMenu;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserDraft;
import transbit.tbits.domain.UserReadAction;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.mail.TBitsMailer;
import transbit.tbits.plugin.PluginManager;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;
import transbit.tbits.searcher.DqlSearcher;
import transbit.tbits.searcher.SearchResult;
import transbit.tbits.webapps.WebUtil;
import au.com.bytecode.opencsv.CSVWriter;

import commons.com.tbitsGlobal.utils.client.DQLConstants;
import commons.com.tbitsGlobal.utils.client.DQLResults;
import commons.com.tbitsGlobal.utils.client.Dummy;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UserListLoadResult;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.ColPrefs;
import commons.com.tbitsGlobal.utils.client.domainObjects.BAMenuClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJODate;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.DQL;
import commons.com.tbitsGlobal.utils.client.service.UtilService;

public class UtilServiceImpl extends TbitsRemoteServiceServlet implements UtilService, TBitsPropEnum{
	
	public static TBitsLogger LOG	= TBitsLogger.getLogger("commons.com.tbitsGlobal.utils.server");
	private static final long serialVersionUID = 1L;

	public UserClient getCurrentUser() throws TbitsExceptionClient{
		User user = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			
			if(user == null)
				return null;
			
			UserClient userClient = GWTServiceHelper.fromUser(user);
			
			userClient.setColPrefs(getColPreferences(user));
			
			return userClient;
		} catch(TbitsExceptionClient tec)
		{
			LOG.info(TBitsLogger.getStackTrace(tec));
			throw tec;
		} catch (Exception e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	/**
	 * Get column preferences--column to be displayed and their size
	 * @throws TbitsExceptionClient 
	 * @throws SQLException 
	 * @throws SQLException 
	 */
	private HashMap<Integer,HashMap<Integer, List<ColPrefs>>> getColPreferences(User user) throws TbitsExceptionClient{
		HashMap<Integer,HashMap<Integer, List<ColPrefs>>> map = new HashMap<Integer,HashMap<Integer, List<ColPrefs>>>();
		
		int userId = user.getUserId();
		
		getColPreferences(userId, map, true);
		getColPreferences(0, map, false);
		
		return map;
	}
	
	private void getColPreferences(int userId, HashMap<Integer,HashMap<Integer, List<ColPrefs>>> map, boolean override) throws TbitsExceptionClient{
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			
			String sql=	"select distinct sys_id, view_id, field_id, max(col_size), \"order\" " +
					"from user_grid_col_prefs " +
					"WHERE user_id = ? " +
					"group by sys_id, view_id, field_id, \"order\" " +
					"order by \"order\" ASC";
			
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, userId);
			
			HashMap<Integer,HashMap<Integer, List<ColPrefs>>> localMap = new HashMap<Integer, HashMap<Integer,List<ColPrefs>>>();
			
			ResultSet rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					Integer sysId = rs.getInt(1);
					Integer viewId = rs.getInt(2);
					
					Field field = Field.lookupBySystemIdAndFieldId(sysId, rs.getInt(3));
					if(field != null){
						ColPrefs pref = new ColPrefs();
						pref.setName(field.getName());
						pref.setFieldId(rs.getInt(3));
						pref.set(ColPrefs.COLUMN_SIZE,rs.getInt(4));
						
						if(localMap.get(sysId) == null){
							localMap.put(sysId, new HashMap<Integer, List<ColPrefs>>());
						}
						
						if(localMap.get(sysId).get(viewId) == null){
							localMap.get(sysId).put(viewId, new ArrayList<ColPrefs>());
						}
							
						localMap.get(sysId).get(viewId).add(pref);
					}
				}
			}
			ps.close();
			
			for(int sysId : localMap.keySet()){
				HashMap<Integer, List<ColPrefs>> viewMap = localMap.get(sysId);
				if(!map.containsKey(sysId)){
					map.put(sysId, viewMap);
				}else{
					for(int viewId : viewMap.keySet()){
						List<ColPrefs> prefs = viewMap.get(viewId);
						if(!map.get(sysId).containsKey(viewId)){
							map.get(sysId).put(viewId, prefs);
						}else{
							if(override){
								map.get(sysId).put(viewId, prefs);
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		finally{
			if(connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					LOG.error(TBitsLogger.getStackTrace(e));
					throw new TbitsExceptionClient(e);
				}
			}
		}
	}
	
	public List<BusinessAreaClient> getBAList()	throws TbitsExceptionClient {
		try {
			User user = WebUtil.validateUser(this.getRequest());
			if(user != null)
				return GWTServiceHelper.getBAList(user.getUserLogin());
		}
		catch(TbitsExceptionClient tec)
		{
			throw tec;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
		
		return null;
	}
	
	public BAMenuClient getBAMenu() throws TbitsExceptionClient{
		User user = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			
			BAMenuClient respMenu = new BAMenuClient();
			respMenu.setMenuCaption("");
			respMenu.setMenuId(0);
			respMenu.setParentMenuId(0);
			respMenu.setBaList(new ArrayList<BusinessAreaClient>());
			respMenu.setSubMenu(new ArrayList<BAMenuClient>());
			HashMap<Integer, BAMenu> menuMap = BAMenu.getBAMenuMap();
			
			List<BusinessAreaClient> baClientList = GWTServiceHelper.getBAList(user.getUserLogin());
			HashMap<Integer, BusinessAreaClient> baMap = new HashMap<Integer, BusinessAreaClient>();
			for(BusinessAreaClient ba : baClientList){
				baMap.put(ba.getSystemId(), ba);
			}
			
			ArrayList<BusinessAreaClient> baHavingMenu = new ArrayList<BusinessAreaClient>();
			
			for(int menuId : menuMap.keySet()){
				BAMenu menu = menuMap.get(menuId);
				if(menu.getParentMenuId() == 0){
					if((menu.getBaIds() == null || menu.getBaIds().size() == 0) && 
							(menu.getSubMenuIds() == null || menu.getSubMenuIds().size() == 0))
						continue;
					BAMenuClient menuClient = createMenu(menu, menuMap, baMap, baHavingMenu);
					if(menuClient != null && !(menuClient.getBaList() == null && menuClient.getSubMenu() == null))
						respMenu.getSubMenu().add(menuClient);
				}
			}
			
			for(BusinessAreaClient baClient : baMap.values()){
				if(!baHavingMenu.contains(baClient))
					respMenu.getBaList().add(baClient);
			}
			
			return respMenu;
		} catch (TbitsExceptionClient e) {
			throw e;
		} catch (Exception e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	private BAMenuClient createMenu(BAMenu menu, HashMap<Integer, BAMenu> menuMap, HashMap<Integer, BusinessAreaClient> baMap, ArrayList<BusinessAreaClient> baHavingMenu){
		BAMenuClient menuClient = new BAMenuClient();
		GWTServiceHelper.setValuesInDomainObject(menu, menuClient);
		
		ArrayList<Integer> baIds = menu.getBaIds();
		ArrayList<Integer> subMenuIds = menu.getSubMenuIds();
		
		boolean hasBa = false;
		if(baIds != null){
			for(int sysId : baIds){
				if(baMap.containsKey(sysId)){
					hasBa = true;
					if(menuClient.getBaList() == null)
						menuClient.setBaList(new ArrayList<BusinessAreaClient>());
					menuClient.getBaList().add(baMap.get(sysId));
					baHavingMenu.add(baMap.get(sysId));
				}
			}
		}
		
		if(!hasBa && (subMenuIds == null || subMenuIds.size() == 0)){
			return null;
		}
		
		if(subMenuIds != null){
			for(int subMenuId : subMenuIds){
				BAMenu subMenu = menuMap.get(subMenuId);
				BAMenuClient subMenuClient = createMenu(subMenu, menuMap, baMap, baHavingMenu);
				if(subMenuClient != null){
					if(menuClient.getSubMenu() == null)
						menuClient.setSubMenu(new ArrayList<BAMenuClient>());
					menuClient.getSubMenu().add(subMenuClient);
				}
			}
		}
		return menuClient;
	}
	
	public TbitsTreeRequestData addRequest(TbitsTreeRequestData requestObj, String sysPrefix) throws TbitsExceptionClient{
		try
		{
			User user = null;
			BusinessArea ba = null;
			try {
				user = WebUtil.validateUser(this.getRequest());
				ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			} catch (DatabaseException e) {
				LOG.error(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			} catch (TBitsException e) {
				LOG.error(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			}
			
			int reqId = requestObj.getRequestId();
			
			if(0 == reqId )
				return GWTServiceHelper.addRequest(requestObj, user, ba);
			else
				return GWTServiceHelper.updateRequest(requestObj, user, ba);
		}
		catch(TbitsExceptionClient t)
		{
			throw t;
		}
		catch(Throwable t)
		{
			LOG.error(TBitsLogger.getStackTrace(t));
			throw new TbitsExceptionClient(t.getMessage());
		}
	}
	
	public String setColPreferences(int user_id, int view_id, int sys_id, List<ColPrefs> columns) throws TbitsExceptionClient{
		String sql="INSERT INTO user_grid_col_prefs VALUES (?,?,?,?,?,?) ";
		String sql1="DELETE from user_grid_col_prefs WHERE user_id=? and view_id=? and sys_id=? ";
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps1 = connection.prepareStatement(sql1);
			ps1.setInt(1, user_id);
			ps1.setInt(2, view_id);
			ps1.setInt(3, sys_id);
			ps1.executeUpdate();
			ps1.close();
			
			for(ColPrefs column: columns){
				Integer field_id = column.get(ColPrefs.FIELD_ID);
				Integer size = column.get(ColPrefs.COLUMN_SIZE);
				
				PreparedStatement ps = connection.prepareStatement(sql);
				ps.setInt(1,user_id);
				ps.setInt(2,view_id);
				ps.setInt(3,sys_id);
				ps.setInt(4,field_id);
				ps.setInt(5,size);
				ps.setInt(6, columns.indexOf(column));
				ps.executeUpdate();
				ps.close();
			}
		}
		catch (Exception e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
			
		}finally{
			if(connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					LOG.error(TBitsLogger.getStackTrace(e));
					throw new TbitsExceptionClient(e);
				}
			}
				
		}
		return "";
	}
	
	public List<BAField> getFields(String sysPrefix) throws TbitsExceptionClient{
		User user = null;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
		
		return GWTServiceHelper.getFields(ba, user);
		} catch (TbitsExceptionClient e) {
			throw e;
		} catch (Exception e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	public List<BAField> getActiveFields(String sysPrefix) throws TbitsExceptionClient{
		User user = null;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
		
		return GWTServiceHelper.getActiveFields(ba, user);
		} catch (TbitsExceptionClient e) {
			throw e;
		} catch (Exception e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	/**
	 * Splits a string based on multiple separators.
	 * Trims the values after splitting.
	 * @param text
	 * @param separators
	 * @return
	 */
	public static HashMap<String, String> split(String text, String[] separators)
	{
		//indices
		HashMap<Integer, String> indices = new HashMap<Integer, String>();
		for(String sep:separators)
		{
			int idx = text.indexOf(sep);
			if(idx > -1)
				indices.put(idx, sep);
		}
		
		Set<Integer> s = indices.keySet();
		List<Integer> l = new ArrayList<Integer>();
		l.addAll(s);
		Collections.sort(l);
		
		HashMap<String, String> hashMap = new HashMap<String, String>();
		for(int i=0; i<l.size(); i++)
		{
			int start = l.get(i);
			String sep = indices.get(start);
			
			int end = text.length();
			if(i+1 < l.size())
			{
				end = l.get(i+1);
			}
			hashMap.put(sep, text.substring(start + sep.length(), end).trim());
		}
		return hashMap;
	}
	
	public DQLResults getRequestsForDQL(String sysPrefix, DQL dqlObject, int pageSize, int pageNo) throws TbitsExceptionClient{
		try {
			String constraints = dqlObject.dql;
			LOG.info("Start time for search : " + new Date());
			
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			User user = WebUtil.validateUser(this.getRequest());
			
			if(ba == null || user == null)
				throw new TbitsExceptionClient("Could not retrieve user or business area");
			
			DQLResults results = new DQLResults();
			
			//Split the constraints
//			String constraintsCopy = constraints.trim();
//			int non_text = 0;
//			int text = 0;
//			int orderBy = 0;
			
			//TODO: This code has become EXTREMELY HACKY because we are parsing dql here.
			//TODO: The client you just send the proper DQL instead of some other format.  
			//TODO: We should be supporting the paging in dql. devicing some form of dql for again commuicating with server is defeating its purpose.
			//TODO: It is going to fail if the field name is NON_TEXT or TEXT_PARAMS or  ___ORDER_BY_PARAMS
			
//			if(constraintsCopy.contains(DQLConstants.NON_TEXT)){
//				non_text = constraints.indexOf(DQLConstants.NON_TEXT, 0) + DQLConstants.NON_TEXT.length();
//				constraintsCopy = constraintsCopy.substring(non_text).trim();
//			}
//			if(constraintsCopy.contains(DQLConstants.TEXT)){
//				text = constraints.indexOf(DQLConstants.TEXT, non_text) + DQLConstants.TEXT.length();
//			}
//
//			if(constraintsCopy.contains(DQLConstants.ORDER_BY)){
//				orderBy = constraints.indexOf(DQLConstants.ORDER_BY, text) + DQLConstants.ORDER_BY.length();
//			}

//			String fieldConstraints = "";
//			String textConstraints = "";
//			String orderByPart = "";
//			
//			if(non_text > 0){
//				int endIndex = constraints.length();
//				if(text > 0)
//					endIndex = text - DQLConstants.TEXT.length();
//				fieldConstraints = constraints.substring(non_text, endIndex).trim();
//			}
//			if(text > 0)
//			{
//				int endIndex = constraints.length();
//				if(orderBy > 0)
//					endIndex = orderBy - DQLConstants.TEXT.length();
//				textConstraints = constraints.substring(text, endIndex).trim();
//			}
//			
//			if(orderBy > 0)
//				orderByPart = constraints.substring(orderBy).trim();
//			
			HashMap<String, String> tokens = split(constraints, new String[]{ DQLConstants.NON_TEXT, DQLConstants.TEXT, DQLConstants.ORDER_BY });
			String fieldConstraints = "";
			if(tokens.containsKey(DQLConstants.NON_TEXT))
				fieldConstraints = tokens.get(DQLConstants.NON_TEXT);
			
			String textConstraints  =  "";
			if(tokens.containsKey(DQLConstants.TEXT))
				textConstraints = tokens.get(DQLConstants.TEXT);
			
			String orderByPart = "";
			if(tokens.containsKey(DQLConstants.ORDER_BY))
				orderByPart = tokens.get(DQLConstants.ORDER_BY);
			
			if(fieldConstraints.startsWith("(") && fieldConstraints.endsWith(")")){
				fieldConstraints = fieldConstraints.substring(1, fieldConstraints.length() -1).trim();
			}
			if(textConstraints.startsWith("(") && textConstraints.endsWith(")")){
				textConstraints = textConstraints.substring(1, textConstraints.length() -1).trim();
			}
			
			if(orderByPart.startsWith("(") && orderByPart.endsWith(")")){
				orderByPart = orderByPart.substring(1, orderByPart.length() -1).trim();
			}
			
			//Pick the column name part of ordering. If we do not supply the name in the order, it is going to give error.
			//This is a HUGE HACK. It is going to fail if there are more than one column in sorting.
			String orderByColumnNameSuffix = "";
			int sortDir = 0; //0 - desc, 1 - asc
			String orderByColumnNames = "";
			if(orderByPart.endsWith(" ASC"))
			{
				sortDir = 1;
				orderByColumnNames = orderByPart.substring(0, orderByPart.length() - " ASC".length());
				orderByColumnNameSuffix = ", " + orderByColumnNames;
			}
			else if(orderByPart.endsWith(" DESC"))
			{
				orderByColumnNames = orderByPart.substring(0, orderByPart.length() - " DESC".length());
				orderByColumnNameSuffix = ", " + orderByColumnNames;
			}
			
			if(orderByColumnNames.trim().equals("request_id"))
				orderByColumnNameSuffix = "";
			
			String dql = "SELECT sys_id, request_id " + orderByColumnNameSuffix + " " +
							((fieldConstraints.trim().equals(""))?"":"WHERE " + fieldConstraints + " ") +
							((textConstraints.trim().equals(""))?"":"HAS TEXT " + textConstraints + " ") +
							"ORDER BY " + ((orderByPart.length() == 0 ) ? "request_id DESC " : orderByPart);
			
			
			DqlSearcher searcher = new DqlSearcher(ba.getSystemId(), user.getUserId(), dql);
			
			searcher.search();
			
			ArrayList<Integer> requestIdsFetched = new ArrayList<Integer>();
			ArrayList<Integer> ids = new ArrayList<Integer>();
			List<SearchResult> result = searcher.getOrderedResult();
			for(SearchResult sr : result){
				if(sr.getSysId() == ba.getSystemId()){
					requestIdsFetched.add(sr.getRequestId());
				}
			}
			
			if(pageSize > 0 && pageNo > 0){
				for(int i=(pageNo-1)*pageSize; i<pageNo*pageSize; i++){
					try{
						ids.add(requestIdsFetched.get(i));
					}
					catch (IndexOutOfBoundsException e) {
						break;
					}
				}
					
			}
			else
				ids.addAll(requestIdsFetched);
			results.setTotalRecords(requestIdsFetched.size());
			
			int userId = user.getUserId();
			int sysId = ba.getSystemId();
			
			List<TbitsTreeRequestData> requests = new ArrayList<TbitsTreeRequestData>();
			if(ids != null){
				for(int requestId : ids){
					try{
						TbitsTreeRequestData model = getDataByRequestId(sysPrefix, requestId);
						
						UserReadAction check = new UserReadAction();
						check = UserReadAction.lookupBySystemIdAndRequestIdAndUserId(sysId,requestId,userId);
						if(check != null){
							if(check.getActionId() == model.getMaxActionId())
							{
								model.setRead(true);
							}
							else model.setRead(false);
						}
						requests.add(model);
					}catch(TbitsExceptionClient e){
						LOG.error(TBitsLogger.getStackTrace(e));
					}catch (Exception e) {
						LOG.error(TBitsLogger.getStackTrace(e));
					}
				}
			}
			results.setRequests(requests);
			results.setSortColumn(orderByColumnNames);
			results.setSortDirection(sortDir);
			
			LOG.info("End time for search : " + new Date());
			return results;
		}catch (TbitsExceptionClient e) {
			throw e;
		} catch (Exception e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	public TbitsTreeRequestData getDataByRequestId(String sysPrefix, int requestId) throws TbitsExceptionClient{
		User user = null;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
		
		return GWTServiceHelper.getDataByRequestId(user, ba, requestId);
		} catch (TbitsExceptionClient e) {
			throw e;
		} catch (Exception e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	public HashMap<Integer,TbitsTreeRequestData> getDataByRequestIds(String sysPrefix, List<Integer> requestIds){
		HashMap<Integer,TbitsTreeRequestData> resp = new HashMap<Integer,TbitsTreeRequestData>();
		
		for(int requestId : requestIds){
			try {
				TbitsTreeRequestData model = getDataByRequestId(sysPrefix, requestId);
				if(model != null)
					resp.put(requestId, model);
			} catch (TbitsExceptionClient e) {
				LOG.info(TBitsLogger.getStackTrace(e));
			}
			
		}
		return resp;
	}
	
	public UserListLoadResult getAllActiveUsers() throws TbitsExceptionClient{
		try {
			return GWTServiceHelper.getAllActiveUsers();
		}
		catch (Exception e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	public UserListLoadResult getActiveUsers(String query, BAField baField) throws TbitsExceptionClient{
		try {
			return GWTServiceHelper.getActiveUsers(query, baField);
		} catch (Exception e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	public HashMap<Integer,HashMap<String, String>> getAllBACaptions(){
		return GWTServiceHelper.getAllBACaptions();
	}

	public String getContextPath(){
		return this.getRequest().getContextPath();
	}
	
	public List<BAField> getSearchGridColumnsByBA(String sysPrefix) throws TbitsExceptionClient{
		try {
			User user = WebUtil.validateUser(this.getRequest());
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			return GWTServiceHelper.getSearchGridColumnsByBA(ba, user);
		} catch (TbitsExceptionClient e) {
			throw e;
		} catch (Exception e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	public boolean deleteUserDraft(String sysPrefix, int draftId) throws TbitsExceptionClient{
		User user = null;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);	
			UserDraft draft = UserDraft.lookupByUserIdAndSystemIdAndDraftId(user.getUserId(), ba.getSystemId(), draftId);
			if(draft != null)
				UserDraft.delete(draft);
			return true;
		} catch (TbitsExceptionClient e) {
			throw e;
		} catch (Exception e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	public int saveUserDraft(int draftId, String sysPrefix, TbitsTreeRequestData model) throws TbitsExceptionClient{
		User user;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);	
		
		
		Hashtable<String, String> paramTable = GWTServiceHelper.prepareParamTableforAddandUpdate(model, user, ba);
		
		UserDraft userDraft = new UserDraft();
		userDraft.setDraftId(draftId);
		userDraft.setUserId(user.getUserId());
		userDraft.setSystemId(ba.getSystemId());
		userDraft.setRequestId(model.getRequestId());
		userDraft.setTimestamp(new Timestamp((new Date()).getTime()));
		
		String xml = DraftConfig.xmlSerialize(ba.getSystemId(), paramTable);
		userDraft.setDraft("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + xml);
		
		if(draftId >0){
				UserDraft.update(userDraft);
		}else{ 
				UserDraft.insert(userDraft);
		}
		
		return userDraft.getDraftId();
		} catch (TbitsExceptionClient e) {
			throw e;
		} catch (Exception e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}

	public boolean connect() {
		return true;
	}
	
	public Dummy getDummy(Dummy dummy){
		return null;
	}
	
	public boolean showLogout(){
		String authType =  this.getRequest().getAuthType();
		if(authType != null && authType.equals(AuthConstants.AUTH_TYPE))
			return true;
		return false;
	}
	
	public boolean initPlugins() {
		File classesFolder = new File("WEB-INF/classes");
		PluginManager.getInstance().loadJaguarPlugins(classesFolder, null);
		return true;
	}
	
	public ArrayList<DisplayGroupClient> getDisplayGroups(String sysPrefix) throws TbitsExceptionClient {
		BusinessArea ba = null;
		try {
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
		} catch (DatabaseException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		return GWTServiceHelper.getDisplayGroups(ba);
	}
	
//============================================================================================ vv TAGS vv
	
	private static final int PUBLIC_TAGS_USER = -1;
	
	//============================================================================================
	
	/**
	 * Fetch the list of tags for the specified user.
	 * @param user_id
	 * @return List of tag names for the user. null is returned only in case of a SQLException.
	 * @throws TbitsExceptionClient 
	 */
	public HashMap<String, ArrayList<String>> getTagsList(int user_id) throws TbitsExceptionClient {
		
		HashMap<String, ArrayList<String>> tags = new HashMap<String, ArrayList<String>>();
		ArrayList<String> privateTags = new ArrayList<String>();
		ArrayList<String> publicTags = new ArrayList<String>();
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			privateTags = getPrivateTagsList(conn, user_id);
			publicTags = getPublicTagsList(conn);
			
			if(privateTags!=null)
				tags.put("private", privateTags);
			if(publicTags!=null)
				tags.put("public", publicTags);
			
			conn.commit();
			return tags;
		} 
		catch (Exception e) {
			try {
				conn.rollback();
			} 
			catch (Exception e1) {
				e1.printStackTrace();
			}
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		finally{
			if(conn != null){
				try {
					conn.close();
				} 
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Fetch the list of public tags.
	 * @return List of public tag names. null is returned only in case of a SQLException.
	 * @throws SQLException 
	 */
	private ArrayList<String> getPublicTagsList(Connection conn) throws SQLException {
		
		ArrayList<String> tags = new ArrayList<String>();
		
		String query = "select name from tags_definitions where user_id=?";
		PreparedStatement statement = conn.prepareStatement(query);
		statement.setInt(1, PUBLIC_TAGS_USER);
		ResultSet rs = statement.executeQuery();
		if(rs != null)
			while(rs.next()){
				tags.add(rs.getString("name"));
			}
		statement.close();
		rs.close();
		
		return tags;
	}
	
	/**
	 * Fetch the list of private tags.
	 * @return List of public tag names. null is returned only in case of a SQLException.
	 * @throws SQLException 
	 */
	private ArrayList<String> getPrivateTagsList(Connection conn, int user_id) throws SQLException {
		
		ArrayList<String> tags = new ArrayList<String>();
		
		String query = "select name from tags_definitions where user_id=?";
		PreparedStatement statement = conn.prepareStatement(query);
		statement.setInt(1, user_id);
		ResultSet rs = statement.executeQuery();
		if(rs != null)
			while(rs.next()){
				tags.add(rs.getString("name"));
			}
		statement.close();
		rs.close();
		
		return tags;
	}
	
	//============================================================================================
	
	/**
	 * Apply specified type of tag to the specified requests.
	 * @param tag
	 * @param tagType
	 * @param user_id
	 * @param sys_id
	 * @param requests
	 * @return True if the operation was completed successfully.
	 * 			False if the tag does not exist or a SQLException occured.
	 * @throws TbitsExceptionClient 
	 */
	public boolean applyTag(String tag, String tagType, int user_id, int sys_id, ArrayList<Integer> requests) throws TbitsExceptionClient {
		
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			int searchTagUser = user_id;
			// Check if the tag to be applied is public tag and search the tag accordingly
			if(tagType.equals("public"))
				searchTagUser = PUBLIC_TAGS_USER;
			
			// Find the tag id
			String query = "select tag_id from tags_definitions where user_id=? and name=?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, searchTagUser);
			statement.setString(2, tag);
			ResultSet rs = statement.executeQuery();
			if(rs == null)
				return false;
			rs.next();
			int tag_id = rs.getInt("tag_id");
			statement.close();
			rs.close();
			
			// Add the requests which are not already tagged by this tag
			String checkQuery = "select * from tags_requests where tag_id=? and sys_id=? and request_id=?";
			String insertStatement = "insert into tags_requests values (?,?,?,?)";
			for(int req : requests){
				PreparedStatement stmt = conn.prepareStatement(checkQuery);
				stmt.setInt(1, tag_id);
				stmt.setInt(2, sys_id);
				stmt.setInt(3, req);
				rs = stmt.executeQuery();
				if(rs != null){
					if(rs.next()){
						rs.close();
						stmt.close();
						continue;
					}
				}
				rs.close();
				stmt.close();
				
				stmt = conn.prepareStatement(insertStatement);
				stmt.setInt(1, tag_id);
				stmt.setInt(2, sys_id);
				stmt.setInt(3, req);
				stmt.setInt(4, user_id);
				stmt.executeUpdate();
				stmt.close();
			}
			
			conn.commit();
			return true;
		} 
		catch (Exception e) {
			try {
				conn.rollback();
			} 
			catch (Exception e1) {
				e1.printStackTrace();
			}
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		finally{
			if(conn != null){
				try {
					conn.close();
				} 
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//============================================================================================

	/**
	 * Add the given tag to the tags table.
	 * @param tag
	 * @param user_id
	 * @return True if the operation was completed successfully.
	 * 			False if the tag already exists or a SQLException occured.
	 * @throws TbitsExceptionClient 
	 */
	public boolean addTag(String tag, int user_id) throws TbitsExceptionClient {
		
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			// Find the max tag id
			int max_id = 0;
			String query = "select max(tag_id) from tags_definitions";
			PreparedStatement statement = conn.prepareStatement(query);
			ResultSet rs = statement.executeQuery();
			if(rs != null){
				rs.next();
				max_id = rs.getInt(1);
			}
			statement.close();
			rs.close();
			
			// Insert the tag into the table if it does not already exist
			query = "select * from tags_definitions where user_id=? and name=?";
			PreparedStatement check = conn.prepareStatement(query);
			check.setInt(1, user_id);
			check.setString(2, tag);
			rs = check.executeQuery();
			if(rs != null)
				if(rs.next())
					return false;
			check.close();
			rs.close();
			
			query =	"insert into tags_definitions values (?,?,?)";
			PreparedStatement insert = conn.prepareStatement(query);
			insert.setInt(1, max_id+1);
			insert.setInt(2, user_id);
			insert.setString(3, tag);
			insert.executeUpdate();
			insert.close();
			
			conn.commit();
			return true;
		} 
		catch (Exception e) {
			try {
				conn.rollback();
			} 
			catch (Exception e1) {
				e1.printStackTrace();
			}
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		finally{
			if(conn != null){
				try {
					conn.close();
				} 
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//============================================================================================

	/**
	 * Delete the tag and disassociate all requests associated with this tag.
	 * @param tag
	 * @param user_id
	 * @return True if the operation was completed successfully.
	 * 			False if the tag does not exist or a SQLException occured.
	 * @throws TbitsExceptionClient 
	 */
	public boolean deleteTag(String tag, int user_id) throws TbitsExceptionClient {
		
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			// Find the tag id
			String query = "select tag_id from tags_definitions where user_id=? and name=?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, user_id);
			statement.setString(2, tag);
			ResultSet rs = statement.executeQuery();
			if(rs == null)
				return false;
			rs.next();
			int tag_id = rs.getInt("tag_id");
			statement.close();
			rs.close();
			
			// Remove all request associations of the tag
			query = "delete from tags_requests where tag_id=?";
			PreparedStatement delete = conn.prepareStatement(query);
			delete.setInt(1, tag_id);
			delete.executeUpdate();
			delete.close();
			
			// Remove tag from the tags definitions
			query = "delete from tags_definitions where tag_id=?";
			delete = conn.prepareStatement(query);
			delete.setInt(1, tag_id);
			delete.executeUpdate();
			delete.close();
			
			conn.commit();
			return true;
		} 
		catch (Exception e) {
			try {
				conn.rollback();
			} 
			catch (Exception e1) {
				e1.printStackTrace();
			}
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		finally{
			if(conn != null){
				try {
					conn.close();
				} 
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//============================================================================================

	/**
	 * Change the name of oldTag to newTag for the specified user.
	 * @param oldTag
	 * @param newTag
	 * @param user_id
	 * @return True if the operation was completed successfully.
	 * 			False if a SQLException occured.
	 * @throws TbitsExceptionClient 
	 */
	public boolean modifyTag(String oldTag, String newTag, int user_id) throws TbitsExceptionClient {
		
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			String query = "update tags_definitions set name=? where user_id=? and name=?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setString(1, newTag);
			statement.setInt(2, user_id);
			statement.setString(3, oldTag);
			statement.executeUpdate();
			statement.close();
			
			conn.commit();
			return true;
		} 
		catch (Exception e) {
			try {
				conn.rollback();
			} 
			catch (Exception e1) {
				e1.printStackTrace();
			}
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		finally{
			if(conn != null){
				try {
					conn.close();
				} 
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//============================================================================================

	/**
	 * Fetch all the requests tagged with the specified tags by the specified user in the specified business area.
	 * @param tags
	 * @param user_id
	 * @param sys_id
	 * @return List of TbitsTreeRequestData that holds the information of all the relevant requests.
	 * 			null returned only in case of a SQLException.
	 * @throws TbitsExceptionClient 
	 */
	public List<TbitsTreeRequestData> fetchTaggedRequests(HashMap<String, ArrayList<String>> tags, int user_id, int sys_id) throws TbitsExceptionClient {
		
		List<TbitsTreeRequestData> taggedRequests = new ArrayList<TbitsTreeRequestData>();
		ArrayList<Integer> requestsToBeFetched = new ArrayList<Integer>();
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			BusinessArea ba = BusinessArea.lookupBySystemId(sys_id);
			
			// Fetch requests tagged by private tags
			for(String tag : tags.get("private")){
				String query = 	"select request_id from tags_requests tr join tags_definitions td " +
								"on tr.tag_id=td.tag_id and td.user_id=? and td.name=? and tr.sys_id=?";
				PreparedStatement statement = conn.prepareStatement(query);
				statement.setInt(1, user_id);
				statement.setString(2, tag);
				statement.setInt(3, sys_id);
				ResultSet rs = statement.executeQuery();
				if(rs != null){
					if(rs.next()){
						do{
							int req = rs.getInt("request_id");
							if(!requestsToBeFetched.contains(req));
							requestsToBeFetched.add(req);
						}while(rs.next());
					}
				}
				rs.close();
				statement.close();
			}
			
			// Fetch requests tagged by public tags
			for(String tag : tags.get("public")){
				String query = 	"select request_id from tags_requests tr join tags_definitions td " +
								"on tr.tag_id=td.tag_id and td.user_id=? and td.name=? and tr.sys_id=?";
				PreparedStatement statement = conn.prepareStatement(query);
				statement.setInt(1, PUBLIC_TAGS_USER);
				statement.setString(2, tag);
				statement.setInt(3, sys_id);
				ResultSet rs = statement.executeQuery();
				if(rs != null){
					if(rs.next()){
						do{
							int req = rs.getInt("request_id");
							if(!requestsToBeFetched.contains(req));
							requestsToBeFetched.add(req);
						}while(rs.next());
					}
				}
				rs.close();
				statement.close();
			}
			
			// Fetch the request data models
			Collection<TbitsTreeRequestData> fetchedRequests = getDataByRequestIds(ba.getSystemPrefix(), requestsToBeFetched).values();
			taggedRequests.addAll(fetchedRequests);
			
			conn.commit();
			return taggedRequests;
		} 
		catch (Exception e) {
			try {
				conn.rollback();
			} 
			catch (Exception e1) {
				e1.printStackTrace();
			}
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} 
		finally{
			if(conn != null){
				try {
					conn.close();
				} 
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//============================================================================================

	/**
	 * Remove all the tags defined by the specified user associated with the given requests in the specified business area.
	 * @param user_id
	 * @param system_id
	 * @param requests
	 * @return True if the operation was completed successfully.
	 * 			False if a SQLException occured.
	 * @throws TbitsExceptionClient 
	 */
	public boolean removeAllTagsFromRequests(int user_id, int system_id, ArrayList<Integer> requests) throws TbitsExceptionClient {
		
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			
			for(int req : requests){
				String query = "select td.tag_id as tag_id from tags_requests tr join tags_definitions td on " +
								"tr.sys_id=? and tr.request_id=? and td.user_id=?";
				PreparedStatement statement = conn.prepareStatement(query);
				statement.setInt(1, system_id);
				statement.setInt(2, req);
				statement.setInt(3, user_id);
				ResultSet rs = statement.executeQuery();
				if(rs != null){
					String batchUpdate = "delete from tags_requests where tag_id=? and sys_id=? and request_id=?";
					PreparedStatement batchStatement = conn.prepareStatement(batchUpdate);
					batchStatement.setInt(2, system_id);
					batchStatement.setInt(3, req);
					while(rs.next()){
						batchStatement.setInt(1, rs.getInt("tag_id"));
						batchStatement.addBatch();
					}
					batchStatement.executeBatch();
					batchStatement.close();
				}
				rs.close();
				statement.close();
			}
			
			conn.commit();
			return true;
		} 
		catch (Exception e) {
			try {
				conn.rollback();
			} 
			catch (Exception e1) {
				e1.printStackTrace();
			}
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		finally{
			if(conn != null){
				try {
					conn.close();
				} 
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//============================================================================================

	/**
	 * Remove the specified tag from the given list of requests.
	 * @param tag
	 * @param user_id
	 * @param system_id
	 * @param requests
	 * @return True if the operation was completed successfully.
	 * 			False if the tag does not exist or a SQLException occured.
	 * @throws TbitsExceptionClient 
	 */
	public boolean removeTagFromRequests(String tag, String tagType, int user_id, int system_id, ArrayList<Integer> requests) throws TbitsExceptionClient {
		
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			int searchTagUser = user_id;
			// Check if the tag to be removed is public tag and remove the tag accordingly
			if(tagType.equals("public"))
				searchTagUser = PUBLIC_TAGS_USER;
			
			int tag_id = -1;
			String query = 	"select tag_id from tags_definitions where " +
							"user_id=? and name=?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, searchTagUser);
			statement.setString(2, tag);
			ResultSet rs = statement.executeQuery();
			
			if(rs != null){
				if(rs.next())
					tag_id = rs.getInt("tag_id");
			}
			
			if(tag_id == -1)
				return false;
			
			rs.close();
			statement.close();
			
			int rowsAffected = 0;
			for(int req : requests){
				
				query = "delete from tags_requests where tag_id=? and sys_id=? and request_id=? and user_id=?";
				statement = conn.prepareStatement(query);
				statement.setInt(1, tag_id);
				statement.setInt(2, system_id);
				statement.setInt(3, req);
				statement.setInt(4, user_id);
				rowsAffected += statement.executeUpdate();
				statement.close();
			}
			
			conn.commit();
			if(rowsAffected == 0)
				return false;
			return true;
		} 
		catch (Exception e) {
			try {
				conn.rollback();
			} 
			catch (SQLException e1) {
				e1.printStackTrace();
			}
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		finally{
			if(conn != null){
				try {
					conn.close();
				} 
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Return the tbits properties value corresponding to the enabling of tags
	 * @return boolean value corresponding to whether tags are supported
	 * @throws TbitsExceptionClient
	 */
	public boolean getIsTagsSupported() throws TbitsExceptionClient {
		
		boolean isTagsSupported = false;
		
		String propVal = PropertiesHandler.getProperty("jaguar.isTagsSupported");
		if(propVal != null && propVal.equals("true"))
			isTagsSupported = true;
		
		return isTagsSupported;
	}

	//============================================================================================ ^^ TAGS ^^
	
	/**
	 * Exports a grid to CSV
	 * @throws TbitsExceptionClient 
	 */
	public String exportGrid(String sysPrefix, DQL dql, List<String> includedFields, int pageSize, int pageNo) throws TbitsExceptionClient {
		DQLResults dqlResults = this.getRequestsForDQL(sysPrefix, dql, pageSize, pageNo);
		return exportGrid(sysPrefix, dqlResults.getRequests(), includedFields);
	}
	
	/**
	 * Exports a grid to CSV
	 * @throws TbitsExceptionClient 
	 */
	public String exportGrid(String sysPrefix, Collection<TbitsTreeRequestData> models, List<String> includedFields) throws TbitsExceptionClient {
		try {
		String tmpOutputLoc =  APIUtil.getTMPDir();//Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_TMPDIR));
		File of;
		String outFileName = "";
		int i = 0;
		
		// TODO :this is wrong logic of generation of a file. One should rely only
		// on create temp file and then use it for any other 
		do
		{
			Random r = new Random();
			
			outFileName += r.nextInt(1000) + "-" + i++ + ".csv";
			of = new File(tmpOutputLoc + "/" + outFileName);
		}
		while(of.exists());
		
		of.createNewFile();
		
		OutputStreamWriter out = null;
		
			out = new OutputStreamWriter(new FileOutputStream(of),"UTF-8");
		
		BusinessArea ba = null;
		User user = null;
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			user = WebUtil.validateUser(this.getRequest());
		
		if(ba != null && user != null && out != null){
//			List<BAField> fields = this.getFields(sysPrefix);
			
			List<Field> fields = new ArrayList<Field>();
			if(includedFields != null){
				for(String fieldName : includedFields){
					try {
						Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), fieldName);
						if(field != null){
							fields.add(field);
						}
					} catch (DatabaseException e) {
						LOG.error(TBitsLogger.getStackTrace(e));
					}
				}
			}else{
				try {
					fields = Field.getFieldsBySystemIdAndUserId(ba.getSystemId(), user.getUserId());
				} catch (DatabaseException e) {
					LOG.error(TBitsLogger.getStackTrace(e));
				}
			}
			
			CSVWriter csvw = new CSVWriter(out);
			
			ArrayList<String> row = new ArrayList<String>();
			for(Field field : fields){
				if(!GWTServiceHelper.isAttachmentField(field) ){
					row.add(field.getName());
				}
			}
			
			csvw.writeNext(row.toArray(new String[1]));
			
			row = new ArrayList<String>();
			for(Field field : fields){
				if(!GWTServiceHelper.isAttachmentField(field)){
					row.add(field.getDisplayName());
				}
			}
			csvw.writeNext(row.toArray(new String[1]));
			
			for(TbitsTreeRequestData model : models){
				row = new ArrayList<String>();
				for(Field field : fields){
					if(!GWTServiceHelper.isAttachmentField(field)){
						POJO pojo = model.getAsPOJO(field.getName());
						String value = "";
						if(pojo != null){
							if(GWTServiceHelper.isDateField(field)){
								Date date = ((POJODate)pojo).getValue();
								value = GWTServiceHelper.formatDate(date, TBitsConstants.API_DATE_FORMAT);
							}else
								value = pojo.toString();
						}
						row.add(value);
					}
				}
				csvw.writeNext(row.toArray(new String[1]));
			}
			
			try {
				out.flush();
				out.close();
				csvw.close();
			} catch (IOException e) {
				LOG.error("Unable to close the csv writer.", e);
				throw new TbitsExceptionClient(e);
			}
			
			if(of != null)
				return "/download-delete?saveAs=true&file=" + of.getName();
		}
		
		} catch (Exception e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		return null;
	}
	
	/**
	 * Save a search query
	 * @throws TbitsExceptionClient 
	 */
	public boolean saveSearch(String sysPrefix, HashMap<String, String> params) throws TbitsExceptionClient{
		User user = null;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);	
		
		if(user == null || ba == null)
			return false;
		
		String      scName        = GWTServiceHelper.readStringParam(params, "scName", "");
        String      query         = GWTServiceHelper.readStringParam(params, "query", "");
        String      desc          = GWTServiceHelper.readStringParam(params, "description", "");
        String      filter        = GWTServiceHelper.readStringParam(params, "filter", "subject");
        int         view          = GWTServiceHelper.readIntegerParam(params, "view", NORMAL_VIEW);
        boolean     bDefault      = GWTServiceHelper.readBooleanParam(params, "isDefault", false);
        boolean     bPublic       = GWTServiceHelper.readBooleanParam(params, "isPublic", false);
        boolean     bListAll      = GWTServiceHelper.readBooleanParam(params, "listAll", false);
        boolean     bIsBAShortcut = GWTServiceHelper.readBooleanParam(params, "isBAWide", false);
        
        /*
         * Only a business area admin can set the shortcut to be available for
         * entire business area.
         */
        if (bIsBAShortcut == true) {

            /*
             * Check if the user is an admin in this BA. If not, we cannot
             * mark this shortcut as BA wide available one.
             */
            boolean isUserAdmin = false;
			try {
				isUserAdmin = GWTServiceHelper.isAdmin(ba.getSystemId(), user.getUserId());
			} catch (DatabaseException e) {
				LOG.error(TBitsLogger.getStackTrace(e));
				return false;
			}

            if (isUserAdmin == false) {
                bIsBAShortcut = false;
            }
        }

        boolean flag = Shortcut.saveShortcut(user, ba, sysPrefix, scName, query, desc, filter, view, bDefault, bPublic, bListAll, bIsBAShortcut);

        return flag;
        
		}catch (Exception e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	public String mergePDF(String sysPrefix, int requestId, int actionId, List<FileClient> fileClients) throws TbitsExceptionClient {	
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			int sysId = ba.getSystemId();
			
			FileClient parentFileClient = fileClients.get(0);
			TBitsFileInfo parentFile = Uploader.getFileInfo(sysId, requestId, actionId, parentFileClient.getRequestFileId(), parentFileClient.getFieldId());
			List<TBitsFileInfo> files = new ArrayList<TBitsFileInfo>();
			
			for(int i = 1; i < fileClients.size(); i++ ){
				FileClient fileClient = fileClients.get(i);
				TBitsFileInfo fi = Uploader.getFileInfo(sysId, requestId, actionId, fileClient.getRequestFileId(), fileClient.getFieldId());
				files.add(fi);
			}
			File file = PDFAnnotationMerge.getMergedFile(parentFile, files);
			if (file != null)
				return "/download-delete?saveAs=true&file=" + file.getName();
		} catch(Throwable e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		return null;
	}
	
	public void sendEmailAgain(String sysPrefix, int requestId) throws TbitsExceptionClient
	{
		int sysId = 0;
		try
    	{
    		BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
    		if(ba == null)
    		{
    			throw new TbitsExceptionClient("Invalid BA.");
    		}
    		sysId = ba.getSystemId();
    	
		Request request;
			request = Request
					.lookupBySystemIdAndRequestId(sysId, requestId);
			if (request == null) {
				throw new TbitsExceptionClient("The request id " + requestId
						+ " does not exist in the " + sysPrefix + " business area");
			} else {
				(new TBitsMailer(request)).sendMail();
			}
		} catch (Exception e) {
			throw new TbitsExceptionClient("Unexpected error occurred while sending the mail. Please contact the system admin and check the logs.", e);
		}
	}
	
	
	/**
	 * Fetches the Html mail content for the given sysId, userId and requestId.
	 * This is being used in the permission tool and print preview.
	 */
	@SuppressWarnings("unchecked")
	public String getEmailHtml(int sysId, int userId, int reqId) {
		
		String htmlMailContent = null;
		try {
			User clonedRecepient = (User) SerialObjectCloner.copy(User.lookupByUserId(userId));
			Request clonedRequest = (Request) SerialObjectCloner.copy(Request.lookupBySystemIdAndRequestId(sysId, reqId))  ;
    		Hashtable<String,Integer> clonedPermissions = (Hashtable<String,Integer>) SerialObjectCloner.copy(RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(sysId, reqId,  userId));
    		ArrayList<Action> clonedActions = (ArrayList<Action>) SerialObjectCloner.copy(Action.getAllActions(sysId, reqId, "desc"));
    		Hashtable<Integer, Collection<ActionFileInfo>> clonedActionFiles = (Hashtable<Integer, Collection<ActionFileInfo>>) SerialObjectCloner.copy(Action.getAllActionFiles(sysId, reqId));
    		
    		htmlMailContent = TBitsMailer.getHtmlMailContent(clonedRecepient, clonedRequest, clonedActions, clonedPermissions, clonedActionFiles);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return htmlMailContent;
	}

	@Override
	public String getHTMLForRequestPrint(int sysId, int userId, int reqId) {
		String htmlMailContent = null;
		try {
			User clonedRecepient = (User) SerialObjectCloner.copy(User.lookupByUserId(userId));
			Request clonedRequest = (Request) SerialObjectCloner.copy(Request.lookupBySystemIdAndRequestId(sysId, reqId))  ;
			Hashtable<String, Integer> emailPermissions = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(sysId, reqId,  userId);
			
			Hashtable<String, Integer> viewPermissions = new Hashtable<String, Integer>();
			// The print preview should be corresponding to the permissions on View 
			// but the underlying email formatter uses Permissions.VIew
			// So, pick view permissions and set those as email permissions.
			for(String principal:emailPermissions.keySet())
			{
				 Field f = Field.lookupBySystemIdAndFieldName(sysId, principal);
                 //return (n & (~to)) | (((n&from)/from)*to);
                 int overallPermission = emailPermissions.get(principal);
                 int newPermission = overallPermission;

                 if(null != f )
                 {
                         newPermission = 0;
                         newPermission = ((overallPermission & Permission.VIEW) & ( f.getPermission() & Permission.VIEW  ) ) << 1;
                 }
                 viewPermissions.put(principal, newPermission);
			}
    		
    		ArrayList<Action> clonedActions = (ArrayList<Action>) SerialObjectCloner.copy(Action.getAllActions(sysId, reqId, "desc"));
    		Hashtable<Integer, Collection<ActionFileInfo>> clonedActionFiles = (Hashtable<Integer, Collection<ActionFileInfo>>) SerialObjectCloner.copy(Action.getAllActionFiles(sysId, reqId));
    		
    		htmlMailContent = TBitsMailer.getHtmlMailContent(clonedRecepient, clonedRequest, clonedActions, viewPermissions, clonedActionFiles);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return htmlMailContent;
	}
	
	//================================================== vv TVN vv

	public boolean getIsTvnSupported() {
		boolean isTvnSupported = false;
		
		String propVal = PropertiesHandler.getProperty("jaguar.isTvnHandlerEnabled");
		if(propVal != null && propVal.equals("true"))
			isTvnSupported = true;
		
		return isTvnSupported;
	}

	public String getTvnProtocolUrl(String server, int systemId, int requestId, int attFieldId) throws TbitsExceptionClient {
		
		if(getContextPath() != null && !getContextPath().equals(""))
			server += "/"+getContextPath();
		
		String filePath = Services.createPathFor(systemId, requestId, attFieldId);
		if(filePath == null || filePath.equals(""))
			throw new TbitsExceptionClient("Could not construct Tvn URL for sysId : "+systemId+" and requestId : "+requestId+".");
		
		server = server.trim();
		return "tsvn:"+server+"/tvn/"+filePath;
	}
	
	//================================================== ^^ TVN ^^
}
