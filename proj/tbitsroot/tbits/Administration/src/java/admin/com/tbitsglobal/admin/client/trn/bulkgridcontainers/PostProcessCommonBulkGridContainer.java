package admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers;

import admin.com.tbitsglobal.admin.client.trn.bulkgrids.PostProcessBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnPostProcessValue;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractCommonBulkGridContainer;

import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public class PostProcessCommonBulkGridContainer extends AbstractCommonBulkGridContainer<TrnPostProcessValue>{

	public PostProcessCommonBulkGridContainer(UIContext myContext, PostProcessBulkGrid bulkGrid) {
		super(myContext, bulkGrid);
	}

}
