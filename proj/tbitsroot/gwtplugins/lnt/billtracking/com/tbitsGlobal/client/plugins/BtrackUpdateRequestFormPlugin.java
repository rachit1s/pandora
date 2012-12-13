package billtracking.com.tbitsGlobal.client.plugins;

import java.util.HashMap;

import billtracking.com.tbitsGlobal.client.BillCache;
import billtracking.com.tbitsGlobal.client.widgets.BtrackUpdateRequestForm;
import billtracking.com.tbitsGlobal.shared.IBillConstants;
import billtracking.com.tbitsGlobal.shared.IBillProperties;

import com.tbitsGlobal.jaguar.client.plugins.IUpdateRequestFormPlugin;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractUpdateRequestForm;

public class BtrackUpdateRequestFormPlugin implements IUpdateRequestFormPlugin,IBillConstants,IBillProperties {
	public AbstractUpdateRequestForm getWidget(UIContext param) {
		// TODO Auto-generated method stub
		return new BtrackUpdateRequestForm(param);
	}

	public boolean shouldExecute(String sysPrefix) {
		// TODO Auto-generated method stub
		BillCache bCache=BillCache.getInstance();
		HashMap<String,String>  billProperties=bCache.getBillProperties();
		if(sysPrefix==null||billProperties==null) return false;
		String billPrefix=billProperties.get(PROPERTY_BILL_BA_PREFIX);
		if(billPrefix==null) return false;
		return billPrefix.equals(sysPrefix);	        

	}
}
