package com.tbitsGlobal.admin.client.services;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CaptionsServiceAsync {
	void updateCaptions(HashMap<String, String> map, int sysId,
			AsyncCallback<Boolean> callback);

	void getAllBACaptionsbySysId(int sysId,
			AsyncCallback<HashMap<String, String>> callback);
	
	void addCaptions(HashMap<String,String> map, int sysId,
			AsyncCallback<Boolean> callback);

	void deletecaption(String captionname, String captionvalue, int sysId,
			AsyncCallback<Boolean> callback);
	
	void updateDefaultCaptions(HashMap<String, String> map,
			AsyncCallback<Boolean> callback);
}
