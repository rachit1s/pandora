package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import commons.com.tbitsGlobal.utils.client.ClickableLink;
import commons.com.tbitsGlobal.utils.client.ClickableLink.ClickableLinkListener;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.LinkCellRenderer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportTypeModel.ExcelImportDataType;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;

public class TypeBulkGrid extends BulkUpdateGridAbstract<TypeClient>{

	public TypeBulkGrid(BulkGridMode mode) {
		super(mode);
		
		canRemoveRow = false;
		showStatus = false;
	}
	
	protected void createColumns(){
		ColumnConfig idCol = new ColumnConfig(TypeClient.TYPE_ID, 100);
		idCol.setHeader("Id");
		idCol.setFixed(true);
		cm.getColumns().add(idCol);
		dataTypeMap.put(TypeClient.TYPE_ID, ExcelImportDataType.Number);
		
		ColumnConfig nameCol = new ColumnConfig(TypeClient.NAME, 200);
		nameCol.setHeader("Name");
		cm.getColumns().add(nameCol);
		
		ColumnConfig displayNameCol = new ColumnConfig(TypeClient.DISPLAY_NAME, 200);
		displayNameCol.setHeader("Display Name");
		displayNameCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(displayNameCol);
		
		CheckColumnConfig defaultCol = getCheckColumn();
		defaultCol.setHeader("Default");
		defaultCol.setId(TypeClient.IS_DEFAULT);
		defaultCol.setWidth(50);
		cm.getColumns().add(defaultCol);
		this.addPlugin(defaultCol);
		
//		CheckColumnConfig searchCol = getCheckColumn();
//		searchCol.setHeader("Checked");
//		searchCol.setId(TypeClient.IS_CHECKED);
//		searchCol.setWidth(50);
//		cm.getColumns().add(searchCol);
//		this.addPlugin(searchCol);
		
		ColumnConfig descCol = new ColumnConfig(TypeClient.DESCRIPTION, 200);
		descCol.setHeader("Description");
		descCol.setEditor(new TbitsCellEditor(new TextArea()));
		cm.getColumns().add(descCol);
		
		CheckColumnConfig activeCol = getCheckColumn();
		activeCol.setHeader("Active");
		activeCol.setId(TypeClient.IS_ACTIVE);
		activeCol.setWidth(50);
		cm.getColumns().add(activeCol);
		this.addPlugin(activeCol);
		
		CheckColumnConfig privateCol = getCheckColumn();
		privateCol.setHeader("Private");
		privateCol.setId(TypeClient.IS_PRIVATE);
		privateCol.setWidth(50);
		cm.getColumns().add(privateCol);
		this.addPlugin(privateCol);
		
		CheckColumnConfig finalCol = getCheckColumn();
		finalCol.setHeader("Final");
		finalCol.setId(TypeClient.IS_FINAL);
		finalCol.setWidth(50);
		cm.getColumns().add(finalCol);
		this.addPlugin(finalCol);
		
		CheckColumnConfig searchCol = getCheckColumn();
		searchCol.setHeader("Search");
		searchCol.setId(TypeClient.IS_CHECKED);
		searchCol.setWidth(50);
		cm.getColumns().add(searchCol);
		this.addPlugin(searchCol);
		
		ColumnConfig dependencyColumn = new ColumnConfig("dependencies", "", 120);
		dependencyColumn.setFixed(true);
		GridCellRenderer<TypeClient> fieldsbuttonRenderer = new LinkCellRenderer<TypeClient>(){
			@Override
			public Object render(final TypeClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<TypeClient> store,
					Grid<TypeClient> grid) {
				ClickableLink link = new ClickableLink("Dependencies", new ClickableLinkListener<GridEvent<TypeClient>>(){
					public void onClick(GridEvent<TypeClient> e) {
						TypeDependencyEditorWindow window = new TypeDependencyEditorWindow(model);
						window.show();
					}
				});
			addLink(link);
			return link.getHtml();
			}};
		dependencyColumn.setRenderer(fieldsbuttonRenderer);
		cm.getColumns().add(dependencyColumn);
	}

}
