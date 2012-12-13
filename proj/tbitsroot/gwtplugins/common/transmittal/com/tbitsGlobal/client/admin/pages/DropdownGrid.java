package transmittal.com.tbitsGlobal.client.admin.pages;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnDropdown;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

public class DropdownGrid extends BulkUpdateGridAbstract<TrnDropdown> {

	private BusinessAreaClient srcBA;
	
	public DropdownGrid(BulkGridMode mode) {
		super(mode);
		showNumberer  = false;
		showStatus	  = false;
	}

	private GridCellRenderer<TrnDropdown> baCellRenderer = new GridCellRenderer<TrnDropdown>(){
		@Override
		public Object render(TrnDropdown model, String property, ColumnData config, int rowIndex, int colIndex,
				ListStore<TrnDropdown> store, Grid<TrnDropdown> grid) {
			BusinessAreaClient ba = model.get(property);
			if(ba != null){
				return ba.getSystemPrefix() + "<" + ba.getSystemId() + ">";
			}else return "";
		}
	};
	
	/**
	 * Define Column Configuration
	 */
	protected void createColumns() {
		ColumnConfig srcSysIdCol = new ColumnConfig(TrnDropdown.SRC_BA, 150);
		srcSysIdCol.setHeader("Source BA");
		srcSysIdCol.setRenderer(baCellRenderer);
		cm.getColumns().add(srcSysIdCol);
		
		ColumnConfig dropdownIdCol = new ColumnConfig(TrnDropdown.DROPDOWN_ID, 100);
		dropdownIdCol.setHeader("Dropdown Id");
		cm.getColumns().add(dropdownIdCol);
		
		ColumnConfig processNameCol = new ColumnConfig(TrnDropdown.PROCESS_NAME, 400);
		processNameCol.setHeader("Process Name");
		processNameCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(processNameCol);
		
		ColumnConfig sortOrderCol	= new ColumnConfig(TrnDropdown.SORT_ORDER, 100);
		sortOrderCol.setHeader("Sort Order");
		sortOrderCol = TrnAdminUtils.getIntegerColConfig(sortOrderCol);
		cm.getColumns().add(sortOrderCol);
	}

	public void setSrcBA(BusinessAreaClient srcBA) {
		this.srcBA = srcBA;
	}

	public BusinessAreaClient getSrcBA() {
		return srcBA;
	}
}
