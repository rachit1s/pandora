package com.tbitsGlobal.admin.client.modelData;

import java.util.HashMap;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

/**
 * Contains the system information variables
 * POJO for sysinfo
 * @author devashish
 *
 */
public class SysInfoClient extends TbitsModelData{
	private HashMap<String, String> sysInfoMap;
	
	//-------Static keys defining the variables to be populated---//
	public static String SYS_PROP = "sysprop";
	public static String SYS_PROP_VALUE = "sysprovalue";
	public static String GROUP			= "group";
	
	//-----------------------Default Constructor------------------//
	public SysInfoClient(){
		super();
	}
	
	//--------------get/set property-----------------------//
	public String getProperty(){
		return (String) this.get(SYS_PROP);
	}
	
	public void setProperty(String property){
		this.set(SYS_PROP, property);
	}
	//--------------get/set value---------------------------//
	public String getPropertyValue(){
		return (String) this.get(SYS_PROP_VALUE);
	}
	
	public void setPropertyValue(String propertyValue){
		this.set(SYS_PROP_VALUE, propertyValue);
	}
	//---------For sysinfomap------------//
	public void setSysInfo(HashMap<String, String> map){
		this.sysInfoMap = map;
	}
	
	public HashMap<String, String> getSysInfo(){
		return sysInfoMap;
	}
	//--------For group-----------------//
	public void setGroup(String group){
		this.set(GROUP, group);
	}
	public String getGroup(){
		return (String) this.get(GROUP);
	}
}
