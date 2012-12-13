package commons.com.tbitsGlobal.utils.client.cache;

import java.util.HashMap;

/**
 * 
 * @author sourabh
 * 
 * A repository class for all caches
 */
@SuppressWarnings("unchecked")
public class CacheRepository {
	private HashMap<Class<? extends AbstractCache>, AbstractCache> map;
	private static CacheRepository repository;
	
	private CacheRepository(){
		map = new HashMap<Class<? extends AbstractCache>, AbstractCache>();
	}
	
	public static CacheRepository getInstance(){
		if(repository == null)
			repository = new CacheRepository();
		return repository;
	}
	
	public <T extends AbstractCache> void registerCache(Class<T> type, T cache){
		if(!map.containsKey(type))
			map.put(type, cache);
	}
	
	public <T extends AbstractCache> T getCache(Class<T> type){
		if(map.containsKey(type))
			return (T) map.get(type);
		else return null;
	}
}
