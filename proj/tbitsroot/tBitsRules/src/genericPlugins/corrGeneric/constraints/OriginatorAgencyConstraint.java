package corrGeneric.constraints;

import static corrGeneric.com.tbitsGlobal.server.util.Utility.*;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.*;

import java.util.Hashtable;

import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

import corrGeneric.com.tbitsGlobal.server.interfaces.ICorrConstraintPlugin;
import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class OriginatorAgencyConstraint implements ICorrConstraintPlugin 
{
	private void checkOriginatorConstraint(CorrObject coob) throws CorrException 
	{
		FieldNameEntry origTypefe = coob.getFieldNameMap().get(OriginatorFieldName);
		User first = coob.getUserMapUsers().get(0);
		if( null == origTypefe )
		{
			Utility.LOG.info("No field mapping found for " + OriginatorFieldName + ". So skipping the Originator Constraint.");
			return ;
		}
		
		if( coob.getType() == CorrObject.TypeAddRequest )
		{
			if(! coob.getOriginator().getName().equals(first.getLocation())) 
					throw new CorrException("The field " + fdn(coob.getBa(),origTypefe.getBaFieldName()) + " was not set properly.");
		}
		else
		{
			if( ! coob.getOriginator().getName().equals(coob.getPrevRequest().get(origTypefe.getBaFieldName())))
				throw new CorrException("The field " + fdn(coob.getBa(),origTypefe.getBaFieldName()) + " was not set properly.");
		}
	}

	public void execute(Hashtable<String, Object> params) throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(ICorrConstraintPlugin.CORROBJECT);
		
		Type disValue = coob.getDisableProtocol();
		if( null != disValue && disValue.getName().equalsIgnoreCase(GenericParams.DisableProtocol_True))
		{
				return;							
		}
		
		checkOriginatorConstraint(coob);
	}

	public String getName() {
		return "checks if the " + GenericParams.OriginatorFieldName + " has been configured for the ba.\n" +
				"If yes then in the add-request it checks if this field maps to the firmCode of the first user in " + LoggerFieldName +".\n" +
						"And in update request it ensures the the originator is same as in the last request state." ;
	}

	public double getOrder() {
		return 3;
	}
}
