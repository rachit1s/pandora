package hse.com.tbitsGlobal.client.forms;

import com.tbitsGlobal.jaguar.client.widgets.forms.AddRequestForm;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;

public class AIRAddRequestForm extends AddRequestForm {

	public AIRAddRequestForm(UIContext parentContext) {
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
		return bafield.isCanAddInBA();
	}

}
