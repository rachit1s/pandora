package corrGeneric.report;

import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.domain.Field;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;

public class Subject implements IReportParamPlugin{

	public String getName() {
		return "return's the subject of the request.";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		Connection con = (Connection) params.get(IReportParamPlugin.CONNECTION);
		CorrObject co = (CorrObject) params.get(IReportParamPlugin.CORROBJECT);
		
		return co.getAsString(Field.SUBJECT);
	}

}
