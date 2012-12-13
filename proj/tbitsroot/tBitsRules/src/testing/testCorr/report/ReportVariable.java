package testCorr.report;

import java.util.Hashtable;

import transbit.tbits.exception.TBitsException;
import corrGeneric.com.tbitsGlobal.server.domain.ReportParamEntry;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;

public class ReportVariable implements IReportParamPlugin {

	public String getName() {
		return "returns <param_name>_value";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws TBitsException 
	{
		ReportParamEntry rpe = (ReportParamEntry) params.get(REPORTPARAMENTRY);
		return rpe.getParamName() + "_value" ;
	}
}
