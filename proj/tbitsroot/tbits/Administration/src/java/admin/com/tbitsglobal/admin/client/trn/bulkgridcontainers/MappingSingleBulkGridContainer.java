package admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers;

import java.util.HashMap;

import admin.com.tbitsglobal.admin.client.trn.bulkgrids.MappingBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnFieldMapping;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractIndividualBulkGridContainer;

public class MappingSingleBulkGridContainer extends AbstractIndividualBulkGridContainer<TrnFieldMapping>{

	public MappingSingleBulkGridContainer(MappingBulkGrid bulkGrid) {
		super(bulkGrid);
	}

	@Override
	public void showStatus(HashMap<Integer, TrnFieldMapping> statusMap) {
	}

	@Override
	public void updateModels() {
	}

}
