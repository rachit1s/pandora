package transmittal.com.tbitsGlobal.client.admin.pages;


import transmittal.com.tbitsGlobal.client.models.TrnProcessParam;

import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;

/**
 * Grid to hold transmittal process parameters.
 * @author devashish
 *
 */
public class ProcessParamsGrid extends BulkUpdateGridAbstract<TrnProcessParam>{

	public ProcessParamsGrid(BulkGridMode mode) {
		super(mode);
		
		showNumberer  = false;
		showStatus	  = false;
	}
	
	/**
	 * Create the column configs that will be shown in the grid
	 */
	protected void createColumns(){
		ColumnConfig nameCol = new ColumnConfig(TrnProcessParam.NAME, 200);
		nameCol.setHeader("Name");
		nameCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(nameCol);
		
		ColumnConfig valueCol = new ColumnConfig(TrnProcessParam.VALUE, 800);
		valueCol.setHeader("Value");
		valueCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(valueCol);
	}

}
