package com.tbitsGlobal.admin.client.widgets.pages;

import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import com.tbitsGlobal.admin.client.widgets.pages.bulk.HolidayBulkGridPanel;

public class HolidayListView extends APTabItem{

	private HolidayBulkGridPanel bulkGridPanel;
	
	public HolidayListView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setLayout(new FitLayout());
	}

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		bulkGridPanel = new HolidayBulkGridPanel();
		this.add(bulkGridPanel, new FitData());
	}
}
