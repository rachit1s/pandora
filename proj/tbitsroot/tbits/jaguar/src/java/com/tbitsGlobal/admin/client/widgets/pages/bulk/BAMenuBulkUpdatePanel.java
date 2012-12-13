package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.List;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.utils.APConstants;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.domainObjects.BAMenuClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class BAMenuBulkUpdatePanel extends AbstractAdminBulkUpdatePanel<BAMenuClient>{
	
	public BAMenuBulkUpdatePanel() {
		super();
		
		canCopyPasteRows = false;
		canReorderRows = false;
	}

	@Override
	public void refresh(int page) {
		singleGridContainer.removeAllModels();
		
		TbitsInfo.info("Retrieving BA Menu List from database... Please Wait...");
		APConstants.apService.getBAMenus(new AsyncCallback<List<BAMenuClient>>() {
			
			@Override
			public void onSuccess(List<BAMenuClient> result) {
				if(result != null){
					singleGridContainer.addModel(result);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not retrieve BA Menu List.. See logs for details..", caught);
				Log.error("Could not retrieve BA Menu List.. See logs for details..", caught);
			}
		});
	}

	@Override
	protected void onSave(List<BAMenuClient> models, Button btn) {
		TbitsInfo.info("Updating BA Menus... Please Wait...");
		APConstants.apService.updateMenus(models, new AsyncCallback<List<BAMenuClient>>() {
			@Override
			public void onSuccess(List<BAMenuClient> result) {
				singleGridContainer.removeAllModels();
				if(result != null){
					singleGridContainer.addModel(result);
				}
				
				TbitsInfo.info("Updated BA Menus successfully..");
			}
			
			@Override
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not update menus.. See logs for details..", caught);
				Log.error("Could not update menus.. See logs for details..", caught);
			}
		});
	}

	@Override
	public BAMenuClient getEmptyModel() {
		return new BAMenuClient();
	}

	@Override
	protected BulkUpdateGridAbstract<BAMenuClient> getNewBulkGrid(
			BulkGridMode mode) {
		return new BAMenuBulkGrid(mode);
	}
	
	@Override
	protected void onRemove() {
		final List<BAMenuClient> selectedModels = singleGridContainer.getSelectedModels();
		if(selectedModels.size() > 0){
			if(Window.confirm("Do you wish to delete " + selectedModels.size() + " records?")){
				APConstants.apService.deleteMenus(selectedModels, new AsyncCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
						for(BAMenuClient baMenu : selectedModels){
							singleGridContainer.getBulkGrid().getStore().remove(baMenu);
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Could not delete menus.. See logs for details..", caught);
						Log.error("Could not delete menus.. See logs for details..", caught);
					}
				});
			}
		}
	}

}
