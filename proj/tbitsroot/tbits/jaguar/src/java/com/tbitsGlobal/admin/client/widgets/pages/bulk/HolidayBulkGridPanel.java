package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.List;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.modelData.HolidayClient;
import com.tbitsGlobal.admin.client.utils.APConstants;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class HolidayBulkGridPanel extends AbstractAdminBulkUpdatePanel<HolidayClient>{

	public HolidayBulkGridPanel() {
		super();
		
		canReorderRows = false;
	}
	
	@Override
	public void refresh(int page) {
		singleGridContainer.removeAllModels();
		TbitsInfo.info("Fetching Holiday List from database... Please Wait...");
		APConstants.apService.getHolidayList(new AsyncCallback<List<HolidayClient>>() {
			@Override
			public void onSuccess(List<HolidayClient> result) {
				if(result != null){
					singleGridContainer.addModel(result);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to fetch Holiday List.. Please see logs for Details..", caught);
				Log.error("Unable to fetch Holiday List.. Please see logs for Details..", caught);
			}
		});
	}

	@Override
	protected void onSave(List<HolidayClient> models, Button btn) {
		APConstants.apService.updateHolidayList(models, new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				if(result){
					TbitsInfo.info("Successfully update Holiday List");
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to update Holiday List.. Please see logs for Details..", caught);
				Log.error("Unable to update Holiday List.. Please see logs for Details..", caught);
			}
		});
	}

	@Override
	public HolidayClient getEmptyModel() {
		return new HolidayClient();
	}

	@Override
	protected BulkUpdateGridAbstract<HolidayClient> getNewBulkGrid(
			BulkGridMode mode) {
		return new HolidayBulkGrid(mode);
	}

}
