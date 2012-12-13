package corrGeneric.com.tbitsGlobal.server.cache;

import java.util.ArrayList;

import corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry;
import corrGeneric.com.tbitsGlobal.shared.key.OnBehalfKey;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import static corrGeneric.com.tbitsGlobal.server.managers.OnBehalfManager.*;

public class OnBehalfTableCache extends AbstractLRUCache<OnBehalfKey, ArrayList<OnBehalfEntry>>
{
	public OnBehalfTableCache(int capacity, int windowSize) {
		super(capacity, windowSize);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ArrayList<OnBehalfEntry> search(OnBehalfKey key) throws CorrException {
		return getAllFromDB(key);
	}
}
