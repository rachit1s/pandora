package com.tbitsGlobal.admin.client.services;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.RemoteService;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;

public interface CaptionsService extends RemoteService{
	public boolean updateCaptions(HashMap<String,String> map,int sysId) throws TbitsExceptionClient;
	
	public HashMap<String,String> getAllBACaptionsbySysId(int sysId) throws TbitsExceptionClient;
	
	public boolean addCaptions(HashMap<String,String> map,int sysId) throws TbitsExceptionClient;
	
	public boolean deletecaption(String captionname,String captionvalue,int sysId)throws TbitsExceptionClient;
	
	public boolean updateDefaultCaptions(HashMap<String,String> map) throws TbitsExceptionClient;
}
