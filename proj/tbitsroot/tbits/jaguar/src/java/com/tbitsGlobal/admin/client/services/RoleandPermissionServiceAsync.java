package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.modelData.RolePermissionModel;

import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleUserClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public interface RoleandPermissionServiceAsync {
	void getUsersByRoleId(int systemId, int roleId,
			AsyncCallback<ArrayList<UserClient>> asyncCallback);


	void updateRoleUser(int sysID, int userId, ArrayList<RoleUserClient> rucList, AsyncCallback<Boolean> callback);
	
	void updateRoleUsers(String sysPrefix, RoleClient role, List<UserClient> users, AsyncCallback<Boolean> callback);
	
	void getRoleTree(AsyncCallback<HashMap<String, ArrayList<RoleClient>>> callback);
	
	void addRole(RoleClient roleClient, AsyncCallback<RoleClient> callback);

	void deleteRole(RoleClient roleClient, AsyncCallback<Boolean> callback);
	
	void deleteRoles(List<RoleClient> roles, AsyncCallback<Boolean> callback);
	
	void getPermissionsbysysIdandRoleId(int sysId, int roleId,
			AsyncCallback<List<RolePermissionModel>> callback);
	
	public void getRolesbySysIDandUserID(int sysID, int userID, AsyncCallback<ArrayList<RoleClient>> callback );
	
	void updateRolePermissions(int sysId, int roleId,
			List<RolePermissionModel> rolePermissions, AsyncCallback<Boolean> callback);
	
	void updateRolePermissions(int sysId, List<Integer> roleIds,
			List<RolePermissionModel> rolePermissions, AsyncCallback<Boolean> callback);
	
	public void getRoleBySysPrefix(String sysPrefix, AsyncCallback<List<RoleClient>> callback);

	void updateRoles(List<RoleClient> roles, AsyncCallback<Boolean> callback); 
}
