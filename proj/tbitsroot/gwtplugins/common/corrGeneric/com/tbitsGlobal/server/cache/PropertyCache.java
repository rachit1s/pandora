package corrGeneric.com.tbitsGlobal.server.cache;

import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import static corrGeneric.com.tbitsGlobal.server.managers.PropertyManager.*;

public class PropertyCache extends AbstractCache<String, PropertyEntry> 
{
	@Override
	protected PropertyEntry search(String propertyName) throws CorrException {
		return getFromDB(propertyName);
	}
}
