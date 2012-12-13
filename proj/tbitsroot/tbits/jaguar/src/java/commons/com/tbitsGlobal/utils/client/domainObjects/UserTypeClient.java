package commons.com.tbitsGlobal.utils.client.domainObjects;

import java.util.ArrayList;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for UserType
public class UserTypeClient extends TbitsModelData {

	public static final int USER = 1;
	public static final int TO = 5;
	public static final int SUBSCRIBER = 4;
	public static final int LOGGER = 2;
	public static final int INTERNAL_USER = 7;
	public static final int INTERNAL_MAILINGLIST = 8;
	public static final int INTERNAL_HIDDEN_LIST = 11;
	public static final int INTERNAL_CONTACT = 10;
	public static final int EXTERNAL_USER = 9;
	public static final int CC = 6;
	public static final int ASSIGNEE = 3;

	// default constructor
	public UserTypeClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String NAME = "name";
	public static String USER_TYPE_ID = "user_type_id";

	// getter and setter methods for variable myName
	public String getName() {
		return (String) this.get(NAME);
	}

	public void setName(String myName) {
		this.set(NAME, myName);
	}

	// getter and setter methods for variable myUserTypeId
	public int getUserTypeId() {
		return (Integer) this.get(USER_TYPE_ID);
	}

	public void setUserTypeId(int myUserTypeId) {
		this.set(USER_TYPE_ID, myUserTypeId);
	}
	
	public static List<UserTypeClient> getUserTypes(){
		List<UserTypeClient> userTypes = new ArrayList<UserTypeClient>();
		
		UserTypeClient userType = new UserTypeClient();
		userType.setName("Internal-User");
		userType.setUserTypeId(INTERNAL_USER);
		userTypes.add(userType);
		
		userType = new UserTypeClient();
		userType.setName("Internal-Mail-List");
		userType.setUserTypeId(INTERNAL_MAILINGLIST);
		userTypes.add(userType);
		
		userType = new UserTypeClient();
		userType.setName("External-Email");
		userType.setUserTypeId(EXTERNAL_USER);
		userTypes.add(userType);
		
		userType = new UserTypeClient();
		userType.setName("Internal Contact");
		userType.setUserTypeId(INTERNAL_CONTACT);
		userTypes.add(userType);
		
		return userTypes;
	}

}