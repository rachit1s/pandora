package com.tbitsGlobal.admin.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BAMenuClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

public interface BAMenuService extends RemoteService{
	public List<BAMenuClient> getBAMenus() throws TbitsExceptionClient;
	
	public boolean deleteMenus(List<BAMenuClient> models) throws TbitsExceptionClient;
	
	public List<BAMenuClient> updateMenus(List<BAMenuClient> models) throws TbitsExceptionClient;
	
	public boolean updateBAMenuMapping(int menuId, List<BusinessAreaClient> bas) throws TbitsExceptionClient;
	
	public List<Integer> getBAMenuMapping(int menuId) throws TbitsExceptionClient;
}
