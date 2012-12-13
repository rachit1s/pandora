package hse.com.tbitsGlobal.client;

import static hse.com.tbitsGlobal.shared.HSEConstants.hseService;
import hse.com.tbitsGlobal.client.plugins.AIRAddRequestFormPlugin;
import hse.com.tbitsGlobal.client.plugins.AIRUpdateRequestFormPlugin;
import hse.com.tbitsGlobal.client.plugins.PARViewRequestFormPlugin;
import hse.com.tbitsGlobal.shared.HSEConstants;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.plugins.IAddRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.IUpdateRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.IViewRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.RequestPanelSlot;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;

public class HSE implements EntryPoint {

	public void onModuleLoad() {
		
		HSEConstants.hseService = GWT.create(HSEService.class);
		((ServiceDefTarget)hseService).setServiceEntryPoint(JaguarConstants.JAGUAR_PROXY);
		
		PARViewRequestFormPlugin parViewRequestFormPlugin = new PARViewRequestFormPlugin();
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class, IViewRequestFormPlugin.class,parViewRequestFormPlugin);
		
		AIRAddRequestFormPlugin airAddRequestFormPlugin=new AIRAddRequestFormPlugin();
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class, IAddRequestFormPlugin.class,airAddRequestFormPlugin);
		
		AIRUpdateRequestFormPlugin airUpdateRequestFormPlugin=new AIRUpdateRequestFormPlugin();
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class, IUpdateRequestFormPlugin.class,airUpdateRequestFormPlugin);
		
		// TODO Auto-generated method stub
		
	}

}
