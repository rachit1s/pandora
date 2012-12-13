package admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers;

import java.util.HashMap;

import admin.com.tbitsglobal.admin.client.trn.bulkgrids.ProcessBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcess;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractIndividualBulkGridContainer;

public class ProcessSingleBulkGridContainer extends AbstractIndividualBulkGridContainer<TrnProcess>{

	public ProcessSingleBulkGridContainer(ProcessBulkGrid bulkGrid) {
		super(bulkGrid);
	}

	@Override
	public void showStatus(HashMap<Integer, TrnProcess> statusMap) {
		
	}

	@Override
	public void updateModels() {
	}
}
