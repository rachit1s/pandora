package transmittal.com.tbitsGlobal.client.models;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

/**
 * POJO for Showing all the values of already existing process or newly 
 * created process
 * @author devashish
 *
 */
public class TrnCreateProcess extends TbitsModelData {
	
	public static String PROCESS_ID = "processId";
	public static String SRC_BA		= "srcBa";
	public static String NAME		= "name";
	public static String VALUE		= "value";
	public static String GROUP		= "group";
	
	
	//---------------getter/setter for process id---------------//
	public Integer getProcessId(){
		return (Integer) this.get(PROCESS_ID);
	}
	
	public void setProcessId(Integer processId){
		this.set(PROCESS_ID, processId);
	}
	
	//----------------getter/setter for src ba-----------------//
	public BusinessAreaClient getSrcBa(){
		return (BusinessAreaClient) this.get(SRC_BA);
	}
	
	public void setSrcBa(BusinessAreaClient srcBa){
		this.set(SRC_BA, srcBa);
	}
	
	//---------------getter/setter for parameter name----------//
	public String getName(){
		return (String) this.get(NAME);
	}
	
	public void setName(String name){
		this.set(NAME, name);
	}
	
	//---------------getter/setter for parameter value---------//
	public String getValue(){
		return (String) this.get(VALUE);
	}
	
	public void setValue(String value){
		this.set(VALUE, value);
	}
	
	//--------------getter/setter for group--------------------//
	public String getGroup(){
		return (String) this.get(GROUP);
	}
	
	public void setGroup(String group){
		this.set(GROUP, group);
	}
}
