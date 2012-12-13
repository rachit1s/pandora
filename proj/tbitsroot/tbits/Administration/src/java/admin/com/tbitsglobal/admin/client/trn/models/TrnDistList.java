package admin.com.tbitsglobal.admin.client.trn.models;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class TrnDistList extends TbitsModelData{
	public static String PROCESS_ID		=	"process_id";
	public static String NAME			=	"name";
	public static String DISPLAY_NAME	=	"display_name";
	public static String DATA_TYPE_ID	=	"data_type_id";
	public static String FIELD_CONFIG	=	"field_config";
	public static String IS_EDITABLE	=	"is_editable";
	public static String IS_ACTIVE		=	"is_active";
	public static String COLUMN_ORDER	=	"column_order";
	
	public int getProcessId(){
		if(this.getPropertyNames().contains(PROCESS_ID))
			return (Integer)this.get(PROCESS_ID);
		return 0;
	}
	
	public void setProcessId(int processId){
		this.set(PROCESS_ID, processId);
	}
	
	public String getName(){
		return (String)this.get(NAME);
	}
	
	public void setName(String name){
		this.set(NAME, name);
	}
	
	public String getDisplayName(){
		return (String)this.get(DISPLAY_NAME);
	}
	
	public void setDisplayName(String displayName){
		this.set(DISPLAY_NAME, displayName);
	}
	
	public int getDataType(){
		return (Integer)this.get(DATA_TYPE_ID);
	}
	
	public void setDataType(int dataTypeId){
		this.set(DATA_TYPE_ID, dataTypeId);
	}
	
	public String getFieldConfig(){
		return (String)this.get(FIELD_CONFIG);
	}
	
	public void setFieldConfig(String fieldConfig){
		this.set(FIELD_CONFIG, fieldConfig);
	}
	
	public boolean getIsEditable(){
		return (Boolean)this.get(IS_EDITABLE);
	}
	
	public void setIsEditable(boolean isEditable){
		this.set(IS_EDITABLE, isEditable);
	}
	
	public boolean getIsActive(){
		return (Boolean)this.get(IS_ACTIVE);
	}
	
	public void setIsActive(boolean isActive){
		this.set(IS_ACTIVE, isActive);
	}
	
	public int getOrder(){
		return (Integer)this.get(COLUMN_ORDER);
	}
	
	public void setOrder(int order){
		this.set(COLUMN_ORDER, order);
	}
}
