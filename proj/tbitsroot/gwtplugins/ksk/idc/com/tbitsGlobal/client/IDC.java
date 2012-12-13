package idc.com.tbitsGlobal.client;

import static idc.com.tbitsGlobal.client.IDCConstants.idcService;

import java.util.ArrayList;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.plugins.IAddRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.IWizardPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.RequestPanelSlot;
import com.tbitsGlobal.jaguar.client.plugins.slots.WizardPluginSlot;
import com.tbitsGlobal.jaguar.client.widgets.TbitsMainTabPanel;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;
public class IDC implements EntryPoint,IWizardPlugin{

	static ArrayList<String> validSrcBAs = null;

	public void onModuleLoad() 
	{


		((ServiceDefTarget)idcService).setServiceEntryPoint(JaguarConstants.JAGUAR_PROXY);

		GWTPluginRegister.getInstance().addPlugin(WizardPluginSlot.class, IWizardPlugin.class, this);

		IDCAddReqFormGWTPlugin idcAddReqPlugin=new IDCAddReqFormGWTPlugin();
		GWTPluginRegister.getInstance().addPlugin(RequestPanelSlot.class,
				IAddRequestFormPlugin.class,idcAddReqPlugin);

		allValidSrcBAs();
		IDCAddReqFormGWTPlugin.getValidBAList();

	}

	private static void allValidSrcBAs() {
		// TODO Auto-generated method stub
		idcService.getValidSrcBAs(new AsyncCallback<ArrayList<String>>() {

			public void onSuccess(final ArrayList<String> result) {
				Log.info("Trying to get Valid Source BAs");
				if(result==null)
					Log.info("Valid Source Business Areas for IDC module Not Found Yet");
				validSrcBAs=result;
				Log.info("Valid Source BAs Found");

			}

			public void onFailure(Throwable caught) {
				Log.info("Unable to find Valid Source Business Areas for IDC module",caught);
			}
		});

	}

	public String getButtonCaption() {
		// TODO Auto-generated method stub
		return "Create IDC";
	}

	public AbstractWizard getWidget(ArrayList<Integer> param) {

		//TODO Auto-generated method stub
		final MessageBox messageBox = MessageBox.wait("Please Wait", 
				"IDC is being created", "Please Wait...");

		String sysPrefix=ClientUtils.getSysPrefix();
		idcService.getIDCRequest(param,sysPrefix,new AsyncCallback<RequestData>() {
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stubtype filter text
				caught.printStackTrace();
				System.out.println(caught.getMessage());
				Window.alert("Some Error Occurred While contacting the idcService");
				messageBox.close();
			}

			public void onSuccess(final RequestData result) {
				// TODO Auto-generated method stub									
				TbitsMainTabPanel tb = JaguarConstants.jaguarTabPanel;
				messageBox.close();
				tb.addNewRequestFormTab(result);
			}
		});
		return null;
	}

	public boolean shouldExecute(String sysPrefix) {
		if (validSrcBAs==null){
			allValidSrcBAs();

		}

		Log.info("should execute for IDC called.");
		if(null != validSrcBAs && null!=sysPrefix && validSrcBAs.contains(sysPrefix))
			return true;
		else
			return false;

	}
}
