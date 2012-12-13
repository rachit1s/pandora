package com.tbitsGlobal.admin.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tbitsGlobal.admin.client.modelData.RolePermissionModel;
import com.tbitsGlobal.admin.client.permTool.PermissionInfo;
import com.tbitsGlobal.admin.client.services.AppPropertiesService;
import com.tbitsGlobal.admin.client.services.BAMenuService;
import com.tbitsGlobal.admin.client.services.BusinessAreaService;
import com.tbitsGlobal.admin.client.services.CaptionsService;
import com.tbitsGlobal.admin.client.services.DisplayGroupService;
import com.tbitsGlobal.admin.client.services.EscalationConditionService;
import com.tbitsGlobal.admin.client.services.EscalationHierarchyService;
import com.tbitsGlobal.admin.client.services.EscalationService;
import com.tbitsGlobal.admin.client.services.FieldService;
import com.tbitsGlobal.admin.client.services.HolidayService;
import com.tbitsGlobal.admin.client.services.JobActionService;
import com.tbitsGlobal.admin.client.services.MailingListService;
import com.tbitsGlobal.admin.client.services.ReportService;
import com.tbitsGlobal.admin.client.services.RoleandPermissionService;
import com.tbitsGlobal.admin.client.services.SystemPropertiesService;
import com.tbitsGlobal.admin.client.services.UserService;
import com.tbitsGlobal.admin.client.widgets.pages.UsersPage;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.NotificationRuleClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RulesClient;
import commons.com.tbitsGlobal.utils.client.rules.RuleDef;
import commons.com.tbitsGlobal.utils.client.service.UtilService;

public interface APService extends 
	UtilService, 
	JobActionService,
	AppPropertiesService,
	BusinessAreaService,
	CaptionsService,
	DisplayGroupService,
	EscalationService,
	FieldService,
	MailingListService,
	ReportService,
	RoleandPermissionService,
	UserService,
	SystemPropertiesService,
	HolidayService,
	BAMenuService,
	EscalationHierarchyService,
	EscalationConditionService
	{
	//-------------------------------------------//
	
	public ArrayList<NotificationRuleClient> getNotifcation() throws TbitsExceptionClient;

	ArrayList<RulesClient> getExistingRules();

	ArrayList<RuleDef> getRuleTemplates();

	boolean saveRule(RuleDef ruleDef) throws TbitsExceptionClient;

	String compileRule(RuleDef ruleDef);

	boolean deployRule(RuleDef ruleDef);

	RulesClient getRuleDetails(String value);

	String getRuleCode(String name);

	String getClassDocumentationUrl(String iClass);
	
	/**
	 * Gets the {@link TbitsTreeRequestData} for a particular request_id and user_id
	 * @param sysPrefix
	 * @param userId
	 * @param requestId
	 * @return
	 * @throws TbitsExceptionClient
	 */
	TbitsTreeRequestData getDataByRequestId(String sysPrefix, int userId, int requestId) throws TbitsExceptionClient;
	
	PermissionInfo fetchPermissionInformation(int sysId, int userId, int reqId);

	HashMap<String, List<String>> fetchRolesAffecting(int sysId, RolePermissionModel rpm, List<Integer> relevantRoleIds) throws TbitsExceptionClient;

	UsersPage fetchQueriedUsers(String filter, String value);
	
	List<FieldClient> fetchQueriedFields(String filter, String value,int sysid);

	boolean deleteRule(RuleDef rd) throws TbitsExceptionClient;

	boolean undeployRule(RuleDef rd) throws TbitsExceptionClient;
	
	List<RolePermissionModel> getFieldControls(int sysId) throws TbitsExceptionClient;
	
	List<RolePermissionModel> updateFieldControls(int sysId, List<RolePermissionModel> fieldControls) throws TbitsExceptionClient;
}