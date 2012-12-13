package admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers;

import admin.com.tbitsglobal.admin.client.trn.bulkgrids.ProcessParamsBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcessParam;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractCommonBulkGridContainer;

import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public class ProcessParamCommonGridContainer extends AbstractCommonBulkGridContainer<TrnProcessParam> {

	public ProcessParamCommonGridContainer(UIContext myContext, ProcessParamsBulkGrid bulkGrid) {
		super(myContext, bulkGrid);
	}
}
