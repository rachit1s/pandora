package com.tbitsGlobal.admin.client.permTool;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

/**
 * This class simply encapsulates the data that needs to be fetched from the server for the permissioning tool.
 * 
 * @author Karan Gupta
 *
 */
public class PermissionInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private HashMap<String, HashMap<String, Boolean>> fieldMap;
	private List<RoleClient> roles;
	private List<UserClient> mailingLists;
	
	// Getter and setter methods
	public void setFieldMap(HashMap<String, HashMap<String, Boolean>> fieldMap) {
		this.fieldMap = fieldMap;
	}
	
	public HashMap<String, HashMap<String, Boolean>> getFieldMap() {
		return fieldMap;
	}

	public void setRoles(List<RoleClient> roles) {
		this.roles = roles;
	}

	public List<RoleClient> getRoles() {
		return roles;
	}

	public void setMailingLists(List<UserClient> mailingLists) {
		this.mailingLists = mailingLists;
	}

	public List<UserClient> getMailingLists() {
		return mailingLists;
	}

}
