package admin.com.tbitsglobal.admin.client.trn.models;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;

public class TrnAttachmentList extends TbitsModelData{
	public static String PROCESS_ID		=	"process_id";
	public static String NAME			=	"name";
	public static String FIELD			=	"field_id";
	public static String DATA_TYPE_ID	=	"data_type_id";
	public static String DEFAULT_VALUE	=	"default_value";
	public static String IS_EDITABLE	=	"is_editable";
	public static String IS_ACTIVE		=	"is_active";
	public static String COLUMN_ORDER	=	"column_order";
	public static String TYPE_VALUE_SOURCE	=	"type_value_source";
	public static String IS_INCLUDED	=	"is_included";
	
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
	
	public BAField getField(){
		return (BAField)this.get(FIELD);
	}
	
	public void setField(BAField field){
		this.set(FIELD, field);
	}
	
	public int getDataType(){
		return (Integer)this.get(DATA_TYPE_ID);
	}
	
	public void setDataType(int dataTypeId){
		this.set(DATA_TYPE_ID, dataTypeId);
	}
	
	public String getDefaultValue(){
		return (String)this.get(DEFAULT_VALUE);
	}
	
	public void setDefaultValue(String defaultValue){
		this.set(DEFAULT_VALUE, defaultValue);
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
	
	public int getTypeValueSource(){
		return (Integer)this.get(TYPE_VALUE_SOURCE);
	}
	
	public void setTypeValueSource(int typeValueSource){
		this.set(TYPE_VALUE_SOURCE, typeValueSource);
	}
	
	public boolean getIsIncluded(){
		return (Boolean)this.get(IS_INCLUDED);
	}
	
	public void setIsIncluded(boolean isIncluded){
		this.set(IS_INCLUDED, isIncluded);
	}
}
