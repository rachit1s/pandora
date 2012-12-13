package commons.com.tbitsGlobal.utils.client.fieldconfigs;

import com.extjs.gxt.ui.client.widget.form.AdapterField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldTextArea;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOString;
import commons.com.tbitsGlobal.utils.client.widgets.forms.RTEditor;

/**
 * 
 * @author sutta
 * 
 * shows RTE
 */
public class TextAreaFieldConfig extends BaseFieldConfig<String, AdapterField> {
	private RTEditor editor;
	
	public TextAreaFieldConfig(BAFieldTextArea baField) {
		super(baField);
		
		editor = new RTEditor();
		field = new AdapterField(editor);
		field.setName(baField.getName());
    	field.setFieldLabel(baField.getDisplayName());
    	field.setLabelStyle("font-weight:bold");
	}

	@SuppressWarnings("unchecked")
	public POJOString getPOJO() {
		if(getValue() == null)
			return null;
		return new POJOString(getValue());
	}

	public String getValue() {
		return editor.getHTML();
	}

	public <T extends POJO<String>> void setPOJO(T pojo) {
		setValue(pojo.getValue());
	}

	public void setValue(String value) {
		editor.setHTML(value);
	}
	
	public void clear() {
		editor.setHTML("");
	}
	
//	public AdapterField getWidget() {
//		return field;
//	}
}
