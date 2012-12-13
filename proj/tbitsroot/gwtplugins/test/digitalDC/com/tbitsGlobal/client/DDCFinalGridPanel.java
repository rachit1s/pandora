package digitalDC.com.tbitsGlobal.client;

import java.util.HashMap;

import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bulkupdate.AbstractBulkUpdatePanel;
import commons.com.tbitsGlobal.utils.client.bulkupdate.AbstractCommonBulkGridContainer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.AbstractSingleBulkGridContainer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;

public class DDCFinalGridPanel extends AbstractBulkUpdatePanel<DocNumberFileTuple> {

	

	public DDCFinalGridPanel(UIContext context){
		super();
		canAddRows = false;
		canDeleteRows = false;
		canReorderRows = false;
		
		AbstractSingleBulkGridContainer<DocNumberFileTuple> singleContainer = new AbstractSingleBulkGridContainer<DocNumberFileTuple>(new DDCFinalGrid(BulkGridMode.SINGLE)){
			public void showStatus(HashMap<Integer, DocNumberFileTuple> statusMap) {
				
			}

			public void updateModels() {
				
			}};
		context.setValue(BulkUpdateGridAbstract.CONTEXT_SINGLE_GRID_CONTAINER, singleContainer);
		AbstractCommonBulkGridContainer<DocNumberFileTuple> commonContainer = new AbstractCommonBulkGridContainer<DocNumberFileTuple>(context, new DDCFinalGrid(BulkGridMode.COMMON)){};
		
		this.singleGridContainer = singleContainer;
		this.commonGridContainer = commonContainer;
	}
	
	public DocNumberFileTuple getEmptyModel() {
		return new DocNumberFileTuple();
	}

	protected BulkUpdateGridAbstract<DocNumberFileTuple> getNewBulkGrid(BulkGridMode mode) {
		return new DDCFinalGrid(mode);
	}
	
	public DDCFinalGridColumnConfig getSingleGridColumnConfig(){
		return ((DDCFinalGrid)this.singleGridContainer.getBulkGrid()).getColumnConfig();
	}
	
	
}

