/**
 * 
 */
package dcn.com.tbitsGlobal.client.plugins;

import com.tbitsGlobal.jaguar.client.plugins.IAddRequestFormPlugin;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractAddRequestForm;

import dcn.com.tbitsGlobal.client.ChangeNoteConstants;
import dcn.com.tbitsGlobal.client.plugins.form.ChangeNoteAddRequestForm;
import dcn.com.tbitsGlobal.client.utils.ChangeNoteClientUtils;

/**
 * @author Lokesh
 *
 */
public class ChangeNoteAddRequestFormPlugin implements IAddRequestFormPlugin {
	
//	ArrayList<ChangeNoteConfig> changeNoteConfigList;
	
	public ChangeNoteAddRequestFormPlugin(){}

	@Override
	public AbstractAddRequestForm getWidget(UIContext param) {
		return new ChangeNoteAddRequestForm(param);
	}

	/* (non-Javadoc)
	 * @see commons.com.tbitsGlobal.utils.client.plugins.IGWTPlugin#shouldExecute(java.lang.String)
	 */
	@Override
	public boolean shouldExecute(String sysPrefix) {
		if ((sysPrefix != null) && (sysPrefix.trim().equals(sysPrefix))){
			if (ChangeNoteClientUtils.isExistsInListAsSourceBA(ChangeNoteConstants.changeNoteConfigList, sysPrefix))
				return true;
		}
		return false;
	}	
}
