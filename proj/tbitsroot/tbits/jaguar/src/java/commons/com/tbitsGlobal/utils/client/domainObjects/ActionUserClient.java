package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for ActionUser
public class ActionUserClient extends TbitsModelData {

	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public ActionUserClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String ACTION_ID = "action_id";
	public static String IS_PRIMARY = "is_primary";
	public static String ORDERING = "ordering";
	public static String REQUEST_ID = "request_id";
	public static String SYSTEM_ID = "system_id";
	public static String USER_ID = "user_id";
	public static String USER_TYPE_ID = "user_type_id";

	// getter and setter methods for variable myActionId
	public int getActionId() {
		return (Integer) this.get(ACTION_ID);
	}

	public void setActionId(int myActionId) {
		this.set(ACTION_ID, myActionId);
	}

	// getter and setter methods for variable myIsPrimary
	public boolean getIsPrimary() {
		return (Boolean) this.get(IS_PRIMARY);
	}

	public void setIsPrimary(boolean myIsPrimary) {
		this.set(IS_PRIMARY, myIsPrimary);
	}

	// getter and setter methods for variable myOrdering
	public int getOrdering() {
		return (Integer) this.get(ORDERING);
	}

	public void setOrdering(int myOrdering) {
		this.set(ORDERING, myOrdering);
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

	// getter and setter methods for variable myUserId
	public int getUserId() {
		return (Integer) this.get(USER_ID);
	}

	public void setUserId(int myUserId) {
		this.set(USER_ID, myUserId);
	}

	// getter and setter methods for variable myUserTypeId
	public int getUserTypeId() {
		return (Integer) this.get(USER_TYPE_ID);
	}

	public void setUserTypeId(int myUserTypeId) {
		this.set(USER_TYPE_ID, myUserTypeId);
	}

}