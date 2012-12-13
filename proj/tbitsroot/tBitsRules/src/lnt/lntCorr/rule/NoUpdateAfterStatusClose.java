package lntCorr.rule;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import lntCorr.others.LnTConst;

import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.managers.ProtocolOptionsManager;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

public class NoUpdateAfterStatusClose implements IRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		try
		{
			if( isAddRequest )
				return new RuleResult(true,"rule not applicable for add-request", true);
			
			String validBAs = PropertiesHandler.getProperty(LnTConst.DIBaList);
			
			if( null == validBAs )
			{
				LOG.info("Property not found : " + LnTConst.DIBaList + " in tbits_properties");
				return new RuleResult(true,"Property not found : " + LnTConst.DIBaList + " in tbits_properties",true); 
			}
			
			ArrayList<String> baList = Utility.splitToArrayList(validBAs, ",");
			
			if(!baList.contains(ba.getSystemPrefix()))
				return new RuleResult(true,"Rule not applicable for this ba.", true);
			
			if( null == FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.StatusFieldName) )
			{
				return new RuleResult(true,"Corr. The field status was not configured hence rule exiting.", true);
			}
			else
			{
				String status = oldRequest.get(FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.StatusFieldName).getBaFieldName());
				if( null != status && status.equals(GenericParams.StatusClosed))
				{
					return new RuleResult(false,"The status field was closed. Hence no more updates allowed.", true);
				}
			}
			
			return new RuleResult(true,"rule executed successfully.", true);
		}
		catch(Exception e)
		{
			LOG.error(e);
			return new RuleResult(false,e.getMessage(),false);
		}
	}

	public String getName() {
		return "If the field " + GenericParams.StatusFieldName + " is configured then no submissions after status is closed." ;
	}

	public double getSequence() {
		return 0;
	}

}
