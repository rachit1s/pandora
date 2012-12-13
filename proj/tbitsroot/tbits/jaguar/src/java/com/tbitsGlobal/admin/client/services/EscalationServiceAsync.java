package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public interface EscalationServiceAsync {
	public void getAllParentChildMapping(int sysID, AsyncCallback<HashMap<Integer, ArrayList<Integer>>> callback);
	
	public void insertUserhierarchy(int aSystemId, int aChildUserId, int aParentUserId, AsyncCallback<Boolean> callback);
	
	public void deleteUserhierarchy(int sysId, int userId, int parentId, AsyncCallback<Boolean> callback);
	
	public void deleteEscalationCondition(int sysId, TbitsModelData tb, AsyncCallback<Boolean> callback);
	
	public void insertEscalationCondition(int sysId, TbitsModelData tb, AsyncCallback<Boolean> callback);
	
	public void getEscalationCondition (int sysId, AsyncCallback<ArrayList<TbitsModelData>> callback);
}