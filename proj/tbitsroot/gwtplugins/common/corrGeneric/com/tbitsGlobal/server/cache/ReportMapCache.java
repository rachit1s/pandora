package corrGeneric.com.tbitsGlobal.server.cache;

import java.util.ArrayList;

import corrGeneric.com.tbitsGlobal.shared.domain.ReportEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import static corrGeneric.com.tbitsGlobal.server.managers.ReportManager.*;

public class ReportMapCache extends AbstractLRUCache<String, ArrayList<ReportEntry>> 
{

	public ReportMapCache(int capacity, int windowSize) {
		super(capacity, windowSize);
	}

	@Override
	protected ArrayList<ReportEntry> search(String sysPrefix) throws CorrException {
		return getReportMapFromDB(sysPrefix);
	}
	
}
