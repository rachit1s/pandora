package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for Request
public class RequestClient extends TbitsModelData {

	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public RequestClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String APPEND_INTERFACE = "append_interface";
	public static String ASSIGNEES = "assignees";
	public static String ATTACHMENTS = "attachments";
	public static String CATEGORY_ID = "category_id";
	public static String CCS = "ccs";
	public static String DESCRIPTION = "description";
	public static String DESCRIPTION_CONTENT_TYPE = "description_content_type";
	// public static String DUE_DATE = "due_date";
	public static String HEADER_DESCRIPTION = "header_description";
	public static String IS_PRIVATE = "is_private";
	// public static String LAST_UPDATED_DATE = "last_updated_date";
	// public static String LOGGED_DATE = "logged_date";
	public static String LOGGERS = "loggers";
	public static String MAX_ACTION_ID = "max_action_id";
	public static String MEMO = "memo";
	public static String NOTIFY = "notify";
	public static String NOTIFY_LOGGERS = "notify_loggers";
	public static String OFFICE_ID = "office_id";
	public static String PARENT_REQUEST_ID = "parent_request_id";
	public static String REPLIED_TO_ACTION = "replied_to_action";
	public static String REQUEST_ID = "request_id";
	public static String REQUEST_TYPE_ID = "request_type_id";
	public static String SEVERITY_ID = "severity_id";
	public static String STATUS_ID = "status_id";
	public static String SMS_ID = "sms_id";
	public static String SUBJECT = "subject";
	public static String SUBSCRIBERS = "subscribers";
	public static String SUMMARY = "summary";
	public static String SUMMARY_CONTENT_TYPE = "summary_content_type";
	public static String SYSTEM_ID = "system_id";
	public static String TOS = "tos";
	public static String USER_ID = "user_id";
	public static String VERSION = "version";

	// getter and setter methods for variable myAppendInterface
	public int getAppendInterface() {
		return (Integer) this.get(APPEND_INTERFACE);
	}

	public void setAppendInterface(int myAppendInterface) {
		this.set(APPEND_INTERFACE, myAppendInterface);
	}

	// getter and setter methods for variable myAssignees
	// public ArrayList<RequestUser> getAssignees (){
	// return (ArrayList<RequestUser>) this.get(ASSIGNEES);
	// }
	// public void setAssignees(ArrayList<RequestUser> myAssignees) {
	// this.set(ASSIGNEES, myAssignees);
	// }

	// getter and setter methods for variable myAttachments
	// public Collection<AttachmentInfo> getAttachments (){
	// return (Collection<AttachmentInfo>) this.get(ATTACHMENTS);
	// }
	// public void setAttachments(Collection<AttachmentInfo> myAttachments) {
	// this.set(ATTACHMENTS, myAttachments);
	// }

	// getter and setter methods for variable myCategoryId
	// public Type getCategoryId (){
	// return (Type) this.get(CATEGORY_ID);
	// }
	// public void setCategoryId(Type myCategoryId) {
	// this.set(CATEGORY_ID, myCategoryId);
	// }

	// getter and setter methods for variable myCcs
	// public ArrayList<RequestUser> getCcs (){
	// return (ArrayList<RequestUser>) this.get(CCS);
	// }
	// public void setCcs(ArrayList<RequestUser> myCcs) {
	// this.set(CCS, myCcs);
	// }

	// getter and setter methods for variable myDescription
	public String getDescription() {
		return (String) this.get(DESCRIPTION);
	}

	public void setDescription(String myDescription) {
		this.set(DESCRIPTION, myDescription);
	}

	// getter and setter methods for variable myDescriptionContentType
	public int getDescriptionContentType() {
		return (Integer) this.get(DESCRIPTION_CONTENT_TYPE);
	}

	public void setDescriptionContentType(int myDescriptionContentType) {
		this.set(DESCRIPTION_CONTENT_TYPE, myDescriptionContentType);
	}

	// getter and setter methods for variable myDueDate
	// public Date getDueDate (){
	// return (Date) this.get(DUE_DATE);
	// }
	// public void setDueDate(Date myDueDate) {
	// this.set(DUE_DATE, myDueDate);
	// }

	// getter and setter methods for variable myHeaderDescription
	public String getHeaderDescription() {
		return (String) this.get(HEADER_DESCRIPTION);
	}

	public void setHeaderDescription(String myHeaderDescription) {
		this.set(HEADER_DESCRIPTION, myHeaderDescription);
	}

	// getter and setter methods for variable myIsPrivate
	public boolean getIsPrivate() {
		return (Boolean) this.get(IS_PRIVATE);
	}

	public void setIsPrivate(boolean myIsPrivate) {
		this.set(IS_PRIVATE, myIsPrivate);
	}

	// getter and setter methods for variable myLastUpdatedDate
	// public Date getLastUpdatedDate (){
	// return (Date) this.get(LAST_UPDATED_DATE);
	// }
	// public void setLastUpdatedDate(Date myLastUpdatedDate) {
	// this.set(LAST_UPDATED_DATE, myLastUpdatedDate);
	// }

	// getter and setter methods for variable myLoggedDate
	// public Date getLoggedDate (){
	// return (Date) this.get(LOGGED_DATE);
	// }
	// public void setLoggedDate(Date myLoggedDate) {
	// this.set(LOGGED_DATE, myLoggedDate);
	// }

