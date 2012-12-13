package admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers;

import admin.com.tbitsglobal.admin.client.trn.bulkgrids.MappingBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnFieldMapping;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractCommonBulkGridContainer;

import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public class MappingCommonBulkGridContainer extends AbstractCommonBulkGridContainer<TrnFieldMapping>{

	public MappingCommonBulkGridContainer(UIContext myContext, MappingBulkGrid bulkGrid) {
		super(myContext, bulkGrid);
	}
}
