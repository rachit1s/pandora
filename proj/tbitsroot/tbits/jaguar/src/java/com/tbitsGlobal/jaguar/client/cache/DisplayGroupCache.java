package com.tbitsGlobal.jaguar.client.cache;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.events.OnDisplayGroupsReceived;
import com.tbitsGlobal.jaguar.client.events.ToRefreshDisplayGroupCache;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.cache.AbstractCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;

/**
 * 
 * @author sourabh
 * 
 * Cache for display groups
 */
public class DisplayGroupCache extends AbstractCache<Integer, DisplayGroupClient> {
	
	public DisplayGroupCache() {
		super();
		
		this.subscribe(ToRefreshDisplayGroupCache.class, new ITbitsEventHandle<ToRefreshDisplayGroupCache>(){
			public void handleEvent(ToRefreshDisplayGroupCache event) {
				refresh();
			}});
	}
	
	@Override
	protected void getFromServer() {
		JaguarConstants.dbService.getDisplayGroups(ClientUtils.getSysPrefix(),new AsyncCallback<ArrayList<DisplayGroupClient>>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error while loading display groups... Please refresh!!!");
			}
			
			public void onSuccess(ArrayList<DisplayGroupClient> result) {
				for(DisplayGroupClient d:result){
					cache.put(d.getId(), d);
			    }
				onRefresh();
			}
	    		
	    });
	}

	public void onRefresh() {
		TbitsEventRegister.getInstance().fireEvent(new OnDisplayGroupsReceived());
	}
	
}
