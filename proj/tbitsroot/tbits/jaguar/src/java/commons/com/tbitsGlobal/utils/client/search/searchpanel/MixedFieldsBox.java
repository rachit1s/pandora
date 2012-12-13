package commons.com.tbitsGlobal.utils.client.search.searchpanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.Element;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCheckBox;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldInt;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldString;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldTextArea;
import commons.com.tbitsGlobal.utils.client.widgets.UserPicker;

/**
 * Box to provide search for various types of fields.
 * 
 * @author sourabh
 *
 */
public class MixedFieldsBox extends FormPanel implements ISearchBox, IFixedFields{
	
	public static final String READ_UNREAD_FIELD_FILTER = "__is_read";
	
	private ArrayList<SimpleComboBox<String>> checkFields;
	private ArrayList<UserPicker> multiValueFields;
	private ArrayList<TextField<String>> textAreaFields;
	private ArrayList<TextField<String>> textBoxFields;
	
	private List<BAField> fields;
	
	public MixedFieldsBox(List<BAField> fields) {
		super();
		
		this.setHeaderVisible(false);
		this.setBodyBorder(false);
		this.setStyleAttribute("borderBottom", "2px solid #99BBE8");
	
		this.fields = fields;
	}
	
	
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		resetToDefault();
	}
	
	private void createField(BAField baField){
		if(baField == null) return;
		if(baField instanceof BAFieldMultiValue){
		    UserPicker field = new UserPicker((BAFieldMultiValue) baField);
		    field.setName(baField.getName());
			field.setFieldLabel(baField.getDisplayName());
	    	this.add(field, new FormData("-10")); 
		    multiValueFields.add(field);
		}else if(baField instanceof BAFieldCheckBox){
			SimpleComboBox<String> field = new SimpleComboBox<String>();
			field.add("Any");
			field.add("True");
			field.add("False");
			field.setSimpleValue("Any");
			field.setName(baField.getName());
			field.setFieldLabel(baField.getDisplayName());
			this.add(field, new FormData("-10"));
			checkFields.add(field);
		}else if(baField instanceof BAFieldTextArea){
			TextField<String> field = new TextField<String>();
			field.setName(baField.getName());
			field.setFieldLabel(baField.getDisplayName());
			this.add(field, new FormData("-10"));
			textAreaFields.add(field);
		}else if( (baField instanceof BAFieldString) || (baField instanceof BAFieldInt)){
			TextField<String> field = new TextField<String>();
			field.setName(baField.getName());
			field.setFieldLabel(baField.getDisplayName());
			this.add(field, new FormData("-10"));
			textBoxFields.add(field);
		}
	}

	public HashMap<String, List<String>> getSearchParams(SearchParamType spt) {
		HashMap<String, List<String>> searchParams = new HashMap<String, List<String>>();
		
		switch(spt){
			case NON_TEXT : 
				for(UserPicker field : multiValueFields){
					String value = field.getStringValue();
					if(value == null)
						continue;
					ArrayList<String> values = new ArrayList<String>();
					String[] vals = value.split(",");
					for(String val : vals){
						if(!val.equals(""))
							values.add("\""+val.trim()+"\"");
					}
					if(values.size() > 0)
						searchParams.put(field.getName(), values);
				}
				for(SimpleComboBox<String> field : checkFields){
					String value = field.getSimpleValue();
					if(value != null && !value.equals("") && !value.toLowerCase().equals("any")){
						ArrayList<String> values = new ArrayList<String>();
						values.add(value.toLowerCase());
						searchParams.put(field.getName(), values);
					}
				}
				for(TextField<String> field : textBoxFields){
					String value = field.getValue();
					if(value != null && !value.equals("")){
						ArrayList<String> values = new ArrayList<String>();
						values.add("\"" + value + "\"");
						searchParams.put(field.getName(), values);
					}
				}
				break;
			case TEXT :
				for(TextField<String> field : textAreaFields){
					String value = field.getValue();
					if(value != null && !value.equals("")){
						ArrayList<String> values = new ArrayList<String>();
						values.add("\"" + value + "\"");
						searchParams.put(field.getName(), values);
					}
				}
				break;
			default :
		}
		
		return searchParams;
	}
	
	public HashMap<SearchParamType, String> getDQL() {
		HashMap<SearchParamType, String> dql = new HashMap<SearchParamType, String>();
		dql.put(SearchParamType.NON_TEXT, TbitsSearchPanel.getDQL(this.getSearchParams(SearchParamType.NON_TEXT)));
		dql.put(SearchParamType.TEXT, TbitsSearchPanel.getDQL(this.getSearchParams(SearchParamType.TEXT)));
		return dql;
	}

	
	public void resetToDefault() {
		checkFields = new ArrayList<SimpleComboBox<String>>();
		multiValueFields = new ArrayList<UserPicker>();
		textAreaFields = new ArrayList<TextField<String>>();
		textBoxFields = new ArrayList<TextField<String>>();
		this.removeAll();
		
		for(BAField baField : fields){
			if(baField.isCanViewInBA() && baField.isCanSearch())
				createField(baField);
		}
		
		BAField baField = new BAFieldCheckBox();
		
		baField.setName(READ_UNREAD_FIELD_FILTER);
		baField.setDisplayName("Read/Unread");
		createField(baField);
		
		this.layout();
	}
}
