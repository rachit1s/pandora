package hse.com.tbitsGlobal.server;


import hse.com.tbitsGlobal.client.HSEService;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleFactory;
import transbit.tbits.plugin.IActivator;

import commons.com.tbitsGlobal.utils.server.plugins.GWTProxyServletManager;

public class Activator implements IActivator {

	public void activate() {
		// TODO Auto-generated method stub

		GWTProxyServletManager.getInstance().subscribe(
				HSEService.class.getName(), HSEServiceImpl.class);

		IRule airPreRule = new AIRPreRule();
		IRule parPreRule = new PARPreRule();
		IPostRule airPostRule = new AIRPostRule();		

		RuleFactory.getInstance().getPreRules().add(airPreRule);
		RuleFactory.getInstance().getPreRules().add(parPreRule);
		RuleFactory.getInstance().getPostRules().add(airPostRule);
	}

}
