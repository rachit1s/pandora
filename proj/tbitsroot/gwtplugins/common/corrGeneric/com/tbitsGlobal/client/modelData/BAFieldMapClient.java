package corrGeneric.com.tbitsGlobal.client.modelData;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;

/**
 * POJO for BA Field Map view
 * @author devashish
 *
 */
public class BAFieldMapClient extends TbitsModelData {
	
	public static String ID = "Id";
	public static String FROM_FIELD = "fromFieldName";
	public static String TO_FIELD	= "toFieldName";
	public static String FROM_SYSPREFIX = "fromSysprefix";
	public static String TO_SYSPREFIX = "toSysprefix";
	
	public BAFieldMapClient(){
		super();
	}

	//---------getter/setter for id---------------------//
	public void setID(String id){
		this.set(ID, id);
	}
	
	public String getId(){
		return (String) this.get(ID);
	}
	//---------getter/setter for from field--------------//
	public void setFromField(FieldClient fromField){
		this.set(FROM_FIELD, fromField);
	}
	
	public FieldClient getFromField(){
		return (FieldClient) this.get(FROM_FIELD);
	}
	
	//---------getter/setter for to field--------------//
	public void setToField(FieldClient toField){
		this.set(TO_FIELD, toField);
	}
	
	public FieldClient getToField(){
		return (FieldClient) this.get(TO_FIELD);
	}
	
	//------------gettter/setter for from sysprefix-----//
	public void setFromSysprefix(String sysPrefix){
		this.set(FROM_SYSPREFIX, sysPrefix);
	}
	
	public String getFromSysprefix(){
		return (String) get(FROM_SYSPREFIX);
	}
	
	//-----------getter/setter for to sysprefix-----------//
	public void setToSysPrefix(String sysPrefix){
		this.set(TO_SYSPREFIX, sysPrefix);
	}
	
	public String getToSysprefix(){
		return (String) get(TO_SYSPREFIX);
	}
}
