package commons.com.tbitsGlobal.utils.client.Events;

import commons.com.tbitsGlobal.utils.client.widgets.UserPicker;


public class OnUserFieldControlValueChange extends TbitsBaseEvent {
	private UserPicker control;
	
	public OnUserFieldControlValueChange(UserPicker control) {
		super();
		this.control = control;
	}

	public void setControl(UserPicker control) {
		this.control = control;
	}

	public UserPicker getControl() {
		return control;
	}
}
