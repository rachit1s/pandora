package corrGeneric.com.tbitsGlobal.server.cache;

import corrGeneric.com.tbitsGlobal.shared.domain.ReportNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import static corrGeneric.com.tbitsGlobal.server.managers.ReportNameManager.*;

public class ReportNameMapCache extends AbstractLRUCache<Integer, ReportNameEntry> 
{

	public ReportNameMapCache(int capacity, int windowSize) 
	{
		super(capacity, windowSize);
	}

	@Override
	protected ReportNameEntry search(Integer t) throws CorrException {
		return getReportNameMapFromDB(t);
	}
	
}
