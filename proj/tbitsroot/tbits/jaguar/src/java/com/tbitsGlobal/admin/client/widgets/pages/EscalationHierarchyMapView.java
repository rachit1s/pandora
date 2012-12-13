package com.tbitsGlobal.admin.client.widgets.pages;


import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;



/**
 * @author Nirmal Agrawal
 * 
 */
public class EscalationHierarchyMapView extends APTabItem {

	public EscalationHierarchyMapView(LinkIdentifier linkId) {
		super(linkId);
		this.setClosable(true);
		this.setBorders(false);
		this.setLayout(new FitLayout());
	}
	
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		EscalationHierarchyMapPanel cp = new EscalationHierarchyMapPanel();
		this.add(cp, new FitData());
	}
	

}
