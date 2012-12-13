package corrGeneric.report;

import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import transbit.tbits.webapps.WebUtil;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;

public class TbitsBaseUrl implements IReportParamPlugin {

	public String getName() 
	{
		return "returns property tbits_base_url";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		return WebUtil.getNearestPath("");
	}

}
