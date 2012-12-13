package com.tbitsGlobal.admin.client.permTool;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IRequestFormData;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractAddRequestForm;

/**
 * Extension of AbstractAddRequestForm to display the add form in the permissioning tool view.
 * 
 * @author Karan Gupta
 *
 */
public class RequestAddPT extends AbstractAddRequestForm{

	private IRequestFormData data;
	
	public RequestAddPT(UIContext parentContext) {
		super(parentContext);
		
		setDbOperationsEnabled(false);
		
		TbitsTreeRequestData requestModel = parentContext.getValue(PTConstants.REQUEST_MODEL, TbitsTreeRequestData.class);
		
		if(requestModel != null)
			data = new RequestFormDataPT(requestModel);
	}

	protected boolean shouldFillField(BAField baField) {
		return baField.isSetEnabled();
	}

	public IRequestFormData getData() {
		return data;
	}

	protected boolean hasBAFieldPermission(BAField bafield) {
		return bafield.isCanAddInBA() ;
	}

}
