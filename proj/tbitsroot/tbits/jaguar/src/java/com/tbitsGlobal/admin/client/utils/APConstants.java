package com.tbitsGlobal.admin.client.utils;

import com.google.gwt.core.client.GWT;
import com.tbitsGlobal.admin.client.APService;
import com.tbitsGlobal.admin.client.APServiceAsync;

public class APConstants {

	public static final APServiceAsync apService = GWT.create(APService.class);
	
	public static final String TOKEN_BA = "ba";
	public static final String CURRENT_TAB = "curr";
}
