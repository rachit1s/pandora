/**
 * 
 */
package com.tbitsGlobal.admin.client.events;

import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;

/**
 * @author naveen
 *
 */
public class OnUsersReceived extends TbitsBaseEvent {
	
	
	public OnUsersReceived(){
	
	}
	
	@Override
	public boolean beforeFire() {
			return true;
	}
	
}
