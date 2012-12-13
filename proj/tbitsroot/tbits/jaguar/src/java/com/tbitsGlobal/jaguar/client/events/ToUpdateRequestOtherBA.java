package com.tbitsGlobal.jaguar.client.events;

import com.tbitsGlobal.jaguar.client.state.AppState;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;

public class ToUpdateRequestOtherBA extends TbitsBaseEvent {
	private int requestId;
	private String sysPrefix;
	
	public ToUpdateRequestOtherBA(String sysPrefix, int requestId) {
		super();
		
		this.sysPrefix = sysPrefix;
		this.requestId = requestId;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}
	
	public void setSysPrefix(String sysPrefix) {
		this.sysPrefix = sysPrefix;
	}

	public String getSysPrefix() {
		return sysPrefix;
	}
	
	@Override
	public boolean beforeFire() {
		if(AppState.checkAppStateIsBefore(AppState.BAChanged)){
			return true;
		}else{
			AppState.delayTillAppStateIsBefore(AppState.BAChanged, this);
		}
		return false;
	}
}
