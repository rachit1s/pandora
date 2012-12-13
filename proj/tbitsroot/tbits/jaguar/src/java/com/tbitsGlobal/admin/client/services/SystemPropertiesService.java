package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.tbitsGlobal.admin.client.modelData.SysInfoClient;

public interface SystemPropertiesService extends RemoteService {
	public ArrayList<SysInfoClient> getSysInfo();
}
