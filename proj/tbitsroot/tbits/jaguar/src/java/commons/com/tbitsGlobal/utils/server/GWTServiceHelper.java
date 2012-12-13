package commons.com.tbitsGlobal.utils.server;

import static transbit.tbits.api.Mapper.ourMailListUserMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;

import com.google.gwt.i18n.client.DateTimeFormat;

import transbit.tbits.Helper.FieldPropertiesConstants;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.api.UserRadixTree;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Timestamp;
import transbit.tbits.config.CaptionsProps;
import transbit.tbits.config.CustomLink;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.DisplayGroup;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FieldProperties;
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.TextDataType;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UserListLoadResult;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCheckBox;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldDate;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldInt;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldString;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldTextArea;
import commons.com.tbitsGlobal.utils.client.domainObjects.AttachmentInfoClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.CustomLinkClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.SysConfigClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeDependency;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;
import commons.com.tbitsGlobal.utils.client.pojo.POJOBoolean;
import commons.com.tbitsGlobal.utils.client.pojo.POJODate;
import commons.com.tbitsGlobal.utils.client.pojo.POJOInt;
import commons.com.tbitsGlobal.utils.client.pojo.POJOString;

public class GWTServiceHelper implements FieldPropertiesConstants{
	public static TBitsLogger LOG	= TBitsLogger.getLogger("commons.com.tbitsGlobal.utils.server");
	
	public static ArrayList<BAField> getSearchGridColumnsByBA(BusinessArea ba, User user) 
			throws DatabaseException, TbitsExceptionClient{
		try
		{
		if(ba == null)
			return null;
		
		ArrayList<Field> fields = new ArrayList<Field>();
		ArrayList<String> searchGridCols = user.getWebConfigObject().getBAConfig(ba.getSystemPrefix()).getDisplayHeader();
		
		if(searchGridCols != null){
			for(String fieldStr : searchGridCols){
				try {
					Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), fieldStr);
					fields.add(field);
				} catch (DatabaseException e) {
					LOG.info(TBitsLogger.getStackTrace(e));
				}
			}
		}
			
