package commons.com.tbitsGlobal.utils.client.fieldconfigs;

import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOString;
import commons.com.tbitsGlobal.utils.client.widgets.TypeFieldControl;

/**
 * 
 * @author sutta
 * 
 * for type fields
 */
public class TypeFieldConfig extends BaseFieldConfig<String, TypeFieldControl>{
	
	public TypeFieldConfig(BAFieldCombo baField) {
		super(baField);
		
		field = new TypeFieldControl(baField);
		field.setName(baField.getName());
    	field.setFieldLabel(baField.getDisplayName());
    	field.setLabelStyle("font-weight:bold");
	}

	@SuppressWarnings("unchecked")
	public POJOString getPOJO() {
		if(field.getValue() == null)
			return null;
		return new POJOString(field.getStringValue());
	}

	public String getValue() {
		return field.getStringValue();
	}

	public <T extends POJO<String>> void setPOJO(T pojo) {
		field.setStringValue(pojo.getValue());
	}

	public void setValue(String value) {
		field.setStringValue(value);
	}
	
	public void clear() {
		field.setValue(((BAFieldCombo) baField).getDefaultValue());
	}
}
