package com.tbitsGlobal.admin.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.APConstants;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class AdminBusinessAreaCache extends BusinessAreaCache {
	@Override
	protected void getFromServer() {
		APConstants.apService.getAllBAList(new AsyncCallback<List<BusinessAreaClient>>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error while initializing Business Areas...", caught);
				Log.error("Error while initializing Business Areas...", caught);
			}

			public void onSuccess(List<BusinessAreaClient> result) {
				if(result != null){
					for(BusinessAreaClient ba : result){
						cache.put(ba.getSystemPrefix(), ba);
					}
					TbitsEventRegister.getInstance().fireEvent(new commons.com.tbitsGlobal.utils.client.Events.OnCacheUpdates());
					onRefresh();
				}
			}
		});
	}
}
