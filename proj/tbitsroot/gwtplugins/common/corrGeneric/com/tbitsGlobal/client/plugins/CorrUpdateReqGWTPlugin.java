package corrGeneric.com.tbitsGlobal.client.plugins;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.plugins.IAddRequestFormPlugin;
import com.tbitsGlobal.jaguar.client.plugins.IUpdateRequestFormPlugin;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractAddRequestForm;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractUpdateRequestForm;
import corrGeneric.com.tbitsGlobal.client.forms.CorrAddReqForm;
import corrGeneric.com.tbitsGlobal.client.forms.CorrUpdateReqForm;
import corrGeneric.com.tbitsGlobal.client.utils.CorrHelperClient;
import corrGeneric.com.tbitsGlobal.shared.CorrConst;

import static corrGeneric.com.tbitsGlobal.shared.CorrConst.*;

public class CorrUpdateReqGWTPlugin implements IUpdateRequestFormPlugin
{
	public AbstractUpdateRequestForm getWidget(UIContext param) {
		return new CorrUpdateReqForm(param);
	}

	public boolean shouldExecute(final String sysPrefix) 
	{
		if( null == sysPrefix )
			return false;
		
		if( null == CorrConst.applicableBas )
		{
			CorrHelperClient.initializeCorrApplicableBAs(true);
		}		
		
		if( null == applicableBas )
		{
			TbitsInfo.info("The Correspondence module was not initialized properly.Please try Reloading this tab.");
		}
		else if( applicableBas.contains(sysPrefix))
			return true; 
		
		return false;
	}

}
