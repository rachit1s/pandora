package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for NotificationRule
public class NotificationRuleClient extends TbitsModelData {

	// default constructor
	public NotificationRuleClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String DISPLAY_NAME = "display_name";
	public static String NAME = "name";
	public static String NOTIFICATION_CONFIG_OBJECT = "notification_config_object";
	public static String NOTIFICATION_RULE_ID = "notification_rule_id";
	public static String RULES_CONFIG = "rules_config";

	// getter and setter methods for variable myDisplayName
	public String getDisplayName() {
		return (String) this.get(DISPLAY_NAME);
	}

	public void setDisplayName(String myDisplayName) {
		this.set(DISPLAY_NAME, myDisplayName);
	}

	// getter and setter methods for variable myName
	public String getName() {
		return (String) this.get(NAME);
	}

	public void setName(String myName) {
		this.set(NAME, myName);
	}

	// getter and setter methods for variable myNotificationConfigObject
	// public NotificationConfig getNotificationConfigObject (){
	// return (NotificationConfig) this.get(NOTIFICATION_CONFIG_OBJECT);
	// }
	// public void setNotificationConfigObject(NotificationConfig
	// myNotificationConfigObject) {
	// this.set(NOTIFICATION_CONFIG_OBJECT, myNotificationConfigObject);
	// }

	// getter and setter methods for variable myNotificationRuleId
	public int getNotificationRuleId() {
		return (Integer) this.get(NOTIFICATION_RULE_ID);
	}

	public void setNotificationRuleId(int myNotificationRuleId) {
		this.set(NOTIFICATION_RULE_ID, myNotificationRuleId);
	}

	// getter and setter methods for variable myRulesConfig
	public String getRulesConfig() {
		return (String) this.get(RULES_CONFIG);
	}

	public void setRulesConfig(String myRulesConfig) {
		this.set(RULES_CONFIG, myRulesConfig);
	}

}