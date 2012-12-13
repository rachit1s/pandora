package transmittal.com.tbitsGlobal.client.admin.pages;


import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnDistList;

import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;

/**
 * Grid to hold distribution table column config table
 * @author devashish
 *
 */
public class DistListGrid extends BulkUpdateGridAbstract<TrnDistList>{

	public DistListGrid(BulkGridMode mode) {
		super(mode);

		showNumberer = false;
		showStatus 	 = false;
	}
	
	/**
	 * Define Column Configuration
	 */
	protected void createColumns(){
		ColumnConfig nameCol = new ColumnConfig(TrnDistList.NAME, 200);
		nameCol.setHeader("Name");
		nameCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(nameCol);
		
		ColumnConfig displayNameCol = new ColumnConfig(TrnDistList.DISPLAY_NAME, 200);
		displayNameCol.setHeader("Display Name");
		displayNameCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(displayNameCol);
		
		ColumnConfig dataTypeCol = new ColumnConfig(TrnDistList.DATA_TYPE_ID, 70);
		dataTypeCol.setHeader("Data Type");
		dataTypeCol = TrnAdminUtils.getIntegerColConfig(dataTypeCol);
		cm.getColumns().add(dataTypeCol);
		
		ColumnConfig fieldConfigCol = new ColumnConfig(TrnDistList.FIELD_CONFIG, 200);
		fieldConfigCol.setHeader("Field Config");
		fieldConfigCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(fieldConfigCol);
		
		CheckColumnConfig isEditableCol = new CheckColumnConfig();
		isEditableCol.setId(TrnDistList.IS_EDITABLE);
		isEditableCol.setWidth(100);
		isEditableCol.setHeader("Is Editable");
		isEditableCol.setEditor(new TbitsCellEditor(new CheckBox()));
		cm.getColumns().add(isEditableCol);
		
		CheckColumnConfig isActiveCol = new CheckColumnConfig();
		isActiveCol.setId(TrnDistList.IS_ACTIVE);
		isActiveCol.setWidth(100);
		isActiveCol.setHeader("Is Active");
		isActiveCol.setEditor(new TbitsCellEditor(new CheckBox()));
		cm.getColumns().add(isActiveCol);
		
		ColumnConfig orderCol = new ColumnConfig(TrnDistList.COLUMN_ORDER, 70);
		orderCol.setHeader("Column Order");
		orderCol = TrnAdminUtils.getIntegerColConfig(orderCol);
		cm.getColumns().add(orderCol);
	}

}
