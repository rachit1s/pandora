package corrGeneric.com.tbitsGlobal.server.cache;

import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.shared.domain.BaFieldEntry;
import corrGeneric.com.tbitsGlobal.shared.key.BaFieldKey;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import static corrGeneric.com.tbitsGlobal.server.managers.BaFieldManager.*;

public class BaFieldMapCache extends AbstractLRUCache<BaFieldKey, Hashtable<String,BaFieldEntry>> 
{

	public BaFieldMapCache(int capacity, int windowSize) {
		super(capacity, windowSize);
	}

	@Override
	protected Hashtable<String, BaFieldEntry> search(BaFieldKey t)
			throws CorrException {
		return getBaFieldMapFromDB(t);
	}
	
}
