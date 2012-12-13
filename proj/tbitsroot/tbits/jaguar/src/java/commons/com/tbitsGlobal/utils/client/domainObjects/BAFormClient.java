package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for BAForm
public class BAFormClient extends TbitsModelData {

	// default constructor
	public BAFormClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String FORM_CONFIG = "form_config";
	public static String FORM_CONFIG_OBJECT = "form_config_object";
	public static String FORM_ID = "form_id";
	public static String NAME = "name";
	public static String SHORT_NAME = "short_name";
	public static String SYSTEM_ID = "system_id";
	public static String TITLE = "title";

	// getter and setter methods for variable myFormConfig
	public String getFormConfig() {
		return (String) this.get(FORM_CONFIG);
	}

	public void setFormConfig(String myFormConfig) {
		this.set(FORM_CONFIG, myFormConfig);
	}

	// getter and setter methods for variable myFormConfigObject
	// public FBForm getFormConfigObject (){
	// return (FBForm) this.get(FORM_CONFIG_OBJECT);
	// }
	// public void setFormConfigObject(FBForm myFormConfigObject) {
	// this.set(FORM_CONFIG_OBJECT, myFormConfigObject);
	// }

	// getter and setter methods for variable myFormId
	public int getFormId() {
		return (Integer) this.get(FORM_ID);
	}

	public void setFormId(int myFormId) {
		this.set(FORM_ID, myFormId);
	}

	// getter and setter methods for variable myName
	public String getName() {
		return (String) this.get(NAME);
	}

	public void setName(String myName) {
		this.set(NAME, myName);
	}

	// getter and setter methods for variable myShortName
	public String getShortName() {
		return (String) this.get(SHORT_NAME);
	}

	public void setShortName(String myShortName) {
		this.set(SHORT_NAME, myShortName);
	}

	// getter and setter methods for variable mySystemId
	public int getSystemId() {
		return (Integer) this.get(SYSTEM_ID);
	}

	public void setSystemId(int mySystemId) {
		this.set(SYSTEM_ID, mySystemId);
	}

	// getter and setter methods for variable myTitle
	public String getTitle() {
		return (String) this.get(TITLE);
	}

	public void setTitle(String myTitle) {
		this.set(TITLE, myTitle);
	}

}