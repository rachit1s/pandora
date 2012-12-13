package hse.com.tbitsGlobal.client.forms;

import com.tbitsGlobal.jaguar.client.widgets.forms.UpdateRequestForm;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.PermissionClient;

public class AIRUpdateRequestForm extends UpdateRequestForm {

	public AIRUpdateRequestForm(UIContext parentContext) {
		super(parentContext);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean shouldFillField(BAField baField) {
		// TODO Auto-generated method stub
		return baField.isSetEnabled();
	}

	@Override
	protected boolean hasBAFieldPermission(BAField bafield) {
		// TODO Auto-generated method stub
		int perm = 0;
		TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		if(requestModel.getPerms().containsKey(bafield.getName()))
			perm = requestModel.getPerms().get(bafield.getName());

		return ((perm & PermissionClient.CHANGE) != 0) && bafield.isCanUpdate() ;
	}

}
