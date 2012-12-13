package transmittal.com.tbitsGlobal.client.models;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

/**
 * POJO for Transmittal Drawing Number Page
 * @author devashish
 *
 */
public class TrnDrawingNumber extends TbitsModelData {
	public static String SRC_BA 	= "businessArea";
	public static String FIELD_NAME	= "fieldName";
	
	
	public TrnDrawingNumber(){}
	
	//------------------getter/setter for source ba--------------//
	public BusinessAreaClient getSrcBa(){
		return (BusinessAreaClient) this.get(SRC_BA);
	}
	
	public void setSrcBa(BusinessAreaClient srcBa){
		this.set(SRC_BA, srcBa);
	}
	
	//-------------------getter/setter for fieldName-------------//
	public BAField getField(){
		return (BAField) this.get(FIELD_NAME);
	}
	
	public void setField(BAField field){
		this.set(FIELD_NAME, field);
	}
}
