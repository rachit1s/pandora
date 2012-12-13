package billtracking.com.tbitsGlobal.server;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleFactory;
import transbit.tbits.plugin.IActivator;
import billtracking.com.tbitsGlobal.client.services.BillService;
import billtracking.com.tbitsGlobal.server.rules.BillInitiationPreRule;
import billtracking.com.tbitsGlobal.server.rules.BillPostRule;
import billtracking.com.tbitsGlobal.server.rules.BillPreRule;

import commons.com.tbitsGlobal.utils.server.plugins.GWTProxyServletManager;

public class Activator implements IActivator {

	public void activate() {
		GWTProxyServletManager.getInstance().subscribe(
				BillService.class.getName(), BillServiceImpl.class);		
		IRule billPreRule= new BillPreRule();
		IRule billInitiationPreRule= new BillInitiationPreRule();
		IPostRule billPostRule = new BillPostRule();
		RuleFactory.getInstance().getPreRules().add(billPreRule);
		RuleFactory.getInstance().getPreRules().add(billInitiationPreRule);
		RuleFactory.getInstance().getPostRules().add(billPostRule);
	}

}
