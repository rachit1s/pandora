package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.Arrays;

import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.ClickableLink;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.ClickableLink.ClickableLinkListener;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.LinkCellRenderer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class RolesBulkUpdateGrid extends BulkUpdateGridAbstract<RoleClient>{

	public RolesBulkUpdateGrid(BulkGridMode mode) {
		super(mode);
		
		canRemoveRow = false;
		showStatus = false;
	}

	@Override
	protected void createColumns() {
		ColumnConfig idcolumn = new ColumnConfig(RoleClient.ROLE_ID, "Id",100);
		idcolumn.setFixed(true);
		cm.getColumns().add(idcolumn);
		
		ColumnConfig namecolumn = new ColumnConfig(RoleClient.ROLE_NAME, "Name",150);
		namecolumn.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(namecolumn);
		
		ColumnConfig desccolumn = new ColumnConfig(RoleClient.DESCRIPTION, "Description",200);
		desccolumn.setEditor(new TbitsCellEditor(new TextArea()));
		cm.getColumns().add(desccolumn);
		
		ColumnConfig removeCol = new ColumnConfig();
		removeCol.setHeader("Remove");
		removeCol.setId("remove_role");
		removeCol.setWidth(50);
		removeCol.setFixed(true);
		removeCol.setRenderer(new LinkCellRenderer<RoleClient>(){
			private ClickableLink link;
			
			public Object render(final RoleClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					final ListStore<RoleClient> store,
					Grid<RoleClient> grid) {
				if(model.getCanBeDeleted() != 0){
					if(link == null){
						link = new ClickableLink("Remove", new ClickableLinkListener<GridEvent<RoleClient>>(){
								public void onClick(GridEvent<RoleClient> e) {
									Grid<RoleClient> grid = e.getGrid();
									if(grid != null){
										final RoleClient selectedItem =  grid.getStore().getAt(e.getRowIndex());
										if(selectedItem != null){
											if(com.google.gwt.user.client.Window.confirm("Do you want to delete the selected Role?")){
												APConstants.apService.deleteRoles(Arrays.asList(selectedItem),new AsyncCallback<Boolean>(){
													public void onFailure(Throwable caught) {
														TbitsInfo.error("Error in deleting Role. Please Try Again...", caught);
														Log.error("Error in deleting Role. Please Try Again...", caught);
													}
													public void onSuccess(Boolean result) {
														if(result){
															TbitsInfo.info("Role deleted");
															RolesBulkUpdateGrid.this.getStore().remove(selectedItem);
														}
													}
												});
											}
										}
									}
							}});
						
						addLink(link);
					}
					return link.getHtml();
				}
				return null;
			}});
		cm.getColumns().add(removeCol);
	}

}
