package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.List;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnFieldMapping;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.log.Log;


public class SrcTargetFieldMapPanel extends AbstractAdminBulkUpdatePanel<TrnFieldMapping>{

	private TrnProcess currentProcess;
	
	public SrcTargetFieldMapPanel() {
		super();
		
		commonGridDisabled = true;
		isExcelImportSupported = false;
		canAddRows			= true;
		canReorderRows		= false;
		canCopyPasteRows	= false;
		
		ComboBox<TrnProcess> processCombo = TrnAdminUtils.getTransmittalProcessesCombo();
		processCombo.addSelectionChangedListener(new SelectionChangedListener<TrnProcess>(){
			public void selectionChanged(SelectionChangedEvent<TrnProcess> se) {
				currentProcess = se.getSelectedItem();
				refresh(0);
			}});
		toolbar.add(processCombo);
	}

	protected void onSave(List<TrnFieldMapping> models, final Button btn) {
		if(currentProcess != null){
			btn.setText("Saving... Please Wait...");
			btn.disable();
			for(TrnFieldMapping entry : models){
				entry.setProcessId(currentProcess.getProcessId());
			}
			
			TbitsInfo.info("Saving... Please Wait...");
			TrnAdminConstants.trnAdminService.saveSrcTargetFieldMap(currentProcess, models, new AsyncCallback<List<TrnFieldMapping>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error saving post process field values", caught);
					Log.error("Error saving post process field values", caught);
					btn.setText("Save");
					btn.enable();
				}
	
				public void onSuccess(List<TrnFieldMapping> result) {
					TbitsInfo.info("Successfully saved the table to database...");
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
					btn.setText("Save");
					btn.enable();
				}});
		}
	}

	public void refresh(int page) {
		if(currentProcess != null){
			TbitsInfo.info("Loading... Please Wait...");
			TrnAdminConstants.trnAdminService.getSrcTargetFieldMap(currentProcess, new AsyncCallback<List<TrnFieldMapping>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error fetching Source Target field Mapping", caught);
					Log.error("Error fetching Source Target field Mapping", caught);
				}

				public void onSuccess(List<TrnFieldMapping> result) {
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
				}});
		}
	}

	public TrnFieldMapping getEmptyModel() {
		return new TrnFieldMapping();
	}

	protected BulkUpdateGridAbstract<TrnFieldMapping> getNewBulkGrid(BulkGridMode mode) {
		return new SrcTargetFieldMapGrid(mode);
	}

}
