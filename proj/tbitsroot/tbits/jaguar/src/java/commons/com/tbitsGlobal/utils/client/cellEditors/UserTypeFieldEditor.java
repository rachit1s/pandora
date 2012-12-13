package commons.com.tbitsGlobal.utils.client.cellEditors;

import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.widgets.UserPicker;

/**
 * 
 * @author sourabh
 * 
 * A cell editor for user type fields
 */
public class UserTypeFieldEditor extends CellEditor {

	public UserTypeFieldEditor(UserPicker field) {
		super(field);
		
		this.setCancelOnEsc(true);
		this.setCompleteOnEnter(true);
	}
	
	@Override
	public Object preProcessValue(Object value) {
		UserPicker control = (UserPicker) this.getField();
		control.setStringValue((String) value);
		return new UserClient();
	}
	
	@Override
	public Object postProcessValue(Object value) {
		UserPicker control = (UserPicker) this.getField();
		String controlValue = control.getStringValue();
		control.setValue(null);
		return controlValue;
	}

}
