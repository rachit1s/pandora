package corrGeneric.com.tbitsGlobal.server.cache;

import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import static corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager.*;

public class FieldNameCache extends AbstractLRUCache<String, Hashtable<String,FieldNameEntry>> 
{

	public FieldNameCache(int capacity, int windowSize) {
		super(capacity, windowSize);
	}

	@Override
	protected Hashtable<String, FieldNameEntry> search(String sysPrefix)
			throws CorrException {
		return getFieldNameMapFromDB(sysPrefix);
	}
}
