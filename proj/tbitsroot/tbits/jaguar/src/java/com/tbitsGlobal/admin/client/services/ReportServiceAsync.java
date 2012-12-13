package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.modelData.ReportParamClient;

import commons.com.tbitsGlobal.utils.client.domainObjects.ReportClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;

public interface ReportServiceAsync {
	void getReports(AsyncCallback<ArrayList<ReportClient>> callback);
	
	void getReportRoles(int reportId,
			AsyncCallback<HashMap<Integer, ArrayList<Integer>>> callback);
	
	void addReport(ReportClient reportClient, AsyncCallback<Integer> callback);

	void updateReport(ReportClient reportClient,
			AsyncCallback<ReportClient> callback);

	void deleteReports(ArrayList<Integer> reportIds,
			AsyncCallback<ArrayList<Integer>> callback);

	void getReportSpecificUsers(int reportId,
			AsyncCallback<HashMap<Integer, Boolean>> callback);
	
	void updateReportRoles(
			HashMap<String, ArrayList<RoleClient>> reportRoleClients,
			int reportId, AsyncCallback<Boolean> callback);

	void updateReportSpecificUsers(List<String> userLogins, int reportId,
			boolean includeOrExclude, AsyncCallback<Boolean> callback);

	void getReportFileNames(AsyncCallback<String[]> callback);

	void getReportParams(int reportId, AsyncCallback<List<ReportParamClient>> callback);

	void updateReportParams(int reportId, List<ReportParamClient> models, AsyncCallback<Boolean> callback);
}
