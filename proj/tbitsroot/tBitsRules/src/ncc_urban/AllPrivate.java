package ncc_urban;

import java.sql.Connection;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class AllPrivate implements IRule {

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		String sysPrefix=ba.getSystemPrefix();
		if(sysPrefix.equalsIgnoreCase("INTL"))
		{
			Boolean isPrivate=currentRequest.getIsPrivate();
			isPrivate=true;
			currentRequest.setIsPrivate(true);
		}
		
		return new RuleResult();
	}

	@Override
	public String getName() {
		
		return "AllPrivate";
	}

	@Override
	public double getSequence() {
		
		return 0;
	}

}
