/**
 * 
 */
package commons.com.tbitsGlobal.utils.client.widgets;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.PropertyEditor;

/**
 * @author dheeru
 *
 * @param <D>	
 */
public class ListStorePropertyEditor<D extends ModelData> implements PropertyEditor<ListStore<D>> {

	public ModelPropertyEditorExtended<D> modelPropertyEditor;
	protected String displayProperty;
	protected String hiddenProperty;

	private String DELIMETER = ",";

	public ListStorePropertyEditor() {
		super();
		modelPropertyEditor = new ModelPropertyEditorExtended<D>();
	}

	public void setList(List<D> models){
		this.modelPropertyEditor.setList(models);
	}

	public void setDelimeter(String delimeter) {
		this.DELIMETER = delimeter;
	}

	public void setHiddenProperty(String hiddenProperty) {
		this.hiddenProperty = hiddenProperty;
		this.modelPropertyEditor.setHiddenProperty(hiddenProperty);
	}

	public void setDisplayProperty(String displayField){
		this.displayProperty = displayField;
		this.modelPropertyEditor.setDisplayProperty(displayField);
	}

	public String getDisplayProperty(){
		return this.displayProperty;
	}

	public String getHiddenProperty(){
		return this.hiddenProperty;
	}

	public String getDelimeter(){
		return this.DELIMETER;
	}
	
	public ModelPropertyEditorExtended<D> getModelPropertyEditor() {
		return this.modelPropertyEditor;
	}
	
	public void setModelpropertyEditor(ModelPropertyEditorExtended<D> modelPropertyEditor) {
		this.modelPropertyEditor = modelPropertyEditor;
	}

	/**
	 *Convert method parse and generate Strings using this template,
	 *	"{"key"}" is replaced by model.get(key);
	 *	Must only be used when generic type is ListStore
	 * @param template
	 */
	public void setTemplate(String template) {
		this.modelPropertyEditor.setTemplate(template);
	}

	public String getTemplate() {
		return this.modelPropertyEditor.template;
	}

	/* (non-Javadoc)
	 * @see com.extjs.gxt.ui.client.widget.form.PropertyEditor#convertStringValue(java.lang.String)
	 */
	public ListStore<D> convertStringValue(String value) {
		if (value == null || value.trim().equals(""))
			return null;
		String values[] = value.split(DELIMETER);
		ArrayList<String> trimmed = new ArrayList<String>();
		for (String v : values){
			if (v != null && !v.trim().equals("")){
				trimmed.add(v.trim());
			}
		}
		if (trimmed.size() == 0)
			return null;
		ListStore<D> returnValue = new ListStore<D>();
		assert (this.displayProperty != null && !this.displayProperty.trim().equals("")) || (this.modelPropertyEditor.template != null && !this.modelPropertyEditor.template.trim().equals("")) :
			"At least one of displayProperty and template must be set";
		for (String str : trimmed){
			D data = modelPropertyEditor.convertStringValue(str);
			if (data != null) {
				returnValue.add(data);
			}
		}
		return returnValue;
	}

	public ListStore<D> convertStringValueUsingHiddenValue(String value, String hiddenValue) {
		if (value == null || value.trim().equals(""))
			return null;
		String values[] = value.split(DELIMETER);
		String hiddenValues[] = hiddenValue.split(DELIMETER);
		ArrayList<String> trimmed = new ArrayList<String>();
		ArrayList<String> hidden = new ArrayList<String>();
		for (int i = 0; i < values.length; i++){
			if (values[i] != null && values[i].trim() != ""){
				trimmed.add(values[i].trim());
				if (hiddenValues.length > i) {
					hidden.add(hiddenValues[i]);
				} 
			}
		}
		if (trimmed.size() == 0)
			return null;
		ListStore<D> returnValue = new ListStore<D>();
		assert (this.displayProperty != null && !this.displayProperty.trim().equals("")) || (this.modelPropertyEditor.template != null && !this.modelPropertyEditor.template.trim().equals("")) :
			"At least one of displayProperty and template must be set";
		for (String str : trimmed){
			D data = this.modelPropertyEditor.convertStringValue(str, hidden.get(trimmed.indexOf(str)));
			if (data != null) {
				returnValue.add(data);
			}
		}
		return returnValue;
	}

	public String getStringValue(ListStore<D> value) {
		assert (this.displayProperty != null && !this.displayProperty.equals("")) || (this.modelPropertyEditor.template != null && !this.modelPropertyEditor.template.equals("")) :
			"At least one of displayProperty and template must be set";
		if (value == null)
			return null;
		String returnValue = "";
		for (D model : value.getModels()){
			if (model.get(this.displayProperty) == null)
				throw new IllegalArgumentException("ModelData D must contain displayProperty");
			returnValue += this.modelPropertyEditor.getStringValue(model) + DELIMETER;
		}
		return returnValue;
	}

	/**
	 * Useful if the Display Property is not unique,
	 * use returned string along with the displayPropertyStringValue 
	 * to get ListStore back in case of non unique Display Property
	 *  
	 * @param value	any Object
	 * @return	property String corresponding to Hidden Property
	 */
	public String getHiddenStringValue(ListStore<D> value){
		assert this.hiddenProperty != null : "Hidden Property must be set";
		if (value == null)
			return null;
		String returnValue = "";
		for (D model : value.getModels()){
			returnValue += this.modelPropertyEditor.getHiddenStringValue(model) + DELIMETER;
		}
		return returnValue;
	}
}
