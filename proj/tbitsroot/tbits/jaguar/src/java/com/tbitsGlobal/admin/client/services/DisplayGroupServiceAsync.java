package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;

public interface DisplayGroupServiceAsync {
	public void getDisplayGroups(String sys_prefix, AsyncCallback<ArrayList<DisplayGroupClient>> asyncCallback);
	
	public void updateDisplayGroup(String sysPrefix, DisplayGroupClient group, AsyncCallback<DisplayGroupClient> callback);

	public void updateDisplayGroups(String sysPrefix, List<DisplayGroupClient> groups, AsyncCallback<List<DisplayGroupClient>> callback);
		
	public void insertDisplayGroup(String sysPrefix, String displayName,int order,boolean isActive,boolean isDefault, AsyncCallback<DisplayGroupClient> callback);
	
	public void deleteDisplayGroup(String sysPrefix, DisplayGroupClient dgc, AsyncCallback<DisplayGroupClient> callback);
	
	public void deleteDisplayGroups(String sysPrefix, List<DisplayGroupClient> groups, AsyncCallback<Boolean> callback);

	void getFieldsByDisplayGroup(DisplayGroupClient displayGroup,AsyncCallback<List<FieldClient>> callback);
}
