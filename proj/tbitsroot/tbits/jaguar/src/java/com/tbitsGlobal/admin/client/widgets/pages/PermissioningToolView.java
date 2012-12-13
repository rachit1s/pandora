package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.permTool.PTCache;
import com.tbitsGlobal.admin.client.permTool.PTConstants;
import com.tbitsGlobal.admin.client.permTool.PermissionListing;
import com.tbitsGlobal.admin.client.permTool.UserFormView;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * This class gives the view for the permissioning tool.
 * 
 * @author Karan Gupta
 *
 */
public class PermissioningToolView extends APTabItem {

	// Singleton instance for the tool.
	private static PermissioningToolView instance;
	
	// Content panels and the like to display and take input
	private ContentPanel inputListing;
	private UserFormView formView;
	private PermissionListing permissionListing;
	private ListView<UserClient> list;
	private TextField<String> requestId;
	private ContentPanel mainPanel;
	private ToolBar toolbar;
	
	// The selected user client is saved here.
	private UserClient selectedUserClient;
	
	/**
	 * Private constructor
	 * 
	 * @param header
	 * @param pageCaption
	 */
	public PermissioningToolView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setLayout(new FitLayout());
		this.setClosable(true);
		initialise();
	}
	
	/**
	 * Initialise the various components of the view.
	 */
	private void initialise() {
		
		ContentPanel cp = new ContentPanel(new FitLayout());
		cp.setHeaderVisible(false);
		cp.setBorders(false);
		
		mainPanel = new ContentPanel();
		toolbar = new ToolBar();
		toolbar.setHeight(30);
		toolbar.setAlignment(HorizontalAlignment.CENTER);
		
		// inputListing contains the userlisting, the user filter and the text field for the requestId
		inputListing = new ContentPanel();
		inputListing.setBorders(false);
		inputListing.setBodyBorder(false);
		inputListing.setWidth(200);
		inputListing.setAutoHeight(true);
		inputListing.setHeading("All Active BA Users");
		list = new ListView<UserClient>(new ListStore<UserClient>()){

			protected UserClient prepareData(UserClient user) {
				String s = user.getUserLogin();  
				user.set("shortName", Format.ellipse(s, 15));
				user.set("name", s);
				return user;  
			}  
		};
		list.setDisplayProperty("name");
		list.setHeight(190);
		
		// Filter for the user list
		StoreFilterField<UserClient> filter = new StoreFilterField<UserClient>(){
			protected boolean doSelect(Store<UserClient> store,	UserClient parent, UserClient record, String property,String filter) {
				String login = record.getUserLogin();
				if (login.toLowerCase().contains(filter.toLowerCase())) {  
					return true;  
				}  
				return false;
			}	
		};
		filter.bind(list.getStore());

		ToolBar filterBar = new ToolBar();  
		filterBar.add(new LabelToolItem("Search :"));  
		filterBar.add(filter); 
		
		inputListing.setTopComponent(filterBar);
		inputListing.add(list);
		
		// Text field to display the selected user
		final TextField<String> selectedUser = new TextField<String>();
		selectedUser.setValue("");
		selectedUser.disable();
		selectedUser.setWidth(195);
		
		inputListing.addText("<br>Selected User :");
		inputListing.add(selectedUser);
		
		list.getSelectionModel().addListener(Events.SelectionChange, new Listener<SelectionChangedEvent<UserClient>>() {  
			
			public void handleEvent(SelectionChangedEvent<UserClient> be) {  
				if(be.getSelectedItem() == null)
					return;
				selectedUserClient = be.getSelectedItem();
				selectedUser.setValue(be.getSelectedItem().getUserLogin());
			}
		});
		
		// Text field to input the requestId
		requestId = new TextField<String>();
		requestId.setWidth(195);
		
		inputListing.addText("<br>Enter Request Id :");
		inputListing.add(requestId);
		inputListing.addText("<br>");
		populateListing();
		
		// tabs for the user form view and the permission listings
		ContentPanel tabs = new ContentPanel(new FitLayout());
		tabs.setHeaderVisible(false);
		tabs.setBorders(false);
		tabs.setWidth(950);
		tabs.setHeight(400);
		TabPanel tp = new TabPanel();
		tp.setLayoutData(new FitLayout());
		tp.setWidth(950);
		tp.setHeight(400);
		formView = new UserFormView("User Form Views");
		permissionListing = new PermissionListing("Permission Listing");
		tp.add(formView);
		tp.add(permissionListing);
		tabs.add(tp);
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(inputListing);
		hp.add(tabs);
		
		mainPanel.add(hp);
		
		cp.add(mainPanel);
		cp.setBottomComponent(toolbar);
		this.add(cp);
		this.layout();
		
		// Sink the select events on the tabs and change the toolbar at the bottom accordingly
		formView.sinkEvents(Events.Select.getEventCode());
		formView.addListener(Events.Select, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				showUserFormOptions();
			}
		});
		
		permissionListing.sinkEvents(Events.Select.getEventCode());
		permissionListing.addListener(Events.Select, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				showPermissionListingOptions();
			}
		});
		
		// Show the user form options in toolbar initially
		showUserFormOptions();
		
		// Initialise the cache
		PTCache.initialise();
	}

	/**
	 * Populate the user list with the list of all the BA users.
	 */
	private void populateListing() {
		
		APConstants.apService.getBAUsers(ClientUtils.getCurrentBA().getSystemPrefix(), new AsyncCallback<ArrayList<UserClient>>() {
			
			public void onSuccess(ArrayList<UserClient> result) {
				list.getStore().removeAll();
				if(result != null)
					list.getStore().add(result);
			}
			
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to fetch BA users!", caught);
			}
		});
		
	}
	
	/**
	 * Show the user form options in the toolbar. The various views' options are shown as buttons.
	 */
	private void showUserFormOptions(){
		
		toolbar.removeAll();
		
		// Add a button to view the "add request" layout
		ToolBarButton btn = new ToolBarButton("Add Request Layout", new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(ButtonEvent ce) {
				if(selectedUserClient == null)
					TbitsInfo.error("No user selected!");
				else
					formView.showAddForm(selectedUserClient.getUserId());
			}

		});
		btn.setWidth(195);
		toolbar.add(btn);
		
		// Add a button to view the "view request" layout
		btn = new ToolBarButton("View Request Layout", new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(ButtonEvent ce) {
				if(selectedUserClient == null)
					TbitsInfo.error("No user selected!");
				else
					formView.showForm(requestId, selectedUserClient.getUserId(), PTConstants.VIEW);
			}
		});
		btn.setWidth(195);
		toolbar.add(btn);
		
		// Add a button to view the "update request" layout
		btn = new ToolBarButton("Update Request Layout", new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(ButtonEvent ce) {
				if(selectedUserClient == null)
					TbitsInfo.error("No user selected!");
				else
					formView.showForm(requestId, selectedUserClient.getUserId(), PTConstants.UPDATE);
			}
		});
		btn.setWidth(195);
		toolbar.add(btn);
		
		btn = new ToolBarButton("Email Layout", new SelectionListener<ButtonEvent>() {
			
			public void componentSelected(ButtonEvent ce) {
				if(selectedUserClient == null)
					TbitsInfo.error("No user selected!");
				else
					formView.showForm(requestId, selectedUserClient.getUserId(), PTConstants.EMAIL);
			}
		});
		btn.setWidth(195);
		toolbar.add(btn);
		
		toolbar.layout();
	}
	
	/**
	 * Show the options for the permission listing. The fetch permissions option is shown.
	 */
	private void showPermissionListingOptions() {
		
		toolbar.removeAll();
		
		ToolBarButton btn = new ToolBarButton("Fetch Permissions", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if(selectedUserClient == null){
					TbitsInfo.error("No user selected!");
					return;
				}
				try{
					int reqId = 0;
					if(requestId.getValue() != null && !requestId.getValue().equals(""))
						reqId = Integer.parseInt(requestId.getValue());
					permissionListing.fetchPermissions(ClientUtils.getCurrentBA().getSystemId(), selectedUserClient.getUserId(), reqId);
				}
				catch(NumberFormatException e){
					TbitsInfo.error("Incorrect request id format. Enter a valid Integer as an id.");
					return;
				}
			}
		});
		toolbar.add(btn);

		toolbar.layout();
	}
	
}
