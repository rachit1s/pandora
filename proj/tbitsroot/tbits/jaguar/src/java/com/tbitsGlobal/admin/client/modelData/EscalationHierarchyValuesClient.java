package com.tbitsGlobal.admin.client.modelData;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public class EscalationHierarchyValuesClient extends TbitsModelData {

	public static String ESC_ID		=	"esc_id";
	public static String CHILD_USER		=	"child_user";
	public static String PARENT_USER	=	"parent_user";
	
	public int getEscId(){
		if(this.getPropertyNames().contains(ESC_ID))
			return (Integer)this.get(ESC_ID);
		return 0;
	}
	
	@Override
	public String toString() {
		return "EscalationHierarchyValuesClient [getChlidUser()="
				+ getChlidUser() + ", getEscId()=" + getEscId()
				+ ", getParentUser()=" + getParentUser() + "]";
	}

	public void setEscId(int escId){
		this.set(ESC_ID, escId);
	}
	public UserClient getChlidUser(){
		return (UserClient)this.get(CHILD_USER);
	}
	
	public void setChildUser(UserClient childUser){
		this.set(CHILD_USER,childUser);
	}
	
	public UserClient getParentUser(){
		return (UserClient)this.get(PARENT_USER);
	}
	
	public void setParentUser(UserClient parentUser){
		this.set(PARENT_USER, parentUser);
	}
	
}
