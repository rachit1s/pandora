package corrGeneric.com.tbitsGlobal.server.cache;

import java.util.ArrayList;

import corrGeneric.com.tbitsGlobal.shared.domain.UserMapEntry;
import corrGeneric.com.tbitsGlobal.shared.key.UserMapKey;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import static corrGeneric.com.tbitsGlobal.server.managers.UserMapManager.*;

public class UserMapCache extends AbstractLRUCache<UserMapKey, ArrayList<UserMapEntry>> {

	public UserMapCache(int capacity, int windowSize) {
		super(capacity, windowSize);
	}

	@Override
	protected ArrayList<UserMapEntry> search(UserMapKey t) throws CorrException 
	{
		return getUserMapFromDB(t);
	}
}
