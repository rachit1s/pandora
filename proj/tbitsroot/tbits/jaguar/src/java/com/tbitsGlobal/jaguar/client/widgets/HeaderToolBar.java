package com.tbitsGlobal.jaguar.client.widgets;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.JaguarUtils;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsNativePreviewHandler;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.KeyboardShortcutEvent;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ToViewRequestOtherBA;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;
import commons.com.tbitsGlobal.utils.client.widgets.BAMenuButton;
import commons.com.tbitsGlobal.utils.client.widgets.TbitsHyperLink;
import commons.com.tbitsGlobal.utils.client.widgets.TbitsHyperLinkEvent;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * Tool Bar present at the top of the UI.
 * It contains the BA Menu and the current user info.
 * 
 * @author sourabh
 *
 */
public class HeaderToolBar extends ToolBar{
	
	protected ToolBarButton baButton;
	
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	public HeaderToolBar() {
		super();
		
		this.setBorders(false);
		this.setSpacing(10);
		
		baButton = new BAMenuButton(){
			@Override
			public void onSelect(BusinessAreaClient baClient) {
				super.onSelect(baClient);
				
				TbitsURLManager.getInstance().addToken(new HistoryToken(GlobalConstants.TOKEN_BA, baClient.getSystemPrefix(), true));
			}
		};
		this.add(baButton);
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		// Loads a new ba.
		ITbitsEventHandle<OnChangeBA> baChangeHandle = new ITbitsEventHandle<OnChangeBA>(){
			public void handleEvent(OnChangeBA event) {
				setButtonTextandLoadBA(event.getBa());
			}};
		
		observable.subscribe(OnChangeBA.class, baChangeHandle);
		
		observable.subscribe(KeyboardShortcutEvent.class, new ITbitsEventHandle<KeyboardShortcutEvent>(){
			public void handleEvent(KeyboardShortcutEvent event) {
				// M to show ba menu
				if(event.getKeyCode() == TbitsNativePreviewHandler.baseASCII + 13)
					baButton.showMenu();
			
				// G to show ba menu
				if(event.getKeyCode() == TbitsNativePreviewHandler.baseASCII + 7)
					requestId.focus();
			}});
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		
		observable.detach();
	}
	
