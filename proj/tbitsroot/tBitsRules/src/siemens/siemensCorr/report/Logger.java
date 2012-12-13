package siemensCorr.report;

import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.domain.User;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
public class Logger implements IReportParamPlugin {

	public String getName() {
		// TODO Auto-generated method stub
		return "Logger";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException {
		Connection con = (Connection) params.get(IReportParamPlugin.CONNECTION);
		CorrObject co = (CorrObject) params.get(IReportParamPlugin.CORROBJECT);
		
		User logger = co.getUserMapUsers().get(0); 
		return logger.getDisplayName() + "<br />" + logger.getDesignation();
	}

}
