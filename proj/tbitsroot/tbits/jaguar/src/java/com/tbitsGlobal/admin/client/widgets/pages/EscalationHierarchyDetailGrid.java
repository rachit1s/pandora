package com.tbitsGlobal.admin.client.widgets.pages;

import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.tbitsGlobal.admin.client.modelData.EscalationHierarchiesClient;

import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;

public class EscalationHierarchyDetailGrid extends BulkUpdateGridAbstract<EscalationHierarchiesClient> {

	public EscalationHierarchyDetailGrid(BulkGridMode mode) {
		super(mode);
		showNumberer=true;
		showStatus=false;
	}

	@Override
	protected void createColumns() {
		
		ColumnConfig idCol=new ColumnConfig(EscalationHierarchiesClient.ESC_ID,100);
		idCol.setHeader("Escalation ID");
		idCol.setFixed(true);
		cm.getColumns().add(idCol);
		
		ColumnConfig nameCol=new ColumnConfig(EscalationHierarchiesClient.NAME,100);
		nameCol.setHeader("NAME");
		cm.getColumns().add(nameCol);
		
		ColumnConfig disNameCol=new ColumnConfig(EscalationHierarchiesClient.DISPLAY_NAME,100);
		disNameCol.setHeader("Display Name");
		disNameCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(disNameCol);
		
		ColumnConfig desCol=new ColumnConfig(EscalationHierarchiesClient.DESCRIPTION,200);
		desCol.setHeader("Description");
		desCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(desCol);
		
	}

}
