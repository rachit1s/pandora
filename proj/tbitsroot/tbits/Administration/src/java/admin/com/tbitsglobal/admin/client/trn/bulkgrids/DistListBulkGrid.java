package admin.com.tbitsglobal.admin.client.trn.bulkgrids;

import admin.com.tbitsglobal.admin.client.trn.models.TrnDistList;
import bulkupdate.com.tbitsglobal.bulkupdate.client.BulkUpdateGridAbstract;

import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.google.gwt.user.client.Element;

public class DistListBulkGrid extends BulkUpdateGridAbstract<TrnDistList>{

	public DistListBulkGrid(int mode) {
		super(mode);

		showNumberer = true;
	}
	
	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		
		this.createColumnModel();
	}
	
	protected void createColumnModel(){
		ColumnConfig nameCol = new ColumnConfig(TrnDistList.NAME, 200);
		nameCol.setHeader("Name");
		nameCol.setEditor(new CellEditor(new TextField<String>()));
		cm.getColumns().add(nameCol);
		
		ColumnConfig displayNameCol = new ColumnConfig(TrnDistList.DISPLAY_NAME, 200);
		displayNameCol.setHeader("Display Name");
		displayNameCol.setEditor(new CellEditor(new TextField<String>()));
		cm.getColumns().add(displayNameCol);
		
		ColumnConfig dataTypeCol = new ColumnConfig(TrnDistList.DATA_TYPE_ID, 70);
		dataTypeCol.setHeader("Data Type");
		dataTypeCol.setEditor(new CellEditor(new NumberField()){
			@Override
			public Object postProcessValue(Object value) {
				return ((Number)super.postProcessValue(value)).intValue();
			}
		});
		cm.getColumns().add(dataTypeCol);
		
		ColumnConfig fieldConfigCol = new ColumnConfig(TrnDistList.FIELD_CONFIG, 200);
		fieldConfigCol.setHeader("Field Config");
		fieldConfigCol.setEditor(new CellEditor(new TextField<String>()));
		cm.getColumns().add(fieldConfigCol);
		
		CheckColumnConfig isEditableCol = new CheckColumnConfig();
		isEditableCol.setId(TrnDistList.IS_EDITABLE);
		isEditableCol.setWidth(100);
		isEditableCol.setHeader("Is Editable");
		isEditableCol.setEditor(new CellEditor(new CheckBox()));
		cm.getColumns().add(isEditableCol);
		
		CheckColumnConfig isActiveCol = new CheckColumnConfig();
		isActiveCol.setId(TrnDistList.IS_ACTIVE);
		isActiveCol.setWidth(100);
		isActiveCol.setHeader("Is Active");
		isActiveCol.setEditor(new CellEditor(new CheckBox()));
		cm.getColumns().add(isActiveCol);
		
		ColumnConfig orderCol = new ColumnConfig(TrnDistList.COLUMN_ORDER, 70);
		orderCol.setHeader("Column Order");
		orderCol.setEditor(new CellEditor(new NumberField()){
			@Override
			public Object postProcessValue(Object value) {
				return ((Number)super.postProcessValue(value)).intValue();
			}
		});
		cm.getColumns().add(orderCol);
	}

}
