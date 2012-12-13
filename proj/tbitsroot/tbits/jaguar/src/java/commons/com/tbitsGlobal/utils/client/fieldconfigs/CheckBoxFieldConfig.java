package commons.com.tbitsGlobal.utils.client.fieldconfigs;

import com.extjs.gxt.ui.client.widget.form.CheckBox;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCheckBox;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOBoolean;

/**
 * 
 * @author sutta
 * 
 * for bit fields
 */
public class CheckBoxFieldConfig extends BaseFieldConfig<Boolean, CheckBox>{
	
	public CheckBoxFieldConfig(BAFieldCheckBox baField) {
		super(baField);
		field = new CheckBox();
		field.setName(baField.getName());
    	field.setFieldLabel(baField.getDisplayName());
    	field.setLabelStyle("font-weight:bold");
    	
		field.setBoxLabel("");
	}
	
	public Boolean getValue(){
		return field.getValue();
	}
	
	public void setValue(Boolean value){
		field.setValue(value);
	}
	
	@SuppressWarnings("unchecked")
	public POJOBoolean getPOJO(){
		if(this.getValue() == null)
			return null;
		return new POJOBoolean(this.getValue());
	}

	public <T extends POJO<Boolean>> void setPOJO(T pojo) {
		this.setValue(pojo.getValue());
	}
	
	public void clear() {
		field.setValue(((BAFieldCheckBox) baField).getDefaultValue());
	}
	
//	public CheckBox getWidget() {
//		return field;
//	}
}
