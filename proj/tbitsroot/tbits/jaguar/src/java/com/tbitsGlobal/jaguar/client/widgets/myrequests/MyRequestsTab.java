package com.tbitsGlobal.jaguar.client.widgets.myrequests;

import java.util.ArrayList;
import java.util.HashMap;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.serializables.BARequests;

import commons.com.tbitsGlobal.utils.client.Captions;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * Tab to show "My Requests".
 * 
 * It loads the requests at the time of rendering.
 * 
 * @author sourabh
 *
 */
public class MyRequestsTab extends TabItem implements IFixedFields{
	private HashMap<String,BARequests> baRequestsMap;
	
	private ContentPanel gridContainer;
	private CheckBox logger;
	private CheckBox assignee;
	private CheckBox subscribers;
	
	/**
	 * Constructor
	 */
	public MyRequestsTab() {
		super(Captions.getCommonCaptionByKey(Captions.CAPTIONS_ALL_MY_REQUESTS));
		this.setBorders(false);
		this.setLayout(new FitLayout());
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		gridContainer = new ContentPanel(new RowLayout());
		gridContainer.setHeaderVisible(false);
		gridContainer.setBodyBorder(false);
		gridContainer.setScrollMode(Scroll.AUTO);
		
		this.addToolBar();
		
		this.add(gridContainer, new FitData());
		
		buildTab();
	}
	
	/**
	 * Builds tab by retrieving the data.
	 */
	private void buildTab(){
		TbitsInfo.info("Loading " + Captions.getBACaptions(0).get(Captions.CAPTIONS_ALL_MY_REQUESTS) + "... Please Wait...");
		
		final int pageSize = 10;
		JaguarConstants.dbService.getMyRequests(getFilterFields(), pageSize, 1, new AsyncCallback<HashMap<String,BARequests>>(){
			public void onFailure(Throwable arg0) {
				TbitsInfo.error("Error while loading data...", arg0);
			}

			public void onSuccess(HashMap<String,BARequests> result) {
				baRequestsMap = result;
				boolean hasRequests = false;
				
				if (baRequestsMap != null){
					for(String sysPrefix : baRequestsMap.keySet()){
						if(buildGrid(sysPrefix, pageSize))
							hasRequests = true;
					}
				}
				if(!hasRequests){
					gridContainer.addText("There are no " + Captions.getRecordDisplayName() + "s to display. Please change the filters and try again.");
					MyRequestsTab.this.layout();
				}
			}});
	}
	
	private boolean buildGrid(String sysPrefix, int pageSize){
		BARequests baRequests = baRequestsMap.get(sysPrefix);
		if(baRequests.getRequestCount() == 0)
			return false;
		
		MyRequestsGrid grid = new MyRequestsGrid(sysPrefix, baRequests);
		
		MyRequestsGridContainer container = new MyRequestsGridContainer(sysPrefix, grid, ClientUtils.getBAbySysPrefix(sysPrefix).getDisplayText(), pageSize, getFilterFields());
		
		gridContainer.add(container, new RowData(0.97, 250, new Margins(15, 5, 0, 5)));
		
		this.layout();
		
		return true;
	}
	
	private void addToolBar(){
		ToolBar bar = new ToolBar();
		
		LabelField label = new LabelField("Display requests where " + ClientUtils.getCurrentUser().getUserLogin() + " is ");
		bar.add(label);
		
		logger = new CheckBox();
		logger.setBoxLabel("Logger");
		logger.setName(LOGGER);
		logger.setValue(true);
		bar.add(logger);
		
		assignee = new CheckBox();
		assignee.setBoxLabel("Assignee");
		assignee.setName(ASSIGNEE);
		bar.add(assignee);
		
		subscribers = new CheckBox();
		subscribers.setBoxLabel("Subscriber");
		subscribers.setName(SUBSCRIBER);
		bar.add(subscribers);
		
		ToolBarButton refresh = new ToolBarButton("Refresh", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				gridContainer.removeAll();
				buildTab();
			}});
		bar.add(refresh);
		
		gridContainer.setTopComponent(bar);
	}
	
	private ArrayList<String> getFilterFields(){
		ArrayList<String> filterFields = new ArrayList<String>();
		if(logger.getValue())
			filterFields.add(logger.getName());
		if(assignee.getValue())
			filterFields.add(assignee.getName());
		if(subscribers.getValue())
			filterFields.add(subscribers.getName());
		
		return filterFields;
	}
}
