package admin.com.tbitsglobal.admin.client.trn.bulkgridpanels;

import java.util.List;

import admin.com.tbitsglobal.admin.client.AbstractAdminBulkUpdatePanel;
import admin.com.tbitsglobal.admin.client.AdminUtils;
import admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers.ProcessParamCommonGridContainer;
import admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers.ProcessParamsSingleBulkGridContainer;
import admin.com.tbitsglobal.admin.client.trn.bulkgrids.ProcessParamsBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcess;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcessParam;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractCommonBulkGridContainer;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractIndividualBulkGridContainer;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public class ProcessParamsBulkUpdateGridPanel extends AbstractAdminBulkUpdatePanel<TrnProcessParam> {
	
	private TrnProcess currentProcess;
	
	public ProcessParamsBulkUpdateGridPanel() {
		super();
		
		ComboBox<TrnProcess> processCombo = AdminUtils.getTransmittalProcessesCombo();
		processCombo.addSelectionChangedListener(new SelectionChangedListener<TrnProcess>(){
			@Override
			public void selectionChanged(SelectionChangedEvent<TrnProcess> se) {
				currentProcess = se.getSelectedItem();
				refresh();
			}});
		toolbar.add(processCombo);
	}
	
	@Override
	public AbstractCommonBulkGridContainer<TrnProcessParam> getCommonBulkUpdateGridContainer(
			UIContext context) {
		ProcessParamsBulkGrid bulkGrid = new ProcessParamsBulkGrid(ProcessParamsBulkGrid.MODE_COMMON);
		ProcessParamCommonGridContainer commonGridContainer = new ProcessParamCommonGridContainer(context, bulkGrid);
		return commonGridContainer;
	}

	@Override
	public AbstractIndividualBulkGridContainer<TrnProcessParam> getIndividualBulkUpdateGridContainer() {
		ProcessParamsBulkGrid bulkGrid = new ProcessParamsBulkGrid(ProcessParamsBulkGrid.MODE_INDIVIDUAL);
		ProcessParamsSingleBulkGridContainer singleGridContainer = new ProcessParamsSingleBulkGridContainer(bulkGrid);
		return singleGridContainer;
	}

	@Override
	public void onSave(List<TrnProcessParam> models, final Button btn) {
		if(currentProcess != null){
			btn.setText("Saving... Please Wait...");
			btn.disable();
			
			AdminUtils.dbService.saveProcessParams(currentProcess, models, new AsyncCallback<List<TrnProcessParam>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.write("Error saving process params", TbitsInfo.ERROR);
					Log.error("Error saving process params", caught);
					btn.setText("Save");
					btn.enable();
				}
	
				public void onSuccess(List<TrnProcessParam> result) {
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
					btn.setText("Save");
					btn.enable();
				}});
		}
	}

	@Override
	public void refresh() {
		if(currentProcess != null){
			AdminUtils.dbService.getProcessParams(currentProcess, new AsyncCallback<List<TrnProcessParam>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.write("Error fetching process parameters", TbitsInfo.ERROR);
					Log.error("Error fetching process parameters", caught);
				}
	
				public void onSuccess(List<TrnProcessParam> result) {
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
				}});
		}
	}

	@Override
	public TrnProcessParam getEmptyModel() {
		return new TrnProcessParam();
	}

}
