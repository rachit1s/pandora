package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.CheckBoxListView;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.cache.UserCacheAdmin;
import com.tbitsGlobal.admin.client.events.OnBaUsersAdd;
import com.tbitsGlobal.admin.client.events.OnBaUsersDelete;
import com.tbitsGlobal.admin.client.events.OnRolesChange;
import com.tbitsGlobal.admin.client.events.OnUsersReceived;
import com.tbitsGlobal.admin.client.state.AppState;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleUserClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;
import commons.com.tbitsGlobal.utils.client.widgets.UserPicker;

/**
 * The Class for BAUser
 * Displays the Roles of the Selected Users
 * Users can also be Added & Deleted from BA
 * 
 */
public class BAUserView extends APTabItem {

	private EditorGrid<TbitsModelData> grid;
	private CheckBoxListView<UserClient> usernameList;

	
	public BAUserView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setLayout(new FitLayout());
		
		usernameList = new CheckBoxListView<UserClient>();
		usernameList.setStore(new ListStore<UserClient>());
		usernameList.setDisplayProperty(UserClient.USER_LOGIN);
		usernameList.setBorders(false);
		usernameList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		observable.subscribe(OnChangeBA.class, new ITbitsEventHandle<OnChangeBA>() {
			public void handleEvent(OnChangeBA event) {
				usernameList.getStore().removeAll();
				grid.getStore().removeAll();
				getuser();
				loadGrid();	
			}
		});

		observable.subscribe(OnRolesChange.class, new ITbitsEventHandle<OnRolesChange>() {
			public void handleEvent(OnRolesChange event) {
				grid.getStore().removeAll();
				loadGrid();
			}
		});

