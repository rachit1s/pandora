package corrGeneric.constraints;

import static corrGeneric.com.tbitsGlobal.server.util.Utility.fdn;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.GenerationAgencyFieldName;

import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import corrGeneric.com.tbitsGlobal.server.interfaces.ICorrConstraintPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class CorrGenerationAgencyConstraint implements ICorrConstraintPlugin {

	public static TBitsLogger LOG = TBitsLogger.getLogger("corrGeneric.constraints");
	private void checkGenerationAgencyConstraint(CorrObject coob) throws CorrException 
	{
		FieldNameEntry genFieldEntry = coob.getFieldNameMap().get(GenerationAgencyFieldName);
		if(null == genFieldEntry )
		{
			Utility.LOG.info("Cannot find " + GenerationAgencyFieldName + " mapping for the ba field. So ignoring any checks on this field.");
			return;
		}
		
		User firstLogger = coob.getUserMapUsers().get(0);
		if( null == coob.getGenerationAgency() || !coob.getGenerationAgency().getName().equals(firstLogger.getLocation()))
			throw new CorrException("The field " + fdn(coob.getBa(),genFieldEntry.getBaFieldName()) + " was not set properly.");
	}
	
	public void execute(Hashtable<String, Object> params) throws CorrException 
	{
		Connection con = (Connection) params.get(ICorrConstraintPlugin.CONNECTION);
		CorrObject coob = (CorrObject) params.get(ICorrConstraintPlugin.CORROBJECT);
		
//		FieldNameEntry disableProtFne = FieldNameManager.lookupFieldNameEntry(coob.getBa().getSystemPrefix(), GenericParams.DisableProtocolFieldName);
		
		if( coob.getDisableProtocol() != null )
		{
			Type disValue = coob.getDisableProtocol();
			if(disValue.getName().equalsIgnoreCase(GenericParams.DisableProtocol_True))
			{
					return;							
			}
		}
		
		checkGenerationAgencyConstraint(coob);
	}

	public String getName() {
		return "To check the Agency Constraint on the Corr-Protocol. It ignores the constraint if " + GenerationAgencyFieldName + " is not configured for this ba.";
	}

	public double getOrder() {
		return 5;
	}

}
