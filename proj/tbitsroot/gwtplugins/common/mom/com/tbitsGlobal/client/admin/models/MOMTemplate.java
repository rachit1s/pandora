package mom.com.tbitsGlobal.client.admin.models;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

public class MOMTemplate extends TbitsModelData {
	public static String MOM_BA			= "momBa";
	public static String MOM_FIELD		= "momField";
	public static String MOM_TYPE_ID	= "momTypeId";
	public static String IS_MEETING		= "isMeeting";
	public static String MOM_TEMPLATE	= "momTemplate";
	
	public MOMTemplate(){}
	
	//-----------getter/setter for momTemplate----------//
	public String getTemplate(){
		return (String) this.get(MOM_TEMPLATE);
	}
	
	public void setTemplate(String template){
		this.set(MOM_TEMPLATE, template);
	}
	
	//-----------getter/setter for isMeeting------------//
	public Integer getIsMeeting(){
		return (Integer) this.get(IS_MEETING);
	}
	
	public void setIsMeeting(Integer isMeeting){
		this.set(IS_MEETING, isMeeting);
	}
	
	//-----------getter/setter for type-----------------//
	public void setTypeId(Integer typeId){
		this.set(MOM_TYPE_ID, typeId);
	}
	
	public Integer getTypeId(){
		return (Integer) get(MOM_TYPE_ID);
	}
	
	//-----------getter/setter for field----------------//
	public void setField(BAField field){
		this.set(MOM_FIELD, field);
	}
	
	public BAField getField(){
		return (BAField) this.get(MOM_FIELD);
	}
	//-----------getter/setter for ba-------------------//
	public BusinessAreaClient getBa(){
		return (BusinessAreaClient) this.get(MOM_BA);
	}
	
	public void setBa(BusinessAreaClient ba){
		this.set(MOM_BA, ba);
	}
}
