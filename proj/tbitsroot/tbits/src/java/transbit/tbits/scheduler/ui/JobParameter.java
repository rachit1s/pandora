package transbit.tbits.scheduler.ui;

import java.util.ArrayList;

public class JobParameter {
	private String name;
	private ParameterType type;
	private ArrayList values;
	private Object defaultValue;
	private boolean isMandatory;
	
	public JobParameter(){
		isMandatory = false;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setType(ParameterType type) {
		this.type = type;
	}

	public ParameterType getType() {
		return type;
	}

	public void setValues(ArrayList values) {
		this.values = values;
	}

	public ArrayList getValues() {
		return values;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setMandatory(boolean isMandatory) {
		this.isMandatory = isMandatory;
	}

	public boolean isMandatory() {
		return isMandatory;
	}
}
