package com.tbitsGlobal.jaguar.client.widgets.advsearch;

import java.util.ArrayList;

import com.tbitsGlobal.jaguar.client.events.OnRequestsRecieved;
import com.tbitsGlobal.jaguar.client.events.ToSearch;
import com.tbitsGlobal.jaguar.client.plugins.ISearchToolBarPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.MOMPluginSlot;
import com.tbitsGlobal.jaguar.client.searchgrid.SearchToolBar;
import com.tbitsGlobal.jaguar.client.state.AppState;

import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGridToolBar;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import commons.com.tbitsGlobal.utils.client.search.grids.AbstractSearchGrid;
import commons.com.tbitsGlobal.utils.client.search.grids.AbstractSearchGridContainer;
import commons.com.tbitsGlobal.utils.client.widgets.GridPagingBar;

/**
 * 
 * @author sourabh
 * 
 * The container panel for {@link AdvSearchGrid}
 * 
 */
public class AdvSearchGridContainer extends AbstractSearchGridContainer{
	
	public AdvSearchGridContainer(String sysPrefix, AdvSearchGrid grid) {
		super(sysPrefix, grid);
		
		this.setPagingBar(new GridPagingBar(GlobalConstants.SEARCH_PAGESIZE){
			@Override
			protected void loadPage(int page) {
				if(page <= 0 || page > this.getTotalPages()){
					TbitsInfo.error("Invalid Page Number");
					return;
				}
				/**
				 * Refreshes request cache but does not mark it in the url. 
				 */
				TbitsEventRegister.getInstance().fireEvent(
						new ToSearch(AdvSearchGridContainer.this.sysPrefix, 
								((AbstractSearchGrid) AdvSearchGridContainer.this.grid).getDql(), 
								page, pageSize, false, false));
			}});
		
		ITbitsEventHandle<OnRequestsRecieved> requestsReceivedHandle = new ITbitsEventHandle<OnRequestsRecieved>(){
			public void handleEvent(OnRequestsRecieved event) {
				if(pagingBar != null && !event.isBasicSearch()){
					/**
					 * Adgust the buttons when requests arrive
					 */
					int totalPages = event.getDqlResults().getTotalRecords();
					pagingBar.adjustButtons(event.getPage(), totalPages);
				}
			}};
			
		observable.subscribe(OnRequestsRecieved.class, requestsReceivedHandle);
		
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