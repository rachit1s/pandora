package com.tbitsGlobal.admin.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.tbitsGlobal.admin.client.modelData.MailingListUserClient;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public interface MailingListService extends RemoteService{
	List<MailingListUserClient> getAllMailingLists() throws TbitsExceptionClient;
	
	List<UserClient> getMailingListByUserId(int userId) throws TbitsExceptionClient;

	boolean deleteMailingLists(List<MailingListUserClient> models) throws TbitsExceptionClient;

	boolean updateMailingList(String mailListName, List<UserClient> mailListMembers) throws TbitsExceptionClient;
}
