package digitalDC.com.tbitsGlobal.server;


//import digitalDC.com.tbitsGlobal.client.HolcimJagService;
import commons.com.tbitsGlobal.utils.server.plugins.GWTProxyServletManager;
import digitalDC.com.tbitsGlobal.client.DDCJagService;

import transbit.tbits.plugin.IActivator;

public class Activator implements IActivator{

	@Override
	public void activate() {
		
	//	GWTProxyServletManager.getInstance().subscribe(HolcimJagService.class.getName.class.getName(), HolcimJagServiceImpl.class);
		GWTProxyServletManager.getInstance().subscribe(DDCJagService.class.getName(), DDCJagServiceImpl.class);
		// TODO Auto-generated method stub
		
	}

	
	
	
	
}
