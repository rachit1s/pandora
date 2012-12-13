package lntCorr.report;

import java.util.Hashtable;

import transbit.tbits.domain.Field;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
public class Protocol implements IReportParamPlugin {

	public String getName() {
		// TODO Auto-generated method stub
		return "returns the category_id";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject co = (CorrObject) params.get(CORROBJECT);
		return co.getAsString(Field.CATEGORY);
	}

}
