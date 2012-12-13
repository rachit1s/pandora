/**
 * 
 */
package transmittal.com.tbitsGlobal.client;

import java.util.ArrayList;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.jaguar.client.plugins.IWizardPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.WizardPluginSlot;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnCurrentUserReceived;
import commons.com.tbitsGlobal.utils.client.Events.OnFieldsReceived;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;

/**
 * @author lokesh
 *
 */
public class TransmittalWizardPlugin implements EntryPoint, IWizardPlugin {

	private TbitsObservable observable = new BaseTbitsObservable();
	
	final ArrayList<String> baList = new ArrayList<String>();
	
	protected boolean isExistsInHistoryRole = false;
	
	/* (non-Javadoc)
	 * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
	 */
	public void onModuleLoad() {
		((ServiceDefTarget)TransmittalConstants.dbService).setServiceEntryPoint("/jaguar/proxy");
		GWTPluginRegister.getInstance().addPlugin(WizardPluginSlot.class, IWizardPlugin.class, this);
		
		observable.attach();
		
		observable.subscribe(OnFieldsReceived.class, new ITbitsEventHandle<OnFieldsReceived>() {
			public void handleEvent(OnFieldsReceived event) {
				TransmittalConstants.fieldCache = CacheRepository.getInstance().getCache(FieldCache.class);
			}
		});
					
		observable.subscribe(OnCurrentUserReceived.class, new ITbitsEventHandle<OnCurrentUserReceived>() {
			public void handleEvent(OnCurrentUserReceived event) {
				TransmittalConstants.dbService.getDCRBusinessAreas(new AsyncCallback<ArrayList<String>>() {
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}
					public void onSuccess(ArrayList<String> dcrBAList) {
						baList.addAll(dcrBAList);
						if (!baList.isEmpty())
							baList.trimToSize();
					}
				});					
			}
		});
	}

	/* (non-Javadoc)
	 * @see com.tbitsGlobal.jaguar.client.plugins.IWizardPlugin#getButtonCaption()
	 */
	public String getButtonCaption() {
		return "Create Transmittal";
	}

	/* (non-Javadoc)
	 * @see com.tbitsGlobal.jaguar.client.plugins.IGWTPlugin#getWidget(java.lang.Object)
	 */
	public AbstractWizard getWidget(ArrayList<Integer> param) {
		if (param.size() <= 0){
			Window.alert("No drawings/documents selected for transmittal. Please, select atleast one or more drawings/documents " +
					"that have to be transmitted.");
			return null;
		}
		else{
			return new TransmittalWizard(param);			
		}
	}

	/**
	 * Specifies whether the transmittal process will proceed or not.
	 * Returns true if ba list is not empty, false otherwise
	 */
	public boolean shouldExecute(String sysPrefix) {		
		if ((baList!=null) && (!baList.isEmpty())){
			if(baList.contains(sysPrefix))
				return true;
		}
		return false;
	}

}
