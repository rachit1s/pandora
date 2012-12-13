package admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers;

import admin.com.tbitsglobal.admin.client.trn.bulkgrids.AttachmentListBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnAttachmentList;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractCommonBulkGridContainer;

import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public class AttachmentListCommonGridContainer extends AbstractCommonBulkGridContainer<TrnAttachmentList>{

	public AttachmentListCommonGridContainer(UIContext myContext,
			AttachmentListBulkGrid bulkGrid) {
		super(myContext, bulkGrid);
	}
}
