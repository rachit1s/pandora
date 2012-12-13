package com.tbitsGlobal.admin.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.tbitsGlobal.admin.client.modelData.HolidayClient;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;

public interface HolidayService extends RemoteService{
	public List<HolidayClient> getHolidayList() throws TbitsExceptionClient;
	
	public boolean updateHolidayList(List<HolidayClient> models) throws TbitsExceptionClient;
}
