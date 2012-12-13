package transmittal.com.tbitsGlobal.server.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.ObjectExistsException;

/**
 * This is a global manager for all the cache's defined for transmittal plugin
 * Any new cache instance must first register/add itself with this in order
 * to be activated.
 * @author devashish
 *
 */
public class TrnCacheManager {
	public static TrnCacheManager instance = null;
	private CacheManager trnCacheManager;
	
	public synchronized static TrnCacheManager getInstance(){
		if(null == instance)
			instance = new TrnCacheManager();
		return instance;
	}
	
	private TrnCacheManager() throws CacheException{
		trnCacheManager = CacheManager.create();
	}
	
	/**
	 * Register a new cache with the existing cache manager for 
	 * Transmittal plugin
	 * @param cache: Cache to be registered
	 * @throws IllegalStateException : 
	 * @throws ObjectExistsException : If the cache already exists
	 * @throws CacheException		 : If there was an error creating the cache	
	 */
	public void registerCache(Ehcache cache) throws IllegalStateException, ObjectExistsException, CacheException{
		this.trnCacheManager.addCache(cache);
	}
	
	/**
	 * Delete the cache from cache manager with specified name
	 * @param Name of the cache
	 * @throws IllegalStateException
	 * @throws ObjectExistsException
	 */
	public void deleteCache(String cacheName) throws IllegalStateException, ObjectExistsException{
		this.trnCacheManager.removeCache(cacheName);
	}

	/**
	 * Empty the cache with the specified name
	 * @param cacheName
	 * @throws CacheException
	 */
	public void emtpyCache(String cacheName) throws CacheException{
		this.trnCacheManager.clearAllStartingWith(cacheName);
	}

	
	/**
	 * Gets an EhCache
	 * @param cacheName
	 * @return
	 * @throws IllegalStateException - If the cache is not Status.STATUS_ALIVE
	 */
	public Ehcache getEhCache(String cacheName) throws IllegalStateException{
		return this.trnCacheManager.getEhcache(cacheName);
	}
	
	
}
