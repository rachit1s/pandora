package commons.com.tbitsGlobal.utils.client.domainObjects;

import java.util.HashMap;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.bafield.ColPrefs;

//pojo for User
public class UserClient extends TbitsModelData {

public static final String HOME_PHONE_COLUMN_NAME = "home_phone";
	public static final String MOBILE_COLUMN_NAME = "mobile";
	public static final String EXTENSION_COLUMN_NAME = "extension";
	public static final String LOCATION_COLUMN_NAME = "location";
	public static final String MAIL_NICKNAME_COLUMN_NAME = "mail_nickname";
	public static final String MEMBER_COLUMN_NAME = "member";
	public static final String MEMBER_OF_COLUMN_NAME = "member_of";
	public static final String NAME_COLUMN_NAME = "name";
	public static final String DISTINGUISHED_NAME_COLUMN_NAME = "distinguished_name";
	public static final String CN_COLUMN_NAME = "cn";
	public static final String IS_DISPLAY_COLUMN_NAME = "is_display";
	public static final String IS_ON_VACATION_COLUMN_NAME = "is_on_vacation";
	public static final String WINDOWS_CONFIG_COLUMN_NAME = "windows_config";
	public static final String WEB_CONFIG_COLUMN_NAME = "web_config";
	public static final String USER_TYPE_ID_COLUMN_NAME = "user_type_id";
	public static final String IS_ACTIVE_COLUMN_NAME = "is_active";
	public static final String EMAIL_COLUMN_NAME = "email";
	public static final String DISPLAY_NAME_COLUMN_NAME = "display_name";
	public static final String LAST_NAME_COLUMN_NAME = "last_name";
	public static final String FIRST_NAME_COLUMN_NAME = "first_name";
	public static final String USER_LOGIN_COLUMN_NAME = "user_login";
	public static final String USER_ID_COLUMN_NAME = "user_id";
	public static final String FIRM_CODE_COLUMN_NAME = "firm_code";
	public static final String FIRM_ADDRESS_COLUMN_NAME = "firm_address";
	public static final String FULL_FIRM_NAME_COLUMN_NAME = "full_firm_name";
	public static final String SEX_COLUMN_NAME = "sex";
	public static final String DESIGNATION_COLUMN_NAME = "designation";
	
	// Static Strings defining keys for corresponding variable
	public static String CN = "cn";
	public static String DISPLAY_NAME = "display_name";
	public static String DISTINGUISHED_NAME = "distinguished_name";
	public static String EMAIL = "email";
	public static String EXTENSION = "extension";
	public static String FIRST_NAME = "first_name";
	public static String HOME_PHONE = "home_phone";
	public static String IS_ACTIVE = "is_active";
	public static String IS_DISPLAY = "is_display";
	public static String IS_ON_VACATION = "is_on_vacation";
	public static String LAST_NAME = "last_name";
	public static String LOCATION = "location";
	public static String MAIL_NICKNAME = "mail_nickname";
	public static String MEMBER = "member";
	public static String MEMBER_OF = "member_of";
	public static String MOBILE = "mobile";
	public static String NAME = "name";
	public static String USER_ID = "user_id";
	public static String USER_LOGIN = "user_login";
	public static String USER_TYPE_ID = "user_type_id";
	public static String WEB_CONFIG = "web_config";
	public static String WEB_CONFIG_OBJECT = "web_config_object";
	public static String WINDOWS_CONFIG = "windows_config";
	public static String WEB_DATE_FORMAT = "web_date_format";
	public static String DEFAULT_BA = "default_ba";
	public static String IS_SUPER_USER = "is_super_user";
	
	public static final String FIRM_CODE = "firm_code";
	public static final String FIRM_ADDRESS = "firm_address";
	public static final String FULL_FIRM_NAME = "full_firm_name";
	public static final String SEX = "sex";
	public static final String DESIGNATION = "designation";
	
	private HashMap<Integer,HashMap<Integer, List<ColPrefs>>> colPrefs;

	// default constructor
	public UserClient() {
		super();
		
		this.setUserId(0);
	}	
	
	// getter and setter methods for variable myCn
	public String getCn() {
		return (String) this.get(CN);
	}

	public void setCn(String myCn) {
		this.set(CN, myCn);
	}

	// getter and setter methods for variable myDisplayName
	public String getDisplayName() {
		return (String) this.get(DISPLAY_NAME);
	}

	public void setDisplayName(String myDisplayName) {
		this.set(DISPLAY_NAME, myDisplayName);
	}

	// getter and setter methods for variable myDistinguishedName
	public String getDistinguishedName() {
		return (String) this.get(DISTINGUISHED_NAME);
	}

