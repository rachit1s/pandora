package qap.com.tbitsGlobal.client;

import java.util.ArrayList;

import transmittal.com.tbitsGlobal.client.TransmittalConstants;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.jaguar.client.plugins.IWizardPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.WizardPluginSlot;

import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnFieldsReceived;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;

public class QapPlugin implements EntryPoint, IWizardPlugin {
	
	
	private TbitsObservable observable = new BaseTbitsObservable();
	//final ArrayList<String> baList = new ArrayList<String>();

	@Override
	public void onModuleLoad() {
		
		
		
		
		
		((ServiceDefTarget) QAPConstants.dbService)
				.setServiceEntryPoint("/jaguar/proxy");
		GWTPluginRegister.getInstance().addPlugin(WizardPluginSlot.class,
				IWizardPlugin.class, this);
		
		
		
		
		
observable.attach();
		
		observable.subscribe(OnFieldsReceived.class, new ITbitsEventHandle<OnFieldsReceived>() {
			public void handleEvent(OnFieldsReceived event) {
				QAPConstants.fieldCache = CacheRepository.getInstance().getCache(FieldCache.class);
			}
		});
		/*QAPConstants.dbService.getApplicableBas(new AsyncCallback<ArrayList<String>>() {
			
			@Override
			public void onSuccess(ArrayList<String> result) {
				
				baList.addAll(result);
				if (!baList.isEmpty())
					baList.trimToSize();
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				
				caught.printStackTrace();
				
			}
		});
		*/
		

	}

	@Override
	public String getButtonCaption() {
		return "Create MDCC";
	}

	@Override
	public AbstractWizard getWidget(ArrayList<Integer> param) {
		if (param.size() <= 0) {
			Window
					.alert("No drawings/documents selected for MDCC. Please, select atleast one or more drawings/documents "
							+ "that have to be transmitted.");
			return null;
		} else {
			return new QapWizard(param);
		}

	}

	@Override
	public boolean shouldExecute(String sysPrefix) {
		
		/*if ((baList!=null) && (!baList.isEmpty())){
			if(baList.contains(sysPrefix))
				return true;
		}
		return false;*/
		return sysPrefix.toUpperCase().contains("INSPECTION");
		

	}

}
