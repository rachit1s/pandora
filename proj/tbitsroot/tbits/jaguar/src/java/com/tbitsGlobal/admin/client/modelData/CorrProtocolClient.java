package com.tbitsGlobal.admin.client.modelData;

import java.util.HashMap;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class CorrProtocolClient extends TbitsModelData {
	private HashMap<String, String> corrProtocolMap;
	
	//-------Static keys defining the variables to be populated---//
	public static String SYS_PREFIX		= "sysPrefix";
	public static String CORR_PROT_ID	= "corrPropId";
	public static String CORR_PROT 		= 	"corrProp";
	public static String CORR_PROT_VALUE = "corrPropValue";
	public static String CORR_PROT_DESC	= "corrPropDescription";
	
	//-----------------------Default Constructor------------------//
	public CorrProtocolClient(){
		super();
	}
	
	//--------------get/set sysPrefix----------------------//
	public String getSysPrefix(){
		return (String) this.get(SYS_PREFIX);
	}
	
	public void setSysPrefix(String sysPrefix){
		this.set(SYS_PREFIX, sysPrefix);
	}
	//--------------get/set id-----------------------------//
	public String getId(){
		return (String) this.get(CORR_PROT_ID);
	}
	
	public void setId(String id){
		this.set(CORR_PROT_ID, id);
	}
	
	//----------------get/set description------------------//
	public String getDescription(){
		return (String) this.get(CORR_PROT_DESC);
	}
	
	public void setDescription(String desc){
		this.set(CORR_PROT_DESC, desc);
	}
	//--------------get/set property-----------------------//
	public String getProperty(){
		return (String) this.get(CORR_PROT);
	}	
	public void setProperty(String property){
		this.set(CORR_PROT, property);
	}
	//--------------get/set value---------------------------//
	public String getPropertyValue(){
		return (String) this.get(CORR_PROT_VALUE);
	}
	
	public void setPropertyValue(String propertyValue){
		this.set(CORR_PROT_VALUE, propertyValue);
	}
	//---------For corrPropMap------------//
	public void setCorrPropMap(HashMap<String, String> map){
		this.corrProtocolMap = map;
	}
	
	public HashMap<String, String> getCorrPropMap(){
		return corrProtocolMap;
	}
}
