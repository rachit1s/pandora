package com.tbitsGlobal.admin.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.modelData.MailingListUserClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public interface MailingListServiceAsync {
	void getAllMailingLists(AsyncCallback<List<MailingListUserClient>> callback);
	
	void getMailingListByUserId(int userId, AsyncCallback<List<UserClient>> callback);

	void deleteMailingLists(List<MailingListUserClient> models, AsyncCallback<Boolean> callback);
	
	void updateMailingList(String mailListName, List<UserClient> mailListMembers, AsyncCallback<Boolean> callback);
}
