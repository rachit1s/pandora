package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DualListField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.events.OnBaUsersAdd;
import com.tbitsGlobal.admin.client.events.OnBaUsersDelete;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import com.tbitsGlobal.admin.client.widgets.pages.bulk.RolesBulkGridPanel;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * 
 * @author sourabh
 * 
 * Page for roles.
 */
public class RolesView extends APTabItem{

	private RolesBulkGridPanel bulkGridPanel;
	private DualListField<UserClient> roleUsersField;
	
	private List<UserClient> baUsers;
	
	public RolesView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setLayout(new BorderLayout());
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		bulkGridPanel = new RolesBulkGridPanel();
		this.add(bulkGridPanel, new BorderLayoutData(LayoutRegion.WEST, 450));
		
		bulkGridPanel.getSingleGridContainer().getBulkGrid().addListener(Events.Render, new Listener<ComponentEvent>(){
			public void handleEvent(ComponentEvent be) {
				bulkGridPanel.getSingleGridContainer().getBulkGrid().getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<RoleClient>(){
					@Override
					public void selectionChanged(SelectionChangedEvent<RoleClient> se) {
						roleUsersField.getToField().getStore().removeAll();
						
						roleUsersField.getFromField().getStore().removeAll();
						if(baUsers != null)
							roleUsersField.getFromField().getStore().add(baUsers);
						
						RoleClient role = se.getSelectedItem();
						if(role != null){
							getRoleUsers(role.getSystemId(), role.getRoleId());
						}
					}});
			}});
		
		ContentPanel roleUsersContainer = new ContentPanel(new FitLayout());
		roleUsersContainer.setHeading("BA Users --> Role Users");
		roleUsersContainer.setBodyBorder(false);
		roleUsersField = new DualListField<UserClient>();
		roleUsersField.setBorders(false);
		roleUsersField.getFromField().setStore(new ListStore<UserClient>());
		roleUsersField.getToList().setStore(new ListStore<UserClient>());
		roleUsersField.getFromList().setDisplayField(UserClient.USER_LOGIN);
		roleUsersField.getToField().setDisplayField(UserClient.USER_LOGIN);
		roleUsersContainer.add(roleUsersField, new FitData());
		
		roleUsersContainer.addButton(new Button("Save Role Users", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<UserClient> users = roleUsersField.getToField().getStore().getModels();
				RoleClient role = bulkGridPanel.getSingleGridContainer().getBulkGrid().getSelectionModel().getSelectedItem();
				APConstants.apService.updateRoleUsers(ClientUtils.getSysPrefix(), role, users, new AsyncCallback<Boolean>(){
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Error saving Role Users... Please see log for details", caught);
						Log.error("Error saving Role Users... Please see log for details", caught);
					}

					public void onSuccess(Boolean result) {
						if(result){
							TbitsInfo.info("Role Users saved");
						}else{
							TbitsInfo.info("Unknown error saving Role Users");
						}
					}});
			}}));
		
		this.add(roleUsersContainer, new BorderLayoutData(LayoutRegion.CENTER));
		
		String sysPrefix = ClientUtils.getSysPrefix();
		this.getBAUsers(sysPrefix);
		
		observable.subscribe(OnBaUsersAdd.class, new ITbitsEventHandle<OnBaUsersAdd>() {
			@Override
			public void handleEvent(OnBaUsersAdd event) {
				roleUsersField.getFromField().getStore().add(event.getUsers());
			}
		});
		
		observable.subscribe(OnBaUsersDelete.class, new ITbitsEventHandle<OnBaUsersDelete>() {
			@Override
			public void handleEvent(OnBaUsersDelete event) {
				List<UserClient> users = event.getUsers();
				for(UserClient user : users){
					roleUsersField.getFromField().getStore().remove(user);
					roleUsersField.getToField().getStore().remove(user);
				}
			}
		});
	}
	
	@Override
	protected void afterRender() {
		super.afterRender();
		
		observable.subscribe(OnChangeBA.class, new ITbitsEventHandle<OnChangeBA>() {
			public void handleEvent(OnChangeBA event) {
				if(bulkGridPanel != null)
					bulkGridPanel.refresh(0);
				
				String sysPrefix = event.getBa().getSystemPrefix();
				getBAUsers(sysPrefix);
			}
		});
	}
	
	private void getBAUsers(String sysPrefix){
		roleUsersField.getFromField().getStore().removeAll();
		APConstants.apService.getBAUsers(sysPrefix, new AsyncCallback<ArrayList<UserClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error fetching BA Users.. Please see logs for details", caught);
				Log.error("Error fetching BA Users.. Please see logs for details", caught);
			}

			public void onSuccess(ArrayList<UserClient> result) {
				if(result != null){
					baUsers = result;
					roleUsersField.getFromField().getStore().add(result);
				}
			}});
	}
	
	private void getRoleUsers(int systemId, int roleId){
		roleUsersField.getToField().getStore().removeAll();
		APConstants.apService.getUsersByRoleId(systemId, roleId, new AsyncCallback<ArrayList<UserClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error fetching Role Users.. Please see logs for details", caught);
				Log.error("Error fetching Role Users.. Please see logs for details", caught);
			}

			public void onSuccess(ArrayList<UserClient> result) {
				if(result != null){
					roleUsersField.getToField().getStore().add(result);
					for(UserClient model : result){
						roleUsersField.getFromField().getStore().remove(model);
					}
				}
			}});
	}
}
