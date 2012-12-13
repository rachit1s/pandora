package com.tbitsGlobal.admin.client.widgets.pages.bulk;


import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.tbitsGlobal.admin.client.modelData.RolePermissionModel;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.domainObjects.PermissionClient;

public class PermissionBulkGrid extends BulkUpdateGridAbstract<RolePermissionModel>{

	public PermissionBulkGrid(BulkGridMode mode) {
		super(mode);
		
		canRemoveRow = false;
		showStatus = false;
	}
	
	protected void createColumns(){
		ColumnConfig fieldColumn = new ColumnConfig(RolePermissionModel.FIELD_NAME , 200);
		fieldColumn.setMenuDisabled(true);
		fieldColumn.setHeader("Field Name");
		fieldColumn.setRenderer(new GridCellRenderer<RolePermissionModel>() {
			@Override
			public Object render(RolePermissionModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<RolePermissionModel> store,
					Grid<RolePermissionModel> grid) {
				return model.getFieldName();
			}});
		cm.getColumns().add(fieldColumn);
		ColumnConfig fieldCol = new ColumnConfig(RolePermissionModel.DISPLAY_NAME , 200);
		fieldCol.setMenuDisabled(true);
		fieldCol.setHeader("Display Name");
		fieldCol.setRenderer(new GridCellRenderer<RolePermissionModel>() {
			@Override
			public Object render(RolePermissionModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<RolePermissionModel> store,
					Grid<RolePermissionModel> grid) {
				return model.getDisplayName();
			}});
		cm.getColumns().add(fieldCol);
		
		CheckColumnConfig viewCol = getCheckColumn();
		viewCol.setHeader("View");
		viewCol.setId(RolePermissionModel.IS_VIEW);
		viewCol.setWidth(40);
		cm.getColumns().add(viewCol);
		
		CheckColumnConfig addCol = getCheckColumn();
		addCol.setHeader("Add");
		addCol.setId(RolePermissionModel.IS_ADD);
		addCol.setWidth(40);
		cm.getColumns().add(addCol);
		
		CheckColumnConfig updateCol = getCheckColumn();
		updateCol.setHeader("Change");
		updateCol.setId(RolePermissionModel.IS_UPDATE);
		updateCol.setWidth(60);
		cm.getColumns().add(updateCol);
		
		CheckColumnConfig emailCol = getCheckColumn();
		emailCol.setHeader("E-Mail");
		emailCol.setId(RolePermissionModel.IS_EMAIL);
		emailCol.setWidth(50);
		cm.getColumns().add(emailCol);
		
		this.addPlugin(viewCol);
		this.addPlugin(addCol);
		this.addPlugin(updateCol);
		this.addPlugin(emailCol);
	}
}
