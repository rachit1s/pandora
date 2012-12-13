package com.tbitsGlobal.admin.client.widgets.pages.bulk;

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
import commons.com.tbitsGlobal.utils.client.domainObjects.BAMenuClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.DualListWindow;

public class BAMenuDualListWindow extends DualListWindow<BusinessAreaClient>{

	private BAMenuClient menu;
	
	public BAMenuDualListWindow(BAMenuClient menu, List<BusinessAreaClient> models,
			List<BusinessAreaClient> currentModels) {
		super(models, currentModels, BusinessAreaClient.SYSTEM_PREFIX, 
				new String[]{BusinessAreaClient.SYSTEM_PREFIX, BusinessAreaClient.DISPLAY_NAME},
				"Update");
		this.setHeading("Add/Delete Business Areas");
		this.menu = menu;
	}

	@Override
	protected ListView<BusinessAreaClient> createSourceList(
			List<BusinessAreaClient> models,
			List<BusinessAreaClient> currentModels, String displayProperty) {
		ListView<BusinessAreaClient> sourceList = new ListView<BusinessAreaClient>();
		sourceList.setBorders(false);
		sourceList.setDisplayProperty(displayProperty);
		
		final ListStore<BusinessAreaClient> sourceStore = new ListStore<BusinessAreaClient>();
		sourceStore.add(models);
		for(BusinessAreaClient pref : currentModels){
			BusinessAreaClient ba = sourceStore.findModel(BusinessAreaClient.SYSTEM_PREFIX, pref.getSystemPrefix());
			sourceStore.remove(ba);
		}
		sourceStore.sort(BusinessAreaClient.SYSTEM_PREFIX, SortDir.ASC);
		
		sourceList.setStore(sourceStore);
		
		return sourceList;
	}

	@Override
	protected EditorGrid<BusinessAreaClient> createTargetGrid(
			List<BusinessAreaClient> models,
			List<BusinessAreaClient> currentModels) {
		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();  
        
		CheckBoxSelectionModel<BusinessAreaClient> sm = new CheckBoxSelectionModel<BusinessAreaClient>();
		columns.add(sm.getColumn());
		
		
        columns.add(new ColumnConfig(BusinessAreaClient.SYSTEM_PREFIX, "BA", 150));
        
        ColumnModel cm = new ColumnModel(columns);
        
        ListStore<BusinessAreaClient> targetStore = new ListStore<BusinessAreaClient>();
        targetStore.add(ClientUtils.sort(currentModels, -1, -1, true));
		
        EditorGrid<BusinessAreaClient> targetGrid = new EditorGrid<BusinessAreaClient>(targetStore, cm);
		targetGrid.setBorders(false);
		targetGrid.setSelectionModel(sm);
		targetGrid.setLayoutData(new FitLayout());
		targetGrid.setAutoExpandColumn(BusinessAreaClient.SYSTEM_PREFIX);
		 
		return targetGrid;
	}

	@Override
	protected void onSubmit() {
		List<BusinessAreaClient> bas = targetGrid.getStore().getModels();
		APConstants.apService.updateBAMenuMapping(menu.getMenuId(), bas, new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				if(result){
					TbitsInfo.info("Business Areas added to menu succesfully");
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Business Areas could not be added to menu.. please see logs for details..", caught);
				Log.error("Business Areas could not be added to menu.. please see logs for details..", caught);
			}
		});
	}

}
