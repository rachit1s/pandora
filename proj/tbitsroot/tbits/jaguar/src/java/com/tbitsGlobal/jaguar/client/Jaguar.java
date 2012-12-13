package com.tbitsGlobal.jaguar.client;

import java.util.HashMap;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.tbitsGlobal.jaguar.client.bulkupdate.BulkUpdateConstants;
import com.tbitsGlobal.jaguar.client.cache.DisplayGroupCache;
import com.tbitsGlobal.jaguar.client.cache.UserCache;
import com.tbitsGlobal.jaguar.client.events.OnRequestsRecieved;
import com.tbitsGlobal.jaguar.client.events.ToRefreshDisplayGroupCache;
import com.tbitsGlobal.jaguar.client.events.ToRefreshUserCache;
import com.tbitsGlobal.jaguar.client.events.ToSearch;
import com.tbitsGlobal.jaguar.client.state.AppState;
import com.tbitsGlobal.jaguar.client.widgets.HeaderToolBar;
import com.tbitsGlobal.jaguar.client.widgets.MainPanel;
import com.tbitsGlobal.jaguar.client.widgets.TbitsStatus;
import commons.com.tbitsGlobal.utils.client.Captions;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.DQLResults;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsNativePreviewHandler;
import commons.com.tbitsGlobal.utils.client.TbitsUncaughtExceptionHandler;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.DefaultContextHandle;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.KeyboardShortcutEvent;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.Events.OnCurrentUserReceived;
import commons.com.tbitsGlobal.utils.client.Events.OnFieldsReceived;
import commons.com.tbitsGlobal.utils.client.Events.OnHistoryTokensChanged;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ToRefreshFieldCache;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister.IPostFireHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister.IPreFireHandle;
import commons.com.tbitsGlobal.utils.client.cache.ActiveFieldCache;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.DQL;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;

/**
 *  @author sourabh
 *  
 * Entry point classes define <code>onModuleLoad()</code>.
 * This class initializes the whole UI of the application
 */
public class Jaguar extends JaguarConstants implements EntryPoint{
	/**
	 * viewport on which the whole UI is added
	 */
	private static Viewport viewport;
	private ContentPanel mainContainer;
	
	private TbitsObservable observable;
	
