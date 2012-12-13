package transmittal.com.tbitsGlobal.client.models;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

public class TrnFieldMapping extends TbitsModelData{
	public static String SRC_BA			=	"src_ba";
	public static String SRC_FIELD		=	"src_field";
	public static String PROCESS_ID		=	"process_id";
	public static String TARGET_BA		=	"target_ba";
	public static String TARGET_FIELD	=	"target_field";
	
	
	public int getProcessId(){
		if(this.getPropertyNames().contains(PROCESS_ID))
			return (Integer)this.get(PROCESS_ID);
		return 0;
	}
	
	public void setProcessId(int processId){
		this.set(PROCESS_ID, processId);
	}
	
	public BusinessAreaClient getSrcBA(){
		return (BusinessAreaClient)this.get(SRC_BA);
	}
	
	public void setSrcBA(BusinessAreaClient srcBA){
		this.set(SRC_BA, srcBA);
	}
	
	public BAField getSrcField(){
		return (BAField)this.get(SRC_FIELD);
	}
	
	public void setSrcField(BAField srcField){
		this.set(SRC_FIELD, srcField);
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
}
