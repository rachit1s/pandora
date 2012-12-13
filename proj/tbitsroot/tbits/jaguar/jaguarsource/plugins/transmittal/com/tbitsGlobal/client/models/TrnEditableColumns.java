package transmittal.com.tbitsGlobal.client.models;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class TrnEditableColumns extends TbitsModelData {
	public static String ORDER = "column_order";
	public static String NAME = "name";
	public static String PROPERTY = "property";
	public static String DATATYPEID = "data_type_id";
	public static String FIELDID = "field_id";
	
	public void setORDER(Integer order) {
		this.set(ORDER, order);
	}

	public Integer getOrder() {

		return this.get(ORDER);

	}

	public void setNAME(String Name) {
		this.set(NAME, Name);
	}

	public String getNAME() {

		return this.get(NAME);

	}

	public void setPROPERTY(String property) {
		this.set(PROPERTY, property);
	}

	public String getPROPERTY() {

		return this.get(PROPERTY);

	}

	public void setDataTypeId(Integer datatypeid) {
		this.set(DATATYPEID, datatypeid);
	}

	public Integer getDataTypeId() {

		return this.get(DATATYPEID);

	}
	
	

	public void setFIELDID(Integer fieldid) {
		this.set(FIELDID, fieldid);
	}

	public Integer getFIELDID() {

		return this.get(FIELDID);

	}
}
