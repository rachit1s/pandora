package lntCorr.report;

import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
public class SysPrefix implements IReportParamPlugin {

	public String getName() {
		return "returns the SysPrefix for the given ba.";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		return coob.getBa().getSystemPrefix();
	}

}
