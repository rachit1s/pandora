package lntTbits;

import java.sql.Connection;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class MarkSupportRequestsPrivate implements IRule 
{
	public static final String sysPrefix = "tbits"; 
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		if( null != ba && null != ba.getSystemPrefix() && ba.getSystemPrefix().equals(sysPrefix))
		{
			currentRequest.setObject("is_private", true);
			return new RuleResult(true,"Request Made Private.",true);
		}
		
		return new RuleResult(true,"Rule not applicable",true);
	}

	public String getName() {
		return "Marks all request in " + sysPrefix + " business areas as private.";
	}

	public double getSequence() {
		return 0;
	}

}
