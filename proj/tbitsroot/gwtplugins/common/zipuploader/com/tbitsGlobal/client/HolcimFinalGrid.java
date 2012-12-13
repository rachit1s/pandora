package zipuploader.com.tbitsGlobal.client;

import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;

public class HolcimFinalGrid extends BulkUpdateGridAbstract<DocNumberFileTuple> {

	

	HolcimFinalGridColumnConfig columnConfiguration;
	
	public HolcimFinalGrid(BulkGridMode mode) {
		super(mode);
		showNumberer = false;
		showStatus	  = false;
		showSelectionModel = true;
		columnConfiguration = new HolcimFinalGridColumnConfig();
	}

	protected void createColumns() {
		cm.getColumns().addAll(columnConfiguration.configureColumns());
	}

	public HolcimFinalGridColumnConfig getColumnConfig(){
		return this.columnConfiguration;
	}
	
	
}
