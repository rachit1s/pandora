package com.tbitsGlobal.jaguar.client.dashboard;

import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * 
 * @author sourabh
 *
 * Tab showing the dashboard
 */
public class DashboardTab extends TabItem{
	
	/**
	 * Keeps a map of info and Buttons to Show/Hide a gadget
	 */
	private HashMap<GadgetInfo, ToggleButton> buttonMap;
	
	/**
	 * Keeps a map of info and gadgets
	 */
	private HashMap<GadgetInfo, Gadget> gadgetMap;
	
	/**
	 * Container to hold gadgets
	 */
	private ContentPanel container;
	
	/**
	 * Toolbar to hold buttons to Show/Hide gadgets
	 */
	private ButtonBar toolbar;
	
	/**
	 * Constructor
	 */
	public DashboardTab() {
		// Initialize with the caption. TODO : externalize this caption
		super("Dashboard");
		this.setLayout(new FitLayout());
		
		buttonMap = new HashMap<GadgetInfo, ToggleButton>();
		gadgetMap = new HashMap<GadgetInfo, Gadget>();
		
		container = new ContentPanel();
		container.setHeaderVisible(false);
		container.setBodyBorder(false);
		container.setLayout(new ColumnLayout());
		container.setScrollMode(Scroll.AUTO);
		toolbar = new ButtonBar();
		container.setTopComponent(toolbar);
		this.add(container, new FitData());
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		// Collect infos for all the gadgets
		JaguarConstants.dbService.getGadgetInfo(new AsyncCallback<List<GadgetInfo>>(){
			public void onFailure(Throwable caught) {
				Log.error("Error retreiving gadgets...", caught);
			}

			public void onSuccess(List<GadgetInfo> result) {
				if(result != null){
					for(final GadgetInfo info : result){
						// Button to toggle state of the gadget
						ToggleButton toggleBtn = new ToggleButton(info.getCaption(), new SelectionListener<ButtonEvent>(){
							@Override
							public void componentSelected(ButtonEvent ce) {
								if(info.isVisible()){
									hideGadget(info);
								}else{
									showGadget(info);
								}
							}});
						toggleBtn.setStyleAttribute("marginLeft", "5px");
						toolbar.add(toggleBtn);
						buttonMap.put(info, toggleBtn);
						
						Gadget gadget = createGadget(info);
						container.add(gadget, new ColumnData(info.getWidth()));
//						gadget.show();
						
						if(!info.isVisible()){
							hideGadget(info);
						}
					}
				}
				
				DashboardTab.this.layout();
			}});
	}
	
	/**
	 * Creates a gadget from info
	 * @param info
	 * @return the {@link Gadget}
	 */
	protected Gadget createGadget(GadgetInfo info){
		Gadget gadget = new Gadget(info){
			@Override
			protected void onHide() {
				super.onHide();
				
				ToggleButton btn = buttonMap.get(info);
				if(btn != null)
					btn.toggle(false);
			}
			
			@Override
			protected void onShow() {
				super.onShow();
				
				ToggleButton btn = buttonMap.get(info);
				if(btn != null)
					btn.toggle(true);
			}
		};
		
		gadgetMap.put(info, gadget);
		
		ToggleButton btn = buttonMap.get(info);
		if(btn != null)
			btn.toggle(true);
		
		return gadget;
	}
	
	/**
	 * Hides a gadget
	 * @param info
	 */
	protected void hideGadget(GadgetInfo info){
		Gadget gadget = gadgetMap.get(info);
		if(gadget != null){
			gadget.hide();
		}
	}
	
	/**
	 * Shows a gadget
	 * @param info
	 */
	protected void showGadget(GadgetInfo info){
		Gadget gadget = gadgetMap.get(info);
		if(gadget != null){
			gadget.show();
		}
	}
}
