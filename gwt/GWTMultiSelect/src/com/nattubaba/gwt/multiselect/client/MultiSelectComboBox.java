package com.nattubaba.gwt.multiselect.client;

import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

class MultiSelectModelData extends BaseModelData
{
	public MultiSelectModelData(String name)
	{
		setName(name);
	}
	
	public static final String NAME = "NAME";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1086050967173537990L;
	
	public void setName(String name)
	{
		this.set(NAME, name);
	}
	
	public String getName()
	{
		return this.get(NAME);
	}
	
}
public class MultiSelectComboBox extends ComboBox<MultiSelectModelData>{
	
	private String suspendedStringValue = null;
	
	public MultiSelectComboBox(List<MultiSelectModelData> list) {
		super();
		
		this.setDisplayField(MultiSelectModelData.NAME);
		
		this.setTemplate(getTemplateString(MultiSelectModelData.NAME, MultiSelectModelData.NAME));
		
		this.setTriggerAction(TriggerAction.ALL);
		this.setMinChars(1);
		this.setMinListWidth(100);
		ListStore<MultiSelectModelData> listStore = new ListStore<MultiSelectModelData>();
		listStore.add(list);
		this.setStore(listStore);
	}
	
//	public UserPicker(List<? extends UserClient> userList) {
//		super();
//		
//		this.setDisplayField(UserClient.USER_LOGIN);
//		
//		this.setTemplate(getTemplateString(UserClient.USER_LOGIN, UserClient.EMAIL));
//		
//		this.setTriggerAction(TriggerAction.QUERY);
//		this.setMinChars(1);
//		this.setMinListWidth(100);
//		this.setStore(new ListStore<UserClient>());
//		this.getStore().add(userList);
//	}
	
	@Override
	public String getRawValue() {
		String rawValue =  super.getRawValue();
		int index = rawValue.lastIndexOf(',');
		if( index != -1)
			rawValue = rawValue.substring(0,index).trim();
		return rawValue;
	}
	
	@Override
	public void setRawValue(String text) {
		if(text == null || text.equals("null"))
			return;
		String rawValue = super.getRawValue();
		int index = rawValue.lastIndexOf(',');
		if(index != -1)
			rawValue = rawValue.substring(0, index + 1);
		else
			rawValue = "";
		if((rawValue + text).equals("")){
			super.setRawValue("");
			return;
		}
		super.setRawValue(rawValue + text + ",");
	}
	
	@Override
	protected void removeEmptyText() {
		if (rendered) {
	      getInputEl().removeStyleName(emptyStyle);
	      if (super.getRawValue().equals("") || super.getRawValue().equals(",")) {
	    	  super.setRawValue("");
	      }
	    }
	}
	
	@Override
	public void setValue(MultiSelectModelData value) {
		if(value == null)
			super.setRawValue("");
		else
			super.setValue(value);
	}
	
	public String getStringValue(){
		String rawValue = super.getRawValue();
		int index = rawValue.lastIndexOf(',');
		if(index != -1)
			rawValue = rawValue.substring(0, index + 1);
		else
			rawValue = "";
		
		return rawValue;
	}
	
	public void setStringValue(String value){
		if(value != null && !value.equals("") && value.lastIndexOf(',') != value.length() - 1)
			value += ",";
		if(rendered){
			super.setRawValue(value);
		}else
			suspendedStringValue = value;
	}
	
	private native String getTemplateString(String displayName, String email) /*-{ 
		return  [ 
		'<tpl for=".">', 
		'<div class="x-combo-list-item">{', displayName, '}<{', email, '}></div>', 
		'</tpl>' 
		].join(""); 
	}-*/;
	
	@Override
	protected void afterRender() {
		super.afterRender();
		
		if(suspendedStringValue != null)
			this.setStringValue(suspendedStringValue);
		suspendedStringValue = null;
	}
}
