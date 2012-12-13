package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.List;

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

import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;
import transmittal.com.tbitsGlobal.client.models.TrnValidationRule;

public class ValidationRulesPanel extends AbstractAdminBulkUpdatePanel<TrnValidationRule> {

	private TrnProcess currentProcess;
	
	public ValidationRulesPanel(){
		super();
		commonGridDisabled 	= true;
		isExcelImportSupported = false;
		canAddRows			= true;
		canReorderRows		= false;
		canCopyPasteRows	= false;
		
		ComboBox<TrnProcess> processCombo = TrnAdminUtils.getTransmittalProcessesCombo();
		processCombo.addSelectionChangedListener(new SelectionChangedListener<TrnProcess>(){
			@Override
			public void selectionChanged(SelectionChangedEvent<TrnProcess> se) {
				currentProcess = se.getSelectedItem();
				((ValidationRulesGrid)commonGridContainer.getBulkGrid()).setSrcBA(currentProcess.getSrcBA());
				((ValidationRulesGrid)singleGridContainer.getBulkGrid()).setSrcBA(currentProcess.getSrcBA());
				refresh(0);
			}});
		toolbar.add(processCombo);
	}
	
	protected void onSave(List<TrnValidationRule> models, final Button btn) {
		if(null != currentProcess){
			btn.setText("Saving... Please Wait...");
			btn.disable();
			
			for(TrnValidationRule entry : models){
				if(null == entry.getSrcBa()){
					entry.setSrcBa(((ValidationRulesGrid)commonGridContainer.getBulkGrid()).getSrcBA());
					entry.setSrcBa(((ValidationRulesGrid)singleGridContainer.getBulkGrid()).getSrcBA());
				}
				if(null == entry.getProcess()){
					entry.setProcess(currentProcess);
				}
			}
			
			TrnAdminConstants.trnAdminService.saveValidationRulesForProcess(currentProcess, models, new AsyncCallback<List<TrnValidationRule>>(){

				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error Saving Validation Rules to database...", caught);
					Log.error("Error Saving Validation Rules to database...", caught);
				}

				public void onSuccess(List<TrnValidationRule> result) {
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
					btn.setText("Save");
					btn.enable();
					
				}
				
			});
		}
	}

	public void refresh(int page) {
		if(null != currentProcess){
			TbitsInfo.info("Loading... Please Wait...");
			TrnAdminConstants.trnAdminService.getValidationRulesForProcess(currentProcess, new AsyncCallback<List<TrnValidationRule>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error fetching validation rules from database...", caught);
					Log.error("Error fetching validation rules from database...", caught);
				}
				
				public void onSuccess(List<TrnValidationRule> result) {
					if(result.isEmpty()){
						TbitsInfo.info("No Validation Rules Exist for the specified process...");
					}else{
						singleGridContainer.removeAllModels();
						singleGridContainer.addModel(result);
					}
				}
			});
		}
	}
	
	public TrnValidationRule getEmptyModel() {
		return new TrnValidationRule();
	}

	protected BulkUpdateGridAbstract<TrnValidationRule> getNewBulkGrid(BulkGridMode mode) {
		return new ValidationRulesGrid(mode);
	}

}
