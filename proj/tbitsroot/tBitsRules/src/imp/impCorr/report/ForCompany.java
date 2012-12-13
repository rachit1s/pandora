package impCorr.report;

import java.sql.Connection;
import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;

public class ForCompany implements IReportParamPlugin {

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException {
		Connection con = (Connection) params.get(IReportParamPlugin.CONNECTION);
		CorrObject co = (CorrObject) params.get(IReportParamPlugin.CORROBJECT);
		
		return co.getUserMapUsers().get(0).getFullFirmName() ;
	}

}
