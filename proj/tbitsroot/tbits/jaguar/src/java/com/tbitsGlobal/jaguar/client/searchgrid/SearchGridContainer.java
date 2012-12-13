package com.tbitsGlobal.jaguar.client.searchgrid;

import java.util.ArrayList;

import com.tbitsGlobal.jaguar.client.events.OnRequestsRecieved;
import com.tbitsGlobal.jaguar.client.events.ToSearch;
import com.tbitsGlobal.jaguar.client.plugins.ISearchToolBarPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.MOMPluginSlot;
import com.tbitsGlobal.jaguar.client.state.AppState;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGridToolBar;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import commons.com.tbitsGlobal.utils.client.search.grids.AbstractSearchGrid;
import commons.com.tbitsGlobal.utils.client.search.grids.AbstractSearchGridContainer;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.DQL;
import commons.com.tbitsGlobal.utils.client.widgets.GridPagingBar;

/**
 * 
 * @author sourabh
 * 
 * Container panel for {@link SearchGrid}
 */
public class SearchGridContainer extends AbstractSearchGridContainer{
	
	public SearchGridContainer(String sysPrefix, SearchGrid grid) {
		super(sysPrefix, grid);
		
		if(this.sysPrefix.equals(ClientUtils.getSysPrefix())){
			this.setPagingBar(new GridPagingBar(GlobalConstants.SEARCH_PAGESIZE){
				@Override
				protected void loadPage(int page) {
					if(page <= 0 || page > this.getTotalPages()){
						TbitsInfo.error("Invalid Page Number");
						return;
					}
					DQL dql = ((AbstractSearchGrid) SearchGridContainer.this.grid).getDql();
					/**
					 * Refreshes the request cache and marks it in the url.
					 */
					TbitsEventRegister.getInstance().fireEvent(new ToSearch(SearchGridContainer.this.sysPrefix, dql, page, pageSize, false));
				}});
			
			final ITbitsEventHandle<OnRequestsRecieved> requestsReceivedHandle = new ITbitsEventHandle<OnRequestsRecieved>(){
				public void handleEvent(OnRequestsRecieved event) {
					if(pagingBar != null && event.isBasicSearch()){
						pagingBar.adjustButtons(event.getPage(), event.getDqlResults().getTotalRecords());
					}
				}};
				
			observable.subscribe(OnRequestsRecieved.class, requestsReceivedHandle);
		}
		
		this.setToolBar();
	}
	
	/**
	 * Initializes the toolbar
	 */
	protected void setToolBar() {
		DefaultUIContext context = new DefaultUIContext();
		context.setValue(RequestsViewGridToolBar.CONTEXT_GRID, getGrid());

		boolean foundToolBar = false;

		/**
		 * Check for the presence of plugins
		 */
		//TODO: This logic needs to be changed.
		//If the plugins is not yet downloaded the config setting on the basis of which it needs to tell "shouldExecute"
		//shouldExecute generally returns false
		//So instead of returning boolean from the method, we will just return RuleResult [Yes, No, Wait]
		//And this portion needs to be under the a TimerThread.
		ArrayList<ISearchToolBarPlugin> plugins = GWTPluginRegister.getInstance().getPlugins(MOMPluginSlot.class, ISearchToolBarPlugin.class);
		if (plugins != null && plugins.size() > 0
				&& AppState.checkAppStateIsTill(AppState.BAChanged)) {
			for (ISearchToolBarPlugin plugin : plugins) {
				if (plugin.shouldExecute(sysPrefix)) {
					this.setToolbar(plugin.getWidget(context));
					foundToolBar = true;
					break;
				}
			}
		}

		if (!foundToolBar) {
			this.setToolbar(new SearchToolBar(sysPrefix, context));
		}
	}
}