	@Override
	protected void beforeRender() {
		super.beforeRender();
		
		// Refresh Button. Reloads current BA.
		TbitsHyperLink refreshLink = new TbitsHyperLink("Refresh", new SelectionListener<TbitsHyperLinkEvent>(){
			@Override
			public void componentSelected(TbitsHyperLinkEvent ce) {
				TbitsURLManager.getInstance().init();
			}});
		this.add(refreshLink);
		
		this.add(new FillToolItem());
		
		this.addJumpToRequest();
		
		// Link to old version.
		final LayoutContainer oldVersion = new LayoutContainer();
		JaguarConstants.dbService.showOldVersion(new AsyncCallback<Boolean>(){
			public void onFailure(Throwable caught) {
				Log.error("Unable to know whether to show 'Old Version' link or not. Please upgrade your database.");
			}

			public void onSuccess(Boolean result) {
				if(result){
					oldVersion.addText("<a href='" + JaguarUtils.getAppBaseURL() + "/my-requests' target='_blank'><b>Old Version</b></a>");
					oldVersion.layout();
				}
			}});
		this.add(oldVersion);
		
		// Link to user setting.
		final LayoutContainer settings = new LayoutContainer();
		this.add(settings);
		
		observable.subscribe(OnChangeBA.class, new ITbitsEventHandle<OnChangeBA>(){
			public void handleEvent(OnChangeBA event) {
				settings.removeAll();
				settings.addText("<a href='" + JaguarUtils.getAppBaseURL() + "options/" + ClientUtils.getSysPrefix() + "' target='_blank'><b>Settings</b></a>");
				settings.layout();
			}});
		
		if(ClientUtils.getCurrentUser().getIsSuperUser()){
			// Link to administration.
			final LayoutContainer admin = new LayoutContainer();
			this.add(admin);
				
			observable.subscribe(OnChangeBA.class, new ITbitsEventHandle<OnChangeBA>(){
				public void handleEvent(OnChangeBA event) {
					admin.removeAll();
					admin.addText("<a href='" + JaguarUtils.getAppBaseURL() + "adm/#ba=" +
							ClientUtils.getSysPrefix() + "' target='_blank'><b>Administration</b></a>");
					admin.layout();
				}});
		}
		
		// Link to logout.
		final LayoutContainer logout = new LayoutContainer();
		JaguarConstants.dbService.showLogout(new AsyncCallback<Boolean>(){
			public void onFailure(Throwable caught) {
				Log.error("Unable to know whether to show 'Old Version' link or not. Please upgrade your database.");
			}

			public void onSuccess(Boolean result) {
				if(result){
					logout.addText("<a href='" + JaguarUtils.getAppBaseURL() + "logout'><b>Logout</b></a>");
					logout.layout();
				}
			}});
		this.add(logout);
		
		// User Info Box.
		this.addUserBox();
		
//		ToolButton upButton = new ToolButton(".x-tool-up", new SelectionListener<IconButtonEvent>(){
//			@Override
//			public void componentSelected(IconButtonEvent ce) {
//				
//			}});
//		this.add(upButton);
		
		Timer heartBeat = new Timer(){
			@Override
			public void run() {
				JaguarConstants.dbService.connect(new AsyncCallback<Boolean>(){
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Unable to connect to server... Trying To reconnect...", caught);
						
						if(caught instanceof StatusCodeException){
							StatusCodeException ex = (StatusCodeException) caught;
							Log.error("StatusCodeException --> Status Code : " + ex.getStatusCode());
						}else if(caught instanceof InvocationException){
							TbitsInfo.error("You have been logged out. Reload this page or <a href = '" + JaguarUtils.getAppBaseURL() 
									+ "' target = '_blank'>Click Here</a> to login.", caught);
							Log.error("User has been logged out. ", caught);
						}
//						Log.fatal("Unable to connect to server...", caught);
					}

					public void onSuccess(Boolean result) {
						if(!result)
							TbitsInfo.error("Unable to connect to server... Trying To reconnect...");
					}});
			}};
		heartBeat.scheduleRepeating(5000);
	}

	
	/**
	 * Adds User Info Box on the {@link HeaderToolBar}
	 */
	private void addUserBox(){
		final Status user = new Status();
		user.setBox(true);
		this.add(user);
		
		UserClient currentUser = ClientUtils.getCurrentUser();
		user.setText(currentUser.getDisplayName()); 
		user.setToolTip("Login : " + currentUser.getUserLogin() + "<br />" 
				+ "E-Mail : " + currentUser.getEmail());
	}
	
	private TextField<String> requestId;
	
	private void addJumpToRequest(){
		requestId = new TextField<String>();
		requestId.setWidth(150);
		requestId.setEmptyText("sysPrefix#id 'OR' id1,id2,id3,..");
		requestId.addKeyListener(new KeyListener(){
			@Override
			public void componentKeyPress(ComponentEvent event) {
				super.componentKeyPress(event);
				int keyCode = event.getKeyCode();
				if(keyCode == KeyCodes.KEY_ENTER){
					jumpToRequest();
				}
			}
		});
		this.add(requestId);
		
		ToolBarButton go = new ToolBarButton("GO", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				jumpToRequest();
			}});
		this.add(go);
	}
	
	private void jumpToRequest(){
		String idString = requestId.getValue();
		if(idString == null || idString.trim().equals(""))
			return;
		
		String[] ids = idString.split(",");
		for(String id : ids){
			try{
				int rid = Integer.parseInt(id.trim());
				if(rid > 0){
					TbitsURLManager.getInstance().addToken(new HistoryToken(GlobalConstants.TOKEN_VIEW, rid + "", false));
				}
			}catch(Exception ex){
				String[] arr = id.split("#");
				if(arr.length == 2){
					String sysPrefix = arr[0];
					if(ClientUtils.getBAbySysPrefix(sysPrefix) != null){
						try{
							int rid = Integer.parseInt(arr[1].trim());
							if(rid > 0){
								if(sysPrefix.equals(ClientUtils.getSysPrefix())){
									TbitsURLManager.getInstance().addToken(new HistoryToken(GlobalConstants.TOKEN_VIEW, rid + "", false));
								}else{
									TbitsBaseEvent event = new ToViewRequestOtherBA(sysPrefix, rid);
									TbitsEventRegister.getInstance().fireEvent(event);
								}
								
							}
						}catch(Exception e){
							
						}
					}
				}
			}
		}
	}
	
	protected void setButtonTextandLoadBA(BusinessAreaClient ba){
		if(ba != null){
			baButton.setText(ba.getDisplayText());
			baButton.setToolTip(ba.getDescription());
		}
	}
}
