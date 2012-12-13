package commons.com.tbitsGlobal.utils.client.urlManager;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class HistoryToken extends TbitsModelData{
	
	public static int ADD		=	1;
	public static int REMOVE	=	2;
	
	public static String KEY		= "key";
	public static String VALUE 		= "value";
	public static String IS_FORCED	= "is_forced";
	
	public HistoryToken() {
		super();
	}
	
	public HistoryToken(String key, String value, boolean isForced) {
		this();
		this.setKey(key);
		this.setValue(value);
		this.setForced(isForced);
	}
	
	public String getKey() {
		if(this.get(KEY) == null)
			return null;
		return (String)this.get(KEY);
	}
	
	public void setKey(String key) {
		this.set(KEY, key);
	}
	
	public String getValue() {
		if(this.get(VALUE) == null)
			return null;
		return (String)this.get(VALUE);
	}
	
	public void setValue(String value) {
		this.set(VALUE, value);
	}
	
	public boolean isForced() {
		if(this.get(IS_FORCED) == null)
			return false;
		return (Boolean)this.get(IS_FORCED);
	}
	
	public void setForced(boolean isForced) {
		this.set(IS_FORCED, isForced);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof HistoryToken){
			HistoryToken token = (HistoryToken) obj;
			if(this.getKey().equals(token.getKey()) && this.getValue().equals(token.getValue()))
				return true;
		}
		return false;
	}
}
