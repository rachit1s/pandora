package com.tbitsGlobal.jaguar.client.widgets;

import java.util.List;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.plugins.ITbitsMainTabPanelPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.MOMPluginSlot;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;

/**
 * Component that carries all the major UI components.
 * 
 * @author sourabh
 *
 */
public class MainPanel extends ContentPanel {
	public MainPanel() {
		super();
		this.setHeaderVisible(false);
//		this.setStyleAttribute("background", "#e9f2fa");
		this.setLayout(new FitLayout());
	}
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		Log.info("Rendering Main Panel");
		
		FitData fitData = new FitData(5, 0, 0, 0);
		
		/*
		 * Look for Tab Panel plugin
		 */
		List<ITbitsMainTabPanelPlugin> plugins = GWTPluginRegister.getInstance().getPlugins(MOMPluginSlot.class, ITbitsMainTabPanelPlugin.class);
		if(plugins != null && plugins.size() > 0){
			JaguarConstants.jaguarTabPanel = plugins.get(0).getWidget(null);
		}
		
		if(JaguarConstants.jaguarTabPanel == null)
			JaguarConstants.jaguarTabPanel = new TbitsMainTabPanel();
	    
		this.add(JaguarConstants.jaguarTabPanel, fitData);
	}
}
