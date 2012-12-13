package impCorr.report;

import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;

public class PreviousThread implements IReportParamPlugin {

	public String getName() {
		return "send the value in previ";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject co = (CorrObject) params.get(CORROBJECT);
		
		String fieldName = "previous_thread";
		String prevThread = co.getAsString(fieldName);
		if( null == prevThread || prevThread.trim().equals(""))
			return 0 + "";
		try
		{
			Integer prevId = Integer.parseInt(prevThread);
			if( null == prevId )
				return 0 + "";
			else 
				return prevId + "";
		}
		catch(Exception e)
		{
			return 0  + "" ;
		}
	}

}
