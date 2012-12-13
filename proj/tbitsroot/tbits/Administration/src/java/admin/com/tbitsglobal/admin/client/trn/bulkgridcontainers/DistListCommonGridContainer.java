package admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers;

import admin.com.tbitsglobal.admin.client.trn.bulkgrids.DistListBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnDistList;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractCommonBulkGridContainer;

import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public class DistListCommonGridContainer extends AbstractCommonBulkGridContainer<TrnDistList>{

	public DistListCommonGridContainer(UIContext myContext, DistListBulkGrid bulkGrid) {
		super(myContext, bulkGrid);
	}
}
