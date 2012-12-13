package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.List;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnDistList;
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
 * Panel to hold distribution list grid
 * @author devashish
 *
 */
public class DistListPanel extends AbstractAdminBulkUpdatePanel<TrnDistList>{
	
	private TrnProcess currentProcess;
	
	public DistListPanel() {
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

	protected void onSave(List<TrnDistList> models, final Button btn) {
		if(currentProcess != null){
			btn.setText("Saving... Please Wait...");
			btn.disable();
			
			TbitsInfo.info("Saving... Please Wait...");
			TrnAdminConstants.trnAdminService.saveDistLists(currentProcess, models, new AsyncCallback<List<TrnDistList>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error saving distribution list", caught);
					Log.error("Error saving distribution list", caught);
					btn.setText("Save");
					btn.enable();
				}
	
				public void onSuccess(List<TrnDistList> result) {
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
					btn.setText("Save");
					btn.enable();
				}
			});
		}
	}

	public void refresh(int page) {
		if(currentProcess != null){
			TbitsInfo.info("Loading... Please Wait");
			TrnAdminConstants.trnAdminService.getDistList(currentProcess, new AsyncCallback<List<TrnDistList>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error fetching distribution list", caught);
					Log.error("Error fetching distribution list", caught);
				}

				public void onSuccess(List<TrnDistList> result) {
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
				}
			});
		}
	}

	public TrnDistList getEmptyModel() {
		return new TrnDistList();
	}

	protected BulkUpdateGridAbstract<TrnDistList> getNewBulkGrid(BulkGridMode mode) {
		return new DistListGrid(mode);
	}

}
