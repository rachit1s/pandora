package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for MailListUser
public class MailListUserClient extends TbitsModelData {

	// default constructor
	public MailListUserClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String MAIL_LIST_ID = "mail_list_id";
	public static String USER_ID = "user_id";

	// getter and setter methods for variable myMailListId
	public int getMailListId() {
		return (Integer) this.get(MAIL_LIST_ID);
	}

	public void setMailListId(int myMailListId) {
		this.set(MAIL_LIST_ID, myMailListId);
	}

	// getter and setter methods for variable myUserId
	public int getUserId() {
		return (Integer) this.get(USER_ID);
	}

	public void setUserId(int myUserId) {
		this.set(USER_ID, myUserId);
	}

}