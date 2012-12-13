package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.events.OnBaUsersAdd;
import com.tbitsGlobal.admin.client.events.OnBaUsersDelete;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeUserClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * Bulk grid panel for displaying Type Users
 *
 */
public class TypeUserBulkGridPanel extends AbstractAdminBulkUpdatePanel<TypeUserClient>{

	private TypeClient type;
	
	public TypeUserBulkGridPanel() {
		super();
		
		canCopyPasteRows = false;
		canAddRows = true;
		canDeleteRows = true;
		canReorderRows = false;
		
		observable.subscribe(OnChangeBA.class, new ITbitsEventHandle<OnChangeBA>(){
			public void handleEvent(OnChangeBA event) {
				getBAUsers();
			}
		});
		
		observable.subscribe(OnBaUsersAdd.class, new ITbitsEventHandle<OnBaUsersAdd>() {
			public void handleEvent(OnBaUsersAdd event) {
				getBAUsers();
			}
		});
		
		observable.subscribe(OnBaUsersDelete.class, new ITbitsEventHandle<OnBaUsersDelete>() {
			public void handleEvent(OnBaUsersDelete event) {
				getBAUsers();
			}
		});
	}
	
	protected void onSave(List<TypeUserClient> models, Button btn) {
		if(type != null ){
			List<TypeUserClient> saveModels = new ArrayList<TypeUserClient>();
			for(TypeUserClient model : models){
				model.setSystemId(ClientUtils.getCurrentBA().getSystemId());
				model.setFieldId(type.getFieldId());
				model.setTypeId(type.getTypeId());
				model.setUserId(model.getUser().getUserId());
			
				saveModels.add(model);
			}
			TbitsInfo.info("Updating Type Users into database... Please Wait...");
			APConstants.apService.updateTypeUser(ClientUtils.getCurrentBA().getSystemPrefix(), type.getFieldId(), type.getTypeId(), saveModels, new AsyncCallback<HashMap<Integer, TypeUserClient>>(){
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Failed to Save Type Users... Please see log for Details", caught);
						Log.error("Failed to Save Type Users... Please see log for Details", caught);
					}
					public void onSuccess(HashMap<Integer, TypeUserClient> result) {
						if(result != null){
							TbitsInfo.info("Saved Notification rules..");
						}
					}
				}
			);
		}
	}
	
	protected void afterRender() {
		super.afterRender();
//		getBAUsers();
	}

	public TypeUserClient getEmptyModel() {
		TypeUserClient newClient = new TypeUserClient();
		newClient.setIsActive(true);
		newClient.setIsVolunteer(false);
		newClient.setNotificationId(1);
		newClient.setUserId(1);
		newClient.setUserTypeId(1);
		newClient.setIsVolunteer(false);
		newClient.setRRVolunteer(false);
		newClient.setTypeId(1);
		newClient.setSystemId(1);
		
		UserClient newUser = new UserClient();
		newUser.setUserLogin("NULL");
		
		newClient.setUser(newUser);
		return newClient;
	}
	
	protected void onAdd() {
		TypeUserClient newClient = new TypeUserClient();
		newClient.setIsActive(true);
		newClient.setIsVolunteer(false);
		newClient.setNotificationId(1);
		newClient.setUserId(1);
		newClient.setUserTypeId(1);
		newClient.setIsVolunteer(false);
		newClient.setRRVolunteer(false);
		newClient.setTypeId(1);
		newClient.setSystemId(1);
		
		UserClient newUser = new UserClient();
		newUser.setUserLogin("NULL");
		
		newClient.setUser(newUser);
		
		singleGridContainer.addModel(newClient);
		populateUserComboBox();
	}

	protected BulkUpdateGridAbstract<TypeUserClient> getNewBulkGrid(BulkGridMode mode) {
		return new TypeUserBulkGrid(mode);
	}

	public void refresh(int page) {
		if(type != null){
			TbitsInfo.info("Fetching Type Users from database... Please Wait...");
			singleGridContainer.getBulkGrid().getStore().removeAll();
			APConstants.apService.getTypeUser(ClientUtils.getCurrentBA().getSystemPrefix(), type.getFieldId(), type.getTypeId(), new AsyncCallback<HashMap<Integer,TypeUserClient>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error while loading type list... Please see log for Details", caught);
					Log.error("Error while loading type list... Please see log for Details", caught);
				}
	
				public void onSuccess(HashMap<Integer, TypeUserClient> result) {
					if((result != null) && (!result.isEmpty())){
						
						singleGridContainer.getBulkGrid().getStore().removeAll();
						Integer count = 0;
						for(Integer index : result.keySet()){
							singleGridContainer.getBulkGrid().getStore().insert(result.get(index), count++);
						}
					}
				}
			});
		}
		populateUserComboBox();
	}
	
	private void populateUserComboBox(){
//		((TypeUserBulkGrid)singleGridContainer.getBulkGrid()).getStore().removeAll();
//		((TypeUserBulkGrid)commonGridContainer.getBulkGrid()).getStore().removeAll();
		
		((TypeUserBulkGrid)singleGridContainer.getBulkGrid()).populateUserStore();
		((TypeUserBulkGrid)commonGridContainer.getBulkGrid()).populateUserStore();
	}
	
	private void getBAUsers(){
		singleGridContainer.removeAllModels();
		TbitsInfo.info("Fetching BA Users... Please Wait...");
		APConstants.apService.getBAUsers(ClientUtils.getCurrentBA().getSystemPrefix(), new AsyncCallback<ArrayList<UserClient>>(){
			public void onFailure(Throwable caught) {
				Log.error("Error while loading BA users... Please see logs for Details", caught);
				TbitsInfo.error("Error while loading BA users... Please see logs for Details", caught);
			}

			public void onSuccess(ArrayList<UserClient> result) {
				if(result != null){
					List<TypeUserClient> typeUsers = new ArrayList<TypeUserClient>();
					for(UserClient uc : result){
						TypeUserClient model  = new TypeUserClient();
						model.set(TypeUserClient.USER, uc.getUserLogin());
						model.setUserId(uc.getUserId());
						model.setSystemId(ClientUtils.getCurrentBA().getSystemId());
						model.setIsActive(uc.getIsActive());
						typeUsers.add(model);
					}
					singleGridContainer.addModel(typeUsers);
				}
			}
			
		});
	}

	public void setType(TypeClient type) {
		this.type = type;
	}

	public TypeClient getType() {
		return type;
	}

}
