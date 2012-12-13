package nccCorr.report;
import java.sql.Connection;
import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;


public class Description implements IReportParamPlugin {

	public String getName() {
		return "the description for the pdf.";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException {
		Connection con = (Connection) params.get(IReportParamPlugin.CONNECTION);
		CorrObject co = (CorrObject) params.get(IReportParamPlugin.CORROBJECT);
		
		FieldNameEntry descFieldName = co.getFieldNameMap().get(GenericParams.CorrDescriptionFieldName);
		if(null == descFieldName || null == descFieldName.getBaFieldName() )
			throw new CorrException("The field " + GenericParams.CorrDescriptionFieldName + " was not properly configured for the ba : " + co.getBa().getSystemPrefix());
		
		return co.getAsString(descFieldName.getBaFieldName());
	}

}
