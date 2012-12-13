package mom.com.tbitsGlobal.client.admin;

import java.util.ArrayList;
import java.util.List;


import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.state.AppState;

import mom.com.tbitsGlobal.client.MOMAdminConstants;
import mom.com.tbitsGlobal.client.admin.models.MOMTemplate;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * Panel to hold the grid which contains mom template columns
 * @author devashish
 *
 */

public class MOMTemplatesPanel extends AbstractAdminBulkUpdatePanel<MOMTemplate> {

	private BusinessAreaClient currentBa;
	
	public MOMTemplatesPanel(){
		super();
		
		commonGridDisabled = true;
		isExcelImportSupported = false;
		canAddRows			= true;
		canReorderRows		= false;
		canCopyPasteRows	= false;
		
		fetchMOMBA();
	}
	
	protected void fetchMOMBA(){
		
		MOMAdminConstants.momAdminService.getMOMBA(new AsyncCallback<List<BusinessAreaClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not fetch the list of MOM BA's from database...", caught);
				Log.error("Could not fetch the list of MOM BA's from database...", caught);
			}
			
			public void onSuccess(List<BusinessAreaClient> result) {
				if(null == result){
					TbitsInfo.info("MOM has not been configured for any Business Area");
					return;
				}else{
					insertBACombo(result);
				}
			}
		});
	}
	
	protected void insertBACombo(List<BusinessAreaClient> baList){
		ComboBox<BusinessAreaClient> bacombo = this.getBACombo();
		bacombo.getStore().removeAll();
		bacombo.getStore().add(baList);
		
		bacombo.setEmptyText("Please Select a BA");
		bacombo.setWidth(300);
		bacombo.addSelectionChangedListener(new SelectionChangedListener<BusinessAreaClient>(){
			public void selectionChanged(SelectionChangedEvent<BusinessAreaClient> se) {
				currentBa = se.getSelectedItem();
				((MOMTemplatesGrid)commonGridContainer.getBulkGrid()).setMOMBa(currentBa);
				((MOMTemplatesGrid)singleGridContainer.getBulkGrid()).setMOMBa(currentBa);
				refresh(0);
			}
		});
		
		toolbar.add(bacombo);
	}

	protected boolean isValidInput(List<MOMTemplate> models){
		for(MOMTemplate entry : models){
			if(null != entry.getIsMeeting()){
				if(((entry.getIsMeeting() != 1) && (entry.getIsMeeting() == 0)) || ((entry.getIsMeeting() == 1) && (entry.getIsMeeting() != 0))){
					continue;
				}else{
					TbitsInfo.error("Invalid value in 'Is Meeting' Column... Enter either 0 or 1");
					return false;
				}
			}
		}
		return true;
	}

	protected void onAdd() {
		if(null == currentBa){
			TbitsInfo.error("Please select a BA before adding rows.");
			return;
		}
		super.onAdd();
	}
	
	protected void onSave(List<MOMTemplate> models, final Button btn) {
		if(null != currentBa){
			
			if(!isValidInput(models)){
				return;
			}
			
			btn.setText("Saving... Please Wait...");
			btn.disable();
			
			for(MOMTemplate entry : models){
				entry.setBa(currentBa);
			}
			MOMAdminConstants.momAdminService.setMomTemplateProperties(models, new AsyncCallback<List<MOMTemplate>>(){

				public void onFailure(Throwable caught) {
					TbitsInfo.info("Error MOM Templates... See logs for more information...", caught);
					Log.error("Error saving MOM Templates", caught);
					btn.setText("Save");
					btn.enable();
				}

				public void onSuccess(List<MOMTemplate> result) {
					TbitsInfo.info("Successfully Saved Transmittal Properties to database...");
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
					btn.setText("Save");
					btn.enable();
				}
			});
		}
		
	}

	public void refresh(int page) {
		if(null != currentBa){
			TbitsInfo.info("Loading Properties... Please wait...");
			MOMAdminConstants.momAdminService.getMOMTemplatesForBa(currentBa, new AsyncCallback<List<MOMTemplate>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Could not retreive MOM Template properties for specified BA...");
					Log.error("Could not retreive MOM Template properties for specified BA...", caught);
				}
				
				public void onSuccess(List<MOMTemplate> result) {
					if(result.isEmpty()){
						TbitsInfo.info("No MOM Template Properties exist for the specified BA...");
						return;
					}
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
				}
			});
		}
	}
	

	protected BulkUpdateGridAbstract<MOMTemplate> getNewBulkGrid(BulkGridMode mode) {
		return new MOMTemplatesGrid(mode);
	}
	
	public MOMTemplate getEmptyModel() {
		return new MOMTemplate();
	}
	
	/**
	 * Get Business area combobox
	 * @return
	 */
	public  ComboBox<BusinessAreaClient> getBACombo(){
		ListStore<BusinessAreaClient> baStore = new ListStore<BusinessAreaClient>();
		final ComboBox<BusinessAreaClient> baCombo = new ComboBox<BusinessAreaClient>();
		baCombo.setStore(baStore);
		baCombo.setDisplayField(BusinessAreaClient.SYSTEM_PREFIX);
		baCombo.setTemplate(getBATemplate());
		baCombo.setEmptyText("Select Source BA For New Process ");
		
//		APConstants.apService.getBAList(new AsyncCallback<List<BusinessAreaClient>>(){
//			public void onFailure(Throwable caught) {
//				TbitsInfo.error("Error fetching business area list from database...", caught);
//				Log.error("Error fetching business area list from database...", caught);
//				caught.printStackTrace();
//			}
//			public void onSuccess(List<BusinessAreaClient> result) {
//				baCombo.getStore().add(result);
//			}});
		
		
		if(AppState.checkAppStateIsTill(AppState.BAMapReceived)){
			BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
			List<BusinessAreaClient> baList = new ArrayList<BusinessAreaClient>(cache.getValues());
			if((baList != null) && (!baList.isEmpty())){
				for(BusinessAreaClient entry : baList){
					baStore.add(entry);
				}
			}else{
				TbitsInfo.error("Could not get the list of business areas... Please refresh....");
				Log.error("Error while getting list of ba's");
			}
		}
		
		return baCombo;
	}
	
	private  native String getBATemplate() /*-{ 
	return  [ 
	'<tpl for=".">', 
	'<div class="x-combo-list-item">{display_name} [{system_prefix}] [{system_id}]</div>', 
	'</tpl>' 
	].join(""); 
}-*/; 

}
