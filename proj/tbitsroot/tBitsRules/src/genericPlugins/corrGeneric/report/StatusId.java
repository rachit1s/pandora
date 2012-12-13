package corrGeneric.report;

import java.util.Hashtable;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.Type;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;

public class StatusId implements IReportParamPlugin {

	public String getName() {
		return "returns the value of the field status_id";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject co = (CorrObject) params.get(CORROBJECT);
		

		String fieldName = "status_id";
		String fieldValue = co.getAsString(fieldName);
		if( null == fieldValue )
			throw new CorrException("Please select a value in " + Utility.fdn(co.getBa(),fieldName));
		
		Type type = null;
		try {
			type = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), fieldName, fieldValue);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		
		if( null == type )
			throw new CorrException("Please select a value in " + Utility.fdn(co.getBa(),fieldName));

		return type.getDescription() ;
	}

}
