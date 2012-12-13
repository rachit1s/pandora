package commons.com.tbitsGlobal.utils.client.fieldconfigs;

import java.util.Date;

import commons.com.tbitsGlobal.utils.client.bafield.BAFieldDate;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJODate;
import commons.com.tbitsGlobal.utils.client.widgets.DateTimeControl;

/**
 * 
 * @author sutta
 * 
 * for date fields
 */
public class DateFieldConfig extends BaseFieldConfig<Date, DateTimeControl>{
	
	public DateFieldConfig(BAFieldDate baField) {
		super(baField);
		
		field = new DateTimeControl();
		field.setName(baField.getName());
    	field.setFieldLabel(baField.getDisplayName());
    	field.setLabelStyle("font-weight:bold");
    	
    	if(baField.getDateFormat() != null && !baField.getDateFormat().equals(""))
    		field.setFormat(baField.getDateFormat());
	}

	@SuppressWarnings("unchecked")
	public POJODate getPOJO() {
		if(field.getValue() == null)
			return null;
		return new POJODate(field.getValue());
	}

	public Date getValue() {
		return field.getValue();
	}

	public <T extends POJO<Date>> void setPOJO(T pojo) {
		field.setValue(pojo.getValue());
	}

	public void setValue(Date value) {
		field.setValue(value);
	}
	
	public void clear() {
		field.setValue(null);
	}

//	public DateFieldControl getWidget() {
//		return field;
//	}
}
