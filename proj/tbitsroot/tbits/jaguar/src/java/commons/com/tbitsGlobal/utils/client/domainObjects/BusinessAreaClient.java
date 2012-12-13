package commons.com.tbitsGlobal.utils.client.domainObjects;

import java.util.Date;

import com.extjs.gxt.ui.client.util.Format;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for BusinessArea
public class BusinessAreaClient extends TbitsModelData {
	// default constructor
	public BusinessAreaClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String DATE_CREATED = "date_created";
	public static String DESCRIPTION = "description";
	public static String DISPLAY_NAME = "display_name";
	public static String EMAIL = "email";
	public static String FIELD_CONFIG = "field_config";
	public static String IS_ACTIVE = "is_active";
	public static String IS_EMAIL_ACTIVE = "is_email_active";
	public static String IS_PRIVATE = "is_private";
	public static String LOCATION = "location";
	public static String MAX_EMAIL_ACTIONS = "max_email_actions";
	public static String MAX_REQUEST_ID = "max_request_id";
	public static String NAME = "name";
	public static String SYS_CONFIG = "sys_config";
	public static String SYS_CONFIG_OBJECT = "sys_config_object";
	public static String SYSTEM_ID = "system_id";
	public static String SYSTEM_PREFIX = "system_prefix";
	public static String TYPE = "type";

	// getter and setter methods for variable myDateCreated
	public Date getDateCreated (){
		return (Date) this.get(DATE_CREATED);
	}
	
	public void setDateCreated(Date myDateCreated) {
		this.set(DATE_CREATED, myDateCreated);
	}

	// getter and setter methods for variable myDescription
	public String getDescription() {
		return (String) this.get(DESCRIPTION);
	}

	public void setDescription(String myDescription) {
		this.set(DESCRIPTION, myDescription);
	}

	// getter and setter methods for variable myDisplayName
	public String getDisplayName() {
		return Format.htmlEncode((String) this.get(DISPLAY_NAME));
	}

	public void setDisplayName(String myDisplayName) {
		this.set(DISPLAY_NAME, myDisplayName);
	}

	// getter and setter methods for variable myEmail
	public String getEmail() {
		return (String) this.get(EMAIL);
	}

	public void setEmail(String myEmail) {
		this.set(EMAIL, myEmail);
	}

	// getter and setter methods for variable myFieldConfig
	public String getFieldConfig() {
		return (String) this.get(FIELD_CONFIG);
	}

	public void setFieldConfig(String myFieldConfig) {
		this.set(FIELD_CONFIG, myFieldConfig);
	}

	// getter and setter methods for variable myIsActive
	public boolean getIsActive() {
		return (Boolean) this.get(IS_ACTIVE);
	}

	public void setIsActive(boolean myIsActive) {
		this.set(IS_ACTIVE, myIsActive);
	}

	// getter and setter methods for variable myIsEmailActive
	public boolean getIsEmailActive() {
		return (Boolean) this.get(IS_EMAIL_ACTIVE);
	}

	public void setIsEmailActive(boolean myIsEmailActive) {
		this.set(IS_EMAIL_ACTIVE, myIsEmailActive);
	}

	// getter and setter methods for variable myIsPrivate
	public boolean getIsPrivate() {
		return (Boolean) this.get(IS_PRIVATE);
	}

	public void setIsPrivate(boolean myIsPrivate) {
		this.set(IS_PRIVATE, myIsPrivate);
	}

	// getter and setter methods for variable myLocation
	public String getLocation() {
		return (String) this.get(LOCATION);
	}

	public void setLocation(String myLocation) {
		this.set(LOCATION, myLocation);
	}

	// getter and setter methods for variable myMaxEmailActions
	public int getMaxEmailActions() {
		return (Integer) this.get(MAX_EMAIL_ACTIONS);
	}

	public void setMaxEmailActions(int myMaxEmailActions) {
		this.set(MAX_EMAIL_ACTIONS, myMaxEmailActions);
	}

	// getter and setter methods for variable myMaxRequestId
	public int getMaxRequestId() {
		return (Integer) this.get(MAX_REQUEST_ID);
	}

	public void setMaxRequestId(int myMaxRequestId) {
		this.set(MAX_REQUEST_ID, myMaxRequestId);
	}

	// getter and setter methods for variable myName
	public String getName() {
		return (String) this.get(NAME);
	}

	public void setName(String myName) {
		this.set(NAME, myName);
	}

	// getter and setter methods for variable mySysConfig
	public String getSysConfig() {
		return (String) this.get(SYS_CONFIG);
	}

	public void setSysConfig(String mySysConfig) {
		this.set(SYS_CONFIG, mySysConfig);
	}

//	 getter and setter methods for variable mySysConfigObject
	public SysConfigClient getSysConfigObject (){
		return (SysConfigClient) this.get(SYS_CONFIG_OBJECT);
	}
	
	public void setSysConfigObject(SysConfigClient mySysConfigObject) {
		this.set(SYS_CONFIG_OBJECT, mySysConfigObject);
	}

	// getter and setter methods for variable mySystemId
	public int getSystemId() {
		return (Integer) this.get(SYSTEM_ID);
	}

	public void setSystemId(int mySystemId) {
		this.set(SYSTEM_ID, mySystemId);
	}

	// getter and setter methods for variable mySystemPrefix
	public String getSystemPrefix() {
		return (String) this.get(SYSTEM_PREFIX);
	}

	public void setSystemPrefix(String mySystemPrefix) {
		this.set(SYSTEM_PREFIX, mySystemPrefix);
	}

	// getter and setter methods for variable myType
	public String getType() {
		return (String) this.get(TYPE);
	}

	public void setType(String myType) {
		this.set(TYPE, myType);
	}
	
	public String getDisplayText(){
		return this.getDisplayName() + " [" + this.getSystemPrefix() +  "]";
	}

	public int compareTo(BusinessAreaClient arg0) {
		return this.getSystemId() - arg0.getSystemId();
	}

}