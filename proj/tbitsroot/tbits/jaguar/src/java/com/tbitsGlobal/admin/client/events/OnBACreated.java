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
public class OnBACreated extends TbitsBaseEvent {
	
	private BusinessAreaClient newBA;
	
	public OnBACreated(BusinessAreaClient newBA) {
		super();
		this.newBA = newBA; 
	}
	
	public BusinessAreaClient getNewBA() {
		return newBA;
	}
	
	public boolean beforeFire() {
		BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
		cache.refresh();
		return true;
	}
	
}
