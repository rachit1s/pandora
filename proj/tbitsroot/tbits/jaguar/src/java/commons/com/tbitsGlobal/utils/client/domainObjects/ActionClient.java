package commons.com.tbitsGlobal.utils.client.domainObjects;

import java.util.ArrayList;
import java.util.Date;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for Action
public class ActionClient extends TbitsModelData {
	
	private UserClient actionUser;
	
	// default constructor
	public ActionClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String ACTION_ID = "action_id";
	public static String APPEND_INTERFACE = "append_interface";
	public static String ASSIGNEE_IDS = "assignee_ids";
	public static String ATTACHMENTS = "attachments";
	public static String CATEGORY_ID = "category_id";
	public static String CC_IDS = "cc_ids";
	public static String DESCRIPTION = "description";
	public static String DESCRIPTION_CONTENT_TYPE = "description_content_type";
	public static String DUE_DATE = "due_date";
	public static String HEADER_DESCRIPTION = "header_description";
	public static String IS_PRIVATE = "is_private";
	public static String LAST_UPDATED_DATE = "last_updated_date";
	public static String LOGGED_DATE = "logged_date";
	public static String LOGGER_IDS = "logger_ids";
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
	public static String SUBJECT = "subject";
	public static String SUBSCRIBER_IDS = "subscriber_ids";
	public static String SUMMARY = "summary";
	public static String SUMMARY_CONTENT_TYPE = "summary_content_type";
	public static String SYSTEM_ID = "system_id";
	public static String TO_IDS = "to_ids";
	public static String USER_ID = "user_id";
	
	public static String ATTACHMENT_HTML = "att_html";

	// getter and setter methods for variable myActionId
	public int getActionId() {
		return (Integer) this.get(ACTION_ID);
	}

	public void setActionId(int myActionId) {
		this.set(ACTION_ID, myActionId);
	}

	// getter and setter methods for variable myAppendInterface
	public int getAppendInterface() {
		return (Integer) this.get(APPEND_INTERFACE);
	}

	public void setAppendInterface(int myAppendInterface) {
		this.set(APPEND_INTERFACE, myAppendInterface);
	}

	// getter and setter methods for variable myAssigneeIds
	public ArrayList<Integer> getAssigneeIds() {
		return (ArrayList<Integer>) this.get(ASSIGNEE_IDS);
	}

	public void setAssigneeIds(ArrayList<Integer> myAssigneeIds) {
		this.set(ASSIGNEE_IDS, myAssigneeIds);
	}

	// getter and setter methods for variable myAttachments
	public String getAttachments() {
		return (String) this.get(ATTACHMENTS);
	}

	public void setAttachments(String myAttachments) {
		this.set(ATTACHMENTS, myAttachments);
	}

	// getter and setter methods for variable myCategoryId
	public int getCategoryId() {
		return (Integer) this.get(CATEGORY_ID);
	}

	public void setCategoryId(int myCategoryId) {
		this.set(CATEGORY_ID, myCategoryId);
	}

	// getter and setter methods for variable myCcIds
	public ArrayList<Integer> getCcIds() {
		return (ArrayList<Integer>) this.get(CC_IDS);
	}

	public void setCcIds(ArrayList<Integer> myCcIds) {
		this.set(CC_IDS, myCcIds);
	}

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

//	 getter and setter methods for variable myDueDate
	public Date getDueDate (){
		return (Date) this.get(DUE_DATE);
	}
	
	public void setDueDate(Date myDueDate) {
		this.set(DUE_DATE, myDueDate);
	}

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
	public Date getLastUpdatedDate (){
		return (Date) this.get(LAST_UPDATED_DATE);
	}
	public void setLastUpdatedDate(Date myLastUpdatedDate) {
		this.set(LAST_UPDATED_DATE, myLastUpdatedDate);
	}

	// getter and setter methods for variable myLoggedDate
	public Date getLoggedDate (){
		return (Date) this.get(LOGGED_DATE);
	}
	public void setLoggedDate(Date myLoggedDate) {
		this.set(LOGGED_DATE, myLoggedDate);
	}

	// getter and setter methods for variable myLoggerIds
	public ArrayList<Integer> getLoggerIds() {
		return (ArrayList<Integer>) this.get(LOGGER_IDS);
	}

	public void setLoggerIds(ArrayList<Integer> myLoggerIds) {
		this.set(LOGGER_IDS, myLoggerIds);
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
	public int getOfficeId() {
		return (Integer) this.get(OFFICE_ID);
	}

	public void setOfficeId(int myOfficeId) {
		this.set(OFFICE_ID, myOfficeId);
	}

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
	public int getRequestTypeId() {
		return (Integer) this.get(REQUEST_TYPE_ID);
	}

	public void setRequestTypeId(int myRequestTypeId) {
		this.set(REQUEST_TYPE_ID, myRequestTypeId);
	}

	// getter and setter methods for variable mySeverityId
	public int getSeverityId() {
		return (Integer) this.get(SEVERITY_ID);
	}

	public void setSeverityId(int mySeverityId) {
		this.set(SEVERITY_ID, mySeverityId);
	}

	// getter and setter methods for variable myStatusId
	public int getStatusId() {
		return (Integer) this.get(STATUS_ID);
	}

	public void setStatusId(int myStatusId) {
		this.set(STATUS_ID, myStatusId);
	}

	// getter and setter methods for variable mySubject
	public String getSubject() {
		return (String) this.get(SUBJECT);
	}

	public void setSubject(String mySubject) {
		this.set(SUBJECT, mySubject);
	}

	// getter and setter methods for variable mySubscriberIds
	public ArrayList<Integer> getSubscriberIds() {
		return (ArrayList<Integer>) this.get(SUBSCRIBER_IDS);
	}

	public void setSubscriberIds(ArrayList<Integer> mySubscriberIds) {
		this.set(SUBSCRIBER_IDS, mySubscriberIds);
	}

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

	// getter and setter methods for variable myToIds
	public ArrayList<Integer> getToIds() {
		return (ArrayList<Integer>) this.get(TO_IDS);
	}

	public void setToIds(ArrayList<Integer> myToIds) {
		this.set(TO_IDS, myToIds);
	}

	// getter and setter methods for variable myUserId
	public int getUserId() {
		return (Integer) this.get(USER_ID);
	}

	public void setUserId(int myUserId) {
		this.set(USER_ID, myUserId);
	}
	
	public String getAttachmentHTML(){
		return (String)this.get(ATTACHMENT_HTML);
	}
	
	public void setAttachmentHTML(String html){
		this.set(ATTACHMENT_HTML, html);
	}

	public void setActionUser(UserClient actionUser) {
		this.actionUser = actionUser;
	}

	public UserClient getActionUser() {
		return actionUser;
	}
}