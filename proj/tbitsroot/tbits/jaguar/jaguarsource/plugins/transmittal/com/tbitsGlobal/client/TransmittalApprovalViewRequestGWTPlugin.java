package transmittal.com.tbitsGlobal.client;

import java.util.ArrayList;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.jaguar.client.plugins.IViewRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.RequestPanelSlot;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractViewRequestForm;

public class TransmittalApprovalViewRequestGWTPlugin implements EntryPoint, IViewRequestFormPlugin {
	
	ArrayList<Integer> approvalBASysIds;
	public AbstractViewRequestForm getWidget(UIContext param) {
		
		return new TransmittalApprovalViewRequestForm(param);
	}

	public boolean shouldExecute(String sysPrefix) {
		if( null == sysPrefix )
			return false;
		
		if ((approvalBASysIds != null) && (!approvalBASysIds.isEmpty())){
			BusinessAreaClient bac = ClientUtils.getBAbySysPrefix(sysPrefix);
			if (bac != null){
				if (approvalBASysIds.contains(bac.getSystemId()))
					return true;
			}
		}
		
		return false;
	}

	public void onModuleLoad() {
		((ServiceDefTarget)TransmittalConstants.dbService).setServiceEntryPoint("/jaguar/proxy");
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class, IViewRequestFormPlugin.class, this);
		TransmittalConstants.dbService.getAllApprovalBASysIds (
				new AsyncCallback<ArrayList<Integer>>(){		

			public void onFailure(Throwable caught) {
				caught.printStackTrace();
			}

			public void onSuccess(ArrayList<Integer> result) {
				if (result != null){
					approvalBASysIds = result;
				}
			}
		});			
		
	}
}
