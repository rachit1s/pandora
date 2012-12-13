package admin.com.tbitsglobal.admin.client;

import java.util.ArrayList;
import java.util.List;

import admin.com.tbitsglobal.admin.client.trn.models.TrnProcess;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

public class AdminUtils implements AdminConstants{

	public static ComboBox<BusinessAreaClient> getBACombo(){
		ListStore<BusinessAreaClient> baStore = new ListStore<BusinessAreaClient>();
		final ComboBox<BusinessAreaClient> baCombo = new ComboBox<BusinessAreaClient>();
		baCombo.setStore(baStore);
		baCombo.setDisplayField(BusinessAreaClient.SYSTEM_PREFIX);
		baCombo.setTemplate(getBATemplate());
		
		dbService.getBAs(new AsyncCallback<List<BusinessAreaClient>>(){
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
			}

			public void onSuccess(List<BusinessAreaClient> result) {
				baCombo.getStore().add(result);
			}});
		
		return baCombo;
	}
	
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
	
	public static ComboBox<TrnProcess> getTransmittalProcessesCombo(){
		final ListStore<TrnProcess> transmittalStore = new ListStore<TrnProcess>();
		dbService.getTransmittalProcesses(new AsyncCallback<List<TrnProcess>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.write("Error fetching processes", TbitsInfo.ERROR);
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
		processCombo.setDisplayField(TrnProcess.NAME);
		processCombo.setTemplate(getProcessTemplate());
		return processCombo;
	}
	
	public static NumberField getSortOrderField(String label, String name){
		NumberField sortField = new NumberField();
		sortField.setFieldLabel(label);
		sortField.setName(name);
		return sortField;
	}
	
	private static native String getBATemplate() /*-{ 
		return  [ 
		'<tpl for=".">', 
		'<div class="x-combo-list-item">{display_name} [{system_prefix}]</div>', 
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
