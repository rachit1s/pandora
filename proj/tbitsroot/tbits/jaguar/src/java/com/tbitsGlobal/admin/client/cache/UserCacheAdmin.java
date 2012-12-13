/**
 * 
 */
package com.tbitsGlobal.admin.client.cache;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.events.OnUserAdd;
import com.tbitsGlobal.admin.client.events.OnUserChange;
import com.tbitsGlobal.admin.client.events.OnUserDelete;
import com.tbitsGlobal.admin.client.events.OnUsersReceived;
import com.tbitsGlobal.admin.client.events.ToRefreshUsersCache;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.UserListLoadResult;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.cache.AbstractCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * @author naveen
 *
 */

public class UserCacheAdmin extends AbstractCache<String, UserClient>{
	
	public UserCacheAdmin() {
		super();

		this.subscribe(ToRefreshUsersCache.class, new ITbitsEventHandle<ToRefreshUsersCache>() {
			public void handleEvent(ToRefreshUsersCache event) {
				if(event.getState()== ToRefreshUsersCache.INITIAL){
					refresh();
				}
				else{
					UserClient uc = event.getChangedUser();
					if(event.getState() == ToRefreshUsersCache.ADD){
						cache.put(uc.getUserLogin(), uc);
						TbitsEventRegister.getInstance().fireEvent(new OnUserAdd(uc));
					}
					else if (event.getState() == ToRefreshUsersCache.DELETE){
						cache.remove(uc.getUserLogin());
						TbitsEventRegister.getInstance().fireEvent(new OnUserDelete(uc));
					}
					else{ 
						cache.put(uc.getUserLogin(), uc);
						TbitsEventRegister.getInstance().fireEvent(new OnUserChange(uc));
					}
				}
			}
		});
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
		TbitsEventRegister.getInstance().fireEvent(new OnUsersReceived());
	}
}




