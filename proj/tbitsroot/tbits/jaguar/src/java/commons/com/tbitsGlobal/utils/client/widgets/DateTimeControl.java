package commons.com.tbitsGlobal.utils.client.widgets;

import java.util.Date;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DomEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.DatePicker;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.google.gwt.user.client.Element;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;

public class DateTimeControl extends DateField{
	private DateTimeMenu menu;
	
	private String format = GlobalConstants.API_DATE_FORMAT;
	
	public DateTimeControl() {
		super();
	}
	
	@Override
	public DatePicker getDatePicker() {
		if (menu == null) {
			menu = new DateTimeMenu();

			menu.getDatePicker().addListener(Events.Select, new Listener<ComponentEvent>() {
				public void handleEvent(ComponentEvent ce) {
				    focusValue = getValue();
				    setValue(menu.getDate());
				    menu.hide();
				    el().blur();
			    }
			});
			menu.addListener(Events.Hide, new Listener<ComponentEvent>() {
			    public void handleEvent(ComponentEvent be) {
			    	focus();
			    }
			});
		}
		return menu.getDatePicker();
	}
	
	protected void expand() {
		DatePicker picker = getDatePicker();
		
	    Object v = getValue();
	    Date d = null;
	    if (v instanceof Date) {
	    	d = (Date) v;
	    } else {
	    	d = new Date();
	    }
	    picker.setValue(d, true);
	    picker.setMinDate(getMinValue());
	    picker.setMaxDate(getMaxValue());

	    menu.show(el().dom, "tl-bl?");
	    menu.focus();
	}

	protected void onDown(FieldEvent fe) {
	    fe.cancelBubble();
	    if (menu == null || !menu.isAttached()) {
	    	expand();
	    }
	}

	  @Override
	protected void onKeyPress(FieldEvent fe) {
	    super.onKeyPress(fe);
	    int code = fe.getKeyCode();
	    if (code == 8 || code == 9) {
		    if (menu != null && menu.isAttached()) {
		        menu.hide();
		    }
	    }
	}
	  
	protected boolean validateBlur(DomEvent e,Element target){
		return menu == null|| (menu != null && !menu.isVisible());
	}

	public void setFormat(String format) {
		this.format = format;
		this.setPropertyEditor(new DateTimePropertyEditor(format));
	}

	public String getFormat() {
		return format;
	}
}
