package admin.com.tbitsglobal.admin.client.trn.bulkgridpanels;

import java.util.List;

import admin.com.tbitsglobal.admin.client.AbstractAdminBulkUpdatePanel;
import admin.com.tbitsglobal.admin.client.AdminUtils;
import admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers.AttachmentListCommonGridContainer;
import admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers.AttachmentListIndividualGridContainer;
import admin.com.tbitsglobal.admin.client.trn.bulkgrids.AttachmentListBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnAttachmentList;
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

public class AttachmentListBulkUpdateGridPanel extends AbstractAdminBulkUpdatePanel<TrnAttachmentList>{
	private TrnProcess currentProcess;
	
	public AttachmentListBulkUpdateGridPanel() {
		super();
		

		ComboBox<TrnProcess> processCombo = AdminUtils.getTransmittalProcessesCombo();
		processCombo.addSelectionChangedListener(new SelectionChangedListener<TrnProcess>(){
			@Override
			public void selectionChanged(SelectionChangedEvent<TrnProcess> se) {
				currentProcess = se.getSelectedItem();
				((AttachmentListBulkGrid)commonGridContainer.getBulkGrid()).setSrcBA(currentProcess.getSrcBA());
				((AttachmentListBulkGrid)singleGridContainer.getBulkGrid()).setSrcBA(currentProcess.getSrcBA());
				refresh();
			}});
		toolbar.add(processCombo);
	}
	@Override
	public AbstractCommonBulkGridContainer<TrnAttachmentList> getCommonBulkUpdateGridContainer(
			UIContext context) {
		AttachmentListBulkGrid bulkGrid = new AttachmentListBulkGrid(AttachmentListBulkGrid.MODE_COMMON);
		AttachmentListCommonGridContainer commonGridContainer = new AttachmentListCommonGridContainer(context, bulkGrid);
		return commonGridContainer;
	}

	@Override
	public AbstractIndividualBulkGridContainer<TrnAttachmentList> getIndividualBulkUpdateGridContainer() {
		AttachmentListBulkGrid bulkGrid = new AttachmentListBulkGrid(AttachmentListBulkGrid.MODE_INDIVIDUAL);
		AttachmentListIndividualGridContainer singleGridContainer = new AttachmentListIndividualGridContainer(bulkGrid);
		return singleGridContainer;
	}

	@Override
	public void onSave(List<TrnAttachmentList> models, final Button btn) {
		if(currentProcess != null){
			btn.setText("Saving... Please Wait...");
			btn.disable();
			
			AdminUtils.dbService.saveAttachmentLists(currentProcess, models, new AsyncCallback<List<TrnAttachmentList>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.write("Error saving post process field values", TbitsInfo.ERROR);
					Log.error("Error saving post process field values", caught);
					btn.setText("Save");
					btn.enable();
				}
	
				public void onSuccess(List<TrnAttachmentList> result) {
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
			AdminUtils.dbService.getAttachmentList(currentProcess, new AsyncCallback<List<TrnAttachmentList>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.write("Error fetching Attachment List", TbitsInfo.ERROR);
					Log.error("Error fetching Attachment List", caught);
				}

				public void onSuccess(List<TrnAttachmentList> result) {
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
				}});
		}
	}
	
	@Override
	public TrnAttachmentList getEmptyModel() {
		return new TrnAttachmentList();
	}

}
