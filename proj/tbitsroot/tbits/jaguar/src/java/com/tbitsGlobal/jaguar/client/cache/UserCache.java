package com.tbitsGlobal.jaguar.client.cache;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.events.ToRefreshUserCache;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.UserListLoadResult;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.cache.AbstractCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * 
 * @author sourabh
 * 
 * Cache to keep Users
 */
public class UserCache extends AbstractCache<String, UserClient>{
	
	public UserCache() {
		super();
		
		this.subscribe(ToRefreshUserCache.class, new ITbitsEventHandle<ToRefreshUserCache>(){
			public void handleEvent(ToRefreshUserCache event) {
				refresh();
			}});
	}
	
	protected void getFromServer() {
		GlobalConstants.utilService.getAllActiveUsers(new AsyncCallback<UserListLoadResult>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error loading users...", caught);
				Log.error("Error loading users...", caught);
			}
			
			public void onSuccess(UserListLoadResult result) {
				if(result == null)
					return;
				for(UserClient user : result.getData()){
					cache.put(user.getUserLogin(), user);
				}
				onRefresh();
			}});
	}
	
	public void onRefresh() {
		
	}
}
