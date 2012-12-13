package corrGeneric.report;

import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;

public class OtherReferences implements IReportParamPlugin {

	public String getName() {
		return "The otherReferences after replaceing ; with <br />";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		// TODO Auto-generated method stub  otherReference
		CorrObject co = (CorrObject) params.get(CORROBJECT);
		String referFN = "Reference";
		String references = co.getAsString(referFN);
		if( null == references )
			return "" ;
		else return references.replace(";", "<br />");
	}
}
