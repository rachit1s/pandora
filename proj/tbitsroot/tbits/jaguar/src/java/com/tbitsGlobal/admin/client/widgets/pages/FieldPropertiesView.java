package com.tbitsGlobal.admin.client.widgets.pages;

import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import com.tbitsGlobal.admin.client.widgets.pages.bulk.FieldsBulkGridPanel;

import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;

public class FieldPropertiesView extends APTabItem{

	private FieldsBulkGridPanel bulkGridPanel;
	
	public FieldPropertiesView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setLayout(new FitLayout());
		this.setLayoutOnChange(true);
		
		observable.subscribe(OnChangeBA.class, new ITbitsEventHandle<OnChangeBA>(){
			public void handleEvent(OnChangeBA event) {
				if(bulkGridPanel != null)
					bulkGridPanel.refresh(0);
			}	
		});
	}
	
	public void onRender(Element parent, int pos) {
		super.onRender(parent, pos);

		bulkGridPanel = new FieldsBulkGridPanel();
		this.add(bulkGridPanel, new FitData());
	}

}
