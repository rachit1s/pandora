package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for TypeDescriptor
public class TypeDescriptorClient extends TbitsModelData {
	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public TypeDescriptorClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String DESCRIPTOR = "descriptor";
	public static String FIELD_ID = "field_id";
	public static String IS_PRIMARY = "is_primary";
	public static String SYSTEM_ID = "system_id";
	public static String TYPE_ID = "type_id";

	// getter and setter methods for variable myDescriptor
	public String getDescriptor() {
		return (String) this.get(DESCRIPTOR);
	}

	public void setDescriptor(String myDescriptor) {
		this.set(DESCRIPTOR, myDescriptor);
	}

	// getter and setter methods for variable myFieldId
	public int getFieldId() {
		return (Integer) this.get(FIELD_ID);
	}

	public void setFieldId(int myFieldId) {
		this.set(FIELD_ID, myFieldId);
	}

	// getter and setter methods for variable myIsPrimary
	public boolean getIsPrimary() {
		return (Boolean) this.get(IS_PRIMARY);
	}

	public void setIsPrimary(boolean myIsPrimary) {
		this.set(IS_PRIMARY, myIsPrimary);
	}

	// getter and setter methods for variable mySystemId
	public int getSystemId() {
		return (Integer) this.get(SYSTEM_ID);
	}

	public void setSystemId(int mySystemId) {
		this.set(SYSTEM_ID, mySystemId);
	}

	// getter and setter methods for variable myTypeId
	public int getTypeId() {
		return (Integer) this.get(TYPE_ID);
	}

	public void setTypeId(int myTypeId) {
		this.set(TYPE_ID, myTypeId);
	}

}