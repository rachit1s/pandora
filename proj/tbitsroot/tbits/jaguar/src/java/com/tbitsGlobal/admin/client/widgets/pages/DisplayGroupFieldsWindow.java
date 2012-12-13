package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.DualListWindow;

public class DisplayGroupFieldsWindow extends DualListWindow<FieldClient>{

	private DisplayGroupClient displayGroup;
	
	public DisplayGroupFieldsWindow(DisplayGroupClient displayGroup, List<FieldClient> models,
			List<FieldClient> currentModels) {
		super(models, currentModels, FieldClient.DISPLAY_NAME, new String[]{FieldClient.DISPLAY_NAME, FieldClient.NAME}, "Save");
		
		this.displayGroup = displayGroup;
	}

	@Override
	protected ListView<FieldClient> createSourceList(List<FieldClient> models,
			List<FieldClient> currentModels, String displayProperty) {
		ListView<FieldClient> sourceList = new ListView<FieldClient>();
		sourceList.setBorders(false);
		sourceList.setDisplayProperty(displayProperty);
		
		final ListStore<FieldClient> sourceStore = new ListStore<FieldClient>();
		sourceStore.add(models);
		for(FieldClient pref : currentModels){
			FieldClient c = sourceStore.findModel(FieldClient.NAME, pref.getName());
			sourceStore.remove(c);
		}
		sourceStore.sort(FieldClient.DISPLAY_ORDER, SortDir.ASC);
		
		sourceList.setStore(sourceStore);
		
		return sourceList;
	}

	@Override
	protected EditorGrid<FieldClient> createTargetGrid(
			List<FieldClient> models, List<FieldClient> currentModels) {
		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();  
        
		CheckBoxSelectionModel<FieldClient> sm = new CheckBoxSelectionModel<FieldClient>();
		columns.add(sm.getColumn());
		
		
        columns.add(new ColumnConfig(FieldClient.DISPLAY_NAME, "Field", 150));
        
        ColumnModel cm = new ColumnModel(columns);
        
        ListStore<FieldClient> targetStore = new ListStore<FieldClient>();
        targetStore.add(ClientUtils.sort(currentModels, -1, -1, true));
		
        EditorGrid<FieldClient> targetGrid = new EditorGrid<FieldClient>(targetStore, cm);
		targetGrid.setBorders(false);
		targetGrid.setSelectionModel(sm);
		targetGrid.setLayoutData(new FitLayout());
		targetGrid.setAutoExpandColumn(FieldClient.DISPLAY_NAME);
		 
		return targetGrid;
	}

	
	protected void onSubmit() {
		List<FieldClient> fields = targetGrid.getStore().getModels();
		int index = 0;
		for(FieldClient field : fields){
			index++;
			field.setDisplayGroup(DisplayGroupFieldsWindow.this.displayGroup.getId());
			field.setDisplayOrder(index);
		}
		APConstants.apService.updateFields(ClientUtils.getSysPrefix(), fields, new AsyncCallback<List<FieldClient>>() {
			@Override
			public void onSuccess(List<FieldClient> result) {
				if(result != null){
					TbitsInfo.info("Fields added to display group succesfully");
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Fields could not be added to display groups.. please see logs for details..", caught);
				Log.error("Fields could not be added to display groups.. please see logs for details..", caught);
			}
		});
	}
}
