package commons.com.tbitsGlobal.utils.client.UIContext;

/**
 * 
 * @author sourabh
 * 
 * A generic chainable context object that can have keys and values to be passed on to child widgets
 */
public interface UIContext {
	/**
	 * @param <X> Class of value
	 * @param key
	 * @param clazz
	 * @return Value for a given key
	 */
	public <X> X getValue(String key, Class<X> clazz);
	
	/**
	 * Set value for a given key
	 * @param <X>
	 * @param key
	 * @param value
	 */
	public <X> void setValue(String key, X value);
	
	/**
	 * @return The Parent context
	 */
	public UIContext getParentContext();
	
	/**
	 * Resets the context object
	 */
	public void resetContext();
	
	/**
	 * @param key
	 * @return True is this key exists
	 */
	public boolean hasKey(String key);
}
