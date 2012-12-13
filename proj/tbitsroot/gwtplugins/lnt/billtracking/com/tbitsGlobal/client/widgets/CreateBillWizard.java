package billtracking.com.tbitsGlobal.client.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import billtracking.com.tbitsGlobal.client.BillCache;
import billtracking.com.tbitsGlobal.shared.IBillConstants;
import billtracking.com.tbitsGlobal.shared.IBillProperties;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;
import com.tbitsGlobal.jaguar.client.plugins.IWizardPlugin;
import com.tbitsGlobal.jaguar.client.widgets.TbitsMainTabPanel;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;

public class CreateBillWizard implements IWizardPlugin,IBillConstants,IBillProperties {
	
	public String getButtonCaption() {
		// TODO Auto-generated method stub
		HashMap<String,String>  billProperties=BillCache.getInstance().getBillProperties();
        String caption=billProperties.get(PROPERTY_BUTTON_CAPTION);
		if(caption==null) return "Transfer for flow";
		else return caption;
	}

	public AbstractWizard getWidget(ArrayList<Integer> param) {
		if(!BillCache.getInstance().isTransferBillAllowed())
		{
			Window.alert("You are not allowed to transfer the bills.");
			return null;
		}

		if(param.size() != 1){
			Window.alert("Please select one record.");
			return null;
		}
		
		final MessageBox messageBox = MessageBox.wait("Please Wait ","Bills are getting linked", "Please Wait...");
		String srcSysPrefix=ClientUtils.getSysPrefix();
		billService.linkBills(param, srcSysPrefix, new AsyncCallback<List<RequestData>>() {

			public void onSuccess(List<RequestData> rdList) {
				TbitsMainTabPanel tb = JaguarConstants.jaguarTabPanel;
				
				Iterator<RequestData> i = rdList.iterator();
				while(i.hasNext()){
					UIContext uic=new DefaultUIContext();
					RequestData rd=i.next();
					tb.addNewRequestFormTab(uic,rd);
				}
				messageBox.close();
			}

			public void onFailure(Throwable arg0) {
				arg0.printStackTrace();
				Window.alert("Some Error Occurred While contacting the billService");
				messageBox.close();

			}
		});
		return null;
	}

	public boolean shouldExecute(String sysPrefix) {
		Log.info("shouldExecute in CreateBillWizard Called");
		if(sysPrefix==null) return false;
		// TODO Auto-generated method stub
		HashMap<String,String>  billProperties = BillCache.getInstance().getBillProperties();
		if(sysPrefix == null) 
			return false;
		
		if(billProperties == null){
			return false;
		}
		
		String sourceBa = billProperties.get(PROPERTY_SUBMISSION_BA_PREFIX);
		if(sourceBa == null) 
			return false;
		
		return sourceBa.equalsIgnoreCase(sysPrefix);
	}

}
