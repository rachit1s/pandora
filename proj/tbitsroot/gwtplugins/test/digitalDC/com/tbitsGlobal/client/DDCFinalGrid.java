package digitalDC.com.tbitsGlobal.client;

import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;

public class DDCFinalGrid extends BulkUpdateGridAbstract<DocNumberFileTuple> {

	

	DDCFinalGridColumnConfig columnConfiguration;
	
	public DDCFinalGrid(BulkGridMode mode) {
		super(mode);
		showNumberer = false;
		showStatus	  = false;
		showSelectionModel = true;
		columnConfiguration = new DDCFinalGridColumnConfig();
	}

	protected void createColumns() {
		cm.getColumns().addAll(columnConfiguration.configureColumns());
	}

	public DDCFinalGridColumnConfig getColumnConfig(){
		return this.columnConfiguration;
	}
	
	
}
