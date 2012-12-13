package transmittal.com.tbitsGlobal.client.models;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

public class TrnPostProcessValue extends TbitsModelData{
	public static String SRC_BA			=	"src_ba";
	public static String PROCESS_ID		=	"process_id";
	public static String TARGET_BA		=	"target_ba";
	public static String TARGET_FIELD	=	"target_field";
	public static String TARGET_VALUE	=	"target_field_value";
	public static String TEMP			=	"temp";
	
	public BusinessAreaClient getSrcBA(){
		return (BusinessAreaClient)this.get(SRC_BA);
	}
	
	public void setSrcBA(BusinessAreaClient srcBA){
		this.set(SRC_BA, srcBA);
	}
	
	public int getProcessId(){
		if(this.getPropertyNames().contains(PROCESS_ID))
			return (Integer)this.get(PROCESS_ID);
		return 0;
	}
	
	public void setProcessId(int processId){
		this.set(PROCESS_ID, processId);
	}
	
	public BusinessAreaClient getTargetBA(){
		return (BusinessAreaClient)this.get(TARGET_BA);
	}
	
	public void setTargetBA(BusinessAreaClient targetBA){
		this.set(TARGET_BA, targetBA);
	}
	
	public BAField getTargetField(){
		return (BAField)this.get(TARGET_FIELD);
	}
	
	public void setTargetField(BAField targetField){
		this.set(TARGET_FIELD, targetField);
	}
	
	public String getTargetFieldValue(){
		return (String)this.get(TARGET_VALUE);
	}
	
	public void setTargetFieldValue(String targetFieldValue){
		this.set(TARGET_VALUE, targetFieldValue);
	}
	
	public int getTemp(){
		return (Integer)this.get(TEMP);
	}
	
	public void setTemp(int temp){
		this.set(TEMP, temp);
	}
}
