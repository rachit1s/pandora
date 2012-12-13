package commons.com.tbitsGlobal.utils.client.fieldconfigs;

import com.extjs.gxt.ui.client.widget.form.LabelField;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldDate;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJODate;

/**
 * 
 * @author sutta
 *
 * @param <D>
 * 
 * shows a label
 */
public class LabelFieldConfig<D> extends BaseFieldConfig<D, LabelField> {
	
	public LabelFieldConfig(BAField baField) {
		super(baField);
		
		field = new LabelField();
		field.setName(baField.getName());
    	field.setFieldLabel(baField.getDisplayName());
    	field.setLabelStyle("font-weight:bold");
	}

	public void clear() {
		field.setValue("");
	}

	public <T extends POJO<D>> T getPOJO() {
		return null;
	}

//	public LabelField getWidget() {
//		return field;
//	}

	public <T extends POJO<D>> void setPOJO(T pojo) {
		if(baField instanceof BAFieldCombo){
			String value = pojo.toString();
			if(value != null){
				TypeClient type = ((BAFieldCombo) baField).getModelForName(value);
				if(type != null)
					field.setValue(type.getDisplayName());
			}
		}else if(baField instanceof BAFieldDate){
			POJODate pojoDate = (POJODate) pojo;
			String format = ClientUtils.getCurrentUser().getWebDateFormat();
			if(format != null)
				pojoDate.setFormat(format);
			field.setValue(pojoDate.toString());
		}else if(baField instanceof BAFieldMultiValue){
			String value = pojo.toString();
			value = value.replaceAll(",", ", ");
			field.setValue(value);
		}else
			field.setValue(pojo.toString());
	}

}
