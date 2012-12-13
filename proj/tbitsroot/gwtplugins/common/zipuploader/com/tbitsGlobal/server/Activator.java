package zipuploader.com.tbitsGlobal.server;


import zipuploader.com.tbitsGlobal.client.HolcimJagService;
import commons.com.tbitsGlobal.utils.server.plugins.GWTProxyServletManager;

import transbit.tbits.plugin.IActivator;

public class Activator implements IActivator{

	@Override
	public void activate() {
		
	//	GWTProxyServletManager.getInstance().subscribe(HolcimJagService.class.getName.class.getName(), HolcimJagServiceImpl.class);
		GWTProxyServletManager.getInstance().subscribe(HolcimJagService.class.getName(), HolcimJagServiceImpl.class);
		// TODO Auto-generated method stub
		
	}

	
	
	
	
}
