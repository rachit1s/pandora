package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for TypeUser
public class TypeUserClient extends TbitsModelData {

	// default constructor
	public TypeUserClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String FIELD_ID = "field_id";
	public static String IS_ACTIVE = "is_active";
	public static String IS_VOLUNTEER = "is_volunteer";
	public static String NOTIFICATION_ID = "notification_id";
	public static String R_R_VOLUNTEER = "r_r_volunteer";
	public static String SYSTEM_ID = "system_id";
	public static String TYPE_ID = "type_id";
	public static String USER = "user";
	public static String USER_ID = "user_id";
	public static String USER_TYPE_ID = "user_type_id";

	// getter and setter methods for variable myFieldId
	public int getFieldId() {
		return (Integer) this.get(FIELD_ID);
	}

	public void setFieldId(int myFieldId) {
		this.set(FIELD_ID, myFieldId);
	}

	// getter and setter methods for variable myIsActive
	public boolean getIsActive() {
		return (Boolean) this.get(IS_ACTIVE);
	}

	public void setIsActive(boolean myIsActive) {
		this.set(IS_ACTIVE, myIsActive);
	}

	// getter and setter methods for variable myIsVolunteer
	public boolean getIsVolunteer() {
		return (Boolean) this.get(IS_VOLUNTEER);
	}

	public void setIsVolunteer(boolean myIsVolunteer) {
		this.set(IS_VOLUNTEER, myIsVolunteer);
	}

	// getter and setter methods for variable myNotificationId
	public int getNotificationId() {
		return (Integer) this.get(NOTIFICATION_ID);
	}

	public void setNotificationId(int myNotificationId) {
		this.set(NOTIFICATION_ID, myNotificationId);
	}

	// getter and setter methods for variable myRRVolunteer
	public boolean getRRVolunteer() {
		return (Boolean) this.get(R_R_VOLUNTEER);
	}

	public void setRRVolunteer(boolean myRRVolunteer) {
		this.set(R_R_VOLUNTEER, myRRVolunteer);
	}

	// getter and setter methods for variable mySystemId
	public int getSystemId() {
		return (Integer) this.get(SYSTEM_ID);
	}

	public void setSystemId(int mySystemId) {
		this.set(SYSTEM_ID, mySystemId);
	}

	// getter and setter methods for variable myTypeId
	public int getTypeId() {
		return (Integer) this.get(TYPE_ID);
	}

	public void setTypeId(int myTypeId) {
		this.set(TYPE_ID, myTypeId);
	}

//	 getter and setter methods for variable myUser
	 public UserClient getUser (){
		 return (UserClient) this.get(USER);
	 }
	 public void setUser(UserClient myUser) {
		 this.set(USER, myUser);
	 }

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