package admin.com.tbitsglobal.admin.client.trn.bulkgridcontainers;

import admin.com.tbitsglobal.admin.client.trn.bulkgrids.ProcessBulkGrid;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcess;
import bulkupdate.com.tbitsglobal.bulkupdate.client.AbstractCommonBulkGridContainer;

import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public class ProcessCommonGridContainer extends AbstractCommonBulkGridContainer<TrnProcess>{

	public ProcessCommonGridContainer(UIContext myContext, ProcessBulkGrid bulkGrid) {
		super(myContext, bulkGrid);
	}
}
