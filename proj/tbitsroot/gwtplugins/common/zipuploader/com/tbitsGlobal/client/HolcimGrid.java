package zipuploader.com.tbitsGlobal.client;

import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;

public class HolcimGrid extends BulkUpdateGridAbstract<DocNumberFileTuple> {

	

	HolcimColumnConfig columnConfiguration;
	
	public HolcimGrid(BulkGridMode mode) {
		super(mode);
		showNumberer = false;
		showStatus	  = false;
		showSelectionModel = true;
		columnConfiguration = new HolcimColumnConfig();
	}

	protected void createColumns() {
		
		cm.getColumns().addAll(columnConfiguration.configureColumns(gridMode));
	}

	public HolcimColumnConfig getColumnConfig(){
		return this.columnConfiguration;
	}
	
	
}
