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
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportWindow;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class ControlsBulkGridPanel extends AbstractAdminBulkUpdatePanel<RolePermissionModel> {

	public ControlsBulkGridPanel(){
		canAddRows = false;
		canDeleteRows = false;
		canCopyPasteRows = false;
		applyHandlers();
	}
	
	protected void applyHandlers(){
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
		TbitsInfo.info("Saving Field Controls... Please Wait...");
		APConstants.apService.updateFieldControls(ClientUtils.getCurrentBA().getSystemId(), models, new AsyncCallback<List<RolePermissionModel>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not save the updated Field Controls to database...", caught);
				Log.error("Could not save the updated Field Controls to database...", caught);
			}
			
			public void onSuccess(List<RolePermissionModel> result) {
				if(result != null){
					TbitsInfo.info("Successfully saved field controls...");
					getSingleGridContainer().removeAllModels();
					getSingleGridContainer().addModel(result);
				}
			}
		});
	}

	public void refresh(int page) {
		TbitsInfo.info("Fetching field controls from database... Please wait");
		APConstants.apService.getFieldControls(ClientUtils.getCurrentBA().getSystemId(), new AsyncCallback<List<RolePermissionModel>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to retrieve Field Controls... See Logs for more information", caught);
				Log.error("Unable to retrieve Permissions.", caught);
			}

			public void onSuccess(List<RolePermissionModel> result) {
				if(result != null){
					getSingleGridContainer().removeAllModels();
					getSingleGridContainer().addModel(result);
				}
			}
		});
		
	}
	
	public RolePermissionModel getEmptyModel() {
		return new RolePermissionModel();
	}

	protected BulkUpdateGridAbstract<RolePermissionModel> getNewBulkGrid(BulkGridMode mode) {
		return new ControlsBulkGrid(mode);
	}
	
	protected ExcelImportWindow<RolePermissionModel> onImport() {
		ExcelImportWindow<RolePermissionModel> window = super.onImport();
		window.setDefaultUniqueMatchingProperty(RolePermissionModel.FIELD_NAME);
		
		return window;
	}

}
