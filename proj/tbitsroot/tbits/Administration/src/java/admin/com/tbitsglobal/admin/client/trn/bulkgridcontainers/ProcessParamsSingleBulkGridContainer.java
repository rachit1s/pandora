package admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers;

import java.util.HashMap;

import admin.com.tbitsglobal.admin.client.trn.bulkgrids.ProcessParamsBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcessParam;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractIndividualBulkGridContainer;

public class ProcessParamsSingleBulkGridContainer extends AbstractIndividualBulkGridContainer<TrnProcessParam>{

	public ProcessParamsSingleBulkGridContainer(ProcessParamsBulkGrid bulkGrid) {
		super(bulkGrid);
	}

	@Override
	public void showStatus(HashMap<Integer, TrnProcessParam> statusMap) {
	}

	@Override
	public void updateModels() {
	}

}
