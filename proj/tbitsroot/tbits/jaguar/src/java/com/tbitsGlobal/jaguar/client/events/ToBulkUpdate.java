package com.tbitsGlobal.jaguar.client.events;

import java.util.List;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;

public class ToBulkUpdate extends TbitsBaseEvent {
	private String sysPrefix;
	private List<TbitsTreeRequestData> data;
	
	public ToBulkUpdate(String sysPrefix, List<TbitsTreeRequestData> data) {
		super();
		
		this.sysPrefix = sysPrefix;
		this.data = data;
	}

	public List<TbitsTreeRequestData> getData() {
		return data;
	}

	public void setData(List<TbitsTreeRequestData> data) {
		this.data = data;
	}

	public void setSysPrefix(String sysPrefix) {
		this.sysPrefix = sysPrefix;
	}

	public String getSysPrefix() {
		return sysPrefix;
	}
}
