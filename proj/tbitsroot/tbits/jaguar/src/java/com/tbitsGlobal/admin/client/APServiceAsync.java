package com.tbitsGlobal.admin.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.modelData.EscalationHierarchiesClient;
import com.tbitsGlobal.admin.client.modelData.RolePermissionModel;
import com.tbitsGlobal.admin.client.permTool.PermissionInfo;
import com.tbitsGlobal.admin.client.services.AppPropertiesServiceAsync;
import com.tbitsGlobal.admin.client.services.BAMenuServiceAsync;
import com.tbitsGlobal.admin.client.services.BusinessAreaServiceAsync;
import com.tbitsGlobal.admin.client.services.CaptionsServiceAsync;
import com.tbitsGlobal.admin.client.services.DisplayGroupServiceAsync;
import com.tbitsGlobal.admin.client.services.EscalationConditionServiceAsync;
import com.tbitsGlobal.admin.client.services.EscalationHierarchyServiceAsync;
import com.tbitsGlobal.admin.client.services.EscalationServiceAsync;
import com.tbitsGlobal.admin.client.services.FieldServiceAsync;
import com.tbitsGlobal.admin.client.services.HolidayServiceAsync;
import com.tbitsGlobal.admin.client.services.JobActionServiceAsync;
import com.tbitsGlobal.admin.client.services.MailingListServiceAsync;
import com.tbitsGlobal.admin.client.services.ReportServiceAsync;
import com.tbitsGlobal.admin.client.services.RoleandPermissionServiceAsync;
import com.tbitsGlobal.admin.client.services.SystemPropertiesServiceAsync;
import com.tbitsGlobal.admin.client.services.UserServiceAsync;
import com.tbitsGlobal.admin.client.widgets.pages.UsersPage;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.NotificationRuleClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RulesClient;
import commons.com.tbitsGlobal.utils.client.rules.RuleDef;
import commons.com.tbitsGlobal.utils.client.service.UtilServiceAsync;

public interface APServiceAsync extends
	UtilServiceAsync, 
	JobActionServiceAsync,
	AppPropertiesServiceAsync,
	BusinessAreaServiceAsync,
	CaptionsServiceAsync,
	DisplayGroupServiceAsync,
	EscalationServiceAsync,
	FieldServiceAsync,
	MailingListServiceAsync,
	ReportServiceAsync,
	RoleandPermissionServiceAsync,
	UserServiceAsync,
	SystemPropertiesServiceAsync,
	HolidayServiceAsync,
	BAMenuServiceAsync,
	EscalationHierarchyServiceAsync,
	EscalationConditionServiceAsync
	{

	void getNotifcation(AsyncCallback<ArrayList<NotificationRuleClient>> callback);
	
	void getExistingRules(AsyncCallback<ArrayList<RulesClient>> asyncCallback);

	void getRuleTemplates(AsyncCallback<ArrayList<RuleDef>> asyncCallback);

	void saveRule(RuleDef ruleDef, AsyncCallback<Boolean> asyncCallback);

	void compileRule(RuleDef ruleDef, AsyncCallback<String> asyncCallback);

	void deployRule(RuleDef ruleDef, AsyncCallback<Boolean> asyncCallback);

	void getRuleDetails(String value, AsyncCallback<RulesClient> asyncCallback);

	void getRuleCode(String name, AsyncCallback<String> asyncCallback);
	
	void getClassDocumentationUrl(String iClass, AsyncCallback<String> asyncCallback);
	
	void getDataByRequestId(String sysPrefix, int userId, int requestId, AsyncCallback<TbitsTreeRequestData> callback);

	void fetchPermissionInformation(int sysId, int userId, int reqId, AsyncCallback<PermissionInfo> asyncCallback);

	void fetchRolesAffecting(int sysId, RolePermissionModel rpm, List<Integer> relevantRoleIds, AsyncCallback<HashMap<String, List<String>>> asyncCallback);

	void fetchQueriedUsers(String filter, String value, AsyncCallback<UsersPage> asyncCallback);
	
	void fetchQueriedFields(String filter, String value, int sysid,AsyncCallback<List<FieldClient>> asyncCallback);

	void deleteRule(RuleDef rd, AsyncCallback<Boolean> asyncCallback);

	void undeployRule(RuleDef rd, AsyncCallback<Boolean> asyncCallback);

	void getFieldControls(int sysId, AsyncCallback<List<RolePermissionModel>> callback);

	void updateFieldControls(int sysId, List<RolePermissionModel> fieldControls, AsyncCallback<List<RolePermissionModel>> callback);

}
