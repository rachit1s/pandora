package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.cache.UserCacheAdmin;
import com.tbitsGlobal.admin.client.state.AppState;
import com.tbitsGlobal.admin.client.utils.APConstants;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkGridCheckColumnConfig;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.NotificationRuleClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeUserClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserTypeClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class TypeUserBulkGrid extends BulkUpdateGridAbstract<TypeUserClient>{

	protected ListStore<UserClient> userListStore;
	
	public TypeUserBulkGrid(BulkGridMode mode) {
		super(mode);
		
		canRemoveRow = false;
		showStatus = false;
		
		userListStore	= new GroupingStore<UserClient>();
	}
	
	protected void populateUserStore(){
		userListStore.removeAll();
		userListStore.add(getUsersList());
	}
	

	protected void createColumns() {
		ColumnConfig  typeUser = new ColumnConfig(TypeUserClient.USER, "User", 200);
		ComboBox<UserClient> userCombo = new ComboBox<UserClient>();
		userCombo = new ComboBox<UserClient>();
		userCombo.setTriggerAction(TriggerAction.ALL);
		userCombo.setForceSelection(false);
		userCombo.setStore(userListStore);
		userCombo.setDisplayField(UserClient.USER_LOGIN);	
		
		CellEditor editor = new CellEditor(userCombo);	
		typeUser.setEditor(editor);
		GridCellRenderer<TypeUserClient> userComboRenderer = new GridCellRenderer<TypeUserClient>(){
			
			public String render(TypeUserClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<TypeUserClient> store, Grid<TypeUserClient> grid) {
				
				UserClient currentUser = model.getUser();
				return currentUser.getUserLogin();
			}		
		};
		typeUser.setRenderer(userComboRenderer);
		typeUser.setSortable(false);
		cm.getColumns().add(typeUser);
		
//		ColumnConfig col = new ColumnConfig(TypeUserClient.USER,"User", 200);
//		cm.getColumns().add(col);
		
		CheckColumnConfig assigneeCol = new BulkGridCheckColumnConfig(gridMode){
			protected void onMouseDown(GridEvent<ModelData> ge) {
				String cls = ge.getTarget().getClassName();
			    if (cls != null && cls.indexOf("x-grid3-cc-" + getId()) != -1 && cls.indexOf("disabled") == -1) {
			      ge.stopEvent();
			      int index = grid.getView().findRowIndex(ge.getTarget());
			      ModelData m = grid.getStore().getAt(index);
			      Record r = grid.getStore().getRecord(m);
			      Integer userTypeId = m.get(getDataIndex());
			      if(userTypeId == null || userTypeId != UserTypeClient.ASSIGNEE){
			    	  r.set(getDataIndex(), UserTypeClient.ASSIGNEE);
			      }else{
			    	  r.set(getDataIndex(), 0);
			      }
			    }
			}
			
			protected String getCheckState(ModelData model, String property,
					int rowIndex, int colIndex) {
				Integer v = model.get(property);
			    String on = (v != null && v == UserTypeClient.ASSIGNEE) ? "-on" : "";
			    return on;
			}
		};
		
		assigneeCol.setId(TypeUserClient.USER_TYPE_ID);
		assigneeCol.setWidth(100);
		assigneeCol.setHeader("Assignee");
		cm.getColumns().add(assigneeCol);
		this.addPlugin(assigneeCol);
		
		CheckColumnConfig volunteerCol = new BulkGridCheckColumnConfig(gridMode);
		volunteerCol.setId(TypeUserClient.IS_VOLUNTEER);
		volunteerCol.setWidth(100);
		volunteerCol.setHeader("Volunteer");
		cm.getColumns().add(volunteerCol);
		this.addPlugin(volunteerCol);
	
		final ComboBox<NotificationRuleClient> notificationCombo = new ComboBox<NotificationRuleClient>();
		notificationCombo.setStore(new ListStore<NotificationRuleClient>());
		notificationCombo.setDisplayField(NotificationRuleClient.DISPLAY_NAME);
		APConstants.apService.getNotifcation(new AsyncCallback<ArrayList<NotificationRuleClient>>(){
			public void onFailure(Throwable caught) {
			}
	
			public void onSuccess(ArrayList<NotificationRuleClient> result) {
				if(result != null){
					notificationCombo.getStore().add(result);
				}
			}
		});
		ColumnConfig notificationCol = new ColumnConfig(TypeUserClient.NOTIFICATION_ID,"Notification",200);
		notificationCol.setEditor(new CellEditor(notificationCombo){
			@Override
			public Object preProcessValue(Object value) {
				if(value != null){
					NotificationRuleClient model = notificationCombo.getStore().findModel(NotificationRuleClient.NOTIFICATION_RULE_ID, value);
					if(model != null)
						return model;
				}
				return null;
			}
			
			@Override
			public Object postProcessValue(Object value) {
				if(value != null){
					NotificationRuleClient model = (NotificationRuleClient) value;
					return model.getNotificationRuleId();
				}
				return null;
			}
		});
		notificationCol.setRenderer(new GridCellRenderer<TypeUserClient>(){
			public Object render(final TypeUserClient model, String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config,
					final int rowIndex, int colIndex,
					ListStore<TypeUserClient> store, final Grid<TypeUserClient> grid) {
				if(model.get(TypeUserClient.NOTIFICATION_ID) != null){
					int notificationId = model.getNotificationId();
					NotificationRuleClient rule = notificationCombo.getStore().findModel(NotificationRuleClient.NOTIFICATION_RULE_ID, notificationId);
					if(rule != null){
						return rule.getDisplayName();
					}
				}
				return null;
		}});
		cm.getColumns().add(notificationCol);
	}
	
	
	private UserClient getUser(Integer userId){
		if(AppState.checkAppStateIsTill(AppState.UserReceived)){
			UserCacheAdmin cache = CacheRepository.getInstance().getCache(UserCacheAdmin.class);
			List<UserClient> userList = new ArrayList<UserClient>(cache.getValues());
			if((userList != null) && (!userList.isEmpty())){
				for(UserClient user : userList){
					if(user.getUserId() == userId){
						return user;
					}
				}
			}else{
				TbitsInfo.error("Could not get the list of users... Please refresh....");
				Log.error("Error while getting list of users");
			}
		}
		return null;
	}

	/**
	 * Get the list of users
	 * @return List of all the users
	 */
	private List<UserClient> getUsersList(){
		if(AppState.checkAppStateIsTill(AppState.UserReceived)){
			UserCacheAdmin cache = CacheRepository.getInstance().getCache(UserCacheAdmin.class);
			List<UserClient> userList = new ArrayList<UserClient>(cache.getValues());
			if((userList != null) && (!userList.isEmpty())){
				return userList;
			}else{
				TbitsInfo.error("Could not get the list of users... Please refresh....");
				Log.error("Error while getting list of users");
			}
		}
		return null;
	}
}
