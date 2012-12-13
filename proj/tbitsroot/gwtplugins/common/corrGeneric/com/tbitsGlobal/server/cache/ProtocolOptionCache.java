package corrGeneric.com.tbitsGlobal.server.cache;

import java.util.Hashtable;

import static corrGeneric.com.tbitsGlobal.server.managers.ProtocolOptionsManager.*;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class ProtocolOptionCache extends AbstractLRUCache<String, Hashtable<String,ProtocolOptionEntry>> {

	public ProtocolOptionCache(int capacity, int windowSize) {
		super(capacity, windowSize);
	}

	@Override
	protected Hashtable<String, ProtocolOptionEntry> search(String sysPrefix)
			throws CorrException 
	{
		return getProtocolOptionsFromDB(sysPrefix);
	}

}
