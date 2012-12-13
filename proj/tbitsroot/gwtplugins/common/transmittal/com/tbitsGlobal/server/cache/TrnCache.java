package transmittal.com.tbitsGlobal.server.cache;

import java.util.HashMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Ehcache;

public class TrnCache{
	
	public Ehcache cache;
	
	public static String CACHE_NAME 			= "cacheName";
	public static String CACHE_SIZE				 = "maxElements";
	public static String OVERFLOW_TO_DISK		= "overflowToDisk";
	public static String IS_ETERNAL				= "isEternal";
	public static String TIME_TO_LIVE_SECONDS	= "ttlSeconds";
	public static String TIME_TO_IDLE_SECONDS	= "ttidleSeconds";
	
	public TrnCache(HashMap<String, String> cacheConfig){
		String cacheName 		= cacheConfig.get(CACHE_NAME);
		Integer cacheSize 		= Integer.valueOf(cacheConfig.get(CACHE_SIZE));
		Boolean overflowToDisk 	= Boolean.getBoolean(cacheConfig.get(OVERFLOW_TO_DISK));
		Boolean isEternal		= Boolean.getBoolean(cacheConfig.get(IS_ETERNAL));
		Integer ttlSeconds 		= Integer.valueOf(cacheConfig.get(TIME_TO_LIVE_SECONDS));
		Integer ttIdleSeconds	= Integer.valueOf(cacheConfig.get(TIME_TO_IDLE_SECONDS));
		
		cache = new Cache(cacheName, cacheSize, overflowToDisk, isEternal, ttlSeconds, ttIdleSeconds);
	}
	
	public Ehcache getCache(){
		return this.cache;
	}
}
