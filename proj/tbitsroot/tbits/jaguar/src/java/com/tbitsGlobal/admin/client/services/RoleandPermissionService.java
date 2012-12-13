package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.tbitsGlobal.admin.client.modelData.RolePermissionModel;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleUserClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public interface RoleandPermissionService extends RemoteService{
	public HashMap<String, ArrayList<RoleClient>> getRoleTree() throws TbitsExceptionClient;
	
	public List<RoleClient> getRoleBySysPrefix(String sysPrefix) throws TbitsExceptionClient ;
	
	public ArrayList<RoleClient> getRolesbySysIDandUserID(int sysID, int userID) throws TbitsExceptionClient ;
	
	public boolean updateRoleUser(int sysID, int userId, ArrayList<RoleUserClient> rucList)throws TbitsExceptionClient ;
	
	public boolean updateRoleUsers(String sysPrefix, RoleClient role, List<UserClient> users)throws TbitsExceptionClient ;

	public List<RolePermissionModel> getPermissionsbysysIdandRoleId(int sysId,int roleId) throws TbitsExceptionClient;
	
	public RoleClient addRole(RoleClient roleClient) throws TbitsExceptionClient;
	
	public boolean deleteRole(RoleClient roleClient) throws TbitsExceptionClient;
	
	public boolean deleteRoles(List<RoleClient> roles) throws TbitsExceptionClient;
	
	public boolean updateRoles(List<RoleClient> roles) throws TbitsExceptionClient;
	
	ArrayList<UserClient> getUsersByRoleId(int systemId, int roleId) throws TbitsExceptionClient;
	
	public boolean updateRolePermissions(int sysId,int roleId, List<RolePermissionModel> rolePermissions) throws TbitsExceptionClient;

	public boolean updateRolePermissions(int sysId, List<Integer> roleIds, List<RolePermissionModel> rolePermissions) throws TbitsExceptionClient;
}
