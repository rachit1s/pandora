package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for TransferredRequest
public class TransferredRequestClient extends TbitsModelData {

	// default constructor
	public TransferredRequestClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String SOURCE_PREFIX = "source_prefix";
	public static String SOURCE_REQUEST_ID = "source_request_id";
	public static String TARGET_PREFIX = "target_prefix";
	public static String TARGET_REQUEST_ID = "target_request_id";

	// getter and setter methods for variable mySourcePrefix
	public String getSourcePrefix() {
		return (String) this.get(SOURCE_PREFIX);
	}

	public void setSourcePrefix(String mySourcePrefix) {
		this.set(SOURCE_PREFIX, mySourcePrefix);
	}

	// getter and setter methods for variable mySourceRequestId
	public int getSourceRequestId() {
		return (Integer) this.get(SOURCE_REQUEST_ID);
	}

	public void setSourceRequestId(int mySourceRequestId) {
		this.set(SOURCE_REQUEST_ID, mySourceRequestId);
	}

	// getter and setter methods for variable myTargetPrefix
	public String getTargetPrefix() {
		return (String) this.get(TARGET_PREFIX);
	}

	public void setTargetPrefix(String myTargetPrefix) {
		this.set(TARGET_PREFIX, myTargetPrefix);
	}

	// getter and setter methods for variable myTargetRequestId
	public int getTargetRequestId() {
		return (Integer) this.get(TARGET_REQUEST_ID);
	}

	public void setTargetRequestId(int myTargetRequestId) {
		this.set(TARGET_REQUEST_ID, myTargetRequestId);
	}

}