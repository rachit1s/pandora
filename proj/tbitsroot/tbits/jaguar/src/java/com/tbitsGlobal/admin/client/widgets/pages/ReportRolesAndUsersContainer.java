package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.CheckCascade;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.cache.UserCacheAdmin;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * Container to hold the Report roles and users 
 *
 */
public class ReportRolesAndUsersContainer extends LayoutContainer{

	private ListView<UserClient> incUsersListView;
	private ListView<UserClient> excUsersListView;
	
	private ListStore<UserClient> allUsersStore;
	
	private TreePanel<ModelData> treePanel;
	
	/**
	 * Default constructor. Build the container
	 */
	public ReportRolesAndUsersContainer() {
		this.setSize(800, 355);
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));
		
		ContentPanel baRoles = getRolesContainer();
		this.add(baRoles, new RowData(-1, 1, new Margins(2)));
		
		allUsersStore = new ListStore<UserClient>();
		Collection<UserClient> userList = CacheRepository.getInstance().getCache(UserCacheAdmin.class).getValues();
		allUsersStore.add(Util.createList(userList));

		ContentPanel includedUsers = getIncludedUsersContainer(allUsersStore);
		this.add(includedUsers, new RowData(-1, 1, new Margins(2)));

		ContentPanel excludedUsers = getExcludedUsersContainer(allUsersStore);
		this.add(excludedUsers, new RowData(-1, 1, new Margins(2)));
	}
	
	/**
	 * Create the Panel to hold the BA Roles in a tree structure.
	 * @return - Panel containing the BA Roles
	 */
	private ContentPanel getRolesContainer(){
		TreeStore<ModelData> rolesTreeStore = new TreeStore<ModelData>();
		this.getRoleTree(rolesTreeStore);
		
		ContentPanel baRoles = new ContentPanel();
		baRoles.setWidth(250);
		baRoles.setHeading("Permission to BA Roles");
		baRoles.setScrollMode(Scroll.AUTO);
		
		treePanel = new TreePanel<ModelData>(rolesTreeStore);
		treePanel.setDisplayProperty(RoleClient.ROLE_NAME);
		treePanel.setCheckable(true);
		treePanel.setAutoLoad(true);
		treePanel.setTrackMouseOver(false);
		treePanel.setCheckStyle(CheckCascade.PARENTS);

		baRoles.add(treePanel);
		
		return baRoles;
	}
	
	/**
	 * Build the container for holding values of included users
	 * @param allUsersStore - User store to fill the selection combo
	 * @return - container which holds the value of included users
	 */
	private ContentPanel getIncludedUsersContainer(ListStore<UserClient> allUsersStore){
		ContentPanel includedUsers = new ContentPanel();
		includedUsers.setHeading("Include Users");
		includedUsers.setScrollMode(Scroll.AUTO);
		includedUsers.setWidth(280);

		ToolBar addToolBar = new ToolBar();

		final ComboBox<UserClient> addUserCombo = new ComboBox<UserClient>();
		addUserCombo.setStore(allUsersStore);
		addUserCombo.setDisplayField(UserClient.DISPLAY_NAME);
		addUserCombo.setWidth(150);
		addToolBar.add(addUserCombo);

		ToolBarButton addButton = new ToolBarButton("Add");
		addToolBar.add(addButton);

		includedUsers.setTopComponent(addToolBar);

		incUsersListView = new ListView<UserClient>();
		ListStore<UserClient> addedUsers = new ListStore<UserClient>();
		incUsersListView.setStore(addedUsers);
		incUsersListView.setDisplayProperty(UserClient.DISPLAY_NAME);
		incUsersListView.setBorders(false);

		includedUsers.add(incUsersListView);

		addButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				UserClient selectedUser = addUserCombo.getValue();
				if(selectedUser != null && incUsersListView.getStore().findModel(selectedUser) == null){
					incUsersListView.getStore().add(selectedUser);
				}
			}
		});

		ToolBar removeUserBar = new ToolBar();

		ToolBarButton removeUserButton = new ToolBarButton("Remove Selected Users");
		removeUserButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				for (UserClient uc : incUsersListView.getSelectionModel().getSelectedItems())
					incUsersListView.getStore().remove(uc);
			}

		});
		removeUserBar.add(removeUserButton);

		includedUsers.setBottomComponent(removeUserBar);
		
		return includedUsers;
	}
	
	/**
	 * Build the container for holding values of excluded users
	 * @param allUsersStore - User store to fill the selection combo
	 * @return - container which holds the value of excluded users
	 */
	private ContentPanel getExcludedUsersContainer(ListStore<UserClient> allUsersStore){
		ContentPanel excludedUsers = new ContentPanel();
		excludedUsers.setHeading("Exclude Users");
		excludedUsers.setScrollMode(Scroll.AUTO);
		excludedUsers.setWidth(250);

		ToolBar tb = new ToolBar();

		final ComboBox<UserClient> addUC = new ComboBox<UserClient>();
		addUC.setStore(allUsersStore);
		addUC.setDisplayField(UserClient.DISPLAY_NAME);

		tb.add(addUC);

		ToolBarButton addBtn = new ToolBarButton("Add");
		tb.add(addBtn);

		excludedUsers.setTopComponent(tb);

		excUsersListView = new ListView<UserClient>();
		final ListStore<UserClient> usersStore = new ListStore<UserClient>();
		excUsersListView.setStore(usersStore);
		excUsersListView.setDisplayProperty(UserClient.DISPLAY_NAME);
		excUsersListView.setBorders(false);

		excludedUsers.add(excUsersListView);

		addBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				UserClient selectedUser = addUC.getValue();
				if(selectedUser != null && incUsersListView.getStore().findModel(selectedUser) == null){
					excUsersListView.getStore().add(selectedUser);
				}
			}

		});

		ToolBar removeUBar = new ToolBar();

		ToolBarButton removeUserBtn = new ToolBarButton("Remove Selected Users");
		removeUserBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				for (UserClient uc : excUsersListView.getSelectionModel().getSelectedItems())
					usersStore.remove(uc);
			}

		});
		removeUBar.add(removeUserBtn);

		excludedUsers.setBottomComponent(removeUBar);
		
		return excludedUsers;
	}
	
	/**
	 * Populate the BA Roles store after fetching values from database.
	 * @param rolesTreeStore - Store to be populated
	 */
	private void getRoleTree(final TreeStore<ModelData> rolesTreeStore) {
		TbitsInfo.info("Retrieving Business Area Roles ....");
		APConstants.apService.getRoleTree(new AsyncCallback<HashMap<String, ArrayList<RoleClient>>>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Failed to load baRole tree for report permissions :", new TbitsExceptionClient(caught));
			}

			public void onSuccess(final HashMap<String, ArrayList<RoleClient>> result) {
				if (result == null) {
					TbitsInfo.warn("List can not be recieved");
					return;
				}
				for (String sysPrefix : result.keySet()) {
					ModelData baName = new TbitsModelData();
					baName.set(RoleClient.ROLE_NAME, ClientUtils.getBAbySysPrefix(sysPrefix).getDisplayName());
					baName.set(BusinessAreaClient.SYSTEM_PREFIX, sysPrefix);
					rolesTreeStore.add(baName, false);
					List<ModelData> temp = new ArrayList<ModelData>();
					temp.addAll(result.get(sysPrefix));
					rolesTreeStore.add(baName, temp, false);
				}
			}
		});
	}
	
	public void setPermittedRoles(HashMap<Integer, ArrayList<Integer>> permittedRoles) {
		treePanel.collapseAll();
		for (ModelData ba : treePanel.getStore().getRootItems()) {
			treePanel.setChecked(ba, false);
			for (ModelData roleClient : treePanel.getStore().getChildren(ba)) {
				treePanel.setChecked(roleClient, false);
				if (permittedRoles != null) {
					for (Integer sysId : permittedRoles.keySet()) {
						if (sysId == (ClientUtils.getBAbySysPrefix(BusinessAreaClient.SYSTEM_PREFIX)).getSystemId()) {
							for (int roleId : permittedRoles.get(sysId)) {
								if ((Integer) roleClient.get(RoleClient.ROLE_ID) == roleId) {
									treePanel.setChecked(roleClient, true);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void fillReportSpecificUsers(HashMap<Integer, Boolean> userIdMap) {
		if (!isRendered())
			return;
		if (userIdMap == null)
			return;
		
		List<UserClient> incUsers = new ArrayList<UserClient>();
		List<UserClient> excUsers = new ArrayList<UserClient>();
		
		for (int userId : userIdMap.keySet()) {
			UserClient user = allUsersStore.findModel(UserClient.USER_ID, userId);
			if(user != null){
				if(userIdMap.get(userId))
					incUsers.add(user);
				else
					excUsers.add(user);
			}
		}
		
		incUsersListView.getStore().removeAll();
		incUsersListView.getStore().add(incUsers);
		
		excUsersListView.getStore().removeAll();
		excUsersListView.getStore().add(excUsers);
	}
	
	public HashMap<String, ArrayList<RoleClient>> getRolesMap(){
		HashMap<String, ArrayList<RoleClient>> rolesMap = new HashMap<String, ArrayList<RoleClient>>();
		for (ModelData model : treePanel.getCheckedSelection()) {
			if (model instanceof RoleClient) {
				ArrayList<RoleClient> roleClients = rolesMap.get(treePanel.getStore().getParent(model).get(BusinessAreaClient.SYSTEM_PREFIX));
				if (roleClients == null) {
					roleClients = new ArrayList<RoleClient>();
					roleClients.add((RoleClient) model);
					rolesMap.put((String) treePanel.getStore().getParent(model).get(BusinessAreaClient.SYSTEM_PREFIX),roleClients);
				} else {
					roleClients.add((RoleClient) model);
				}
			}
		}
		
		return rolesMap;
	}
	
	public List<String> getIncludedUserLogins(){
		ArrayList<String> includedUserLogins = new ArrayList<String>();
		for (UserClient user : incUsersListView.getStore().getModels())
			includedUserLogins.add(user.getUserLogin());
		
		return includedUserLogins;
	}
	
	public List<String> getExcludedUserLogins(){
		ArrayList<String> excludedUserLogins = new ArrayList<String>();
		for (UserClient user : excUsersListView.getStore().getModels())
			excludedUserLogins.add(user.getUserLogin());
		
		return excludedUserLogins;
	}
	
	
}
