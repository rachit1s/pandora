package commons.com.tbitsGlobal.utils.client.pojo;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;

public class POJOException extends POJO<TbitsExceptionClient>{
	
	public POJOException() {
		super();
	}
	
	public POJOException(TbitsExceptionClient e) {
		this.value = e;
	}
	
	@Override
	public POJO<TbitsExceptionClient> clone() {
		return new POJOException(this.value);
	}

	@Override
	public String toString() {
		return this.value.getMessage();
	}

	public int compareTo(TbitsExceptionClient o) {
		return o.equals(this.value) ? 0 : 1;
	}

	@Override
	public int compareTo(POJO<TbitsExceptionClient> o) {
		return 0;
	}

}
