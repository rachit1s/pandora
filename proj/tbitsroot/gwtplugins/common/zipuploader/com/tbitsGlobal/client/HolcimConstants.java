package zipuploader.com.tbitsGlobal.client;

import com.google.gwt.core.client.GWT;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;

public class HolcimConstants {

	

	public static final HolcimJagServiceAsync dbService = GWT.create(HolcimJagService.class);

	//public static HashMap<Integer, TbitsTreeRequestData> requests = new HashMap<Integer, TbitsTreeRequestData>();

//	public static HashMap<String, String> approval = new HashMap<String, String>();

//	public static ArrayList<TrnEditableColumns> editableCoumns = new ArrayList<TrnEditableColumns>();
//	public static List<TbitsTreeRequestData> requestList = new ArrayList<TbitsTreeRequestData>();

	public static FieldCache fieldCache = CacheRepository.getInstance()
			.getCache(FieldCache.class);
 

}
