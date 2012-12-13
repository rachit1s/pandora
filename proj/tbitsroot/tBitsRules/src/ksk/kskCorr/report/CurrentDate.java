package kskCorr.report;

import java.util.Hashtable;

import transbit.tbits.common.Timestamp;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class CurrentDate implements IReportParamPlugin {

	public String getName() {
		return "returns the current date " + new Timestamp().toCustomFormat("yyyy-MM-dd");
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		return new Timestamp().toCustomFormat("yyyy-MM-dd");
	}

}
