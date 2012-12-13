package impCorr.report;

import java.util.Hashtable;

import transbit.tbits.domain.Field;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;

public class RID implements IReportParamPlugin {

	public String getName() {
		return "returns the request id " ;
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		String rid = coob.getAsString(Field.REQUEST);
		if( null == rid )
			rid = "0" ;
		
		return rid;
	}

}
