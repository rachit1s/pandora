package com.tbitsGlobal.jaguar.client.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.dashboard.GadgetInfo;
import com.tbitsGlobal.jaguar.client.serializables.BARequests;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;
import commons.com.tbitsGlobal.utils.client.DQLResults;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.ActionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.ReportClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.ShortcutClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserDraftClient;
import commons.com.tbitsGlobal.utils.client.service.UtilServiceAsync;

public interface DBServiceAsync extends UtilServiceAsync{
	void getRequestData(String sysPrefix, int requestId, AsyncCallback<RequestData> callback);
	
	void getActions(String sysPrefix, int requestId, AsyncCallback<ArrayList<ActionClient>> callback);
	
	void getMyRequests(ArrayList<String> filterFields, int pageSize, int pageNo, AsyncCallback<HashMap<String,BARequests>> callback);
	void getMyRequestsByBA(String sysPrefix, List<String> filterFields, int pageSize, int pageNo, AsyncCallback<DQLResults> callback);
	
	void getUserReports(AsyncCallback<ArrayList<ReportClient>> callback);
	
	public void getActionIdbysysIdbyrequestIdbyuserId(int sysID,int requestId, int userId,AsyncCallback<Integer> callback);
	public void registerReadAction(int sysId,int requestId, int actionId,int userId,AsyncCallback<Boolean>callback);
	
	void getDefaultSearch(String sysPrefix, AsyncCallback<ShortcutClient> callback);
	void getSavedSearches(String sysPrefix, AsyncCallback<ArrayList<ShortcutClient>> callback);
	void deleteSavedSearch(String sysPrefix, String scName, AsyncCallback<Boolean> callback);
	//void shareSavedSearch(String sysPrefix, String scName, boolean share, AsyncCallback<Boolean> callback);
	void defaultSavedSearch(String sysPrefix, String scName, boolean _default, AsyncCallback<Boolean> callback);
	
	void getUserDrafts(String sysPrefix, AsyncCallback<List<UserDraftClient>> callback);
	
	void showOldVersion(AsyncCallback<Boolean> callback);
	
	
	void getGadgetInfo(AsyncCallback<List<GadgetInfo>> callback);
	void updateGadgetInfo(GadgetInfo info, AsyncCallback<GadgetInfo> callback);
	void getGadgetContent(int reportId, AsyncCallback<String> callback);
	
	//roles and permissions
	void getRolesForBA(BusinessAreaClient baClient, AsyncCallback<List<RoleClient>> callback);
}
