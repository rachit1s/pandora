package transmittal.com.tbitsGlobal.client.admin.pages;

import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import transmittal.com.tbitsGlobal.client.models.TrnReplicateProcess;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;

public class ReplicateProcessGrid extends BulkUpdateGridAbstract<TrnReplicateProcess> {

	public ReplicateProcessGrid(BulkGridMode mode) {
		super(mode);
		
		showNumberer = false;
		showStatus	  = false;
		showSelectionModel = false;
	}

	protected void createColumns() {
		ColumnConfig paramNameConfig = new ColumnConfig(TrnReplicateProcess.PARAM_NAME, 250);
		paramNameConfig.setHeader("Parameter");
		cm.getColumns().add(paramNameConfig);
		
		ColumnConfig oldValueConfig = new ColumnConfig(TrnReplicateProcess.PARAM_VALUE_OLD, 250);
		oldValueConfig.setHeader("Selected Process Value");
		cm.getColumns().add(oldValueConfig);
		
		ColumnConfig newValueConfig = new ColumnConfig(TrnReplicateProcess.PARAM_VALUE_NEW, 250);
		newValueConfig.setHeader("New Process Value");
		newValueConfig.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(newValueConfig);
	}

}
