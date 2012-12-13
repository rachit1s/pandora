package commons.com.tbitsGlobal.utils.client;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.js.JsonConverter;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;
import commons.com.tbitsGlobal.utils.client.pojo.POJOBoolean;
import commons.com.tbitsGlobal.utils.client.pojo.POJODate;
import commons.com.tbitsGlobal.utils.client.pojo.POJOException;
import commons.com.tbitsGlobal.utils.client.pojo.POJOInt;
import commons.com.tbitsGlobal.utils.client.pojo.POJOString;

/**
 * 
 * @author sourabh
 * 
 * A extention of {@link BaseTreeModel} to carry a request object along with its subrequests
 */
public class TbitsTreeRequestData extends BaseTreeModel implements Serializable, IFixedFields {
	private static final long serialVersionUID = 1L;
	
	POJO dummy_obj;
	
	private boolean read = false;
	private HashMap<String, Integer> perms;
	
	public TbitsTreeRequestData() {
		super();
		
		perms = new HashMap<String, Integer>();
		
		this.setRequestId(0);
	}
	
	public TbitsTreeRequestData(int sysId, int requestId) {
		this();
		this.setRequestId(requestId);
		this.setSystemId(sysId);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <X> X get(String property) {
		try{
			POJO obj =  super.get(property);
			if(obj == null || obj.toString().equals("null"))
				return null;
			return (X) obj.getValue();
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * @param property
	 * @return String representation of the value on the given property. Blank String if there is no value set
	 */
	public String getAsString(String property){
		try{
			POJO obj =  super.get(property);
			if(obj == null || obj.toString().equals("null"))
				return "";
			return obj.toString();
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * @param property
	 * @return {@link POJO} value set for the given property. NULL if not present
	 */
	@SuppressWarnings("unchecked")
	public POJO getAsPOJO(String property){
		try{
			POJO obj =  (POJO)super.get(property);
			return obj;
		}catch(Exception e){
			return null;
		}
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> map = new HashMap<String, Object>();
		for(String k : getPropertyNames()){
			Object obj = this.get(k);
			map.put(k, obj);
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <X> X set(String property, X value) {
		if(value instanceof Integer){
			X old = (X) super.get(property);
			super.set(property, new POJOInt(Integer.parseInt(value.toString())));
			return old ;
		}else if(value instanceof Boolean){
			X old = (X) super.get(property);
			super.set(property, new POJOBoolean((Boolean)value));
			return old ;
		}else if(value instanceof String){
			X old = (X) super.get(property);
			super.set(property, new POJOString((String) value));
			return old;
		}else if(value instanceof Date){
			X old = (X) super.get(property);
			super.set(property, new POJODate((Date) value));
			return old ;
		}else if(value instanceof List){
			try{
				super.set(property, new POJOAttachment((List<FileClient>)value));
			}catch(ClassCastException cce){
				return null;
			}
			X old = (X) super.get(property);
			return old ;
		}else if(value instanceof POJO){
			X old = (X) super.set(property, value);
			return old;
		}
		return null;
	}
	
	/**
	 * Finds a sub request based on request id 
	 * @param requestId
	 * @return
	 */
	public TbitsTreeRequestData findByRequestIdInChildren(int requestId){
		return findByRequestIdInChildren(requestId, this);
	}
	
	private TbitsTreeRequestData findByRequestIdInChildren(int requestId, TbitsTreeRequestData data){
		for(ModelData d : data.getChildren()){
			if(((TbitsTreeRequestData) d).getRequestId() == requestId)
				return (TbitsTreeRequestData) d;
			else{
				TbitsTreeRequestData temp = findByRequestIdInChildren(requestId, (TbitsTreeRequestData) d);
				if(temp != null)
					return temp;
			}
		}
		return null;
	}
	
	
	public void setRequestId(int myRequestId) {
		this.set(REQUEST, new POJOInt(myRequestId));
	}

	public int getRequestId() {
		Object o = this.get(REQUEST);
		if(o == null)
			return 0;
		if(o instanceof String){
			try{
				return Integer.parseInt((String)o);
			}catch(Exception e){
				return 0;
			}
		}
		return (Integer)this.get(REQUEST);
	}
	
	public void setSystemId(int mySystemId) {
		this.set(BUSINESS_AREA, new POJOInt(mySystemId));
	}
	
	public int getSystemId() {
		Object o = this.get(BUSINESS_AREA);
		if(o == null)
			return 0;
		if(o instanceof String){
			try{
				return Integer.parseInt((String)o);
			}catch(Exception e){
				return 0;
			}
		}
		return (Integer)this.get(BUSINESS_AREA);
	}
	
	public void setMaxActionId(int maxActionId) {
		this.set(MAX_ACTION_ID, new POJOInt(maxActionId));
	}

	public int getMaxActionId() {
		Object o = this.get(MAX_ACTION_ID);
		if(o == null)
			return 0;
		if(o instanceof String)
			try{
				return Integer.parseInt((String)o);
			}catch(Exception e){
				return 0;
			}
		return (Integer)this.get(MAX_ACTION_ID);
	}
	
	public void setError(TbitsExceptionClient e){
		this.set("E", new POJOException(e));
	}
	
	public TbitsExceptionClient getError(){
		Object o = this.get("E");
		if(o instanceof TbitsExceptionClient)
			return (TbitsExceptionClient) o;
		return null;
	}
	
	public void removeError(){
		this.remove("E");
	}
	
	public TbitsTreeRequestData clone(){
		TbitsTreeRequestData data = new TbitsTreeRequestData();
		for(String s : this.getPropertyNames()){
			POJO obj = this.getAsPOJO(s);
			if(obj != null)
				data.set(s, obj.clone());
		}
		for(ModelData child : this.getChildren()){
			data.add(((TbitsTreeRequestData)child).clone());
		}
		return data;
	}
	
	public static TbitsTreeRequestData fromString(String str){
		Map<String, Object> map = JsonConverter.decode(str);
		TbitsTreeRequestData data = new TbitsTreeRequestData();
		data.setProperties(map);
		return data;
	}
	
	public boolean getRead(){		
		return this.read;	
	}
	
	public void setRead(boolean read){		
		this.read=read;
	}

	public void setPerms(HashMap<String, Integer> perms) {
		this.perms = perms;
	}

	public HashMap<String, Integer> getPerms() {
		return perms;
	}
}
