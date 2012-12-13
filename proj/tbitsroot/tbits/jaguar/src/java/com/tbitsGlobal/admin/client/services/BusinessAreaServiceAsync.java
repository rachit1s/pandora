package com.tbitsGlobal.admin.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.domainObjects.BAMailAccountClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.SysConfigClient;

public interface BusinessAreaServiceAsync {
	void updateBA(BusinessAreaClient baToUpdate, AsyncCallback<Boolean> callback);
	
	void getBAClient(String sys_prefix,	AsyncCallback<BusinessAreaClient> callback);
	
	void getBAMailAccount(String sysPrefix,
			AsyncCallback<List<BAMailAccountClient>> callback);

	void updateMailAccounts(String sysPrefix, List<BAMailAccountClient> mailAccounts,
			AsyncCallback<Boolean> callback);
	
	void createNewBA(String sysPrefix, String BAName,
			AsyncCallback<BusinessAreaClient> callback);

	void testMailSetting(String server, String port, String login,
            String password, String protocol, AsyncCallback<Boolean> callback);

	void getSysConfigClient(SysConfigClient sysconfig, AsyncCallback<SysConfigClient> callback);

	void getAllBAList(AsyncCallback<List<BusinessAreaClient>> callback);
}
