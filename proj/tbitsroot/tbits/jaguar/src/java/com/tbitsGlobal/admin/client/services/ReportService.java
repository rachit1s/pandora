package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.tbitsGlobal.admin.client.modelData.ReportParamClient;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.ReportClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;

public interface ReportService extends RemoteService{
	public ArrayList<Integer> deleteReports(ArrayList<Integer> reportIds) throws TbitsExceptionClient;
	
	public HashMap<Integer, Boolean> getReportSpecificUsers(int reportId) throws TbitsExceptionClient;
	
	public HashMap<Integer, ArrayList<Integer>> getReportRoles(int reportId) throws TbitsExceptionClient;
	
	public ReportClient updateReport(ReportClient reportClient) throws TbitsExceptionClient;
	
	public int addReport(ReportClient reportClient) throws TbitsExceptionClient;
	
	public boolean updateReportRoles(HashMap<String, ArrayList<RoleClient>> reportRoleClients, int reportId) throws TbitsExceptionClient;
	
	public ArrayList<ReportClient> getReports() throws TbitsExceptionClient;
	
	public String[] getReportFileNames() throws TbitsExceptionClient ;

	public boolean updateReportSpecificUsers(List<String> userLogins, int reportId, boolean includeOrExclude) throws TbitsExceptionClient;
	
	public List<ReportParamClient> getReportParams(int reportId) throws TbitsExceptionClient;
	
	public boolean updateReportParams(int reportId, List<ReportParamClient> models) throws TbitsExceptionClient;
}
