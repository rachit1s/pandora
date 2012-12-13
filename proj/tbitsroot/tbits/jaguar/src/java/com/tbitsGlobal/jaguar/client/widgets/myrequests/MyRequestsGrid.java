package com.tbitsGlobal.jaguar.client.widgets.myrequests;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.tbitsGlobal.jaguar.client.serializables.BARequests;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.ColPrefs;
import commons.com.tbitsGlobal.utils.client.grids.GridColumnView;
import commons.com.tbitsGlobal.utils.client.grids.IRequestsGrid;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGrid;

/**
 * Grid used to display "My Requests" for a BA.
 * 
 * @author sourabh
 *
 */
public class MyRequestsGrid extends RequestsViewGrid implements IRequestsGrid{
	private BARequests baRequests;
	
	/**
	 * Contructor
	 * 
	 * @param sysPrefix
	 * @param baRequests
	 * @param filterFields
	 */
	public MyRequestsGrid(String sysPrefix, BARequests baRequests) {
		super(sysPrefix);
		
		this.baRequests = baRequests;
		
		this.setCustomizable(false);
		
		menu = new MyRequestsGridContextMenu(sysPrefix, this);
	}
	
	@Override
	protected void afterRenderView() {
		super.afterRenderView();
		
		this.createStore();
	}
	
	/**
	 * Creates store from {@link BARequests}.
	 * 
	 * @param baRequests
	 * @return. The {@link TreeStore}
	 */
	private void createStore(){
		this.addModels(baRequests.getResults().getRequests());
	}
	
	@Override
	public List<ColumnConfig> getColumnsFromCache() {
		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		List<BAField> baFields = baRequests.getFields();
		ListStore<BAField> baFieldStore = new ListStore<BAField>();
		if(baFields != null){
			
			baFieldStore.add(baFields);
			
			/**
			 * make prefs from cache if prefs are null
			 */
			if(prefs == null){
				prefs = new ArrayList<ColPrefs>();
				if(baFields != null){
					for(BAField baField : baFields){
						prefs.add(ClientUtils.fieldToColPref(baField));
					}
				}
			}
			
			for(ColPrefs pref : prefs){
				BAField baField = baFieldStore.findModel(BAField.NAME, pref.getName());
				if(baField == null || baField.getName().equals(REQUEST) || !baField.isCanViewInBA())
					continue;
				ColumnConfig config = this.getColumn(baField, pref.getColSize());
				if(config != null)
					columns.add(config);
			}
		}
		return columns;
	}

	public Grid<TbitsTreeRequestData> getGrid() {
		return this;
	}

	public void setBaRequests(BARequests baRequests) {
		this.baRequests = baRequests;
	}

	public BARequests getBaRequests() {
		return baRequests;
	}

	public GridColumnView getViewId() {
		return GridColumnView.SearchGrid;
	}
}
