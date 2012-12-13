package com.tbitsGlobal.jaguar.client.bulkupdate;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.service.UtilServiceAsync;

public interface BulkUpdateServiceAsync extends UtilServiceAsync{
	void bulkUpdate(String sysPrefix, List<TbitsTreeRequestData> models, AsyncCallback<HashMap<Integer, TbitsTreeRequestData>> callback);
}
