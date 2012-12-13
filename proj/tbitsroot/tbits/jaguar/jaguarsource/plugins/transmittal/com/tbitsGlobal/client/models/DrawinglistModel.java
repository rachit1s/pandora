package transmittal.com.tbitsGlobal.client.models;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

@SuppressWarnings("serial")
public class DrawinglistModel extends TbitsModelData {
	
	
	public static String FIELD_ID = "field_id";
	public static String FIELD_VALUE = "value";
	public static String TYPE_VAUE_SOURCE = "type_value_source";
	
	public  String getFIELD_ID() {
		return (String) this.get(FIELD_ID);
	}

	public  void setFIELD_ID(String FIELD_ID) {
		this.set(this.FIELD_ID, FIELD_ID);
		
		

	}
	
	public  String getFIELD_VALUE() {
		return (String) this.get(FIELD_VALUE);
	}

	public  void setFIELD_VALUE(String FIELD_VALUE) {
		this.set(this.FIELD_VALUE, FIELD_VALUE);
		
		

	}
	
	public  String getTYPE_VAUE_SOURCE() {
		return (String) this.get(TYPE_VAUE_SOURCE);
	}

	public  void setTYPE_VAUE_SOURCE(String TYPE_VAUE_SOURCE) {
		this.set(this.TYPE_VAUE_SOURCE, TYPE_VAUE_SOURCE);
		
		

	}

}
