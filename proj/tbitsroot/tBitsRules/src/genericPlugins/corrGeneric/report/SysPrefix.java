package corrGeneric.report;

import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;

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
