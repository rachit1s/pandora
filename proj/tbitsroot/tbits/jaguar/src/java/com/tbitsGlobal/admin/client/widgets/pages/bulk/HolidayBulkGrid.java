package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.Date;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Time;
import com.extjs.gxt.ui.client.widget.form.TimeField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.tbitsGlobal.admin.client.modelData.HolidayClient;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;

public class HolidayBulkGrid extends BulkUpdateGridAbstract<HolidayClient>{
	private String format = "MM/dd/yyyy";
	
	public HolidayBulkGrid(BulkGridMode mode) {
		super(mode);
		
		canRemoveRow = false;
		showStatus = false;
	}

	protected void createColumns() {
		ColumnConfig officeCol = new ColumnConfig(HolidayClient.OFFICE, "Office", 200);
		officeCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(officeCol);
		
		ColumnConfig dateCol = new ColumnConfig(HolidayClient.DATE, "Date", 200);
		DateField dateField = new DateField();
		dateField.getDatePicker().addListener(Events.Select, new Listener<BaseEvent>(){
			public void handleEvent(BaseEvent be) {
				//do nothing
			}
		});
		CellEditor dateColEditor = new TbitsCellEditor(dateField){
			public Object postProcessValue(Object value) {
				return DateTimeFormat.getFormat(format).format((Date) value);
			}
		};
		
		dateColEditor.setCancelOnEsc(true);
		dateColEditor.setCompleteOnEnter(true);
		dateCol.setEditor(dateColEditor);
		dateCol.setDateTimeFormat(DateTimeFormat.getFormat(format));
		cm.getColumns().add(dateCol);
		
		ColumnConfig zoneCol = new ColumnConfig(HolidayClient.ZONE, "Zone", 200);
		zoneCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(zoneCol);
		
		ColumnConfig descCol = new ColumnConfig(HolidayClient.DESCRIPTION, "Description", 200);
		descCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(descCol);
	}

}
