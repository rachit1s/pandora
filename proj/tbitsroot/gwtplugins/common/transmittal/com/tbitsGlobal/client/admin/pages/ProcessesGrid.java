package transmittal.com.tbitsGlobal.client.admin.pages;


import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

/**
 * Grid to hold trn_processes table
 * @author devashish
 *
 */
public class ProcessesGrid extends BulkUpdateGridAbstract<TrnProcess>{

	public ProcessesGrid(BulkGridMode mode) {
		super(mode);
		
		showNumberer  = false;
		showStatus	  = false;
	}
	
	private GridCellRenderer<TrnProcess> baCellRenderer = new GridCellRenderer<TrnProcess>(){
		public Object render(TrnProcess model, String property,
				ColumnData config, int rowIndex, int colIndex,
				ListStore<TrnProcess> store, Grid<TrnProcess> grid) {
			BusinessAreaClient ba = model.get(property);
			if(ba != null)
				return ba.getSystemPrefix() + " <" + ba.getSystemId() + ">";
			else return "";
		}};
	

	/**
	 * Define column configuration
	 */
	protected void createColumns(){
		ColumnConfig idCol = new ColumnConfig(TrnProcess.PROCESS_ID, 70);
		idCol.setHeader("Process Id");
//		idCol = TrnAdminUtils.getIntegerColConfig(idCol);
		cm.getColumns().add(idCol);
		
		ColumnConfig srcBACol = new ColumnConfig(TrnProcess.SRC_BA, 200);
		srcBACol.setHeader("SRC BA");
		srcBACol.setRenderer(baCellRenderer);
		cm.getColumns().add(srcBACol);
		
		ColumnConfig nameCol = new ColumnConfig(TrnProcess.NAME, 200);
		nameCol.setHeader("Name");
	//	nameCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(nameCol);
		
		ColumnConfig displayNameCol = new ColumnConfig(TrnProcess.DESCRIPTION, 200);
		displayNameCol.setHeader("Description");
		displayNameCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(displayNameCol);
		
		ColumnConfig orderCol = new ColumnConfig(TrnProcess.ORDER, 70);
		orderCol.setHeader("Sort Order");
		orderCol = TrnAdminUtils.getIntegerColConfig(orderCol);
		cm.getColumns().add(orderCol);
		
		ColumnConfig dtnBACol = new ColumnConfig(TrnProcess.DTN_BA, 200);
		dtnBACol.setHeader("DTN BA");
		dtnBACol.setEditor(new TbitsCellEditor(TrnAdminUtils.getBACombo()));
		dtnBACol.setRenderer(baCellRenderer);
		cm.getColumns().add(dtnBACol);
		
		ColumnConfig dtrBACol = new ColumnConfig(TrnProcess.DTR_BA, 200);
		dtrBACol.setHeader("DTR BA");
		dtrBACol.setEditor(new TbitsCellEditor(TrnAdminUtils.getBACombo()));
		dtrBACol.setRenderer(baCellRenderer);
		cm.getColumns().add(dtrBACol);
		
		ColumnConfig serialCol = new ColumnConfig(TrnProcess.SERIAL_KEY, 200);
		serialCol.setHeader("Serial Key");
		serialCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(serialCol);
	}
}
