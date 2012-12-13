package commons.com.tbitsGlobal.utils.client.Events;

public class ToViewRequestOtherBA extends TbitsBaseEvent {
	private int requestId;
	private String sysPrefix;
	
	public ToViewRequestOtherBA(String sysPrefix, int requestId) {
		super("Displaying " + sysPrefix + "#" + requestId + "... Please Wait...",
				"Could not display " + sysPrefix + "#" + requestId + "... Try Again!!!");
		
		this.sysPrefix = sysPrefix;
		this.requestId = requestId;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}
	
	public void setSysPrefix(String sysPrefix) {
		this.sysPrefix = sysPrefix;
	}

	public String getSysPrefix() {
		return sysPrefix;
	}
	
	@Override
	public boolean beforeFire() {
		return true;
	}
}
