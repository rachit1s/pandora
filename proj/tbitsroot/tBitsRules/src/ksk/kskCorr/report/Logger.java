package kskCorr.report;

import java.util.Hashtable;

import kskCorr.others.GenReportHelper;

import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class Logger implements IReportParamPlugin 
{

	public String getName() {
		return "The name of Logger";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		if( null == coob.getUserMapUsers() || coob.getUserMapUsers().size() == 0 )
			return "" ;
		
		return GenReportHelper.getName(coob.getUserMapUsers().get(0));
	}

}
