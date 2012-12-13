package commons.com.tbitsGlobal.utils.client.fieldconfigs;

import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCheckBox;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOBoolean;

/**
 * 
 * @author sutta
 * 
 * Shows a drop down with Yes/No options for a bit field
 */
public class BooleanComboFieldConfig  extends BaseFieldConfig<Boolean, SimpleComboBox<String>>{

	private final String TRUE = "Yes";
	private final String FALSE = "No";
	
	public BooleanComboFieldConfig(BAFieldCheckBox baField) {
		super(baField);
		
		field = new SimpleComboBox<String>();
		field.setName(baField.getName());
    	field.setFieldLabel(baField.getDisplayName());
    	field.setLabelStyle("font-weight:bold");
    	
    	field.add(TRUE);
    	field.add(FALSE);
	}

	
	public void clear() {
		
	}

	
	public <T extends POJO<Boolean>> T getPOJO() {
		if(field.getSimpleValue() == null)
			return null;
		return (T) new POJOBoolean(field.getSimpleValue().equals(TRUE) ? true : false);
	}

	
	public <T extends POJO<Boolean>> void setPOJO(T pojo) {
		field.setSimpleValue(pojo.getValue() ? TRUE : FALSE);
	}

}
