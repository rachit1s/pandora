package com.tbitsGlobal.admin.client.widgets;

import java.util.List;

import com.tbitsGlobal.admin.client.plugins.AbstractPagePluginContainer;

public class PagePluginContainer extends AbstractPagePluginContainer{
	public PagePluginContainer() {
		super();
	}
	
	protected List<APPageLink> getPageLinks(){
		return null;
	}
	
	protected String getCaption(){
		return "Plugin Name";
	}
}
