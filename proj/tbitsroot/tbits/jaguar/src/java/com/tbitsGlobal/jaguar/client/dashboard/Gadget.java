package com.tbitsGlobal.jaguar.client.dashboard;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * 
 * @author sourabh
 *
 * Represents a gadget in the dashboard
 */
public class Gadget extends ContentPanel{
	protected GadgetInfo info;
	
	/**
	 * The refresh timer
	 */
	protected Timer timer;
	
	/**
	 * Menu to be displayed when user clicks on configure button on the top of the gadget
	 */
	protected Menu configureMenu;
	
	/**
	 * Container to hold report HTML
	 */
	private ContentPanel container;
	
	public Gadget(GadgetInfo info) {
		super();
		
		this.info = info;
		
//		this.setDraggable(false);
//		this.setResizable(false);
		this.setCollapsible(true);
		this.setShadow(false);
//		this.setOnEsc(false);
		this.setLayout(new FitLayout());
		this.setScrollMode(Scroll.AUTO);
		this.setStyleAttribute("margin", "5px");
		
		container = new ContentPanel();
		container.setHeaderVisible(false);
		container.setBodyBorder(false);
		container.setScrollMode(Scroll.AUTO);
		
		this.add(container, new FitData());
		
		this.setHeading(this.info.getCaption());
		
		// Set height if > 0 else use default
		if(info.getHeight() > 0)
			this.setHeight(info.getHeight());
		
		// Set width if > 0 else use default
		if(info.getWidth() > 0)
			this.setWidth(info.getWidth());
		
		// make the configure menu
		this.setConfigureMenu();
		
		// the button used to configure the gadget
		this.getHeader().addTool(new ToolButton("x-tool-gear", new SelectionListener<IconButtonEvent>(){
			@Override
			public void componentSelected(IconButtonEvent ce) {
				configureMenu.show(ce.getIconButton());
			}}));
		
		// refresh button
		this.getHeader().addTool(new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>(){
			@Override
			public void componentSelected(IconButtonEvent ce) {
				Gadget.this.refresh();
			}}));
		
		timer = new Timer(){
			@Override
			public void run() {
				Gadget.this.refresh();
			}};
		
		if(this.info.getRefreshRate() > 0)
			timer.scheduleRepeating(this.info.getRefreshRate());
		
		// refresh for the first time to bring the content
		this.refresh();
	}
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		if(info.isMinimized())
			collapse();
		
//		this.setPosition(info.getLeft(), info.getTop());
	}
	
	@Override
	protected void onCollapse() {
		super.onCollapse();
		
		info.setIsMinimized(true);
		this.updateInfo();
	}
	
	@Override
	protected void onExpand() {
		super.onExpand();
		
		info.setIsMinimized(false);
		this.updateInfo();
	}
	
//	@Override
//	protected void onResize(int width, int height) {
//		super.onResize(width, height);
//		
//		this.info.setWidth(width);
//		this.info.setHeight(height);
//		this.updateInfo();
//	}
	
	/**
	 * Brings the content of gadget from server
	 */
	protected void refresh(){
		int reportId = info.getGadgetId();
		JaguarConstants.dbService.getGadgetContent(reportId, new AsyncCallback<String>(){
			public void onFailure(Throwable caught) {
				Log.error("Error getting gadget content", caught);
			}

			public void onSuccess(String result) {
				if(result != null){
					container.removeAll();
					container.addText(result);
					container.layout();
				}
			}});
	}
	
	@Override
	protected void onHide() {
		super.onHide();
		
		// cancel the timer to prevent unnecessary refresh. It would be rescheduled when gadget is shown
		this.timer.cancel();
		
		this.info.setIsVisble(false);
		
		this.updateInfo();
	}
	
//	/**
//	 * Overriding to implemet show() of {@link ContentPanel}
//	 */
//	@Override
//	public void show() {
//		if (fireEvent(Events.BeforeShow)) {
//	      hidden = false;
//	      if (rendered) {
//	        onShow();
//	      }
//	      fireEvent(Events.Show);
//	    }
//	}
	
	@Override
	protected void onShow() {
		super.onShow();
		
//		this.setPosition(info.getLeft(), info.getTop());
		
		if(this.info.getRefreshRate() > 0)
			timer.scheduleRepeating(this.info.getRefreshRate());
		
		this.info.setIsVisble(true);
		
		this.updateInfo();
	}
	
//	@Override
//	protected void onPosition(int x, int y) {
//		super.onPosition(x, y);
//		
//		Point point = getPosition(true);
//		if(info.getLeft() != point.x || info.getTop() != point.y){
//			info.setLeft(point.x);
//			info.setTop(point.y);
//			
//			this.updateInfo();
//		}
//	}
	
	protected void setConfigureMenu(){
		configureMenu = new Menu();
		configureMenu.add(new MenuItem("Refresh every 5 mins", new SelectionListener<MenuEvent>(){
			@Override
			public void componentSelected(MenuEvent ce) {
				updateRefreshRate(5 * 60 * 1000);
			}}));
		configureMenu.add(new MenuItem("Refresh every 30 secs", new SelectionListener<MenuEvent>(){
			@Override
			public void componentSelected(MenuEvent ce) {
				updateRefreshRate(30 * 1000);
			}}));
		configureMenu.add(new MenuItem("Refresh every 20 secs", new SelectionListener<MenuEvent>(){
			@Override
			public void componentSelected(MenuEvent ce) {
				updateRefreshRate(20 * 1000);
			}}));
		configureMenu.add(new MenuItem("Remove auto refresh", new SelectionListener<MenuEvent>(){
			@Override
			public void componentSelected(MenuEvent ce) {
				updateRefreshRate(0);
			}}));
		
//		configureMenu.add(new MenuItem("<a target='_blank' href='" + ClientUtils.getUrlToFilefromBase("/reports/frameset?__report=" + info.getReportFile() + "&user_id=" + Jaguar.currentUser.getUserId() + info.getParamQuery()) + "'>View</a>"));
		
//		configureMenu.add(new MenuItem("Change report parameters"));
	}
	
	protected void updateRefreshRate(final int milliSeconds){
		Gadget.this.info.setRefreshRate(milliSeconds);
		JaguarConstants.dbService.updateGadgetInfo(this.info, new AsyncCallback<GadgetInfo>(){
			public void onFailure(Throwable caught) {
				Log.error("Error updating gadget info", caught);
			}

			public void onSuccess(GadgetInfo result) {
				if(result == null)
					Log.error("Error updating gadget info");
				else{
					if(milliSeconds > 0)
						timer.scheduleRepeating(milliSeconds);
					else{
						timer.cancel();
					}
				}
			}});
	}
	
	/**
	 * Updates the gadget info to the db
	 */
	private void updateInfo(){
		JaguarConstants.dbService.updateGadgetInfo(Gadget.this.info, new AsyncCallback<GadgetInfo>(){
			public void onFailure(Throwable caught) {
				Log.error("Error updating gadget info", caught);
			}

			public void onSuccess(GadgetInfo result) {
				if(result == null)
					Log.error("Error updating gadget info");
			}});
	}
}
