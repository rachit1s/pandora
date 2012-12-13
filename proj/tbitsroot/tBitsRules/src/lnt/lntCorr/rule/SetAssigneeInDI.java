package lntCorr.rule;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;

import lntCorr.others.LnTConst;

import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;

public class SetAssigneeInDI implements IRule 
{
	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorrCustom");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		if( Source == TBitsConstants.SOURCE_EMAIL )
		{
			return new RuleResult(true,"Rule not applicable for Emails.", true);
		}
		
		String appBAs = PropertiesHandler.getProperty(LnTConst.DIBaList);
		
		if( null == appBAs )
		{
			LOG.info("Property not found : property_name = " + LnTConst.DIBaList + " in tbits_properties.");
			return new RuleResult(true,"Property not found : property_name = " + LnTConst.DIBaList + " in tbits_properties.",true);
		}
					
		ArrayList<String> validBAs = Utility.splitToArrayList(appBAs);
		
		String fromFieldName = "CorrLogger";
		
		String toFieldName = "assignee_ids";
		
		if( null == ba || null == ba.getSystemPrefix() || !(validBAs.contains(ba.getSystemPrefix())))
			return new RuleResult(true,"Rule not applicable for this ba." , true );
		
		int index = validBAs.indexOf(ba.getSystemPrefix());
		if( index < 0 )
			return new RuleResult(false,"Rule with name : \"" + getName() + "\" failed.");
		
		try
		{
			String fromField = fromFieldName;
			String toField = toFieldName;
			Field fField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), toField);
			
			if( fField != null && null != toField )
			{
				Collection<RequestUser> fromUsers = (Collection<RequestUser>) currentRequest.getObject(fromField);
				Collection<RequestUser> currToUsers = (Collection<RequestUser>) currentRequest.getObject(toField);
				if( null != fromUsers && ((currToUsers != null && currToUsers.size() == 0) || currToUsers == null ))
				{
					ArrayList<RequestUser> toUsers = new ArrayList<RequestUser>();
					for( RequestUser ru : fromUsers )
					{
						RequestUser nru = new RequestUser(ru.getSystemId(),currentRequest.getRequestId(), ru.getUserId(), ru.getOrdering(), ru.getIsPrimary(), fField.getFieldId());
						toUsers.add(nru);
					}
					currentRequest.setObject(toField, toUsers);
					LOG.info("Adding following users from " + fromField + " to " + toField + ": " + toUsers);
				}
			}
		}
		catch(Exception e)
		{
			LOG.error(TBitsLogger.getStackTrace(e));
			return new RuleResult(false,"Rule with name \"" + getName() + "\" failed with message : " + e.getMessage(),false);
		}
		return new RuleResult(true,"Rule with name \"" + getName() + "\" successfull.",true);
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Sets the assignee's in to the Corr-Logger in configured business-areas";
	}

	public double getSequence() {
		return 3;
	}

}
