package com.tbitsGlobal.jaguar.client.bulkupdate;

import com.extjs.gxt.ui.client.store.ListStore;

import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportWindow.Parser;
import commons.com.tbitsGlobal.utils.client.cellEditors.TypeFieldEditor;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.widgets.TypeFieldControl;

/**
 * 
 * @author sourabh
 * 
 * A Parser for Type Field values during excel import
 * 
 * Can parse following things in priority order :
 * 1. Type Name
 * 2. Type Display Name
 * 3. Type Id 
 */
public class TypeFieldParser implements Parser{

	private TypeFieldEditor editor;
	
	public TypeFieldParser(TypeFieldEditor editor) {
		this.editor = editor;
	}
	
	public Object parse(String value) {
		TypeFieldControl control = editor.getTypeFieldControl();
		ListStore<TypeClient> store = control.getStore();
		
		TypeClient type = store.findModel(TypeClient.NAME, value);
		if(type != null)
			return type.getName();
		
		type = store.findModel(TypeClient.DISPLAY_NAME, value);
		if(type != null)
			return type.getName();
		
		try{
			type = store.findModel(TypeClient.TYPE_ID, Integer.parseInt(value));
			if(type != null)
				return type.getName();
		}catch(Exception e){}
		
		return null;
	}

}
