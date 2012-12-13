/**
 * 
 */
package dcn.com.tbitsGlobal.client.plugins;

import java.util.ArrayList;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.plugins.IViewRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.RequestPanelSlot;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractViewRequestForm;

import dcn.com.tbitsGlobal.client.ChangeNoteConstants;
import dcn.com.tbitsGlobal.client.plugins.form.ChangeNoteViewRequestForm;
import dcn.com.tbitsGlobal.shared.ChangeNoteConfig;

/**
 * @author Lokesh
 *
 */
public class ChangeNoteViewRequestFormPlugin implements EntryPoint, IViewRequestFormPlugin {

	protected ArrayList<ChangeNoteConfig> changeNoteConfigList = null;
	ChangeNoteConfig changeNoteConfig = null;
	
	/* (non-Javadoc)
	 * @see commons.com.tbitsGlobal.utils.client.plugins.IGWTPlugin#getWidget(java.lang.Object)
	 */
	@Override
	public AbstractViewRequestForm getWidget(UIContext param) {	
		return new ChangeNoteViewRequestForm(param);
	}

	/* (non-Javadoc)
	 * @see commons.com.tbitsGlobal.utils.client.plugins.IGWTPlugin#shouldExecute(java.lang.String)
	 */
	@Override
	public boolean shouldExecute(String sysPrefix) {
		return isExistsAsTargetSysPrefix(sysPrefix);
	}	

	@Override
	public void onModuleLoad() {
		
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class, IViewRequestFormPlugin.class, this);
		ChangeNoteConstants.dcnService.lookupAllChangeNoteConfig( 
				new AsyncCallback<ArrayList<ChangeNoteConfig>>(){

					@Override
					public void onFailure(Throwable caught) {
						TbitsInfo.write("Could not load \"Generate pdf\" plugin.", TbitsInfo.INFO);
					}
		
					@Override
					public void onSuccess(ArrayList<ChangeNoteConfig> result) {
						changeNoteConfigList = result;
					}

				});		
	}
	
	private boolean isExistsAsTargetSysPrefix(String sysPrefix) {
		if (changeNoteConfigList != null)
			for (ChangeNoteConfig cnc : changeNoteConfigList){
				if (cnc.getTargetSysPrefix().trim().equals(sysPrefix)){
					changeNoteConfig = cnc;
					return true;
				}
			}
		return false;
	}
}
