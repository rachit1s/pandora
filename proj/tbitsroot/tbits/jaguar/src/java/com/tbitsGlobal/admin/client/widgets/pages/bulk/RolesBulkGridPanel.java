package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.events.OnRolesChange;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class RolesBulkGridPanel extends AbstractAdminBulkUpdatePanel<RoleClient>{

	public RolesBulkGridPanel() {
		super();
		
		canCopyPasteRows = false;
		commonGridDisabled = true;
		canReorderRows = false;
		canDeleteRows = false;
	}
	
	@Override
	protected void onSave(List<RoleClient> models, Button btn) {
		TbitsInfo.info("Updating Roles into database... Please Wait...");
		APConstants.apService.updateRoles(models, new AsyncCallback<Boolean>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error saving roles... Please see log for details..", caught);
				Log.error("Error saving roles... Please see log for details..", caught);
			}

			public void onSuccess(Boolean result) {
				if(result){
					TbitsInfo.info("Roles saved.");
				}
			}});
	}

	@Override
	public RoleClient getEmptyModel() {
		return new RoleClient();
	}

	@Override
	protected BulkUpdateGridAbstract<RoleClient> getNewBulkGrid(BulkGridMode mode) {
		return new RolesBulkUpdateGrid(mode);
	}

	public void refresh(int page) {
		singleGridContainer.removeAllModels();
		TbitsInfo.info("Fetching roles from database... Please Wait...");
		APConstants.apService.getRoleBySysPrefix(ClientUtils.getSysPrefix(), new AsyncCallback<List<RoleClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error loading roles... Please see log for details..", caught);
				Log.error("Error loading roles... Please see log for details..", caught);
			}

			public void onSuccess(List<RoleClient> result) {
				if(result != null){
					singleGridContainer.addModel(result);
					RoleClient baUsersRole = singleGridContainer.getBulkGrid().getStore().findModel(RoleClient.ROLE_NAME, "BAUsers");
					if(baUsersRole != null)
						singleGridContainer.getBulkGrid().getStore().remove(baUsersRole);
				}
			}});
	}
	
	protected void onAdd(){
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
								singleGridContainer.addModel(result);
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
}
