package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.modelData.SysInfoClient;

public interface SystemPropertiesServiceAsync {
	void getSysInfo(AsyncCallback<ArrayList<SysInfoClient>> callback);
}
