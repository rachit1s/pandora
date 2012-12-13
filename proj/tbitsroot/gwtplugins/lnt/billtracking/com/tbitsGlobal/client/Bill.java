package billtracking.com.tbitsGlobal.client;

import billtracking.com.tbitsGlobal.client.plugins.BtrackAddRequestFormPlugin;
import billtracking.com.tbitsGlobal.client.plugins.BtrackUpdateRequestFormPlugin;
import billtracking.com.tbitsGlobal.client.widgets.CreateBillWizard;
import billtracking.com.tbitsGlobal.shared.IBillConstants;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.plugins.IAddRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.IUpdateRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.IWizardPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.RequestPanelSlot;
import com.tbitsGlobal.jaguar.client.plugins.slots.WizardPluginSlot;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;

public class Bill implements EntryPoint,IBillConstants {

	public void onModuleLoad() {

		// TODO Auto-generated method stub
		((ServiceDefTarget)billService).setServiceEntryPoint(JaguarConstants.JAGUAR_PROXY);

		BtrackAddRequestFormPlugin btrackAddRequestFormPlugin=new BtrackAddRequestFormPlugin();
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class, IAddRequestFormPlugin.class,btrackAddRequestFormPlugin);

		BtrackUpdateRequestFormPlugin btrackUpdateRequestFormPlugin=new BtrackUpdateRequestFormPlugin();
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class, IUpdateRequestFormPlugin.class,btrackUpdateRequestFormPlugin);
		
		GWTPluginRegister.getInstance().addPlugin(WizardPluginSlot.class,IWizardPlugin.class,new CreateBillWizard());
		
					
	}

}
