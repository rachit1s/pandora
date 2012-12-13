package admin.com.tbitsglobal.admin.client.trn.bulkgrids;

import admin.com.tbitsglobal.admin.client.AdminUtils;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcess;
import bulkupdate.com.tbitsglobal.bulkupdate.client.BulkUpdateGridAbstract;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.Element;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

public class ProcessBulkGrid extends BulkUpdateGridAbstract<TrnProcess>{

	private GridCellRenderer<TrnProcess> baCellRenderer = new GridCellRenderer<TrnProcess>(){
		public Object render(TrnProcess model, String property,
				ColumnData config, int rowIndex, int colIndex,
				ListStore<TrnProcess> store, Grid<TrnProcess> grid) {
			BusinessAreaClient ba = model.get(property);
			if(ba != null)
				return ba.getSystemPrefix() + " <" + ba.getSystemId() + ">";
			else return "";
		}};
	
	public ProcessBulkGrid(int mode) {
		super(mode);
		
		showNumberer = true;
	}
	
	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		
		this.createColumnModel();
	}
	
	protected void createColumnModel(){
		ColumnConfig idCol = new ColumnConfig(TrnProcess.PROCESS_ID, 70);
		idCol.setHeader("Process Id");
		cm.getColumns().add(idCol);
		
		ColumnConfig srcBACol = new ColumnConfig(TrnProcess.SRC_BA, 200);
		srcBACol.setHeader("SRC BA");
		srcBACol.setEditor(new CellEditor(AdminUtils.getBACombo()));
		srcBACol.setRenderer(baCellRenderer);
		cm.getColumns().add(srcBACol);
		
		ColumnConfig nameCol = new ColumnConfig(TrnProcess.NAME, 200);
		nameCol.setHeader("Name");
		nameCol.setEditor(new CellEditor(new TextField<String>()));
		cm.getColumns().add(nameCol);
		
		ColumnConfig displayNameCol = new ColumnConfig(TrnProcess.DESCRIPTION, 200);
		displayNameCol.setHeader("Description");
		displayNameCol.setEditor(new CellEditor(new TextField<String>()));
		cm.getColumns().add(displayNameCol);
		
		ColumnConfig orderCol = new ColumnConfig(TrnProcess.ORDER, 70);
		orderCol.setHeader("Sort Order");
		orderCol.setEditor(new CellEditor(new NumberField()){
			@Override
			public Object postProcessValue(Object value) {
				return ((Number)super.postProcessValue(value)).intValue();
			}
		});
		cm.getColumns().add(orderCol);
		
		ColumnConfig dtnBACol = new ColumnConfig(TrnProcess.DTN_BA, 200);
		dtnBACol.setHeader("DTN BA");
		dtnBACol.setEditor(new CellEditor(AdminUtils.getBACombo()));
		dtnBACol.setRenderer(baCellRenderer);
		cm.getColumns().add(dtnBACol);
		
		ColumnConfig dtrBACol = new ColumnConfig(TrnProcess.DTR_BA, 200);
		dtrBACol.setHeader("DTR BA");
		dtrBACol.setEditor(new CellEditor(AdminUtils.getBACombo()));
		dtrBACol.setRenderer(baCellRenderer);
		cm.getColumns().add(dtrBACol);
		
		ColumnConfig serialCol = new ColumnConfig(TrnProcess.SERIAL_KEY, 200);
		serialCol.setHeader("Serial Key");
		serialCol.setEditor(new CellEditor(new TextField<String>()));
		cm.getColumns().add(serialCol);
	}
}
