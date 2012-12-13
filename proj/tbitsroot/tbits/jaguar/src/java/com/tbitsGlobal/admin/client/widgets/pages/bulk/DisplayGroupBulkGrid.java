package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.widgets.pages.DisplayGroupFieldsWindow;

import commons.com.tbitsGlobal.utils.client.ClickableLink;
import commons.com.tbitsGlobal.utils.client.ClickableLink.ClickableLinkListener;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.LinkCellRenderer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class DisplayGroupBulkGrid extends BulkUpdateGridAbstract<DisplayGroupClient>{

	public DisplayGroupBulkGrid(BulkGridMode mode) {
		super(mode);
		
		canRemoveRow = false;
		showStatus = false;
	}
	
	protected void createColumns(){
		ColumnConfig namecolumn = new ColumnConfig(DisplayGroupClient.DISPLAY_NAME, "Name",330);
		namecolumn.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(namecolumn);
		
	

		CheckColumnConfig isActivecolumn = getCheckColumn();
		isActivecolumn.setId(DisplayGroupClient.IS_ACTIVE);
		isActivecolumn.setWidth(60);
		isActivecolumn.setHeader("Is Active");
		cm.getColumns().add(isActivecolumn);
		this.addPlugin(isActivecolumn);
		
		
		CheckColumnConfig isDefaultcolumn = getCheckColumn();
		isDefaultcolumn.setId(DisplayGroupClient.IS_DEFAULT);
		isDefaultcolumn.setWidth(60);
		isDefaultcolumn.setHeader("Is Default");
		cm.getColumns().add(isDefaultcolumn);
		this.addPlugin(isDefaultcolumn);
		
		
		
		
		
	
		
	/*	ColumnConfig savecolumn = new ColumnConfig("save", "Save", 120);
		savecolumn.setFixed(true);
		GridCellRenderer<DisplayGroupClient> savebuttonRenderer = new LinkCellRenderer<DisplayGroupClient>(){
			private ClickableLink link;
			
			@Override
			public Object render(DisplayGroupClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<DisplayGroupClient> store,
					Grid<DisplayGroupClient> grid) {
				if(link == null){
					link = new ClickableLink("Save", new ClickableLinkListener<GridEvent<DisplayGroupClient>>(){
							public void onClick(GridEvent<DisplayGroupClient> e) {
							Grid<DisplayGroupClient> grid = e.getGrid();
							if(grid != null){
								final DisplayGroupClient model =  grid.getStore().getAt(e.getRowIndex());
								APConstants.apService.updateDisplayGroup(ClientUtils.getCurrentBA().getSystemPrefix(), model, new AsyncCallback<DisplayGroupClient>(){
									public void onFailure(Throwable caught) {
										TbitsInfo.error("DisplayGroup not Updated ...Please Refresh ...", caught);
										Log.error("DisplayGroup not Updated ...Please Refresh ...", caught);
									}
									public void onSuccess(DisplayGroupClient result) {
										if(result != null){
											TbitsInfo.info("DisplayGroup has been updated ...");
										}
									}
								});
							}
						}});
					
					addLink(link);
				}
				return link.getHtml();
			}};
		savecolumn.setRenderer(savebuttonRenderer);
		cm.getColumns().add(savecolumn);
		
		ColumnConfig deletecolumn = new ColumnConfig("delete", "Delete", 120);
		deletecolumn.setFixed(true);
		GridCellRenderer<DisplayGroupClient> deletebuttonRenderer = new LinkCellRenderer<DisplayGroupClient>(){
			private ClickableLink link;
			
			@Override
			public Object render(DisplayGroupClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					final ListStore<DisplayGroupClient> store,
					Grid<DisplayGroupClient> grid) {
				if(link == null){
					link = new ClickableLink("Delete", new ClickableLinkListener<GridEvent<DisplayGroupClient>>(){
							public void onClick(GridEvent<DisplayGroupClient> e) {
							Grid<DisplayGroupClient> grid = e.getGrid();
							if(grid != null){
								final DisplayGroupClient model =  grid.getStore().getAt(e.getRowIndex());
								boolean doDelete = com.google.gwt.user.client.Window.confirm("Are you sure you want to Delete ?");
								if(doDelete){
									APConstants.apService.deleteDisplayGroup(ClientUtils.getCurrentBA().getSystemPrefix(), model, new AsyncCallback<DisplayGroupClient>(){
										public void onFailure(Throwable caught) {
											TbitsInfo.error("DisplayGroup not Deleted ...Please Refresh ...", caught);
											Log.error("DisplayGroup not Deleted ...Please Refresh ...", caught);
										}
										public void onSuccess(DisplayGroupClient result) {
											if(result != null){
												store.remove(model);
												TbitsInfo.info("DisplayGroup has been deleted ...");
											}
										}
									});
								}
							}
						}});
					
					addLink(link);
				}
				return link.getHtml();
			}};
		deletecolumn.setRenderer(deletebuttonRenderer);
		cm.getColumns().add(deletecolumn);*/
		
		ColumnConfig addFieldsColumn = new ColumnConfig("addFields", "Add/Remove Fields", 120);
		addFieldsColumn.setFixed(true);
		GridCellRenderer<DisplayGroupClient> fieldsbuttonRenderer = new LinkCellRenderer<DisplayGroupClient>(){
			private ClickableLink link;
			
			@Override
			public Object render(DisplayGroupClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<DisplayGroupClient> store,
					Grid<DisplayGroupClient> grid) {
				if(link == null){
					link = new ClickableLink("Add/Remove Fields", new ClickableLinkListener<GridEvent<DisplayGroupClient>>(){
							public void onClick(GridEvent<DisplayGroupClient> e) {
							Grid<DisplayGroupClient> grid = e.getGrid();
							if(grid != null){
								final DisplayGroupClient model =  grid.getStore().getAt(e.getRowIndex());
								APConstants.apService.getFieldClients(ClientUtils.getSysPrefix(), new AsyncCallback<List<FieldClient>>() {
									@Override
									public void onSuccess(List<FieldClient> result) {
										if(result != null){
											List<FieldClient> currentModels = new ArrayList<FieldClient>();
											for(FieldClient field : result){
												if(field.getDisplayGroup() == model.getId()){
													currentModels.add(field);
												}
											}
											DisplayGroupFieldsWindow window = new DisplayGroupFieldsWindow(model, result, currentModels);
											window.show();
										}
									}
									
									@Override
									public void onFailure(Throwable caught) {
										TbitsInfo.error("Unable to retrieve fields for this display group.. Please see logs for details..", caught);
										Log.error("Unable to retrieve fields for this display group.. Please see logs for details..", caught);
									}
								});
							}
						}});
					
					addLink(link);
				}
				return link.getHtml();
			}};
		addFieldsColumn.setRenderer(fieldsbuttonRenderer);
		cm.getColumns().add(addFieldsColumn);
	}

}
