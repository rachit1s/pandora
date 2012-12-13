package admin.com.tbitsglobal.admin.client.trn.bulkgridpanels;

import java.util.List;

import admin.com.tbitsglobal.admin.client.AbstractAdminBulkUpdatePanel;
import admin.com.tbitsglobal.admin.client.AdminUtils;
import admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers.PostProcessCommonBulkGridContainer;
import admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers.PostProcessSingleBulkGridContainer;
import admin.com.tbitsglobal.admin.client.trn.bulkgrids.PostProcessBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnPostProcessValue;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcess;
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

public class PostProcessBulkUpdateGridPanel extends AbstractAdminBulkUpdatePanel<TrnPostProcessValue>{
	
	private TrnProcess currentProcess;
	
	public PostProcessBulkUpdateGridPanel() {
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
	public AbstractCommonBulkGridContainer<TrnPostProcessValue> getCommonBulkUpdateGridContainer(
			UIContext context) {
		PostProcessBulkGrid bulkGrid = new PostProcessBulkGrid(PostProcessBulkGrid.MODE_COMMON);
		PostProcessCommonBulkGridContainer commonGridContainer = new PostProcessCommonBulkGridContainer(context, bulkGrid);
		return commonGridContainer;
	}

	@Override
	public AbstractIndividualBulkGridContainer<TrnPostProcessValue> getIndividualBulkUpdateGridContainer() {
		PostProcessBulkGrid bulkGrid = new PostProcessBulkGrid(PostProcessBulkGrid.MODE_INDIVIDUAL);
		PostProcessSingleBulkGridContainer singleGridContainer = new PostProcessSingleBulkGridContainer(bulkGrid);
		return singleGridContainer;
	}

	@Override
	public void onSave(List<TrnPostProcessValue> models, final Button btn) {
		if(currentProcess != null){
			btn.setText("Saving... Please Wait...");
			btn.disable();
			
			AdminUtils.dbService.savePostProcessFieldValues(currentProcess, models, new AsyncCallback<List<TrnPostProcessValue>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.write("Error saving post process field values", TbitsInfo.ERROR);
					Log.error("Error saving post process field values", caught);
					btn.setText("Save");
					btn.enable();
				}
	
				public void onSuccess(List<TrnPostProcessValue> result) {
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
			AdminUtils.dbService.getPostProcessFieldValues(currentProcess, new AsyncCallback<List<TrnPostProcessValue>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.write("Error fetching post process parameters", TbitsInfo.ERROR);
					Log.error("Error fetching post process parameters", caught);
				}

				public void onSuccess(List<TrnPostProcessValue> result) {
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
				}});
		}
	}

	@Override
	public TrnPostProcessValue getEmptyModel() {
		return new TrnPostProcessValue();
	}

}
