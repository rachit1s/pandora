package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.List;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;
import transmittal.com.tbitsGlobal.client.models.TrnProcessParam;

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

/**
 * Panel for transmittal process parameters.
 * @author devashish
 *
 */
public class ProcessParamsPanel extends AbstractAdminBulkUpdatePanel<TrnProcessParam> {
	
	private TrnProcess currentProcess;
	
	public ProcessParamsPanel() {
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
	
	protected void onSave(List<TrnProcessParam> models, final Button btn) {
		if(currentProcess != null){
			btn.setText("Saving... Please Wait...");
			btn.disable();
			
			TbitsInfo.info("Saving... Please Wait...");
			TrnAdminConstants.trnAdminService.saveProcessParams(currentProcess, models, new AsyncCallback<List<TrnProcessParam>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.info("Error saving process params", caught);
					Log.error("Error saving process params", caught);
					btn.setText("Save");
					btn.enable();
				}

				public void onSuccess(List<TrnProcessParam> result) {
					TbitsInfo.info("Successfully Saved Transmittal Properties to database...");
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
			TrnAdminConstants.trnAdminService.getProcessParams(currentProcess, new AsyncCallback<List<TrnProcessParam>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.info("Error fetching process parameters", caught);
					Log.error("Error fetching process parameters", caught);
				}
	
				public void onSuccess(List<TrnProcessParam> result) {
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
				}});
		}
	}

	public TrnProcessParam getEmptyModel() {
		return new TrnProcessParam();
	}

	protected BulkUpdateGridAbstract<TrnProcessParam> getNewBulkGrid(BulkGridMode mode) {
		return new ProcessParamsGrid(mode);
	}

}
