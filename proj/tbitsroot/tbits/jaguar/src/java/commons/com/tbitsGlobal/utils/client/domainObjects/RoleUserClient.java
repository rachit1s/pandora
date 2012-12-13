package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for RoleUser
public class RoleUserClient extends TbitsModelData {

	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public RoleUserClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String IS_ACTIVE = "is_active";
	public static String ROLE_ID = "role_id";
	public static String SYSTEM_ID = "system_id";
	public static String USER_ID = "user_id";

	// getter and setter methods for variable myIsActive
	public boolean getIsActive() {
		return (Boolean) this.get(IS_ACTIVE);
	}

	public void setIsActive(boolean myIsActive) {
		this.set(IS_ACTIVE, myIsActive);
	}

	// getter and setter methods for variable myRoleId
	public int getRoleId() {
		return (Integer) this.get(ROLE_ID);
	}

	public void setRoleId(int myRoleId) {
		this.set(ROLE_ID, myRoleId);
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