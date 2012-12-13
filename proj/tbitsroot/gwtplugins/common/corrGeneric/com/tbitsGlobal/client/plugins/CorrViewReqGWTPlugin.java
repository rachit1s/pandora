package corrGeneric.com.tbitsGlobal.client.plugins;

import static corrGeneric.com.tbitsGlobal.shared.CorrConst.*;

import com.tbitsGlobal.jaguar.client.plugins.IViewRequestFormPlugin;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractViewRequestForm;

import corrGeneric.com.tbitsGlobal.client.forms.CorrViewReqForm;
import corrGeneric.com.tbitsGlobal.client.utils.CorrHelperClient;
import corrGeneric.com.tbitsGlobal.shared.CorrConst;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class CorrViewReqGWTPlugin implements IViewRequestFormPlugin
{
	public AbstractViewRequestForm getWidget(UIContext param) {
		return new CorrViewReqForm(param);
	}

	public boolean shouldExecute(final String sysPrefix) 
	{
		if( null == sysPrefix )
			return false;
		
		if( null == applicableBas )
		{
			CorrHelperClient.initializeCorrApplicableBAs(true) ;// .initializeViewRequestOptions(true);
		}		
		
		if( null == applicableBas ) //transferToAppBas || null == CorrConst.sendMeEmail || null == CorrConst.statusFieldName)
		{
			TbitsInfo.write("The Correspondence module was not initialized properly.Please consider opening this request again.", TbitsInfo.ERROR);
		}
		else if( applicableBas.contains(sysPrefix) ) // || (null != sendMeEmail.get(sysPrefix) && sendMeEmail.get(sysPrefix).equals(GenericParams.SendMeEmail_Yes)))
			return true; 
		
		return false;
	}

}
