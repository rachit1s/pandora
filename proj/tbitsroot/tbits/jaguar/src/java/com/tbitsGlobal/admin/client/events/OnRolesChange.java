
package com.tbitsGlobal.admin.client.events;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;

/**
 * @author naveen
 * 
 */
public class OnRolesChange extends TbitsBaseEvent {

	public RoleClient updatedRole;
	
	//true if the roleclient is added and false if deleted
	public boolean added;

	public OnRolesChange(RoleClient updatedRole,boolean added) {
		this.updatedRole = updatedRole;
		this.added = added;
	}

	public boolean beforeFire() {
		return true;
	}
}
