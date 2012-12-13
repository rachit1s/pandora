package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.state.AppState;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnDropdown;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;
import transmittal.com.tbitsGlobal.client.models.TrnReplicateProcess;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class ReplicateProcessPanel extends AbstractAdminBulkUpdatePanel<TrnReplicateProcess> {

	private TrnProcess currentProcess;
	private BusinessAreaClient currentBa;
	private List<TrnProcess> processesList;
	private List<TrnDropdown> dropdownList;
	private Integer gridRowCount;
	
	public ReplicateProcessPanel(){
		commonGridDisabled = true;
		isExcelImportSupported = false;
		canAddRows			= false;
		canReorderRows		= false;
		canCopyPasteRows	= false;
		canDeleteRows		= false;
		processesList 	= new ArrayList<TrnProcess>();
		dropdownList	= new ArrayList<TrnDropdown>();
		gridRowCount	= 0;
		
		buildToolbar();
	}
	
	/**
	 * Get the dropdown values for all the business areas
	 */
	private void getDropdownValues(){
		
		TrnAdminConstants.trnAdminService.getAllDropdownEntries(new AsyncCallback<List<TrnDropdown>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error fetching dropdown values...", caught);
				Log.error("Error fetching dropdown values...", caught);
			}

			public void onSuccess(List<TrnDropdown> result) {
				if(null != result){
					dropdownList.addAll(result);
				}
			}
		});
	}
	
	/**
	 * Build the top toolbar
	 */
	protected void buildToolbar(){
		final ComboBox<BusinessAreaClient> bacombo = TrnAdminUtils.getBACombo();
		bacombo.setWidth(250);
		bacombo.addSelectionChangedListener(new SelectionChangedListener<BusinessAreaClient>(){
			public void selectionChanged(SelectionChangedEvent<BusinessAreaClient> se) {
				currentBa = se.getSelectedItem();
				refresh(0);
			}
		});
		
		final ComboBox<TrnProcess> processCombo = TrnAdminUtils.getTransmittalProcessesCombo();
		
		processCombo.addSelectionChangedListener(new SelectionChangedListener<TrnProcess>(){
			public void selectionChanged(SelectionChangedEvent<TrnProcess> se) {
				bacombo.clear();
				bacombo.setEmptyText("Select Source BA For New Process ");
				
				singleGridContainer.removeAllModels();
				commonGridContainer.removeAllModels();
				
				currentProcess = se.getSelectedItem();
				
				for(int i = 0 ; i < processCombo.getStore().getCount(); i++){
					processesList.add(processCombo.getStore().getAt(i));
				}
				
				getDropdownValues();
			}
		});
		
		toolbar.add(processCombo);
		/*
		LabelField filterLabel = new LabelField(" ");
		toolbar.add(filterLabel);
		*/
		toolbar.add(bacombo);
		
		ComboBox<BusinessAreaClient> lookupBaCombo = TrnAdminUtils.getBACombo();
		lookupBaCombo.setEmptyText("Lookup BA");
		
		toolbar.add(lookupBaCombo);
		
	
		
		
	}

	/**
	 * For each entry in the table, validate the entry
	 * @param models
	 * @return
	 */
	private boolean isValidInput(List<TrnReplicateProcess> models){
		/*
		 * If user has changed the number of rows in the grid, return false
		 */
		if(models.size() != gridRowCount){
			TbitsInfo.error("Cannot Remove rows... Please refresh and try again...");
			return false;
		}
		
		for(TrnReplicateProcess entry : models){
			
			if((null == entry.getParamValueNew()) || (entry.getParamValueNew().equals(""))){
				TbitsInfo.error("Empty value in one or more parameters...");
				return false;
			}
			if(entry.getParamName().contains(TrnReplicateProcess.PROCESS_ID)){
				if(!isValidInteger(entry.getParamValueNew(), entry.getParamName(), true))
					return false;
				if(!isValidTransmittalProcessId(Integer.valueOf(entry.getParamValueNew())))
					return false;
			}
			else if(entry.getParamName().contains(TrnReplicateProcess.DEST_BA)){
				if(!isValidInteger(entry.getParamValueNew(), entry.getParamName(), false)){
					/*
					 * First check if the sys_prefix is a valid sys_prefix or not
					 */
					if(!isValidBa(entry.getParamValueNew(), null)){
						Log.error("Invalid value of Sys_Prefix in 'Destination BA [Source Target Field Map]");
						return false;
					}
					/*
					 * Convert the sys_prefix into sys_id
					 */
					entry.setParamValueNew(Integer.toString(getBaSysId(entry.getParamValueNew())));
				}else if(!isValidBa(null, Integer.valueOf(entry.getParamValueNew()))){
					/*
					 * If the sys_id is not a valid sys_id, return 
					 */
					Log.error("Invalid value of SYS_ID in 'Destination BA [Source Target Field Map]");
					return false;
				}
			}else if(entry.getParamName().contains(TrnReplicateProcess.DEST_BA_POST_TRN)){
				if(!isValidInteger(entry.getParamValueNew(), entry.getParamName(), false)){
					/*
					 * First check if the sys_prefix is a valid sys_prefix or not
					 */
					if(!isValidBa(entry.getParamValueNew(), null)){
						Log.error("Invalid value of Sys_Prefix in 'Destination BA [Post Transmittal Field Map]");
						return false;
					}
					/*
					 * Convert the sys_prefix into sys_id
					 */
					entry.setParamValueNew(Integer.toString(getBaSysId(entry.getParamValueNew())));
				}else if(!isValidBa(null, Integer.valueOf(entry.getParamValueNew()))){
					/*
					 * If the sys_id is not a valid sys_id, return
					 */
					Log.error("Invalid value of Sys_ID in 'Destination BA [Post Transmittal Field Map]");
					return false;
				}
			}else if(entry.getParamName().contains(TrnReplicateProcess.DROPDOWN_ID)){
				if(!isValidInteger(entry.getParamValueNew(), entry.getParamName(), true))
					return false;
				if(!isValidDropdownId(Integer.valueOf(entry.getParamValueNew())))
					return false;
			}else if(entry.getParamName().contains(TrnReplicateProcess.DROPDOWN_NAME)){
				if((null == entry.getParamValueNew()) || (entry.getParamValueNew().equals(""))){
					TbitsInfo.info("Plese select a value for Dropdown Name");
					return false;
				}
			}else if(entry.getParamName().contains(TrnReplicateProcess.PROCESS_DESC)){
				if((null == entry.getParamValueNew()) || (entry.getParamValueNew().equals(""))){
					TbitsInfo.info("Plese select a value for Process Description");
					return false;
				}
			}else if(entry.getParamName().contains(TrnReplicateProcess.DTN_SYS_ID) || (entry.getParamName().contains(TrnReplicateProcess.DTR_SYS_ID))){
				if(!isValidInteger(entry.getParamValueNew(), entry.getParamName(), false)){
					/*
					 * First check if the sys_prefix is a valid sys_prefix or not
					 */
					if(!isValidBa(entry.getParamValueNew(), null)){
						Log.error("Invalid value of sys_prefix in " + entry.getParamName());
						return false;
					}
					/*
					 * Convert the sys_prefix into sys_id
					 */
					entry.setParamValueNew(Integer.toString(getBaSysId(entry.getParamValueNew())));
				}else if(!isValidBa(null, Integer.valueOf(entry.getParamValueNew()))){
					/*
					 * If the sys_id is not a valid sys_id return false
					 */
					Log.error("Invalid value of Sys_ID in " + entry.getParamName());
					return false;
				}
			}else if(entry.getParamName().contains(TrnReplicateProcess.MAX_KEY)){
				if((null == entry.getParamValueNew()) || (entry.getParamValueNew().equals(""))){
					TbitsInfo.info("Plese select a value for Max Key");
					return false;
				}
//				if(!isValidMaxKey(entry.getParamValueNew()))
//					return false;
			}
		}
		return true;
	}
	
	/**
	 * Ceck if the max key value is valid or not.
	 * @param maxKey
	 * @return
	 */
	private boolean isValidMaxKey(String maxKey){
		for(TrnProcess entry : processesList){
			if(maxKey.toLowerCase().trim().equals(entry.getSerialKey().toLowerCase().toString())){
				TbitsInfo.error("The new value of Max Serial Key already exists for the current Business Area...");
				return false;
			}
		}
		return true;
	}
	
	private boolean isValidDropdownId(Integer dropdownId){
		for(TrnDropdown entry : dropdownList){
			if(dropdownId == entry.getDropdownId()){
				TbitsInfo.error("The new value of Dropdown ID is already in use... Please select another value...");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Get an ID corresponding t sysprefix
	 * @param sysPrefix
	 * @return sysId
	 */
	private Integer getBaSysId(String sysPrefix){
		if(AppState.checkAppStateIsTill(AppState.BAMapReceived)){
			BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
			List<BusinessAreaClient> baList = new ArrayList<BusinessAreaClient>(cache.getValues());
			if((baList != null) && (!baList.isEmpty())){
				for(BusinessAreaClient entry : baList){
					if(null != sysPrefix){
						if(sysPrefix.toLowerCase().trim().equals(entry.getSystemPrefix().toLowerCase().trim()))
							return entry.getSystemId();
					}
				}
			}else{
				TbitsInfo.error("Could not get the list of business areas... Please refresh....");
				Log.error("Error while getting list of ba's");
			}
		}
		return null;
	}
	
	/**
	 * Check if the business area specified by "either" sysprefix or sysId exists or not. If only one value is known,
	 * put the other one as null
	 * @param baSysPrefix 	- sysprefix of destination ba
	 * @param baSysId 		- sysid of destination ba
	 * @return
	 */
	private boolean isValidBa(String baSysPrefix, Integer baSysId){
		if(AppState.checkAppStateIsTill(AppState.BAMapReceived)){
			BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
			List<BusinessAreaClient> baList = new ArrayList<BusinessAreaClient>(cache.getValues());
			if((baList != null) && (!baList.isEmpty())){
				for(BusinessAreaClient entry : baList){
					if(null != baSysPrefix){
						if(baSysPrefix.toLowerCase().trim().equals(entry.getSystemPrefix().toLowerCase().trim()))
							return true;
					}
					if(null != baSysId){
						if(baSysId == entry.getSystemId())
							return true;
					}
				}
			}else{
				TbitsInfo.error("Could not get the list of business areas... Please refresh....");
				Log.error("Error while getting list of ba's");
			}
		}
		return false;
	}
	
	/**
	 * Validate if the new value of transmittal process Id is already in use or not
	 * @param trnProcessId
	 * @return false, if the value is already in use
	 */
	private boolean isValidTransmittalProcessId(Integer trnProcessId){
		if(null == trnProcessId)
			return false;
		for(TrnProcess entry : processesList){
			if(trnProcessId == entry.getProcessId()){
				TbitsInfo.error("The New Value of Transmittal Process Id Is Already In Use... Please Select Another Value...");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Validate if the parameter is an integer or not
	 * @param paramValue
	 * @param paramName
	 * @return
	 */
	private boolean isValidInteger(String paramValue, String paramName, boolean showError){
		try{
			Integer.parseInt(paramValue);
		}catch (NumberFormatException ne){
			if(showError){
				TbitsInfo.error("Invalid value of " + paramName);
				Log.error("Invalid value of " + paramName, ne);
			}
			return false;
		}
		return true;
	}
	
	protected void onSave(List<TrnReplicateProcess> models, final Button btn) {
		if((null != currentProcess) && (null != currentBa)){
			btn.setText("Saving...");
			btn.disable();
			
			if(!isValidInput(models)){
				btn.setText("Save");
				btn.enable();
				return;
			}
			
			TrnAdminConstants.trnAdminService.copyProcess(models, currentProcess, currentBa, new AsyncCallback<List<TrnReplicateProcess>>(){

				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error replicating the specified process... Check logs for more information...", caught);
					Log.error(caught.getMessage(), caught);
					btn.setText("Save");
					btn.enable();
				}

				public void onSuccess(List<TrnReplicateProcess> result) {
					if(null != result){
						TbitsInfo.info("Successfully Replicated the Transmittal Process Parameters...");
						Log.info("Successfully Replicated the Transmittal Process Parameters...");
						singleGridContainer.removeAllModels();
						singleGridContainer.addModel(result);
						btn.setText("Save");
						btn.enable();
					}else{
						TbitsInfo.info("Server Error... Please see logs for more information...");
						Log.info("Server Error... Please see logs for more information...");
						btn.setText("Save");
						btn.enable();
					}
				}
			});
		}
	}

	public void refresh(int page) {
		if(null == currentProcess){
			TbitsInfo.info("Please select a Transmittal Process...");
		}
		if((null != currentProcess) && (null != currentBa)){
			TrnAdminConstants.trnAdminService.getProcessParams(currentProcess, currentBa, new AsyncCallback<List<TrnReplicateProcess>>(){

				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error Fetching Current Process Parameters from database...", caught);
					Log.error("Error Fetching Current Process Parameters from database...", caught);
				}

				public void onSuccess(List<TrnReplicateProcess> result) {
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
					gridRowCount = result.size();
				}
			});
		}
	}
	
	public TrnReplicateProcess getEmptyModel() {
		return new TrnReplicateProcess();
	}

	protected BulkUpdateGridAbstract<TrnReplicateProcess> getNewBulkGrid(BulkGridMode mode) {
		return new ReplicateProcessGrid(mode);
	}

}
