package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for UserReadAction
public class UserReadActionClient extends TbitsModelData {

	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public UserReadActionClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String ACTION_ID = "action_id";
	public static String REQUEST_ID = "request_id";
	public static String SYSTEM_ID = "system_id";
	public static String USER_ID = "user_id";

	// getter and setter methods for variable myActionId
	public int getActionId() {
		return (Integer) this.get(ACTION_ID);
	}

	public void setActionId(int myActionId) {
		this.set(ACTION_ID, myActionId);
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

}