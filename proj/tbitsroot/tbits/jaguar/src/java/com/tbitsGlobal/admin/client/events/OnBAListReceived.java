package com.tbitsGlobal.admin.client.events;

import com.tbitsGlobal.admin.client.state.AppState;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;

public class OnBAListReceived extends TbitsBaseEvent {

	public OnBAListReceived() {
		super();
	}

	public boolean beforeFire() {
		if (AppState.checkAppStateIsBefore(AppState.BAMapReceived)) {
			AppState.setAppState(AppState.BAMapReceived);
			return true;
		} else {
			AppState.delayTillAppStateIsBefore(AppState.BAMapReceived, this);
		}
		return false;
	}
}
