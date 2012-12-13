package commons.com.tbitsGlobal.utils.client.fieldconfigs;

import com.extjs.gxt.ui.client.widget.form.NumberField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldInt;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOInt;

/**
 * 
 * @author sutta
 * 
 * for int fields
 */
public class NumberFieldConfig extends BaseFieldConfig<Integer, NumberField>{

	public NumberFieldConfig(BAFieldInt baField) {
		super(baField);
		
		field = new NumberField();
		field.setName(baField.getName());
    	field.setFieldLabel(baField.getDisplayName());
    	field.setLabelStyle("font-weight:bold");
	}
	
	@SuppressWarnings("unchecked")
	public POJOInt getPOJO() {
		if(field.getValue() == null)
			return null;
		return new POJOInt(field.getValue().intValue());
	}

	public Integer getValue() {
		return field.getValue().intValue();
	}

	public <T extends POJO<Integer>> void setPOJO(T pojo) {
		field.setValue(pojo.getValue());
	}

	public void setValue(Integer value) {
		field.setValue(value);
	}
	
	public void clear() {
		field.setValue(null);
	}
	
//	public NumberField getWidget() {
//		return field;
//	}

}
