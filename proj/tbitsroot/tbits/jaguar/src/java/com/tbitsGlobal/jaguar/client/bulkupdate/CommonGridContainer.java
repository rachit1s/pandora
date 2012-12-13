package com.tbitsGlobal.jaguar.client.bulkupdate;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bulkupdate.AbstractCommonBulkGridContainer;

/**
 * Contains the common {@link BulkUpdateGrid}
 * 
 * @author sourabh
 *
 */
public class CommonGridContainer extends AbstractCommonBulkGridContainer<TbitsTreeRequestData> {
	public CommonGridContainer(UIContext myContext, BulkUpdateGrid bulkGrid) {
		super(myContext, bulkGrid);
	}
}
