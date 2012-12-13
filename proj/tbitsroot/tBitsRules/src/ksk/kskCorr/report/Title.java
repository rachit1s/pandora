package kskCorr.report;

import java.util.Hashtable;

import transbit.tbits.domain.Field;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class Title implements IReportParamPlugin {

	public String getName() {
		return "Title";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		return coob.getAsString(Field.SUBJECT);
	}

}
