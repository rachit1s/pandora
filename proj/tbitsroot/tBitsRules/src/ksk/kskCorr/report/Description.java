package kskCorr.report;

import java.util.Hashtable;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.Field;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class Description implements IReportParamPlugin {

	public String getName() {
		return "Description";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		
		FieldNameEntry decEntry = coob.getFieldNameMap().get(GenericParams.CorrDescriptionFieldName);
		
		if( null == decEntry )
			throw new CorrException(GenericParams.CorrDescriptionFieldName + " is not configured.");
		
		String decName = decEntry.getBaFieldName();
		Field decField = null;
		try {
			decField = Field.lookupBySystemIdAndFieldName(coob.getBa().getSystemId(), decName);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		
		if( null == decField )
			throw new CorrException("Field not found with name : " + decName + " configured for property : " + GenericParams.CorrDescriptionFieldName);
		
		if( null == coob.getAsString(decName))
			return "";
		else return coob.getAsString(decName);
	}
}
