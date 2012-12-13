package idc.com.tbitsGlobal.server;

import idc.com.tbitsGlobal.client.IDCService;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleFactory;
import transbit.tbits.plugin.IActivator;

import commons.com.tbitsGlobal.utils.server.plugins.GWTProxyServletManager;
public class Activator implements IActivator
{
	public void activate() 
	{
		GWTProxyServletManager.getInstance().subscribe(
				IDCService.class.getName(), IDCServiceImpl.class);
		IPostRule idcPostRule=new IDCPostRule();
		
		IRule idcPreRule= new IDCPreRule();
		RuleFactory.getInstance().getPostRules().add(idcPostRule);
		RuleFactory.getInstance().getPreRules().add(idcPreRule);
			
		// TODO :register all the other plugins too.
	}

}
