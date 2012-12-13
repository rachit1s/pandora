package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.widgets.pages.UsersPage;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public interface UserServiceAsync {
	void getAllUsers(AsyncCallback<ArrayList<UserClient>> callback);
	
	void getAllUsersPage(int page, int pageSize, AsyncCallback<UsersPage> callback);

	void updateUser(UserClient uClient, AsyncCallback<UserClient> callback);

	void setPassword(String userlogin, String password,
			AsyncCallback<Boolean> callback);

	void insertUser(UserClient uc, AsyncCallback<UserClient> callback);
	
	public void addBAUsers(int sysID, String userStr, AsyncCallback<List<UserClient>> callback);
	
	public void deleteBAUsers(int sysID, List<UserClient> users, AsyncCallback<Boolean> callback);
		
	public void getBAUsers(String sysPrefix, AsyncCallback<ArrayList<UserClient>> callback);
	
	public void updateUsers(List<UserClient> users, AsyncCallback<Boolean> callback);
}
