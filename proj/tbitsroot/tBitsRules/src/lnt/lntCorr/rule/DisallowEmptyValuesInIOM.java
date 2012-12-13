package lntCorr.rule;

import java.sql.Connection;

import corrGeneric.com.tbitsGlobal.server.util.Utility;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

public class DisallowEmptyValuesInIOM implements IRule {

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		if( Source == TBitsConstants.SOURCE_EMAIL )
			return new RuleResult(true, "Rule not applicable for request from email.", true);
		
		if( null == ba || null == ba.getSystemPrefix() || !ba.getSystemPrefix().trim().equalsIgnoreCase("IOM"))
		{
			return new RuleResult(true, "Rule not applicable for this ba." , true);
		}

		try
		{
			String fromDep = "from_department";
			String toDep = "to_department" ;
			
			String from = currentRequest.get(fromDep);
			String to = currentRequest.get(toDep);
			
			if( null == from )
				throw new TBitsException("Please select a value in " + Utility.fdn(ba,fromDep));
			if( null == to )
				throw new TBitsException("Please select a value in " + Utility.fdn(ba,toDep));
			
			Type fromType = null;
			try {
				 fromType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), fromDep, from);
			} catch (DatabaseException e1) {
				e1.printStackTrace();
			}
			
			Type toType = null;
			try {
				toType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), toDep, to);
			} catch (DatabaseException e1) {
				e1.printStackTrace();
			}
			
			if( null == fromType || fromType.getName().equalsIgnoreCase("null"))
				throw new TBitsException("Please select a value in " + Utility.fdn(ba,fromDep));
			if( null == toType || toType.getName().equalsIgnoreCase("null"))
				throw new TBitsException("Please select a value in " + Utility.fdn(ba,toDep));
			
			String fieldName = "status_id";
			String fieldValue = currentRequest.get(fieldName);
			if( null == fieldValue )
				throw new TBitsException("Please select a value in " + Utility.fdn(ba,fieldName));
			
			Type type = null;
			try {
				type = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), fieldName, fieldValue);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			
			if( null == type || type.getName().equalsIgnoreCase("null"))
				throw new TBitsException("Please select a value in " + Utility.fdn(ba,fieldName));
			
			return new RuleResult(true,"Rule executed successfully.",true);
		}
		catch(TBitsException te)
		{
			return new RuleResult(false,te.getDescription(),false);
		}
		catch(Exception e)
		{
			return new RuleResult(false,e.getMessage(),false);
		}
	}

	public String getName() {
		return "disallow empty values / - in from deparment to department and project field."; 
	}

	public double getSequence() 
	{
		return 0;
	}

}
