package admin.com.tbitsglobal.admin.client.trn.bulkgridpanels;

import java.util.List;

import admin.com.tbitsglobal.admin.client.AbstractAdminBulkUpdatePanel;
import admin.com.tbitsglobal.admin.client.AdminUtils;
import admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers.DistListCommonGridContainer;
import admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers.DistListSingleBulkGridContainer;
import admin.com.tbitsglobal.admin.client.trn.bulkgrids.DistListBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnDistList;
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

public class DistListBulkUpdateGridPanel extends AbstractAdminBulkUpdatePanel<TrnDistList>{
	
	private TrnProcess currentProcess;
	
	public DistListBulkUpdateGridPanel() {
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
	public AbstractCommonBulkGridContainer<TrnDistList> getCommonBulkUpdateGridContainer(
			UIContext context) {
		DistListBulkGrid bulkGrid = new DistListBulkGrid(DistListBulkGrid.MODE_COMMON);
		DistListCommonGridContainer commonGridContainer = new DistListCommonGridContainer(context, bulkGrid);
		return commonGridContainer;
	}

	@Override
	public AbstractIndividualBulkGridContainer<TrnDistList> getIndividualBulkUpdateGridContainer() {
		DistListBulkGrid bulkGrid = new DistListBulkGrid(DistListBulkGrid.MODE_INDIVIDUAL);
		DistListSingleBulkGridContainer singleGridContainer = new DistListSingleBulkGridContainer(bulkGrid);
		return singleGridContainer;
	}

	@Override
	public void onSave(List<TrnDistList> models, final Button btn) {
		if(currentProcess != null){
			btn.setText("Saving... Please Wait...");
			btn.disable();
			
			AdminUtils.dbService.saveDistLists(currentProcess, models, new AsyncCallback<List<TrnDistList>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.write("Error saving distribution list", TbitsInfo.ERROR);
					Log.error("Error saving distribution list", caught);
					btn.setText("Save");
					btn.enable();
				}
	
				public void onSuccess(List<TrnDistList> result) {
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
			AdminUtils.dbService.getDistList(currentProcess, new AsyncCallback<List<TrnDistList>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.write("Error fetching distribution list", TbitsInfo.ERROR);
					Log.error("Error fetching distribution list", caught);
				}

				public void onSuccess(List<TrnDistList> result) {
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
				}});
		}
	}

	@Override
	public TrnDistList getEmptyModel() {
		return new TrnDistList();
	}

}