	// getter and setter methods for variable myLoggers
	// public ArrayList<RequestUser> getLoggers (){
	// return (ArrayList<RequestUser>) this.get(LOGGERS);
	// }
	// public void setLoggers(ArrayList<RequestUser> myLoggers) {
	// this.set(LOGGERS, myLoggers);
	// }

	// getter and setter methods for variable myMaxActionId
	public int getMaxActionId() {
		return (Integer) this.get(MAX_ACTION_ID);
	}

	public void setMaxActionId(int myMaxActionId) {
		this.set(MAX_ACTION_ID, myMaxActionId);
	}

	// getter and setter methods for variable myMemo
	public String getMemo() {
		return (String) this.get(MEMO);
	}

	public void setMemo(String myMemo) {
		this.set(MEMO, myMemo);
	}

	// getter and setter methods for variable myNotify
	public int getNotify() {
		return (Integer) this.get(NOTIFY);
	}

	public void setNotify(int myNotify) {
		this.set(NOTIFY, myNotify);
	}

	// getter and setter methods for variable myNotifyLoggers
	public boolean getNotifyLoggers() {
		return (Boolean) this.get(NOTIFY_LOGGERS);
	}

	public void setNotifyLoggers(boolean myNotifyLoggers) {
		this.set(NOTIFY_LOGGERS, myNotifyLoggers);
	}

	// getter and setter methods for variable myOfficeId
	// public Type getOfficeId (){
	// return (Type) this.get(OFFICE_ID);
	// }
	// public void setOfficeId(Type myOfficeId) {
	// this.set(OFFICE_ID, myOfficeId);
	// }

	// getter and setter methods for variable myParentRequestId
	public int getParentRequestId() {
		return (Integer) this.get(PARENT_REQUEST_ID);
	}

	public void setParentRequestId(int myParentRequestId) {
		this.set(PARENT_REQUEST_ID, myParentRequestId);
	}

	// getter and setter methods for variable myRepliedToAction
	public int getRepliedToAction() {
		return (Integer) this.get(REPLIED_TO_ACTION);
	}

	public void setRepliedToAction(int myRepliedToAction) {
		this.set(REPLIED_TO_ACTION, myRepliedToAction);
	}

	// getter and setter methods for variable myRequestId
	public int getRequestId() {
		return (Integer) this.get(REQUEST_ID);
	}

	public void setRequestId(int myRequestId) {
		this.set(REQUEST_ID, myRequestId);
	}

	// getter and setter methods for variable myRequestTypeId
	// public Type getRequestTypeId (){
	// return (Type) this.get(REQUEST_TYPE_ID);
	// }
	// public void setRequestTypeId(Type myRequestTypeId) {
	// this.set(REQUEST_TYPE_ID, myRequestTypeId);
	// }
	//
	//
	// //getter and setter methods for variable mySeverityId
	// public Type getSeverityId (){
	// return (Type) this.get(SEVERITY_ID);
	// }
	// public void setSeverityId(Type mySeverityId) {
	// this.set(SEVERITY_ID, mySeverityId);
	// }
	//
	//
	// //getter and setter methods for variable myStatusId
	// public Type getStatusId (){
	// return (Type) this.get(STATUS_ID);
	// }
	// public void setStatusId(Type myStatusId) {
	// this.set(STATUS_ID, myStatusId);
	// }

	// getter and setter methods for variable smsId
	public int getSmsId() {
		return (Integer) this.get(SMS_ID);
	}

	public void setSmsId(int smsId) {
		this.set(SMS_ID, smsId);
	}

	// getter and setter methods for variable mySubject
	public String getSubject() {
		return (String) this.get(SUBJECT);
	}

	public void setSubject(String mySubject) {
		this.set(SUBJECT, mySubject);
	}

	// getter and setter methods for variable mySubscribers
	// public ArrayList<RequestUser> getSubscribers (){
	// return (ArrayList<RequestUser>) this.get(SUBSCRIBERS);
	// }
	// public void setSubscribers(ArrayList<RequestUser> mySubscribers) {
	// this.set(SUBSCRIBERS, mySubscribers);
	// }

	// getter and setter methods for variable mySummary
	public String getSummary() {
		return (String) this.get(SUMMARY);
	}

	public void setSummary(String mySummary) {
		this.set(SUMMARY, mySummary);
	}

	// getter and setter methods for variable mySummaryContentType
	public int getSummaryContentType() {
		return (Integer) this.get(SUMMARY_CONTENT_TYPE);
	}

	public void setSummaryContentType(int mySummaryContentType) {
		this.set(SUMMARY_CONTENT_TYPE, mySummaryContentType);
	}

	// getter and setter methods for variable mySystemId
	public int getSystemId() {
		return (Integer) this.get(SYSTEM_ID);
	}

	public void setSystemId(int mySystemId) {
		this.set(SYSTEM_ID, mySystemId);
	}

	// getter and setter methods for variable myTos
	// public ArrayList<RequestUser> getTos (){
	// return (ArrayList<RequestUser>) this.get(TOS);
	// }
	// public void setTos(ArrayList<RequestUser> myTos) {
	// this.set(TOS, myTos);
	// }
	//
	//
	// //getter and setter methods for variable myUserId
	// public User getUserId (){
	// return (User) this.get(USER_ID);
	// }
	// public void setUserId(User myUserId) {
	// this.set(USER_ID, myUserId);
	// }

	// getter and setter methods for variable version
	public int getVersion() {
		return (Integer) this.get(VERSION);
	}

	public void setVersion(int version) {
		this.set(VERSION, version);
	}

}