package com.tbitsGlobal.admin.client.permTool;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.PermissionClient;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IRequestFormData;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractUpdateRequestForm;

/**
 * Extension of AbstractUpdateRequestForm to display the update form in the permissioning tool view.
 * 
 * @author Karan Gupta
 *
 */
public class RequestUpdatePT extends AbstractUpdateRequestForm {

private IRequestFormData data;
	
	public RequestUpdatePT(UIContext parentContext) {
		super(parentContext);

		setDbOperationsEnabled(false);
		
		TbitsTreeRequestData requestModel = parentContext.getValue(PTConstants.REQUEST_MODEL, TbitsTreeRequestData.class);
		
		if(requestModel != null)
			data = new RequestFormDataPT(requestModel);
	}

	protected boolean hasBAFieldPermission(BAField bafield) 
	{
		int perm = 0;
		
		TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		if(requestModel != null && requestModel.getPerms().containsKey(bafield.getName()))
			perm = requestModel.getPerms().get(bafield.getName());
		
		return ((perm & PermissionClient.CHANGE) != 0) && bafield.isCanUpdate() ;
	}

	protected boolean shouldFillField(BAField baField) {
		return baField.isSetEnabled();
	}
	
	public IRequestFormData getData() {
		return data;
	}

}
