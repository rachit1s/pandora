package com.tbitsGlobal.admin.client.modelData;

import java.util.HashMap;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
/**
 * Contains the correspondence properties variables
 * POJO for corrProperties
 * @author devashish
 *
 */
public class CorrPropertiesClient extends TbitsModelData {
	
	private HashMap<String, String> corrPropertiesMap;
	
	//-------Static keys defining the variables to be populated---//
	public static String CORR_PROP_ID	= "corrPropId";
	public static String CORR_PROP 		= 	"corrProp";
	public static String CORR_PROP_VALUE = "corrPropValue";
	public static String CORR_PROP_DESC	= "corrPropDescription";
	
	//-----------------------Default Constructor------------------//
	public CorrPropertiesClient(){
		super();
	}
	//--------------get/set id-----------------------------//
	public String getId(){
		return (String) this.get(CORR_PROP_ID);
	}
	
	public void setId(String id){
		this.set(CORR_PROP_ID, id);
	}
	
	//----------------get/set description------------------//
	public String getDescription(){
		return (String) this.get(CORR_PROP_DESC);
	}
	
	public void setDescription(String desc){
		this.set(CORR_PROP_DESC, desc);
	}
	//--------------get/set property-----------------------//
	public String getProperty(){
		return (String) this.get(CORR_PROP);
	}	
	public void setProperty(String property){
		this.set(CORR_PROP, property);
	}
	//--------------get/set value---------------------------//
	public String getPropertyValue(){
		return (String) this.get(CORR_PROP_VALUE);
	}
	
	public void setPropertyValue(String propertyValue){
		this.set(CORR_PROP_VALUE, propertyValue);
	}
	//---------For corrPropMap------------//
	public void setCorrPropMap(HashMap<String, String> map){
		this.corrPropertiesMap = map;
	}
	
	public HashMap<String, String> getCorrPropMap(){
		return corrPropertiesMap;
	}
}
