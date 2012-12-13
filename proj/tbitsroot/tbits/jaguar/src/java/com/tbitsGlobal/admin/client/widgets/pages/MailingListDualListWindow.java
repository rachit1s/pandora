package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.cache.UserCacheAdmin;
import com.tbitsGlobal.admin.client.modelData.MailingListUserClient;
import com.tbitsGlobal.admin.client.utils.APConstants;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.DualListWindow;

public class MailingListDualListWindow extends DualListWindow<UserClient>{

	private TextField<String> mailingListName;
	
	public MailingListDualListWindow(List<UserClient> allUsers, MailingListUserClient mailingList) {
		super(allUsers, mailingList != null ? mailingList.getMailListMembers() : new ArrayList<UserClient>(), UserClient.USER_LOGIN, 
				new String[]{UserClient.USER_LOGIN}, "Save Mailing List");
		
		enableOrdering = false;
		
		mailingListName = new TextField<String>();
		mailingListName.setEmptyText("Mailing List Name");
		if(mailingList != null){
			mailingListName.setValue(mailingList.getMailListUser().getUserLogin());
			mailingListName.disable();
		}
		
		ToolBar toolBar = new ToolBar();
		toolBar.add(mailingListName);
		this.setTopComponent(toolBar);
	}
	
	public MailingListDualListWindow(List<UserClient> allUsers){
		this(allUsers, null);
	}

	@Override
	protected ListView<UserClient> createSourceList(List<UserClient> models,
			List<UserClient> currentModels, String displayProperty) {
		ListView<UserClient> sourceList = new ListView<UserClient>();
		sourceList.setBorders(false);
		sourceList.setDisplayProperty(displayProperty);
		
		final ListStore<UserClient> sourceStore = new ListStore<UserClient>();
		sourceStore.add(models);
		for(UserClient user : currentModels){
			UserClient c = sourceStore.findModel(UserClient.USER_LOGIN, user.getUserLogin());
			sourceStore.remove(c);
		}
		sourceStore.sort(UserClient.USER_LOGIN, SortDir.ASC);
		
		sourceList.setStore(sourceStore);
		
		return sourceList;
	}

	@Override
	protected EditorGrid<UserClient> createTargetGrid(List<UserClient> models,
			List<UserClient> currentModels) {
		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();  
        
		CheckBoxSelectionModel<UserClient> sm = new CheckBoxSelectionModel<UserClient>();
		columns.add(sm.getColumn());
        
		columns.add(new ColumnConfig(UserClient.USER_LOGIN, "User", 150));
        
        ColumnModel cm = new ColumnModel(columns);
        
        ListStore<UserClient> targetStore = new ListStore<UserClient>();
        targetStore.add(ClientUtils.sort(currentModels, -1, -1, true));
		
        EditorGrid<UserClient> targetGrid = new EditorGrid<UserClient>(targetStore, cm);
		targetGrid.setBorders(false);
		targetGrid.setSelectionModel(sm);
		targetGrid.setLayoutData(new FitLayout());
		targetGrid.setAutoExpandColumn(UserClient.USER_LOGIN);
		 
		return targetGrid;
	}

	@Override
	protected void onSubmit() {
		List<UserClient> users = targetGrid.getStore().getModels();
		String name = mailingListName.getValue();
		if(!isValidMailingList(name)){
			TbitsInfo.error("The User Type Id of the specified Mailing List is not 8... Hence not valid...");
			return;
		}
		if(name != null){
			APConstants.apService.updateMailingList(name, users, new AsyncCallback<Boolean>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("MailingList not Updated... Please see logs for details...",caught);
					Log.error("MailingList not Updated... Please see logs for details...",caught);
				}
				public void onSuccess(Boolean result) {
					if(result){
						TbitsInfo.info("Mailing lists updated");
					}
				}
			});
		}else{
			Window.alert("Please specify Mailing List Name");
		}
	}
	
	/**
	 * A mailing list name is valid if and only if its user type id is 8
	 * @param name
	 * @return
	 */
	protected boolean isValidMailingList(String name){
		UserCacheAdmin cache = CacheRepository.getInstance().getCache(UserCacheAdmin.class);
		List<UserClient> allUsers = new ArrayList<UserClient>(cache.getValues());
		for(UserClient user : allUsers){
			if(user.getUserLogin().trim().equals(name)){
				if(user.getUserTypeId() == 8){
					return true;
				}else{
					return false;
				}
			}
		}
		return false;
	}
}
