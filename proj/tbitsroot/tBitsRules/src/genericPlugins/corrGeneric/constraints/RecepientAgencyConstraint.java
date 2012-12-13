package corrGeneric.constraints;

import static corrGeneric.com.tbitsGlobal.server.util.Utility.fdn;
import static corrGeneric.com.tbitsGlobal.server.util.Utility.toUsers;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.RecepientAgencyFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.RecepientUserTypeFieldName;

import java.util.ArrayList;
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

public class RecepientAgencyConstraint implements ICorrConstraintPlugin {

	private void checkRecepientAgencyConstraint(CorrObject coob) throws CorrException 
	{
		FieldNameEntry recTypefe = coob.getFieldNameMap().get(RecepientAgencyFieldName);
		FieldNameEntry recUserfe = coob.getFieldNameMap().get(RecepientUserTypeFieldName);
		if( null == recTypefe || null == recUserfe )
		{
			Utility.LOG.info("No field mapping found for " + RecepientAgencyFieldName + " Or " + RecepientUserTypeFieldName + ". So not checking the RecepientAgency constraint.");
			return;
		}

		String recepientList = coob.getAsString(recUserfe.getBaFieldName());
		ArrayList<User> recepients = toUsers(recepientList);
		
		if( null == recepients|| recepients.size() == 0 || coob.getRecepientAgency() == null )
			throw new CorrException("The field : " + fdn( coob.getBa(), recTypefe.getBaFieldName()) + " Or " +  fdn(coob.getBa(),recUserfe.getBaFieldName())  + " was empty.");

		User first = recepients.get(0);
		
		if( !coob.getRecepientAgency().getName().equals(first.getLocation()))
			throw new CorrException("The field : " + fdn(coob.getBa(),recTypefe.getBaFieldName()) + " was not set properly.");
	}
	
	public void execute(Hashtable<String, Object> params) throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(ICorrConstraintPlugin.CORROBJECT);
		
		FieldNameEntry disableProtFne = FieldNameManager.lookupFieldNameEntry(coob.getBa().getSystemPrefix(), GenericParams.DisableProtocolFieldName);
		
		if( disableProtFne != null && disableProtFne.getBaFieldName() != null )
		{
			Type disValue = coob.getDisableProtocol();
			if( null != disValue && disValue.getName().equalsIgnoreCase("true"))
			{
					return;							
			}
		}
		
		checkRecepientAgencyConstraint(coob);
	}

	public String getName() {
		return "Checks if the " + RecepientAgencyFieldName + " and " + RecepientUserTypeFieldName + " are configured for the ba.\n" +
				"If yes then checks if the " + RecepientAgencyFieldName + " matches the firm_code of the first user in the " + RecepientUserTypeFieldName ;
	}

	public double getOrder() {
		// TODO Auto-generated method stub
		return 4;
	}

}
