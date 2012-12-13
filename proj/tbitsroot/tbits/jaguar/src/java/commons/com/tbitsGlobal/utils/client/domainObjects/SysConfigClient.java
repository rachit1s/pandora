package commons.com.tbitsGlobal.utils.client.domainObjects;

import java.util.ArrayList;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for SysConfig
public class SysConfigClient extends TbitsModelData {

	private static final long serialVersionUID = 1L;

	//Defining useless variables for datatypes which needs to be serialized
	private CustomLinkClient customLink;
	private long l;
	private int i;
	private boolean b;
	private String s; 		//funny
	
	
	
	// default constructor
	public SysConfigClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String MY_ACTION_NOTIFY = "my_action_notify";
	public static String MY_ACTION_NOTIFY_LOGGERS = "my_action_notify_loggers";
	public static String MY_ADMINISTRATOR = "my_administrator";
	public static String MY_ALLOW_NULL_DUE_DATE = "my_allow_null_due_date";
	public static String MY_ASSIGN_TO_ALL = "my_assign_to_all";
	public static String MY_BMP2_PNG = "my_bmp2_png";
	public static String MY_CUSTOM_LINKS = "my_custom_links";
	public static String MY_DEFAULT_DUE_DATE = "my_default_due_date";
	public static String MY_EMAIL_DATE_FORMAT = "my_email_date_format";
	public static String MY_EMAIL_STYLESHEET = "my_email_stylesheet";
	public static String MY_INCOMING_SEVERITY_HIGH = "my_incoming_severity_high";
	public static String MY_INCOMING_SEVERITY_LOW = "my_incoming_severity_low";
	public static String MY_IS_DUE_DATE_DISABLED = "my_is_due_date_disabled";
	public static String MY_IS_TIME_DISABLED = "my_is_time_disabled";
	public static String MY_LEGACY_PREFIX_LIST = "my_legacy_prefix_list";
	public static String MY_LIST_DATE_FORMAT = "my_list_date_format";
	public static String MY_MAIL_FORMAT = "my_mail_format";
	public static String MY_NOTIFY_APPENDER = "my_notify_appender";
	public static String MY_OUTGOING_SEVERITY_HIGH = "my_outgoing_severity_high";
	public static String MY_OUTGOING_SEVERITY_LOW = "my_outgoing_severity_low";
	public static String MY_PREFERRED_ZONE = "my_preferred_zone";
	public static String MY_REQUEST_NOTIFY = "my_request_notify";
	public static String MY_REQUEST_NOTIFY_LOGGERS = "my_request_notify_loggers";
	public static String MY_VOLUNTEER = "my_volunteer";
	public static String MY_WEB_STYLESHEET = "my_web_stylesheet";

	// getter and setter methods for variable myActionNotify
	public int getActionNotify() {
		return (Integer) this.get(MY_ACTION_NOTIFY);
	}

	public void setActionNotify(int myActionNotify) {
		this.set(MY_ACTION_NOTIFY, myActionNotify);
	}

	// getter and setter methods for variable myActionNotifyLoggers
	public boolean getActionNotifyLoggers() {
		return (Boolean) this.get(MY_ACTION_NOTIFY_LOGGERS);
	}

	public void setActionNotifyLoggers(boolean myActionNotifyLoggers) {
		this.set(MY_ACTION_NOTIFY_LOGGERS, myActionNotifyLoggers);
	}

	// getter and setter methods for variable myAdministrator
	public String getAdministrator() {
		return (String) this.get(MY_ADMINISTRATOR);
	}

	public void setAdministrator(String myAdministrator) {
		this.set(MY_ADMINISTRATOR, myAdministrator);
	}

	// getter and setter methods for variable myAllowNullDueDate
	public boolean getAllowNullDueDate() {
		return (Boolean) this.get(MY_ALLOW_NULL_DUE_DATE);
	}

	public void setAllowNullDueDate(boolean myAllowNullDueDate) {
		this.set(MY_ALLOW_NULL_DUE_DATE, myAllowNullDueDate);
	}

	// getter and setter methods for variable myAssignToAll
	public boolean getAssignToAll() {
		return (Boolean) this.get(MY_ASSIGN_TO_ALL);
	}

	public void setAssignToAll(boolean myAssignToAll) {
		this.set(MY_ASSIGN_TO_ALL, myAssignToAll);
	}

	// getter and setter methods for variable myBmp2Png
	public boolean getBmp2Png() {
		return (Boolean) this.get(MY_BMP2_PNG);
	}

	public void setBmp2Png(boolean myBmp2Png) {
		this.set(MY_BMP2_PNG, myBmp2Png);
	}

	// getter and setter methods for variable myCustomLinks
	@SuppressWarnings("unchecked")
	public ArrayList<CustomLinkClient> getCustomLinks() {
		return (ArrayList<CustomLinkClient>) this.get(MY_CUSTOM_LINKS);
	}

	public void setCustomLinks(ArrayList<CustomLinkClient> myCustomLinks) {
		this.set(MY_CUSTOM_LINKS, myCustomLinks);
	}

	// getter and setter methods for variable myDefaultDueDate
	public long getDefaultDueDate() {
		return (Long) this.get(MY_DEFAULT_DUE_DATE);
	}

	public void setDefaultDueDate(long myDefaultDueDate) {
		this.set(MY_DEFAULT_DUE_DATE, myDefaultDueDate);
	}

	// getter and setter methods for variable myEmailDateFormat
	public int getEmailDateFormat() {
		return (Integer) this.get(MY_EMAIL_DATE_FORMAT);
	}

	public void setEmailDateFormat(int myEmailDateFormat) {
		this.set(MY_EMAIL_DATE_FORMAT, myEmailDateFormat);
	}

