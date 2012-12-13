package transmittal.com.tbitsGlobal.client.models;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class TrnReplicateProcess extends TbitsModelData {
	
	public static String PROCESS_ID		= "Process ID";
	public static String DEST_BA		= "Destination BA [Source Target Field Map]";
	public static String DEST_BA_POST_TRN 	= "Destination BA [Post Transmittal Field Map]";
	public static String DROPDOWN_ID	= "Dropdown ID";
	public static String DROPDOWN_NAME	= "Dropdown Name";
	public static String DROPDOWN_SORT_ORDER = "Dropdown Sort Order";
	public static String PROCESS_DESC	= "Process Description";
	public static String DTR_SYS_ID		= "DTR Sys ID";
	public static String DTN_SYS_ID		= "DTN Sys ID";
	public static String MAX_KEY		= "Max Key";
	
	public static String PARAM_NAME			= "paramName";
	public static String PARAM_VALUE_OLD	= "paramValueOld";
	public static String PARAM_VALUE_NEW	= "paramValueNew";
	
	public TrnReplicateProcess(){}
	
	//------------getter/setter for new value--------------------//
	public void setParamValueNew(String paramValueNew){
		this.set(PARAM_VALUE_NEW, paramValueNew);
	}
	
	public String getParamValueNew(){
		return (String) this.get(PARAM_VALUE_NEW);
	}
	//------------getter/setter for old value--------------------//
	public void setParamValueOld(String paramValueOld){
		this.set(PARAM_VALUE_OLD, paramValueOld);
	}
	
	public String getParamValueOld(){
		return (String) this.get(PARAM_VALUE_OLD);
	}
	//------------getter/setter for name-------------------------//
	public String getParamName(){
		return (String) this.get(PARAM_NAME);
	}
	
	public void setParamName(String paramName){
		this.set(PARAM_NAME, paramName);
	}

}
