package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for RolePermission
public class RolePermissionClient extends TbitsModelData {

	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public RolePermissionClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String D_PERMISSION = "d_permission";
	public static String FIELD_ID = "field_id";
	public static String PERMISSION = "permission";
	public static String ROLE_ID = "role_id";
	public static String SYSTEM_ID = "system_id";

	// getter and setter methods for variable myDPermission
	public int getDPermission() {
		return (Integer) this.get(D_PERMISSION);
	}

	public void setDPermission(int myDPermission) {
		this.set(D_PERMISSION, myDPermission);
	}

	// getter and setter methods for variable myFieldId
	public int getFieldId() {
		return (Integer) this.get(FIELD_ID);
	}

	public void setFieldId(int myFieldId) {
		this.set(FIELD_ID, myFieldId);
	}

	// getter and setter methods for variable myPermission
	public int getPermission() {
		return (Integer) this.get(PERMISSION);
	}

	public void setPermission(int myPermission) {
		this.set(PERMISSION, myPermission);
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

}