package com.tbitsGlobal.jaguar.client.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.bulkupdate.BulkUpdatePanel;
import com.tbitsGlobal.jaguar.client.dashboard.DashboardTab;
import com.tbitsGlobal.jaguar.client.events.OnConsolidateRequests;
import com.tbitsGlobal.jaguar.client.events.ToAddNewRequest;
import com.tbitsGlobal.jaguar.client.events.ToBulkUpdate;
import com.tbitsGlobal.jaguar.client.events.ToPinSearchResults;
import com.tbitsGlobal.jaguar.client.events.ToUpdateRequest;
import com.tbitsGlobal.jaguar.client.events.ToUpdateRequestOtherBA;
import com.tbitsGlobal.jaguar.client.events.ToViewDrafts;
import com.tbitsGlobal.jaguar.client.events.ToViewRequest;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;
import com.tbitsGlobal.jaguar.client.widgets.advsearch.AdvSearchTab;
import com.tbitsGlobal.jaguar.client.widgets.forms.RequestFormFactory;
import com.tbitsGlobal.jaguar.client.widgets.grids.RequestsConsolidationGrid;
import com.tbitsGlobal.jaguar.client.widgets.grids.RequestsConsolidationGridContainer;
import com.tbitsGlobal.jaguar.client.widgets.myreports.MyReportsTab;
import com.tbitsGlobal.jaguar.client.widgets.myrequests.MyRequestsTab;
import com.tbitsGlobal.jaguar.client.widgets.search.SearchTab;
import commons.com.tbitsGlobal.utils.client.Captions;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsNativePreviewHandler;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.KeyboardShortcutEvent;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.Events.OnDeleteDraft;
import commons.com.tbitsGlobal.utils.client.Events.OnHistoryTokensChanged;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ToAddSubRequest;
import commons.com.tbitsGlobal.utils.client.Events.ToViewRequestOtherBA;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserDraftClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.pojo.POJOInt;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IAddRequestForm;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IEditRequestForm;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IRequestForm;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IRequestFormData;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IUpdateRequestForm;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IViewRequestForm;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;

/**
 * Component that contains all the major tabs in {@link MainPanel}.
 * 
 * @author sourabh
 *
 */
public class TbitsMainTabPanel extends TabPanel implements IFixedFields{
	
	protected HashMap<String, ArrayList<HistoryEnabledTab>> historyEnabledTabs;
	protected SearchTab searchTab;
	protected AdvSearchTab advSearchTab;
	protected MyRequestsTab myRequestsTab;
	protected MyReportsTab myReportsTab;
	protected DashboardTab dashboardTab;
	protected TagsTab tagsTab;
	
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	public TbitsMainTabPanel() {
		super();
		this.setBodyBorder(false);
		this.setTabScroll(true);
		this.setAnimScroll(true);
		this.setCloseContextMenu(true);
		this.setBorderStyle(false);
		this.setPlain(true);
		
		this.historyEnabledTabs = new HashMap<String, ArrayList<HistoryEnabledTab>>();
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		this.addEventHandles();
		this.setResizeTabs(true);
	}

	/**
	 * WARNING: GXT-HACK: This might make it dis-function if the gxt is upgraded. 
	 * This piece of code has been taken from TabPanel class of gxt. Since this code was rendering as well as showing, there was possibility of changing the menu.
	 * The only change I have done it moved the show line. 
	 * @param item
	 * @param x
	 * @param y
	 */
	protected void super_onItemContextMenu(TabItem item, int x, int y) {
	    if (isCloseContextMenu()) {
	      if (closeContextMenu == null) {
	        closeContextMenu = new Menu();
	        closeContextMenu.addListener(Events.Hide, new Listener<MenuEvent>() {
	          public void handleEvent(MenuEvent be) {
	            be.getContainer().setData("tab", null);
	          }
	        });

	        closeContextMenu.add(new MenuItem("Close this tab", new SelectionListener<MenuEvent>() {
	          @Override
	          public void componentSelected(MenuEvent ce) {
	            TabItem item = (TabItem) ce.getContainer().getData("tab");
	            close(item);
	          }
	        }));
	        closeContextMenu.add(new MenuItem("Close all other tabs", new SelectionListener<MenuEvent>() {

	          @Override
	          public void componentSelected(MenuEvent ce) {
	            TabItem item = (TabItem) ce.getContainer().getData("tab");
	            List<TabItem> items = new ArrayList<TabItem>();
	            items.addAll(getItems());
	            for (TabItem currentItem : items) {
	              if (currentItem != item && currentItem.isClosable()) {
	                close(currentItem);
	              }
	            }
	          }

	        }));
	      }

	      closeContextMenu.getItem(0).setEnabled(item.isClosable());
	      closeContextMenu.setData("tab", item);
	      boolean hasClosable = false;
	      for (TabItem item2 : getItems()) {
	        if (item2.isClosable() && item2 != item) {
	          hasClosable = true;
	          break;
	        }
	      }
	      closeContextMenu.getItem(1).setEnabled(hasClosable);
	    }
	  }

