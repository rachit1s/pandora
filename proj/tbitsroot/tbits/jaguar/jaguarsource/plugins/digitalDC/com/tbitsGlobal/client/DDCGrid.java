package digitalDC.com.tbitsGlobal.client;

import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;

public class DDCGrid extends BulkUpdateGridAbstract<DocNumberFileTuple> {

	

	DDCColumnConfig columnConfiguration;
	
	public DDCGrid(BulkGridMode mode) {
		super(mode);
		showNumberer = false;
		showStatus	  = false;
		showSelectionModel = true;
		columnConfiguration = new DDCColumnConfig();
	}

	protected void createColumns() {
		
		cm.getColumns().addAll(columnConfiguration.configureColumns(gridMode));
	}

	public DDCColumnConfig getColumnConfig(){
		return this.columnConfiguration;
	}
	
	
}
