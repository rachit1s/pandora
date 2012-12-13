package com.tbitsGlobal.admin.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.modelData.HolidayClient;

public interface HolidayServiceAsync {

	void getHolidayList(AsyncCallback<List<HolidayClient>> callback);

	void updateHolidayList(List<HolidayClient> models, AsyncCallback<Boolean> callback);

}
