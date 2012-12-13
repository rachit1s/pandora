package com.tbitsGlobal.admin.client.widgets.pages;




import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.tbitsGlobal.admin.client.modelData.EscalationHierarchyValuesClient;
import com.tbitsGlobal.admin.client.utils.EscalationUtils;

import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public class EscalationHierarchyGrid extends BulkUpdateGridAbstract<EscalationHierarchyValuesClient> {

	public EscalationHierarchyGrid(BulkGridMode mode) {
		super(mode);
		showNumberer  = false;
		showStatus	  = false;
	}

	private GridCellRenderer<EscalationHierarchyValuesClient> userCellRenderer = new GridCellRenderer<EscalationHierarchyValuesClient>(){

		@Override
		public Object render(EscalationHierarchyValuesClient model, String property,
				ColumnData config, int rowIndex, int colIndex,
				ListStore<EscalationHierarchyValuesClient> store,
				Grid<EscalationHierarchyValuesClient> grid) {
			
			UserClient user = model.get(property);
			if(user != null)
				return user.getDisplayName() + " <" + user.getUserId() + ">";
			else return "";
		}};
	@Override
	protected void createColumns() {
		
		ColumnConfig userCol = new ColumnConfig(EscalationHierarchyValuesClient.CHILD_USER, 200);
		userCol.setHeader("Child User");
		ComboBox<UserClient> userCombo = EscalationUtils.getUserCombo(); 
		userCol.setEditor(new TbitsCellEditor(userCombo));
		userCol.setRenderer(userCellRenderer);
		cm.getColumns().add(userCol);
		ColumnConfig parentUserCol = new ColumnConfig(EscalationHierarchyValuesClient.PARENT_USER, 200);
		parentUserCol.setHeader("Parent User");
		ComboBox<UserClient> parentUserCombo = EscalationUtils.getUserCombo(); 
		parentUserCol.setEditor(new TbitsCellEditor(parentUserCombo));
		parentUserCol.setRenderer(userCellRenderer);
		cm.getColumns().add(parentUserCol);
		
		
		
	}

}
