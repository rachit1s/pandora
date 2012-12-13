package transmittal.com.tbitsGlobal.server.cache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.ObjectExistsException;

/**
 * Interface for Cache's to be used in the Transmittal Plugin
 * Each new Cache has to implement this and register/add itself 
 * to the cache manager
 * @author devashish
 *
 */
public interface ITrnCache {
	/**
	 * Register a new cache with the existing cachemanager
	 * @param cache: Cache to be registered
	 * @throws IllegalStateException : 
	 * @throws ObjectExistsException : If the cache already exists
	 * @throws CacheException		 : If there was an error creating the cache	
	 */
	public void registerCache();
}
