package lntCorr.report;

import java.util.Hashtable;

import transbit.tbits.domain.Field;
import transbit.tbits.domain.Type;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
public class ToDepartment implements IReportParamPlugin
{

	public String getName() {
		return "return display name of to_department";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException {
		try
		{
			String fieldName = "to_department";
			CorrObject co = (CorrObject) params.get(CORROBJECT);
			
			String toDep = co.getAsString( fieldName );
			Field field = Field.lookupBySystemIdAndFieldName(co.getBa().getSystemId(), fieldName);
			
			if( null != toDep && null != field )
			{
				Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), fieldName, toDep);
				if( null != type )
					return type.getDisplayName() ;
			}
			
			return "";
		}
		catch(Exception te)
		{
			throw new CorrException(te.getMessage());
		}
	}
	
}
