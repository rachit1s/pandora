package commons.com.tbitsGlobal.utils.client.bafield;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author sourabh
 * 
 * Class for bit type fields
 */
public class BAFieldCheckBox extends BAField implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<Boolean> values;
	private Boolean value;
	private boolean defaultValue;
	
	public BAFieldCheckBox() {
		super();
		setDisplaySize(0.33);
	}

	public void setValue(Boolean value) {
		this.value = value;
	}

	public Boolean getValue() {
		return value;
	}

	public List<Boolean> getValues() {
		return values;
	}

	public void setDefaultValue(boolean defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean getDefaultValue() {
		return defaultValue;
	}
}
