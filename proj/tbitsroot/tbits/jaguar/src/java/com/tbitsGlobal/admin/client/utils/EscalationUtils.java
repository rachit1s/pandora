package com.tbitsGlobal.admin.client.utils;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;

import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;


public class EscalationUtils {
	
	public static ComboBox<UserClient> getUserCombo(){
		final ListStore<UserClient> userStore = new ListStore<UserClient>();
		final ComboBox<UserClient> userCombo = new ComboBox<UserClient>();
		userCombo.setStore(userStore);
		userCombo.setDisplayField(UserClient.DISPLAY_NAME);
		
		APConstants.apService.getAllUsers(new AsyncCallback<ArrayList<UserClient>>() {
			
			@Override
			public void onSuccess(ArrayList<UserClient> result) {
				userCombo.getStore().add(result);
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error in  fetching user list from database...", caught);
				Log.error("Error in fetching user list from database...", caught);
				caught.printStackTrace();
				
			}
		});
		
		
		return userCombo;
		
	}
}
