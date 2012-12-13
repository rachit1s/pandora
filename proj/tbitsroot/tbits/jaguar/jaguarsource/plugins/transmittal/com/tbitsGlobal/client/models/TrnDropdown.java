package transmittal.com.tbitsGlobal.client.models;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

/**
 * POJO for Transmittal Dropdown table page
 * @author devashish
 */
public class TrnDropdown extends TbitsModelData {
	public static String SRC_BA		 	= "srcBa";
	public static String DROPDOWN_ID	= "dropdownId";
	public static String PROCESS_NAME	= "processName";
	public static String SORT_ORDER		= "sortOrder";
	
	public TrnDropdown(){
	}
	
	//---------------getter/setter for sort order----------------------//
	public void setSortOrder(Integer sortOrder){
		this.set(SORT_ORDER, sortOrder);
	}
	
	public Integer getSortOrder(){
		if(this.getPropertyNames().contains(SORT_ORDER))
			return (Integer) this.get(SORT_ORDER);
		return 0;
	}
	//---------------getter/setter for Process Name--------------------//
	public void setProcessName(String processName){
		this.set(PROCESS_NAME, processName);
	}
	
	public String getProcessName(){
		return (String)this.get(PROCESS_NAME);
	}
	
	//----------------getter/setter for src sys id---------------------//
	public BusinessAreaClient getSrcBa(){
		if(this.getPropertyNames().contains(SRC_BA))
			return (BusinessAreaClient) this.get(SRC_BA);
		return null;
	}
	
	public void setSrcBa(BusinessAreaClient businessArea){
		this.set(SRC_BA, businessArea);
	}
	
	//----------------getter/setter for dropdown id--------------------//
	public Integer getDropdownId(){
		if(this.getPropertyNames().contains(DROPDOWN_ID))
			return (Integer)this.get(DROPDOWN_ID);
		return 0;
	}
	
	public void setDropdownId(Integer dropdownId){
		this.set(DROPDOWN_ID, dropdownId);
	}
}
