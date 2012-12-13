package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for CustomLink
public class CustomLinkClient extends TbitsModelData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// default constructor
	public CustomLinkClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String MY_NAME = "my_name";
	public static String MY_VALUE = "my_value";

	// getter and setter methods for variable myName
	public String getName() {
		return (String) this.get(MY_NAME);
	}

	public void setName(String myName) {
		this.set(MY_NAME, myName);
	}

	// getter and setter methods for variable myValue
	public String getValue() {
		return (String) this.get(MY_VALUE);
	}

	public void setValue(String myValue) {
		this.set(MY_VALUE, myValue);
	}

}