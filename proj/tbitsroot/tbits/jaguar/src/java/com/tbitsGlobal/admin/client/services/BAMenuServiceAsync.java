package com.tbitsGlobal.admin.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.domainObjects.BAMenuClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

public interface BAMenuServiceAsync {
	void getBAMenus(AsyncCallback<List<BAMenuClient>> callback);

	void deleteMenus(List<BAMenuClient> models, AsyncCallback<Boolean> callback);

	void updateMenus(List<BAMenuClient> models, AsyncCallback<List<BAMenuClient>> callback);

	void updateBAMenuMapping(int menuId, List<BusinessAreaClient> bas, AsyncCallback<Boolean> callback);

	void getBAMenuMapping(int menuId, AsyncCallback<List<Integer>> callback);
}
