package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for ExclusionList
public class ExclusionListClient extends TbitsModelData {

	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public ExclusionListClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String SYSTEM_ID = "system_id";
	public static String USER = "user";
	public static String USER_ID = "user_id";
	public static String USER_TYPE_ID = "user_type_id";

	// getter and setter methods for variable mySystemId
	public int getSystemId() {
		return (Integer) this.get(SYSTEM_ID);
	}

	public void setSystemId(int mySystemId) {
		this.set(SYSTEM_ID, mySystemId);
	}

	// getter and setter methods for variable myUser
	// public User getUser (){
	// return (User) this.get(USER);
	// }
	// public void setUser(User myUser) {
	// this.set(USER, myUser);
	// }

	// getter and setter methods for variable myUserId
	public int getUserId() {
		return (Integer) this.get(USER_ID);
	}

	public void setUserId(int myUserId) {
		this.set(USER_ID, myUserId);
	}

	// getter and setter methods for variable myUserTypeId
	public int getUserTypeId() {
		return (Integer) this.get(USER_TYPE_ID);
	}

	public void setUserTypeId(int myUserTypeId) {
		this.set(USER_TYPE_ID, myUserTypeId);
	}

}