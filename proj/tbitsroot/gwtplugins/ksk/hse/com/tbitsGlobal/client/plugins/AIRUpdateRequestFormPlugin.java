package hse.com.tbitsGlobal.client.plugins;

import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_PREFIX;
import hse.com.tbitsGlobal.client.forms.AIRUpdateRequestForm;

import com.tbitsGlobal.jaguar.client.plugins.IUpdateRequestFormPlugin;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractUpdateRequestForm;

public class AIRUpdateRequestFormPlugin implements IUpdateRequestFormPlugin {

	@Override
	public AbstractUpdateRequestForm getWidget(UIContext param) {
		// TODO Auto-generated method stub
		return new AIRUpdateRequestForm(param);
	}

	@Override
	public boolean shouldExecute(String sysPrefix) {
		if(sysPrefix!=null&&sysPrefix.equals(AIR_PREFIX))
			return true;
		return false;
	}

}
