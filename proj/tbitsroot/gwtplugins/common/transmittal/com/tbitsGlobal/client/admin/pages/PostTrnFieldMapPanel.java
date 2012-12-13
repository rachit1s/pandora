package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.List;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnPostProcessValue;
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

/**
 * Panel to hold post transmittal field map grid
 * @author devashish
 *
 */
public class PostTrnFieldMapPanel extends AbstractAdminBulkUpdatePanel<TrnPostProcessValue>{
	
	private TrnProcess currentProcess;
	
	public PostTrnFieldMapPanel() {
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

	protected void onSave(List<TrnPostProcessValue> models, final Button btn) {
		if(currentProcess != null){
			btn.setText("Saving... Please Wait...");
			btn.disable();
			
			TbitsInfo.info("Saving... Please Wait...");
			TrnAdminConstants.trnAdminService.savePostProcessFieldValues(currentProcess, models, new AsyncCallback<List<TrnPostProcessValue>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error saving post process field values", caught);
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

	public void refresh(int page) {
		if(currentProcess != null){
			TbitsInfo.info("Loading... Please Wait...");
			TrnAdminConstants.trnAdminService.getPostProcessFieldValues(currentProcess, new AsyncCallback<List<TrnPostProcessValue>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error fetching post process parameters", caught);
					Log.error("Error fetching post process parameters", caught);
				}

				public void onSuccess(List<TrnPostProcessValue> result) {
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
				}
			});
		}
	}

	public TrnPostProcessValue getEmptyModel() {
		return new TrnPostProcessValue();
	}

	protected BulkUpdateGridAbstract<TrnPostProcessValue> getNewBulkGrid(BulkGridMode mode) {
		return new PostTrnFieldMapGrid(mode);
	}

}
