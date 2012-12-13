/**
 * 
 */
package commons.com.tbitsGlobal.utils.client.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.form.ListModelPropertyEditor;

/**
 * ModelPropertyEditor implementation, provides method for using 
 * template as displayProperty
 * <p>
 * Works with ComboBox
 * @author dheeru
 *
 */
public class ModelPropertyEditorExtended<D extends ModelData> extends ListModelPropertyEditor<D> {

	/**
	 * To set the display template
	 * <p>"{" + key + "}" will be replaced by model.get(key) in the displayValue
	 * and display property won't be necessary
	 */
	protected String template;
	/**
	 * Use this if display property is not unique
	 */
	protected String hiddenProperty;

	protected String separator = "`";

	protected boolean needsSeparator = false;

	public void setseparator(String separator) {
		this.separator = separator;
	}

	public String getseparator() {
		return separator;
	}
	/**
	 * If needsSeparator is set to true separator will be put around
	 * all values replaced for keys in template
	 * <p> If you are setting it to false and want to use convertStringValue(String)
	 * method, maintain template such that respective values in model data
	 * must not contain 'trimmed substrings' separating two consecutive keys 
	 * of the template
	 * <p>For Example templates like '{key}\t{another_key}' will not work
	 * with convertStringvalue(String) method if needsSeparator is set to false
	 * <p> defaults to false </p>
	 */
	public void setNeedsSeparator(boolean needsSeparator) {
		this.needsSeparator = needsSeparator;
	}

	public boolean getNeedsSeparator() {
		return needsSeparator;
	}

	public void setTemplate(String template) {
		validateTemplate(template);
		this.template = template;
		this.setDisplayProperty(template.substring(template.indexOf("{") + 1, template.indexOf("}", template.indexOf("{") + 1)));
	}

	public String getTemplate() {
		return this.template;
	}

	public String getHiddenProperty() {
		return this.hiddenProperty;
	}

	public void setHiddenProperty(String hiddenProperty) {
		this.hiddenProperty = hiddenProperty;
	}

	public D convertStringValue(String value) {
		assert models != null : "List Store must be set";
		if (value == null || value.trim().equals(""))
			return null;
		assert (this.displayProperty != null && !this.displayProperty.trim().equals("")) || (this.template != null && !this.template.trim().equals("")) :
			"At least one of displayProperty and template must be set";
		D data = null;
		if (template == null || template.trim().equals("")) {
			data = searchModel(models, this.displayProperty, (Object) value);
		} else {
			HashMap<String, String> map = getDisplayPropertyMap(value);
			List<D> models = findModels(map);
			if (models.size() > 0)
				data = models.get(0);
		}
		return data;
	}

	public D convertStringValue(String value, String hiddenValue) {
		if (value == null || value.trim().equals(""))
			return null;
		assert (this.displayProperty != null && !this.displayProperty.trim().equals("")) || (this.template != null && !this.template.trim().equals("")) :
			"At least one of displayProperty and template must be set";
		D data = null;
		if (template == null || template.trim().equals("")) {
			ArrayList<D> modelList = (ArrayList<D>) searchModels(models, this.displayProperty, (Object) value);
			if (modelList.size() >0) {
				data = filterUsingValueField(hiddenValue, modelList);
			}
		} else {
			HashMap<String, String> map = getDisplayPropertyMap(value);
			List<D> models = findModels(map);
			if (models.size() > 0) {
				data = filterUsingValueField(hiddenValue, models);
			}
		}
		if (data != null) {
			return data;
		}
		return null;
	}

