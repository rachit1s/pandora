/**
 * 
 */
package com.tbitsGlobal.admin.client.events;


import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

/**
 * @author naveen
 *
 */
public class ToRefreshUsersCache extends TbitsBaseEvent{
	
	public final static int ADD = 1;
	public final static int DELETE = 2;
	public final static int CHANGE = 3;
	public final static int INITIAL = 0;
	
	private int state;
	private UserClient userChanged;
	
	public ToRefreshUsersCache(UserClient uc, int state) {
		this.state = state;
		this.userChanged = uc;
			
	}
	
	public ToRefreshUsersCache() {
		this.state = INITIAL;
	}

	public int getState(){
		return this.state;
	}
	
	public UserClient getChangedUser(){
		if(this.getState() != 0)
			return this.userChanged;
		else return null;
	}
	
	@Override
	public boolean beforeFire() {
		return true;
	}
}
