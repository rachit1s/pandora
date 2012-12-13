package billtracking.com.tbitsGlobal.client.plugins;


import java.util.HashMap;

import billtracking.com.tbitsGlobal.client.BillCache;
import billtracking.com.tbitsGlobal.client.widgets.BtrackAddRequestForm;
import billtracking.com.tbitsGlobal.shared.IBillConstants;
import billtracking.com.tbitsGlobal.shared.IBillProperties;

import com.tbitsGlobal.jaguar.client.plugins.IAddRequestFormPlugin;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractAddRequestForm;

public class BtrackAddRequestFormPlugin implements IAddRequestFormPlugin,IBillConstants,IBillProperties {
	
	public AbstractAddRequestForm getWidget(UIContext param) {
		// TODO Auto-generated method stub
		return new BtrackAddRequestForm(param);
	}

	public boolean shouldExecute(String sysPrefix) {
		// TODO Auto-generated method stub
		HashMap<String,String>  billProperties=BillCache.getInstance().getBillProperties();
		if(sysPrefix==null||billProperties==null) return false;
		else{
			 return billProperties.get(PROPERTY_BILL_BA_PREFIX).equals(sysPrefix);
		}
        
       
	}

}
