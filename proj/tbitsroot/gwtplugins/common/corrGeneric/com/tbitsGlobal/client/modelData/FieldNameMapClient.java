package corrGeneric.com.tbitsGlobal.client.modelData;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;

public class FieldNameMapClient extends TbitsModelData {
	public static String ID = "id";
	public static String CORR_FIELD_NAME = "corrFieldName";
	public static String SYS_PREFIX	= "sysprefix";
	public static String FIELD_NAME	= "fieldName";
	
	public FieldNameMapClient(){
		super();
	}
	
	//-------------getter/setter for id------------//
	public String getId(){
		return (String) this.get(ID);
	}
	
	public void setID(String id){
		this.set(ID, id);
	}
	
	//---------getter/setter for corrfieldname---//
	public String getCorrFieldName(){
		return (String) this.get(CORR_FIELD_NAME);
	}
	
	public void setCorrFieldName(String corrFieldName){
		this.set(CORR_FIELD_NAME, corrFieldName);
	}
	
	//--------getter/setter for sysprefix-------//
	public String getSysprefix(){
		return (String) this.get(SYS_PREFIX);
	}
	
	public void setSysprefix(String sysprefix){
		this.set(SYS_PREFIX, sysprefix);
	}
	
	//--------getter/setter for field-------------//
	public FieldClient getField(){
		return (FieldClient) this.get(FIELD_NAME);
	}
	
	public void setField(FieldClient fieldName){
		this.set(FIELD_NAME, fieldName);
	}
}
