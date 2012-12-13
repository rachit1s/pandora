package impCorr.report;

import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;

public class Project implements IReportParamPlugin {

	public String getName() {
		return "returns the description of BA for the project variable.";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject co = (CorrObject) params.get(CORROBJECT);
		return co.getBa().getDescription();
	}

}
