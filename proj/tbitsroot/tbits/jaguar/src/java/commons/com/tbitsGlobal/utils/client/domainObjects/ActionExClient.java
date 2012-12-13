package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for ActionEx
public class ActionExClient extends TbitsModelData {

	// default constructor
	public ActionExClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String ACTION_ID = "action_id";
	public static String BIT_VALUE = "bit_value";
	// public static String DATE_TIME_VALUE = "date_time_value";
	public static String INT_VALUE = "int_value";
	public static String REAL_VALUE = "real_value";
	public static String REQUEST_ID = "request_id";
	public static String SYSTEM_ID = "system_id";
	public static String TEXT_VALUE = "text_value";
	public static String TEXT_VALUE_CONTENT_TYPE = "text_value_content_type";
	public static String TYPE_VALUE = "type_value";
	public static String FIELD_ID = "field_id";
	public static String VARCHAR_VALUE = "varchar_value";

	// getter and setter methods for variable myActionId
	public int getActionId() {
		return (Integer) this.get(ACTION_ID);
	}

	public void setActionId(int myActionId) {
		this.set(ACTION_ID, myActionId);
	}

	// getter and setter methods for variable myBitValue
	public boolean getBitValue() {
		return (Boolean) this.get(BIT_VALUE);
	}

	public void setBitValue(boolean myBitValue) {
		this.set(BIT_VALUE, myBitValue);
	}

	// getter and setter methods for variable myDateTimeValue
	// public Date getDateTimeValue (){
	// return (Date) this.get(DATE_TIME_VALUE);
	// }
	// public void setDateTimeValue(Date myDateTimeValue) {
	// this.set(DATE_TIME_VALUE, myDateTimeValue);
	// }

	// getter and setter methods for variable myIntValue
	public int getIntValue() {
		return (Integer) this.get(INT_VALUE);
	}

	public void setIntValue(int myIntValue) {
		this.set(INT_VALUE, myIntValue);
	}

	// getter and setter methods for variable myRealValue
	public double getRealValue() {
		return (Double) this.get(REAL_VALUE);
	}

	public void setRealValue(double myRealValue) {
		this.set(REAL_VALUE, myRealValue);
	}

	// getter and setter methods for variable myRequestId
	public int getRequestId() {
		return (Integer) this.get(REQUEST_ID);
	}

	public void setRequestId(int myRequestId) {
		this.set(REQUEST_ID, myRequestId);
	}

	// getter and setter methods for variable mySystemId
	public int getSystemId() {
		return (Integer) this.get(SYSTEM_ID);
	}

	public void setSystemId(int mySystemId) {
		this.set(SYSTEM_ID, mySystemId);
	}

	// getter and setter methods for variable myTextValue
	public String getTextValue() {
		return (String) this.get(TEXT_VALUE);
	}

	public void setTextValue(String myTextValue) {
		this.set(TEXT_VALUE, myTextValue);
	}

	// getter and setter methods for variable myTextValueContentType
	public int getTextValueContentType() {
		return (Integer) this.get(TEXT_VALUE_CONTENT_TYPE);
	}

	public void setTextValueContentType(int myTextValueContentType) {
		this.set(TEXT_VALUE_CONTENT_TYPE, myTextValueContentType);
	}

	// getter and setter methods for variable myTypeValue
	public int getTypeValue() {
		return (Integer) this.get(TYPE_VALUE);
	}

	public void setTypeValue(int myTypeValue) {
		this.set(TYPE_VALUE, myTypeValue);
	}

	// getter and setter methods for variable myFieldId
	public int getFieldId() {
		return (Integer) this.get(FIELD_ID);
	}

	public void setFieldId(int myFieldId) {
		this.set(FIELD_ID, myFieldId);
	}

	// getter and setter methods for variable myVarcharValue
	public String getVarcharValue() {
		return (String) this.get(VARCHAR_VALUE);
	}

	public void setVarcharValue(String myVarcharValue) {
		this.set(VARCHAR_VALUE, myVarcharValue);
	}

}