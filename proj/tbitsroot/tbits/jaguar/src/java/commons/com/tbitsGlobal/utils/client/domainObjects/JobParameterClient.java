package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for JobParameter
public class JobParameterClient extends TbitsModelData {



	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//default constructor
	public JobParameterClient (){
		super();
	}


	//Static Strings defining keys for corresponding variable
	public static String Integer = "Integer";
	public static String TextArea = "TextArea";
	public static String Select ="Select";
	public static String Text = "Text";
	public static String CheckBox = "CheckBox";
	
	 public static String NAME = "name";
	 public static String TYPE = "type";
	 public static String VALUES = "values";
	 public static String DEFAULT_VALUE = "default_value";
	 public static String IS_MANDATORY = "is_mandatory";
	 public static String IS_EXTENDED = "is_extended";

	//getter and setter methods for variable name
	public String getName (){
		return (String) this.get(NAME);
	}
	public void setName(String name) {
		 this.set(NAME, name);
	}


	//getter and setter methods for variable type
	public String getType(){
		return (String) this.get(TYPE);
	}
	public void setType(String type) {
		 this.set(TYPE, type);
	}


	//getter and setter methods for variable values
	public Object getValues (){
		if(this.get(VALUES) == null)
			return null;
		return (Object) this.get(VALUES);
	}
	public void setValues(Object values) {
		 this.set(VALUES, values);
	}


	//getter and setter methods for variable defaultValue
	public Object getDefaultValue (){
		return (Object) this.get(DEFAULT_VALUE);
	}
	public void setDefaultValue(Object defaultValue) {
		 this.set(DEFAULT_VALUE, defaultValue);
	}


	//getter and setter methods for variable isMandatory
	public boolean getIsMandatory (){
		if(this.get(IS_MANDATORY) == null)
			return false;
		return (Boolean) this.get(IS_MANDATORY);
	}
	public void setIsMandatory(boolean isMandatory) {
		 this.set(IS_MANDATORY, isMandatory);
	}
	
//	//getter and setter methods for variable isEXTENDED
//	public boolean getIsExtended (){
//		if(this.get(IS_EXTENDED) == null)
//			return false;
//		return (Boolean) this.get(IS_EXTENDED);
//	}
//	public void setIsExtended(boolean isExtended) {
//		 this.set(IS_EXTENDED, isExtended);
//	}

}