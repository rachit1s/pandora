package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.modelData.ReportParamClient;
import com.tbitsGlobal.admin.client.utils.APConstants;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.domainObjects.ReportClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class ReportParamsBulkGridPanel extends AbstractAdminBulkUpdatePanel<ReportParamClient>{

	private int reportId;
	
	public ReportParamsBulkGridPanel(int reportId, ListStore<ReportClient> store) {
		super();
		
		this.reportId = reportId;
		
		this.setHeaderVisible(false);
		this.setAnimCollapse(true);
		
		canCopyPasteRows = false;
		canReorderRows = false;
		
		toolbar.add(new LabelToolItem("Report : "));
		
		ComboBox<ReportClient> reportsCombo = new ComboBox<ReportClient>();
		reportsCombo.setStore(store);
		reportsCombo.setDisplayField(ReportClient.REPORT_NAME);
		reportsCombo.addSelectionChangedListener(new SelectionChangedListener<ReportClient>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<ReportClient> se) {
				ReportClient model = se.getSelectedItem();
				ReportParamsBulkGridPanel.this.reportId = model.getReportId();
				refresh(1);
			}
		});
		toolbar.add(reportsCombo);
		
		ReportClient model = store.findModel(ReportClient.REPORT_ID, reportId);
		if(model != null)
			reportsCombo.setValue(model);
	}
	
	@Override
	public void refresh(int page) {
		TbitsInfo.info("Fetching Report Parameters from database... Please Wait...");
		singleGridContainer.removeAllModels();
		APConstants.apService.getReportParams(reportId, new AsyncCallback<List<ReportParamClient>>() {
			@Override
			public void onSuccess(List<ReportParamClient> result) {
				if(result != null){
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not fetch report parameters.. Please see logs for details..", caught);
				Log.error("Could not fetch report parameters.. Please see logs for details..", caught);
			}
		});
	}

	@Override
	protected void onSave(List<ReportParamClient> models, Button btn) {
		TbitsInfo.info("Updating Report Parameters into database... Please Wait...");
		APConstants.apService.updateReportParams(reportId, models, new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				if(result){
					TbitsInfo.info("Succesfully updated report parameters..");
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				TbitsInfo.info("Could not update report parameters.. Please see logs for details..", caught);
				Log.info("Could not update report parameters.. Please see logs for details..", caught);
			}
		});
	}

	@Override
	public ReportParamClient getEmptyModel() {
		return new ReportParamClient();
	}

	@Override
	protected BulkUpdateGridAbstract<ReportParamClient> getNewBulkGrid(
			BulkGridMode mode) {
		return new ReportParamsBulkGrid(mode);
	}
	
	@Override
	protected void onAdd() {
		ReportParamClient model = getEmptyModel();
		model.setReportId(reportId);
		singleGridContainer.addModel(model);
	}
	
	@Override
	protected boolean beforeRemove() {
		List<ReportParamClient> selectedItems = singleGridContainer.getSelectedModels();
		if(selectedItems != null && selectedItems.size() > 0){
			return Window.confirm("Do you wish to delete " + selectedItems.size() + " parameters?");
		}
		
		return true;
	}

}
