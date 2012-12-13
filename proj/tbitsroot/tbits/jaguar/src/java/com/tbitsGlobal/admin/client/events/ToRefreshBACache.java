package com.tbitsGlobal.admin.client.events;

import com.tbitsGlobal.admin.client.state.AppState;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;

public class ToRefreshBACache extends TbitsBaseEvent {

	public ToRefreshBACache() {
		super("Retrieving BA List.. please wait", "fail to load BA List");
	}

	@Override
	public boolean beforeFire() {
		if (AppState.checkAppStateIsTill(AppState.UserReceived)) {
			return true;
		} else {
			AppState.delayTillAppStateIsTill(AppState.UserReceived, this);
		}
		return false;
	}

}
