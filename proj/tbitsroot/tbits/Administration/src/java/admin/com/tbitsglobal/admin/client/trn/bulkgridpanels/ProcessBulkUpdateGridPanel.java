package admin.com.tbitsglobal.admin.client.trn.bulkgridpanels;

import java.util.List;

import admin.com.tbitsglobal.admin.client.AbstractAdminBulkUpdatePanel;
import admin.com.tbitsglobal.admin.client.AdminUtils;
import admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers.ProcessCommonGridContainer;
import admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers.ProcessSingleBulkGridContainer;
import admin.com.tbitsglobal.admin.client.trn.bulkgrids.ProcessBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcess;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractCommonBulkGridContainer;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractIndividualBulkGridContainer;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public class ProcessBulkUpdateGridPanel extends AbstractAdminBulkUpdatePanel<TrnProcess>{
	public ProcessBulkUpdateGridPanel() {
		super();
	}
	
	@Override
	public AbstractCommonBulkGridContainer<TrnProcess> getCommonBulkUpdateGridContainer(UIContext context) {
		ProcessBulkGrid bulkGrid = new ProcessBulkGrid(ProcessBulkGrid.MODE_COMMON);
		ProcessCommonGridContainer commonGridContainer = new ProcessCommonGridContainer(context, bulkGrid);
		return commonGridContainer;
	}

	@Override
	public AbstractIndividualBulkGridContainer<TrnProcess> getIndividualBulkUpdateGridContainer() {
		ProcessBulkGrid bulkGrid = new ProcessBulkGrid(ProcessBulkGrid.MODE_INDIVIDUAL);
		ProcessSingleBulkGridContainer singleGridContainer = new ProcessSingleBulkGridContainer(bulkGrid);
		return singleGridContainer;
	}
	
	public void refresh(){
		AdminUtils.dbService.getTransmittalProcesses(new AsyncCallback<List<TrnProcess>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.write("Error fetching processes", TbitsInfo.ERROR);
				Log.error("Error fetching processes", caught);
			}

			public void onSuccess(List<TrnProcess> result) {
				singleGridContainer.removeAllModels();
				singleGridContainer.addModel(result);
			}});
	}

	@Override
	public void onSave(List<TrnProcess> models, final Button btn) {
		btn.setText("Saving... Please Wait...");
		btn.disable();
		
		AdminUtils.dbService.saveTransmittalProcesses(models, new AsyncCallback<List<TrnProcess>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.write("Error saving processes", TbitsInfo.ERROR);
				Log.error("Error saving processes", caught);
				btn.setText("Save");
				btn.enable();
			}

			public void onSuccess(List<TrnProcess> result) {
				singleGridContainer.removeAllModels();
				singleGridContainer.addModel(result);
				btn.setText("Save");
				btn.enable();
			}});
	}

	@Override
	public TrnProcess getEmptyModel() {
		return new TrnProcess();
	}
}
