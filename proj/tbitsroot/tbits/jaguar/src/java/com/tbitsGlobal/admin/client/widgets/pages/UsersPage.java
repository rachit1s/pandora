package com.tbitsGlobal.admin.client.widgets.pages;

import java.io.Serializable;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public class UsersPage implements Serializable{
	private int totalUsers;
	private List<UserClient> users;
	
	public UsersPage() {
		// TODO Auto-generated constructor stub
	}
	
	public UsersPage(List<UserClient> users, int totalUsers) {
		this();
		this.users = users;
		this.totalUsers = totalUsers;
	}

	public int getTotalUsers() {
		return totalUsers;
	}

	public void setTotalUsers(int totalUsers) {
		this.totalUsers = totalUsers;
	}

	public List<UserClient> getUsers() {
		return users;
	}

	public void setUsers(List<UserClient> users) {
		this.users = users;
	}
	
}
