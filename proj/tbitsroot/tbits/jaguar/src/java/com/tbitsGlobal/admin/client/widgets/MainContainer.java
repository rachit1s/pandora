package com.tbitsGlobal.admin.client.widgets;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;

/**
 * @author dheeru
 * 
 */
public class MainContainer extends ContentPanel {

	protected static MainContainer self;

	public APHeader header; // Header of Admin Panel
	public APLeftMenu leftGrid;
	public TabContainer tabContainer; // information area of admin panel

	private APStatusBar footer; // footer for admin panel

	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;

	protected MainContainer() {
		super();
		this.setBodyBorder(false);
		this.setHeaderVisible(false);
		this.setLayout(new FitLayout());
		
		LayoutContainer c = new LayoutContainer();
		c.setLayout(new BorderLayout());
		c.setStyleAttribute("background", "#fff");
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		// header size and features
		header = new APHeader();

		// leftGrid size and features
		leftGrid = new APLeftMenu();
		BorderLayoutData wData = new BorderLayoutData(LayoutRegion.WEST, 210);
		wData.setCollapsible(false);

		// infoBox size and features
		tabContainer = new TabContainer();
		BorderLayoutData cData = new BorderLayoutData(LayoutRegion.CENTER);
		cData.setMargins(new Margins(0, 0, 0, 3));
		
		c.add(leftGrid, wData);
		c.add(tabContainer, cData);

		// footer size and features + log
		footer = new APStatusBar();

		this.setTopComponent(header);
		this.add(c, new FitData(5, 0, 0, 0));
		this.setBottomComponent(footer);
	}

	public static MainContainer getInstance() {
		if (self == null)
			self = new MainContainer();
		return self;
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		
		observable.detach();
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		
		observable.attach();
	}
}