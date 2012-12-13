package com.tbitsGlobal.jaguar.client.events;

import com.tbitsGlobal.jaguar.client.state.AppState;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;

public class ToRefreshDisplayGroupCache extends TbitsBaseEvent{
	
	public ToRefreshDisplayGroupCache() {
		super();
	}
	
	public boolean beforeFire() {
		if((GlobalConstants.appState & AppState.BAChanged.getVal()) != 0){
			return true;
		}else{
			AppState.delayTillAppStateIsBefore(AppState.BAChanged, this);
		}
		return false;
	}
}
