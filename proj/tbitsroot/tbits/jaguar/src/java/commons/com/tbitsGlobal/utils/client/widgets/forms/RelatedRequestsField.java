package commons.com.tbitsGlobal.utils.client.widgets.forms;

import com.extjs.gxt.ui.client.widget.form.AdapterField;

public class RelatedRequestsField extends AdapterField{

	private BrowseFieldWidget fieldWidget;
	
	public RelatedRequestsField(BrowseFieldWidget widget) {
		super(widget);
		
		this.fieldWidget = widget;
	}

	public String getStringValue() {
		return fieldWidget.getStringValue();
	}

	public void setStringValue(String value) {
		fieldWidget.setStringValue(value);
	}
}
