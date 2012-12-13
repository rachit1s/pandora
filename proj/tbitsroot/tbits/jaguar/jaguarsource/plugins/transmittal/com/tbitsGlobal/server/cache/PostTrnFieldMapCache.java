package transmittal.com.tbitsGlobal.server.cache;

import java.util.HashMap;
import java.util.Hashtable;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.exception.TBitsException;
import transmittal.com.tbitsGlobal.server.TransmittalProcess;
import transmittal.com.tbitsGlobal.server.cacheObjects.PostTrnFieldMapObject;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;


public class PostTrnFieldMapCache extends AbstractTrnCache {


	public static Integer POST_TRN_FIELD_MAP_CACHE_SIZE = 100;
	public static String POST_TRN_FIELD_MAP_CACHE_NAME = "postTrnFieldMapCache";
	
	public static String DTN_BA_ID 	= "dtnBAId";
	public static String TRN_PROCESS_ID = "trnProcessId";
	public static String DTN_SYS_ID	= "dtnSysId";
	public static String DTR_SYS_ID	= "dtrSysId";
	
	
	public static PostTrnFieldMapCache instance = null;
	
	public synchronized static PostTrnFieldMapCache getInstance(){
		if(null == instance)
			instance = new PostTrnFieldMapCache();
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	private PostTrnFieldMapCache(){
		HashMap<String, String> cacheConfig = new HashMap<String, String>();
		cacheConfig.put(TrnCache.CACHE_NAME, POST_TRN_FIELD_MAP_CACHE_NAME);
		cacheConfig.put(TrnCache.CACHE_SIZE, Integer.toString(POST_TRN_FIELD_MAP_CACHE_SIZE));
		cacheConfig.put(TrnCache.IS_ETERNAL, "false");
		cacheConfig.put(TrnCache.OVERFLOW_TO_DISK, "false");
		cacheConfig.put(TrnCache.TIME_TO_IDLE_SECONDS, "500");
		cacheConfig.put(TrnCache.TIME_TO_LIVE_SECONDS, "1000");
		
		this.createCache(cacheConfig);
		this.registerCacheReader(new PostTrnFieldMapReader());
		this.registerCacheWriter(new SrcTargetFieldMapWriter());
	}


	public void getPostTrnBusinessAreaFieldsAndValues(Hashtable<String, String> paramTable, BusinessArea ba, TransmittalProcess trnProcess) throws TBitsException{
		PostTrnFieldMapObject keyObj = new PostTrnFieldMapObject();
		keyObj.setDtnBAId(ba.getSystemId());
		keyObj.setTrnProcessId(trnProcess.getTrnProcessId());
		keyObj.setDtnSysId(trnProcess.getDtnSysId());
		keyObj.setDtrSysId(trnProcess.getDtrSysId());
		
		
		HashMap<String, String> keyMap = new HashMap<String, String>();
		keyMap.put(DTN_BA_ID, Integer.toString(ba.getSystemId()));
		keyMap.put(TRN_PROCESS_ID, Integer.toString(trnProcess.getTrnProcessId()));
		keyMap.put(DTN_SYS_ID, Integer.toString(trnProcess.getDtnSysId()));
		keyMap.put(DTR_SYS_ID, Integer.toString(trnProcess.getDtrSysId()));
		
		Hashtable<String, String> value = new Hashtable<String, String>();
		value.putAll((Hashtable<String, String>) this.getValue(keyObj));
		if(null != value)
			paramTable.putAll(value);
		else throw new TBitsException("could not get post transmittal business area fields.");
		
	}
}