	public void setDistinguishedName(String myDistinguishedName) {
		this.set(DISTINGUISHED_NAME, myDistinguishedName);
	}

	// getter and setter methods for variable myEmail
	public String getEmail() {
		return (String) this.get(EMAIL);
	}

	public void setEmail(String myEmail) {
		this.set(EMAIL, myEmail);
	}

	// getter and setter methods for variable myExtension
	public String getExtension() {
		return (String) this.get(EXTENSION);
	}

	public void setExtension(String myExtension) {
		this.set(EXTENSION, myExtension);
	}

	// getter and setter methods for variable myFirstName
	public String getFirstName() {
		return (String) this.get(FIRST_NAME);
	}

	public void setFirstName(String myFirstName) {
		this.set(FIRST_NAME, myFirstName);
	}

	// getter and setter methods for variable myHomePhone
	public String getHomePhone() {
		return (String) this.get(HOME_PHONE);
	}

	public void setHomePhone(String myHomePhone) {
		this.set(HOME_PHONE, myHomePhone);
	}

	// getter and setter methods for variable myIsActive
	public boolean getIsActive() {
		return (Boolean) this.get(IS_ACTIVE);
	}

	public void setIsActive(boolean myIsActive) {
		this.set(IS_ACTIVE, myIsActive);
	}

	// getter and setter methods for variable myIsDisplay
	public boolean getIsDisplay() {
		return (Boolean) this.get(IS_DISPLAY);
	}

	public void setIsDisplay(boolean myIsDisplay) {
		this.set(IS_DISPLAY, myIsDisplay);
	}

	// getter and setter methods for variable myIsOnVacation
	public boolean getIsOnVacation() {
		return (Boolean) this.get(IS_ON_VACATION);
	}

	public void setIsOnVacation(boolean myIsOnVacation) {
		this.set(IS_ON_VACATION, myIsOnVacation);
	}

	// getter and setter methods for variable myLastName
	public String getLastName() {
		return (String) this.get(LAST_NAME);
	}

	public void setLastName(String myLastName) {
		this.set(LAST_NAME, myLastName);
	}

	// getter and setter methods for variable myLocation
	public String getLocation() {
		return (String) this.get(LOCATION);
	}

	public void setLocation(String myLocation) {
		this.set(LOCATION, myLocation);
	}

	// getter and setter methods for variable myMailNickname
	public String getMailNickname() {
		return (String) this.get(MAIL_NICKNAME);
	}

	public void setMailNickname(String myMailNickname) {
		this.set(MAIL_NICKNAME, myMailNickname);
	}

	// getter and setter methods for variable myMember
	public String getMember() {
		return (String) this.get(MEMBER);
	}

	public void setMember(String myMember) {
		this.set(MEMBER, myMember);
	}

	// getter and setter methods for variable myMemberOf
	public String getMemberOf() {
		return (String) this.get(MEMBER_OF);
	}

	public void setMemberOf(String myMemberOf) {
		this.set(MEMBER_OF, myMemberOf);
	}

	// getter and setter methods for variable myMobile
	public String getMobile() {
		return (String) this.get(MOBILE);
	}

	public void setMobile(String myMobile) {
		this.set(MOBILE, myMobile);
	}

	// getter and setter methods for variable myName
	public String getName() {
		return (String) this.get(NAME);
	}

	public void setName(String myName) {
		this.set(NAME, myName);
	}

	// getter and setter methods for variable myUserId
	public int getUserId() {
		return (Integer) this.get(USER_ID);
	}

	public void setUserId(int myUserId) {
		this.set(USER_ID, myUserId);
	}

	// getter and setter methods for variable myUserLogin
	public String getUserLogin() {
		return (String) this.get(USER_LOGIN);
	}

	public void setUserLogin(String myUserLogin) {
		this.set(USER_LOGIN, myUserLogin);
	}

	// getter and setter methods for variable myUserTypeId
	public int getUserTypeId() {
		return (Integer) this.get(USER_TYPE_ID);
	}

	public void setUserTypeId(int myUserTypeId) {
		this.set(USER_TYPE_ID, myUserTypeId);
	}

	// getter and setter methods for variable myWebConfig
	public String getWebConfig() {
		return (String) this.get(WEB_CONFIG);
	}

	public void setWebConfig(String myWebConfig) {
		this.set(WEB_CONFIG, myWebConfig);
	}
	
	public String getDefaultBA(){
		Object obj = this.get(DEFAULT_BA);
		if(obj == null)
			return null;
		return (String) obj;
	}
	
