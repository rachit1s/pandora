package corrGeneric.report;

import java.util.Hashtable;

import transbit.tbits.common.TBitsLogger;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class RefNo implements IReportParamPlugin {

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public String getName() {
		return "generates ref No." ;
	}

	public String getReportParam(Hashtable<String, Object> params) throws CorrException 
	{
		CorrObject co = (CorrObject) params.get(IReportParamPlugin.CORROBJECT);
		
		FieldNameEntry corrNofne = FieldNameManager.lookupFieldNameEntry(co.getBa().getSystemPrefix(), GenericParams.CorrespondenceNumberFieldName);
		if( null == corrNofne || null == corrNofne.getBaFieldName() )
			throw new CorrException("The field " + GenericParams.CorrespondenceNumberFieldName + " was not properly set for the ba : " + co.getBa().getSystemPrefix() );
		
		return co.getAsString(corrNofne.getBaFieldName());
	}

}
