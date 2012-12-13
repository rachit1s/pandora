package commons.com.tbitsGlobal.utils.client.pojo;

public class POJOString extends POJO<String>{
	
	public POJOString() {
		super();
	}
	
	public POJOString(String value) {
		super(value);
	}

	@Override
	public POJO<String> clone() {
		return new POJOString(new String(this.value));
	}

	@Override
	public String toString() {
		return this.getValue();
	}

	@Override
	public int compareTo(POJO<String> o) {
		return o.getValue().compareTo(this.value);
	}

}
