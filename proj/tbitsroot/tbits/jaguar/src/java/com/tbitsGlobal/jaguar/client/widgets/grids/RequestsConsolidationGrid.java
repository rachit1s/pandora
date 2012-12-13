package com.tbitsGlobal.jaguar.client.widgets.grids;

import commons.com.tbitsGlobal.utils.client.grids.GridColumnView;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGrid;

/**
 * @author sourabh
 * 
 * Grid for consolidation of requests
 */
public class RequestsConsolidationGrid extends RequestsViewGrid{
	
	public RequestsConsolidationGrid(String sysPrefix) {
		super(sysPrefix);
		
		this.isCustomizable = false;
	}

	public GridColumnView getViewId() {
		return GridColumnView.SearchGrid;
	}
}
