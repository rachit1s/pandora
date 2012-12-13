package com.tbitsGlobal.admin.client;

import com.extjs.gxt.ui.client.event.Observable;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.tbitsGlobal.admin.client.cache.UserCacheAdmin;
import com.tbitsGlobal.admin.client.events.OnBAListReceived;
import com.tbitsGlobal.admin.client.events.ToRefreshUsersCache;
import com.tbitsGlobal.admin.client.state.AppState;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.widgets.MainContainer;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsUncaughtExceptionHandler;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.DefaultContextHandle;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.Events.OnCurrentUserReceived;
import commons.com.tbitsGlobal.utils.client.Events.OnHistoryTokensChanged;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister.IPostFireHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister.IPreFireHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;

public class Admin extends AdminConstants implements EntryPoint{

	public static Viewport viewport;
	
	private TbitsObservable observable;
	
	public void onModuleLoad() {
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		this.setContextHandle();
		
		((ServiceDefTarget)GlobalConstants.utilService).setServiceEntryPoint("/adm/admin");
		((ServiceDefTarget)APConstants.apService).setServiceEntryPoint("/adm/admin");
//		((ServiceDefTarget)APConstants.dbService).setServiceEntryPoint("/adm/trn");
		
		// Set UncaughtExceptionHandler to handle unpredictable exceptions
		GWT.setUncaughtExceptionHandler(new TbitsUncaughtExceptionHandler());
		ClientUtils.init();
		// Initialize plugins in hosted mode.
		if(!GWT.isScript()){
			APConstants.apService.initPlugins(new AsyncCallback<Boolean>(){
				public void onFailure(Throwable arg0) {
					arg0.printStackTrace();
				}

				public void onSuccess(Boolean arg0) {
					System.out.println(arg0);
				}});
		}
		
		// Initialize all the Cache's
		CacheRepository.getInstance().registerCache(UserCacheAdmin.class, new UserCacheAdmin());
		CacheRepository.getInstance().registerCache(BusinessAreaCache.class, new AdminBusinessAreaCache(){
			@Override
			public void onRefresh() {
				super.onRefresh();
				
				TbitsEventRegister.getInstance().fireEvent(new OnBAListReceived());
			}
		});
		
		// Handle change in history token in URL
		observable.subscribe(OnHistoryTokensChanged.class, 
				new ITbitsEventHandle<OnHistoryTokensChanged>(){
					public void handleEvent(OnHistoryTokensChanged event) {
						handleHistoryTokens(event);
					}});
		
		TbitsURLManager.getInstance().register(APConstants.TOKEN_BA, true);
		TbitsURLManager.getInstance().register(APConstants.CURRENT_TAB, true);
		
		// Add a value change handler to monitor url changes
		History.addValueChangeHandler(new ValueChangeHandler<String>(){
			public void onValueChange(ValueChangeEvent<String> event) {
				TbitsURLManager.getInstance().init();
			}});
		
		observable.subscribe(OnCurrentUserReceived.class, new ITbitsEventHandle<OnCurrentUserReceived>(){
			public void handleEvent(OnCurrentUserReceived event) {
				if(ClientUtils.getCurrentUser().getIsSuperUser()){
					AppState.setAppState(AppState.UserReceived);
					initializeDisplay();
				}else{
					Window.alert("You are not authorized to view this page");
				}
			}});
		
		
		
		TbitsEventRegister.getInstance().fireEvent(new ToRefreshUsersCache());
		observable.subscribe(OnCurrentUserReceived.class,new ITbitsEventHandle<OnCurrentUserReceived>() {

			@Override
			public void handleEvent(OnCurrentUserReceived event) {
				
				CacheRepository.getInstance().getCache(BusinessAreaCache.class)
				.refresh();	
			}
		});
		
				}
	
	/**
	 * parse the URL Token string and issues events accordingly
	 */
	private void handleHistoryTokens(OnHistoryTokensChanged event){
		ListStore<HistoryToken> store = event.getStore();
		HistoryToken baToken = store.findModel(HistoryToken.KEY, APConstants.TOKEN_BA);
		if(baToken != null){
			String sysPrefix = baToken.getValue();
			TbitsEventRegister.getInstance().fireEvent(new OnChangeBA(sysPrefix));
		}
	}

	private void initializeDisplay() {
		viewport = new Viewport();
		viewport.setLayout(new FitLayout());

		MainContainer mainContainer = MainContainer.getInstance();
		viewport.add(mainContainer, new FitData());

		RootPanel.get().add(viewport);
	}
	
	private void setContextHandle(){
		DefaultContextHandle contextHandle = new DefaultContextHandle();
		
		contextHandle.setPreFireHandle(OnChangeBA.class, new IPreFireHandle<OnChangeBA>(){
			public boolean beforeFire(OnChangeBA event) {
				if (AppState.checkAppStateIsBefore(AppState.BAChanged)) {
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
					
					Document.get().setTitle("tBits Administration - [" + ClientUtils.getSysPrefix() + "]");
					
					return true;
				} else {
					AppState.delayTillAppStateIsBefore(AppState.BAChanged, event);
				}
				return false;
			}});
		contextHandle.setPostFireHandle(OnChangeBA.class, new IPostFireHandle<OnChangeBA>(){
			@Override
			public void afterFire(OnChangeBA event) {
				AppState.setAppState(AppState.BAChanged);
			}});
		TbitsEventRegister.getInstance().setContextHandle(contextHandle);
	}
}
