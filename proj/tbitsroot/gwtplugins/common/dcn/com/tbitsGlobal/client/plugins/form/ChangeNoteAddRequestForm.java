/**
 * 
 */
package dcn.com.tbitsGlobal.client.plugins.form;


import com.tbitsGlobal.jaguar.client.widgets.forms.AddRequestForm;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;

/**
 * @author lokesh
 *
 */
public class ChangeNoteAddRequestForm extends AddRequestForm {

	public ChangeNoteAddRequestForm(UIContext parentContext) {
		super(parentContext);
	}

	/* (non-Javadoc)
	 * @see com.tbitsGlobal.jaguar.client.widgets.forms.AbstractEditRequestForm#shouldFillField(commons.com.tbitsGlobal.utils.client.bafield.BAField)
	 */
	@Override
	protected boolean shouldFillField(BAField baField) {
		return baField.getName().equals(IFixedFields.RELATED_REQUESTS);
	}

	/* (non-Javadoc)
	 * @see com.tbitsGlobal.jaguar.client.widgets.forms.AbstractRequestForm#hasBAFieldPermission(commons.com.tbitsGlobal.utils.client.bafield.BAField)
	 */
	@Override
	protected boolean hasBAFieldPermission(BAField bafield) {
		return bafield.isCanAddInBA();
	}

}
