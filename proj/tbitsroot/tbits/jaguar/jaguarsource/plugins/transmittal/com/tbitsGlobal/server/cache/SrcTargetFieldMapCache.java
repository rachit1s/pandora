package transmittal.com.tbitsGlobal.server.cache;

import java.util.HashMap;
import java.util.Hashtable;

import transmittal.com.tbitsGlobal.server.cacheObjects.SrcTargetFieldMapObject;

/**
 * @author devashish
 *
 */
public class SrcTargetFieldMapCache extends AbstractTrnCache {

	
	public static Integer SRC_TARGET_FIELD_MAP_CACHE_SIZE = 100;
	public static String SRC_TARGET_FIELD_MAP_CACHE_NAME = "srcTargetFieldMap";

	public static SrcTargetFieldMapCache instance = null;
	
	
	public synchronized static SrcTargetFieldMapCache getInstance(){
		if(null == instance){
			instance = new SrcTargetFieldMapCache();
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	private SrcTargetFieldMapCache(){
		HashMap<String, String> cacheConfig = new HashMap<String, String>();
		cacheConfig.put(TrnCache.CACHE_NAME, SRC_TARGET_FIELD_MAP_CACHE_NAME);
		cacheConfig.put(TrnCache.CACHE_SIZE, Integer.toString(SRC_TARGET_FIELD_MAP_CACHE_SIZE));
		cacheConfig.put(TrnCache.IS_ETERNAL, "false");
		cacheConfig.put(TrnCache.OVERFLOW_TO_DISK, "false");
		cacheConfig.put(TrnCache.TIME_TO_IDLE_SECONDS, "500");
		cacheConfig.put(TrnCache.TIME_TO_LIVE_SECONDS, "1000");
		
		this.createCache(cacheConfig);
		this.registerCacheReader(new SrcTargetFieldMapReader());
		this.registerCacheWriter(new SrcTargetFieldMapWriter());
	}

	
	/**
	 * Get the source-destination field mapping for the specified source-destination business areas
	 * @param trnProcessId
	 * @param dcrSysId
	 * @param targetSysId
	 * @return Map of properties 
	 */
	public Hashtable<String, String> getSrcTargetFieldMap(SrcTargetFieldMapObject keyObject){
		
		Hashtable<String, String> resultMap = new Hashtable<String, String>();
		resultMap.putAll((Hashtable<String, String>) this.getValue(keyObject));		
		
		return resultMap;
	}

}
