package commons.com.tbitsGlobal.utils.client.Events;


public class ToAddSubRequest extends TbitsBaseEvent {
	private int requestId;
	
	public ToAddSubRequest(int requestId) {
		super();
		this.requestId = requestId;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	@Override
	public boolean beforeFire() {
		return true;
	}
}
