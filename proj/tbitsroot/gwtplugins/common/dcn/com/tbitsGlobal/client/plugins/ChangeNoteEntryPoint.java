/**
 * 
 */
package dcn.com.tbitsGlobal.client.plugins;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.plugins.IAddRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.IWizardPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.RequestPanelSlot;
import com.tbitsGlobal.jaguar.client.plugins.slots.WizardPluginSlot;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;

import dcn.com.tbitsGlobal.client.ChangeNoteConstants;
import dcn.com.tbitsGlobal.shared.ChangeNoteConfig;

/**
 * @author lokesh
 *
 */
public class ChangeNoteEntryPoint implements EntryPoint {

	private ArrayList<ChangeNoteConfig> changeNoteConfigList;
//	private TbitsObservable observable = new BaseTbitsObservable();

	/* (non-Javadoc)
	 * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
	 */
	@Override
	public void onModuleLoad() {
		((ServiceDefTarget)dcn.com.tbitsGlobal.client.ChangeNoteConstants.dcnService).setServiceEntryPoint(JaguarConstants.JAGUAR_PROXY);
		ChangeNoteConstants.dcnService.lookupAllChangeNoteConfig(
				new AsyncCallback<ArrayList<ChangeNoteConfig>>(){

					public void onFailure(Throwable caught) {
						TbitsInfo.info("Error occurred trying to load Change Note module for current BA. " + caught.getMessage());
					}

					public void onSuccess(ArrayList<ChangeNoteConfig> result) {
						ChangeNoteConstants.changeNoteConfigList.addAll(result);						
					}				
				});
		instantiatePlugins();
	}

	private void instantiatePlugins() {
		
		ChangeNoteConstants.dcnService.lookupDistinctBATypes(
				new AsyncCallback<HashMap<String, String>>(){

					public void onFailure(Throwable caught) {
						TbitsInfo.info("Error occurred trying to load Change Note module for current BA. " + caught.getMessage());
					}
					
					public void onSuccess(HashMap<String, String> result) {
						
						if (result != null){
							for (String baType : result.keySet()){
								GWTPluginRegister.getInstance().addPlugin(WizardPluginSlot.class, IWizardPlugin.class,
										new ChangeNoteWizardPlugin(baType, result.get(baType)));
							}
							GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class, IAddRequestFormPlugin.class,
									new ChangeNoteAddRequestFormPlugin());
						}
					}				
				});		
	}
}
