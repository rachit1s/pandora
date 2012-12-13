package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.cache.UserCacheAdmin;
import com.tbitsGlobal.admin.client.modelData.MailingListUserClient;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.widgets.pages.MailingListDualListWindow;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class MailingListBulkGridPanel extends AbstractAdminBulkUpdatePanel<MailingListUserClient>{

	protected StoreFilterField<MailingListUserClient> filter;
	
	public MailingListBulkGridPanel() {
		super();
		
		commonGridDisabled = true;
		canSave = false;
		canReorderRows = false;
		canCopyPasteRows = false;
		
		LabelField filterLabel = new LabelField("Search : ");
		toolbar.add(filterLabel);
		applySearchFilter();
		toolbar.add(filter);
	}
	
	protected void applySearchFilter(){
		filter = new StoreFilterField<MailingListUserClient>(){
			protected boolean doSelect(Store<MailingListUserClient> store, MailingListUserClient parent, MailingListUserClient record,
					String property, String filter) {
				
				String mailListUser = record.getMailListUser().getUserLogin();
				ArrayList<String> mailListMembers = new ArrayList<String>();
				
				for(UserClient mailListMember : record.getMailListMembers()){
					String memberName = mailListMember.getDisplayName();
					memberName = memberName.toLowerCase();
					mailListMembers.add(memberName);
				}
				
				if(null != mailListUser){
					mailListUser = mailListUser.toLowerCase();
					if(mailListUser.contains(filter.toLowerCase())){
						return true;
					}
				}
				
				for(String mailListMember : mailListMembers){
					if(mailListMember.contains(filter.toLowerCase())){
							return true;
					}
				}
				return false;
			}
		};
		filter.bind(((MailingListBulkGrid)singleGridContainer.getBulkGrid()).getStore());
		filter.setEmptyText("Search");
	}
	
	@Override
	public void refresh(int page) {
		singleGridContainer.removeAllModels();
		TbitsInfo.info("Fetching Mailing List from database... Please Wait...");
		APConstants.apService.getAllMailingLists(new AsyncCallback<List<MailingListUserClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Mailing Lists not Loaded ...Please Refresh ...", caught);
				Log.error("Mailing Lists not Loaded ...Please Refresh ...", caught);
			}	
			public void onSuccess(List<MailingListUserClient> result) {
				if(result != null){
					singleGridContainer.addModel(result);
				}
			}			
		});
	}

	@Override
	protected void onSave(List<MailingListUserClient> models, Button btn) {
	}

	@Override
	public MailingListUserClient getEmptyModel() {
		return new MailingListUserClient();
	}

	@Override
	protected BulkUpdateGridAbstract<MailingListUserClient> getNewBulkGrid(
			BulkGridMode mode) {
		return new MailingListBulkGrid(mode);
	}
	
	@Override
	protected void onRemove() {
		final List<MailingListUserClient> selectedItems = singleGridContainer.getSelectedModels();
		if(selectedItems != null){
			if(selectedItems.size() > 0 && 
					com.google.gwt.user.client.Window.confirm("Do you want to delete " + selectedItems.size() + " selected Mailing Lists?")){
				APConstants.apService.deleteMailingLists(selectedItems, new AsyncCallback<Boolean>(){
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Mailing Lists could not be deleted...Please see logs for details ...", caught);
						Log.error("Mailing Lists could not be deleted...Please see logs for details ...", caught);
					}
					public void onSuccess(Boolean result) {
						if(result){
							TbitsInfo.info("Mailing Lists deleted");
						}
					}
				});
			}
				
		}
	}
	
	@Override
	protected void onAdd() {
		super.onAdd();
		
		UserCacheAdmin cache = CacheRepository.getInstance().getCache(UserCacheAdmin.class);
		List<UserClient> allUsers = new ArrayList<UserClient>(cache.getValues());
		
		MailingListDualListWindow window = new MailingListDualListWindow(allUsers);
		window.show();
	}

}
