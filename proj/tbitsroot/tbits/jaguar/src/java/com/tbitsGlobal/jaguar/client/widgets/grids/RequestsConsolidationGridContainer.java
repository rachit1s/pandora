package com.tbitsGlobal.jaguar.client.widgets.grids;

import java.util.ArrayList;

import com.tbitsGlobal.jaguar.client.plugins.ISearchToolBarPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.MOMPluginSlot;
import com.tbitsGlobal.jaguar.client.state.AppState;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGridContainer;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGridToolBar;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;

/**
 * 
 * @author sourabh
 * 
 */
public class RequestsConsolidationGridContainer extends RequestsViewGridContainer{

	public RequestsConsolidationGridContainer(String sysPrefix, RequestsConsolidationGrid grid) {
		super(sysPrefix, grid);
		this.setHeaderVisible(false);
		
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
			this.setToolbar(new RequestsConsolidationGridToolBar(sysPrefix, context));
		}
	}
}
