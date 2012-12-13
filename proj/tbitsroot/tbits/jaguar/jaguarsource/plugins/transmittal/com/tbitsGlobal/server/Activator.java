package transmittal.com.tbitsGlobal.server;

import commons.com.tbitsGlobal.utils.server.plugins.GWTProxyServletManager;

import transbit.tbits.plugin.IActivator;
import transmittal.com.tbitsGlobal.client.TransmittalService;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminService;

public class Activator implements IActivator {	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void activate() {
		GWTProxyServletManager.getInstance().subscribe(TransmittalService.class.getName(), TransmittalServiceImpl.class);
		/*
		 * Registering the TrnAdminService with the activator
		 */
		GWTProxyServletManager.getInstance().subscribe(TrnAdminService.class.getName(), "Transmittal_Admin", TrnAdminServiceImpl.class);
		
	}

}
