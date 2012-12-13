package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.tbitsGlobal.admin.client.cache.UserCacheAdmin;
import com.tbitsGlobal.admin.client.modelData.MailingListUserClient;
import com.tbitsGlobal.admin.client.widgets.pages.MailingListDualListWindow;

import commons.com.tbitsGlobal.utils.client.ClickableLink;
import commons.com.tbitsGlobal.utils.client.ClickableLink.ClickableLinkListener;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.LinkCellRenderer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public class MailingListBulkGrid extends BulkUpdateGridAbstract<MailingListUserClient>{

	public MailingListBulkGrid(BulkGridMode mode) {
		super(mode);
		
		canRemoveRow = false;
		showStatus = false;
	}

	@Override
	protected void createColumns() {
		ColumnConfig mailListUserCol = new ColumnConfig(MailingListUserClient.MAIL_LIST_USER, 200);
		mailListUserCol.setHeader("Mailing List User");
		mailListUserCol.setRenderer(new GridCellRenderer<MailingListUserClient>() {
			@Override
			public Object render(MailingListUserClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<MailingListUserClient> store,
					Grid<MailingListUserClient> grid) {
				if(model.get(MailingListUserClient.MAIL_LIST_USER) != null){
					UserClient user = model.getMailListUser();
					return user.getUserLogin();
				}
				return "";
			}
		});
		cm.getColumns().add(mailListUserCol);
		
		ColumnConfig mailListMembersCol = new ColumnConfig(MailingListUserClient.MAIL_LIST_MEMBERS, 500);
		mailListMembersCol.setHeader("Mailing List Members");
		mailListMembersCol.setRenderer(new GridCellRenderer<MailingListUserClient>() {
			@Override
			public Object render(MailingListUserClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<MailingListUserClient> store,
					Grid<MailingListUserClient> grid) {
				if(model.get(MailingListUserClient.MAIL_LIST_MEMBERS) != null){
					List<UserClient> members = model.getMailListMembers();
					if(members != null){
						String loginString = "";
						for(UserClient user : members){
							if(!loginString.equals(""))
								loginString += ", ";
							loginString += user.getUserLogin();
						}
						return loginString;
					}
				}
				return "";
			}
		});
		cm.getColumns().add(mailListMembersCol);
		
		ColumnConfig editColumn = new ColumnConfig("edit", "Edit", 120);
		editColumn.setFixed(true);
		GridCellRenderer<MailingListUserClient> fieldsbuttonRenderer = new LinkCellRenderer<MailingListUserClient>(){
			@Override
			public Object render(final MailingListUserClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<MailingListUserClient> store,
					Grid<MailingListUserClient> grid) {
				
				ClickableLink link = new ClickableLink("Edit", new ClickableLinkListener<GridEvent<MailingListUserClient>>(){
						public void onClick(GridEvent<MailingListUserClient> e) {
							UserCacheAdmin cache = CacheRepository.getInstance().getCache(UserCacheAdmin.class);
							List<UserClient> allUsers = new ArrayList<UserClient>(cache.getValues());
							MailingListDualListWindow window = new MailingListDualListWindow(allUsers, model);
							window.show();
						}
					});
				addLink(link);
				
				return link.getHtml();
			}};
		editColumn.setRenderer(fieldsbuttonRenderer);
		cm.getColumns().add(editColumn);
	}

}