	public String getStringValue(D value) {
		assert (this.displayProperty != null && !this.displayProperty.equals("")) || (this.template != null && !this.template.equals("")) :
			"At least one of displayProperty and template must be set";
		if (value == null || value.getProperties().size() == 0)
			return null;
		String returnValue = "";
		if (template == null || template.trim().equals("")) {
			if (value.get(this.displayProperty) == null)
				throw new IllegalArgumentException("ModelData D must contain displayProperty");
			returnValue = value.get(this.displayProperty);
			return returnValue;
		} else {
			returnValue = this.template;
			Iterator<String> it = value.getProperties().keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (value.getProperties().get(key) == null || value.getProperties().get(key).toString().trim().equals(""))
					continue;
				if (needsSeparator)
					returnValue = returnValue.replaceAll("\\{" + key + "}",this.separator + value.getProperties().get(key).toString() + this.separator);
				else 
					returnValue = returnValue.replaceAll("\\{" + key + "}",value.getProperties().get(key).toString());
			}
			return returnValue;
		}
	}

	public String getHiddenStringValue(D value) {
		if (value == null)
			return null;
		if (this.hiddenProperty == null || this.hiddenProperty.equals(""))
			return null;
		if (value.get(this.hiddenProperty) == null)
			throw new IllegalArgumentException("ModelData D must contain displayProperty");
		String returnValue = value.get(this.hiddenProperty);
		return returnValue;
	}


	private D filterUsingValueField(String hiddenValue, List<D> models) {
		if (models == null)
			return null;
		if (this.hiddenProperty == null || this.hiddenProperty.equals(""))
			return (D) models.get(0);
		for (D data : models) {
			if (data.get(this.hiddenProperty).equals(hiddenValue))
				return data;
		}
		return (D) models.get(0);
	}

	private void validateTemplate(String template){
		if (template == null || template.trim().equals(""))
			throw new IllegalArgumentException("Template must be non empty string");
		int index = 0;
		boolean atleastOneKey = false;
		while (template.indexOf("{", index) != -1) {
			atleastOneKey = true;
			if (!(template.indexOf("}", index) != -1))
				throw new IllegalArgumentException(" '{' must always appear " +
				"in pair with '}' and inside must be a key of model data of the generic ListStore");
			index = template.indexOf("}", template.indexOf("{", index));
		} //TODO : also check if trimmed separator substring are non empty when needsSeparator is false
		if (!atleastOneKey)
			throw new IllegalArgumentException("Useless template, must contain atleast one key");
	}

	private HashMap<String, String> getDisplayPropertyMap(String str) {
		HashMap<String, String> map = new HashMap<String, String>();
		int index = 0;
		int strIndex = 0;
		if (needsSeparator) {
			while (this.template.indexOf("{", index) != -1) {
				int begIndex = this.template.indexOf("{", index);
				int closIndex = this.template.indexOf("}", begIndex);
				String key = this.template.substring(begIndex + 1, closIndex);
				int strBegin = str.indexOf("'", strIndex);
				int strEnd = str.indexOf("'", strBegin + 1);
				String val = "";
				try {
					val = str.substring(strBegin + 1, strEnd).trim();
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				} 
				map.put(key, val);
				index = closIndex;
				strIndex = strEnd + 1;
			}
		} else {
			while (this.template.indexOf("{", index) != -1) {
				int begIndex = this.template.indexOf("{", index);
				int closIndex = this.template.indexOf("}", begIndex);
				String key = this.template.substring(begIndex + 1, closIndex);
				String beginSeparator = this.template.substring(index, begIndex);
				int temp = this.template.indexOf("{", closIndex + 1);
				String endSeparator = this.template.substring(closIndex + 1, temp == -1 ? this.template.length() : temp);
				int strBegin = str.indexOf(beginSeparator.trim(), strIndex);
				int strEnd;
				if (endSeparator.trim().equals("")) {
					strEnd = str.length();
				} else {
					strEnd = str.indexOf(endSeparator.trim(), strBegin + 1);
				}
				index = closIndex + 1;
				strIndex = strEnd;
				String val = "";
				if (strEnd == -1) {
					continue;
				} //TODO : handle this if block
				try {
					val = str.substring(strBegin + beginSeparator.trim().length(), strEnd).trim();
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
				map.put(key, val);
			}
		}
		return map;
	}


	private List<D> findModels(HashMap<String, String> map) {
		if (map == null || map.size() == 0)
			return null;
		List<D> filtered = new ArrayList<D>();
		String testKey = map.keySet().iterator().next();
		for (D data : searchModels(models, testKey, (Object) map.get(testKey))){
			boolean b = false;
			for (String str : map.keySet()) {
				if (!data.get(str).toString().trim().equals(map.get(str).toString().trim())) {
					b = true;
					break;
				}
			}
			if (!b)
				filtered.add(data);
		}
		return filtered;
	}

	private List<D> searchModels(List<D> list, String testKey, Object object) {
		List<D> models = new ArrayList<D>();
		for (D data : list) {
			if (data.get(testKey).toString().trim().equals(object.toString().trim())) {
				models.add(data);
			}
		}
		return models;
	}


	private D searchModel(List<D> list, String testKey, Object object) {
		for (D data : list) {
			if (data.get(testKey).toString().trim().equals(object.toString().trim()))
				return data;
		}
		return null;
	}

}
