package commons.com.tbitsGlobal.utils.client.pojo;

public class POJOBoolean extends POJO<Boolean> {
	
	public POJOBoolean() {
		super();
	}
	
	public POJOBoolean(Boolean value) {
		super(value);
	}

	@Override
	public POJO<Boolean> clone() {
		return new POJOBoolean(new Boolean(this.value));
	}

	@Override
	public String toString() {
		return this.value ? "true" : "false";
	}

	public int compareTo(Boolean o) {
		return o.compareTo(this.value);
	}

	@Override
	public int compareTo(POJO<Boolean> o) {
		return o.getValue() == this.value ? 0 : 1;
	}

}
