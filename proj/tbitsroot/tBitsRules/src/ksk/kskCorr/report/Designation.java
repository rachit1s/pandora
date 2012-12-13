package kskCorr.report;

import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class Designation implements IReportParamPlugin 
{

	public String getName() {
		return "returns Designation of Logger";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		if( null == coob.getUserMapUsers() || coob.getUserMapUsers().size() == 0 )
			return "" ;
		
		return coob.getUserMapUsers().get(0).getDesignation();
	}
}
