/**
 * 
 */
package pdf.com.tbitsGlobal.server;

import pdf.com.tbitsGlobal.client.PdfService;
import commons.com.tbitsGlobal.utils.server.plugins.GWTProxyServletManager;

import transbit.tbits.plugin.IActivator;

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
		GWTProxyServletManager.getInstance().subscribe(PdfService.class.getName(), PdfServiceImpl.class);
	}

}
