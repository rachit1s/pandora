package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.widgets.NumberCellEditor;
import commons.com.tbitsGlobal.utils.client.ClickableLink;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.ClickableLink.ClickableLinkListener;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.LinkCellRenderer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportTypeModel.ExcelImportDataType;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BAMenuClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class BAMenuBulkGrid extends BulkUpdateGridAbstract<BAMenuClient>{

	public BAMenuBulkGrid(BulkGridMode mode) {
		super(mode);
		
		canRemoveRow = false;
		showStatus = false;
	}

	@Override
	protected void createColumns() {
		ColumnConfig idCol = new ColumnConfig(BAMenuClient.MENU_ID, 100);
		idCol.setHeader("Menu Id");
		idCol.setFixed(true);
		cm.getColumns().add(idCol);
		dataTypeMap.put(BAMenuClient.MENU_ID, ExcelImportDataType.Number);
		
		ColumnConfig captionCol = new ColumnConfig(BAMenuClient.MENU_CAPTION, 200);
		captionCol.setHeader("Caption");
		captionCol.setEditor(new CellEditor(new TextField<String>()));
		cm.getColumns().add(captionCol);
		
		ColumnConfig parentIdCol = new ColumnConfig(BAMenuClient.PARENT_MENU_ID, 100);
		parentIdCol.setHeader("Parent Menu Id");
		parentIdCol.setEditor(new NumberCellEditor(new TextField<String>()));
		cm.getColumns().add(parentIdCol);
		dataTypeMap.put(BAMenuClient.PARENT_MENU_ID, ExcelImportDataType.Number);
		
		ColumnConfig savecolumn = new ColumnConfig("add", "Add/Delete Business Area", 200);
		savecolumn.setFixed(true);
		GridCellRenderer<BAMenuClient> savebuttonRenderer = new LinkCellRenderer<BAMenuClient>(){
			@Override
			public Object render(BAMenuClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BAMenuClient> store,
					Grid<BAMenuClient> grid) {
				if(model.get(BAMenuClient.MENU_ID) != null && model.getMenuId() != 0){
					ClickableLink link = new ClickableLink("Add/Delete Business Area", new ClickableLinkListener<GridEvent<BAMenuClient>>(){
						public void onClick(GridEvent<BAMenuClient> e) {
						Grid<BAMenuClient> grid = e.getGrid();
						if(grid != null){
							final BAMenuClient model =  grid.getStore().getAt(e.getRowIndex());
							
							APConstants.apService.getBAMenuMapping(model.getMenuId(), new AsyncCallback<List<Integer>>() {
								@Override
								public void onSuccess(List<Integer> result) {
									if(result != null){
										BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
										List<BusinessAreaClient> allBas = Util.createList(cache.getValues());
										List<BusinessAreaClient> currentBas = new ArrayList<BusinessAreaClient>();
										for(int sysId : result){
											BusinessAreaClient ba = ClientUtils.getBAbySysId(sysId);
											if(ba != null){
												currentBas.add(ba);
											}
										}
										
										BAMenuDualListWindow window = new BAMenuDualListWindow(model, allBas, currentBas);
										window.show();
									}
								}
								
								@Override
								public void onFailure(Throwable caught) {
									TbitsInfo.error("Could not retrieve BA Menu Mapping.. See logs for details..", caught);
									Log.error("Could not retrieve BA Menu Mapping.. See logs for details..", caught);
								}
							});
						}
					}});
					
					addLink(link);
					return link.getHtml();
				}
				return "";
			}};
		savecolumn.setRenderer(savebuttonRenderer);
		cm.getColumns().add(savecolumn);
	}

}
