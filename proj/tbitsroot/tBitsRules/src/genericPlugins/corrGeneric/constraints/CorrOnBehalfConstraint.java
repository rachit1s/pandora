package corrGeneric.constraints;

import static corrGeneric.com.tbitsGlobal.server.util.Utility.fdn;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.LoggerFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.MoreThanOneLoggerAllowed;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.MoreThanOneLoggerAllowed_No;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.ProtFollowOnBehalf;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.ProtFollowOnBehalf_Yes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

import corrGeneric.com.tbitsGlobal.server.interfaces.ICorrConstraintPlugin;
import corrGeneric.com.tbitsGlobal.server.managers.OnBehalfManager;
import corrGeneric.com.tbitsGlobal.server.managers.ProtocolOptionsManager;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class CorrOnBehalfConstraint implements ICorrConstraintPlugin 
{
	private void checkOnBehalfConstraint(CorrObject coob) throws CorrException 
	{
		if(null == coob.getUserMapUsers() || coob.getUserMapUsers().size() == 0)
			throw new CorrException("The value in field " + fdn(coob.getBa(), coob.getFieldNameMap().get(LoggerFieldName).getBaFieldName()) + " cannot be empty.");

		if( null == coob.getOptionMap() || coob.getOptionMap().get(MoreThanOneLoggerAllowed) == null || coob.getOptionMap().get(MoreThanOneLoggerAllowed).equals(MoreThanOneLoggerAllowed_No))
			if( coob.getUserMapUsers().size() > 1 )
				throw new CorrException("More than one user not allowed in the field " + fdn(coob.getBa(),coob.getFieldNameMap().get(LoggerFieldName).getBaFieldName()));

		HashSet<String> allowedLoggers = new HashSet<String>();
		ArrayList<OnBehalfEntry> map = OnBehalfManager.lookupOnBehalfList(coob.getBa().getSystemPrefix(), coob.getLoginUser().getUserLogin());
		if( null == map )
			throw new CorrException("You("+ coob.getLoginUser().getUserLogin() + ") are not allowed to log on behalf of anyone for ba : " + coob.getBa().getSystemPrefix());
		// get all the possible loggers
		for( OnBehalfEntry ob : map)
		{
			if(( null == ob.getType1() && null == coob.getOnBehalfType1() ) ||( ob.getType1() != null && coob.getOnBehalfType1() != null && ob.getType1().equals(coob.getOnBehalfType1().getName())) )
			{
				if(( null == ob.getType2() && null == coob.getOnBehalfType2() ) ||( ob.getType2() != null && coob.getOnBehalfType2() != null && ob.getType2().equals(coob.getOnBehalfType2().getName())) )
				{
					if(( null == ob.getType3() && null == coob.getOnBehalfType3() ) ||( ob.getType3() != null && coob.getOnBehalfType3() != null && ob.getType3().equals(coob.getOnBehalfType3().getName())) )
					{
						allowedLoggers.add(ob.getOnBehalfUser());
					}
				}
			}
		}
		
		User firstLogger = coob.getUserMapUsers().get(0);
		
		if( ! allowedLoggers.contains(firstLogger.getUserLogin()) )
			throw new CorrException("You (" + coob.getLoginUser().getUserLogin() + ") are not allowed to log on behalf of " + firstLogger.getUserLogin());

	}

	public void execute(Hashtable<String, Object> params) throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(ICorrConstraintPlugin.CORROBJECT);
		
//		FieldNameEntry disableProtFne = FieldNameManager.lookupFieldNameEntry(coob.getBa().getSystemPrefix(), GenericParams.DisableProtocolFieldName);
		
		if( null != coob.getDisableProtocol() )
		{
			Type disValue = coob.getDisableProtocol();
			if(disValue.getName().equalsIgnoreCase(GenericParams.DisableProtocol_True))
			{
					return;							
			}
		}
		
		ProtocolOptionEntry followOnBehalf = ProtocolOptionsManager.lookupProtocolEntry(coob.getBa().getSystemPrefix(), ProtFollowOnBehalf);
		if( null == followOnBehalf || followOnBehalf.getValue() == null || followOnBehalf.getValue().equalsIgnoreCase(ProtFollowOnBehalf_Yes))
		{
			checkOnBehalfConstraint(coob);
		}
	}

	public String getName() {
		return "To check onBehalfOf constraint on corr ba's. First checks if the " +
		ProtFollowOnBehalf + " is set to " + ProtFollowOnBehalf_Yes + ". The if atleast one user is present in " + LoggerFieldName + ".\n " +
				"Then check if " + MoreThanOneLoggerAllowed + " constraint. And the values in " + OnBehalfEntry.TableName + " table.";
	}

	public double getOrder() {
		return 1;
	}

}
