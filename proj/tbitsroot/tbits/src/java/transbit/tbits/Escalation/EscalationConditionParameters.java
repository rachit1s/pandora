package transbit.tbits.Escalation;

import java.io.Serializable;

public class EscalationConditionParameters implements Serializable {
	
	private String Name;
	private String value;
	
	public String getName() {
		return Name;
	}
	public String getValue() {
		return value;
	}
	public void setName(String name) {
		Name = name;
	}
	public void setValue(String value) {
		this.value = value;
	}
	

}
