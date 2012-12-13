package transmittal.com.tbitsGlobal.client.models;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

public class TrnValidationRule extends TbitsModelData {

	public static String SRC_BA		= "srcBa";
	public static String PROCESS	= "trnProcess";
	public static String FIELD		= "field";
	public static String RULE_VALUE	= "ruleValue";
	
	public TrnValidationRule(){}
	//-------------------getter/setter for srcBa----------------------//
	public BusinessAreaClient getSrcBa(){
		return (BusinessAreaClient) this.get(SRC_BA);
	}
	
	public void setSrcBa(BusinessAreaClient srcBa){
		this.set(SRC_BA, srcBa);
	}
	
	//-------------------getter/setter for rule-----------------------//
	public String getRule(){
		return (String) this.get(RULE_VALUE);
	}
	
	public void setRule(String rule){
		this.set(RULE_VALUE, rule);
	}
	//-------------------getter/setter for field----------------------//
	public BAField getField(){
		return (BAField) this.get(FIELD);
	}
	
	public void setField(BAField field){
		this.set(FIELD, field);
	}
	
	//-------------------getter/setter for process--------------------//
	public TrnProcess getProcess(){
		return (TrnProcess) this.get(PROCESS);
	}
	
	public void setProcess(TrnProcess process){
		this.set(PROCESS, process);
	}
 }
