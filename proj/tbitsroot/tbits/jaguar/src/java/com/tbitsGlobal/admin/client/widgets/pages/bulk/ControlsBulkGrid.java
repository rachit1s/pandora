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

public class ControlsBulkGrid extends BulkUpdateGridAbstract<RolePermissionModel> {

	public ControlsBulkGrid(BulkGridMode mode) {
		super(mode);
		
		canRemoveRow = false;
		showStatus = false;
	}

	protected void createColumns() {
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
		ColumnConfig fieldCol = new ColumnConfig(RolePermissionModel.DISPLAY_NAME, 200);
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
		
		
		for(String controlName : PermissionClient.FIELD_CONTROLS){
			CheckColumnConfig col = getCheckColumn();
			col.setHeader(controlName + " Control");
			col.setId(controlName);
			col.setWidth(90);
			cm.getColumns().add(col);
			
			this.addPlugin(col);
		}
	}

}
