package admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers;

import java.util.HashMap;

import admin.com.tbitsglobal.admin.client.trn.bulkgrids.DistListBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnDistList;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractIndividualBulkGridContainer;

public class DistListSingleBulkGridContainer extends AbstractIndividualBulkGridContainer<TrnDistList>{

	public DistListSingleBulkGridContainer(
			DistListBulkGrid bulkGrid) {
		super(bulkGrid);
	}

	@Override
	public void showStatus(HashMap<Integer, TrnDistList> statusMap) {
	}

	@Override
	public void updateModels() {
	}
}
