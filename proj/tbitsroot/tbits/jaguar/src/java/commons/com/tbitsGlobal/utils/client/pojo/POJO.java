package commons.com.tbitsGlobal.utils.client.pojo;

import java.io.Serializable;

/**
 * 
 * @author sourabh
 *
 * @param <T> Data Type
 * 
 * A serializable wrapper for all kinds of data that a request object can have
 */
public abstract class POJO<T> implements Serializable, Comparable<POJO<T>>{
	private static final long serialVersionUID = 1L;
	
	protected T value;
	
	public POJO() {
		
	}
	
	public POJO(T value) {
		assert value != null : "Value of a POJO can not be null";
		this.value = value;
	}
	
	public T getValue(){
		return this.value;
	};
	
	public void setValue(T value){
		this.value = value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof POJO){
			return ((POJO) obj).getValue().equals(value);
		}
		return super.equals(obj);
	}
	
	public abstract String toString();
	public abstract POJO<T> clone();
}
