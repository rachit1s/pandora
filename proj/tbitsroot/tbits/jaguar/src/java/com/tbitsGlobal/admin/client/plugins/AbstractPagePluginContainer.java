package com.tbitsGlobal.admin.client.plugins;

import java.util.List;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.widgets.APPageLink;

public abstract class AbstractPagePluginContainer extends ContentPanel{
	public AbstractPagePluginContainer() {
		super();
		
		this.setHeading(getCaption());
		this.setCollapsible(true);
		this.setBodyBorder(false);
		
		this.setExpanded(false);
	}
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		List<APPageLink> pages = getPageLinks();
		if(pages != null){
			for(APPageLink link : pages){
				this.add(link);
			}
		}
	}
	
	protected abstract List<APPageLink> getPageLinks();
	
	protected abstract String getCaption();
}
