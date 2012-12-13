package lntCorr.report;

import java.util.Hashtable;

import lntCorr.others.LnTConst;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;

public class CCLetter implements IReportParamPlugin {

	public String getName() {
		return null;
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		
		String ccs = coob.getAsString(LnTConst.CCLetterFieldName);
		if( null == ccs )
			return "" ;
		else 
		{
			return ccs.replaceAll(";", "<br />");
		}
	}
}
