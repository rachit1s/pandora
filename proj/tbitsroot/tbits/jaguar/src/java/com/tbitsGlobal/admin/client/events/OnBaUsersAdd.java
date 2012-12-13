package com.tbitsGlobal.admin.client.events;

import java.util.List;

import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public class OnBaUsersAdd extends TbitsBaseEvent{
	private List<UserClient> users;

	public OnBaUsersAdd(List<UserClient> users) {
		super();
		this.users = users;
	}

	public List<UserClient> getUsers() {
		return users;
	}

	public void setUsers(List<UserClient> users) {
		this.users = users;
	}
	
}
