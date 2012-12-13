package com.tbitsGlobal.jaguar.client.widgets.forms;

import com.tbitsGlobal.jaguar.client.events.ToViewRequest;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.ToViewRequestOtherBA;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IRequestFormData;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractAddRequestForm;

/**
 * 
 * @author sourabh
 * 
 * Form to be used adding requests
 */
public class AddRequestForm extends AbstractAddRequestForm{

	private IRequestFormData data;
	
	public AddRequestForm(UIContext parentContext) {
		super(parentContext);
		
		data = new DefaultRequestFormData(parentContext);
	}

	@Override
	protected boolean hasBAFieldPermission(BAField bafield) {
		return bafield.isCanAddInBA() ;
	}

	@Override
	protected boolean shouldFillField(BAField baField) {
		return isSourceDraft ? true : baField.isSetEnabled();
	}
	
	@Override
	public void afterSubmit(String sysPrefix, int requestId) {
		super.afterSubmit(sysPrefix, requestId);
		
		if(sysPrefix != null){
			if(sysPrefix.equals(ClientUtils.getSysPrefix()))
				TbitsEventRegister.getInstance().fireEvent(new ToViewRequest(requestId));
			else
				TbitsEventRegister.getInstance().fireEvent(new ToViewRequestOtherBA(sysPrefix, requestId));
		}
	}
	@Override
	protected void onAfterLayout() {
		super.onAfterLayout();
		if(myContext.hasKey(IRequestFormData.CONTEXT_DRAFT)){
			this.editor.setHTML(this.getData().getRequestModel().getAsString(IFixedFields.DESCRIPTION));
		}
	}
	@Override
	public IRequestFormData getData() {
		return data;
	}
}
