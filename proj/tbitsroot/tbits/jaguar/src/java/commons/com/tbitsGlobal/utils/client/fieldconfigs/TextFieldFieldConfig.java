package commons.com.tbitsGlobal.utils.client.fieldconfigs;

import com.extjs.gxt.ui.client.widget.form.TextField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldString;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOString;

/**
 * 
 * @author sutta
 * 
 * shows a text box
 */
public class TextFieldFieldConfig extends BaseFieldConfig<String, TextField<String>>{
	
	public TextFieldFieldConfig(BAFieldString baField) {
		super(baField);
		
		field = new TextField<String>();
		field.setName(baField.getName());
    	field.setFieldLabel(baField.getDisplayName());
    	field.setLabelStyle("font-weight:bold");
	}
	
	@SuppressWarnings("unchecked")
	public POJOString getPOJO() {
		if(field.getValue() == null)
			return null;
		return new POJOString(field.getValue());
	}

	public String getValue() {
		return field.getValue();
	}

	public <T extends POJO<String>> void setPOJO(T pojo) {
		field.setValue(pojo.getValue());
	}

	public void setValue(String value) {
		field.setValue(value);
	}
	
	public void clear() {
		field.setValue(null);
	}
	
//	public TextField<String> getWidget() {
//		return field;
//	}	
}
