package corrGeneric.com.tbitsGlobal.client;

import static corrGeneric.com.tbitsGlobal.shared.CorrConst.*;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.plugins.IAddRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.IUpdateRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.IViewRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.RequestPanelSlot;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;

import corrGeneric.com.tbitsGlobal.client.plugins.CorrAddReqGWTPlugin;
import corrGeneric.com.tbitsGlobal.client.plugins.CorrUpdateReqGWTPlugin;
import corrGeneric.com.tbitsGlobal.client.plugins.CorrViewReqGWTPlugin;
import corrGeneric.com.tbitsGlobal.client.services.CorrDBService;
import corrGeneric.com.tbitsGlobal.client.utils.CorrHelperClient;
import corrGeneric.com.tbitsGlobal.shared.CorrConst;

public class CorrGeneric implements EntryPoint 
{
	public void onModuleLoad() 
	{
		CorrConst.corrDBService = GWT.create(CorrDBService.class);
		
		((ServiceDefTarget) CorrConst.corrDBService)
		.setServiceEntryPoint(JaguarConstants.JAGUAR_PROXY);

		CorrHelperClient.initializeCorrApplicableBAs(false);
//		CorrHelperClient.initializeViewRequestOptions(false);
		corrAddReqGWTPlugin = new CorrAddReqGWTPlugin();
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class,
					IAddRequestFormPlugin.class, corrAddReqGWTPlugin);
		
		corrUpdateReqGWTPlugin = new CorrUpdateReqGWTPlugin();
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class,
					IUpdateRequestFormPlugin.class, corrUpdateReqGWTPlugin);
		
		CorrConst.corrViewReqGWTPlugin = new CorrViewReqGWTPlugin();
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class,
					IViewRequestFormPlugin.class, CorrConst.corrViewReqGWTPlugin);
	}

}