	// getter and setter methods for variable myEmailStylesheet
	public String getEmailStylesheet() {
		return (String) this.get(MY_EMAIL_STYLESHEET);
	}

	public void setEmailStylesheet(String myEmailStylesheet) {
		this.set(MY_EMAIL_STYLESHEET, myEmailStylesheet);
	}

	// getter and setter methods for variable myIncomingSeverityHigh
	public String getIncomingSeverityHigh() {
		return (String) this.get(MY_INCOMING_SEVERITY_HIGH);
	}

	public void setIncomingSeverityHigh(String myIncomingSeverityHigh) {
		this.set(MY_INCOMING_SEVERITY_HIGH, myIncomingSeverityHigh);
	}

	// getter and setter methods for variable myIncomingSeverityLow
	public String getIncomingSeverityLow() {
		return (String) this.get(MY_INCOMING_SEVERITY_LOW);
	}

	public void setIncomingSeverityLow(String myIncomingSeverityLow) {
		this.set(MY_INCOMING_SEVERITY_LOW, myIncomingSeverityLow);
	}

	// getter and setter methods for variable myIsDueDateDisabled
	public boolean getIsDueDateDisabled() {
		return (Boolean) this.get(MY_IS_DUE_DATE_DISABLED);
	}

	public void setIsDueDateDisabled(boolean myIsDueDateDisabled) {
		this.set(MY_IS_DUE_DATE_DISABLED, myIsDueDateDisabled);
	}

	// getter and setter methods for variable myIsTimeDisabled
	public boolean getIsTimeDisabled() {
		return (Boolean) this.get(MY_IS_TIME_DISABLED);
	}

	public void setIsTimeDisabled(boolean myIsTimeDisabled) {
		this.set(MY_IS_TIME_DISABLED, myIsTimeDisabled);
	}

	// getter and setter methods for variable myLegacyPrefixList
	@SuppressWarnings("unchecked")
	public ArrayList<String> getLegacyPrefixList() {
		return (ArrayList<String>) this.get(MY_LEGACY_PREFIX_LIST);
	}

	public void setLegacyPrefixList(ArrayList<String> myLegacyPrefixList) {
		this.set(MY_LEGACY_PREFIX_LIST, myLegacyPrefixList);
	}

	// getter and setter methods for variable myListDateFormat
	public int getListDateFormat() {
		return (Integer) this.get(MY_LIST_DATE_FORMAT);
	}

	public void setListDateFormat(int myListDateFormat) {
		this.set(MY_LIST_DATE_FORMAT, myListDateFormat);
	}

	// getter and setter methods for variable myMailFormat
	public int getMailFormat() {
		return (Integer) this.get(MY_MAIL_FORMAT);
	}

	public void setMailFormat(int myMailFormat) {
		this.set(MY_MAIL_FORMAT, myMailFormat);
	}

	// getter and setter methods for variable myNotifyAppender
	public boolean getNotifyAppender() {
		return (Boolean) this.get(MY_NOTIFY_APPENDER);
	}

	public void setNotifyAppender(boolean myNotifyAppender) {
		this.set(MY_NOTIFY_APPENDER, myNotifyAppender);
	}

	// getter and setter methods for variable myOutgoingSeverityHigh
	@SuppressWarnings("unchecked")
	public ArrayList<String> getOutgoingSeverityHigh() {
		return (ArrayList<String>) this.get(MY_OUTGOING_SEVERITY_HIGH);
	}

	public void setOutgoingSeverityHigh(ArrayList<String> myOutgoingSeverityHigh) {
		this.set(MY_OUTGOING_SEVERITY_HIGH, myOutgoingSeverityHigh);
	}

	// getter and setter methods for variable myOutgoingSeverityLow
	@SuppressWarnings("unchecked")
	public ArrayList<String> getOutgoingSeverityLow() {
		return (ArrayList<String>) this.get(MY_OUTGOING_SEVERITY_LOW);
	}

	public void setOutgoingSeverityLow(ArrayList<String> myOutgoingSeverityLow) {
		this.set(MY_OUTGOING_SEVERITY_LOW, myOutgoingSeverityLow);
	}

	// getter and setter methods for variable myPreferredZone
	public int getPreferredZone() {
		return (Integer) this.get(MY_PREFERRED_ZONE);
	}

	public void setPreferredZone(int myPreferredZone) {
		this.set(MY_PREFERRED_ZONE, myPreferredZone);
	}

	// getter and setter methods for variable myRequestNotify
	public int getRequestNotify() {
		return (Integer) this.get(MY_REQUEST_NOTIFY);
	}

	public void setRequestNotify(int myRequestNotify) {
		this.set(MY_REQUEST_NOTIFY, myRequestNotify);
	}

	// getter and setter methods for variable myRequestNotifyLoggers
	public boolean getRequestNotifyLoggers() {
		return (Boolean) this.get(MY_REQUEST_NOTIFY_LOGGERS);
	}

	public void setRequestNotifyLoggers(boolean myRequestNotifyLoggers) {
		this.set(MY_REQUEST_NOTIFY_LOGGERS, myRequestNotifyLoggers);
	}

	// getter and setter methods for variable myVolunteer
	public int getVolunteer() {
		return (Integer) this.get(MY_VOLUNTEER);
	}

	public void setVolunteer(int myVolunteer) {
		this.set(MY_VOLUNTEER, myVolunteer);
	}

	// getter and setter methods for variable myWebStylesheet
	public String getWebStylesheet() {
		return (String) this.get(MY_WEB_STYLESHEET);
	}

	public void setWebStylesheet(String myWebStylesheet) {
		this.set(MY_WEB_STYLESHEET, myWebStylesheet);
	}

}
