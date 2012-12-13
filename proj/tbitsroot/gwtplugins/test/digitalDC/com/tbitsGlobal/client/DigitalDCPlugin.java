
package digitalDC.com.tbitsGlobal.client;

import java.util.ArrayList;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.jaguar.client.plugins.IWizardPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.WizardPluginSlot;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;


/**
 * @author lokesh
 *
 */
public class DigitalDCPlugin implements EntryPoint, IWizardPlugin {

	private TbitsObservable observable = new BaseTbitsObservable();
	

	
	protected boolean isExistsInHistoryRole = false;
	
	/* (non-Javadoc)
	 * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
	 */
	
	 
		
		
	public void onModuleLoad() {
	//	Window.alert("hello");
		((ServiceDefTarget)DDCConstants.dbService).setServiceEntryPoint("/jaguar/proxy");
		
		
		
		GWTPluginRegister.getInstance().addPlugin(WizardPluginSlot.class, IWizardPlugin.class, this);
		
		observable.attach();
		
	}

	/* (non-Javadoc)
	 * @see com.tbitsGlobal.jaguar.client.plugins.IWizardPlugin#getButtonCaption()
	 */
	public String getButtonCaption() {
		return "Upload Zip File";
	}

	/* (non-Javadoc)
	 * @see com.tbitsGlobal.jaguar.client.plugins.IGWTPlugin#getWidget(java.lang.Object)
	 */
	public AbstractWizard getWidget(ArrayList<Integer> param) {
		
			TbitsTreeRequestData tmd  = new TbitsTreeRequestData ();
//			BAFieldAttachment newField  = new BAFieldAttachment();
			
			return new DDCWizard();	
		
	}

	/**
	 * Specifies whether the transmittal process will proceed or not.
	 * Returns true if ba list is not empty, false otherwise
	 */
	public boolean shouldExecute(String sysPrefix) {		
		
		return true;
	}

}

