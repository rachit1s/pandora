package mom.com.tbitsGlobal.client;

import mom.com.tbitsGlobal.client.admin.MOMAdminPagePluginContainer;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.admin.client.AdminConstants;
import com.tbitsGlobal.admin.client.plugins.AbstractPagePluginContainer;
import com.tbitsGlobal.admin.client.plugins.IPagePlugin;
import com.tbitsGlobal.admin.client.plugins.slots.PagePluginsSlot;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;

/**
 * Entrypoint class for MOM Admin Panel. This sets the entrypoint and register the
 * mom admin module with the plugin register
 * @author devashish
 *
 */
public class MOMAdmin implements EntryPoint, MOMAdminConstants {

	public void onModuleLoad() {
		((ServiceDefTarget) momAdminService).setServiceEntryPoint(AdminConstants.ADM_PROXY);
		
		GWTPluginRegister.getInstance().addPlugin(PagePluginsSlot.class, IPagePlugin.class, new IPagePlugin(){

			public AbstractPagePluginContainer getWidget(Object param) {
				return new MOMAdminPagePluginContainer();
			}

			public boolean shouldExecute(String sysPrefix) {
				return true;
			}
			
		});
	}

}
