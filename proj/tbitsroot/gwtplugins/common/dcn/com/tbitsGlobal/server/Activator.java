/**
 * 
 */
package dcn.com.tbitsGlobal.server;

import commons.com.tbitsGlobal.utils.server.plugins.GWTProxyServletManager;

import transbit.tbits.api.RuleFactory;
import transbit.tbits.plugin.IActivator;
import dcn.com.tbitsGlobal.client.service.ChangeNoteService;
import dcn.com.tbitsGlobal.server.Activator;

/**
 * @author Lokesh
 *
 */
public class Activator implements IActivator {

	/* (non-Javadoc)
	 * @see transbit.tbits.plugin.IActivator#activate()
	 */
	@Override
	public void activate() {
		GWTProxyServletManager.getInstance().subscribe(ChangeNoteService.class.getName(), ChangeNoteServiceImpl.class);
	}

}
