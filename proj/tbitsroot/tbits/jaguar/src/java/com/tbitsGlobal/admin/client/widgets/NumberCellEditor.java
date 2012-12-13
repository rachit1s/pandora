package com.tbitsGlobal.admin.client.widgets;

import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;

public class NumberCellEditor extends CellEditor{

	public NumberCellEditor(TextField<String> field) {
		super(field);
	}
	
	@Override
	public Object preProcessValue(Object value) {
		if(value != null && value instanceof Integer)
			return value + "";
		return "";
	}
	
	@Override
	public Object postProcessValue(Object value) {
		if(value != null){
			try{
				return Integer.parseInt((String)value);
			}catch(Exception e){}
		}
		return value;
	}

}
