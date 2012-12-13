package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.CheckBoxListView;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.events.OnRolesChange;
import com.tbitsGlobal.admin.client.modelData.RolePermissionModel;
import com.tbitsGlobal.admin.client.state.AppState;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import com.tbitsGlobal.admin.client.widgets.pages.bulk.PermissionBulkGridPanel;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

public class FieldPermissionsView  extends APTabItem {
	
	private ComboBox<RoleClient> roleCombo;
	private ToolBarButton deleteBtn;
	private PermissionBulkGridPanel gridContainer;
	
	public FieldPermissionsView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setLayout(new FitLayout());
		
		observable.subscribe(OnChangeBA.class, new ITbitsEventHandle<OnChangeBA>() {
			public void handleEvent(OnChangeBA event) {
				fillRoles();
			}
		});
		
		roleCombo = new ComboBox<RoleClient>();
		roleCombo.setStore(new ListStore<RoleClient>());
		roleCombo.setDisplayField((String)RoleClient.ROLE_NAME);
		roleCombo.addSelectionChangedListener(new SelectionChangedListener<RoleClient>(){
			@Override
			public void selectionChanged(SelectionChangedEvent<RoleClient> se) {
				switchRole(se.getSelectedItem());
			}});
		
		gridContainer = new PermissionBulkGridPanel();
	}
	
	@Override
	public void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		ContentPanel container = new ContentPanel(new FitLayout());
		container.setHeaderVisible(false);
		
		ToolBar toolbar = new ToolBar();
		toolbar.add(roleCombo);
		toolbar.add(new ToolBarButton("Add Role", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				addRole();
			}}));
		deleteBtn = new ToolBarButton("Delete Role", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				RoleClient role = roleCombo.getValue();
				if(role != null)
					deleteRole(role);
			}});
		toolbar.add(deleteBtn);
		
		toolbar.add(new ToolBarButton("Apply these permissions to other Roles", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				final CheckBoxListView<RoleClient> rolesView = new CheckBoxListView<RoleClient>();
				rolesView.setDisplayProperty(RoleClient.ROLE_NAME);
				
				ListStore<RoleClient> rolesStore = new ListStore<RoleClient>();
				rolesStore.add(roleCombo.getStore().getModels());
				rolesStore.remove(roleCombo.getValue());
				rolesView.setStore(rolesStore);
				
				Window window = new Window();
				window.setHeading("Copy Permissions");
				window.add(rolesView);
				window.addButton(new Button("Apply", new SelectionListener<ButtonEvent>(){
					@Override
					public void componentSelected(ButtonEvent ce) {
						List<RolePermissionModel> permissions = gridContainer.getSingleGridContainer().getModels();
						
						List<RoleClient> selectedModels = rolesView.getChecked();
						List<Integer> roleIds = new ArrayList<Integer>();
						for(RoleClient model : selectedModels){
							roleIds.add(model.getRoleId());
						}
						
						APConstants.apService.updateRolePermissions(ClientUtils.getCurrentBA().getSystemId(), roleIds, permissions, 
								new AsyncCallback<Boolean>(){
									public void onFailure(Throwable caught) {
										TbitsInfo.error("error while updating role permissions", caught);
										Log.error("error while updating role permissions", caught);
									}

									public void onSuccess(Boolean result) {
										if(result){
											TbitsInfo.info("Successfully updated permissions");
											
										}else TbitsInfo.error("error while updating role permissions");
									}});
					}}));
				window.show();
			}}));
		
		container.setTopComponent(toolbar);
		
		container.add(gridContainer, new FitData());
		
		this.add(container, new FitData());
		
		fillRoles();
	}
	
	private void switchRole(RoleClient role){
		if(role == null)
			return;
		
		if(role.getCanBeDeleted() == 0)
			deleteBtn.disable();
		else
			deleteBtn.enable();
		
		fillPermissions(role);
	}
	
	private void addRole(){
		final Window addRoleWindow = new Window();
		addRoleWindow.setHeading("Add Role "); 
		addRoleWindow.setModal(true);
		addRoleWindow.setLayout(new FitLayout());
		addRoleWindow.setHeight(140);
		addRoleWindow.setClosable(true);
		
		FormPanel formPanel = new FormPanel();
		FormLayout formLayout = new FormLayout();
		formLayout.setLabelWidth(80);
		formPanel.setLayout(formLayout);  
		formPanel.setHeaderVisible(false);
		formPanel.setBodyBorder(false);

		final TextField<String> rolename = new TextField<String>();
		rolename.setFieldLabel("Role Name");
		final TextField<String> roledesc = new TextField<String>();
		roledesc.setFieldLabel("Description");
		formPanel.add(rolename, new FormData("100%"));
		formPanel.add(roledesc, new FormData("100%"));
		
		addRoleWindow.add(formPanel, new FitData());

		Button submit = new Button("Submit", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent se) {
				if(rolename.getValue() == null){
					TbitsInfo.warn("Role Name cannot be empty.......try again !!! ");
					addRoleWindow.hide();
				}
				else{
					RoleClient roleClient = new RoleClient();
					roleClient.setRoleName(rolename.getValue());
					roleClient.setDescription(roledesc.getValue());
					roleClient.setSystemId(ClientUtils.getCurrentBA().getSystemId());
					roleClient.setFieldId(0);
					roleClient.setCanBeDeleted(1);
					APConstants.apService.addRole(roleClient, new AsyncCallback<RoleClient>(){
						public void onFailure(Throwable caught) {
							TbitsInfo.error("Role could not be added.......try again !!! ", caught);
						}
						public void onSuccess(RoleClient result) {
							if(result != null){
								TbitsInfo.info("Role added succesfully");
								roleCombo.getStore().add(result);
								roleCombo.getStore().sort(RoleClient.ROLE_NAME, SortDir.ASC);
								roleCombo.setValue(result);
								TbitsEventRegister.getInstance().fireEvent(new OnRolesChange(result,true));
							}
						}
					});
				}
				addRoleWindow.hide();
				addRoleWindow.setModal(false);
			}
		});
		
		addRoleWindow.addButton(submit);
		
		addRoleWindow.show();
	}
	
	private void deleteRole(final RoleClient role){
		if (role == null)
			return;
		Listener<MessageBoxEvent> l = new Listener<MessageBoxEvent>(){
			public void handleEvent(MessageBoxEvent be) {
				Button b = be.getButtonClicked();
				if(b.getText().endsWith("Yes")){
					APConstants.apService.deleteRole(role,new AsyncCallback<Boolean>(){
						public void onFailure(Throwable caught) {
							TbitsInfo.error("Error in deleting Role. Please Try Again...", caught);
							Log.error("Error in deleting Role. Please Try Again...", caught);
						}
						public void onSuccess(Boolean result) {
							if(result){
								roleCombo.getStore().remove(role);
								TbitsEventRegister.getInstance().fireEvent(new OnRolesChange(role, false));
								TbitsInfo.info("Role Has Been Deleted Successfully");
								if(roleCombo.getStore().getCount() > 0)
									roleCombo.setValue(roleCombo.getStore().getAt(0));
							}
						}
					});
				}
			}
		};
		MessageBox.confirm("Confirm", "Are you sure you want to Delete ?",l);
	}

	private void fillRoles(){
		TbitsInfo.info("Retrieving roles... Please wait..");
		roleCombo.getStore().removeAll();
		if (!AppState.checkAppStateIsTill(AppState.BAChanged)) {
			TbitsInfo.warn("Business Area not loaded");
			return;
		}
		APConstants.apService.getRoleBySysPrefix(ClientUtils.getSysPrefix(),new AsyncCallback<List<RoleClient>>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("unable to retrieve roles",caught);
				Log.error("unable to retrieve roles",caught);
			}
			public void onSuccess(List<RoleClient> result) {
				roleCombo.getStore().add(result);
				roleCombo.getStore().sort(RoleClient.ROLE_NAME, SortDir.ASC);
				if (!(roleCombo.getStore().getCount() == 0)){
					roleCombo.setValue(roleCombo.getStore().getAt(0));
				}
			}
		});
	}
	
	private void fillPermissions(final RoleClient role){
		if(role == null)
			return;
		
		gridContainer.setRole(role);
		gridContainer.refresh(0);
	}
}
