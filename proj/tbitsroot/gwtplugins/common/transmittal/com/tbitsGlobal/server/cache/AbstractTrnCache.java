package transmittal.com.tbitsGlobal.server.cache;

import java.util.HashMap;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

import net.sf.ehcache.CacheException;

import net.sf.ehcache.Element;
import net.sf.ehcache.ObjectExistsException;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import net.sf.ehcache.writer.CacheWriter;


/**
 * Abstract class for all the cache's for transmittal plugin.
 * Any type of cache can be defined and added to the central cache manager which will ensure that
 * all the futher transactions for the registered data will occur through the cache.
 * @author devashish
 *
 */
public abstract class AbstractTrnCache <T extends TbitsModelData> implements ITrnCache {
	

	
	private SelfPopulatingCache selfPopulatingCache = null;
	protected TrnCache trnCache = null;
	
	/**
	 * Create a cache with the given configuration. This also registers the cache
	 * with the cache manager
	 * @param cacheConfig - Map of configuration values
	 */
	public void createCache(HashMap<String, String> cacheConfig){
		this.trnCache = new TrnCache(cacheConfig);
		registerCache();
	}

	/**
	 * Register the Cache with the cache manager for this particular module. All the caches 
	 * will register themselves with the manager.
	 */
	public void registerCache() throws IllegalStateException, ObjectExistsException, CacheException {
		TrnCacheManager.getInstance().registerCache(this.trnCache.getCache());
	}
	
	
	/**
	 * Register the Reader for the cache. This will be called when a cache miss occurs to fetch 
	 * the data.
	 * @param reader - class which implements CacheEntryFactory 
	 */
	public void registerCacheReader(CacheEntryFactory reader){
		this.selfPopulatingCache = new SelfPopulatingCache(trnCache.getCache(), reader);	
	}
	
	
	/**
	 * Register the Writer for the cache. This will be called if any updates take place on the 
	 * objects in the cache.
	 * @param writer - class which implements CacheWriter
	 */
	public void registerCacheWriter(CacheWriter writer){
		trnCache.getCache().registerCacheWriter(writer);
	}
	
	
	/**
	 * Get the value from the cache corresponding to the specified key
	 * @param key
	 * @return object - result object. Cast it into the desired data structure
	 */
	public Object getValue(T key){
		Element element = this.selfPopulatingCache.get(key);
		return element.getObjectValue();
	}

}