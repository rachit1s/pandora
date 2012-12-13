package com.tbitsGlobal.admin.client.permTool;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IRequestFormData;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractViewRequestForm;

/**
 * Extension of AbstractViewRequestForm to display the add form in the permissioning tool view.
 * 
 * @author Karan Gupta
 *
 */
public class RequestViewPT extends AbstractViewRequestForm {

	private IRequestFormData data;
	
	public RequestViewPT(UIContext parentContext) {
		super(parentContext);
		
		TbitsTreeRequestData requestModel = parentContext.getValue(PTConstants.REQUEST_MODEL, TbitsTreeRequestData.class);
		
		if(requestModel != null)
			data = new RequestFormDataPT(requestModel);
	}

	public IRequestFormData getData() {
		return data;
	}

	public void refresh() {
		// TODO Auto-generated method stub
	}

	public void registerReadAction() {
		// TODO Auto-generated method stub
	}

}
