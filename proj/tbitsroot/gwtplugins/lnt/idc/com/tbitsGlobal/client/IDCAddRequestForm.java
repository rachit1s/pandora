package idc.com.tbitsGlobal.client;

import com.tbitsGlobal.jaguar.client.widgets.forms.AddRequestForm;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;

public class IDCAddRequestForm extends AddRequestForm {

	protected IDCAddRequestForm(UIContext parentContext) {
		super(parentContext);

	}

	protected boolean hasBAFieldPermission(BAField bafield) {
		return bafield.isCanAddInBA();
	}

	protected void onAfterLayout() {
		String html = this.getData().getRequestModel().get(IFixedFields.DESCRIPTION);
		this.editor.setHTML(html);
	}

	
	protected boolean shouldFillField(BAField baField) {
		return baField.isSetEnabled();
	}

}
