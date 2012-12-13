package commons.com.tbitsGlobal.utils.client.cache;

import java.util.Collection;
import java.util.HashMap;

import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;

/**
 * 
 * Abstract class for any cache. 
 * All the data that has to be kept stored for a particular session in the memory shall be brought by a cache.
 */

public abstract class AbstractCache<K,V> extends BaseTbitsObservable implements ICache<K,V> {
	/**
	 * Hashmap that holds the data for the cache
	 */
	protected HashMap<K, V> cache;
	
	public AbstractCache() {
		super();
		
		this.attach();
	}
	
	
	/**
	 *  Finds the object for a specified key
	 * 
	 *  @param key
	 *  @return object
	 */
	public V getObject(K key) {
		if(cache.containsKey(key))
			return cache.get(key);
		return null;
	}
	
	/**
	 *  return the map for the cache
	 * 
	 *  @return map of key and values
	 *  @deprecated : It is very risky to expose the HashMap of abstract class.
	 */
	public HashMap<K, V> getMap(){
		return cache;
	}
	
	/**
	 * @return returns the values stored in the cache.
	 */
	public Collection<V> getValues()
	{
		if( null == cache )
			return null ;
		else return cache.values() ;
	}
	
	/**
	 *  checks if the cache has been initialized or not
	 *  If not, it has to be inilialized using refreshCache method in {@link CacheRepository}
	 * 
	 *  @return true if initialized
	 */
	public boolean isInitialized(){
		if(cache == null)
			return false;
		return true;
	}
	
	/**
	 *  Retrieves data from the server again and removes the previous data
	 */
	protected void refresh(){
		if(cache == null)
			cache = new HashMap<K, V>();
		else cache.clear();
		this.getFromServer();
	}
	
	/**
	 *  Retrieves data from the server
	 */
	protected abstract void getFromServer();
}
