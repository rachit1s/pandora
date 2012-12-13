package com.tbitsGlobal.jaguar.client.events;

import com.tbitsGlobal.jaguar.client.state.AppState;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;

public class OnDisplayGroupsReceived extends TbitsBaseEvent{
	@Override
	public boolean beforeFire() {
		if(AppState.checkAppStateIsBefore(AppState.DisplayGroupsReceived)){
			AppState.setAppState(AppState.DisplayGroupsReceived);
			return true;
		}else{
			AppState.delayTillAppStateIsBefore(AppState.DisplayGroupsReceived, this);
		}
		return false;
	}
}
