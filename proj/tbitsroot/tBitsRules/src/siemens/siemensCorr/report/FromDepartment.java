package siemensCorr.report;

import java.util.Hashtable;

import transbit.tbits.domain.Field;
import transbit.tbits.domain.Type;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
public class FromDepartment implements IReportParamPlugin {

	public String getName() {
		return "returns from_department";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		try
		{
			String fieldName = "from_department";
			CorrObject co = (CorrObject) params.get(CORROBJECT);
			
			String fromDep = co.getAsString( fieldName );
			Field field = Field.lookupBySystemIdAndFieldName(co.getBa().getSystemId(), fieldName);
			
			if( null != fromDep && null != field )
			{
				Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), fieldName, fromDep);
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
