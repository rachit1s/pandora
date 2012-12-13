package corrGeneric.com.tbitsGlobal.server.cache;

import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.shared.domain.ReportParamEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import static corrGeneric.com.tbitsGlobal.server.managers.ReportParamsManager.*;

public class ReportParamCache extends AbstractLRUCache<Integer, Hashtable<String,ReportParamEntry>> {

	public ReportParamCache(int capacity, int windowSize) {
		super(capacity, windowSize);
	}

	@Override
	protected Hashtable<String, ReportParamEntry> search(Integer reportId)
			throws CorrException 
	{
		return getReportParamMap(reportId);
	}

}
