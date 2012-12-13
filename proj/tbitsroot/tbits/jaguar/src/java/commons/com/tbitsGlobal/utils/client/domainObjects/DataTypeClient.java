package commons.com.tbitsGlobal.utils.client.domainObjects;

import java.util.HashMap;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for DataType
public class DataTypeClient extends TbitsModelData {

	public static final int DATE = 2;
	public static final int BOOLEAN = 1;
	public static final int TIME = 3;
	public static final int TEXT = 8;
	public static final int STRING = 7;
	public static final int REAL = 6;
	public static final int INT = 5;
	public static final int DATETIME = 4;
	public static final int TYPE = 9;
	public static final int MULTI_VALUE = 10;
	public static final int ATTACHMENTS = 11;

	// default constructor
	public DataTypeClient() {
		super();
	}
	
	public static HashMap<Integer, String> dataTypeMap = new HashMap<Integer, String>();
	
	public static HashMap<Integer, String> getDataTypeMap() {
		if (dataTypeMap.keySet().size() == 0) {
			dataTypeMap.put(DATE, "date");
			dataTypeMap.put(BOOLEAN, "bit");
			dataTypeMap.put(TIME, "time");
			dataTypeMap.put(TEXT, "text");
			dataTypeMap.put(STRING, "string");
			dataTypeMap.put(REAL, "real");
			dataTypeMap.put(INT, "int");
			dataTypeMap.put(DATETIME, "date-time");
			dataTypeMap.put(TYPE, "type");
			dataTypeMap.put(MULTI_VALUE, "multi-value");
			dataTypeMap.put(ATTACHMENTS, "attachments");
		}
		return dataTypeMap;

	}

	// Static Strings defining keys for corresponding variable
	public static String DATA_TYPE = "data_type";
	public static String DATA_TYPE_ID = "data_type_id";
	public static String DESCRIPTION = "description";

	// getter and setter methods for variable myDataType
	public String getDataType() {
		return (String) this.get(DATA_TYPE);
	}

	public void setDataType(String myDataType) {
		this.set(DATA_TYPE, myDataType);
	}

	// getter and setter methods for variable myDataTypeId
	public int getDataTypeId() {
		return (Integer) this.get(DATA_TYPE_ID);
	}

	public void setDataTypeId(int myDataTypeId) {
		this.set(DATA_TYPE_ID, myDataTypeId);
	}

	// getter and setter methods for variable myDescription
	public String getDescription() {
		return (String) this.get(DESCRIPTION);
	}

	public void setDescription(String myDescription) {
		this.set(DESCRIPTION, myDescription);
	}

}