		observable.subscribe(OnUsersReceived.class, new ITbitsEventHandle<OnUsersReceived>(){
			public void handleEvent(OnUsersReceived event) {
				getuser();
			}
		});
	}

	public void onRender(Element parent, int pos) {
		super.onRender(parent, pos);

		
		/*
		 * Single selection model for selecting users. 
		 */
		usernameList.addListener(Events.OnClick, new Listener<ListViewEvent<UserClient>>() {
			public void handleEvent(ListViewEvent<UserClient> be) {
				if(null != be.getModel()){
					for(int i = 0 ; i < usernameList.getStore().getCount() ; i++){
						if(usernameList.getStore().getAt(i).getUserId() != be.getModel().getUserId()){
							usernameList.setChecked(usernameList.getStore().getAt(i), false);
						}
					}
					getRolesOfUser(be.getModel());
					}else return;
				}
			});
		
		ContentPanel cpanel= new ContentPanel();
		cpanel.setHeaderVisible(false);	
		cpanel.setLayout(new BorderLayout());
		cpanel.setBodyBorder(false);

		ContentPanel userpanel = new ContentPanel();
		userpanel.setLayout(new FitLayout());
		userpanel.setHeading("Users");

		//filter for the BA Users
		StoreFilterField<UserClient> filter = new StoreFilterField<UserClient>(){
			@Override
			protected boolean doSelect(Store<UserClient> store,	UserClient parent,UserClient record, String property,String filter) {
				String login = record.getUserLogin();
				if (login.toLowerCase().contains(filter.toLowerCase())) {  
					return true;  
				}  
				return false;
			}	
		};
		filter.bind(usernameList.getStore());

		ToolBar toolBar = new ToolBar();  
		toolBar.add(new LabelToolItem("Search :"));  
		toolBar.add(filter);  
		userpanel.setTopComponent(toolBar);

		userpanel.add(usernameList, new FitData());
		
		ToolBarButton adduser = new ToolBarButton("Add an user", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				final Window win = new Window();
				win.setHeading("Add BA User");
				win.setModal(true);
				win.setClosable(true);
				
				FormLayout formLayout = new FormLayout();
				formLayout.setLabelWidth(60);
				win.setLayout(formLayout);
				
				List<UserClient> allUsers = new ArrayList<UserClient>();
				UserCacheAdmin cache = CacheRepository.getInstance().getCache(UserCacheAdmin.class);
				if(cache.isInitialized()){
					allUsers.addAll(cache.getValues());
				}
				
				final UserPicker allUserCombobox = new UserPicker(allUsers);
				allUserCombobox.setFieldLabel("UserName");
				win.add(allUserCombobox);
				
				final Button submit = new Button("Submit", new SelectionListener<ButtonEvent>(){
					public void componentSelected(ButtonEvent se) {	
						String [] userList= allUserCombobox.getStringValue().split(",");
						
						for(int j = 0 ; j < userList.length ; j++){
							for(int i = 0 ; i < usernameList.getStore().getCount(); i++){
								if(usernameList.getStore().getAt(i).getUserLogin().trim().equals(userList[j])){
									TbitsInfo.info("User " + userList[j]  + " already present in the User List...");
									win.hide();
									return;
								}
							}
						}
						
						APConstants.apService.addBAUsers(ClientUtils.getCurrentBA().getSystemId(), allUserCombobox.getStringValue() , new AsyncCallback<List<UserClient>>(){
							public void onFailure(Throwable caught) {
								TbitsInfo.error("Users not Added... Please see log for details", caught);
								Log.error("Users not Added... Please see log for details", caught);
							}
							public void onSuccess(List<UserClient> result) {
								if(result != null) {
									usernameList.getStore().add(result);
									TbitsInfo.info("Users have been added to Business Area ...");
									TbitsEventRegister.getInstance().fireEvent(new OnBaUsersAdd(result));
								}
								win.hide();
							}
						});
					}
				});
				win.addButton(submit);
				
				win.show();
			}
		});
		
		ToolBarButton deleteuser = new ToolBarButton("Delete users", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				if(com.google.gwt.user.client.Window.confirm("Are you sure you want to Delete selected users")){
					List<UserClient> selectedUsers = usernameList.getChecked();
					deleteUser(selectedUsers);
				}
			}
		});
		ToolBar buttonpanel = new ToolBar();
		buttonpanel.add(adduser);
		buttonpanel.add(deleteuser);
		userpanel.setBottomComponent(buttonpanel);

		BorderLayoutData bdata = new BorderLayoutData(LayoutRegion.WEST, 350);
		bdata.setSplit(true);
		cpanel.add(userpanel, bdata);

		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig cname = new ColumnConfig(RoleClient.ROLE_NAME, "Name", 250);
		configs.add(cname);

		CheckColumnConfig checkColumn = new CheckColumnConfig("member",	"Member", 60);
		configs.add(checkColumn);

		ColumnModel cm = new ColumnModel(configs);

		grid = new EditorGrid<TbitsModelData>(new ListStore<TbitsModelData>(), cm);
		grid.addPlugin(checkColumn);
		
		ContentPanel cp = new ContentPanel();
		cp.setHeading("Roles");
		cp.setLayout(new FitLayout());
		cp.add(grid, new FitData());

		ToolBar buttonBar = new ToolBar();
		buttonBar.setAlignment(HorizontalAlignment.LEFT);
		ToolBarButton save = new ToolBarButton("Save changes", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				
				
				if(usernameList.getChecked().size() == 0)
				{
				
					TbitsInfo.error("Please Select a user before making changes in the roles ...");	
					return;
				}
				
				final UserClient selectedUser = usernameList.getChecked().get(0);
				System.out.println("Selected user is ---> " + selectedUser.getUserLogin());
				if(selectedUser != null){
					ArrayList<RoleUserClient> rucList = new ArrayList<RoleUserClient>();
					for(TbitsModelData tb : grid.getStore().getModels()){
						if((Boolean) tb.get("member")){
							RoleUserClient rc = new RoleUserClient();
							rc.setRoleId((Integer)(tb.get(RoleClient.ROLE_ID)));
							rc.setSystemId(ClientUtils.getCurrentBA().getSystemId());
							rc.setUserId(selectedUser.getUserId());
							rc.setIsActive(selectedUser.getIsActive());
							rucList.add(rc);
						}
					}
					TbitsInfo.info("Updating User Roles... Please Wait...");
					APConstants.apService.updateRoleUser(ClientUtils.getCurrentBA().getSystemId(), selectedUser.getUserId(), rucList, new AsyncCallback<Boolean>(){
						public void onFailure(Throwable caught) {
							TbitsInfo.error("Role User Not Updated ...Please refresh ...", caught);
						}
						public void onSuccess(Boolean result) {
							if(result){
								TbitsInfo.info("Roles of User have been updated...");
								loadGrid();
								getRolesOfUser(selectedUser);
							}else{
								TbitsInfo.error("Could not update roles... ");
							}
						}
					});
				}
			}	
		});
		
		buttonBar.add(save);

		cp.setBottomComponent(buttonBar);

		bdata = new BorderLayoutData(LayoutRegion.CENTER);
		bdata.setSplit(true);
		cpanel.add(cp, bdata);

		this.add(cpanel, new FitData());	
		
		getuser();
		loadGrid();
	}

	/**
	 *  The function to make Grid containing all Roles of particular BA
	 */
	private void loadGrid(){
		if (!AppState.checkAppStateIsTill(AppState.BAChanged)) {
			return;
		}
		APConstants.apService.getRoleBySysPrefix(ClientUtils.getSysPrefix() , new AsyncCallback<List<RoleClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Roles not Loaded ...Please Refresh ...", caught);	
			}
			public void onSuccess(List<RoleClient> result) {
				if(result != null){
					grid.getStore().removeAll();
					for(RoleClient temp : result){
						
						if(temp.getPropertyNames().contains(RoleClient.FIELD_ID) && temp.getFieldId() == 0 && !temp.getRoleName().equals("BAUsers")){
						
							TbitsModelData check = new TbitsModelData();
							check.set(RoleClient.ROLE_NAME, temp.getRoleName());
							check.set("member",false);
							check.set(RoleClient.ROLE_ID, temp.getRoleId());
							grid.getStore().add(check);
						}							
					}
				}
			}					
		});
	}

	/**
	 *  The Function to get the Users of BA and fill up in userlist
	 */
	private void getuser(){
		if (!AppState.checkAppStateIsTill(AppState.BAChanged)) {
			return;
		}
		usernameList.getStore().removeAll();
		APConstants.apService.getBAUsers(ClientUtils.getCurrentBA().getSystemPrefix(), new AsyncCallback<ArrayList<UserClient>>(){
			public void onFailure(Throwable caught) {	
				TbitsInfo.error("BAUsers not Loaded ...Please Refresh ...", caught);
			}
			public void onSuccess(ArrayList<UserClient> result) {				
				if(result != null){
					usernameList.getStore().removeAll();
					usernameList.getStore().add(result);
				}
			}
		});
	}

	/**
	 *  The Function gets all The Roles of the Selected User
	 *  
	 *  @param user
	 */
	private void getRolesOfUser(UserClient user){
		if(user == null) return;
		for(TbitsModelData tc : grid.getStore().getModels())
			tc.set("member", false);	
		grid.getView().refresh(false);
		
		APConstants.apService.getRolesbySysIDandUserID(ClientUtils.getCurrentBA().getSystemId(), user.getUserId(), new AsyncCallback<ArrayList<RoleClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Roles of Selected BAUser not Loaded ...Please Refresh ...", caught);
			}
			public void onSuccess(ArrayList<RoleClient> result) {
				if(result != null){
					for(RoleClient rc : result){
						TbitsModelData model = grid.getStore().findModel(RoleClient.ROLE_ID, rc.getRoleId());
						if(model != null){
							model.set("member", true);
						}
					}
					grid.getView().refresh(false);
				}
			}					
		});		
	}

	/**
	 *  The Function to delete User from BA
	 * @param del
	 */
	private void deleteUser(final List<UserClient> users){
		if(users == null) 
			return;
		APConstants.apService.deleteBAUsers(ClientUtils.getCurrentBA().getSystemId(), users, new AsyncCallback<Boolean>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("BAUsers not Deleted... Please see logs for details..", caught);
				Log.error("BAUsers not Deleted... Please see logs for details..", caught);
			}
			public void onSuccess(Boolean result) {
				if(result){
					for(UserClient user : users){
						usernameList.getStore().remove(user);
					}
					TbitsInfo.info("Users have been deleted from Business Area ...");
					TbitsEventRegister.getInstance().fireEvent(new OnBaUsersDelete(users));
				}
			}
		});	
	}
}