	ArrayList<TabItem> tabsForCtxMnu = new ArrayList<TabItem>();
	ArrayList<TabItem> tabsInCtxMnu = new ArrayList<TabItem>();
	/**
	 * The objective is to display the right click context menu containing a list of tabs opened to give a 
	 * clear view of opened tabs to the users.
	 * We maintain two lists of tabs 
	 * 	* tabsInCtxMnu - tabs that have already been rendered
	 *  * tabsForCtxMnu - tabs that are yet to be rendered
	 *  So, when a new tab is opened, it is put in tabsForCtxMnu. Once the context menu is open, it appends these tabs to the menu and
	 *  then moves these tabs to tabsInCtxMnu.
	 *  
	 *  When a tab is closed, it tries to remove it from both the list as it might be at any of the place.
	 * @param item
	 * @param x
	 * @param y
	 */
	protected void onItemContextMenu(TabItem item, int x, int y) 
	{
		super_onItemContextMenu(item, x, y);
		if(this.closeContextMenu != null)
		{
			//If this is the first time.
			if(tabsInCtxMnu.size() == 0)
				this.closeContextMenu.add(new SeparatorMenuItem());

			for(TabItem tab:tabsForCtxMnu)
			{
				if(tabsInCtxMnu.contains(tab))
					continue;
				
				MenuItem menuItem = new MenuItem(tab.getText());
				menuItem.setData("reftab", tab);
				menuItem.addSelectionListener(new SelectionListener<MenuEvent>() {
					 @Override
			          public void componentSelected(MenuEvent ce) {
			            TabItem item = (TabItem) ce.getItem().getData("reftab");
			            item.getTabPanel().setSelection(item);
			          }
				});
				
				this.closeContextMenu.add(menuItem);
				tabsInCtxMnu.add(tab);
			}
			tabsForCtxMnu.clear();
			closeContextMenu.setAutoWidth(true);
			closeContextMenu.showAt(x, y);
			
		}

	}
	
	protected void onAttach() {
		super.onAttach();
		
		observable.attach();
	}
	
	
	protected void onDetach() {
		super.onDetach();
		
		observable.detach();
	}
	
	/**
	 * Adds the "Search" Tab.
	 */
	protected void addSearchTab(){
		Log.info("Initializing Basic Search tab");
		
		if(searchTab == null){
			searchTab = new SearchTab();
		}
	    this.add(searchTab);
	    this.setSelection(searchTab);
	}

	@Override
	protected void onRemove(TabItem item) {
		tabsForCtxMnu.remove(item);
		tabsInCtxMnu.remove(item);
		
		tabsForCtxMnu.addAll(tabsInCtxMnu);
		tabsInCtxMnu.clear();
		
		this.closeContextMenu = null;
		super.onRemove(item);
	}
	/**
	 * Adds the "Advanced Search" Tab.
	 */
	protected void addAdvSearchTab(){
		if(advSearchTab == null){
			advSearchTab = new AdvSearchTab();
		}
	    this.add(advSearchTab);
	}
	
	/**
	 * Adds the "My Requests" Tab.
	 */
	protected void addMyRequestsTab(){
		if(myRequestsTab == null){
			myRequestsTab = new MyRequestsTab();
		}
		this.add(myRequestsTab);
	}
	
	/**
	 * Adds the "My Reports" Tab.
	 */
	protected void addMyReportsTab(){
		if(myReportsTab == null){
			myReportsTab = new MyReportsTab();
		}
		this.add(myReportsTab);
	}
	
	/**
	 * Adds the "Dashboard" Tab.
	 */
	protected void addDashboardTab(){
		if(dashboardTab == null){
			dashboardTab = new DashboardTab();
		}
		this.add(dashboardTab);
	}
	
	/**
	 * Adds the "Tags" Tab.
	 */
	protected void addTagsTab(){
		if(tagsTab == null){
			tagsTab = new TagsTab();
		}
		this.add(tagsTab);
	}
	
	/**
	 * Adds a Consolidation Grid Tab
	 * @param sysPrefix
	 * @param models
	 */
	protected void addConsolidationTab(String sysPrefix, final List<TbitsTreeRequestData> models){
		TabItem tab = new TabItem("Consolidate " + Captions.getRecordDisplayName() + "s");
		tab.setClosable(true);
		tab.setLayout(new FitLayout());
		
		final RequestsConsolidationGrid grid = new RequestsConsolidationGrid(sysPrefix);
		RequestsConsolidationGridContainer container =  new RequestsConsolidationGridContainer(sysPrefix, grid);
		tab.add(container, new FitData());
		
		this.add(tab);
		this.setSelection(tab);
		
		DelayedTask task = new DelayedTask(new Listener<BaseEvent>(){
			
			public void handleEvent(BaseEvent be) {
				if(models != null){
					grid.addModels(models);
				}
			}});
		task.delay(5);
	}
	
