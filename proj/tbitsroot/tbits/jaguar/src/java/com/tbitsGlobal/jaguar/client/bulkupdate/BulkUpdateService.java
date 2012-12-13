package com.tbitsGlobal.jaguar.client.bulkupdate;

import java.util.HashMap;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.service.UtilService;

public interface BulkUpdateService extends UtilService {
	/**
	 * Performs Bulk Update
	 * @param sysPrefix
	 * @param models
	 * @return
	 * @throws TbitsExceptionClient
	 */
	HashMap<Integer, TbitsTreeRequestData> bulkUpdate(String sysPrefix, List<TbitsTreeRequestData> models) throws TbitsExceptionClient;
}
