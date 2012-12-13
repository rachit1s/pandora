package com.tbitsGlobal.admin.client.modelData;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class RolePermissionModel extends TbitsModelData{
	public static String FIELD_NAME = "name";
	public static String DISPLAY_NAME = "display_name";
	public static String IS_VIEW	= "is_view";
	public static String IS_ADD		= "is_add";
	public static String IS_UPDATE	= "is_update";
	public static String IS_EMAIL	= "is_email";

	public void setFieldName(String fieldName){
		this.set(FIELD_NAME, fieldName);
	}
	
	public String getFieldName(){
		return (String)this.get(FIELD_NAME);
	}
	
	public void setDisplayName(String displayName){
		this.set(DISPLAY_NAME, displayName);
	}
	
	public String getDisplayName(){
		return (String)this.get(DISPLAY_NAME);
	}
	
	public void setView(boolean isView){
		this.set(IS_VIEW, isView);
	}
	
	public boolean isView(){
		return (Boolean)this.get(IS_VIEW);
	}
	
	public void setAdd(boolean isAdd){
		this.set(IS_ADD, isAdd);
	}
	
	public boolean isAdd(){
		return (Boolean)this.get(IS_ADD);
	}
	
	public void setUpdate(boolean isUpdate){
		this.set(IS_UPDATE, isUpdate);
	}
	
	public boolean isUpdate(){
		return (Boolean)this.get(IS_UPDATE);
	}
	
	public void setEMail(boolean isEMail){
		this.set(IS_EMAIL, isEMail);
	}
	
	public boolean isEMail(){
		return (Boolean)this.get(IS_EMAIL);
	}
}
