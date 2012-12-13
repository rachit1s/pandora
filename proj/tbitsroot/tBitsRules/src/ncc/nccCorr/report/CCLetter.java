package nccCorr.report;

import java.util.Hashtable;

import nccCorr.others.CorresConstants;

import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class CCLetter implements IReportParamPlugin {

	public String getName() {
		return null;
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		
		String ccs = coob.getAsString(CorresConstants.CCLetterFieldName);
		if( null == ccs )
			return "" ;
		else 
		{
			return ccs.replaceAll(";", "<br />");
		}
	}
}
