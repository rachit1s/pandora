package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;

public class BAMenuView extends APTabItem{

	private BAMenuBulkUpdatePanel bulkGridPanel;
	
	public BAMenuView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setLayout(new FitLayout());

		observable.subscribe(OnChangeBA.class, new ITbitsEventHandle<OnChangeBA>(){
			public void handleEvent(OnChangeBA event) {
				if(bulkGridPanel != null)
					bulkGridPanel.refresh(0);
			}	
		});
	}

	public void onRender(Element parent, int pos) {
		super.onRender(parent, pos);

		bulkGridPanel = new BAMenuBulkUpdatePanel();
		this.add(bulkGridPanel, new FitData());
	}
}
