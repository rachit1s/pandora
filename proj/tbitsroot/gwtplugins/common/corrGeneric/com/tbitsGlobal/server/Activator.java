package corrGeneric.com.tbitsGlobal.server;

import commons.com.tbitsGlobal.utils.server.plugins.GWTProxyServletManager;

import corrGeneric.com.tbitsGlobal.client.services.CorrAdminService;
import corrGeneric.com.tbitsGlobal.client.services.CorrDBService;

import transbit.tbits.plugin.IActivator;


public class Activator implements IActivator {

	public void activate() 
	{
		GWTProxyServletManager.getInstance().subscribe(
				CorrDBService.class.getName(), CorrDBServiceImpl.class);
		
		GWTProxyServletManager.getInstance().subscribe(
				CorrAdminService.class.getName(), "Correspondence_Admin", CorrAdminServiceImpl.class);
	}

}
