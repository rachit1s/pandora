package admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers;

import java.util.HashMap;

import admin.com.tbitsglobal.admin.client.trn.bulkgrids.AttachmentListBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnAttachmentList;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractIndividualBulkGridContainer;

public class AttachmentListIndividualGridContainer extends AbstractIndividualBulkGridContainer<TrnAttachmentList>{

	public AttachmentListIndividualGridContainer(
			AttachmentListBulkGrid bulkGrid) {
		super(bulkGrid);
	}

	@Override
	public void showStatus(HashMap<Integer, TrnAttachmentList> statusMap) {
	}

	@Override
	public void updateModels() {
	}
}
