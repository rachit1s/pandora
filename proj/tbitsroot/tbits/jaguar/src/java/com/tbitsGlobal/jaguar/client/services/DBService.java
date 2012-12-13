/*
 * tBits - tBits Global Pvt. Ltd.
 * Copyright(c) 2009 - 2010, tBits Global.
 */
package com.tbitsGlobal.jaguar.client.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tbitsGlobal.jaguar.client.dashboard.GadgetInfo;
import com.tbitsGlobal.jaguar.client.serializables.BARequests;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;
import commons.com.tbitsGlobal.utils.client.DQLResults;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.ActionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.ReportClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.ShortcutClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserDraftClient;
import commons.com.tbitsGlobal.utils.client.service.UtilService;

/**
 * 
 * @author sourabh
 * 
 * Service for all data transport in Jaguar
 */
public interface DBService extends UtilService {
	
	//------------------------------------------------------ Requests and associated objects----------------------------------------------------------------//
	/**
	 * Fetched the request for a given BA and request id
	 * @param sysPrefix
	 * @param requestId
	 * @return a {@link RequestData}
	 * @throws TbitsExceptionClient
	 */
	RequestData getRequestData(String sysPrefix, int requestId) throws TbitsExceptionClient;
	
	/**
	 * Get actions for a given request
	 * @param sysPrefix
	 * @param requestId
	 * @return. List of {@link ActionClient}s
	 * @throws TbitsExceptionClient
	 */
	ArrayList<ActionClient> getActions(String sysPrefix, int requestId) throws TbitsExceptionClient;
	
	//------------------------------------------------------ My requests ----------------------------------------------------------------//
	/**
	 * Gets My Requests for a User
	 * @param filterFields. List of filters (logger, assignee_ids etc.)
	 * @throws TbitsExceptionClient
	 */
	HashMap<String,BARequests> getMyRequests(ArrayList<String> filterFields, int pageSize, int pageNo) throws TbitsExceptionClient;
	
	/**
	 * Gets My Requests for a specific BA
	 * @param sysPrefix
	 * @param filterFields
	 * @param pageSize
	 * @param pageNo
	 * @return
	 * @throws TbitsExceptionClient
	 */
	DQLResults getMyRequestsByBA(String sysPrefix, List<String> filterFields, int pageSize, int pageNo) throws TbitsExceptionClient;
	
	//------------------------------------------------------ My reports ----------------------------------------------------------------//
	/**
	 * Gets the User Reports
	 * @return
	 * @throws TbitsExceptionClient
	 */
	ArrayList<ReportClient> getUserReports() throws TbitsExceptionClient;
	
	//------------------------------------------------------ User read actions ----------------------------------------------------------------//
	public int getActionIdbysysIdbyrequestIdbyuserId(int sysId,int requestId, int userId);
	public Boolean registerReadAction(int sysId,int requestId, int actionId,int userId);
	
	//------------------------------------------------------ Saved Search ----------------------------------------------------------------//
	
	/**
	 * Get default saved search
	 * @param sysPrefix
	 * @return
	 * @throws TbitsExceptionClient
	 */
	ShortcutClient getDefaultSearch(String sysPrefix) throws TbitsExceptionClient;
	
	/**
	 * Get all the saved searches for a BA for the logged in user
	 * @param sysPrefix
	 * @return
	 * @throws TbitsExceptionClient
	 */
	ArrayList<ShortcutClient> getSavedSearches(String sysPrefix) throws TbitsExceptionClient;
	
	/**
	 * Deletes a saved search
	 * @param sysPrefix
	 * @param scName
	 * @return
	 * @throws TbitsExceptionClient
	 */
	boolean deleteSavedSearch(String sysPrefix, String scName) throws TbitsExceptionClient;
	
	/**
	 * Marks the shared status of a saved search
	 * @param sysPrefix
	 * @param scName
	 * @param share
	 * @return
	 * @throws TbitsExceptionClient
	 */
	//boolean shareSavedSearch(String sysPrefix, String scName, boolean share) throws TbitsExceptionClient;
	
	//added code for default saved serch defaultSavedSearch
	boolean defaultSavedSearch(String sysPrefix, String scName, boolean _default) throws TbitsExceptionClient;
	//------------------------------------------------------ Drafts ----------------------------------------------------------------//
	/**
	 * Gets all the drafts for a User.
	 * 
	 * @param sysPrefix
	 * @return
	 * @throws TbitsExceptionClient
	 */
	List<UserDraftClient> getUserDrafts(String sysPrefix) throws TbitsExceptionClient;
	
	/**
	 * @return True is "Old version" link is to be displayed
	 */
	boolean showOldVersion();
	
	//------------------------------------------------------ DashBoard ----------------------------------------------------------------//
	
	/**
	 * gets List of all gadget infos
	 */
	List<GadgetInfo> getGadgetInfo() throws TbitsExceptionClient;
	
	/**
	 * Update info of a gadget
	 * @param info
	 * @return. The updated info
	 * @throws TbitsExceptionClient
	 */
	GadgetInfo updateGadgetInfo(GadgetInfo info) throws TbitsExceptionClient;
	
	/**
	 * Gets the HTML content of a gadget
	 * @param reportId
	 * @return
	 * @throws TbitsExceptionClient
	 */
	String getGadgetContent(int reportId) throws TbitsExceptionClient;
	
	//------------------------------------------------------ Roles and permissions ----------------------------------------------------------------//

	List<RoleClient> getRolesForBA(BusinessAreaClient baClient) throws TbitsExceptionClient;
}
