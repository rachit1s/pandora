package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for DActionLog
public class DActionLogClient extends TbitsModelData {

	// default constructor
	public DActionLogClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String ACTION_ID = "action_id";
	public static String D_ACTION_LOG = "d_action_log";
	public static String REQUEST_ID = "request_id";
	public static String SYSTEM_ID = "system_id";

	// getter and setter methods for variable myActionId
	public int getActionId() {
		return (Integer) this.get(ACTION_ID);
	}

	public void setActionId(int myActionId) {
		this.set(ACTION_ID, myActionId);
	}

	// getter and setter methods for variable myDActionLog
	public String getDActionLog() {
		return (String) this.get(D_ACTION_LOG);
	}

	public void setDActionLog(String myDActionLog) {
		this.set(D_ACTION_LOG, myDActionLog);
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

}