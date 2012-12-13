package qap.com.tbitsGlobal.server;

import qap.com.tbitsGlobal.client.QapService;
import commons.com.tbitsGlobal.utils.server.plugins.GWTProxyServletManager;

import transbit.tbits.plugin.IActivator;

public class Activator implements IActivator {

	@Override
	public void activate() {
		
		GWTProxyServletManager.getInstance().subscribe(QapService.class.getName(), QapServiceImpl.class);
	}

}
