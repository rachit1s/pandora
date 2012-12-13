package hse.com.tbitsGlobal.client.plugins;

import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_PREFIX;
import hse.com.tbitsGlobal.client.forms.AIRAddRequestForm;

import com.tbitsGlobal.jaguar.client.plugins.IAddRequestFormPlugin;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractAddRequestForm;
public class AIRAddRequestFormPlugin implements IAddRequestFormPlugin {

	@Override
	public AbstractAddRequestForm getWidget(UIContext param) {
		// TODO Auto-generated method stub
		return new AIRAddRequestForm(param);
	}

	@Override
	public boolean shouldExecute(String sysPrefix) {
		// TODO Auto-generated method stub
		if(sysPrefix!=null&&sysPrefix.equals(AIR_PREFIX))
			return true;
		return false;
	}

}
