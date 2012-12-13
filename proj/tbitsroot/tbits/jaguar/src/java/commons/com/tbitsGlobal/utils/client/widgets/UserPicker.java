package commons.com.tbitsGlobal.utils.client.widgets;

import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

/**
 * 
 * @author sourabh
 * 
 * A control that provides multiselection facility for Users
 * 
 * The User list appears according to the following property of user type field : 
 * 
 * 	key : user_filter
	value :
	Pattern :
		        whitespace or "" means all users
		       	+-UserTypePattern; +-RolePattern; +-UserPattern;
		UserTypePattern:
				usertype:user_type_id,...; UserTypePattern
		RolePattern:
		        role:role_id,...; RolePattern
		UserPattern:
		        user:user_id,... ; UserPattern

 */
public class UserPicker extends ComboBox<UserClient>{
	
	private String suspendedStringValue = null;
	
	public UserPicker(BAFieldMultiValue baField) {
		super();
		
		this.setDisplayField(UserClient.USER_LOGIN);
		
		this.setTemplate(getTemplateString(UserClient.USER_LOGIN, UserClient.EMAIL));
		
		this.setTriggerAction(TriggerAction.QUERY);
		this.setMinChars(1);
		this.setMinListWidth(100);
		
		this.setStore(new ListStore<UserClient>(UserPickerListLoader.getloader(baField)));
	}
	
	public UserPicker(List<? extends UserClient> userList) {
		super();
		
		this.setDisplayField(UserClient.USER_LOGIN);
		
		this.setTemplate(getTemplateString(UserClient.USER_LOGIN, UserClient.EMAIL));
		
		this.setTriggerAction(TriggerAction.QUERY);
		this.setMinChars(1);
		this.setMinListWidth(100);
		this.setStore(new ListStore<UserClient>());
		this.getStore().add(userList);
	}
	
	@Override
	public String getRawValue() {
		String rawValue =  super.getRawValue();
		rawValue = rawValue.substring(rawValue.lastIndexOf(',') + 1).trim();
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
	public void setValue(UserClient value) {
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
