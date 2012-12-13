package commons.com.tbitsGlobal.utils.client.pojo;

public class POJOInt extends POJO<Integer>{
	private static final long serialVersionUID = 1L;
	
	public POJOInt() {
		super();
	}
	
	public POJOInt(int intValue) {
		super(intValue);
	}

	@Override
	public POJO<Integer> clone() {
		return new POJOInt(this.value.intValue()) ;
	}

	@Override
	public String toString() {
		return this.value + "";
	}
	
	@Override
	public int compareTo(POJO<Integer> o) {
		if(o != null){
			if(o.getValue() < this.value)
				return 1;
			if(o.getValue() > this.value)
				return -1;
		}
		return 0;
	}
}
