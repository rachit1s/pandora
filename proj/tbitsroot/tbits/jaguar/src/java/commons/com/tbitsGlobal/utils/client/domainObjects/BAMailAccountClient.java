package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

/**
 * pojo for BAMailAccount
 *
 */
public class BAMailAccountClient extends TbitsModelData {
	private static final long serialVersionUID = 1L;
	public static final String[] ALLOWED_PROTOCOLS = {"pop3", "imap", "pop3s"};
	public static final String DEFAULT_PROTOCOL = ALLOWED_PROTOCOLS[0];
	public static final int DEFAULT_PORT = 110;

	// private static Vector<BAMailAccount> allBAMailAccounts = null;

	// default constructor
	public BAMailAccountClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String EMAIL_ID = "email_id";
	public static String MAIL_SERVER = "mail_server";
	public static String BA_PREFIX = "ba_prefix";
	public static String PASSWARD = "passward";
	public static String BA_MAIL_AC_ID = "ba_mail_ac_id";
	public static String CATEGORY_ID = "category_id";

	public static String PROTOCOL = "protocol";
	public static String BA_EMAIL_ADDRESS = "ba_email_address";
	public static String PORT = "port";
	public static String IS_ACTIVE = "is_active";


	// getter and setter methods for variable myEmailID
	public String getEmailID() {
		return (String) this.get(EMAIL_ID);
	}

	public void setEmailID(String myEmailID) {
		this.set(EMAIL_ID, myEmailID);
	}

	// getter and setter methods for variable myMailServer
	public String getMailServer() {
		return (String) this.get(MAIL_SERVER);
	}

	public void setMailServer(String myMailServer) {
		this.set(MAIL_SERVER, myMailServer);
	}

	// getter and setter methods for variable myBAPrefix
	public String getBAPrefix() {
		return (String) this.get(BA_PREFIX);
	}

	public void setBAPrefix(String myBAPrefix) {
		this.set(BA_PREFIX, myBAPrefix);
	}

	// getter and setter methods for variable myPassward
	public String getPassward() {
		return (String) this.get(PASSWARD);
	}

	public void setPassward(String myPassward) {
		this.set(PASSWARD, myPassward);
	}

	// getter and setter methods for variable myBAMailAcId
	public int getBAMailAcId() {
		return (Integer) this.get(BA_MAIL_AC_ID);
	}

	public void setBAMailAcId(int myBAMailAcId) {
		this.set(BA_MAIL_AC_ID, myBAMailAcId);
	}

	// getter and setter methods for variable myBAEmailAddress
	public String getBAEmailAddress() {
		return (String) this.get(BA_EMAIL_ADDRESS);
	}

	public void setBAEmailAddress(String myBAEmailAddress) {
		this.set(BA_EMAIL_ADDRESS, myBAEmailAddress);
	}
	
	// getter and setter methods for variable myProtocol
	public String getProtocol() {
		return (String) this.get(PROTOCOL);
	}

	public void setProtocol(String myProtocol) {
		this.set(PROTOCOL, myProtocol);
	}
	
	// getter and setter methods for variable myCategoryId
	public int getCategoryId() {
		return (Integer) this.get(CATEGORY_ID);
	}

	public void setCategoryId(int myCategoryId) {
		this.set(CATEGORY_ID, myCategoryId);
	}
	
	// getter and setter methods for variable myPort
	public int getPort() {
		return (Integer) this.get(PORT);
	}

	public void setPort(int myPort) {
		this.set(PORT, myPort);
	}

	// getter and setter methods for variable isActive
	public boolean getIsActive() {
		return (Boolean) this.get(IS_ACTIVE);
	}

	public void setIsActive(boolean isActive) {
		this.set(IS_ACTIVE, isActive);
	}
}