package com.tbitsGlobal.admin.client.modelData;

import java.util.List;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public class MailingListUserClient extends TbitsModelData{
	public static final String MAIL_LIST_USER = "mail_list_user"; 
	public static final String MAIL_LIST_MEMBERS = "mail_list_members";
	
	public MailingListUserClient() {
		super();
	}
	
	public void setMailListUser(UserClient user){
		this.set(MAIL_LIST_USER, user);
	}
	
	public UserClient getMailListUser(){
		return (UserClient)this.get(MAIL_LIST_USER);
	}
	
	public void setMailListMembers(List<UserClient> users){
		this.set(MAIL_LIST_MEMBERS, users);
	}
	
	public List<UserClient> getMailListMembers(){
		return (List<UserClient>)this.get(MAIL_LIST_MEMBERS);
	}
}
