package corrGeneric.com.tbitsGlobal.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.admin.client.AdminConstants;
import com.tbitsGlobal.admin.client.plugins.AbstractPagePluginContainer;
import com.tbitsGlobal.admin.client.plugins.IPagePlugin;
import com.tbitsGlobal.admin.client.plugins.slots.PagePluginsSlot;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import corrGeneric.com.tbitsGlobal.client.extensions.CorrPagePluginContainer;
import corrGeneric.com.tbitsGlobal.client.CorrConstants;
/**
 * Defines the entry point for the module CorrAdmin
 */
public class CorrAdmin implements EntryPoint, CorrConstants {
	
	
	public void onModuleLoad() {
		((ServiceDefTarget) corrAdminService).setServiceEntryPoint(AdminConstants.ADM_PROXY);

		GWTPluginRegister.getInstance().addPlugin(PagePluginsSlot.class, IPagePlugin.class, new IPagePlugin(){
			
			public AbstractPagePluginContainer getWidget(Object param) {
				return new CorrPagePluginContainer();
			}

			
			public boolean shouldExecute(String sysPrefix) {
					return true;
			}});
	}

}