	protected void addPinnedSearchTab(){
//		TabItem tab = new TabItem("Pinned Search");
//		tab.setClosable(true);
//		tab.setLayout(new FitLayout());
		
//		List<ISearchResultsPanelPlugin> plugins = 
//			GWTPluginRegister.getInstance().getPlugins(SearchResultsViewPluginSlot.class, ISearchResultsPanelPlugin.class);
//		if(plugins != null && plugins.size() > 0){
//			ISearchResultsPanelPlugin plugin = plugins.get(0);
//			tab.add(plugin.getWidget(null), new FitData());
//		}else{
//			SearchGridPanel panel = new SearchGridPanel();
//			panel.getSearchGrid().setMonitorOnChangeBA(false);
//			panel.getSearchGrid().setMonitorToCustomizeColumns(false);
//			panel.getSearchGrid().setMonitorOnFieldsReceived(false);
//			panel.getSearchGrid().setMonitorOnRequestsRecieved(false);
//			tab.add(panel, new FitData());
//			
//			panel.getSearchGrid().createColumnModel();
//			panel.getSearchGrid().createStore();
//		}
		
//		this.add(tab);
//		this.setSelection(tab);
	}
	
	/**
	 * Adds a Tab to view the request specified by requestId
	 * 
	 * @param requestId
	 */
	private void addViewTab(final int requestId){	
		JaguarConstants.dbService.getDataByRequestId(ClientUtils.getSysPrefix(), requestId,  
				new AsyncCallback<TbitsTreeRequestData>(){
					public void onFailure(Throwable caught) {
						TbitsInfo.error(caught.getMessage(), caught);
						Log.error("Error while loading data for tBits Id : " + requestId, caught);
					}
					 
					public void onSuccess(TbitsTreeRequestData result) {
						if(result == null){
							TbitsInfo.error("No " + Captions.getRecordDisplayName() + " fetched for tBits Id : " + requestId);
							TbitsURLManager.getInstance().removeToken(new HistoryToken(GlobalConstants.TOKEN_VIEW, requestId + "", true));
							return;
						}
						TbitsTreeRequestData data = result;
						data.setRequestId(requestId);
						addViewTab(data);
					}});
	}
	
	/**
	 * Adds a Tab to view a request
	 * 
	 * @param Source. The Component from which the event has been fired
	 * @param data. TbitsTreeRequestData containing request details
	 */
	private void addViewTab(TbitsTreeRequestData model){
		ArrayList<HistoryEnabledTab> tabs = this.historyEnabledTabs.get(GlobalConstants.TOKEN_VIEW);
		
		int requestId = model.getRequestId();
		if(tabs != null){
			for(HistoryEnabledTab tab : tabs){
				if(tab.getValue().equals(requestId + "")){
					this.setSelection(tab);
					tab.refresh();
					return;
				}
			}
		}
		
		if(requestId != 0){
			String subject = model.getAsString(SUBJECT);
			String shortSubject = "";
			if(subject != null && subject.length() > 50)
				shortSubject = subject.substring(0,50) + "...";
			else
				shortSubject = subject;
			
			HistoryEnabledTab tab = new ViewRequestTab(requestId, 
					requestId + " [" + ClientUtils.getSysPrefix() + "] - " + shortSubject, model, historyEnabledTabs);
			this.add(tab);
			this.setSelection(tab);
		}
	}

