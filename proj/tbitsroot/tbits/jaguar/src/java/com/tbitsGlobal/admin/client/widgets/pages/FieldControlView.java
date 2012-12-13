package com.tbitsGlobal.admin.client.widgets.pages;

import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import com.tbitsGlobal.admin.client.widgets.pages.bulk.ControlsBulkGridPanel;

public class FieldControlView extends APTabItem {

	public FieldControlView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setLayout(new FitLayout());
	}
	
	public void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		ControlsBulkGridPanel cp = new ControlsBulkGridPanel();
		this.add(cp, new FitData());
	}
}
