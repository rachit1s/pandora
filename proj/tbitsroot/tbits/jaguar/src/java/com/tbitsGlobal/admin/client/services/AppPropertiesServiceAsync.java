package com.tbitsGlobal.admin.client.services;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AppPropertiesServiceAsync {
	void getAppProperties(AsyncCallback<HashMap<String, String>> callback);

	void updateAppProperties(HashMap<String, String> map,
			AsyncCallback<Boolean> callback);
	
	void insertAppProperties(String name, String value,
			AsyncCallback<Boolean> callback);

	void deleteAppProperties(String name, AsyncCallback<Boolean> callback);
}
