package hse.com.tbitsGlobal.client.plugins;

import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_PREFIX;
import hse.com.tbitsGlobal.client.forms.PARViewRequestForm;

import com.tbitsGlobal.jaguar.client.plugins.IViewRequestFormPlugin;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractViewRequestForm;
public class PARViewRequestFormPlugin implements IViewRequestFormPlugin {

	@Override
	public AbstractViewRequestForm getWidget(UIContext param) {
		// TODO Auto-generated method stub
		return new PARViewRequestForm(param);
	}

	@Override
	public boolean shouldExecute(String sysPrefix) {
		// TODO Auto-generated method stub
		if(null!=sysPrefix && sysPrefix.equals(PAR_PREFIX))
			return true;
		else
		return false;
	}

}
