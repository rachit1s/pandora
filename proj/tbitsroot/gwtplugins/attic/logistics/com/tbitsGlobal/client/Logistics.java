package logistics.com.tbitsGlobal.client;

import java.util.HashMap;

import logistics.com.tbitsGlobal.client.widgets.LogisticsAddRequestForm;
import logistics.com.tbitsGlobal.client.widgets.LogisticsUpdateRequestForm;
import logistics.com.tbitsGlobal.client.widgets.LogisticsViewRequestForm;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.plugins.IAddRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.IUpdateRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.IViewRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.RequestPanelSlot;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractAddRequestForm;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractUpdateRequestForm;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractViewRequestForm;

public class Logistics implements EntryPoint, LogisticsConstants{

	private HashMap<String, Stage> stagesMap;
	
	@Override
	public void onModuleLoad() {
		((ServiceDefTarget)logisticsService).setServiceEntryPoint(JaguarConstants.JAGUAR_PROXY);
		
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class, IViewRequestFormPlugin.class,
				new IViewRequestFormPlugin(){
					@Override
					public AbstractViewRequestForm getWidget(UIContext param) {
						return new LogisticsViewRequestForm(param, Logistics.this.stagesMap.get(ClientUtils.getSysPrefix()));
					}

					@Override
					public boolean shouldExecute(String sysPrefix) {
						if(stagesMap != null && stagesMap.get(sysPrefix) != null)
							return true;
						return false;
					}});
		
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class, IAddRequestFormPlugin.class,
				new IAddRequestFormPlugin(){
					@Override
					public AbstractAddRequestForm getWidget(UIContext param) {
						return new LogisticsAddRequestForm(param, Logistics.this.stagesMap.get(ClientUtils.getSysPrefix()));
					}

					@Override
					public boolean shouldExecute(String sysPrefix) {
						if(stagesMap != null && stagesMap.get(sysPrefix) != null)
							return true;
						return false;
					}});
		
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class, IUpdateRequestFormPlugin.class,
				new IUpdateRequestFormPlugin(){
					@Override
					public AbstractUpdateRequestForm getWidget(UIContext param) {
						return new LogisticsUpdateRequestForm(param, Logistics.this.stagesMap.get(ClientUtils.getSysPrefix()));
					}

					@Override
					public boolean shouldExecute(String sysPrefix) {
						if(stagesMap != null && stagesMap.get(sysPrefix) != null)
							return true;
						return false;
					}});
		
		buildStageParamsMap();
	}
	
	/**
	 * Builds the Stage Parameters for all the stages
	 * 
	 * TODO : write the RPC
	 */
	private void buildStageParamsMap(){
		LogisticsConstants.logisticsService.getStagesMap(new AsyncCallback<HashMap<String,Stage>>(){
			@Override
			public void onFailure(Throwable caught) {
				Log.error("Could not load stage parameters for logistics", caught);
			}

			@Override
			public void onSuccess(HashMap<String, Stage> result) {
				stagesMap = result;
			}});
	}

}
