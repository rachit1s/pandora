package com.tbitsGlobal.jaguar.client.events;

import java.util.List;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;

public class OnConsolidateRequests extends TbitsBaseEvent{
	private String sysPrefix;
	private List<TbitsTreeRequestData> models;

	public OnConsolidateRequests(String sysPrefix, List<TbitsTreeRequestData> models) {
		super();
		
		this.sysPrefix = sysPrefix;
		this.models = models;
	}
	
	public List<TbitsTreeRequestData> getModels() {
		return models;
	}

	public void setModels(List<TbitsTreeRequestData> models) {
		this.models = models;
	}

	public void setSysPrefix(String sysPrefix) {
		this.sysPrefix = sysPrefix;
	}

	public String getSysPrefix() {
		return sysPrefix;
	}
}
