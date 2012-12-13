package admin.com.tbitsglobal.admin.client.trn.bulkgrids;

import admin.com.tbitsglobal.admin.client.trn.models.TrnProcessParam;
import bulkupdate.com.tbitsglobal.bulkupdate.client.BulkUpdateGridAbstract;

import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.google.gwt.user.client.Element;

public class ProcessParamsBulkGrid extends BulkUpdateGridAbstract<TrnProcessParam>{

	public ProcessParamsBulkGrid(int mode) {
		super(mode);
		
		showNumberer = true;
	}
	
	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		
		this.createColumnModel();
	}
	
	protected void createColumnModel(){
		ColumnConfig nameCol = new ColumnConfig(TrnProcessParam.NAME, 200);
		nameCol.setHeader("Name");
		nameCol.setEditor(new CellEditor(new TextField<String>()));
		cm.getColumns().add(nameCol);
		
		ColumnConfig valueCol = new ColumnConfig(TrnProcessParam.VALUE, 800);
		valueCol.setHeader("Value");
		valueCol.setEditor(new CellEditor(new TextField<String>()));
		cm.getColumns().add(valueCol);
	}

}