	/**
	 * Adds a Tab to view a request of a any BA
	 * 
	 * @param sysPrefix
	 * @param requestId
	 */
	private void addViewTab(String sysPrefix, final int requestId){
		if(sysPrefix == ClientUtils.getSysPrefix()){
			addViewTab(requestId);
			return;
		}
			
		JaguarConstants.dbService.getRequestData(sysPrefix, requestId, new AsyncCallback<RequestData>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error(caught.getMessage(), caught);
				Log.error("Error while loading data for tBits Id : " + requestId, caught);
			}

			public void onSuccess(RequestData result) {
				if(result != null)
					addViewTab(result);
			}});
	}
	
	/**
	 * dds a Tab to view a request of a any BA through {@link RequestData}
	 * 
	 * @param data
	 */
	private void addViewTab(RequestData data){
		if(data.getSysPrefix() == ClientUtils.getSysPrefix()){
			addViewTab(data.getModel());
			return;
		}
		String subject = data.getModel().getAsString(SUBJECT);
		String shortSubject = "";
		if(subject != null && subject.length() > 50)
			shortSubject = subject.substring(0,50) + "...";
		else
			shortSubject = subject;
		
		TabItem tab = new TabItem(data.getSysPrefix() + "#" + data.getRequestId() + " : " + shortSubject);
		tab.setClosable(true);
		tab.setLayout(new FitLayout());
	
		// Create the view.
		DefaultUIContext mainContext = new DefaultUIContext();
		mainContext.setValue(IRequestFormData.CONTEXT_REQUEST_DATA, data);
		String sysPrefix = data.getSysPrefix() ;
		if( sysPrefix != null )
			mainContext.setValue(RequestFormFactory.SYS_PREFIX, sysPrefix);
		
		IViewRequestForm form = RequestFormFactory.getInstance().getViewRequestForm(mainContext);
		
		tab.add(form.getWidget(), new FitData());
		this.add(tab);
		this.setSelection(tab);
		
	}
	
	/**
	 * Adds a Tab to update the request specified by requestId.
	 * 
	 * @param requestId
	 */
	private void addUpdateTab(final int requestId){
		JaguarConstants.dbService.getDataByRequestId(ClientUtils.getSysPrefix(), requestId, 
				new AsyncCallback<TbitsTreeRequestData>(){
					 
					public void onFailure(Throwable caught) {
						TbitsInfo.error(caught.getMessage(), caught);
						Log.error("Error while loading data for tBits Id : " + requestId, caught);
					}

					 
					public void onSuccess(TbitsTreeRequestData result) {
						if(result == null){
							TbitsInfo.error("No " +Captions.getRecordDisplayName() + " fetched for tBits Id : " + requestId);
							return;
						}
						TbitsTreeRequestData data = result;
						data.setRequestId(requestId);
						addUpdateTab(data);
					}});
	}
	
	/**
	 * Adds a Tab to update a request
	 * 
	 * @param Source. The Component from which the event has been fired
	 * @param data. TbitsTreeRequestData containing request details
	 */
	private void addUpdateTab(TbitsTreeRequestData model){
		String subject = model.getAsString(SUBJECT);
		String shortSubject = "";
		if(subject != null && subject.length() > 50)
			shortSubject = subject.substring(0,50) + "...";
		else
			shortSubject = subject;
		
		int requestId = model.getRequestId();
		ArrayList<HistoryEnabledTab> tabs = this.historyEnabledTabs.get(GlobalConstants.TOKEN_UPDATE);
		if(tabs != null){
			for(HistoryEnabledTab tab : tabs){
				if(tab.getValue().equals(requestId + "")){
					this.setSelection(tab);
					return;
				}
			}
		}
		
		if(requestId != 0){
			HistoryEnabledTab tab = new HistoryEnabledTab(ClientUtils.getSysPrefix() + "#" + requestId + " : " + shortSubject, GlobalConstants.TOKEN_UPDATE, requestId + "", historyEnabledTabs);
			tab.setLayout(new FitLayout());
			
			IUpdateRequestForm urf = this.getUpdateRequestForm(tab, model);//RequestFormFactory.getInstance().getUpdateRequestForm(context);
			
			tab.add(urf.getWidget(), new FitData());
			this.add(tab);
			this.setSelection(tab);
		}
	}
	
	/**
	 * Adds a Tab to update a request of any BA.
	 * 
	 * @param sysPrefix
	 * @param requestId
	 */
	private void addUpdateTab(String sysPrefix, final int requestId){
		JaguarConstants.dbService.getRequestData(sysPrefix, requestId, new AsyncCallback<RequestData>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error(caught.getMessage(), caught);
				Log.error("Error while loading data for tBits Id : " + requestId, caught);
			}

			public void onSuccess(RequestData result) {
				if(result != null)
					addUpdateTab(result);
			}});
	}
	
	/**
	 * Adds a Tab to update a request of any BA through {@link RequestData}
	 * 
	 * @param data
	 */
	public void addUpdateTab(RequestData data){
		String subject = data.getModel().getAsString(SUBJECT);
		String shortSubject = "";
		if(subject != null && subject.length() > 50)
			shortSubject = subject.substring(0,50) + "...";
		else
			shortSubject = subject;
		
		final TabItem tab = new TabItem(data.getSysPrefix() + "#" + data.getRequestId() + " : " + shortSubject);
		tab.setClosable(true);
		tab.setLayout(new FitLayout());
		
		IUpdateRequestForm form = this.getUpdateRequestForm(tab, data);//RequestFormFactory.getInstance().getUpdateRequestForm(context);
		
		tab.add(form.getWidget(), new FitData());
		this.add(tab);
		this.setSelection(tab);
	}
	
	/**
	 * Adds a Tab to update a request of any BA through {@link RequestData}
	 * 
	 * @param data
	 */
	public void addUpdateTab(UIContext uicontext, RequestData data){
		String subject = data.getModel().getAsString(SUBJECT);
		String shortSubject = "";
		if(subject != null && subject.length() > 50)
			shortSubject = subject.substring(0,50) + "...";
		else
			shortSubject = subject;
		
		final TabItem tab = new TabItem(data.getSysPrefix() + "#" + data.getRequestId() + " : " + shortSubject);
		tab.setClosable(true);
		tab.setLayout(new FitLayout());
		
		IUpdateRequestForm form = this.getUpdateRequestForm(uicontext, tab, data); //this.getUpdateRequestForm(tab, data);//RequestFormFactory.getInstance().getUpdateRequestForm(context);
		
		tab.add(form.getWidget(), new FitData());
		this.add(tab);
		this.setSelection(tab);
	}
	
	public void addNewRequestFormTab(UIContext uicontext,RequestData data)
	{
		String sysPrefix = data.getSysPrefix() ;
		if( null == sysPrefix )
		{
			TbitsInfo.error("Illegal value of BusinessArea");
			return ;
		}
		
		String title = Captions.getRecordDisplayName(sysPrefix);
		final TabItem tab = new TabItem("New " + title);
		tab.setClosable(true);
		tab.setScrollMode(Scroll.AUTO);
		tab.setLayout(new FitLayout());
		
		IAddRequestForm form = this.getAddRequestForm(uicontext,tab, data);
		
		tab.add(form.getWidget(), new FitData());
		this.add(tab);
		this.setSelection(tab);
	}
	
	public void addNewRequestFormTab(RequestData data)
	{
		String sysPrefix = data.getSysPrefix() ;
		if( null == sysPrefix )
		{
			TbitsInfo.error("Illegal value of BusinessArea");
			return ;
		}
		
		String title = Captions.getRecordDisplayName(sysPrefix);
		final TabItem tab = new TabItem("New " + title);
		tab.setClosable(true);
		tab.setScrollMode(Scroll.AUTO);
		tab.setLayout(new FitLayout());
		
		IAddRequestForm form = this.getAddRequestForm(tab, data);
		
		tab.add(form.getWidget(), new FitData());
		this.add(tab);
		this.setSelection(tab);
	}
	
	/**
	 * Adds a Tab to add a subrequest to a request
	 * 
	 * @param Source. The Component from which the event has been fired
	 * @param data. TbitsTreeRequestData containing request details
	 */
	protected void addSubRequestTab(int parentRequestId){
		TabItem tab = new TabItem("New " + Captions.getRecordDisplayName());
		tab.setClosable(true);
		tab.setLayout(new FitLayout());
		
		IAddRequestForm form = this.getAddRequestForm(tab, parentRequestId);//RequestFormFactory.getInstance().getAddRequestForm(context);
		
		tab.add(form.getWidget(), new FitData());
		this.add(tab);
		this.setSelection(tab);
	}
	
	/**
	 * Adds a Tab to add a new request.
	 * 
	 * @param text
	 */
	protected void addNewRequestFormTab(String text){
		TabItem tab = new TabItem(text);
		tab.setClosable(true);
		tab.setScrollMode(Scroll.AUTO);
		tab.setLayout(new FitLayout());
		
		DefaultUIContext context = new DefaultUIContext();
		context.setValue(IRequestForm.CONTEXT_PARENT_TAB, tab);
		
		IAddRequestForm form = this.getAddRequestForm(tab, 0);//RequestFormFactory.getInstance().getAddRequestForm(context);
		tab.add(form.getWidget(), new FitData());
		
		this.add(tab);
		this.setSelection(tab);
	}
	
	
	
	/**
	 * Adds Bulk Update Tab with specified data and fields.
	 * 
	 * @param data
	 */
	protected void addBulkUpdateTab(String sysPrefix, List<TbitsTreeRequestData> data){
		TabItem tab = new TabItem("Bulk Add/Update");
		tab.setClosable(true);
		tab.setLayout(new FitLayout());
		
		final BulkUpdatePanel panel = new BulkUpdatePanel(sysPrefix, data, BulkUpdatePanel.DEFAULT_CONFIG);
		
		tab.addListener(Events.BeforeClose, new Listener<TabPanelEvent>(){
			
			public void handleEvent(TabPanelEvent be) {
				if (!panel.isBusyUploading()) {
					panel.hideWindows();
				} else
					be.setCancelled(true);
			}});
		
		tab.add(panel, new FitData());
		this.add(tab);
		this.setSelection(tab);
	}
	
	protected void addDraftsTab(){
		TabItem tab = new TabItem("Drafts");
		tab.setClosable(true);
		tab.setLayout(new FitLayout());
		
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		
		ColumnConfig column = new ColumnConfig();
		column.setId(UserDraftClient.REQUEST_ID);
		column.setHeader("tBits Id");
		column.setWidth(50);
		configs.add(column);
		
		column = new ColumnConfig();
		column.setId(UserDraftClient.DATE);
		column.setHeader("Date");
		column.setWidth(100);
		configs.add(column);
		
		column = new ColumnConfig();
		column.setId(SUBJECT);
		column.setHeader("Subject");
		column.setWidth(200);
		column.setRenderer(new GridCellRenderer<UserDraftClient>(){
			public String render(UserDraftClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<UserDraftClient> store, Grid<UserDraftClient> grid) {
				return model.getModel().getAsString(SUBJECT);
			}});
		configs.add(column);
		
		column = new ColumnConfig();
		column.setHeader("Delete");
		column.setWidth(100);
		column.setRenderer(new GridCellRenderer<UserDraftClient>(){
			public Object render(final UserDraftClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					final ListStore<UserDraftClient> store, Grid<UserDraftClient> grid) {
				final int draftId = model.getDraftId();
				Button btn = new Button("Delete", new SelectionListener<ButtonEvent>(){
					
					public void componentSelected(ButtonEvent ce) {
						JaguarConstants.dbService.deleteUserDraft(ClientUtils.getSysPrefix(), draftId, new AsyncCallback<Boolean>(){
							public void onFailure(Throwable caught) {
								TbitsInfo.error(caught.getMessage(), caught);
								Log.error("Couldn't delete draft Id : " + draftId, caught);
							}

							public void onSuccess(Boolean result) {
								if(!result){
									Log.error("Couldn't delete draft Id : " + draftId);
								}else{
									Log.info("Deleted draft Id : " + draftId);
									JaguarConstants.drafts.remove(model);
								}
							}});
					}});
				
				return btn;
			}});
		configs.add(column);
		
		Grid<UserDraftClient> draftGrid = new Grid<UserDraftClient>(JaguarConstants.drafts, new ColumnModel(configs));
		draftGrid.setAutoExpandColumn(SUBJECT);
		tab.add(draftGrid, new FitData());
		
		this.add(tab);
		this.setSelection(tab);
		
		observable.subscribe(OnDeleteDraft.class, new ITbitsEventHandle<OnDeleteDraft>(){
			public void handleEvent(OnDeleteDraft event) {
				UserDraftClient draft = JaguarConstants.drafts.findModel(UserDraftClient.DRAFT_ID, event.getDraftId());
				if(draft != null)
					JaguarConstants.drafts.remove(draft);
			}});
		
		draftGrid.addListener(Events.OnClick, new Listener<GridEvent<UserDraftClient>>(){
			public void handleEvent(GridEvent<UserDraftClient> be) {
				if(be.getColIndex() == 1 || be.getColIndex() == -1  || be.getRowIndex() == -1)
					return;
				UserDraftClient draft = be.getModel();
				String subject = draft.getModel().getAsString(SUBJECT);
				String shortSubject = "";
				if(subject != null && subject.length() > 50)
					shortSubject = subject.substring(0,50) + "...";
				else
					shortSubject = subject;
				
				TabItem tab = new TabItem(shortSubject);
				tab.setLayout(new FitLayout());
				tab.setClosable(true);
				
				ContentPanel container = new ContentPanel();
				container.setHeaderVisible(false);
				container.setScrollMode(Scroll.AUTO);
				
				DefaultUIContext context = new DefaultUIContext();
				context.setValue(IRequestForm.CONTEXT_PARENT_TAB, tab);
				context.setValue(IRequestFormData.CONTEXT_DRAFT, draft);
				IEditRequestForm form = null ;
				if(draft.getRequestId() != 0)
				{
//					context.setValue(AbstractEditRequestForm.CONTEXT_ADD_MODE, false);
					form = RequestFormFactory.getInstance().getUpdateRequestForm(context);
				}
				else
				{
//					context.setValue(AbstractEditRequestForm.CONTEXT_ADD_MODE, true);
					form = RequestFormFactory.getInstance().getAddRequestForm(context);
				}

//				AbstractEditRequestForm form = new AbstractEditRequestForm(context);
				
				container.add(form.getWidget());
				tab.add(container);
				TbitsMainTabPanel.this.add(tab);
				TbitsMainTabPanel.this.setSelection(tab);
			}
		});
	}
	
	/**
	 * Initialises.
	 */
	protected void initialize(){
		Log.info("Initializing Main Tab Panel");
		
		this.removeAll();
		this.historyEnabledTabs.clear();
		
		this.addSearchTab();
//		if(GWT.isScript()){
			this.addAdvSearchTab();
			this.addMyRequestsTab();
			this.addMyReportsTab();
			this.addDashboardTab();
			if(GlobalConstants.isTagsSupported)
				this.addTagsTab();
//		}
		ListStore<HistoryToken> store = TbitsURLManager.getInstance().stringToStore();
		this.handleHistoryTokens(store);
	}
	
	public boolean add(TabItem item)
	{
		tabsForCtxMnu.add(item);
		return super.add(item);
	}
	public boolean add(HistoryEnabledTab item) {
		if(hasTab(item))
			return false;
		if(item.getKey() != null && historyEnabledTabs.containsKey(item.getKey())){
			ArrayList<HistoryEnabledTab> tabArr = historyEnabledTabs.get(item.getKey());
			tabArr.add(item);
		}else if(item.getKey() != null && !historyEnabledTabs.containsKey(item.getKey())){
			ArrayList<HistoryEnabledTab> tabArr = new ArrayList<HistoryEnabledTab>();
			tabArr.add(item);
			historyEnabledTabs.put(item.getKey(), tabArr);
		}
		return add( (TabItem) item);
	}
	
	private boolean hasTab(HistoryEnabledTab item){
		if(item.getKey() != null && historyEnabledTabs.containsKey(item.getKey())){
			ArrayList<HistoryEnabledTab> tabArr = historyEnabledTabs.get(item.getKey());
			for(HistoryEnabledTab tab : tabArr){
				if(tab.getValue().equals(item.getValue()))
					return true;
			}
		}
		return false;
	}
	
	private boolean hasTab(String key, String value){
		if(key != null && historyEnabledTabs.containsKey(key)){
			ArrayList<HistoryEnabledTab> tabArr = historyEnabledTabs.get(key);
			for(HistoryEnabledTab tab : tabArr){
				if(tab.getValue().equals(value))
					return true;
			}
		}
		return false;
	}
	
	protected void addEventHandles(){
		observable.subscribe(OnChangeBA.class, new ITbitsEventHandle<OnChangeBA>(){
			public void handleEvent(OnChangeBA event) {
				initialize();
//				historyTokenTask.delay(50);
			}});
		
		observable.subscribe(ToViewRequest.class, new ITbitsEventHandle<ToViewRequest>(){
			public void handleEvent(ToViewRequest event) {
				if(event.getData() != null)
					addViewTab(event.getData());
				else if(event.getRequestId() != 0){
					addViewTab(event.getRequestId());
				}
			}});
		
		observable.subscribe(ToAddNewRequest.class, new ITbitsEventHandle<ToAddNewRequest>(){
			public void handleEvent(ToAddNewRequest event) {
				String text = event.getText();
				if(text != null && !text.trim().equals("")){
					addNewRequestFormTab(text);
				}
			}});
		
		observable.subscribe(ToUpdateRequest.class, new ITbitsEventHandle<ToUpdateRequest>(){
			public void handleEvent(ToUpdateRequest event) {
				if(event.getSourceGrid() != null && event.getData() != null)
					addUpdateTab(event.getData());
				else if(event.getRequestId() != 0){
					addUpdateTab(event.getRequestId());
				}
			}});
		
		observable.subscribe(ToAddSubRequest.class, new ITbitsEventHandle<ToAddSubRequest>(){
			 
			public void handleEvent(ToAddSubRequest event) {
				addSubRequestTab(event.getRequestId());
			}});
		
		observable.subscribe(ToBulkUpdate.class, new ITbitsEventHandle<ToBulkUpdate>(){
			public void handleEvent(ToBulkUpdate event) {
				addBulkUpdateTab(event.getSysPrefix(), event.getData());
			}});
		
		observable.subscribe(OnHistoryTokensChanged.class, new ITbitsEventHandle<OnHistoryTokensChanged>(){
			public void handleEvent(OnHistoryTokensChanged event) {
				TbitsMainTabPanel.this.handleHistoryTokens(event);
			}});
		
		observable.subscribe(ToViewRequestOtherBA.class, new ITbitsEventHandle<ToViewRequestOtherBA>(){
			public void handleEvent(ToViewRequestOtherBA event) {
				String sysPrefix = event.getSysPrefix();
				int requestId = event.getRequestId();
				addViewTab(sysPrefix, requestId);
			}});
		
		observable.subscribe(ToUpdateRequestOtherBA.class, new ITbitsEventHandle<ToUpdateRequestOtherBA>(){
			public void handleEvent(ToUpdateRequestOtherBA event) {
				String sysPrefix = event.getSysPrefix();
				int requestId = event.getRequestId();
				addUpdateTab(sysPrefix, requestId);
			}});
		
		observable.subscribe(ToViewDrafts.class, new ITbitsEventHandle<ToViewDrafts>(){
			public void handleEvent(ToViewDrafts event) {
				addDraftsTab();
			}});
		
		observable.subscribe(ToPinSearchResults.class, new ITbitsEventHandle<ToPinSearchResults>(){
			public void handleEvent(ToPinSearchResults event) {
				addPinnedSearchTab();
			}});
		
		observable.subscribe(OnConsolidateRequests.class, new ITbitsEventHandle<OnConsolidateRequests>(){
			public void handleEvent(OnConsolidateRequests event) {
				addConsolidationTab(event.getSysPrefix(), event.getModels());
			}});
		
		observable.subscribe(KeyboardShortcutEvent.class, new ITbitsEventHandle<KeyboardShortcutEvent>(){
			
			public void handleEvent(KeyboardShortcutEvent event) {
				// W to close current tab
				if(event.getKeyCode() == TbitsNativePreviewHandler.baseASCII + 23){
					TabItem item = TbitsMainTabPanel.this.getSelectedItem();
					if(item.isClosable())
						item.close();
				}
			}});
	}
	
	private void handleHistoryTokens(ListStore<HistoryToken> store){
		List<HistoryToken> viewTokens = store.findModels(HistoryToken.KEY, GlobalConstants.TOKEN_VIEW);
		if(viewTokens != null){
			for(HistoryToken token : viewTokens){
				int requestId = Integer.parseInt(token.getValue());
				TbitsEventRegister.getInstance().fireEvent(new ToViewRequest(requestId));
			}
		}
		
		List<HistoryToken> updateTokens = store.findModels(HistoryToken.KEY, GlobalConstants.TOKEN_UPDATE);
		if(updateTokens != null){
			for(HistoryToken token : updateTokens){
				int requestId = Integer.parseInt(token.getValue());
				TbitsEventRegister.getInstance().fireEvent(new ToUpdateRequest(requestId));
			}
		}
	}
	
	private IAddRequestForm getAddRequestForm(TabItem tab, int parentRequestId){
		DefaultUIContext context = new DefaultUIContext();
		context.setValue(IRequestForm.CONTEXT_PARENT_TAB, tab);
		
		TbitsTreeRequestData model = new TbitsTreeRequestData();
		model.set(PARENT_REQUEST_ID, new POJOInt(parentRequestId));
		context.setValue(IRequestFormData.CONTEXT_MODEL, model);
		
		IAddRequestForm form = RequestFormFactory.getInstance().getAddRequestForm(context);
		
		return form;
	}
	
	private IUpdateRequestForm getUpdateRequestForm(TabItem tab, TbitsTreeRequestData model){
		DefaultUIContext context = new DefaultUIContext();
		context.setValue(IRequestForm.CONTEXT_PARENT_TAB, tab);
		context.setValue(IRequestFormData.CONTEXT_MODEL, model);
		
		Integer sysId = model.get(IFixedFields.BUSINESS_AREA);
		if( null != sysId )
		{
			BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
			for( BusinessAreaClient ba : cache.getValues() )
			{
				if( null != ba && ba.getSystemId() == sysId )
				{					
					context.setValue(RequestFormFactory.SYS_PREFIX, ba.getSystemPrefix());
					break ;
				}
			}
		}
		
		IUpdateRequestForm form = RequestFormFactory.getInstance().getUpdateRequestForm(context);
		
		return form;
	}
	
	private IUpdateRequestForm getUpdateRequestForm(UIContext context, TabItem tab, RequestData requestData){
		if( null == context )
			context = new DefaultUIContext();
		context.setValue(IRequestForm.CONTEXT_PARENT_TAB, tab);
		context.setValue(IRequestFormData.CONTEXT_REQUEST_DATA, requestData);
		String sysPrefix = requestData.getSysPrefix() ;
		
		if( null != sysPrefix )
			context.setValue(RequestFormFactory.SYS_PREFIX, sysPrefix);
		
		IUpdateRequestForm form = RequestFormFactory.getInstance().getUpdateRequestForm(context);
		
		return form;
	}
	
	private IUpdateRequestForm getUpdateRequestForm(TabItem tab, RequestData requestData){
		DefaultUIContext context = new DefaultUIContext();
		context.setValue(IRequestForm.CONTEXT_PARENT_TAB, tab);
		context.setValue(IRequestFormData.CONTEXT_REQUEST_DATA, requestData);
		String sysPrefix = requestData.getSysPrefix() ;
		
		if( null != sysPrefix )
			context.setValue(RequestFormFactory.SYS_PREFIX, sysPrefix);
		
		IUpdateRequestForm form = RequestFormFactory.getInstance().getUpdateRequestForm(context);
		
		return form;
	}
	
	private IAddRequestForm getAddRequestForm(UIContext context, TabItem tab, RequestData requestData){
		if( null == context )
			context = new DefaultUIContext();
		context.setValue(IRequestForm.CONTEXT_PARENT_TAB, tab);
		context.setValue(IRequestFormData.CONTEXT_REQUEST_DATA, requestData);

		String sysPrefix = requestData.getSysPrefix() ;
		if( null == sysPrefix )
			return null ;
		else
			context.setValue(RequestFormFactory.SYS_PREFIX, sysPrefix);
		
		IAddRequestForm form = RequestFormFactory.getInstance().getAddRequestForm(context);
		
		return form;
	}
	
	private IAddRequestForm getAddRequestForm(TabItem tab, RequestData requestData){
		DefaultUIContext context = new DefaultUIContext();
		context.setValue(IRequestForm.CONTEXT_PARENT_TAB, tab);
		context.setValue(IRequestFormData.CONTEXT_REQUEST_DATA, requestData);

		String sysPrefix = requestData.getSysPrefix() ;
		if( null == sysPrefix )
			return null ;
		else
			context.setValue(RequestFormFactory.SYS_PREFIX, sysPrefix);
		
		IAddRequestForm form = RequestFormFactory.getInstance().getAddRequestForm(context);
		
		return form;
	}
	
	private void handleHistoryTokens(OnHistoryTokensChanged event){
		ListStore<HistoryToken> store = event.getStore();
		handleHistoryTokens(store);
	}
}
