package com.tbitsGlobal.admin.client.services;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.RemoteService;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;

public interface AppPropertiesService extends RemoteService{
	public HashMap<String,String> getAppProperties() throws TbitsExceptionClient;
	
	public boolean updateAppProperties(HashMap<String,String>map) throws TbitsExceptionClient;
	
	public boolean insertAppProperties(String name,String value) throws TbitsExceptionClient;
	
	public boolean deleteAppProperties(String name)throws TbitsExceptionClient;
}
