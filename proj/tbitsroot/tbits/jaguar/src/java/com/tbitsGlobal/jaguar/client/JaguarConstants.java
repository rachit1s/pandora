package com.tbitsGlobal.jaguar.client;

import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.core.client.GWT;
import com.tbitsGlobal.jaguar.client.services.DBService;
import com.tbitsGlobal.jaguar.client.services.DBServiceAsync;
import com.tbitsGlobal.jaguar.client.widgets.TbitsMainTabPanel;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserDraftClient;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;

/**
 * 
 * @author sourabh
 *
 * This class contains all the constants to be used for the Jaguar Module
 */
public class JaguarConstants extends GlobalConstants{
	public static final JaguarMessages MESSAGES = GWT.create(JaguarMessages.class);
	
	public static final JaguarProps PROPS = GWT.create(JaguarProps.class);
	
	public static final DBServiceAsync dbService = GWT.create(DBService.class);
	
	public static ListStore<UserDraftClient> drafts = null;
	
	public static String JAGUAR_PROXY = PROPS.proxy();
	
	public static TbitsMainTabPanel jaguarTabPanel = null;
	
	public static void registerKeys(){
		TbitsURLManager.getInstance().register(GlobalConstants.TOKEN_VIEW, false);
		TbitsURLManager.getInstance().register(GlobalConstants.TOKEN_UPDATE, false);
		TbitsURLManager.getInstance().register(GlobalConstants.TOKEN_BA, true);
		TbitsURLManager.getInstance().register(GlobalConstants.TOKEN_DQL, true);
		TbitsURLManager.getInstance().register(GlobalConstants.TOKEN_TAGS_PUBLIC, false);
		TbitsURLManager.getInstance().register(GlobalConstants.TOKEN_TAGS_PRIVATE, false);
	}
}
