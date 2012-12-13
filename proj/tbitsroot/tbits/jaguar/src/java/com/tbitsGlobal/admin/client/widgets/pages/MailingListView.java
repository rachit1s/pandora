package com.tbitsGlobal.admin.client.widgets.pages;

import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import com.tbitsGlobal.admin.client.widgets.pages.bulk.MailingListBulkGridPanel;

public class MailingListView extends APTabItem{

	private MailingListBulkGridPanel bulkGridPanel;
	
	public MailingListView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setLayout(new FitLayout());
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		bulkGridPanel = new MailingListBulkGridPanel();
		this.add(bulkGridPanel, new FitData());
	}

}
