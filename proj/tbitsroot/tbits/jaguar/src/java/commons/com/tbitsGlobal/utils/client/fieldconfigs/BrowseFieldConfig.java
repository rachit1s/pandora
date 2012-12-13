package commons.com.tbitsGlobal.utils.client.fieldconfigs;

import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOString;
import commons.com.tbitsGlobal.utils.client.widgets.forms.RelatedRequestsField;
import commons.com.tbitsGlobal.utils.client.widgets.forms.BrowseFieldWidget;

/**
 * 
 * @author sutta
 * 
 * shows a {@link BrowseFieldWidget} 
 */
public class BrowseFieldConfig extends BaseFieldConfig<String, RelatedRequestsField> {

	public BrowseFieldConfig(BAField baField) {
		super(baField);
		
		BrowseFieldWidget widget = new BrowseFieldWidget();
		field = new RelatedRequestsField(widget);
		field.setName(baField.getName());
    	field.setFieldLabel(baField.getDisplayName());
    	field.setLabelStyle("font-weight:bold");
	}

	public void clear() {
		field.setValue(null);
	}

	public POJO<String> getPOJO() {
		if(field.getStringValue() == null)
			return null;
		return new POJOString(field.getStringValue());
	}

	public <T extends POJO<String>> void setPOJO(T pojo) {
		field.setStringValue(pojo.getValue());
	}

}
