package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for RelatedRequest
public class RelatedRequestClient extends TbitsModelData {

	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public RelatedRequestClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String PRIMARY_ACTION_ID = "primary_action_id";
	public static String PRIMARY_REQUEST_ID = "primary_request_id";
	public static String PRIMARY_SYS_PREFIX = "primary_sys_prefix";
	public static String RELATED_ACTION_ID = "related_action_id";
	public static String RELATED_REQUEST_ID = "related_request_id";
	public static String RELATED_SYS_PREFIX = "related_sys_prefix";

	// getter and setter methods for variable myPrimaryActionId
	public int getPrimaryActionId() {
		return (Integer) this.get(PRIMARY_ACTION_ID);
	}

	public void setPrimaryActionId(int myPrimaryActionId) {
		this.set(PRIMARY_ACTION_ID, myPrimaryActionId);
	}

	// getter and setter methods for variable myPrimaryRequestId
	public int getPrimaryRequestId() {
		return (Integer) this.get(PRIMARY_REQUEST_ID);
	}

	public void setPrimaryRequestId(int myPrimaryRequestId) {
		this.set(PRIMARY_REQUEST_ID, myPrimaryRequestId);
	}

	// getter and setter methods for variable myPrimarySysPrefix
	public String getPrimarySysPrefix() {
		return (String) this.get(PRIMARY_SYS_PREFIX);
	}

	public void setPrimarySysPrefix(String myPrimarySysPrefix) {
		this.set(PRIMARY_SYS_PREFIX, myPrimarySysPrefix);
	}

	// getter and setter methods for variable myRelatedActionId
	public int getRelatedActionId() {
		return (Integer) this.get(RELATED_ACTION_ID);
	}

	public void setRelatedActionId(int myRelatedActionId) {
		this.set(RELATED_ACTION_ID, myRelatedActionId);
	}

	// getter and setter methods for variable myRelatedRequestId
	public int getRelatedRequestId() {
		return (Integer) this.get(RELATED_REQUEST_ID);
	}

	public void setRelatedRequestId(int myRelatedRequestId) {
		this.set(RELATED_REQUEST_ID, myRelatedRequestId);
	}

	// getter and setter methods for variable myRelatedSysPrefix
	public String getRelatedSysPrefix() {
		return (String) this.get(RELATED_SYS_PREFIX);
	}

	public void setRelatedSysPrefix(String myRelatedSysPrefix) {
		this.set(RELATED_SYS_PREFIX, myRelatedSysPrefix);
	}

}