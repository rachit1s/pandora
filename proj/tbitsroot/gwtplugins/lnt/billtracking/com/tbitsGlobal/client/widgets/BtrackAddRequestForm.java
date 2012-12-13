package billtracking.com.tbitsGlobal.client.widgets;

import billtracking.com.tbitsGlobal.shared.IBillConstants;

import com.extjs.gxt.ui.client.store.ListStore;
import com.tbitsGlobal.jaguar.client.widgets.forms.AddRequestForm;

import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.IFieldConfig;

public class BtrackAddRequestForm extends AddRequestForm implements IBillConstants{

	public BtrackAddRequestForm(UIContext parentContext) {
		super(parentContext);
	}

	@Override
	protected void afterRender() {
		super.afterRender();
		hideField(Project);
		hideField(Subject);
		hideField(IFixedFields.ATTACHMENTS);
	}



	/**
	 * 
	 * hides the field in add request form 
	 * back-end functionality remains the same
	 */
	private void hideField(String fieldName) {
		IFieldConfig config=fieldConfigs.get(fieldName);
		//	((TypeFieldConfig)config).getWidget().setVisible(false);
		config.getWidget().setVisible(false);
		ListStore<BAField> baFieldStore=this.getData().getBAFields();
		BAField field = baFieldStore.findModel(BAField.NAME,fieldName);
		this.reDrawField(field, config);
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
