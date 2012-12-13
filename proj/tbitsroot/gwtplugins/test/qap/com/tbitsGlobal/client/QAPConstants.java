package qap.com.tbitsGlobal.client;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;

public class QAPConstants {

	public static QapServiceAsync dbService = GWT.create(QapService.class);
	public static FieldCache fieldCache = CacheRepository.getInstance()
			.getCache(FieldCache.class);
	public static ArrayList<TbitsTreeRequestData> requestData = new ArrayList<TbitsTreeRequestData>();
}
