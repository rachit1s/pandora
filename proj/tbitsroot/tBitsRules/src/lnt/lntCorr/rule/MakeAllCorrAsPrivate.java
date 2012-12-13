package lntCorr.rule;

import java.sql.Connection;
import java.util.ArrayList;

import corrGeneric.com.tbitsGlobal.server.managers.PropertyManager;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

public class MakeAllCorrAsPrivate implements IRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
//		PropertyEntry appBAs = null;
//		try 
//		{
//			appBAs = PropertyManager.getProperty(GenericParams.CommaSeparatedListOfApplicableBa);
//		} catch (TBitsException e1) {
//			e1.printStackTrace();
//		}
//		catch (Exception e1) {
//			e1.printStackTrace();
//		}
		try
		{
			String appBas = null;
			try
			{
				appBas = PropertiesHandler.getProperty(GenericParams.CommaSeparatedListOfApplicableBa);
			}
			catch(Exception iae)
			{
				LOG.error(TBitsLogger.getStackTrace(iae));
			}
			
			if( null == appBas || appBas.trim().equals("") )
			{
				LOG.info("Property not found : property_name = " + GenericParams.CommaSeparatedListOfApplicableBa + " in tbits_properties.");
				return new RuleResult(true, "No BA configured for Correspondence.", true );
			}
			
			ArrayList<String> sysPrefixes = Utility.splitToArrayList(appBas.trim());
			
			if( null == ba || null == ba.getSystemPrefix() || ! sysPrefixes.contains(ba.getSystemPrefix()))
				return new RuleResult(true,"Either the ba was null Or was not applicable for correspondence module.", true);
		
			String privateField = Field.IS_PRIVATE ;
			currentRequest.setObject(privateField,true);
			return new RuleResult(true,"The rule executed successfully making the private to true.", true);
		}
		catch(Exception e)
		{
			LOG.error(TBitsLogger.getStackTrace(e));
			LOG.error("The request will not be made private." + ba.getSystemPrefix() + "#" + currentRequest.getRequestId());
			return new RuleResult(true, "The request will not be made private." + ba.getSystemPrefix() + "#" + currentRequest.getRequestId(), false);
		}
	}

	public String getName() {
		return "Make all the correspondences as private ALWAYS";
	}

	public double getSequence() {
		return 0;
	}

}
