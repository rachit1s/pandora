package mom.com.tbitsGlobal.client;

import java.util.List;

import mom.com.tbitsGlobal.client.Extensions.MOMGridContextMenu;
import mom.com.tbitsGlobal.client.Extensions.MOMMainTabPanel;
import mom.com.tbitsGlobal.client.Extensions.MOMSearchToolBar;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.plugins.IGridContextMenuPlugin;
import com.tbitsGlobal.jaguar.client.plugins.ISearchToolBarPlugin;
import com.tbitsGlobal.jaguar.client.plugins.ITbitsMainTabPanelPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.MOMPluginSlot;
import com.tbitsGlobal.jaguar.client.searchgrid.SearchToolBar;
import com.tbitsGlobal.jaguar.client.widgets.TbitsMainTabPanel;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.grids.GridContextMenu;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGrid;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;

public class MOM implements EntryPoint, MOMConstants {
	
	public static List<String> validBAList;
	
	public void onModuleLoad() {
		((ServiceDefTarget) momService).setServiceEntryPoint(JaguarConstants.JAGUAR_PROXY);
		
		momService.getValidBAList(new AsyncCallback<List<String>>(){
			public void onFailure(Throwable caught) {
				Log.error("Could not find Valid Business Areas for MOM module", caught);
			}

			public void onSuccess(List<String> result) {
				if(result == null)
					Log.error("Could not find Valid Business Areas for MOM module");
				validBAList = result;
			}});
		
		GWTPluginRegister.getInstance().addPlugin(MOMPluginSlot.class,
				ITbitsMainTabPanelPlugin.class, new ITbitsMainTabPanelPlugin() {
					public TbitsMainTabPanel getWidget(Object param) {
						return new MOMMainTabPanel();
					}

					public boolean shouldExecute(String sysPrefix) {
						if(validBAList == null)
							return false;
						return validBAList.contains(sysPrefix);
					}
				});

		GWTPluginRegister.getInstance().addPlugin(MOMPluginSlot.class,
				IGridContextMenuPlugin.class, new IGridContextMenuPlugin() {
					public GridContextMenu getWidget(RequestsViewGrid iGrid) {
						return new MOMGridContextMenu(iGrid);//, param);
					}
					
					public boolean shouldExecute(String sysPrefix) {
						if(validBAList == null)
							return false;
						return validBAList.contains(sysPrefix);
					}
				});

		GWTPluginRegister.getInstance().addPlugin(MOMPluginSlot.class,
				ISearchToolBarPlugin.class, new ISearchToolBarPlugin() {
					public SearchToolBar getWidget(UIContext param) {
						return new MOMSearchToolBar(ClientUtils.getSysPrefix(), param);
					}
					
					public boolean shouldExecute(String sysPrefix) {
						if(validBAList == null)
							return false;
						return validBAList.contains(sysPrefix);
					}
				});
	}
}
