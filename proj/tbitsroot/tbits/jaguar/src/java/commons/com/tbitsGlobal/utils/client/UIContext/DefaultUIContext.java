package commons.com.tbitsGlobal.utils.client.UIContext;

import java.util.HashMap;

import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * 
 * @author sutta
 * 
 * Default implementation of {@link UIContext}
 */
public class DefaultUIContext implements UIContext{
	
	private HashMap<String, Object> contextMap;
	private UIContext parentContext;
	private boolean isReset;
	
	public DefaultUIContext() {
		this.isReset = false;
	}
	
	public DefaultUIContext(UIContext parentContext){
		this();
		this.parentContext = parentContext;
	}
	
	public UIContext getParentContext(){
		if(!checkState())
			return null;
		if(parentContext != null)
			return parentContext;
		return null;
	}

	public <X> X getValue(String key, Class<X> clazz){
		if(!checkState())
			return null;
		if(contextMap == null && parentContext == null)
			return null;
		if(contextMap != null && contextMap.containsKey(key)){
			try{
				return (X) contextMap.get(key);
			}catch(ClassCastException e){
				Log.warn("Unable to cast in DefaultUIContext", e);
				return null;
			}
		}
		if(parentContext != null && parentContext.hasKey(key))
			return (X) parentContext.getValue(key, clazz);
		return null;
	}

	public void resetContext() {
		this.parentContext = null;
		if(contextMap != null){
			contextMap.clear();
			contextMap = null;
		}
		this.isReset = true;
	}

	public <X> void setValue(String key, X value){
		if(!checkState())
			return ;
		if(key == null || key.trim().length() == 0)
			throw new NullPointerException("Key can not be a null");
		if(value == null)
			throw new NullPointerException("Value can not be a null");
		if(contextMap == null)
			contextMap = new HashMap<String, Object>();
		contextMap.put(key, value);
	}
	
	private boolean checkState(){
		if(isReset)
			return false;
		return true;
	}

	public boolean hasKey(String key){
		if(!checkState())
			return false;
		if(contextMap == null && parentContext == null)
			return false;
		if(contextMap != null && contextMap.containsKey(key))
			return true;
		if(parentContext != null && parentContext != null)
			return (parentContext.getValue(key, Object.class) != null);
		return false;
	}
}
