package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;

/**
 * Panel to hold the bulk grid for editing 'All Users' page in
 * the admin panel
 *
 */
public class AllUsersViewBulkUpdate extends APTabItem{
	
	private UserBulkGridPanel bulkGridPanel;
	
	public AllUsersViewBulkUpdate(LinkIdentifier linkId) {
		super(linkId);
		
		this.setLayout(new FitLayout());
		this.setClosable(true);
	}
	
	public void onRender(Element parent , int pos){
		super.onRender(parent, pos);

		bulkGridPanel = new UserBulkGridPanel();
		this.add(bulkGridPanel, new FitData());
	}
}
