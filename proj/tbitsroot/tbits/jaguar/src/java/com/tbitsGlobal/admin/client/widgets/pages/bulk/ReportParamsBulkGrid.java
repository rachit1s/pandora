package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.tbitsGlobal.admin.client.modelData.ReportParamClient;

import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportTypeModel.ExcelImportDataType;

public class ReportParamsBulkGrid extends BulkUpdateGridAbstract<ReportParamClient>{

	public ReportParamsBulkGrid(BulkGridMode mode) {
		super(mode);
		
		canRemoveRow = false;
		showStatus = false;
	}

	@Override
	protected void createColumns() {
		ColumnConfig idCol = new ColumnConfig(ReportParamClient.REPORT_ID, 100);
		idCol.setHeader("Id");
		cm.getColumns().add(idCol);
		dataTypeMap.put(ReportParamClient.REPORT_ID, ExcelImportDataType.Number);
		
		ColumnConfig nameCol = new ColumnConfig(ReportParamClient.PARAM_NAME, 200);
		nameCol.setHeader("Name");
		nameCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(nameCol);
		
		ColumnConfig valueCol = new ColumnConfig(ReportParamClient.PARAM_VALUE, 200);
		valueCol.setHeader("Value");
		valueCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(valueCol);
	}

}
