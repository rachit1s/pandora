package com.tbitsGlobal.jaguar.client.widgets.forms;

import com.google.gwt.user.client.Element;
import com.tbitsGlobal.jaguar.client.events.ToViewRequest;
import com.tbitsGlobal.jaguar.client.widgets.ActionHistoryPanel;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.ToViewRequestOtherBA;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.PermissionClient;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IRequestFormData;
import commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractUpdateRequestForm;

/**
 * 
 * @author sourabh
 * 
 * Form used to update requests
 */
public class UpdateRequestForm extends AbstractUpdateRequestForm {

	private IRequestFormData data;
	protected ActionHistoryPanel actionPanel;

	public UpdateRequestForm(UIContext parentContext) {
		super(parentContext);

		data = new DefaultRequestFormData(parentContext);
	}

	@Override
	protected boolean hasBAFieldPermission(BAField bafield) 
	{
		int perm = 0;
		
		TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		if(requestModel != null && requestModel.getPerms().containsKey(bafield.getName()))
			perm = requestModel.getPerms().get(bafield.getName());
		
		return ((perm & PermissionClient.CHANGE) != 0) && bafield.isCanUpdate() ;
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

	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);

		TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		String sysPrefix = this.getData().getSysPrefix();
		if(requestModel != null && sysPrefix != null){
			actionPanel = new ActionHistoryPanel(sysPrefix, requestModel.getRequestId(), this.getData().getBAFields());
			this.add(actionPanel);
		}

	}
}
