package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;

public interface DisplayGroupService extends RemoteService{
	public ArrayList<DisplayGroupClient> getDisplayGroups(String sys_prefix) throws TbitsExceptionClient;
	
	public DisplayGroupClient updateDisplayGroup(String sysPrefix, DisplayGroupClient group) throws TbitsExceptionClient;
	
	public List<DisplayGroupClient> updateDisplayGroups(String sysPrefix, List<DisplayGroupClient> groups) throws TbitsExceptionClient;
	
	public DisplayGroupClient insertDisplayGroup(String sysPrefix, String displayName,int order,boolean isActive,boolean isDefault) throws TbitsExceptionClient ;
	
	public DisplayGroupClient deleteDisplayGroup(String sysPrefix, DisplayGroupClient dgc) throws TbitsExceptionClient ;
	
	public boolean deleteDisplayGroups(String sysPrefix, List<DisplayGroupClient> groups) throws TbitsExceptionClient ;
	
	public List<FieldClient> getFieldsByDisplayGroup(DisplayGroupClient displayGroup) throws TbitsExceptionClient;
}
