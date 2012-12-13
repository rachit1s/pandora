package corrGeneric.com.tbitsGlobal.server.cache;

import static corrGeneric.com.tbitsGlobal.server.managers.ProtocolOptionsManager.*;

import java.util.ArrayList;

import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class ProtocolOptionNameCache extends AbstractLRUCache<String,ArrayList<ProtocolOptionEntry>> {

	public ProtocolOptionNameCache(int capacity, int windowSize) {
		super(capacity, windowSize);
	}

	@Override
	protected ArrayList<ProtocolOptionEntry> search(String optionName)
			throws CorrException 
	{
		return getProtocolOptionNamesFromDB(optionName);
	}

}
