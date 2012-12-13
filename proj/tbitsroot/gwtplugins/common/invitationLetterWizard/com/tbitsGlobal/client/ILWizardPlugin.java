package invitationLetterWizard.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.plugins.IWizardPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.WizardPluginSlot;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;

public class ILWizardPlugin implements EntryPoint, IWizardPlugin  {

	private List<String> validBAList;
	
	public void onModuleLoad() {
		((ServiceDefTarget) ILConstants.dbService).setServiceEntryPoint(JaguarConstants.JAGUAR_PROXY);
		GWTPluginRegister.getInstance().addPlugin(WizardPluginSlot.class,
				IWizardPlugin.class, this);
		
		ILConstants.dbService.getValidBAList(new AsyncCallback<List<String>>(){
			public void onFailure(Throwable caught) {
				Log.error("Could not find Valid Business Areas for IL module", caught);
			}

			public void onSuccess(List<String> result) {
				if(result == null)
					Log.error("Could not find Valid Business Areas for IL module");
				validBAList = result;
			}});
	}

	public String getButtonCaption() {
		return "Create Invitation Letter";
	}

	public AbstractWizard getWidget(ArrayList<Integer> param) {
		if(param != null && param.size() > 0)
			return new ILWizard(param);
		else
			Window.alert("Please select records to create Invitation Letter");
		return null;
	}

	public boolean shouldExecute(String sysPrefix) {
		if( null != sysPrefix && validBAList != null && validBAList.contains(sysPrefix))
			return true ;
		
		return false;
	}
}
