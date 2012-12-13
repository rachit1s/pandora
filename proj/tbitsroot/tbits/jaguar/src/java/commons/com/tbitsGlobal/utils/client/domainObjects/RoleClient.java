package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for Role
public class RoleClient extends TbitsModelData {

	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public RoleClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String DESCRIPTION = "description";
	public static String ROLE_ID = "role_id";
	public static String ROLE_NAME = "role_name";
	public static String SYSTEM_ID = "system_id";
	public static String FIELD_ID = "field_id";
	public static String CAN_BE_DELETED = "can_be_deleted";

	// getter and setter methods for variable myDescription
	public String getDescription() {
		return (String) this.get(DESCRIPTION);
	}

	public void setDescription(String myDescription) {
		this.set(DESCRIPTION, myDescription);
	}

	// getter and setter methods for variable myRoleId
	public int getRoleId() {
		return (Integer) this.get(ROLE_ID);
	}

	public void setRoleId(int myRoleId) {
		this.set(ROLE_ID, myRoleId);
	}

	// getter and setter methods for variable myRoleName
	public String getRoleName() {
		return (String) this.get(ROLE_NAME);
	}

	public void setRoleName(String myRoleName) {
		this.set(ROLE_NAME, myRoleName);
	}

	// getter and setter methods for variable mySystemId
	public int getSystemId() {
		return (Integer) this.get(SYSTEM_ID);
	}

	public void setSystemId(int mySystemId) {
		this.set(SYSTEM_ID, mySystemId);
	}
	
	public int getFieldId() {
		return (Integer) this.get(FIELD_ID);
	}

	public void setFieldId(int myFieldId) {
		this.set(FIELD_ID, myFieldId);
	}
	
	public int getCanBeDeleted() {
		return (Integer) this.get(CAN_BE_DELETED);
	}

	public void setCanBeDeleted(int myCanBeDeleted) {
		this.set(CAN_BE_DELETED, myCanBeDeleted);
	}

}