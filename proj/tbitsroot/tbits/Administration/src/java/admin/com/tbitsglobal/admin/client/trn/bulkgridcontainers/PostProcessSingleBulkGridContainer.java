package admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers;

import java.util.HashMap;

import admin.com.tbitsglobal.admin.client.trn.bulkgrids.PostProcessBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnPostProcessValue;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractIndividualBulkGridContainer;

public class PostProcessSingleBulkGridContainer extends AbstractIndividualBulkGridContainer<TrnPostProcessValue>{

	public PostProcessSingleBulkGridContainer(PostProcessBulkGrid bulkGrid) {
		super(bulkGrid);
	}

	@Override
	public void showStatus(HashMap<Integer, TrnPostProcessValue> statusMap) {
	}

	@Override
	public void updateModels() {
	}

}