		ArrayList<BAField> response = GWTServiceHelper.fromFields(ba, fields, user);
		return response;
		}
		catch(Exception e)
		{
			LOG.error(e);
			throw new TbitsExceptionClient(e);
		}
	}
	
	/**
	 * get List of {@link BAField} for a given {@link BusinessArea}
	 * @param ba
	 * @param user
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static ArrayList<BAField> getActiveFields(BusinessArea ba, User user) 
			throws TbitsExceptionClient{
		try {
			if(ba == null || user == null)
				return null;
			
			List<Field> fields = Field.lookupActiveBySystemId(ba.getSystemId());
			
			if(fields == null)
				return null;
			
			ArrayList<BAField> response = fromFields(ba, fields, user);
			
			return response;
		} catch (Exception e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	/**
	 * get List of {@link BAField} for a given {@link BusinessArea}
	 * @param ba
	 * @param user
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static ArrayList<BAField> getFields(BusinessArea ba, User user) 
			throws TbitsExceptionClient{
		try {
			if(ba == null || user == null)
				return null;
			
			ArrayList<Field> fields = Field.lookupBySystemId(ba.getSystemId());
			
			if(fields == null)
				return null;
			
			ArrayList<BAField> response = fromFields(ba, fields, user);
			
			return response;
		} catch (Exception e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}

	public static BusinessAreaClient getBAClientBySysPrefix(String sys_prefix) throws TbitsExceptionClient 
	{
		try
		{
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sys_prefix);
			BusinessAreaClient baClient = new BusinessAreaClient();
			GWTServiceHelper.getBAClientByBA(ba, baClient);
			return baClient;
		}
		catch( Exception e)
		{
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}

	public static List<BusinessAreaClient> getBAList(String userLogin)throws TbitsExceptionClient 
	{
		try
		{
			User u = User.lookupAllByUserLogin(userLogin);
			ArrayList<BusinessArea> baList = BusinessArea.getBusinessAreasByUserId(u.getUserId());
			ArrayList<BusinessAreaClient> baClientList = new ArrayList<BusinessAreaClient>();
			for (BusinessArea ba : baList) {
				if (!ba.getIsActive())
					continue;
				BusinessAreaClient baClient = new BusinessAreaClient();
				GWTServiceHelper.getBAClientByBA(ba, baClient);
				baClientList.add(baClient);
			}
			return baClientList;
		}
		catch(Exception e)
		{
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}

	public static void getBAClientByBA(BusinessArea ba, BusinessAreaClient baClient) throws TbitsExceptionClient 
	{
		try
		{
		GWTServiceHelper.setValuesInDomainObject(ba, baClient);

		SysConfig sysConf = ba.getSysConfigObject();
		SysConfigClient sysConfClient = new SysConfigClient();
		GWTServiceHelper.setValuesInDomainObject(sysConf, sysConfClient);
		baClient.setSysConfigObject(sysConfClient);

		ArrayList<CustomLink> links = ba.getSysConfigObject().getCustomLinks();
		ArrayList<CustomLinkClient> linkClients = new ArrayList<CustomLinkClient>();
		CustomLinkClient linkClient;
		for (CustomLink link : links) {
			linkClient = new CustomLinkClient();
			GWTServiceHelper.setValuesInDomainObject(link, linkClient);
			linkClients.add(linkClient);
		}
		sysConfClient.setCustomLinks(linkClients);
		}
		catch(Exception e)
		{
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	
	}

	/**
	 * Get {@link TbitsTreeRequestData} for a given request_id
	 * @param user
	 * @param ba
	 * @param requestId
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static TbitsTreeRequestData getDataByRequestId(User user, BusinessArea ba, int requestId) throws TbitsExceptionClient{
		try {
			Request request = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), requestId);
			if(request != null)
			{
				// check basic view permission here
				//Nitiraj : just added to let it work with current system but its not the currect place.
				// it should have been checked at the API level itself.
				
				// Retrieve the permission of the user
				Hashtable<String, Integer> perms = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(ba.getSystemId(), request.getRequestId(), user.getUserId());
				if(perms == null)
					throw new TbitsExceptionClient("Unable to retrieve user permissions"); // can not continue without permissions

				// BA view permission
				Integer baperm = perms.get(Field.BUSINESS_AREA);
				if( null == baperm ) baperm = 0 ; 
				if( (baperm & Permission.VIEW) == 0 )
					throw new TbitsExceptionClient("You( " + user.getDisplayName() + ") do not have sufficient permission to view the Business Area (" + ba.getSystemPrefix() + ")");
				
				Integer reqperm = perms.get(Field.REQUEST);
				if( null == reqperm ) reqperm = 0 ;				
				if( (reqperm & Permission.VIEW) == 0)
					throw new TbitsExceptionClient("You( " + user.getDisplayName() + ") do not have sufficient permission to view the Request " + request.getRequestId() + " of Business Area (" + ba.getSystemPrefix() + ")");		
				
				Boolean privateValue = request.getIsPrivate() ;
				if( null == privateValue )
					privateValue = false;
				Integer priperm = perms.get(Field.IS_PRIVATE);
				if( null == priperm ) priperm = 0 ;
				if( (privateValue) && ((priperm & Permission.VIEW) == 0))
					throw new TbitsExceptionClient("You( " + user.getDisplayName() + ") do not have sufficient permission to view the Request " + request.getRequestId() + " of Business Area (" + ba.getSystemPrefix() + ") as the request is marked private.");
				
				TbitsTreeRequestData model = createRequestData(user, ba, request, perms);
				// Add the tags to a field called request_tags
				setTags(model, user.getUserId(), ba.getSystemId(), requestId);
				return model;
			}
		
		return null;
		}
		catch ( TbitsExceptionClient tec )
		{
			throw tec;
		}
		catch (Exception e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	/**
	 * Called by the getDataByRequestId method to fetch the tags related to the request.
	 * @param model
	 * @param user_id
	 * @param system_id
	 * @param request_id
	 * @throws TbitsExceptionClient 
	 */
	private static void setTags(TbitsTreeRequestData model, int user_id, int system_id, int request_id) throws TbitsExceptionClient 
	{
		try
		{
			String private_request_tags = "";
			String public_request_tags = "";
			String request_tags = "";
			
			Connection conn = null;
			try {
				conn = DataSourcePool.getConnection();
				conn.setAutoCommit(false);
				
				// Fetch the public tags
				String query = 	"select td.name as name from tags_definitions td join tags_requests tr " +
								"on td.tag_id=tr.tag_id and td.user_id=? and tr.sys_id=? and tr.request_id=?";
				PreparedStatement statement = conn.prepareStatement(query);
				statement.setInt(1, -1);
				statement.setInt(2, system_id);
				statement.setInt(3, request_id);
				ResultSet rs = statement.executeQuery();
				if(rs != null){
					if(rs.next()){
						public_request_tags = rs.getString("name");
						while(rs.next()){
							public_request_tags += (", " + rs.getString("name"));
						}
					}
				}
				rs.close();
				statement.close();
				
				// Fetch the private tags
				query = "select td.name as name from tags_definitions td join tags_requests tr " +
						"on td.tag_id=tr.tag_id and td.user_id=? and tr.sys_id=? and tr.request_id=?";
				statement = conn.prepareStatement(query);
				statement.setInt(1, user_id);
				statement.setInt(2, system_id);
				statement.setInt(3, request_id);
				rs = statement.executeQuery();
				if(rs != null){
					if(rs.next()){
						private_request_tags = rs.getString("name");
						while(rs.next()){
							private_request_tags += (", " + rs.getString("name"));
						}
					}
				}
				rs.close();
				statement.close();
				
				// Append the public and private tags and put them in request_tags
				if(!public_request_tags.equals(""))
					request_tags = "<span style='color:#f00; font-weight:bold;'> " + public_request_tags + " </span>";
				if(!request_tags.equals("")){
					if(!private_request_tags.equals(""))
						request_tags += "<span style='color:#00f;'> " + ", " + private_request_tags + "</span>";
				}
				else
					request_tags = "<span style='color:#00f;'> " + private_request_tags + "</span>";
				
				conn.commit();
			} 
			catch (SQLException e) {
				e.printStackTrace();
				LOG.info("Error fetching tags.");
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
			model.set("request_tags", request_tags);
			model.set("public_request_tags", public_request_tags);
			model.set("private_request_tags", private_request_tags);
		}
		catch(Exception e)
		{
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}

	/**
	 * Create {@link TbitsTreeRequestData} from {@link Request}
	 * @param user
	 * @param ba
	 * @param request
	 * @return
	 * @throws DatabaseException
	 * @throws TbitsExceptionClient
	 */
	public static TbitsTreeRequestData createRequestData(User user, BusinessArea ba, Request request, Hashtable<String, Integer> perms) throws TbitsExceptionClient{
		try
		{
			Hashtable<String, String> map = new Hashtable<String, String>();
			List<Field> fields = Field.lookupBySystemId(ba.getSystemId());
			for(Field field : fields){
				String fName = field.getName();
				String value = request.get(fName);
				if(value != null){
					//TODO: incorrect place to htmlify it. It should be done while displaying in the UI
					if(field.getDataTypeId() == DataType.TEXT){
						Object textDataType = request.getObject(field);
						if(textDataType != null && ((TextDataType)textDataType).getContentType() == TBitsConstants.CONTENT_TYPE_TEXT){
							value = ClientUtils.htmlify(value);
						}
					}
					map.put(fName, value);
				}
			}
			TbitsTreeRequestData model = createRequestData(user, ba, request, map, perms);
			return model;
		}
		catch(Exception e)
		{
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	/**
	 * creates {@link TbitsTreeRequestData} from paramTable
	 * @param user
	 * @param ba
	 * @param requestId
	 * @param paramTable
	 * @return
	 * @throws DatabaseException
	 * @throws TbitsExceptionClient
	 */
	
	/*
	 */
	public static TbitsTreeRequestData createRequestData(User user, BusinessArea ba, int requestId, Hashtable<String, String> paramTable, Hashtable<String, Integer> perms) throws TbitsExceptionClient{
		try
		{
			TbitsTreeRequestData model = new TbitsTreeRequestData(ba.getSystemId(), requestId);
			
			// Fill the permissions in the model
			HashMap<String, Integer> permissions = new HashMap<String, Integer>();
			for(String fName : perms.keySet()){
				int perm = perms.get(fName);
				permissions.put(fName, perm);
			}
			
			model.setPerms(permissions);
			
			for(String fieldName : paramTable.keySet()){
				Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), fieldName);
				String fieldValue = paramTable.get(field.getName());
				if(fieldValue != null){
					POJO tempPrimitiveObject = null;
					
					/*
					 * Fill appropriate POJO value in the model
					 */
					//TODO: The values should be GMT. No parsing should be required here. While presentation, the value should be formatted with local time
					if(isDateField(field)){
						try{
							String formatString = WebUtil.API_DATE_FORMAT;
							DateFormat df = new SimpleDateFormat(formatString);
							df.setTimeZone(TimeZone.getDefault());
		                	Date d = null;
							try {
								d = df.parse(fieldValue);
							} catch (ParseException e) {
								formatString = WebUtil.API_DATE_ONLY_FORMAT;
								df = new SimpleDateFormat(formatString);
			                	df.setTimeZone(TimeZone.getDefault());
			                	try {
			                		d = df.parse(fieldValue);
			                	}catch (ParseException ex) {
			                		LOG.error("Could not parse date : ", ex);
									continue;
			                	}
							}
							tempPrimitiveObject = new POJODate(d);
						}catch(Exception e){
							model.set(field.getName(), tempPrimitiveObject);
						}
					}else if(isBitField(field)){
						try{
							tempPrimitiveObject = new POJOBoolean(fieldValue.equals("false")?false:true);
						}catch(NullPointerException ne){
							
						}
					}else if(isAttachmentField(field)){
						try{
							Collection<AttachmentInfo> attachments = AttachmentInfo.fromJson(fieldValue);
							List<FileClient> resp = new ArrayList<FileClient>();
							for(AttachmentInfo attachment : attachments){
								FileClient file = new FileClient();
								file.setSysPrefix(ba.getSystemPrefix());
								file.setFieldId(field.getFieldId());
								file.setFileName(attachment.name);
								file.setRepoFileId(attachment.repoFileId);
								file.setRequestFileId(attachment.requestFileId);
								file.setRequestId(requestId);
								file.setSize(attachment.size);
								resp.add(file);
							}
							tempPrimitiveObject = new POJOAttachment(resp);
						}catch(NullPointerException ne){
							tempPrimitiveObject = new POJOAttachment(new ArrayList<FileClient>());
						}
					}else if(isIntField(field)){
						try{
							int n = Integer.parseInt(fieldValue);
							tempPrimitiveObject = new POJOInt(n);
						}catch(Exception e){
							
						}
					}else{
						tempPrimitiveObject = new POJOString(fieldValue);
					}
					if(tempPrimitiveObject != null)
						model.set(field.getName(), tempPrimitiveObject);
				}
			}
			return model;
		}
		catch(Exception e)
		{
			LOG.error(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	public static TbitsTreeRequestData createRequestData(User user, BusinessArea ba, Request request, Hashtable<String, String> paramTable, Hashtable<String, Integer> perms) throws DatabaseException, TbitsExceptionClient{
		TbitsTreeRequestData model = createRequestData(user, ba, request.getRequestId(), paramTable, perms);
		model.setMaxActionId(request.getMaxActionId());
		return model;
	}
	
	/**
	 * get all Active users
	 * @return
	 * @throws DatabaseException 
	 */
	public static UserListLoadResult getAllActiveUsers() throws DatabaseException{
		return getActiveUsers("", null, -1);
	}
	
	/**
	 * Get active users mtching a query
	 * 
	 * Filters acc to the following field property of user type field : 
	 * 
	 * 	key : user_filter
		value :
		Pattern :
		        whitespace or "" means all users
		       	+-UserTypePattern; +-RolePattern; +-UserPattern;
		UserTypePattern:
				usertype:user_type_id,...; UserTypePattern
		RolePattern:
		        role:role_id,...; RolePattern
		UserPattern:
		        user:user_id,... ; UserPattern

	 * @param query
	 * @return
	 * @throws DatabaseException 
	 */
	public static UserListLoadResult getActiveUsers(String query, BAField baField) throws DatabaseException{
		return getActiveUsers(query, baField, 50);
	}
	
	public static UserListLoadResult getActiveUsers(String query, BAField baField, int limit) throws DatabaseException{
		Date start = new Date();
		// To make it case- insensitive
		query = query.toLowerCase();
		
		/*
		 * Decision variables in order of precedence. 
		 */
		boolean all = true;				// If true no users are filtered out
		int internal = 0;				// If > 0 all internal users are included in results , if < 0 all internal users are excluded from results
		int external = 0;				// If > 0 all external users are included in results , if < 0 all extrnal users are excluded from results
		int mailingList = 0;			// If > 0 all mailing list users are included in results , if < 0 all mailing list users are excluded from results
		
		List<Integer> inclusionList = new ArrayList<Integer>();  // all elements are included in result
		List<Integer> exclusionList = new ArrayList<Integer>();  //	all elements are excluded in result
		
		
		// In god I trust, for anything else I use a try/catch
		try{
			List<FieldProperties> fieldProperties = null;
			if(baField != null){
				try {
					fieldProperties = FieldProperties.lookupBySystemIdAndFieldIdAndProperty(baField.getSystemId(), baField.getFieldId(), USER_FILTER);
				} catch (DatabaseException e) {
					LOG.info(TBitsLogger.getStackTrace(e));
					LOG.info("Failed to load properties for field : " + baField.getSystemId() + "_" + baField.getFieldId() + " \nProceeding with properties");
				}
			}
			
			if(fieldProperties != null){
				for(FieldProperties fieldProperty : fieldProperties){
					String pattern = fieldProperty.getValue();
					
					if(pattern != null && !pattern.trim().equals("")){	// If empty or blank then assume all
						String[] tokens = pattern.split(";");			// ; is used as the boundary character
						for(String token : tokens){
							boolean isInclude = true;
							if(token.trim().matches("^\\-"))	// If starts with - then exclusion filter
								isInclude = false;
							if(token.trim().matches("(^\\+|^\\-)?usertype:(&)?[0-9]+(,(&)?[0-9]+)*")){	// user_type
								try{
									token = token.replaceFirst("(^\\+|^\\-)usertype:|^usertype:", "");	// only ids would be left
									String[] idTokens = token.split(",");
								     
									for(String idToken : idTokens){
									if(idToken.trim().matches("&7")||(idToken.trim().matches("&9")))
									{
										isInclude= false;
										LOG.info("cannot derefernce internal or external users");
										
									}
									if(idToken.trim().matches("&8"))
											{
										String withoutAmp = idToken.replaceFirst("&","");
									
											int userTypeId = Integer.parseInt(withoutAmp.trim());
											if(userTypeId == UserType.INTERNAL_MAILINGLIST){
												
											ArrayList<User> allUser= lookupByUserTypeId(UserType.INTERNAL_MAILINGLIST);
												if(isInclude){

													
													for(User user:allUser){
													List<User> mailListUsers = MailListUser.getMemberUsers(user.getUserId());
													
													for(User mailListUser : mailListUsers){
														if(isInclude)
															inclusionList.add(mailListUser.getUserId());
														else
															exclusionList.add(mailListUser.getUserId());
													}
													}
													all= false;
												}
											
											}
										
											}
										
										int userTypeId = Integer.parseInt(idToken.trim());
										if (userTypeId == UserType.INTERNAL_USER) {
											if(isInclude)
												internal = 1;
											else
												internal = -1;
											all = false;
										}else if(userTypeId == UserType.EXTERNAL_USER){
											if(isInclude)
												external = 1;
											else
												external = -1;
											all = false;
										}else if(userTypeId == UserType.INTERNAL_MAILINGLIST){
											if(isInclude)
												mailingList = 1;
											else
												mailingList = -1;
											all = false;
										}
									}
								}catch(Exception e){
									LOG.info(TBitsLogger.getStackTrace(e));
								}
							}else if(token.trim().matches("(^\\+|^\\-)?role:[&]?[A-Za-z]+(,[&]?[A-Za-z]+)*")){	// specifies role_id
								try{
									token = token.replaceFirst("(^\\+|^\\-)role:|^role:", "");	// only rolename  would be left
									String[] idTokens = token.split(",");
									for(String idToken : idTokens){
										if(idToken.trim().matches("&[A-Za-z]+"))
										{
											String withoutAmp = idToken.replaceFirst("&","");
											Role role = Role.lookupBySystemIdAndRoleName(baField.getSystemId(), withoutAmp);
											int roleId = role.getRoleId();
											ArrayList<RoleUser> roleUsers = RoleUser.lookupBySystemIdAndRoleId(baField.getSystemId(), roleId);
									
											for(RoleUser roleUser : roleUsers){
												if(isInclude){	
													
												User usr= User.lookupByUserId(roleUser.getUserId());
											
											if(usr.getUserTypeId()==UserType.INTERNAL_MAILINGLIST)
												{
												List<User> mailListUsers = MailListUser.getMemberUsers(usr.getUserId());
												for(User mailListUser : mailListUsers){
													if(isInclude)
														inclusionList.add(mailListUser.getUserId());
													else
														exclusionList.add(mailListUser.getUserId());
												}
												} else if (usr.getUserTypeId()==UserType.INTERNAL_USER||(usr.getUserTypeId()==UserType.EXTERNAL_USER)){
													
													
													if(isInclude)
													inclusionList.add(usr.getUserId());
													
													else
														exclusionList.add(usr.getUserId());
													
												}
												
												}
												}
											}
											
								
							
										else if(idToken.trim().matches("[A-Za-z]+")){
									Role role = Role.lookupBySystemIdAndRoleName(baField.getSystemId(), idToken);
										
											int roleId = role.getRoleId();
										ArrayList<RoleUser> roleUsers = RoleUser.lookupBySystemIdAndRoleId(baField.getSystemId(), roleId);
										for(RoleUser roleUser : roleUsers){
											if(isInclude)
												inclusionList.add(roleUser.getUserId());
											else
												exclusionList.add(roleUser.getUserId());
										}
									}
									}
								}catch(Exception e){
									LOG.info(TBitsLogger.getStackTrace(e));
								}
							}else if(token.trim().matches("(\\+|\\-)?user:[&]?[[a-zA-Z0-9_\\.@]+]+(,[&]?[[a-zA-Z0-9_\\.@]+]+)*")){	// specifies user_id
								try{
									token = token.replaceFirst("^(\\+|\\-)user:|^user:", "");	// only ids would be left
									String[] idTokens = token.split(",");
									for(String idToken : idTokens){
										if(idToken.trim().matches("&([a-zA-Z0-9_\\.@]+)+"))
										{
											String withoutAmp = idToken.replaceFirst("&","");
											User user= User.lookupByUserLogin(withoutAmp);
											int userId = user.getUserId();
										if(user.getUserTypeId() == UserType.INTERNAL_MAILINGLIST){	// If user is a mailing list.. dereference it
											List<User> mailListUsers = MailListUser.getMemberUsers(user.getUserId());
											
											for(User mailListUser : mailListUsers){
												if(isInclude)
													inclusionList.add(mailListUser.getUserId());
												else
													exclusionList.add(mailListUser.getUserId());
											}
										}
										}
										else if(idToken.trim().matches("([a-zA-Z0-9_\\.@]+)+")){
											User serI= User.lookupByUserLogin(idToken);
											if(isInclude)
												inclusionList.add(serI.getUserId());
											else
												exclusionList.add(serI.getUserId());
										}	
										} 
									
								}catch(Exception e){
									LOG.info(TBitsLogger.getStackTrace(e));
								}
							}
							
							if(inclusionList.size() > 0 || exclusionList.size() > 0)
								all = false;
						}
					}
					
					break; // I expect only one entry of this property per field. All except first are ignored
				}
			}
		}catch (Exception e) {
			LOG.info(TBitsLogger.getStackTrace(e));
		}
		
		List<Integer> userIdList = new ArrayList<Integer>();
		List<UserClient> response = new ArrayList<UserClient>();
		
		//int limit = 50;
		
		UserRadixTree tree = UserRadixTree.getInstance();
		List<Integer> userIds = tree.searchPrefix(query, 100000); // search on query
		for(int userId : userIds){
			
			if(userIdList.contains(userId))	// already included
				continue;
			
			User user = User.lookupByUserId(userId);
			
			if(!all){ 
				if(exclusionList.contains(user.getUserId()))	// exclude from results if in exclusion list
					continue;
				
				if(!inclusionList.contains(user.getUserId())){	// include in results if in inclusion list
					if(user.getUserTypeId() == UserType.INTERNAL_USER && internal < 0){	// exclude internal if excluded
						continue;
					}
					
					if(user.getUserTypeId() == UserType.EXTERNAL_USER && external < 0){	// exclude external if excluded
						continue;
					}
					
					if(user.getUserTypeId() == UserType.INTERNAL_MAILINGLIST && mailingList < 0){ // exclude mailing list if excluded
						continue;
					}
					
					if(internal > 0){	// internal == 1;
						if(external == 0 && user.getUserTypeId() == UserType.EXTERNAL_USER){
							continue;
						}
						
						if(mailingList == 0 && user.getUserTypeId() == UserType.INTERNAL_MAILINGLIST){
							continue;
						}
					}else{				// internal == 0
						if(external > 0){
							if(user.getUserTypeId() == UserType.INTERNAL_USER)
								continue;
							
							if(mailingList == 0 && user.getUserTypeId() == UserType.INTERNAL_MAILINGLIST){
								continue;
							}
						}else{			// intenal == 0 and external == 0
							if(mailingList > 0){
								if(user.getUserTypeId() == UserType.INTERNAL_USER)
									continue;
								
								if(external == 0 && user.getUserTypeId() == UserType.EXTERNAL_USER)
									continue;
							}
						}
					}
					
					if(internal == 0 && external == 0 && mailingList == 0)
						continue;
				}
			}
			
			if(user != null){
				UserClient userClient = fromUser(user);
				if(userClient != null){
					userIdList.add(user.getUserId());
					response.add(userClient);
				}
			}
			if(limit != -1)
			{
				if(response.size() >= limit)
					break;
			}
		}
		
		Date end = new Date();
		
		LOG.info("Time taken to filter users : " + (end.getTime() - start.getTime()) + " milliseconds");
		
		return new UserListLoadResult(response);
	}
	
	public static UserClient fromUser(User user) throws DatabaseException{
		if(user != null){
			UserClient userClient = new UserClient();
			setValuesInDomainObject(user, userClient);
			
			boolean isSuperUser = RoleUser.isSuperUser(user.getUserId());
			userClient.setIsSuperUser(isSuperUser);
			
			WebConfig wc = user.getWebConfigObject();
			if((wc != null))				
			{
				userClient.setDefaultBA(wc.getSystemPrefix());
				if(wc.getWebDateFormat() != null){
					try{
						userClient.setWebDateFormat(user.getWebConfigObject().getWebDateFormat());
					}
					catch(Throwable exp){
						LOG.error(exp);
					}
				}
			}
			return userClient;
		}
		return null;
	}
	
	/**
	 * Calls add request
	 * @param requestObj
	 * @param user
	 * @param ba
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static TbitsTreeRequestData addRequest(TbitsTreeRequestData requestObj, User user, BusinessArea ba) throws TbitsExceptionClient{
		Hashtable<String, String> aParamTable = prepareParamTableforAddandUpdate(requestObj, user, ba);
		AddRequest addRequest = new AddRequest();
		addRequest.setSource(AddRequest.SOURCE_WEB);
		try {
			Request request = addRequest.addRequest(aParamTable);
			// Retrieve the permission of the user
			Hashtable<String, Integer> perms = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(ba.getSystemId(), request.getRequestId(), user.getUserId());
			if(perms == null)
				throw new TbitsExceptionClient("Unable to retrieve user permissions"); // can not continue without permissions

			TbitsTreeRequestData model = createRequestData(user, ba, request, perms);
			return model;
		} 
		catch(Throwable t)
		{
			t.printStackTrace();
			LOG.error(TBitsLogger.getStackTrace(t));
			throw new TbitsExceptionClient(t.getMessage());
		}
	}
	
	/**
	 * Calls add request
	 * @param connection
	 * @param tbitsResMgr
	 * @param requestObj
	 * @param user
	 * @param ba
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static TbitsTreeRequestData addRequest(Connection connection, TBitsResourceManager tbitsResMgr, TbitsTreeRequestData requestObj, User user, BusinessArea ba) throws TbitsExceptionClient{ 
		Hashtable<String, String> aParamTable = prepareParamTableforAddandUpdate(requestObj, user, ba);
		AddRequest addRequest = new AddRequest();
		addRequest.setSource(AddRequest.SOURCE_WEB);
		try {
			Request request = addRequest.addRequest(connection, tbitsResMgr, aParamTable);
			
			// Retrieve the permission of the user
			Hashtable<String, Integer> perms = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(ba.getSystemId(), request.getRequestId(), user.getUserId());
			if(perms == null)
				throw new TbitsExceptionClient("Unable to retrieve user permissions"); // can not continue without permissions

			
			TbitsTreeRequestData model = createRequestData(user, ba, request,perms);
			return model;
		} catch (Exception e){
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} 
		catch(Throwable t)
		{
			LOG.error(TBitsLogger.getStackTrace(t));
			throw new TbitsExceptionClient(t);
		}
	}
	
	/**
	 * Calls update request
	 * @param requestObj
	 * @param user
	 * @param ba
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static TbitsTreeRequestData updateRequest(TbitsTreeRequestData requestObj, User user, BusinessArea ba) throws TbitsExceptionClient{
		Hashtable<String, String> aParamTable = prepareParamTableforAddandUpdate(requestObj, user, ba);
		
    	String dateString = formatDate(new Date(), TBitsConstants.API_DATE_FORMAT);
		
		
		aParamTable.put(Field.LASTUPDATED_DATE,dateString);
		String maxActionIdString = aParamTable.get(Field.MAX_ACTION_ID);
		if(maxActionIdString != null && !maxActionIdString.trim().equals("") && !maxActionIdString.trim().equals("0"))
			aParamTable.put(Field.REPLIED_TO_ACTION, maxActionIdString);

		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.setSource(UpdateRequest.SOURCE_WEB);
		try {
			Request request = updateRequest.updateRequest(aParamTable);
			// Retrieve the permission of the user
			Hashtable<String, Integer> perms = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(ba.getSystemId(), request.getRequestId(), user.getUserId());
			if(perms == null)
				throw new TbitsExceptionClient("Unable to retrieve user permissions"); // can not continue without permissions

			TbitsTreeRequestData model = createRequestData(user, ba, request,perms);
			return model;
		} catch (Throwable e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	/**
	 * Calls update request
	 * @param connection
	 * @param tbitsResMgr
	 * @param requestObj
	 * @param user
	 * @param ba
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static TbitsTreeRequestData updateRequest(Connection connection, TBitsResourceManager tbitsResMgr, TbitsTreeRequestData requestObj, User user, BusinessArea ba) throws TbitsExceptionClient{
		Hashtable<String, String> aParamTable = prepareParamTableforAddandUpdate(requestObj, user, ba);
		
		String maxActionIdString = aParamTable.get(Field.MAX_ACTION_ID);
		if( !(maxActionIdString == null || maxActionIdString.trim().equals("") || maxActionIdString.trim().equals("0")))
			aParamTable.put(Field.REPLIED_TO_ACTION, maxActionIdString);
//			throw new TbitsExceptionClient(Field.MAX_ACTION_ID + " not provided");
		
		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.setSource(UpdateRequest.SOURCE_WEB);
		try {
			Request request = updateRequest.updateRequest(connection, tbitsResMgr, aParamTable);
			
			// Retrieve the permission of the user
			Hashtable<String, Integer> perms = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(ba.getSystemId(), request.getRequestId(), user.getUserId());
			if(perms == null)
				throw new TbitsExceptionClient("Unable to retrieve user permissions"); // can not continue without permissions

			TbitsTreeRequestData model = createRequestData(user, ba, request, perms);
			return model;
		} catch (Throwable e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	/**
	 * Prepares paramTable from {@link TbitsTreeRequestData} for add request and updte request
	 * @param requestObj
	 * @param user
	 * @param ba
	 * @return
	 */
	public static Hashtable<String, String> prepareParamTableforAddandUpdate(TbitsTreeRequestData requestObj, User user, BusinessArea ba){
		Hashtable<String, String> aParamTable = new Hashtable<String, String>();
		
		for(String f: requestObj.getPropertyNames()){
			POJO obj = requestObj.getAsPOJO(f);
			if(obj instanceof POJODate){
				try{
			    	String dateString = formatDate((Date)obj.getValue(), TBitsConstants.API_DATE_FORMAT);
			    	aParamTable.put(f,dateString);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else if(obj instanceof POJOAttachment){
				try{
					List<FileClient> attachments = ((POJOAttachment)obj).getValue();
					List<AttachmentInfo> atts = new ArrayList<AttachmentInfo>();
					for(AttachmentInfoClient attachment : attachments){
						AttachmentInfo att = new AttachmentInfo();
						att.name = attachment.getFileName();
						att.repoFileId = attachment.getRepoFileId();
						att.requestFileId = attachment.getRequestFileId();
						att.size = attachment.getSize();
						atts.add(att);
					}
					String attsString = AttachmentInfo.toJson(atts);
					aParamTable.put(f, attsString);
				}catch(Exception e){
					
				}
			}else{
				POJO pojo = requestObj.getAsPOJO(f);
				if(pojo != null)
					aParamTable.put(f, pojo.toString());
			}
		}
		
		aParamTable.put(Field.USER,user.getUserLogin());
		aParamTable.put(Field.BUSINESS_AREA, ba.getSystemPrefix());
		
		return aParamTable;
	}
	public static ArrayList<User> lookupByUserTypeId(int aUserTypeId) throws DatabaseException {
        User user = null;

        // Look in the mapper first.
      /*  Integer key = new Integer(aUserTypeId);

        if (ourTypeUserMap != null) {
            user = ourTypeUserMap.get(key);

            if (user != null) {
                if (user.getIsActive() == true) {
                    return user;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }*/

        // Else go to the database.
        ArrayList<User> allUsersList=new ArrayList<User>();
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            //CallableStatement cs = connection.prepareCall("stp_user_lookupByUserId ?");

            //cs.setInt(1, aUserId);
            PreparedStatement ps = connection.prepareStatement("select * from users where user_type_id = " 
            		+ aUserTypeId);

            ResultSet rs ;
            rs = ps.executeQuery();

            if (rs != null) {
                while(rs.next() != false) {
                    user = User.createFromResultSet(rs);
                    allUsersList.add(user);
                }
                if(rs !=null)
                rs.close();
            }
            if(ps !=null)
           ps.close();
           
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the user.").append("\naUserTypeId ").append(aUserTypeId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.info("Exception while closing the connection");
            }
        }

        return allUsersList;
    }

	/**
	 * All captions.
	 * @return
	 */
	public static HashMap<Integer,HashMap<String, String>> getAllBACaptions(){
		return CaptionsProps.getInstance().getAllBAcaptionsMap();
	}
	
	/**
	 * Get display groups for a BA.
	 * @param ba
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static ArrayList<DisplayGroupClient> getDisplayGroups(BusinessArea ba) throws TbitsExceptionClient {
		try {
			ArrayList<DisplayGroup> displayGroups = DisplayGroup.lookupIncludingDefaultForSystemId(ba.getSystemId());
			ArrayList<DisplayGroupClient> response = new ArrayList<DisplayGroupClient>();
			for(DisplayGroup displayGroup:displayGroups){
				DisplayGroupClient displayGroupClient = new DisplayGroupClient();
				setValuesInDomainObject(displayGroup, displayGroupClient);

				response.add(displayGroupClient);
			}
			return response;
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
	}
	
	/**
	 * get type dependencies for a type field
	 * @param field
	 * @return
	 * @throws TbitsExceptionClient
	 */
	public static List<TypeDependency> getTypeDependenciesForField(Field field) throws TbitsExceptionClient{
		List<TypeDependency> dependencies = new ArrayList<TypeDependency>();
		
		Connection conn = null;
		try{
			conn = DataSourcePool.getConnection();
			
			String sql = "select * from type_dependency where sys_id = ? and src_field_id = ?" ;
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, field.getSystemId());
			statement.setInt(2, field.getFieldId());
			
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				int sysId = rs.getInt(TypeDependency.SYS_ID);
				int srcFieldId = rs.getInt(TypeDependency.SRC_FIELD_ID);
				int srcTypeId = rs.getInt(TypeDependency.SRC_TYPE_ID);
				int destFieldId = rs.getInt(TypeDependency.DEST_FIELD_ID);
				int destTypeId = rs.getInt(TypeDependency.DEST_TYPE_ID);
				
				TypeDependency td = new TypeDependency();
				td.setSysId(sysId);
				td.setSrcFieldId(srcFieldId);
				td.setSrcTypeId(srcTypeId);
				td.setDestFieldId(destFieldId);
				td.setDestTypeId(destTypeId);
				
				dependencies.add(td);
			}
		}catch(Exception e){
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}finally{
			if(conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.info(TBitsLogger.getStackTrace(e));
					throw new TbitsExceptionClient(e);
				}
		}
		
		return dependencies;
	}
	
	public static ArrayList<BAField> fromFields(BusinessArea ba, List<Field> fields, User user) throws DatabaseException, TbitsExceptionClient{
		if(fields == null || ba == null || user == null)
			return null;
		
		Hashtable<String, Integer> myPerms = RolePermission.getPermissionsBySystemIdAndUserId(ba.getSystemId(), user.getUserId());
		if(myPerms == null)
			return null;
		
		ArrayList<BAField> baFields = new ArrayList<BAField>();
		for(Field field : fields){
			BAField baField = fromField(ba, field, user, myPerms);
			if(baField != null)
				baFields.add(baField);
		}
		return baFields;
	}
	
	public static BAField fromField(BusinessArea ba, Field field, User user) throws DatabaseException, TbitsExceptionClient{
		return fromField(ba, field, user, null);
	}
	
	public static BAField fromField(BusinessArea ba, Field field, User user, Hashtable<String, Integer> myPerms) throws DatabaseException, TbitsExceptionClient{
		if(field != null){
			if(isTypeField(field)){
				BAFieldCombo baField = fromField(field, user, myPerms, BAFieldCombo.class);
				if(baField != null){
					baField.setDependencies(getTypeDependenciesForField(field));
					
					List<Type> types = Type.lookupAllBySystemIdAndFieldName(ba.getSystemId(), field.getName());
					List<TypeClient> typeClients = new ArrayList<TypeClient>();
					for(Type t:types){
						TypeClient typeClient = new TypeClient();
						setValuesInDomainObject(t, typeClient);
						typeClients.add(typeClient);
					}
					baField.setTypes(typeClients);
				}
				return baField;
			}else if(isBitField(field)){
				BAFieldCheckBox baField = fromField(field, user, myPerms, BAFieldCheckBox.class);
				return baField;
			}else if(isDateField(field)){
				BAFieldDate baField = fromField(field, user, myPerms, BAFieldDate.class);
				if(baField != null){
					String formatStr = user.getWebConfigObject().getListDateFormat();
					baField.setDateFormat(formatStr);
				}
				return baField;
			}else if(isTextField(field)){
				BAFieldTextArea baField = fromField(field, user, myPerms, BAFieldTextArea.class);
				return baField;
			}else if(isUserTypeField(field)){
				BAFieldMultiValue baField = fromField(field, user, myPerms, BAFieldMultiValue.class);
				return baField;
			}else if(isAttachmentField(field)){
				BAFieldAttachment baField = fromField(field, user, myPerms, BAFieldAttachment.class);
				return baField;
			}else if(isIntField(field)){
				BAFieldInt baField = fromField(field, user, myPerms, BAFieldInt.class);
				return baField;
			}else{
				BAFieldString baField = fromField(field, user, myPerms, BAFieldString.class);
				return baField;
			}
		}
		
		return null;
	}
	
	/**
	 * converts {@link Field} to {@link BAField}
	 * @param <T> extends {@link BAField}
	 * @param field
	 * @param user
	 * @param myPerms
	 * @param clazz {@link BAField} subclass
	 * @return
	 * @throws DatabaseException
	 * @throws TbitsExceptionClient
	 */
	public static <T extends BAField> T fromField(Field field, User user, Hashtable<String, Integer> myPerms, Class<T> clazz) throws DatabaseException, TbitsExceptionClient{
		if(field == null)
			return null;
		
		T baField = null;
		try {
			Constructor<T> constructor = clazz.getConstructor();
			baField = constructor.newInstance();
			setValuesInDomainObject(field, baField);
			baField.setDisplayGroup(field.getDisplayGroup());
			baField.setDisplayOrder(field.getDisplayOrder());
			baField.setIsExtended(field.getIsExtended());
			
			if(user != null){
				if(myPerms == null)
					myPerms = RolePermission.getPermissionsBySystemIdAndUserId(field.getSystemId(), user.getUserId());
				if(myPerms == null)
					return null;
				
				int perm = 0;
				try{
					perm = myPerms.get(field.getName());
				}catch(Exception e){
					e.printStackTrace();
				}
				
				baField.setUserPerm(perm);
				
				if((field.getPermission() & Permission.VIEW) != 0)
					baField.setCanView(true);
				else
					baField.setCanView(false);
				
				if((field.getPermission() & Permission.ADD) != 0)
					baField.setCanAdd(true);
				else
					baField.setCanAdd(false);
				
				if((field.getPermission() & Permission.CHANGE) != 0)
					baField.setCanUpdate(true);
				else
					baField.setCanUpdate(false);
				
				if(field.getIsSearchEnabled())
					baField.setCanSearch(true);
				else
					baField.setCanSearch(false);
				
				baField.setSetEnabled(field.getIsSetEnabled());
			}
		
		} catch (IllegalArgumentException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (SecurityException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (InstantiationException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (IllegalAccessException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (InvocationTargetException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (NoSuchMethodException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		return baField;
	}
	
	/**
	 * @param field
	 * @return true when the field is a date type field
	 */
	public static boolean isDateField(Field field){
		int dataTypeId = field.getDataTypeId();
		return dataTypeId == DataType.DATE || dataTypeId == DataType.DATETIME;
	}
	
	/**
	 * @param field
	 * @return true when the field is a Type field
	 */
	public static boolean isTypeField(Field field){
		int dataTypeId = field.getDataTypeId();
		return dataTypeId == DataType.TYPE;
	}
	
	/**
	 * @param field
	 * @return true when the field is a Bit Type field
	 */
	public static boolean isBitField(Field field){
		int dataTypeId = field.getDataTypeId();
		return dataTypeId == DataType.BOOLEAN;
	}
	
	/**
	 * @param field
	 * @return true when the field is a Attachment Type field
	 */
	public static boolean isAttachmentField(Field field){
		int dataTypeId = field.getDataTypeId();
		return dataTypeId == DataType.ATTACHMENTS;
	}
	
	/**
	 * @param field
	 * @return true when the field is a Text Type field
	 */
	public static boolean isTextField(Field field){
		int dataTypeId = field.getDataTypeId();
		return dataTypeId == DataType.TEXT;
	}
	
	/**
	 * @param field
	 * @return true when the field is a Integer field
	 */
	public static boolean isIntField(Field field){
		int dataTypeId = field.getDataTypeId();
		return dataTypeId == DataType.INT;
	}
	
	/**
	 * @param field
	 * @return true when the field is a User Type field
	 */
	public static boolean isUserTypeField(Field field){
		int dataTypeId = field.getDataTypeId();
		return dataTypeId == DataType.USERTYPE;
	}
	
	/**
     * This method checks if the user is an admin in specified BA.
     *
     * @param systemId          BA ID
     * @param userId            User ID
     * @return True if user is an admin
     * @throws DatabaseException
     */
    public static boolean isAdmin(int systemId, int userId) throws DatabaseException {
        Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(systemId, userId);
        Integer                    temp      = null;

        // Check if the user has change permission on Business Area.
        temp = permTable.get(Field.BUSINESS_AREA);

        if ((temp != null) && (temp & Permission.CHANGE) != 0) {
            return true;
        }

        // Check for the admin tag in the permission table.
        temp = permTable.get("__ADMIN__");

        if ((temp != null) && (temp != 0)) {
            return true;
        }

        // Check for the permission admin tag.
        temp = permTable.get("__PERMISSIONADMIN__");

        if ((temp != null) && (temp != 0)) {
            return true;
        }

        return false;
    }
    
    /**
     * This method reads a request parameter that holds a boolean value.
     *
     * @param params          	Parameters.
     * @param aParamName        Name of the request parameter.
     * @param aDefaultValue     Default value.
     * @return Boolean value.
     */
    public static boolean readBooleanParam(HashMap<String, String> params, String aParamName, boolean aDefaultValue) {
        String strValue = params.get(aParamName);

        strValue = (strValue == null)
                   ? ""
                   : strValue.trim().toLowerCase();

        if (strValue.trim().equals("") == true) {
            return aDefaultValue;
        }

        if (strValue.equals("false") || strValue.equals("0")) {
            return false;
        }

        if (strValue.equals("true") || strValue.equals("1")) {
            return true;
        }

        return aDefaultValue;
    }

    /**
     * This method reads a request parameter that holds an integer value.
     *
     * @param params          	Parameters.
     * @param aParamName        Name of the request parameter.
     * @param aDefaultValue     Default value.
     * @return integer value.
     */
    public static int readIntegerParam(HashMap<String, String> params, String aParamName, int aDefaultValue) {
        String strValue = params.get(aParamName);

        if (strValue == null) {
            return aDefaultValue;
        }

        try {
            return Integer.parseInt(strValue);
        } catch (NumberFormatException nfe) {
            LOG.info("Exception while parsing an integer in string: " + strValue + "\n" + TBitsLogger.getStackTrace(nfe));
        }

        return aDefaultValue;
    }

    /**
     * This method reads a request parameter that holds a string value.
     *
     * @param params          	Parameters.
     * @param aParamName        Name of the request parameter.
     * @param aDefaultValue     Default value.
     * @return String value.
     */
    public static String readStringParam(HashMap<String, String> params, String aParamName, String aDefaultValue) {
        String strValue = params.get(aParamName);

        if (strValue == null) {
            return aDefaultValue;
        }

        return strValue.trim();
    }
    
    /**
     * Converts date from one format to other
     * @param dateString
     * @param fromFormat
     * @param toFormat
     * @return
     */
    public static String reFormatDate(String dateString, String fromFormat, String toFormat){
		if(dateString == null || fromFormat == null || toFormat == null)
			return null;
		
    	DateFormat df = new SimpleDateFormat(fromFormat);
    	df.setTimeZone(TimeZone.getDefault());
    	Date d = null;
		try {
			d = df.parse(dateString);
		} catch (ParseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			return null;
		}
		
		return formatDate(d, toFormat);
    }
    
    /**
     * format date into a specified format
     * @param date
     * @param toFormat
     * @return
     */
    public static String formatDate(Date date, String toFormat){
    	if(date == null)
    		return "";
    	DateFormat df = new SimpleDateFormat(toFormat);
		df.setTimeZone(TimeZone.getDefault());
		return df.format(date);
    }
    
    /**
     * Sets values in the target object from source object by matching the names of getters in source and setters in target objects using reflection.
     *  
     * @param sourceObj
     * @param targetObj
     */
    public static void setValuesInDomainObject(Object sourceObj, Object targetObj){
    	Class<?> sourceClazz = sourceObj.getClass();
    	Class<?> targetClazz = targetObj.getClass();
    	Method[] sourceMethods = sourceClazz.getMethods();
    	for(Method method : sourceMethods){
    		if(Modifier.isStatic(method.getModifiers()) || method.getParameterTypes().length > 0)
    			continue;
    			
    		if(method.getName().startsWith("get")){
    			String name = method.getName();
    			String desiredName = name.replaceFirst("get", "set");
    			Method targetMethod;
    			try {
    				Class<?> returnType;
					if (method.getReturnType().getName().endsWith("Timestamp")) {
						returnType = Date.class;
						targetMethod = targetClazz.getMethod(desiredName,returnType);
						Object obj = method.invoke(sourceObj, new Object[0]);
						if(obj != null && targetMethod != null){
							Date date = new Date(((Timestamp)obj).getTime());
							targetMethod.invoke(targetObj, date);
						}
					} else if (method.getReturnType().getName().endsWith("Date")) {
						returnType = Timestamp.class;
						targetMethod = targetClazz.getMethod(desiredName,returnType);
						Object obj = method.invoke(sourceObj, new Object[0]);
						if(obj != null && targetMethod != null){
							Timestamp timeStamp = new Timestamp(((Date) obj).getTime());
							targetMethod.invoke(targetObj, timeStamp);
						}
					} else {
						returnType = method.getReturnType();
						targetMethod = targetClazz.getMethod(desiredName,returnType);
						Object obj = method.invoke(sourceObj, new Object[0]);
						if(obj != null && targetMethod != null){
							targetMethod.invoke(targetObj, obj);
						}
					}
				} catch (SecurityException e) {
//					e.printStackTrace();
				} catch (NoSuchMethodException e) {
//					e.printStackTrace();
				} catch (IllegalArgumentException e) {
//					e.printStackTrace();
				} catch (IllegalAccessException e) {
//					e.printStackTrace();
				} catch (InvocationTargetException e) {
//					e.printStackTrace();
				}
    		}
    	}
    }
    
    public static void main(String[] args) throws DatabaseException 
    {
//	    	BAField baf = new BAField();
//	    	baf.setSystemId(1);
//	    	baf.setFieldId(8);
//    		try {
//    			List<UserClient> result = getActiveUsers("", baf, 10000).getData();
//    			System.out.println(result.size());
//    			for(UserClient uc : result)
//				{
//					System.out.println(uc.getUserId() + " " + uc.getUserLogin());
//				}
//			} catch (DatabaseException e) {
//				e.printStackTrace();
//			}
    	
//    	for(int i = 0; i < 2000; i++){
//    	AddRequest req = new AddRequest();
//    	try {
//    		Hashtable<String, String> paramTable = new Hashtable<String, String>();
//    		paramTable.put(Field.BUSINESS_AREA, "tbits");
//    		paramTable.put(Field.USER, "root");
//			req.addRequest(paramTable);
//		} catch (APIException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	}
    	BAField baf = new BAField();
    	baf.setFieldId(8);
    	baf.setSystemId(84);
    	System.out.println("for field: " + 8);
    	printUsers(baf);
    	
    	baf = new BAField();
     	baf.setFieldId(9);
     	baf.setSystemId(84);
     	System.out.println("for field: " + 9);
    	printUsers(baf);
    	
	}
    /**
     * Method to print the users. This is just for testing purpose
     * @param baf
     */
    private static void printUsers(BAField baf)
    {
    	UserListLoadResult ullr = null;
		try {
			ullr = getActiveUsers("ab", baf);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		if(ullr != null)
		{
	    	for(UserClient uc: ullr.getData())
	    	{
	    		System.out.println("User: " + uc.getUserLogin());
	    	}
		}
		else
			System.out.println("The output was null.");
    }
}
