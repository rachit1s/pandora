package commons.com.tbitsGlobal.utils.client.cache;


/**
 * Interface to be implemented by all the cache classes
 * 
 */
public interface ICache<K,T> {
	/**
	 * @param key
	 * @return returns an object for given key.
	 */
	public T getObject(K key);
	
	/**
	 * Refreshes the cache
	 */
	public void onRefresh();
	
	/**
	 * @return True if initialized
	 */
	public boolean isInitialized();
}