	/**
	 * Method called when the "Jaguar" module loads 
	 * 
	 * It initializes whole UI from scratch. After this function the whole UI generates itself step by step.
	 */
	public void onModuleLoad(){
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		this.setContextHandle();
		
		/*
		 * Alert the user for the non compatibility with IE 6
		 */
		if(GXT.isIE6)
			Window.alert(JaguarConstants.MESSAGES.ie6_found());
		
		// Set UncaughtExceptionHandler to handle unpredictable exceptions
		GWT.setUncaughtExceptionHandler(new TbitsUncaughtExceptionHandler());
		
		// Set the NativePreviewHandler
		Event.addNativePreviewHandler(TbitsNativePreviewHandler.getInstance());
		this.addDefaultKeyBoardShortcuts();
		
		// Confirmation on page leave
		if(GWT.isScript()){
			Window.addWindowClosingHandler(new ClosingHandler(){
				public void onWindowClosing(ClosingEvent event) {
					event.setMessage(JaguarConstants.MESSAGES.on_window_leave());
				}});
		}
		
		// Initializing the Remote Services
		((ServiceDefTarget)JaguarConstants.dbService).setServiceEntryPoint(JaguarConstants.PROPS.url());
		((ServiceDefTarget)GlobalConstants.utilService).setServiceEntryPoint(JaguarConstants.PROPS.url());
		((ServiceDefTarget)BulkUpdateConstants.bulkUpdateService).setServiceEntryPoint(JaguarConstants.PROPS.url());
		
		// Initialize plugins in hosted mode.
		if(!GWT.isScript()){
			JaguarConstants.dbService.initPlugins(new AsyncCallback<Boolean>(){
				public void onFailure(Throwable arg0) {
					arg0.printStackTrace();
				}

				public void onSuccess(Boolean arg0) {
					System.out.println(arg0);
					Log.info("Plugins initialized...");
				}});
		}
		
		// Initialize all the Cache's
		CacheRepository.getInstance().registerCache(FieldCache.class, new ActiveFieldCache());
		CacheRepository.getInstance().registerCache(DisplayGroupCache.class, new DisplayGroupCache());
		CacheRepository.getInstance().registerCache(UserCache.class, new UserCache());
		CacheRepository.getInstance().registerCache(BusinessAreaCache.class, new BusinessAreaCache(){
			@Override
			public void onRefresh() {
				AppState.setAppStateAfterCheckState(AppState.BAMapReceived);
				
				super.onRefresh();
			}
		});
		
		// Handle change in history token in URL
		observable.subscribe(OnHistoryTokensChanged.class, 
				new ITbitsEventHandle<OnHistoryTokensChanged>(){
					public void handleEvent(OnHistoryTokensChanged event) {
						handleHistoryTokens(event);
					}});
		
		// Initialize the keys in the URL manager
		JaguarConstants.registerKeys();
		
		// Add a value change handler to monitor url changes
		History.addValueChangeHandler(new ValueChangeHandler<String>(){
			public void onValueChange(ValueChangeEvent<String> event) {
				TbitsURLManager.getInstance().init();
			}});
		
		// Fetch the initial data from server
		this.initData();
		
		//Initialize UI
		observable.subscribe(OnCurrentUserReceived.class, new ITbitsEventHandle<OnCurrentUserReceived>(){
			public void handleEvent(OnCurrentUserReceived event) {
				AppState.setAppState(AppState.CurrentUserReceived);
				
				// adding viewport
				viewport = new Viewport();
				viewport.setLayout(new FitLayout());
				
				// adding mainpanel
				mainContainer = new ContentPanel();
				mainContainer.setHeaderVisible(false);
				mainContainer.setBodyBorder(false);
				mainContainer.setLayout(new FitLayout());
				
				
				// Create components in the mainpanel
				Jaguar.this.createHeader();
				Jaguar.this.createMainPanel();
				Jaguar.this.createFooter();
				viewport.add(mainContainer, new FitData());
				
				RootPanel.get().add(viewport);
			}});
		
		// Get Business Areas and Users
		CacheRepository.getInstance().getCache(BusinessAreaCache.class).refresh();
		TbitsEventRegister.getInstance().fireEvent(new ToRefreshUserCache());
		
		observable.subscribe(ToSearch.class, new ITbitsEventHandle<ToSearch>(){
			public void handleEvent(ToSearch event) {
				String sysPrefix = event.getSysPrefix();
				final DQL dql = event.getDql();
				final int pageNo = event.getPage();
				final int pageSize = event.getPageSize();
				final boolean isBasicSearch = event.isBasicSearch();

				JaguarConstants.dbService.getRequestsForDQL(sysPrefix , dql, pageSize, pageNo, new AsyncCallback<DQLResults>() {
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Error while loading requests... Please refresh!!!", caught);
						Log.error("Error while loading requests...", caught);
					}

					public void onSuccess(DQLResults result) {
						if(result != null)
						{
							OnRequestsRecieved event = new OnRequestsRecieved(dql, isBasicSearch, result, pageNo, pageSize);
							event.setSortOrderColumns(result.getSortColumn());
							event.setSortDirection(result.getSortDirection());
							TbitsEventRegister.getInstance().fireEvent(event);
						}
						else
							TbitsInfo.info("No records retreived...");
					}
				}) ;
			}});
		