	public void setDefaultBA(String defaultBA){
		this.set(DEFAULT_BA, defaultBA);
	}

	// //getter and setter methods for variable myWebConfigObject
	// public WebConfig getWebConfigObject (){
	// return (WebConfig) this.get(WEB_CONFIG_OBJECT);
	// }
	// public void setWebConfigObject(WebConfig myWebConfigObject) {
	// this.set(WEB_CONFIG_OBJECT, myWebConfigObject);
	// }

	// getter and setter methods for variable myWindowsConfig
	public String getWindowsConfig() {
		return (String) this.get(WINDOWS_CONFIG);
	}

	public void setWindowsConfig(String myWindowsConfig) {
		this.set(WINDOWS_CONFIG, myWindowsConfig);
	}

	public void setWebDateFormat(String webDateFormat) {
		this.set(WEB_DATE_FORMAT, webDateFormat);
	}

	public String getWebDateFormat() {
		return (String)this.get(WEB_DATE_FORMAT);
	}

	public boolean getIsSuperUser() {
		return (Boolean)this.get(IS_SUPER_USER);
	}

	public void setIsSuperUser(boolean isSuperUser) {
		this.set(IS_SUPER_USER, isSuperUser);
	}


	 public String getFirmCode()
	    {
	    	return (String)this.get(UserClient.FIRM_CODE_COLUMN_NAME) ;
	    }
	    
	    public String getDesignation()
	    {
	    	return (String)this.get(UserClient.DESIGNATION_COLUMN_NAME);
	    }
	    
	    public String getFirmAddress()
	    {
	    	return (String)this.get(UserClient.FIRM_ADDRESS_COLUMN_NAME);
	    }
	    
	    public String getSex()
	    {
	    	return (String)this.get(UserClient.SEX_COLUMN_NAME);
	    }
	    
	    public String getFullFirmName()
	    {
	    	return (String) this.get(UserClient.FULL_FIRM_NAME_COLUMN_NAME);
	    }
	    
	    public void setFirmCode( String firmCode ) 
	    {
	    	if( null == firmCode )
	    		this.set(UserClient.FIRM_CODE_COLUMN_NAME, "") ;
			
			this.set(UserClient.FIRM_CODE_COLUMN_NAME, firmCode.trim()) ;   	
	    }
	    
	    public void setDesignation( String designation)
	    {
	    	if(null == designation )
	    		this.set(UserClient.DESIGNATION_COLUMN_NAME, "") ;
	    	
	    	this.set(UserClient.DESIGNATION_COLUMN_NAME, designation.trim()) ;
	    	
	    }
	    
	    public void setFirmAddress( String firmAddress )
	    {
	    	if( null == firmAddress )
	    		this.set(UserClient.FIRM_ADDRESS_COLUMN_NAME,"") ;
	    	
	    	this.set(UserClient.FIRM_ADDRESS_COLUMN_NAME,firmAddress.trim()) ;
	    }
	    
	    public void setSex( String sex )
	    {    	
	    	if( null != sex )
	    	{
	    		this.set(UserClient.SEX_COLUMN_NAME,sex.trim()) ;
	    	}
	    	
	    	if( null == sex || sex.length() == 0 ) 
	    		this.set(UserClient.SEX_COLUMN_NAME," ") ;
	    	// not checking whether the first letter is a Alpha or other thing. Just trying to convert it
	    	// to upper case set it .. 
	    	this.set(UserClient.SEX_COLUMN_NAME,sex.toUpperCase().substring(0,1)) ;
	    }
	    
	    public void setFullFirmName(String fullFirmName ) 
	    {
	    	if( null == fullFirmName )
	    		this.set(UserClient.FULL_FIRM_NAME_COLUMN_NAME,"");

	    	this.set(UserClient.FULL_FIRM_NAME_COLUMN_NAME,fullFirmName.trim()) ;
	    }
	    
	@Override
	public boolean equals(Object obj) {
		if( null == obj )
			return false ;
		
		if( ! (obj instanceof UserClient) )
			return false ;
		
		return this.getUserLogin().equals(((UserClient) obj).getUserLogin());
	}

	public void setColPrefs(HashMap<Integer,HashMap<Integer, List<ColPrefs>>> colPref) {
		this.colPrefs = colPref;
	}

	public HashMap<Integer,HashMap<Integer, List<ColPrefs>>> getColPrefs() {
		return colPrefs;
	}
	
	@Override
	public int compareTo(TbitsModelData o) {
		Object obj = o.get(USER_LOGIN);
		String login = (String) obj;
		return this.getUserLogin().compareTo(login);
	}

}