package transmittal.com.tbitsGlobal.client;

import transmittal.com.tbitsGlobal.client.admin.TrnAdmPagePluginContainer;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.admin.client.AdminConstants;
import com.tbitsGlobal.admin.client.plugins.AbstractPagePluginContainer;
import com.tbitsGlobal.admin.client.plugins.IPagePlugin;
import com.tbitsGlobal.admin.client.plugins.slots.PagePluginsSlot;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
/**
 * Define the entrypoint for Transmittal Admin Module
 * @author devashish
 *
 */
public class TrnAdminPlugin implements EntryPoint, TrnAdminConstants {

	@Override
	public void onModuleLoad() {
		((ServiceDefTarget) trnAdminService).setServiceEntryPoint(AdminConstants.ADM_PROXY);

		GWTPluginRegister.getInstance().addPlugin(PagePluginsSlot.class, IPagePlugin.class, new IPagePlugin(){

			@Override
			public AbstractPagePluginContainer getWidget(Object param) {
				return new TrnAdmPagePluginContainer();
			}

			@Override
			public boolean shouldExecute(String sysPrefix) {
				return true;
			}
			
		});
	}

}
