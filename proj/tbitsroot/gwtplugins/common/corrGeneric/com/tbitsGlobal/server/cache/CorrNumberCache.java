package corrGeneric.com.tbitsGlobal.server.cache;

import java.util.HashMap;

import corrGeneric.com.tbitsGlobal.server.managers.CorrNumberManager;
import corrGeneric.com.tbitsGlobal.shared.domain.CorrNumberEntry;
import corrGeneric.com.tbitsGlobal.shared.key.CorrNumberKey;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class CorrNumberCache extends AbstractLRUCache<String,HashMap<CorrNumberKey,CorrNumberEntry>>{

	public CorrNumberCache(int capacity, int windowSize) {
		super(capacity, windowSize);
	}

	@Override
	protected HashMap<CorrNumberKey,CorrNumberEntry> search(String sysPrefix) throws CorrException {
		return CorrNumberManager.getCorrNumberFromDB(sysPrefix);
	}
}
