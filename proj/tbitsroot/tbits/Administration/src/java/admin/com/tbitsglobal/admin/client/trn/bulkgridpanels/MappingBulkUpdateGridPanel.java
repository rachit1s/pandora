package admin.com.tbitsglobal.admin.client.trn.bulkgridpanels;

import java.util.List;

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

import admin.com.tbitsglobal.admin.client.AbstractAdminBulkUpdatePanel;
import admin.com.tbitsglobal.admin.client.AdminUtils;
import admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers.MappingCommonBulkGridContainer;
import admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers.MappingSingleBulkGridContainer;
import admin.com.tbitsglobal.admin.client.trn.bulkgrids.MappingBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnFieldMapping;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcess;

public class MappingBulkUpdateGridPanel extends AbstractAdminBulkUpdatePanel<TrnFieldMapping>{

	private TrnProcess currentProcess;
	
	public MappingBulkUpdateGridPanel() {
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
	public AbstractCommonBulkGridContainer<TrnFieldMapping> getCommonBulkUpdateGridContainer(
			UIContext context) {
		MappingBulkGrid bulkGrid = new MappingBulkGrid(MappingBulkGrid.MODE_COMMON);
		MappingCommonBulkGridContainer commonGridContainer = new MappingCommonBulkGridContainer(context, bulkGrid);
		return commonGridContainer;
	}

	@Override
	public AbstractIndividualBulkGridContainer<TrnFieldMapping> getIndividualBulkUpdateGridContainer() {
		MappingBulkGrid bulkGrid = new MappingBulkGrid(MappingBulkGrid.MODE_INDIVIDUAL);
		MappingSingleBulkGridContainer singleGridContainer = new MappingSingleBulkGridContainer(bulkGrid);
		return singleGridContainer;
	}

	@Override
	public void onSave(List<TrnFieldMapping> models, final Button btn) {
		if(currentProcess != null){
			btn.setText("Saving... Please Wait...");
			btn.disable();
			
			AdminUtils.dbService.saveMapValues(currentProcess, models, new AsyncCallback<List<TrnFieldMapping>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.write("Error saving post process field values", TbitsInfo.ERROR);
					Log.error("Error saving post process field values", caught);
					btn.setText("Save");
					btn.enable();
				}
	
				public void onSuccess(List<TrnFieldMapping> result) {
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
			AdminUtils.dbService.getMapValues(currentProcess, new AsyncCallback<List<TrnFieldMapping>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.write("Error fetching Source Target field Mapping", TbitsInfo.ERROR);
					Log.error("Error fetching Source Target field Mapping", caught);
				}

				public void onSuccess(List<TrnFieldMapping> result) {
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
				}});
		}
	}

	@Override
	public TrnFieldMapping getEmptyModel() {
		return new TrnFieldMapping();
	}

}
