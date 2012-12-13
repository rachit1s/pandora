package commons.com.tbitsGlobal.utils.client;

/**
 * 
 * @author sourabh
 * 
 * An object carrying identifiers for a Linked Request
 */
public class LinkedRequest {
	private String sysPrefix = "";
	private int requestId = 0;
	private int actionId = 0;
	
	public LinkedRequest(String sysPrefix, int requestId) {
		super();
		this.requestId = requestId;
		this.sysPrefix = sysPrefix;
	}

	public LinkedRequest(String sysPrefix, int requestId, int actionId) {
		super();
		this.actionId = actionId;
		this.requestId = requestId;
		this.sysPrefix = sysPrefix;
	}

	public String getSysPrefix() {
		return sysPrefix;
	}

	public void setSysPrefix(String sysPrefix) {
		this.sysPrefix = sysPrefix;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
		this.actionId = actionId;
	}
	
	@Override
	public String toString() {
		return sysPrefix + "#" + requestId + (actionId > 0 ? "#" + actionId : "");
	}
	
}
