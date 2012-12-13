package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.List;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.events.OnFieldAdd;
import com.tbitsGlobal.admin.client.events.OnFieldsDelete;
import com.tbitsGlobal.admin.client.events.OnFieldsUpdate;
import com.tbitsGlobal.admin.client.modelData.RolePermissionModel;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportWindow;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class PermissionBulkGridPanel extends AbstractAdminBulkUpdatePanel<RolePermissionModel> {

	private RoleClient role;
	
	public PermissionBulkGridPanel() {
		super();
		
		canAddRows = false;
		canDeleteRows = false;
		canCopyPasteRows = false;
		
		observable.subscribe(OnFieldAdd.class, new ITbitsEventHandle<OnFieldAdd>() {
			public void handleEvent(OnFieldAdd event) {
				refresh(0);
			}
		});
		
		observable.subscribe(OnFieldsDelete.class, new ITbitsEventHandle<OnFieldsDelete>() {
			public void handleEvent(OnFieldsDelete event) {
				refresh(0);
			}
		});
		
		observable.subscribe(OnFieldsUpdate.class, new ITbitsEventHandle<OnFieldsUpdate>() {
			public void handleEvent(OnFieldsUpdate event) {
				refresh(0);
			}
		});
	}
	
	protected void onSave(List<RolePermissionModel> models, Button btn) {
		TbitsInfo.info("Updating role permissions in database... Please Wait...");
		APConstants.apService.updateRolePermissions(ClientUtils.getCurrentBA().getSystemId(), role.getRoleId(), 
				models, new AsyncCallback<Boolean>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("error while updating role permissions", caught);
				Log.error("error while updating role permissions", caught);
			}

			public void onSuccess(Boolean result) {
				if(result){
					TbitsInfo.info("Successfully updated permissions");
					
				}else TbitsInfo.error("error while updating role permissions");
			}

		});
	}

	public void refresh(int page) {
		if(role != null){
			TbitsInfo.info("Retreiving permissions for selected role.. Please Wait..");
			APConstants.apService.getPermissionsbysysIdandRoleId(ClientUtils.getCurrentBA().getSystemId(), role.getRoleId(),
					new AsyncCallback<List<RolePermissionModel>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Unable to retrieve Permissions. Please select Role again", caught);
					Log.error("Unable to retrieve Permissions. Please select Role again", caught);
				}

				public void onSuccess(List<RolePermissionModel> result) {
					if(result != null){
						getSingleGridContainer().removeAllModels();
						getSingleGridContainer().addModel(result);
					}
				}
			});
		}
	}

	@Override
	public RolePermissionModel getEmptyModel() {
		return new RolePermissionModel();
	}

	public void setRole(RoleClient role) {
		this.role = role;
	}

	public RoleClient getRole() {
		return role;
	}

	@Override
	protected BulkUpdateGridAbstract<RolePermissionModel> getNewBulkGrid(
			BulkGridMode mode) {
		return new PermissionBulkGrid(mode);
	}
	
	@Override
	protected ExcelImportWindow<RolePermissionModel> onImport() {
		ExcelImportWindow<RolePermissionModel> window = super.onImport();
		window.setDefaultUniqueMatchingProperty(RolePermissionModel.FIELD_NAME);
		
		return window;
	}

}
