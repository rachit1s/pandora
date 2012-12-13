package com.tbitsGlobal.admin.client;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.bulkupdate.IBulkUpdatePanel;

public interface IAdminBulkUpdatePanel<M extends TbitsModelData> extends IBulkUpdatePanel<M> {
	public void refresh(int page);
}
