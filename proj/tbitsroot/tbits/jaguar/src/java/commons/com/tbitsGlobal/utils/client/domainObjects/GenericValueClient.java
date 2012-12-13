package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for GenericValue
public class GenericValueClient extends TbitsModelData {

	// default constructor
	public GenericValueClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	// public static String _DATE_VALUE = "_date_value";
	public static String _DOUBLE_VALUE = "_double_value";

	// getter and setter methods for variable DateValue
	// public Date getDateValue (){
	// return (Date) this.get(_DATE_VALUE);
	// }
	// public void setDateValue(Date DateValue) {
	// this.set(_DATE_VALUE, DateValue);
	// }

	// getter and setter methods for variable DoubleValue
	public double getDoubleValue() {
		return (Double) this.get(_DOUBLE_VALUE);
	}

	public void setDoubleValue(double DoubleValue) {
		this.set(_DOUBLE_VALUE, DoubleValue);
	}

}