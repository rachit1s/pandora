package com.tbitsGlobal.jaguar.client.widgets.grids;

import com.tbitsGlobal.jaguar.client.searchgrid.SearchToolBar;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public class RequestsConsolidationGridToolBar extends SearchToolBar{

	public RequestsConsolidationGridToolBar(String sysPrefix, UIContext parentContext) {
		super(sysPrefix, parentContext);
	}
	
	@Override
	protected void initializeButtons() {
		super.initializeButtons();
		
		this.addPasteButton();
		this.addDeleteButton();
	}

}
