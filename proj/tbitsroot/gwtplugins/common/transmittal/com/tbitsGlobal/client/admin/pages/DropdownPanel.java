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
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnDropdown;

/**
 * Panel to hold the dropdown Grid
 * @author devashish
 *
 */
public class DropdownPanel extends AbstractAdminBulkUpdatePanel<TrnDropdown> {

	private BusinessAreaClient currentBa;
	
	public DropdownPanel(){
		super();
		
		commonGridDisabled	 = true;
		isExcelImportSupported = false;
		canAddRows			= false;
		canReorderRows		= false;
		canCopyPasteRows	= false;
		canDeleteRows		= false;
		
		ComboBox<BusinessAreaClient> bacombo = TrnAdminUtils.getBACombo();
		bacombo.setEmptyText("Please Select a BA");
		bacombo.setWidth(300);
		bacombo.addSelectionChangedListener(new SelectionChangedListener<BusinessAreaClient>(){
			public void selectionChanged(SelectionChangedEvent<BusinessAreaClient> se) {
				currentBa = se.getSelectedItem();
				((DropdownGrid)commonGridContainer.getBulkGrid()).setSrcBA(currentBa);
				((DropdownGrid)singleGridContainer.getBulkGrid()).setSrcBA(currentBa);
				refresh(0);
			}
		});
		
		toolbar.add(bacombo);
	}
	
	@Override
	public void refresh(int page) {
		if(null != currentBa){
			TbitsInfo.info("Loading... Please Wait...");
			TrnAdminConstants.trnAdminService.getDropdownTable(currentBa, new AsyncCallback<List<TrnDropdown>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error Fetchin Dropdown list for selected ba...", caught);
					Log.error("Error Fetching dropdown list for selected ba...", caught);
				}
				
				public void onSuccess(List<TrnDropdown> result) {
					if(result.isEmpty()){
						TbitsInfo.info("No Transmittal Dropdown Exists for selected business area...");
						return;
					}else{
						singleGridContainer.removeAllModels();
						singleGridContainer.addModel(result);
					}
				}
			});
		}
	}
	
	protected void onSave(List<TrnDropdown> models, final Button btn) {
		if(null != currentBa){
			btn.setText("Saving... Please Wait...");
			btn.disable();
			
			for(TrnDropdown entry : models){
				entry.setSrcBa(currentBa);
			}
			
			TrnAdminConstants.trnAdminService.saveDropdownTable(currentBa, models, new AsyncCallback<List<TrnDropdown>>(){

				public void onFailure(Throwable caught) {
					TbitsInfo.info("Error Saving Dropdown Table to database...", caught);
					Log.error("Error Saving Dropdown Table to database...", caught);
				}

				public void onSuccess(List<TrnDropdown> result) {
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
					btn.setText("Save");
					btn.enable();
					
				}
			});
		}
	}

	public TrnDropdown getEmptyModel() {
		return new TrnDropdown();
	}

	protected BulkUpdateGridAbstract<TrnDropdown> getNewBulkGrid(BulkGridMode mode) {
		return new DropdownGrid(mode);
	}

}
