package transmittal.com.tbitsGlobal.client;

import java.util.List;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;

/**
 * The bulk grid used for page 2 panel of transmittal wizard
 * @author devashish
 *
 */
public class Page2Grid extends BulkUpdateGridAbstract<TbitsTreeRequestData> {

	
	AttachmentSelectionTableColumnsConfiguration columnConfiguration;
	
	public Page2Grid(BulkGridMode mode,WizardData wizardData) {
		super(mode);
		showNumberer = false;
		showStatus	  = false;
		showSelectionModel = true;
		columnConfiguration = new AttachmentSelectionTableColumnsConfiguration(wizardData);
	}

	protected void createColumns() {
		cm.getColumns().addAll(columnConfiguration.configureColumns());
	}
	protected void setmodifiedConfig(AttachmentSelectionTableColumnsConfiguration columnConfiguration2) {
		
		columnConfiguration=columnConfiguration2;
	
	}

	public AttachmentSelectionTableColumnsConfiguration getColumnConfig(){
		return this.columnConfiguration;
	}

	
	
}