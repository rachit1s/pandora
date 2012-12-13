package browse.com.tbitsglobal.browse.client;

import java.util.HashMap;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.plugins.IAddRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.IUpdateRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.RequestPanelSlot;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractAddRequestForm;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractUpdateRequestForm;

public class Browse implements EntryPoint, BrowseConstants{
	
	private HashMap<String, Params> params;
	
	@Override
	public void onModuleLoad() {
		((ServiceDefTarget)browseService).setServiceEntryPoint(JaguarConstants.JAGUAR_PROXY);
		
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class, IAddRequestFormPlugin.class,
				new IAddRequestFormPlugin(){
					@Override
					public AbstractAddRequestForm getWidget(UIContext param) {
						return new BrowseAddRequestForm(param, Browse.this.params.get(ClientUtils.getSysPrefix()));
					}

					@Override
					public boolean shouldExecute(String sysPrefix) {
						if(params != null && params.get(sysPrefix) != null)
							return true;
						return false;
					}});
		
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class, IUpdateRequestFormPlugin.class,
				new IUpdateRequestFormPlugin(){
					@Override
					public AbstractUpdateRequestForm getWidget(UIContext param) {
						return new BrowseUpdateRequestForm(param, Browse.this.params.get(ClientUtils.getSysPrefix()));
					}

					@Override
					public boolean shouldExecute(String sysPrefix) {
						if(params != null && params.get(sysPrefix) != null)
							return true;
						return false;
					}});
		
		buildParams();
	}
	
	private void buildParams(){
		browseService.getParamsMap(new AsyncCallback<HashMap<String,Params>>() {
			@Override
			public void onSuccess(HashMap<String, Params> result) {
				if(result == null)
					Log.error("Unable to get paramaters for Browse module");
				params = result;
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Log.error("Unable to get paramaters for Browse module", caught);
			}
		});
	}

}
