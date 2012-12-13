package com.tbitsGlobal.admin.client.events;

import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public class OnUserDelete extends TbitsBaseEvent{
	private UserClient user;

	public OnUserDelete(UserClient user) {
		super();
		
		this.user = user;
	}

	public void setUser(UserClient user) {
		this.user = user;
	}

	public UserClient getUser() {
		return user;
	}
}
