package transmittal.com.tbitsGlobal.client.admin;

import java.util.ArrayList;
import java.util.List;

import transmittal.com.tbitsGlobal.client.models.TrnProcess;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.state.AppState;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * Utility functions for Transmittal Admin Panel
 * @author devashish
 *
 */
public class TrnAdminUtils implements TrnAdminConstants{

	/**
	 * Check if the business area specified by "either" sysprefix or sysId exists or not. If only one value is known,
	 * put the other one as null
	 * @param baSysPrefix 	- sysprefix of destination ba
	 * @param baSysId 		- sysid of destination ba
	 * @return
	 */
	public static BusinessAreaClient getBA(String baSysPrefix, Integer baSysId){
		if(AppState.checkAppStateIsTill(AppState.BAMapReceived)){
			BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
			List<BusinessAreaClient> baList = new ArrayList<BusinessAreaClient>(cache.getValues());
			if((baList != null) && (!baList.isEmpty())){
				for(BusinessAreaClient entry : baList){
					if(null != baSysPrefix){
						if(baSysPrefix.toLowerCase().trim().equals(entry.getSystemPrefix().toLowerCase().trim()))
							return entry;
					}
					if(null != baSysId){
						if(baSysId == entry.getSystemId())
							return entry;
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
	 * Get Business area combobox
	 * @return
	 */
	public static ComboBox<BusinessAreaClient> getBACombo(){
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
	
	/**
	 * Get Fields combobox with empty store
	 * @return
	 */
	public static ComboBox<BAField> getFieldsCombo(){
		final ListStore<BAField> fieldStore = new ListStore<BAField>();
		ComboBox<BAField> fieldCombo = new ComboBox<BAField>();
		fieldCombo.setStore(fieldStore);
		fieldCombo.setDisplayField(BAField.DISPLAY_NAME);
		return fieldCombo;
	}
	
	
	public static ComboBox<BAField> getFieldsCombo(String label, String name, ArrayList<BAField> fieldList){
		ComboBox<BAField> fieldCombo = getFieldsCombo();
		ListStore<BAField> fieldStore = fieldCombo.getStore();
		if(fieldList != null)
			fieldStore.add(fieldList);
		return fieldCombo;
	}
	
	/**
	 * Get a Transmittal Processes Combobox
	 * @return
	 */
	public static ComboBox<TrnProcess> getTransmittalProcessesCombo(){
		final ListStore<TrnProcess> transmittalStore = new ListStore<TrnProcess>();
		TrnAdminConstants.trnAdminService.getTransmittalProcesses(new AsyncCallback<List<TrnProcess>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error fetching processes", caught);
				Log.error("Error fetching processes", caught);
			}

			public void onSuccess(List<TrnProcess> result) {
				if(result != null){
					transmittalStore.add(result);
				}
			}});
		ComboBox<TrnProcess> processCombo = new ComboBox<TrnProcess>();
		processCombo.setWidth(400);
		processCombo.setStore(transmittalStore);
		processCombo.setDisplayField(TrnProcess.PROCESS_ID);
		processCombo.setTemplate(getProcessTemplate());
		processCombo.setEmptyText("Select a Transmittal Process");
		return processCombo;
	}
	
	public static boolean isProcessExists(Integer trnProcessId){
		final List<TrnProcess> processList = new ArrayList<TrnProcess>();
		
		TrnAdminConstants.trnAdminService.getTransmittalProcesses(new AsyncCallback<List<TrnProcess>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error fetching processes", caught);
				Log.error("Error fetching processes", caught);
			}

			public void onSuccess(List<TrnProcess> result) {
				if(result != null){
					processList.addAll(result);
				}
			}
		});
		
		for(TrnProcess entry : processList){
			if(entry.getProcessId() == trnProcessId){
				return true;
			}
		}
		return false;
	}
	
	public static NumberField getSortOrderField(String label, String name){
		NumberField sortField = new NumberField();
		sortField.setFieldLabel(label);
		sortField.setName(name);
		return sortField;
	}
	
	/**
	 * Get the column config for a Number Field
	 * @param config
	 * @return
	 */
	public static ColumnConfig getIntegerColConfig(ColumnConfig config){
		final TextField<String> field = new TextField<String>();
		field.setAllowBlank(false);
		config.setEditor(new CellEditor(field){
			public Object postProcessValue(Object value) {
				try{
					if(value instanceof String)
						return Integer.parseInt((String) value);
					return (Integer)value;
				}catch(Exception e){
					return 0;
				}
			}
			
			public Object preProcessValue(Object value) {
				if(value instanceof Integer){
					return (Integer)value + "";
				}
				return super.preProcessValue(value);
			}
		});
		return config;
	}
	
	private static native String getBATemplate() /*-{ 
		return  [ 
		'<tpl for=".">', 
		'<div class="x-combo-list-item">{display_name} [{system_prefix}] [{system_id}]</div>', 
		'</tpl>' 
		].join(""); 
	}-*/; 
	
	private static native String getProcessTemplate() /*-{ 
		return  [ 
		'<tpl for=".">', 
		'<div class="x-combo-list-item">{name} [{process_id}]</div>', 
		'</tpl>' 
		].join(""); 
	}-*/; 
}
