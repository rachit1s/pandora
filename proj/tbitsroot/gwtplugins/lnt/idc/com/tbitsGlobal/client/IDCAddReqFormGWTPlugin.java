package idc.com.tbitsGlobal.client;

import static idc.com.tbitsGlobal.client.IDCConstants.idcService;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.plugins.IAddRequestFormPlugin;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractAddRequestForm;

public class IDCAddReqFormGWTPlugin implements IAddRequestFormPlugin {
	static ArrayList<String> validBAs;

	public AbstractAddRequestForm getWidget(UIContext param) {
		// TODO Auto-generated method stub
		return new IDCAddRequestForm(param);

	}

	public static void getValidBAList() {
		// TODO Auto-generated method stub
		idcService.getValidBAs(new AsyncCallback<ArrayList<String>>() {

			
			public void onSuccess(ArrayList<String> result) {
				if (result == null)
					Log
							.error("Could not find Valid Business Areas for IDC module");
				validBAs = result;

			}

			
			public void onFailure(Throwable caught) {
				Log.error("Could not find Valid Business Areas for IDC module",
						caught);
			}
		});

	}

	public boolean shouldExecute(String sysPrefix) {

		if (validBAs == null) {
			getValidBAList();

		}
		if (null != validBAs && null != sysPrefix
				&& validBAs.contains(sysPrefix))
			return true;
		else
			return false;
	}

}