		// In case the user enters a previously bookmarked URL
		TbitsURLManager.getInstance().init();
	}
	
	/**
	 * Initializes data retrieval for the system.
	 */
	private void initData(){
		ClientUtils.init();
		
		// Receive all the captions.
		JaguarConstants.dbService.getAllBACaptions(new AsyncCallback<HashMap<Integer,HashMap<String,String>>>(){
			public void onFailure(Throwable arg0) {
				TbitsInfo.error("Error while loading captions... Please refresh!!!", arg0);
				Log.error("Error while loading captions... Please refresh!!!", arg0);
			}

			public void onSuccess(HashMap<Integer, HashMap<String, String>> result) {
				if(result != null){
					Captions.setAllBACaptionsMap(result);
					AppState.setAppStateAfterCheckState(AppState.CaptionReceived);
				}
				Log.info("Captions retrieved");
			}});
		
		JaguarConstants.dbService.getIsTagsSupported(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error loading if tags supported. Using default value...", caught);
				Log.warn("Error loading if tags supported. Using default value...", caught);
			}

			public void onSuccess(Boolean result) {
				
				GlobalConstants.isTagsSupported = result.booleanValue();
			}
		});
		
		JaguarConstants.dbService.getIsTvnSupported(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error loading if tags supported. Using default value...", caught);
				Log.warn("Error loading if tags supported. Using default value...", caught);
			}

			public void onSuccess(Boolean result) {
				
				GlobalConstants.isTvnSupported = result.booleanValue();
			}
		});
	}
	
	/**
	 * Creates the {@link HeaderToolBar} at the Top of the UI
	 */
	private void createHeader(){	
		HeaderToolBar headerToolBar = new HeaderToolBar();
		mainContainer.setTopComponent(headerToolBar);
		
		Log.info("Header Tool Bar initialized...");
	}
	
	/**
	 * Creates the {@link TbitsStatus} at the bottom of the UI
	 */
	private void createFooter(){
		TbitsStatus statusBar = TbitsStatus.getInstance();
		mainContainer.setBottomComponent(statusBar);
		
		Log.info("Footer Tool Bar initialized");
	}
	
	/**
	 * Creates the {@link MainPanel} which will hold all the major components of the UI
	 */
	private void createMainPanel(){
		MainPanel mainPanel = new MainPanel();
		mainContainer.add(mainPanel, new FitData());
	}
	
	/**
	 * parse the URL Token string and issues events accordingly
	 */
	private void handleHistoryTokens(OnHistoryTokensChanged event){
		ListStore<HistoryToken> store = event.getStore();
		HistoryToken baToken = store.findModel(HistoryToken.KEY, GlobalConstants.TOKEN_BA);
		if(baToken != null){
			String sysPrefix = baToken.getValue();
			TbitsEventRegister.getInstance().fireEvent(new OnChangeBA(sysPrefix));
		}
		
		HistoryToken dqlToken = store.findModel(HistoryToken.KEY, GlobalConstants.TOKEN_DQL);
		if(dqlToken != null){
			String dql = dqlToken.getValue();
			DQL dqlObject = new DQL(dql);
			TbitsEventRegister.getInstance().fireEvent(new ToSearch(ClientUtils.getSysPrefix(), dqlObject, 1 , GlobalConstants.SEARCH_PAGESIZE, true));
		}
	}
	
	private void addDefaultKeyBoardShortcuts(){
		observable.subscribe(KeyboardShortcutEvent.class, new ITbitsEventHandle<KeyboardShortcutEvent>(){
			public void handleEvent(KeyboardShortcutEvent event) {
				switch(event.getKeyCode()){
					case TbitsNativePreviewHandler.baseASCII + 18 : // R for local refresh
						TbitsURLManager.getInstance().init();
						break;
				}
			}});
	}
	
	private void setContextHandle(){
		DefaultContextHandle contextHandle = new DefaultContextHandle();
		
		contextHandle.setPreFireHandle(OnChangeBA.class, new IPreFireHandle<OnChangeBA>(){
			public boolean beforeFire(OnChangeBA event) {
				if(AppState.checkAppStateIsBefore(AppState.BAChanged)){
					if(event.getBa() == null && event.getSysPrefix() != null)
						event.setBa(ClientUtils.getBAbySysPrefix(event.getSysPrefix()));
					
					if(event.getBa() == null){
						TbitsInfo.error("The requested Business Area does not exist");
						return false;
					}
					
					BusinessAreaCache baCache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
					if(baCache != null){
						baCache.setCurrentBA(event.getBa());
					}
					
					Document.get().setTitle("tBits Jaguar - [" + ClientUtils.getSysPrefix() + "]");
					JaguarConstants.drafts = null;
					
					AppState.setAppState(AppState.BAChanged);
					return true;
				}else{
					AppState.delayTillAppStateIsBefore(AppState.BAChanged, event);
				}
				return false;
			}});
		
		contextHandle.setPostFireHandle(OnChangeBA.class, new IPostFireHandle<OnChangeBA>(){
			public void afterFire(OnChangeBA event) {
				TbitsEventRegister.getInstance().fireEvent(new ToRefreshFieldCache());
				TbitsEventRegister.getInstance().fireEvent(new ToRefreshDisplayGroupCache());
			}});
		
		contextHandle.setPreFireHandle(OnFieldsReceived.class, new IPreFireHandle<OnFieldsReceived>(){
			public boolean beforeFire(OnFieldsReceived event) {
				if(AppState.checkAppStateIsBefore(AppState.FieldsReceived)){
					AppState.setAppState(AppState.FieldsReceived);
					return true;
				}else
					AppState.delayTillAppStateIsBefore(AppState.FieldsReceived, event);
				return false;
			}});
		
		contextHandle.setPreFireHandle(ToRefreshFieldCache.class, new IPreFireHandle<ToRefreshFieldCache>(){
			public boolean beforeFire(ToRefreshFieldCache event) {
				if((GlobalConstants.appState & AppState.BAChanged.getVal()) != 0){
					return true;
				}else{
					AppState.delayTillAppStateIsBefore(AppState.BAChanged, event);
				}
				return false;
			}});
		
		TbitsEventRegister.getInstance().setContextHandle(contextHandle);
	}
}