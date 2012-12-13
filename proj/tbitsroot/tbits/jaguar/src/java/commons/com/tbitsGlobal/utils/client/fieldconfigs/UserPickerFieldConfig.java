package commons.com.tbitsGlobal.utils.client.fieldconfigs;

import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOString;
import commons.com.tbitsGlobal.utils.client.widgets.UserPicker;

/**
 * 
 * @author sutta
 * 
 * for User Type fields
 */
public class UserPickerFieldConfig extends BaseFieldConfig<String, UserPicker>{
	
	public UserPickerFieldConfig(BAFieldMultiValue baField) {
		super(baField);
	    
	    field = new UserPicker(baField);
	    field.setName(baField.getName());
    	field.setFieldLabel(baField.getDisplayName());
    	field.setLabelStyle("font-weight:bold");
	}
	
	public UserPickerFieldConfig(BAFieldMultiValue baField, UserPicker up) {
		super(baField);
	    
	    field = up;
	    field.setName(baField.getName());
    	field.setFieldLabel(baField.getDisplayName());
    	field.setLabelStyle("font-weight:bold");
	}

	public void setWidget(UserPicker w)
	{
		this.field = w;
	}
	
	@SuppressWarnings("unchecked")
	public POJOString getPOJO() {
		if(field.getStringValue() != null)
			return new POJOString(field.getStringValue());
		return null;
	}

	public <T extends POJO<String>> void setPOJO(T pojo) {
		field.setStringValue(pojo.getValue());
	}

	public void clear() {
		field.setValue(null);
	}
	
//	public UserPicker getWidget() {
//		return field;
//	}
}
