package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.tbitsGlobal.admin.client.widgets.pages.UsersPage;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public interface UserService extends RemoteService{
	public ArrayList<UserClient> getBAUsers(String sysPrefix) throws TbitsExceptionClient;
	
	public List<UserClient> addBAUsers(int sysID, String userStr);
	
	public ArrayList<UserClient> getAllUsers();
	
	public UserClient updateUser(UserClient uClient) throws TbitsExceptionClient;
	
	public boolean setPassword(String userlogin,String password)throws TbitsExceptionClient;
	
	public UserClient insertUser(UserClient uc) throws TbitsExceptionClient;

	public boolean deleteBAUsers(int sysID, List<UserClient> users);

	public UsersPage getAllUsersPage(int page, int pageSize);

	boolean updateUsers(List<UserClient> users) throws TbitsExceptionClient;
}
