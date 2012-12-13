package com.tbitsGlobal.admin.client;

import com.google.gwt.core.client.GWT;

public class AdminUtils {
	public static String getAppBaseURL(){
		String url = GWT.getHostPageBaseURL();
		url = url.substring(0, url.indexOf("adm/"));
		return url;
	}
}
