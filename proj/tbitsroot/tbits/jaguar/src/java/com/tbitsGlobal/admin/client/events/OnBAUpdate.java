/**
 * 
 */
package com.tbitsGlobal.admin.client.events;

import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

/**
 * @author dheeru
 * 
 */
public class OnBAUpdate extends TbitsBaseEvent {

	private BusinessAreaClient updatedBA;

	public OnBAUpdate(BusinessAreaClient updatedBA) {
		this.updatedBA = updatedBA;
	}

	public boolean beforeFire() {
		BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
		if(cache != null){
			cache.refresh();
			cache.setCurrentBA(this.updatedBA);
		}
		return true;
	}
}
