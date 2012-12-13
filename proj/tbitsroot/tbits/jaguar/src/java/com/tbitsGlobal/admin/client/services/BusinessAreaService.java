package com.tbitsGlobal.admin.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BAMailAccountClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.SysConfigClient;

public interface BusinessAreaService extends RemoteService{
	public List<BAMailAccountClient> getBAMailAccount(String sysPrefix) throws TbitsExceptionClient;
	
	public boolean updateMailAccounts(String sysPrefix, List<BAMailAccountClient> mailAccounts) throws TbitsExceptionClient;
	
	public List<BusinessAreaClient> getAllBAList()	throws TbitsExceptionClient;
	public BusinessAreaClient getBAClient(String sys_prefix) throws TbitsExceptionClient;

	public Boolean updateBA(BusinessAreaClient baToUpdate) throws TbitsExceptionClient;
	
	public BusinessAreaClient createNewBA(String sysPrefix, String BAName) throws TbitsExceptionClient;
	
	public Boolean testMailSetting(String server, String port, String login, String password, String protocol) throws TbitsExceptionClient;
	
	public SysConfigClient getSysConfigClient(SysConfigClient sysconfig);
	
}
